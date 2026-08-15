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


import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;


// ── CLASS: EclipseLogLogger — R2-D2 Routing Distress Signals to the Right Screen ─
// R2-D2 receives a constant stream of ship status messages.  He knows that
// not everything needs to go to Princess Leia's main display: only critical
// alerts (ERROR) and concerning anomalies (WARN) are worth surfacing in the
// Eclipse Error Log view.  Routine chatter (INFO), maintenance logs (DEBUG),
// and telemetry (TRACE) would flood the screen and hide the real problems, so
// R2 silently discards them.
// EclipseLogLogger is R2-D2 for the SLF4J logging bridge: it implements the
// SLF4J Logger interface and routes only ERROR and WARN to
// Platform.getLog().log() (which writes to .metadata/.log and the Error Log
// view).  All other levels are no-ops.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Adapts the {@link org.slf4j.Logger} interface to the Eclipse platform log
 * ({@code .metadata/.log} and the Error Log view).
 * <p>Only {@code error} and {@code warn} are forwarded; {@code info},
 * {@code debug}, and {@code trace} are silently discarded because they
 * would write too much to the Eclipse log and there is no equivalent
 * granularity in the Eclipse log severity model.</p>
 * Think of this as R2-D2 filtering ship status messages: only genuine alerts
 * and warnings reach the main display; routine chatter is dropped.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EclipseLogLogger extends MarkerIgnoringBase
{
    private static final long serialVersionUID = 1L;

    private static final String SYMBOLIC_NAME = "org.apache.directory.studio.slf4j-eclipselog";


    // ── Route the Alert to Eclipse's Error Log ────────────────────────────────
    // We look up our own bundle by symbolic name (the bundle must be active for
    // Platform.getLog() to work) and write a Status entry with the given
    // severity, message, and optional throwable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes a log entry to the Eclipse platform log at the given severity.
     * If our bundle is not active, the call is silently ignored (no NPE).
     *
     * @param severity  one of {@link Status#ERROR} or {@link Status#WARNING}.
     * @param message   the log message.
     * @param t         an optional throwable; may be {@code null}.
     */
    private void internalLog( int severity, String message, Throwable t )
    {
        Bundle bundle = Platform.getBundle( SYMBOLIC_NAME );
        if ( bundle != null )
        {
            Status status = new Status( severity, SYMBOLIC_NAME, message, t );
            Platform.getLog( bundle ).log( status );
        }
    }


    // ERROR

    // ── Is ERROR Enabled? — Always Yes ────────────────────────────────────────
    // ERROR is the one level we always forward; it maps to Status.ERROR.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — ERROR level is always enabled.
     *
     * @return {@code true}.
     */
    public boolean isErrorEnabled()
    {
        return true;
    }


    // ── Forward a Plain Error Message ─────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error message with no arguments.
     *
     * @param msg  the error message.
     */
    public void error( String msg )
    {
        if ( isErrorEnabled() )
        {
            internalLog( Status.ERROR, msg, null );
        }
    }


    // ── Forward a One-Arg Error Message ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error message with one format argument.
     *
     * @param format  the SLF4J format string (using {@code {}}).
     * @param arg     the single argument.
     */
    public void error( String format, Object arg )
    {
        if ( isErrorEnabled() )
        {
            String msgStr = MessageFormatter.format( format, arg ).getMessage();
            internalLog( Status.ERROR, msgStr, null );
        }
    }


    // ── Forward a Two-Arg Error Message ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error message with two format arguments.
     *
     * @param format  the SLF4J format string.
     * @param arg1    the first argument.
     * @param arg2    the second argument.
     */
    public void error( String format, Object arg1, Object arg2 )
    {
        if ( isErrorEnabled() )
        {
            String msgStr = MessageFormatter.format( format, arg1, arg2 ).getMessage();
            internalLog( Status.ERROR, msgStr, null );
        }
    }


    // ── Forward a Varargs Error Message ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error message with a variable number of format arguments.
     *
     * @param format    the SLF4J format string.
     * @param argArray  the arguments.
     */
    public void error( String format, Object... argArray )
    {
        if ( isErrorEnabled() )
        {
            String msgStr = MessageFormatter.arrayFormat( format, argArray ).getMessage();
            internalLog( Status.ERROR, msgStr, null );
        }
    }


    // ── Forward an Error Message with an Exception ────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs an error message with an accompanying throwable.
     *
     * @param msg  the error message.
     * @param t    the throwable.
     */
    public void error( String msg, Throwable t )
    {
        if ( isErrorEnabled() )
        {
            internalLog( Status.ERROR, msg, t );
        }
    }


    // WARN

    // ── Is WARN Enabled? — Always Yes ────────────────────────────────────────
    // WARN maps to Status.WARNING and is always forwarded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — WARN level is always enabled.
     *
     * @return {@code true}.
     */
    public boolean isWarnEnabled()
    {
        return true;
    }


    // ── Forward a Plain Warning ───────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a warning message with no arguments.
     *
     * @param msg  the warning message.
     */
    public void warn( String msg )
    {
        if ( isWarnEnabled() )
        {
            internalLog( Status.WARNING, msg, null );
        }
    }


    // ── Forward a One-Arg Warning ─────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a warning message with one format argument.
     *
     * @param format  the SLF4J format string.
     * @param arg     the single argument.
     */
    public void warn( String format, Object arg )
    {
        if ( isWarnEnabled() )
        {
            String msgStr = MessageFormatter.format( format, arg ).getMessage();
            internalLog( Status.WARNING, msgStr, null );
        }
    }


    // ── Forward a Two-Arg Warning ─────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a warning message with two format arguments.
     *
     * @param format  the SLF4J format string.
     * @param arg1    the first argument.
     * @param arg2    the second argument.
     */
    public void warn( String format, Object arg1, Object arg2 )
    {
        if ( isWarnEnabled() )
        {
            String msgStr = MessageFormatter.format( format, arg1, arg2 ).getMessage();
            internalLog( Status.WARNING, msgStr, null );
        }
    }


    // ── Forward a Varargs Warning ─────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a warning message with a variable number of format arguments.
     *
     * @param format    the SLF4J format string.
     * @param argArray  the arguments.
     */
    public void warn( String format, Object... argArray )
    {
        if ( isWarnEnabled() )
        {
            String msgStr = MessageFormatter.arrayFormat( format, argArray ).getMessage();
            internalLog( Status.WARNING, msgStr, null );
        }
    }


    // ── Forward a Warning with an Exception ───────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Logs a warning message with an accompanying throwable.
     *
     * @param msg  the warning message.
     * @param t    the throwable.
     */
    public void warn( String msg, Throwable t )
    {
        if ( isWarnEnabled() )
        {
            internalLog( Status.WARNING, msg, t );
        }
    }


    // INFO disabled, it would write too much logs

    // ── Is INFO Enabled? — No, Too Much Noise ────────────────────────────────
    // Routine status messages would flood the Error Log view and bury real alerts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — INFO would write too much to the Eclipse log.
     *
     * @return {@code false}.
     */
    public boolean isInfoEnabled()
    {
        return false;
    }


    /** No-op — INFO is disabled. @param msg ignored. */
    public void info( String msg )
    {
    }


    /** No-op — INFO is disabled. @param format ignored. @param arg ignored. */
    public void info( String format, Object arg )
    {
    }


    /** No-op — INFO is disabled. @param format ignored. @param arg1 ignored. @param arg2 ignored. */
    public void info( String format, Object arg1, Object arg2 )
    {
    }


    /** No-op — INFO is disabled. @param format ignored. @param argArray ignored. */
    public void info( String format, Object... argArray )
    {
    }


    /** No-op — INFO is disabled. @param msg ignored. @param t ignored. */
    public void info( String msg, Throwable t )
    {
    }


    // DEBUG disabled, there is no appropriate log level in Eclipse log

    // ── Is DEBUG Enabled? — No, No Equivalent Eclipse Level ──────────────────
    // The Eclipse log has no DEBUG severity; we don't forward these.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — there is no DEBUG-equivalent severity in
     * the Eclipse platform log.
     *
     * @return {@code false}.
     */
    public boolean isDebugEnabled()
    {
        return false;
    }


    /** No-op — DEBUG is disabled. @param msg ignored. */
    public void debug( String msg )
    {
    }


    /** No-op — DEBUG is disabled. @param format ignored. @param arg ignored. */
    public void debug( String format, Object arg )
    {
    }


    /** No-op — DEBUG is disabled. @param format ignored. @param arg1 ignored. @param arg2 ignored. */
    public void debug( String format, Object arg1, Object arg2 )
    {
    }


    /** No-op — DEBUG is disabled. @param format ignored. @param argArray ignored. */
    public void debug( String format, Object... argArray )
    {
    }


    /** No-op — DEBUG is disabled. @param msg ignored. @param t ignored. */
    public void debug( String msg, Throwable t )
    {
    }


    // TRACE disabled, there is no appropriate log level in Eclipse log

    // ── Is TRACE Enabled? — No, No Equivalent Eclipse Level ──────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — there is no TRACE-equivalent severity in
     * the Eclipse platform log.
     *
     * @return {@code false}.
     */
    public boolean isTraceEnabled()
    {
        return false;
    }


    /** No-op — TRACE is disabled. @param msg ignored. */
    public void trace( String msg )
    {
    }


    /** No-op — TRACE is disabled. @param format ignored. @param arg ignored. */
    public void trace( String format, Object arg )
    {
    }


    /** No-op — TRACE is disabled. @param format ignored. @param arg1 ignored. @param arg2 ignored. */
    public void trace( String format, Object arg1, Object arg2 )
    {
    }


    /** No-op — TRACE is disabled. @param format ignored. @param argArray ignored. */
    public void trace( String format, Object... argArray )
    {
    }


    /** No-op — TRACE is disabled. @param msg ignored. @param t ignored. */
    public void trace( String msg, Throwable t )
    {
    }
}
