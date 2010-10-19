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

import com.okidokiteam.gouken.KernelException;
import com.okidokiteam.gouken.KernelWorkflowException;
import com.okidokiteam.gouken.VaultHandle;
import com.okidokiteam.gouken.plugin.bridge.MyService;
import com.okidokiteam.gouken.plugin.intern.DefaultVaultPluginPoint;
import org.junit.Test;
import org.ops4j.pax.repository.AetherResolver;

/**
 *
 */
public class DefaultPluginVaultTest
{

    @Test
    public void testDefault()
        throws KernelException, KernelWorkflowException
    {

        // Prepare a Builder that uses broad aether based repository.
        PluginVaultBuilder builder = new PluginVaultBuilder( new AetherResolver() );

        PluginVault vault = builder.create(
            new DefaultVaultPluginPoint<MyService>( MyService.class )
        );

        VaultHandle handle = vault.start();

        vault.registerCallbacks( new PluginCallback<MyService>()
        {

            public void activated( MyService service )
            {

            }

            public void deactivated( MyService service )
            {

            }
        }
        );

        // do install plugins via Gouken Remote Plugin Control API

        // shutdown..
        vault.stop( handle );

        // now it must be possible to call + interact with those services in an extender pattern:

    }
}
