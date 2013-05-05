package com.smartbear.restplugin;

import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

@AForm(name = "Add REST API from Apigee Console", description = "Imports an API hosted by Apigee")
public interface AddFromApigeeForm {
    @AField(description = "Status", type = AField.AFieldType.LABEL)
    public final static String STATUS = "Status";

    @AField(description = "API Name", type = AField.AFieldType.COMPONENT)
    public final static String NAME = "Name";

    @AField(description = "API Description", type = AField.AFieldType.LABEL)
    public final static String DESCRIPTION = "Description";
}