package org.example.service;

import org.example.data.MemberOfParlament;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Path;
import java.io.File;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class parses the MDB_STAMMDATEN.XML file
 */
public class StammdatenParser {

    public List<MemberOfParlament> parseStammdatenXML(Path xmlFilePath){
        // This list will hold all the Member of Parliament objects, created from the XML
        List<MemberOfParlament> memberOfParlaments = new ArrayList<>();
        System.out.println("Starting Parsing of Stammdata from" + xmlFilePath.toString());

        try {
            File xmlFile = new File(xmlFilePath.toString());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // get all MDB nodes
            NodeList mdbNodes = doc.getElementsByTagName("MDB");

            // Loop through each <MDB> node found in the document
            for  (int i = 0; i < mdbNodes.getLength(); i++) {
                Node node = mdbNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element mdbElement = (Element) node;

                    // Create object
                    MemberOfParlament memberOfParlament = new MemberOfParlament();
                    // speech data from xml
                    memberOfParlament.setId(getTagValue("ID", mdbElement));

                    // Get the content of the <vorname> and <nachname> tags
                    memberOfParlament.setFirstname(getTagValue("VORNAME", mdbElement));
                    memberOfParlament.setLastname(getTagValue("NACHNAME", mdbElement));


                    // get party | EDITED
                    NodeList institutionNodes = mdbElement.getElementsByTagName("INSTITUTION");
                    // Variabel to save the last found party
                    String lastFraction = null;
                    // Loot through all found INSTITUTIONs in Data
                    for (int j = 0; j < institutionNodes.getLength(); j++) {
                        // INSTITUTION i as an element casting
                        Element inst = (Element) institutionNodes.item(j);
                        // Reading the value of INSART_LANG, could be a Fraction or a Group
                        String insArt = getTagValue("INSART_LANG", inst);
                        if ("Fraktion/Gruppe".equals(insArt)) {
                            // Extract Fraction or Group name from the INS_LANG
                            String fraktionName = getTagValue("INS_LANG", inst);
                            if (!fraktionName.isEmpty()) {
                                lastFraction = fraktionName;
                            }
                        }
                    }
                    // Set extracted Fraction or Group name to member of parlament
                    if(lastFraction != null){
                        memberOfParlament.setFraction(lastFraction);
                    }

                    // additional data
                    Map<String, String> metaData = new HashMap<>();
                    metaData.put("GEBURTSDATUM", getTagValue("GEBURTSDATUM", mdbElement));
                    metaData.put("BERUF", getTagValue("BERUF", mdbElement));
                    memberOfParlament.setMetaData(metaData);

                    memberOfParlaments.add(memberOfParlament);
                }
            }
            long withFraction = memberOfParlaments.stream().filter(a -> a.getFraction() != null).count();
            System.out.println("Found" + memberOfParlaments.size() + "Members of Parliament and" + withFraction + "are in Fraction");


        } catch (Exception e){
            System.out.println("Parsing of Stammdata has failed");
            e.printStackTrace();
        }
        return memberOfParlaments;
    }

    // get text from xml tag | EDITED
    private String getTagValue(String tagName, Element element){
        // Search for all child elements with name-tag in the element
        NodeList nodeList = element.getElementsByTagName(tagName);
        // Checks if at least one element with a name-tag exists
        if (nodeList.getLength() > 0) {
            // Takes first found element and read its text content
            String text = nodeList.item(0).getTextContent();
            // returns null if text content is empty
            return text != null ? text.trim() : "";
        }
        return "";
    }
}
