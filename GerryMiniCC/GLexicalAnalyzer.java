package bit.minisys.minicc;

import static bit.minisys.minicc.GScanner.printErrorPosition;
import static bit.minisys.minicc.GScanner.addTokenToLab;

/**
 * gerry-minic-compiler
 * Created by Gerry on 03/05/2017.
 * Copyright © 2016 Gerry. All rights reserved.
 */

@SuppressWarnings("Duplicates")
public class GLexicalAnalyzer {
    private String currentProcessingLine;
    private int lineIndex = 0;
    private int state = 0;

    GLexicalAnalyzer(String sCurrentLine) {
        currentProcessingLine = sCurrentLine;
    }

    char getNextChar() {
        char nextChar = 0;
        if (lineIndex < currentProcessingLine.length()-1) {
            lineIndex++;
            nextChar = currentProcessingLine.charAt(lineIndex);
        }
        return nextChar;
    }

    char backToLastChar() {
        char lastChar = 0;
        lineIndex--;
        if (lineIndex > 0) {
            lastChar = currentProcessingLine.charAt(lineIndex);
        }
        return lastChar;
    }

    boolean processLine() {
        String sCurrentLine = currentProcessingLine;
        for (lineIndex = 0; lineIndex < sCurrentLine.length(); lineIndex++) {
            char ch = sCurrentLine.charAt(lineIndex);
            GTokenInfo result;

            switch (state) {
                case 0:
                    switch (ch) {
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                            state = 1; backToLastChar(); break;

                        case '"':
                            state = 2; backToLastChar(); break;

                        case '0':case '1':case '2':case '3':case '4':case '5':
                        case '6':case '7':case '8':case '9':case '.':
                            state = 3; backToLastChar(); break;

                        case '\'':
                            state = 4; backToLastChar(); break;

                        case '_':case '$':
                        case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':
                        case 'i':case 'j':case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':
                        case 'q':case 'r':case 's':case 't':case 'u':case 'v':case 'w':case 'x':
                        case 'y':case 'z':
                        case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':
                        case 'I':case 'J':case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':
                        case 'Q':case 'R':case 'S':case 'T':case 'U':case 'V':case 'W':case 'X':
                        case 'Y':case 'Z':
                            state = 5; backToLastChar(); break;

                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':
                            state = 6; backToLastChar(); break;

                        case ' ':case '\t':case '\r':case '\n':case'\0':
                            state = 7; backToLastChar(); break;

                        default:
                            state = 8; backToLastChar(); break;
                    }
                    break;
                case 1:
                    result = operatorScanner(ch);               // Scan operator
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 2:
                    result = stringScanner(ch);			        // Scan string
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 3:
                    result = valueScanner(ch);			        // Scan value
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 4:
                    result = charScanner(ch);			        // Scan char
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 5:
                    result = identifierScanner(ch);		        // Scan identifier
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 6:
                    result = separatorScanner(ch);		        // Scan separator
                    addTokenToLab(result);
                    if (result.type == GTokenInfo.ERROR_TYPE)	// Error
                    {
                        printErrorPosition(lineIndex);
                    }
                    state = 0;
                    break;
                case 7:
                    blankScanner(ch);			                // Scan blank
                    state = 0;
                    break;
                case 8:
                    specificSymbolScanner(ch);	                // Scan other symbols
                    System.out.println("It Occurs at :");
                    printErrorPosition(lineIndex);
                    state = 0;
                    break;
                default:
                {
                    System.out.println("ProcessLine: switch error"+state);
                    return false;
                }
            }
        }
        return true;
    }

