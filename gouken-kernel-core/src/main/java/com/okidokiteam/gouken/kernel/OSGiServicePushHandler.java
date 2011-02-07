package com.okidokiteam.gouken.kernel;import java.lang.reflect.InvocationHandler;import java.lang.reflect.InvocationTargetException;import java.lang.reflect.Method;import com.okidokiteam.gouken.KernelException;import org.osgi.framework.BundleContext;import org.osgi.framework.InvalidSyntaxException;import org.osgi.framework.ServiceReference;import org.slf4j.Logger;import org.slf4j.LoggerFactory;/** * */public class OSGiServicePushHandler<PUSHTYPE> implements InvocationHandler {    private static final Logger LOG = LoggerFactory.getLogger( CoreVault.class );    final private Class<PUSHTYPE> m_pushServiceType;    final private BundleContext m_ctx;    public OSGiServicePushHandler( Class<PUSHTYPE> pushType, BundleContext bundleContext )    {        m_ctx = bundleContext;        m_pushServiceType = pushType;    }    /**     * {@inheritDoc} Delegates the call to remote bundle context.     */    public Object invoke( final Object proxy,                          final Method method,                          final Object[] params )        throws Exception    {        try {            return dynamicService(                method,                params            );        } catch( KernelException e ) {            throw e;        } catch( Exception t ) {            throw new KernelException( "Internal exception", t );        }    }    private Object dynamicService( Method method, Object[] params )        throws InvalidSyntaxException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, KernelException    {        // find that service and invoke the method:        LOG.info( "Trying to locate service " + m_pushServiceType.getName() + " for method " + method.getName() );        ServiceReference reference = null;        Object ret = null;        long start = System.currentTimeMillis();        long timeout = 1000L;        long deadline = start + timeout;        while( reference == null && ( System.currentTimeMillis() < deadline ) ) {            reference = m_ctx.getServiceReference( m_pushServiceType.getName() );            if( reference != null ) {                Object o = m_ctx.getService( reference );                try {                    ret = method.invoke( o, params );                } finally {                    m_ctx.ungetService( reference );                }            }        }        if( reference == null ) {            throw new KernelException( "Timeout for " + m_pushServiceType.getName() + ". Service has not been aquired on time." );        }        else {            return ret;        }    }}