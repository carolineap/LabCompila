package ast;

public class ReadExpr extends AuxFactor{

	public ReadExpr(Type readType) {
		this.readType = readType;
	}

	public Type getType() {
		//System.out.println(this.readType);
		return this.readType;
	}
	
	private Type readType;
	
}
