/**
 * This code was developed for the class project in COP5556 Programming Language Principles 
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

import cop5556fa20.AST.Type;
import cop5556fa20.AST.*;
import cop5556fa20.runtime.LoggedIO;
import org.objectweb.asm.*;

import java.util.List;

import static cop5556fa20.Scanner.Kind;

public class CodeGen5 implements ASTVisitor, Opcodes {
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	final String className;
	final boolean isInterface = false;
	ClassWriter cw;
	MethodVisitor mv;
	
	public CodeGen5(String className) {
		super();
		this.className = className;
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		cw = new ClassWriter(0); //If the call to methodVisitor.visitMaxs crashes, it
		// is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.

		// String sourceFileName = className; //TODO Temporary solution, FIX THIS
		int version = -65478;
		cw.visit(version, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(null, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// insert label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// visit children to add instructions to method
		List<ASTNode> nodes = program.decOrStatement();
		for (ASTNode node : nodes) {
			node.visit(this, null);
		}
		// add  required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		// handles parameters and local variables of main. The only local var is args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not pass verification, but you will at least be
		// able to see what instructions it contains.
		mv.visitMaxs(0, 0);

		// finish construction of main method
		mv.visitEnd();

		// finish class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();

	}


	/**
	 * Add a static field to the class for this variable.
	 */
	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		String varName = decVar.name();
		Type type = decVar.type();
		String desc;

		if (type == Type.String) {desc = "Ljava/lang/String;";}
		else if (type == Type.Int) {desc = "I";}
		else {throw new UnsupportedOperationException("visitDecVar can only be type of Sting or Int. Received: " + type);}

		FieldVisitor fieldVisitor = cw.visitField(ACC_STATIC, varName, desc, null, null);
		fieldVisitor.visitEnd();

		//evaluate initial value and store in variable, if one is given.
		Expression e = decVar.expression();
		if (e != Expression.empty) {
			e.visit(this, type); // generates code to evaluate expression and leave value on top of the stack
//			mv.visitLdcInsn(e.visit(this, type));

			mv.visitFieldInsn(PUTSTATIC, className, varName, desc);
		}
		return null;
	}
	
	
	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementAssign.name();
		Dec dec = statementAssign.dec();
		Type type = dec.type();
		String desc;

		switch (type) {
			case String -> {
				desc = "Ljava/lang/String;";
			}
			case Int -> {
				//IMPLEMENT THIS FOR ASSIGNMENT 5
				desc = "I";

//				throw new UnsupportedOperationException("not yet implemented");
			}
			default -> throw new UnsupportedOperationException("not yet implemented");
		}

		mv.visitFieldInsn(GETSTATIC, className, name, desc);

		//evaluate initial value and store in variable
		Expression e = statementAssign.expression();
		if (e != Expression.empty) {
			e.visit(this, type); // generates code to evaluate expression and leave value on top of the stack
			mv.visitFieldInsn(PUTSTATIC, className, name, desc);
		}
		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		String name = statementOutScreen.name();
		Expression e0 = statementOutScreen.X();
		Expression e1 = statementOutScreen.Y();
		Type type0 = e0.type();
		Type type1 = e1.type();

		Dec dec = statementOutScreen.dec();
		Type type = dec.type();
		String desc;
		switch (type) {
			case String -> {
				desc = "Ljava/lang/String;";

				mv.visitFieldInsn(GETSTATIC, className, name, desc);
				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "stringToScreen", LoggedIO.stringToScreenSig,
						isInterface);
				//mv.visitJumpInsn();
			}
			case Int -> {
				//IMPLEMENT THIS FOR ASSIGNMENT 5
				desc = "I";

				mv.visitFieldInsn(GETSTATIC, className, name, desc);
				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig,
						isInterface);
//				throw new UnsupportedOperationException("not yet implemented");
			}
			case Image -> {
				throw new UnsupportedOperationException("not yet implemented");
			}
			default -> throw new UnsupportedOperationException("not yet implemented");
		}


		return null;
	}

	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type type = exprArg.type();
		Expression e = exprArg.e();
		mv.visitVarInsn(ALOAD, 0);
		e.visit(this, arg);
		mv.visitInsn(AALOAD);

		if (type == Type.Int) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",
					isInterface);
		}

