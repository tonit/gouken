package com.okidokiteam.gouken.plugin;

import com.okidokiteam.gouken.Vault;

/**
 * Vault with extra interface to embed plugin architectures into legacy systems.
 *
 * It is recommended to give Vault's start/stop cleanroom instances so that:
 * start,registerCallbacks(*),unregisterCallbacks(*),stop is true even without explicit user api interaction.
 */
public interface PluginVault extends Vault
{

    /**
     * Get plugin points that might have services installed.
     */
    VaultPluginPoint[] getPluginPoints();

    /**
     * Get notified when plugins are being activated or deactivated.
     *
     * @param callbacks
     */
    void registerCallbacks( PluginCallback... callbacks );

    /**
     * @param callbacks
     */
    void unregisterCallbacks( PluginCallback... callbacks );
}
