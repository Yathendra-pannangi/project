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
import java.io.Serializable;
import java.util.Hashtable;
//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
 import com.jdedwards.mgmt.agent.Server;
 import com.jdedwards.base.logging.E1Logger;
 import com.jdedwards.base.logging.JdeLog;
 import com.jdedwards.base.logging.log4j.LogUtils;
 import com.jdedwards.mgmt.agent.E1Agent;
import com.jdedwards.mgmt.agent.E1AgentUtils;
//=================================================
// Imports from org namespace
//=================================================

/**
 * Description of the class.
 */
public class EventMetrics implements Serializable
{
    //=================================================
    // Public static final fields.
    //=================================================

    /**  Metric name for the number of committed real-time events.  */
    public static final String NUM_COMMITTED_RT_EVENTS = "num.committed.rt.events";

    /**  Metric name for the number of committed XAPI events.  */
    public static final String NUM_COMMITTED_XAPI_EVENTS = "num.committed.xapi.events";

    /**  Metric name for the number of committed workflow events.  */
    public static final String NUM_COMMITTED_WORKFLOW_EVENTS = "num.committed.workflow.events";

    /**  Metric name for the number of committed Z events.  */
    public static final String NUM_COMMITTED_Z_EVENTS = "num.committed.z.events";

    //=================================================
    // Static class fields.
    //=================================================
     private static E1Logger sE1Logger =
         JdeLog.getE1Logger(FailedEventMessage.class.getName());
    //=================================================
    // Instance member fields.
    //=================================================

    private String mMatricName = null;

    private long mMatricCount = 0;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Returns the matric count.
     * @return long matric count
     */
    public long getMatricCount()
    {
        return mMatricCount;
    }

    /**
     * Returns the matric name.
     * @return String name
     */
    public String getMatricName()
    {
        return mMatricName;
    }

    /**
     * Sets the metrics count.
     * @param count long metrics count
     */
    public void setMatricCount(long count)
    {
        mMatricCount = count;
    }

    /**
     * Sets the metrics name.
     * @param name String matric name
     */
    public void setMatricName(String name)
    {
        mMatricName = name;
    }

    /**
     * Registers the event metric with the Management Console.
     * @param eventType EventType to register for unique MBean
     */
    public void registerObject(String eventType) {

        if(sE1Logger.isDebug())
        {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Registering rte_monitoring_event_metrics for event type: " + eventType , null, null, null);
        }
        if(E1AgentUtils.isRunningInSCFManagedEnviroment()) {
            Hashtable hash = new Hashtable();
            hash.put("eventType", eventType);

            /** Register the object with the management server using eventType as a unique key*/
            Server.getServer().registerRuntimeMetric(this, hash, "rte_monitoring_event_metrics");

            if(sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"rte_monitoring_event_metrics MBean registered.", null, null, null);
            }
        }

    }

}
