package cop5556fa20;

import cop5556fa20.AST.*;
import cop5556fa20.Scanner.Token;

import java.util.Arrays;
import java.util.List;

import static cop5556fa20.Scanner.Kind;
import static cop5556fa20.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

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

	SymbolTable st = new SymbolTable();

	public TypeCheckVisitor() throws Scanner.LexicalException {
		super();
		// TODO Auto-generated constructor stub
		Scanner scanner = new Scanner("XY").scan();
		Token tX = scanner.nextToken();
		Expression eX = new ExprVar(tX, "X");
		Dec decX = new DecVar(tX, Type.Int, "X", eX);
		st.addDec("X", decX);

		Token tY = scanner.nextToken();
		Expression eY = new ExprVar(tX, "Y");
		Dec decY = new DecVar(tX, Type.Int, "Y", eX);
		st.addDec("Y", decX);
	}

	/**
	 * First visit method that is called.  It simply visits its children and returns null if no type errors were encountered.
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		st.enterScope();
		show("{");
		for(ASTNode node: program.decOrStatement()) {
			show("--------------");
			node.visit(this, arg);
		}
		st.closeScope();
		show("}");
		return null;
	}

	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		// TODO Auto-generated method stub
		show("decVar.name(): " + decVar.name());
		show("decVar.type(): " + decVar.type());
		if (st.duplicate(decVar.name())) {
			String message = "Duplicate declaration.";
			throw new TypeException(decVar.first(), message);
		}

		decVar.expression().setDefaultType(List.of(decVar.type()));

		show("decVar.expression().type(): " +decVar.expression().type());

		decVar.expression().setType((Type)decVar.expression().visit(this, arg));

		show("decVar.expression().type(): " +decVar.expression().type());

		show("decVar.expression(): " + decVar.expression());
		show("decVar.type(): " + decVar.type());
		if (decVar.expression() instanceof ExprEmpty || decVar.type() == decVar.expression().type()) {
			show("Adding the decVar to the symbol table");
			st.addDec(decVar.name(), decVar);
			return null;
		} else {
				String message = "Incompatible declaration type.";
				throw new TypeException(decVar.first(), message);
			}
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		// TODO Auto-generated method stub
		show("decVar.name(): " + decImage.name());
		if (st.duplicate(decImage.name())) {
			String message = "Duplicate declaration.";
			throw new TypeException(decImage.first(), message);
		}

		decImage.width().setDefaultType(List.of(Type.Int, Type.Void));
		decImage.height().setDefaultType(List.of(Type.Int, Type.Void));

		if (decImage.op() == LARROW) {
			decImage.source().setDefaultType(List.of(Type.String));
		}
		if (decImage.op() == ASSIGN) {
			decImage.source().setDefaultType(List.of(Type.Image));
		}

		decImage.width().setType((Type)decImage.width().visit(this, arg));
		decImage.height().setType((Type)decImage.height().visit(this, arg));
		decImage.source().setType((Type)decImage.source().visit(this, arg));

		show("decVar.width(): " + decImage.width());
		show("decVar.width(): " + decImage.width().type());
		show("decVar.height(): " + decImage.height());
		show("decVar.height(): " + decImage.height().type());
		show("decVar.op(): " + decImage.op());
		show("decVar.source(): " + decImage.source());
		show("decVar.source(): " + decImage.source().type());
		show("decVar.type(): " + decImage.type());

		boolean EmptyWidth = decImage.width() instanceof ExprEmpty;
		boolean EmptyHeight = decImage.height() instanceof ExprEmpty;

		boolean constraint_1 = decImage.width().type() == decImage.height().type();
		boolean constraint_2 = decImage.width().type() == Type.Int || decImage.width().type() == Type.Void;
		boolean constraint_3 = decImage.height().type() == Type.Int || decImage.height().type() == Type.Void;

		boolean constraint_4;

		show("decImage.source().type(): " + decImage.source().type());

		if (decImage.op() == LARROW && decImage.source().type() == Type.String) {
			constraint_4 = true;
		}else if (decImage.op() == ASSIGN && decImage.source().type() == Type.Image){
			constraint_4 = true;
		}else if(decImage.op() == NOP){
			constraint_4 = true;
		}else {
			constraint_4 = false;
		}

		if (EmptyWidth && EmptyHeight && constraint_4 || constraint_1 && constraint_2 && constraint_3 && constraint_4){
			st.addDec(decImage.name(), decImage);
			return null;
		}else{
			String message = "Incompatible declaration type.";
			throw new TypeException(decImage.first(), message);
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementOutFile.name();
		Dec statementOutFileDec = st.lookup(name);

		statementOutFile.filename().setDefaultType(List.of(Type.String));
		Type filenameT = (Type) statementOutFile.filename().visit(this, arg);

		show("name: " + name);
		show("filenameT: " + filenameT);
		show("statementOutFileDec: " + statementOutFileDec);

		if (statementOutFileDec == null) {
			String message = "statementOutScreen.name() should be declared before use.";
			throw new TypeException(statementOutFile.first(), message);
		} else if (statementOutFileDec.type() == Type.Image && filenameT == Type.String){
			statementOutFile.setDec(statementOutFileDec);
			return null;
		}

		String message = "Incompatible types for statementOutFile.";
		throw new TypeException(statementOutFile.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementOutScreen.name();
		Dec statementOutScreenDec = st.lookup(name);

		if (statementOutScreenDec == null) {
			String message = "statementOutScreen.name() should be declared before use.";
			throw new TypeException(statementOutScreen.first(), message);
		}

		if(statementOutScreenDec.type() == Type.Int || statementOutScreenDec.type() == Type.String) {
			statementOutScreen.X().setDefaultType(List.of(Type.Void));
			statementOutScreen.Y().setDefaultType(List.of(Type.Void));
		} else if (statementOutScreenDec.type() == Type.Image) {
			statementOutScreen.X().setDefaultType(List.of(Type.Int, Type.Void));
			statementOutScreen.Y().setDefaultType(List.of(Type.Int, Type.Void));
		}

		Type exT = (Type) statementOutScreen.X().visit(this, arg);
		Type eyT = (Type) statementOutScreen.Y().visit(this, arg);



		show("name: " + name);
		show("exT" + exT);
		show("eyT: " + eyT);
		boolean statementOutScreenConstraint = false;

		if (exT == eyT){
			if (statementOutScreenDec.type() == Type.Int || statementOutScreenDec.type() == Type.String) {
				if (exT == Type.Void){
					statementOutScreenConstraint = true;
				}
			} else if (statementOutScreenDec.type() == Type.Image) {
				if (exT == Type.Int || exT == Type.Void) {
					statementOutScreenConstraint = true;
				}
			}
		}

		if (statementOutScreenConstraint) {
			statementOutScreen.setDec(statementOutScreenDec);
			return null;
		}

		String message = "Incompatible types for statementOutScreen.";
		throw new TypeException(statementOutScreen.first(), message);

//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementImageIn.name();
		Dec statementImageInDec = st.lookup(name);

		statementImageIn.source().setDefaultType(List.of(Type.Image, Type.String));
		Type sourceT = (Type) statementImageIn.source().visit(this, arg);

		show("name: " + name);
		show("statementImageIn.source(): " + statementImageIn.source());
		show("sourceT: " + sourceT);

		if (statementImageInDec == null) {
			String message = "statementOutScreen.name() should be declared before use.";
			throw new TypeException(statementImageIn.first(), message);
		} else if (statementImageInDec.type() == Type.Image && (sourceT == Type.Image || sourceT == Type.String)){
			statementImageIn.setDec(statementImageInDec);
			return null;
		}

		String message = "Incompatible types for statementImageIn.";
		throw new TypeException(statementImageIn.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementAssign.name();
		Dec statementAssignDec = st.lookup(name);

		if (statementAssignDec == null) {
			String message = "statementAssign.name() should be declared before use.";
			throw new TypeException(statementAssign.first(), message);
		}

		statementAssign.expression().setDefaultType(List.of(statementAssignDec.type()));
		Type expressionT = (Type) statementAssign.expression().visit(this, arg);

		show("name: " + name);
		show("statementAssign.expression(): " + statementAssign.expression());
		show("sourceT: " + expressionT);

		if (statementAssignDec.type() == expressionT){
			statementAssign.setDec(statementAssignDec);
			return null;
		}

		String message = "Incompatible types for statementAssign.";
		throw new TypeException(statementAssign.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = statementLoop.name();
		Dec statementLoopDec = st.lookup(name);

		statementLoop.cond().setDefaultType(List.of(Type.Void, Type.Boolean));
		statementLoop.e().setDefaultType(List.of(Type.Int));
		Type condT = (Type) statementLoop.cond().visit(this, arg);
		Type eT = (Type) statementLoop.e().visit(this, arg);

		show("name: " + name);
		show("statementLoop.cond(): " + statementLoop.cond());
		show("statementLoop.e(): " + statementLoop.e());
		show("condT: " + condT);
		show("eT: " + eT);

		if (statementLoopDec == null) {
			String message = "statementAssign.name() should be declared before use.";
			throw new TypeException(statementLoop.first(), message);
		} else if (statementLoopDec.type() == Type.Image &&
				(condT == Type.Boolean || condT == Type.Void) &&
				eT == Type.Int){
			statementLoop.setDec(statementLoopDec);
			return null;
		}

		String message = "Incompatible types for statementLoop.";
		throw new TypeException(statementLoop.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub

		exprConditional.condition().setDefaultType(List.of(Type.Boolean));
		if (exprConditional.trueCase() instanceof ExprArg && exprConditional.falseCase() instanceof ExprArg) {
			exprConditional.trueCase().setDefaultType(List.of(Type.Int, Type.String));
			exprConditional.falseCase().setDefaultType(List.of(Type.Int, Type.String));
		} else if (exprConditional.trueCase() instanceof ExprArg) {
			exprConditional.trueCase().setDefaultType(List.of((Type) exprConditional.falseCase().visit(this, arg)));
		} else if (exprConditional.falseCase() instanceof ExprArg) {
			exprConditional.falseCase().setDefaultType(List.of((Type) exprConditional.trueCase().visit(this, arg)));
		}

//		if (exprConditional.condition() instanceof ExprArg
//				|| exprConditional.trueCase() instanceof ExprArg
//				|| exprConditional.falseCase() instanceof ExprArg) {
//			String message = "One of the arguments is instance of ExprArg. Which is not allowed in ExprConditional. " +
//					"Because one of the arguments is type of Boolean.";
//			throw new TypeException(exprConditional.first(), message);
//		}

		Type conditionT = (Type) exprConditional.condition().visit(this, arg);
		Type trueCaseT = (Type) exprConditional.trueCase().visit(this, arg);
		Type falseCaseT = (Type) exprConditional.falseCase().visit(this, arg);

		show("exprConditional.condition(): " + exprConditional.condition());
		show("exprConditional.trueCase(): " + exprConditional.trueCase());
		show("exprConditional.falseCase(): " + exprConditional.falseCase());
		show("conditionT: " + conditionT);
		show("trueCaseT: " + trueCaseT);
		show("falseCaseT: " + falseCaseT);

		if (conditionT !=  Type.Boolean) {
			String message = "conditionT type should be Boolean.";
			throw new TypeException(exprConditional.first(), message);
		}

		if (trueCaseT !=  falseCaseT) {
			String message = "exprConditional has incompatible types to compare.";
			throw new TypeException(exprConditional.first(), message);
		}

		exprConditional.setType(trueCaseT);
		return exprConditional.type();
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind op = exprBinary.op();

		Type e0T = null;
		Type e1T = null;

		// OrExpr, AndExpr
		if (op == AND || op == OR) {
			exprBinary.e0().setDefaultType(List.of(Type.Boolean));
			exprBinary.e1().setDefaultType(List.of(Type.Boolean));
			e0T = (Type) exprBinary.e0().visit(this, arg);
			e1T = (Type) exprBinary.e1().visit(this, arg);
			if (e0T == Type.Boolean && e1T == Type.Boolean) {
				exprBinary.setType(Type.Boolean);
				return exprBinary.type();
			}
		}


		// EqExpr
		if (op == EQ ||op == NEQ) {
			exprBinary.e0().setDefaultType(List.of(Type.Boolean));
			exprBinary.e1().setDefaultType(List.of(Type.Boolean));
			e0T = (Type) exprBinary.e0().visit(this, arg);
			e1T = (Type) exprBinary.e1().visit(this, arg);
			if (e0T == e1T) {
				exprBinary.setType(Type.Boolean);
				return exprBinary.type();
			}
		}

		// RelExpr
		List<Kind> relExprOpType = Arrays.asList(LT, GT, LE, GE);
		if (relExprOpType.contains(op)) {
			exprBinary.e0().setDefaultType(List.of(Type.Boolean));
			exprBinary.e1().setDefaultType(List.of(Type.Boolean));
			e0T = (Type) exprBinary.e0().visit(this, arg);
			e1T = (Type) exprBinary.e1().visit(this, arg);

			if (e0T == e1T && e0T == Type.Int){
				exprBinary.setType(Type.Boolean);
				return exprBinary.type();
			}
		}

		// AndExpr
		if (op == PLUS) {
			List<Type> expectContextTList = exprBinary.defaultType();
//			show ("Plus expectContextTList: " + expectContextTList);
//			if (Set.of(Type.Int, Type.String).contains(new HashSet<>(expectContextTList))) {
//				show ("True");
//				exprBinary.e0().setDefaultType(expectContextTList);
//			} else {
//				show("False");
//				exprBinary.e0().setDefaultType(List.of(Type.Int, Type.String));
//			}
			exprBinary.e0().setDefaultType(expectContextTList);
			e0T = (Type) exprBinary.e0().visit(this, arg);
			exprBinary.e1().setDefaultType(List.of(e0T));
			e1T = (Type) exprBinary.e1().visit(this, arg);


			if (e0T == e1T && (e0T == Type.Int || e1T == Type.String)) {
				exprBinary.setType(e0T);
				return exprBinary.type();
			}
		} else if(op == MINUS) {
			exprBinary.e0().setDefaultType(List.of(Type.Int));
			e0T = (Type) exprBinary.e0().visit(this, arg);
			exprBinary.e1().setDefaultType(List.of(e0T));
			e1T = (Type) exprBinary.e1().visit(this, arg);

			if (e0T == e1T && (e0T == Type.Int)) {
				exprBinary.setType(e0T);
				return exprBinary.type();
			}
		}

		// MultExpr
		if (op == STAR || op == DIV || op == MOD) {
			exprBinary.e0().setDefaultType(List.of(Type.Int));
			e0T = (Type) exprBinary.e0().visit(this, arg);
			exprBinary.e1().setDefaultType(List.of(e0T));
			e1T = (Type) exprBinary.e1().visit(this, arg);

			if (e0T == e1T && e0T == Type.Int) {
				exprBinary.setType(Type.Int);
				return exprBinary.type();
			}
		}

		String message = "Incompatible type for expressionBinary.  e0: " + e0T
				+ "e1: " + e1T;
		throw new TypeException(exprBinary.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind op = exprUnary.op();

		if (op == PLUS || op == MINUS){
			exprUnary.e().setDefaultType(List.of(Type.Int));
		}else if (op == EXCL) {
			exprUnary.e().setDefaultType(List.of(Type.Boolean));
		}

		Type eT = (Type) exprUnary.e().visit(this, arg);

		if ((op == PLUS || op == MINUS) && eT == Type.Int) {
			exprUnary.setType(Type.Int);
			return exprUnary.type();
		}

		if (op == EXCL && eT == Type.Boolean){
			exprUnary.setType(Type.Boolean);
			return exprUnary.type();
		}

		String message = "Wrong UnaryExpr type. Expecting Int, actual: " + eT;
		throw new TypeException(exprUnary.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		// TODO Auto-generated method stub
		exprHash.e().setDefaultType(List.of(Type.Int, Type.Image));
		Type eT = (Type) exprHash.e().visit(this, arg);
		String attr = exprHash.attr();

		boolean hashExprConstraint = false;
		List<String> hashExprIntAttr = Arrays.asList("red", "green", "blue");
		List<String> hashExprImageAttr = Arrays.asList("width", "height");
		if (eT == Type.Int || eT == Type.Image){
			if (eT == Type.Int && hashExprIntAttr.contains(attr)) {
				hashExprConstraint = true;
			} else if (eT == Type.Image && hashExprImageAttr.contains(attr)){
				hashExprConstraint = true;
			}
		}
		if (hashExprConstraint) {
			exprHash.setType(Type.Int);
			return exprHash.type();
		}

		String message = "Wrong ExprHash type. Got eT: " + eT + "and attr: " + attr;
		throw new TypeException(exprHash.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		// TODO Auto-generated method stub
		exprIntLit.setType(Type.Int);
		return exprIntLit.type();
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = exprVar.name();
		show("name :" + name);

		Dec ExprVarDec = st.lookup(name);
		if (ExprVarDec == null) {
			String message = "exprVar.name() should be declared before use.";
			throw new TypeException(exprVar.first(), message);
		} else {
			exprVar.setType(ExprVarDec.type());
			return exprVar.type();
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {
		// TODO Auto-generated method stub
		exprStringLit.setType(Type.String);
		return exprStringLit.type();
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		// TODO Auto-generated method stub
		exprConst.setType(Type.Int);
		return exprConst.type();
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if (exprPixelSelector.image() instanceof ExprArg
				|| exprPixelSelector.X() instanceof ExprArg
				|| exprPixelSelector.Y() instanceof ExprArg) {
			String message = "One of the arguments is instance of ExprArg. Which is not allowed in ExprPixelSelector. " +
					"Because one of the arguments is type of image.";
			throw new TypeException(exprPixelSelector.first(), message);
		}

		show("exprPixelSelector.image(): " + exprPixelSelector.image());
		show("exprPixelSelector.X(): " + exprPixelSelector.X());
		show("exprPixelSelector.Y(): " + exprPixelSelector.Y());

		exprPixelSelector.image().setDefaultType(List.of(Type.Image));
		exprPixelSelector.X().setDefaultType(List.of(Type.Int));
		exprPixelSelector.Y().setDefaultType(List.of(Type.Int));
//		if (exprPixelSelector.X() instanceof ExprArg && exprPixelSelector.Y() instanceof ExprArg) {
//			exprPixelSelector.X().setDefaultType(List.of(Type.Int));
//			exprPixelSelector.Y().setDefaultType(List.of(Type.Int));
//		} else if (exprPixelSelector.X() instanceof ExprArg) {
//			exprPixelSelector.X().setDefaultType(List.of((Type) exprPixelSelector.Y().visit(this, arg)));
//		} else if (exprPixelSelector.Y() instanceof ExprArg) {
//			exprPixelSelector.Y().setDefaultType(List.of((Type) exprPixelSelector.X().visit(this, arg)));
//		}

		Type imageT = (Type) exprPixelSelector.image().visit(this, arg);
		Type exT = (Type) exprPixelSelector.X().visit(this, arg);
		Type eyT = (Type) exprPixelSelector.Y().visit(this, arg);

		show("imageT: " + imageT);
		show("exT: " + exT);
		show("eyT: " + eyT);

		if (imageT == Type.Image && exT == Type.Int && eyT == Type.Int) {
			exprPixelSelector.setType(Type.Int);
			return exprPixelSelector.type();
		}

		String message = "Wrong ExprPixelSelector type. Got imageT: " + imageT + ", exT: " + exT + ", eyT: " + eyT;
		throw new TypeException(exprPixelSelector.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		// TODO Auto-generated method stub
		exprPixelConstructor.redExpr().setDefaultType(List.of(Type.Int));
		exprPixelConstructor.greenExpr().setDefaultType(List.of(Type.Int));
		exprPixelConstructor.blueExpr().setDefaultType(List.of(Type.Int));
		Type redExprT = (Type) exprPixelConstructor.redExpr().visit(this, arg);
		Type greenExprT = (Type) exprPixelConstructor.greenExpr().visit(this, arg);
		Type blueExprT = (Type) exprPixelConstructor.blueExpr().visit(this, arg);

		if (redExprT == Type.Int && greenExprT == Type.Int && blueExprT == Type.Int) {
			exprPixelConstructor.setType(Type.Int);
			return exprPixelConstructor.type();
		}

		String message = "Wrong ExprPixelConstructor type. Got redExprT: " + redExprT + ", greenExprT: " + greenExprT + ", blueExprT: " + blueExprT
				+ ". Which should all be the type of Int.";
		throw new TypeException(exprPixelConstructor.first(), message);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type eT = (Type) exprArg.e().visit(this, arg);
		List<Type> expectContextTList = exprArg.defaultType();

		show("expectContextTList: " + expectContextTList);

		Type expectContextT;
		if (expectContextTList.contains(Type.String)) {
			expectContextT = Type.String;
		} else if (expectContextTList.contains(Type.Int)) {
			expectContextT = Type.Int;
		} else {
			String message = "Expected context type of ExprArg should be Int or String. Got : " + expectContextTList;
			throw new TypeException(exprArg.first(), message);
		}

		show("expectContextT: " + expectContextT);

		if (eT == Type.Int) {
			exprArg.setType(expectContextT);
			return exprArg.type();

		} else {
			String message = "ExprArg.e().type() should be Int. Got : " + eT;
			throw new TypeException(exprArg.first(), message);
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return Type.Void;
//		throw new UnsupportedOperationException();
	}
}
