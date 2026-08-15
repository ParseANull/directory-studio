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
package org.apache.directory.studio.openldap.syncrepl;


import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// ── CLASS: KeepAlive — The Empire's Persistent Channel Heartbeat ─────────────
// The sector command keeps its connection to Imperial HQ open at all times.
// To prevent the HoloNet link from silently dying during quiet periods, the
// communications droid sends a heartbeat: after N seconds of idle silence,
// it sends up to P probe packets, one every I seconds.  If none of the probes
// receive a response the link is declared dead and the sector command must
// reconnect.
// This class models that heartbeat config — the syncrepl "keepalive" parameter
// in "idle:probes:interval" format — for an OpenLDAP LDAP replication link.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Models the syncrepl {@code keepalive} parameter.
 * Controls TCP keep-alive behaviour on the replication connection.
 * Format: {@code "<idle>:<probes>:<interval>"} where all three are integers
 * (seconds for idle and interval, count for probes).
 * For example — {@code "60:3:10"} means: after 60 s of idle, send up to
 * 3 probes spaced 10 s apart before considering the connection dead.
 * Think of this as the Imperial communications droid's heartbeat protocol
 * for the sector command's HoloNet link.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class KeepAlive
{
    /** The pattern used for parsing */
    private static final Pattern pattern = Pattern.compile( "^([0-9]+):([0-9]+):([0-9]+)$" );

    /** The idle */
    private int idle;

    /** The probes */
    private int probes;

    /** The interval */
    private int interval;


    // ── Blank Heartbeat Config — Fill In the Values Later ────────────────────
    // Creates a keep-alive object with all fields at 0; the caller populates
    // them via setters or via the full constructor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a zero-valued keep-alive configuration.
     * Use setters or the three-argument constructor to provide actual values.
     */
    public KeepAlive()
    {
    }


    // ── All Three Heartbeat Parameters Specified Upfront ──────────────────────
    // The communications droid is configured with all three values at once:
    // idle time, probe count, and probe interval.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a keep-alive configuration with all three parameters.
     *
     * @param idle      seconds of idle time before the first probe is sent.
     * @param probes    maximum number of probe packets to send.
     * @param interval  seconds between consecutive probe packets.
     */
    public KeepAlive( int idle, int probes, int interval )
    {
        this.idle = idle;
        this.probes = probes;
        this.interval = interval;
    }


    // ── Copy the Heartbeat Config for Another Sector Command ──────────────────
    // Each sector command gets its own copy of the keep-alive configuration
    // so they can adjust settings independently.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given keep-alive configuration, or {@code null}
     * if the input is {@code null}.
     *
     * @param keepAlive  the keep-alive to copy.
     * @return           a new {@link KeepAlive} with identical field values.
     */
    public static KeepAlive copy( KeepAlive keepAlive )
    {
        if ( keepAlive != null )
        {
            KeepAlive keepAliveCopy = new KeepAlive();

            keepAliveCopy.setIdle( keepAlive.getIdle() );
            keepAliveCopy.setProbes( keepAlive.getProbes() );
            keepAliveCopy.setInterval( keepAlive.getInterval() );

            return keepAliveCopy;
        }

        return null;
    }


    // ── Copy This Heartbeat Config ────────────────────────────────────────────
    // Convenience instance method delegating to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this keep-alive configuration.
     *
     * @return  a new {@link KeepAlive} with the same values.
     */
    public KeepAlive copy()
    {
        return KeepAlive.copy( this );
    }


    // ── Decode the Heartbeat Config from the Configuration String ─────────────
    // The syncrepl directive stores the keep-alive as "idle:probes:interval" —
    // we split on ':' using a regex and parse each integer from the captured groups.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a keep-alive string in {@code "<idle>:<probes>:<interval>"} format.
     * All three components must be non-negative integers.
     *
     * @param s  the keep-alive string, e.g. {@code "60:3:10"}.
     * @return   the parsed {@link KeepAlive}.
     * @throws ParseException  if the string doesn't match the expected format
     *                         or any component can't be parsed as an integer.
     */
    public static KeepAlive parse( String s ) throws ParseException
    {
        // Creating the keep alive
        KeepAlive keepAlive = new KeepAlive();

        // Matching the string
        Matcher matcher = pattern.matcher( s );

        // Checking the result
        if ( matcher.find() )
        {
            // Idle
            String idle = matcher.group( 1 );

            try
            {
                keepAlive.setIdle( Integer.parseInt( idle ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert idle value '" + idle + "' as an integer.", 0 );
            }

            // Probes
            String probes = matcher.group( 2 );

            try
            {
                keepAlive.setProbes( Integer.parseInt( probes ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert probes value '" + probes + "' as an integer.", 0 );
            }

            // Interval
            String interval = matcher.group( 3 );

            try
            {
                keepAlive.setInterval( Integer.parseInt( interval ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert interval value '" + interval + "' as an integer.", 0 );
            }
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid keep alive.", 0 );
        }

        return keepAlive;
    }


    /**
     * Returns the idle time in seconds before the first probe is sent.
     *
     * @return  the idle seconds.
     */
    public int getIdle()
    {
        return idle;
    }


    /**
     * Returns the maximum number of probe packets to send.
     *
     * @return  the probe count.
     */
    public int getProbes()
    {
        return probes;
    }


    /**
     * Returns the interval in seconds between consecutive probe packets.
     *
     * @return  the probe interval seconds.
     */
    public int getInterval()
    {
        return interval;
    }


    /**
     * Sets the idle time in seconds.
     *
     * @param idle  seconds of idle before the first probe.
     */
    public void setIdle( int idle )
    {
        this.idle = idle;
    }


    /**
     * Sets the maximum number of probe packets.
     *
     * @param probes  the probe count.
     */
    public void setProbes( int probes )
    {
        this.probes = probes;
    }


    /**
     * Sets the interval between probe packets in seconds.
     *
     * @param interval  the probe interval seconds.
     */
    public void setInterval( int interval )
    {
        this.interval = interval;
    }


    // ── Write the Heartbeat Config Back into the Directive ────────────────────
    // We format the three integers separated by colons to produce the token
    // that goes into the OpenLDAP syncrepl configuration.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the keep-alive as an {@code "<idle>:<probes>:<interval>"} string
     * for use in the OpenLDAP syncrepl directive.
     *
     * @return  the formatted keep-alive string.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( idle );
        sb.append( ":" );
        sb.append( probes );
        sb.append( ":" );
        sb.append( interval );

        return sb.toString();
    }
}
