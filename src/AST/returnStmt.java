package AST;

public class returnStmt extends Statement {

  public returnStmt( Expr retorno ) {
    this.retorno = retorno;
  }

  public void genC(PW pw) {
    pw.print("return ");

    if (retorno != null)
      retorno.genC(pw);
    else
      pw.show("void");
    pw.showln(";");
  }
  
  public String stmtNome() {
      return "return";
  }  
  
    public String getTipo() {
        return retorno.getTipo();
  }

  private Expr retorno;
}
