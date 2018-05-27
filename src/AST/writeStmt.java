package AST;

import java.util.ArrayList;

public class writeStmt extends Statement {

  public writeStmt( ArrayList<Variable> idList ) {
    this.idList = idList;
  }

  public void genC(PW pw) {
    pw.print("printf (");

    int i = 0;

    for (; i < idList.size(); i++) {
        if (i == 0)
            pw.show("\"");

        if (idList.get(i).getTipo() == "int")
          pw.show("%d");
        else if (idList.get(i).getTipo() == "float")
          pw.show("%f");
        else if (idList.get(i).getTipo() == "string")
          pw.show("%s");

        if (i == idList.size() - 1)
            pw.show("\\n\", ");
        else
            pw.show(" ");
    }

    i = 0;
    for (Variable var : idList) {

      if (var.getTipo() == "string")
        pw.show("&");

      pw.show(var.getName());
      if (i++ != idList.size() - 1)
        pw.show(", ");
    }

    pw.showln(");");

  }
  
  public String stmtNome() {
      return "write";
  }

  public String getTipo() {
    return "null";
  }  


  private ArrayList<Variable> idList;
}
