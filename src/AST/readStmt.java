package AST;

import java.util.ArrayList;

public class readStmt extends Statement {

  public readStmt( ArrayList<Variable> idList ) {
    this.idList = idList;
  }

  public void genC(PW pw) {
    pw.print("scanf (");

    int i = 0;

    for (; i < idList.size(); i++) {
        if (i == 0)
            pw.show("\"");

        if (idList.get(i).getTipo() == "int")
          pw.show("%d");
        else if (idList.get(i).getTipo() == "float")
          pw.show("%f");

        if (i == idList.size() - 1)
            pw.show("\", ");
        else
            pw.show(" ");

    }

    i = 0;
    for (Variable var : idList) {

      pw.show("&");
      
      var.simpleC(pw);
      if (i++ != idList.size() - 1)
        pw.show(", ");
    }

    pw.showln(");");

  }


  private ArrayList<Variable> idList;
}