    GTokenInfo operatorScanner(char ch) {
        GTokenInfo result = new GTokenInfo();
        GTokenInfo temp;

        int state = 0;

        result.type = 0x00;
        result.value = "";
        result.isValid = false;

        while (true) {
            switch (state) {
            case 0:
                switch (ch)
                {
                case '+':
                    state = 1; break;
                case '-':
                    state = 2; break;
                case '*':
                    state = 3; break;
                case '/':
                    state = 4; break;
                case '%':
                    state = 5; break;
                case '!':
                    state = 6; break;
                case '=':
                    state = 7; break;
                case '^':
                    state = 8; break;
                case '?':
                case ':':
                    state = 9; break;
                case '~':
                    state = 10; break;
                case '<':
                    state = 11; break;
                case '>':
                    state = 12; break;
                case '&':
                    state = 13; break;
                case '|':
                    state = 14; break;
                default:
                    System.out.println("operatorScanner::switch Error. "+state);
                    return result;
                }
                break;

            case 1:  // state 1: '+' set
                switch (ch)
                {
                    // state 51: '+float/integer'
                    case '0':case '1':case '2':case '3':case '4':case '5':
                    case '6':case '7':case '8':case '9':case '.':
                    {
                        // enter value scanner
                        temp = valueScanner(ch);

                        // value scanner return error token -> print it
                        if (temp.type == GTokenInfo.ERROR_TYPE)
                        {
                            System.out.println("operatorScanner::Read (+)value Error"+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                        // read +.  -->  get back to last char. return '+' token
                        else if (temp.type == GTokenInfo.OPERATOR_TYPE)
                        {
                            backToLastChar();

                            result.value = "+";
                            result.type = GTokenInfo.OPERATOR_TYPE;
                            result.isValid = true;

                            return result;
                        }
                        // get valid value  -->  return float or integer
                        else
                        {
                            result.value = "+" + temp.value;
                            result.type = temp.type;
                            result.isValid = temp.isValid;

                            return result;
                        }
                    }

                    // state 16: '+='
                    case '=':
                    {
                        result.value = "+=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 47: '++'
                    case '+':
                    {
                        result.value = "++";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    //state 42: '+'
                    default:
                    {
                        backToLastChar();

                        result.value = "+";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                }

            case 2:  // state 2: '-' set
                switch (ch)
                {
                    // state 51: '-float/integer'
                    case '0':case '1':case '2':case '3':case '4':case '5':
                    case '6':case '7':case '8':case '9':case '.':
                    {
                        // enter value scanner
                        temp = valueScanner(ch);

                        // value scanner return invalid value   -->  print it
                        if (temp.type == GTokenInfo.ERROR_TYPE)
                        {
                            System.out.println("operatorScanner::Read (+)value Error"+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                        // read -.   -->  get back to last char. return '-' token
                        else if (temp.type == GTokenInfo.OPERATOR_TYPE)
                        {
                            backToLastChar();

                            result.value = "-";
                            result.type = GTokenInfo.OPERATOR_TYPE;
                            result.isValid = true;

                            return result;
                        }
                        // get valid value  -->  return float or integer
                        else
                        {
                            result.value = "-" + temp.value;
                            result.type = temp.type;
                            result.isValid = temp.isValid;

                            return result;
                        }
                    }

                    // state 17:	'-='
                    case '=':
                    {
                        result.value = "-=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 48: '--'
                    case '-':
                    {
                        result.value = "--";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 43: '-'
                    default:
                    {
                        backToLastChar();

                        result.value = "-";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 3:  // state 3: '*' set
                switch (ch)
                {
                    // state 18: '*='
                    case '=':
                    {
                        result.value = "*=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 44: '*'
                    default:
                    {
                        backToLastChar();

                        result.value = "*";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 4:  //state 4: '/' set
                switch (ch)
                {
                    // state 19: '/='
                    case '=':
                    {
                        result.value = "/=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 45: '/'
                    default:
                    {
                        backToLastChar();

                        result.value = "/";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 5:  // state 5: '%'set
                switch (ch)
                {
                    // state 20: '%='
                    case '=':
                    {
                        result.value = "%=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 45: '%'
                    default:
                    {
                        backToLastChar();

                        result.value = "%";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                }
            case 6:  // state 6: '!'set
                switch (ch)
                {
                    // state 34: '!='
                    case '=':
                    {
                        result.value = "!=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 45: '!'
                    default:
                    {
                        backToLastChar();

                        result.value = "!";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                }
            case 7:  // state 7: '='set
                switch (ch)
                {
                    // state 33: '=='
                    case '=':
                    {
                        result.value = "==";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 15: '='
                    default:
                    {
                        backToLastChar();

                        result.value = "=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }
            case 8:  // state 8: '^' set
                switch (ch)
                {
                    // state 22:	'^='
                    case '=':
                    {
                        result.value = "^=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 31: '^'
                    default:
                    {
                        backToLastChar();

                        result.value = "^";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }
            case 9:  //state 9: '?:' set
                switch (ch)
                {
                    // state 27: '?:'
                    default:
                    {
                        char lastChar = backToLastChar();
                        result.value = ""+lastChar;	//获取？或：
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 10: // state 10: '~' set
                switch (ch)
                {
                    // state 50: '~'
                    default:
                    {
                        backToLastChar();

                        result.value = "~";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 11:  //state 11: '<' set
                switch (ch)
                {
                    // state 37:	'<='
                    case '=':
                    {
                        result.value = "<=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 52: '<<' set
                    case '<':
                    {
                        ch = getNextChar();
                        switch (ch)
                        {
                            // state 25: '<<='
                            case '=':
                            {
                                result.value = "<<=";
                                result.type = GTokenInfo.OPERATOR_TYPE;
                                result.isValid = true;

                                return result;
                            }

                            // state 39: '<<'
                            default:
                            {
                                backToLastChar();

                                result.value = "<<";
                                result.type = GTokenInfo.OPERATOR_TYPE;
                                result.isValid = true;

                                return result;
                            }

                        }
                    }

                    // state 35: '<'
                    default:
                    {
                        backToLastChar();

                        result.value = "<";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }
            case 12:  // state 12: '>' set
                switch (ch)
                {
                    // state 38:	'>='
                    case '=':
                    {
                        result.value = ">=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 53: '>>'set
                    case '>':
                    {
                        ch = getNextChar();
                        switch (ch)
                        {
                            // state 24:	'>>='
                            case '=':
                            {
                                result.value = ">>=";
                                result.type = GTokenInfo.OPERATOR_TYPE;
                                result.isValid = true;

                                return result;
                            }

                            // state 39: '>>>'set
                            case '>':
                            {
                                ch = getNextChar();
                                switch (ch)
                                {
                                    // state 26: '>>>='
                                    case '=':
                                    {
                                        result.value = ">>>=";
                                        result.type = GTokenInfo.OPERATOR_TYPE;
                                        result.isValid = true;

                                        return result;
                                    }

                                    // state 41: '>>>'
                                    default:
                                    {
                                        backToLastChar();

                                        result.value = ">>>";
                                        result.type = GTokenInfo.OPERATOR_TYPE;
                                        result.isValid = true;

                                        return result;
                                    }
                                }
                            }

                            // state 40: '>>'
                            default:
                            {
                                backToLastChar();

                                result.value = ">>";
                                result.type = GTokenInfo.OPERATOR_TYPE;
                                result.isValid = true;

                                return result;
                            }

                        }
                    }

                    // state 36: '>'
                    default:
                    {
                        backToLastChar();

                        result.value = ">";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }
            case 13:  // state 13: '&'set
                switch (ch)
                {
                    // state 21:	'&='
                    case '=':
                    {
                        result.value = "&=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 29: '&&'
                    case '&':
                    {
                        result.value = "&&";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 32: '&'
                    default:
                    {
                        backToLastChar();

                        result.value = "&";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }

            case 14: // state 14: '|'set
                switch (ch)
                {
                    // state 23:	'|='
                    case '=':
                    {
                        result.value = "|=";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 28: '||'
                    case '|':
                    {
                        result.value = "||";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }

                    // state 30: '|'
                    default:
                    {
                        backToLastChar();

                        result.value = "|";
                        result.type = GTokenInfo.OPERATOR_TYPE;
                        result.isValid = true;

                        return result;
                    }
                }
                default:
                {
                    System.out.println("operatorScanner::switch Error. "+state);
                    return result;
                }
            }
            ch = getNextChar();
        }
    }

    GTokenInfo stringScanner(char ch) {
        GTokenInfo result = new GTokenInfo();

        int state = 0;

        result.type = 0x00;
        result.value = "";
        result.isValid = true;

        while (true)
        {
            switch (state)
            {
                case 0:
                    switch (ch)
                    {
                        case '"':
                            state = 1; result.value += ch; break;
                        default:
                            System.out.println("stringScanner::switch Error."+state);
                            return result;
                    }
                    break;
                case 1:
                    switch (ch)
                    {
                        case '\\':
                            state = 2; result.value += ch; break;
                        case '"':
                            state = 3; result.value += ch; break;
                        default:
                            state = 1; result.value += ch; break;
                    }
                    break;
                case 2:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 5; result.value += ch; break;
                        case '\"':case '\'':case '\\':case 'r':
                        case 'n':case 'f':case 't':case 'b':
                        state = 13; result.value += ch; break;
                        case 'u':
                            state = 8; result.value += ch; break;
                        default:
                        {
                            System.out.println("stringScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 3: // chars need back to last
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                            state = 4; backToLastChar(); break;
                        default:
                        {
                            System.out.println("charScanner::Read not blank operator char after string Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                case 4:	// terminate
                {
//                    backToLastChar();

//                    result.value = "#";
                    result.type = GTokenInfo.STRING_TYPE;
                    result.isValid = true;

                    return result;
                }
                case 5:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 6; result.value += ch; break;
                        default:
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 6:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 7; result.value += ch; break;
                        default:
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 7: // back to last
                    switch (ch)
                    {
                        default:
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 8:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 9; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = true;

                            return result;
                        }
                    }
                    break;
                case 9:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 10; result.value += ch; break;
                        default:  // back to last char
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 10:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 11; result.value += ch; break;
                        default:  // back to last char
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 11:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 12; result.value += ch; break;
                        default:  // back to last char
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 12:  // back to last char
                    switch (ch)
                    {
                        default:
                            state = 1; backToLastChar(); break;
                    }
                    break;
                case 13:  // back to last char
                    switch (ch)
                    {
                        default:
                            state = 1; backToLastChar(); break;
                    }
                    break;
                default:
                {
                    System.out.println("stringScanner::switch Error."+state);
                    return result;
                }
            }
            ch = getNextChar();
        }

    }

    GTokenInfo valueScanner(char ch) {
        GTokenInfo result = new GTokenInfo();

        int state = 0;

        result.type = 0x00;
        result.value = "";
        result.isValid = false;

        while (true)
        {
            switch (state)
            {
                case 0:
                    switch (ch)
                    {
                        case '0':
                            state = 4; result.value += ch; break;
                        case '1':case '2':case '3':case '4':case '5':
                        case '6':case '7':case '8':case '9':
                            state = 1; result.value += ch; break;
                        case '.':
                            state = 14; result.value += ch; break;
                        default:
                            System.out.println("valueScanner::switch Error."+state);
                            return result;
                    }
                    break;
                case 1:
                    switch (ch)
                    {
                        case 'E':	case 'e':
                            state = 12; result.value += ch; break;
                        case '.':
                            state = 10; result.value += ch; break;
                        case 'F':case 'f':case 'D':case 'd':
                            state = 11; result.value += ch; break;
                        case 'L':case 'l':
                            state = 2; result.value += ch; break;
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':case '8':case '9':
                            state = 1; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                            state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 2:  // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 3: // terminate
                {
                    backToLastChar();  // back to last char

                    result.type = GTokenInfo.VALUE_TYPE;
                    result.isValid= true;

                    return result;
                }

                case 4:
                    switch (ch)
                    {
                        case 'E':case 'e':
                        state = 12; result.value += ch; break;
                        case '.':
                            state = 10; result.value += ch; break;
                        case 'F':case 'f':case 'D':case 'd':
                        state = 11; result.value += ch; break;
                        case 'L':case 'l':
                        state = 5; result.value += ch; break;
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':
                        state = 6; result.value += ch; break;
                        case 'X':case 'x':
                        state = 8; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 5:  // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 6:
                    switch (ch)
                    {
                        case 'L':case 'l':
                        state = 7; result.value += ch; break;
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':
                        state = 6; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 7: // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                            state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value= "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 8:
                    switch (ch)
                    {
                        case 'L':case 'l':
                            state = 9; result.value += ch; break;
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                            state = 8; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                            state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 9: // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 3; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 10: // terminate
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':case '8':case '9':
                            state = 10; result.value += ch; break;
                        case 'E':case 'e':
                            state = 12; result.value += ch; break;
                        case 'F':case 'f':case 'D':case 'd':
                            state = 11; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':// back to last char
                        {
                            backToLastChar();

                            result.type = GTokenInfo.VALUE_TYPE;
                            result.isValid = true;

                            return result;
                        }
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 11: // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                            state = 10; backToLastChar(); break;
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 12:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':case '8':case '9':
                        state = 13; result.value += ch; break;
                        default:
                        {
                            System.out.println("valueScanner::Read number Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 13: // terminate
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':case '8':case '9':
                        state = 13; result.value += ch; break;
                        case 'F':case 'f':case 'D':case 'd':
                        state = 11; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        {
                            backToLastChar();	// back to last char

                            result.type = GTokenInfo.VALUE_TYPE;
                            result.isValid = true;

                            return result;
                        }
                        default:
                        {
                            System.out.println("valueScanner::Read not blank separator operator char after digital Error. "+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;

                case 14:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':
                        case '5':case '6':case '7':case '8':case '9':
                        state = 10; result.value += ch; break;
                        default: // back to last char
                            state = 15; backToLastChar(); break;
                    }
                    break;
                case 15: // terminate
                {
                    backToLastChar();	// back to last char

                    result.value = ".";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                default:
                {
                    System.out.println("valueScanner::switch Error."+state);
                    return result;
                }
            }

            ch = getNextChar();
        }
    }

    GTokenInfo charScanner(char ch) {
        GTokenInfo result = new GTokenInfo();
        GTokenInfo temp = new GTokenInfo();

        int state = 0;

        result.type = 0x00;
        result.value = "";
        result.isValid = true;

        while (true)
        {
            switch (state)
            {
                case 0:
                    switch (ch)
                    {
                        case '\'':
                            state = 1; result.value += ch; break;
                        default:
                            System.out.println("charScanner::switch Error."+state);
                            return result;
                    }
                    break;
                case 1:
                    switch (ch)
                    {
                        case '\\':
                            state = 2; result.value += ch; break;
                        default:
                            state = 3; result.value += ch; break;
                    }
                    break;
                case 2:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 5; result.value += ch; break;
                        case '\"':case '\'':case '\\':case 'r':
                        case 'n':case 'f':case 't':case 'b':
                        state = 13; result.value += ch; break;
                        case 'u':
                            state = 8; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 3:
                    switch (ch)
                    {
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read not only one char in single quote Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 4:	 // back to last char
                    switch (ch)
                    {
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0':
                        state = 14; backToLastChar(); break;
                        default:
                        {
                            System.out.println("charScanner::Read not blank separator operator char after char Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 5:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 6; result.value += ch; break;
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 6:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                        state = 7; result.value += ch; break;
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 7:
                    switch (ch)
                    {
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 8:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 9; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 9:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 10; result.value += ch; break;
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 10:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 11; result.value += ch; break;
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 11:
                    switch (ch)
                    {
                        case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        case 'a':case 'b':case 'c':case 'd':case 'e':
                        case 'A':case 'B':case 'C':case 'D':case 'E':
                        state = 12; result.value += ch; break;
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 12:
                    switch (ch)
                    {
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 13:
                    switch (ch)
                    {
                        case '\'':
                            state = 4; result.value += ch; break;
                        default:
                        {
                            System.out.println("charScanner::Read '\\' Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 14: // terminate
                {
                    backToLastChar();	// back to last char

                    result.type = GTokenInfo.CHAR_TYPE;
                    result.isValid = false;

                    return result;
                }
                default:
                {
                    System.out.println("charScanner::switch Error."+state);
                    return result;
                }
            }
            ch = getNextChar();
        }
    }

    GTokenInfo identifierScanner(char ch) {
        GTokenInfo result = new GTokenInfo();
        GTokenInfo temp = new GTokenInfo();

        int state = 0;

        result.type = 0x00;
        result.value = "";
        result.isValid = false;

        while (true)
        {
            switch (state)
            {
                case 0:
                    switch (ch)
                    {
                        case '_':case '$':
                        case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':
                        case 'i':case 'j':case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':
                        case 'q':case 'r':case 's':case 't':case 'u':case 'v':case 'w':case 'x':
                        case 'y':case 'z':
                        case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':
                        case 'I':case 'J':case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':
                        case 'Q':case 'R':case 'S':case 'T':case 'U':case 'V':case 'W':case 'X':
                        case 'Y':case 'Z':
                        state = 1; result.value += ch; break;
                        default:
                            System.out.println("identifierScanner::switch Error."+state);
                            return result;
                    }
                    break;
                case 1:
                    switch (ch)
                    {
                        case '_':case '$':
                        case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':
                        case 'i':case 'j':case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':
                        case 'q':case 'r':case 's':case 't':case 'u':case 'v':case 'w':case 'x':
                        case 'y':case 'z':
                        case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':
                        case 'I':case 'J':case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':
                        case 'Q':case 'R':case 'S':case 'T':case 'U':case 'V':case 'W':case 'X':
                        case 'Y':case 'Z':
                        case '0':case '1':case '2':case '3':case '4':case '5':
                        case '6':case '7':case '8':case '9':
                        state = 1; result.value += ch; break;
                        // operator
                        case '+':case '&':case '=':case '~':case '|':case '^':case '?':case ':':
                        case '-':case '/':case '>':case '<':case '%':case '*':case '!':
                        // separator
                        case '{':case '}':case '(':case ')':case '[':case ']':case ',':case ';':case '.':
                        // blank
                        case ' ':case '\t':case '\r':case '\n':case '\0': // back to last char
                        state = 2; backToLastChar(); break;
                        default:
                        {
                            System.out.println("identifierScanner::Read not blank separator operator after identifier Error."+state);
                            result.value = "Error String";
                            result.type = GTokenInfo.ERROR_TYPE;
                            result.isValid = false;

                            return result;
                        }
                    }
                    break;
                case 2: // terminate
                {
                    backToLastChar();	// back to last char

                    switch (result.value)
                    {
                        // keyword
                        case "auto":case "break":case "case":case "char":case "const":case "continue":
                        case "default":case "do":case "double":case "else":case "enum":case "extern":
                        case "float":case "for":case "goto":case "if":case "inline":case "int":
                        case "long":case "register":case "restrict":case "return":case "short":case "signed":
                        case "sizeof":case "static":case "struct":case "switch":case "typedef":case "union":
                        case "unsigned":case "void":case "volatile":case "while":
                            result.type = GTokenInfo.KEYWORD_TYPE;
                            result.isValid = true;

                            return result;
                        // identifier
                        default:
                            result.type = GTokenInfo.IDENTIFIER_TYPE;
                            result.isValid = true;
                            return result;
                    }
                }
                default:
                {
                    System.out.println("identifierScanner::switch Error."+state);
                    return result;
                }
            }
            ch = getNextChar();
        }

    }

    GTokenInfo separatorScanner(char ch) {
        GTokenInfo result = new GTokenInfo();	// valid token
        int state = 0;	                        // state


        result.type = 0x00;
        result.value = "";
        result.isValid = false;

        while (true)
        {
            switch (ch)
            {
                case '{':
                {
                    result.value = "{";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case '}':
                {
                    result.value = "}";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case '(':
                {
                    result.value = "(";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case ')':
                {
                    result.value = ")";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case '[':
                {
                    result.value = "[";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case ']':
                {
                    result.value = "]";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case ',':
                {
                    result.value = ",";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                case ';':
                {
                    result.value = ";";
                    result.type = GTokenInfo.SEPARATOR_TYPE;
                    result.isValid = true;

                    return result;
                }
                default:
                {
                    System.out.println("separatorScanner::switch Error."+state);
                    return result;
                }
            }
        }
    }

    void blankScanner(char ch) {
        // Just ignore
        return;
    }

    boolean specificSymbolScanner(char ch) {
        System.out.println("Some specific symbol in code: "+ch);
        return false;
    }
}
