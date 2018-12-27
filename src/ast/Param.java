package ast;

public class Param {

	public Param(Type type, String name) {
		this.type = type;
		this.name = name;
	}
	
	
	public Type getType() {
		return type;
	}
	public String getName() {
		return name;
	}

	private Type type;
	private String name;
	
}
