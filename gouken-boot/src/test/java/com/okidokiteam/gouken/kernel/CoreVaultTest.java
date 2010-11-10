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
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultConfiguration;
import com.okidokiteam.gouken.VaultHandle;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;
import org.ops4j.pax.repository.resolver.FastLocalM2Resolver;

import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 *
 */
public class CoreVaultTest
{

    @Test
    public void testCore()
        throws KernelWorkflowException, KernelException
    {
        Vault coreVault = create();
        VaultHandle handle = coreVault.start();

        coreVault.update( null );

        coreVault.stop( handle );
    }

    private Vault create()
        throws KernelException
    {
        RepositoryResolver resolver = new FastLocalM2Resolver();
        Vault vault;
        File workDir = new File( ".target/gouken" );

        FileUtils.delete( workDir );

        workDir.mkdirs();

        vault = new CoreVault(
            new VaultConfiguration()
            {
                public Artifact[] getArtifacts()
                {
                    return new Artifact[ 0 ]; 
                }
            },
            workDir,
            resolver
        );

        return vault;
    }
}
