package ast;
import lexer.Token;
import java.util.ArrayList;

public class FieldDec extends Member {
	
	public FieldDec(Variable field) {
		this.field = field;
	}
	
	public String getFieldName() {
		return field.getName();
	}
	
	public Type getType() {
		return field.getType();
	}
	
	public Token getQualifier() {
		return field.getQualifier();
	}
	
	private Variable field;

}
