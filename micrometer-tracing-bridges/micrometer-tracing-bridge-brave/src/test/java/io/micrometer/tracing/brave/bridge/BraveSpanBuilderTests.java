/**
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.tracing.brave.bridge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.test.TestSpanHandler;
import io.micrometer.tracing.Link;
import io.micrometer.tracing.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class BraveSpanBuilderTests {

    TestSpanHandler handler = new TestSpanHandler();

    Tracing tracing = Tracing.newBuilder().addSpanHandler(handler).build();

    @AfterEach
    void cleanup() {
        tracing.close();
    }

    @Test
    void should_set_child_span_when_using_builders() {
        Tracer tracer = tracing.tracer();
        Span.Builder builder = new BraveSpanBuilder(tracer);
        Span parentSpan = BraveSpan.fromBrave(tracer.nextSpan());

        Span child = builder.setParent(parentSpan.context()).start();

        then(child.context().traceId()).isEqualTo(parentSpan.context().traceId());
        then(child.context().parentId()).isEqualTo(parentSpan.context().spanId());
    }

    @Test
    void should_set_links() {
        Tracer tracer = tracing.tracer();
        Span.Builder builder = new BraveSpanBuilder(tracer);
        Span span1 = BraveSpan.fromBrave(tracer.nextSpan());
        Span span2 = BraveSpan.fromBrave(tracer.nextSpan());

        builder.addLink(new Link(span2.context(), tags())).addLink(new Link(span1)).start().end();

        MutableSpan finishedSpan = handler.get(0);
        then(finishedSpan.tags()).containsEntry("links[0].traceId", span2.context().traceId())
            .containsEntry("links[0].spanId", span2.context().spanId())
            .containsEntry("links[0].tags[tag1]", "value1")
            .containsEntry("links[0].tags[tag2]", "value2")
            .containsEntry("links[1].traceId", span1.context().traceId())
            .containsEntry("links[1].spanId", span1.context().spanId());
    }

    @Test
    void should_set_non_string_tags() {
        new BraveSpanBuilder(tracing.tracer()).tag("string", "string")
            .tag("double", 2.5)
            .tag("long", 2)
            .tag("boolean", true)
            .start()
            .end();

        then(handler.get(0).tags()).containsEntry("string", "string")
            .containsEntry("double", "2.5")
            .containsEntry("long", "2")
            .containsEntry("boolean", "true");
    }

    @Test
    void should_set_multi_value_tags() {
        new BraveSpanBuilder(tracing.tracer()).tagOfStrings("strings", Arrays.asList("s1", "s2", "s3"))
            .tagOfDoubles("doubles", Arrays.asList(1.0, 2.5, 3.7))
            .tagOfLongs("longs", Arrays.asList(2L, 3L, 4L))
            .tagOfBooleans("booleans", Arrays.asList(true, false, false))
            .start()
            .end();

        then(handler.get(0).tags()).containsEntry("strings", "s1,s2,s3")
            .containsEntry("doubles", "1.0,2.5,3.7")
            .containsEntry("longs", "2,3,4")
            .containsEntry("booleans", "true,false,false");
    }

    private Map<String, Object> tags() {
        Map<String, Object> map = new HashMap<>();
        map.put("tag1", "value1");
        map.put("tag2", "value2");
        return map;
    }

}
