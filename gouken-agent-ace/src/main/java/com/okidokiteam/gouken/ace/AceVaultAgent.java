package com.okidokiteam.gouken.ace;

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.base.RepositoryFactory;

import com.okidokiteam.gouken.VaultAgent;

/**
 * 
 * @author tonit
 * 
 */
public class AceVaultAgent implements VaultAgent
{
    private final String[] m_bundles;
    private final Resolver m_resolver;

    private Artifact[] m_artifacts;

    public AceVaultAgent( Resolver resolver )
    {
        assert ( resolver != null ) : "Resolver should not be null.";
        m_resolver = resolver;

        // perhaps read this from a properties file instead ;)
        m_bundles = new String[] {
                "org.apache.felix:org.apache.felix.deploymentadmin:0.9.0-SNAPSHOT"
                    , "org.apache.felix:org.apache.felix.dependencymanager:3.0.0-SNAPSHOT"
                    , "org.osgi:org.osgi.compendium:4.2.0"
                    , "org.ops4j.pax.logging:pax-logging-api:1.5.1"
                    , "org.apache.felix:org.apache.felix.eventadmin:1.2.2"
                    , "javax.servlet:servlet-api:2.4"
                    , "org.apache.felix:org.apache.felix.configadmin:1.2.4"

                    , "org.apache.ace:ace-deployment-api:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-range-api:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-discovery-api:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-identification-api:0.8.0-SNAPSHOT"

                    , "org.apache.ace:ace-deployment-deploymentadmin:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-deployment-task:0.8.0-SNAPSHOT"

                    , "org.apache.ace:ace-consolelogger:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-discovery-property:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-identification-property:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-scheduler:0.8.0-SNAPSHOT"

                    , "org.apache.ace:ace-log:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-log-listener:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-gateway-log:0.8.0-SNAPSHOT"
                    , "org.apache.ace:ace-gateway-log-store:0.8.0-SNAPSHOT"
        };
    }

    public synchronized Artifact[] getArtifacts() throws RepositoryException
    {
        if (m_artifacts == null)
        {
            resolve();
        }
        return m_artifacts;

    }

    private void resolve() throws RepositoryException
    {
        List<Artifact> list = new ArrayList<Artifact>();

        for (String s : m_bundles)
        {
            list.add( m_resolver.find( RepositoryFactory.createQuery( s ) ) );

        }
        m_artifacts = list.toArray( new Artifact[list.size()] );
    }

}
