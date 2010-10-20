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

import java.util.ArrayList;
import java.util.List;
import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.Vault;
import com.okidokiteam.gouken.kernel.StaticVaultConfiguration;
import com.okidokiteam.gouken.kernel.VaultBoot;
import com.okidokiteam.gouken.plugin.intern.DefaultPluginVault;
import org.ops4j.pax.repository.AetherResolver;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;

import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 * Interfaced Vault ( "PluginVault" for embedding into other plugin applications )
 *
 * Uses default resources and settings. This may be replaced to match user specifics.
 */
public class PluginVaultBuilder
{

    private static final String[] DEFAULT_MA_BUNDLES = {
        "org.ops4j.pax.exam:pax-exam-container-rbc:2.0.0-SNAPSHOT",
        "org.ops4j.pax.exam:pax-exam-spi:2.0.0-SNAPSHOT",
        "org.apache.felix:org.apache.felix.fileinstall:1.0.0"
    };

    private final RepositoryResolver m_repo;

    public PluginVaultBuilder(RepositoryResolver resolver)
        throws KernelException
    {
      
        m_repo = resolver;

    }

    public PluginVault create( VaultPluginPoint... pointDefaultVault )
        throws KernelException
    {
        Vault vault;
        try
        {
            vault = new VaultBoot( new StaticVaultConfiguration( true,
                                                                 getDefaultBundles(),
                                                                 "fileinstall.poll=300",
                                                                 "org.ops4j.pax.exam.port=9000") );
        } catch( RepositoryException e )
        {
            throw new KernelException( "Underlying Repository for MA Bundles has a problem.", e );
        }

        return new DefaultPluginVault( vault, pointDefaultVault );
    }

    private Artifact[] getDefaultBundles()
        throws RepositoryException
    {
        List<Artifact> list = new ArrayList<Artifact>();
        for( String bundle : DEFAULT_MA_BUNDLES )
        {
            list.add( m_repo.find( parseFromURL( bundle ) ) );

        }
        return list.toArray( new Artifact[ list.size() ] );
    }

}
