package br.ufscar.dc.compiladores.compiladorT3;

// Importações necessárias
import br.ufscar.dc.compiladores.compiladorT3.TabelaDeSimbolos.TipoT3;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class LASemanticoUtils {

    // Lista que armazena os erros semânticos encontrados
    public static List<String> errosSemanticos = new ArrayList<>();

    // Adiciona um erro semântico à lista, evitando duplicatas
    public static void adicionaErroSemantico(Token tok, String mensagem) {
        int linha = tok.getLine();
        if (!errosSemanticos.contains("Linha " + linha + ": " + mensagem))
            errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }

    // Verifica se dois tipos são compatíveis para operações aritméticas
    public static boolean verificaCompatibilidade(TipoT3 T1, TipoT3 T2) {
        boolean flag = false;

        if (T1 == TipoT3.INTEIRO && T2 == TipoT3.REAL)
            flag = true;
        else if (T1 == TipoT3.REAL && T2 == TipoT3.INTEIRO)
            flag = true;
        else if (T1 == TipoT3.REAL && T2 == TipoT3.REAL)
            flag = true;

        return flag;
    }

    // Verifica compatibilidade de tipos para operações relacionais/lógicas
    public static boolean verificaCompatibilidadeLogica(TipoT3 T1, TipoT3 T2) {
        boolean flag = false;

        if (T1 == TipoT3.INTEIRO && T2 == TipoT3.REAL)
            flag = true;
        else if (T1 == TipoT3.REAL && T2 == TipoT3.INTEIRO)
            flag = true;

        return flag;
    }

    // Verifica o tipo resultante de uma expressão aritmética
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Exp_aritmeticaContext ctx) {
        TipoT3 tipoRetorno = verifyType(tabela, ctx.termo().get(0));

        for (var termoArit : ctx.termo()) {
            TipoT3 tipoAtual = verifyType(tabela, termoArit);

            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoT3.INVALIDO))
                tipoRetorno = TipoT3.REAL;
            else
                tipoRetorno = tipoAtual;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de um termo (parte de uma expressão aritmética)
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.TermoContext ctx) {
        TipoT3 tipoRetorno = verifyType(tabela, ctx.fator().get(0));

        for (AlgumaParser.FatorContext fatorArit : ctx.fator()) {
            TipoT3 tipoAtual = verifyType(tabela, fatorArit);

            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoT3.INVALIDO))
                tipoRetorno = TipoT3.REAL;
            else
                tipoRetorno = tipoAtual;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de um fator (parte de um termo aritmético)
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.FatorContext ctx) {
        TipoT3 tipoRetorno = null;

        for (AlgumaParser.ParcelaContext parcela : ctx.parcela())
            tipoRetorno = verifyType(tabela, parcela);

        return tipoRetorno;
    }

    // Verifica o tipo de uma parcela (parte de um fator)
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null)
            return verifyType(tabela, ctx.parcela_unario());
        else
            return verifyType(tabela, ctx.parcela_nao_unario());
    }

    // Verifica o tipo de uma parcela unária
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Parcela_unarioContext ctx) {
        TipoT3 tipoRetorno;
        String nome;

        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();

            if (tabela.existe(nome))
                tipoRetorno = tabela.verificar(nome);
            else {
                TabelaDeSimbolos tabelaAux = LASemantico.nestedScope.traverseNestedScopes().get(LASemantico.nestedScope.traverseNestedScopes().size() - 1);
                if (!tabelaAux.existe(nome)) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + nome + " nao declarado");
                    tipoRetorno = TipoT3.INVALIDO;
                } else
                    tipoRetorno = tabelaAux.verificar(nome);
            }
        } else if (ctx.NUM_INT() != null)
            tipoRetorno = TipoT3.INTEIRO;
        else if (ctx.NUM_REAL() != null)
            tipoRetorno = TipoT3.REAL;
        else
            tipoRetorno = verifyType(tabela, ctx.exp_aritmetica().get(0));

        return tipoRetorno;
    }

    // Verifica o tipo de uma parcela não unária
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Parcela_nao_unarioContext ctx) {
        TipoT3 tipoRetorno;
        String nome;

        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();

            if (!tabela.existe(nome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + nome + " nao declarado");
                tipoRetorno = TipoT3.INVALIDO;
            } else
                tipoRetorno = tabela.verificar(nome);
        } else
            tipoRetorno = TipoT3.LITERAL;

        return tipoRetorno;
    }

    // Verifica o tipo de uma expressão lógica
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.ExpressaoContext ctx) {
        TipoT3 tipoRetorno = verifyType(tabela, ctx.termo_logico(0));

        for (AlgumaParser.Termo_logicoContext termoLogico : ctx.termo_logico()) {
            TipoT3 tipoAtual = verifyType(tabela, termoLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoT3.INVALIDO)
                tipoRetorno = TipoT3.INVALIDO;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de um termo lógico
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Termo_logicoContext ctx) {
        TipoT3 tipoRetorno = verifyType(tabela, ctx.fator_logico(0));

        for (AlgumaParser.Fator_logicoContext fatorLogico : ctx.fator_logico()) {
            TipoT3 tipoAtual = verifyType(tabela, fatorLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoT3.INVALIDO)
                tipoRetorno = TipoT3.INVALIDO;
        }
        return tipoRetorno;
    }

    // Verifica o tipo de um fator lógico
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Fator_logicoContext ctx) {
        return verifyType(tabela, ctx.parcela_logica());
    }

    // Verifica o tipo de uma parcela lógica
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null)
            return verifyType(tabela, ctx.exp_relacional());
        else
            return TipoT3.LOGICO;
    }

    // Verifica o tipo de uma expressão relacional
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, AlgumaParser.Exp_relacionalContext ctx) {
        TipoT3 tipoRetorno = verifyType(tabela, ctx.exp_aritmetica().get(0));

        if (ctx.exp_aritmetica().size() > 1) {
            TipoT3 tipoAtual = verifyType(tabela, ctx.exp_aritmetica().get(1));

            if (tipoRetorno == tipoAtual || verificaCompatibilidadeLogica(tipoRetorno, tipoAtual))
                tipoRetorno = TipoT3.LOGICO;
            else
                tipoRetorno = TipoT3.INVALIDO;
        }

        return tipoRetorno;
    }

    // Verifica o tipo de uma variável na tabela de símbolos
    public static TipoT3 verifyType(TabelaDeSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}
