package AST;

abstract public class Expr {
    abstract public void genC(PW pw);

    abstract public String getTipo();
}
