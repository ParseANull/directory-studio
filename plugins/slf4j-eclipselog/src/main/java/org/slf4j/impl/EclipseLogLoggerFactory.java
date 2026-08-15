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


// ── CLASS: EclipseLogLoggerFactory — R2-D2 Handing Out Distress Beacons ──────
// R2-D2 has only one distress-signal module; no matter which system in the ship
// asks for a beacon (any logger name), R2 hands them the same shared module.
// EclipseLogLoggerFactory is that droid: it holds a single shared EclipseLogLogger
// instance and returns it to every caller regardless of the requested name.
// This keeps things simple — all SLF4J log calls from anywhere in the plugin
// converge on one logger which routes to the Eclipse log.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link ILoggerFactory} that returns the same singleton {@link EclipseLogLogger}
 * for every logger name.
 * Since {@link EclipseLogLogger} routes all calls through the Eclipse platform
 * log (which has no concept of named loggers), there is no benefit to creating
 * separate instances per name.
 * Think of this as R2-D2 handing the same distress beacon to every crew member
 * who asks — the beacon always routes to the same receiver.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EclipseLogLoggerFactory implements ILoggerFactory
{
    private final EclipseLogLogger logger = new EclipseLogLogger();


    // ── Hand the Shared Distress Beacon to the Caller ─────────────────────────
    // No matter what name is requested, we return the one shared logger instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link EclipseLogLogger} singleton regardless of the
     * requested logger name.
     *
     * @param name  the requested logger name (ignored).
     * @return      the singleton {@link EclipseLogLogger}.
     */
    @Override
    public EclipseLogLogger getLogger( String name )
    {
        return logger;
    }

}
