package com.peoplesoft.pt.e1.server.enterprise.events.connectorsvc;

import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;

import com.peoplesoft.pt.e1.common.events.EventProcessingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SchemaGenerationLogic {

    private Document doc = null;
    private Document schemaDoc = null;
    private Element schemaElement;
    private Element elmtCT;
    private Element oschemaelmt;
    private Element simpleContent = null;
    private Element extension = null;
    private Element elmtComplex = null;
    boolean skip = false;
    boolean zFileEvent = false;
    private String docNameSpace = "http://www.schemas.e1.oracle.com";
    private String defaultNameSapce = "http://www.w3.org/2001/XMLSchema";
    private String prefix = "xsd";
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(SchemaGenerationLogic.class.getName());

    public static SchemaGenerationLogic getInstance(){
            return new SchemaGenerationLogic();
    }

    public String generateSchema(String templateString) throws EventProcessingException {

            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setIgnoringElementContentWhitespace(true);

            try {
                    doc = dfactory.newDocumentBuilder().parse(new InputSource(new StringReader(templateString)));
                    schemaDoc = dfactory.newDocumentBuilder().newDocument();
            }
            catch (SAXException e) {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Exception in parsing the template:"+e.getMessage(),null,null,e);
                    throw new EventProcessingException("Exception while parsing the template "+e.getMessage());
            }
            catch(IOException ioe){
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Exception: "+ioe.getMessage(),null,null,ioe);
                    throw new EventProcessingException("Exception while parsing the template for Schema Generation "+ioe.getMessage());
            }
            catch(ParserConfigurationException pe){
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Exception: "+pe.getMessage(),null,null,pe);
                    throw new EventProcessingException("Exception while creating newDocumentBuilder " +pe.getMessage());
            }
            try{
                    schemaElement = schemaDoc.createElement(prefix+":schema");
                    schemaElement.setAttribute("xmlns:"+prefix,defaultNameSapce);
                    schemaElement.setAttribute("xmlns",docNameSpace);
                    schemaElement.setAttribute("targetNamespace",docNameSpace);
                    schemaElement.setAttribute("elementFormDefault","qualified");
            }
            catch(DOMException domE){
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Exception: "+ domE.getMessage(),null,null,domE);
                    throw new EventProcessingException("DOM Exception " +domE.getMessage());
            }

            if(doc.getFirstChild().getFirstChild().getNodeName().equalsIgnoreCase("event")){
                    zFileEvent = false;
            }
            else if(doc.getFirstChild().getFirstChild().getNodeName().equalsIgnoreCase("transaction")){
                    zFileEvent =true;
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Generating schema for Z Event ",null,null,null);
            }
       
       try{
       processXML(doc,schemaElement);
       }catch(DOMException domE){
               sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Exception: "+ domE.getMessage(),null,null,domE);
               throw new EventProcessingException("Exception while processing XML for Schema Generation " +domE.getMessage());
       }
        
    // Serialize the document onto System.out
    /*
    DOMWriter writer = new XMLSerializer();
    String documentElement = writer.writeToString(schemaElement);
    */
   // Source source = new DOMSource(schemaElement);
    String elemString = null;
        try {
            elemString = element2String(schemaElement);
        } catch (IOException e) {
        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Exception during converting xml element to string: " + e.getMessage(), null, null, e);
        }
        Source source = new StreamSource(new StringReader(elemString));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
    Result result = new StreamResult(os);
    Transformer xformer;
    try {
       xformer = TransformerFactory.newInstance().newTransformer();
       xformer.transform(source, result);
    } catch (TransformerConfigurationException e) {
        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"TransformerConfigurationException: " + e.getMessage(), null, null, e);
    }
    catch (TransformerException e) {
       sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"TransformerException: " + e.getMessage(), null, null, e);                    
    }
    catch(Exception e){
        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception during Trasformation: " + e.getMessage(), null, null, e);                     
    }

    String documentElement = os.toString();
    int indexSch = documentElement.indexOf("<"+prefix+":schema");
    if(indexSch < 0){
        int indexElm = documentElement.indexOf("<"+prefix+":element name");
        String str1 = documentElement.substring(0,indexElm);
        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"element index"+indexElm, null, null, null);                         
        String str3 = documentElement.substring(indexElm,documentElement.length());
        documentElement = str1+"<"+prefix+":schema xmlns:"+prefix+"=\""+defaultNameSapce+"\" xmlns=\""+docNameSpace+"\" elementFormDefault=\"qualified\" targetNamespace=\""+docNameSpace+"\">"+str3+"</"+prefix+":schema>";
    }
    return documentElement;
}

    private void processXML(Node domNode ,Element oParent) throws DOMException{
        
        NodeList nList = domNode.getChildNodes();
       
        boolean attOnly = false;
        
            if (domNode.getNodeType()== Node.ELEMENT_NODE)
                {
                Element oelmt = (Element) domNode;
                String detailTag = "detail";
                
                NodeList oChildList = domNode.getChildNodes();
                oschemaelmt = schemaDoc.createElement(prefix+":element");
                
                if(oelmt.getNodeName().equalsIgnoreCase(detailTag))
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"oelmt getAttribute(DSTMPL)->"+oelmt.getAttribute("DSTMPL"),null,null,null);
                
                if(oelmt.getNodeName().equalsIgnoreCase(detailTag) && oelmt.getAttribute("DSTMPL")!= null)
                {
                    detailTag = detailTag+"_"+oelmt.getAttribute("DSTMPL");
                    oschemaelmt.setAttribute("name",detailTag);
                }
                else
                    oschemaelmt.setAttribute("name",oelmt.getNodeName());
                
                oParent.appendChild(oschemaelmt);

                Element elmtSQ = schemaDoc.createElement(prefix+":sequence");
                
                if((oChildList.getLength()>0 && domNode.getFirstChild().getNodeType()!=Node.TEXT_NODE)){
                    elmtCT = schemaDoc.createElement(prefix+":complexType");
                    oschemaelmt.appendChild(elmtCT);
                    elmtCT.appendChild(elmtSQ);
                }
                else if(oChildList.getLength()>0 && domNode.getFirstChild().getNodeType()==Node.TEXT_NODE){
                      
                      // This If condition seperate out Header fields of XAPI and RTE, and set the type of the field
                      if(!zFileEvent && (oelmt.getParentNode().getNodeName().equalsIgnoreCase("header") || oelmt.getParentNode().getNodeName().equalsIgnoreCase("instanceInfo"))){
                           oschemaelmt.setAttribute("type",getHeaderTypeInfo(oelmt.getNodeName()));                           
                      }
                      // This If condition checks for all Z Event Field with with TextNode as First Child, 
                      // All RTE & XAPI Field without Header Fields whose having TEXT NODE as First Child,
                      // This will not include fields having only one attribute called 'Type'
                      else if(oelmt.getAttributes().getLength() > 1 || (oelmt.getAttributes().getLength()==1 && !oelmt.getAttributes().item(0).getNodeName().equalsIgnoreCase("type"))){
                          createComplexType(oelmt);
                          attOnly = true;
                      }
                      else{
                          String stElemval = domNode.getFirstChild().getNodeValue();
                          String nodeType = getNodeType(stElemval);
                          oschemaelmt.setAttribute("type",nodeType.toLowerCase());
                      }
                }
                // This If condition set type attribute for fields having no child nodes
                else if(oChildList.getLength() <= 0){
                    if(oelmt.getAttributes().getLength() > 1 || (oelmt.getAttributes().getLength()==1 && !oelmt.getAttributes().item(0).getNodeName().equalsIgnoreCase("type"))){
                        createComplexType(oelmt);
                        attOnly = true;
                    }
                    else{
                        try{
                            if(oelmt.getParentNode().getNodeName().equalsIgnoreCase("header") || oelmt.getParentNode().getNodeName().equalsIgnoreCase("instanceInfo"))
                                oschemaelmt.setAttribute("type",getHeaderTypeInfo(oelmt.getNodeName()));                           
                            else
                                oschemaelmt.setAttribute("type",getFieldType(oelmt.getAttribute("type")));
                        }
                        catch(Exception e){
                            oschemaelmt.setAttribute("type",prefix+":string");
                        }
                    }
                }
               
                try
                {
                if(!oelmt.getNodeName().equalsIgnoreCase("jdeResponse")){
                    oschemaelmt.setAttribute("minOccurs","0");
                }

                for(int i=0 ; i<oelmt.getAttributes().getLength(); i++){
                    if((!zFileEvent && !oelmt.getParentNode().getNodeName().equals("detail")) || (zFileEvent && oelmt.getChildNodes().getLength() > 0)){
                        if(attOnly)
                            addAttributeNode(extension,oelmt.getAttributes().item(i).getNodeName());
                        else
                            addAttributeNode(elmtCT,oelmt.getAttributes().item(i).getNodeName());
                    }
                }
                }
                catch(NullPointerException npe)
                {
                    //Ignore
                }
                for(int i=0;i<oChildList.getLength();i++)
                    {
                    processXML(nList.item(i),elmtSQ);
                    }
                }
            else {
                 Node oNode = nList.item(0);
                 if(oNode!=null)
                     processXML(oNode,oParent);
            }
    }
    
    private void createComplexType(Element oelmt){
        elmtComplex = schemaDoc.createElement(prefix+":complexType");
        oschemaelmt.appendChild(elmtComplex);
        simpleContent = schemaDoc.createElement(prefix+":simpleContent");
        extension = schemaDoc.createElement(prefix+":extension");
        extension.setAttribute("base",getHeaderTypeInfo(oelmt.getNodeName()));
        simpleContent.appendChild(extension);
        elmtComplex.appendChild(simpleContent);
       }
    // This method creates attribute node in the Schema
    
    private void addAttributeNode(Element attParentNode, String attVal) throws DOMException{
                if(attVal.equalsIgnoreCase("xmlns"))
                    return;
                Element attElement = schemaDoc.createElement(prefix+":attribute");
                attElement.setAttribute("name",attVal);
                attElement.setAttribute("type",getAttType(attVal));
                attParentNode.appendChild(attElement);
        }

    private String getFieldType(String type){

        if(type.equalsIgnoreCase("String"))
                return prefix+":string";
        else if(type.equalsIgnoreCase("Long"))
                return prefix+":long";
        else if(type.equalsIgnoreCase("Double"))
                return prefix+":double";
        else if (type.equalsIgnoreCase("Integer") || type.equalsIgnoreCase("int"))
                return prefix+":integer";
        else if (type.equalsIgnoreCase("Date") || type.equalsIgnoreCase("Dat"))
                return prefix+":date";  
        else if (type.equalsIgnoreCase("Time"))
                return prefix+":time";
        else if (type.equalsIgnoreCase("Bool") || type.equalsIgnoreCase("Boolean"))
                return prefix+":boolean";
        else
            return prefix+":string";
    }
    
    private String getAttType(String att){
        if(att.equalsIgnoreCase("elementCount") || att.equalsIgnoreCase("executionOrder") || att.equalsIgnoreCase("parameterCount") || att.equalsIgnoreCase("code") || att.equalsIgnoreCase("port"))            
            return prefix+":integer";
        else if(att.equalsIgnoreCase("date"))            
            return prefix+":date";
        else if(att.equalsIgnoreCase("time"))            
            return prefix+":time";
        else 
            return prefix+":string";
    }
    
    private String getNodeType(String eleVal){
        try{
            Integer.parseInt(eleVal);
        }
        catch(NumberFormatException nfe){
            return prefix+":string";
        }
        return prefix+":integer";
    }
    
    private String getHeaderTypeInfo(String headerFiled){
       if(headerFiled.equals("eventversion") || headerFiled.equals("version"))
            return prefix+":float";
       //else if(headerFiled.equals("type") || headerFiled.equals("user")|| headerFiled.equals("role") || headerFiled.equals("application") || headerFiled.equals("environment") || headerFiled.equals("host") || headerFiled.equals("scope") || headerFiled.equals("type") || headerFiled.equals("codepage") || headerFiled.equals("returnCode"))     
      //      return prefix+":string";
       else if(headerFiled.equals("sequenceID"))     
            return prefix+":integer";
       else if(headerFiled.equals("date"))     
             return prefix+":date";
	   else if(headerFiled.equals("time"))     
            return prefix+":time";       
       else
            return prefix+":string";
    }

   public String element2String(Element element)
        throws IOException
    {
        StringWriter stringwriter = new StringWriter();
        OutputFormat outputformat = new OutputFormat();
        XMLSerializer xmlserializer = new XMLSerializer(stringwriter, outputformat);
        xmlserializer.serialize(element);
        return stringwriter.toString();
    }
}

