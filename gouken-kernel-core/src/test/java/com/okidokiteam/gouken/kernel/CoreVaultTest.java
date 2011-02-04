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

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import com.okidokiteam.gouken.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.RepositoryException;
import org.osgi.service.deploymentadmin.DeploymentAdmin;

import com.okidokiteam.gouken.ace.AceManagementAgent;

/**
 *
 */
public class CoreVaultTest {

    @Test
    public void testEmptyStartStop()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        GoukenResolver resolver = mock( GoukenResolver.class );

        Vault<VaultPush> coreVault = getVault( resolver, VaultPush.class );

        ManagementAgent agent = Mockito.mock( ManagementAgent.class );
        when( agent.getRuntimeParts() ).thenReturn( new ArtifactReference[ 0 ] );

        coreVault.start( agent );
        coreVault.stop();

        verify( agent, Mockito.only() ).getRuntimeParts();
    }

    @Test
    public void testACEBased()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        GoukenResolver resolver = mock( GoukenResolver.class );

        Vault<VaultPush> coreVault = getVault( resolver, VaultPush.class );

        ManagementAgent agent = new AceManagementAgent();

        coreVault.start( agent );
        coreVault.stop();
    }

    @Test
    public void testPush()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        // we know that the ace agent also includes DeploymentAdmin. For that reason we can use it here as a test service:
        GoukenResolver resolver = mock( GoukenResolver.class );
       
        Vault<DeploymentAdmin> coreVault = new CoreVault<DeploymentAdmin>( resolver, getSettings(), DeploymentAdmin.class );

        DeploymentAdmin push = coreVault.start( new AceManagementAgent() );
        // do stuff
        assertThat( push.listDeploymentPackages().length, is( 0 ) );

        coreVault.stop();
    }

    private VaultSettings getSettings()
    {
        final File f = getCleanDirectory();
        return new VaultSettings() {
            public File getWorkingFolder()
            {
                return f;
            }
        };
    }

    private <T> Vault<T> getVault( GoukenResolver resolver, Class<T> clazz )
        throws KernelException
    {

        return new CoreVault<T>( resolver, getSettings(), clazz );
    }

    private File getCleanDirectory()
    {

        File workDir = new File( ".target/gouken" );
        FileUtils.delete( workDir );
        workDir.mkdirs();
        return workDir;
    }
}
