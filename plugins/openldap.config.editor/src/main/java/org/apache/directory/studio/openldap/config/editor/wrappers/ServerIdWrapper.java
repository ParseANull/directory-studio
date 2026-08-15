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

// ── CLASS: ServerIdWrapper — The Star Destroyer's Hull Registry Entry ─────────
// Every Star Destroyer in the Imperial fleet carries a unique hull number
// stamped on the hull — an integer between 0 and 4095 (or 0x000 to 0xFFF in
// hex) — optionally followed by a comm-relay URL.  ServerIdWrapper stores one
// such entry (the olcServerId value), parses both decimal and hex forms,
// and serializes back to "N" or "N URL".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for the olcServerId attribute value.
 * Stores a numeric server ID (0–4095, decimal or hex) and an optional URL.
 * Sorting is by server ID first, then by URL (case-insensitive).
 *
 * <pre>
 * ServerId ::= ( INT | HEX ) [ ' ' URL ]
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerIdWrapper implements Cloneable, Comparable<ServerIdWrapper>
{
    /** The server ID */
    private int serverId;

    /** The URL, if any */
    private String url;


    // ── Default Constructor — An Unnamed Hull ─────────────────────────────────
    // The shipyard registers a new hull without an ID yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of ServerIdWrapper.
     */
    public ServerIdWrapper()
    {
    }


    // ── Constructor (int) — A Hull with a Known ID ────────────────────────────
    // The hull is stamped with an integer ID on the construction line.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of ServerIdWrapper.
     *
     * @param serverId the serverID
     */
    public ServerIdWrapper( int serverId )
    {
        this.serverId = serverId;
    }


    // ── Constructor (int, String) — A Hull with ID and Comm URL ───────────────
    // The hull carries both a numeric ID and a comm-relay URL for replication.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of ServerIdWrapper.
     *
     * @param serverId the serverID
     * @param url the URL
     */
    public ServerIdWrapper( int serverId, String url )
    {
        this.serverId = serverId;
        this.url = url;
    }


    // ── parseInt — Parse Decimal or Hex Notation ──────────────────────────────
    // The shipyard accepts hull numbers in either base-10 or base-16 notation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parse a String that contains either a integer value or an hexa value
     */
    private int parseInt( String str )
    {
        if ( str.startsWith( "0x" ) || str.startsWith( "0X" ) )
        {
            return Integer.parseInt( str.substring( 2 ), 16 );
        }
        else
        {
            return Integer.parseInt( str );
        }
    }


    // ── Constructor (String) — Parsing the Hull Registry String ───────────────
    // The registry clerk reads the text form of the hull entry and splits it
    // into the numeric ID and the optional comm URL.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of ServerIdWrapper.
     *
     * @param serverIdStr the serverID
     */
    public ServerIdWrapper( String serverIdStr )
    {
        if ( !Strings.isEmpty( serverIdStr ) )
        {
            // Let's see if we have an URL, and if so, it's after the number
            int pos = serverIdStr.indexOf( ' ' );

            if ( pos == -1 )
            {
                // No URL
                serverId = parseInt( serverIdStr );
            }
            else
            {
                // ServerID and URL
                this.serverId = parseInt( serverIdStr.substring( 0, pos ) );
                this.url = serverIdStr.substring( pos + 1 );
            }
        }
    }


    // ── getServerId — Read the Hull Number ────────────────────────────────────
    /**
     * @return the serverId
     */
    public int getServerId()
    {
        return serverId;
    }


    // ── setServerId — Update the Hull Number ──────────────────────────────────
    /**
     * @param serverId the serverId to set
     */
    public void setServerId( int serverId )
    {
        this.serverId = serverId;
    }


    // ── getUrl — Read the Comm Relay URL ──────────────────────────────────────
    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }


    // ── setUrl — Update the Comm Relay URL ────────────────────────────────────
    /**
     * @param url the url to set
     */
    public void setUrl( String url )
    {
        this.url = url;
    }


    // ── clone — Duplicate the Registry Entry ──────────────────────────────────
    /**
     * Clone the current object
     */
    public ServerIdWrapper clone()
    {
        try
        {
            return (ServerIdWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── equals — Check If Two Entries Describe the Same Hull ──────────────────
    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object that )
    {
        // Quick test
        if ( this == that )
        {
            return true;
        }

        if ( that instanceof ServerIdWrapper )
        {
            ServerIdWrapper thatInstance = (ServerIdWrapper)that;

            if ( serverId != thatInstance.serverId )
            {
                return false;
            }

            if ( url == thatInstance.url )
            {
                return true;
            }

            if ( url != null )
            {
                return url.equals( thatInstance.url );
            }
            else
            {
                return thatInstance.url == null;
            }
        }
        else
        {
            return false;
        }
    }


    // ── hashCode — Compute a Hash from ID and URL ──────────────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        h += h*17 + serverId;

        if ( url != null )
        {
            h += h*17 + url.hashCode();
        }

        return h;
    }


    // ── compareTo — Sort Entries by ID Then URL ────────────────────────────────
    // The registry is kept sorted by hull number first, then by comm-relay URL.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( ServerIdWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        // Check the serverId first
        if ( serverId == that.serverId )
        {
            // Now, compare the url
            if ( Strings.isEmpty( url ) )
            {
                return 0;
            }
            else
            {
                return url.compareToIgnoreCase( that.url );
            }
        }
        else
        {
            return serverId - that.serverId;
        }
    }


    // ── toString — Serialize the Registry Entry ────────────────────────────────
    // The registry clerk writes out the entry: "N" or "N URL".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        if ( url == null )
        {
            return Integer.toString( serverId );
        }
        else
        {
            return Integer.toString( serverId ) + " " + url;
        }
    }
}
