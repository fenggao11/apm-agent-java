/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2020 Elastic and contributors
 * %%
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */



package co.elastic.apm.agent.socket;

import co.elastic.apm.agent.impl.ElasticApmTracer;
import co.elastic.apm.agent.impl.transaction.Span;
import co.elastic.apm.agent.impl.transaction.TraceContextHolder;
import co.elastic.apm.agent.impl.transaction.Transaction;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.annotation.Nullable;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SocketInstrumentation extends BaseSocketInstrumentation {

//    @VisibleForAdvice
//    public static final Logger logger = LoggerFactory.getLogger(SocketInstrumentation.class);

    public SocketInstrumentation(ElasticApmTracer tracer) {
        super(tracer);
    }


    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("com.travelsky.socket.SocketClient")
            ;
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {

        return any();
    }

    @Advice.OnMethodEnter(suppress = Throwable.class)
    private static void setTransactionName(@Advice.Local("span") @Nullable Span span) {
        if (tracer == null || tracer.getActive() == null) {
            return;
        }
        final TraceContextHolder<?> active = tracer.getActive();
        span = active.createExitSpan();
        span.withType("socket")
            .withAction("execute")
            .withName("socket-client-span");
        span.activate();
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.Local("transaction") @Nullable Transaction transaction,
                                    @Advice.Local("span") @Nullable Span span, @Advice.Thrown Throwable t) {

        if (span != null) {
            span.captureException(t);
            span.deactivate()
                .end();
        }
        if (transaction != null) {
            transaction.deactivate()
                .end();
        }
    }
}




//    @Override
//    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
//
//        return named("Socket").or(named("close"));
//        return named("Socket");
//
//                .and(takesArguments(2));
//                    .and(takesArgument(0, named("java.lang.String")))
//                    .and(takesArgument(1, named("java.lang.Integer")));
//    }

