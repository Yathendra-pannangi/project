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
 *  Represents the F90707 (Event Definition Sub-types) table.
 */
public final class F907011_EventDefinitionSubTypes extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F907011";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F907011_EventDefinitionSubTypes TABLE = 
        new F907011_EventDefinitionSubTypes();
    
    /**  Event name (a.k.a. type) field.  */
    public static final JDBField EVNTNAME = new JDBField("EVNTNAME");
    
    /**  Event sub-name field.  */
    public static final JDBField EVNTSNAME = new JDBField("EVNTSNAME");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        EVNTNAME,
        EVNTSNAME,
    };

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private F907011_EventDefinitionSubTypes()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
