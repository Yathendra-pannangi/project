//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.db;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBTable;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Represents the F90710 (Event Transfer) table.
 */
public final class F90712_FailedEvent extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90712";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90712_FailedEvent TABLE = new F90712_FailedEvent();
    
    /**  EventID field.  */
    public static final JDBField EVNTID = new JDBField("EVNTID");
    
    /**  Event date/time field.  */
    public static final JDBField EVNTTIME = new JDBField("EVNTTIME");
    
    /**  Event sequence number field.  */
    public static final JDBField EVNTSEQ = new JDBField("EVNTSEQ");
    
    /**  Event name (a.k.a. type) field.  */
    public static final JDBField EVNTNAME = new JDBField("EVNTNAME");
    
    /** 
     *  Event type (a.k.a. category) field.
     *  <br>
     *  Note: The data dictionary item EVNTTYPE was reused for the category.
     *        This field really is the event category (e.g. RT, XAPI, etc.).
     */
    public static final JDBField EVNTTYPE = new JDBField("EVNTTYPE");
    
    /**  Event state field.  */
    public static final JDBField EVNTST = new JDBField("EVNTST");
    
    /**  Originating environment field.  */
    public static final JDBField ENV = new JDBField("ENV");
    
    /**  User field.  */
    public static final JDBField EVNTUSER = new JDBField("EVNTUSER");
    
    /**  Role (a.k.a. Group) field.  */
    public static final JDBField UGRP = new JDBField("UGRP");
    
    /**  Application field.  */
    public static final JDBField OBNM = new JDBField("OBNM");
    
    /**  Application version field.  */
    public static final JDBField VER = new JDBField("VER");
    
    /**  User session field.  */
    public static final JDBField EVNTSNS = new JDBField("EVNTSNS");
    
    /**  Scope field.  */
    public static final JDBField EVNTSCOPE = new JDBField("EVNTSCOPE");
    
    /**  Originating host field.  */
    public static final JDBField EVNTHOST = new JDBField("EVNTHOST");
    
    /**  Source routing field.  */
    public static final JDBField EVNTSRT = new JDBField("EVNTSRT");
    
    /**  Failure point field.  */
    public static final JDBField EVNTFPT = new JDBField("EVNTFPT");
    
    /**  Originating BSFN field.  */
    public static final JDBField EVNTBSFN = new JDBField("EVNTBSFN");
    
    /**  Function name field.  */
    public static final JDBField FCTNM = new JDBField("FCTNM");
    
    /**  Originating process ID field.  */
    public static final JDBField EVNTPRID = new JDBField("EVNTPRID");
    
    /**  Event data field.  */
    public static final JDBField EDATA = new JDBField("EDATA");
        
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        EVNTID,
        EVNTTIME,
        EVNTSEQ,
        EVNTNAME,
        EVNTTYPE,
        EVNTST,
        ENV,
        EVNTUSER,
        UGRP,
        OBNM,
        VER,
        EVNTSNS,
        EVNTSCOPE,
        EVNTHOST,
        EVNTSRT,
        EVNTFPT,
        EVNTBSFN,
        FCTNM,
        EVNTPRID,
        EDATA
    };
    
    //=======================================================================
    //  Note: The numeric values below must match those specified in
    //        the system/include/event.h C header file.
    //=======================================================================
    
    /**  Event state Committed. */
    public static final int STATE_COMMITTED = 3;
    
    /**  Event state InProcess. */
    public static final int STATE_IN_PROCESS = 4;
    
    /**  Event state Processed. */
    public static final int STATE_PROCESSED = 5;
    
    /**  Event state Failed. */
    public static final int STATE_FAILED = 6;
    
    /**  Real-time event category.  */
    public static final String CATEGORY_REAL_TIME = "RTE";
    
    /**  XAPI event category.  */
    public static final String CATEGORY_XAPI = "XAPI";
    
    /**  Workflow event category. */
    public static final String CATEGORY_WORKFLOW = "WF";
    
    /**  Z event category.  */
    public static final String CATEGORY_Z = "ZFILE";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90712_FailedEvent()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
