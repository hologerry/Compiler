package bit.minisys.minicc;

/**
 * gerry-minic-compiler
 * Created by Gerry on 03/05/2017.
 * Copyright © 2016 Gerry. All rights reserved.
 */

public class GTokenInfo {
    // token type
    public static int NULL_TYPE = (0x0000);
    public static int OPERATOR_TYPE = (0x0001 << 0);
    public static int STRING_TYPE = (0x0001 << 1);
    public static int VALUE_TYPE = (0x0001 << 2);
    public static int CHAR_TYPE = (0x0001 << 3);
    public static int IDENTIFIER_TYPE = (0x0001 << 4);
    public static int KEYWORD_TYPE = (0x0001 << 5);
    public static int SEPARATOR_TYPE = (0x0001 << 6);
    public static int ERROR_TYPE = -1;

    public int number;          // token's number
    public int line;            // token's line
    public String value;        // token's value
    public int type;            // token's type
    public boolean isValid;     // if token is valid
}
