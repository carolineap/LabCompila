package ast;
import lexer.Token;
import java.util.ArrayList;

public class FieldDec extends Member {
	
	public FieldDec(Type type, ArrayList<Token> idList) {
		this.type = type;
		this.idList = idList;
	}
	
	private Type type;
	private ArrayList<Token> idList;

}
