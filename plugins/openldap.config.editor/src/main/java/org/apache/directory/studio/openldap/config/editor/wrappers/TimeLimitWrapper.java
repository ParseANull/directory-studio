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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: TimeLimitWrapper — The Star Destroyer's Mission Timer ───────────────
// Every mission on a Star Destroyer runs against the clock.  TimeLimitWrapper
// wraps one or more "time=..." tokens from the olcLimits (or olcTimeLimit)
// attribute.  It parses time=, time.hard=, and time.soft= tokens — including
// the special sentinels "unlimited", "none", and "soft" — and rolls them up
// into globalLimit, hardLimit, and softLimit fields inherited from
// AbstractLimitWrapper.  getType() returns "time" to distinguish it from
// SizeLimitWrapper.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * This class wraps the TimeLimit parameter :
 * <pre>
 * time      ::= 'time' timeLimit time-e
 * time-e    ::= ' time' timeLimit time-e | e
 * timeLimit ::= '.soft=' limit | '.hard=' hardLimit | '=' limit
 * limit     ::= 'unlimited' | 'none' | INT
 * hardLimit ::= 'soft' | limit
 * </pre>
 *
 * Note : each of the limit is an Integer, so that we can have two states :
 * <ul>
 * <li>not existent</li>
 * <li>has a value</li>
 * </ul>
 * A -1 value means unlimited. Any other value is accepted, if > 0.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TimeLimitWrapper extends AbstractLimitWrapper
{
    // ── Protected No-Arg Constructor — An Unset Mission Timer ─────────────────
    // The flight controller creates an empty timer before parsing the token
    // string into it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a TimeLimitWrapper instance
     */
    protected TimeLimitWrapper()
    {
        super();
    }


    // ── Constructor (Integer, Integer, Integer) — Set All Three Timers ─────────
    // The flight controller sets the global, hard, and soft time limits
    // directly when all three values are already known.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a TimeLimitWrapper instance
     *
     * @param globalLimit The global limit
     * @param hardLimit The hard limit
     * @param softLimit The soft limit
     */
    public TimeLimitWrapper( Integer globalLimit, Integer hardLimit, Integer softLimit )
    {
        super( globalLimit, hardLimit, softLimit );
    }


    // ── Constructor (String) — Parse the Time-Limit Token String ───────────────
    // The flight controller reads the raw olcLimits time-limit substring, splits
    // it on spaces, and delegates each "time..." token to parseLimit.  When hard
    // and soft limits turn out to match, they are collapsed into a single global
    // limit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a TimeLimitWrapper instance from a String.
     *
     * @param timeLimitStr The String that contain the value
     */
    public TimeLimitWrapper( String timeLimitStr )
    {
        if ( !Strings.isEmpty( timeLimitStr ) )
        {
            // use a lowercase version of the string
            String lowerCaseTimeLimitStr = timeLimitStr.toLowerCase();

            TimeLimitWrapper tmp = new TimeLimitWrapper();

            // Split the strings
            String[] limits = lowerCaseTimeLimitStr.split( " " );

            if ( limits != null )
            {
                // Parse each limit
                for ( String limit : limits )
                {
                    tmp.clear();
                    boolean result = parseLimit( tmp, limit );

                    if ( !result )
                    {
                        clear();
                        isValid = false;
                        break;
                    }
                    else
                    {
                        if ( tmp.globalLimit != null )
                        {
                            // replace the existing global limit, nullify the hard and soft limit
                            globalLimit = tmp.globalLimit;
                            hardLimit = null;
                            softLimit = null;
                        }
                        else
                        {
                            // We don't set the soft and hard limit of the global limit is not null
                            if ( globalLimit == null )
                            {
                                if ( tmp.softLimit != null )
                                {
                                    softLimit = tmp.softLimit;

                                    if ( hardLimit != null )
                                    {
                                        if ( hardLimit.equals( HARD_SOFT ) || hardLimit.equals( softLimit ) )
                                        {
                                            // Special case : we have had a time.hard=soft before,
                                            // or the hard and soft limit are equals : we set the global limit
                                            globalLimit = softLimit;
                                            softLimit = null;
                                            hardLimit = null;
                                        }
                                    }
                                }
                                else if ( tmp.hardLimit != null )
                                {
                                    if ( ( tmp.hardLimit.equals( HARD_SOFT ) && ( softLimit != null ) ) || tmp.hardLimit.equals( softLimit ) )
                                    {
                                        // special case, softLimit was set and hardLimit was time.hard=soft,
                                        // or is equal to softLimit
                                        globalLimit = softLimit;
                                        softLimit = null;
                                        hardLimit = null;
                                    }
                                    else
                                    {
                                        hardLimit = tmp.hardLimit;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // ── parseLimit (private static) — Parse a Single "time..." Token ───────────
    // The flight controller reads one space-delimited token (e.g. "time.hard=30")
    // and sets the corresponding field in the temporary TimeLimitWrapper.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parse a single limit :
     * <pre>
     * timeLimit ::= 'time' ( '.hard=' hardLimit | '.soft=' limit | '=' limit )
     * limit     ::= 'unlimited' | 'none' | INT
     * hardLimit ::= 'soft' | limit
     * </pre>
     * @param tlw
     * @param limitStr
     */
    private static boolean parseLimit( TimeLimitWrapper tlw, String limitStr )
    {
        int pos = 0;

        // The timelimit always starts with a "time"
        if ( limitStr.startsWith( "time" ) )
        {
            pos += 4;

            // A global or hard/soft ?
            if ( limitStr.startsWith( "=", pos ) )
            {
                // Global : get the limit
                pos++;

                if ( limitStr.startsWith( UNLIMITED_STR, pos ) )
                {
                    pos += 9;
                    tlw.globalLimit = UNLIMITED;
                }
                else if ( limitStr.startsWith( NONE_STR, pos ) )
                {
                    pos += 4;
                    tlw.globalLimit = UNLIMITED;
                }
                else
                {
                    String integer = getInteger( limitStr, pos );

                    if ( integer != null )
                    {
                        pos += integer.length();

                        try
                        {
                            Integer value = Integer.valueOf( integer );

                            if ( value > UNLIMITED )
                            {
                                tlw.globalLimit = value;
                            }
                            else
                            {
                                return false;
                            }
                        }
                        catch ( NumberFormatException nfe )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else if ( limitStr.startsWith( ".hard=", pos ) )
            {
                // Hard limit : get the hard limit
                pos += 6;

                if ( limitStr.startsWith( UNLIMITED_STR, pos ) )
                {
                    pos += 9;

                    tlw.hardLimit = UNLIMITED;
                }
                else if ( limitStr.startsWith( NONE_STR, pos ) )
                {
                    pos += 4;

                    tlw.hardLimit = UNLIMITED;
                }
                else if ( limitStr.startsWith( SOFT_STR, pos ) )
                {
                    pos += 4;
                    tlw.globalLimit = HARD_SOFT;
                }
                else
                {
                    String integer = getInteger( limitStr, pos );

                    if ( integer != null )
                    {
                        pos += integer.length();

                        try
                        {
                            Integer value =  Integer.valueOf( integer );

                            if ( value >= UNLIMITED )
                            {
                                tlw.hardLimit = value;
                            }
                        }
                        catch ( NumberFormatException nfe )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else if ( limitStr.startsWith( ".soft=", pos ) )
            {
                // Soft limit : get the limit
                pos += 6;

                if ( limitStr.startsWith( UNLIMITED_STR, pos ) )
                {
                    pos += 9;

                    tlw.softLimit = UNLIMITED;
                }
                else if ( limitStr.startsWith( NONE_STR, pos ) )
                {
                    pos += 4;

                    tlw.softLimit = UNLIMITED;
                }
                else
                {
                    String integer = getInteger( limitStr, pos );

                    if ( integer != null )
                    {
                        pos += integer.length();

                        try
                        {
                            Integer value = Integer.valueOf( integer );

                            if ( value > UNLIMITED )
                            {
                                tlw.softLimit = value;
                            }
                            else
                            {
                                return false;
                            }
                        }
                        catch ( NumberFormatException nfe )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else
            {
                // This is wrong
                return false;
            }
        }
        else
        {
            // This is wrong
            return false;
        }

        // last check : the pos should be equal to the limitStr length
        return ( pos == limitStr.length() );
    }


    // ── getType — Identify This Limit as a Time Limit ─────────────────────────
    // The mission timer identifies itself by type so the UI can label the column
    // correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return The Limit's type
     */
    public String getType()
    {
        return "time";
    }
}
