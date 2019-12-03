//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events;

//=================================================
//Imports from java namespace
//=================================================
import java.io.Serializable;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.JDECalendar;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Represents an event message ready for delivery to a subscriber.
 */
public class EventMessage implements Serializable
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
    
    private String mEventID = "";
    
    private String mCategory = "";
    
    private String mType = "";
    
    private String mEnvironment = "";
    
    private long   mSequenceNumber;
    
    private String mXMLPayload = "";
    
    private String mUser = "";
    
    private String mRole = "";
    
    private String mSessionID = "";
    
    private String mApplication = "";
    
    private String mApplicationVersion = "";
    
    private String mHost = "";
    
    private String mScope = "";
    
    private String mSourceRoute = "";
    
    private String mBSFN = "";
    
    private String mFunction = "";
    
    private String mProcessID = "";
    
    private JDECalendar mDateTime;
      

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get the XML payload for the event.
     * 
     *  @return  The XML payload.
     */
    public String getXMLPayload()
    {
        return mXMLPayload;
    }
    
    /**
     *  Set the XML payload for the event.
     * 
     *  @param xml  the XML payload.
     */
    public void setXMLPayload(String xml)
    {
        mXMLPayload = xml;
    }
    
    /**
     * @return  Event category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @return  Originating environment.
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * @return  Sequence number.
     */
    public long getSequenceNumber()
    {
        return mSequenceNumber;
    }

    /**
     * @return  Event type.
     */
    public String getType()
    {
        return mType;
    }

    /**
     * @param category  Event category.
     */
    public void setCategory(String category)
    {
        mCategory = category;
    }

    /**
     * @param env  Originating environment.
     */
    public void setEnvironment(String env)
    {
        mEnvironment = env;
    }

    /**
     * @param seqNum  Event sequence number.
     */
    public void setSequenceNumber(long seqNum)
    {
        mSequenceNumber = seqNum;
    }

    /**
     * @param type  Event type.
     */
    public void setType(String type)
    {
        mType = type;
    }

    /**
     * @return String application name
     */
    public String getApplication()
    {
        return mApplication;
    }

    /**
     * @return Date Date/time event was generated.
     */
    public JDECalendar getDateTime()
    {
        return mDateTime;
    }

    /**
     * @return String hostname 
     */
    public String getHost()
    {
        return mHost;
    }

    /**
     * @return String role
     */
    public String getRole()
    {
        return mRole;
    }

    /**
     * @return String Originating user session ID.
     */
    public String getSessionID()
    {
        return mSessionID;
    }

    /**
     * @return String userID
     */
    public String getUser()
    {
        return mUser;
    }

    /**
     * @return  Event ID.
     */
    public String getEventID()
    {
        return mEventID;
    }

    /**
     * @param id  Event ID.
     */
    public void setEventID(String id)
    {
        mEventID = id;
    }

    /**
     * @param user  The originating user.
     */
    public void setUser(String user)
    {
        mUser = user;
    }

    /**
     * @param role  The user's role.
     */
    public void setRole(String role)
    {
        mRole = role;
    }

    /**
     * @return  Application version.
     */
    public String getApplicationVersion()
    {
        return mApplicationVersion;
    }

    /**
     * @param version  Application version.
     */
    public void setApplicationVersion(String version)
    {
        mApplicationVersion = version;
    }

    /**
     * @param app  Originating application.
     */
    public void setApplication(String app)
    {
        mApplication = app;
    }

    /**
     * @param date  Originating date/time.
     */
    public void setDateTime(JDECalendar date)
    {
        mDateTime = date;
    }

    /**
     * @param host  Originating host.
     */
    public void setHost(String host)
    {
        mHost = host;
    }

    /**
     * @param id  Originating user session ID.
     */
    public void setSessionID(String id)
    {
        mSessionID = id;
    }

    /**
     * @return  The event scope value.
     */
    public String getScope()
    {
        return mScope;
    }

    /**
     * @param scope  Event scope value.
     */
    public void setScope(String scope)
    {
        mScope = scope;
    }

    /**
     * @return  The source route, if one exists.
     */
    public String getSourceRoute()
    {
        return mSourceRoute;
    }

    /**
     * @param route  The source route.
     */
    public void setSourceRoute(String route)
    {
        mSourceRoute = route;
    }

    /**
     * @return  Originating BSFN.
     */
    public String getBSFN()
    {
        return mBSFN;
    }

    /**
     * @return  Originating function.
     */
    public String getFunction()
    {
        return mFunction;
    }

    /**
     * @return  Originating process ID.
     */
    public String getProcessID()
    {
        return mProcessID;
    }

    /**
     * @param bsfn  Originating BSFN.
     */
    public void setBSFN(String bsfn)
    {
        mBSFN = bsfn;
    }

    /**
     * @param function  Originating function.
     */
    public void setFunction(String function)
    {
        mFunction = function;
    }

    /**
     * @param pid  Originating process ID.
     */
    public void setProcessID(String pid)
    {
        mProcessID = pid;
    }

}
