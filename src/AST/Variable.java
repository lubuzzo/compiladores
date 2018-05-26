package AST;

public class Variable {

  public Variable( String name, String tipo, String valor ) {
      this.name = name;
      this.tipo = tipo;
      this.valor = valor;
  }

    public Variable( String name, String tipo ) {
        this.name = name;
        this.tipo = tipo;
    }

    public Variable( String name) {
        this.name = name;
        this.tipo = "void";
    }


    public String getTipo() {
      return this.tipo;
    }

    public String getName() { return name; }

    public void genC(PW pw) {
      //System.out.println(tipo);
        if (tipo == "string") {
          pw.println("char " + name + "[]" + " = \"" + valor + "\";");
          //pw.println()
        } else
          pw.println( tipo + " " + name + ";");
    }

    public void condC(PW pw) {
      pw.show(tipo + " " + name);
    }

    public void simpleC(PW pw) {
      pw.show(name);
    }

    private String name;
    private String tipo;
    private String valor;
}
