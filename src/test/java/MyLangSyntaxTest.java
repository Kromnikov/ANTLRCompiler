import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by makst on 03.03.2017.
 */
public class MyLangSyntaxTest {
    MyLangLexer lexer;
    MyLangParser parser;

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
    }

//    @Test
//    public void testLexerWithCorrectProgram() {
//        setUp("testLexer.txt");
//        parser.parse();
//        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 0);
//    }

    @Test
    public void testWithCorrectProgram() {
        setUp("syntax/testCorrectSyntax.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 0);
    }

    @Test
    public void testDefinition() {
        setUp("syntax/testDefinition.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testProgramClosure() {
        setUp("syntax/testProgramClosure.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testBeginWithoutEnd() {
        setUp("syntax/testBeginWithoutEnd.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testEndWithoutBegin() {
        setUp("syntax/testEndWithoutBegin.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testElseWithoutIf() {
        setUp("syntax/testElseWithoutIf.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testForCircle() {
        setUp("syntax/testForCircle.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 5);
    }

    @Test
    public void testInsertedForCircle() {
        setUp("syntax/testInsertedForCircle.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testWhileCircle() {
        setUp("syntax/testWhileCircle.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 2);
    }

    @Test
    public void testDoubleCommentClosure() {
        //todo
        setUp("syntax/testComments.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 0);
    }

    @Test
    public void testWrongExpression() {
        setUp("syntax/testWrongExpression.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testUseUninitializedVariableInForCircle(){
        setUp("syntax/testUseUninitializedVariableInForCircle.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 1);
    }

    @Test
    public void testIncorrectOperator(){
        setUp("syntax/testIncorrectOperator.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 2);
    }
}