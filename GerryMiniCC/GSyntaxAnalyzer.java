package bit.minisys.minicc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

import static bit.minisys.minicc.GTokenInfo.*;
import static javafx.application.Platform.exit;


/**
 * gerry-minic-compiler
 * Created by Gerry on 14/06/2017.
 * Copyright © 2017 Gerry. All rights reserved.
 */

public class GSyntaxAnalyzer {
    // word info lab
    private static List<GWordInfo> wordLab;
    private static int currentWordLabIndex;    // used for get current word
    GWordInfo currentWord;

    // xml
    DocumentBuilderFactory docFactory;
    DocumentBuilder docBuilder;
    Document doc;

    private GWordInfo getNextWord() {
        currentWordLabIndex++;
        if (currentWordLabIndex >= wordLab.size()) {
            return null;
        }
        return wordLab.get(currentWordLabIndex);
    }

    public void startSyntaxAnalysis(List<GWordInfo> wordLab, String oXMLFile) {
        this.wordLab = wordLab;
        currentWordLabIndex = -1;

        String[] stringsPath = oXMLFile.split("/");
        String stringProject = stringsPath[stringsPath.length-1];

        String[] strings = stringProject.split("\\."); // or "[.]"
        String projectName = strings[0];

        System.out.println("Project name: "+projectName);

        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();


            // root
            doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("project");
            rootElement.setAttribute("name", projectName);

            doc.appendChild(rootElement);

            Element subRootElement = doc.createElement("ParseTree");
            subRootElement.setAttribute("name", oXMLFile);

            // PARSING ......
            Element cmplUnitElement = cmplUnit();

            System.out.println("Finished parsing!");

            subRootElement.appendChild(cmplUnitElement);

            rootElement.appendChild(subRootElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(oXMLFile));

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, result);



            System.out.println("File saved to '" + oXMLFile + "'");

        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    private Element cmplUnit() {
        Element cmplUnitElement = doc.createElement("CompilationUnit");
        Element funcListElement = funcList();
        cmplUnitElement.appendChild(funcListElement);
        return cmplUnitElement;
    }

    private Element funcList() {
        Element funcListElement = doc.createElement("Functions");
        Element funcDefElement = funcDef();
        while (funcDefElement != null) {
            funcListElement.appendChild(funcDefElement);

            funcDefElement = funcDef();
        }
        if (funcListElement.hasChildNodes()) {
            return funcListElement;
        } else {
            return null;
        }
    }

    private Element funcDef() {
        Element funcDefElement = doc.createElement("Function");
        Element typeSpecElement = typeSpec();
        Element identifierElement;
        Element leftPaElement;
        Element argListElement;
        Element rightPaElement;
        Element codeBlockElement;

        if (typeSpecElement != null) {
            funcDefElement.appendChild(typeSpecElement);

            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == IDENTIFIER_TYPE) {
                identifierElement = doc.createElement("identifier");
                identifierElement.appendChild(doc.createTextNode(currentWord.value));

                funcDefElement.appendChild(identifierElement);

                currentWord = getNextWord();
                if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals("(")) {
                    leftPaElement = doc.createElement("separator");
                    leftPaElement.appendChild(doc.createTextNode(currentWord.value));
                    funcDefElement.appendChild(leftPaElement);
                } else {
                    System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
                    exit();
                }

                argListElement = argList();
                if (argListElement != null) {
                    funcDefElement.appendChild(argListElement);
                }

                currentWord = getNextWord();
                if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(")")) {
                    rightPaElement = doc.createElement("separator");
                    rightPaElement.appendChild(doc.createTextNode(currentWord.value));
                    funcDefElement.appendChild(rightPaElement);
                } else {
                    System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
                    exit();
                }
                codeBlockElement = codeBlock();
                if (codeBlockElement != null) {
                    funcDefElement.appendChild(codeBlockElement);
                } else {
                    System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                    exit();
                }

