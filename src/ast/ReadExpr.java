package ast;

public class ReadExpr extends AuxFactor{

	public ReadExpr(String readType) {
		this.readType = readType;
	}

	private String readType;
	
}
