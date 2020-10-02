
/**
 * Test class for  for the class project in COP5556 Programming Language Principles 
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

import org.junit.jupiter.api.Test;

import cop5556fa20.SimpleParser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;
import static cop5556fa20.Scanner.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Beverly Sanders
 *
 */
@SuppressWarnings("preview") //text blocks are preview features in Java 14

class SimpleParserTest {
	
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	//creates and returns a parser for the given input.
	private SimpleParser makeSimpleParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Tokens
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}
	
	/*
	 * Parses the given input and expects a normal return from parse.
	 */
	void pass(String input) throws SyntaxException, LexicalException {
		SimpleParser p = makeSimpleParser(input);
		p.parse();
		assertTrue(p.consumedAll());
	}
	
	/*
	 * Parses the given input using the fragment of the grammar
	 * that uses Expression as the start symbol.  This is for convenience in
	 * testing--it allows expressions to be written and tested directly without
	 * having to be part of a statement.
	 * 
	 * This should test that all the tokens have been consumed except the EOF token.
	 */
	void passExpression(String input) throws LexicalException, SyntaxException {
		SimpleParser p = makeSimpleParser(input);
		p.expression();
		assertTrue(p.consumedAll());
		
	}
	
	/**
	 * Use this when a lexical error is expected with the given input
	 * 
	 * @param input
	 */
	void failLexical(String input)  {
		Exception exception = assertThrows(LexicalException.class, () -> {
		 makeSimpleParser(input);
	 });
	show(exception);
	}
	
	/**
	 * Use this when a syntax error is expected for the given input.
	 * 
	 * Kind is the kind of token the manifests an error.  (Not the expected token
	 * kind in a correct program, but the erroneous token)
	 * 
	 * @param input
	 * @param kind
	 */
	void fail(String input, Scanner.Kind kind)  {
		Exception exception = assertThrows(SyntaxException.class, () -> {
		 SimpleParser parser = makeSimpleParser(input);
		 parser.parse();
	 });
	show(exception);
	assertEquals(kind, ((SyntaxException) exception).token().kind());
	}
	
	/**
	 * Use this when a syntax error is expected for the given input.  The
	 * token kind that manifests the error is not given or checked.
	 * 
	 * @param input
	 */
	void fail(String input)  {
		Exception exception = assertThrows(SyntaxException.class, () -> {
		 SimpleParser parser = makeSimpleParser(input);
		 parser.parse();
	 });
	show(exception);
	}
	
	/**
	 * Use when a syntax error is expected in a standalone expression.
	 * 
	 * @param input
	 */
	void failExpression(String input)  {
		Exception exception = assertThrows(SyntaxException.class, () -> {
		 SimpleParser parser = makeSimpleParser(input);
		 parser.expression();
	 });
	show(exception);
	}

	/**
	 * Use when a syntax error is expected in a standalone expression.
	 *
	 * @param input
	 * @param kind
	 */
	void failExpression(String input, Scanner.Kind kind)  {
		Exception exception = assertThrows(SyntaxException.class, () -> {
			SimpleParser parser = makeSimpleParser(input);
			parser.expression();
		});
		show(exception);
		assertEquals(kind, ((SyntaxException) exception).token().kind());
	}



	@Test
	public void testEmpty() throws Scanner.LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  This is legal
		pass(input);
	}	
	
	

	@Test
	public void testDec0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc;
				string bcd;
				""";		
		pass(input);
	}
	
	
	/* Extra Ident at end.  The parser treats this as the left hand side of a 
	 * statement and finds EOF instead of ASSIGN, LARROW or RARROW.  
	 */
	@Test
	public void testDec0fail0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc;
				string bcd;
				x
				""";	
		fail(input,EOF);
	}
	
	/*
	 * Missing ; at end of first line.  Error manifests itself on "string".
	 */
	@Test
	public void testDec0fail1() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc
				string bcd;
				""";
		fail(input, KW_string);
	}
	
	
	@Test
	public void testImageIn0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				abc <- "https://this.is.a.url";
				""";
		pass(input);
	}
	

	
	@Test
	public void testBinary0() throws LexicalException, SyntaxException {
		String input = """
				a+b
				""";
		passExpression(input);
	}
	
	
	/*
	 * This input fails because these two adjacent operators are not legal.
	 */
	@Test
	public void testBinary0fail() throws LexicalException, SyntaxException {
		String input = """
				a + *b
				""";
		failExpression(input);
	}
	
	/*
	 * This input is OK.  The plus is part of a unary expression.
	 */
	@Test
	public void testBinary1() throws LexicalException, SyntaxException {
		String input = """
				a * + b
				""";
		passExpression(input);
	}

	/* ---------------------Customized Testcases---------------------------*/

	/* ----------Testcases for Expressions----------*/

	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		System.out.println("input1: ");
		System.out.println("Expecting: failExpression, because Expression cannot be empty.");
		String input = """

				""";
		failExpression(input, EOF);

		System.out.println("\ninput2: ");
		System.out.println("Expecting: LexicalException, because Expression can only be consist by legal tokens.");
		String input2 = """
				(a * + b) ^_^
				""";
		failLexical(input2);
	}

	@Test
	public void testOrExpression() throws LexicalException, SyntaxException {
		String input1 = """
				(a * + b) | (a * + b) | (a * + b)
				""";
		passExpression(input1);

		String input2 = """
				(a * + b) || (a * + b)
				""";
		failExpression(input2, OR);
	}

	@Test
	public void testAndExpression() throws LexicalException, SyntaxException {
		String input = """
				(a * + b) & (a * + b) & (a * + b)
				""";
		passExpression(input);

		String input2 = """
				(a * + b) && (a * + b)
				""";
		failExpression(input2, AND);
	}

	@Test
	public void testEqExpression() throws LexicalException, SyntaxException {
		String input = """
				(a * + b) == (a * + b) != (a * + b)
				""";
		passExpression(input);
	}

	@Test
	public void testRelExpression() throws LexicalException, SyntaxException {
		String input = """
				(a * + b) >= (a * + b) <= (a * + b) > (a * + b) < (a * + b)
				""";
		passExpression(input);
	}

	@Test
	public void testAddExpression() throws LexicalException, SyntaxException {
		String input = """
				(a * + b) + (a * + b) - (a * + b)
				""";
		passExpression(input);
	}

	@Test
	public void testMultAddExpression() throws LexicalException, SyntaxException {
		String input = """
				(a * + b) * (a * + b) / (a * + b) % (a * + b)
				""";
		passExpression(input);
	}

	@Test
	public void testUnaryExpression() throws LexicalException, SyntaxException {
		String input1 = """
				+(a * + b) * -(a * + b) / (a * + b) % +(a * + b)
				""";
		passExpression(input1);

		String input2 = """
				+(a * + b) * -(a * + b) / (a * + b) % *(a * + b)
				""";
		failExpression(input2, STAR);
	}

	@Test
	public void testUnaryExpressionNotPlusMinus() throws LexicalException, SyntaxException {
		String input1 = """
				!(a * + b)
				""";
		passExpression(input1);
	}

	@Test
	public void testHashExpressionNotPlusMinus() throws LexicalException, SyntaxException {
		String input1 = """
				(a * + b) # width # height # red # green # blue
				""";
		passExpression(input1);

		String input2 = """
				(a * + b) # width # height # red # green # test
				""";
		failExpression(input2, IDENT);
	}

	@Test
	public void testPrimary() throws LexicalException, SyntaxException {
		System.out.println("input1: ");
		System.out.println("Expecting: passExpression");
		String input1 = """
				100 + abc - (!"def") * X / Y % BLACK & <<(a * + b), (a * + b), (a * + b)>> | 
				X[(a * + b), (a * + b)] % @ (a * + b)
				""";
		passExpression(input1);

		System.out.println("\ninput2: ");
		System.out.println("Expecting: failExpression, because Prime cannot start by PixelSelector, a.k.a, LSQUARE.");
		String input2 = """
				[(a * + b), (a * + b)]
				""";
		failExpression(input2, LSQUARE);

		System.out.println("\ninput3: ");
		System.out.println("Expecting: failExpression, because Prime cannot be (KW_SCREEN).");
		String input3 = """
				(screen)
				""";
		failExpression(input3, KW_SCREEN);

		System.out.println("\ninput4: ");
		System.out.println("Expecting: failExpression, because expression cannot be empty.");
		String input4 = """
				()
				""";
		failExpression(input4, RPAREN);
	}

	@Test
	public void testPixelSelector() throws LexicalException, SyntaxException {
		System.out.println("input1: ");
		System.out.println("Expecting: passExpression");
		String input1 = """
				X[(a * + b), (a * + b)]
				""";
		passExpression(input1);

		System.out.println("\ninput2: ");
		System.out.println("Expecting: failExpression, because missing token: LSQUARE.");
		String input2 = """
				X[(a * + b), (a * + b)
				""";
		failExpression(input2, EOF);
	}

	/* ----------Testcases for Programs----------*/
	@Test
	public void testProgram() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: LexicalException, because program can only be consist by legal tokens.");
		String input = """
				t = (a * + b) ^_^
				""";
		failLexical(input);
	}

	@Test
	public void testVariableDeclaration() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: fail, because expecting Expression after ASSIGN but get SEMI");
		String input1 = """
				int abc;
				string bcd = ;
				""";
		fail(input1,SEMI);

		show("input2: ");
		show("Expecting: fail, because expecting Expression after ASSIGN but get EOF");
		String input2 = """
				int abc;
				string bcd = 
				""";
		fail(input2,EOF);

		show("input3: ");
		show("Expecting: pass");
		String input3 = """
				int abc;
				string bcd = (a * + b);
				""";
		pass(input3);

		show("input4: ");
		show("Expecting: fail, because expecting IDEN after KW_int/KW_string but get ASSIGN");
		String input4 = """
				int abc;
				string = 
				""";
		fail(input4, ASSIGN);
	}

	@Test
	public void testImageDeclaration() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: pass");
		String input1 = """
				image [(a * + b), (a * + b)] identifier <- (a * + b);
				""";
		pass(input1);

		show("input2: ");
		show("Expecting: pass");
		String input2 = """
				image identifier <- (a * + b);
				""";
		pass(input2);

		show("input3: ");
		show("Expecting: pass");
		String input3 = """
				image identifier;
				""";
		pass(input3);

		show("input4: ");
		show("Expecting: pass");
		String input4 = """
				image [(a * + b), (a * + b)] identifier;
				""";
		pass(input4);
	}

	@Test
	public void testAssignmentStatement() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: pass");
		String input1 = """
				identifier =  (a * + b);
				""";
		pass(input1);
	}

	@Test
	public void testLoopStatement() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: pass");
		String input1 = """
				identifier =* [X, Y] : (a * + b) : (a * + b);
				""";
		pass(input1);

		show("input2: ");
		show("Expecting: pass");
		String input2 = """
				identifier =* [X, Y] :: (a * + b);
				""";
		pass(input2);
	}

	@Test
	public void testImageInStatement() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: pass");
		String input1 = """
				identifier <- (a * + b);
				""";
		pass(input1);
	}

	@Test
	public void testImageOutStatement() throws LexicalException, SyntaxException {
		show("input1: ");
		show("Expecting: pass");
		String input1 = """
				identifier -> (a * + b);
				""";
		pass(input1);

		show("input2: ");
		show("Expecting: pass");
		String input2 = """
				identifier -> screen [(a * + b), (a * + b)];
				""";
		pass(input2);

		show("input3: ");
		show("Expecting: pass");
		String input3 = """
				identifier -> screen;
				""";
		pass(input3);
	}

}