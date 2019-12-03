//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.monitoringservice;

//=================================================
// Imports from java namespace
//=================================================
import java.util.HashMap;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.EventMetrics;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90720_EventMetrics;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Description of the class.
 */
public class EventMetricsDBLookup
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventMetricsDBLookup.class.getName());

    /**  Fields to select.  */
    private static final JDBField[] SELECT_FIELDS = new JDBField[] 
    { 
        F90720_EventMetrics.EVENT_METRIC_NAME, 
        F90720_EventMetrics.EVENT_METRIC_CATEGORY, 
        F90720_EventMetrics.EVENT_METRIC_COUNT,
        F90720_EventMetrics.EVENT_METRIC_CREATIONTIME,
        F90720_EventMetrics.EVENT_METRIC_USER, 
        F90720_EventMetrics.EVENT_METRIC_PID, 
        F90720_EventMetrics.EVENT_METRIC_KEY, 
        F90720_EventMetrics.EVENT_METRIC_CREATIONDATE, 
    };
    
    //=================================================
    // Instance member fields.
    //=================================================

    private HashMap mResult = new HashMap();
    
    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get events metrics.
     * @param  connection  Database connection to use for retreiving committed events.
     * @return  A hashmap of Name-Value pair of events metrics.
     * @throws EventProcessingException  Error getting events metrics from the database.
     */
    HashMap getEventsMetrics(JDBDatabaseAccess connection) throws EventProcessingException
    {
        try
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Select events metrics from database begin.", null, null, null);
            }
            JDBResultSet resultSet =
                connection.select(F90720_EventMetrics.TABLE, SELECT_FIELDS);
            try
            {
                while (resultSet.hasMoreRows())
                {
                    //
                    //  Build a Event Metrics object for each row.
                    //
                    JDBFieldMap map = resultSet.fetchNext();
                    buildEventMetrics(map);                    
                }
            }
            finally
            {
                try
                {
                    //
                    //  Close the result set.
                    //
                    resultSet.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(),null, null, e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Exception from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, null);
            throw new EventProcessingException(msg,e);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Select failed events from database complete.", null, null, null);
        }        
        return mResult;
    }

    private void buildEventMetrics(JDBFieldMap map) throws JDBException
    {
        String metricName = map.getString(F90720_EventMetrics.EVENT_METRIC_NAME).trim();
        tagMathNumeric metricCount = 
            (tagMathNumeric)map.getValue(F90720_EventMetrics.EVENT_METRIC_COUNT);
        EventMetrics metrics = new EventMetrics();
        metrics.setMatricName(metricName);
        metrics.setMatricCount(metricCount.asBigDecimal().longValue());       
        mResult.put(metricName, metrics);
    }
}
