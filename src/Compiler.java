import Lexer.*;
import Error.*;
import AST.*;
//import Aux.SymbolTable;
import java.util.*;

public class Compiler {

  private Lexer lexer;
  private CompilerError error;

  // para geracao de codigo
  public static final boolean GC = false;


  ArrayList<Variable> vl = new ArrayList<>();

  /*
  Acho que dá para seguir sem SymbolTable...
              let's freakout!!!
  //symbolTable = new SymbolTable();
  */

  StatementList sl = null;
  ArrayList<functionDlc> dlc = new ArrayList<>();

    public Program compile( char []p_input ) {
      error = new CompilerError(null);
      lexer = new Lexer(p_input, error);
      error.setLexer(lexer);
      lexer.nextToken();

      Program p = program();
      if (error.wasAnErrorSignalled())
        return null;
      return p;
    }

    public Program program() {

      if(lexer.token != Symbol.PROGRAM)
        error.signal("'PROGRAM' expected");

      lexer.nextToken();

      if(lexer.token != Symbol.IDENT)
        error.signal("identifier of program expected");

      lexer.nextToken();

      if (lexer.token != Symbol.BEGIN)
        error.signal("'BEGIN' expected");
      lexer.nextToken();

      if (lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING)
        vl = var_decl_list(null);

      //lexer.nextToken();

      while (lexer.token == Symbol.FUNCTION) {
        dlc.add(func_decl());
        //System.out.println("token " + lexer.token.toString());
      }

      //lexer.nextToken();

      //if (!(lexer.token.toString().isEmpty() || lexer.token == Symbol.END ))
      sl = stmt(null, null);

      //lexer.nextToken();

      if (lexer.token != Symbol.END)
        error.signal("'END' expected");

        lexer.nextToken();

      return new Program(vl, sl, dlc);
    }

    public ArrayList<Variable> var_decl_list(ArrayList<Variable> checagem){
      ArrayList<Variable> vl = new ArrayList<>();
      if (!(lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING))
        error.signal("Variable type expected");

      else if (!(lexer.token == Symbol.STRING)) {
        while (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT) {
          String tipo = "a";
          if (lexer.token == Symbol.INT)
            tipo = "int";
          else
            tipo = "float";

   	      lexer.nextToken();
          if (lexer.token != Symbol.IDENT)
              error.signal("Variable name expected");

        if (checagem != null) {
            for (int i = 0; i < checagem.size(); i++) {
              if (lexer.getStringValue().equals(checagem.get(i).getName()) ) {
                error.signal(lexer.getStringValue() + " é uma redeclação de um parâmetro da função");
              }
            }
        }

          Variable v = new Variable(lexer.getStringValue(), tipo);
          //ArrayList<Variable> vl = new ArrayList<>();
          vl.add(v);

          lexer.nextToken();

          while (lexer.token == Symbol.COMMA){
            lexer.nextToken();
            if (lexer.token != Symbol.IDENT)
              error.signal("variable name expected");

            v = new Variable(lexer.getStringValue(), tipo);
            vl.add(v);
            lexer.nextToken();
          }

          //lexer.nextToken();

          if (lexer.token != Symbol.SEMICOLON)
            error.signal("';' expected");

            lexer.nextToken();

          }

        } else {
          while (lexer.token == Symbol.STRING) {
            //System.out.println("achei string");
            lexer.nextToken();

            if (lexer.token != Symbol.IDENT)
                error.signal("Variable name expected");

            String stringName = lexer.getStringValue();

            lexer.nextToken();

            if (lexer.token != Symbol.ASSIGN)
                error.signal("':=' expected");

            lexer.nextToken();

            //Check no nextToken de " para StringLiteral

            if (lexer.token != Symbol.STRINGLITERAL)
              error.signal("STRINGLITERAL expected");

            Variable v = new Variable(stringName, "string", lexer.getStringValue());

            vl.add(v);

            //Expr e = null;
            //e = new StringExpr(lexer.getStringValue());

            lexer.nextToken();

            if (lexer.token != Symbol.SEMICOLON)
                error.signal("';' expected");
            lexer.nextToken();
          }
        }
        //System.out.println("token novo: " + lexer.token.toString());
      return vl;
    }

