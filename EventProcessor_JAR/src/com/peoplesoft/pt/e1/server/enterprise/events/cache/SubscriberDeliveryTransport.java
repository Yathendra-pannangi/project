//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.cache;

//=================================================
//Imports from java namespace
//=================================================
import java.util.HashMap;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.peoplesoft.pt.e1.server.enterprise.events.common.DeliveryTransportType;
import com.peoplesoft.pt.e1.server.enterprise.events.common.UnknownDeliveryTransportException;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90711_EventTransportParameter;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Describes the event delivery transport for a subscriber.
 */
public class SubscriberDeliveryTransport
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(SubscriberDeliveryTransport.class.getName());

    //=================================================
    // Instance member fields.
    //=================================================
    
    private DeliveryTransportType  mTransportType;
    
    private HashMap mTransportParameter = new HashMap();

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Create a SubscriberDeliveryTransport.
     * 
     *  @param transType  String representation of the transport type.
     * 
     *  @param username   Username of the user the transport belongs to.
     * 
     *  @param connection Database connection to use for additional queries.
     * 
     *  @throws JDBException  Error accessing the database.
     * 
     *  @throws UnknownDeliveryTransportException  Unknown delivery transport type
     *                                             string value.
     */
    SubscriberDeliveryTransport(String transType, 
                                String username,
                                JDBDatabaseAccess connection)
        throws JDBException, UnknownDeliveryTransportException
    {
        //
        //  Get the transport type.
        //
        mTransportType = DeliveryTransportType.getTransportType(transType);
        
        //
        //  Get the transport parameters.
        //
        JDBResultSet resultSet =
            connection.select(F90711_EventTransportParameter.TABLE, 
                              F90711_EventTransportParameter.FIELDS, 
                              getSelection(username), null);
        
        //
        //  Extract all the parameters from the result set.
        //
        try
        {
            while (resultSet.hasMoreRows())
            {
                JDBFieldMap pramMap = resultSet.fetchNext();
                
                String temp = null;
                
                temp = pramMap.getString(F90711_EventTransportParameter.EVTPMN);
                String name = null;
                if (temp != null)
                {
                    name = temp.trim();
                }
                else
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"null event transport parameter name found in database",null, null,null);
                    continue;
                }
                
                temp = pramMap.getString(F90711_EventTransportParameter.EVTPMV);
                String value = null;
                if (temp != null)
                {
                    value = temp.trim();
                }
                else
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                        "null event transport parameter value for name "
                            + name
                            + " found in database",null, null,null);
                    continue;
                }

                mTransportParameter.put(name, value);
            }
        }
        finally
        {
            //
            //  Make sure to try closing the result set.
            //
            try
            {
                resultSet.close();
            }
            catch (Exception e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception closing database result set: " + e.getMessage(),null, null, e);
            }
        }
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  The transport delivery type.
     */
    public DeliveryTransportType getTransportType()
    {
        return mTransportType;
    }
    
    /**
     *  Get a transport configuration parameter.
     * 
     *  @param  name  parameter name.
     * 
     *  @return  The parameter value or <code>null</code> if the parameter is not found.
     */
    public String getProperty(String name)
    {
        String value = (String)mTransportParameter.get(name);
        return value;
    }
    
    private JDBSelection getSelection(String username) throws JDBException
    {
        //
        //  Build a selection for the the user field.
        //
        JDBFieldComparison selection =
            new JDBFieldComparison(
                F90711_EventTransportParameter.USER,
                JDBComparisonOp.EQ,
                username,
                true);

        return selection;
    }
}
