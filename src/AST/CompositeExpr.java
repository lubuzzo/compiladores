package AST;

import Lexer.*;

public class CompositeExpr extends Expr {

    public CompositeExpr( Expr pleft, Symbol poper, Expr pright ) {
      left = pleft;
      oper = poper;
      right = pright;
    }

    public void genC(PW pw) {
      pw.show("(");
      left.genC(pw);
      if (oper == Symbol.EQUAL)
        pw.show(" == ");
      else
        pw.show(" " + oper.toString() + " ");
      right.genC(pw);
      pw.show(")");
    }

    public String getTipo() {

//      System.out.println("left: " + this.left.getTipo() + " right: " + this.right.getTipo());

      if (this.left.getTipo() == "string" || this.right.getTipo() == "string" ) {
        if ((this.left.getTipo().equals(this.right.getTipo())))
          return "string";
        else
          return "umaString";
        }
      else if (this.left.getTipo() == "int" && this.right.getTipo() == "int")
        return "int";
      else if (this.left.getTipo() == "float" || this.right.getTipo() == "float" )
        return "float";

      return "null";
    }

    private Expr left, right;
    private Symbol oper;
}
