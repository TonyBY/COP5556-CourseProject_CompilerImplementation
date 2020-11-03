package cop5556fa20;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertThrows;
import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.Program;
import cop5556fa20.Parser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.TypeCheckVisitor.TypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("preview")
class TypeCheckTest {

	/*
	 * To make it easy to print objects and turn this output on and off.
	 */
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	/**
	 * Test input program that is expected to be syntactically correct and pass type checking.
	 * @param input   
	 * @throws Exception
	 */
	void pass(String input) throws Exception {
		Program prog = parseAndGetProgram(input);		
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
		show(prog);
	}
	
	
	/**
	 * Test input program that is expected to be syntactically correct, but fails type checking.
	 * @param input
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	void fail(String input) throws LexicalException, SyntaxException {
		Program prog = parseAndGetProgram(input);
		//show(prog);  //Display the AST
		TypeCheckVisitor v = new TypeCheckVisitor();
		Exception exception = assertThrows(TypeException.class, () -> {
			prog.visit(v, null);
		});
		show(exception);
	}
	
	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		//show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	
	Program parseAndGetProgram(String input) throws LexicalException, SyntaxException{
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertFalse(parser.scanner.hasTokens());
		return (Program)node;
	}
	
	@Test
	public void testEmpty() throws Exception {
		String input = "";  //The input is the empty string.  This is legal
		Program prog = parseAndGetProgram(input);
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
	}	
	
	
	/**
	 * This is one of the simplest nonempty correct programs.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testdec0() throws Exception {
		String input = """
				int x;
				""";  
		pass(input);
	}	
		

	/**
	 * This program fails type checking due to the attempt to redefine x
	 * @throws Exception
	 */
	@Test
	public void testdec1_fail() throws Exception {
		String input = """
				int x;
				string x;
				""";  
		fail(input);
	}

	@Test
	public void testdecVarPass1() throws Exception {
		String input = """
				int x = @0;
				""";
		pass(input);
	}

