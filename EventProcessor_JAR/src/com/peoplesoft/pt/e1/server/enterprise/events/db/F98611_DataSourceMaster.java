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
 *  Represents the F98611 (Data Source Master) table.
 */
public final class F98611_DataSourceMaster extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F98611";

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F98611_DataSourceMaster TABLE = new F98611_DataSourceMaster();
    
    /**  Data source type field.  */
    public static final JDBField OCM1 = new JDBField("OCM1");
    
    /**  Host name field.  */
    public static final JDBField SRVR = new JDBField("SRVR");
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        OCM1,
        SRVR,
    };
    
    /**  String value in the database that indicates a logical data source. */
    public static final String LOGICAL_DATA_SOURCE = "SVR";
    
    /**  String value in the database that indicates a database data source. */
    public static final String DATABASE_DATA_SOURCE = "DB";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Private constructor to prevent instances from being created.
     */
    private F98611_DataSourceMaster()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================

}
