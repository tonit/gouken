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

import java.io.IOException;
import java.io.InputStream;
import com.okidokiteam.gouken.VaultConfiguration;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.base.io.InputStreamSource;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;
import org.ops4j.pax.swissbox.tinybundles.dp.DP;
import org.ops4j.pax.swissbox.tinybundles.dp.TinyDP;


/**
 * Test Helper
 */
public class DPExample
{

    private static Logger LOG = LoggerFactory.getLogger( DPExample.class );

    private VaultConfiguration example( RepositoryResolver resolver )
        throws DeploymentException, RepositoryException, IOException
    {

        // sample.
        final InputStream dp = createDP( resolver, "org.apache.felix:org.apache.felix.shell.remote:1.0.4",
                                         "org.apache.felix:org.apache.felix.shell:1.0.2",
                                         "org.apache.felix:org.apache.felix.dependencymanager.shell:3.0.0-SNAPSHOT"

        );
        return new VaultConfiguration()
        {

            public InputStreamSource get()
            {
                return new InputStreamSource()
                {

                    public InputStream get()
                        throws IOException
                    {
                        return dp;
                    }
                };
            }
        };

    }

    private InputStream createDP( RepositoryResolver resolver, String... locs )
        throws RepositoryException, IOException
    {
        TinyDP dp = DP.newDeploymentPackage();

        dp.setSymbolicName( "Bee" );
        dp.setVersion( "1.0" );
        int i = 0;
        for( String loc : locs )
        {

           // dp.setBundle( "B" + ( i++ ), resolver.find( parseFromURL( loc ) ).getContent().get() );
        }
        return dp.build();
    }
}
