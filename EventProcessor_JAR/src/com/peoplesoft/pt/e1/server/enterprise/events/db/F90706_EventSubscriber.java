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
public final class F90706_EventSubscriber extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90706";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90706_EventSubscriber TABLE = new F90706_EventSubscriber();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  Description field.  */
    public static final JDBField EVNTSBDC = new JDBField("EVNTSBDC");
    
    /**  Active/Inactive field.  */
    public static final JDBField EVNTACT = new JDBField("EVNTACT");
    
    /**  Transport type.  */
    public static final JDBField EVNTTDRIVE = new JDBField("EVNTTDRIVE");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        EVNTSBDC,
        EVNTACT,
        EVNTTDRIVE
    };
    
    /**  String value in the database that indicates an active status. */
    public static final String ACTIVE_STRING = "AV";
    
    /**  String value in the database that indicates an inactive status. */
    public static final String INACTIVE_STRING = "NA";
    
    /**  String value in the database for a Java Connector transport driver type.  */
    public static final String TRANSPORT_JAVA_CONNECTOR = "JAVACONN";
    
    /**  String value in the database for a COM Connector transport driver type.  */
    public static final String TRANSPORT_COM_CONNECTOR = "COMCONN";
    
    /**  String value in the database for a JDENET transport driver type.  */
    public static final String TRANSPORT_JDENET = "JDENET";
    
    /**  String value in the database for a MSMQ driver type.  */
    public static final String TRANSPORT_MSMQ = "MSMQ";
    
    /**  String value in the database for a MQ Series Queue driver type.  */
    public static final String TRANSPORT_MQSERIES_QUEUE = "MQSQ";

	/**  String value in the database for a JMS Topic type.  */
	public static final String TRANSPORT_JMS_TOPIC = "JMSTOPIC";

	/**  String value in the database for a JMSQUEUE type.  */

    public static final String TRANSPORT_JMS_QUEUE = "JMSQUEUE";

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90706_EventSubscriber()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
