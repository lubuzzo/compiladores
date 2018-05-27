import java.io.*;
import AST.*;

public class Main {
    public static void main( String []args ) {

        File file;
        FileReader stream;
        int numChRead;

        if ( args.length != 1 ) {
            System.out.println("Não tem condições de trabalhar assim.");
            System.out.print("Eu só trabalho com 1 parâmetro, o nome do arquivo LITTLE que eu vou ler");
        }
        else {
           file = new File(args[0]);
           if ( ! file.exists() || ! file.canRead() ) {
             System.out.println("O arquivo " + args[0] + " não existe, ou eu não consegui ler");
             throw new RuntimeException();
           }
           try {
             stream = new FileReader(file);
            } catch ( FileNotFoundException e ) {
                System.out.println("Algo deu errado. Parece que o arquivo foi excluído depois que comecei a ler");
                throw new RuntimeException();
            }
            
           char []input = new char[ (int ) file.length() + 1 ];

            try {
              numChRead = stream.read( input, 0, (int ) file.length() );
            } catch ( IOException e ) {
                System.out.println("Erro enquanto lia o arquivo " + args[0]);
                throw new RuntimeException();
            }

            if ( numChRead != file.length() ) {
                System.out.println("Erro de leitura");
                throw new RuntimeException();
            }
            try {
              stream.close();
            } catch ( IOException e ) {
                System.out.println("Erro com o arquivo " + args[0]);
                throw new RuntimeException();
            }

            String outputFileName;
            outputFileName = args[0].replace(".", "-") + ".c";
            

            Compiler compiler = new Compiler();
            Program p = compiler.compile(input);
            if (p != null) {

                FileOutputStream  outputStream;
                try {
                   outputStream = new FileOutputStream(outputFileName);
                } catch ( IOException e ) {
                    System.out.println("Não achei o arquivo " + args[1]);
                    return ;
                }

                //Printar código C na tela
                //PrintWriter printWriter = new PrintWriter(System.out, true);
                PrintWriter printWriter = new PrintWriter(outputStream, true);
                
                PW pw = new PW();
                pw.set(printWriter);                
                
                p.genC(pw);
            }
        }
    }
}
