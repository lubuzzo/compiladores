package AST;

public class IfStatement extends Statement {

  public IfStatement( Expr expr, StatementList seEntao, StatementList seNao) {
    this.expr = expr;
    this.seEntao = seEntao;
    this.SeNao = seNao;
  }

  public void genC(PW pw) {
    pw.print("if ");
    //System.out.println("expt: " + expr);
    expr.genC(pw);
    pw.showln(" {");
    if (seEntao != null) {
      pw.add();
      seEntao.genC(pw);
      pw.sub();
      //pw.println("}");
    }
    if (SeNao != null) {
      pw.println("} else {");
      pw.add();
      SeNao.genC(pw);
      pw.sub();
    }
    pw.println("}");
  }

  public String stmtNome() {
      return "if";
  }

    public String getTipo() {
      return "null";
    }  
  
  private Expr expr;
  private StatementList seEntao;
  private StatementList SeNao;
}
