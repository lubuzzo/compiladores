package AST;

public class VariableExpr extends Expr {

    public VariableExpr( Variable v ) {
        this.v = v;
    }

    public void genC(PW pw) {
        pw.show( v.getName() );
    }

    public String getName() {
      return v.getName();
    }

    public String getTipo() {
      return v.getTipo();
    }

    private Variable v;
    //private String tipo = "variable";
}