//		String desc;
//		switch (type) {
//			case String -> {
//				desc = "Ljava/lang/String;";
//
//				mv.visitFieldInsn(GETSTATIC, className, name, desc);
//				//mv.visitJumpInsn();
//			}
//			case Int -> {
//				//IMPLEMENT THIS FOR ASSIGNMENT 5
//				desc = "I";
//
//				mv.visitFieldInsn(GETSTATIC, className, name, desc);
//				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig,
//						isInterface);
////				throw new UnsupportedOperationException("not yet implemented");
//			}
//			default -> throw new UnsupportedOperationException("not yet implemented");
//		}

		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression eCon = exprConditional.condition();
		Expression eTrue = exprConditional.trueCase();
		Expression eFalse =  exprConditional.falseCase();

		Label start = new Label();
		Label startTrue = new Label();
		Label startFalse = new Label();
		Label end = new Label();

		mv.visitLabel(start);
		eCon.visit(this, arg);
		mv.visitJumpInsn(IFNE, startTrue);

		mv.visitLabel(startFalse);
		eFalse.visit(this, arg);

		Label endFalse = new Label();
		mv.visitLabel(endFalse);
		mv.visitJumpInsn(GOTO, end); //GOTO END

		mv.visitLabel(startTrue);
		eTrue.visit(this, arg);
		Label endTrue = new Label();
		mv.visitLabel(endTrue);

		mv.visitLabel(end);
		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type type = exprBinary.type();
		Kind op = exprBinary.op();
		Expression e0 = exprBinary.e0();
		Expression e1 = exprBinary.e1();
		Type type0 = e0.type();
		Type type1 = e1.type();

		Label setTrue = new Label();
		Label endEB = new Label();

		if (type0 == Type.Int) {
			switch (op) {
				case PLUS -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IADD);
				}
				case MINUS -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(ISUB);
				}
				case STAR -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IMUL);
				}
				case DIV -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IDIV);
				}
				case MOD -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IREM);
				}
				case EQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
				}
				case NEQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
				}
				case GE -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
				}
				case GT -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
				}
				case LE -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
				}
				case LT -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
				}
				default -> throw new UnsupportedOperationException("not yet implemented");
			}
			mv.visitJumpInsn(GOTO, endEB);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endEB);

		} else if (type0 == Type.Boolean) {
			switch (op) {
				case AND -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IAND);
				}
				case OR -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitInsn(IOR);
				}
				case EQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
				}
				case NEQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
				}

				default-> throw new UnsupportedOperationException("not yet implemented");
			}
			mv.visitJumpInsn(GOTO, endEB);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endEB);
		} else if (type0 == Type.String) {
			switch (op) {
				case PLUS -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
//					mv.visitInsn(IAND);
					mv.visitMethodInsn(INVOKESTATIC, CodeGenUtils.className, "stringConcatenation",
							"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
				}
				case EQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ACMPEQ, setTrue);
					mv.visitLdcInsn(false);
				}
				case NEQ -> {
					e0.visit(this, arg);
					e1.visit(this, arg);
					mv.visitJumpInsn(IF_ACMPNE, setTrue);
					mv.visitLdcInsn(false);
				}

				default-> throw new UnsupportedOperationException("not yet implemented");
			}
			mv.visitJumpInsn(GOTO, endEB);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endEB);
		}

		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind op = exprUnary.op();

		Expression e = exprUnary.e();

		if (e != Expression.empty) {
			e.visit(this, arg); // generates code to evaluate expression and leave value on top of the stack
		}

		switch (op) {
			case PLUS -> {
//				int val1 = (Integer) e.visit(this, null);
//				e.setVal(val1);
//				mv.visitLdcInsn(e.val());
			}
			case MINUS -> {
//				int val1 = (Integer) e.visit(this, null);
//				e.setVal(-1 * val1);
//				mv.visitLdcInsn(e.val());

				mv.visitLdcInsn(-1);
				mv.visitInsn(IMUL);
				}
			case EXCL -> {
//				boolean val1 = (Boolean) e.visit(this,null);
//				e.setVal(val1); //update attribute

				Label originalT = new Label();
				Label originalF = new Label();

				mv.visitLdcInsn(true);
				mv.visitJumpInsn(IF_ICMPEQ, originalT);
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, originalF);
				mv.visitLabel(originalT);
				mv.visitLdcInsn(false);
				mv.visitLabel(originalF);
			}
			default -> throw new UnsupportedOperationException("not yet implemented");
		}

//		return e.val();
		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(exprIntLit.value());
		return exprIntLit.value();
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = exprVar.name();
		Type type = exprVar.type();

		String desc;
		switch (type) {
			case String -> {
				desc = "Ljava/lang/String;";
			}
			case Int -> {
				//IMPLEMENT THIS FOR ASSIGNMENT 5
				desc = "I";
//				throw new UnsupportedOperationException("not yet implemented");
			}
			default -> throw new UnsupportedOperationException("not yet implemented");
		}

		mv.visitFieldInsn(GETSTATIC, className, name, desc);


		return null;

//		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * generate code to put the value of the StringLit on the stack.
	 */
	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {

//		exprStringLit.setVal(exprStringLit.text());
//		return exprStringLit.val();
		mv.visitLdcInsn(exprStringLit.text());
		return null;
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		// TODO Auto-generated method stub
//		exprConst.setVal(exprConst.value());
//		return exprConst.value();
		mv.visitLdcInsn(exprConst.value());
		return null;
//		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

}
