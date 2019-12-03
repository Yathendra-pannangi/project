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
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventMessageBuilder;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Base class for all event processor drivers.
 */
abstract class EventProcessorDriver
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
    
    private EventMessageBuilder mEventMessageBuilder = new EventMessageBuilder();

    //=================================================
    // Constructors.
    //=================================================
    
    //=================================================
    // Methods.
    //=================================================
            
    /**
     *  Gets an event's data BLOB from the database.
     * 
     *  @param connection  Database connection to use.
     * 
     *  @param event  Event.
     * 
     *  @return  The requested event's data BLOB.
     * 
     *  @throws EventProcessingException  An error occured.
     */
    protected byte[] getEventDataFromDB(JDBDatabaseAccess connection, EventMessage event) 
        throws EventProcessingException
    {
        byte[] result = mEventMessageBuilder.getEventData(connection, event.getEventID());
        return result;
    }
 
    /**
     * Process an event.
     * 
     * @param connection  Database connection to use during processing.
     * 
     * @param event  Event to be processed.
     * 
     * @return  Resulting event message object.
     * 
     * @throws EventProcessingException An error occured.
     */
    abstract EventMessage processEvent(JDBDatabaseAccess connection, EventMessage event)
        throws EventProcessingException;
}
