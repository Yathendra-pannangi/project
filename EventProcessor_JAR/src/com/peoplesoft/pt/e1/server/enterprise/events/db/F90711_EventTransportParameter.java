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
 *  Represents the F90711 (Event Transport Parameter) table.
 */
public final class F90711_EventTransportParameter extends JDBTable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String TABLE_NAME = "F90711";
    
    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Table instance.  */
    public static final F90711_EventTransportParameter TABLE = new F90711_EventTransportParameter();
    
    /**  UserID field.  */
    public static final JDBField USER = new JDBField("USER");
    
    /**  Parameter name field.  */
    public static final JDBField EVTPMN = new JDBField("EVTPMN");
    
    /**  Parameter value field.  */
    public static final JDBField EVTPMV = new JDBField("EVTPMV");
    
    /**  Array for fields in the table.  */
    public static final JDBField[] FIELDS = new JDBField[] 
    {
        USER,
        EVTPMN,
        EVTPMV
    };
    
    /**  String value in the database for the MQSeries connection factory JNDI name.  */
    public static final String MQSERIES_CON_FACTORY_NAME = "MQSCFNM";
    
    /**  String value in the database for the MQSeries queue JNDI name.  */
    public static final String MQSERIES_QUEUE_NAME = "MQSNM";
    
    /**  String value in the database for the JDENET host name.  */
    public static final String JDENET_HOST_NAME = "NETHOST";
    
    /**  String value in the database for the JDENET port number.  */
    public static final String JDENET_PORT_NUMBER = "NETPORT";
    
    /**  String value in the database for the JDENET connection timeout.  */
    public static final String JDENET_TIMEOUT = "NETTIME";
    
    /**  String value in the database for the MSMQ queue label.  */
    public static final String MSMQ_QUEUE_LABEL = "MSMQLBL";
    
    /**  String value in the database for the MSMQ queue name.  */
    public static final String MSMQ_QUEUE_NAME = "MSMQNM";


	/**  String value in the database for the JMS Topic connection factory JNDI name.  */
	public static final String JMSTOPIC_CON_FACTORY_NAME = "JTCFNM";
    
	/**  String value in the database for the JMS Topic JNDI name.  */
	public static final String JMS_TOPIC_NAME = "JTNM";

    /**  String value in the database for the JMS Queue connection factory JNDI name.  */
    public static final String JMSQUEUE_CON_FACTORY_NAME = "JMSQCFNM";

    /**  String value in the database for the JMS Queue JNDI name.  */
    public static final String JMS_QUEUE_NAME = "JMSQNM";

    /**  String value in the database for the Message Format.  */
    public static final String JMS_QUEUE_MESSAGE_FORMAT = "JMSQMSGF";

    /**  String value in the database for the Context Factory  */
    public static final String CONTEXT_FACTORY = "JMSQICF";

    /**  String value in the database for the Provider URL  */
    public static final String PROVIDER_URL = "JMSQPURL";



    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Private constructor to prevent instances from being created.
     */
    private F90711_EventTransportParameter()
    {
        super(TABLE_NAME);
    }

    //=================================================
    // Methods.
    //=================================================
}
