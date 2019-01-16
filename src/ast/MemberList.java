/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;
import lexer.Token;

public class MemberList {

	public MemberList(Member member, Token qualifier) {
		this.member = member;
		this.qualifier = qualifier;
	}
	
	public Member getMember() {
		return this.member;
	}
	
	public Token getQualifier() {
		return this.qualifier;
	}
	
	private Member member;
	private Token qualifier;
	
}
