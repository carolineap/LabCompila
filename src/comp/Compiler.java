/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
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
		
		if (!thereWasAnError) {			
			
			boolean flag = false;
		
			for (ClassDec c : CianetoClassList) {
	
				if (c.getName().equals("Program")) {
					
					ArrayList<MethodDec> methods = c.getMethods();
										
					for (MethodDec m: methods) {
						if (m.getMethodName().equals("run") && m.getQualifier() == Token.PUBLIC) {

							if (m.getParamList().isEmpty()) {
								flag = true;
							} else {
								break;
							}
						}
						
					}
					
					break;
					
				}
			}
			
			if (flag == false) {
				try {
					error("Every program must have a class named 'Program' with a public parameterless method called 'run'");
				}
				catch( CompilerError e) {
				}
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
			
			lexer.nextToken();
		}
		
		ArrayList<Member> ml = memberList();
		
		if (ml == null || lexer.token != Token.END)
			error("Class member or 'end' expected");
		lexer.nextToken();
		
		symbolTable.removeClassIdent();
		

			
		ClassDec classDec = new ClassDec(className, superclassName, ml, isInheritable); 

        symbolTable.putInGlobal(className, classDec);
        
        
		return classDec;
	}

	private ArrayList<Member> memberList() {
		
		ArrayList<Member> ml = new ArrayList<>();
		
		while ( true ) {
			
			Token q = qualifier();
			
			if ( lexer.token == Token.VAR ) {
				ml.add(fieldDec(q));
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

	private MethodDec methodDec(Token q) {
		
		
		String id = null;
		ArrayList<Param> paramList = new ArrayList<>();
		Type returnType = null;
		ArrayList<Statement> statList = new ArrayList<>();
		
		lexer.nextToken();
		if ( lexer.token == Token.ID ) {
			// unary method
			id = lexer.getStringValue();
			lexer.nextToken();

		}
		else if ( lexer.token == Token.IDCOLON ) {	
			id = lexer.getLiteralStringValue();
			lexer.nextToken();
			paramList = parameterList();
			for (Param p : paramList) {
                if (symbolTable.getInLocal(p.getName()) != null) {
                    error("Variável '" + p.getName() + "' já foi declarada");
                }
                symbolTable.putInLocal(p.getName(), p);
	        }
		}
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if ( lexer.token == Token.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			returnType = type();
		}
		
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		
		next();
		statList = statementList();
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
        if(returnType != null){
            boolean flag = false; 
            for(Statement s: statList){
                if(s instanceof ReturnStat){
                    flag = true;
                }
            }
            if(flag == false){
                error("A função " + id + " é do tipo " + returnType.getName() + " e não possui retorno");
            }
        }
        
        next();

        symbolTable.removeLocalIdent();
        
		return new MethodDec(id, paramList, returnType, statList, q);
	}
	
	private ArrayList <Param> parameterList() {
		
		ArrayList <Param> paramList = new ArrayList<>();
		
		while (true) {	
			
			Type t = type();
			
			if (lexer.token != Token.ID) 
				error("Identifier expected");
			
			paramList.add(new Param(t, lexer.getStringValue()));
			
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
			
		Expr left = expr();
		
		if (left == null)
			error("Statement expected");
		
		Expr right = null;
		
		if (lexer.token == Token.ASSIGN) {
			next();
			right = expr();
			if (right == null)
				error("Expression expected");
		}
		
		
		return new AssignExpr(left, right);
	}

	private LocalDec localDec() {
		
		next();
		
		ArrayList<Variable> idList = new ArrayList<>();
		Type t = type();
		Expr e = null;
		
		check(Token.ID, "Missing identifier");
		
		while ( lexer.token == Token.ID ) {
			
			Variable v = new Variable(lexer.getStringValue(), t);
			
			if (symbolTable.getInLocal(v.getName()) != null) {
                error("Variável '" + v.getName() + "' já foi declarada");
            }
            symbolTable.putInLocal(v.getName(), v);
            idList.add(v);
            
			next();
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
		
		
		/*if (lexer.token != Token.SEMICOLON) {
			error("';' expected");
		}*/
		
		return new LocalDec(t, idList, e);

	}

	private RepeatStat repeatStat() {
		
		next();
		ArrayList<Statement> statList = statementList();
		check(Token.UNTIL, "'until' was expected");
		next();
		Expr e = expr();
		if (e == null) {
			error("Repeat expression expected");
		}
	
		return new RepeatStat(statList, e);
	}

	private BreakStat breakStat() {
		lexer.nextToken();
		return new BreakStat();
	}

	private ReturnStat returnStat() {
		next();
		Expr e = expr();
		if (e == null) {
			error("Return expression expected");
		}
		return new ReturnStat(e);
	}

	private WhileStat whileStat() {
		
		next();
		Expr e = expr();
		if (e == null) {
			error("While expression expected");
		}
		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		ArrayList<Statement> statList = statementList();
		
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
		next();
		Expr e = expr();
		if (e == null) {
			error("Write expression expected");
		}
		return new WriteStat(e);
	}

	private Expr expr() {
		
		
		SimpleExpr left = simpleExpr();
		
		

		
		if (left == null) return null;
		
		if (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT || 
			lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
			Token op = lexer.token;
			lexer.nextToken();
			SimpleExpr right = simpleExpr();
			if (right == null) return null;
			return new CompositeExpr(left, op, right);
		}
		
			
		return left;
		
	}
	
	private SimpleExpr simpleExpr() {
		
		SumSubExpr left = sumSubExpr();
		

		
		if (left == null) return null;
		
		if (lexer.token == Token.PLUS) {
			lexer.nextToken();
			if (lexer.token == Token.PLUS) {
				Token op = lexer.token;
				lexer.nextToken();	
				SumSubExpr right = sumSubExpr();
				if (right == null) return null;
				return new CompositeSimpleExpr(left, op, right);
			} else {
				error("'++' expected");
			}
		}

		return left;
	}
	
	private SumSubExpr sumSubExpr( ) {
		
		Term left = term();
		

		
		if (left == null) return null;
		
		while (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
			lexer.nextToken();
			Token op = lexer.token;
			Term right = term();
			if (right == null) return null;
			return new CompositeSumSubExpr(left, op, right);
		}
		
		return left;
	}
	
	private Term term() {
		
		SignalFactor left = signalFactor();
		

		
		if (left == null) return null;
		
		while (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
			lexer.nextToken();
			Token op = lexer.token;
			SignalFactor right = signalFactor();
			if (right == null) return null;
			return new CompositeTerm(left, op, right);
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
			return null;
				
		return new CompositeSignalFactor(op, right);
		
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
			return factor();
		} 
		
		if (lexer.token == Token.NIL) {
			lexer.nextToken();
			return null; //não sei o que retornar aqui
		} 
		//Falta PrimaryExpr
		if (lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING || lexer.token == Token.TRUE || lexer.token == Token.FALSE)
				return basicValue();
		
		if (lexer.token == Token.ID || lexer.token == Token.SUPER || lexer.token == Token.SELF || lexer.token == Token.IN){
			return auxFactor();
		} 
		
		//error("Expression expected");
		return null;
		
	}
	private AuxFactor auxFactor() {
		if(lexer.token == Token.ID) {
			String s = lexer.getStringValue();
			lexer.nextToken();
			if (lexer.token == Token.DOT) {
				lexer.nextToken();
				if (lexer.token == Token.NEW) {
					lexer.nextToken();
					return new ObjectCreation(s);
				} else if (lexer.token == Token.ID ) {
					String s1 = lexer.getStringValue();
					lexer.nextToken();
					return new PrimaryExpr("id", s, s1);
				} else if (lexer.token == Token.IDCOLON) {
					String s1 = lexer.getStringValue();
					lexer.nextToken();
					ArrayList<Expr> listExp = exprList();
					return new PrimaryExpr("id", s, s1, listExp);
				} else {
					error("id or idcolon expected");
				}
			} 
			
			return new PrimaryExpr("id",s);
		}else if(lexer.token == Token.SUPER){
			lexer.nextToken();
			if(lexer.token == Token.DOT) {
				lexer.nextToken();
				if(lexer.token == Token.ID) {
					String s = lexer.getStringValue();
					lexer.nextToken();
					return new PrimaryExpr("super", s);
				}else if(lexer.token == Token.IDCOLON) {
					String s1 = lexer.getStringValue();
					lexer.nextToken();
					ArrayList<Expr> listExp = exprList();
					return new PrimaryExpr("super", s1, listExp);
				}else {
					error("id or idcolon expected");
				}
			}else {
				error("dot expected");
			}
		}else if(lexer.token == Token.SELF) {
			lexer.nextToken();
			if(lexer.token == Token.DOT) {
				lexer.nextToken();
				if(lexer.token == Token.ID) {
					String s = lexer.getStringValue();

					if (symbolTable.getInClass(s) == null) {
						error("Class doesn't have the member '" + s + " '");
					} 
					
					lexer.nextToken();
					if(lexer.token == Token.DOT) {
						lexer.nextToken();
						if(lexer.token == Token.ID) {
							String s1 = lexer.getStringValue();
							lexer.nextToken();
							return new PrimaryExpr("self",s ,s1);
						}else if(lexer.token == Token.IDCOLON) {
							String s1 = lexer.getStringValue();
							lexer.nextToken();
							ArrayList<Expr> listExp = exprList();
							return new PrimaryExpr("self", s, s1, listExp);
						}else {
							error("id or idcolon expected");
						}
					}else {
						return new PrimaryExpr("self", s);
					}
				}else if(lexer.token == Token.IDCOLON) {
					String s1 = lexer.getStringValue();
					lexer.nextToken();
					ArrayList<Expr> listExp = exprList();
					return new PrimaryExpr("self", s1, listExp);
				}else {
					error("id or idcolon expected");
				}
			}else {
				error("dot expected");
			}
		}else if(lexer.token == Token.IN) {
			return readExpr();
		}
		return null;
		
	}
	private ReadExpr readExpr() {
		
		lexer.nextToken();
		if(lexer.token == Token.DOT) {
			lexer.nextToken();
			if (lexer.token == Token.ID && (lexer.getStringValue().equals("readInt") || lexer.getStringValue().equals("readString"))) {
				if (lexer.equals("readInt")) {
					lexer.nextToken();
					return new ReadExpr("Int");
				} else {
					lexer.nextToken();
					return new ReadExpr("String");
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
		
		//System.out.println(lexer.getLiteralStringValue());

	
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

	
	private FieldDec fieldDec(Token q) {
		
		lexer.nextToken();
		Type type = type();
		ArrayList<Variable> idList = new ArrayList<>();
		
		if ( lexer.token != Token.ID ) {
			this.error("A variable name was expected");
		}
		else {
			while (true) {
								
				if (lexer.token != Token.ID) 
					error("Missing identifier");
								
				Variable v = new Variable(lexer.getStringValue(), type);
				
				if (symbolTable.getInClass(v.getName()) != null) {
	                error("Variável '" + v.getName() + "' já foi declarada");
	            }
	            symbolTable.putInClass(v.getName(), v);
				
				idList.add(v);
				
				next();
				
				if ( lexer.token == Token.COMMA ) {
					lexer.nextToken();
				} else if (lexer.token == Token.SEMICOLON) {
					break;
				} else {
					error("';' expected after field declaration");
				}
				
			}
				
			lexer.nextToken();
		}
		
		return new FieldDec(type, idList, q);

	}

	private Type type() {
		
		Type type = null;
		
		if (lexer.token == Token.INT) {
			type = new TypeInt();
			next();
		} else if ( lexer.token == Token.BOOLEAN) {
			type = new TypeBoolean();
			next();
		} else if ( lexer.token == Token.STRING ) {
			type = new TypeString();
			next();
		} else if ( lexer.token == Token.ID ) {
			if (symbolTable.getInGlobal(lexer.getStringValue()) == null){
	            error("Invalid type '" + lexer.getStringValue() + "'");
	        }
			type = new TypeClass(lexer.getStringValue());
			next();
		} else {
			this.error("A type was expected");
		}

		return type;
	}


	private Token qualifier() {
		
		Token q = lexer.token;
		
		if ( lexer.token == Token.PRIVATE ) {
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				next();
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
			}
		} else {
			q = Token.PUBLIC;
			
		}
		return q;
	}

	private AssertStat assertStat() {

		int lineNumber = lexer.getLineNumber();
		
		next();
		
		Expr e = expr();
		
		if (e == null) {
			error("Assert expression expected");
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

		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = lexer.getNumberValue();
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private boolean checkType(String type1, String type2) {
		
		if (type1 == type2) {
			return true;
		}

		if (type1 == "String" && type2 == "nil") {
			return true;
		}
		
		ClassDec c1 = (ClassDec) symbolTable.getInGlobal(type1);
		ClassDec c2 = (ClassDec) symbolTable.getInGlobal(type1);
		
		if (c1 == null || c2 == null) {
			return false;
		} else {
			ClassDec c = isSubclass(c1, c2);
			if (c != null) {
				return true;
			}
		}
		
		return false;
		

	}
	
	private ClassDec isSubclass(ClassDec c1, ClassDec c2) {
		
		ClassDec c = c2;
		
		while (c != null && c != c1) {
			c = (ClassDec) symbolTable.getInGlobal(c.getParentName());
		}
		
		if (c != null && c == c1) {
			return c;
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

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
