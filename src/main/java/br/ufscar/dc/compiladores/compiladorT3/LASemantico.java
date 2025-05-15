package br.ufscar.dc.compiladores.compiladorT3;

import static br.ufscar.dc.compiladores.compiladorT3.LASemanticoUtils.verifyType;
import static br.ufscar.dc.compiladores.compiladorT3.LASemanticoUtils.adicionaErroSemantico;
import static br.ufscar.dc.compiladores.compiladorT3.LASemanticoUtils.verificaCompatibilidade;
import br.ufscar.dc.compiladores.compiladorT3.TabelaDeSimbolos.TipoT3;
import org.antlr.v4.runtime.Token;

// Visitante semântico principal responsável por percorrer a AST e realizar verificações semânticas.
public class LASemantico extends AlgumaBaseVisitor<Void> {

    // Tabela de símbolos global.
    TabelaDeSimbolos tabela;

    // Gerenciador de escopos aninhados.
    static Escopos nestedScope = new Escopos();

    // Tabela de símbolos do escopo atual.
    TabelaDeSimbolos tabelaEscopo;

    // Adiciona uma variável à tabela de símbolos do escopo atual com validações de tipo e duplicidade.
    public void adicionaVariavelTabela(String nome, String tipo, Token nomeT, Token tipoT) {
        tabelaEscopo = nestedScope.getActualScope();

        TipoT3 tipoItem;

        switch (tipo) {
            case "literal": tipoItem = TipoT3.LITERAL; break;
            case "inteiro": tipoItem = TipoT3.INTEIRO; break;
            case "real":    tipoItem = TipoT3.REAL;    break;
            case "logico":  tipoItem = TipoT3.LOGICO;  break;
            default:        tipoItem = TipoT3.INVALIDO; break;
        }

        if (tipoItem == TipoT3.INVALIDO) {
            adicionaErroSemantico(tipoT, "tipo " + tipo + " nao declarado");
        }

        if (!tabelaEscopo.existe(nome)) {
            tabelaEscopo.adicionar(nome, tipoItem);
        } else {
            adicionaErroSemantico(nomeT, "identificador " + nome + " ja declarado anteriormente");
        }
    }

    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracoes(AlgumaParser.DeclaracoesContext ctx) {
        tabela = nestedScope.getActualScope();
        for (AlgumaParser.Decl_local_globalContext declaracao : ctx.decl_local_global()) {
            visitDecl_local_global(declaracao);
        }
        return super.visitDeclaracoes(ctx);
    }

    @Override
    public Void visitDecl_local_global(AlgumaParser.Decl_local_globalContext ctx) {
        tabela = nestedScope.getActualScope();
        if (ctx.declaracao_local() != null) {
            visitDeclaracao_local(ctx.declaracao_local());
        } else if (ctx.declaracao_global() != null) {
            visitDeclaracao_global(ctx.declaracao_global());
        }
        return super.visitDecl_local_global(ctx);
    }

    @Override
    public Void visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {
        tabela = nestedScope.getActualScope();

        String tipoVariavel;
        String nomeVariavel;

        if (ctx.getText().contains("declare")) {
            tipoVariavel = ctx.variavel().tipo().getText();
            for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador()) {
                nomeVariavel = ident.getText();
                adicionaVariavelTabela(nomeVariavel, tipoVariavel, ident.getStart(), ctx.variavel().tipo().getStart());
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        tabela = nestedScope.getActualScope();

        for (AlgumaParser.IdentificadorContext id : ctx.identificador()) {
            if (!tabela.existe(id.getText())) {
                adicionaErroSemantico(id.getStart(), "identificador " + id.getText() + " nao declarado");
            }
        }

        return super.visitCmdLeia(ctx);
    }

    @Override
    public Void visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        tabela = nestedScope.getActualScope();

        for (AlgumaParser.ExpressaoContext expressao : ctx.expressao()) {
            verifyType(tabela, expressao);
        }

        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        tabela = nestedScope.getActualScope();

        verifyType(tabela, ctx.expressao());

        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        tabela = nestedScope.getActualScope();

        TipoT3 tipoExpressao = verifyType(tabela, ctx.expressao());
        String varNome = ctx.identificador().getText();

        if (tipoExpressao != TipoT3.INVALIDO) {
            if (!tabela.existe(varNome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + varNome + " nao declarado");
            } else {
                TipoT3 varTipo = verifyType(tabela, varNome);

                if (varTipo == TipoT3.INTEIRO || varTipo == TipoT3.REAL) {
                    if (!verificaCompatibilidade(varTipo, tipoExpressao)) {
                        if (tipoExpressao != TipoT3.INTEIRO) {
                            adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + varNome);
                        }
                    }
                } else if (varTipo != tipoExpressao) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + varNome);
                }
            }
        }

        return super.visitCmdAtribuicao(ctx);
    }
}
