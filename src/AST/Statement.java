package AST;

abstract public class Statement {
  abstract public void genC(PW pw);
  abstract public String stmtNome(); 
  abstract public String getTipo();
}
