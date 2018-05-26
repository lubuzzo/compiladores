package Lexer;

import AST.Expr;
import java.util.*;
import Error.*;

public class Lexer {

    // apenas para verificacao lexica
    public static final boolean DEBUGLEXER = false;

    public Lexer( char []input, CompilerError error ) {
        this.input = input;
        // add an end-of-file label to make it easy to do the lexer
        input[input.length - 1] = '\0';
        // number of the current line
        lineNumber = 1;
        tokenPos = 0;
        this.error = error;
    }

    // contains the keywords - palavras reservadas da linguagem
    static private Hashtable<String, Symbol> keywordsTable;

    // this code will be executed only once for each program execution
    static {
        keywordsTable = new Hashtable<String, Symbol>();
        keywordsTable.put( "begin", Symbol.BEGIN );
        keywordsTable.put( "end", Symbol.END );
        keywordsTable.put( "int", Symbol.INT );
        keywordsTable.put( "float", Symbol.FLOAT );
        keywordsTable.put( "string", Symbol.STRING );
        keywordsTable.put( "void", Symbol.VOID );
        keywordsTable.put( "if", Symbol.IF );
        keywordsTable.put( "then", Symbol.THEN );
        keywordsTable.put( "else", Symbol.ELSE );
        keywordsTable.put( "endif", Symbol.ENDIF );
        keywordsTable.put( "for", Symbol.FOR );
        keywordsTable.put( "endfor", Symbol.ENDFOR );
        keywordsTable.put( "read", Symbol.READ );
        keywordsTable.put( "write", Symbol.WRITE );
        keywordsTable.put( "return", Symbol.RETURN );
        keywordsTable.put( "program", Symbol.PROGRAM );
        keywordsTable.put( "function", Symbol.FUNCTION );
        keywordsTable.put( ":=", Symbol.ASSIGN );

    }


