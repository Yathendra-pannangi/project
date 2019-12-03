//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.enterprise.events.common;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Hashtable;

import com.jdedwards.base.spec.DSTemplate2;
import com.jdedwards.constants.JdeNetConstants;
import com.jdedwards.system.kernel.BSFNData;
import com.jdedwards.system.lib.JdeInputStream;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Data structure implementation class for Events Business Function data.
 */
public class EventsBSFNData extends BSFNData
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

    private String mEncoding = JdeNetConstants.ENCODE_UTF16LE;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Contructor to call the super class.
     * @param ds DSTemplate2
     * @param data Hashtable data is stored after inflate.
     */
    public EventsBSFNData(DSTemplate2 ds, Hashtable data)
    {
        super(ds, data);
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * This method sets the encoding for the conversion of math numeric.
     * @param encoding String
     */
    public void setEncoding(String encoding)
    {
        this.mEncoding = encoding;
    }
    
    /**
     * Read objects from the input stream.
     * @param type data item type. e.g., 'c', 's', 'd', 'm'
     * @param len length of the data item. only valid for 'c' data types, all others are zero.
     * @param is  Input stream to read the objects from.
     * @return Object read. e.g., data type 's' would return an instance of Short
     * @throws   java.io.IOException ex
     */
    protected Object readObject(char type, int len, JdeInputStream is) throws java.io.IOException
    {
        Object result = null;
        switch (type)
        {
            case 's' : // short
                result = new Short(is.readShort());
                break;
                
            case 'i' : // int
                result = new Integer(is.readInt());
                break;
                
            case 'l' : // long
                result = new Integer(is.readInt());
                break;
                
            case 'm' : // math-numeric
                result = is.readMathNumeric(mEncoding);
                break;
                
            case 'c' : // char-array (string or byte)
                /**
                 * Unicode - Changes
                 */
                result = is.readUnicodeString(len);
                break;
                
            case 'd' : // char-array (string or byte)
                result = is.readDate();
                break;
                
            case 'u' :
                result = is.readUTime();
                break;
                
            default :
                result = null;
                break;
        }
        
        return result;
    }
}
