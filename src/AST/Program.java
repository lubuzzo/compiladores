package AST;

import java.util.*;

public class Program {

    public Program( ArrayList<Variable> arrayVariable, StatementList as, ArrayList<functionDlc> dlc ) {
        this.arrayVariable = arrayVariable;
        this.statementList = as;
        this.dlc = dlc;
    }

    public void genC(PW pw) {

      pw.println("#include <stdio.h>");
      pw.println("#include <string.h>");
      pw.println("");

      if ( arrayVariable != null ) {
        for ( Variable v : arrayVariable ) {
          v.genC(pw);
        }
      }

      pw.println("");
      
      for (functionDlc fnc : dlc)
        fnc.genC(pw);

      pw.println("int main() {");

      pw.add();

      statementList.genC(pw);


      pw.println("return 0;");

      pw.sub();
      pw.println("}");
    }

    private ArrayList<Variable> arrayVariable;
    private StatementList statementList;
    private ArrayList<functionDlc> dlc;
}
