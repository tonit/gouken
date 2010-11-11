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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.deploymentadmin.BundleInfo;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;
import org.ops4j.pax.swissbox.tinybundles.dp.DP;
import org.ops4j.pax.swissbox.tinybundles.dp.TinyDP;

import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 *
 */
public class CoreVaultUpdate
{

    private static Logger LOG = LoggerFactory.getLogger( CoreVaultUpdate.class );

    private BundleContext m_ctx;
    private RepositoryResolver m_resolver;

    public CoreVaultUpdate( BundleContext bundleContext,
                            RepositoryResolver resolver )
    {
        m_ctx = bundleContext;
        m_resolver = resolver;
    }

    public void invoke( VaultConfiguration configuration )
    {
        DeploymentAdmin admin = find( m_ctx );
        try
        {
            // assume straigt that this is a compatible package.
            //admin.installDeploymentPackage( configuration.get().get() );
        } catch( Exception e )
        {
            e.printStackTrace();
        } finally
        {
// unget
        }
    }

    private void example( DeploymentAdmin admin )
        throws DeploymentException, RepositoryException, IOException
    {
        // sample.
        DeploymentPackage aPackage = admin.installDeploymentPackage( createDP( "org.apache.felix:org.apache.felix.shell.remote:1.0.4",
                                                                               "org.apache.felix:org.apache.felix.shell:1.0.2",
                                                                               "org.apache.felix:org.apache.felix.dependencymanager.shell:3.0.0-SNAPSHOT"
        )
        );

        LOG.info( "Installed DP " + aPackage.getName() );

        for( BundleInfo bundleInfo : aPackage.getBundleInfos() )
        {

            LOG.info( "INF: Bundle: " + bundleInfo.getSymbolicName() + " is " + bundleInfo.getVersion() );

        }

        for( Bundle b : m_ctx.getBundles() )
        {
            LOG.info( "CTX: Bundle: " + b.getSymbolicName() + " is " + b.getState() );
        }
    }

    private InputStream createDP( String... locs )
        throws RepositoryException, IOException
    {
        TinyDP dp = DP.newDeploymentPackage();

        dp.setSymbolicName( "Bee" );
        dp.setVersion( "1.0" );
        int i = 0;
        for( String loc : locs )
        {

            dp.setBundle( "B" + ( i++ ), m_resolver.find( parseFromURL( loc ) ).getContent().get() );
        }
        return dp.build();
    }

    private DeploymentAdmin find( BundleContext context )
    {
        ServiceReference reference = context.getServiceReference( DeploymentAdmin.class.getName() );
        if( reference != null )
        {
            return (DeploymentAdmin) context.getService( reference );
        }
        else
        {
            throw new IllegalStateException( "No DeploymentAdmin found." );
        }
    }
}
