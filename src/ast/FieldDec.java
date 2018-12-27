package ast;
import lexer.Token;
import java.util.ArrayList;

public class FieldDec extends Member {
	
	public FieldDec(Type type, ArrayList<Variable> idList, Token qualifier) {
		this.type = type;
		this.idList = idList;
		this.qualifier = qualifier;
	}
	
	private Type type;
	private ArrayList<Variable> idList;
	private Token qualifier;

}
