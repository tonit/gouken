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

import java.io.IOException;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.VaultAgent;
import com.okidokiteam.gouken.VaultConfiguration;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deploymentadmin based vault agent.
 */
public class DPVaultAgent implements VaultAgent
{

    private static final Logger LOG = LoggerFactory.getLogger( DPVaultAgent.class );

    // injected
    private volatile DeploymentAdmin m_deploymentAdmin;

    public void update( VaultConfiguration configuration )
        throws KernelException
    {
        try
        {
            LOG.debug( "Update triggered. Installing DP: " + configuration );

            DeploymentPackage dp = m_deploymentAdmin.installDeploymentPackage( configuration.get().get() );

            LOG.debug( "Update successful: Version: " + dp.getVersion() );

        } catch( DeploymentException e )
        {
            LOG.warn( "Update failed!" );
            throw new KernelException( "Unable to update state.", e );
        } catch( IOException e )
        {
            LOG.warn( "Update failed!" );
            throw new KernelException( "Unable to update state.", e );
        }
    }
}
