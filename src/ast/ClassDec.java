package ast;

import java.util.ArrayList;

public class ClassDec {
	
	public ClassDec(String name, String parent, ArrayList<MemberList> memberList) {
		this.name = name;
		this.parent = parent;
		this.memberList = memberList;
	}
	
	private String name;
	private String parent;
	private ArrayList<MemberList> memberList;

}
