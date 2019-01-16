/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

public class NullExpr extends Factor {
    
   public Type getType() {
      return Type.nilType;
   }
   
}