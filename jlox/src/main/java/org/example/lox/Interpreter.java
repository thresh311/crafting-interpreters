package org.example.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        this.evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        System.out.println(stringify(this.evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            }
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r) {
                    return l + r;
                }
                if (left instanceof String l && right instanceof String r) {
                    return l + r;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
        }

        // Unreachable.
        throw new IllegalArgumentException("Found a binary operator that doesn't have evaluation logic: %s".formatted(expr.operator));
        //return null;
    }

    private void checkNumberOperands(Token operator, Object right, Object left) {
        if (right instanceof Double && left instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);

    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG -> {
                return !isTruthy(right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            }
        }

        // Unreachable.
        throw new IllegalArgumentException("Found a unary operator that doesn't have evaluation logic: %s".formatted(expr.operator));
        //return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            //Distinction of Integer-valued doubles in Java
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

}
