package ast;
import lexer.Token;

public class Variable extends Member {

	public Variable(String name, Type type) {
		this.name = name;
		this.type = type;
		this.qualifier = Token.PUBLIC;
	}
	
	public Variable(String name, Type type, Token q) {
		this.name = name;
		this.type = type;
		this.qualifier = q;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public Token getQualifier() {
		return this.qualifier;
	}

	private Token qualifier;
	private String name;
	private Type type;
	
}
