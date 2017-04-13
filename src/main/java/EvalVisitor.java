/**
 * Created by makst on 04.03.2017.
 */

import exceptions.SemanticAnalyzerException;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.mvel2.MVEL;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvalVisitor extends MyLangBaseVisitor<Value> {

    // used to compare floating point numbers
    public static final String WARNING = "WARNING: ";
    public static final String ERROR = "ERROR: ";
    public static final String DEBUG = "DEBUG: ";
    public static final String THE_USE_OF_AN_UNINITIALIZED_VARIABLE = " use of an uninitialized variable: ";
    public static final String THE_USE_OF_AN_UNDEFINED_VARIABLE = " use of an undefined variable: ";
    public static final String REAL_TYPE = "!";
    public static final String INT_TYPE = "$";
    public static final String BOOLEAN_TYPE = "%";

    protected Map<String, Value> IDValueMemory = new HashMap<String, Value>();
    protected Map<String, Types> IDTypeMemory = new HashMap<String, Types>();
    protected Map<String, Integer> IDScopeMemory = new HashMap<String, Integer>();


    private List<String> errors = new ArrayList<String>();
    private List<String> debug = new ArrayList<String>();

    private boolean forAssignment;
    private int scope = 0;

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    // assignment/id overrides
    @Override
    public Value visitAssignment(MyLangParser.AssignmentContext ctx) {
        String id = ctx.ID().getText();
        Value value = this.visit(ctx.expr());
        if (forAssignment) {
            debug.add(DEBUG + "definition: id=" + id + " type - " + REAL_TYPE + " scope - " + scope);
            forAssignment = false;
            checkIDExistence(id);
            IDTypeMemory.put(id, Types.REAL);
        } else {
            checkIDDefinition(id);
        }
        debug.add(DEBUG + "id: " + id + " value:" + value);
        validateType(id, value);
        IDValueMemory.put(id, value);
        return value;
    }

    private void validateType(String id, Value value) {
        boolean isValid = false;
        Types type = IDTypeMemory.get(id);
        switch (type) {
            case INTEGER:
                isValid = value.isInteger();
                break;
            case BOOLEAN:
                isValid = value.isBoolean();
                break;
            case REAL:
                isValid = value.isDouble() || value.isInteger();
                break;
        }
        if (!isValid) {
            throw new SemanticAnalyzerException("Incompatible value " + value.toString() + " for ID " + id);
        }
    }

    private void checkIDExistence(String id) {
        if (IDTypeMemory.get(id) != null) {
            throw new SemanticAnalyzerException("The variable  \"" + id + "\" already exists");
        }
    }

    private void checkIDDefinition(String id) {
        if (IDTypeMemory.get(id) == null) {
            errors.add(ERROR + THE_USE_OF_AN_UNDEFINED_VARIABLE + id);
            throw new SemanticAnalyzerException(THE_USE_OF_AN_UNDEFINED_VARIABLE + id);
        }
    }

    private Value checkIDInitialize(String id) {
        Value value = IDValueMemory.get(id);
        if (value == null) {
            errors.add(ERROR + THE_USE_OF_AN_UNINITIALIZED_VARIABLE + id);
            throw new SemanticAnalyzerException(ERROR + THE_USE_OF_AN_UNINITIALIZED_VARIABLE + id);
        }
        return value;
    }

    @Override
    public Value visitComposite_operator(MyLangParser.Composite_operatorContext ctx) {
        scope++;
        visitChildren(ctx);
        removeOverdueID();
        scope--;
        return Value.VOID;
    }

    @Override
    public Value visitDefinition(MyLangParser.DefinitionContext ctx) {
        for (TerminalNode id : ctx.ID()) {
            String idName = id.getText();
            String idType = ctx.TYPE().getText();
            checkIDExistence(idName);
            IDTypeMemory.put(idName, Types.getType(idType));
            IDScopeMemory.put(idName, scope);
            debug.add(DEBUG + "definition: id=" + idName + " type - " + idType + " scope - " + scope);
        }
        return Value.VOID;
    }

    @Override
    public Value visitIdAtom(MyLangParser.IdAtomContext ctx) {
        String id = ctx.getText();
        checkIDDefinition(id);
        return checkIDInitialize(id);
    }

    // atom overrides
    @Override
    public Value visitNumberAtom(MyLangParser.NumberAtomContext ctx) {
        String value = ctx.getText();
        if (isBin(value)) {
            return new Value(Integer.parseInt(value.substring(0, value.length() - 1), 2));
        }
        if (isOct(value)) {
            return new Value(Integer.parseInt(value.substring(0, value.length() - 1), 8));
        }
        if (isHex(value)) {
            return new Value(Integer.parseInt(value.substring(0, value.length() - 1), 16));
        }
        if (isInteger(value)) {
            return new Value(Integer.valueOf(value));
        }
        return new Value(Double.valueOf(value));
    }

    private boolean isBin(String value) {
        Matcher matcher = Pattern.compile("^[0-1]*(B|b)$").matcher(value);
        return matcher.matches();
    }

    private boolean isOct(String value) {
        Matcher matcher = Pattern.compile("^[0-7]*(O|o)$").matcher(value);
        return matcher.matches();
    }

    private boolean isHex(String value) {
        Matcher matcher = Pattern.compile("^[0-9A-Fa-f]*(H|h)$").matcher(value);
        return matcher.matches();
    }

    @Override
    public Value visitBooleanAtom(MyLangParser.BooleanAtomContext ctx) {
        return new Value(Boolean.valueOf(ctx.getText()));
    }

    // expr overrides
    @Override
    public Value visitParExpr(MyLangParser.ParExprContext ctx) {
        return this.visit(ctx.expr());
    }

    @Override
    public Value visitUnaryMinusExpr(MyLangParser.UnaryMinusExprContext ctx) {
        Value value = this.visit(ctx.expr());
        return new Value(-value.asDouble());
    }

    @Override
    public Value visitNotExpr(MyLangParser.NotExprContext ctx) {
        Value value = this.visit(ctx.expr());
        return new Value(!value.asBoolean());
    }

    @Override
    public Value visitMultiplicationExpr(@NotNull MyLangParser.MultiplicationExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        if (!(isValidType(left) & isValidType(right))) {
            throw new SemanticAnalyzerException("wrong type in expr: " + ctx.op.getLine());
        }

        switch (ctx.op.getType()) {
            case MyLangParser.MULT:
                return new Value(MVEL.eval(left.toString() + '*' + right.toString()));
            case MyLangParser.DIV:
                if (left.isInteger() && right.isInteger()) {
                    return new Value(left.asInteger() / right.asInteger());
                }
                return new Value(MVEL.eval(left.toString() + '/' + right.toString()));
            default:
                throw new SemanticAnalyzerException("unknown operator: " + MyLangParser.tokenNames[ctx.op.getType()]);
        }
    }

    private boolean isValidType(Value value) {
        return value.isDouble() || value.isInteger();
    }

    @Override
    public Value visitAdditiveExpr(@NotNull MyLangParser.AdditiveExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        if (!(isValidType(left) & isValidType(right))) {
            throw new SemanticAnalyzerException("wrong type in expr, line: " + ctx.op.getLine());
        }

        switch (ctx.op.getType()) {
            case MyLangParser.PLUS:
                return new Value(MVEL.eval(left.toString() + '+' + right.toString()));
            case MyLangParser.MINUS:
                return new Value(MVEL.eval(left.toString() + '-' + right.toString()));
            default:
                throw new SemanticAnalyzerException("unknown operator: " + MyLangParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitRelationalExpr(@NotNull MyLangParser.RelationalExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch (ctx.op.getType()) {
            case MyLangParser.LT:
                return new Value(left.asDouble() < right.asDouble());
            case MyLangParser.LTEQ:
                return new Value(left.asDouble() <= right.asDouble());
            case MyLangParser.GT:
                return new Value(left.asDouble() > right.asDouble());
            case MyLangParser.GTEQ:
                return new Value(left.asDouble() >= right.asDouble());
            default:
                throw new SemanticAnalyzerException("unknown operator: " + MyLangParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitEqualityExpr(@NotNull MyLangParser.EqualityExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch (ctx.op.getType()) {
            case MyLangParser.EQ:
                return new Value(MVEL.eval(left.toString() + "==" + right.toString()));
            case MyLangParser.NEQ:
                return new Value(MVEL.eval(left.toString() + "!=" + right.toString()));
            default:
                throw new SemanticAnalyzerException("unknown operator: " + MyLangParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitAndExpr(MyLangParser.AndExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() && right.asBoolean());
    }

    @Override
    public Value visitOrExpr(MyLangParser.OrExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() || right.asBoolean());
    }

    // writeln override
    @Override
    public Value visitOutput(MyLangParser.OutputContext ctx) {
        Value value = this.visit(ctx.expr());
        System.out.println(value);
        return value;
    }

    @Override
    public Value visitInput(MyLangParser.InputContext ctx) {
        String id = ctx.ID().getText();
        Scanner keyboard = new Scanner(System.in);
        String idValue = keyboard.next();
        Value value = Value.VOID;
        switch (IDTypeMemory.get(id)) {
            case INTEGER:
                value = new Value(Integer.valueOf(idValue));
                break;
            case BOOLEAN:
                value = new Value(Boolean.valueOf(idValue));
                break;
            case REAL:
                value = new Value(Double.valueOf(idValue));
                break;
        }
        IDValueMemory.put(id, value);
        return value;
    }

    // if override
    @Override
    public Value visitIf_stat(MyLangParser.If_statContext ctx) {

        MyLangParser.Condition_blockContext condition = ctx.condition_block();

        boolean evaluatedBlock = false;
        Value evaluated = this.visit(condition.expr());
        if (evaluated.asBoolean()) {
            evaluatedBlock = true;
            // evaluate this block whose expr==true
            this.visit(condition.stat());
        }

        if (!evaluatedBlock && ctx.stat() != null) {
            // evaluate the else-stat_block (if present == not null)
            this.visit(ctx.stat());
        }

        return Value.VOID;
    }

    // while override
    @Override
    public Value visitWhile_stat(MyLangParser.While_statContext ctx) {

        Value value = this.visit(ctx.expr());

        while (value.asBoolean()) {

            // evaluate the code block
            this.visit(ctx.stat());

            // evaluate the expression
            value = this.visit(ctx.expr());
        }

        return Value.VOID;
    }

    @Override
    public Value visitFor_stat(@NotNull MyLangParser.For_statContext ctx) {
        scope++;
        forAssignment = true;
        MyLangParser.AssignmentContext assignmentCtx = ctx.assignment();
        List<MyLangParser.ExprContext> exprContextList = ctx.expr();

        Double value = this.visit(assignmentCtx).asDouble();
        Double step = exprContextList.size() > 1 ? this.visit(exprContextList.get(1)).asDouble() : 1;
        Double to = this.visit(exprContextList.get(0)).asDouble();

        if ((value < to && step <= 0) || (value > to && step >= 0)) {
            getErrors().add(WARNING + "infinite loop ");
            return Value.VOID;
        }
        String forID = assignmentCtx.ID().getText();
        if (value > to) {
            for (; value >= to; value += step) {
                IDValueMemory.put(forID, new Value(value));
                this.visit(ctx.block());
                removeOverdueID();
            }
        } else if (value < to) {
            for (; value <= to; value += step) {
                IDValueMemory.put(forID, new Value(value));
                this.visit(ctx.block());
                removeOverdueID();
            }
        }

        debug.add(DEBUG + "remove: id=" + forID);
        IDTypeMemory.remove(forID);
        IDValueMemory.remove(forID);
        scope--;
        return Value.VOID;
    }

    void removeOverdueID() {
        Map<String, Integer> buffer = new HashMap<String, Integer>();
        buffer.putAll(IDScopeMemory);
        for (Map.Entry<String, Integer> entry : buffer.entrySet()) {
            if (scope > 0 & entry.getValue() >= scope) {
                IDScopeMemory.remove(entry.getKey());
                IDTypeMemory.remove(entry.getKey());
                IDValueMemory.remove(entry.getKey());
                debug.add(DEBUG + "remove: id=" + entry.getKey());
            }
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getDebug() {
        return debug;
    }

    public void setDebug(List<String> debug) {
        this.debug = debug;
    }

    public void printErrors() {
        System.out.println();
        System.out.println("Errors:");
        for (String error : errors) {
            System.out.println(error);
        }
    }

    public void printDebug() {
        System.out.println();
        for (String entry : debug) {
            System.out.println(entry);
        }
    }

}
