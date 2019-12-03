//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;

//=================================================
//Imports from java namespace
//=================================================
import java.io.UnsupportedEncodingException;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Event processing driver for XML event categories.
 */
public class XMLEventDriver extends EventProcessorDriver
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static final String CHAR_ENCODING = "UTF-16";
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(XMLEventDriver.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /** 
     * {@inheritDoc}
     */
    XMLEventDriver()
    {
        super();
    }

    //=================================================
    // Methods.
    //=================================================
    
    /** 
     * {@inheritDoc}
     */
    EventMessage processEvent(JDBDatabaseAccess connection, EventMessage event)
        throws EventProcessingException
    {
        byte[] data = getEventDataFromDB(connection, event);
        String xml;
        try
        {
            xml = new String(data, CHAR_ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            String msg = "Error reading XML BLOB from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        event.setXMLPayload(xml);
        
        return event;
    }

}
