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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// ── CLASS: Retry — The Sector Command's Full Reconnection Plan ────────────────
// A sector command that loses its HoloNet link follows a multi-stage
// reconnection plan.  The plan is expressed as a list of (interval, count)
// phases — for example: "try every 5 s five times, then every 30 s ten times,
// then every 300 s forever."  Retry holds that full sequence of RetryPair objects
// and serialises/deserialises them from the syncrepl "retry" parameter string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Models the syncrepl {@code retry} parameter — the full reconnection schedule.
 * A retry schedule is an ordered list of {@link RetryPair} objects.  Each pair
 * specifies an interval (seconds between attempts) and a retry count (or
 * {@code "+"} for indefinite).  OpenLDAP uses the phases in order, moving to
 * the next once the current phase's retry count is exhausted.
 * Format: {@code "[<retry interval> <# of retries>]+"} — pairs separated by spaces.
 * Think of this as the sector command's full staged reconnection plan.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Retry
{
    /** The pattern used for parsing */
    private static final Pattern pattern = Pattern.compile( "^(([0-9]+) ([0-9]+|\\+))( ([0-9]+) ([0-9]+|\\+))*$" );

    /** The pairs */
    private List<RetryPair> pairs = new ArrayList<RetryPair>();


    // ── Copy the Full Reconnection Plan ───────────────────────────────────────
    // Each consumer has its own reconnection plan — we deep-copy all RetryPair
    // objects so the copy is fully independent of the original.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given retry schedule, or {@code null} if the input is {@code null}.
     * Each {@link RetryPair} is copied individually.
     *
     * @param retry  the retry schedule to copy.
     * @return       a new {@link Retry} with copied pairs.
     */
    public static Retry copy( Retry retry )
    {
        if ( retry != null )
        {
            Retry retryCopy = new Retry();

            for ( RetryPair retryPair : retry.getPairs() )
            {
                retryCopy.addPair( RetryPair.copy( retryPair ) );
            }

            return retryCopy;
        }

        return null;
    }


    // ── Copy This Reconnection Plan ───────────────────────────────────────────
    // Convenience instance method delegating to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this retry schedule.
     *
     * @return  a new {@link Retry} with the same ordered list of pairs.
     */
    public Retry copy()
    {
        return Retry.copy( this );
    }


    // ── Decode the Full Reconnection Plan from the Config String ──────────────
    // The config stores the plan as space-separated pairs — "5 5 30 10 300 +" —
    // we first validate the overall format with a regex, then split into even
    // chunks and parse each pair via RetryPair.parse().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a retry schedule string into a {@link Retry} object.
     * The string must contain an even number of space-separated tokens, where
     * each consecutive pair is (interval, retries).
     *
     * @param s  the retry schedule string, e.g. {@code "5 5 30 10 300 +"}.
     * @return   the parsed {@link Retry}.
     * @throws ParseException  if the string doesn't match the expected format.
     */
    public static Retry parse( String s ) throws ParseException
    {
        // Creating the retry
        Retry retry = new Retry();

        // Matching the string
        Matcher matcher = pattern.matcher( s );

        // Checking the result
        if ( matcher.find() )
        {
            // Splitting the string into pieces
            String[] pieces = s.split( " " );

            // Checking we got a even number of pieces
            if ( ( pieces.length % 2 ) == 0 )
            {
                for ( int i = 0; i < pieces.length; i = i + 2 )
                {
                    retry.addPair( RetryPair.parse( pieces[i] + " " + pieces[i + 1] ) );
                }
            }
            else
            {
                throw new ParseException( "Unable to parse string '" + s + "' as a valid retry.", 0 );
            }
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid retry.", 0 );
        }

        return retry;
    }


    /**
     * Appends a retry pair to the end of the schedule.
     *
     * @param pair  the pair to add.
     */
    public void addPair( RetryPair pair )
    {
        pairs.add( pair );
    }


    /**
     * Returns all retry pairs in this schedule as an array.
     *
     * @return  the ordered array of retry pairs; never {@code null}.
     */
    public RetryPair[] getPairs()
    {
        return pairs.toArray( new RetryPair[0] );
    }


    /**
     * Removes the given retry pair from the schedule.
     *
     * @param pair  the pair to remove.
     */
    public void removePair( RetryPair pair )
    {
        pairs.remove( pair );
    }


    /**
     * Replaces the current list of pairs with the given array.
     *
     * @param pairs  the new ordered array of retry pairs.
     */
    public void setPairs( RetryPair[] pairs )
    {
        this.pairs = new ArrayList<RetryPair>();
        this.pairs.addAll( Arrays.asList( pairs ) );
    }


    /**
     * Returns the number of retry pairs in the schedule.
     *
     * @return  the pair count.
     */
    public int size()
    {
        return pairs.size();
    }


    // ── Write the Full Reconnection Plan Back into the Config ─────────────────
    // We join all the pair strings with spaces to produce the single token
    // that goes into the syncrepl "retry=" value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full retry schedule as a space-separated string.
     * Each pair contributes two space-separated tokens; pairs themselves are
     * separated by a single space, producing a flat token sequence.
     *
     * @return  the formatted retry schedule string, e.g. {@code "5 5 30 10 300 +"}.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for ( int i = 0; i < pairs.size(); i++ )
        {
            sb.append( pairs.get( i ).toString() );

            if ( i != ( pairs.size() - 1 ) )
            {
                sb.append( " " );
            }
        }

        return sb.toString();
    }
}
