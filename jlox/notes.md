
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

