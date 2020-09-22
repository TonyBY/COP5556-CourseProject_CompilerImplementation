package cop5556fa20;

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("preview")

class ScannerStringExamples {
	
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	char[] stringChars(String s){
		 return Arrays.copyOf(s.toCharArray(), s.length()); // input string terminated with null char
	}
	
	void showChars(char[] chars){
		System.out.println("index\tascii\tcharacter");
		for(int i = 0; i < chars.length; ++i) {
			char ch = chars[i];
			System.out.println(i + "\t" + (int)ch + "\t" + ch);
		}
	}
	
	/**
	 * Both instances of \\n get passed to scanner as \ n.  This yields an error because '\' is not
	 * legal outside of String literals.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void newline0() throws LexicalException {

		String input =  """
              \\n "Example\\nString" 
              """;
		System.out.println("\n\n\n***** newline0 *****");
		System.out.println("Input chars");
		showChars(stringChars(input));
		Scanner scanner = new Scanner(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
	
	
	/** 
	 * In this example, the first \n is converted by Java to the NL character (value = 10) and 
	 * treated as white space.
	 *  
	 * The \\n is passed as \ n.  
	 * 
	 * When getText is called to get the text of the STRINGLIT token, the \ n 
	 * is converted to the NL character.
	 * 
	 * the String literal 
	 * @throws LexicalException
	 */

	@Test
	public void newline1() throws LexicalException {
		String input =  """
              \n "Example\\nString" 
              """;	
		System.out.println("\n\n\n***** newline1 *****");
		System.out.println("Input chars");
		showChars(stringChars(input));
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Token t = scanner.nextToken();
		assertEquals(Scanner.Kind.STRINGLIT, t.kind());
		String text = scanner.getText(t);
		System.out.println("Token text");
		
		showChars(stringChars(text));
	}
	
	
	/**
	 * Both \n are converted by Java to NL before being passed to the 
	 * Scanner.  
	 * 
	 * This is an error since it has a NL in a String literal which is
	 * not allowed. 
	 * 
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void newline2() throws LexicalException {
		String input =  """
              \n "Example\nString" 
              """;	
		System.out.println("\n\n\n***** newline2 *****");
		System.out.println("Input chars");
		showChars(stringChars(input));
		Scanner scanner = new Scanner(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
}


/*

***** newline0 *****
Input chars
index	ascii	character
0	92	\
1	110	n
2	32	 
3	34	"
4	69	E
5	120	x
6	97	a
7	109	m
8	112	p
9	108	l
10	101	e
11	92	\
12	110	n
13	83	S
14	116	t
15	114	r
16	105	i
17	110	n
18	103	g
19	34	"
20	10	

cop5556fa20.Scanner$LexicalException: 1:1 unexpected character  ch= \ at pos 0



***** newline1 *****
Input chars
index	ascii	character
0	10	

1	32	 
2	34	"
3	69	E
4	120	x
5	97	a
6	109	m
7	112	p
8	108	l
9	101	e
10	92	\
11	110	n
12	83	S
13	116	t
14	114	r
15	105	i
16	110	n
17	103	g
18	34	"
19	10	

Token text
index	ascii	character
0	69	E
1	120	x
2	97	a
3	109	m
4	112	p
5	108	l
6	101	e
7	10	

8	83	S
9	116	t
10	114	r
11	105	i
12	110	n
13	103	g



***** newline2 *****
Input chars
index	ascii	character
0	10	

1	32	 
2	34	"
3	69	E
4	120	x
5	97	a
6	109	m
7	112	p
8	108	l
9	101	e
10	10	

11	83	S
12	116	t
13	114	r
14	105	i
15	110	n
16	103	g
17	34	"
18	10	

cop5556fa20.Scanner$LexicalException: 2:10 illegal line termination character in String literal
*/
