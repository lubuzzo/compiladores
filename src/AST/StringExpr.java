package AST;

public class StringExpr extends Expr {

    public StringExpr( String value ) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void genC(PW pw) {
      pw.show(this.getValue());
    }

    public String getTipo() {
      return this.tipo;
    }

    private String value;
    private String tipo = "string";
}
