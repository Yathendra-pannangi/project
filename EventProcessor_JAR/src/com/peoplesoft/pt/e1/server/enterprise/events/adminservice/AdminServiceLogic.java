//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.adminservice;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.NamingException;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;
import com.peoplesoft.pt.e1.server.common.events.adminservice.IAdminService;
import com.peoplesoft.pt.e1.server.common.events.adminservice.UnknownDataReloadCategoryException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.DataReloadCategory;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Implementation logic for the admin service.
 */
public class AdminServiceLogic implements IAdminService
{
    //=================================================
    // Non-public static class fields.
    //=================================================
        
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(AdminServiceLogic.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Reload a specific data category.
     * 
     *  @param  category  The category to be reloaded.
     * 
     *  @throws UnknownDataReloadCategoryException  The category
     *          specified was not recognized.
     * 
     *  @throws EventProcessingException  Error during processing.
     */
    public void reloadData(String category)
        throws UnknownDataReloadCategoryException, EventProcessingException
    {
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Reloading category: " + category, null, null, null);
        }
        
        DataReloadCategory reloadCategory = null;
        try
        {
            reloadCategory = DataReloadCategory.getDataReloadCategory(category);
        }
        catch (UnknownDataReloadCategoryException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Invalid data reload category: " + e.getMessage(), null, null, e);
            throw e;
        }

        //
        //  Lookup the connection factory and topic.
        //
        TopicConnectionFactory factory = null;
        Topic topic = null;
        try
        {
            factory = 
                (TopicConnectionFactory)JNDIUtil.lookup(JMSNames.RELOAD_TOPIC_FACTORY);
            topic = (Topic)JNDIUtil.lookup(JMSNames.RELOAD_TOPIC);
        }
        catch (NamingException e)
        {
            String msg = "JNDI lookup of topic factory or topic failed: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,  e);
            throw new EventProcessingException(msg, e);
        }
        
        try
        {
            //
            //  Send a reload message via the JMS topic.
            //
            TopicConnection connection = factory.createTopicConnection();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicPublisher publisher = session.createPublisher(topic);
            ObjectMessage message = session.createObjectMessage();
            message.setObject(reloadCategory);
            
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Sending reload message to JMS topic", null, null,null);
            }
            publisher.publish(message);
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"JMS topic send successful",null, null, null);
            }
            
            publisher.close();
            session.close();
            connection.close();
        }
        catch (JMSException e)
        {
            String msg = "Error publishing data reload message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
}
