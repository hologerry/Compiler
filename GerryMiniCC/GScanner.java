package bit.minisys.minicc;

import bit.minisys.minicc.scanner.IMiniCCScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * gerry-minic-compiler
 * Created by Gerry on 03/05/2017.
 * Copyright © 2016 Gerry. All rights reserved.
 */

import static bit.minisys.minicc.GTokenInfo.*;


@SuppressWarnings("Duplicates")
public class GScanner implements IMiniCCScanner {

    private static int currentLineNum = 0;
    private static int currentTokenNum = 0;
    private static List<GTokenInfo> tokenLab;

    private String sCurrentLine;
    private GLexicalAnalyzer lexicalAnalyzer;

    public static void addTokenToLab(GTokenInfo tokenInfo) {
        currentTokenNum++;
        tokenInfo.line = currentLineNum;
        tokenInfo.number = currentTokenNum;
        if (tokenInfo.isValid) {
            System.out.println("\nToken Number: "+tokenInfo.number);
            System.out.println("Token Line: "+tokenInfo.line);
            System.out.println("Token Value: "+tokenInfo.value);
            System.out.println("Token Type: "+typeIntToString(tokenInfo.type));

            tokenLab.add(tokenInfo);
        }
    }

    public static void printErrorPosition(int errorColumnNum) {
        System.out.println("Error at Line: "+currentLineNum+"ColumnNum"+errorColumnNum);
        return;
    }

    public static String typeIntToString(int typeInt) {
        String typeString = "";
        if (typeInt == OPERATOR_TYPE) {
            typeString = "operator";
        } else if (typeInt == STRING_TYPE) {
            typeString = "string";
        } else if (typeInt == VALUE_TYPE) {
            typeString = "value";
        } else if (typeInt == CHAR_TYPE) {
            typeString = "char";
        } else if (typeInt == IDENTIFIER_TYPE) {
            typeString = "identifier";
        } else if (typeInt == KEYWORD_TYPE) {
            typeString = "keyword";
        } else if (typeInt == SEPARATOR_TYPE) {
            typeString = "separator";
        }
        return typeString;
    }

    public static void saveToXML(String oXMLFile) {
        String[] strsPath = oXMLFile.split("/");
        String strsProject = strsPath[strsPath.length-1];

        String[] strs = strsProject.split("\\."); // or "[.]"
        String projectName = strs[0];
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


            // root
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("project");
            rootElement.setAttribute("name", projectName);

            doc.appendChild(rootElement);

            Element subRootElement = doc.createElement("tokens");
            rootElement.appendChild(subRootElement);

            for (int i = 0; i < tokenLab.size(); i++) {
                Element tokenElement = doc.createElement("token");
                // number
                Element numberElement = doc.createElement("number");
                numberElement.appendChild(doc.createTextNode(String.valueOf(tokenLab.get(i).number)));
                tokenElement.appendChild(numberElement);

                // line
                Element lineElement = doc.createElement("line");
                lineElement.appendChild(doc.createTextNode(String.valueOf(tokenLab.get(i).line)));
                tokenElement.appendChild(lineElement);

                // value
                Element valueElement = doc.createElement("value");
                valueElement.appendChild(doc.createTextNode(tokenLab.get(i).value));
                tokenElement.appendChild(valueElement);

                // type
                Element typeElement = doc.createElement("type");
                typeElement.appendChild(doc.createTextNode(typeIntToString(tokenLab.get(i).type)));
                tokenElement.appendChild(typeElement);

                // valid
                Element validElement = doc.createElement("valid");
                validElement.appendChild(doc.createTextNode(String.valueOf(tokenLab.get(i).isValid)));
                tokenElement.appendChild(validElement);

                subRootElement.appendChild(tokenElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(oXMLFile));

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, result);

            System.out.println("File saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void parseFile(String iFile) throws Exception {
        BufferedReader bufferedReader;
        FileReader fileReader;

        try {
            fileReader = new FileReader(iFile);
            bufferedReader = new BufferedReader(fileReader);

            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                currentLineNum ++;
                sCurrentLine += '\n';
                lexicalAnalyzer = new GLexicalAnalyzer(sCurrentLine);
                lexicalAnalyzer.processLine();
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String iFile, String oFile) throws Exception {
        tokenLab = new ArrayList<>();

        parseFile(iFile);
        saveToXML(oFile);
    }
}
