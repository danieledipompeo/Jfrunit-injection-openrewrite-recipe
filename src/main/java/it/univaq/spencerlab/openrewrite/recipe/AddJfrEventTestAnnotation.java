package it.univaq.spencerlab.openrewrite.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Comparator;

// Making your recipe immutable helps make them idempotent and eliminates categories of possible bugs.
// Configuring your recipe in this way also guarantees that basic validation of parameters will be done for you by rewrite.
// Also note: All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
public class AddJfrEventTestAnnotation extends Recipe {
    @Option(displayName = "Fully Qualified Class Name",
            description = "A fully qualified class name indicating which class to add a hello() method to.",
            example = "com.yourorg.FooBar")

    String jfrEventFullyQualifiedName = "org.moditect.jfrunit.JfrEventTest";
    String jfrEventSimpleName = "JfrEventTest";

    // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
    @JsonCreator
    public AddJfrEventTestAnnotation() {
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

            // Don't change the class if it is not a test class
            if (!classDecl.getSimpleName().contains("Test"))
                return classDecl;

            // Don't add the annotation if already exists
            if (classDecl.getLeadingAnnotations().stream().anyMatch(annotation -> annotation.getSimpleName().equals(jfrEventSimpleName))) {
                return classDecl;
            }

            // Interpolate the fullyQualifiedClassName into the template and use the resulting LST to update the class body
            JavaTemplate jfrEventTestAnnotationTemplate = JavaTemplate.builder("@" + jfrEventSimpleName)
                .imports(jfrEventFullyQualifiedName)
                    // https://rewriteoss.slack.com/archives/C01A843MWG5/p1714384083519139?thread_ts=1713961631.720619&cid=C01A843MWG5
                    // one option is to add a META-INF/rewrite/classpath/jfrunit-core-1.0.0.Alpha2.jar  to your recipe library,
                    // and then use .classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2"))
                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2"))
                    //.javaParser(JavaParser.fromJavaVersion().classpath("jfrunit-core"))
                .build();
            maybeAddImport(jfrEventFullyQualifiedName);
            classDecl = jfrEventTestAnnotationTemplate.apply(getCursor(), classDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));

            String jfrEventsVariableName = "jfrEvents";
            String jfrEventsVariableType = "JfrEvents";
            String jfrEventsVariableModifier = "public";

            JavaTemplate jfrEventsVariableTemplate = JavaTemplate.builder(String.format("%s %s %s = new %s();", jfrEventsVariableModifier, jfrEventsVariableType, jfrEventsVariableName, jfrEventsVariableType))
                    .imports("org.moditect.jfrunit.JfrEvents")
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2"))
                    .build();

           // classDecl = jfrEventsVariableTemplate.apply(getCursor(), classDecl.getBody().getCoordinates().firstStatement());

            return classDecl;
        }
    }
}
