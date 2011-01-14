package com.okidokiteam.gouken.simpleagent;

import com.okidokiteam.gouken.VaultAgent;
import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.Resolver;
import org.ops4j.pax.repository.base.RepositoryFactory;
import org.ops4j.pax.repository.base.helpers.LocalArtifact;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundle;
import org.ops4j.store.Store;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.newBundle;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.withBnd;

public class SimpleVaultAgent implements VaultAgent
{

    private final Resolver m_resolver;

    private final Store<InputStream> m_store;

    @Inject
    public SimpleVaultAgent( final Resolver resolver, final Store<InputStream> store )
    {
        m_resolver = resolver;
        m_store = store;
    }

    public Artifact[] getArtifacts()
        throws RepositoryException
    {
        List<Artifact> res = new ArrayList<Artifact>();

        res.add( resolve( "org.osgi:org.osgi.compendium:4.2.0" ) );
        res.add( resolve( "org.apache.felix:org.apache.felix.fileinstall:1.0.0" ) );

        //res.add(createDynamic());
        return res.toArray( new Artifact[ res.size() ] );
    }

    private Artifact createDynamic()
    {

        return null;
    }

    private Artifact installDynamicBundle( String name, Class a, Class... extraContent )
        throws IOException
    {
        TinyBundle tb = newBundle();

        for( Class c : extraContent )
        {
            tb.add( c );
        }

        if( a != null )
        {
            tb.add( a );
            tb.set( "Bundle-Activator", a.getName() );
        }
        return new LocalArtifact(
            new File( m_store.getLocation(
                m_store.store( tb.build( withBnd() ) )
            ).toASCIIString()
            )
        );
    }

    @Override
    public String toString()
    {
        return "[SimpleVaultAgent resolver=" + m_resolver + " , store=" + m_store + "]";
    }

    private Artifact resolve( String string )
        throws RepositoryException
    {
        return m_resolver.find( RepositoryFactory.createQuery( string ) );
    }

}
