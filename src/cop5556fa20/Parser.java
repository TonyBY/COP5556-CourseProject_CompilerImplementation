/**
 * Parser for the class project in COP5556 Programming Language Principles 
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
import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cop5556fa20.Scanner.Kind.*;

public class Parser {

	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = false;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;  //the token that caused an error to be discovered.

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken(); // establish invariant that t is always the next token to be processed
	}

	public Program parse() throws SyntaxException, LexicalException {
		Program p = program();
		matchEOF();
		return p;
	}

//	private static final Kind[] firstProgram = {KW_int, KW_string}; //this is not the correct FIRST(Program...), but illustrates a handy programming technique

	private static final Set<Kind> PROGRAM_PREDICT_SET = Set.of(KW_int, KW_string, KW_image, IDENT);
	private static final Set<Kind> VARIABLE_DECLARATION_PREDICT_SET = Set.of(KW_int, KW_string);
	private static final Set<Kind> STATEMENT_TAIL_PREDICT_SET = Set.of(ASSIGN, RARROW,  LARROW);
	private static final Set<Kind> NEW_ASSIGNMENT_STATEMENT_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT,
			LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT);
	private static final Set<Kind> NEW_LOOP_STATEMENT_PREDICT_SET = Set.of(STAR);
	private static final Set<Kind> NEW_IMAGE_OUT_STATEMENT_PREDICT_SET = Set.of(RARROW);
	private static final Set<Kind> NEW_IMAGE_OUT_STATEMENT_TAIL_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT,
			LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT, KW_SCREEN);
	private static final Set<Kind> NEW_IMAGE_IN_STATEMENT_PREDICT_SET = Set.of(LARROW);
	private static final Set<Kind> EXPRESSION_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT, LPAREN,
			STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT);

	private static final Set<Kind> UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET = Set.of(EXCL, INTLIT, IDENT, LPAREN,
			STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT);
	private static final Set<Kind> HASH_EXPRESSION_PREDICT_SET = Set.of(INTLIT, IDENT, LPAREN, STRINGLIT, KW_X, KW_Y,
			CONST, LPIXEL, AT);

	private static final Set<Kind> ATTRIBUTE_PREDICT_SET =Set.of(KW_WIDTH, KW_HEIGHT, KW_RED, KW_GREEN, KW_BLUE);

	private Program program() throws SyntaxException, LexicalException {
		show("program: " + t.kind());
		if (isKind(PROGRAM_PREDICT_SET) || isKind(EOF)) {
			Token first = t; //always save the current token.
			List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
			while (isKind(PROGRAM_PREDICT_SET)) {
				switch (t.kind()) {
					case KW_int, KW_string, KW_image -> {
						Dec dec = declaration();
						decsAndStatements.add(dec);
//						match(SEMI);
					}
					case IDENT -> {
						Statement sta = statement();
						decsAndStatements.add(sta);
					}
					//Your finished parser should NEVER throw UnsupportedOperationException, but it is convenient as a placeholder for unimplemented features.
					default -> throw new UnsupportedOperationException("unimplemented feature in program");
				}
				match(SEMI);
			}
			return new Program(first, decsAndStatements);  //return a Program object
		} else {
			throw new Parser.SyntaxException(t,
					String.format("Program is wrong! Expecting %s, actual %s", PROGRAM_PREDICT_SET, t.kind()));
		}

	}

	private Dec declaration() throws SyntaxException, LexicalException {
		show("declaration: " + t.kind());

		Token first = t;  //always save the current token
		if (isKind(VARIABLE_DECLARATION_PREDICT_SET)) {
			return variableDeclaration();
		} else {
			return imageDeclaration();
		}
//		return null; //this is hack.  Your completed version should always return some sort of Dec object.
	}

	private  DecVar variableDeclaration() throws SyntaxException, LexicalException {
		show("variableDeclaration: " + t.kind());
		Token first = t;

		Type type;

		if (isKind(KW_int)) {
			type = Type.Int;
		} else {
			type = Type.String;
		}

		consume();

		Token name = match(IDENT);
		Expression e = Expression.empty; //use this special Expression object if an optional expression is missing.
		//Using null is an obvious alternative, but that requires checking for null all over the place.  Using a dummy object is much easier.
		if (isKind(ASSIGN)) {
			consume();
			e = expression();
		}
		return new DecVar(first, type , scanner.getText(name), e);  //returns a DecVar object
	}

	private DecImage imageDeclaration() throws SyntaxException, LexicalException {
		show("imageDeclaration: " + t.kind());
		Token first = t;
		Token name = match(KW_image);
		Type type = Type.Image;
		Expression e0 = Expression.empty;
		Expression e1 = Expression.empty;
		Expression e2 = Expression.empty;
		Kind op = NOP;
		if (t.isKind(LSQUARE)) {
			consume();
			e0 = expression();
			match(COMMA);
			e1 = expression();
			match(RSQUARE);
		}

		match(IDENT);

		if (t.isKind(LARROW) || t.isKind(ASSIGN)) {
			op = t.kind();
			consume();
			e2 = expression();
		}
		return new DecImage(first, type , scanner.getText(name), e0, e1, op, e2);  //returns a DecVar object
	}

	private Statement statement() throws SyntaxException, LexicalException {
		show("statement: " + t.kind());

		Token first = t;  //always save the current token

		Token name = match(IDENT);
		 return statementTail(first);
//		return null; //this is hack.  Your completed version should always return some sort of Dec object.
	}

	private Statement statementTail(Token first) throws SyntaxException, LexicalException {
		show("statementTail: " + t.kind());

//		Token first = t;  //always save the current token

		if (isKind(STATEMENT_TAIL_PREDICT_SET)) {
			if (isKind(ASSIGN)) {
				consume();
				if (isKind(NEW_ASSIGNMENT_STATEMENT_PREDICT_SET)) {
					return newAssignmentStatement(first);
				} else if (isKind(NEW_LOOP_STATEMENT_PREDICT_SET)) {
					return newLoopStatement(first);
				} else {
					throw new SyntaxException(t,
							String.format("statementTail is wrong! Expecting %s%s, actual %s",
									NEW_ASSIGNMENT_STATEMENT_PREDICT_SET, NEW_LOOP_STATEMENT_PREDICT_SET, t.kind()));
				}
			} else if (NEW_IMAGE_OUT_STATEMENT_PREDICT_SET.contains(t.kind())) {
				return newImageOutStatement(first);
			} else if (NEW_IMAGE_IN_STATEMENT_PREDICT_SET.contains(t.kind())) {
				return newImageInStatement(first);
			} else {
				throw new SyntaxException(t,
						String.format("statementTail is wrong! Expecting %s%s, actual %s",
								NEW_IMAGE_OUT_STATEMENT_PREDICT_SET, NEW_IMAGE_IN_STATEMENT_PREDICT_SET, t.kind()));
			}
		} else {
			throw new SyntaxException(t,
					String.format("statementTail is wrong! Expecting %s, actual %s",
							STATEMENT_TAIL_PREDICT_SET, t.kind()));
		}
	}

	private StatementAssign newAssignmentStatement(Token first) throws SyntaxException, LexicalException {
		show("newAssignmentStatement: " + t.kind());
		Expression e = expression();
		return new StatementAssign(first, scanner.getText(first), e);
	}

	private StatementLoop newLoopStatement(Token first) throws SyntaxException, LexicalException {
		show("newLoopStatement: " + t.kind());

		Expression e0 = Expression.empty;

		match(STAR);
		constXYSelector();
		match(COLON);
		if (isKind(EXPRESSION_PREDICT_SET)){
			e0 = expression();
		}
		match(COLON);
		Expression e1 = expression();

		return new StatementLoop(first, scanner.getText(first), e0, e1);
	}

	private Statement newImageOutStatement(Token first) throws SyntaxException, LexicalException {
		show("newImageOutStatement: " + t.kind());
		match(RARROW);
		return newImageOutStatementTail(first);
	}

	private Statement newImageOutStatementTail(Token first) throws SyntaxException, LexicalException {
		show("newImageOutStatementTail: " + t.kind());

		if (isKind(EXPRESSION_PREDICT_SET)) {
			Expression e0 = expression();
			return new StatementOutFile(first, scanner.getText(first), e0);

		} else if (isKind(KW_SCREEN)) {
			consume();

			Expression e1 = Expression.empty;
			Expression e2 = Expression.empty;
			if (t.isKind(LSQUARE)) {
				consume();
				e1 = expression();
				match(COMMA);
				e2 = expression();
				match(RSQUARE);
			}
			return new StatementOutScreen(first, scanner.getText(first), e1, e2);
		} else {
			throw new SyntaxException(t,
					String.format("newImageOutStatementTail is wrong! Expecting %s, actual %s",
							NEW_IMAGE_OUT_STATEMENT_TAIL_PREDICT_SET, t.kind()));
		}
	}

	private StatementImageIn newImageInStatement(Token first) throws SyntaxException, LexicalException {
		show("newImageInStatement: " + t.kind());
		match(LARROW);
		Expression e0 = expression();
		return new StatementImageIn(first, scanner.getText(first), e0);
	}

	private void constXYSelector() throws SyntaxException {
		show("constXYSelector: " + t.kind());
		match(LSQUARE);
		match(KW_X);
		match(COMMA);
		match(KW_Y);
		match(RSQUARE);
	}


//	//expression has package visibility (rather than private) to allow tests to call expression directly
//	protected Expression expression() throws SyntaxException, LexicalException {
//		return primary();  //this is a hack and is not the correct body of expression.
//	}

	//make this public for convenience testing
	protected Expression expression() throws SyntaxException, LexicalException {
		show("Expression: " + t.kind());
		Token first = t;  //always save the current token

		if (isKind(EXPRESSION_PREDICT_SET)) {

			Expression e0 = orExpression();
			if (isKind(Q)) {
				consume();
				Expression e1 = expression();
				match(COLON);
				Expression e2 = expression();
				return new ExprConditional(first, e0, e1, e2);
			} else {
				return e0;
			}
		}else{
			throw new SyntaxException(t,
					String.format("Expression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression orExpression() throws SyntaxException, LexicalException {
		show("orExpression: " + t.kind());
		Token first = t;
		if (isKind(EXPRESSION_PREDICT_SET)) {
			Expression e0 = andExpression();
			while (t.isKind(OR)) {
				Token op = t;
				consume();
				Expression e1 = andExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("orExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}

	}

	public Expression andExpression() throws SyntaxException, LexicalException {
		show("andExpression: " + t.kind());
		Token first = t;
		if (isKind(EXPRESSION_PREDICT_SET)) {
			Expression e0 = eqExpression();
			while (t.isKind(AND)) {
				Token op = t;
				consume();
				Expression e1 = eqExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("andExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression eqExpression() throws SyntaxException, LexicalException {
		show("eqExpression: " + t.kind());
		Token first = t;
		if (isKind(EXPRESSION_PREDICT_SET)) {
			Expression e0 = relExpression();
			while (isKind(EQ) || isKind(NEQ)) {
				Token op = t;
				consume();
				Expression e1 = relExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("eqExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression relExpression() throws SyntaxException, LexicalException {
		show("relExpression: " + t.kind());
		Token first = t;
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			Expression e0 = addExpression();
			while (isKind(LT) || isKind(GT) || isKind(LE) || isKind(GE)) {
				Token op = t;
				consume();
				Expression e1 = addExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("relExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression addExpression() throws SyntaxException, LexicalException {
		show("addExpression: " + t.kind());
		Token first = t;
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			Expression e0 = multExpression();
			while (t.isKind(PLUS) || t.isKind(MINUS)) {
				Token op = t;
				consume();
				Expression e1 = multExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("addExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression multExpression() throws SyntaxException, LexicalException {
		show("multExpression: " + t.kind());
		Token first = t;
		if (isKind(EXPRESSION_PREDICT_SET)) {
			Expression e0 = unaryExpression();
			while (isKind(STAR) || isKind(DIV) || isKind(MOD)) {
				Token op = t;
				consume();
				Expression e1 = unaryExpression();
				e0 = new ExprBinary(first, e0, op.kind(), e1);
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("multExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression unaryExpression() throws SyntaxException, LexicalException {
		show("unaryExpression: " + t.kind());
		Token first = t;
		if (isKind(EXPRESSION_PREDICT_SET)) {
			if (isKind(PLUS) || isKind(MINUS)) {
				Token op = t;
				consume();
				Expression e0 = unaryExpression();
				return new ExprUnary(first, op.kind(), e0);
			}else{
				return unaryExpressionNotPlusMinus();
			}
		}else{
			throw new SyntaxException(t,
					String.format("unaryExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public Expression unaryExpressionNotPlusMinus() throws SyntaxException, LexicalException {
		show("unaryExpressionNotPlusMinus: " + t.kind());
		Token first = t;
		if (isKind(UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET)) {
			if (isKind(EXCL)) {
				Token op = t;
				consume();
				Expression e0 = unaryExpression();
				return new ExprUnary(first,op.kind(), e0);
			}else{
				return hashExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("unaryExpressionNotPlusMinus is wrong! Expecting: %s, actual: %s",
							UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET, t.kind()));
		}
	}

	public Expression hashExpression() throws SyntaxException, LexicalException {
		show("hashExpression: " + t.kind());
		Token first = t;
		if (isKind(HASH_EXPRESSION_PREDICT_SET)) {
			Expression e0 = primary();
			while (isKind(HASH)) {
				consume();
				Token attr = attribute();
				e0 = new ExprHash(first, e0, scanner.getText(attr));
			}
			return e0;
		}else{
			throw new SyntaxException(t,
					String.format("hashExpression is wrong! Expecting: %s, actual: %s",
							HASH_EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	private Expression primary() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e = switch (t.kind()) {
			case INTLIT -> {
				int value = scanner.intVal(t);
				consume();
				yield new ExprIntLit(first, value);
			}
			case IDENT, KW_X, KW_Y -> {
				String name = scanner.getText(t);
				consume();
				yield new ExprVar(first, name);
			}
			case LPAREN -> {
				consume();
				Expression e0 = expression();
				match(RPAREN);
				yield e0;
			}
			case STRINGLIT -> {
				String text = scanner.getText(t);
				consume();
				yield new ExprStringLit(first, text);
			}
			case CONST -> {
				String name = scanner.getText(t);
				int value = scanner.intVal(t);
				consume();
				yield new ExprConst(first, name, value);
			}
			case LPIXEL -> pixelConstructor(first);
			case AT -> argExpression(first);

			default -> throw new SyntaxException(first, String.format("primary is wrong! Expecting: %s, actual: %s",
					HASH_EXPRESSION_PREDICT_SET, t.kind()));
			};

		if (isKind(LSQUARE)){
			e = pixelSelector(first, e);
		}

		return e;
	}

	public Expression pixelConstructor(Token first) throws SyntaxException, LexicalException {
		show("pixelConstructor: " + t.kind());
		match(LPIXEL);
		Expression er = expression();
		match(COMMA);
		Expression eg = expression();
		match(COMMA);
		Expression eb = expression();
		match(RPIXEL);
		return new ExprPixelConstructor(first, er, eg, eb);
	}

	public Expression argExpression(Token first) throws SyntaxException, LexicalException {
		show("argExpression: " + t.kind());
		match(AT);
		Expression e0 = primary();
		return new ExprArg(first, e0);
	}

	public Expression pixelSelector(Token first, Expression e0) throws SyntaxException, LexicalException {
		show("pixelSelector: " + t.kind());
		match(LSQUARE);
		Expression ex = expression();
		match(COMMA);
		Expression ey = expression();
		match(RSQUARE);
		return new ExprPixelSelector(first, e0, ex, ey);
	}

	public Token attribute() throws SyntaxException {
		show("attribute: " + t.kind());
		Token first = t;
		if (isKind(ATTRIBUTE_PREDICT_SET)) {
			consume();
			return first;
		}else{
			throw new SyntaxException(t,
					String.format("attribute is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	protected boolean isKind(Kind kind) {
		return t.kind() == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind())
				return true;
		}
		return false;
	}

	protected boolean isKind(Set<Kind> kinds) {
		if (kinds.contains(t.kind())) {
			return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(t, kind.toString());
		return null; // unreachable
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kinds
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		error(t, "expected one of " + kinds);
		return null; // unreachable
	}

	private Token match(Set<Kind> kinds) throws SyntaxException {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		error(t, "expected one of " + kinds);
		return null; // unreachable
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			error(t, "attempting to consume EOF");
		}
		t = scanner.nextToken();
		return tmp;
	}

	private void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message);
	}
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so there is no
	 * attempt to get the nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		error(t, EOF.toString());
		return null; // unreachable
	}
}
