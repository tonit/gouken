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

import java.io.IOException;
import com.okidokiteam.gouken.kernel.CoreVault;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.RepositoryResolver;
import org.ops4j.pax.repository.resolver.FastLocalM2Resolver;

import static junit.framework.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.repository.resolver.RepositoryFactory.*;

/**
 * OSGi Tests using Pax Exam 2
 */
@RunWith( JUnit4TestRunner.class )
public class OSGiAgentTest
{

    @Configuration
    public Option[] config()
        throws RepositoryException, IOException
    {
        RepositoryResolver res = new FastLocalM2Resolver();
        return options(
            provision(
                res.find( parseFromURL( CoreVault.BUNDLE_DEPLOYMENTADMIN ) ).getContent().get(),
                res.find( parseFromURL( CoreVault.BUNDLE_DM ) ).getContent().get()
            )

        );
    }

    @Test
    public void tryToFindDeploymentAdmin( BundleContext ctx )
    {
        ServiceReference serviceReference = ctx.getServiceReference( DeploymentAdmin.class.getName() );
        if( serviceReference != null )
        {
            DeploymentAdmin service = (DeploymentAdmin) ctx.getService( serviceReference );
            if( service == null )
            {
                fail( "No DP1" );
            }
            else
            {
                return; // success
            }
        }
        else
        {
            fail( "No DP2" );
        }
        fail( "no dp" );
    }
}
