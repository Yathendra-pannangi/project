//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.common.events.monitoring;


//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
//=================================================
//Imports from org namespace
//=================================================

/**
 * Indicates that an Monitoring error was encountered.
 */

public class EventMonitoringException extends EventProcessingException
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

    //=================================================
    // Constructors.
    //=================================================
    /**
     *  {@inheritDoc}
     */
    public EventMonitoringException(String msg)
    {
        super(msg);
    }

    /**
     *  {@inheritDoc}
     */
    public EventMonitoringException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     *  {@inheritDoc}
     */
    public EventMonitoringException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
