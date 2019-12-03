//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

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
 * Indacates that an unknown event type was encountered.
 */
public class UnknownEventTypeException extends EventProcessingException
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
    
    private String  mCategory;
    
    private String  mType;
    
    private String  mEnvironment;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  An unknown event type was encountered.
     * 
     *  @param category  Evenet category.
     * 
     *  @param type  Event type.
     * 
     *  @param environment  Originating environment.
     */
    public UnknownEventTypeException(String category, String type, String environment)
    {
        super("Unknown event: " + type);
        
        mCategory    = category;
        mType        = type;
        mEnvironment = environment;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  Category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @return  Environment.
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * @return  Event type.
     */
    public String getType()
    {
        return mType;
    }
    
    /**
     *  Get descriptive message.
     * 
     *  @return  message.
     */
    public String getMessage()
    {
        StringBuffer buffer = new StringBuffer(80).append("Unknown event:");
        buffer.append(" type=").append(mType);
        buffer.append(" category=").append(mCategory);
        buffer.append(" environment=").append(mEnvironment);
        
        return buffer.toString();
    }
}
