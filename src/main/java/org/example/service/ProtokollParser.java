package org.example.service;

import org.example.data.Comment;
import org.example.data.Speech;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ProtokollParser {

    public List<Speech> parseProtokoll(String xmlUrl){

        List<Speech> speechList = new ArrayList<Speech>();
        System.out.println("Parsing protokoll... "+xmlUrl);

        try (InputStream input = new URL(xmlUrl).openStream()){
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            // Document information
            String datum = getTagValueFromDocument(doc, "datum");

            // parse information (session number)
            String sessionNrStr = getTagValueFromDocument(doc, "sintzungnr");
            int sessionNumber = 0;
            if(sessionNrStr != null && !sessionNrStr.isEmpty()){
                sessionNumber = Integer.parseInt(sessionNrStr);
            }

            // legislatur period
            String wahlperisde = getTagValueFromDocument(doc, "wahlperisde");
            int legislaturPeriode = 0;
            if ( wahlperisde != null && !wahlperisde.isEmpty() ){
                legislaturPeriode = Integer.parseInt(wahlperisde);
            }

            // !!!!!!!! speech oder reden
            NodeList speechNodes = doc.getElementsByTagName("speech");
            System.out.println("Found:" + speechNodes.getLength()+"speeches");

            // Loop for Nodes
            for(int i = 0; i < speechNodes.getLength(); i++){
                Node node = speechNodes.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element speechElement = (Element) node;

                    Speech speech = new Speech();
                    speech.setId(speechElement.getAttribute("id"));

                    // Set session metadaten
                    speech.setDate(datum);
                    speech.setSessionNumber(sessionNumber);
                    speech.setLegislativePeriod(legislaturPeriode);

                    // speaker ID
                    // !!!!!!!! speech oder reden
                    NodeList memberNodes = speechElement.getElementsByTagName("speech");
                    if (memberNodes.getLength() > 0) {
                        Element memberElement = (Element) memberNodes.item(0);
                        speech.setMemberId(memberElement.getAttribute("id"));
                    }

                    // speech text
                    StringBuilder speechText = new StringBuilder();
                    NodeList pNodes = speechElement.getElementsByTagName("p");
                    for(int j = 0; j < pNodes.getLength(); j++){
                        speechText.append(pNodes.item(j).getTextContent()).append("\n");
                    }
                    speech.setText(speechText.toString().trim());

                    // comments
                    NodeList commentsNodes = speechElement.getElementsByTagName("comment");
                    for(int k=0; k < commentsNodes.getLength(); k++){
                        Element commentElement = (Element) commentsNodes.item(k);
                        Comment comment = new Comment();
                        comment.setText(commentElement.getTextContent());
                        speech.getComment().add(comment);
                    }

                    speechList.add(speech);

                }

                System.out.println(" => " + speechList.size() + "speeches parsed");

            }

        } catch (Exception e) {
            System.out.println("Error parsing protokoll");
            e.printStackTrace();
        }
        return speechList;
    }

    // Function to get Value out of Document
    private String getTagValueFromDocument(Document doc, String tagName){
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0  && nodeList.item(0).getChildNodes().getLength() > 0) {

            return nodeList.item(0).getChildNodes().item(0).getNodeValue();

        }

        return "";
    }

}
