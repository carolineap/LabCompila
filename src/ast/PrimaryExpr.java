package ast;
import java.util.ArrayList;
public class PrimaryExpr extends AuxFactor {
	
	public PrimaryExpr(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return this.type;
	}
	
	private Type type;

}
