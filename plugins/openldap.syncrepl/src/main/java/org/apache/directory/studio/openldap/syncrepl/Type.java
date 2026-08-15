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


// ── CLASS: Type — The Replication Rhythm: Poll vs. Persistent Channel ─────────
// A sector command can receive intelligence from Imperial HQ in two ways.
// "refreshOnly" is like dispatching a courier: the sector command contacts HQ
// at a fixed interval, gets the latest updates, then disconnects.  It relies
// on a scheduled poll (the Interval setting).
// "refreshAndPersist" is like leaving a permanent HoloNet channel open: HQ
// pushes changes to the sector command in real time as they happen — no need
// to poll.
// This enum models those two replication modes — the syncrepl "type" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code type} parameter.
 * The type determines how the consumer keeps its data in sync with the provider.
 * {@link #REFRESH_ONLY} polls periodically; {@link #REFRESH_AND_PERSIST} keeps
 * a persistent connection open so changes arrive in real time.
 * Think of this as choosing between a scheduled intelligence courier
 * (refreshOnly) and a permanent open HoloNet channel (refreshAndPersist).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum Type
{
    /** The 'refreshOnly' type value */
    REFRESH_ONLY("refreshOnly"),

    /** The 'refreshAndPersist' type value */
    REFRESH_AND_PERSIST("refreshAndPersist");

    /** The value */
    private String value;


    // ── Parse the Replication Rhythm Token ───────────────────────────────────
    // The parser matches "refreshOnly" or "refreshAndPersist" (case-insensitive)
    // and returns the correct constant.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a replication type string into the corresponding enum constant.
     *
     * @param s  the type string — {@code "refreshOnly"} or {@code "refreshAndPersist"}
     *           (case-insensitive).
     * @return   the matching {@link Type} constant.
     * @throws ParseException  if {@code s} is not a recognised replication type.
     */
    public static Type parse( String s ) throws ParseException
    {
        // REFRESH_ONLY
        if ( REFRESH_ONLY.value.equalsIgnoreCase( s ) )
        {
            return REFRESH_ONLY;
        }
        // REFRESH_AND_PERSIST
        else if ( REFRESH_AND_PERSIST.value.equalsIgnoreCase( s ) )
        {
            return REFRESH_AND_PERSIST;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid type.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its camelCase token exactly as OpenLDAP expects it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Type constant with its config directive token.
     *
     * @param value  the camelCase token — {@code "refreshOnly"} or {@code "refreshAndPersist"}.
     */
    private Type( String value )
    {
        this.value = value;
    }


    // ── Write the Replication Rhythm Back into the Configuration ─────────────
    // Returns the exact camelCase token OpenLDAP expects in the type= parameter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this replication type.
     *
     * @return  {@code "refreshOnly"} or {@code "refreshAndPersist"}.
     */
    public String toString()
    {
        return value;
    }
}
