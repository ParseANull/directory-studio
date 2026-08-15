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

package org.apache.directory.studio.connection.core;


import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;


// ── CLASS: Utils — C-3PO'S GALAXY-WIDE TRANSLATION AND FORMATTING TOOLKIT ────
// C-3PO is the crew's universal translator and protocol expert — he converts
// alien codes to plain text, trims communications to fit transmission windows,
// and can format a search request as a command any operator understands.
// This class is C-3PO's toolkit: OID-to-text translation, result code decoding,
// string truncation, filename sanitization, LDAP URL assembly, and ldapsearch
// command-line generation — all the utility methods that don't belong elsewhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * General-purpose utility methods for the connection.core plugin.
 * We cover four broad areas:
 * <ol>
 *   <li>OID lookup — translate numeric OIDs to human-readable names</li>
 *   <li>Result code lookup — translate numeric LDAP result codes to descriptions</li>
 *   <li>String manipulation — truncate and sanitize strings for display and filenames</li>
 *   <li>URL and command-line generation — build LDAP URLs and ldapsearch invocations
 *       from search parameters</li>
 * </ol>
 * Think of this class as C-3PO's universal toolkit: he translates codes, trims
 * communications, and formats requests for any audience.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Utils
{

    /** The ellipsis inserted when a string is truncated. */
    private static final String DOT_DOT_DOT = "..."; //$NON-NLS-1$

    // ── OID DESCRIPTIONS — C-3PO'S ALIEN CODE DICTIONARY ─────────────────────────
    // C-3PO keeps a dictionary mapping alien code sequences (OIDs) to their
    // plain-language meanings.  We load it once at class-init time.
    // ────────────────────────────────────────────────────────────────────────────────
    /** Resource bundle mapping OID strings to human-readable descriptions. */
    public static ResourceBundle oidDescriptions = null;
    // Load RessourceBundle with OID descriptions
    static
    {
        try
        {
            oidDescriptions = ResourceBundle.getBundle( "org.apache.directory.studio.connection.core.OIDDescriptions" ); //$NON-NLS-1$
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    // ── RESULT CODE DESCRIPTIONS — C-3PO'S STATUS CODE GLOSSARY ──────────────────
    // C-3PO also keeps a glossary of status codes — what numeric codes returned
    // by the server actually mean in plain language.
    // ────────────────────────────────────────────────────────────────────────────────
    /** Resource bundle mapping LDAP result code integers to human-readable descriptions. */
    public static ResourceBundle resultCodeDescriptions = null;
    // Load RessourceBundle with result code descriptions
    static
    {
        try
        {
            resultCodeDescriptions = ResourceBundle
                .getBundle( "org.apache.directory.studio.connection.core.ResultCodeDescriptions" ); //$NON-NLS-1$
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    // ── GET OID DESCRIPTION — TRANSLATE A NUMERIC OID ─────────────────────────────
    // C-3PO looks up the alien code sequence and returns its plain-language meaning.
    // Returns null if the OID isn't in his dictionary.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a human-readable description for the given LDAP OID string.
     * We search the bundled OIDDescriptions resource bundle.
     * Returns {@code null} if the OID is not recognized.
     *
     * @param oid  The numeric OID string (e.g. {@code "1.2.840.113556.1.4.319"}).
     * @return  The human-readable description, or {@code null} if not found.
     */
    public static String getOidDescription( String oid )
    {
        if ( oidDescriptions != null )
        {
            try
            {
                return oidDescriptions.getString( oid );
            }
            catch ( MissingResourceException ignored )
            {
            }
        }

        return null;
    }


    // ── GET RESULT CODE DESCRIPTION — TRANSLATE A STATUS CODE ─────────────────────
    // C-3PO looks up a status code in his glossary and returns the plain-language
    // meaning.  Returns null if the code isn't in his glossary.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a human-readable description for the given LDAP result code.
     * We search the bundled ResultCodeDescriptions resource bundle.
     * Returns {@code null} if the code is not recognized.
     *
     * @param code  The numeric LDAP result code (e.g. {@code 49} for invalidCredentials).
     * @return  The human-readable description, or {@code null} if not found.
     */
    public static String getResultCodeDescription( int code )
    {
        if ( resultCodeDescriptions != null )
        {
            try
            {
                return resultCodeDescriptions.getString( "" + code ); //$NON-NLS-1$
            }
            catch ( MissingResourceException ignored )
            {
            }
        }
        return null;
    }


    // ── SHORTEN — TRIM A LABEL TO FIT IN A TRANSMISSION WINDOW ───────────────────
    // C-3PO clips long diplomatic messages to fit the transmission buffer: he keeps
    // the first half and the last half, joined by "...", and replaces any control
    // characters with "." so nothing garbles the display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Shortens {@code label} to at most {@code maxLength} characters, inserting an
     * ellipsis in the middle, and replaces ISO control characters with {@code '.'}.
     * If {@code maxLength < 3}, returns {@code "..."} immediately.
     * If the label is already short enough, only control-character filtering is applied.
     *
     * @param label      The string to shorten; {@code null} returns {@code null}.
     * @param maxLength  Maximum length of the result (including the ellipsis).
     * @return  The shortened and filtered string.
     */
    public static String shorten( String label, int maxLength )
    {
        if ( label == null )
        {
            return null;
        }

        // shorten label
        if ( maxLength < 3 )
        {
            return DOT_DOT_DOT;
        }
        if ( label.length() > maxLength )
        {
            label = label.substring( 0, maxLength / 2 ) + DOT_DOT_DOT
                + label.substring( label.length() - maxLength / 2, label.length() );

        }

        // filter non-printable characters
        StringBuffer sb = new StringBuffer( maxLength + 3 );
        for ( int i = 0; i < label.length(); i++ )
        {
            char c = label.charAt( i );
            if ( Character.isISOControl( c ) )
            {
                sb.append( '.' );
            }
            else
            {
                sb.append( c );
            }
        }

        return sb.toString();
    }


    // ── GET FILENAME STRING — SANITIZE A STRING FOR USE AS A FILENAME ─────────────
    // C-3PO converts any string (which might contain spaces, slashes, or unicode)
    // into a filesystem-safe string by hex-encoding non-alphanumeric bytes.
    // Safe characters: 0-9, A-Z, a-z, '-', '_'.
    // Everything else becomes a two-character hex code.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts an arbitrary string into a filesystem-safe filename string.
     * We UTF-8 encode the input, then allow only {@code [0-9A-Za-z-_]} bytes through
     * as-is; all other bytes are hex-encoded (e.g. a space becomes {@code "20"}).
     * Returns {@code null} if the input is {@code null}.
     *
     * @param s  The string to convert.
     * @return  The filesystem-safe string, or {@code null} if {@code s} is {@code null}.
     */
    public static String getFilenameString( String s )
    {
        if ( s == null )
        {
            return null;
        }

        byte[] b = Strings.getBytesUtf8( s );
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < b.length; i++ )
        {

            if ( b[i] == '-' || b[i] == '_' || ( '0' <= b[i] && b[i] <= '9' ) || ( 'A' <= b[i] && b[i] <= 'Z' )
                || ( 'a' <= b[i] && b[i] <= 'z' ) )
            {
                sb.append( ( char ) b[i] );
            }
            else
            {
                int x = ( int ) b[i];
                if ( x < 0 )
                    x = 256 + x;
                String t = Integer.toHexString( x );
                if ( t.length() == 1 )
                    t = '0' + t; //$NON-NLS-1$
                sb.append( t );
            }
        }

        return sb.toString();
    }


    // ── GET LDAP URL — ASSEMBLE A SEARCH AS AN RFC 4516 URL ───────────────────────
    // C-3PO formats the search parameters as an LDAP URL — the universal
    // machine-readable notation for "run this search against this server."
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link LdapUrl} from a set of search parameters and a connection.
     * The URL includes the scheme (ldap/ldaps), host, port, base DN, attribute list,
     * scope, and filter.
     * We swallow {@link LdapInvalidDnException} silently — if the base DN is invalid,
     * the URL just won't include a DN component.
     *
     * @param connection   The connection providing host, port, and encryption method.
     * @param searchBase   The base DN string for the search.
     * @param scope        The search scope ({@link SearchControls#SUBTREE_SCOPE} etc.).
     * @param filter       The LDAP filter string.
     * @param attributes   Requested attribute names, or {@code null} for all attributes.
     * @return  The assembled {@link LdapUrl}.
     */
    public static LdapUrl getLdapURL( Connection connection, String searchBase, int scope, String filter,
        String[] attributes )
    {
        LdapUrl url = new LdapUrl();
        url.setScheme( connection.getEncryptionMethod() == EncryptionMethod.LDAPS ? LdapUrl.LDAPS_SCHEME
            : LdapUrl.LDAP_SCHEME );
        url.setHost( connection.getHost() );
        url.setPort( connection.getPort() );
        try
        {
            url.setDn( new Dn( searchBase ) );
        }
        catch ( LdapInvalidDnException e )
        {
        }
        if ( attributes != null )
        {
            url.setAttributes( Arrays.asList( attributes ) );
        }
        url.setScope( scope );
        url.setFilter( filter );
        return url;
    }


    // ── GET SIMPLE NORMALIZED URL — HOST:PORT ONLY ────────────────────────────────
    // C-3PO strips an LDAP URL down to just the essential routing coordinates:
    // scheme + host + port.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the scheme, lower-cased host, and port from an {@link LdapUrl},
     * e.g. {@code "ldap://dc.example.com:389"}.
     * We use this as a stable key for caching or deduplication.
     *
     * @param url  The LDAP URL to normalize.
     * @return  The normalized scheme+host+port string.
     */
    public static String getSimpleNormalizedUrl( LdapUrl url )
    {
        return url.getScheme()
            + ( url.getHost() != null ? Strings.toLowerCase( url.getHost() ) : "" ) + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── GET LDAP SEARCH COMMAND LINE — BUILD AN ldapsearch INVOCATION ─────────────
    // C-3PO translates the search parameters into the exact ldapsearch command
    // a system operator would type at the terminal — useful for logging and for
    // the "copy as ldapsearch" feature in the UI.
    // We include the host URL, TLS flag, auth method, base DN, scope, alias
    // dereferencing, size limit, time limit, filter, and attribute list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@code ldapsearch} command-line string from the given search parameters.
     * The resulting string can be pasted into a terminal to reproduce the search
     * using the system {@code ldapsearch} utility.
     * <p>Authentication is translated to the appropriate {@code ldapsearch} flags:
     * {@code -x -D "..." -W} for simple auth; {@code -U "..." -Y MECH} for SASL.</p>
     *
     * @param connection                   The connection providing host, port, encryption, and auth.
     * @param searchBase                   The base DN for the search.
     * @param scope                        The search scope (sub/one/base).
     * @param aliasesDereferencingMethod   How aliases should be handled.
     * @param sizeLimit                    Maximum entries to return; 0 for server default.
     * @param timeLimit                    Maximum time in seconds; 0 for server default.
     * @param filter                       The LDAP filter string.
     * @param attributes                   Attribute names to return, or {@code null} for all.
     * @return  A complete {@code ldapsearch} command-line string.
     */
    public static String getLdapSearchCommandLine( Connection connection, String searchBase, int scope,
        AliasDereferencingMethod aliasesDereferencingMethod, long sizeLimit, long timeLimit, String filter,
        String[] attributes )
    {
        StringBuilder cmdLine = new StringBuilder();

        cmdLine.append( "ldapsearch" ); //$NON-NLS-1$

        cmdLine.append( " -H " ).append( //$NON-NLS-1$
            connection.getEncryptionMethod() == EncryptionMethod.LDAPS ? LdapUrl.LDAPS_SCHEME : LdapUrl.LDAP_SCHEME )
            .append( connection.getHost() ).append( ":" ).append( connection.getPort() ); //$NON-NLS-1$

        if ( connection.getEncryptionMethod() == EncryptionMethod.START_TLS )
        {
            cmdLine.append( " -ZZ" ); //$NON-NLS-1$
        }

        switch ( connection.getAuthMethod() )
        {
            case SIMPLE:
                cmdLine.append( " -x" ); //$NON-NLS-1$
                cmdLine.append( " -D \"" ).append( connection.getBindPrincipal() ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
                cmdLine.append( " -W" ); //$NON-NLS-1$
                break;
            case SASL_CRAM_MD5:
                cmdLine.append( " -U \"" ).append( connection.getBindPrincipal() ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
                cmdLine.append( " -Y \"CRAM-MD5\"" ); //$NON-NLS-1$
                break;
            case SASL_DIGEST_MD5:
                cmdLine.append( " -U \"" ).append( connection.getBindPrincipal() ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
                cmdLine.append( " -Y \"DIGEST-MD5\"" ); //$NON-NLS-1$
                break;
            case SASL_GSSAPI:
                cmdLine.append( " -Y \"GSSAPI\"" ); //$NON-NLS-1$
                break;
        }

        cmdLine.append( " -b \"" ).append( searchBase ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$

        String scopeAsString = scope == SearchControls.SUBTREE_SCOPE ? "sub" //$NON-NLS-1$
            : scope == SearchControls.ONELEVEL_SCOPE ? "one" : "base"; //$NON-NLS-1$ //$NON-NLS-2$
        cmdLine.append( " -s " ).append( scopeAsString ); //$NON-NLS-1$

        if ( aliasesDereferencingMethod != AliasDereferencingMethod.NEVER )
        {
            String aliasAsString = aliasesDereferencingMethod == AliasDereferencingMethod.ALWAYS ? "always" //$NON-NLS-1$
                : aliasesDereferencingMethod == AliasDereferencingMethod.FINDING ? "find" //$NON-NLS-1$
                    : aliasesDereferencingMethod == AliasDereferencingMethod.SEARCH ? "search" : "never"; //$NON-NLS-1$ //$NON-NLS-2$
            cmdLine.append( " -a " ).append( aliasAsString ); //$NON-NLS-1$
        }

        if ( sizeLimit > 0 )
        {
            cmdLine.append( " -z " ).append( sizeLimit ); //$NON-NLS-1$
        }
        if ( timeLimit > 0 )
        {
            cmdLine.append( " -l " ).append( timeLimit ); //$NON-NLS-1$
        }

        cmdLine.append( " \"" ).append( filter ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$

        if ( attributes != null )
        {
            if ( attributes.length == 0 )
            {
                cmdLine.append( " \"1.1\"" ); //$NON-NLS-1$
            }
            for ( String attribute : attributes )
            {
                cmdLine.append( " \"" ).append( attribute ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return cmdLine.toString();
    }


    // ── GET LDAP DN — PARSE A STRING TO A DN OR RETURN NULL ───────────────────────
    // C-3PO tries to parse a coordinate string (DN) into a structured object.
    // If it's invalid syntax, he quietly returns null rather than crashing the crew.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given string as an LDAP DN and returns the resulting {@link Dn}.
     * Returns {@code null} if the input is {@code null} or cannot be parsed as a valid DN.
     * We swallow {@link LdapInvalidDnException} silently — callers should be prepared
     * for a {@code null} return.
     *
     * @param dn  The DN string to parse.
     * @return  The parsed {@link Dn}, or {@code null} if invalid.
     */
    public static Dn getLdapDn( String dn )
    {
        if ( dn == null )
        {
            return null;
        }
        try
        {
            return new Dn( dn );
        }
        catch ( LdapInvalidDnException e )
        {
            return null;
        }
    }

}
