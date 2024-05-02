package it.univaq.spencerlab.openrewrite.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddJfrEventsAnnotationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        /*spec.recipeFromYaml(
            """
            ---
            type: specs.openrewrite.org/v1beta/recipe
            name: it.univaq.spencerlab.openrewrite.recipe.AddJDKEventsAnnotationWrapper
            displayName: AddJDKEventsAnnotationWrapper
            description: Applies Recipe B.
            recipeList:
              - it.univaq.spencerlab.openrewrite.recipe.AddJDKEventsAnnotation:
                    enabledJdkEvents:
                        - jdk.G1*
                        - jdk.GC*
            """)//, "it.univaq.spencerlab.openrewrite.recipe.AddJDKEventsAnnotation")*/
          spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "it.univaq.spencerlab.openrewrite.recipe.AddJDKEventsAnnotation")
                  .parser(JavaParser.fromJavaVersion()
                          .classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2","junit-jupiter-api-5.10.2"));
        //spec.recipe(new AddJDKEventsAnnotation(spec.);
//        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2","junit-jupiter-api-5.10.2"));
        System.out.println(spec.getRecipe().getDisplayName());
    }

    @Test
    void isNotATestMethod() {
        rewriteRun(
            java(
                """
                package it.univaq.spencerlab.openrewrite.recipe;
                
                import org.junit.jupiter.api.Test;
                import org.moditect.jfrunit.JfrEventTest;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                @JfrEventTest
                public class AddAnnotationTest {
                    
                    public void testMethod() {
                    }
                }
                """));
    }

    @Test
    void annotationShouldBeAdded() {
        rewriteRun(
            java(
                """
                package it.univaq.spencerlab.openrewrite.recipe;
                
                import org.junit.jupiter.api.Test;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                public class AddAnnotationTest {
                    
                    @Test
                    public void testMethod() {
                        assertTrue(true);
                    }
                }
                """,
                """
                package it.univaq.spencerlab.openrewrite.recipe;
                
                import org.junit.jupiter.api.Test;
                import org.moditect.jfrunit.EnableEvent;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                public class AddAnnotationTest {
                    
                    @EnableEvent("jdk.G1*")
                    @EnableEvent("jdk.GC*")
                    @Test
                    public void testMethod() {
                        assertTrue(true);
                    }
                }
                """));
    }

}