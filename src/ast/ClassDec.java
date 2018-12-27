package ast;

import java.util.ArrayList;

public class ClassDec {
	
	public ClassDec(String name, String parent, ArrayList<Member> memberList, boolean isInheritable) {
		this.name = name;
		this.parent = parent;
		this.memberList = memberList;
		this.isInheritable = isInheritable;
	}
	
	public String getName() {
		return name;
	}
	
	public String getParentName() {
		return this.parent;
	}
	
	public ArrayList<MethodDec> getMethods() {
		
				
		ArrayList<MethodDec> methods = new ArrayList<>();
	
		for (Member m: memberList) {
			
			if (m instanceof MethodDec) {
				methods.add((MethodDec) m);
			}
			
		}
		
		
		return methods;
		
	}
	
	private String name;
	private String parent;
	private ArrayList<Member> memberList;
	private boolean isInheritable;

}
