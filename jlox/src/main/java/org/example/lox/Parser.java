package org.example.lox;

import java.util.List;

import static org.example.lox.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }


    private Expr expression() {
        return commaSeparation();
    }

    private Expr commaSeparation() {
        Expr expr = ternaryConditional();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternaryConditional();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr ternaryConditional() {
        Expr left = equality();

        if (!match(QUESTION_MARK)) return left;

        Token firstOp = previous();
        //Reason: https://en.cppreference.com/c/language/operator_precedence
        Expr middle = expression();
        Token secondOp = consume(COLON, "Expect ':' after expression in ternary conditional");
        Expr right = ternaryConditional();

        return new Expr.Ternary(left, firstOp, middle, secondOp, right);
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        if(match(COMMA)) {
            Token operator = previous();
            error(operator, "Missing left operand for operator '%s'".formatted(operator.lexeme));
            ternaryConditional();
            return unary();
        }

        if(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            error(operator, "Missing left operand for operator '%s'".formatted(operator.lexeme));
            comparison();
            return unary();
        }

        if(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            error(operator, "Missing left operand for operator '%s'".formatted(operator.lexeme));
            term();
            return unary();
        }

        if(match(PLUS, MINUS)) {
            Token operator = previous();
            error(operator, "Missing left operand for operator '%s'".formatted(operator.lexeme));
            factor();
            return unary();
        }

        if(match(STAR, SLASH)) {
            Token operator = previous();
            error(operator, "Missing left operand for operator '%s'".formatted(operator.lexeme));
            unary();
            return unary();
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

           throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    //Synchronization on statement boundary;
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
