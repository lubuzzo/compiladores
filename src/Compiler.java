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


  private ArrayList<Variable> vl = new ArrayList<>();

  /*
  Acho que dá para seguir sem SymbolTable...
              let's freakout!!!
  //symbolTable = new SymbolTable();
  */

  private StatementList sl = null;
  private ArrayList<functionDlc> dlc = new ArrayList<>();

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
        error.signal("Palavra-chave 'PROGRAM' esperada");

      lexer.nextToken();

      if(lexer.token != Symbol.IDENT)
        error.signal("O programa deve ter um identificador, após a palavra chave 'PROGRAM'");

      lexer.nextToken();

      if (lexer.token != Symbol.BEGIN)
        error.signal("Após o identificador do programa, deve haver a palavra-chave'BEGIN'");
      lexer.nextToken();

      if (lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING)
        vl = var_decl_list(null, null);

      while (lexer.token == Symbol.FUNCTION)
        dlc.add(func_decl());

      sl = stmt(null, null);

      if (lexer.token != Symbol.END)
        error.signal("O programa deve conter a palavra-chave 'END' no final");

      lexer.nextToken();

      return new Program(vl, sl, dlc);
    }

    public ArrayList<Variable> var_decl_list(ArrayList<Variable> checagem, String functionName){
      ArrayList<Variable> vl = new ArrayList<>();
      if (!(lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING))
        error.signal("Na LITTLE, as variáveis são tipadas de forma explícita. Favor, especificar o tipo da variável");

      else if (!(lexer.token == Symbol.STRING)) {
        while (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT) {
          String tipo = "a";
          if (lexer.token == Symbol.INT)
            tipo = "int";
          else
            tipo = "float";

   	      lexer.nextToken();
          if (lexer.token != Symbol.IDENT)
              error.signal("Parece que você esqueceu o nome da variável. Pode verificar, por favor?");
          
          Variable verificando = null;
          if (checagem != null) {
              verificando = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
              if (verificando != null)
                error.signal(lexer.getStringValue() + " é uma redeclação de um parâmetro da função " + functionName);  
          } else {
              verificando = variavelDeclarada(lexer.getStringValue());
              if (verificando != null)
                error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              else {
                  verificando = variavelDeclaradaLocal(lexer.getStringValue(), vl);
                  if (verificando != null)
                    error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              }
          }
          

          Variable v = new Variable(lexer.getStringValue(), tipo, lexer.getLineNumber());
          vl.add(v);

          lexer.nextToken();

          while (lexer.token == Symbol.COMMA){
            lexer.nextToken();
            //TODO: Acho que nesse if, vai dar erro quando houver uma redeclaração de variável com tail
            if (lexer.token != Symbol.IDENT)
              error.signal("Parece que você esqueceu o nome da variável. Pode verificar, por favor?");

            verificando = null;
            if (checagem != null) {
              verificando = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
              if (verificando != null)
                error.signal(lexer.getStringValue() + " é uma redeclação de um parâmetro da função " + functionName);  
            } else {
              verificando = variavelDeclarada(lexer.getStringValue());
              if (verificando != null)
                error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              else {
                  verificando = variavelDeclaradaLocal(lexer.getStringValue(), vl);
                  if (verificando != null)
                    error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              }
            }       
            
            v = new Variable(lexer.getStringValue(), tipo, lexer.getLineNumber());
            vl.add(v);
            lexer.nextToken();
          }

          if (lexer.token != Symbol.SEMICOLON)
            error.signal("Ops, parece que você esqueceu o nosso querido ';'", true);

          lexer.nextToken();

          }

        } else {
          while (lexer.token == Symbol.STRING) {
            lexer.nextToken();

            if (lexer.token != Symbol.IDENT)
                error.signal("Parece que você esqueceu o nome da String que tava tentando declarar. Pode verificar, por favor?");

            String stringName = lexer.getStringValue();

            lexer.nextToken();

            if (lexer.token != Symbol.ASSIGN)
                error.signal("Na LITTLE, as Strings devem ser declaradas já sendo atribuído o valor (inicializadas), com o operador ':='");

            lexer.nextToken();

            if (lexer.token != Symbol.STRINGLITERAL)
              error.signal("Na LITTLE, as Strings devem começar com \" e ter no máximo 80 caracteres. Verifique se você não se equivocou aqui, ok?");

            Variable verificando = null;
            if (checagem != null) {
              verificando = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
              if (verificando != null)
                error.signal(lexer.getStringValue() + " é uma redeclação de um parâmetro da função " + functionName);  
            } else {
              verificando = variavelDeclarada(lexer.getStringValue());
              if (verificando != null)
                error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              else {
                  verificando = variavelDeclaradaLocal(lexer.getStringValue(), vl);
                  if (verificando != null)
                    error.signal(lexer.getStringValue() + " já está declarada na linha: " + verificando.getLinha());
              }
            }            
            
            Variable v = new Variable(stringName, "string", lexer.getStringValue(), lexer.getLineNumber());

            vl.add(v);

            lexer.nextToken();

            if (lexer.token != Symbol.SEMICOLON)
                error.signal("Ops, parece que você esqueceu do nosso querido ';'", true);
            lexer.nextToken();
          }
        }
        return vl;
    }

    public AssignmentStatement assign_stmt(ArrayList<Variable> parametros, ArrayList<Variable> varLocal){

      if (lexer.token != Symbol.IDENT)
        error.signal("Eu acho que você se confundiu aqui. Estamos tentando atribuir o valor para uma variável, mas não achei o nome da variável");

      Variable v = variavelDeclarada(lexer.getStringValue());

      if (v == null && parametros != null) {
          v = variavelDeclaradaLocal(lexer.getStringValue(), parametros);
      }
      
      if (v == null && varLocal != null) {
          v = variavelDeclaradaLocal(lexer.getStringValue(), varLocal);
      }
      
      if (v == null)
        error.signal("Não me leve a mal, mas " + lexer.getStringValue() + " ainda não foi declarada");

      lexer.nextToken();

      if (lexer.token != Symbol.ASSIGN)
        error.signal("Véi, cadê o ':=' da atribuição? Colabora aí, só tô tentando fazer meu trabalho");

      lexer.nextToken();

      Expr e = expr(true, parametros, varLocal);
      
      tiposValidos(v.getTipo(), e.getTipo(), false);

      if ((lexer.token != Symbol.RPAR) && lexer.token != Symbol.SEMICOLON)
        error.signal("Ops, parece que você esqueceu o nosso querido ';'");

      return new AssignmentStatement(v, e);
    }

    private boolean tiposValidos(String variavel, String valor, boolean funcao) {
      /*
      String só com string
      int só com int
      float pode com int
      */

      if (variavel.equals("float") && valor.equals("int"))
        return true;
      else if (!(variavel.equals(valor))) {
          if (funcao)
              return false;
          else
            error.signal("Queria tanto " + variavel + ", mas você só me dá " + valor + " :(");
            return false;
      }
      return true;
    }

    private Expr expr(final boolean deveExistir, final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr e = null;
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
            error.signal("É... é... " + lexer.getStringValue() + " é uma variável/função? Porque meio que ela não foi declarada");
      }

        if (e == null)
            e = new VariableExpr(variavelDeclarada(lexer.getStringValue()));

        if (e == null)
          e = new VariableExpr(new Variable(lexer.getStringValue(), lexer.getLineNumber()));


        lexer.nextToken();

        if (lexer.token == Symbol.LPAR) {
          lexer.nextToken();

          functionDlc funcao = null;
          
          funcao = funcaoDeclarada(nomeVerificar);
          
          if (funcao == null)
              error.signal("Olha, se você não declarar a função " + nomeVerificar + " vamos ter um problema aqui... e quem avisa amigo é");
          
          ArrayList<Variable> parametros = new ArrayList<>();

          while (lexer.token == Symbol.IDENT || lexer.token == Symbol.COMMA) {
            if (lexer.token == Symbol.COMMA)
                lexer.nextToken();
            Variable v = new Variable(lexer.getStringValue(), lexer.getLineNumber());
            parametros.add(v);
            lexer.nextToken();
          }

          if (lexer.token != Symbol.RPAR)
              error.signal("Se o fim justifica os meios, aqui não tem justificativa. Você esqueceu o )");

          lexer.nextToken();

          e = new functionExpr(nomeVerificar, parametros, funcao.getTipo());
        }

      } else if (((lexer.token == Symbol.PLUS) || (lexer.token == Symbol.MINUS) || (lexer.token == Symbol.MULT) || (lexer.token == Symbol.DIV))) {
        Symbol op = lexer.token;

        lexer.nextToken();
        Expr l = expr(true, checagem, varLocal);

        Expr r = expr(true, checagem, varLocal);

        e = new CompositeExpr(l, op, r);
      } else {
        error.signal("Mais bagunçada que a vida, só essa sua expressão. Verifica aí, vai, please");
      }
      return e;
    }

    private Symbol compOp() {
      Symbol op = lexer.token;
      if (lexer.token != Symbol.LT && lexer.token != Symbol.GT && lexer.token != Symbol.EQUAL)
        error.signal("Quando você A e B, você quer saber a relação entre eles (maior, menor ou igual). O que você quer saber com " + lexer.getStringValue() + "?");
        lexer.nextToken();

      return op;
    }

    private Expr cond(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr l = expr(true, checagem, varLocal);

      Symbol op = compOp();

      Expr r = expr(true, checagem, varLocal);

      return new CompositeExpr(l, op, r);
    }

    private IfStatement if_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      Expr ifExpr = null;

      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
        error.signal("Cumpade, if precisa de um ( no começo do teste lógico");
      lexer.nextToken();

      ifExpr = cond(checagem, varLocal);

      if (lexer.token != Symbol.RPAR)
        error.signal("Tudo que começa tem um fim. Coloca um ) para terminar seu teste lógico, pode ser?");

      lexer.nextToken();

      if (lexer.token != Symbol.THEN)
        error.signal("Na LITTLE, depois do teste lógico do if vem um THEN. Entendeu? E que isso não se repita, hein");

      lexer.nextToken();

      StatementList seEntao = stmt(checagem, varLocal);
      StatementList seNao = null;

      if (lexer.token == Symbol.ELSE) {
        lexer.nextToken();
        seNao = stmt(checagem, varLocal);
      }

      if (lexer.token != Symbol.ENDIF) {
        error.signal("Se você não especificar o ENDIF, não vai funcionar");
      }
      
      return new IfStatement(ifExpr, seEntao, seNao);
    }

    private StatementList stmt(final ArrayList<Variable> parametros, final ArrayList<Variable> varLocal) {
      ArrayList<Statement> v = new ArrayList<Statement>();
      while (lexer.token != Symbol.ENDIF && lexer.token != Symbol.ELSE && lexer.token != Symbol.ENDFOR && lexer.token != Symbol.END ) {
        v.add( statement(parametros, varLocal) );

        if (lexer.token != Symbol.END)
          lexer.nextToken();
      }
      return new StatementList(v);
    }

    private Statement statement(ArrayList<Variable> parametros, ArrayList<Variable> varLocal) {
      if (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT || lexer.token == Symbol.STRING)
        vl.addAll(var_decl_list(null, null));
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
        error.signal("Tela azul da morte. " + lexer.getStringValue() + " era para ser um comando? Eu não conheço");

      stmt(null, null);

      return null;
    }

    private functionDlc func_decl () {
          
      String functionType = "0";
      String functionName = "0";
      ArrayList<Variable> parametros = new ArrayList<>();

      lexer.nextToken();

      if (lexer.token != Symbol.FLOAT && lexer.token != Symbol.INT && lexer.token != Symbol.VOID)
          error.signal("Presta atenção, as funções precisam ter um tipo (FLOAT, INT, VOID). E você TIPO esqueceu!!! Perdoa a piada ruim e não desiste de mim");

      functionType = lexer.token.toString();

      lexer.nextToken();

      if (lexer.token != Symbol.IDENT)
          error.signal("Se você não der um nome para a sua função, não vou saber como chamar ela.");

      functionName = lexer.getStringValue();

      if (funcaoDeclarada(functionName) != null)
        error.signal("A função " + lexer.getStringValue() + " já foi declarada na linha: " + funcaoDeclarada(functionName).getLinha() + ". Use outro nome");

      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("Eu realmente preciso de um '(' depois do nome da função");
      lexer.nextToken();

      if (lexer.token == Symbol.VOID)
        error.signal("Maluco, void não é aceito como parâmetro da função");

      if (lexer.token == Symbol.STRING)
        error.signal("Chato dizer isso, mas string não é aceito como parâmetro da função");

      while (lexer.token == Symbol.FLOAT || lexer.token == Symbol.INT || lexer.token == Symbol.COMMA) {

        if (lexer.token == Symbol.COMMA)
          lexer.nextToken();

        String varTipo = lexer.token.toString();
        lexer.nextToken();

        Variable v = new Variable(lexer.getStringValue(), varTipo, lexer.getLineNumber());
        parametros.add(v);

        lexer.nextToken();
      }

      if (lexer.token != Symbol.RPAR)
        error.signal("Depois dos parâmetros vem um ')'. Combinado?");

      lexer.nextToken();

      if (lexer.token != Symbol.BEGIN)
        error.signal("A palavra-chave BEGIN precisa estar presente na função");

      int linhaDeclarada = lexer.getLineNumber();
      
      lexer.nextToken();

      ArrayList<Variable> fncVariaveis = new ArrayList<>();

      if (lexer.token == Symbol.INT || lexer.token == Symbol.FLOAT || lexer.token == Symbol.STRING)
        fncVariaveis = var_decl_list(parametros, functionName);
      
      StatementList sl = null;
      sl = stmt(parametros, fncVariaveis);      
      
      Statement retorno = sl.getRetorno();
      
      if (retorno != null) {
          if (functionType == "void")
              error.signal("Funções do tipo void não devem ter retorno", true);
          else if (!(tiposValidos(functionType, retorno.getTipo(), true))) {
            error.signal("O retorno (" + retorno.getTipo() + ") da função " +  functionName + " (" + functionType + ") são diferentes", true);    
          }
      }
      
      if (retorno == null && functionType != "void") {
          error.signal("A função " + functionName + " é do tipo " + functionType + " você DEVE ter um retorno do tipo " + functionType);
      }
      
      if (lexer.token != Symbol.END)
        error.signal("end expected");

      lexer.nextToken();
      
      return new functionDlc(functionType, functionName, parametros, fncVariaveis, sl, linhaDeclarada);
    }


    private returnStmt return_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      if (lexer.token != Symbol.RETURN)
        error.signal("Aqui vai a palavra-chave RETURN, não?");

      lexer.nextToken();

      Expr retorno = expr(true, checagem, varLocal);

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("Ops, parece que noss querido ';' foi esquecido de novo");

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
          error.signal("Preciso de um '(' depois do FOR");
      }
      lexer.nextToken();

      if (lexer.token != Symbol.SEMICOLON)
        asgt = assign_stmt(null, null);

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("Logo vão lançar um filme: Esqueceram De Mim - edição ';'");

      lexer.nextToken();

      if (lexer.token != Symbol.SEMICOLON)
        condicao = cond(checagem, varLocal);

      if (lexer.token != Symbol.SEMICOLON)
        error.signal("Ôia quem esqueceu o ';'. HAHAHAHAHA!");

      lexer.nextToken();

      if (lexer.token == Symbol.IDENT)
        passo = assign_stmt(null, null);

      if (lexer.token != Symbol.RPAR)
        error.signal(") expected");

      lexer.nextToken();

      loopFaz = stmt(null, null);

      if (lexer.token != Symbol.ENDFOR)
        error.signal("Esqueceu da palavra-chave ENDFOR... só tô fazendo meu trabalho");

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
          error.signal("Eu preciso da palavra-chave 'READ' aqui");
      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("Você esqueceu do '(' do read");
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
          
          if (v == null)
            error.signal("Tentando ler uma variável (" + lexer.getStringValue() + ") não declarada?");
          
          if (v.getTipo() == "string")
            error.signal("Strings são constantes. Não faz sentido fazer um READ em uma constante");          

          idList.add(v);

          lexer.nextToken();

        } while (lexer.token == Symbol.COMMA);

        if (lexer.token != Symbol.RPAR)
            error.signal("Depois das variáveis a serem lidas, vem um ')'.Combinado?");
        lexer.nextToken();

        if (lexer.token != Symbol.SEMICOLON)
            error.signal("';' é o porquê da programação, ninguém sabe usar... (Pera, eu usei o porquê certo? Tem acento e é junto, né?)");
        
        return new readStmt(idList);
    }

    private Statement write_stmt(final ArrayList<Variable> checagem, final ArrayList<Variable> varLocal) {
      ArrayList<Variable> idList = new ArrayList<>();

      if (lexer.token != Symbol.WRITE)
          error.signal("Tudo que eu queria hoje era um 'WRITE' aqui");
      lexer.nextToken();

      if (lexer.token != Symbol.LPAR)
          error.signal("Você esqueceu o '(' do write");
      lexer.nextToken();

        do {
          if (lexer.token == Symbol.COMMA)
            lexer.nextToken();

          if (lexer.token != Symbol.IDENT)
            error.signal("Eu só posso escrever o conteúdo de variáveis. O que eu deveria escrever em um " + lexer.getStringValue());

          Variable v = variavelDeclarada(lexer.getStringValue());

          if (v == null && checagem != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), checagem);
          }
          
          if (v == null && varLocal != null) {
              v = variavelDeclaradaLocal(lexer.getStringValue(), varLocal);
          }
          
          if (v == null)
            error.signal("Tentando escrever uma variável (" + lexer.getStringValue() + ") não declarada?");         
          
          idList.add(v);

          lexer.nextToken();

        } while (lexer.token == Symbol.COMMA);

        if (lexer.token != Symbol.RPAR)
            error.signal("Depois do write tem um ')', você esqueceu");
        lexer.nextToken();

        if (lexer.token != Symbol.SEMICOLON)
            error.signal("AHA! ';' depois do write foi esquecido");
        
        return new writeStmt(idList);
    }

}
