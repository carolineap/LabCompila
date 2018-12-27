package ast;

public class Variable {

	public Variable(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Type getType() {
		return this.type;
	}

	private String name;
	private Type type;
	
}
