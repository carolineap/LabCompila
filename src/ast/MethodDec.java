package ast;
import java.util.ArrayList;

public class MethodDec extends Member {
	
	public MethodDec(String id, ArrayList<Param> paramList, Type returnType, ArrayList<Statement> statList) {
		this.id = id;
		this.paramList = paramList;
		this.returnType = returnType;
		this.statList = statList;
	}
	
	private String id;
	private ArrayList<Param> paramList;
	private Type returnType;
	private ArrayList<Statement> statList;
	
}
