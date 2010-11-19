package com.okidokiteam.gouken.ace;

import java.io.IOException;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;

import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.ArtifactQuery;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;

public class AceVaultAgentTest
{
    public final static int ENTRIES_IN_SAMPLE = 4;
    
    @Test
    public void testLoadingPropertiesCorrectly() throws IOException, RepositoryException
    {
        Resolver resolver = mock( Resolver.class );
        Artifact artifact = mock( Artifact.class );

        when( resolver.find( any( ArtifactQuery.class ) ) ).thenReturn( artifact );

        AceVaultAgent agent = new AceVaultAgent( resolver );

        assertThat( agent.getArtifacts().length, is( ENTRIES_IN_SAMPLE ) );

        verify( resolver, times( ENTRIES_IN_SAMPLE ) ).find( any( ArtifactQuery.class ) );
    }
}
