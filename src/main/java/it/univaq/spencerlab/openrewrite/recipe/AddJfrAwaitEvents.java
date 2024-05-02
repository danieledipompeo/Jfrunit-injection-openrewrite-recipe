package it.univaq.spencerlab.openrewrite.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.JavaTemplate;

public class AddJfrAwaitEvents extends Recipe {

    @JsonCreator
    public AddJfrAwaitEvents(){}

    @Override
    public String getDisplayName() {
        return "AddJfrAwaitEvents";
    }

    @Override
    public String getDescription() {
        return "This recipe adds the `jfrEvents.awaitEvents()` method invocation to the test methods annotated with `@EnableEvent`.";
    }

     @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new JfrUnitVisitor();
    }

    public class JfrUnitVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDecl, ExecutionContext executionContext) {

            // Don't change the class if any jfr event is enabled
            if(methodDecl.getLeadingAnnotations().stream().noneMatch(a -> a.getSimpleName().contains("EnableEvent")))
                return methodDecl;

            String awaitJfrEvents = "jfrEvents.awaitEvents()";
            // Don't invoke awaitEvents if already exists
            if(methodDecl.getBody() == null || methodDecl.getBody().getStatements().stream().anyMatch(statement -> statement.toString().contains(awaitJfrEvents)))
                return methodDecl;

            JavaTemplate jfrEnableEventTemplate = JavaTemplate.builder(awaitJfrEvents).build();

            // Add the annotation
            methodDecl = jfrEnableEventTemplate.apply(getCursor(),
                    methodDecl.getBody().getCoordinates().lastStatement());

            return methodDecl;

        }
    }

}