/*    public static class Socket extends SocketInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            // will match both variants : with and without timeout
            return named("main");
//                .and(takesArguments(2));
//                    .and(takesArgument(0, named("java.lang.String")))
//                    .and(takesArgument(1, named("java.lang.Integer"))));
//             .and(takesArguments(2)
//                .and(takesArgument(0, named("java.lang.String")))
//                .and(takesArgument(1, named("java.lang.Integer"))));
        }

        @Override
        public Class<?> getAdviceClass() {
            return SocketAdvice.class;
        }

        public static class SocketAdvice {

            @Advice.OnMethodEnter(suppress = Throwable.class)
            private static void setTransactionName(
//                @Advice.Argument(value = 0) @Nullable String ip, @Advice.Argument(value = 1) @Nullable Integer port, @SimpleMethodSignatureOffsetMappingFactory.SimpleMethodSignature String signature,
//                @Advice.Origin Class<?> clazz
//                , @Advice.Local("transaction") Transaction transaction
            ) {
//                if (ElasticApmInstrumentation.tracer != null) {
                    TraceContextHolder<?> active = ElasticApmInstrumentation.tracer.getActive();
                    Span span = active.createSpan();
                    span.withType("socket")
                        .withAction("execute")
                        .withName("socket-client-span");
                    span.activate();
//                    if (ip == null) {
//                        logger.warn("Cannot correctly name transaction for method {} because ip is null", signature);
//                    Transaction transaction = ElasticApmInstrumentation.tracer.startRootTransaction(clazz.getClassLoader());
//                        if (transaction != null) {
//                            transaction.withName("Transaction#test")
//                                .withType("TCP")
//                                .activate();
//                                .addLabel(ip,port);

//                        }

               *//* else if (active == null) {
                        transaction = ElasticApmInstrumentation.tracer.startRootTransaction(clazz.getClassLoader());
                        if (transaction != null) {
                            transaction.withName("IBEClient#query")
                                .withType("TCP")
                                .activate();
                        }
                    }*//*
              *//*  else {
                        logger.debug("Not creating transaction for method {} because there is already a transaction running ({})", signature, active);
                    }*//*
//                    Span span = ((TraceContextHolder) active).createSpan()
//                Span span = transaction.createSpan()
//                        .withType("socket")
//                        .withAction("execute")
//                        .withName("socket-client-span");
//                    span.addLabel(ip,port);
                }
//            }

            @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
            public static void onMethodExitException(@Advice.Local("span") @Nullable Span span,
                                                     @Advice.Local("transaction") @Nullable Transaction transaction,
                                                     @Advice.Thrown Throwable t) {
                if (span != null) {
                    span.captureException(t)
                        .deactivate()
                        .end();
                }
                if (transaction != null ) {
                    transaction.deactivate()
                        .end();
                }
            }
        }
    }

//    public static class Connect extends SocketInstrumentation {
//
//        @Override
//        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
//            // will match both variants : with and without timeout
//            return named("connect")
//                    .and(takesArgument(0, named("java.net.SocketAddress")))
//                    .and(takesArgument(1, named("java.lang.Integer")));
//        }
//
//        @Override
//        public Class<?> getAdviceClass() {
//            return ConnectAdvice.class;
//        }
//
//        public static class ConnectAdvice {
//
//            @Advice.OnMethodEnter(suppress = Throwable.class)
//            private static void setTransactionName(@Advice.Argument(value = 0) @Nullable SocketAddress address, @Advice.Argument(value = 1) @Nullable Integer timeout,
//                                                   @SimpleMethodSignatureOffsetMappingFactory.SimpleMethodSignature String signature, @Advice.Origin Class<?> clazz, @Advice.Local("transaction") Transaction transaction) {
//                if (ElasticApmInstrumentation.tracer != null) {
//                    TraceContextHolder<?> active = ElasticApmInstrumentation.tracer.getActive();
//                    if (address == null) {
//                        logger.warn("Cannot correctly name transaction for method {} because ip is null", signature);
//                        transaction = ElasticApmInstrumentation.tracer.startRootTransaction(clazz.getClassLoader());
//                        if (transaction != null) {
//                            transaction.withName(signature)
//                                .withType("TCP")
//                                .activate();
//                        }
//                    } else if (active == null) {
//                        transaction = ElasticApmInstrumentation.tracer.startRootTtimeoutransaction(clazz.getClassLoader());
//                        if (transaction != null) {
//                            transaction.withName("IBEClient#query")
//                                .withType("TCP")
//                                .activate();
//                        }
//                    } else {
//                        logger.debug("Not creating transaction for method {} because there is already a transaction running ({})", signature, active);
//                    }
//                    Span span = active.createSpan()
//                        .withType("socket")
//                        .withAction("execute")
//                        .withName("socket-client-span");
//                    span.addLabel(String.valueOf(address),timeout);
//                }
//            }
//
//            @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
//            public static void onMethodExitException(@Advice.Local("span") @Nullable Span span,
//                                                     @Advice.Local("transaction") @Nullable Transaction transaction, @Advice.Thrown Throwable t) {
//                if (span != null) {
//                    span.captureException(t)
//                        .deactivate()
//                        .end();
//                }
//                if (transaction != null ) {
//                    transaction.deactivate()
//                        .end();
//                }
//            }
//        }
//    }

    public static class Close extends SocketInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            // will match both variants : with and without timeout
            return named("close");
//                .and(takesArguments(0));
        }

        @Override
        public Class<?> getAdviceClass() {
            return CloseAdvice.class;
        }

        public static class CloseAdvice {

            @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
            public static void onMethodExit(@Advice.Local("transaction") @Nullable Transaction transaction,
                                                     @Advice.Local("span") @Nullable Span span, @Advice.Thrown Throwable t) {

                if (span != null ) {
                    span.deactivate()
                        .end();
                }
                if (transaction != null ) {
                    transaction.deactivate()
                        .end();
                }
            }
        }
    }

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return Arrays.asList("socket");
    }
}*/

//    @Override
//    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
//        return named("Socket")
//            .and(takesArguments(2)
//                .and(takesArgument(0, named("java.lang.String"))))
//            .or(named("connect")
//                .and(takesArgument(0, named(" java.net.SocketAddress"))))
//            ;
//        return named("Socket")
//            .and(takesArguments(2))
//            .and(takesArgument(0, named("java.lang.String"))
//                .and(takesArgument(1)))
//            .or(named("connect")
//                .and(takesArgument(0, named(" java.net.SocketAddress")))
//            );
//    }
