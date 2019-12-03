package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;

import com.jdedwards.base.logging.log4j.LogUtils;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;

public class ProcessEventForESB
{

        private Document doc = null;
        private Document newDoc = null;
        private Element rootElement = null;
        private Element newElement = null;
        boolean skip = false;
        boolean zFileEvent = false;
        private static String docNameSpace = "http://www.schemas.e1.oracle.com";
        private static E1Logger sE1Logger =
            JdeLog.getE1Logger(ProcessEventForESB.class.getName());

        public ProcessEventForESB()
        {
        }

        public String initialize(String eventXML)
        {
                String xmlStr = eventXML.replaceAll(">[ \\n\\t\\r]<", "><").replaceAll(">\\s*",">").replaceAll("\\s*<","<");
                DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
                dfactory.setIgnoringElementContentWhitespace(true);
                dfactory.setNamespaceAware(true);
                try {
                        DocumentBuilder db = dfactory.newDocumentBuilder();
                        doc = dfactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlStr)));
                        newDoc = db.newDocument();
                }
                catch(ParserConfigurationException pe){
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"ParserConfigurationException: " + pe.getMessage(), null, null, pe);
                }
                catch(SAXException se) {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"SAXException: " + se.getMessage(), null, null, se);
                }
                catch(IOException ie) {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"IOException: " + ie.getMessage(), null, null, ie);
                }

                try{
                        rootElement = (Element)newDoc.createElement("EventData");
                }
                catch(DOMException domE){
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"DOMException: " + domE.getMessage(), null, null, domE);
                }
                Element jeResponseElm = null;
                try{
                        processXML(doc,rootElement);
                        jeResponseElm = (Element)rootElement.getFirstChild();

               }
               catch(DOMException domE){
                   sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"DOMException: " + domE.getMessage(), null, null, domE);
               }
                 // Prepare the DOM document for writing
               //  Source source = new DOMSource(jeResponseElm);
			    String stringXML = null;
			   try {
				   stringXML = element2String(jeResponseElm);
			   } 
			  catch (IOException e) {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Exception during converting xml element to string: " + e.getMessage(), null, null, e);
               }
                Source source = new StreamSource(new StringReader(stringXML));
                Writer out = new StringWriter();
                 Result result = new StreamResult(out);
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
                String documentElement = out.toString();
                documentElement = documentElement.replaceFirst("xmlns=\"\"","xmlns=\""+docNameSpace+"\"");
                return documentElement;
        }

        private void processXML(Node domNode ,Element oParent) throws DOMException
        {
               NodeList nList = domNode.getChildNodes();
               if (domNode.getNodeType()== Node.ELEMENT_NODE)
               {
                    Element oelmt = (Element) domNode;
                    String detailTag = "detail";
                    NodeList oChildList = domNode.getChildNodes();
                    if(oelmt.getNodeName().equalsIgnoreCase(detailTag) && oelmt.getAttribute("DSTMPL")!= null)
                    {
                        detailTag = detailTag+"_"+oelmt.getAttribute("DSTMPL");
                        newElement = newDoc.createElement(detailTag);
                    }
                    else
                        newElement = newDoc.createElement(oelmt.getNodeName());
                        oParent.appendChild(newElement);
                        Element parentNode ;
                        try
                        {
                            parentNode = (Element)newElement.getParentNode();
                            if((oelmt.getChildNodes().getLength()>0 && oelmt.getFirstChild().getNodeType()!=Node.TEXT_NODE))
                                parentNode = newElement;
                        }
                        catch(NullPointerException e)
                        {
                            parentNode = newElement;
                        }
                        if(oelmt.getNodeName().equalsIgnoreCase("jdeResponse"))
                            oelmt.setAttribute("xmlns",docNameSpace);

                        if((oChildList.getLength()==1 && domNode.getFirstChild().getNodeType()==Node.TEXT_NODE))
                        {
                            Node textNode = newDoc.createTextNode(oelmt.getFirstChild().getNodeValue());
                            newElement.appendChild(textNode);
                        }
                        if(oelmt.getAttributes().getLength()> 1)
                        {
                              for(int i=0; i<oelmt.getAttributes().getLength();i++)
                              {
                                  String attName = oelmt.getAttributes().item(i).getNodeName();
                                  newElement.setAttribute(attName,oelmt.getAttribute(attName));
                              }
                         }
                         else if(oelmt.getAttributes().getLength() == 1 && !oelmt.getAttributes().item(0).getNodeName().equalsIgnoreCase("type"))
                         {
                              String attName = oelmt.getAttributes().item(0).getNodeName();
                              newElement.setAttribute(attName,oelmt.getAttribute(attName));
                         }
                         for(int i=0;i<oChildList.getLength();i++)
                         {
                              processXML(nList.item(i),parentNode);
                         }
                    }
                    else
                    {
                         Node oNode = nList.item(0);
                         if(oNode!=null)
                             processXML(oNode,oParent);
                    }
       }

    public String element2String(Element element) throws IOException {
        StringWriter stringOut = new StringWriter();
        OutputFormat of = new OutputFormat();
        XMLSerializer serializer = new XMLSerializer(stringOut, of);
        serializer.serialize(element);
        return stringOut.toString();
    }
    
}