	@Test
	public void testdecVarFail1() throws Exception {
		String input = """
				int x = 10;
				int x = 20;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Pass1() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (a == b) ? c : d;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprConditional_Pass2() throws Exception {
		String input = """
				image a;
				image b;
				int c;
				int d;
				int x = (a == b) ? c : d;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprConditional_Pass3() throws Exception {
		String input = """
				image a;
				image b;
				int c;
				int d;
				int x = (a == b) ? @0 : d;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprConditional_Pass4() throws Exception {
		String input = """
				image a;
				image b;
				int c;
				int d;
				int x = (a == b) ? 1 : @0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail1() throws Exception {
		String input = """
				int a;
				int b;
				image c;
				image d;
				int x = (a == b) ? c : d;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail2() throws Exception {
		String input = """
				int c;
				int d;
				int x = (a == b) ? c : d;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail3() throws Exception {
		String input = """
				int c;
				int d;
				int x = (@0 == @1) ? c : d;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail4() throws Exception {
		String input = """
				int a;
				int b;
				int x = (a == b) ? @0 : @1;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail5() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				string d;
				int x = (a == b) ? c : d;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConditional_Fail6() throws Exception {
		String input = """
				int a;
				int c;
				string d;
				int x = a ? c : d;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_Boolean_Pass1() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = ((a != b & c >= d) | ( a<=c & b>d & c < d)) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprBinary_Boolean__Fail1() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (@0 | @1) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_Boolean__Fail2() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (@0 & @1) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_Boolean__Fail3() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (@0 > @1) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_Boolean__Fail4() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (@0 == @1 ? 1 : 0);
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_int_string_Pass1() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = ((a + @0 == c - @1) & ( @2 * b >= c / @3) & (@4 % 2 == 0)) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprBinary_int_string_Pass2() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (("a" + @0 == "c" + @1) & ( @2 * b >= c / @3) & (@4 % 2 == 0)) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprBinary_int_string_Fail1() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = ((a + @0 == c - @1) & ( @2 * b >= c / @3) & (@4 % 2 == "0")) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_int_string_Fail2() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = ((a + @0 == c - @1) & ( @2 * "b" >= c / @3) & (@4 % 2 == 0)) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprBinary_int_string_Fail3() throws Exception {
		String input = """
				int a;
				int b;
				int c;
				int d;
				int x = (("a" + @0 == "c" - @1) & ( @2 * b >= c / @3) & (@4 % 2 == 0)) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprUnary_Pass1() throws Exception {
		String input = """
				int x = !(+@0 == -@1) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprUnary_Pass3() throws Exception {
		String input = """
				int x = !(+@0 == -@1) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprUnary_Fail1() throws Exception {
		String input = """
				string a;
				int x = (+a != "a") ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprUnary_Fail2() throws Exception {
		String input = """
				int x = (!@0 == 3) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprUnary_Fail3() throws Exception {
		String input = """
				int x = (!3 != 3) ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprHash_Pass1() throws Exception {
		String input = """
				image img;
				int x = 3#red;
				int y = img#width;
				int z = @0#green;
				int a = @1#blue;
				int b = img#height;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprHash_Fail1() throws Exception {
		String input = """
				image img;
				int x = img#red;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprHash_Fail2() throws Exception {
		String input = """
				int x = @0#width;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprHash_Fail3() throws Exception {
		String input = """
				image x = @0#blue;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprVar_Pass1() throws Exception {
		String input = """
				int x = 10;
				int y = X;
				int z = Y;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprVar_Pass2() throws Exception {
		String input = """
				int x = (X + @0 > 10) ? 1 : 0;
				int y = (Y + @1 < 10) ? 1 : 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprVar_Fail1() throws Exception {
		String input = """
				image x = X;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprVar_Fail2() throws Exception {
		String input = """
				int x = (X + "a" == "ba") ? 1 : 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprConst_Pass1() throws Exception {
		String input = """
				int x = Z;
				int y = (WHITE + @0 > 100) ? 1: 0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprConst_Fail1() throws Exception {
		String input = """
				int y = (Z + @0 == "aabb") ? 1: 0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprPixelConstructor_Pass1() throws Exception {
		String input = """
				int x = <<@0, @1, @2>>;
				int y = <<10, 20, 30>>;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprPixelConstructor_Fail1() throws Exception {
		String input = """
				string x = <<@0, @1, @2>>;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprPixelConstructor_Fail2() throws Exception {
		String input = """
				int x = <<@0, "1", 2>>;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprPixelSelector_Pass1() throws Exception {
		String input = """
				image img;
				int x = img [30, 40];
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprPixelSelector_Pass2() throws Exception {
		String input = """
				image img;
				int x = img [(@0 + @1), (@2 + @3)];
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprPixelSelector_Fail1() throws Exception {
		String input = """
				int x = @0 [30, 40];
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprPixelSelector_Fail2() throws Exception {
		String input = """
				image img;
				int x = img [@0, @1];
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprArg_Pass1() throws Exception {
		String input = """
				int x = @(Z);
				int y = @(X + Y + Z);
				string z = @0;
				""";
		pass(input);
	}

	@Test
	public void testdecVar_ExprArg_Fail1() throws Exception {
		String input = """
				image x = @0;
				""";
		fail(input);
	}

	@Test
	public void testdecVar_ExprArg_Fail2() throws Exception {
		String input = """
				int x = @"a";
				""";
		fail(input);
	}

	@Test
	public void testdecImage0() throws Exception {
		String input = """
				image x;
				""";
		pass(input);
	}

	@Test
	public void testdecImagePass1() throws Exception {
		String input = """
				image [1000,2000] im <- @0;
				""";
		pass(input);
	}

	@Test
	public void testdecImagePass2() throws Exception {
		String input = """
				image [@0, @1] im <- "image";
				""";
		pass(input);
	}

	@Test
	public void testdecImagePass3() throws Exception {
		String input = """
    			image img;
    			image [@0, @1] im = img;
				""";
		pass(input);
	}

	@Test
	public void testdecImageFail1() throws Exception {
		String input = """
				image x;
				int x;
				""";
		fail(input);
	}

	@Test
	public void testdecImageFail2() throws Exception {
		String input = """
				image [1000,2000] im = @0;
				""";
		fail(input);
	}

	@Test
	public void testdecImageFail3() throws Exception {
		String input = """
				image [1000,2000] im <- 10;
				""";
		fail(input);
	}

	@Test
	public void testdecImageFail4() throws Exception {
		String input = """
				image x;
				image x;
				""";
		fail(input);
	}

	@Test
	public void testStatementOutFilePass1() throws Exception {
		String input = """
				image im;
				im -> @0;
				""";
		pass(input);
	}

	@Test
	public void testStatementOutFileFail1() throws Exception {
		String input = """
				int im;
				im -> @0;
				""";
		fail(input);
	}

	@Test
	public void testStatementOutFileFail2() throws Exception {
		String input = """
				im -> @0;
				""";
		fail(input);
	}

	@Test
	public void testStatementOutScreenPass1() throws Exception {
		String input = """
				image im;
				im -> screen[@0, 100];
				""";
		pass(input);
	}

	@Test
	public void testStatementOutScreenPass2() throws Exception {
		String input = """
				image im;
				im -> screen[@0, @1];
				""";
		pass(input);
	}

	@Test
	public void testStatementOutScreenPass3() throws Exception {
		String input = """
				image im;
				im -> screen;
				""";
		pass(input);
	}

	@Test
	public void testStatementOutScreenPass4() throws Exception {
		String input = """
				int im;
				im -> screen;
				""";
		pass(input);
	}

	@Test
	public void testStatementOutScreenPass5() throws Exception {
		String input = """
				string im;
				im -> screen;
				""";
		pass(input);
	}

	@Test
	public void testStatementOutScreenFail1() throws Exception {
		String input = """
				int im;
				im -> screen[@0, 100];
				""";
		fail(input);
	}

	@Test
	public void testStatementOutScreenFail2() throws Exception {
		String input = """
				string im;
				im -> screen[@0, 100];
				""";
		fail(input);
	}

	@Test
	public void testStatementOutScreenFail3() throws Exception {
		String input = """
				im -> screen[@0, @1];
				""";
		fail(input);
	}

	@Test
	public void testStatementOutScreenFail4() throws Exception {
		String input = """
				int im;
				im -> screen["a", 20];
				""";
		fail(input);
	}

	@Test
	public void testStatementImageInPass1() throws Exception {
		String input = """
				image im;
				im <- @0;
				""";
		pass(input);
	}

	@Test
	public void testStatementImageInPass2() throws Exception {
		String input = """
				image im;
				image im2;
				im <- im2;
				""";
		pass(input);
	}

	@Test
	public void testStatementImageInFail1() throws Exception {
		String input = """
				image im;
				im <- 1;
				""";
		fail(input);
	}

	@Test
	public void testStatementImageInFail2() throws Exception {
		String input = """
				image im2;
				im <- im2;
				""";
		fail(input);
	}

	@Test
	public void testStatementAssignPass1() throws Exception {
		String input = """
				image im;
				image im2;
				im = im2;
				""";
		pass(input);
	}

	@Test
	public void testStatementAssignFail1() throws Exception {
		String input = """
				image im;
				im = @0;
				""";
		fail(input);
	}

	@Test
	public void testStatementAssignFail2() throws Exception {
		String input = """
				image im2;
				im = im2;
				""";
		fail(input);
	}

	@Test
	public void testStatementAssignFail3() throws Exception {
		String input = """
				image im1;
				int a;
				im1 = a;
				""";
		fail(input);
	}

	@Test
	public void testStatementLoopPass1() throws Exception {
		String input = """
				image im;
				im =* [X, Y] :: 10;
				""";
		pass(input);
	}

	@Test
	public void testStatementLoopPass2() throws Exception {
		String input = """
				image im;
				int a;
				int b;
				im =* [X, Y] : (a == b) : @0;
				""";
		pass(input);
	}

	@Test
	public void testStatementLoopFail1() throws Exception {
		String input = """
				image im;
				im =* [X, Y] : @0 : 10;
				""";
		fail(input);
	}

	@Test
	public void testStatementLoopFail2() throws Exception {
		String input = """
				image im;
				im =* [X, Y] :: "10";
				""";
		fail(input);
	}

	@Test
	public void testStatementLoopFail3() throws Exception {
		String input = """
				image im;
				im =* [X, Y] : (@0 == @1) : "10";
				""";
		fail(input);
	}

	@Test
	public void testStatementLoopFail4() throws Exception {
		String input = """
				im =* [X, Y] :: 10;
				""";
		fail(input);
	}
}