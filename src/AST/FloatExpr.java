package AST;

public class FloatExpr extends Expr {

    public FloatExpr( float value ) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void genC(PW pw) {
      pw.show(Float.toString(value));
    }

    public String getTipo() {
      return this.tipo;
    }

    private float value;
    private String tipo = "float";
}
