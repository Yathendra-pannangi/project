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
 *  Represents the F907072 (Subscribed Environments) table.
 */
public final class F907072_SubscribedEnvironments extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F907072";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F907072_SubscribedEnvironments TABLE = new F907072_SubscribedEnvironments();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  Subscription name field.  */
    public static final JDBField EVNTSBNM = new JDBField("EVNTSBNM");
    
    /**  Environment field.  */
    public static final JDBField ENV = new JDBField("ENV");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        EVNTSBNM,
        ENV
    };
    
    /**  Wildcard value for the subscribed to environment.  */
    public static final String WILDCARD_ENVIRONMENT = "*ALL";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private F907072_SubscribedEnvironments()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
