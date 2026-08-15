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
package org.apache.directory.studio.openldap.common.ui.model;

// ── CLASS: LogLevelEnum — DEATH STAR THREAT ALERT LEVEL SYSTEM ───────────────
// Picture the Death Star's layered alert system where each subsystem has its
// own threat indicator: trace is the lowest-level sensor sweep, packets and
// args watch the communications traffic, conns monitors docking connections,
// BER tracks the binary encoding layer, filter watches search queries, config
// flags configuration events, ACL traces access-control decisions, stats and
// stats2 summarize throughput metrics, shell and parse handle external
// processing, sync tracks replication. NONE is silent running; ANY lights up
// every sensor at once (-1).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized OpenLDAP log-level bit-flags along with their
 * integer values. We also provide helper methods to convert between the
 * bitmask integer and human-readable text, and to parse a log-level string
 * (which may contain names, decimal integers, or hex values). The possible
 * values are:
 * <ul>
 * <li>none        0</li>
 * <li>trace       1</li>
 * <li>packets     2</li>
 * <li>args        4</li>
 * <li>conns       8</li>
 * <li>BER        16</li>
 * <li>filter     32</li>
 * <li>config     64</li>
 * <li>ACL       128</li>
 * <li>stats     256</li>
 * <li>stats2    512</li>
 * <li>shell    1024</li>
 * <li>parse    2048</li>
 * <li>sync    16384</li>
 * <li>any       -1</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LogLevelEnum
{
    NONE( "none", 0 ),
    TRACE( "trace", 1 ),
    PACKETS( "packets", 2 ),
    ARGS( "args", 4 ),
    CONNS( "conns", 8 ),
    BER( "ber", 16 ),
    FILTER( "filter", 32 ),
    CONFIG( "config", 64 ),
    ACL( "acl", 128 ),
    STATS( "stats", 256 ),
    STATS2( "stats2", 512 ),
    SHELL( "shell", 1024 ),
    PARSE( "parse", 2048 ),
    // 4096 not used
    // 8196 not used
    SYNC( "sync", 16384 ),
    // 327168 and -1 are equivalent
    ANY( "any", -1 );

    /** The inner value */
    private int value;

    /** The inner name */
    private String name;


    // ── CONSTRUCTOR: LogLevelEnum — REGISTERING AN ALERT SENSOR ──────────────
    // Each log-level constant records both its human-readable name and the
    // integer bit-value so we can convert in both directions without loss.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We initialize each constant with its configuration-file name and its
     * integer bit-flag value.
     *
     * @param name   the log-level name string (e.g., {@code "acl"})
     * @param value  the integer bit-flag (e.g., {@code 128})
     */
    private LogLevelEnum( String name, int value )
    {
        this.name = name;
        this.value = value;
    }


    // ── METHOD: getValue — READING THE ALERT SENSOR BIT-FLAG ─────────────────
    // We return the integer bit-value so callers can OR multiple levels together
    // to build a composite bitmask.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the integer bit-flag for this log level (e.g., {@code 128} for
     * ACL). Callers OR multiple values together to form a composite bitmask.
     *
     * @return the integer bit-flag value
     */
    public int getValue()
    {
        return value;
    }


    // ── METHOD: getName — READING THE ALERT SENSOR LABEL ─────────────────────
    // We return the configuration-file name so callers can display or serialize
    // this log level without knowing the underlying integer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the configuration-file string for this log level
     * (e.g., {@code "acl"}).
     *
     * @return the log level name string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL SENSOR LABELS ─────────────────────────
    // We assemble all name strings into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all log-level name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( LogLevelEnum logLevel : values() )
        {
            names[pos] = logLevel.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getLogLevelText — TRANSLATING A BITMASK TO READABLE TEXT ──────
    // We decode the bitmask by testing each bit in turn and accumulating the
    // names of all active sensors into a space-separated string. The special
    // cases "none" (0) and "any" (-1) short-circuit this logic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We convert a log-level integer bitmask to a space-separated string of
     * level names (e.g., {@code "ACL stats conns"}). We handle the special
     * values {@code 0} (none) and {@code -1} (any) directly.
     *
     * @param logLevel  the integer bitmask to decode
     * @return          a space-separated string of active level names
     */
    public static String getLogLevelText( int logLevel )
    {
        if ( logLevel == NONE.value )
        {
            return "none";
        }

        if ( logLevel == ANY.value )
        {
            return "any";
        }

        StringBuilder sb = new StringBuilder();

        if ( ( logLevel & ACL.value ) != 0 )
        {
            sb.append( "ACL " );
        }

        if ( ( logLevel & ARGS.value ) != 0 )
        {
            sb.append( "args " );
        }

        if ( ( logLevel & BER.value ) != 0 )
        {
            sb.append( "BER " );
        }

        if ( ( logLevel & CONFIG.value ) != 0 )
        {
            sb.append( "config " );
        }

        if ( ( logLevel & CONNS.value ) != 0 )
        {
            sb.append( "conns " );
        }

        if ( ( logLevel & FILTER.value ) != 0 )
        {
            sb.append( "filter " );
        }

        if ( ( logLevel & PACKETS.value ) != 0 )
        {
            sb.append( "packets " );
        }

        if ( ( logLevel & PARSE.value ) != 0 )
        {
            sb.append( "parse " );
        }

        if ( ( logLevel & SHELL.value ) != 0 )
        {
            sb.append( "shell " );
        }

        if ( ( logLevel & STATS.value ) != 0 )
        {
            sb.append( "stats " );
        }

        if ( ( logLevel & STATS2.value ) != 0 )
        {
            sb.append( "stats2 " );
        }

        if ( ( logLevel & SYNC.value ) != 0 )
        {
            sb.append( "sync " );
        }

        if ( ( logLevel & TRACE.value ) != 0 )
        {
            sb.append( "trace " );
        }

        return sb.toString();
    }


    // ── METHOD: getIntegerValue — LOOKING UP A BIT-FLAG BY SENSOR NAME ────────
    // We map each recognized name string to its integer bit-flag. If the name
    // is null, empty, or unrecognized we throw an IllegalArgumentException so
    // the caller knows exactly what went wrong.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the integer bit-flag for the log level with the given name.
     * The comparison is case-insensitive. We throw {@link IllegalArgumentException}
     * if the name is null, empty, or not recognized.
     *
     * @param name  the log level name to look up
     * @return      the corresponding integer bit-flag
     * @throws IllegalArgumentException if the name is not recognized
     */
    public static int getIntegerValue( String name )
    {
        if ( ( name == null ) || ( name.length() == 0 ) )
        {
            throw new IllegalArgumentException( "Wrong LogLevel name : " + name );
        }

        if ( "acl".equalsIgnoreCase( name ) )
        {
            return ACL.value;
        }

        if ( "any".equalsIgnoreCase( name ) )
        {
            return ANY.value;
        }

        if ( "args".equalsIgnoreCase( name ) )
        {
            return ARGS.value;
        }

        if ( "ber".equalsIgnoreCase( name ) )
        {
            return BER.value;
        }

        if ( "config".equalsIgnoreCase( name ) )
        {
            return CONFIG.value;
        }

        if ( "conns".equalsIgnoreCase( name ) )
        {
            return CONNS.value;
        }

        if ( "filter".equalsIgnoreCase( name ) )
        {
            return FILTER.value;
        }

        if ( "none".equalsIgnoreCase( name ) )
        {
            return NONE.value;
        }

        if ( "packets".equalsIgnoreCase( name ) )
        {
            return PACKETS.value;
        }

        if ( "parse".equalsIgnoreCase( name ) )
        {
            return PARSE.value;
        }

        if ( "shell".equalsIgnoreCase( name ) )
        {
            return SHELL.value;
        }

        if ( "stats".equalsIgnoreCase( name ) )
        {
            return STATS.value;
        }

        if ( "stats2".equalsIgnoreCase( name ) )
        {
            return STATS2.value;
        }

        if ( "sync".equalsIgnoreCase( name ) )
        {
            return SYNC.value;
        }

        if ( "trace".equalsIgnoreCase( name ) )
        {
            return TRACE.value;
        }

        throw new IllegalArgumentException( "Wrong LogLevel name : " + name );
    }


    // ── METHOD: parseLogLevel — DECODING A MIXED-FORMAT SENSOR STRING ─────────
    // The OpenLDAP log-level value can be a space-separated mix of names,
    // decimal integers, and hex literals. We walk the character array token by
    // token, dispatching on the first character to recognize each format, and
    // OR the resulting bit-flags together. An illegal character mid-token throws
    // IllegalArgumentException so parsing failures are caught early.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We parse a space-separated log-level string that may contain level names
     * (case-insensitive), decimal integers, or hex literals (e.g.,
     * {@code "ACL stats 0x40"}) and return the OR of all their bit-flags.
     * An empty or null input returns {@code 0}.
     * <p>
     * Format:
     * <pre>
     * &lt;logLevel&gt; ::= ( Integer | Hex | &lt;Name&gt; )*
     * &lt;name&gt; ::= 'none' | 'any' | 'ACL' | 'args' | 'BER' | 'config' | 'conns' |
     *              'filter' | 'packets' | 'parse' | 'stats' | 'stats2' | 'sync' | 'trace'
     *              ;; Note: those names are case insensitive
     * </pre>
     * TODO parseLogLevel.
     *
     * @param logLevelString  the space-separated log-level string to parse
     * @return                the OR of all recognized bit-flags
     * @throws IllegalArgumentException if an unrecognized token is encountered
     */
    public static int parseLogLevel( String logLevelString )
    {
        if ( ( logLevelString == null ) || ( logLevelString.length() == 0 ) )
        {
            return 0;
        }

        int currentPos = 0;
        char[] chars = logLevelString.toCharArray();
        int logLevel = 0;

        while ( currentPos < chars.length )
        {
            // Skip the ' ' at the beginning
            while ( ( currentPos < chars.length ) && ( chars[currentPos] == ' ' ) )
            {
                currentPos++;
            }

            if ( currentPos >= chars.length )
            {
                break;
            }

            // Now, start analysing what's next
            switch ( chars[currentPos] )
            {
                case 'a' :
                case 'A' :
                    // ACL, ANY or ARGS
                    if ( parseName( chars, currentPos, "ACL" ) )
                    {
                        // ACL
                        currentPos += 3;
                        logLevel |= ACL.value;
                    }
                    else if ( parseName( chars, currentPos, "ANY" ) )
                    {
                        // ANY
                        currentPos += 3;
                        logLevel |= ANY.value;
                    }
                    else if ( parseName( chars, currentPos, "ARGS" ) )
                    {
                        // ARGS
                        currentPos += 4;
                        logLevel |= ARGS.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 'b' :
                case 'B' :
                    // BER
                    if ( parseName( chars, currentPos, "BER" ) )
                    {
                        // BER
                        currentPos += 3;
                        logLevel |= BER.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 'c' :
                case 'C' :
                    // CONFIG or CONNS
                    if ( parseName( chars, currentPos, "CONFIG" ) )
                    {
                        // CONFIG
                        currentPos += 6;
                        logLevel |= CONFIG.value;
                    }
                    else if ( parseName( chars, currentPos, "CONNS" ) )
                    {
                        // CONNS
                        currentPos += 5;
                        logLevel |= CONNS.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 'f' :
                case 'F' :
                    // FILTER
                    if ( parseName( chars, currentPos, "FILTER" ) )
                    {
                        // FILTER
                        currentPos += 6;
                        logLevel |= FILTER.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 'n' :
                case 'N' :
                    // NONE
                    if ( parseName( chars, currentPos, "NONE" ) )
                    {
                        // NONE
                        currentPos += 4;
                        logLevel |= NONE.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 'p' :
                case 'P' :
                    // PACKETS or PARSE
                    if ( parseName( chars, currentPos, "PACKETS" ) )
                    {
                        // PACKETS
                        currentPos += 7;
                        logLevel |= PACKETS.value;
                    }
                    else if ( parseName( chars, currentPos, "PARSE" ) )
                    {
                        // PARSE
                        currentPos += 5;
                        logLevel |= PARSE.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 's' :
                case 'S' :
                    // SHELL, STATS, STATS2 or SYNC
                    if ( parseName( chars, currentPos, "SHELL" ) )
                    {
                        // SHELL
                        currentPos += 5;
                        logLevel |= SHELL.value;
                    }
                    else if ( parseName( chars, currentPos, "STATS" ) )
                    {
                        // STATS
                        currentPos += 5;
                        logLevel |= STATS.value;
                    }
                    else if ( parseName( chars, currentPos, "STATS2" ) )
                    {
                        // STATS2
                        currentPos += 6;
                        logLevel |= STATS2.value;
                    }
                    else if ( parseName( chars, currentPos, "SYNC" ) )
                    {
                        // SYNC
                        currentPos += 4;
                        logLevel |= SYNC.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case 't' :
                case 'T' :
                    // TRACE
                    if ( parseName( chars, currentPos, "TRACE" ) )
                    {
                        // TRACE
                        currentPos += 5;
                        logLevel |= TRACE.value;
                    }
                    else
                    {
                        // Wrong name
                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                    }

                    break;

                case '0' :
                    // Numeric or hexa ?
                    currentPos++;

                    if ( currentPos < chars.length )
                    {
                        if ( ( chars[currentPos] == 'x' ) || ( chars[currentPos] == 'X' ) )
                        {
                            // Hex
                            currentPos++;
                            boolean done = false;
                            int numValue = 0;

                            while ( ( currentPos < chars.length ) && !done )
                            {
                                switch ( chars[currentPos] )
                                {
                                    case '0' :
                                    case '1' :
                                    case '2' :
                                    case '3' :
                                    case '4' :
                                    case '5' :
                                    case '6' :
                                    case '7' :
                                    case '8' :
                                    case '9' :
                                        numValue = numValue*16 + chars[currentPos] - '0';
                                        currentPos++;
                                        break;

                                    case 'a' :
                                    case 'b' :
                                    case 'c' :
                                    case 'd' :
                                    case 'e' :
                                    case 'f' :
                                        numValue = numValue*16 + 10 + chars[currentPos] - 'a';
                                        currentPos++;
                                        break;

                                    case 'A' :
                                    case 'B' :
                                    case 'C' :
                                    case 'D' :
                                    case 'E' :
                                    case 'F' :
                                        numValue = numValue*16 + 10 + chars[currentPos] - 'A';
                                        currentPos++;
                                        break;

                                    case ' ' :
                                        logLevel |= numValue;
                                        done = true;
                                        break;

                                    default :
                                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                                }

                                // Special case : we are at the end of the STring
                                if ( !done )
                                {
                                    logLevel |= numValue;
                                }
                            }
                        }
                        else
                        {
                            // decimal value
                            boolean done = false;
                            int numValue = 0;

                            while ( ( currentPos < chars.length ) && !done )
                            {
                                switch ( chars[currentPos] )
                                {
                                    case '0' :
                                    case '1' :
                                    case '2' :
                                    case '3' :
                                    case '4' :
                                    case '5' :
                                    case '6' :
                                    case '7' :
                                    case '8' :
                                    case '9' :
                                        numValue = numValue*10 + chars[currentPos] - '0';
                                        currentPos++;
                                        break;

                                    case ' ' :
                                        logLevel |= numValue;
                                        done = true;
                                        break;

                                    default :
                                        throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                                }
                            }

                            // Special case : we are at the end of the STring
                            if ( !done )
                            {
                                logLevel |= numValue;
                            }
                        }
                    }

                    break;

                case '1' :
                case '2' :
                case '3' :
                case '4' :
                case '5' :
                case '6' :
                case '7' :
                case '8' :
                case '9' :
                    // Numeric
                    int numValue = chars[currentPos] - '0';

                    currentPos++;
                    boolean done = false;

                    while ( ( currentPos < chars.length ) && !done )
                    {
                        switch ( chars[currentPos] )
                        {
                            case '0' :
                            case '1' :
                            case '2' :
                            case '3' :
                            case '4' :
                            case '5' :
                            case '6' :
                            case '7' :
                            case '8' :
                            case '9' :
                                numValue = numValue*10 + chars[currentPos] - '0';
                                currentPos++;
                                break;

                            case ' ' :
                                logLevel |= numValue;
                                done = true;
                                break;

                            default :
                                throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
                        }

                    }

                    // Special case : we are at the end of the STring
                    if ( !done )
                    {
                        logLevel |= numValue;
                    }

                    break;

                default :
                    throw new IllegalArgumentException( "Wrong LogLevel at " + currentPos + " : " + logLevelString );
            }
        }

        return logLevel;
    }


    // ── METHOD: parseName — CHECKING A TOKEN AGAINST AN EXPECTED LABEL ────────
    // We compare a slice of the character array against the expected string
    // using case-insensitive matching. We return true only if every character
    // aligns — this lets the caller determine which keyword starts at the
    // current position without allocating a substring.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We verify that the characters in {@code chars} starting at {@code pos}
     * match {@code expected} case-insensitively. We return {@code true} only if
     * every character aligns; we return {@code false} on any mismatch or if the
     * array is too short.
     *
     * @param chars     the character array being scanned
     * @param pos       the starting position in {@code chars}
     * @param expected  the uppercase keyword to match against
     * @return          {@code true} if the token matches, {@code false} otherwise
     */
    private static boolean parseName( char[] chars, int pos, String expected )
    {
        for ( int current = 0; current < expected.length(); current++ )
        {
            if ( pos + current < chars.length )
            {
                char c = chars[pos+ current];
                char e = expected.charAt( current );

                if ( ( c != e ) && ( c != e + ( 'a' - 'A' ) ) )
                {
                    return false;
                }
            }
        }

        return true;
    }
}
