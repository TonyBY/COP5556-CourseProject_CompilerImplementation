/**
 * Class for  for the class project in COP5556 Programming Language Principles
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

import java.util.Set;

import static cop5556fa20.Scanner.Kind;
import static cop5556fa20.Scanner.Kind.*;

public class SimpleParser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}

	final Scanner scanner; //Token producer
	Token t; //next token

	private static final Set<Kind> PROGRAM_PREDICT_SET = Set.of(KW_int, KW_string, KW_image, IDENT);
//	private static final Set<Kind> DECLARATION_PREDICT_SET = Set.of(KW_int, KW_string, KW_image);
//	private static final Set<Kind> STATEMENT_PREDICT_SET = Set.of(IDENT);
	private static final Set<Kind> VARIABLE_DECLARATION_PREDICT_SET = Set.of(KW_int, KW_string);
//	private static final Set<Kind> VAR_TYPE_PREDICT_SET = Set.of(KW_int, KW_string);
//	private static final Set<Kind> IMAGE_DECLARATION_PREDICT_SET = Set.of(KW_image);
	private static final Set<Kind> STATEMENT_TAIL_PREDICT_SET = Set.of(ASSIGN, RARROW,  LARROW);
	private static final Set<Kind> NEW_ASSIGNMENT_STATEMENT_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT,
			LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT, LSQUARE);
	private static final Set<Kind> NEW_LOOP_STATEMENT_PREDICT_SET = Set.of(STAR);
	private static final Set<Kind> NEW_IMAGE_OUT_STATEMENT_PREDICT_SET = Set.of(RARROW);
	private static final Set<Kind> NEW_IMAGE_OUT_STATEMENT_TAIL_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT,
			LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT, LSQUARE, KW_SCREEN);
	private static final Set<Kind> NEW_IMAGE_IN_STATEMENT_PREDICT_SET = Set.of(LARROW);
	private static final Set<Kind> EXPRESSION_PREDICT_SET = Set.of(PLUS, MINUS, EXCL, INTLIT, IDENT, LPAREN,
			STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT, LSQUARE);

	private static final Set<Kind> UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET = Set.of(EXCL, INTLIT, IDENT, LPAREN,
			STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT, LSQUARE);
	private static final Set<Kind> HASH_EXPRESSION_PREDICT_SET = Set.of(INTLIT, IDENT, LPAREN, STRINGLIT, KW_X, KW_Y,
			CONST, LPIXEL, AT, LSQUARE);

	private static final Set<Kind> ATTRIBUTE_PREDICT_SET =Set.of(KW_WIDTH, KW_HEIGHT, KW_RED, KW_GREEN, KW_BLUE);

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		//TODO ??
	}

	public void parse() throws SyntaxException, LexicalException {
		program();
		if (!consumedAll()) throw new SyntaxException(t, "tokens remain after parsing");
			//If consumedAll returns false, then there is at least one
		    //token left (the EOF token) so the call to nextToken is safe. 
	}

	Token consume() {
		t = scanner.nextToken();
		return t;
	}

	void match(Kind kind) throws SyntaxException {
		if( t.isKind(kind)){
			consume();
		} else {
			throw new SyntaxException(t,
					String.format("Token does not match! Expecting %s, actual %s", kind, t.kind()));
		}
	}

	public boolean consumedAll() {
		if (scanner.hasTokens()) { 
			Token t = scanner.nextToken();
			System.out.println("consumedAll: " + t.kind());
			return t.kind() == Kind.EOF;
		}
		return true;
	}

	private void program() throws SyntaxException, LexicalException {
		//TODO
		System.out.println("program: " + t.kind());
		if (PROGRAM_PREDICT_SET.contains(t.kind()) || t.isKind(EOF)) {
			while (PROGRAM_PREDICT_SET.contains(t.kind())) {
				if (t.isKind(Kind.IDENT)) {
					statement();
				} else {
					declaration();
				}
				match(SEMI);
			}
		} else {
			throw new SyntaxException(t,
					String.format("Program is wrong! Expecting %s, actual %s", PROGRAM_PREDICT_SET, t.kind()));
		}

	}

	private void declaration() throws SyntaxException, LexicalException {
		System.out.println("declaration: " + t.kind());
		if (VARIABLE_DECLARATION_PREDICT_SET.contains(t.kind())) {
			variableDeclaration();
		} else { // if (IMAGE_DECLARATION_PREDICT_SET.contains(t.kind())) {
			imageDeclaration();
		}
	}

	private  void variableDeclaration() throws SyntaxException, LexicalException {
		System.out.println("variableDeclaration: " + t.kind());
		consume(); // t.token should be KW_int|KW_string.
		match(IDENT);
		if (t.isKind(ASSIGN)){
			consume();
			expression();
		}
	}

	private void imageDeclaration() throws SyntaxException, LexicalException {
		System.out.println("imageDeclaration: " + t.kind());
		match(KW_int);
		if (t.isKind(LSQUARE)) {
			consume();
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		} else {
			match(IDENT);
		}
		if (t.isKind(LARROW) || t.isKind(ASSIGN)) {
			consume();
			expression();
		}
	}

	private void statement() throws SyntaxException, LexicalException {
		System.out.println("statement: " + t.kind());
		match(IDENT);
		statementTail();
	}

	private void statementTail() throws SyntaxException, LexicalException {
		System.out.println("statementTail: " + t.kind());
		if (STATEMENT_TAIL_PREDICT_SET.contains(t.kind())) {
			if (t.isKind(ASSIGN)) {
				consume();
				if (NEW_ASSIGNMENT_STATEMENT_PREDICT_SET.contains(t.kind())) {
					newAssignmentStatement();
				} else if (NEW_LOOP_STATEMENT_PREDICT_SET.contains(t.kind())) {
					newLoopStatement();
				} else {
					throw new SyntaxException(t,
							String.format("statementTail is wrong! Expecting %s%s, actual %s",
									NEW_ASSIGNMENT_STATEMENT_PREDICT_SET, NEW_LOOP_STATEMENT_PREDICT_SET, t.kind()));
				}
			} else if (NEW_IMAGE_OUT_STATEMENT_PREDICT_SET.contains(t.kind())) {
				newImageOutStatement();
			} else if (NEW_IMAGE_IN_STATEMENT_PREDICT_SET.contains(t.kind())) {
				newImageInStatement();
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

	private void newAssignmentStatement() throws SyntaxException, LexicalException {
		System.out.println("newAssignmentStatement: " + t.kind());
		expression();
	}

	private void newLoopStatement() throws SyntaxException, LexicalException {
		System.out.println("newLoopStatement: " + t.kind());
		match(STAR);
		constXYSelector();
		match(COLON);
		if (EXPRESSION_PREDICT_SET.contains(t.kind())){
			expression();
		} else {
			match(COLON);
			expression();
		}
	}

	private void newImageOutStatement() throws SyntaxException, LexicalException {
		System.out.println("newImageOutStatement: " + t.kind());
		match(RARROW);
		newImageOutStatementTail();
	}

	private void newImageOutStatementTail() throws SyntaxException, LexicalException {
		System.out.println("newImageOutStatementTail: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			expression();
		} else if (t.isKind(KW_SCREEN)) {
			consume();
			if (t.isKind(LSQUARE)) {
				consume();
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
			}
		} else {
			throw new SyntaxException(t,
					String.format("newImageOutStatementTail is wrong! Expecting %s, actual %s",
							NEW_IMAGE_OUT_STATEMENT_TAIL_PREDICT_SET, t.kind()));
		}
	}

	private void newImageInStatement() throws SyntaxException, LexicalException {
		System.out.println("newImageInStatement: " + t.kind());
		match(LARROW);
		expression();
	}

	private void constXYSelector() throws SyntaxException {
		System.out.println("constXYSelector: " + t.kind());
		match(LSQUARE);
		match(KW_X);
		match(COMMA);
		match(KW_Y);
		match(RSQUARE);
	}


	//make this public for convenience testing
	public void expression() throws SyntaxException, LexicalException {
		//TODO
		System.out.println("Expression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			orExpression();
			if (t.isKind(Q)) {
				consume();
				expression();
				match(COLON);
				expression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("Expression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void orExpression() throws SyntaxException, LexicalException {
		System.out.println("orExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			andExpression();
			while (t.isKind(OR)) {
				consume();
				andExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("orExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}

	}

	public void andExpression() throws SyntaxException, LexicalException {
		System.out.println("andExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			eqExpression();
			while (t.isKind(AND)) {
				consume();
				eqExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("andExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void eqExpression() throws SyntaxException, LexicalException {
		System.out.println("eqExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			relExpression();
			while (t.isKind(EQ) || t.isKind(NEQ)) {
				consume();
				relExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("eqExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void relExpression() throws SyntaxException, LexicalException {
		System.out.println("relExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			addExpression();
			while (t.isKind(LT) || t.isKind(GT) || t.isKind(LE) || t.isKind(GE)) {
				consume();
				addExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("relExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void addExpression() throws SyntaxException, LexicalException {
		System.out.println("addExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			multExpression();
			while (t.isKind(PLUS) || t.isKind(MINUS)) {
				consume();
				multExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("addExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void multExpression() throws SyntaxException, LexicalException {
		System.out.println("multExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			unaryExpression();
			while (t.isKind(STAR) || t.isKind(DIV) || t.isKind(MOD)) {
				consume();
				unaryExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("multExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void unaryExpression() throws SyntaxException, LexicalException {
		System.out.println("unaryExpression: " + t.kind());
		if (EXPRESSION_PREDICT_SET.contains(t.kind())) {
			if (t.isKind(PLUS) || t.isKind(MINUS)) {
				consume();
				unaryExpression();
			}else{
				unaryExpressionNotPlusMinus();
			}
		}else{
			throw new SyntaxException(t,
					String.format("unaryExpression is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void unaryExpressionNotPlusMinus() throws SyntaxException, LexicalException {
		System.out.println("unaryExpressionNotPlusMinus: " + t.kind());
		if (UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET.contains(t.kind())) {
			if (t.isKind(EXCL)) {
				consume();
				unaryExpression();
			}else{
				hashExpression();
			}
		}else{
			throw new SyntaxException(t,
					String.format("unaryExpressionNotPlusMinus is wrong! Expecting: %s, actual: %s",
							UNARY_EXPRESSION_NOT_PLUS_MINUS_PREDICT_SET, t.kind()));
		}
	}

	public void hashExpression() throws SyntaxException, LexicalException {
		System.out.println("hashExpression: " + t.kind());
		if (HASH_EXPRESSION_PREDICT_SET.contains(t.kind())) {
			primary();
			while (t.isKind(HASH)) {
				consume();
				attribute();
			}
		}else{
			throw new SyntaxException(t,
					String.format("hashExpression is wrong! Expecting: %s, actual: %s",
							HASH_EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void primary() throws SyntaxException, LexicalException {
		System.out.println("primary: " + t.kind());
		if (HASH_EXPRESSION_PREDICT_SET.contains(t.kind())) {
			if (t.isKind(LPAREN)){
				consume();
				expression();
				match(RPAREN);
			}else if (t.isKind(LPIXEL)) {
				pixelConstructor();
			}else if (t.isKind(AT)) {
				argExpression();
			}else if (t.isKind(LSQUARE)){
				pixelSelector();
			}else{
				consume();
			}
		}else{
			throw new SyntaxException(t,
					String.format("primary is wrong! Expecting: %s, actual: %s",
							HASH_EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

	public void pixelConstructor() throws SyntaxException, LexicalException {
		System.out.println("pixelConstructor: " + t.kind());
		match(LPIXEL);
		expression();
		match(COMMA);
		expression();
		match(COMMA);
		expression();
		match(RPIXEL);
	}

	public void argExpression() throws SyntaxException, LexicalException {
		System.out.println("argExpression: " + t.kind());
		match(AT);
		primary();
	}

	public void pixelSelector() throws SyntaxException, LexicalException {
		System.out.println("pixelSelector: " + t.kind());
		match(LSQUARE);
		expression();
		match(COMMA);
		expression();
		match(RSQUARE);
	}

	public void attribute() throws SyntaxException {
		System.out.println("attribute: " + t.kind());
		if (ATTRIBUTE_PREDICT_SET.contains(t.kind())) {
			consume();
		}else{
			throw new SyntaxException(t,
					String.format("attribute is wrong! Expecting: %s, actual: %s",
							EXPRESSION_PREDICT_SET, t.kind()));
		}
	}

   //TODO--everything else.  Have fun!!
}
