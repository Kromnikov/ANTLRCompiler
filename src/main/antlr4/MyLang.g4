grammar MyLang;

parse
 : '{'  (stat | definition)+ '}' EOF
 ;

block
 : stat*
 ;

stat
 : assignment SCOL
 | composite_operator
 | if_stat
 | while_stat
 | for_stat
 | output
 | input
 | OTHER {System.err.println("unknown char: " + $OTHER.text);}
 ;

composite_operator
 : begin=BEGIN block end=END
 ;

definition
    : var1=ID (',' var2=ID)* ':' type SCOL
    ;

assignment
 : ID ASSIGN expr
 ;

if_stat
 : IF condition_block (ELSE stat)?
 ;

condition_block
 : expr stat
 ;

while_stat
 : WHILE LPAR expr RPAR stat
 ;

for_stat
 : FOR assignment TO expr (STEP expr)? block NEXT
 ;

output
 : WRITELN expr SCOL
 ;

input
 : READLN ID SCOL
 ;

expr
 : MINUS expr                           #unaryMinusExpr
 | NOT expr                             #notExpr
 | expr op=(MULT | DIV ) expr           #multiplicationExpr
 | expr op=(PLUS | MINUS) expr          #additiveExpr
 | expr op=(LTEQ | GTEQ | LT | GT) expr #relationalExpr
 | expr op=(EQ | NEQ) expr              #equalityExpr
 | expr AND expr                        #andExpr
 | expr OR expr                         #orExpr
 | atom                                 #atomExpr
 ;

atom
 : LPAR expr RPAR #parExpr
 | (n=NUMBER)  #numberAtom
 | (TRUE | FALSE) #booleanAtom
 | ID             #idAtom
 ;

OR : 'or';
AND : 'and';
EQ : 'EQ';
NEQ : 'NE';
GT : 'GT';
LT : 'LT';
GTEQ : 'GE';
LTEQ : 'LE';
PLUS : 'plus';
MINUS : 'min';
MULT : 'mult';
DIV : 'div';
NOT : '~';

SCOL : ';';
ASSIGN : ':=';
LPAR : '(';
RPAR : ')';
BEGIN : 'begin';
END : 'end';

TRUE : 'true';
FALSE : 'false';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
WRITELN : 'writeln';
READLN : 'readln';
TO : 'to';
NEXT : 'next';
STEP : 'step';
FOR : 'for';


ID
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

type
    : 'real'
    | 'boolean'
    | 'integer'
    ;

NUMBER
    : INTEGER
    | REAL
    ;

INTEGER
    : DEC
    | BIN
    | OCT
    | HEX
    ;

BIN : ('0'|'1')+ ('b'|'B');
OCT : ('0'..'7')+ ('o'|'O');
DEC : ('0'..'9')+ ;
HEX : ('0'..'9'|'a'..'f'|'A'..'F')+ ('h'|'H');

REAL
    : DEC? ('.' DEC)
    | DEC? ('.' DEC) MANTISSA
    | DEC MANTISSA
    ;

MANTISSA : E ('+'|'-')? DEC ;

E : 'E' | 'e' ;

COMMENT : '/*' (.)*? '*/' -> skip;

SPACE
 : [ \t\r\n] -> skip
 ;

OTHER
 : .
 ;