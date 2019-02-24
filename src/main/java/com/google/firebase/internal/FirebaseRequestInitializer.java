/*
 * Copyright 2018 Google Inc.
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

package com.google.firebase.internal;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.common.collect.ImmutableList;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.ImplFirebaseTrampolines;
import java.io.IOException;
import java.util.List;

/**
 * {@code HttpRequestInitializer} for configuring outgoing REST calls. Initializes requests with
 * OAuth2 credentials, timeout and retry settings.
 */
public final class FirebaseRequestInitializer implements HttpRequestInitializer {

  private final List<HttpRequestInitializer> initializers;

  public FirebaseRequestInitializer(FirebaseApp app) {
    this(app, null);
  }

  public FirebaseRequestInitializer(FirebaseApp app, @Nullable RetryConfig retryConfig) {
    HttpCredentialsAdapter credentials = new HttpCredentialsAdapter(
        ImplFirebaseTrampolines.getCredentials(app));
    this.initializers = ImmutableList.of(
        credentials,
        new TimeoutInitializer(app.getOptions()),
        new RetryInitializer(credentials, retryConfig));
  }

  @Override
  public void initialize(HttpRequest request) throws IOException {
    for (HttpRequestInitializer initializer : initializers) {
      initializer.initialize(request);
    }
  }

  private static class TimeoutInitializer implements HttpRequestInitializer {

    private final int connectTimeout;
    private final int readTimeout;

    TimeoutInitializer(FirebaseOptions options) {
      this.connectTimeout = options.getConnectTimeout();
      this.readTimeout = options.getReadTimeout();
    }

    @Override
    public void initialize(HttpRequest request) {
      request.setConnectTimeout(connectTimeout);
      request.setReadTimeout(readTimeout);
    }
  }
}
