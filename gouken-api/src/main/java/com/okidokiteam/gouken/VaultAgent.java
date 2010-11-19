package com.okidokiteam.gouken;

import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.RepositoryException;

/**
 * An agent defines how a Vault is being managed.
 * You need to issue an agent instance upon Vault.start.
 * Vaults itself are agnostic to their managing agents.
 * 
 */
public interface VaultAgent
{

    /**
     * List of artifacts that may be used by the vault to install the management agent
     * 
     * @throws RepositoryException in case artifacts cannot be resolved upon request.
     * 
     */
    Artifact[] getArtifacts() throws RepositoryException;
}
