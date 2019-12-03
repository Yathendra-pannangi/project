//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.jdenet;

//=================================================
// Imports from java namespace
//=================================================
import java.util.HashMap;
import java.util.Iterator;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.ListenerContext;
import com.jdedwards.system.net.ProcessKernel;
import com.jdedwards.system.net.ProcessKernelException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This class listens for jdenet messages and dispatches them to
 * the corresponding event system handler class.
 */
public class JdeNetListener extends ProcessKernel
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(JdeNetListener.class.getName());
    
    private static final Integer TRIGGER = new Integer(15501);
    
    private static final Integer RELOAD_EVENT_CACHE = new Integer(15502);
    
    private static final Integer RELOAD_SUBSCRIBER_CACHE = new Integer(15503);

    //=================================================
    // Public static final fields.
    //=================================================   

    //=================================================
    // Instance member fields.
    //================================================= 
    
    private boolean mInitialized = false;
    
    private HashMap mHandlers = new HashMap();

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     */
    public JdeNetListener()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  This method is the main processing method for JdeNet messages.
     * 
     *  @param  m the message
     * 
     *  @throws ProcessKernelException  Error during processing.
     * 
     *  @see ProcessKernel
     */
    public void process(JdeMsg m) throws ProcessKernelException
    {
        if (!mInitialized)
        {
            throw new ProcessKernelException("event system JdeNetListener not initialized");
        }
        
        Integer msgType = new Integer(m.getMsgHdr().getMsgType());
        
        if (sE1Logger.isDebug())
        {
			sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event JdeNet listener received msg type: " + msgType, null,null, null);
        }
        
        ProcessKernel handler = (ProcessKernel)mHandlers.get(msgType);
        if (handler == null)
        {
            String msg = "no event JdeNet handler for msg type: " + msgType;
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null,null, null);
            throw new ProcessKernelException(msg);
        }
        
        handler.process(m);
    }

    /**
     *  Initializes the message listener.
     * 
     *  @param  context  Listener context.
     * 
     *  @throws ProcessKernelException  Error during processing.
     */
    public void initialize(ListenerContext context) throws ProcessKernelException
    {
        ProcessKernel handler = null;
        
        //
        //  Trigger message handler.
        //
        handler = new TriggerHandler();
        handler.initialize(context);
        mHandlers.put(TRIGGER, handler);
        
        //
        //  Reload event cache message handler.
        //
        handler = new ReloadEventCacheHandler();
        handler.initialize(context);
        mHandlers.put(RELOAD_EVENT_CACHE, handler);
        
        //
        //  Reload subscriber cache message handler.
        //
        handler = new ReloadSubscriberCacheHandler();
        handler.initialize(context);
        mHandlers.put(RELOAD_SUBSCRIBER_CACHE, handler);
        
        mInitialized = true;
    }

    /**
     *  Shuts down the listener.
     * 
     *  @throws ProcessKernelException  Error during processing.
     */
    public void shutdown() throws ProcessKernelException
    {
        mInitialized = false;
        
        for (Iterator iter = mHandlers.values().iterator(); iter.hasNext();)
        {
            ProcessKernel handler = (ProcessKernel)iter.next();
            handler.shutdown();
        }
    }
}
