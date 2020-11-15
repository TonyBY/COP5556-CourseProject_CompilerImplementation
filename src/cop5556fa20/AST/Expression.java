/**
 * Code for the class project in COP5556 Programming Language Principles 
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

package cop5556fa20.AST;

import cop5556fa20.Scanner.Token;

import java.util.ArrayList;
import java.util.List;

public abstract class Expression extends ASTNode {
	
	public static final ExprEmpty empty = ExprEmpty.empty;
	
	Type type;  //NOT final.  Will be set during type checking in most cases
	List<Type> defaultType;
	Object val;

	public Expression(Token first) {
		super(first);
		this.type = null;
		this.defaultType = new ArrayList<>();
		this.val = null;
	}

	public Type type() {
		return type;
	}
	public List<Type> defaultType() {return defaultType;}
	public Object val() {return val;}

	public void setType(Type type) {
		this.type = type;
	}
	public void setDefaultType(List<Type> defaultType) {
		this.defaultType = defaultType;
	}
	public void setVal(Object val) {this.val = val;}



}
