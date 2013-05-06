package com.smartbear.restplugin

import com.eviware.soapui.impl.rest.RestResource
import com.eviware.soapui.impl.rest.RestService
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder
import com.eviware.soapui.impl.rest.support.RestUtils

class ApigeeUtils {

    public static void postProcessService( RestService restService )
    {
        extractTemplateParametersFromHostName( restService )
        fixMultiResources( restService );
    }

    public static void extractTemplateParametersFromHostName( RestService restService )
    {
        RestResource resource = null

        restService.resourceList.each { it

            resource = it
            RestUtils.extractTemplateParams( it.path ).each { it
                if( !resource.params.hasProperty( it ))
                {
                    resource.params.addProperty( it ).style = RestParamsPropertyHolder.ParameterStyle.TEMPLATE
                }
            }
        }

    }

    public static void fixMultiResources( RestService restService )
    {
        String endpoint = null

        restService.getAllResources().each{ it

            if( it.parentResource == null )
                endpoint = null

            it.getRestMethodList().each{ it
                it.getRequestList().each { it
                    if( it.endpoint == null )
                    {
                        def resource = it.resource
                        while( resource.parentResource != null )
                            resource = resource.parentResource

                        // extract host from path
                        if( resource.path.startsWith( "http" ))
                        {
                            def path = resource.path
                            def ix = path.indexOf( "//" )
                            if( ix != -1 )
                            {
                                ix = path.indexOf( "/", ix+2 )
                                if( ix != -1 )
                                {
                                   endpoint = path.substring( 0, ix )
                                   resource.path = path.substring( ix )
                                }
                            }
                        }

                        if( endpoint != null )
                        {
                            it.endpoint = endpoint
                        }
                    }
                }
            }
        }

    }
}
