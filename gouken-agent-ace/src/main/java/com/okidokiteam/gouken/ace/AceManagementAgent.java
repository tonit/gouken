package com.okidokiteam.gouken.ace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.okidokiteam.gouken.GoukenRuntimeArtifact;
import com.okidokiteam.gouken.ManagementAgent;
import org.ops4j.pax.repository.RepositoryException;

/**
 * Lazily resolves artifacts from a file in classpath (shipped with the jar), resolves them (lazily).
 *
 * @author tonit
 */
public class AceManagementAgent implements ManagementAgent<GoukenRuntimeArtifact> {

    private static final String LOCATION = "/META-INF/bundles.provision";

    private GoukenRuntimeArtifact[] m_artifacts;

    public AceManagementAgent( )
        throws IOException
    {
      
    }

    public synchronized GoukenRuntimeArtifact[] getRuntimeParts()
        throws RepositoryException
    {
        if( m_artifacts == null ) {
            readFromFile();
        }
        return m_artifacts;

    }

    private void readFromFile()
        throws RepositoryException
    {
        List<GoukenRuntimeArtifact> list = new ArrayList<GoukenRuntimeArtifact>();

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( LOCATION ) ) );
        String line = null;

        try {
            while( ( line = bufferedReader.readLine() ) != null ) {
                line = line.trim();
                if( !line.isEmpty() ) {
                    list.add( newArtifact( line ) );
                }
            }
        } catch( IOException e ) {

            throw new RepositoryException( "Problem reading artifacts from " + LOCATION, e );
        }

        m_artifacts = list.toArray( new GoukenRuntimeArtifact[ list.size() ] );
    }

    private GoukenRuntimeArtifact newArtifact( String line )
    {
        return new DefaultRuntimeArtifact( line );
    }

    private class DefaultRuntimeArtifact implements GoukenRuntimeArtifact {

        private final String m_name;

        public DefaultRuntimeArtifact( String line )
        {
            m_name = line;
        }

        public String name()
        {
            return m_name;
        }
    }
}
