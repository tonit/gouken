package com.okidokiteam.gouken;

import org.ops4j.pax.repository.Artifact;
import org.ops4j.pax.repository.InputStreamSource;

/**
 * Configuration of a desired vault. After issuing this to a vault, it will adapt that state.
 * This is the only way to change the state of a vault.
 * In the OSGi internals, this is very much like a DeploymentPackage.
 */
public interface VaultConfiguration
{
    
    InputStreamSource get();

}
