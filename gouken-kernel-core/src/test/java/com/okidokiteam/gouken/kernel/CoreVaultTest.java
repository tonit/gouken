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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.aether.AetherResolver;

import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultAgent;

/**
 *
 */
public class CoreVaultTest
{

    @Test
    public void testEmptyStartStop()
        throws KernelWorkflowException, KernelException, IOException, RepositoryException
    {
        Vault<Void> coreVault = create();

        VaultAgent agent = Mockito.mock( VaultAgent.class );
        when( agent.getArtifacts() ).thenReturn( new Artifact[0] );

        coreVault.start( agent );
        coreVault.stop();

        verify( agent, Mockito.only() ).getArtifacts();
    }

    private Vault<Void> create()
        throws KernelException
    {
        Resolver resolver = new AetherResolver( null, "http://localhost:8081/nexus/content/groups/public/" );
        Vault<Void> vault;
        File workDir = new File( ".target/gouken" );

        FileUtils.delete( workDir );

        workDir.mkdirs();

        vault = new CoreVault(
                workDir,
                resolver );

        return vault;
    }
}
