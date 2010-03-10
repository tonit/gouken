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
        Configuration config = m_configAdmin.getConfiguration( "org.apache.ace.identification.property" );

        Dictionary p = new Hashtable();

        p.put( "gatewayID", "ToniGatewayID" );
        config.update( p );
        System.out.println( "--ping--" );
    }
}
