/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.omg.CORBA.Current;

import com.sun.xml.internal.ws.api.pipe.Fiber;

import ast.*;
import lexer.Lexer;
import lexer.Token;

public class Compiler {

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		// Program ::= CianetoClass { CianetoClass }
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<ClassDec> CianetoClassList = new ArrayList<>();
		Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		haveRun = false;
		while ( lexer.token == Token.CLASS ||
				(lexer.token == Token.ID && lexer.getStringValue().equals("open") ) ||
				lexer.token == Token.ANNOT ) {
			try {
				while ( lexer.token == Token.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				
				//classDec();
				CianetoClassList.add(classDec());
				
			}
			catch( CompilerError e) {
				// if there was an exception, there is a compilation error
				thereWasAnError = true;
				while ( lexer.token != Token.CLASS && lexer.token != Token.EOF ) {
					try {
						next();
					}
					catch ( RuntimeException ee ) {
						e.printStackTrace();
						return program;
					}
				}
			}
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
			}

		}
		if ( !thereWasAnError && lexer.token != Token.EOF ) {
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}
		
		if ( !thereWasAnError && haveRun == false) {
			try {
				error("Every program must have a class named 'Program' with a public parameterless method called 'run'");
			} catch( CompilerError e) {
			}
		}

		
		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * @cep(5, "'class' expected") <br>
     * class Program <br>
     *     func run { } <br>
     * end <br>
     * </code>
     *

