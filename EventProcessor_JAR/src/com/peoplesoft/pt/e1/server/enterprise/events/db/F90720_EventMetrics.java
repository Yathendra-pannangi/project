//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.enterprise.events.db;

import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBTable;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================

/**
 * Represents the F90720 (Event Metrics) table.
 */
public final class F90720_EventMetrics extends JDBTable
{
    //=================================================
    // Public static final fields.
    //=================================================
    private static final String TABLE_NAME = "F90720";
    //=================================================
    // Static class fields.
    //=================================================
    /**  Table instance.  */
    public static final F90720_EventMetrics TABLE = new F90720_EventMetrics();

    /**  Event metric name field.  */
    public static final JDBField EVENT_METRIC_NAME = new JDBField("EVTMNM");

    /**  Event metric category name field.  */
    public static final JDBField EVENT_METRIC_CATEGORY = new JDBField("EVTMCAT");

    /**  Event metric count field.  */
    public static final JDBField EVENT_METRIC_COUNT = new JDBField("EVTMCNT");

    /**  Event metric caretion time field.  */
    public static final JDBField EVENT_METRIC_CREATIONTIME = new JDBField("UPMT");

    /**  Event metric caretion time field.  */
    public static final JDBField EVENT_METRIC_USER = new JDBField("USER");

    /**  Event metric caretion time field.  */
    public static final JDBField EVENT_METRIC_PID = new JDBField("PID");

    /**  Event metric caretion time field.  */
    public static final JDBField EVENT_METRIC_KEY = new JDBField("MKEY");

    /**  Event metric caretion time field.  */
    public static final JDBField EVENT_METRIC_CREATIONDATE = new JDBField("UPMJ");

    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    { 
        EVENT_METRIC_NAME,
        EVENT_METRIC_CATEGORY,
        EVENT_METRIC_COUNT,
        EVENT_METRIC_CREATIONTIME,
        EVENT_METRIC_USER,
        EVENT_METRIC_PID,
        EVENT_METRIC_KEY,
        EVENT_METRIC_CREATIONDATE
    };
    
    /**  Category for event metrics.  */
    public static final String CATEGORY_EVENTS = "events";
    
    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90720_EventMetrics()
    {
        super(TABLE_NAME);
    }
    //=================================================
    // Methods.
    //=================================================
}
