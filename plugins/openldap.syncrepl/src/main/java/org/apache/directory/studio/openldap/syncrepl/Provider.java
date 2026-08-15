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

import org.apache.directory.api.util.Strings;


// ── CLASS: Provider — The Address of Imperial HQ (the Master Server) ─────────
// Each sector command must know the HoloNet address of Imperial Intelligence HQ
// in order to dial in and receive the intelligence sync.  The address is either
// an unencrypted "ldap://hostname:port" link or an encrypted "ldaps://hostname:port"
// channel — the sector command picks the secure channel for sensitive data.
// Provider models that address: protocol (ldap vs ldaps), hostname, and optional
// port number.  It is the syncrepl "provider" parameter value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Models the syncrepl {@code provider} parameter — the URL of the master LDAP server.
 * Format: {@code "ldap[s]://<hostname>[:port]"}.
 * When no port is specified we store {@link #NO_PORT} ({@code -1}) so that
 * {@link #toString()} omits the port, letting OpenLDAP use its default
 * (389 for LDAP, 636 for LDAPS).
 * Think of this as the HoloNet address of Imperial Intelligence HQ that the
 * sector command dials when it wants an intelligence sync.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Provider
{
    /** Constant used as port value when no port is provided */
    public static final int NO_PORT = -1;

    /** The pattern used for parsing */
    private static final Pattern pattern = Pattern
        .compile( "^[l|L][d|D][a|A][p|P]([s|S]?)://([^:]+)([:]([0-9]{1,5}))?$" );

    /** The LDAPS flag */
    private boolean isLdaps;

    /** The host */
    private String host;

    /** The port */
    private int port = NO_PORT;


    // ── Blank Provider — Fill in the Address Later ────────────────────────────
    // A provider with no address yet; the caller populates host, port, and
    // the ldaps flag via setters or via the parse() method.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty provider.
     * Use {@link #parse(String)} or the setters to populate it.
     */
    public Provider()
    {
        // TODO Auto-generated constructor stub
    }


    // ── Full Address Known at Creation Time ───────────────────────────────────
    // The sector command is given all three components of the HQ address at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a provider with all three address components.
     *
     * @param isLdaps  {@code true} for LDAPS (encrypted), {@code false} for plain LDAP.
     * @param host     the hostname or IP address of the provider server.
     * @param port     the port number, or {@link #NO_PORT} to use the default.
     */
    public Provider( boolean isLdaps, String host, int port )
    {
        this.isLdaps = isLdaps;
        this.host = host;
        this.port = port;
    }


    // ── Copy the HQ Address for Another Consumer ──────────────────────────────
    // Multiple consumers can share the same provider URL; we deep-copy so each
    // consumer's Provider object is independent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given provider, or {@code null} if the input is {@code null}.
     *
     * @param provider  the provider to copy.
     * @return          a new {@link Provider} with the same host, port, and LDAPS flag.
     */
    public static Provider copy( Provider provider )
    {
        if ( provider != null )
        {
            Provider providerCopy = new Provider();

            providerCopy.setHost( provider.getHost() );
            providerCopy.setPort( provider.getPort() );
            providerCopy.setLdaps( provider.isLdaps() );

            return providerCopy;
        }

        return null;
    }


    // ── Copy This Provider ────────────────────────────────────────────────────
    // Convenience instance method delegating to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this provider.
     *
     * @return  a new {@link Provider} with the same values.
     */
    public Provider copy()
    {
        return Provider.copy( this );
    }


    // ── Decode the HQ Address from the Configuration String ───────────────────
    // The configuration file contains "ldap://ldap.example.com:389" or similar —
    // we match with a regex, extract the optional 's' flag for LDAPS, the host,
    // and the optional port, then populate the Provider object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a provider URL string into a {@link Provider} object.
     * Accepts both {@code ldap://} and {@code ldaps://} (case-insensitive).
     * The port component is optional; if absent, {@link #NO_PORT} is stored.
     *
     * @param s  the provider URL string, e.g. {@code "ldap://ldap.example.com:389"}.
     * @return   the parsed {@link Provider}.
     * @throws ParseException  if the string doesn't match the expected URL format
     *                         or if the port can't be parsed as an integer.
     */
    public static Provider parse( String s ) throws ParseException
    {
        // Creating the provider
        Provider provider = new Provider();

        // Matching the string
        Matcher matcher = pattern.matcher( s );

        // Checking the result
        if ( matcher.find() )
        {
            // LDAPS
            provider.setLdaps( "s".equalsIgnoreCase( matcher.group( 1 ) ) );

            // Host
            String host = matcher.group( 2 );

            if ( !Strings.isEmpty( host ) )
            {
                provider.setHost( host );
            }

            // Port
            String port = matcher.group( 4 );

            if ( !Strings.isEmpty( port ) )
            {
                try
                {
                    provider.setPort( Integer.parseInt( port ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new ParseException( "Unable to convert port value '" + port + "' as an integer.", 0 );
                }
            }
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid provider.", 0 );
        }

        return provider;
    }


    /**
     * Returns {@code true} if this provider uses LDAPS (encrypted channel).
     *
     * @return  {@code true} for LDAPS, {@code false} for plain LDAP.
     */
    public boolean isLdaps()
    {
        return isLdaps;
    }


    /**
     * Returns the hostname of the provider server.
     *
     * @return  the hostname or IP address.
     */
    public String getHost()
    {
        return host;
    }


    /**
     * Returns the port number, or {@link #NO_PORT} if no port was specified.
     *
     * @return  the port number, or -1 if defaulting.
     */
    public int getPort()
    {
        return port;
    }


    /**
     * Sets whether to use LDAPS.
     *
     * @param isLdaps  {@code true} for encrypted LDAPS connection.
     */
    public void setLdaps( boolean isLdaps )
    {
        this.isLdaps = isLdaps;
    }


    /**
     * Sets the hostname of the provider server.
     *
     * @param host  the hostname or IP address.
     */
    public void setHost( String host )
    {
        this.host = host;
    }


    /**
     * Sets the port number.  Pass {@link #NO_PORT} to omit the port from the URL.
     *
     * @param port  the port number, or {@link #NO_PORT}.
     */
    public void setPort( int port )
    {
        this.port = port;
    }


    // ── Write the HQ Address Back into the Configuration ──────────────────────
    // We reconstruct the "ldap[s]://host[:port]" URL from the stored components
    // for writing back into the OpenLDAP syncrepl directive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the provider URL as a string for use in the OpenLDAP syncrepl directive.
     * Omits the port if it is {@link #NO_PORT}.
     *
     * @return  the URL string, e.g. {@code "ldaps://ldap.example.com:636"}.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "ldap" );

        if ( isLdaps )
        {
            sb.append( "s" );
        }

        sb.append( "://" );
        sb.append( host );

        if ( port != NO_PORT )
        {
            sb.append( ":" );
            sb.append( port );
        }

        return sb.toString();
    }
}
