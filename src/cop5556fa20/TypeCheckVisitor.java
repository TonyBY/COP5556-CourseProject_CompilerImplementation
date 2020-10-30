package cop5556fa20;

import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.ASTVisitor;
import cop5556fa20.AST.DecImage;
import cop5556fa20.AST.DecVar;
import cop5556fa20.AST.ExprArg;
import cop5556fa20.AST.ExprBinary;
import cop5556fa20.AST.ExprConditional;
import cop5556fa20.AST.ExprConst;
import cop5556fa20.AST.ExprEmpty;
import cop5556fa20.AST.ExprHash;
import cop5556fa20.AST.ExprIntLit;
import cop5556fa20.AST.ExprPixelConstructor;
import cop5556fa20.AST.ExprPixelSelector;
import cop5556fa20.AST.ExprStringLit;
import cop5556fa20.AST.ExprUnary;
import cop5556fa20.AST.ExprVar;
import cop5556fa20.AST.Program;
import cop5556fa20.AST.StatementAssign;
import cop5556fa20.AST.StatementImageIn;
import cop5556fa20.AST.StatementLoop;
import cop5556fa20.AST.StatementOutFile;
import cop5556fa20.AST.StatementOutScreen;
import cop5556fa20.Scanner.Token;

public class TypeCheckVisitor implements ASTVisitor {
	


	@SuppressWarnings("serial")
	class TypeException extends Exception {
		Token first;
		String message;
		
		public TypeException(Token first, String message) {
			super();
			this.first = first;
			this.message = "Semantic error:  "+first.line() + ":" + first.posInLine() + " " +message;
		}
		
		public String toString() {
			return message;
		}	
	}
	
	
	public TypeCheckVisitor() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	/**
	 * First visit method that is called.  It simply visits its children and returns null if no type errors were encountered.  
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for(ASTNode node: program.decOrStatement()) {
			node.visit(this, arg);
		}
		return null;
	}
	

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
