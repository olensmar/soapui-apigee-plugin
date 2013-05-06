package com.smartbear.restplugin

import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.support.WadlImporter
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.support.UISupport
import com.eviware.soapui.support.action.support.AbstractSoapUIAction
import com.eviware.x.dialogs.Worker
import com.eviware.x.dialogs.XProgressMonitor
import com.eviware.x.form.XFormDialog
import com.eviware.x.form.support.ADialogBuilder
import groovy.json.JsonSlurper

import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.*

class AddAPIFromApigeeAction extends AbstractSoapUIAction<WsdlProject> {

    private static final String CONSOLES_LISTING_URL = "http://api.apigee.com/v1/consoles.json"
    private static final String APIGEE_LISTING_URL_PROPERTY = "com.smartbear.soapui.apigee.listingurl"

    private XFormDialog dialog = null
    private def apiEntries = new TreeMap()
    private JList apiList;
    private def selectedEntry = null

    public AddAPIFromApigeeAction() {
        super("Add API from Apigee", "Imports an API in the Apigee directory");
    }

    void perform(WsdlProject project, Object o) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(AddFromApigeeForm.class);
            def dlg = UISupport.dialogs.createProgressDialog("Loading API Directory...", 0, "", false)
            dlg.run(new Worker.WorkerAdapter() {
                Object construct(XProgressMonitor monitor) {
                    initEntries()
                }
            })

            def cnt = apiEntries.size()
            dialog.setValue(AddFromApigeeForm.STATUS, "$cnt APIs loaded")
            apiList = new JList(apiEntries.keySet().toArray())

            dialog.getFormField(AddFromApigeeForm.NAME).setProperty("component", new JScrollPane(apiList))
            dialog.getFormField(AddFromApigeeForm.NAME).setProperty("preferredSize", new Dimension(300, 100))

            apiList.addListSelectionListener({ ListSelectionEvent e ->
                Object entry = apiList.selectedValue
                if (apiEntries.containsKey(entry)) {
                    def apiEntry = apiEntries[entry]

                    dialog.setValue(AddFromApigeeForm.DESCRIPTION, buildDescription(apiEntry))

                    selectedEntry = apiEntry
                }
            } as ListSelectionListener)
        }

        dialog.setValue(AddFromApigeeForm.DESCRIPTION, "")
        if (dialog.show()) {
            if (selectedEntry != null) {
                RestService restService = (RestService) project
                        .addNewInterface(selectedEntry.displayName, RestServiceFactory.REST_TYPE);
                UISupport.select(restService);
                try {
                    def dlg = UISupport.dialogs.createProgressDialog("Importing WADL...", 0, "", false)
                    dlg.run(new Worker.WorkerAdapter() {
                        Object construct(XProgressMonitor monitor) {
                            new WadlImporter(restService).initFromWadl(selectedEntry.wadlUrl);
                            ApigeeUtils.postProcessService( restService )
                        }
                    })
                }
                catch (Exception e) {
                    UISupport.showErrorMessage(e);
                }
            } else
                UISupport.showErrorMessage("Missing WSDL to import")
        }
    }

    def buildDescription(def entry) {
        "<html>$entry.description<hr/><a href\"$entry.develeropSiteUrl\">$entry.developerSiteUrl</a></html>"
    }

    def initEntries() {
        initEntries(System.getProperty(APIGEE_LISTING_URL_PROPERTY, CONSOLES_LISTING_URL))
    }

    def initEntries(String url) {

        def doc = loadJsonDoc(url)

        doc.console.each { it
            apiEntries[it.displayName] = it
        }

        apiEntries.sort{ a,b -> a.key.compareTo(b.key)}

        apiEntries
    }

    private loadJsonDoc(String url) {
        Console.println("loading Apigee consoles from $url")
        def payload = new URL(url).text
        def slurper = new JsonSlurper()
        return slurper.parseText(payload)
    }
}
