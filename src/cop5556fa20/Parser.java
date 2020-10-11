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

import static cop5556fa20.Scanner.Kind.ASSIGN;
import static cop5556fa20.Scanner.Kind.EOF;
import static cop5556fa20.Scanner.Kind.IDENT;
import static cop5556fa20.Scanner.Kind.KW_int;
import static cop5556fa20.Scanner.Kind.KW_string;
import static cop5556fa20.Scanner.Kind.SEMI;

import java.util.ArrayList;
import java.util.List;

import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.Dec;
import cop5556fa20.AST.DecVar;
import cop5556fa20.AST.ExprIntLit;
import cop5556fa20.AST.ExprStringLit;
import cop5556fa20.AST.Expression;
import cop5556fa20.AST.Program;
import cop5556fa20.AST.Type;

public class Parser {

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

	private static final Kind[] firstProgram = {KW_int, KW_string}; //this is not the correct FIRST(Program...), but illustrates a handy programming technique

	private Program program() throws SyntaxException, LexicalException {
		Token first = t; //always save the current token.  
		List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		while (isKind(firstProgram)) {
			switch (t.kind()) {
			case KW_int, KW_string -> {
				Dec dec = declaration();
				decsAndStatements.add(dec);
				match(SEMI);
			}
			//Your finished parser should NEVER throw UnsupportedOperationException, but it is convenient as a placeholder for unimplemented features.
			default -> throw new UnsupportedOperationException("unimplemented feature in program"); 
			}
		}
		return new Program(first, decsAndStatements);  //return a Program object
	}

	private Dec declaration() throws SyntaxException, LexicalException {
		Token first = t;  //always save the current token
		if (isKind(KW_int)) {
			consume();
			Type type = Type.Int;
			Token name = match(IDENT);
			Expression e = Expression.empty; //use this special Expression object if an optional expression is missing.  
			                                 //Using null is an obvious alternative, but that requires checking for null all over the place.  Using a dummy object is much easier.
			if (isKind(ASSIGN)) {
				consume();
				e = expression();
			}
			return new DecVar(first, type , scanner.getText(name), e);  //returns a DecVar object
		}
		return null; //this is hack.  Your completed version should always return some sort of Dec object.
	}


	//expression has package visibility (rather than private) to allow tests to call expression directly  
	protected Expression expression() throws SyntaxException, LexicalException {
		return primary();  //this is a hack and is not the correct body of expression.
	}


	private Expression primary() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e = switch (t.kind()) {
		case INTLIT -> {
			int value = scanner.intVal(t);
			consume();
			yield new ExprIntLit(first, value);
		}
		case STRINGLIT -> {
			String text = scanner.getText(t);
			consume();
			yield new ExprStringLit(first, text);
		}

		default -> throw new SyntaxException(first, "Error or unimplemented feature: " + t.kind());
		};
		return e;
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
	 * @param kind
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
