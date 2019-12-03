//============================================================================
//
//Copyright © [2004]
//PeopleSoft, Inc.
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.common.events.monitoring;

//=================================================
// Imports from java namespace
//=================================================
 import java.util.Hashtable;
//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;

import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.jdedwards.mgmt.agent.Server;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Contains a failed event.
 */
public class FailedEventMessage extends EventMessage
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
     private static E1Logger sE1Logger =
         JdeLog.getE1Logger(FailedEventMessage.class.getName());
    //=================================================
    // Instance member fields.
    //=================================================
    private String mFailedMessage = "";
    private String mState = "Failed " + 6;
    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    /**
     * This method returns the failed event message in transfer events table.
     * @return Failed Event Message
     */
    public String getFailedMessage()
    {
        return mFailedMessage;
    }

    /**
     * This method returns the failed events state.
     * @return String Failed Event State
     */
    public String getState()
    {
        return mState;
    }

    /**
     * Sets the failed event message which it read from transfer events table.
     * @param message Message to be set
     */
    public void setFailedMessage(String message)
    {
        mFailedMessage = message;
    }
    /**
     * Registers the failed event message with the Management Console.
     * @param eventID EventID to register for unique MBean
     */
    public void registerObject(String eventID) {

        if(sE1Logger.isDebug())
        {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Registering rte_monitoring_failed_event_info for eventID: " + eventID , null, null, null);
        }
        if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {

            Hashtable hash = new Hashtable();
            hash.put("eventId", eventID);

            /** Register the object with the management server using eventId as a unique key*/
            Server.getServer().registerRuntimeMetric(this, hash, "rte_monitoring_failed_event_info");

            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_failed_event_info MBean registered.", null, null, null);
            }

        }
    }

}
