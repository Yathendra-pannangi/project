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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import com.jdedwards.base.datatypes.DataTypes;
import com.jdedwards.base.datatypes.JDECalendar;
import com.jdedwards.base.datatypes.SqlDate;
import com.jdedwards.base.datatypes.tagMathNumeric;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.base.spec.DSTemplate2;
import com.jdedwards.constants.JdeNetConstants;
import com.jdedwards.system.lib.JdeUtil;
import com.jdedwards.system.net.JdeNetInputStream;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDataStructure;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;

/**
 * This class creates an XML document for all types of events. 
 * It converts the blob into a XML document.
 */
public class EventXMLBuilder
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    //=================================================
    // Static class fields.
    //=================================================
    
    private static final String XML_EVENT_TAG = "event";
    private static final String XML_HEADER_TAG = "header";
    private static final String XML_JDE_RESPONSE_TAG = "jdeResponse";
    private static final String XML_JDE_REQUEST_TAG = "jdeRequest";
    private static final String UTF_8_STRING = "UTF-8";
    private static final char UTF16_BYTE_ORDER_MARKER = '\uFEFF';
    
    private static SimpleDateFormat sTimeFormatter = new SimpleDateFormat("HHmmss");
    private static SimpleDateFormat sTimeFormatterWithColon = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sDateFormatter = new SimpleDateFormat("MMddyyyy");
    private static final SimpleDateFormat DS_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(EventXMLBuilder.class.getName());    
    
    //=================================================
    // Instance member fields.
    //=================================================
    private DocumentBuilder mBuilder = null;
    private Transformer mTransformer = null;
    private EventTypeDataStructure[] mTemplateStructs = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * This is a default constructor.
     * 
     * @throws EventProcessingException  Failed to initialize the XML builder.
     */
    public EventXMLBuilder() throws EventProcessingException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        TransformerFactory tranFactory = TransformerFactory.newInstance();        
        try
        {
            mBuilder = factory.newDocumentBuilder(); 
            mTransformer = tranFactory.newTransformer();                        
        }
        catch (ParserConfigurationException e)
        {
            String msg = "Error creating a document builder: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventProcessingException(msg, e);
        }
        catch (TransformerException e)
        {
            String msg = "Error creating a document transformer: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * This method converts the byte array into XML document.
     * @param data The byte array to be converted to XML.
     * @param message EventMessage
     * @return String byte[]
     * @throws EventProcessingException ex
     */
    public String eventsXMLConverter(byte[] data, EventMessage message) 
        throws EventProcessingException
    {
        String resultXML = null;
        EventsXMLData eventsxmldata = new EventsXMLData();
        eventsxmldata.setMDocument(mBuilder.newDocument());
        if(eventsxmldata.getMDocument() == null)
        {
            throw new EventProcessingException("mDocument is null.");
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Converting XML to String.", null, null, null);
        }
        // Convert Blob to String before creating XML Document as some 
        // of the data needed is in blob.
        eventsxmldata = convertBlobToString(data, message, eventsxmldata, false);
        //Create the Event XML document based on event type.
        createEventXML(message, eventsxmldata, false);
        Element element = eventsxmldata.getMDocument().getDocumentElement();
        if(element != null) 
        {
            element.normalize();
        } 
      //  Source source = new DOMSource(eventsxmldata.getMDocument());
        Writer out = new StringWriter();
        StreamResult result = new StreamResult(out);
        String stringXML = null;
        try
        {            
            stringXML = dom2string(eventsxmldata.getMDocument());
            Source source = new StreamSource(new StringReader(stringXML));
			mTransformer.transform(source, result);
            resultXML = out.toString();
            out.flush();
            out.close();
        }
        catch (TransformerException te)
        {
            throw new EventProcessingException("Exception converting XML document to string " 
                                                    + te,te);
        }
        catch (IOException ie)
        {
            throw new EventProcessingException("IOException converting XML document to string " 
                                                    + ie,ie);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"The XML document is: " + resultXML, null, null, null);
        }
        return resultXML;
    }

    
    /**
     * This method creates a XML document template.
     * 
     * @param message EventMessage
     * @param DSName  data structure name array
     * @param structs the EventTypeDataStructure array corresponding to the data
     *                structure name array
     * @return String byte[]
     * @throws EventProcessingException ex
     */
    public String eventsXMLTemplateGenerator(EventMessage message,
        String[] DSName, EventTypeDataStructure[] structs)
        throws EventProcessingException
    {
        String resultXML = null;
        mTemplateStructs = structs;
        EventsXMLData eventsxmldata = new EventsXMLData();
        eventsxmldata.setMDocument(mBuilder.newDocument());
        if (eventsxmldata.getMDocument() == null)
        {
            throw new EventProcessingException("mDocument is null.");
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Converting XML to String.", null, null, null);
        }
        eventsxmldata.setMDSName(DSName);
        int numberOfDS = DSName.length;
        eventsxmldata.setMNumberOfDS(numberOfDS);
        // Convert Blob to String before creating XML Document as some 
        // of the data needed is in blob.
        eventsxmldata = convertBlobToString(null, message, eventsxmldata, true);
        //Create the Event XML document based on event type.
        createEventXML(message, eventsxmldata, true);
        Element element = eventsxmldata.getMDocument().getDocumentElement();
        if (element != null)
        {
            element.normalize();
        }
     //   Source source = new DOMSource(eventsxmldata.getMDocument());
        Writer out = new StringWriter();
        StreamResult result = new StreamResult(out);
        try
        {
            String stringXML = dom2string(eventsxmldata.getMDocument());
            Source source = new StreamSource(new StringReader(stringXML));
			mTransformer.transform(source, result);
            resultXML = out.toString();
            out.flush();
            out.close();
        }
        catch (TransformerException te)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception converting XML document to string " + te,null, null,te);
        }
        catch (IOException ie)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"IOException converting XML document to string " + ie,null, null,ie);
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"The XML document is: " + resultXML, null, null, null);
        }
        return resultXML;
    }
    

    private void createEventXML(EventMessage message, EventsXMLData eventsxmldata, 
                                    boolean isTemplate)
    {
        String sourceRoute = message.getSourceRoute();
        // Create Root 
        if((message.getCategory().equalsIgnoreCase(F90710_EventTransfer.CATEGORY_XAPI)))
        {
            if (sourceRoute != null && !sourceRoute.equalsIgnoreCase(""))
            {
                eventsxmldata.setMRoot(
                    (Element)eventsxmldata.getMDocument().createElement(XML_JDE_REQUEST_TAG));
                createXMLHeader(message, true, eventsxmldata);
                //Make sure this event is not a partial event. If partial event
                //then mNumberOfDS is always zero and do not need to add a DS detail.
                if (eventsxmldata.getMNumberOfDS() > 0)
                {        
                    appendXAPIResponse(message, eventsxmldata, isTemplate);
                }
            }
            else
            {
             
                eventsxmldata.setMRoot(
                    (Element)eventsxmldata.getMDocument().createElement(XML_JDE_RESPONSE_TAG));
                createXMLHeader(message, false, eventsxmldata);
                //Make sure this event is not a partial event. If partial event
                //then mNumberOfDS is always zero and do not need to add a DS detail.
                if (eventsxmldata.getMNumberOfDS() > 0)
                {
                    appendRTORXAPIRequest(message, eventsxmldata, isTemplate);
                }   
            }
        }
        else
        {
            eventsxmldata.setMRoot(
                (Element) eventsxmldata.getMDocument().createElement(XML_JDE_RESPONSE_TAG));
            createXMLHeader(message, false, eventsxmldata);
            //Make sure this event is not a partial event. If partial event
            //then mNumberOfDS is always zero and do not need to add a DS detail.
            if (eventsxmldata.getMNumberOfDS() > 0)
            {
                appendRTORXAPIRequest(message, eventsxmldata, isTemplate);
            }
        }

    }

    private void createXMLHeader(EventMessage message, boolean isSourceRoute, 
                                        EventsXMLData eventsxmldata)
    {
        Document document = eventsxmldata.getMDocument();
        Element root = eventsxmldata.getMRoot();
        document.appendChild(root);
        root.setAttribute("pwd", ""); // keep "pwd" for backwards compatibility
        root.setAttribute("token", eventsxmldata.getToken());
        root.setAttribute("role", message.getRole());
        if((message.getCategory().equalsIgnoreCase(F90710_EventTransfer.CATEGORY_XAPI))
                        && isSourceRoute)
        {
            root.setAttribute("type", "xapicallmethod");
        } 
        else        
        {
            root.setAttribute("type", "realTimeEvent");
        } 
        root.setAttribute("category", message.getCategory());
        root.setAttribute("user", message.getUser());
        if((message.getCategory().equalsIgnoreCase(F90710_EventTransfer.CATEGORY_XAPI)))
        {
            root.setAttribute("session", "");
        }
        else
        {
            root.setAttribute("session", message.getSessionID());
        }
        root.setAttribute("environment", message.getEnvironment());
        root.setAttribute("responseCreator", "XAPI");
        Node header = null;
        if(isSourceRoute)
        {
            header = document.createElement(XML_HEADER_TAG);
            root.appendChild(header);            
        }
        else
        {
            Node event = document.createElement(XML_EVENT_TAG);
            root.appendChild(event);
            header = document.createElement(XML_HEADER_TAG);
            event.appendChild(header);
        }
        // Insert Items
        insertItem(document, header, "eventVersion", "1.0");
        insertItem(document, header, "type", message.getType());
        insertItem(document, header, "user", message.getUser());
        insertItem(document, header, "role", message.getRole());
        insertItem(document, header, "application", message.getApplication());
        insertItem(document, header, "version", message.getApplicationVersion());
        insertItem(document, header, "sessionID", message.getSessionID());
        insertItem(document, header, "environment", message.getEnvironment());
        insertItem(document, header, "host", message.getHost());
        insertItem(document,header,"sequenceID",Long.toString(message.getSequenceNumber()));
        JDECalendar calendar = message.getDateTime();
        if (calendar != null)
        {
            sTimeFormatter.setTimeZone(calendar.getTimeZone());
            String time = sTimeFormatter.format(calendar.getTime());
            
            sDateFormatter.setTimeZone(calendar.getTimeZone());
            String date = sDateFormatter.format(calendar.getTime());
		  
    
            insertItem(document, header, "date", date);
            insertItem(document, header, "time", time);
        }
        else
        {
            insertItem(document, header, "date", "");
			insertItem(document, header, "time", "");            
        }
        insertItem(document, header, "scope", message.getScope());
        insertItem(document, header, "codepage", Integer.toString(eventsxmldata.getMCodePage()));
        //Add the instanceInfo part
        Node instanceInfo = document.createElement("instanceInfo");
        header.appendChild(instanceInfo);
        if(isSourceRoute)
        {                     
            //
            //  Get the source routing information, which is an XML document containing
            //  the destination host and port.
            //
            String sourceRouterInfo = message.getSourceRoute();
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "The SourceRouter Info in createXMLHeader is: " + sourceRouterInfo, null, null, null);
            }
            if ((sourceRouterInfo == null) || (sourceRouterInfo.length() == 0))
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Source Routing Info is null/blank.", null, null, null);
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Source routed event not sent", null, null, null);
            }
            //
            //  Look for a UTF-16 byte order marker at the start of the string.  If one
            //  is present skip over it so that the XML parser does not fail.
            //
            char first = sourceRouterInfo.charAt(0);
            if (first == UTF16_BYTE_ORDER_MARKER)
            {
                sourceRouterInfo = sourceRouterInfo.substring(1);
            }      
            //
            //  Parse the XML.
            //
            InputSource xmlInput = new InputSource(new StringReader(sourceRouterInfo));   
            try
            {
                Document sourceDocument = mBuilder.parse(xmlInput);
                
                //
                //  Extract the host and port.
                //
                Element hostElement = (Element)sourceDocument.getElementsByTagName("host").item(0);
                String host = hostElement.getFirstChild().getNodeValue();
                Element portElement = (Element)sourceDocument.getElementsByTagName("port").item(0);
                String portstring = null;                 
                try
                {
                    portstring = portElement.getFirstChild().getNodeValue();
                    int port = Integer.parseInt(portstring.trim());
                    if (sE1Logger.isDebug())
                    {
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Source router host and port is : "
                                        + host + " " + port, null, null, null);
                    }
                }
                catch(NumberFormatException ex)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error getting a port number"
                                    + " from XML docuemnt"+ex, null, null, ex);
                    return;                                    
                }
                insertItem(document, instanceInfo, "host", host);
                insertItem(document, instanceInfo, "port", portstring);
                insertItem(document, instanceInfo, "type", "JDENET");       
            }
            catch(Exception ex)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error parsing source routing information. " 
                                    + ex.getMessage(),null, null, ex);
            }
             
        }
        else
        {
            insertItem(document, instanceInfo, "host", message.getHost());
            //Need to get the port from the originator call object blob
            insertItem(document, instanceInfo, "port", eventsxmldata.getMPort());
            insertItem(document, instanceInfo, "type", "JDENET");
        }
    }

    /**
     * Insert HeaderInformation to Document.
     * @param document - Event Document
     * @param parent - Header Node "header"
     * @param element - Element Name
     * @param value - Element value
     */
    private void insertItem(Document document,Node parent,String element,String value)
    {
        Node node = document.createElement(element);
        parent.appendChild(node);
        // Insert ID value
        Node nodevalue;
        //if(value != null && value!= "")
        //{
        nodevalue = document.createTextNode(value);
        node.appendChild(nodevalue);
        // }
    }

    private void appendXAPIResponse(EventMessage message, EventsXMLData eventsxmldata,
                                    boolean isTemplate)
    {
        DSTemplate2 dsSpecs = null;
        Object dsKey = null;
        Document document = eventsxmldata.getMDocument();
        Element root = eventsxmldata.getMRoot();
        //Add the body
        Element body = (Element) document.createElement("body");
        body.setAttribute("elementCount", Integer.toString(eventsxmldata.getMNumberOfDS()));
        root.appendChild(body);
        //add the errorCount if errors exists.
        if (eventsxmldata.getMErrorCount() > 0)
        {
            Element errors = (Element) document.createElement("errors");
            errors.setAttribute("errorCount", Integer.toString(eventsxmldata.getMErrorCount()));
            body.appendChild(errors);
            String []errorcode = eventsxmldata.getMErrorText();
            String []errorType = eventsxmldata.getMErrorCode();
            for (int i = 0; i < errorcode.length; i++)
            {
                Element error = document.createElement("error");
                error.setAttribute("code", errorcode[i]);
                error.setAttribute("type", errorType[i]);
                errors.appendChild(error);
            }
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing EventXMLBuilder.appendXAPIResponse()", null, null, null);
        }
        EventTypeCache cache = EventTypeCache.getInstance();
        String []dsName = eventsxmldata.getMDSName();
        EventTypeDefinition eventTypeDef = eventsxmldata.getMEventTypeDef();
        EventTypeDataStructure []eventDS = eventsxmldata.getMEventDS();
        if(isTemplate)
        {
            eventDS = new EventTypeDataStructure[eventsxmldata.getMNumberOfDS()];
        }
        for (int i = 0; i < eventsxmldata.getMNumberOfDS(); i++)
        {
            Element params = document.createElement("params");
            body.appendChild(params);
            params.setAttribute("type", dsName[i]);
            params.setAttribute("executionOrder", Integer.toString(i));
            if(isTemplate)
            {
                try
                {
                    eventTypeDef =
                        cache.getEventTypeDefinition(
                            message.getCategory(),
                            message.getType(),
                            message.getEnvironment());
                    eventDS[i] =
                        (EventTypeDataStructure) eventTypeDef.getDataStructureSpec(dsName[i]);
                }
                catch (EventProcessingException ex)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error while creating EventTypeDS in createEventXMLBody. "
                                        + ex,null, null,ex);
                }
            }
            if(eventDS[i] != null)
            {
                dsSpecs = (DSTemplate2) eventDS[i].getSpecs();
                ((Element) params).setAttribute(
                    "parameterCount",
                    Integer.toString(dsSpecs.dsItem.length));
                Hashtable []dsData = eventsxmldata.getMDSData();
                
                //Convert the idItem-Value pair to szDict-Value pair
                Hashtable szDataItemValueMap = new Hashtable();                                
                Enumeration idItemKeys = null;
                if(!isTemplate)
                {                    
                    idItemKeys = dsData[i].keys(); 
                    while(idItemKeys.hasMoreElements())
                    {
                        Object idItemObj = idItemKeys.nextElement();
                        int idItem = ((Long)idItemObj).intValue();
                        for (int j = 0; j < dsSpecs.dsItem.length; j++)
                        {                        
                            if(idItem == dsSpecs.dsItem[j].idItem)
                            {
                                szDataItemValueMap.put(
                                        dsSpecs.dsItem[j].szDataItem,dsData[i].get(idItemObj));     
                            }
                        }
                    }
                }
                String keyAsString = "";
                Enumeration keys = szDataItemValueMap.keys();
                for (int j = 0; j < dsSpecs.dsItem.length; j++)
                {                     
                    if(!isTemplate)
                    {
                        dsKey = keys.nextElement();
                        Object dsValue = szDataItemValueMap.get(dsKey);
                        keyAsString = convertDSElementToFormattedString(dsValue);
                        insertItemWithAttribute(document,params,"param",
                                                keyAsString,"name",(String)dsKey);
                    }
                    if(isTemplate)
                    {
                        insertItemWithAttribute(document,params,"param","",
                                                    "name",dsSpecs.dsItem[j].szDataItem);
                    }
                }
            }
        }
    }

    private void appendRTORXAPIRequest(EventMessage message, EventsXMLData eventsxmldata,
                                        boolean isTemplate)
    {
        DSTemplate2 dsSpecs = null;
        String xmlDataType = null;
        String lastXMLDataType = null;
        String keyAsString = "";        
        Document document = eventsxmldata.getMDocument();
        Element body = (Element) document.createElement("body");
        body.setAttribute("elementCount", Integer.toString(eventsxmldata.getMNumberOfDS()));
        Node event = document.getElementsByTagName(XML_EVENT_TAG).item(0);
        event.appendChild(body);
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing EventXMLBuilder.appendRTORXAPIRequest()", null, null, null);
        }
        EventTypeCache cache = EventTypeCache.getInstance();
        JDECalendar calendar = message.getDateTime();
        String time = "";
        String date = "";
        if(calendar != null)
        {
            sTimeFormatterWithColon.setTimeZone(calendar.getTimeZone());
            time = sTimeFormatterWithColon.format(calendar.getTime());
            sDateFormatter.setTimeZone(calendar.getTimeZone());
            date = sDateFormatter.format(calendar.getTime());
        }
        String []dsName = eventsxmldata.getMDSName();
        EventTypeDataStructure []eventDS = eventsxmldata.getMEventDS();
        EventTypeDefinition eventTypeDef = eventsxmldata.getMEventTypeDef();
        if(isTemplate)
        {
            eventDS = mTemplateStructs;
        }
        Hashtable []dsData = eventsxmldata.getMDSData();
        for (int i = 0; i < eventsxmldata.getMNumberOfDS(); i++)
        {
            Element detail = document.createElement("detail");
            body.appendChild(detail);
            detail.setAttribute("date", date);
            detail.setAttribute("name", message.getFunction());
            detail.setAttribute("time", time);
            
            //detail.setAttribute("type", message.getType());
            detail.setAttribute("type", eventDS[i].getEventType());

            detail.setAttribute("DSTMPL", dsName[i]);
            detail.setAttribute("executionOrder", Integer.toString(i));
            if(isTemplate)
            {         
                try
                {
                    eventTypeDef =
                        cache.getEventTypeDefinition(
                            message.getCategory(),
                            message.getType(),
                            message.getEnvironment());
                    eventDS[i] =
                        (EventTypeDataStructure) eventTypeDef.getDataStructureSpec(dsName[i]);
                }
                catch (EventProcessingException ex)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Error while creating EventTypeDS in createEventtXMLBody. "
                                    + ex,null, null,ex);
                }
            }    
            if(eventDS[i] != null)
            {
                dsSpecs = (DSTemplate2) eventDS[i].getSpecs();
                ((Element) detail).setAttribute("parameterCount",
                                                 Integer.toString(dsSpecs.dsItem.length));
                Enumeration idItemKeys = null;
                Hashtable szDataItemValueMap = new Hashtable();
                Hashtable szDataTypeMap = new Hashtable();
                if(!isTemplate)
                {                    
                    idItemKeys = dsData[i].keys(); 
                    while(idItemKeys.hasMoreElements())
                    {
                        Object idItemObj = idItemKeys.nextElement();
                        int idItem = ((Long)idItemObj).intValue();
                        for (int j = 0; j < dsSpecs.dsItem.length; j++)
                        {                        
                            if(idItem == dsSpecs.dsItem[j].idItem)
                            {
                                szDataItemValueMap.put(
                                        dsSpecs.dsItem[j].szDataItem,dsData[i].get(idItemObj));
                                szDataTypeMap.put(
                                        dsSpecs.dsItem[j].szDataItem,
                                                    new Integer(dsSpecs.dsItem[j].dataType));
                            }
                        }
                    }
                }
                Enumeration keys = szDataItemValueMap.keys();
                Enumeration specKeys = szDataTypeMap.keys();
                if(szDataItemValueMap.size() > 0)
                {
                    for (int j = 0; j < dsSpecs.dsItem.length; j++)
                    {
                        //dataType = dsSpecs.dsItem[j].dataType;
                        Object key = specKeys.nextElement();
                        int dataType = ((Integer)szDataTypeMap.get(key)).intValue();
                        xmlDataType = convertOWTypeToString(dataType);
                        if (xmlDataType != null)
                        {
                            lastXMLDataType = xmlDataType;
                        }
                        else
                        {
                            xmlDataType = lastXMLDataType;
                        }
                        Object dsKey = null;
                        if(!isTemplate)
                        {
                            dsKey = keys.nextElement();
                            Object dsValue = szDataItemValueMap.get(dsKey);                      
                            keyAsString = convertDSElementToFormattedString(dsValue);
                        }            
                        insertItemWithAttribute(document,detail,(String)dsKey
                                                        ,keyAsString,"type",xmlDataType);
                    }
                }
                if(isTemplate)
                {
                    for (int j = 0; j < dsSpecs.dsItem.length; j++)
                    {
                        int dataType = dsSpecs.dsItem[j].dataType;
                        xmlDataType = convertOWTypeToString(dataType);
                        if (xmlDataType != null)
                        {
                            lastXMLDataType = xmlDataType;
                        }
                        else
                        {
                            xmlDataType = lastXMLDataType;
                        }                        
                        insertItemWithAttribute(document,detail,dsSpecs.dsItem[j].szDataItem,
                                                              keyAsString,"type",xmlDataType); 
                    }  
                }
            }
        }
    }
    
    /**
      * Insert HeaderInformation to Document.
      * @param document - Event Document
      * @param parent - Header Node "header"
      * @param element - Element Name
      * @param value - Element value
      */
    private void insertItemWithAttribute(Document document,Node parent,String element,
                                            String value,String attr,String attrvalue)
    {
        Element node = document.createElement(element);
        node.setAttribute(attr, attrvalue);
        parent.appendChild(node);
        // Insert ID value
        Node nodevalue;
        nodevalue = document.createTextNode(value);
        node.appendChild(nodevalue);
    }

    private String convertOWTypeToString(int type)
    {
        String result = null;
        switch (type)
        {
            case DataTypes.EVDT_CHAR :
                result = "Character";
                break;
            case DataTypes.EVDT_SZCHAR :
                result = "String";
                break;
            case DataTypes.EVDT_SHORT :
                result = "Int";
                break;
            case DataTypes.EVDT_USHORT :
                result = "Int";
                break;
            case DataTypes.EVDT_LONG :
                result = "Long";
                break;
            case DataTypes.EVDT_ULONG :
                result = "Long";
                break;
            case DataTypes.EVDT_ID :
                result = "Long";
                break;
            case DataTypes.EVDT_ID2 :
                result = "Long";
                break;
            case DataTypes.EVDT_MATH_NUMERIC :
                result = "Double";
                break;
            case DataTypes.EVDT_JDEDATE :
                result = "Date";
                break;
            case DataTypes.EVDT_BYTE :
                result = "BYTE";
                break;
            case DataTypes.EVDT_BOOL :
                result = "BOOL";
                break;
            case DataTypes.EVDT_INT :
                result = "Int";
                break;
            case DataTypes.EVDT_JDEUTIME :
                result = "Universal Time";
                break;
            default :
                result = null;
        }
        return result;
    }

    /**
     * Insert HeaderInformation to Document.
     * @param data - byte array to be converted.
     * @return EventsXMLData Object with all the values assigned to be put in XML.
     */
    private EventsXMLData convertBlobToString(byte[] data, EventMessage message, 
                 EventsXMLData eventsxmldata, boolean isTemplate)throws EventProcessingException
    {
        JdeNetInputStream jis = null;
        int blobSize = 0;
        DSTemplate2 dsSpecs = null;
        EventsBSFNData eventsData = null;
        String encoding = null;
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Executing ConvertBlobToString()", null, null,null);
        }
        if (data == null || data.length == 0)
        {
            if(!isTemplate)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Cannot convert Blob to String. Blob is null",null, null,null);
            }    
        }
        else
        {
            jis = new JdeNetInputStream(data, JdeNetConstants.ENCODE_UTF16LE);
            try
            {
                int remoteOS = Integer.parseInt(jis.readUnicodeString(10).trim());
                eventsxmldata.setMRemoteOS(remoteOS);
                int codePage = Integer.parseInt(jis.readUnicodeString(10).trim());
                eventsxmldata.setMCodePage(codePage);
                encoding = JdeUtil.getEncoding(String.valueOf(codePage));
                int storageScheme =
                    Integer.parseInt(jis.readUnicodeString(1).trim());
                eventsxmldata.setMStorageScheme(storageScheme);
                //During JDENet adaptor request we need to send originator port number 
                //in the instanceInfo information. 
                String port = jis.readUnicodeString(10);
                if(port != null || port.equalsIgnoreCase(""))
                {
                    eventsxmldata.setMPort(port.trim());
                }
                else
                {
                    throw new  EventProcessingException("Port number is null or blank");
                }
                int isToken = Integer.parseInt(jis.readUnicodeString(1));
                eventsxmldata.setIsToken(isToken);
                if (isToken == 1)
                {
                    int tokenLength = Integer.parseInt(jis.readUnicodeString(6).trim());
                    eventsxmldata.setTokenLength(tokenLength);
                    String token = jis.readUnicodeString(tokenLength);
                    eventsxmldata.setToken(token);
                }
                int errorCount = Integer.parseInt(jis.readUnicodeString(10).trim());
                eventsxmldata.setMErrorCount(errorCount);
                if (errorCount > 0)
                {                    
                    int []errorCodeSize = new int[errorCount];            
                    String [] errorCode = new String[errorCount];                   
                    int []errorTextSize = new int[errorCount];                   
                    String []errorText = new String[errorCount];                 
                    int []errorDescSize = new int[errorCount];                  
                    String []errorDescription = new String[errorCount];     
                    for (int i = 0; i < errorCount; i++)
                    {
                        errorCodeSize[i] = Integer.parseInt(jis.readUnicodeString(6).trim());
                        errorCode[i] = jis.readUnicodeString(errorCodeSize[i]);
                        errorTextSize[i] = Integer.parseInt(jis.readUnicodeString(6).trim());
                        errorText[i] = jis.readUnicodeString(errorTextSize[i]);
                        errorDescSize[i] = Integer.parseInt(jis.readUnicodeString(6).trim());
                        errorDescription[i] = jis.readUnicodeString(errorDescSize[i]); 
                    }
                    eventsxmldata.setMErrorCodeSize(errorCodeSize);
                    eventsxmldata.setMErrorCode(errorCode);
                    eventsxmldata.setMErrorTextSize(errorTextSize);
                    eventsxmldata.setMErrorText(errorText);
                    eventsxmldata.setMErrorDescSize(errorDescSize);
                    eventsxmldata.setMErrorDescription(errorDescription);
                }
                int numberOfDS =
                    Integer.parseInt(jis.readUnicodeString(10).trim());             
                String[] dsName = new String[numberOfDS];        
                Hashtable []dsData = new Hashtable[numberOfDS];            
                for(int i=0;i<dsData.length;i++)
                {
                    dsData[i] = new Hashtable();
                }
                EventTypeDataStructure[] eventDS = new EventTypeDataStructure[numberOfDS];
                EventTypeCache cache = EventTypeCache.getInstance();
                EventTypeDefinition eventTypeDef = null;
                for (int i = 0; i < numberOfDS; i++)
                {
                    dsName[i] = jis.readUnicodeString(11).trim();
                    blobSize =
                        Integer.parseInt(jis.readUnicodeString(10).trim());
                    try
                    {
                        eventTypeDef =
                            cache.getEventTypeDefinition(
                                message.getCategory(),
                                message.getType(),
                                message.getEnvironment());
                        eventDS[i] =
                            (EventTypeDataStructure) eventTypeDef.getDataStructureSpec(dsName[i]);
                        if(eventDS[i] != null)
                        {
                            dsSpecs = (DSTemplate2) eventDS[i].getSpecs();
                            eventsData = new EventsBSFNData(dsSpecs, dsData[i]);
                            eventsData.setEncoding(encoding);
                            eventsData.inflate(jis);
                        } 
                        else
                        {
                            StringBuffer buffer = new StringBuffer(150);
                            buffer.append("Event XML Generation:");
                            buffer.append(" EventTypeDataStructure is null");
                            buffer.append(System.getProperty("line.separator"));
                            buffer.append("  DataStructureName=").append(dsName[i]);
                            buffer.append(System.getProperty("line.separator"));
                            buffer.append("  EventCategory=").append(eventTypeDef.getCategory());
                            buffer.append(System.getProperty("line.separator"));
                            buffer.append("  EventType=").append(eventTypeDef.getType());
                            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,buffer.toString(),null, null,null);                           
                            //
                            // Move the pointer forward by exactly the Blob size.
                            //
                            int count = 0;
                            while(count < blobSize)
                            {
                                jis.readByte();
                                count++;
                            }                 
                        }
                    }
                    catch (EventProcessingException ex)
                    {
                        String msg = "Error while creating EventTypeDS in " 
                                        + "createEventtXMLBody. " + ex;
                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null,ex);
                        throw new EventProcessingException(msg, ex);
                    }      
                }
                //Set all the values to the eventsxmldata object so that we can use it later.
                eventsxmldata.setMNumberOfDS(numberOfDS);
                eventsxmldata.setMDSName(dsName);
                eventsxmldata.setMDSData(dsData);
                eventsxmldata.setMEventDS(eventDS);
                eventsxmldata.setMEventTypeDef(eventTypeDef);                
                jis.close();
            }
            catch (IOException ex)
            {
                String msg = "IOException occurred during blob conversion. " + ex;
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, ex);
                throw new EventProcessingException(msg, ex);
            }
            catch (Exception e)
            {
                String msg = "Exception occurred during blob conversion. " + e;
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Finished ConvertBlobToString()", null, null, null);
        }
        return eventsxmldata;
    }
    
    /**
     * Converts an element in the event data structure to a properly formatted String.
     * 
     * @param dsValue the data structure element
     * @return the formatted String
     */
    private String convertDSElementToFormattedString(Object dsValue)
    {
        String keyAsString;
        if (dsValue instanceof Integer)
        {
            int intValue = ((Integer) dsValue).intValue();
            keyAsString = Integer.toString(intValue);
        }
        else if (dsValue instanceof Long)
        {
            long longValue = ((Long) dsValue).longValue();
            keyAsString = Long.toString(longValue);
        }
        else if (dsValue instanceof Double)
        {
            double doubleValue = ((Double) dsValue).doubleValue();
            keyAsString = Double.toString(doubleValue);
        }
        else if (dsValue instanceof tagMathNumeric)
        {
            keyAsString = ((tagMathNumeric) dsValue).toString();
        }
        else if (dsValue instanceof SqlDate)
        {
            // convert to SqlDate
            SqlDate sqlDate = (SqlDate)dsValue;
            
            // format as required to maintain backwards compatibility
            keyAsString = DS_DATE_FORMAT.format(sqlDate.convertToJDECalendar().getTime());
        }
        else if (dsValue instanceof JDECalendar)
        {
            // convert to JDECalendar
            JDECalendar calendar = (JDECalendar)dsValue;
            
            // format as required to maintain backwards compatibility
            keyAsString = DS_DATE_FORMAT.format(calendar.getTime());
        }
        else
        {
            keyAsString = dsValue.toString();
        }                                     
        return keyAsString;
    }

    private class EventsXMLData
    {
        private int mRemoteOS = 0;
        private int mStorageScheme = 0;
        private int mCodePage = 1252; /*Code page for English*/
        private int mIsToken = 0;
        private int mErrorCount = 0;
        private int[] mErrorCodeSize;
        private String[] mErrorCode;
        private int[] mErrorTextSize;
        private int[] mErrorDescSize;
        private int mNumberOfDS = 0;
        private String[] mDSName = null;
        private String[] mErrorText;
        private String mToken = "";
        private int mTokenLength = 0;
        private String[] mErrorDescription;
        private EventTypeDataStructure[] mEventDS = null;
        private EventTypeDefinition mEventTypeDef = null;
        private Hashtable[] mDSData = null;
        private Document mDocument = null;
        private Element mRoot = null;
        private String mPort = "";
    
        /**
         *
         */
        public EventsXMLData()
        {
        }

        /**
         * @return
         */
        public int getMCodePage()
        {
            return mCodePage;
        }

        /**
         * @return
         */
        public Document getMDocument()
        {
            return mDocument;
        }

        /**
         * @return
         */
        public Hashtable[] getMDSData()
        {
            return mDSData;
        }

        /**
         * @return
         */
        public String[] getMDSName()
        {
            return mDSName;
        }

        /**
         * @return
         */
        public String[] getMErrorCode()
        {
            return mErrorCode;
        }

        /**
         * @return
         */
        public int[] getMErrorCodeSize()
        {
            return mErrorCodeSize;
        }

        /**
         * @return
         */
        public int getMErrorCount()
        {
            return mErrorCount;
        }

        /**
         * @return
         */
        public String[] getMErrorDescription()
        {
            return mErrorDescription;
        }

        /**
         * @return
         */
        public int[] getMErrorDescSize()
        {
            return mErrorDescSize;
        }

        /**
         * @return
         */
        public String[] getMErrorText()
        {
            return mErrorText;
        }

        /**
         * @return
         */
        public int[] getMErrorTextSize()
        {
            return mErrorTextSize;
        }

        /**
         * @return
         */
        public EventTypeDataStructure[] getMEventDS()
        {
            return mEventDS;
        }

        /**
         * @return
         */
        public EventTypeDefinition getMEventTypeDef()
        {
            return mEventTypeDef;
        }

        /**
         * @return
         */
        public int getIsToken()
        {
            return mIsToken;
        }
        /**
         * @return
         */
        public int getMNumberOfDS()
        {
            return mNumberOfDS;
        }

        /**
         * @return
         */
        public String getToken()
        {
            return mToken;
        }

        /**
         * @return
         */
        public int getTokenLength()
        {
            return mTokenLength;
        }

        /**
         * @return
         */
        public int getMRemoteOS()
        {
            return mRemoteOS;
        }

        /**
         * @return
         */
        public Element getMRoot()
        {
            return mRoot;
        }

        /**
         * @return
         */
        public int getMStorageScheme()
        {
            return mStorageScheme;
        }

        /**
         * @param i
         */
        public void setMCodePage(int i)
        {
            mCodePage = i;
        }

        /**
         * @param document
         */
        public void setMDocument(Document document)
        {
            mDocument = document;
        }

        /**
         * @param hashtables
         */
        public void setMDSData(Hashtable[] hashtables)
        {
            mDSData = hashtables;
        }

        /**
         * @param strings
         */
        public void setMDSName(String[] strings)
        {
            mDSName = strings;
        }

        /**
         * @param strings
         */
        public void setMErrorCode(String[] strings)
        {
            mErrorCode = strings;
        }

        /**
         * @param is
         */
        public void setMErrorCodeSize(int[] is)
        {
            mErrorCodeSize = is;
        }

        /**
         * @param i
         */
        public void setMErrorCount(int i)
        {
            mErrorCount = i;
        }

        /**
         * @param strings
         */
        public void setMErrorDescription(String[] strings)
        {
            mErrorDescription = strings;
        }

        /**
         * @param is
         */
        public void setMErrorDescSize(int[] is)
        {
            mErrorDescSize = is;
        }

        /**
         * @param strings
         */
        public void setMErrorText(String[] strings)
        {
            mErrorText = strings;
        }

        /**
         * @param is
         */
        public void setMErrorTextSize(int[] is)
        {
            mErrorTextSize = is;
        }

        /**
         * @param structures
         */
        public void setMEventDS(EventTypeDataStructure[] structures)
        {
            mEventDS = structures;
        }

        /**
         * @param definition
         */
        public void setMEventTypeDef(EventTypeDefinition definition)
        {
            mEventTypeDef = definition;
        }

        /**
         * @param i
         */
        public void setIsToken(int i)
        {
            mIsToken = i;
        }


        /**
         * @param i
         */
        public void setMNumberOfDS(int i)
        {
            mNumberOfDS = i;
        }

        /**
         * @param string
         */
        public void setToken(String string)
        {
            mToken = string;
        }

        /**
         * @param i
         */
        public void setTokenLength(int i)
        {
            mTokenLength = i;
        }

        /**
         * @param i
         */
        public void setMRemoteOS(int i)
        {
            mRemoteOS = i;
        }

        /**
         * @param element
         */
        public void setMRoot(Element element)
        {
            mRoot = element;
        }

        /**
         * @param i
         */
        public void setMStorageScheme(int i)
        {
            mStorageScheme = i;
        }

        /**
         * @return
         */
        public String getMPort()
        {
            return mPort;
        }

        /**
         * @param string
         */
        public void setMPort(String string)
        {
            mPort = string;
        }
    }

    public String dom2string(Document document) throws IOException {
        StringWriter stringOut = new StringWriter();
        OutputFormat of = new OutputFormat();
        XMLSerializer serializer = new XMLSerializer(stringOut, of);
        serializer.serialize(document);
        return stringOut.toString();
    }
}
