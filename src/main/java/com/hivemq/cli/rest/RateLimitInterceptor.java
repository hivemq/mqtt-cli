/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
package com.hivemq.cli.rest;

import com.google.common.util.concurrent.RateLimiter;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
public class RateLimitInterceptor implements Interceptor  {

    private final @NotNull RateLimiter rateLimiter;

    public RateLimitInterceptor(final double requestsPerSecond) {
        this.rateLimiter = RateLimiter.create(requestsPerSecond);
    }

    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        rateLimiter.acquire(1);
        return chain.proceed(chain.request());
    }
}