    public AssignmentStatement assign_stmt(ArrayList<Variable> parametros, ArrayList<Variable> varLocal){

      //System.out.println("entrei " + lexer.getStringValue());
      if (lexer.token != Symbol.IDENT)
        error.signal("Variable name expected");

        //System.out.println(variavelDeclarada(lexer.getStringValue()).getName());

      Variable v = variavelDeclarada(lexer.getStringValue());

      if (v == null && parametros != null) {
          v = variavelDeclaradaLocal(lexer.getStringValue(), parametros);
      }
      
      if (v == null && varLocal != null) {
          v = variavelDeclaradaLocal(lexer.getStringValue(), varLocal);
      }
      
      if (v == null)
        error.signal(lexer.getStringValue() + " not declared");

      lexer.nextToken();

      if (lexer.token != Symbol.ASSIGN)
        error.signal("':=' expected");

      lexer.nextToken();

      Expr e = expr(true, parametros, varLocal);
      /* Podemos usar isso no for */

      //System.out.print(e.getTipo());
      
      tiposValidos(v.getTipo(), e.getTipo());

      if ((lexer.token != Symbol.RPAR) && lexer.token != Symbol.SEMICOLON)
        error.signal("';' expected");

      return new AssignmentStatement(v, e);
    }

    private void tiposValidos(String variavel, String valor) {
      /*
      String só com string
      int só com int
      float pode com int
      */

      if (variavel.equals("float") && valor.equals("int"))
        return;
      else if (!(variavel.equals(valor))) {
        error.signal("Esperava " + variavel + ", mas recebi " + valor);
      }
    }

    private Expr expr(final boolean deveExistir, final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr e = null;
      //System.out.println("op: " + lexer.token.toString());
      if (lexer.token == Symbol.INTLITERAL) {
        e = new NumberExpr(lexer.getNumberValue());
        lexer.nextToken();
      } else if (lexer.token == Symbol.FLOATLITERAL) {
        e = new FloatExpr(lexer.getFloatValue());
        lexer.nextToken();
      } else if (lexer.token == Symbol.STRINGLITERAL) {
        e = new StringExpr(lexer.getStringValue());
        lexer.nextToken();
      } else if (lexer.token == Symbol.IDENT) {
        //Verificar se está declarada a variável ou se é função.

        String nomeVerificar = lexer.getStringValue();
        
      if (deveExistir) {
          boolean existe = false;
          if (e == null && checagem != null) {
              if (variavelDeclaradaLocal(nomeVerificar, checagem) != null) {
                existe = true;
                e = new VariableExpr(variavelDeclaradaLocal(nomeVerificar, checagem));
              }
          }
          
          if (e == null && varLocal != null) {
              if (variavelDeclaradaLocal(nomeVerificar, varLocal) != null) {
                existe = true;
                e = new VariableExpr(variavelDeclaradaLocal(nomeVerificar, varLocal));
              }
          }
        
          if (funcaoDeclarada(lexer.getStringValue()) != null || (variavelDeclarada(lexer.getStringValue()) != null)) {
            existe = true;
          }
          
          if(!(existe))
            error.signal(lexer.getStringValue() + " não declarado");
      }

        if (e == null)
            e = new VariableExpr(variavelDeclarada(lexer.getStringValue()));

        if (e == null)
          e = new VariableExpr(new Variable(lexer.getStringValue()));


        lexer.nextToken();


        //Chamando uma função
        if (lexer.token == Symbol.LPAR) {
          //String funcName = lexer.getStringValue();
          lexer.nextToken();

          functionDlc funcao = null;
          
          funcao = funcaoDeclarada(nomeVerificar);
          
          if (funcao == null)
              error.signal(nomeVerificar + " é uma função não declarada");
          
          ArrayList<Variable> parametros = new ArrayList<>();

          while (lexer.token == Symbol.IDENT || lexer.token == Symbol.COMMA) {
            if (lexer.token == Symbol.COMMA)
                lexer.nextToken();
            Variable v = new Variable(lexer.getStringValue());
            parametros.add(v);
            lexer.nextToken();
          }

          if (lexer.token != Symbol.RPAR)
              error.signal(") expected");

          lexer.nextToken();

          e = new functionExpr(nomeVerificar, parametros, funcao.getTipo());
        }

      } else if (((lexer.token == Symbol.PLUS) || (lexer.token == Symbol.MINUS) || (lexer.token == Symbol.MULT) || (lexer.token == Symbol.DIV))) {
        Symbol op = lexer.token;

        lexer.nextToken();
        Expr l = expr(true, checagem, varLocal);

        //lexer.nextToken();
        Expr r = expr(true, checagem, varLocal);

        e = new CompositeExpr(l, op, r);
        //lexer.nextToken();
      } else {
        error.signal("Expressao nao valida");
      }
      return e;
    }

