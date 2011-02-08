package com.okidokiteam.gouken.it;import java.io.InputStream;import com.google.inject.AbstractModule;import com.google.inject.Guice;import com.google.inject.Injector;import com.google.inject.Key;import com.google.inject.Module;import com.google.inject.TypeLiteral;import com.okidokiteam.gouken.Vault;import com.okidokiteam.gouken.kernel.CoreVault;import com.okidokiteam.gouken.simpleagent.SimpleAgentModule;import org.junit.Test;import org.osgi.service.packageadmin.PackageAdmin;import org.ops4j.pax.repository.Provider;import org.ops4j.pax.repository.base.helpers.UncachedProvider;import org.ops4j.pax.repository.guice.RepositoryBaseModule;import org.ops4j.pax.repository.guice.TarballModule;import static com.google.inject.name.Names.*;/** * This is about how it all fits together using raw corevault (and not one of the assemblies) * * Steps: * - Create a Guice Injector wiring up all the Modules * - get the vault and launch. */public class SimpleWorkflowTest {    @Test    public void testSimpleWorkflowTest()    {        Vault<PackageAdmin> admin = bootVault(setup("/"));    }    private Vault<PackageAdmin> bootVault( Module setup )    {        Injector injector = Guice.createInjector( setup );        return injector.getInstance( Key.get( new TypeLiteral<Vault<PackageAdmin>>() {}  ) );    }    private Module setup( final String repoSource )    {        return new AbstractModule() {            @Override            protected void configure()            {                install(new SimpleAgentModule());                install( new RepositoryBaseModule() );                install(new TarballModule());                bind( new TypeLiteral<Provider<InputStream>>() {} ).annotatedWith( named( "repository" ) ).toInstance( new UncachedProvider<InputStream>( getClass().getResourceAsStream( repoSource ) ) );                bind( new TypeLiteral<Vault<PackageAdmin>>() { }).to( new TypeLiteral<CoreVault<PackageAdmin>>() {} );            }        };    }}