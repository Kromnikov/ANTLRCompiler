import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;

public class MyLang {
    public static void main(String[] args) throws Exception {
        InputStream stream = MyLang.class.getClassLoader().getResourceAsStream("test.txt");
        ANTLRInputStream in = new ANTLRInputStream(stream);
        MyLangLexer lexer = new MyLangLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MyLangParser parser = new MyLangParser(tokens);
//        parser.parse();
//        System.out.println(parser.getNumberOfSyntaxErrors());

        ParseTree tree = parser.parse();
        EvalVisitor visitor = new EvalVisitor();
        visitor.visit(tree);
        //visitor.printErrors();
        visitor.printDebug();


    }
}