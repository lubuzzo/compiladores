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

  private Expr retorno;
}
