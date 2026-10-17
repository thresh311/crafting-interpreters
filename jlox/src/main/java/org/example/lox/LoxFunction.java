package org.example.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment localEnv = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params.size(); i++) {
            Token param = declaration.params.get(i);
            localEnv.define(param.lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, localEnv);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn" + declaration.name.lexeme + ">";
    }
}
