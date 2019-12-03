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
import java.io.Serializable;
import java.util.HashMap;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.server.common.events.adminservice.IAdminService;
import com.peoplesoft.pt.e1.server.common.events.adminservice.UnknownDataReloadCategoryException;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Enumerates the various data reload categories.
 */
public final class DataReloadCategory implements Serializable
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    private static HashMap sInstances = new HashMap();
    
    /** Event type definitions. */
    public static final DataReloadCategory EVENT_TYPE_DEFINITIONS
        = new DataReloadCategory(10, IAdminService.EVENT_TYPE_CATEGORY);
    
    /** Event subscriber definitions. */
    public static final DataReloadCategory SUBSCRIBER_DEFINITIONS
        = new DataReloadCategory(20, IAdminService.SUBSCRIBER_CATEGORY);

    //=================================================
    // Static class fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private final int     mValue;
    
    private final String  mLabel;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Private constructor to prevent external creation of instances.
     */
    private DataReloadCategory(int value, String label)
    {
        mValue = value;
        mLabel = label;
        
        sInstances.put(label, this);
    }

    //=================================================
    // Methods.
    //=================================================
        
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        boolean result = false;
        
        if (other instanceof DataReloadCategory)
        {
            DataReloadCategory otherObj = (DataReloadCategory)other;
            if (otherObj.mValue == mValue)
            {
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return mValue;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return mLabel + ":" + Integer.toString(mValue);
    }
    
    /**
     * Get the data reload category represented by a string value.
     * 
     * @param value  The string value of a data reload category.  The case of the value
     *               is ignored when searching for a matching transport type.
     * 
     * @return  The data reload category.
     * 
     * @throws UnknownDataReloadCategoryException  The string value specified does
     *         not correspond to any known data reload category.
     */
    public static DataReloadCategory getDataReloadCategory(String value)
        throws UnknownDataReloadCategoryException
    {
        DataReloadCategory result = (DataReloadCategory)sInstances.get(value);        
        if (result == null)
        {
            throw new UnknownDataReloadCategoryException("Label=" + value);
        }
        
        return result;
    }
}
