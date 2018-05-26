package AST;

import java.util.ArrayList;

public class functionDlc extends Statement {

  public functionDlc( String functionType, String functionName, ArrayList<Variable> parametros, ArrayList<Variable> fncVariaveis, StatementList sl) {
    this.functionType = functionType;
    this.functionName = functionName;
    this.parametros = parametros;
    this.fncVariaveis = fncVariaveis;
    this.sl = sl;
  }

  public void genC(PW pw) {
    int i = 0;
    pw.show(functionType + " ");

    pw.show(functionName + " (");

    if ( parametros != null ) {
      for ( Variable v : parametros ) {
        v.condC(pw);
        if (i++ != parametros.size() - 1)
            pw.show(", ");
      }
    }

    pw.showln(") {");
    pw.add();


    if ( fncVariaveis != null ) {
      for ( Variable v : fncVariaveis ) {
        v.genC(pw);
      }
    }

    sl.genC(pw);

    pw.sub();
    pw.println("}\n");
  }

  public String getName() {
    return this.functionName;
  }

  public String getTipo() {
    return this.functionType;
  }

  private String functionType;
  private String functionName;
  private ArrayList<Variable> parametros;
  private ArrayList<Variable> fncVariaveis;
  private StatementList sl;
}