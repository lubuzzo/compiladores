package AST;

public class Variable {

  public Variable( String name, String tipo, String valor, int linha ) {
      this.name = name;
      this.tipo = tipo;
      this.valor = valor;
      this.linhaDeclarada = linha;
  }

    public Variable( String name, String tipo, int linha ) {
        this.name = name;
        this.tipo = tipo;
        this.linhaDeclarada = linha;
    }

    public Variable( String name, int linha) {
        this.name = name;
        this.tipo = "void";
        this.linhaDeclarada = linha;
    }


    public String getTipo() {
      return this.tipo;
    }
    
    public int getLinha() {
        return this.linhaDeclarada;
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

    private String name;
    private String tipo;
    private String valor;
    private int linhaDeclarada;
}
