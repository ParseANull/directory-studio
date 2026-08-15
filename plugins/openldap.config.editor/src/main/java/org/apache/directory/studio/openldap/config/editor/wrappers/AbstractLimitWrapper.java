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

// ── CLASS: AbstractLimitWrapper — The Imperial Academy's Shared Discipline ────
// Every Imperial officer learns the same core skills at the Academy before they
// specialize: navigation, tactics, protocol.  AbstractLimitWrapper teaches those
// shared skills to both TimeLimitWrapper and SizeLimitWrapper.  It holds the
// three limit slots (global, hard, soft), provides the parsing and serialization
// logic they share, and implements the Comparable contract so limits can be
// ordered consistently.  Subclasses extend it only for their specific parsing
// and type-name details.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A shared base class for {@link TimeLimitWrapper} and {@link SizeLimitWrapper}.
 * It stores the three OpenLDAP limit tiers (global, hard, soft) and handles
 * their serialization to an OpenLDAP configuration string and comparison.
 * Think of it as the Imperial Academy curriculum — both wrapper types learn the
 * same core discipline here before they specialize.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractLimitWrapper implements LimitWrapper, Comparable<LimitWrapper>
{
    /** The global limit */
    protected Integer globalLimit;

    /** The soft limit */
    protected Integer softLimit;

    /** The hard limit */
    protected Integer hardLimit;

    /** A flag that tells if the Limit is valid */
    protected boolean isValid = true;

    /** The length of the parsed String, if any */
    protected int parsedLength = 0;


    // ── Default Constructor — An Empty Cadet Record ───────────────────────────
    // A new cadet is registered at the Academy with no performance records yet
    // — all limit fields start null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a AbstractLimitWrapper instance
     */
    public AbstractLimitWrapper()
    {
    }


    // ── Full Constructor — A Seasoned Officer With Known Limits ───────────────
    // An officer arrives with their performance records already on file: a global
    // cap, a hard ceiling, and a soft advisory threshold.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create an AbstractLimitWrapper instance
     *
     * @param globalLimit The global limit
     * @param hardLimit The hard limit
     * @param softLimit The soft limit
     */
    public AbstractLimitWrapper( Integer globalLimit, Integer hardLimit, Integer softLimit )
    {
        this.globalLimit = globalLimit;
        this.hardLimit = hardLimit;
        this.softLimit = softLimit;
    }


    // ── clear — The Academy Wipes an Officer's Records ────────────────────────
    // When a review board clears an officer's file, all their limit records are
    // erased and reset to null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clear the TimeLimitWrapper (reset all the values to null)
     */
    public void clear()
    {
        globalLimit = null;
        softLimit = null;
        hardLimit = null;
    }


    // ── getInteger — Parse a Digit Sequence from a Config String ──────────────
    // The Academy's records are written in a compact notation — this helper
    // method extracts a digit sequence starting at a given position, returning
    // null if there are no digits at all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get an integer out of a String. Return null if we don't find any.
     */
    protected static String getInteger( String str, int pos )
    {
        for ( int i = pos; i < str.length(); i++ )
        {
            char c = str.charAt( i );

            if ( ( c < '0') && ( c > '9' ) )
            {
                if ( i == pos )
                {
                    return null;
                }
                else
                {
                    return str.substring( pos, i );
                }
            }
        }

        return str.substring( pos );
    }


    // ── getGlobalLimit — Read the Overall Performance Cap ─────────────────────
    // Return the global limit value (the single cap that overrides both hard
    // and soft limits when set).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return the globalLimit
     */
    public Integer getGlobalLimit()
    {
        return globalLimit;
    }


    // ── setGlobalLimit — Set the Overall Performance Cap ──────────────────────
    // Update the global limit value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @param globalLimit the globalLimit to set
     */
    public void setGlobalLimit( Integer globalLimit )
    {
        this.globalLimit = globalLimit;
    }


    // ── getSoftLimit — Read the Advisory Threshold ────────────────────────────
    // Return the soft limit — the advisory threshold that can be exceeded by
    // privileged users.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return the softLimit
     */
    public Integer getSoftLimit()
    {
        return softLimit;
    }


    // ── setSoftLimit — Set the Advisory Threshold ─────────────────────────────
    // Update the soft limit value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @param softLimit the softLimit to set
     */
    public void setSoftLimit( Integer softLimit )
    {
        this.softLimit = softLimit;
    }


    // ── getHardLimit — Read the Absolute Ceiling ──────────────────────────────
    // Return the hard limit — the absolute ceiling that no user can exceed,
    // not even the privileged ones.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return the hardLimit
     */
    public Integer getHardLimit()
    {
        return hardLimit;
    }


    // ── setHardLimit — Set the Absolute Ceiling ───────────────────────────────
    // Update the hard limit value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @param hardLimit the hardLimit to set
     */
    public void setHardLimit( Integer hardLimit )
    {
        this.hardLimit = hardLimit;
    }


    // ── toString — Serialize the Limit to an OpenLDAP Config String ───────────
    // The Academy's records officer translates the officer's limit tiers into
    // the compact notation used in slapd.conf: global=N, .hard=N, .soft=N,
    // or combinations thereof, following the precedence rules.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        String limitType = getType();

        if ( globalLimit != null )
        {
            // The globalLimit overrides the soft and hard limit
            sb.append( limitType );

            if ( globalLimit.intValue() >= 0 )
            {
                sb.append( "=" ).append( globalLimit );
            }
            else if ( globalLimit.equals( UNLIMITED ) )
            {
                sb.append( "=" ).append( UNLIMITED_STR );
            }
            else if ( globalLimit.equals( HARD_SOFT ) )
            {
                sb.append( ".hard=" ).append( SOFT_STR );
            }
        }
        else
        {
            if ( hardLimit != null )
            {
                // First check the hard limit, has it can be set to be equal to soft limit
                if ( softLimit != null )
                {
                    if ( hardLimit.equals( softLimit ) )
                    {
                        // If hard and soft are set and equals, we use the global limit instead
                        sb.append( limitType ).append( "=" );

                        if ( hardLimit.equals( UNLIMITED ) )
                        {
                            sb.append( UNLIMITED_STR );
                        }
                        else if ( hardLimit.intValue() >= 0 )
                        {
                            sb.append( hardLimit );
                        }
                    }
                    else
                    {
                        // We have both values, the aren't equal.
                        if ( hardLimit.equals( UNLIMITED ) )
                        {
                            sb.append( limitType ).append( ".hard=unlimited " );
                            sb.append( limitType ).append( ".soft=" );
                            sb.append( softLimit );
                        }
                        else if ( hardLimit.intValue() == 0 )
                        {
                            // Special cases : hard = soft
                            sb.append( limitType ).append( "=" ).append( softLimit );
                        }
                        else if ( hardLimit.intValue() < softLimit.intValue() )
                        {
                            // when the hard limit is lower than the soft limit : use the hard limit
                            sb.append( limitType ).append( "=" ).append( hardLimit );
                        }
                        else
                        {
                            // Special case : softLimit is -1
                            if ( softLimit.equals( UNLIMITED ) )
                            {
                                // We use the hard limit
                                sb.append( limitType ).append( "=" ).append( hardLimit );
                            }
                            else
                            {
                                sb.append( limitType ).append( ".hard=" );

                                if ( hardLimit.equals( UNLIMITED ) )
                                {
                                    sb.append( UNLIMITED_STR );
                                }
                                else if ( hardLimit.intValue() > 0 )
                                {
                                    sb.append( hardLimit );
                                }

                                sb.append( ' ' ).append( limitType ).append( ".soft=" );

                                if ( softLimit.equals( UNLIMITED ) )
                                {
                                    sb.append( UNLIMITED_STR );
                                }
                                else if ( softLimit.intValue() >= 0 )
                                {
                                    sb.append( softLimit );
                                }
                            }
                        }
                    }
                }
                else
                {
                    // Only an hard limit
                    sb.append( limitType ).append( ".hard=" );

                    if ( hardLimit.equals( UNLIMITED ) )
                    {
                        sb.append( UNLIMITED_STR );
                    }
                    else if ( hardLimit.intValue() >= 0 )
                    {
                        sb.append( hardLimit );
                    }
                }
            }
            else if ( softLimit != null )
            {
                // Only a soft limit
                sb.append( limitType ).append( ".soft=" );

                if ( softLimit.equals( UNLIMITED ) )
                {
                    sb.append( UNLIMITED_STR );
                }
                else if ( softLimit.intValue() >= 0 )
                {
                    sb.append( softLimit );
                }
            }
        }

        return sb.toString();
    }


    // ── compareTo — Compare Two Limit Wrappers by String Representation ───────
    // The Academy ranks officers by their serialized limit string, which gives
    // a consistent, deterministic ordering.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( LimitWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        return toString().compareTo( that.toString() );
    }


    // ── isValid — Check If the Limit Configuration Is Coherent ────────────────
    // The Academy's review board checks whether the officer's recorded limits
    // make sense — a wrapper is invalid if its parsing failed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells if the TimeLimit element is valid or not
     * @return true if the values are correct, false otherwise
     */
    public boolean isValid()
    {
        return isValid;
    }
}
