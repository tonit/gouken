package com.okidokiteam.gouken.ace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.base.RepositoryFactory;

import com.google.inject.Inject;
import com.okidokiteam.gouken.VaultAgent;

/**
 * 
 * Lazily resolves artifacts from a file in classpath (shipped with the jar), resolves them (lazily).
 * 
 * @author tonit
 * 
 */
public class AceVaultAgent implements VaultAgent
{
    private static final String LOCATION = "/META-INF/bundles.provision";
    private final Resolver m_resolver;

    private Artifact[] m_artifacts;

    @Inject
    public AceVaultAgent( Resolver resolver ) throws IOException
    {
        assert ( resolver != null ) : "Resolver should not be null.";
        m_resolver = resolver;
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

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( LOCATION ) ) );
        String line = null;

        try
        {
            while (( line = bufferedReader.readLine() ) != null)
            {
                line = line.trim();
                if (!line.isEmpty())
                {
                    list.add( m_resolver.find( RepositoryFactory.createQuery( line ) ) );
                }
            }
        } catch (IOException e)
        {

            throw new RepositoryException( "Problem reading artifacts from " + LOCATION, e );
        }

        m_artifacts = list.toArray( new Artifact[list.size()] );
    }

}
