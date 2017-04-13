import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 284 on 31.03.2017.
 */
public class MyLangLexisTest {

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

    @Test
    public void testNumberTypes(){
        setUp("lexis/testNumberTypes.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 6);
    }

    @Test
    public void testID(){
        setUp("lexis/testID.txt");
        parser.parse();
        Assert.assertEquals(parser.getNumberOfSyntaxErrors(), 2);
    }
    @Test
    public void testSeparator(){
        setUp("lexis/testSeparator.txt");
        try {
            parser.parse();
        } catch (Exception ex) {
            Assert.fail();
        }
    }
}
