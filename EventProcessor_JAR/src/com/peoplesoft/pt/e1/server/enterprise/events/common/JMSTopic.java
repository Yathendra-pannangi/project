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
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSubscriber;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.Session;
import javax.naming.NamingException;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;

//=================================================
// Imports from org namespace
//=================================================

/**
 * JMSTopic is a utility class for creating the necessary
 * JMS Topic configurations.
 */
public class JMSTopic
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(JMSTopic.class.getName());

    private static final String DEFAULT_TOPIC_CON_FACTORY_JNDI_NAME = JMSNames.Q_CON_FACTORY;

    //=================================================
    // Instance member fields.
    //=================================================

    private String mTopicJNDIName;

    private String mConnectionFactoryJNDIName = DEFAULT_TOPIC_CON_FACTORY_JNDI_NAME;

    private Topic mTopic = null;
    
    private TopicConnection mTopicConnection = null;

    private TopicSession mTopicSession = null;
    
    private TopicPublisher mTopicPublisher = null;
    
    private TopicSubscriber mTopicSubscriber = null;
    
    //Default is auto acknowledge.
    private int mTopicAcknowledge = Session.AUTO_ACKNOWLEDGE;
    
    private boolean mTransacted = false;

    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     * 
     *  @param topicJNDIName  The JNDI name for the topic.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the topic connection factory.
     *                                    If this value is <code>null</code> a default value
     *                                    will be used.
     * 
     *  @param transacted  Create the topic session as transacted?
     * 
     *  @param acknowledge The topic session acknowledgement value.
     */
    public JMSTopic(
        String topicJNDIName,
        String connectionFactoryJNDIName,
        boolean transacted,
        int acknowledge)
    {
        mTopicJNDIName = topicJNDIName;
        if (connectionFactoryJNDIName != null)
        {
            mConnectionFactoryJNDIName = connectionFactoryJNDIName;
        }
        mTopicAcknowledge = acknowledge;
        mTransacted = transacted;
    }
    
    /**
     *  Constructor.
     * 
     *  @param topicJNDIName  The JNDI name for the topic.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the topic connection factory.
     *                                    If this value is <code>null</code> a default value
     *                                    will be used.
     * 
     *  @param transacted  Create the topic session as transacted?
     */
    public JMSTopic(String topicJNDIName, String connectionFactoryJNDIName, boolean transacted)
    {
        mTopicJNDIName = topicJNDIName;
        if (connectionFactoryJNDIName != null)
        {
            mConnectionFactoryJNDIName = connectionFactoryJNDIName;
        }
        mTransacted = transacted;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Initialize the output topic.
     * 
     *  @throws EventProcessingException  An error occured during initialization.
     */
    public void initialize() throws EventProcessingException
    {
        //
        //  Lookup the topic conneciton factory.
        //
        TopicConnectionFactory connectionFactory = null;
        try
        {
            connectionFactory = (TopicConnectionFactory)JNDIUtil.lookup(mConnectionFactoryJNDIName);

        }
        catch (NamingException e)
        {
            String msg = "JNDI lookup of TopicConnectionFactory failed: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Lookup the topic.
        //
        try
        {
            mTopic = (Topic)JNDIUtil.lookup(mTopicJNDIName);
        }
        catch (NamingException e)
        {
            String msg =
                "JNDI lookup of Topic failed: "
                    + "topicJNDIName= "
                    + mTopicJNDIName
                    + " message="
                    + e.getMessage();
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Create the topic connection, session, and sender.
        //
        try
        {
            mTopicConnection = connectionFactory.createTopicConnection();
            mTopicConnection.start();
            mTopicSession = mTopicConnection.createTopicSession(mTransacted, mTopicAcknowledge);
            mTopicPublisher = mTopicSession.createPublisher(mTopic);
         //mTopicSubscriber = mTopicSession.createSubscriber(mTopic);

       }
        catch (JMSException e)
        {
            //
            //  Clean-up the session and connection.
            //
            if (mTopicSession != null)
            {
                try
                {
                    mTopicSession.close();
                }
                catch (Exception e1)
                {
                    //  Ignore.
                }
            }
            if (mTopicConnection != null)
            {
                try
                {
                    mTopicConnection.close();
                }
                catch (Exception e1)
                {
                    //  Ignore.
                }
            }
            mTopicPublisher = null;
            mTopicSession = null;
            mTopicConnection = null;
            
            StringBuffer buffer = 
                new StringBuffer("Failed to initialize topic connection and session:");
            buffer.append(System.getProperty("line.separator"));
            buffer.append("    topicJNDIName=").append(mTopicJNDIName);
            buffer.append(System.getProperty("line.separator"));
            buffer.append("    exception: ").append(e.getMessage());
            String msg = buffer.toString();
            
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        mInitialized = true;
    }

    /**
     *  Shutdown the output topic.
     * 
     *  @throws EventProcessingException  An error occured during the shutdown.
     */
    public void shutdown() throws EventProcessingException
    {
        mInitialized = false;
        try
        {
            mTopicPublisher.close();
            mTopicPublisher = null;
            mTopicSession.close();
            mTopicSession = null;
            mTopicConnection.close();
            mTopicConnection = null;
        }
        catch (JMSException e)
        {
            String msg = "Failed to shutdown EventTopic: " + e.getMessage();
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }

    /**
     * @return  The current topic session.
     */
    public TopicSession getTopicSession()
    {
        return mTopicSession;
    }

    /**
     * @return  <code>true</code> = has been initialized, <code>false</code> = has not been
     *          initialized.
     */
    public boolean isInitialized()
    {
        return mInitialized;
    }

    /**
     * @return  Topic topic.
     */
    public Topic getTopic()
    {
        return mTopic;
    }
   
    /**
     * @return  The current Topic sender.
     */
    public TopicPublisher getTopicPublisher()
    {
        return mTopicPublisher;
    }

    /**
     * @return  The current topic receiver.
     */
    public TopicSubscriber getTopicSubscriber()
    {
        return mTopicSubscriber;
    }
    
    /**
     * @return  JNDI name of the topic.
     */
    public String getTopicJNDIName()
    {
        return mTopicJNDIName;
    }

}
