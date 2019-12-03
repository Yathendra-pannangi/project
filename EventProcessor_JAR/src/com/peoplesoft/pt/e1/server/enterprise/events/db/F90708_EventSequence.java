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
 *  Represents the F90708 (Event Sequence) table.
 */
public final class F90708_EventSequence extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90708";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90708_EventSequence TABLE = new F90708_EventSequence();
    
    /**  LastSequence field.  */
    public static final JDBField EVNTSEQ = new JDBField("EVNTSEQ");
    
        
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
    EVNTSEQ
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
    private F90708_EventSequence()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
