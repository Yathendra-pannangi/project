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
//Imports from java namespace
//=================================================
import java.io.Serializable;
import java.util.Hashtable;
//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
 import com.jdedwards.mgmt.agent.Server;
 import com.jdedwards.base.logging.E1Logger;
 import com.jdedwards.base.logging.JdeLog;
 import com.jdedwards.base.logging.log4j.LogUtils;
 import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
//=================================================
//Imports from org namespace
//=================================================

/**
 * This class contanins the subscriber monitoring info.
 */
public class SubscriberMonitoringInfo implements Serializable
{
    //=================================================
    // Non-public static class fields.
    //=================================================
     private static E1Logger sE1Logger =
         JdeLog.getE1Logger(FailedEventMessage.class.getName());
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    private String mUsername;

    private String mDescription;

    private boolean mIsActive;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    /**
     * Returns the subcriber description.
     * @return String description.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * Returns True if subcriber is active else returns False.
     * @return boolean true/false
     */
    public boolean isActive()
    {
        return mIsActive;
    }

    /**
     * Returns the subcriber user name.
     * @return String user name
     */
    public String getUsername()
    {
        return mUsername;
    }

    /**
     * Sets the subcriber description.
     * @param decription String
     */
    public void setDescription(String decription)
    {
        mDescription = decription;
    }

    /**
     * Sets if subcribers is active or inactive.
     * @param active true is active else false
     */
    public void setIsActive(boolean active)
    {
        mIsActive = active;
    }

    /**
     * Sets subcriber user name.
     * @param userName String user name
     */
    public void setUsername(String userName)
    {
        mUsername = userName;
    }

    /**
     * Returns the string representation of Subcriber Info.
     * @return String subcriber information
     */
    public String toString()
    {
        return mUsername + " " + mDescription + " " + mIsActive;
    }

    /**
     * Registers the event metric with the Management Console.
     * @param subscriberID subscriberID to register for unique MBean
     */
    public void registerObject(String subscriberID) {

        if(sE1Logger.isDebug())
        {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Registering rte_monitoring_subscriber_info for Subscriber ID: " + subscriberID , null, null, null);
        }
        if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {

            Hashtable hash = new Hashtable();
            hash.put("subscriberID", subscriberID);

            /** Register the object with the management server using subscriberID as a unique key*/
            Server.getServer().registerRuntimeMetric(this, hash, "rte_monitoring_subscriber_info");

            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_subscriber_info MBean registered.", null, null, null);
            }

        }

    }

}
