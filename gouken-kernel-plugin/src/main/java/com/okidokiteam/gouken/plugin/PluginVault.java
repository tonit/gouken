package com.okidokiteam.gouken.plugin;

import com.okidokiteam.gouken.Vault;

/**
 * Vault with extra interface to embed plugin architectures into legacy systems.
 *
 * It is recommended to give Vault's start/stop cleanroom instances so that:
 * start,registerCallbacks(*),unregisterCallbacks(*),stop is true even without explicit user api interaction.
 */
public interface PluginVault<T> extends Vault<T>
{

    /**
     * Get notified when plugins are being activated or deactivated.
     * Already active callbacks will be delivered uppon registration.
     * Its some kind of "post callback registration event mechanism".
     * Because of this you can use callbacks anytime and never lose a service.
     *
     * @param callbacks to be registered.
     */
    void registerCallbacks( PluginCallback... callbacks );

    /**
     * Remove this callback from the notification recipient list.
     * @param callbacks to be removed.
     */
    void unregisterCallbacks( PluginCallback... callbacks );
}