                return funcDefElement;
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }
        }
        return null;
    }

    private Element typeSpec() {
        Element typeSpecElement = doc.createElement("Type");
        Element typeElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == KEYWORD_TYPE) {
            if (currentWord.value.equals("int") || currentWord.value.equals("float") || currentWord.value.equals("void")) {
                typeElement = doc.createElement("keyword");
                typeElement.appendChild(doc.createTextNode(currentWord.value));
                typeSpecElement.appendChild(typeElement);

                return typeSpecElement;
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }
        } else {
            currentWordLabIndex--;
            return null;
        }
        return null;
    }

    private Element argList() {
        Element argListElement = doc.createElement("Arguments");
        Element argumentElement = argument();
        while (argumentElement != null) {
            argListElement.appendChild(argumentElement);
            currentWord = getNextWord();
            if (currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(",")) {
                Element separatorElement = doc.createElement("separator");
                separatorElement.appendChild(doc.createTextNode(currentWord.value));
                argumentElement = argument();
                if (argListElement != null) {
                    argListElement.appendChild(separatorElement);
                    argListElement.appendChild(argumentElement);
                } else {
                    System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
                    exit();
                }
            } else {
                // back to last word
                currentWordLabIndex--;
                break;
            }
            argumentElement = argument();
        }

        if (argListElement.hasChildNodes()) {
            return argListElement;
        } else {
            return null;
        }
    }

    private Element argument() {
        Element argumentElement = doc.createElement("Argument");
        Element typeSpecElement = typeSpec();
        if (typeSpecElement != null) {
            currentWord = getNextWord();
            if (currentWord.type == IDENTIFIER_TYPE) {
                Element identifierElement = doc.createElement("identifier");
                identifierElement.appendChild(doc.createTextNode(currentWord.value));
                argumentElement.appendChild(typeSpecElement);
                argumentElement.appendChild(identifierElement);
                return argumentElement;
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }
        }
        return null;
    }

    private Element codeBlock() {
        Element codeBlockElement = doc.createElement("CodeBlock");
        Element leftBigPaElement;
        Element stmtListElement;
        Element rightBigPaElement;
        Element stmtElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals("{")) {
            leftBigPaElement = doc.createElement("separator");
            leftBigPaElement.appendChild(doc.createTextNode(currentWord.value));
            codeBlockElement.appendChild(leftBigPaElement);

            stmtListElement = stmtList();
            if (stmtListElement != null) {
                codeBlockElement.appendChild(stmtListElement);
            }

            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals("}")) {
                rightBigPaElement = doc.createElement("separator");
                rightBigPaElement.appendChild(doc.createTextNode(currentWord.value));
                codeBlockElement.appendChild(rightBigPaElement);
            }

        } else {
            currentWordLabIndex--;
            stmtElement = statement();
            if (stmtElement != null) {
                codeBlockElement.appendChild(stmtElement);
            }
        }

        if (codeBlockElement.hasChildNodes()) {
            return codeBlockElement;
        }

        return null;
    }

    private Element stmtList() {
        Element stmtListElement = doc.createElement("Statements");
        Element stmtElement = statement();
        while (stmtElement != null) {
            stmtListElement.appendChild(stmtElement);
            stmtElement = statement();
        }
        if (stmtListElement.hasChildNodes()) {
            return stmtListElement;
        } else {
            return null;
        }
    }

    private Element statement() {
        Element stmtElement = doc.createElement("Statement");
        currentWord = getNextWord();
        currentWordLabIndex--;
        if (currentWord != null) {
            switch (currentWord.type) {
                case SEPARATOR_TYPE: {
                    if (currentWord.value.equals(";")) {
                        currentWordLabIndex++;
                        Element separator = doc.createElement("separator");
                        separator.appendChild(doc.createTextNode(currentWord.value));
                        stmtElement.appendChild(separator);
                        return stmtElement;
                    } else if (currentWord.value.equals("}")) {
                        // at the end of code block
                        return null;
                    }
                    break;
                }
                case KEYWORD_TYPE: {
                    switch (currentWord.value) {
                        case "return":
                            Element returnStmtElement = returnStmt();
                            if (returnStmtElement != null) {
                                stmtElement.appendChild(returnStmtElement);
                                return stmtElement;
                            }
                            break;
                        case "if":case "else":
                            Element branchStmtElement = branchStmt();
                            if (branchStmtElement != null) {
                                stmtElement.appendChild(branchStmtElement);
                                return stmtElement;
                            }
                            break;
                        case "while":
                            Element loopStmtElement = loopStmt();
                            if (loopStmtElement != null) {
                                stmtElement.appendChild(loopStmtElement);
                                return stmtElement;
                            }
                            break;
                        case "void":case "int":case "float":
                            Element decdefStmtElement = decdefStmt();
                            if (decdefStmtElement != null) {
                                stmtElement.appendChild(decdefStmtElement);
                                return stmtElement;
                            }
                    }
                    break;
                }
                case IDENTIFIER_TYPE: {
                    Element assignStmtElement = assignStmt();
                    if (assignStmtElement != null) {
                        stmtElement.appendChild(assignStmtElement);
                        return stmtElement;
                    }
                    break;
                }

            }
        }
        return null;
    }

    private Element assignStmt() {
        Element assignStmtElement = doc.createElement("AssignStatement");
        Element identifierElement;
        Element equalsignElement;
        Element exprElement;
        Element separatorElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == IDENTIFIER_TYPE) {
            identifierElement = doc.createElement("identifier");
            identifierElement.appendChild(doc.createTextNode(currentWord.value));
            assignStmtElement.appendChild(identifierElement);

            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == OPERATOR_TYPE && currentWord.value.equals("=")) {
                equalsignElement = doc.createElement("separator");
                equalsignElement.appendChild(doc.createTextNode(currentWord.value));
                assignStmtElement.appendChild(equalsignElement);
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }

            exprElement = expr();
            if (exprElement != null) {
                assignStmtElement.appendChild(exprElement);
            }

            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(";")) {
                separatorElement = doc.createElement("separator");
                separatorElement.appendChild(doc.createTextNode(currentWord.value));
                assignStmtElement.appendChild(separatorElement);
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }
        return assignStmtElement;
    }

    private Element decdefStmt() {
        Element decdefStmtElement = doc.createElement("DeclareDefineStatement");
        Element typeSpecElement = typeSpec();
        Element identifierElement = null;
        Element separatorElement = null;
        Element assignStmtElement;
        if (typeSpecElement != null) {
            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == IDENTIFIER_TYPE) {
                identifierElement = doc.createElement("identifier");
                identifierElement.appendChild(doc.createTextNode(currentWord.value));

                currentWord = getNextWord();
                if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(";")) {
                    separatorElement = doc.createElement("separator");
                    separatorElement.appendChild(doc.createTextNode(currentWord.value));
                    decdefStmtElement.appendChild(typeSpecElement);
                    decdefStmtElement.appendChild(identifierElement);
                    decdefStmtElement.appendChild(separatorElement);
                } else {
                    currentWordLabIndex -= 2;
                    assignStmtElement = assignStmt();
                    if (assignStmtElement != null) {
                        decdefStmtElement.appendChild(typeSpecElement);
                        decdefStmtElement.appendChild(assignStmtElement);
                    }
                }
            }

            return decdefStmtElement;
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }
        return null;
    }

    private Element returnStmt() {
        Element returnStmtElement = doc.createElement("ReturnStatement");
        Element keywordElement;
        Element exprElement;
        Element separatorElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == KEYWORD_TYPE && currentWord.value.equals("return")) {
            keywordElement = doc.createElement("keyword");
            keywordElement.appendChild(doc.createTextNode(currentWord.value));
            returnStmtElement.appendChild(keywordElement);

            exprElement = expr();
            if (exprElement != null) {
                returnStmtElement.appendChild(exprElement);
            }

            currentWord = getNextWord();
            if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(";")) {
                separatorElement = doc.createElement("separator");
                separatorElement.appendChild(doc.createTextNode(currentWord.value));
                returnStmtElement.appendChild(separatorElement);
            } else {
                System.out.println("Got ERROR when parse token #"+(currentWordLabIndex+1));
                exit();
            }
            return returnStmtElement;
        } else {
            return null;
        }
    }

    private Element branchStmt() {
        Element branchStmtElement = doc.createElement("Branch");
        Element ifKeywordElement;
        Element cmpCodeElement;
        Element elseKeywordElement;
        Element codeblockElement;

        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == KEYWORD_TYPE) {
            if (currentWord.value.equals("if")) {
                ifKeywordElement = doc.createElement("keyword");
                ifKeywordElement.appendChild(doc.createTextNode(currentWord.value));
                branchStmtElement.appendChild(ifKeywordElement);

                cmpCodeElement = cmpCode();
                if (cmpCodeElement != null) {
                    //
                    // TODO: For SYNTAX should get all nodes and write to parent branchStmtElement
                    // cmpCodeElement.getChildNodes();
                    branchStmtElement.appendChild(cmpCodeElement);
                }

                return branchStmtElement;
            } else if (currentWord.value.equals("else")) {
                elseKeywordElement = doc.createElement("keyword");
                elseKeywordElement.appendChild(doc.createTextNode(currentWord.value));
                branchStmtElement.appendChild(elseKeywordElement);

                codeblockElement = codeBlock();
                if (codeblockElement != null) {
                    branchStmtElement.appendChild(codeblockElement);
                }
                return branchStmtElement;
            } else {
                return null;
            }
        }
        return null;
    }

    private Element loopStmt() {
        Element loopStmtElement = doc.createElement("LoopStatement");
        Element whlKeyElement;
        Element cmpCodeElement;
        currentWord = getNextWord();
        if (currentWord.type == KEYWORD_TYPE && currentWord.value.equals("while")) {
            whlKeyElement = doc.createElement("keyword");
            whlKeyElement.appendChild(doc.createTextNode(currentWord.value));
            loopStmtElement.appendChild(whlKeyElement);

            cmpCodeElement = cmpCode();
            if (cmpCodeElement != null) {
                loopStmtElement.appendChild(cmpCodeElement);
            }
            return loopStmtElement;
        }
        return null;
    }

    private Element cmpCode() {
        Element cmpCodeElement = doc.createElement("CompareCodeBlock");
        Element leftPaElement;
        Element compareEprElement;
        Element rightPaElement;
        Element codeBlockElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals("(")) {
            leftPaElement = doc.createElement("separator");
            leftPaElement.appendChild(doc.createTextNode(currentWord.value));
            cmpCodeElement.appendChild(leftPaElement);
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }

        compareEprElement = compareExpr();
        if (compareEprElement != null) {
            cmpCodeElement.appendChild(compareEprElement);
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }

        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(")")) {
            rightPaElement = doc.createElement("separator");
            rightPaElement.appendChild(doc.createTextNode(currentWord.value));
            cmpCodeElement.appendChild(rightPaElement);
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }

        codeBlockElement = codeBlock();
        if (codeBlockElement != null) {
            cmpCodeElement.appendChild(codeBlockElement);
        } else {
            System.out.println("Got ERROR when parse token #" + currentWordLabIndex);
            exit();
        }
        return codeBlockElement;
    }

    private Element compareExpr() {
        Element compareExprElement = doc.createElement("CompareExpression");
        Element leftComparerElement;
        Element rightComparerElement;
        Element separatorElement;

        leftComparerElement = comparer();
        if (leftComparerElement != null) {
            compareExprElement.appendChild(leftComparerElement);
        }

        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == OPERATOR_TYPE) {
            if (currentWord.value.equals("==") || currentWord.value.equals("<") || currentWord.value.equals(">")) {
                separatorElement = doc.createElement("operator");
                separatorElement.appendChild(doc.createTextNode(currentWord.value));
                compareExprElement.appendChild(separatorElement);
            }
        } else {
            System.out.println("Got ERROR when parse token #"+currentWordLabIndex);
            exit();
        }

        rightComparerElement = comparer();
        if (rightComparerElement != null) {
            compareExprElement.appendChild(rightComparerElement);
        }

        return compareExprElement;
    }

    private Element comparer() {
        Element comparerElement = doc.createElement("Comparer");
        currentWord = getNextWord();
        if (currentWord != null) {
            switch (currentWord.type) {
                case IDENTIFIER_TYPE: {
                    Element identifierElement = doc.createElement("identifier");
                    identifierElement.appendChild(doc.createTextNode(currentWord.value));
                    comparerElement.appendChild(identifierElement);
                    break;
                }
                case VALUE_TYPE: {
                    Element valueElement = doc.createElement("value");
                    valueElement.appendChild(doc.createTextNode(currentWord.value));
                    comparerElement.appendChild(valueElement);
                    break;
                }
                default: {
                    System.out.println("Got ERROR when parse token #" + currentWordLabIndex);
                    exit();
                }
            }
            return comparerElement;
        }
        else {
            return null;
        }
    }

    private Element expr() {
        Element exprElement = doc.createElement("Expression");
        Element termElement;
        Element expr2Element;

        termElement = term();
        if (termElement != null) {
            exprElement.appendChild(termElement);
        }

        expr2Element = expr2();
        if (expr2Element != null) {
            exprElement.appendChild(expr2Element);
        }

        if (exprElement.getChildNodes().getLength() >= 1) {
            return exprElement;
        }
        return null;
    }

    private Element expr2() {
        Element expr2Element = doc.createElement("Expr2");
        Element rExpr2Element; // recursive element;
        Element plusMinusElement;
        Element termElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == OPERATOR_TYPE) {
            if (currentWord.value.equals("+") || currentWord.value.equals("-")) {
                plusMinusElement = doc.createElement("operator");
                plusMinusElement.appendChild(doc.createTextNode(currentWord.value));
                expr2Element.appendChild(plusMinusElement);
            }

            termElement = term();
            if (termElement != null) {
                expr2Element.appendChild(termElement);
            }

            rExpr2Element = expr2();
            while (rExpr2Element != null ) {
                expr2Element.appendChild(rExpr2Element);
                rExpr2Element = expr2();
            }

        } else {
            currentWordLabIndex--;
            return null;
        }

        if (expr2Element.hasChildNodes()) {
            return expr2Element;
        } else {
            return null;
        }

    }

    private Element term() {
        Element termElement = doc.createElement("Term");
        Element factorElement;
        Element term2Element;
        factorElement = factor();
        if (factorElement != null) {
            termElement.appendChild(factorElement);
        }

        term2Element = term2();
        if (term2Element != null) {
            termElement.appendChild(term2Element);
        }

        if (termElement.getChildNodes().getLength() >= 1) {
            return termElement;
        }
        return null;
    }

    private Element term2() {
        Element term2Element = doc.createElement("Term2");
        Element rTerm2Element;
        Element mltDivElement;
        Element factorElement;
        currentWord = getNextWord();
        if (currentWord != null && currentWord.type == OPERATOR_TYPE) {
            if (currentWord.value.equals("*") || currentWord.value.equals("/")) {
                mltDivElement = doc.createElement("operator");
                mltDivElement.appendChild(doc.createTextNode(currentWord.value));
                term2Element.appendChild(mltDivElement);
            }

            factorElement = factor();
            if (factorElement != null) {
                term2Element.appendChild(factorElement);
            }

            rTerm2Element = term2();
            while(rTerm2Element != null) {
                term2Element.appendChild(rTerm2Element);
                rTerm2Element = term2();
            }
        } else {
            currentWordLabIndex--;
            return null;
        }
        if (term2Element.hasChildNodes()) {
            return term2Element;
        } else {
            return null;
        }
    }

    private Element factor() {
        Element factorElement = doc.createElement("Factor");
        Element identifierElement;
        Element valueElement;
        currentWord = getNextWord();
        if (currentWord != null) {
            if (currentWord.type == IDENTIFIER_TYPE) {
                identifierElement = doc.createElement("identifier");
                identifierElement.appendChild(doc.createTextNode(currentWord.value));
                factorElement.appendChild(identifierElement);
            } else if (currentWord.type == VALUE_TYPE) {
                valueElement = doc.createElement("value");
                valueElement.appendChild(doc.createTextNode(currentWord.value));
                factorElement.appendChild(valueElement);
            } else if (currentWord.type == SEPARATOR_TYPE && currentWord.value.equals("(")) {
                Element leftPaElement = doc.createElement("separator");
                leftPaElement.appendChild(doc.createTextNode(currentWord.value));

                Element exprElement = expr();

                Element rightPaElement = null;
                currentWord = getNextWord();
                if (currentWord.type == SEPARATOR_TYPE && currentWord.value.equals(")")) {
                    rightPaElement = doc.createElement("separator");
                    rightPaElement.appendChild(doc);
                } else {
                    System.out.println("Got ERROR when parse token #" + currentWordLabIndex);
                    exit();
                }

                if (leftPaElement != null && exprElement != null && rightPaElement != null) {
                    factorElement.appendChild(leftPaElement);
                    factorElement.appendChild(exprElement);
                    factorElement.appendChild(rightPaElement);
                } else {
                    System.out.println("Got ERROR when parse token #" + currentWordLabIndex);
                    exit();
                }
            }

            if (factorElement.hasChildNodes()) {
                return factorElement;
            }
            return null;
        } else {
            return null;
        }
    }


}
