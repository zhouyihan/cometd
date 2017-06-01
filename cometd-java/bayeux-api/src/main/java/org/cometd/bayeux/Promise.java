/*
 * Copyright (c) 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cometd.bayeux;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Promise<C>
{
    /**
     * <p>Shared instance whose methods are implemented empty,</p>
     * <p>use {@link #noop()} to ease type inference.</p>
     */
    static final Promise<?> NOOP = new Promise<Object>() {
        @Override
        public void succeed(Object result) {
        }

        @Override
        public void fail(Throwable failure) {
        }
    };

    /**
     * <p>Callback to invoke when the operation succeeds.</p>
     *
     * @param result the result
     * @see #fail(Throwable)
     */
    default void succeed(C result)
    {
    }

    /**
     * <p>Callback to invoke when the operation fails.</p>
     *
     * @param failure the operation failure
     */
    default void fail(Throwable failure)
    {
    }

    /**
     * @return a Promise whose methods are implemented empty.
     */
    static <T> Promise<T> noop() {
        return (Promise<T>)NOOP;
    }

    /**
     * @param succeed the Consumer to call in case of successful completion
     * @param fail the Consumer to call in case of failed completion
     * @return a Promise from the given consumers
     */
    static <T> Promise<T> from(Consumer<T> succeed, Consumer<Throwable> fail) {
        return new Promise<T>() {
            @Override
            public void succeed(T result) {
                succeed.accept(result);
            }

            @Override
            public void fail(Throwable failure) {
                fail.accept(failure);
            }
        };
    }

    /**
     * <p>A CompletableFuture that is also a Promise.</p>
     */
    class Completable<S> extends CompletableFuture<S> implements Promise<S>
    {
        @Override
        public void succeed(S result)
        {
            complete(result);
        }

        @Override
        public void fail(Throwable failure)
        {
            completeExceptionally(failure);
        }
    }
}