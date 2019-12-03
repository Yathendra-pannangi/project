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

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldMap;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90715_EventQueueAssignment;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Contains the description of a subcriber's event queue.
 */
public class SubscriberQueueDescription
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    /** JNDI name of the subscriber queue. */
    private String mQueueJNDIName;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param map  Field map tp build queue description from.
     * 
     *  @throws JDBException  Error reading data from database.
     */
    SubscriberQueueDescription(JDBFieldMap map)
        throws JDBException
    {
        mQueueJNDIName = map.getString(F90715_EventQueueAssignment.JNDINM).trim();
    }
    
	SubscriberQueueDescription(String queueName)
	{
			mQueueJNDIName = queueName.trim();
	}
	
 
    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  JNDI name of the subscriber queue.
     */
    public String getQueueJNDIName()
    {
        return mQueueJNDIName;
    }
}
