/*
 * Copyright 2011 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import static org.ops4j.pax.tinybundles.core.TinyBundles.*;

/**
 * Example of an MA implementation that uses external references (bundles) as well as a
 * Tinybundles-made on-the-fly bundle for providing the {@code PushService} {@link MyPush}.
 */
public class SimpleManagementAgent implements ManagementAgent {

    /**
     * We do use tinybundles here for creating a bundle out of thin air- kind of.
     * In order to materialize and not make the repository (see below) depend on tinybundles we need
     * store it somehere. Using OPS4J Base Store is a good way to do it.
     */
    final private Store<InputStream> m_store;

    /**
     * Using this also means that our resolver ({@link com.okidokiteam.gouken.GoukenResolver}) should recognize this repository
     * when executing "queries".
     */
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

        /**
         * A small note:
         * Technically it would work to supply an anonymous typed reference:
         * {@code makeReference(new TypedReference() {}, bakeBundle() )}
         *
         * because we return the reference to the client (which then he knows about).
         * But using a dedicated type allows others to find this resource as well.
         */
        return new ArtifactReference[]{
            makeReference( SIMPLEAGENT, bakeBundle() ), // typed !
            //makeReference( "org:apache.felix:org.apache.felix.dm" ), // untyped
            //makeReference( "org.apache.felix:org.osgi.compendium" ) // untyped
        };
    }

    private ArtifactReference makeReference( String term )
    {
        return new UntypedArtifactReference( term );
    }

    /**
     * Returns a ready to use reference. This reference will be wired to the given handle.
     * It actually sets the connection (defined by this methods params) in given {@link TypedRepository}.
     * So this will just be found if this applications resolver consults this repo at all.
     *
     * @param reference how this artifact is referenced as.
     * @param handle    what you want reference to return when someone asks for it.
     *
     * @return Artifact that has been made out of part of this component
     *
     * @throws org.ops4j.pax.repository.RepositoryException
     *          probs
     */
    private ArtifactReference makeReference( TypedReference reference, Handle handle )
        throws RepositoryException

    {
        return new TypedArtifactReference(
            m_target.set( reference, handle )
        );
    }

    /**
     * This is Tinybundles Action, then materializing the result into {@link Store}.
     * You will get the handle.
     *
     * @return Handle of a materialized bundle created on the fly. Use {@link Store} to get the actual bundle stream.
     * @throws RepositoryException in case putting the bundle to disk fails.
     */
    private Handle bakeBundle()
        throws RepositoryException
    {
        try {
            return m_store.store(
                bundle( withBnd())
                    .add( MyPush.class )
                    .add( MyPushImpl.class )
                    .set( "Export-Package", MyPush.class.getPackage().getName() )
                    .build( )
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
