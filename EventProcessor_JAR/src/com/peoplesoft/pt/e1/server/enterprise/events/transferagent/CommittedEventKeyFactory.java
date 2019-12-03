//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.transferagent;

//=================================================
//Imports from java namespace
//=================================================
import java.util.LinkedList;
import java.util.List;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBCompositeSelection;
import com.jdedwards.database.base.JDBConjunctionOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBField;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBParameterMarker;
import com.jdedwards.database.base.JDBPropertyName;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.database.base.JDBSelection;
import com.jdedwards.database.base.JDBSortDirection;
import com.jdedwards.database.base.JDBSortField;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.CommittedEventKey;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventState;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;

//=================================================
//Imports from org namespace
//=================================================

/**
 * This class encapsulates the database operations needed to retreive committed event keys.
 */
class CommittedEventKeyFactory
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static final String EVENT_SECTION = "EVENTS";
    
    private static final String EVENT_TRANSFER_ISOLATION_LEVEL =
        "eventTransferTransactionIsolation";
    
    private static final String ISOLATION_LEVEL_DEFAULT = "repeatable read";
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(CommittedEventKeyFactory.class.getName());
        
    /**  Fields to select.  */
    private static final JDBField[] SELECT_FIELDS = new JDBField[] 
    {
        F90710_EventTransfer.EVNTID,      // Event ID
        F90710_EventTransfer.EVNTSEQ,     // Sequence number
        F90710_EventTransfer.EVNTNAME,    // Name (a.k.a. event type)
        F90710_EventTransfer.EVNTTYPE,    // Type (a.k.a. category)
        F90710_EventTransfer.EVNTST,      // State
        F90710_EventTransfer.ENV,         // Environment
        F90710_EventTransfer.EVNTSNS      // User session
    };
    
    /** Sort by ascending sequence number. */
    private static final JDBSortField SORT = 
        new JDBSortField(F90710_EventTransfer.EVNTSEQ, JDBSortDirection.ASCENDING);


    //=================================================
    // Instance member fields.
    //=================================================
    
    private JDBParameterMarker mSeqNumMarker = null;

    private JDBSelection mSelection = null;
    
    private JDBDatabaseAccess mConnection = null;
    
    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Perform any initializations needed.
     * 
     *  @throws EventProcessingException  An error occured.
     */
    void initialize() throws EventProcessingException
    {
        try
        {
            //
            //  Get the database connection to use for selecting committed events.
            //
            mConnection = EventDBUtil.getExtendedTokenDefaultDBAccess();
            mConnection.setProperty(JDBPropertyName.AUTO_COMMIT, false);
            mConnection.setProperty(
                JDBPropertyName.TRANSACTION_ISOLATION,
                getTransactionIsolationLevel());
        }
        catch (JDBException e)
        {
            String msg =
                "Failed to get DB connection for committed event factory: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        //
        //  Initialize the selection criteria object used to select committed events from
        //  from the database.
        //
//        mSeqNumMarker = new JDBParameterMarker();
//
//        JDBSelection stateSelection =
//            new JDBFieldComparison(
//                F90710_EventTransfer.EVNTST,
//                JDBComparisonOp.EQ,
//                F90710_EventTransfer.STATE_COMMITTED);
//
//        JDBSelection seqNumSelection =
//            new JDBFieldComparison(
//                F90710_EventTransfer.EVNTSEQ,
//                JDBComparisonOp.LE,
//                mSeqNumMarker);
//
//        mSelection =
//            new JDBCompositeSelection(stateSelection, JDBConjunctionOp.AND, seqNumSelection);

        mInitialized = true;
    }
    
    /**
     *  Perform any cleanup needed.
     */
    void shutdown()
    {
        mInitialized = false;
        mConnection = null;
        mSelection = null;
    }
    
    /**
     * Get committed events that are ready to be transfered.
     * 
     * @param maxSeqNum  Maximum sequence number to select.
     * 
     * @return  A list of <code>CommittedEventKey</code> objects sorted by their
     *          sequence numbers.
     * 
     * @throws EventProcessingException  Error getting committed events from the database.
     */
/*    List getCommittedEvents(long maxSeqNum) throws EventProcessingException
    {
        //
        //  Verify that initialization has been performed.
        //
        if (!mInitialized)
        {
            throw new IllegalStateException(TransferAgentLogic.class.getName()
                                            + " not initialized");
        }

        JDBResultSet resultSet;
        List result = new LinkedList();
        try
        {
            //
            //  Select committed events from the database.
            //
            mSeqNumMarker.setValue(new Long(maxSeqNum));

            mConnection.setProperty(JDBPropertyName.RESULT_SET_UPDATABLE, true);
            mConnection.getTransaction().begin();
            resultSet =
                mConnection.select(F90710_EventTransfer.TABLE, SELECT_FIELDS, mSelection, SORT);
//            try
//            {
//                while (resultSet.hasMoreRows())
//                {
//                    //
//                    //  Build a committed event key object for each row.
//                    //
//                    JDBFieldMap map = resultSet.fetchNext();
//                    CommittedEventKey eventKey = buildCommittedEventKey(map);
//                    result.add(eventKey);
//                }
              try
              {
               //
               //  Build a committed event key object for each row.
               //
               JDBFieldMap map = resultSet.fetchNext();
                  while(map != null){
                      CommittedEventKey eventKey = buildCommittedEventKey(map);
                      result.add(eventKey);
                      map =  resultSet.fetchNext();
                  }
            }
            finally
            {
                try
                { 
                    //
                    //  Close the result set.
                    //
                    resultSet.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(), null, null,  e);
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Exception from database: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
            throw new EventProcessingException(msg, e);
        }
		finally
		{
			try
			{ 
				//
				//  End the transaction.
				//
				mConnection.getTransaction().commit();
			}
			catch (JDBException e)
			{
				sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to commit database transaction: " + e.getMessage(), null, null,  e);
			}
		}
        
        return result;
    } */
    
 List getCommittedEvents(long minSeqNum, long maxSeqNum) throws EventProcessingException
 {
     //
     //  Verify that initialization has been performed.
     //
     if (!mInitialized)
     {
         throw new IllegalStateException(TransferAgentLogic.class.getName()
                                         + " not initialized");
     }

     JDBResultSet resultSet;
     List result = new LinkedList();
     try
     {
         //
         //  Select committed events from the database.
         //
         mConnection.getTransaction().begin();

         JDBSelection stateSelection =
             new JDBFieldComparison(
                 F90710_EventTransfer.EVNTST,
                 JDBComparisonOp.EQ,
                 F90710_EventTransfer.STATE_COMMITTED);

         JDBSelection minSeqNumSelection =
             new JDBFieldComparison(
                 F90710_EventTransfer.EVNTSEQ,
                 JDBComparisonOp.GE,
                 minSeqNum);
                 
         JDBSelection maxSeqNumSelection =
             new JDBFieldComparison(
                 F90710_EventTransfer.EVNTSEQ,
                 JDBComparisonOp.LE,
                 maxSeqNum);
                 
        JDBSelection seqSelection =
            new JDBCompositeSelection(minSeqNumSelection, JDBConjunctionOp.AND, maxSeqNumSelection);
        mSelection =
             new JDBCompositeSelection(stateSelection, JDBConjunctionOp.AND, seqSelection);         
        resultSet =
             mConnection.select(F90710_EventTransfer.TABLE, SELECT_FIELDS, mSelection, SORT);

           try
           {
            //
            //  Build a committed event key object for each row.
            //
            JDBFieldMap map = resultSet.fetchNext();
               while(map != null){
                   CommittedEventKey eventKey = buildCommittedEventKey(map);
                   result.add(eventKey);
                   map =  resultSet.fetchNext();
               }
         }
         finally
         {
             try
             {
                 //
                 //  Close the result set.
                 //
                 resultSet.close();
             }
             catch (JDBException e)
             {
                 sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database result set: " + e.getMessage(), null, null,  e);
             }
         }
     }
     catch (JDBException e)
     {
         String msg = "Exception from database: " + e.getMessage();
         sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,  e);
         throw new EventProcessingException(msg, e);
      //   }
     }
             finally
             {
                     try
                     {
                             //
                             //  End the transaction.
                             //
                             mConnection.getTransaction().commit();
                     }
                     catch (JDBException e)
                     {
                             sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to commit database transaction: " + e.getMessage(), null, null,  e);
                     }
             }

     return result;
 }    

    /**
     *  Build a committed event object from a field map.
     */
    private CommittedEventKey buildCommittedEventKey(JDBFieldMap map)
        throws JDBException
    {
        String temp = null;
        CommittedEventKey event = new CommittedEventKey();
        
        //
        //  Event ID.
        //
        temp = map.getString(F90710_EventTransfer.EVNTID);
        if (temp != null)
        {
            event.setEventID(temp.trim());
        }
        
        //
        //  Event type (a.k.a. name).
        //
        temp = map.getString(F90710_EventTransfer.EVNTNAME);
        if (temp != null)
        {
            event.setType(temp.trim());
        }
        
        //
        //  Event category (a.k.a. type).
        //
        temp = map.getString(F90710_EventTransfer.EVNTTYPE);
        if (temp != null)
        {
            event.setCategory(temp.trim());
        }
        
        //
        //  Event's originating environment.
        //
        temp = map.getString(F90710_EventTransfer.ENV);
        if (temp != null)
        {
            event.setOriginatingEnvironment(temp.trim());
        }
        
        //
        //  Event's originating user session.
        //
        temp = map.getString(F90710_EventTransfer.EVNTSNS);
        if (temp != null)
        {
            event.setOriginatingUserSession(temp.trim());
        }
        
        //
        //  Event sequence number.
        //
        tagMathNumeric sequence = (tagMathNumeric)map.getValue(F90710_EventTransfer.EVNTSEQ);
        event.setSequenceNumber(sequence.asBigDecimal().longValue());
        
        //
        //  Set the event state.
        //
        event.setState(EventState.COMMITTED);
        
        return event;
    }
    
    /**
     *  Get the transaction isolation level to be used when getting committed events
     *  from the database.
     */
    private String getTransactionIsolationLevel()
    {
        String result = JdeProperty.getProperty(
                EVENT_SECTION,
                EVENT_TRANSFER_ISOLATION_LEVEL,
                ISOLATION_LEVEL_DEFAULT);
        
        if(sE1Logger.isDebug())        
        	sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Event transfer transaction isolation level = " + result, null, null, null);
                
        return result;
    }
}