    public void nextToken() {
      while((input[tokenPos] == '\n') || (input[tokenPos] == '\t') || (input[tokenPos] == ' ')) {
        if(input[tokenPos] == '\n')
          lineNumber++;
        tokenPos++;
      }

        /*chegou no fim do arquivo*/
      if(input[tokenPos] == '\0'){
        token = Symbol.EOF;
        return;
      }

	/*  verificando se eh comentario
            comentario é --, não //
        */
      if (input[tokenPos] == '-' && input[tokenPos+1] == '-'){
        while(input[tokenPos] != '\n' && input[tokenPos] != '\0'){
            tokenPos++;
        }
        nextToken(); /* Consome todos os espaços, procurando um token válido */
        return;
      }
        StringBuffer aux = new StringBuffer();

        /* quero reconhecer o token
        o token sera um dos Symbol */
        int countDecimal = 0;
        while ((Character.isDigit(input[tokenPos]) || ( (input[tokenPos] == '.') )) ){
          aux = aux.append(input[tokenPos]); /*concatenando. AINDA é string*/
          if (input[tokenPos] == '.')
              countDecimal++;
          tokenPos++;
        }

        if (aux.length() > 0) {
            //converte string para inteiro
            //se countDecimal == 1 eh float
            if (countDecimal == 1) {
                floatValue = Float.parseFloat(aux.toString());
                token = Symbol.FLOATLITERAL;
            } else if (countDecimal > 1) {
                error.signal("Float mal formatado");
            }
            //Token é inteiro
            else {
                numberValue = Integer.parseInt(aux.toString());
                if ((Integer.valueOf(aux.toString())).intValue() > MaxValueInteger){
                    error.signal("Estourou o maior inteiro permitido");
                } else {
                    token = Symbol.INTLITERAL;
                }
            }

        } else {
          boolean stringLiteral = false;
          if (input[tokenPos] == '"') {
            stringLiteral = true;
            tokenPos++;
          }

          if (stringLiteral) {
              while (input[tokenPos] != '"') {
                  aux = aux.append(input[tokenPos]);
                  tokenPos++;
              }
          }

            while ((!stringLiteral) && Character.isLetterOrDigit(input[tokenPos])){
              aux = aux.append(input[tokenPos]); //vai concatenando todas as letras, ainda eh string
              tokenPos++;
            }
            //System.out.println("getToken = " + aux);
            if (aux.length() > 0){
                Symbol temp;
                temp = keywordsTable.get(aux.toString()); /*Busca na tabela de Keywords, retorna símbolo se encontrar.*/
                if (temp == null){ //nao eh palavra
                  if (stringLiteral && aux.length() < 80) {
                    token = Symbol.STRINGLITERAL;
                    stringValue = aux.toString();
                  } else if (stringLiteral) {
                      token = Symbol.ERROR;
                      error.signal("String deve ter no máximo 80 caracteres");
                  }
                  else {
                    token = Symbol.IDENT;
                    //verificar se começa com letra && tem 30 caracteres no max
                    String conferencia_ident = aux.toString();
                    if(Character.isLetter(conferencia_ident.charAt(0)) && conferencia_ident.length() < 30)
                        stringValue = aux.toString();
                    else
                        error.signal("um identificador deve começar por uma letra e ser de tam max 30.");
                  }
                } else {
                    token = temp;
                }
                if (stringLiteral) {
                  if (input[tokenPos] != '"')
                    token = Symbol.ERROR;
                    tokenPos++;
                }
            } else{
                switch (input[tokenPos]){
                    case '+':
                        token = Symbol.PLUS;
                        break;
                    case '-':
                        token = Symbol.MINUS;
                        break;
                    case '/':
                        token = Symbol.DIV;
                        break;
                    case '*':
                        token = Symbol.MULT;
                        break;
                    case ',':
                        token = Symbol.COMMA;
                        break;
                    case ';':
                        token = Symbol.SEMICOLON;
                        break;
                    case '(':
                        token = Symbol.LPAR;
                        break;
                    case ')':
                        token = Symbol.RPAR;
                        break;
                    case '<':
                        token = Symbol.LT;
                        break;
                    case '>':
                        token = Symbol.GT;
                        break;
                    case ':':
                        aux = aux.append(input[tokenPos]);
                        tokenPos++;
                        if(input[tokenPos] == '=')
                            aux = aux.append(input[tokenPos]);
                        else
                            error.signal("erro léxicoo!");
                        Symbol temp;
                        temp = keywordsTable.get(aux.toString());
                        if (temp == Symbol.ASSIGN)
                            token = Symbol.ASSIGN;
                        break;
                    case '\"':
                      token = Symbol.ASPAS;
                      break;
                    case '=':
                      token = Symbol.EQUAL;
                      break;
                    default:
                        error.signal("erro lexico!!");
                }
                tokenPos++;
            }
        }


   if (DEBUGLEXER)
      System.out.println(token.toString());
      lastTokenPos = tokenPos - 1;
    }

    // return the line number of the last token got with getToken()
    public int getLineNumber() {
        return lineNumber;
    }

    public String getCurrentLine() {
        int i = lastTokenPos;
        //System.out.println(i);
        if ( i == 0 )
            i = 1;
        else
            if ( i >= input.length )
                i = input.length;

        StringBuffer line = new StringBuffer();
        // go to the beginning of the line
        while ( i >= 1 && input[i] != '\n' )
            i--;
        if ( input[i] == '\n' )
            i++;
        // go to the end of the line putting it in variable line
        while ( input[i] != '\0' && input[i] != '\n' && input[i] != '\r' ) {
            if (line.length() == 0 && (input[i] == ' ' || input[i] == '\t')) {
                //Limpando os tabs e espaço do comeco da mensagem de erro
            } else {
                line.append( input[i] );
            }
                i++;
        }
        return line.toString();
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getNumberValue() {
        return numberValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public char getCharValue() {
        return charValue;
    }

    // current token
    public Symbol token;
    private String stringValue;
    private int numberValue;
    private float floatValue;
    private char charValue;
    private String atribuicao;

    private int  tokenPos;
    //  input[lastTokenPos] is the last character of the last token
    private int lastTokenPos;
    // program given as input - source code
    private char []input;

    // number of current line. Starts with 1
    private int lineNumber;

    private CompilerError error;
    private static final int MaxValueInteger = 32768;
}
