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
 *  Represents the F907015 (Event QueueAssignment) table.
 */
public final class F90715_EventQueueAssignment extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90715";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90715_EventQueueAssignment TABLE = new F90715_EventQueueAssignment();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  JNDI Name field.  */
    public static final JDBField JNDINM = new JDBField("JNDINM");
    
    /**  Assigned field.  */
    public static final JDBField EVNTQA = new JDBField("EVNTQA");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        JNDINM,
        EVNTQA
    };
    
    /**  String value in the database that indicates an assigned queue. */
    public static final String ASSIGNED_TRUE_STRING = "Y";
    
    /**  String value in the database that indicates an unassigned queue. */
    public static final String ASSIGNED_FALSE_STRING = "N";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90715_EventQueueAssignment()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
