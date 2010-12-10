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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import com.okidokiteam.gouken.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.aether.AetherResolver;
import org.osgi.service.deploymentadmin.DeploymentAdmin;

import com.okidokiteam.gouken.ace.AceVaultAgent;

import static org.hamcrest.core.Is.*;

/**
 *
 */
public class CoreVaultTest
{

    @Test
    public void testEmptyStartStop()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        Vault<VaultPush> coreVault = getVault( VaultPush.class );

        VaultAgent agent = Mockito.mock( VaultAgent.class );
        when( agent.getArtifacts() ).thenReturn( new Artifact[ 0 ] );

        coreVault.start( agent );
        coreVault.stop();

        verify( agent, Mockito.only() ).getArtifacts();
    }

    @Test
    public void testACEBased()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        Resolver resolver = getResolver();

        Vault<VaultPush> coreVault = getVault( VaultPush.class );

        VaultAgent agent = new AceVaultAgent( resolver );

        coreVault.start( agent );
        coreVault.stop();
    }

    @Test
    public void testPush()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        // we know that the ace agent also includes DeploymentAdmin. For that reason we can use it here as a test service:
        Vault<DeploymentAdmin> coreVault = new CoreVault<DeploymentAdmin>( getSettings(), DeploymentAdmin.class );
        DeploymentAdmin push = coreVault.start( new AceVaultAgent( getResolver() ) );
        // do stuff
        assertThat( push.listDeploymentPackages().length, is( 0 ) );

        coreVault.stop();
    }

    private VaultSettings getSettings()
    {
        final File f = getCleanDirectory();
        return new VaultSettings()
        {
            public File getWorkingFolder()
            {
                return f;
            }
        };
    }

    private AetherResolver getResolver()
    {
        return new AetherResolver( null, "http://localhost:8081/nexus/content/groups/public/" );
    }

    private <T> Vault<T> getVault( Class<T> clazz )
        throws KernelException
    {
        return new CoreVault<T>( getSettings(), clazz );
    }

    private File getCleanDirectory()
    {
        File workDir = new File( ".target/gouken" );
        FileUtils.delete( workDir );
        workDir.mkdirs();
        return workDir;
    }
}
