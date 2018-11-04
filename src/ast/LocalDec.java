package ast;
import lexer.Token;

import java.util.ArrayList;

public class LocalDec extends Statement {
	
	public LocalDec(Type type, ArrayList<Token> idList, Expr e) {
		this.type = type;
		this.idList = idList;
		this.expr = e;
		
	}

	private ArrayList<Token> idList;
	private Type type;
	private Expr expr;
}
