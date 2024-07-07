package org.andy;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.stream.Stream;

@ApplicationScoped
public class MyMessagingApplicationsService {

    @Inject
    Tracer otel;

    public void outro(Span parent) {
        Span span = otel.spanBuilder("My custom span- 2")
                .setAttribute("attr", "attr.value")
                .setParent(Context.current().with(parent))
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try {
            throw new RuntimeException("ocorreu um erro");
        } finally {
            span.end();
        }

    }
}
