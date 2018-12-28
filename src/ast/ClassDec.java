package ast;

import java.util.ArrayList;

import lexer.Token;

public class ClassDec extends Type {
	
	public ClassDec(String name, ClassDec parent, boolean isInheritable) {
		super(name);
		this.name = name;
		this.parent = parent;
		this.isInheritable = isInheritable;
	}
	
	public String getName() {
		return name;
	}
	
	public ClassDec getParent() {
		return this.parent;
	}
	
	public boolean getInheritable() {
		return this.isInheritable;
	}
	
	public ArrayList<MethodDec> getMethods() {
		
				
		ArrayList<MethodDec> methods = new ArrayList<>();
			
		for (Member m: memberList) {
		
			if (m instanceof MethodDec) {
				MethodDec method = (MethodDec) m;
				if (method.getQualifier() == Token.PUBLIC)	
					methods.add(method);
			}
			
		}
		
		return methods;
		
	}
	
	public ArrayList<FieldDec> getFields() {
		
		
		ArrayList<FieldDec> fields = new ArrayList<>();
			
		for (Member f: memberList) {
		
			if (f instanceof FieldDec) {
				FieldDec field = (FieldDec) f;
				if (field.getQualifier() == Token.PUBLIC)	
					fields.add(field);
			}
			
		}
		
		return fields;
		
	}
	
	public void setMemberList(ArrayList<Member> memberList) {
		this.memberList = memberList;
	}
	
	public ArrayList<Member> getMembers() {
		return this.memberList;
	}
	
	private String name;
	private ClassDec parent;
	private ArrayList<Member> memberList;
	private boolean isInheritable;

}
