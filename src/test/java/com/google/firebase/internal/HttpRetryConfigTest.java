/*
 * Copyright 2019 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.Test;

public class HttpRetryConfigTest {

  @Test
  public void testEmptyBuilder() {
    HttpRetryConfig config = HttpRetryConfig.builder().build();

    assertTrue(config.getRetryStatusCodes().isEmpty());
    assertEquals(0, config.getMaxRetries());
    assertEquals(2 * 60 * 1000, config.getMaxIntervalMillis());
    assertEquals(2.0, config.getBackOffMultiplier(), 0.01);

    ExponentialBackOff backOff = (ExponentialBackOff) config.newBackOff();
    assertEquals(2 * 60 * 1000, backOff.getMaxIntervalMillis());
    assertEquals(2.0, backOff.getMultiplier(), 0.01);
    assertEquals(500, backOff.getInitialIntervalMillis());
    assertEquals(0.0, backOff.getRandomizationFactor(), 0.01);
    assertNotSame(backOff, config.newBackOff());
  }

  @Test
  public void testBuilder() {
    ImmutableList<Integer> statusCodes = ImmutableList.of(500, 503);
    HttpRetryConfig config = HttpRetryConfig.builder()
        .setMaxRetries(4)
        .setRetryStatusCodes(statusCodes)
        .setMaxIntervalMillis(5 * 60 * 1000)
        .setBackOffMultiplier(1.5)
        .build();

    assertEquals(2, config.getRetryStatusCodes().size());
    assertEquals(statusCodes.get(0), config.getRetryStatusCodes().get(0));
    assertEquals(statusCodes.get(1), config.getRetryStatusCodes().get(1));
    assertEquals(4, config.getMaxRetries());
    assertEquals(5 * 60 * 1000, config.getMaxIntervalMillis());
    assertEquals(1.5, config.getBackOffMultiplier(), 0.01);

    ExponentialBackOff backOff = (ExponentialBackOff) config.newBackOff();
    assertEquals(500, backOff.getInitialIntervalMillis());
    assertEquals(5 * 60 * 1000, backOff.getMaxIntervalMillis());
    assertEquals(1.5, backOff.getMultiplier(), 0.01);
    assertEquals(0.0, backOff.getRandomizationFactor(), 0.01);
    assertNotSame(backOff, config.newBackOff());
  }

  @Test
  public void testExponentialBackOff() throws IOException {
    HttpRetryConfig config = HttpRetryConfig.builder()
        .setMaxIntervalMillis(12000)
        .build();

    BackOff backOff = config.newBackOff();

    assertEquals(500, backOff.nextBackOffMillis());
    assertEquals(1000, backOff.nextBackOffMillis());
    assertEquals(2000, backOff.nextBackOffMillis());
    assertEquals(4000, backOff.nextBackOffMillis());
    assertEquals(8000, backOff.nextBackOffMillis());
    assertEquals(12000, backOff.nextBackOffMillis());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaxRetriesNotAllowed() {
    HttpRetryConfig.builder()
        .setMaxRetries(-1)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMaxIntervalMillisTooSmall() {
    HttpRetryConfig.builder()
        .setMaxIntervalMillis(499)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBackOffMultiplierTooSmall() {
    HttpRetryConfig.builder()
        .setBackOffMultiplier(0.99)
        .build();
  }
}
