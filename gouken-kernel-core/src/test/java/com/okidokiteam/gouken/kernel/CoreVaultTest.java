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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import com.okidokiteam.gouken.ArtifactReference;
import com.okidokiteam.gouken.GoukenResolver;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.ManagementAgent;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultPush;
import com.okidokiteam.gouken.VaultServiceException;
import com.okidokiteam.gouken.VaultSettings;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.Provider;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.typed.TypedReference;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class CoreVaultTest {

    @Test
    public void testEmptyStartStop()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        GoukenResolver resolver = mock( GoukenResolver.class );

        Vault<VaultPush> vault = makeVault( resolver, getSettings(), VaultPush.class );

        ManagementAgent agent = Mockito.mock( ManagementAgent.class );
        when( agent.getRuntimeParts() ).thenReturn( new ArtifactReference[ 0 ] );

        vault.start( agent );
        vault.stop();

        verify( agent, Mockito.only() ).getRuntimeParts();
    }

    @Test( expected = VaultServiceException.class )
    public void testPushTimeout()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        // we know that the ace agent also includes DeploymentAdmin. For that reason we can use it here as a test service:
        GoukenResolver resolver = mock( GoukenResolver.class );

        when( resolver.find( Matchers.<ArtifactReference>any() ) ).thenReturn( getExampleResource() );

        Vault<DeploymentAdmin> vault = makeVault( resolver, getSettings(), DeploymentAdmin.class );

        ManagementAgent agent = mock( ManagementAgent.class );
        when( agent.getRuntimeParts() ).thenReturn( new ArtifactReference[]{ reference( "" ) } );

        DeploymentAdmin push = vault.start( agent );

        // will not work, must timeout.
        push.listDeploymentPackages();
    }

    @Test
    public void testPush()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException, InvalidSyntaxException
    {
        // we know that the ace agent also includes DeploymentAdmin. For that reason we can use it here as a test service:
        GoukenResolver resolver = mock( GoukenResolver.class );
        ManagementAgent agent = mock( ManagementAgent.class );
        when( agent.getRuntimeParts() ).thenReturn( new ArtifactReference[]{ } );

        // learn from Guice TypeLiteral how to suck out generic type information.
        // then we can leave out the last parameter.
        Vault<PackageAdmin> coreVault = makeVault( resolver, getSettings(), PackageAdmin.class );
        PackageAdmin push = coreVault.start( agent );

        // Should work
        Bundle bundle = push.getBundle( PackageAdmin.class );
        assertNotNull( bundle );
    }

    private ArtifactReference reference( final String s )
    {
        return new ArtifactReference() {

            public String untyped()
            {
                return s;
            }

            public TypedReference typed()
            {
                return null; 
            }
        };
    }

    private Provider<InputStream> getExampleResource()
    {
        return new Provider<InputStream>() {
            public InputStream get()
                throws RepositoryException
            {
                return getClass().getResourceAsStream( "/org.osgi.compendium-4.2.0.jar" );
            }
        };
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

    private File getCleanDirectory()
    {
        File workDir = new File( "target/gouken" );
        FileUtils.delete( workDir );
        workDir.mkdirs();
        return workDir;
    }

    // HELPER
    private <T> Vault<T> makeVault( GoukenResolver resolver, VaultSettings settings, Class<T> push )
    {
        m_vault = new CoreVault<T>( resolver, settings, push );
        return m_vault;
    }

    @After
    public void after()
    {
        // tear down if available:
        if( m_vault != null ) {
            try {
                m_vault.stop();
            } catch( KernelWorkflowException e ) {
                // don't care
            } catch( KernelException e ) {
                // don't care
            }
        }
    }

    private Vault m_vault;
}