    private Symbol compOp() {
      //System.out.println("Comp op: " + lexer.token.toString());
      Symbol op = lexer.token;
      if (lexer.token != Symbol.LT && lexer.token != Symbol.GT && lexer.token != Symbol.EQUAL)
        error.signal("Comp op invalid");
        lexer.nextToken();

      return op;
    }

    private Expr cond(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr l = expr(true, checagem, varLocal);

      //lexer.nextToken();

      Symbol op = compOp();

      //lexer.nextToken();

      Expr r = expr(true, checagem, varLocal);

      return new CompositeExpr(l, op, r);
    }

    private IfStatement if_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr ifExpr = null;

      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
        error.signal("( expected");
      lexer.nextToken();

      //System.out.println("if: " + lexer.token.toString());

      ifExpr = cond(checagem, varLocal);

      //lexer.nextToken();

      if (lexer.token != Symbol.RPAR)
        error.signal(") expected");

      lexer.nextToken();

      if (lexer.token != Symbol.THEN)
        error.signal("THEN expected");

      lexer.nextToken();

      StatementList seEntao = stmt(checagem, varLocal);
      StatementList seNao = null;

      //System.out.println("cheguei: " + lexer.token.toString());

      if (lexer.token == Symbol.ELSE) {
        lexer.nextToken();
        seNao = stmt(checagem, varLocal);
      }

      if (lexer.token != Symbol.ENDIF) {
        error.signal("ENDIF expected");
      }
      //lexer.nextToken();

