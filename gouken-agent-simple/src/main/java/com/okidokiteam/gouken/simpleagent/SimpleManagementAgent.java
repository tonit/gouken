package com.okidokiteam.gouken.simpleagent;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import com.okidokiteam.gouken.ArtifactReference;
import com.okidokiteam.gouken.ManagementAgent;
import com.okidokiteam.gouken.def.TypedArtifactReference;
import com.okidokiteam.gouken.def.UntypedArtifactReference;
import com.okidokiteam.gouken.simpleagent.api.MyPush;
import com.okidokiteam.gouken.simpleagent.service.MyPushImpl;
import org.ops4j.pax.repository.RepositoryException;
import org.ops4j.pax.repository.typed.TypedReference;
import org.ops4j.pax.repository.typed.TypedRepository;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

import static com.okidokiteam.gouken.simpleagent.SimpleAgentReference.*;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;

public class SimpleManagementAgent implements ManagementAgent {

    final private Store<InputStream> m_store;

    final private TypedRepository<Handle> m_target;

    /**
     * @param target when contributing to a {@link TypedRepository} you need to have this.
     * @param store  We use tinybundles internally. This is where we toss the bundle at.
     */
    @Inject
    public SimpleManagementAgent( TypedRepository<Handle> target, Store<InputStream> store )
    {
        m_store = store;
        m_target = target;
    }

    /**
     * The dynamic partion created using tinybundles.
     * Also the static parts.
     */
    public ArtifactReference[] getRuntimeParts()
        throws RepositoryException
    {

        return new ArtifactReference[]{
            makeReference( SIMPLEAGENT ), // typed !
            makeReference( "org.apache.felix.dm" ), // untyped
            makeReference( "org.osgi.compendium" ) // untyped
        };
    }

    private ArtifactReference makeReference( String term )
    {
        return new UntypedArtifactReference( term );
    }

    /**
     * Tinybundles in Action
     *
     * @param reference how this artifact is referenced as.
     *
     * @return Artifact that has been made out of part of this component
     *
     * @throws org.ops4j.pax.repository.RepositoryException
     *          probs
     */
    private ArtifactReference makeReference( TypedReference reference )
        throws RepositoryException

    {
        try {
            return new TypedArtifactReference(
                m_target.set(
                    reference, m_store.store(
                    newBundle()
                        .add( MyPush.class )
                        .add( MyPushImpl.class )
                        .set( "Export-Package", MyPush.class.getPackage().getName() )
                        .build( withBnd() )
                )
                )
            );

        } catch( IOException e ) {
            throw new RepositoryException( "Baking the bundle failed", e );
        }
    }

    @Override
    public String toString()
    {
        return "[SimpleManagementAgent]";
    }


}
