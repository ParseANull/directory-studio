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


// ── CLASS: RetryPair — One Phase of the Sector Command's Reconnection Plan ───
// When a sector command loses its HoloNet connection to Imperial HQ it doesn't
// give up immediately.  It follows a staged reconnection plan: "try every 10
// seconds up to 5 times, then try every 60 seconds forever."  Each stage of
// that plan is a retry pair — an interval (how often to try) and a retries
// count (how many times, or '+' meaning forever).
// RetryPair models one such stage.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * One stage in a syncrepl retry schedule — a pair of (interval, retries).
 * Format: {@code "<retry interval> <# of retries>"} where the retry count can
 * be a positive integer or {@code "+"} (meaning retry indefinitely).
 * The constant {@link #PLUS} ({@code -1}) represents the {@code "+"} value.
 * For example — {@code "10 5"} means retry every 10 seconds up to 5 times.
 * {@code "60 +"} means retry every 60 seconds indefinitely.
 * Think of this as one phase of the sector command's reconnection plan.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RetryPair
{
    /** The '+' retries value */
    public static final int PLUS = -1;

    /** The pattern used for parsing */
    private static final Pattern pattern = Pattern.compile( "^([0-9]+) ([0-9]+|\\+)$" );

    /** The interval */
    private int interval;

    /** The retries */
    private int retries;


    // ── Copy One Reconnection Phase ───────────────────────────────────────────
    // Each consumer can have its own independent retry schedule — we copy the
    // pair so editing one consumer's schedule doesn't affect another's.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given retry pair, or {@code null} if the input is {@code null}.
     *
     * @param retryPair  the pair to copy.
     * @return           a new {@link RetryPair} with the same interval and retries values.
     */
    public static RetryPair copy( RetryPair retryPair )
    {
        if ( retryPair != null )
        {
            RetryPair retryPairCopy = new RetryPair();

            retryPairCopy.setInterval( retryPair.getInterval() );
            retryPairCopy.setRetries( retryPair.getRetries() );

            return retryPairCopy;
        }

        return null;
    }


    // ── Copy This Reconnection Phase ──────────────────────────────────────────
    // Convenience instance method delegating to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this retry pair.
     *
     * @return  a new {@link RetryPair} with the same values.
     */
    public RetryPair copy()
    {
        return RetryPair.copy( this );
    }


    // ── Decode One Reconnection Phase from the Config String ──────────────────
    // The config stores each phase as "interval retries" — we match the two
    // space-separated tokens and parse them: the retries token is special because
    // '+' means "retry forever" and maps to our PLUS sentinel value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a retry pair string in {@code "<interval> <retries>"} format.
     * The retries component can be a non-negative integer or {@code "+"}.
     *
     * @param s  the pair string, e.g. {@code "10 5"} or {@code "60 +"}.
     * @return   the parsed {@link RetryPair}.
     * @throws ParseException  if the string doesn't match the expected format
     *                         or if any numeric component can't be parsed.
     */
    public static RetryPair parse( String s ) throws ParseException
    {
        // Creating the retry pair
        RetryPair retryPair = new RetryPair();

        // Matching the string
        Matcher matcher = pattern.matcher( s );

        // Checking the result
        if ( matcher.find() )
        {
            // Interval
            String interval = matcher.group( 1 );

            try
            {
                retryPair.setInterval( Integer.parseInt( interval ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert interval value '" + interval + "' as an integer.", 0 );
            }

            // Retries
            String retries = matcher.group( 2 );

            if ( "+".equalsIgnoreCase( retries ) )
            {
                retryPair.setRetries( PLUS );
            }
            else
            {
                try
                {
                    retryPair.setRetries( Integer.parseInt( retries ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new ParseException( "Unable to convert retries value '" + retries + "' as an integer.", 0 );
                }
            }
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid retry pair.", 0 );
        }

        return retryPair;
    }


    /**
     * Returns the retry interval in seconds.
     *
     * @return  seconds between consecutive retry attempts.
     */
    public int getInterval()
    {
        return interval;
    }


    /**
     * Returns the maximum number of retries, or {@link #PLUS} ({@code -1}) for unlimited.
     *
     * @return  the retry count, or {@link #PLUS}.
     */
    public int getRetries()
    {
        return retries;
    }


    /**
     * Sets the retry interval in seconds.
     *
     * @param interval  seconds between consecutive retry attempts.
     */
    public void setInterval( int interval )
    {
        this.interval = interval;
    }


    /**
     * Sets the maximum number of retries.  Use {@link #PLUS} for unlimited retries.
     *
     * @param retries  the retry count, or {@link #PLUS} ({@code -1}).
     */
    public void setRetries( int retries )
    {
        this.retries = retries;
    }


    // ── Write This Reconnection Phase Back into the Config ────────────────────
    // We output "interval retries" where retries is either an integer or "+" for
    // the PLUS sentinel, producing the token OpenLDAP expects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this retry pair as an {@code "<interval> <retries>"} string.
     * {@link #PLUS} is rendered as {@code "+"}.
     *
     * @return  the formatted retry pair string.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( interval );
        sb.append( " " );

        if ( retries == PLUS )
        {
            sb.append( "+" );
        }
        else
        {
            sb.append( retries );
        }

        return sb.toString();
    }
}
