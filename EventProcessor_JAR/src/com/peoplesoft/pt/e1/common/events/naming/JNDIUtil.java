//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.naming;

//=================================================
//Imports from java namespace
//=================================================
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Utility class for performing JNDI operations.
 */
public final class JNDIUtil
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(JNDIUtil.class.getName());
    
    private static final String SECTION = "EVENTS";
    
    private static final String CONTEXT_FACTORY = "initialContextFactory";
    
    private static final String PROVIDER_URL = "jndiProviderURL";
    
    private static final String CONTAINER_PREFIX = "java:comp/env/";

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    private JNDIUtil()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  <p>
     *  Build a bootstrap JNDI initial context.
     *  </p><p>
     *  This method loads the JNDI bootstrap information it needs using the
     *  <code>com.jdedwards.system.lib.JdeProperty</code> class.  See that class
     *  for information on how to configure which .ini file it loads properties
     *  from.
     *  </p><p>
     *  The JNDI bootstrap properties are read from the .ini file from a section
     *  titled <code>JNDI</code>.  The following properties must be defined within
     *  this section:
     *  </p>
     *  <ul>
     *      <li><code>initialContextFactory</code> - The fully qualified class name
     *      of the JNDI initial context factory class.</li>
     *      <li><code>jndiProviderURL</code> - The JNDI server's connection URL.</li>
     *  </ul>
     * 
     *  @return  A bootstrap initial context.
     * 
     *  @throws NamingException  Error during processing.
     */
    private static InitialContext getBootstrapInitialContext() throws NamingException
    {
        Hashtable environment = new Hashtable();
        
        String contextFactory = JdeProperty.getProperty(SECTION, CONTEXT_FACTORY, null);
        if (contextFactory != null)
        {
            environment.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        }
        
        String providerURL = JdeProperty.getProperty(SECTION, PROVIDER_URL, null);
        if (providerURL != null)
        {
            environment.put(Context.PROVIDER_URL, providerURL);
        }
        
        //
        //  Add a property to prevent WebSphere from caching JNDI objects.  This is
        //  needed for the case where this class is being used in a WebSphere client
        //  process and its trying to reconnect after WebSphere has been restarted.  Without
        //  this setting the context will continue to return the "old" EJB home object
        //  which will fail to return an EJB instance.
        //
        environment.put("com.ibm.websphere.naming.jndicache.cacheobject", "none");
        
        //Prime the system -- this seems to help IBM's VM realize that the code is in fact available
        if ("com.ibm.websphere.naming.WsnInitialContextFactory".equals(contextFactory))
        {
            try
            {
                ClassLoader cls = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
                {
                    public Object run()
                    {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
                if (cls == null)
                {
                    cls = JNDIUtil.class.getClassLoader();
                    Thread.currentThread().setContextClassLoader(cls);
                }
            }
            catch (Throwable t)
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                    "Error priming the WsnInitialContextFactory, defaulting to IBM instantiation",
				   null,null,t);
            }
        }

        //standard context instantiation
        InitialContext context = new InitialContext(environment);       
        
        return context;
    }
    
    /**
     *  Perform a JNDI lookup of an object.  The default JNDI <code>InitialContext</code>
     *  is tried with the <code>java:comp/env/</code> prefix first.  If the object is not
     *  found then the default context wihtout the prefix is tried.  If the object still
     *  has not been found then the bootstrap context is used.
     * 
     *  @param name  The JNDI name of the object.
     * 
     *  @return  The requested object.
     * 
     *  @throws NamingException  if an error is encountered.
     */
    public static Object lookup(String name)
        throws NamingException
    {
        //
        //  Try to lookup the object in the default context.
        //
        Object object = null;
        String foundName = name;  
        try
        {        
            InitialContext defaultContext = new InitialContext();
            
            //
            //  Try the default context without the prefix.
            //
            String prefixName = CONTAINER_PREFIX + name;
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Looking for object " + prefixName + " in default JNDI context", null,null, null);
            }
                
            try
            {
                object = defaultContext.lookup(name);
                foundName = prefixName;
            }
            catch (NamingException e)
            {
                //  Object not found without prefix.
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Object: " + prefixName + " not found in default JNDI context", null,null, null);
                }
            }
            
            //
            //  Try the default context with the prefix.
            //
            if (object == null)
            {
                try
                {
                    if (sE1Logger.isDebug())
                    {
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Looking for object " + name + " in default JNDI context", null,null, null);
                    }
                    object = defaultContext.lookup(prefixName);
                }
                catch (NamingException e)
                {
                    //  Object not found in default context.
                    if (sE1Logger.isDebug())
                    {
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Object: " + name + " not found in default JNDI context", null,null, null);
                    }
                }
            }
        }
        catch(Exception ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"DefaultContext init failed using BootStrap. " + ex.getMessage(), null,null, ex);
        }   

        //
        //  Try the bootstrap context.
        //
        if (object == null)
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Looking for object " + name + " in bootstrap JNDI context", null,null, null);
            }
            
            InitialContext bootstrapContext = getBootstrapInitialContext();
            object = bootstrapContext.lookup(name);
        }
        
        if (object == null)
        {
            throw new NameNotFoundException();
        }
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JNDI object " + foundName + " found", null,null, null);
        }
        
        return object;
    }
}
