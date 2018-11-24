package ast;
import java.util.ArrayList;
public class PrimaryExpr extends AuxFactor {
	
	public PrimaryExpr(String tipo) {
		this.tipo = tipo;
	}
	
	public PrimaryExpr(String tipo, String id) {
		this.tipo = tipo;
		this.id1 = id;
	}
	public PrimaryExpr(String tipo, String idColum, ArrayList<Expr> exprList ) {
		this.tipo = tipo;
		this.exprList = exprList;
		this.idColum = idColum;
	}
	public PrimaryExpr(String tipo, String id, String id2) {
		this.tipo = tipo;
		this.id1 = id;
		this.id2 = id2;
	}
	public PrimaryExpr(String tipo, String id, String idColum, ArrayList<Expr> exprList ) {
		this.tipo = tipo;
		this.id1 = id;
		this.exprList = exprList;
		this.idColum = idColum;
	}
	private String tipo;
	private String id1;
	private String id2;
	private String idColum;
	private ArrayList<Expr> exprList;

}
