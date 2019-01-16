/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;
import java.util.ArrayList;

public class RepeatStat extends Statement {
	
	public RepeatStat(ArrayList<Statement> statList, Expr e) {
		this.statList = statList;
		this.e = e;
	}
	
	private ArrayList<Statement> statList;
	private Expr e;
	
}
