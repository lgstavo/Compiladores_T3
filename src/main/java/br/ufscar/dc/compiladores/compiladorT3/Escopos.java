package br.ufscar.dc.compiladores.compiladorT3;

import java.util.LinkedList;
import java.util.List;

public final class Escopos {

    private final LinkedList<TabelaDeSimbolos> pilhaDeTabelas;

    public Escopos() {
        pilhaDeTabelas = new LinkedList<>();
        createNewScope();
    }

    public void createNewScope() {
        pilhaDeTabelas.push(new TabelaDeSimbolos());
    }

    public TabelaDeSimbolos getActualScope() {
        return pilhaDeTabelas.peek();
    }

    public List<TabelaDeSimbolos> traverseNestedScopes() {
        return pilhaDeTabelas;
    }
}