package org.example.lox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> declaredVariables = new HashSet<>();
    //Could also be implement using a flag Object
//    private final Object UNINITIALIZED = new Object();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if(declaredVariables.contains(name.lexeme)) {
            if (values.containsKey(name.lexeme)) {
                return values.get(name.lexeme);
            }

            throw new RuntimeError(name,
                    "The variable hasn't been initialized nor assigned to '" + name.lexeme + "'.");
        }

        if(enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name) {
        declaredVariables.add(name);
        values.remove(name); // When redeclaring a variable without an initial value inside a scope, we mark it as uninitialized again
    }

    void define(String name, Object value) {
        declaredVariables.add(name);
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (declaredVariables.contains(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if(enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '%s'.".formatted(name.lexeme));
    }

}
