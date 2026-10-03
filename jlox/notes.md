
## Chapter 3 - The Lox Language

**Expression:** Something that evaluates to a value. 1 + 2/x
- An expression followed by a semicolon get "promoted" into a statement. This is called **expression statement**.

**Statement:** A line of code that produces an effect. print "Hello"

## Chapter 4 - Scanning

**Objective:** Read a chain of characters as an input and group them together into chunks or "tokens". These tokens constitute the building blocks of a language.
- Scripting language: Language used to write simple sets of instructions to automate a manual process. (shells)
- It is good practice to keep the code that **generates** an error separate from the one that **reports** it to the user.

Lexeme: Smallest sequence of characters that still represent something in the language. By themselves, they are just raw substrings. 
Then, we can add more data to them to form  **tokens**:
- Token type: Indicates the corresponding element of the language for a lexeme.
- Literal value: Used for strings and numbers present in the code. It converts the string value of the lexeme into the object that will be used later by the interpreter.
- Location: Where the lexeme appeared inside the file.

### Challenges 

## Chapter 5 - Representing code

The representation produced by the parser, should simple to produce and easy for the interpreter to consume.
- One limitation of regular languages is that they can't handle expressions which can nest deeply.

**Context Free Grammar (CFG):** It is a formal grammar that takes a set of atomic pieces ("alphabet"), and then it defines a set (usually infinite) of "words" that are recognized by it. 

To be able to say if a word is recognized by the grammar, we write down a finite series of rules.


### Challenges
1 - Earlier, I said that the |, *, and + forms we added to our grammar metasyntax were just syntactic sugar. Take this grammar:
```
expr → expr ( "(" ( expr ( "," expr )* )? ")" | "." IDENTIFIER )+
| IDENTIFIER
| NUMBER
```
Produce a grammar that matches the same language but does not use any of that notational sugar.
```
expr -> expr subExpr1
expr -> IDENTIFIER
expr -> NUMBER
subExpr1 -> subExpr2 
subExpr1 -> subExpr2 subExpr1
subExpr2 -> "(" subExpr3 ")"
subExpr2 ->  "(" ")"
subExpr2 -> "." IDENTIFIER
subExpr3 -> expr
subExpr3 -> subExpr3 "," expr
```
Bonus: What kind of expression does this bit of grammar encode? Chained methods call and field accessors
Examples:
token.lexeme.toString()
math.sqrt(2,3)

## Chapter 6 - Parsing expressions

### Challenges 
1 - Add support for C like comma expressions. Give them the same precedence and associativity as in C. Write the grammar, and then implement the necessary parsing code.

The comma has the lowest precedence and its left associative (left-to-right).

2 - Likewise, add support for the C-style conditional or “ternary” operator ?:. What precedence level is allowed between the ? and :? Is the whole operator left-associative or right-associative?

Reference: https://en.cppreference.com/c/language/operator_precedence

The expression in the middle of the conditional operator (between ? and :) is parsed as if parenthesized: its precedence relative to ?: is ignored. 

The whole operator is right associative. The **operator associativity** is simply the way to **group operators with the same precedence** and **doesn't affect order of evaluation** in any way.

New grammar:
```
expression     → comma ;

comma -> coma "," ternary-conditional | ternary-conditional;
comma -> ternary-conditional ("," ternary-conditional)*;

ternary-conditional -> equality "?" expression ":" ternary-conditional | equality;
ternary-conditional -> (equality "?" expression ":")* equality;

equality       → equality comparison | comparison ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;

comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
| primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
| "(" expression ")" ;

```
3 - Add error productions to handle each binary operator appearing without a left-hand operand. In other words, detect a binary operator appearing at the beginning of an expression. Report that as an error, but also parse and discard a right-hand operand with the appropriate precedence.
New grammar:
```
expression     → comma ;

comma -> coma "," ternary-conditional | ternary-conditional;
comma -> ternary-conditional ("," ternary-conditional)*;

ternary-conditional -> equality "?" expression ":" ternary-conditional | equality;
ternary-conditional -> (equality "?" expression ":")* equality;

equality       → equality comparison | comparison ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;

comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
| error_prod
| primary ;
unary -> ("!" | "-")* (primary | error_prod) 
error_prod     ::= "," ternary
                 | ( "!=" | "==" ) comparison
                 | ( ">" | ">=" | "<" | "<=" ) term
                 | ( "-" | "+" ) factor
                 | ( "/" | "*" ) unary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
| "(" expression ")" ;

```
## Chapter 7 - Evaluating expressions

Up to this point, all errors that we had encountered where syntax or static errors. Those are detected and reported before any code is executed. _Runtime errors_ are failures that the language semantics demand we detect and report while the program is running.

- "We could print a runtime error and then abort the process and exit the application entirely. **That has a certain melodramatic flair. Sort of the programming language interpreter equivalent of a mic drop.**"

REPL (read-eval-print-loo): an interactive language shell. It takes single user inputs, executes them and returns the result to the user.

### Semantic choices
- The subexpressions in a binary expression are evaluated from left to right.
- In the case of a binary expression, we evaluate both operands before checking the type of _either_.
  - We could have specified that the left operand is checked before even evaluating the right one.

### Challenges
1 - Allowing comparisons on types other than numbers could be useful. The operators might have a reasonable interpretation for strings. Even comparisons among mixed types, like 3 < "pancake" could be handy to enable things like ordered collections of heterogeneous types. Or it could simply lead to bugs and confusion.

Would you extend Lox to support comparing other types? If so, which pairs of types do you allow and how do you define their ordering? Justify your choices and compare them to other languages.

(reference: https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.20)