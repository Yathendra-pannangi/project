//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.transferagent;

//=================================================
//Imports from java namespace
//=================================================
import java.util.Timer;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.server.enterprise.events.util.IJVMLifecycleListener;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Starts and stops the transfer agent timer along with preloading the
 *  data caches.
 */
public class TransferAgentJVMLifecycleListener implements IJVMLifecycleListener
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(TransferAgentJVMLifecycleListener.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private TriggerTimerTask  mTimerTask;
    
    private Timer mTimer;
    
    private long mIntervalTime = 0;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Perform start-up processing.
     */
    public void startup()
    {        
        //
        //  Send an initial trigger at start-up.
        //
        mTimerTask = TriggerTimerTask.getInstance();
     //   mTimerTask.sendTrigger();

        //
        //  Start the timer if configured to do so.
        //
        mIntervalTime = JdeProperty.getProperty("EVENTS", "TriggerListenerDelay", 1000);
        if (mIntervalTime > 0)
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Starting event transfer agent trigger timer", null,null,null);
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(mTimerTask, mIntervalTime, mIntervalTime);
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event transfer agent trigger timer started, interval = " + mIntervalTime, null,null,null);
        }
        else
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Event transfer agent timer configured to not run", null,null,null);
        }
    }

    /**
     *  Perform shutdown processing.
     */
    public void shutdown()
    {
        if (mTimer != null)
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Stopping transfer agent timer task", null,null,null);
            mTimer.cancel();
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Transfer agent timer task stoped", null,null,null);
        }
        mTimerTask.shutdown();
    }
}
