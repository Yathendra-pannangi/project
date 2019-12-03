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
 *  Represents the F90706 (Event Subscriber) table.
 */
public final class F90705_EventActivationStatus extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90705";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90705_EventActivationStatus TABLE = new F90705_EventActivationStatus();
    
    /**  Environment field.  */
    public static final JDBField ENV = new JDBField("ENV");
    
    /**  Event Name (a.k.a. Type) field.  */
    public static final JDBField EVNTNAME = new JDBField("EVNTNAME");
    
    /**  Event Type (a.k.a. category) field.  */
    public static final JDBField EVNTTYPE = new JDBField("EVNTTYPE");
    
    /**  Event Status field.  */
    public static final JDBField EVNTACT = new JDBField("EVNTACT");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        ENV,
        EVNTNAME,
        EVNTTYPE,
        EVNTACT
    };
    
    /**  String value in the database that indicates an active status. */
    public static final String ACTIVE_STRING = "AV";
    
    /**  String value in the database that indicates an inactive status. */
    public static final String INACTIVE_STRING = "NA";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90705_EventActivationStatus()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
