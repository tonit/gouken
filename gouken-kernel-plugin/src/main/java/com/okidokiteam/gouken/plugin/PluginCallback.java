package com.okidokiteam.gouken.plugin;

/**
 *
 */
public interface PluginCallback<T>
{

    void activated( T service );

    void deactivated( T service );
}
