package it.univaq.spencerlab.openrewrite.recipe;

import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEvents;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.openrewrite.java.Assertions.java;

class AddJfrEventsVariableTest implements RewriteTest {

    public JfrEvents jfrEvents = new JfrEvents();

    @Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new AddJfrEventsVariable());
	}

    @Test
    void variableIsAlready() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2")),
            java(
                """
                package it.univaq.spencerlab.openrewrite.recipe;

                import org.moditect.jfrunit.JfrEventTest;
                import org.moditect.jfrunit.JfrEvents;

                import static org.junit.jupiter.api.Assertions.*;

                @JfrEventTest
                public class AddVariableTest {
                
                    public JfrEvents jfrEvents = new JfrEvents();

                } 
                """));
    }


    @Test
    void variableShouldBeAdded() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2")),
                java(
                      """
                        package it.univaq.spencerlab.openrewrite.recipe;
        
                        import org.moditect.jfrunit.JfrEventTest;
        
                        import static org.junit.jupiter.api.Assertions.*;
        
                        @JfrEventTest
                        public class AddVariableTest {
                            int a = 0;
                            String str = "Hello";
                        } 
                        """,
                        """
                        package it.univaq.spencerlab.openrewrite.recipe;
        
                        import org.moditect.jfrunit.JfrEventTest;
                        import org.moditect.jfrunit.JfrEvents;
        
                        import static org.junit.jupiter.api.Assertions.*;
        
                        @JfrEventTest
                        public class AddVariableTest {
                            public JfrEvents jfrEvents = new JfrEvents();
                            int a = 0;
                            String str = "Hello";
                        } 
                        """));
    }

}