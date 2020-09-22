package cop5556fa20;

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import org.junit.jupiter.api.Test;

import static cop5556fa20.Scanner.Kind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@SuppressWarnings("preview")

class SimpleScannerTest {

	
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Scanner.Token t = scanner.nextToken();
		Token expected = new Token(kind,pos,length,line,pos_in_line);
		assertEquals(expected, t);
		return t;
	}
	

	Token checkNext(Scanner scanner, Scanner.Kind kind) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind());
		return t;
	}
	

	Token checkNext(Scanner scanner, Scanner.Kind kind, int val) throws Exception {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind());
		assertEquals(val, scanner.intVal(t));
		return t;
	}
	
	Token checkNext(Scanner scanner, Scanner.Kind kind, String text)  {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind());
		assertEquals(text, scanner.getText(t));
		return t;
	}

	Token checkNextIsEOF(Scanner scanner) {
		Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind());
		assertFalse(scanner.hasTokens());
		return token;
	}
	
	@Test
	public void test0() throws Exception {

		String input = """
				abc
				   123
				   *2+==
				0a0
				01
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner,IDENT,"abc");
		checkNext(scanner, INTLIT,123);
		checkNext(scanner,STAR);
		checkNext(scanner, INTLIT,2);
		checkNext(scanner,PLUS);
		checkNext(scanner,EQ);
		checkNext(scanner,INTLIT,0);
		checkNext(scanner,IDENT,"a0");
		checkNext(scanner,INTLIT,0);
		checkNext(scanner,INTLIT,1);
		
		
				
	}
	
	@Test
	public void test1() throws LexicalException {
		String input = """
				abc
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner,IDENT);
				
	}
	


}
