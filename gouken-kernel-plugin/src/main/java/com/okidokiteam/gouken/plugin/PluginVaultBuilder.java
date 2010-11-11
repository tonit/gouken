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
package com.okidokiteam.gouken.plugin;

import java.io.File;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.VaultConfiguration;
import com.okidokiteam.gouken.kernel.CoreVault;
import com.okidokiteam.gouken.plugin.intern.DefaultPluginVault;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.repository.InputStreamSource;
import org.ops4j.pax.repository.RepositoryResolver;

/**
 * Interfaced Vault ( "PluginVault" for embedding into other plugin applications )
 *
 * Uses default resources and settings. This may be replaced to match user specifics.
 */
public class PluginVaultBuilder
{

    private static final String[] DEFAULT_MA_BUNDLES = {
        ""
    };

    private final RepositoryResolver m_resolver;

    public PluginVaultBuilder( RepositoryResolver resolver )
        throws KernelException
    {

        m_resolver = resolver;

    }

    public PluginVault create( VaultPluginPoint... pointDefaultVault )
        throws KernelException
    {
        Vault vault;
        File workDir = new File( ".target/gouken" );

        FileUtils.delete( workDir );

        workDir.mkdirs();

        vault = new CoreVault(
            new VaultConfiguration()
            {

                public InputStreamSource get()
                {
                    return null; 
                }
            },
            workDir,
            m_resolver
        );

        return new DefaultPluginVault( vault, pointDefaultVault );
    }

}
