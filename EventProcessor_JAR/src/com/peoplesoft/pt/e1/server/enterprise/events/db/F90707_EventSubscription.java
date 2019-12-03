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
 *  Represents the F90707 (Event Subscription) table.
 */
public final class F90707_EventSubscription extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90707";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90707_EventSubscription TABLE = new F90707_EventSubscription();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  Subscription name field.  */
    public static final JDBField EVNTSBNM = new JDBField("EVNTSBNM");
    
    /**  Description field.  */
    public static final JDBField EVNTSBDC = new JDBField("EVNTSBDC");
    
    /**  Active/Inactive field.  */
    public static final JDBField EVNTACT = new JDBField("EVNTACT");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        EVNTSBNM,
        EVNTSBDC,
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
    private F90707_EventSubscription()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
