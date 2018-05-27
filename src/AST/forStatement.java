package AST;

public class forStatement extends Statement {

  public forStatement( AssignmentStatement asgt, Expr condicao, AssignmentStatement passo, StatementList loopFaz) {
    this.asgt = asgt;
    this.condicao = condicao;
    this.passo = passo;
    this.loopFaz = loopFaz;
  }

  public void genC(PW pw) {
    pw.print("for ( ");
    //System.out.println("expt: " + expr);
    if (asgt != null)
      asgt.condC(pw, true);
    //pw.show(" ;");

    if (condicao != null)
      condicao.genC(pw);
    pw.show("; ");

    if (passo != null)
      passo.condC(pw, false);
    pw.showln(" ) {");

    pw.add();
    //pw.show("")
    loopFaz.genC(pw);
    pw.sub();

    pw.println("}");
  }

  public String stmtNome() {
      return "for";
  }

  public String getTipo() {
    return "null";
  }
  
  private AssignmentStatement asgt;
  private Expr condicao;
  private AssignmentStatement passo;
  private StatementList loopFaz;
}
