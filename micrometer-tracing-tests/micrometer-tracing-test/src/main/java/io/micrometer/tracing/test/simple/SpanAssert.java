/*
 * Copyright 2021 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.tracing.test.simple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.util.StringUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;

/**
 * Assertion methods for {@code SimpleSpan}s.
 * <p>
 * To create a new instance of this class, invoke {@link SpanAssert#assertThat(FinishedSpan)}
 * or {@link SpanAssert#then(FinishedSpan)}.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class SpanAssert extends AbstractAssert<SpanAssert, FinishedSpan> {

    protected SpanAssert(FinishedSpan actual) {
        super(actual, SpanAssert.class);
    }

    /**
     * Creates the assert object for {@link FinishedSpan}.
     *
     * @param actual span to assert against
     * @return span assertions
     */
    public static SpanAssert assertThat(FinishedSpan actual) {
        return new SpanAssert(actual);
    }

    /**
     * Creates the assert object for {@link FinishedSpan}.
     *
     * @param actual span to assert against
     * @return span assertions
     */
    public static SpanAssert then(FinishedSpan actual) {
        return new SpanAssert(actual);
    }

    public SpanAssert hasNoTags() {
        isNotNull();
        Map<String, String> tags = this.actual.getTags();
        if (!tags.isEmpty()) {
            failWithMessage("Span should have no tags but has <%s>", tags);
        }
        return this;
    }

    public SpanAssert hasTagWithKey(String key) {
        isNotNull();
        if (!this.actual.getTags().containsKey(key)) {
            failWithMessage("Span should have a tag with key <%s> but it's not there. List of all keys <%s>", key, this.actual.getTags().keySet());
        }
        return this;
    }

    public SpanAssert hasTag(String key, String value) {
        isNotNull();
        hasTagWithKey(key);
        Map<String, String> tags = this.actual.getTags();
        String tagValue = tags.get(key);
        if (!tagValue.equals(value)) {
            failWithMessage("Span should have a tag with key <%s> and value <%s>. The key is correct but the value is <%s>", key, value, tagValue);
        }
        return this;
    }

    public SpanAssert doesNotHaveTagWithKey(String key) {
        isNotNull();
        if (this.actual.getTags().containsKey(key)) {
            failWithMessage("Span should not have a tag with key <%s>", key, this.actual.getTags().keySet());
        }
        return this;
    }

    public SpanAssert doesNotHaveTag(String key, String value) {
        isNotNull();
        doesNotHaveTagWithKey(key);
        Map<String, String> tags = this.actual.getTags();
        String tagValue = tags.get(key);
        if (tagValue.equals(value)) {
            failWithMessage("Span should not have a tag with key <%s> and value <%s>", key, value);
        }
        return this;
    }

    public SpanAssert isStarted() {
        isNotNull();
        if (this.actual.getStartTimestamp() == 0) {
            failWithMessage("Span should be started");
        }
        return this;
    }

    public SpanAssert isNotStarted() {
        isNotNull();
        if (this.actual.getStartTimestamp() != 0) {
            failWithMessage("Span should not be started");
        }
        return this;
    }

    public SpanAssert isEnded() {
        isNotNull();
        if (this.actual.getEndTimestamp() == 0) {
            failWithMessage("Span should be ended");
        }
        return this;
    }

    public SpanAssert isNotEnded() {
        isNotNull();
        if (this.actual.getEndTimestamp() != 0) {
            failWithMessage("Span should not be ended");
        }
        return this;
    }

    public SpanAssertReturningAssert assertThatThrowable() {
        return new SpanAssertReturningAssert(actual.getError(), this);
    }

    public SpanAssertReturningAssert thenThrowable() {
        return assertThatThrowable();
    }

    public SpanAssert hasRemoteServiceNameEqualTo(String remoteServiceName) {
        isNotNull();
        if (!remoteServiceName.equals(this.actual.getRemoteServiceName())) {
            failWithMessage("Span should have remote service name equal to <%s> but has <%s>", remoteServiceName, this.actual.getRemoteServiceName());
        }
        return this;
    }

    public SpanAssert doesNotHaveRemoteServiceNameEqualTo(String remoteServiceName) {
        isNotNull();
        if (remoteServiceName.equals(this.actual.getRemoteServiceName())) {
            failWithMessage("Span should not have remote service name equal to <%s>", remoteServiceName);
        }
        return this;
    }

    public SpanAssert hasKindEqualTo(Span.Kind kind) {
        isNotNull();
        if (!kind.equals(this.actual.getKind())) {
            failWithMessage("Span should have span kind equal to <%s> but has <%s>", kind, this.actual.getKind());
        }
        return this;
    }

    public SpanAssert doesNotHaveKindEqualTo(Span.Kind kind) {
        isNotNull();
        if (kind.equals(this.actual.getKind())) {
            failWithMessage("Span should not have span kind equal to <%s>", kind);
        }
        return this;
    }

    public SpanAssert hasEventWithNameEqualTo(String eventName) {
        isNotNull();
        List<String> eventNames = eventNames();
        if (!eventNames.contains(eventName)) {
            failWithMessage("Span should have an event with name <%s> but has <%s>", eventName, eventNames);
        }
        return this;
    }

    private List<String> eventNames() {
        return this.actual.getEvents().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public SpanAssert doesNotHaveEventWithNameEqualTo(String eventName) {
        isNotNull();
        List<String> eventNames = eventNames();
        if (eventNames.contains(eventName)) {
            failWithMessage("Span should not have an event with name <%s>", eventName);
        }
        return this;
    }

    public SpanAssert hasNameEqualTo(String spanName) {
        isNotNull();
        if (!this.actual.getName().equals(spanName)) {
            failWithMessage("Span should have a name <%s> but has <%s>", spanName, this.actual.getName());
        }
        return this;
    }

    public SpanAssert doesNotHaveNameEqualTo(String spanName) {
        isNotNull();
        if (!this.actual.getName().equals(spanName)) {
            failWithMessage("Span should not have a name <%s>", spanName, this.actual.getName());
        }
        return this;
    }

    public SpanAssert hasIpEqualTo(String ip) {
        isNotNull();
        if (!this.actual.getRemoteIp().equals(ip)) {
            failWithMessage("Span should have ip equal to <%s> but has <%s>", ip, this.actual.getRemoteIp());
        }
        return this;
    }

    public SpanAssert doesNotHaveIpEqualTo(String ip) {
        isNotNull();
        if (this.actual.getRemoteIp().equals(ip)) {
            failWithMessage("Span should not have ip equal to <%s>", ip, this.actual.getRemoteIp());
        }
        return this;
    }

    public SpanAssert hasIpThatIsNotBlank() {
        isNotNull();
        if (StringUtils.isBlank(this.actual.getRemoteIp())) {
            failWithMessage("Span should have ip that is not blank");
        }
        return this;
    }

    public SpanAssert hasIpThatIsBlank() {
        isNotNull();
        if (StringUtils.isNotBlank(this.actual.getRemoteIp())) {
            failWithMessage("Span should have ip that is blank");
        }
        return this;
    }

    public SpanAssert hasPortEqualTo(int port) {
        isNotNull();
        if (this.actual.getRemotePort() != port) {
            failWithMessage("Span should have port equal to <%s> but has <%s>", port, this.actual.getRemotePort());
        }
        return this;
    }

    public SpanAssert doesNotHavePortEqualTo(int port) {
        isNotNull();
        if (this.actual.getRemotePort() == port) {
            failWithMessage("Span should not have port equal to <%s>", port, this.actual.getRemotePort());
        }
        return this;
    }

    public SpanAssert hasPortThatIsNotSet() {
        isNotNull();
        if (this.actual.getRemotePort() != 0) {
            failWithMessage("Span should have port that is not set but was set to <%s>", this.actual.getRemotePort());
        }
        return this;
    }

    public SpanAssert hasPortThatIsSet() {
        isNotNull();
        if (this.actual.getRemotePort() == 0) {
            failWithMessage("Span should have port that is set but wasn't");
        }
        return this;
    }

    public static class SpanAssertReturningAssert extends AbstractThrowableAssert<SpanAssertReturningAssert, Throwable> {

        private final SpanAssert spanAssert;

        public SpanAssertReturningAssert(Throwable throwable, SpanAssert spanAssert) {
            super(throwable, SpanAssertReturningAssert.class);
            this.spanAssert = spanAssert;
        }

        public SpanAssert backToSpan() {
            return this.spanAssert;
        }
    }
}
