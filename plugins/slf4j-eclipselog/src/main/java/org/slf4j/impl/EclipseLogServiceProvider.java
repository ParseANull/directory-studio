/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.slf4j.impl;


import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;


// ── CLASS: EclipseLogServiceProvider — The Rebel Comm Officer Registering R2-D2 ─
// When the Rebel Alliance brings a new comm droid (SLF4J 2.x) online, it needs
// to know three things: who gives out loggers (the factory), who manages log
// markers (the marker factory), and who manages Mapped Diagnostic Context (the
// MDC adapter).  The service provider is the registration card that tells the
// SLF4J 2.x bootstrap which implementations to use for this runtime.
// EclipseLogServiceProvider is that registration card: an SLF4JServiceProvider
// implementation that wires up EclipseLogLoggerFactory (factory),
// BasicMarkerFactory (markers), and BasicMDCAdapter (MDC) during initialize().
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link SLF4JServiceProvider} that routes all SLF4J 2.x logging calls to
 * the Eclipse platform log via {@link EclipseLogLogger}.
 * Registered in {@code META-INF/services/org.slf4j.spi.SLF4JServiceProvider}.
 * On {@link #initialize}, we create an {@link EclipseLogLoggerFactory},
 * a {@link BasicMarkerFactory}, and a {@link BasicMDCAdapter}.
 * Think of this as the Rebel comm officer registering R2-D2's distress-beacon
 * module with the Alliance communications network so the SLF4J bootstrap knows
 * to use it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EclipseLogServiceProvider implements SLF4JServiceProvider
{
    /** The SLF4J API version this provider is built against. */
    public static final String REQUESTED_API_VERSION = "2.0.99";

    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;


    // ── Return the Eclipse Log Logger Factory ─────────────────────────────────
    // SLF4J calls this after initialize() to get the factory it should use for
    // all LoggerFactory.getLogger() calls.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EclipseLogLoggerFactory} that hands out
     * {@link EclipseLogLogger} instances.
     *
     * @return  the logger factory.
     */
    @Override
    public ILoggerFactory getLoggerFactory()
    {
        return loggerFactory;
    }


    // ── Return the Marker Factory ─────────────────────────────────────────────
    // We use the basic no-op marker factory since Eclipse log has no marker
    // support.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BasicMarkerFactory} (no-op marker support).
     *
     * @return  the marker factory.
     */
    @Override
    public IMarkerFactory getMarkerFactory()
    {
        return markerFactory;
    }


    // ── Return the MDC Adapter ────────────────────────────────────────────────
    // We use the basic in-memory MDC adapter since we don't propagate MDC to
    // the Eclipse log.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BasicMDCAdapter} (simple thread-local MDC storage).
     *
     * @return  the MDC adapter.
     */
    @Override
    public MDCAdapter getMDCAdapter()
    {
        return mdcAdapter;
    }


    // ── Announce Which SLF4J API Version We Support ───────────────────────────
    // SLF4J uses this to check compatibility between the API and the provider.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SLF4J API version this provider was compiled against.
     *
     * @return  {@value #REQUESTED_API_VERSION}.
     */
    @Override
    public String getRequestedApiVersion()
    {
        return REQUESTED_API_VERSION;
    }


    // ── Register R2-D2 with the Alliance Comms Network ───────────────────────
    // Called once by the SLF4J bootstrap at startup.  We create all three
    // components here so they're ready before any logging call comes in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the provider — creates the logger factory, marker factory,
     * and MDC adapter.  Called once by the SLF4J 2.x bootstrap before any
     * logging calls are made.
     */
    @Override
    public void initialize()
    {
        loggerFactory = new EclipseLogLoggerFactory();
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new BasicMDCAdapter();
    }
}
