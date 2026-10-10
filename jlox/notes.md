
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
- Because we don't check that a variable is not already registered in an environment when we define it, it is possible to use an expression statement to redefine an existing variable. 
  - This works better when the user is in the middle of a REPL session, since they don't have to mentally track which variables already exist.
```
var a = "before";
print a; // "before".
var a = "after";
print a; // "after".
```
- What happens when we can't find variable in an environment? We could make it a syntax error, a runtime error or return a default value. 
  - In the case of Lox, we choose to make it a runtime error. Making it a syntax error would make defining recursive functions much more difficult.
  - _Using_ a variable is not the same as _referring_ to it. You can refer to a variable in a chunk of code without immediately evaluating it, for example, when it is located inside the body of a function.
  - In Java, a program is a set of declarations which all come into being simultaneously. The implementation declares all of the names before looking at the bodies of any of the functions.
```
fun isOdd(n) {
  if (n == 0) return false;
  return isEven(n - 1);
}

fun isEven(n) {
  if (n == 0) return true;
  return isOdd(n - 1);
}
```

### Challenges
1 - Allowing comparisons on types other than numbers could be useful. The operators might have a reasonable interpretation for strings. Even comparisons among mixed types, like 3 < "pancake" could be handy to enable things like ordered collections of heterogeneous types. Or it could simply lead to bugs and confusion.

Would you extend Lox to support comparing other types? If so, which pairs of types do you allow and how do you define their ordering? Justify your choices and compare them to other languages.

(reference: https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.20)

## Chapter 8 - Statements and state
We will incorporate an internal state to the interpreter, so that it can remember the associations between identifiers and values specified by a user.

**Statements**, by definition, don't evaluate to a value. They produce a **side effect**. This could be producing user-visible output or modifying the state of the interpreter.

Types of statements:
1 - Expression statement: Lets you place an expression where a statement is expected. They exist to evaluate expressions that have side effects. For example, function calls followed by ';' (hola();) or 1 + 2;
2 - print statement: Evaluates an expression and displays the result to the user.
  - This could be made into a library function, instead of baking into the language.
3 - Variable declaration: Creates a binding between a name and a value.
  - These types of statements are not allowed in control flow statements. (branches of an if or body of a while) This is done to avoid confusion about variable lifetime and scope.
```
if (monday) var beverage = "espresso"; //Does it stop existing after the if? Does it only exist when the condition is true?
```
4 - Block statement: It is a (possibly empty) series of statements or declarations enclosed in curly braces.

There are two levels "precedence" for statements: Some places, like inside a block or at the top level, allow any kind of statement. Others allow only statements that don't declare a name.

Type of expressions:
1 - Variable expression: When a variable identifier is used as an expression, it looks up the value associated to that name and returns it.
2 - Assignment: Is an expression and not an assignment. It is the lowest precedence expression form.
  - It is right associative.

New Lox syntax rules:
```
program        → declaration* EOF ; //Statring point of the grammar. Represents a complete Lox script or REPL entry.

declaration   -> varDecl 
                 | statement; //Later add function and class declarations to this rule

varDecl       -> "var" IDENTIFIER ("=" expression)? ";";

statement      → exprStmt
               | printStmt
               | block ;

block          → "{" declaration* "}" ;
exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;

expression     → assignment ;
assignment     → IDENTIFIER "=" assignment
               | equality ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;
```

**Environments**: They store the bindings that associate variables to values.

### Problems with parsing an assignment

A single token lookahead recursive decent parser can't see far enough to tell that it's parsing an assignment until _after_ it has gone through the left-hand side and encountered the =. This may occur may tokens later. 

```
makeList().head.next = node;
```

This doesn't affect other binary operators, like '+', because the left-hand side of an assignment is an expression that **doesn't evaluate to a value**. It a _pseudo-expression_ that evaluates to an "object" you can assign to.
  - The classic terms for these two constructs are l-value and r-value.
  - All other expressions implemented so far are r-values.
  - An l-value "evaluates" to a location you can assign into.

Example:
```
var a = "before";
a = "value"; //The left side is not evaluating 'a' (which return "before"). Instead, we identify the variable where to sore the right-hand side.
```

The trick to parse it is realizing that every assigment target (left-hand) is also valid syntax as a normal expression. We first parse the left-side as a normal expression (r-value), then when we find an '=' operator, we check if this expression is a valid assignment target. If that's the case, then we convert the r-value expression node into an l-value. If it's not, then we have an error.

### Scope
It defines a region where a name is associated to a certain entity. The same name might refer to different things depending on the context. There are two types of scopes:
  - **Lexical scope (or static scope):** It is possible to determine the start and end of the scope just by looking at the code. 
  - **Dynamic scope:** It isn't possible to determine what a name refers to until you execute the program. This happens in Lox for methods and fields on objects.
```
class Saxophone {
  play() {
    print "Careless Whisper";
  }
}

class GolfClub {
  play() {
    print "Fore!";
  }
}

fun playIt(thing) {
  thing.play();
}
```

The way we implement scopes it through **environments**. As the interpreter walks though the syntax tree nodes, the ones that affect scope will change the environment (block scope). 
  - When we enter a new block scope, we create a new environment that will contain only the variables declared in that scope. When we exit the block, we discard its environment.
  - In the case of variables that are declared in an enclosing scope and not _shadowed_, we need to chain the environments together. When we look up a variable, we walk the chain from the innermost to the outermost environment until we find it. 

**Shadowing:** When a local variable has the same name as a variable in an enclosing scope, the code inside the block looses access to the outer variable. 
