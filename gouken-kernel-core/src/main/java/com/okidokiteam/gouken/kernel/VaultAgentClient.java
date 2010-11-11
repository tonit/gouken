/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.gouken.kernel;

import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.VaultAgent;
import com.okidokiteam.gouken.VaultConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VaultAgentClient implements VaultAgent
{

    private static Logger LOG = LoggerFactory.getLogger( VaultAgentClient.class );

    private BundleContext m_ctx;

    public VaultAgentClient( BundleContext bundleContext )
    {
        m_ctx = bundleContext;

    }

    public void update( VaultConfiguration configuration )
        throws KernelException
    {

        find( m_ctx ).update( configuration );


    }

    private VaultAgent find( BundleContext context )
    {
        ServiceReference reference = context.getServiceReference( VaultAgent.class.getName() );
        if( reference != null )
        {
            return (VaultAgent) context.getService( reference );
        }
        else
        {
            throw new IllegalStateException( "No VaultAgent found." );
        }
    }
}
