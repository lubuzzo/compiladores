package AST;

public class AssignmentStatement extends Statement {

    public AssignmentStatement( Variable v, Expr expr ) {
      this.v = v;
      this.expr = expr;
    }

    //Removidos '/n' por causa do For
    //Voltei, porque ficou feio sem

    public void genC(PW pw) {
      if (v.getTipo() != "string") {
        pw.print( v.getName() + " = " );
        expr.genC(pw);
        pw.showln(";");
      } else {
        pw.print( v.getName() + " = \"" );
        expr.genC(pw);
        pw.show("\"");
        pw.showln(";");
      }
    }

    public void condC(PW pw, boolean comeco) {
      pw.show( v.getName() + " = ");
      expr.genC(pw);
      if (comeco)
        pw.show("; ");
    }

  public String stmtNome() {
      return "assignment";
  }
  
  public String getTipo() {
      return "null";
  }
    
    private Variable v;
    private Expr expr;
}
