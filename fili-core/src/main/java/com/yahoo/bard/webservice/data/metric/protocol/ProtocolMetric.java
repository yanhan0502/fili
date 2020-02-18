// Copyright 2020 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.data.metric.protocol;

import com.yahoo.bard.webservice.data.metric.LogicalMetric;

import java.util.Map;

/**
 * Protocol metrics are capable of handling transforming signals from the client.
 * This is a key feature in reducing the number of persisted variations on base metrics.  A signal could indicate that
 * a metric should be directly transformed, should be composed into a new metric, or have it's entire expression tree
 * rebuilt after modifying some root dependency.
 */
public interface ProtocolMetric extends LogicalMetric {

    /**
     * Test whether this Metric (or potentially a dependency of it) accepts this kind of signal.
     *
     * @param signalName  The name of the signal.
     *
     * @return true if this metric knows how to handle this signal type.
     */
    boolean accepts(String signalName);

    /**
     * Apply this protocol and parameters to this metric and return a (potentially different) metric.
     *
     * @param protocolName  The name of the protocol to apply
     * @param parameters  A map of keys and values representing the transformation.
     *
     * @return A metric that has accepted this protocol transformation.
     * @throws UnknownProtocolValueException if the protocol cannot be processed correctly
     */
    LogicalMetric accept(String protocolName, Map<String, String> parameters) throws UnknownProtocolValueException;

    /**
     * Get the underlying signal handler for this metric.
     *
     * @return The signal handler for this metric.
     */
    ProtocolSupport getProtocolSupport();
}