package org.andy;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.stream.Stream;

@ApplicationScoped
public class MyMessagingApplication {

    private static final Logger logger = Logger.getLogger(MyMessagingApplication.class.getName());

    @Inject
    Tracer otel;

    @Inject
    MyMessagingApplicationsService service;

    @Inject
    @Channel("words-out")
    Emitter<String> emitter;

    /**
     * Sends message to the "words-out" channel, can be used from a JAX-RS resource or any bean of your application.
     * Messages are sent to the broker.
     **/
    void onStart(@Observes StartupEvent ev) {
        outroMetodo();
        Stream.of("Hello", "with", "Quarkus", "Messaging", "message").forEach(string -> emitter.send(string));
    }

    private void outroMetodo() {
        Span span = otel.spanBuilder("My custom span")
                .setAttribute("attr", "attr.value")
                .setParent(Context.current().with(Span.current()))
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try {
            service.outro(span);
        } catch (Throwable t){
            logger.log(Level.ERROR, "error", t);
            span.setAttribute("trace log", t.getMessage());
            span.setStatus(StatusCode.ERROR);
        } finally {
            span.end();
        }

    }

    /**
     * Consume the message from the "words-in" channel, uppercase it and send it to the uppercase channel.
     * Messages come from the broker.
     **/
    @Incoming("words-in")
    @Outgoing("uppercase")
    public Message<String> toUpperCase(Message<String> message) {
        return message.withPayload(message.getPayload().toUpperCase());
    }

    /**
     * Consume the uppercase channel (in-memory) and print the messages.
     **/
    @Incoming("uppercase")
    public void sink(String word) {
        System.out.println(">> " + word);
    }
}
