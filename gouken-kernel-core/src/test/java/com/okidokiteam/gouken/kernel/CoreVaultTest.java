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
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultConfigurationSource;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.aether.AetherResolver;
import org.ops4j.pax.repository.maven.FastLocalM2Resolver;

/**
 *
 */
public class CoreVaultTest
{

    @Test
    public void testCore()
        throws KernelWorkflowException, KernelException, IOException
    {
        Vault coreVault = create();
        VaultConfigurationSource conf = coreVault.start();

        // now install some software:

        coreVault.update( conf
                              .build()
        );

        coreVault.stop();
    }

    private Vault create()
        throws KernelException
    {
        Resolver resolver = new AetherResolver(null,"http://localhost:8081/nexus/content/groups/public/");
        Vault vault;
        File workDir = new File( ".target/gouken" );

        FileUtils.delete( workDir );

        workDir.mkdirs();

        vault = new CoreVault(
            workDir,
            resolver
        );

        return vault;
    }
}
