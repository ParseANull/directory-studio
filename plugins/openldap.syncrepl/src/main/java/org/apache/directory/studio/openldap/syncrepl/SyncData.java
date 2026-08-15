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


// ── CLASS: SyncData — Which Imperial Archive to Sync From ─────────────────────
// Imperial HQ can source its intelligence packages from three different filing
// systems: the live directory itself ("default"), the access log database
// (a structured audit trail recorded by the accesslog overlay — "accesslog"),
// or a legacy changelog ("changelog" — an older format still used in some
// Imperial outposts).
// This enum models those three data sources for the syncrepl "syncdata" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code syncdata} parameter.
 * Controls which data source the provider uses to generate the replication stream.
 * {@link #DEFAULT} uses the directory's built-in synchronisation mechanism.
 * {@link #ACCESSLOG} uses the accesslog overlay — useful for delta sync.
 * {@link #CHANGELOG} uses the older changelog format.
 * Think of this as the sector command specifying which Imperial filing system
 * its intelligence comes from.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SyncData
{
    /** The 'default' Sync Data value */
    DEFAULT("default"),

    /** The 'accesslog' Sync Data value */
    ACCESSLOG("accesslog"),

    /** The 'changelog' Sync Data value */
    CHANGELOG("changelog");

    /** The value */
    private String value;


    // ── Select the Right Imperial Archive ────────────────────────────────────
    // The parser matches the incoming string against all three archive names
    // and returns the matching constant, or throws for anything unrecognised.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a sync data string into the corresponding enum constant.
     *
     * @param s  the string to parse — one of {@code "default"}, {@code "accesslog"},
     *           or {@code "changelog"} (case-insensitive).
     * @return   the matching {@link SyncData} constant.
     * @throws ParseException  if {@code s} is not a recognised sync data value.
     */
    public static SyncData parse( String s ) throws ParseException
    {
        // DEFAULT
        if ( DEFAULT.value.equalsIgnoreCase( s ) )
        {
            return DEFAULT;
        }
        // ACCESSLOG
        else if ( ACCESSLOG.value.equalsIgnoreCase( s ) )
        {
            return ACCESSLOG;
        }
        // CHANGELOG
        else if ( CHANGELOG.value.equalsIgnoreCase( s ) )
        {
            return CHANGELOG;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid sync data.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its lowercase token for the configuration file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SyncData constant with its config directive token.
     *
     * @param value  the lowercase token as it appears in the syncrepl directive.
     */
    private SyncData( String value )
    {
        this.value = value;
    }


    // ── Write the Archive Source Back into the Configuration ──────────────────
    // Returns the exact token OpenLDAP expects in the syncdata= parameter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this sync data source.
     *
     * @return  one of {@code "default"}, {@code "accesslog"}, {@code "changelog"}.
     */
    public String toString()
    {
        return value;
    }
}
