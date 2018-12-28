package ast;

public class ObjectCreation extends AuxFactor {
	
	public ObjectCreation(ClassDec c) {
		this.type = new TypeClass(c.getName());
	}
	
	@Override
	public Type getType() {
		return type;
	}
	    
	private Type type;

}
