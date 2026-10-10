package org.example.lox;

import static org.example.lox.TokenType.MINUS;

public class AstRPNPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    public String visitTernaryExpr(Expr.Ternary expr) {
        return postfix(expr.firstOperator.lexeme, expr.left, expr.middle, expr.right);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return postfix(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        String op = expr.operator.lexeme;
        //Disambiguate polish notation
        if (expr.operator.type == MINUS) {
            op = "~";
        }
        return postfix(op, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        StringBuilder builder = new StringBuilder();

        builder.append(expr.name.lexeme)
                .append(" ")
                .append(expr.value.accept(this))
                .append(" ")
                .append("=");

        return builder.toString();
    }

    private String postfix(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this))
                    .append(" ");
        }
        builder.append(name);

        return builder.toString();
    }

}
