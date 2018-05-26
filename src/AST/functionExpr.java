package AST;

import java.util.ArrayList;

public class functionExpr extends Expr {

    public functionExpr( String funcName, ArrayList<Variable> e, String tipo ) {
      this.funcName = funcName;
      this.e = e;
      this.tipo = tipo;
    }

    public void genC(PW pw) {
        pw.show( funcName + "(" );

        int i = 0;

        if ( e != null ) {
          for ( Variable v : e ) {
            pw.show(v.getName());
            if (i++ != e.size() - 1)
                pw.show(", ");
          }
        }

        pw.show(")");
    }

    public String getTipo() {
      return this.tipo;
    }


    private String funcName;
    private ArrayList<Variable> e;
    private String tipo;
}
