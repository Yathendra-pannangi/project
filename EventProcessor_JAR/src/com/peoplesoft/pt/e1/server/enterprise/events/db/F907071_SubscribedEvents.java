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
 *  Represents the F907071 (Subscribed Events) table.
 */
public final class F907071_SubscribedEvents extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F907071";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F907071_SubscribedEvents TABLE = new F907071_SubscribedEvents();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  Subscription name field.  */
    public static final JDBField EVNTSBNM = new JDBField("EVNTSBNM");
    
    /**  Event name (a.k.a. type) field.  */
    public static final JDBField EVNTNAME = new JDBField("EVNTNAME");
    
    /**  Event type (a.k.a. category) field.  */
    public static final JDBField EVNTTYPE = new JDBField("EVNTTYPE");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        EVNTSBNM,
        EVNTNAME,
        EVNTTYPE
    };
    
    /**  Wilecard value for event type.  */
    public static final String WILDCARD_TYPE = "*ALL";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private F907071_SubscribedEvents()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
