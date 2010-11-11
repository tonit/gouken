package com.okidokiteam.gouken;

/**
 * An agent running inside osgi
 */
public interface VaultAgent
{

    /**
     * @param configuration configuration you want to issue to the vault.
     *
     * @throws KernelException in case of a problem
     */
    void update( VaultConfiguration configuration )
        throws KernelException;
}
