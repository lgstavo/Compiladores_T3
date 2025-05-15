package br.ufscar.dc.compiladores.compiladorT3;

// Importações necessárias para leitura, escrita e análise sintática.
import br.ufscar.dc.compiladores.compiladorT3.AlgumaParser.ProgramaContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {

    public static void main(String[] args) throws IOException {

        // Abre o arquivo de saída para escrita.
        try (PrintWriter pw = new PrintWriter(new FileWriter(args[1]))) {
            try {
                // Lê o arquivo de entrada (args[0]) como fluxo de caracteres.
                CharStream cs = CharStreams.fromFileName(args[0]);

                // Inicializa o analisador léxico.
                AlgumaLexer lexer = new AlgumaLexer(cs);

                // Constrói o fluxo de tokens a partir do léxico.
                CommonTokenStream tokens = new CommonTokenStream(lexer);

                // Inicializa o parser e gera a árvore sintática do programa.
                AlgumaParser parser = new AlgumaParser(tokens);
                ProgramaContext arvore = parser.programa();

                // Cria o analisador semântico e inicia a análise a partir da árvore.
                LASemantico las = new LASemantico();
                las.visitPrograma(arvore);

                // Escreve todos os erros semânticos encontrados, se houver.
                LASemanticoUtils.errosSemanticos.forEach(pw::println);

                // Indica o fim da análise.
                pw.println("Fim da compilacao");
                pw.close();

            } catch (RuntimeException e) {
                // Silencia exceções de tempo de execução (não recomendado em produção).
            }
        }
    }
}
