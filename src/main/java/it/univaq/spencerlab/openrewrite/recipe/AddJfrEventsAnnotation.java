package it.univaq.spencerlab.openrewrite.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AddJfrEventsAnnotation extends Recipe {

    List<String> jdkEvents;

    @JsonCreator
    public AddJfrEventsAnnotation(@JsonProperty("enabledJdkEvents") String[] enabledJdkEvents) {
       this.jdkEvents =  Arrays.stream(enabledJdkEvents).toList();
    }

    @Override
    public String getDisplayName() {
        return "AddJfrEventsAnnotation";
    }

    @Override
    public String getDescription() {
        return "This recipe adds the `@EnableEvent` annotation to the test methods annotated with `@Test`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new JfrUnitVisitor();
    }

    public class JfrUnitVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDecl, ExecutionContext executionContext) {

            // Don't change the class if it is not a test method
            if(!methodDecl.getLeadingAnnotations().stream().anyMatch(a -> a.getSimpleName().contains("Test")))
                return methodDecl;

            String enableEventSimpleName = "EnableEvent";
            // Don't add the annotation if already exists
            if(methodDecl.getLeadingAnnotations().stream().anyMatch(annotation -> annotation.getSimpleName().contains(enableEventSimpleName)))
                return methodDecl;

            String fullyQualifiedNameJfrEventTypes = "org.moditect.jfrunit.EnableEvent";

            // create a string where each row contains @EnableEvent annotation with the event name from the jdkEvents list
            String annotationsString = jdkEvents.stream().map(jdkEvent -> String.format("@EnableEvent(\"%s\")", jdkEvent))
                    .collect(Collectors.joining("\n"));

            JavaTemplate jfrEnableEventTemplate = JavaTemplate.builder(annotationsString)
                    .imports(fullyQualifiedNameJfrEventTypes)
                    .javaParser(JavaParser.fromJavaVersion()
                            .classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2"))
                    .build();
            maybeAddImport(fullyQualifiedNameJfrEventTypes);

            // Add the annotation
            methodDecl = jfrEnableEventTemplate.apply(getCursor(),
                    methodDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));

            return methodDecl;

        }
    }
}
