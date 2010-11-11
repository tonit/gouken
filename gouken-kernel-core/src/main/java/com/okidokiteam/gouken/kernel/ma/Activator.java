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
package com.okidokiteam.gouken.kernel.ma;

import java.util.Hashtable;
import com.okidokiteam.gouken.VaultAgent;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.deploymentadmin.DeploymentAdmin;

/**
 * Management Agent Activator.
 */
public class Activator extends DependencyActivatorBase
{

    
    @Override
    public void init( BundleContext ctx, DependencyManager dm )
        throws Exception
    {
        VaultAgent agentImpl = new DPVaultAgent();
        dm.createComponent()
            .setInterface( VaultAgent.class.getName(), new Hashtable() ).setImplementation( agentImpl )
            .add( dm.createServiceDependency().setService( DeploymentAdmin.class ).setRequired( true ) )
            .start();
    }

    @Override
    public void destroy( BundleContext ctx, DependencyManager dm )
        throws Exception
    {

    }
}
