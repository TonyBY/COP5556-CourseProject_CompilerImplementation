/**
 * Example JUnit tests for the Scanner in the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2020.  
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2020 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2020
 *
 */
package cop5556fa20;

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import org.junit.jupiter.api.Test;

import static cop5556fa20.Scanner.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("preview") //text blocks are preview features in Java 14

class ScannerTest {
	
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		Token expected = new Token(kind,pos,length,line,pos_in_line);
		assertEquals(expected, t);
		return t;
	}
	
	
	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind());
		assertFalse(scanner.hasTokens());
		return token;
	}
	
	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws Scanner.LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}	
	
	
	/**
	 * Test illustrating how to check content of tokens.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws Scanner.LexicalException {		
		
		String input = """
				;;
				;;
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Another example test, this time with an ident.  While simple tests like this are useful,
	 * many errors occur with sequences of tokens, so make sure that you have more complex test cases
	 * with multiple tokens and test the edge cases. 
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testIdent() throws LexicalException {
		String input = " \t\fij208";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 3, 5, 1, 4);
		assertEquals("ij208", scanner.getText(t0));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testSTRINGLIT() throws LexicalException {
		String input = """
				"123456789"X
				"123\r\n\f\'\\"
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, STRINGLIT, 0, 11, 1, 1);
		assertEquals("123456789", scanner.getText(t0));
		show(scanner.getText(t0));
		Token t1 = checkNext(scanner, KW_X, 11, 1, 1, 12);
		assertEquals("X", scanner.getText(t1));
		Token t2 = checkNext(scanner, STRINGLIT, 13, 10, 2, 1);
		assertEquals("123\r\n\f\'\\", scanner.getText(t2));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testCR() throws LexicalException {
		String input = """
				a\r9\n\rX\r\nZ
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 1, 1, 1);
		assertEquals("a", scanner.getText(t0));
		Token t1 = checkNext(scanner, INTLIT, 2, 1, 2, 1);
		assertEquals("9", scanner.getText(t1));
		Token t2 = checkNext(scanner, KW_X, 5, 1, 4, 1);
		assertEquals("X", scanner.getText(t2));
		Token t3 = checkNext(scanner, CONST, 8, 1, 5, 1);
		assertEquals("Z", scanner.getText(t3));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testCOMMENT() throws LexicalException {
		String input = """
				//abcd09102\r9//abc\nX//abc\r\nZ
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t1 = checkNext(scanner, INTLIT, 12, 1, 2, 1);
		assertEquals("9", scanner.getText(t1));
		Token t2 = checkNext(scanner, KW_X, 19, 1, 3, 1);
		assertEquals("X", scanner.getText(t2));
		Token t3 = checkNext(scanner, CONST, 27, 1, 4, 1);
		assertEquals("Z", scanner.getText(t3));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testKW() throws LexicalException {
		String input = """
				X Y
				width height
				screen
				screen_width
				screen_height
				image
				int
				string
				red
				green
				blue
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, KW_X, 0, 1, 1, 1);
		assertEquals("X", scanner.getText(t0));
		Token t1 = checkNext(scanner, KW_Y, 2, 1, 1, 3);
		assertEquals("Y", scanner.getText(t1));
		Token t2 = checkNext(scanner, KW_WIDTH, 4, 5, 2, 1);
		assertEquals("width", scanner.getText(t2));
		Token t3 = checkNext(scanner, KW_HEIGHT, 10, 6, 2, 7);
		assertEquals("height", scanner.getText(t3));
		Token t4 = checkNext(scanner, KW_SCREEN, 17, 6, 3, 1);
		assertEquals("screen", scanner.getText(t4));
		Token t5 = checkNext(scanner, KW_SCREEN_WIDTH, 24, 12, 4, 1);
		assertEquals("screen_width", scanner.getText(t5));
		Token t6 = checkNext(scanner, KW_SCREEN_HEIGHT, 37, 13, 5, 1);
		assertEquals("screen_height", scanner.getText(t6));
		Token t7 = checkNext(scanner, KW_image, 51, 5, 6, 1);
		assertEquals("image", scanner.getText(t7));
		Token t8 = checkNext(scanner, KW_int, 57, 3, 7, 1);
		assertEquals("int", scanner.getText(t8));
		Token t9 = checkNext(scanner, KW_string, 61, 6, 8, 1);
		assertEquals("string", scanner.getText(t9));
		Token t10 = checkNext(scanner, KW_RED, 68, 3, 9, 1);
		assertEquals("red", scanner.getText(t10));
		Token t11 = checkNext(scanner, KW_GREEN, 72, 5, 10, 1);
		assertEquals("green", scanner.getText(t11));
		Token t12 = checkNext(scanner, KW_BLUE, 78, 4, 11, 1);
		assertEquals("blue", scanner.getText(t12));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testCONST() throws LexicalException {
		String input = """
				ZWHITE
				Z WHITE
				SILVER
				GRAY
				BLACK
				RED
				MAROON
				YELLOW
				OLIVE
				LIME
				GREEN
				AQUA
				TEAL
				BLUE
				NAVY
				FUCHSIA
				PURPLE
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 6, 1, 1);
		assertEquals("ZWHITE", scanner.getText(t0));
		Token t1 = checkNext(scanner, CONST, 7, 1, 2, 1);
		assertEquals("Z", scanner.getText(t1));
		assertEquals(255, scanner.intVal(t1));
		show(scanner.intVal(t1));
		Token t2 = checkNext(scanner, CONST, 9, 5, 2, 3);
		assertEquals("WHITE", scanner.getText(t2));
		assertEquals(0xffffffff, scanner.intVal(t2));
		show(scanner.intVal(t2));
		Token t3 = checkNext(scanner, CONST, 15, 6, 3, 1);
		assertEquals("SILVER", scanner.getText(t3));
		assertEquals(0xffc0c0c0, scanner.intVal(t3));
		show(scanner.intVal(t3));
		Token t4 = checkNext(scanner, CONST, 22, 4, 4, 1);
		assertEquals("GRAY", scanner.getText(t4));
		Token t5 = checkNext(scanner, CONST, 27, 5, 5, 1);
		assertEquals("BLACK", scanner.getText(t5));
		Token t6 = checkNext(scanner, CONST, 33, 3, 6, 1);
		assertEquals("RED", scanner.getText(t6));
		Token t7 = checkNext(scanner, CONST, 37, 6, 7, 1);
		assertEquals("MAROON", scanner.getText(t7));
		Token t8 = checkNext(scanner, CONST, 44, 6, 8, 1);
		assertEquals("YELLOW", scanner.getText(t8));
		Token t9 = checkNext(scanner, CONST, 51, 5, 9, 1);
		assertEquals("OLIVE", scanner.getText(t9));
		Token t10 = checkNext(scanner, CONST, 57, 4, 10, 1);
		assertEquals("LIME", scanner.getText(t10));
		Token t11 = checkNext(scanner, CONST, 62, 5, 11, 1);
		assertEquals("GREEN", scanner.getText(t11));
		Token t12 = checkNext(scanner, CONST, 68, 4, 12, 1);
		assertEquals("AQUA", scanner.getText(t12));
		Token t13 = checkNext(scanner, CONST, 73, 4, 13, 1);
		assertEquals("TEAL", scanner.getText(t13));
		Token t14 = checkNext(scanner, CONST, 78, 4, 14, 1);
		assertEquals("BLUE", scanner.getText(t14));
		Token t15 = checkNext(scanner, CONST, 83, 4, 15, 1);
		assertEquals("NAVY", scanner.getText(t15));
		Token t16 = checkNext(scanner, CONST, 88, 7, 16, 1);
		assertEquals("FUCHSIA", scanner.getText(t16));
		Token t17 = checkNext(scanner, CONST, 96, 6, 17, 1);
		assertEquals("PURPLE", scanner.getText(t17));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testIntLit() throws LexicalException {
		String input = """
				01203ab9
				4ab921
				""";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, INTLIT, 0, 1, 1, 1);
		assertEquals("0", scanner.getText(t0));
		show(scanner.intVal(t0));
		assertEquals(0, scanner.intVal(t0));
		Token t1 = checkNext(scanner, INTLIT, 1, 4, 1, 2);
		assertEquals("1203", scanner.getText(t1));
		show(scanner.intVal(t1));
		assertEquals(1203, scanner.intVal(t1));
		Token t2 = checkNext(scanner, IDENT, 5, 3, 1, 6);
		assertEquals("ab9", scanner.getText(t2));
		Token t3 = checkNext(scanner, INTLIT, 9, 1, 2, 1);
		assertEquals("4", scanner.getText(t3));
		Token t4 = checkNext(scanner, IDENT, 10, 5, 2, 2);
		assertEquals("ab921", scanner.getText(t4));
		checkNextIsEOF(scanner);
	}

	@Test
	public void testSymbols() throws LexicalException {
		String input = """
       <<<
       >>>
       <=!
       --><-
       !====?:%@#&|
       """;
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, LPIXEL, 0, 2, 1, 1);
		assertEquals("<<", scanner.getText(t0));
		Token t1 = checkNext(scanner, LT, 2, 1, 1, 3);
		assertEquals("<", scanner.getText(t1));
		Token t2 = checkNext(scanner, RPIXEL, 4, 2, 2, 1);
		assertEquals(">>", scanner.getText(t2));
		Token t3 = checkNext(scanner, GT, 6, 1, 2, 3);
		assertEquals(">", scanner.getText(t3));
		Token t4 = checkNext(scanner, LE, 8, 2, 3, 1);
		assertEquals("<=", scanner.getText(t4));
		Token t5 = checkNext(scanner, EXCL, 10, 1, 3, 3);
		assertEquals("!", scanner.getText(t5));
		Token t6 = checkNext(scanner, MINUS, 12, 1, 4, 1);
		assertEquals("-", scanner.getText(t6));
		Token t7 = checkNext(scanner, RARROW, 13, 2, 4, 2);
		assertEquals("->", scanner.getText(t7));
		Token t8 = checkNext(scanner, LARROW, 15, 2, 4, 4);
		assertEquals("<-", scanner.getText(t8));
		Token t9 = checkNext(scanner, NEQ, 18, 2, 5, 1);
		assertEquals("!=", scanner.getText(t9));
		Token t10 = checkNext(scanner, EQ, 20, 2, 5, 3);
		assertEquals("==", scanner.getText(t10));
		Token t11 = checkNext(scanner, ASSIGN, 22, 1, 5, 5);
		assertEquals("=", scanner.getText(t11));
		Token t12 = checkNext(scanner, Q, 23, 1, 5, 6);
		assertEquals("?", scanner.getText(t12));
		Token t13 = checkNext(scanner, COLON, 24, 1, 5, 7);
		assertEquals(":", scanner.getText(t13));
		Token t14 = checkNext(scanner, MOD, 25, 1, 5, 8);
		assertEquals("%", scanner.getText(t14));
		Token t15 = checkNext(scanner, AT, 26, 1, 5, 9);
		assertEquals("@", scanner.getText(t15));
		Token t16 = checkNext(scanner, HASH, 27, 1, 5, 10);
		assertEquals("#", scanner.getText(t16));
		Token t17 = checkNext(scanner, AND, 28, 1, 5, 11);
		assertEquals("&", scanner.getText(t17));
		Token t18 = checkNext(scanner, OR, 29, 1, 5, 12);
		assertEquals("|", scanner.getText(t18));
		checkNextIsEOF(scanner);
	}
	
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, a String literal
	 * that is missing the closing ".  
	 * 
	 * In contrast to Java String literals, the text block feature simply passes the characters
	 * to the scanner as given, using a LF (\n) as newline character.  If we had instead used a 
	 * Java String literal, we would have had to escape the double quote and explicitly insert
	 * the LF at the end:  String input = "\"greetings\n";
	 * 
	 * assertThrows takes the class of the expected exception and a lambda with the test code in the body.
	 * The test passes if the expected exception is thrown.  The Exception object is returned and
	 * an be printed.  It should contain an appropriate error message. 
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = """
				"greetings
				""";
		show(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
}
