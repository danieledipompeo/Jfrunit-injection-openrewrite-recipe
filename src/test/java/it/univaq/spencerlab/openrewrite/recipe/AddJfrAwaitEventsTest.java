package it.univaq.spencerlab.openrewrite.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddJfrAwaitEventsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddJfrAwaitEvents())
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "jfrunit-core-1.0.0.Alpha2","junit-jupiter-api-5.10.2",""));
    }

    @Test
    void shouldAddJfrEvents_AwaitEvents() {
        rewriteRun(
            java(
                """
                package it.univaq.spencerlab.openrewrite.recipe;
                
                import org.junit.jupiter.api.Test;
                import org.moditect.jfrunit.EnableEvent;
                import org.moditect.jfrunit.JfrEventTest;
                import org.moditect.jfrunit.JfrEvents;
                import static org.moditect.jfrunit.ExpectedEvent.*;
                import org.moditect.jfrunit.events.JfrEventTypes;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                @JfrEventTest
                public class AddJfrEventsAwaitTest {
                
                        JfrEvents jfrEvents = new JfrEvents();
                        
                        @EnableEvent("jdk.G1*")
                        @EnableEvent("jdk.GC*")
                        @Test
                        void testToCSV() {
                            assertTrue(true);
                        }
                }
                """,
                """
                package it.univaq.spencerlab.openrewrite.recipe;
                
                import org.junit.jupiter.api.Test;
                
                import org.moditect.jfrunit.EnableEvent;
                import org.moditect.jfrunit.JfrEventTest;
                import org.moditect.jfrunit.JfrEvents;
                import static org.moditect.jfrunit.ExpectedEvent.*;
                import org.moditect.jfrunit.events.JfrEventTypes;
                
                import static org.junit.jupiter.api.Assertions.*;
                
                @JfrEventTest
                public class AddJfrEventsAwaitTest {
                
                        JfrEvents jfrEvents = new JfrEvents();
                    
                        @EnableEvent("jdk.G1*")
                        @EnableEvent("jdk.GC*")
                        @Test
                        void testToCSV() {
                            assertTrue(true);
                            jfrEvents.awaitEvents();
                        }
                }
                """));
    }
}