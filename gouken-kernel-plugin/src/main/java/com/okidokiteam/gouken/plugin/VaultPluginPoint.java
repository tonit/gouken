package com.okidokiteam.gouken.plugin;

/**
 *
 */
public interface VaultPluginPoint<T>
{

    /**
     * Type of this endpoint.
     */
    T getType();

    /**
     * Installed and readily usable services, provided by the underlying vault.
     */
    T[] getPlugins();
}