	 */
	@SuppressWarnings("incomplete-switch")
	private void metaobjectAnnotation(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		String name = lexer.getMetaobjectName();
		int lineNumber = lexer.getLineNumber();
		lexer.nextToken();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		boolean getNextToken = false;
		if ( lexer.token == Token.LEFTPAR ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING ||
					lexer.token == Token.ID ) {
				switch ( lexer.token ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.getNumberValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.getLiteralStringValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.getStringValue());
				}
				lexer.nextToken();
				if ( lexer.token == Token.COMMA )
					lexer.nextToken();
				else
					break;
			}
			if ( lexer.token != Token.RIGHTPAR )
				error("')' expected after metaobject call with parameters");
			else {
				getNextToken = true;
			}
		}
		if ( name.equals("nce") ) {
			if ( metaobjectParamList.size() != 0 )
				error("Metaobject 'nce' does not take parameters");
		}
		else if ( name.equals("cep") ) {
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Metaobject 'cep' take three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of metaobject 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of metaobject 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
				error("The fourth parameter of metaobject 'cep' should be a literal string");

		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) lexer.nextToken();
	}

	private ClassDec classDec() {
		
		boolean isInheritable = false;
		String superclassName = null;
		ClassDec superclass = null;
		
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			isInheritable = true;
			lexer.nextToken();
		}
		if ( lexer.token != Token.CLASS ) error("'class' expected");
		lexer.nextToken();
		
		if ( lexer.token != Token.ID )
			error("Identifier expected");
		String className = lexer.getStringValue();
		
		if (symbolTable.getInGlobal(className) != null){
            error("Class " + className + " has already been declared");
        }
         
		lexer.nextToken();
		
		if ( lexer.token == Token.EXTENDS ) {
			lexer.nextToken();	
			if ( lexer.token != Token.ID )
				error("Identifier expected");
			superclassName = lexer.getStringValue();
			superclass = (ClassDec) symbolTable.getInGlobal(superclassName);
			if (superclass != null) {
				if (superclass.getInheritable() == false) {
					error("Superclass '" + superclassName + "' is not inheritable");
				}
			} else {
				error("The superclass '" + superclassName + "' does not exist");
			}
			ClassDec aux = superclass;
			do {
				ArrayList<Member> memberlist = aux.getMembers();
				for(int i = 0; i < memberlist.size(); i++) {
					Member m = memberlist.get(i);
					if(m instanceof MethodDec) {
						symbolTable.putInSuperClassTable(((MethodDec)m).getMethodName(), m);
					}
				}
				aux = aux.getParent();
			}while(aux != null);
			lexer.nextToken();
		}
		ClassDec classDec = new ClassDec(className, superclass, isInheritable); 
		
		currentClass = classDec;
       
		symbolTable.putInGlobal(className, classDec);

        ArrayList<Member> ml = memberList();
       
		currentClass.setMemberList(ml);
		
		if (ml == null || lexer.token != Token.END)
			error("Class member or 'end' expected");
		lexer.nextToken();
		
		if (currentClass.getName().equals("Program")) {
        	
			boolean flag  = false;
			
			for (Member m : ml) {
        		if (m instanceof MethodDec && ((MethodDec) m).getMethodName().equals("run")) {
        			flag = true;
        		}
        	}
        
        	if (flag == false) {
				error("Method 'run' was not found in class 'Program'");
			}
		
		}
		
		symbolTable.removeClassIdent();

		return classDec;
	}

	private ArrayList<Member> memberList() {
		
		ArrayList<Member> ml = new ArrayList<>();
		ArrayList<Variable> fieldList = null;
		
		while ( true ) {
			
			Qualifier q = qualifier();
			
			if ( lexer.token == Token.VAR) {
				
				fieldList = fieldDec(q);
				ml.addAll(fieldList);
			}
			else if ( lexer.token == Token.FUNC ) {
				ml.add(methodDec(q));
			}
			else {
				break;
			}
		}

		return ml;
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private void next() {
		lexer.nextToken();
	}

	private void check(Token shouldBe, String msg) {
		if ( lexer.token != shouldBe ) {
			error(msg);
		}
	}

	private MethodDec methodDec(Qualifier q) {
		haveReturn = false;
		String id = null;
		ArrayList<Variable> paramList = new ArrayList<>();
		Type returnType = null;
		ArrayList<Statement> statList = new ArrayList<>();
		
		lexer.nextToken();
		if ( lexer.token == Token.ID ) {
			// unary method
			id = lexer.getStringValue();
			lexer.nextToken();

		} else if ( lexer.token == Token.IDCOLON ) {	
			id = lexer.getStringValue();
			lexer.nextToken();
			paramList = parameterList();
			for (Variable p : paramList) {
                if (symbolTable.getInLocal(p.getName()) != null) {
                    error("variavel" + p.getName() + "' ja foi declarada");
                }
                symbolTable.putInLocal(p.getName(), p);
	        }
		} else {
			error("An identifier or identifer: was expected after 'func'");
		}
		
		Object obj = symbolTable.getInClass(id);
		
		if (obj != null) 
			error("Member '"+ id + "' has been redeclared");
		
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			returnType = type();
		}
		
		MethodDec method = (MethodDec) symbolTable.getInSuperClassTable(id);
		if(q.override()) {
			if(method == null) {
				error("The method '" + id +"' doesn't exist in superclass or the signature is different");	
			}else if(method.getQualifier().getToken1() == Token.FINAL) {
				error("The method in superclass is final, so it can not be override");
				
			}else if(paramList.size() != method.getParamList().size()) {
				error("The signature of the method is different from the signature of the superclass");
			
			}else if(returnType != method.getReturnType()) {
				error("The return type of '" + method.getMethodName() + "' is different from the superclass");
			}
			for(int i = 0; i < method.getParamList().size(); i++) {
				if(paramList.get(i).getType() != method.getParamList().get(i).getType()) {
					error("The signature of the method is different from the signature of the superclass");
				}
			}
		}else if(method != null) {
			error("The method '" + id + "' is defined in superclass and 'override' is missing");
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}

					
		MethodDec m = new MethodDec(id, paramList, returnType, q);
		
		if (currentClass.getName().equals("Program")) {
			if (m.getMethodName().equals("run") || m.getMethodName().equals("run:")) {
				if (m.getQualifier().getToken1() == Token.PUBLIC) {
					if (m.getParamList().isEmpty() == false || m.getReturnType() != null) {
						error("Class 'Program' must have a public, parameterless and no return method called 'run'");
					} else {
						haveRun = true;
					}
				} else {
					error("Method 'run' of class 'Program' must be a public method");
				}
			}
		} 
			
		next();
		
		currentMethod = m;
		
		symbolTable.putInClass(id, m);
		
		statList = statementList();
		
		currentMethod.setStatList(statList);
	
		
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
        if(returnType != null){
        	
            if(haveReturn == false){
                error("The function " + id + " has type " + returnType.getName() + " and does not have a return");
            }
        }
        
        next();
        symbolTable.removeLocalIdent();
        
		return m;
	}
	
	private ArrayList <Variable> parameterList() {
		
		ArrayList <Variable> paramList = new ArrayList<>();
		
		while (true) {	
			
			Type t = type();
						
			if (lexer.token != Token.ID) 
				error("Identifier expected");
			
			paramList.add(new Variable(lexer.getStringValue(), t));
			
			lexer.nextToken();
			
			if (lexer.token == Token.COMMA) 
				lexer.nextToken();
			else
				break;
		}
		
		return paramList;
		
	}

	private ArrayList<Statement> statementList() {
	
		ArrayList<Statement> listStmt = new ArrayList<>();
		  // only '}' is necessary in this test
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.UNTIL ) {
			listStmt.add(statement());
		}	
		return listStmt;
	}

	private Statement statement() {
		
		Statement stmt = null;
	
		boolean checkSemiColon = true;
		
		switch ( lexer.token ) {
		case IF:
			stmt =  ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			stmt =  whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			stmt =  returnStat();
			break;
		case BREAK:
			stmt =  breakStat();
			break;
		case SEMICOLON:
			break;
		case REPEAT:
			stmt =  repeatStat();
			break;
		case VAR:
			stmt =  localDec();
			break;
		case ASSERT:
			stmt = assertStat();
			break;
		default:
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
				stmt = writeStat();
			} else  {
				AssignExpr ae = assignExpr();
			} 
		}
				
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected");
			lexer.nextToken();
		}
		
		return stmt;
	}
	
	private AssignExpr assignExpr() {	
		AssignExpr a = null;
		isReadExpr = false;
		isMethod = null; //flag sendo usada para verificar se statement e metodo com retorno
		ehvalido = false; //flag sendo usada para verificar se o lado esquerdo do = eh is ou self.id
		Expr left = expr();
		if (left == null)
			error("Expression expected");
		//System.out.println(left.getType());
		Expr right = null;
		if (lexer.token == Token.ASSIGN) {
			next();
			right = expr();
			if (right == null)
				error("Expression expected");			
			if (!checkType(left.getType(), right.getType())) {
				if (left.getType() != null && right.getType() != null) {
					error("Type error: value of the right-hand side is not subtype of the variable of the left-hand side: cannot assign type " + right.getType().getName() + " to " + left.getType().getName());
				} else if (right.getType() == null){
					error("Expression expected in the right-hand side of assignment");
				} else {
					error("Type error: value of the right-hand side is not subtype of the variable of the left-hand side");	
				}
			}else if(ehvalido == false) {
				error("error on letf-hand side of assignment, expected a variable or self.variable");
			}
		} else {
			if (isMethod == null && isReadExpr == false) {
				error("Statement expected");
			} else {
				if (isMethod != null && isMethod.getReturnType() != null) {
					error("Method '" + isMethod.getMethodName() + "' returns a value that is not used");
				}
			}
		}
		
		return new AssignExpr(left, right);
	}

	private LocalDec localDec() {
		
		next();

		ArrayList<Variable> idList = new ArrayList<>();
		Type t = type();
		Expr e = null;
		
		check(Token.ID, "Identifier expected");
				
		while ( lexer.token == Token.ID ) {
			
			Variable v = new Variable(lexer.getStringValue(), t);
			
			if (symbolTable.getInLocal(v.getName()) != null) {
                error("Variable '" + v.getName() + "' is being redeclared");
            }
            symbolTable.putInLocal(v.getName(), v);
            idList.add(v);
            
			lexer.nextToken();
								
			if ( lexer.token == Token.COMMA ) {
				next();
				check(Token.ID, "Missing identifier");
			}
			else {
				break;
			}
		}
		
		if ( lexer.token == Token.ASSIGN ) {
			next();
			e = expr();
			if (e == null) {
				error("Assign expression expected");
			}
		}		
		
		return new LocalDec(t, idList, e);

	}

	private RepeatStat repeatStat() {
		next();
		ehLoop = true;
		ArrayList<Statement> statList = statementList();
		ehLoop = false;
		check(Token.UNTIL, "'until' was expected");
		next();
		
		Expr e = expr();
		
		if (e == null) {
			error("Repeat expression expected");
		}
		
		if (e.getType() != Type.booleanType) {
			error("Only boolean expressions are allowed in repeat statements");
		}
		
		return new RepeatStat(statList, e);
	}

	private BreakStat breakStat() {
		if(ehLoop == false) {
			error("'break' statement found outside a 'while' statement");
		}
		lexer.nextToken();
		return new BreakStat();
	}

	private ReturnStat returnStat() {
		next();
		Expr e = expr();
		//System.out.println(lexer.token);
		if (e == null) {
			error("Return expression expected");
		}
		if (currentMethod.getReturnType() == null) {
			error("Method '" + currentMethod.getMethodName() + "' does not have return");
		}
		if (checkType(currentMethod.getReturnType(), e.getType()) == false) {
			error("Return type and method type are differents");
		}
		
		haveReturn = true;
		
		return new ReturnStat(e);
	}

	private WhileStat whileStat() {
		//System.out.println(ehLoop);	
		next();
		boolean loopInsideLoop = false;
		Expr e = expr();
		if(ehLoop) { // se ehLopp ja for verdadeiro, quer dizer q eh loop dentro de um loop
			loopInsideLoop = true;
		}
		if (e == null) {
			error("While expression expected");
		}
		
		if (e.getType() != Type.booleanType) {
			error("Only boolean expressions are allowed in while statements");
		}
		
		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		ehLoop = true;
		ArrayList<Statement> statList = statementList();
		if(!loopInsideLoop) {
			ehLoop = false;
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected after the 'while' statement");
		
		next();
			
		return new WhileStat(e, statList);
	}

	private IfStat ifStat() {
		next();
		Expr e = expr();
		if (e == null) {
			error("If expression expected");
		}
		if (e.getType() != Type.booleanType) {
			error("Only boolean expressions are allowed in if statements");
		}
		
		ArrayList<Statement> ifPart = new ArrayList<>();
		ArrayList<Statement> elsePart = new ArrayList<>();
		
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			ifPart.add(statement());
		}
		
		check(Token.RIGHTCURBRACKET, "'}' was expected after the 'if' statement");
		next();
		
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				elsePart.add(statement());
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected after the 'else' statement");
			next();
		}
		return new IfStat(e, ifPart, elsePart);
	}

	private WriteStat writeStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		if(!printName.equals("print:") && !printName.equals("println:")) {
			this.error("'print:' or 'println:' was expected after 'Out.'");
		}
		next();
		Expr e = expr();
		if (e == null) {
			error("Write expression expected");
		}
		if (e.getType() != Type.intType && e.getType() != Type.stringType) {
			error("Type error on write expression: only int and string type are allowed");
		}
		return new WriteStat(e);
	}

	private Expr expr() {
		
		Expr left = simpleExpr();

		while (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT || 
			lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
	
			Token op = lexer.token;		
			
			lexer.nextToken();
			
			Expr right = simpleExpr();
			
			Type old = left.getType();
			
			left = new CompositeExpr(left, op, right);
		
			if (left.getType() == Type.undefType) { 
				error("Type error: cannot compare types " + old.getName() + " and " + right.getType().getName());
			}
			
		}
		
		return left;
	}
	
	private Expr simpleExpr() {
		Expr left = sumSubExpr();
				
		while (lexer.token == Token.CONCAT) {
			Token op = lexer.token;
			lexer.nextToken();	
			
			Expr right = sumSubExpr();
			
			Type old = left.getType();

			left = new CompositeSimpleExpr(left, op, right);
	
			if (left.getType() == Type.undefType) {
				error("Type error: cannot concat types " + old.getName() + " and " + right.getType().getName());
			}
			
		}
		return left;
	}
	
	private Expr sumSubExpr( ) {	
		Expr left = term();
		
		while (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
			Token op = lexer.token;
			lexer.nextToken();
			Expr right = term();
			Type old = left.getType();
			left = new CompositeSumSubExpr(left, op, right);
			if (left.getType() == Type.undefType) {
				error("Type error: cannot use operator " + op.toString() + " with "  + old.getName() + " and " + right.getType().getName());
			}
			
		}
		
		return left;
	}
	
	private Expr term() {
		
		Expr left = signalFactor();
				
		while (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
			Token op = lexer.token;
			lexer.nextToken();
			Expr right = signalFactor();
			Type old = left.getType();
			left = new CompositeTerm(left, op, right);
			
			if (left.getType() == Type.undefType) {
				error("Type error: cannot use operator " + op.toString() + " with "  + old.getName() + " and " + right.getType().getName());
			}
			
		}
		
		return left;
	}
	
	private SignalFactor signalFactor() {
		Token op = null;
	
		if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
			op = lexer.token;
			lexer.nextToken();
		}
		
		Factor right = factor();
		
		if (right == null)
			error("Expression expected");
		
		CompositeSignalFactor c = new CompositeSignalFactor(op, right);
		
		if (c.getType() == Type.undefType) {
			error("Type error: cannot use operator " + op.toString() + " as signal to " + right.getType().getName());
		}
		
		return c;
		
	}
	
	private Factor factor() {
		Expr e;
		ArrayList<Expr> eList;
		if (lexer.token == Token.LEFTPAR) {
			lexer.nextToken();
			e = expr();
			if (lexer.token != Token.RIGHTPAR) 
				error("') expected");
			if (e == null)
				return null;
			lexer.nextToken();
			return new ExprFactor(e);
		} 
		if (lexer.token == Token.NOT) {
			lexer.nextToken();
			Factor f = factor();
			if (f.getType() != Type.booleanType) {
				error("Only booleans are allowed with '!' operator");
			}
			return f;
		} 
		if (lexer.token == Token.NIL) {
			lexer.nextToken();
			return new NullExpr(); 
		} 
		if (lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING || lexer.token == Token.TRUE || lexer.token == Token.FALSE)
			return basicValue();
		
		if (lexer.token == Token.SUPER) {
			return auxSuper();
		} else if (lexer.token == Token.ID) { 
			return auxId();
		} else if (lexer.token == Token.SELF){
			return auxSelf();
		} else if (lexer.token == Token.IN) {
			return readExpr();
		}
		return null;
	}
	
	private AuxFactor auxSuper() {
		Type type = Type.undefType;
		
		if(lexer.token == Token.SUPER){
			if (currentClass.getParent() == null)
				error("Class '" + currentClass.getName() + "' does not have a parent");
			lexer.nextToken();
			if(lexer.token == Token.DOT) {
				lexer.nextToken();
				if(lexer.token == Token.ID) {
					String memberName = lexer.getStringValue();
					lexer.nextToken();
					Variable f = searchFields(currentClass.getParent(), memberName);
				
					if (f != null) {
						type = f.getType();
					} else {
						MethodDec m = searchMethod(currentClass.getParent(), memberName);
						if (m != null) {	
							if (m.getParamList().isEmpty() == false) {
								error("Method '" + memberName + "' has parameters");
							}
							type = m.getReturnType();
							isMethod = m;
						} else {
							error("Superclass of '" + currentClass.getName() + "' does not have a member called '" + memberName + "'");
						}
					}			
				} else if(lexer.token == Token.IDCOLON) {
					String methodName = lexer.getStringValue();
					lexer.nextToken();
					
					MethodDec m = searchMethod(currentClass.getParent(), methodName);

					if (m == null) {
						error("Superclass of '" + currentClass.getName() + "' does not have a method called '" + methodName + "'");
					}
					ArrayList<Expr> exprList = exprList();					
					checkParameters(exprList, m.getParamList(), methodName);
					type = m.getReturnType();
					isMethod = m;
				} else {
					error("id or idcolon expected");
				}
			} else {
				error("dot expected");
			}
	}
	if (type != Type.undefType) {
		return new PrimaryExpr(type);
	} else {
		return null;
	}
}

	private AuxFactor auxId() {
		Type type = Type.undefType;
		if(lexer.token == Token.ID) {
			String s = lexer.getStringValue();
			lexer.nextToken();
			if (lexer.token == Token.DOT) {
				lexer.nextToken();								
				if (lexer.token == Token.NEW) {
					
					lexer.nextToken();
					ClassDec c = (ClassDec) symbolTable.getInGlobal(s);
					if (c == null) {
						error("'" + s + "' is not a class");
					}
					return new ObjectCreation(c);
					
				} else if (lexer.token == Token.ID ) {
					String memberName = lexer.getStringValue();
					lexer.nextToken();
					Variable v = (Variable)symbolTable.getInClass(s);	
					
					if (v == null) {
						error("Variable '" + s + "' not declared");
					}
					ClassDec c = (ClassDec) symbolTable.getInGlobal(v.getType().getName());
					if (c != null) {
						Variable f = searchFields(c, memberName);
						if (f != null) {
							type = f.getType();
						} else {
							MethodDec m = searchMethod(c, memberName);
							if (m != null) {	
								if (m.getParamList().isEmpty() == false) {
									error("Method '" + memberName + "' has parameters");
								}
								type = m.getReturnType();
								isMethod = m;
							} else {
								error("Member '" + memberName + "' was not found in class '" + v.getType().getName() + "' or its superclasses");
							}
						}				
					} else {
						error("Type of '" + s + "' does not have members");
					}
				} else if (lexer.token == Token.IDCOLON) {
					
					String methodName = lexer.getStringValue();
					lexer.nextToken();	
					Variable v = (Variable) symbolTable.getInClass(s);	
					
					if (v == null) {
						error("'" + s + "' not declared");
					}
					
					ClassDec c = (ClassDec) symbolTable.getInGlobal(v.getType().getName());
					
					if (c != null) {
						MethodDec m = searchMethod(c, methodName);
						if (m == null) {
							error("Method '" + methodName + "' was not found in class '" + v.getType().getName() + "' or its superclasses");
						}						
						ArrayList<Expr> exprList = exprList();					
						checkParameters(exprList, m.getParamList(), methodName);
						
						type = m.getReturnType();
						isMethod = m;	
					} else {
						error("Type of '" + s + "' does not have members");
					}
				} else {
					error("id or idcolon expected");
				}
			} else {
				Object obj = (Object)symbolTable.getInClass(s);
				if (obj instanceof Variable) {
				
					Variable v = (Variable) symbolTable.getInClass(s);		
					Variable v1 = (Variable) symbolTable.getInLocal(s);	
					if (v == null) {
						error("Variable '" + s + "' not declared");
					}else if (v.getQualifier().getToken1() != Token.PUBLIC){
						error("self."+ s +" expected");
					}	
					if(v1 == null) {
						error("Variable '" + s + "' not declared");
					}
					
					type = v.getType();
					ehvalido = true;
				} else {
					error("'" + s + "' not declared");
				}	
			}
		} 
		if (type != Type.undefType) {
			return new PrimaryExpr(type);
		} else {
			return null;
		}
	}
	
	private AuxFactor auxSelf() {

		Type type = Type.undefType;
		ArrayList<Expr> exprList = new ArrayList<>();
		MethodDec method = null;
		Member member = null;
		String methodName = null;
		if(lexer.token == Token.SELF) {
			lexer.nextToken();
			if(lexer.token == Token.DOT) {
				lexer.nextToken();
				if(lexer.token == Token.ID) {
					String memberName = lexer.getStringValue();
					member = (Member) symbolTable.getInClass(memberName);
					lexer.nextToken();
					if(lexer.token == Token.DOT) {
						Object obj = symbolTable.getInClass(memberName);
						if(obj == null || (obj instanceof Variable) == false) {
							error("Variable '" + memberName + "' does not exist or was not declared");
						}
						Variable v = (Variable) obj;
						lexer.nextToken();
						if(lexer.token == Token.ID) {
							methodName = lexer.getStringValue();
							lexer.nextToken();
						
						}else if(lexer.token == Token.IDCOLON) {
							methodName = lexer.getStringValue();
							next();
							exprList = exprList();	
						}else{
							error("id or idcolon expected");
						}
						ClassDec classe = (ClassDec) symbolTable.getInGlobal(v.getType().getName());
						method = searchMethod(classe, methodName);
						
						if(method == null) {
							error("The method '" + memberName +"' doesn't exist in superclass or the signature is different");
						}else if(exprList.size() != method.getParamList().size()) {
							error("The signature of the method is different from the signature of the superclass");
							
						}
						for(int i = 0; i < method.getParamList().size(); i++) {
							if(exprList.get(i).getType() != method.getParamList().get(i).getType()) {
								error("The signature of the method is different from the signature of the superclass");
							}
						}
						type = method.getReturnType();
						isMethod = method;
					}else{	
						Variable f = searchFields(currentClass, memberName);
				
						if (f != null) {
							type = f.getType();
							ehvalido = true;
						} else {
							MethodDec m = searchMethod(currentClass, memberName);
							if (m != null) {	
								if (m.getParamList().isEmpty() == false) {
									error("Method '" + memberName + "' has parameters");
								}
								type = m.getReturnType();
								isMethod = m;

							} else {
								error("Class '" + currentClass.getName() + "' does not have a member called '" + memberName + "'");
							}
						}				
					}
				} else if(lexer.token == Token.IDCOLON) {
					
					methodName = lexer.getStringValue();		
					lexer.nextToken();
					MethodDec m = null;
					m = (MethodDec)symbolTable.getInClass(methodName); 
					if (m == null) {
						m = searchMethod(currentClass.getParent(), methodName);
					}
					if (m == null) {
						error("Superclasses or class '" + currentClass.getName() + "' does not have the method '" + methodName + "'");
					}
					exprList = exprList();
					checkParameters(exprList, m.getParamList(), methodName);
					type = m.getReturnType();
					isMethod = m;

				}else {
					error("id or idcolon expected");
				}
			} else {
				type = currentClass;
			}
		}
		
		if (type != Type.undefType) {
			return new PrimaryExpr(type);
		} else {
			return null;
		}
		
	}
	private ReadExpr readExpr() {
		
		lexer.nextToken();
		if(lexer.token == Token.DOT) {
			lexer.nextToken();
			if (lexer.token == Token.ID && (lexer.getStringValue().equals("readInt") || lexer.getStringValue().equals("readString"))) {
				isReadExpr = true;
				if (lexer.getStringValue().equals("readInt")) {
					lexer.nextToken();
					return new ReadExpr(Type.intType);
				} else {
					lexer.nextToken();
					return new ReadExpr(Type.stringType);
				}
			} else {
				error("readInt or readString expected");
			}
		}else {
			error("dot expected");
		}
		return null;
		
	}
	private BasicValue basicValue() {
		
		if (lexer.token == Token.LITERALSTRING) {
			String s = lexer.getLiteralStringValue();
			next();
			return new BasicValue(s);
		}
		
		if (lexer.token == Token.LITERALINT) {
			Integer i = lexer.getNumberValue();
			next();
			return new BasicValue(i);
		}
		
		if (lexer.token == Token.TRUE) {
			boolean b = true;
			next();
			return new BasicValue(b);
		}			
		if (lexer.token == Token.FALSE) {
			boolean b = false;
			next();
			return new BasicValue(b);
		}
		
		error("Basic Value expected");
		return null;
		
	}
	
	private ArrayList<Expr> exprList() {
		ArrayList<Expr> exprList = new ArrayList<>();
		Expr e = null;
		e = expr();
		if (e == null)
			error("Expression expected");
			
		exprList.add(e);
		while (lexer.token == Token.COMMA) {
			lexer.nextToken();
			e = expr();
			if (e == null)
				error("Expression expected");
			exprList.add(e);
		}

		return exprList;
	}

	private ArrayList<Variable> fieldDec(Qualifier q) {
		
		lexer.nextToken();
		Type type = type();
		ArrayList<Variable> fieldList = new ArrayList<>();
		
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected");
		}
		else {
			while (true) {				
				if (lexer.token != Token.ID) 
					error("Missing identifier");
				
				//System.out.println(q);
				if(q.getToken1() != Token.PRIVATE) {
					error("Attempt to declare public instance variable '" + lexer.getStringValue() + "'");
				}
				Variable v = new Variable(lexer.getStringValue(), type, q);
				
				if (symbolTable.getInClass(v.getName()) != null) {
	                error("Variable '" + v.getName() + "' is being redeclared");
	            }
				
	            symbolTable.putInClass(v.getName(), v);
				fieldList.add(v);
				next();
				if ( lexer.token == Token.COMMA ) {
					lexer.nextToken();
				} else {
					break;
				} 
			}
			
			if (lexer.token == Token.SEMICOLON)
				lexer.nextToken();
		}
		return fieldList;
	}

	private Type type() {
		Type type = null;
		
		if (lexer.token == Token.INT) {
			type = Type.intType;
			next();
		} else if ( lexer.token == Token.BOOLEAN) {
			type = Type.booleanType;
			next();
		} else if ( lexer.token == Token.STRING ) {
			type = Type.stringType;
			next();
		} else if ( lexer.token == Token.ID ) {
			if (symbolTable.getInGlobal(lexer.getStringValue()) == null){
	            error("Invalid type '" + lexer.getStringValue() + "'");
	        }
			ClassDec c = (ClassDec) symbolTable.getInGlobal(lexer.getStringValue());
			if (c != null) {
				type = c;
			} else {
				error("Class '" + lexer.getStringValue() + "' does not exist");
			}
			next();
		} else {
			this.error("A type was expected");
		}

		return type;
	}


	private Qualifier qualifier() {
		
		Token q = lexer.token;
		Token q2 = null;
		Token q3 = null;
		if ( lexer.token == Token.PRIVATE ) {
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				q2 = Token.PUBLIC;
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {	
				next();
				q2 = Token.PUBLIC;
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				q2 = Token.OVERRIDE;
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
				q3 = Token.PUBLIC;
			}
		} else if(q == Token.VAR){
			q = Token.PRIVATE;
			
		}else {
			q = Token.PUBLIC;
		}
		return new Qualifier (q, q2, q3);
	}

	private AssertStat assertStat() {
		int lineNumber = lexer.getLineNumber();
		next();
		Expr e = expr();
		if (e == null) {
			error("Assert expression expected");
		}
		if (e.getType() != Type.booleanType) {
			error("Assert expression must be boolean");
		}
		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		next();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getLiteralStringValue();
		next();
		return new AssertStat(e, message);
	}

	private LiteralInt literalInt() {
		LiteralInt e = null;
		int value = lexer.getNumberValue();
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private boolean checkType(Type type1, Type type2) {
		if (type1 == null || type2 == null)
			return false;
		if (type1 == type2) {
			return true;
		} 
		if ((type1 instanceof ClassDec && type2 == Type.nilType) || (type2 instanceof ClassDec && type1 == Type.nilType)) {
			return true;
		}
		if (type1.getClass() == type2.getClass()) {
			
			if (type1 instanceof ClassDec) {
				ClassDec c = (ClassDec) type1;
				if (c.isSubclass((ClassDec) type2)) {
					return true;
				} else {
					return false;
				}
			}
		}
		if ((type1 == Type.stringType && type2 == Type.nilType) || (type2 == Type.stringType && type1 == Type.nilType)) {
			return true;
		}
		
		return false;
	}
	
	private void checkParameters(ArrayList<Expr> list1, ArrayList<Variable> list2, String methodName) {	
		if (list1.size() != list2.size()) {
			error("The number of given parameters are incorrect in '" + methodName + "' method");
		}
		
		for (int i = 0; i < list1.size(); i++) {
            if (!checkType(list2.get(i).getType(), list1.get(i).getType())) {
                error("Type error in given parameters of method '" + methodName + "'");
            }
        }
		
	}
	
	private MethodDec searchMethod(ClassDec c, String methodName) {	
		Member method = (Member) symbolTable.getInClass(methodName);
		if (method != null && method instanceof MethodDec && c == currentClass)
			return (MethodDec) method;
		while (c != null) {
			ArrayList<MethodDec> memberList = c.getMethods();
			for (MethodDec m: memberList) {	
				if (m.getMethodName().equals(methodName)) {
					return m;
				}
			}
			c = c.getParent();	
		}
		return null;
	}
	
	private Variable searchFields(ClassDec c, String fieldName) {
		Member field = (Member) symbolTable.getInClass(fieldName);
		if (field != null && field instanceof Variable) {
			return (Variable) field;
		}
		
		while (c != null) {	
			ArrayList<Variable> memberList = c.getFields();
			for (Variable f: memberList) {
				if (f.getName().equals(fieldName)) {
					return f;
				}
			}
			c = c.getParent();	
		}
		return null;
	}
	

	private static boolean startExpr(Token token) {
		return token == Token.FALSE || token == Token.TRUE
				|| token == Token.NOT || token == Token.SELF
				|| token == Token.LITERALINT || token == Token.SUPER
				|| token == Token.LEFTPAR || token == Token.NIL
				|| token == Token.ID || token == Token.LITERALSTRING;

	}
	
	private ClassDec currentClass;
    private MethodDec currentMethod;
	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;
	private boolean  ehLoop;
	private boolean haveRun;
	private boolean ehvalido;
	private MethodDec isMethod;
	private boolean isReadExpr;
	private boolean haveReturn;
}
