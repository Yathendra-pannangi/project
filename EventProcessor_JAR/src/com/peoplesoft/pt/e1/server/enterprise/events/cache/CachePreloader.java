//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.cache;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.util.IJVMLifecycleListener;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Preloads the event caches upon JVM startup.
 */
public class CachePreloader implements IJVMLifecycleListener
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(CachePreloader.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Preloads caches at startup.
     */
    public void startup()
    {
        if(sE1Logger.isDebug())
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Preloading event caches", null, null, null);
        try
        {
            SubscriberCache.getInstance().preload();
        }
        catch (EventProcessingException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to preload subscriber cache: " + e.getMessage(), null, null, e);
        }
        try
        {
            EventTypeCache.getInstance().preload();
        }
        catch (EventProcessingException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to preload event type cache: " + e.getMessage(),null, null, e);
        }
		if(sE1Logger.isDebug())
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event caches preloaded", null, null, null);
    }

    /**
     *  Does nothing.
     */
    public void shutdown()
    {}

}
