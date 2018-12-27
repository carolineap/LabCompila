package ast;

import java.util.ArrayList;

public class ClassDec {
	
	public ClassDec(String name, String parent, ArrayList<MemberList> memberList, boolean isInheritable) {
		this.name = name;
		this.parent = parent;
		this.memberList = memberList;
		this.isInheritable = isInheritable;
	}
	
	private String name;
	private String parent;
	private ArrayList<MemberList> memberList;
	private boolean isInheritable;

}
