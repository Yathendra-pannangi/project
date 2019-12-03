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
 *  Represents the F90701 (Event Definition) table.
 */
public final class F90701_EventDefinition extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90701";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90701_EventDefinition TABLE = new F90701_EventDefinition();
    
    /**  Event name (a.k.a. type) field.  */
    public static final JDBField EVNTNAME = new JDBField("EVNTNAME");
    
    /**  Event description field.  */
    public static final JDBField EVNTDESC = new JDBField("EVNTDESC");
    
    /**  Event type (a.k.a. category) field.  */
    public static final JDBField EVNTTYPE = new JDBField("EVNTTYPE");
    
    /**  Aggrate field.  */
    public static final JDBField EVNTAGG = new JDBField("EVNTAGG");
    
    /**  Data structure field.  */
    public static final JDBField EVNTDS = new JDBField("EVNTDS");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        EVNTNAME,
        EVNTDESC,
        EVNTTYPE,
        EVNTAGG,
        EVNTDS
    };
    
    /**  String value in the database that indicates a container event. */
    public static final String CONTAINER_STRING = "CONTAINER";
    
    /**  String value in the database that indicates a single event. */
    public static final String SINGLE_STRING = "SINGLE";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90701_EventDefinition()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
