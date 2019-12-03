//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

//=================================================
//Imports from java namespace
//=================================================
import java.math.BigDecimal;
import java.util.HashMap;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBCompositeSelection;
import com.jdedwards.database.base.JDBConjunctionOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldAssignment;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBParameterMarker;
import com.jdedwards.database.base.JDBSelection;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.monitoring.EventMetrics;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90720_EventMetrics;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Used to perform metrics updates in the database.
 */
public class MetricsManager
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(MetricsManager.class.getName());
    
    private static final JDBField[] METRIC_FIELDS = new JDBField[] 
    { 
        F90720_EventMetrics.EVENT_METRIC_NAME,
        F90720_EventMetrics.EVENT_METRIC_CATEGORY,
        F90720_EventMetrics.EVENT_METRIC_COUNT,
    };

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**
     *  Tag appended to the end of a subscriber's username to create
     *  their routed event metric name.
     */ 
    public static final String SUBSCRIBER_ROUTED_METRIC_TAG = ".routed.events";

    //=================================================
    // Instance member fields.
    //=================================================
    
    //  Map of event categories to metrics count key.
    private HashMap mCategoryMatricsMap = new HashMap();
    
    //  Parameter marker for updating the metrics table.
    private JDBParameterMarker mMetricNameMarker = null;
    
    //  Metrics selector.
    private JDBSelection mMetricSelection = null;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  <p>
     *  Increment the committed event metrics count in the database.
     *  </p><p>
     *  Note: the database operation performed by this method is not atomic.  It is 
     *  the caller's responsibility to start and commit a database transaction if
     *  the metric update needs to be atomic.
     *  </p>
     * 
     *  @param  connection  Database connection to use.
     * 
     *  @param  name   Metric name to increment.
     * 
     *  @param  count  Amount to increment the count by.
     * 
     *  @throws EventProcessingException  Error occured during meterics update.
     */
    public void incrementMetricCount(JDBDatabaseAccess connection, String name, int count)
        throws EventProcessingException
    {
        if (name == null)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"null event metric name", null, null,null);
            return;
        }
        
        //
        //  Get the current metric count.
        //
        long current = getCurrentMetricCount(connection, name);
        current += count;
        setMetricCount(connection, name, current);
    }
    
    /**
     *  Map event categories to their metric names.
     * 
     *  @param category  Event category.
     *
     *  @return  Metric name
     */
    public String mapCategoryToName(String category)
    {
        return (String)mCategoryMatricsMap.get(category);
    }
    
    /**
     *  Initialize objects used for metrics reporting.
     */
    public void initialize()
    {
        //
        //  Build a hash map the maps event categorys to their corresponding event count
        //  metric type.
        //
        mCategoryMatricsMap = new HashMap();
        mCategoryMatricsMap.put(
            F90710_EventTransfer.CATEGORY_REAL_TIME,
            EventMetrics.NUM_COMMITTED_RT_EVENTS);
        mCategoryMatricsMap.put(
            F90710_EventTransfer.CATEGORY_XAPI,
            EventMetrics.NUM_COMMITTED_XAPI_EVENTS);
        mCategoryMatricsMap.put(
            F90710_EventTransfer.CATEGORY_WORKFLOW,
            EventMetrics.NUM_COMMITTED_WORKFLOW_EVENTS);
        mCategoryMatricsMap.put(
            F90710_EventTransfer.CATEGORY_Z,
            EventMetrics.NUM_COMMITTED_Z_EVENTS);
        
        JDBFieldComparison category =
            new JDBFieldComparison(
                F90720_EventMetrics.EVENT_METRIC_CATEGORY,
                JDBComparisonOp.EQ,
                F90720_EventMetrics.CATEGORY_EVENTS);
        
        mMetricNameMarker = new JDBParameterMarker();
        JDBFieldComparison name =
            new JDBFieldComparison(
                F90720_EventMetrics.EVENT_METRIC_NAME,
                JDBComparisonOp.EQ,
                mMetricNameMarker);
        
        mMetricSelection = 
            new JDBCompositeSelection(category, JDBConjunctionOp.AND, name);
    }
    
    /**
     *  Returns the metric count. 
     *  @param connection JDBDatabaseAccess
     *  @param name Metric Name as a string
     *  @return long metric count
     *  @throws EventProcessingException ex
     */
    public long getCurrentMetricCount(JDBDatabaseAccess connection, String name)
        throws EventProcessingException
    {
        long value = 0;
        try
        {
            //
            //  Select the current metric value from the database.
            //
            mMetricNameMarker.clearValue();
            mMetricNameMarker.setValue(name);
            JDBFieldMap map =
                connection.selectSingleRow(
                    F90720_EventMetrics.TABLE,
                    METRIC_FIELDS,
                    mMetricSelection,
                    null);
            
            if (map == null)
            {
                insertMetric(connection, name);
                value = 0;
            }
            else
            {
                //
                //  Get the metric value from the field map.
                //
                tagMathNumeric valueMN =
                    (tagMathNumeric)map.getValue(F90720_EventMetrics.EVENT_METRIC_COUNT);
                value = valueMN.asBigDecimal().longValue();
            }
        }
        catch (JDBException e)
        {
            StringBuffer buffer = new StringBuffer(75);
            buffer.append("Error getting event metric value ");
            buffer.append(name);
            buffer.append(" from the database");
            String msg =  buffer.toString();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        return value;
    }
    
    private void setMetricCount(JDBDatabaseAccess connection, String name, long value)
        throws EventProcessingException
    {
        tagMathNumeric valueMN = new tagMathNumeric();
        valueMN.setBigDecimalValue(BigDecimal.valueOf(value));
        JDBFieldAssignment fieldMap = new JDBFieldAssignment();
        fieldMap.addField(F90720_EventMetrics.EVENT_METRIC_COUNT, valueMN);
        mMetricNameMarker.clearValue();
        mMetricNameMarker.setValue(name);
        try
        {
            connection.update(F90720_EventMetrics.TABLE, mMetricSelection, fieldMap);
        }
        catch (JDBException e)
        {
            StringBuffer buffer = new StringBuffer(75);
            buffer.append("Error updating event metric value ");
            buffer.append(name);
            buffer.append(" in the database");
            String msg =  buffer.toString();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
    
    private void insertMetric(JDBDatabaseAccess connection, String name)
        throws JDBException
    {
        tagMathNumeric valueMN = new tagMathNumeric();
        valueMN.setBigDecimalValue(BigDecimal.valueOf(0));
        JDBFieldAssignment fieldMap = new JDBFieldAssignment();
        fieldMap.addField(F90720_EventMetrics.EVENT_METRIC_COUNT, valueMN);
        fieldMap.addField(F90720_EventMetrics.EVENT_METRIC_NAME, name);
        fieldMap.addField(F90720_EventMetrics.EVENT_METRIC_CATEGORY, 
                          F90720_EventMetrics.CATEGORY_EVENTS);
        connection.insert(F90720_EventMetrics.TABLE, fieldMap);
    }
}
