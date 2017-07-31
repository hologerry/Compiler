package bit.minisys.minicc;

import bit.minisys.minicc.codegen.IMiniCCCodeGen;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * gerry-minic-compiler
 * bit.minisys.minicc
 * Created by Gerry on 30/07/2017.
 * Copyright © 2017 Gerry. All rights reserved.
 */

public class GCodeGenerator implements IMiniCCCodeGen {
    private int labelNumber;
    private int registerTNumber;                // Register number $t0 ~ $t7

    private Map<String, Integer> registerTable; // variable associated register
    private Map<String, Integer> dataTable;     // data segment symbols

    private BufferedWriter bufferedWriter;
    private FileWriter fileWriter;

    private String dataSegmentString;
    private String codeSegmentString;

    // Document
    File fXMLFile;
    DocumentBuilderFactory documentBuilderFactory;
    DocumentBuilder documentBuilder;
    Document document;

    private void init(String oFile) {
        registerTNumber = 8;                // temporaries register from $8 ~ $15 ($t0 ~ $t7)
        dataSegmentString = "";
        codeSegmentString = "";

        registerTable = new HashMap<>();
        try {
            fileWriter = new FileWriter(oFile);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDataSegmentHeader() {
        // MIPS Data segment
        String dataSegment = "\t.data\n";

        try {
            bufferedWriter.write(dataSegment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTextSegmentHeader() {
        // MIPS Code segment
        String codeSegment = "\t.text\n";
        try {
            bufferedWriter.write(codeSegment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseXMLFile(String iXMLFile) {
        try {
            fXMLFile = new File(iXMLFile);
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(fXMLFile);
            NodeList funcNodeList = document.getElementsByTagName("Function");

            for(int i = 0; i < funcNodeList.getLength(); i++) {
                System.out.println("Functions");
                if (funcNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element functionElement = (Element) funcNodeList.item(i);
                    processFunction(functionElement);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAsmFile() {
        try {
            bufferedWriter.write(dataSegmentString);
            bufferedWriter.write(codeSegmentString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }


//    private void processArgList() {
//    }

    private void processFunction(Element functionElement) {
        NodeList childNodes = functionElement.getChildNodes();
        // get function name
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
                Element child = (Element) childNodes.item(i);
                if (child.getTagName().equals("identifier")) {
                    codeSegmentString += child.getTextContent()+":\n";
                }
            }
        }
        NodeList funcCodeBlock = functionElement.getElementsByTagName("CodeBlock");
        if (funcCodeBlock.item(0).getNodeType() == Node.ELEMENT_NODE) {
            processFuncCodeBlock((Element) funcCodeBlock.item(0));
        }

    }

    private void processFuncCodeBlock(Element codeBlock) {
        NodeList statementList = codeBlock.getElementsByTagName("Statement");
        for (int i = 0; i < statementList.getLength(); i++) {
            if (statementList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element statement = (Element) statementList.item(i);
                if (statement.getParentNode().getParentNode().getParentNode().getNodeName().equals("Function")) {
                    processFuncStatement(statement);
                }
            }
        }


    }

    private void processFuncStatement(Element statementElement) {
        NodeList children = statementElement.getChildNodes();
        Node firstChild = null;
        for (int i  = 0; i < children.getLength(); i++) {
            System.out.println("item"+i+": ");
            System.out.println(children.item(i).getNodeName() + ": " + children.item(i).getNodeType());
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                firstChild = children.item(i);
                break;
            }
        }
        if (firstChild != null && firstChild.getNodeName().equals("DeclareDefineStatement")) {
            Element declareDefineStmt = (Element) firstChild;
            NodeList identifiers = declareDefineStmt.getElementsByTagName("identifier");
            NodeList operators = declareDefineStmt.getElementsByTagName("operator");
            NodeList values = declareDefineStmt.getElementsByTagName("value");

            if (identifiers.getLength() == 1 && operators.getLength() == 0) {
                // declare statement int a;
                associateID2Reg(identifiers.item(0).getTextContent());
            }
            else if (identifiers.getLength() == 1 && operators.getLength() == 1 && values.getLength() == 1) {
                // declare and assign statement: int a = 10;
                String reg = associateID2Reg(identifiers.item(0).getTextContent());
                generateValue2Reg(reg, values.item(0).getTextContent());
            }

        }
        else if (firstChild != null && firstChild.getNodeName().equals("AssignStatement")) {
            Element assignStatement = (Element) firstChild;
            processAssignStatement(assignStatement);
        }

        else if (firstChild != null && firstChild.getNodeName().equals("BranchStatement")) {
            Element branchStatement = (Element) firstChild;
            Element compareExpression = (Element) branchStatement.
                    getElementsByTagName("CompareExpression").item(0);
            processCompareExpression(compareExpression);
            Element codeBlockStatement = (Element) statementElement.
                    getElementsByTagName("CodeBlock").item(0);
            NodeList assignStatementList = codeBlockStatement.
                    getElementsByTagName("AssignStatement");
            for (int i = 0; i < assignStatementList.getLength(); i++) {
                processAssignStatement((Element) assignStatementList.item(i));
            }

            // create label
            generateLabel();
        }
        else if (firstChild != null && firstChild.getNodeName().equals("LoopStatement")) {
            Element loopStatement = (Element) firstChild;
            Element compareExpression = (Element) loopStatement.
                    getElementsByTagName("CompareExpression").item(0);
            // compare expression label
            generateLabel();
            processCompareExpression(compareExpression);

            Element codeBlockStatement = (Element) statementElement.
                    getElementsByTagName("CodeBlock").item(0);
            NodeList assignStatementList = codeBlockStatement.
                    getElementsByTagName("AssignStatement");
            for (int i = 0; i < assignStatementList.getLength(); i++) {
                processAssignStatement((Element) assignStatementList.item(i));
            }

            generateJump();
            // exit loop label
            generateLabel();
        }
        else if (firstChild != null && firstChild.getNodeName().equals("ReturnStatement")) {
            generateEnd();
        }
    }

    private void processAssignStatement(Element assignStatement) {
        NodeList identifiers = assignStatement.getElementsByTagName("identifier");
        NodeList operators = assignStatement.getElementsByTagName("operator");
        NodeList values = assignStatement.getElementsByTagName("value");
        if (identifiers.getLength() == 1 && operators.getLength() == 1 && values.getLength() == 1) {
            // a = 10
            String reg = associateID2Reg(identifiers.item(0).getTextContent());
            generateValue2Reg(reg, values.item(0).getTextContent());
        }
        else if (identifiers.getLength() == 2 && operators.getLength() == 1 && values.getLength() == 0) {
            // a = b
            String regRT = associateID2Reg(identifiers.item(0).getTextContent());
            String regRS = associateID2Reg(identifiers.item(1).getTextContent());

            generateReg2Reg(regRT, regRS);
        }
        else if (identifiers.getLength() == 2 && operators.getLength() == 2 && values.getLength() == 1) {
            String regRT = associateID2Reg(identifiers.item(0).getTextContent());
            String regRS = associateID2Reg(identifiers.item(1).getTextContent());
            String operator = operators.item(1).getTextContent();
            String value = values.item(0).getTextContent();
            if (operator.equals("+")) {
                // a = b + 1
                generateADDI(regRT, regRS,value);
            }
            else if (operator.equals("-")) {
                // a = b - 1
                generateADDI(regRT, regRS,"-"+value);
            }
            else if (operator.equals("*")) {
                // a = b * 2
                generateMULRRI(regRT, regRS, value);
            } else if (operator.equals("/")) {
                generateDIVRRI(regRT, regRS, value);
            }
        } else if (identifiers.getLength() == 3 && operators.getLength() == 2) {
            String regRD = associateID2Reg(identifiers.item(0).getTextContent());
            String regRS = associateID2Reg(identifiers.item(1).getTextContent());
            String regRT = associateID2Reg(identifiers.item(2).getTextContent());
            String operator = operators.item(1).getTextContent();
            if (operator.equals("+")) {
                generateADD(regRD, regRS, regRT);
            } else if (operator.equals("-")) {
                generateSUB(regRD, regRS, regRT);
            } else if (operator.equals("*")) {
                generateMULRRR(regRD, regRS, regRT);
            } else if (operator.equals("/")) {
                generateDIVRRR(regRD, regRS, regRT);
            }

        }
    }

    private void processCompareExpression(Element compareExpression) {
        String comp1, comp2;
        ArrayList<String> comparerStrs = new ArrayList<>();
        NodeList comparers = compareExpression.getElementsByTagName("Comparer");

        for (int i = 0; i < comparers.getLength(); i++) {
            if (comparers.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element comparer = (Element) comparers.item(i);
                for (int j = 0; j < comparer.getChildNodes().getLength(); j++) {
                    Node cop = comparer.getChildNodes().item(j);
                    if (cop.getNodeType() == Node.ELEMENT_NODE) {
                        if (cop.getNodeName().equals("identifier")) {
                            comparerStrs.add(associateID2Reg(cop.getTextContent()));
                        } else if (cop.getNodeName().equals("value")) {
                            comparerStrs.add(cop.getTextContent());
                        }
                    }
                }
            }
        }
        String compareOperator = compareExpression.getElementsByTagName("operator").item(0).getTextContent();

        comp1 = comparerStrs.get(0);
        comp2 = comparerStrs.get(1);

        if (compareOperator.equals("==")) {
            generateBranch("bne", comp1, comp2);
        }
        else if (compareOperator.equals(">")) {
            generateBranch("ble", comp1, comp2);
        } else if (compareOperator.equals("<")) {
            generateBranch("bge", comp1, comp2);

        }
    }

    private void generateBranch(String operator, String comparer1, String comparer2) {
        String branchSmt = "\t"+operator+"\t"+comparer1+", "+comparer2+", "+"L"+labelNumber+"\n";
        codeSegmentString += branchSmt;
    }

    private void generateLabel() {
        System.out.println("createdLabel: "+labelNumber);
        String label = "L"+labelNumber+":\n";
        labelNumber++;
        codeSegmentString += label;
    }

    private void generateJump() {
        String jumpSmt = "\tj\t"+"L"+(labelNumber-1)+"\n";
        codeSegmentString += jumpSmt;
    }

    private void generateEnd() {
        String endSmt = "\n";
        codeSegmentString += endSmt;
    }

    private String associateID2Reg(String identifier) {
        String register;
        if (!registerTable.containsKey(identifier)) {
            registerTable.put(identifier,registerTNumber);
            register = "$t"+(registerTNumber-8);
            registerTNumber++;
        } else {
            register = "$t"+(registerTable.get(identifier)-8);
        }
        return register;
    }

    private void generateValue2Reg(String reg, String value) {
        String value2regSmt = "\tli\t"+reg+", "+value+"\n";
        codeSegmentString += value2regSmt;
    }

    private void generateReg2Reg(String regRT, String regRS) {
        String reg2regSmt = "\tmove\t"+regRT+", "+regRS+"\n";
        codeSegmentString += reg2regSmt;
    }

    private void generateADDI(String regRT, String regRS, String value) {
        String addiSmt = "\taddi\t"+regRT+", "+regRS+", "+value+"\n";
        codeSegmentString += addiSmt;
    }

    private void generateADD(String regRD, String regRS, String regRT) {
        String addSmt = "\tadd\t"+regRD+", "+regRS+", "+regRT+"\n";
        codeSegmentString += addSmt;
    }

    private void generateSUB(String regRD, String regRS, String regRT) {
        String subSmt = "\tsub\t"+regRD+", "+regRS+", "+regRT+"\n";
        codeSegmentString += subSmt;
    }

    private void generateMULRRI(String regRT, String regRS, String immediate) {
        String mulSmt = "\tmul\t"+regRT+", "+regRS+", "+immediate+"\n";
        codeSegmentString += mulSmt;
    }

    private void generateMULRRR(String regRD, String regRS, String regRT) {
        String mulSmt = "\tmul\t"+regRD+", "+regRS+", "+regRT+"\n";
        codeSegmentString += mulSmt;
    }

    private void generateDIVRRI(String regRT, String regRS, String immediate) {
        String divSmt = "\tdiv\t"+regRT+", "+regRS+", "+immediate+"\n";
        codeSegmentString += divSmt;
    }

    private void generateDIVRRR(String regRD, String regRS, String regRT) {
        String divSmt = "\tdiv\t"+regRD+", "+regRS+", "+regRT+"\n";
        codeSegmentString += divSmt;
    }

    public void run(String iFile, String oFile) throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nMIPS code generating ....");

        init(oFile);

        generateDataSegmentHeader();
        generateTextSegmentHeader();

        parseXMLFile(iFile);
        saveAsmFile();

        System.out.println("Finished code generating\n");
    }
}
