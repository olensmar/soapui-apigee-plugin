package com.smartbear.restplugin

import com.eviware.soapui.impl.rest.RestServiceFactory
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.rest.support.WadlImporter
import com.eviware.soapui.impl.wsdl.WsdlProject

class AddFromApigeeTestCase extends GroovyTestCase {
    void testGetConsoles() {
        def action = new AddAPIFromApigeeAction()
        def entries = action.initEntries("file:src/test/resources/consoles.json")

        assertFalse(entries.isEmpty())

        entries.values().each {
            it
            assertTrue(it.wadlUrl.endsWith("?format=wadl"))
        }
    }

    void testApigeeUtils()
    {
        def project = new WsdlProject()
        def restService = project.addNewInterface("Test", RestServiceFactory.REST_TYPE)

        new WadlImporter(restService).initFromWadl("http://api.apigee.com/v1/consoles/apigee/apidescription?format=wadl");

        assertEquals( restService.getResourceList().size(), 2 )

        ApigeeUtils.extractTemplateParametersFromHostName( restService )

        def param = restService.resourceList[1].params.getProperty( "appname")
        assertNotNull( param )
        assertEquals( RestParamsPropertyHolder.ParameterStyle.TEMPLATE, param.style )

        ApigeeUtils.fixMultiResources( restService );

        assertEquals( "http://api.apigee.com", restService.resourceList[0].childResourceList[0].restMethodList[0].requestList[0].endpoint )
        assertEquals( "https://{appname}-api.apigee.com", restService.resourceList[1].childResourceList[0].restMethodList[0].requestList[0].endpoint )

    }

}
