/*
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.gouken.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

/**
 * @author Toni Menzel
 * @since Mar 10, 2010
 */
public class Configure
{

    private volatile LogService m_log;                  /* injected by dependency manager */
    private volatile ConfigurationAdmin m_configAdmin;  /* injected by dependency manager */
    private volatile BundleContext m_context;           /* injected by dependency manager */

    // just configure and go away

    synchronized void start()
        throws IOException
    {
        config( "org.apache.ace.discovery.property", new String[]{ "serverURL", "http://localhost:8080" } );
        config( "org.apache.ace.identification.property", new String[]{ "gatewayID", "ToniGatewayID" } );
        config( "org.apache.ace.scheduler",
                new String[]{ "auditlog", "2000" },
                new String[]{ "org.apache.ace.deployment.task.DeploymentUpdateTask", "2000" }
        );

        configFactory( "org.apache.ace.gateway.log.factory",
                       new String[]{ "name", "auditlog" }
        );
        configFactory( "org.apache.ace.gateway.log.store.factory",
                       new String[]{ "name", "auditlog" }
        );


    }

    private void config( String pid, String[]... tuples )
        throws IOException
    {
        Configuration config = m_configAdmin.getConfiguration( pid );
        Dictionary p = config.getProperties();
        if( p == null )
        {
            p = new Hashtable();
        }
        for( String[] tuple : tuples )
        {
            p.put( tuple[ 0 ], tuple[ 1 ] );

        }
        config.update( p );

    }

    private void configFactory( String pid, String[]... tuples )
        throws IOException
    {
        Configuration config = m_configAdmin.createFactoryConfiguration( pid,null );
        Dictionary p = config.getProperties();
        if( p == null )
        {
            p = new Hashtable();
        }
        for( String[] tuple : tuples )
        {
            p.put( tuple[ 0 ], tuple[ 1 ] );

        }
        config.update( p );

    }


}
