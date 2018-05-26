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
          //System.out.println( v.getTipo() + ": " + v.getName());
          v.genC(pw);
        }
      }

      pw.println("");
      
      for (functionDlc fnc : dlc)
        fnc.genC(pw);

      pw.println("int main() {");

      pw.add();

      //if (v.getTipo() != "string")
      //System.out.println(statementList.size());
        statementList.genC(pw);

      //arrayComandos.genC(pw);

      pw.println("return 0;");

      pw.sub();
      pw.println("}");
    }

    private ArrayList<Variable> arrayVariable;
    private StatementList statementList;
    private ArrayList<functionDlc> dlc;
}
