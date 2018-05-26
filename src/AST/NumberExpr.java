package AST;

public class NumberExpr extends Expr {

    public NumberExpr( int value ) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public void genC(PW pw) {
      pw.show(Integer.toString(value));
    }

    public String getTipo() {
      return this.tipo;
    }

    private int value;
    private String tipo = "int";
}
