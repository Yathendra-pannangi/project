//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.clientsvc.internal;

//=================================================
// Imports from java namespace
//=================================================
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;


/**
*  This class is used to marshal and unmarshal client requests and responses
*  to and from XML.
*/
public class XmlMarshaller
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static final String MAPPING_XML =
        "com/peoplesoft/pt/e1/common/events/clientsvc/internal/CastorMapping.xml";
    
    private static final int MAX_UNMARSHALLERS = 5;

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    private Mapping mMapping;
    
    private List mUnmarshallers = new LinkedList();

    //=================================================
    // Constructors.
    //=================================================

    /**
    *  Create a new marshaller.
    *
    *  @throws XmlMarshalException  An error occured during initialization.
    */
    public XmlMarshaller() throws XmlMarshalException
    {
        //
        //  Load the mappings.
        //
        ClassLoader classLoader = XmlMarshaller.class.getClassLoader();
        InputStream mappingAsInputStream = classLoader.getResourceAsStream(MAPPING_XML);
        if (mappingAsInputStream == null)
        {
            throw new XmlMarshalException("Failed to load caster mapping file");
        }
        InputSource mappingAsInputSource = new InputSource(mappingAsInputStream);
        mMapping = new Mapping();
        try
        {
            mMapping.loadMapping(mappingAsInputSource);
        }
        catch (Exception e)
        {
            throw new XmlMarshalException("Failed to initialize Castor", e);
        }
    }

    //=================================================
    // Methods.
    //=================================================

    /**
    *  Marshal a request.
    *
    *  @param request  The request to marshal.
    *
    *  @return The marshaled XML string.
    *
    *  @throws XmlMarshalException  An error occured during the marshaling process.
    */
    public String marshalRequest(ClientRequest request) throws XmlMarshalException
    {
        if (request == null)
        {
            throw new NullPointerException("request is null");
        }

        return marshalObject(request);
    }

    /**
    *  Unmarshal an XML request.
    *
    *  @param xml  The XML request.
    *
    *  @return  The request object.
    *
    *  @throws XmlMarshalException  An error occured during the unmarshaling process.
    */
    public ClientRequest unmarshalRequest(String xml) throws XmlMarshalException
    {
        return (ClientRequest)unmarshalObject(xml);
    }

    /**
    *  Marshal a response.
    *
    *  @param response  The response to marshal.
    *
    *  @return The marshaled XML string.
    *
    *  @throws XmlMarshalException  An error occured during the marshaling process.
    */
    public String marshalResponse(ClientResponse response) throws XmlMarshalException
    {
        if (response == null)
        {
            throw new NullPointerException("response is null");
        }

        return marshalObject(response);
    }

    /**
    *  Unmarshal an XML response.
    *
    *  @param xml  The XML response.
    *
    *  @return  The response object.
    *
    *  @throws XmlMarshalException  An error occured during the unmarshaling process.
    */
    public ClientResponse unmarshalResponse(String xml) throws XmlMarshalException
    {
        return (ClientResponse)unmarshalObject(xml);
    }

    private String marshalObject(Object obj) throws XmlMarshalException
    {
        //
        //  Set the SAX parser to the Xerces parser.
        //
        String originalParser =
            System.setProperty("org.xml.sax.parser", "org.apache.xerces.parsers.SAXParser");

        try
        {
            //
            //  Create objects used for marshalling.
            //
            StringWriter outWriter = new StringWriter();
            Marshaller marshaller = new Marshaller(outWriter);
            marshaller.setMapping(mMapping);
            marshaller.setEncoding(CharacterEncoding.ENCODING);
            
            //
            //  Marshal the object.
            //
            marshaller.marshal(obj);
            outWriter.flush();
            outWriter.close();
            String xml = outWriter.toString();

            return xml;
        }
        catch (IOException e)
        {
            throw new XmlMarshalException(e);
        }
        catch (MappingException e)
        {
            throw new XmlMarshalException(e);
        }
        catch (MarshalException e)
        {
            throw new XmlMarshalException(e);
        }
        catch (ValidationException e)
        {
            throw new XmlMarshalException(e);
        }
        finally
        {
            //
            //  Restore the original SAX parser.
            //
            if (originalParser != null)
            {
                System.setProperty("org.xml.sax.parser", originalParser);
            }
        }
    }

    private Object unmarshalObject(String xml) throws XmlMarshalException
    {
        //Removing invalid junk character sequence &#1
        //xml = xml.replaceAll("&#x([0-9]|(1[0-9])|20|[a-fA-F]|(1[a-fA-F])|(18([0-9]|[a-fA-F]))|(8[1|dD|fF])|([9|aA][0|dD])|(7[fF]|190));","");
		xml = removeInvalidXMLChars(xml);
        
        Unmarshaller unmarshaller = getUnmarshaller();
        try
        {
            Object obj = unmarshaller.unmarshal(new StringReader(xml));
            return obj;
        }
        catch (MarshalException e)
        {
            throw new XmlMarshalException(e);
        }
        catch (ValidationException e)
        {
            throw new XmlMarshalException(e);
        }
        finally
        {
            returnUnmarshaller(unmarshaller);
        }
    }
    
    private synchronized Unmarshaller getUnmarshaller() throws XmlMarshalException
    {
        Unmarshaller result = null;
        if (mUnmarshallers.size() > 0)
        {
            result = (Unmarshaller)mUnmarshallers.remove(0);
        }
        else
        {
            try
            {
                result = new Unmarshaller(mMapping);
                result.setWhitespacePreserve(true);
            }
            catch (MappingException e)
            {
                throw new XmlMarshalException(e);
            }
        }
        return result;
    }
    
    private synchronized void returnUnmarshaller(Unmarshaller umarshaller)
    {
        if ((umarshaller != null) && (mUnmarshallers.size() < MAX_UNMARSHALLERS))
        {
            mUnmarshallers.add(umarshaller);
        }
    }

    private String removeInvalidXMLChars(String str) {

        if (str == null || ("".equals(str))) {
            return "";
        }
        StringBuffer out = new StringBuffer();
        char ch;

        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);

            if (ch == '\u200b') {                     //remove Zero Width Space
            continue;
            }
            if ((ch == 0x9) ||
                (ch == 0xA) ||
                (ch == 0xD) ||
                ((ch >= 0x20) && (ch <= 0xD7FF)) ||
                ((ch >= 0xE000) && (ch <= 0xFFFD)) ||
                ((ch >= 0x10000) && (ch <= 0x10FFFF))) {
            out.append(ch);
            }
        }
        return out.toString();
        }
		
		}
