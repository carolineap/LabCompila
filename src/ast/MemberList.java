package ast;
import lexer.Token;

public class MemberList {

	public MemberList(Member member, Token qualifier) {
		this.member = member;
		this.qualifier = qualifier;
	}
	
	private Member member;
	private Token qualifier;
	
}
