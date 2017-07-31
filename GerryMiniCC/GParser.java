package bit.minisys.minicc;

import bit.minisys.minicc.parser.IMiniCCParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static bit.minisys.minicc.GTokenInfo.*;


/**
 * gerry-minic-compiler
 * Created by Gerry on 14/06/2017.
 * Copyright © 2017 Gerry. All rights reserved.
 */

public class GParser implements IMiniCCParser {
    private List<GWordInfo> wordLab;
    private GSyntaxAnalyzer syntaxAnalyzer;

    public void addWordToLab(GWordInfo wordInfo) {
        System.out.println("Word type: "+wordInfo.type);
        System.out.println("Word value: "+wordInfo.value);
        System.out.println();

        wordLab.add(wordInfo);

    }

    private static int parseTypeStringToInt(String typeString) {
        int typeInt = 0;
        if (typeString.equals("operator")) {
            typeInt = OPERATOR_TYPE;
        }
        else if (typeString.equals("string")) {
            typeInt = STRING_TYPE;
        }
        else if (typeString.equals("value")) {
            typeInt = VALUE_TYPE;
        }
        else if (typeString.equals("char")) {
            typeInt = CHAR_TYPE;
        }
        else if (typeString.equals("identifier")) {
            typeInt = IDENTIFIER_TYPE;
        }
        else if (typeString.equals("keyword")) {
            typeInt = KEYWORD_TYPE;
        }
        else if (typeString.equals("separator")) {
            typeInt = SEPARATOR_TYPE;
        }
        return typeInt;
    }
    // parse xml file
    private void readXMLFile(String iXMLFile) {
        try {
            File fXMLFile = new File(iXMLFile);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(fXMLFile);

            //  http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            document.getDocumentElement().normalize();

            System.out.println("Root element: " + document.getDocumentElement().getNodeName());

            NodeList nodeList = document.getElementsByTagName("token");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int tokenNumber = Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());
                    System.out.println("Word #"+tokenNumber);
                    String isValidStr = element.getElementsByTagName("valid").item(0).getTextContent();
                    boolean isValid = Boolean.parseBoolean(isValidStr);
                    String typeStr = element.getElementsByTagName("type").item(0).getTextContent();
                    int type = parseTypeStringToInt(typeStr);
                    String value = element.getElementsByTagName("value").item(0).getTextContent();
                    if (isValid) {
                        GWordInfo wordInfo = new GWordInfo(type, value);
                        addWordToLab(wordInfo);
                    }
                }
            }


        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    public void run(String iFile, String oFile) {

        System.out.println("\nParsing .......");

        syntaxAnalyzer = new GSyntaxAnalyzer();
        wordLab = new ArrayList<>();

        // read xml file
        readXMLFile(iFile);

        syntaxAnalyzer.startSyntaxAnalysis(wordLab, oFile);

    }
}
