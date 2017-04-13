import exceptions.SemanticAnalyzerException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 284 on 29.03.2017.
 */
public class MyLangSemanticsTest {
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
    public void testIncorrectAssignment() {
        setUp("semantics/testIncorrectAssignment.txt");
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("Incompatible value"));
        }
    }

    @Test(expectedExceptions = SemanticAnalyzerException.class, expectedExceptionsMessageRegExp
            = "The variable .* already exists")
    public void testAlreadyExistingVariableInForCircle() {
        setUp("semantics/testAlreadyExistingVariableInForCircle.txt");
        visitor.visit(tree);
    }

    @Test
    public void testUseUndefinedVariable() {
        setUp("semantics/testUseUndefinedVariable.txt");
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("use of an undefined variable"));
        }
    }

    @Test
    public void testUseUndefinedVariableInForCircle() {
        setUp("semantics/testUseUndefinedVariableInForCircle.txt");
        visitor.visit(tree);
        Assert.assertEquals(visitor.IDValueMemory.get("a"), null);
//        Assert.assertEquals(visitor.IDTypeMemory.get("a"), null);
    }

    @Test
    public void testUseUninitializedVariable() {
        setUp("semantics/testUseUninitializedVariable.txt");
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("use of an uninitialized variable"));
        }
    }

    @Test
    public void testIncorrectTypeInAddition() {
        setUp("semantics/testIncorrectTypeInAddition.txt");
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("wrong type in expr"));
        }
    }

    @Test
    public void testIncorrectTypeInMultiplication() {
        setUp("semantics/testIncorrectTypeInMultiplication.txt");
        try {
            visitor.visit(tree);
        } catch (RuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("wrong type in expr"));
        }
    }

    @Test
    public void testInfinityLoopInPositiveDirection() {
        setUp("semantics/testInfinityLoopInPositiveDirection.txt");
        visitor.visit(tree);
        visitor.printErrors();
        Assert.assertTrue(visitor.getErrors().get(0).contains("infinite loop"));
    }

    @Test
    public void testInfinityLoopInNegativeDirection() {
        setUp("semantics/testInfinityLoopInNegativeDirection.txt");
        visitor.visit(tree);
        visitor.printErrors();
        Assert.assertTrue(visitor.getErrors().get(0).contains("infinite loop"));
    }
}
