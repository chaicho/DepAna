

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class testStaticParser {
    @Test
    public void testStaticMemberAccess() {
        String code = "import java.io.File;\n" +
                "public class testClass {\n" +
                "  public void test(){\n" +
                "     String sep = File.seperator; \n" +
                "  }\n" +
                "}";
        TypeSolver solver = new CombinedTypeSolver();
        SymbolResolver resolver = new JavaSymbolSolver(solver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(resolver);
        CompilationUnit cu = StaticJavaParser.parse(code);
        cu.findAll(FieldAccessExpr.class).forEach(fa -> {
//          fa represents "File.separator" here, fa.getScope() represents "File"
//          The "File" is treated as a NameExpression
            fa.calculateResolvedType();
            assertEquals(fa.getScope().getClass(), NameExpr.class);
        });
    }
}
