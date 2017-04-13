import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 284 on 30.03.2017.
 */
public class MyLangInterpretationTest {
    MyLangLexer lexer;
    MyLangParser parser;
    EvalVisitor visitor;
    ParseTree tree;

    public void setUp(String fileName) {
        InputStream stream = MyLang.class.getClassLoader().getResourceAsStream(fileName);
        ANTLRInputStream in = null;
        try {
            in = new ANTLRInputStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lexer = new MyLangLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new MyLangParser(tokens);
        tree = parser.parse();
        visitor = new EvalVisitor();
    }

    @Test
    public void testAssignmentAndDefinition() {
        setUp("interpretation/testAssignment.txt");
        visitor.visit(tree);

        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";

        Assert.assertEquals(visitor.IDTypeMemory.get(a).toString(), "INTEGER");
        Assert.assertEquals(visitor.IDValueMemory.get(a).toString(), "123");

        Assert.assertEquals(visitor.IDTypeMemory.get(b).toString(), "REAL");
        Assert.assertEquals(visitor.IDValueMemory.get(b).toString(), "123.0");

        System.out.println(visitor.IDTypeMemory.get(c));
        Assert.assertEquals(visitor.IDTypeMemory.get(c).toString(), "BOOLEAN");
        Assert.assertEquals(visitor.IDValueMemory.get(c).toString(), "false");

        System.out.println(visitor.IDTypeMemory.get(d));
        Assert.assertEquals(visitor.IDTypeMemory.get(d).toString(), "INTEGER");
        Assert.assertEquals(visitor.IDValueMemory.get(d), null);
    }

    @Test
    public void testUnaryMinusExpr() {
        setUp("interpretation/testUnaryMinusExpr.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("b").toString(), "-10.0");
    }

    @Test
    public void testNotExpr() {
        setUp("interpretation/testNotExpr.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("b").toString(), "false");
    }

    @Test
    public void testIntegerCalculation() {
        setUp("interpretation/testIntegerCalculation.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("b").toString(), "2792");
        Assert.assertEquals(visitor.IDValueMemory.get("d").toString(), "279.2");
    }

    @Test
    public void testRelationalExpr() {
        setUp("interpretation/testRelationalExpr.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("a").toString(), "false");
        Assert.assertEquals(visitor.IDValueMemory.get("b").toString(), "true");
        Assert.assertEquals(visitor.IDValueMemory.get("c").toString(), "true");
        Assert.assertEquals(visitor.IDValueMemory.get("d").toString(), "true");
    }

    @Test
    public void testEqualityExpr() {
        setUp("interpretation/testEqualityExpr.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("a").toString(), "false");
        Assert.assertEquals(visitor.IDValueMemory.get("b").toString(), "false");
    }

    @Test
    public void testLogicalOperators() {
        setUp("interpretation/testLogicalOperators.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("c").toString(), "false");
        Assert.assertEquals(visitor.IDValueMemory.get("d").toString(), "true");
    }

    @Test
    public void testIfStatement() {
        setUp("interpretation/testIfStatement.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("e").toString(), "155");
    }

    @Test
    public void testWhileStatement() {
        setUp("interpretation/testWhileStatement.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("factorial").toString(), "24");
    }

    @Test
    public void testForStatement() {
        setUp("interpretation/testForStatement.txt");
        visitor.visit(tree);

        Assert.assertEquals(visitor.IDValueMemory.get("factorial").toString(), "24.0");
    }
}
