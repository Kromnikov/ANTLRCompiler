grammar Polygon;

@header {
    import org.mvel2.MVEL;
}

program
    : '{' definition* operator* '}'
    ;

compositeOperator
        :  BEGIN operator* END
        ;

operator
    : compositeOperator
    | output
    | input
    | assign
    | r_if
    | cycle
    ;

cycle
    : r_for
    | r_while
    ;

r_for
    : FOR assign TO expression operator NEXT
    | FOR assign TO expression STEP expression operator NEXT
    ;

r_while
    : WHILE '(' expression ')' operator
    ;

r_if
    : IF '(' exp=expression ')' operator
    | IF '(' exp=expression ')' operator ELSE operator
    ;

output
    : WRITELN b1=expression ';' //{ System.out.println($b1.value); }
//    | WRITELN a1=expression ';' //{ System.out.println($a1.value); }
    ;

input
    : READLN v1=Variable ';' { System.out.println("hey"); }
    ;

definition
    : var1=Variable ( ',' var2=Variable)* ':' Type ';'
    ;

assign
    : v=Variable ':=' exp=expression ';'
    ;


expression returns [boolean value]
    : b1=booleanExp  //{ $value = $b1.value; }
    | b1=booleanExp (lo=LogicalOperation b2=booleanExp)*
     // { $value = (Boolean)MVEL.eval($b1.value + $lo.text + $b2.value);}
    ;

booleanExp returns [boolean value]
    : o1=additionExp (lo=LogicalComparison o2=additionExp)*
     // { $value = (Boolean)MVEL.eval($o1.value + $lo.text + $o2.value);}
    | '(' exp=expression ')' //{ $value = $exp.value; }
    ;


additionExp returns [double value]
    : s1=multiplyExp //{$value = $s1.value;}
    ( '+' s2=multiplyExp //{$value += $s2.value;}
    | '-' s2=multiplyExp //{$value -= $s2.value;}
    )*
;

multiplyExp returns [double value]
    : m1=atomExp //{$value = $m1.value;}
    ( '*' m2=atomExp //{$value *= $m2.value;}
    | '/' m2=atomExp //{$value /= $m2.value;}
    )*
;


atomExp returns [double value]
    :  '-'? n=Number // {$value = Double.parseDouble($n.text);}
    |  '-'? var=Variable    //{$value = Double.parseDouble($var.value);}
    | '(' op=additionExp ')' //{$value = $op.value;}
    ;

Variable
    : LETTER (LETTER | Dec)*
    ;


Number
    : Integer
    | Real
    ;

Integer
    : Dec
    | Bin
    | Oct
    | Hex
    ;

Bin : ('0'|'1')+ ('b'|'B');
Oct : ('0'..'7')+ ('o'|'O');
Dec : ('0'..'9')+ ;
Hex : ('0'..'9'|'a'..'f'|'A'..'F')+ ('h'|'H');

Real
    : Dec? ('.' Dec)
    | Dec? ('.' Dec) Mantissa
    ;

Mantissa : E ('+'|'-')? Dec ;

E : 'E' | 'e' ;

LogicalConst
    : 'true'
    | 'false'
    ;

LogicalComparison
    : '>'
    | '<'
    | '=='
    | '<='
    | '>='
    ;

LogicalOperation
    : '&&'
    | '||'
    ;


COMMENT : '/*' (.)*? '*/' -> skip;

WS : [ \r\n\t] + -> channel (HIDDEN);

WRITELN : 'writeln';

READLN : 'readln';

BEGIN : 'begin';

END : 'end';

IF : 'if';

ELSE : 'else';

FOR : 'for';

WHILE : 'while';

TO : 'to';

NEXT : 'next';

STEP : 'step';

Type
    : '!'
    | '%'
    | '$'
    ;

LETTER
   : ('a' .. 'z') | ('A' .. 'Z')
   ;
