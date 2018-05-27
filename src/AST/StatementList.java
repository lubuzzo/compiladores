package AST;

import java.util.*;

public class StatementList {

  public StatementList(ArrayList<Statement> v) {
    this.v = v;
  }

  public void genC(PW pw) {
    if (v != null) {
      for (Statement s : v) {
          if (s != null)
            s.genC(pw);
      }
    }
  }
  
  public void add(Statement s) {
      this.v.add(s);
  }
  
  public Statement getRetorno() {
      for (int i = 0; i < this.v.size(); i++) {
        if (this.v.get(i).stmtNome().equals("return")) {
          return v.get(i);
        }
      }
      return null;
  }

  private ArrayList<Statement> v;

}
