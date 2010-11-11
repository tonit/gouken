package com.okidokiteam.gouken;

/**
 * A vault usually has a single, client side, configuration source that builds and tracks packages.
 * You get an instance when starting the vault.
 * It is valid for the lifetime of the started vault.
 */
public interface VaultConfigurationSource
{

    public VaultConfiguration build()
        throws KernelException;

}
