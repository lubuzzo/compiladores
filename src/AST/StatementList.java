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

  private ArrayList<Statement> v;

}
