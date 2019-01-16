/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;
import lexer.*;

public class Qualifier {
	private Token token1;
	private Token token2;
	private Token token3;
	
	public Qualifier(Token t1, Token t2, Token t3) {
		this.token1 = t1;
		this.token2 = t2;
		this.token2 = t3;
	}
	
	public Token getToken1() {
		return this.token1;
	}
	public Token getToken2() {
		return this.token2;
	}
	public Token getToken3() {
		return this.token3;
	}
	public boolean override() {
		if(this.token1 == Token.OVERRIDE || this.token2 == Token.OVERRIDE ) {
			return true;
		}
		return false;
	}
	
}
