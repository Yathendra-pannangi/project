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

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventXMLBuilder;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Event processing driver for data structure category events.
 */
public class DataStructureEventDriver extends EventProcessorDriver
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private EventXMLBuilder mXMLBuilder = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /** 
     * {@inheritDoc}
     */
    DataStructureEventDriver() throws EventProcessingException
    {
        super();
        
        mXMLBuilder = new EventXMLBuilder();
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
        String xml = mXMLBuilder.eventsXMLConverter(data, event);
        event.setXMLPayload(xml);
        
        return event;
    }
}
