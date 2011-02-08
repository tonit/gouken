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
import org.ops4j.pax.repository.typed.TypedRepository;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

import static com.okidokiteam.gouken.simpleagent.SimpleAgentReference.*;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.*;

public class SimpleManagementAgent implements ManagementAgent {

    final private Store<InputStream> m_store;
    final private TypedRepository<Handle> m_target;

    @Inject
    public SimpleManagementAgent( TypedRepository<Handle> target, Store<InputStream> store )
    {
        m_store = store;
        m_target = target;
    }

    @Override
    public String toString()
    {
        return "[SimpleManagementAgent]";
    }

    /**
     * The dynamic partion created using tinybundles.
     * Also the static parts.
     */
    public ArtifactReference[] getRuntimeParts()
        throws RepositoryException
    {

        return new ArtifactReference[]{
            bakeMe(),
            makeReference( "org.apache.felix.dm" ),
            makeReference( "org.osgi.compendium" )
        };
    }

    private ArtifactReference makeReference( String term )
    {
        return new UntypedArtifactReference( term );
    }

    /**
     * Tinybundles in Action
     *
     * @return Artifact that has been made out of part of this component
     *
     * @throws org.ops4j.pax.repository.RepositoryException
     *          probs
     */
    private ArtifactReference bakeMe()
        throws RepositoryException

    {
        try {
            return new TypedArtifactReference(
                m_target.set(
                    SIMPLEAGENT, m_store.store(
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


}
