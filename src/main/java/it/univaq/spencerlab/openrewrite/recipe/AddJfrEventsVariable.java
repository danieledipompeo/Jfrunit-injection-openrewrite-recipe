package it.univaq.spencerlab.openrewrite.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class AddJfrEventsVariable extends Recipe {

    @Option(displayName = "Fully Qualified Class Name",
            description = "A fully qualified class name indicating which class to add a hello() method to.",
            example = "com.yourorg.FooBar")

    String jfrEventsVariableName = "jfrEvents";
    String jfrEventsVariableType = "JfrEvents";
    String jfrEventsVariableModifier = "public";

    // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
    @JsonCreator
    public AddJfrEventsVariable() {
    }

    @Override
    public String getDisplayName() {
        return "JfrRecipe";
    }

    @Override
    public String getDescription() {
        return "custom OpenRewrite recipe used to inject JfrUnit in JUnit 5 test.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new JfrUnitVisitor();
    }

    public class JfrUnitVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {

            // Check if the JfrAnnotation exists
            if (!classDecl.getLeadingAnnotations().stream().anyMatch(annotation -> annotation.getSimpleName().equals("JfrEventTest")))
                return classDecl;

            String jfrEventsType = "org.moditect.jfrunit.JfrEvents";

            // Don't add the variable if already exists
            List<J.VariableDeclarations> listOfVarDecl = classDecl.getBody().getStatements().stream()
                    .filter(st -> st instanceof J.VariableDeclarations)
                    .map(st -> (J.VariableDeclarations) st)
                    .toList();

            // Don't change the class if the body is not empty and the variable already exists
            if(!listOfVarDecl.isEmpty() && listOfVarDecl.stream()
                    .anyMatch( vd -> vd.getVariables().stream().anyMatch(v -> v.getType().toString().equals(jfrEventsType))))
                return classDecl;

            // Add the variable to the class
            JavaTemplate jfrEventsVariableTemplate = JavaTemplate
                    .builder(String.format("%s %s %s = new %s();", jfrEventsVariableModifier, jfrEventsVariableType, jfrEventsVariableName, jfrEventsVariableType))
                    .imports(jfrEventsType)
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2"))
                    .build();
            maybeAddImport(jfrEventsType);
            classDecl = jfrEventsVariableTemplate.apply(getCursor(), classDecl.getBody().getCoordinates().firstStatement());

            return classDecl;
        }
    }
}