      return new IfStatement(ifExpr, seEntao, seNao);
    }

    private StatementList stmt(final ArrayList<Variable> parametros, final ArrayList<Variable> varLocal) {
      ArrayList<Statement> v = new ArrayList<Statement>();
      //System.out.println("entrei no stmt: " + lexer.token.toString());
      while (lexer.token != Symbol.ENDIF && lexer.token != Symbol.ELSE && lexer.token != Symbol.ENDFOR && lexer.token != Symbol.END ) {
        v.add( statement(parametros, varLocal) );

        //lexer.nextToken();

        //System.out.println("STList: " + lexer.token.toString());
        // if ( lexer.token != Symbol.END && lexer.token != Symbol.EOF && lexer.token != Symbol.SEMICOLON && lexer.token != Symbol.ELSE && lexer.token != Symbol.ENDFOR )
        //   error.signal("';' expectede");
        if (lexer.token != Symbol.END)
          lexer.nextToken();
      }
      return new StatementList(v);
    }

    private Statement statement(ArrayList<Variable> parametros, ArrayList<Variable> varLocal) {
      //System.out.println("lexer.token: " + lexer.token.toString());

      if (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT || lexer.token == Symbol.STRING)
        vl.addAll(var_decl_list(null));
      if (lexer.token == Symbol.IF)
        return if_stmt(parametros, varLocal);
      else if (lexer.token == Symbol.ELSE)
        return null;
      else if (lexer.token == Symbol.IDENT)
        return assign_stmt(parametros, varLocal);
      else if (lexer.token == Symbol.FOR)
        return for_stmt(parametros, varLocal);
      else if (lexer.token == Symbol.ENDFOR)
        return null;
      else if (lexer.token == Symbol.FUNCTION)
        return func_decl();
      else if (lexer.token == Symbol.END)
        return null;
      else if (lexer.token == Symbol.EOF)
        return null;
      else if (lexer.token == Symbol.RETURN)
        return return_stmt(parametros, varLocal);
      else if (lexer.token == Symbol.READ)
        return read_stmt(parametros, varLocal);
      else if (lexer.token == Symbol.WRITE)
        return write_stmt(parametros, varLocal);
      else
        error.signal("Erro leexico");

      stmt(null, null);

      return null;
    }

    private functionDlc func_decl () {
      String functionType = "0";
      String functionName = "0";
      ArrayList<Variable> parametros = new ArrayList<>();

      lexer.nextToken();

      if (lexer.token != Symbol.FLOAT && lexer.token != Symbol.INT && lexer.token != Symbol.VOID)
          error.signal("var_type (FLOAT, INT, VOID) expected at function declaration");

      functionType = lexer.token.toString();

      lexer.nextToken();

      if (lexer.token != Symbol.IDENT)
          error.signal("IDENTIFIER expected");

      functionName = lexer.getStringValue();

      if (funcaoDeclarada(functionName) != null)
        error.signal(lexer.getStringValue() + " já estava declarada");

      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("'(' expected");
      lexer.nextToken();

      if (lexer.token == Symbol.VOID)
        error.signal("void não aceito como parâmetro da função");

      if (lexer.token == Symbol.STRING)
        error.signal("string não aceito como parâmetro da função");

      while (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT || lexer.token == Symbol.COMMA) {

        if (lexer.token == Symbol.COMMA)
          lexer.nextToken();

        String varTipo = lexer.token.toString();
        lexer.nextToken();

        Variable v = new Variable(lexer.getStringValue(), varTipo);
        parametros.add(v);

        lexer.nextToken();
      }

      if (lexer.token != Symbol.RPAR)
        error.signal(") expected");

      lexer.nextToken();

      if (lexer.token != Symbol.BEGIN)
        error.signal("begin expected");

      lexer.nextToken();

      ArrayList<Variable> fncVariaveis = new ArrayList<>();

      if (lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING)
        fncVariaveis = var_decl_list(parametros);

      StatementList sl = null;
      sl = stmt(parametros, fncVariaveis);



      if (lexer.token != Symbol.END)
        error.signal("end expected");

      lexer.nextToken();
      //  System.out.println("oi: " + lexer.token.toString());
      return new functionDlc(functionType, functionName, parametros, fncVariaveis, sl);
    }


    private returnStmt return_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      //System.out.println("oioi");
      if (lexer.token != Symbol.RETURN)
        error.signal("return expected");

      lexer.nextToken();

      Expr retorno = expr(true, checagem, varLocal);

      //lexer.nextToken();

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("; expected");

      lexer.nextToken();

      return new returnStmt(retorno);
    }

    private Statement for_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      AssignmentStatement asgt = null;
      Expr condicao = null;
      AssignmentStatement passo = null;
      StatementList loopFaz = null;

      lexer.nextToken();

      if (lexer.token != Symbol.LPAR) {
          error.signal("'(' expected");
      }
      lexer.nextToken();

      //Aqui posso verificar se é um ident
      //Mentira, não posso porque se não tiver assign COND começa com Expr

      //For (i = 0; i < 10; i++)
      if (lexer.token != Symbol.SEMICOLON)
        asgt = assign_stmt(null, null);

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("; expected");

      lexer.nextToken();

      if (lexer.token != Symbol.SEMICOLON)
        condicao = cond(checagem, varLocal);

      //lexer.nextToken();

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("; expected");

      lexer.nextToken();

      if (lexer.token == Symbol.IDENT)
        passo = assign_stmt(null, null);

      //lexer.nextToken();

      if (lexer.token != Symbol.RPAR)
        error.signal(") expected");

      //lexer.nextToken();

      //if (lexer.token != Symbol.THEN)
      //  error.signal("then expected");

      lexer.nextToken();

      loopFaz = stmt(null, null);

      if (lexer.token != Symbol.ENDFOR)
        error.signal("ENDFOR expected");

      return new forStatement(asgt, condicao, passo, loopFaz);
    }

    private Variable variavelDeclaradaLocal(String variavel, ArrayList<Variable> checagem) {
      for (int i = 0; i < checagem.size(); i++) {
        if (variavel.equals(checagem.get(i).getName()) ) {
          return checagem.get(i);
        }
      }
      return null;
    }    
    
    private Variable variavelDeclarada(String variavel) {
      for (int i = 0; i < vl.size(); i++) {
        if (variavel.equals(vl.get(i).getName()) ) {
          return vl.get(i);
        }
      }
      return null;
    }

    private functionDlc funcaoDeclarada(String funcao) {
      for (int i = 0; i < dlc.size(); i++) {
        if (funcao.equals(dlc.get(i).getName()) ) {
          return dlc.get(i);
        }
      }
      return null;
    }


    private readStmt read_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      ArrayList<Variable> idList = new ArrayList<>();

      if (lexer.token != Symbol.READ)
          error.signal("'READ' expected");
      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("'(' expected");
      lexer.nextToken();

        do {
          if (lexer.token == Symbol.COMMA)
            lexer.nextToken();

          if (lexer.token != Symbol.IDENT)
            error.signal("IDENT expected");

          
          Variable v = variavelDeclarada(lexer.getStringValue());

          if (v == null && checagem != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
          }
          
          if (v == null && varLocal != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), varLocal);
          }
          
          //System.out.println("tipop: " + v.getTipo() + " name: " + v.getName());

          if (v == null)
            error.signal("Tentando ler uma variável (" + lexer.getStringValue() + ") não declarada?");
          
          if (v.getTipo() == "string")
            error.signal("Strings são constantes. Não faz sentido fazer um read em uma constante");          

          idList.add(v);

          lexer.nextToken();

        } while (lexer.token == Symbol.COMMA);

        if (lexer.token != Symbol.RPAR)
            error.signal("')' expected");
        lexer.nextToken();

        if (lexer.token != Symbol.SEMICOLON)
            error.signal("';' expected");
        //lexer.nextToken();

        return new readStmt(idList);
    }

    private Statement write_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      ArrayList<Variable> idList = new ArrayList<>();

      if (lexer.token != Symbol.WRITE)
          error.signal("'WRITE' expected");
      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("'(' expected");
      lexer.nextToken();

        do {
          if (lexer.token == Symbol.COMMA)
            lexer.nextToken();

          if (lexer.token != Symbol.IDENT)
            error.signal("IDENT expected");

          Variable v = variavelDeclarada(lexer.getStringValue());

          if (v == null && checagem != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
          }
          
          if (v == null && varLocal != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), varLocal);
          }
          
          //System.out.println("tipop: " + v.getTipo() + " name: " + v.getName());

          if (v == null)
            error.signal("Tentando escrever uma variável (" + lexer.getStringValue() + ") não declarada?");         
          
          idList.add(v);

          lexer.nextToken();

        } while (lexer.token == Symbol.COMMA);

        if (lexer.token != Symbol.RPAR)
            error.signal("')' expected");
        lexer.nextToken();

        if (lexer.token != Symbol.SEMICOLON)
            error.signal("';' expected");
        //lexer.nextToken();

        return new writeStmt(idList);
    }

}
