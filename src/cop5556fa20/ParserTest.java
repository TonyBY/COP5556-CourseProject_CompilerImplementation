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

import cop5556fa20.AST.*;
import cop5556fa20.Parser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static cop5556fa20.AST.ASTTestLambdas.*;
import static cop5556fa20.Scanner.Kind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Beverly Sanders
 *
 */
@SuppressWarnings("preview") // text blocks are preview features in Java 14

class ParserTest {

	// To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	// creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}

	@Test
	public void testEmpty() throws Scanner.LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. This is legal
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertEquals(Program.class, node.getClass()); // checks that the Parser returns a Program object
		assertEquals(0, ((Program) node).decOrStatement().size()); // checks that the decOrStatement list is empty
	}

	@Test
	public void testDec0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc;
				string bcd = "a";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertEquals(Program.class, node.getClass()); // checks that parser returns a Program object
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // get the decOrStatements list for
																			// convenience
		assertEquals(2, decOrStatement.size()); // check size of decOrStatements
		DecVar dec0 = (DecVar) decOrStatement.get(0); // get element from decOrStatements. If not a DecVar, the cast
														// will fail
		assertEquals(Type.Int, dec0.type()); // check the type of the variable whose declaration is represented by the
												// dec0 node
		assertEquals("abc", dec0.name()); // check the name of the variable whose declaration is represented by the dec0
											// node
		DecVar dec1 = (DecVar) decOrStatement.get(1); // get element from decOrStatements. If not a DecVar, the cast
														// will fail
		assertEquals(Type.String, dec1.type()); // check the type of the variable whose declaration is represented by
												// the dec1 node
		assertEquals("bcd", dec1.name()); // check the name of the variable whose declaration is represented by the dec1
											// node
		assertEquals("a", (((ExprStringLit) dec1.expression()).text())); //check that the expression is an ExprStringLit with value "a")
	}

	
	/**
	 * This example uses a lambda provided in ASTTestLambdas for testing
	 * convenience.
	 * 
	 */
	@Test
	public void testImageIn0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				abc <- "https://this.is.a.url";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // gets the decOrStatement list. The cast will
																			// fail if the node returned from parse is
																			// not a Program.
		ASTNode s0 = decOrStatement.get(0); // get the statement from decOrStatement.
		/**
		 * build up a test using the lambdas, and execute it by calling test. for this
		 * case, we expect s0 to be a StatementImageIn object, so use
		 * checkStatementImageIn. A StatementImageIn has a name, and an Expression. The
		 * first parameter is the name of the variable on the left hand side. The second
		 * is a Predicate<ASTNode> to check that the Expression is a ExprStringLit with
		 * the given value. The constructed Predicate is then executed by calling its
		 * test method and passing in the ASTNode under test.
		 */
		checkStatementImageIn("abc", checkExprStringLit("https://this.is.a.url")).test(s0);
	}

	/**
	 * Another example that uses lambdas for checking.  This one has a binary expression on the right side of a declaration.
	 * Here, lambdas are used to check each of the subexpressions.  
	 * 
	 * @throws Scanner.LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testBinary0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc = 4 + RED;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // gets the decOrStatement list. The cast will
																			// fail if the node returned from parse is
																			// not a Program.
		DecVar d0 = (DecVar) decOrStatement.get(0); // gets the declaration. The cast will fail if not a DecVar
		// use checkDecVar lambda, which takes type, name, and a lambda to check the
		// expression.
		Predicate<ASTNode> checkDecVarPredict = checkDecVar(Type.Int, // the type should be int
				"abc", // the variable name should be "abc"
				checkExprBinary( // the expression is a binary expression with operator PLUS
						checkExprIntLit(4), // the left expression is an ExprIntLit with value 4, use checkExprIntLit to
											// check
						checkExprConst("RED", Scanner.constants.get("RED")), // the right expression is an ExprConst.
																				// Use checkExprConst to check name
																				// "RED", and value from table in
																				// Scanner.
						PLUS));
		checkDecVarPredict.test(d0); // invoke the test method with the decVar to evaluate.
	}

	/**
	 * This example uses a lambda provided in ASTTestLambdas for testing
	 * convenience.
	 * 
	 */
	@Test
	public void testVarDec1() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc = 4;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); 
		DecVar d0 = (DecVar) decOrStatement.get(0);																	
		checkDecVar(Type.Int, "abc", checkExprIntLit(4)).test(d0);
	}

	/* ---------------------Customized Testcases---------------------------*/
	@Test
	public void testVarDec2() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc;
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "abc", checkExprEmpty()).test(d0);
	}

	@Test
	public void testImageDec() throws Scanner.LexicalException, SyntaxException {
		show("input1: ");
		String input1 = """
				image [(a * + b), (a * + b)] identifier <- (a * + b);
				""";
		Parser parser = makeParser(input1);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecImage d0 = (DecImage) decOrStatement.get(0);
		checkDecImage(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				LARROW,
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);

		show("\ninput2: ");
		String input2 = """
				image identifier <- (a * + b);
				""";
		Parser parser2 = makeParser(input2);
		Program node2 = parser2.parse();
		List<ASTNode> decOrStatement2 = node2.decOrStatement();
		DecImage d02 = (DecImage) decOrStatement2.get(0);
		checkDecImage(d02.name(),
				checkExprEmpty(),
				checkExprEmpty(),
				LARROW,
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d02);

		show("\ninput3: ");
		show("Expecting: pass");
		String input3 = """
				image identifier;
				""";
		Parser parser3 = makeParser(input3);
		Program node3 = parser3.parse();
		List<ASTNode> decOrStatement3 = node3.decOrStatement();
		DecImage d03 = (DecImage) decOrStatement3.get(0);
		checkDecImage(d03.name(),
				checkExprEmpty(),
				checkExprEmpty(),
				NOP,
				checkExprEmpty()).test(d03);

		show("\ninput4: ");
		show("Expecting: pass");
		String input4 = """
				image [(a * + b), (a * + b)] identifier;
				""";
		Parser parser4 = makeParser(input4);
		Program node4 = parser4.parse();
		List<ASTNode> decOrStatement4 = node4.decOrStatement();
		DecImage d04 = (DecImage) decOrStatement4.get(0);
		checkDecImage(d04.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				NOP,
				checkExprEmpty()).test(d04);
	}

	@Test
	public void testStatementAssign() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier =  (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementAssign d0 = (StatementAssign) decOrStatement.get(0);
		checkStatementAssignment(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);

	}

	@Test
	public void testStatementLoop1() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier =* [X, Y] : (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementLoop d0 = (StatementLoop) decOrStatement.get(0);
		checkStatementLoop(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);
	}

	@Test
	public void testStatementLoop2() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier =* [X, Y] :: (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementLoop d0 = (StatementLoop) decOrStatement.get(0);
		checkStatementLoop(d0.name(),
				checkExprEmpty(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);
	}

	@Test
	public void testStatementImageIn() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier <- (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementImageIn d0 = (StatementImageIn) decOrStatement.get(0);
		checkStatementImageIn(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);
	}

	@Test
	public void testStatementImageOut1() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier -> (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementOutFile d0 = (StatementOutFile) decOrStatement.get(0);
		checkStatementOutFile(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);
	}

	@Test
	public void testStatementImageOut2() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier -> screen [(a * + b), (a * + b)];
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementOutScreen d0 = (StatementOutScreen) decOrStatement.get(0);
		checkStatementOutScreen(d0.name(),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR),
				checkExprBinary(checkExprVar("a"),
						checkExprUnary(PLUS, checkExprVar("b")), STAR)).test(d0);
	}

	@Test
	public void testStatementImageOut3() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				identifier -> screen;
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		StatementOutScreen d0 = (StatementOutScreen) decOrStatement.get(0);
		checkStatementOutScreen(d0.name(),
				checkExprEmpty(),
				checkExprEmpty()).test(d0);
	}

	/* ----------Testcases for Expressions----------*/
	@Test
	public void testExpressionConditional1() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				int Expression =(a * + b) | (a * + b) ? (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "Expression",
				checkExprConditional(
						checkExprBinary(
							checkExprBinary(checkExprVar("a"),
									checkExprUnary(PLUS, checkExprVar("b")), STAR),
							checkExprBinary(checkExprVar("a"),
									checkExprUnary(PLUS, checkExprVar("b")), STAR), OR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExpressionConditional2() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				int Expression =(a * + b) & (a * + b) ? (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "Expression",
				checkExprConditional(
						checkExprBinary(
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR),
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR), AND),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExpressionConditional3() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				int Expression =(a * + b) == (a * + b) ? (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "Expression",
				checkExprConditional(
						checkExprBinary(
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR),
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR), EQ),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExpressionConditional4() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				int Expression =(a * + b) != (a * + b) ? (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "Expression",
				checkExprConditional(
						checkExprBinary(
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR),
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR), NEQ),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExpressionConditional5() throws LexicalException, SyntaxException{
		show("input: ");
		show("Expecting: pass");
		String input = """
				int Expression =(a * + b) >= (a * + b) ? (a * + b) : (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		checkDecVar(Type.Int, "Expression",
				checkExprConditional(
						checkExprBinary(
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR),
								checkExprBinary(checkExprVar("a"),
										checkExprUnary(PLUS, checkExprVar("b")), STAR), GE),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExprHash() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int hashTest = 3 # width # height;
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		show(d0.expression().toString());
		checkDecVar(Type.Int, "hashTest",
				checkExprHash(
						checkExprHash(
								checkExprIntLit(3), "width"), "height")).test(d0);
	}

	@Test
	public void testExprPixelSelector() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int PixelSelector = X[(a * + b), (a * + b)];
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		show(d0.expression().toString());
		checkDecVar(Type.Int, "PixelSelector",
				checkExprPixelSelector(
						checkExprVar("X"),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExprPixelConstructor() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int PixelConstructor = <<(a * + b), (a * + b), (a * + b)>>;
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		show(d0.expression().toString());
		checkDecVar(Type.Int, "PixelConstructor",
				checkExprPixelConstructor(
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR),
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}

	@Test
	public void testExprArg() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int PixelConstructor = @ (a * + b);
				""";
		Parser parser = makeParser(input);
		Program node = parser.parse();
		List<ASTNode> decOrStatement = node.decOrStatement();
		DecVar d0 = (DecVar) decOrStatement.get(0);
		show(d0.expression().toString());
		checkDecVar(Type.Int, "PixelConstructor",
				checkExprArg(
						checkExprBinary(checkExprVar("a"),
								checkExprUnary(PLUS, checkExprVar("b")), STAR))).test(d0);
	}
}