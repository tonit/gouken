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
import com.okidokiteam.gouken.plugin.remotes.LocalPluginRemote;
import com.sun.xml.internal.ws.org.objectweb.asm.AnnotationVisitor;
import com.sun.xml.internal.ws.org.objectweb.asm.Attribute;
import com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor;
import com.sun.xml.internal.ws.org.objectweb.asm.FieldVisitor;
import com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor;
import org.junit.Test;
import org.mockito.asm.ClassAdapter;
import org.ops4j.pax.repository.resolver.FastLocalM2Resolver;

import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 *
 */
public class DefaultPluginVaultTest
{

    // TODO future feature to allow for seamless service usage without manual callbacks.
    //@Inject private volatile VaultPluginPoint<MyService> services;

    @Test
    public void testDefault()
        throws KernelException, KernelWorkflowException, InterruptedException
    {

        // Prepare a Builder
        PluginVaultBuilder builder = null;
        try
        {
            builder = new PluginVaultBuilder( new FastLocalM2Resolver() );

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
            PluginRemote remote = new LocalPluginRemote( handle );

            // remote.install( parseFromURL( "" ) );

            // now it must be possible to call + interact with those services in an extender pattern:
            //after a treshold..

            // shutdown..
            //vault.stop( handle );

        } catch( KernelException e )
        {
            e.printStackTrace();
        } catch( KernelWorkflowException e )
        {
            e.printStackTrace();
        }

    }

}

