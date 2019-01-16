/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

public class ObjectCreation extends AuxFactor {
	
	public ObjectCreation(ClassDec c) {
		this.type = c;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	    
	private Type type;

}
