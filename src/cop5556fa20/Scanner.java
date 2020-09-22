/**
 * Scanner for the class project in COP5556 Programming Language Principles 
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

import java.util.*;

public class Scanner {

	@SuppressWarnings("preview")
	public record Token(
		Kind kind,
		int pos, //position in char array.  Starts at zero
		int length, //number of chars in token
		int line, //line number of token in source.  Starts at 1
		int posInLine //position in line of source.  Starts at 1
		) {
	}
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		int pos;
		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		public int pos() { return pos; }
	}
	
	
	public static enum Kind {
		IDENT, INTLIT, STRINGLIT, CONST,
		KW_X/* X */,  KW_Y/* Y */, KW_WIDTH/* width */,KW_HEIGHT/* height */, 
		KW_SCREEN/* screen */, KW_SCREEN_WIDTH /* screen_width */, KW_SCREEN_HEIGHT /*screen_height */,
		KW_image/* image */, KW_int/* int */, KW_string /* string */,
		KW_RED /* red */,  KW_GREEN /* green */, KW_BLUE /* blue */,
		ASSIGN/* = */, GT/* > */, LT/* < */, 
		EXCL/* ! */, Q/* ? */, COLON/* : */, EQ/* == */, NEQ/* != */, GE/* >= */, LE/* <= */, 
		AND/* & */, OR/* | */, PLUS/* + */, MINUS/* - */, STAR/* * */, DIV/* / */, MOD/* % */, 
	    AT/* @ */, HASH /* # */, RARROW/* -> */, LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, LPIXEL /* << */, RPIXEL /* >> */,  SEMI/* ; */, COMMA/* , */,  EOF
	}


	/**
	 * Start in the START state
	 * State state = START;
	 * Read a char and go to the indicated state
	 * When a token has been recognized, create a Token and add to tokens.
	 * Repeat until no more input.
	 */
	private enum State {
		START, HAVE_EQUAL, DIGITS, IDENT_PART, HAVE_LT, HAVE_GT, HAVE_EXCL, HAVE_MINUS, HAVE_CR, HAVE_SLASH,
		COMMENT, HAVE_DOUBLE_QUOT, ESCAPE
	}
	

	/**
	 * Returns the text of the token.  If the token represents a String literal, then
	 * the returned text omits the delimiting double quotes and replaces escape sequences with
	 * the represented character.
	 * 
	 * @param token
	 * @return
	 */
	public String getText(Token token) {
		/* IMPLEMENT THIS */
		if (token.kind() == Kind.STRINGLIT) {
			int startPos = token.pos() + 1;
			int n = token.length - 2;
			final char[] stringLitChars;

			// Delimiting double quot has been removed from s0.
			String s0 = String.copyValueOf(chars, startPos , n);
			// Dealing with escapes in the string literal.
			String s1 = s0.replace("\\r","\r");
			String s2 = s1.replace("\\n","\n");
			String s3 = s2.replace("\\f","\f");
			String s4 = s3.replace("\\b","\b");
			String s5 = s4.replace("\\t","\t");
			String s6 = s5.replace("\\'","\'");
			String s7 = s6.replace("\\\"","\"");
			String s8 = s7.replace("\\\\","\\");

			return s8;
		} else {
			return String.copyValueOf(chars, token.pos(), token.length);
		}
	}
	
	
	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}
	
	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	

	/**
	 * The list of tokens created by the scan method.
	 */
	private final ArrayList<Token> tokens = new ArrayList<Token>();

	private final char[] chars; //holds characters with 0 at the end.

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	static final char EOFchar = 0;

	Scanner(String inputString) {
		/* IMPLEMENT THIS */
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1);
		// input char array terminated with EOFchar for convenience
		chars[numChars] = EOFchar;
	}

	
	public Scanner scan() throws LexicalException {
		/* IMPLEMENT THIS */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		int startPos = pos;
		int startPosInLine = posInLine;
//		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));

		State state = State.START;
		//initialization
		while (pos < chars.length){ // read all chars
			char ch = chars[pos]; // get current char
			switch (state) {
				case START -> {
					startPos = pos;
					switch (ch){
						case ' ', '\t', '\f' -> {
							pos++;
							posInLine++;
						}
						case '\n' -> {// NEED TO HANDLE \r
							pos++;
							line++;
							posInLine = 1; //Problem in start code! posInLine here should be 1 instead of 0.
						}
						case '\r' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_CR;
						}
						case '0' -> {
							tokens.add(new Token(Kind.INTLIT, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '(' -> {
							tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case ')' -> {
							tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '[' -> {
							tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case ']' -> {
							tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case ';' -> {
							tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case ',' -> {
							tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '<' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_LT;
						}
						case '>' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_GT;
						}
						case '!' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_EXCL;
						}
						case '?' -> {
							tokens.add(new Token(Kind.Q, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case ':' -> {
							tokens.add(new Token(Kind.COLON, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '=' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_EQUAL;
						}
						case '+' -> {
							tokens.add(new Token(Kind.PLUS, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '-' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_MINUS;
						}
						case '*' -> {
							tokens.add(new Token(Kind.STAR, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '/' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_SLASH;
						}
						case '%' -> {
							tokens.add(new Token(Kind.MOD, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '@' -> {
							tokens.add(new Token(Kind.AT, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '#' -> {
							tokens.add(new Token(Kind.HASH, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '&' -> {
							tokens.add(new Token(Kind.AND, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '|' -> {
							tokens.add(new Token(Kind.OR, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						case '"' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_DOUBLE_QUOT;
						}
						case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.DIGITS;
						}
						default -> { //a..z, A..Z, _, $
							startPosInLine = posInLine;
							if (Character.isJavaIdentifierStart(ch)){
								pos++;
								posInLine++;
								state = State.IDENT_PART;
							}
							else{
								if(ch != EOFchar){
									throw new LexicalException("Line: " + line + ", posInLine: " + posInLine + ", Illegal character: " + ch, pos);
								}
								pos++;
								posInLine++; // Problem in start code! posInLine should increase as well.
							}
						}
					}
				}
				case ESCAPE -> {
					switch (ch) {
						case 'b', 't', 'n', 'f', 'r', '"', '\'', '\\' -> {
							pos++;
							posInLine++;
							state = State.HAVE_DOUBLE_QUOT;
						}
						default -> {
							throw new LexicalException("Illegal character (\\, but not an EscapeSequence) is found in the StingLit", pos);
						}
					}
				}
				case HAVE_DOUBLE_QUOT -> {
					switch (ch) {
						case '"' -> {
							tokens.add(new Token(Kind.STRINGLIT, startPos, 1 + pos - startPos, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						case '\\' -> {
							pos++;
							posInLine++;
							state = State.ESCAPE;
						}
						case '\n', '\r' -> {
							throw new LexicalException("Illegal character (CR or CF) is found in the StingLit", pos);
						}
						case EOFchar -> {
							throw new LexicalException("Failed to close a string literal", pos);
						}
						default -> {
							pos++;
							posInLine++;
						}
					}
				}
				case HAVE_SLASH -> {
					switch (ch) {
						case '/' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.COMMENT;
						}
						default -> {
							tokens.add(new Token(Kind.DIV, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case COMMENT -> {
					switch (ch) {
						case '\n' -> {
							pos++;
							line++;
							posInLine = 1;
							state = State.START;
						}
						case '\r' -> {
							startPosInLine = posInLine;
							pos++;
							posInLine++;
							state = State.HAVE_CR;
						}
						default -> {
							pos++;
							posInLine++;
						}
					}
				}
				case HAVE_CR -> {
					switch (ch) {
						case '\n' -> {
							pos++;
							line++;
							posInLine = 1;
							state = State.START;
						}
						default -> {
							line++;
							posInLine = 1;
							state = State.START;
						}
					}
				}
				case HAVE_EQUAL -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.EQ, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						default -> {
							tokens.add(new Token(Kind.ASSIGN, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case HAVE_LT -> {
					switch (ch) {
						case '<' -> {
							tokens.add(new Token(Kind.LPIXEL, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						case '=' -> {
							tokens.add(new Token(Kind.LE, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						case '-' -> {
							tokens.add(new Token(Kind.LARROW, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						default -> {
							tokens.add(new Token(Kind.LT, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case HAVE_GT -> {
					switch (ch) {
						case '>' -> {
							tokens.add(new Token(Kind.RPIXEL, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						case '=' -> {
							tokens.add(new Token(Kind.GE, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						default -> {
							tokens.add(new Token(Kind.GT, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case HAVE_MINUS -> {
					switch (ch) {
						case '>' -> {
							tokens.add(new Token(Kind.RARROW, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						default -> {
							tokens.add(new Token(Kind.MINUS, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case HAVE_EXCL -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.NEQ, startPos, 2, line, startPosInLine));
							pos++;
							posInLine++;
							state = State.START;
						}
						default -> {
							tokens.add(new Token(Kind.EXCL, startPos, 1, line, startPosInLine));
							state = State.START;
						}
					}
				}
				case DIGITS -> {
					switch (ch) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							pos++;
							posInLine++;
						}

						default -> {
							Token t = new Token(Kind.INTLIT, startPos, pos - startPos, line, startPosInLine);
							tokens.add(t);
							String tokenText = getText(t);
							try {
								Integer.parseInt(tokenText);
							}
							catch (NumberFormatException e) {
								throw new LexicalException("The integer literal provided is out of the range of a Java int", t.pos());
							}
							state = State.START;
						}
					}
				}
				case IDENT_PART -> {
					System.out.println("IDENT_PART CASE");
					Set <Character> digitCharSet = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
					if (Character.isJavaIdentifierStart(ch)){
						pos++;
						posInLine++;
					} else if (digitCharSet.contains(ch)){
						System.out.println("Digit in IDENT_PART: " + ch);
						pos++;
						posInLine++;
					} else {
						String pendingIDENT = String.copyValueOf(chars, startPos, pos - startPos);
						switch (pendingIDENT) {
							case "X" -> {
								tokens.add(new Token(Kind.KW_X, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "Y" -> {
								tokens.add(new Token(Kind.KW_Y, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "width" -> {
								tokens.add(new Token(Kind.KW_WIDTH, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "height" -> {
								tokens.add(new Token(Kind.KW_HEIGHT, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "screen" -> {
								tokens.add(new Token(Kind.KW_SCREEN, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "screen_width" -> {
								tokens.add(new Token(Kind.KW_SCREEN_WIDTH, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "screen_height" -> {
								tokens.add(new Token(Kind.KW_SCREEN_HEIGHT, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "image" -> {
								tokens.add(new Token(Kind.KW_image, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "int" -> {
								tokens.add(new Token(Kind.KW_int, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "string" -> {
								tokens.add(new Token(Kind.KW_string, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "red" -> {
								tokens.add(new Token(Kind.KW_RED, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "green" -> {
								tokens.add(new Token(Kind.KW_GREEN, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "blue" -> {
								tokens.add(new Token(Kind.KW_BLUE, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							case "Z", "WHITE", "SILVER", "GRAY", "BLACK", "RED", "MAROON", "YELLOW", "OLIVE", "LIME",
									"GREEN", "AQUA", "TEAL", "BLUE", "NAVY", "FUCHSIA", "PURPLE" -> {
								tokens.add(new Token(Kind.CONST, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
							default -> {
								tokens.add(new Token(Kind.IDENT, startPos, pos - startPos, line, startPosInLine));
								state = State.START;
							}
						}
					}
				}
			}
		}//while
		// add an EOF token
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;
	}


	

	/**
	 * precondition:  This Token is an INTLIT or CONST
	 * @throws LexicalException 
	 * 
	 * @returns the integer value represented by the token
	 */
	public int intVal(Token t) throws LexicalException {
		/* IMPLEMENT THIS */
		String tokenText = getText(t);
		if (t.kind == Kind.INTLIT) {
			try {
				return Integer.parseInt(tokenText);
			}
			catch (NumberFormatException e) {
				throw new LexicalException("The integer literal provided is out of the range of a Java int", t.pos());
			}
		}else if (t.kind == Kind.CONST){
			return constants.get(tokenText);
		}
		else{
			throw new LexicalException("This token's kind is: " + t.kind + ", which should be INTLIT or CONST instead.", t.pos());
		}
	}
	
	/**
	 * Hashmap containing the values of the predefined colors.
	 * Included for your convenience.  
	 * 
	 */
	private static HashMap<String, Integer> constants;
	static {
		constants = new HashMap<String, Integer>();	
		constants.put("Z", 255);
		constants.put("WHITE", 0xffffffff);
		constants.put("SILVER", 0xffc0c0c0);
		constants.put("GRAY", 0xff808080);
		constants.put("BLACK", 0xff000000);
		constants.put("RED", 0xffff0000);
		constants.put("MAROON", 0xff800000);
		constants.put("YELLOW", 0xffffff00);
		constants.put("OLIVE", 0xff808000);
		constants.put("LIME", 0xff00ff00);
		constants.put("GREEN", 0xff008000);
		constants.put("AQUA", 0xff00ffff);
		constants.put("TEAL", 0xff008080);
		constants.put("BLUE", 0xff0000ff);
		constants.put("NAVY", 0xff000080);
		constants.put("FUCHSIA", 0xffff00ff);
		constants.put("PURPLE", 0xff800080);
	}
	
	/**
	 * Returns a String representation of the list of Tokens.
	 * You may modify this as desired. 
	 */
	public String toString() {
		return tokens.toString();
	}
}
