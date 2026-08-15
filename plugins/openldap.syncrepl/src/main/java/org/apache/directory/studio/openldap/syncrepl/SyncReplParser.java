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

import org.apache.directory.api.util.Position;
import org.apache.directory.api.util.Strings;


// ── CLASS: SyncReplParser — The Imperial Intelligence Officer Decoding the Briefing ─
// A sector command receives a syncrepl directive string — a single long line of
// space-separated "keyword=value" tokens — and must decode it into a structured
// intelligence dossier (a SyncRepl object) before it can act on it.
// The intelligence officer (this parser) reads the line character by character,
// recognises each keyword, extracts the value (which may be bare, single-quoted,
// or double-quoted), and writes it into the correct field of the dossier.
// Any unrecognised or malformed token is logged as a ParseException in a
// SyncReplParserException accumulator; once the whole line is consumed, if any
// errors were collected, the officer throws that accumulator to the caller so
// every problem is reported in one go.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Parses an OpenLDAP {@code syncrepl} directive string into a {@link SyncRepl} object.
 * We scan the directive character by character, recognise each keyword (rid,
 * provider, searchbase, type, …), extract its value (bare or quoted), and set
 * the corresponding field on the {@link SyncRepl} bean.  Parse errors are
 * accumulated in a {@link SyncReplParserException} and thrown collectively at
 * the end so the caller sees every problem at once.
 * Think of this class as the Imperial intelligence officer who decodes a full
 * sector synchronisation briefing line into its individual fields.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyncReplParser
{
    private static final String KEYWORD_RID = "rid";
    private static final String KEYWORD_PROVIDER = "provider";
    private static final String KEYWORD_SEARCHBASE = "searchbase";
    private static final String KEYWORD_TYPE = "type";
    private static final String KEYWORD_INTERVAL = "interval";
    private static final String KEYWORD_RETRY = "retry";
    private static final String KEYWORD_FILTER = "filter";
    private static final String KEYWORD_SCOPE = "scope";
    private static final String KEYWORD_ATTRS = "attrs";
    private static final String KEYWORD_ATTRSONLY = "attrsonly";
    private static final String KEYWORD_SIZELIMIT = "sizelimit";
    private static final String KEYWORD_TIMELIMIT = "timelimit";
    private static final String KEYWORD_SCHEMACHECKING = "schemachecking";
    private static final String KEYWORD_NETWORK_TIMEOUT = "network-timeout";
    private static final String KEYWORD_TIMEOUT = "timeout";
    private static final String KEYWORD_BINDMETHOD = "bindmethod";
    private static final String KEYWORD_BINDDN = "binddn";
    private static final String KEYWORD_SASLMECH = "saslmech";
    private static final String KEYWORD_AUTHCID = "authcid";
    private static final String KEYWORD_AUTHZID = "authzid";
    private static final String KEYWORD_CREDENTIALS = "credentials";
    private static final String KEYWORD_REALM = "realm";
    private static final String KEYWORD_SECPROPS = "secprops";
    private static final String KEYWORD_KEEPALIVE = "keepalive";
    private static final String KEYWORD_STARTTLS = "starttls";
    private static final String KEYWORD_TLS_CERT = "tls_cert";
    private static final String KEYWORD_TLS_KEY = "tls_key";
    private static final String KEYWORD_TLS_CACERT = "tls_cacert";
    private static final String KEYWORD_TLS_CACERTDIR = "tls_cacertdir";
    private static final String KEYWORD_TLS_REQCERT = "tls_reqcert";
    private static final String KEYWORD_TLS_CIPHERSUITE = "tls_ciphersuite";
    private static final String KEYWORD_TLS_CRLCHECK = "tls_crlcheck";
    private static final String KEYWORD_LOGBASE = "logbase";
    private static final String KEYWORD_LOGFILTER = "logfilter";
    private static final String KEYWORD_SYNCDATA = "syncdata";


    // ── The Officer Receives the Full Briefing Line ───────────────────────────
    // The public entry point: trimmed and converted to a char array, then handed
    // to parseInternal() which does the real work.  Synchronized so only one
    // thread can parse through this instance at a time (the Position object is
    // mutable and shared across all parse methods).  If any errors were
    // accumulated, we throw the whole lot in one SyncReplParserException.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a syncrepl directive string into a {@link SyncRepl} object.
     * We trim the input, convert it to a {@code char[]} for efficient indexing,
     * and hand it to the internal dispatcher.  If the internal parse accumulates
     * any {@link ParseException} objects, we throw them all at once as a
     * {@link SyncReplParserException}.
     *
     * <p>For example — an intelligence officer receives the briefing line:</p>
     * <pre>
     *   "rid=001 provider=ldap://ldap.example.com searchbase=\"dc=example,dc=com\"
     *    type=refreshOnly bindmethod=simple binddn=\"cn=repl,dc=example,dc=com\"
     *    credentials=secret"
     * </pre>
     *
     * @param s  the directive string to parse.
     * @return   a populated {@link SyncRepl} bean, or {@code null} if the string
     *           contained no recognisable keywords.
     * @throws SyncReplParserException  if one or more tokens could not be parsed.
     */
    public synchronized SyncRepl parse( String s ) throws SyncReplParserException
    {
        SyncReplParserException parserException = new SyncReplParserException();

        // Trimming the value
        s = Strings.trim( s );

        // Getting the chars of the string
        char[] chars = new char[s.length()];
        s.getChars( 0, s.length(), chars, 0 );

        // Creating the position
        Position pos = new Position();
        pos.start = 0;
        pos.end = 0;
        pos.length = chars.length;

        SyncRepl syncRepl = parseInternal( chars, pos, parserException );

        if ( parserException.size() > 0 )
        {
            throw parserException;
        }

        return syncRepl;
    }


    // ── Dispatch Each Keyword to Its Handler ──────────────────────────────────
    // We advance through the char array character by character.  Whitespace is
    // skipped.  When we recognise a keyword at the current position we advance
    // past it and call the corresponding private parse method.  Unrecognised
    // characters are skipped with a single pos.start++ (the else branch at the
    // bottom of the do-while).  We return the populated SyncRepl, or null if no
    // keyword was ever recognised.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Core parsing loop — scans the character array and dispatches each keyword
     * to its dedicated private parse method.
     * We return a populated {@link SyncRepl} when at least one keyword is
     * recognised, or {@code null} if the string contains no known keywords.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position (mutated throughout the loop).
     * @param parserException accumulator for any errors encountered.
     * @return                a populated {@link SyncRepl}, or {@code null}.
     */
    private SyncRepl parseInternal( char[] chars, Position pos, SyncReplParserException parserException )
    {
        SyncRepl syncRepl = new SyncRepl();
        boolean foundAtLeastOneProperty = false;

        char c = Strings.charAt( chars, pos.start );

        do
        {
            // Whitespace
            if ( Character.isWhitespace( c ) )
            {
                // We ignore all whitespaces
                pos.start++;
            }

            // rid
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_RID, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_RID.length();

                parseRid( chars, pos, syncRepl, parserException );
            }

            // provider
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_PROVIDER, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_PROVIDER.length();

                parseProvider( chars, pos, syncRepl, parserException );
            }

            // searchbase
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SEARCHBASE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SEARCHBASE.length();

                parseSearchBase( chars, pos, syncRepl, parserException );
            }

            // type
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TYPE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TYPE.length();

                parseType( chars, pos, syncRepl, parserException );
            }

            // interval
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_INTERVAL, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_INTERVAL.length();

                parseInterval( chars, pos, syncRepl, parserException );
            }

            // retry
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_RETRY, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_RETRY.length();

                parseRetry( chars, pos, syncRepl, parserException );
            }

            // filter
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_FILTER, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_FILTER.length();

                parseFilter( chars, pos, syncRepl, parserException );
            }

            // scope
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SCOPE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SCOPE.length();

                parseScope( chars, pos, syncRepl, parserException );
            }

            // attrsonly
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_ATTRSONLY, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_ATTRSONLY.length();

                syncRepl.setAttrsOnly( true );
            }

            // attrs
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_ATTRS, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_ATTRS.length();

                parseAttrs( chars, pos, syncRepl, parserException );
            }

            // sizelimit
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SIZELIMIT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SIZELIMIT.length();

                parseSizeLimit( chars, pos, syncRepl, parserException );
            }

            // timelimit
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TIMELIMIT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TIMELIMIT.length();

                parseTimeLimit( chars, pos, syncRepl, parserException );
            }

            // schemachecking
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SCHEMACHECKING, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SCHEMACHECKING.length();

                parseSchemaChecking( chars, pos, syncRepl, parserException );
            }

            // network-timeout
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_NETWORK_TIMEOUT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_NETWORK_TIMEOUT.length();

                parseNetworkTimeout( chars, pos, syncRepl, parserException );
            }

            // timeout
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TIMEOUT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TIMEOUT.length();

                parseTimeout( chars, pos, syncRepl, parserException );
            }

            // bindmethod
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_BINDMETHOD, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_BINDMETHOD.length();

                parseBindMethod( chars, pos, syncRepl, parserException );
            }

            // binddn
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_BINDDN, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_BINDDN.length();

                parseBindDn( chars, pos, syncRepl, parserException );
            }

            // saslmech
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SASLMECH, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SASLMECH.length();

                parseSaslMech( chars, pos, syncRepl, parserException );
            }

            // authcid
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_AUTHCID, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_AUTHCID.length();

                parseAuthcId( chars, pos, syncRepl, parserException );
            }

            // authzid
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_AUTHZID, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_AUTHZID.length();

                parseAuthzId( chars, pos, syncRepl, parserException );
            }

            // credentials
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_CREDENTIALS, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_CREDENTIALS.length();

                parseCredentials( chars, pos, syncRepl, parserException );
            }

            // realm
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_REALM, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_REALM.length();

                parseRealm( chars, pos, syncRepl, parserException );
            }

            // secprops
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SECPROPS, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SECPROPS.length();

                parseSecProps( chars, pos, syncRepl, parserException );
            }

            // keepalive
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_KEEPALIVE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_KEEPALIVE.length();

                parseKeepAlive( chars, pos, syncRepl, parserException );
            }

            // starttls
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_STARTTLS, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_STARTTLS.length();

                parseStartTls( chars, pos, syncRepl, parserException );
            }

            // tls_cert
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_CERT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_CERT.length();

                parseTlsCert( chars, pos, syncRepl, parserException );
            }

            // tls_key
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_KEY, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_KEY.length();

                parseTlsKey( chars, pos, syncRepl, parserException );
            }

            // tls_cacert
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_CACERT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_CACERT.length();

                parseTlsCacert( chars, pos, syncRepl, parserException );
            }

            // tls_cacertdir
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_CACERTDIR, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_CACERTDIR.length();

                parseTlsCacertDir( chars, pos, syncRepl, parserException );
            }

            // tls_reqcert
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_REQCERT, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_REQCERT.length();

                parseTlsReqCert( chars, pos, syncRepl, parserException );
            }

            // tls_ciphersuite
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_CIPHERSUITE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_CIPHERSUITE.length();

                parseTlsCipherSuite( chars, pos, syncRepl, parserException );
            }

            // tls_crlcheck
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_TLS_CRLCHECK, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_TLS_CRLCHECK.length();

                parseTlsCrlCheck( chars, pos, syncRepl, parserException );
            }

            // logbase
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_LOGBASE, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_LOGBASE.length();

                parseLogBase( chars, pos, syncRepl, parserException );
            }

            // logfilter
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_LOGFILTER, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_LOGFILTER.length();

                parseLogFilter( chars, pos, syncRepl, parserException );
            }

            // syncdata
            else if ( Strings.areEquals( chars, pos.start, KEYWORD_SYNCDATA, false ) >= 0 )
            {
                foundAtLeastOneProperty = true;
                pos.start += KEYWORD_SYNCDATA.length();

                parseSyncData( chars, pos, syncRepl, parserException );
            }

            // We couldn't find the appropriate option
            else
            {
                pos.start++;
            }
        }
        while ( ( pos.start != pos.length ) && ( ( c = Strings.charAt( chars, pos.start ) ) != '\0' ) );

        if ( foundAtLeastOneProperty )
        {
            return syncRepl;
        }

        return null;
    }


    // ── Decode the Replica ID Token ───────────────────────────────────────────
    // The officer reads the "rid" field: a numeric string identifying which
    // consumer this is among all consumers polling the same provider.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code rid} option and stores it in the {@link SyncRepl} bean.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseRid( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setRid( value );
        }
    }


    // ── Decode the Provider URL ───────────────────────────────────────────────
    // The officer reads the "provider" field: the LDAP (or LDAPS) URL of the
    // master server — the Imperial HQ address.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code provider} option (a Provider URL) and stores it.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseProvider( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setProvider( Provider.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Search Base DN ─────────────────────────────────────────────
    // The officer reads the "searchbase" field: the root DN of the subtree to
    // replicate — which corner of the Imperial directory to mirror.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code searchbase} option and stores the DN string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSearchBase( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setSearchBase( value );
        }
    }


    // ── Decode the Replication Type ───────────────────────────────────────────
    // The officer reads the "type" field: "refreshOnly" (scheduled courier) or
    // "refreshAndPersist" (permanent open channel).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code type} option and stores the corresponding {@link Type} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseType( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setType( Type.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Polling Interval ───────────────────────────────────────────
    // The officer reads the "interval" field: "dd:hh:mm:ss" — how often the
    // courier dispatches when operating in refreshOnly mode.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code interval} option and stores the corresponding {@link Interval}.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseInterval( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setInterval( Interval.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Reconnection Retry Schedule ────────────────────────────────
    // The officer reads the "retry" field: a quoted sequence of "interval count"
    // pairs describing the staged reconnection plan when Imperial HQ is
    // temporarily unreachable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code retry} option and stores the corresponding {@link Retry} schedule.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseRetry( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setRetry( Retry.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the LDAP Search Filter ────────────────────────────────────────
    // The officer reads the "filter" field: an RFC 4515 filter string restricting
    // which entries get replicated — e.g. only active personnel records.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code filter} option and stores the filter string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseFilter( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setFilter( value );
        }
    }


    // ── Decode the Search Scope ───────────────────────────────────────────────
    // The officer reads the "scope" field: base, one, sub, or subord — how deep
    // into the directory tree to mirror below the search base.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code scope} option and stores the corresponding {@link Scope} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseScope( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setScope( Scope.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Attribute List ─────────────────────────────────────────────
    // The officer reads the "attrs" field: a comma-separated list of attribute
    // types to include — only those columns of the intelligence file matter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code attrs} option and splits the comma-separated list into
     * individual attribute names, then adds them to the {@link SyncRepl} bean.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseAttrs( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            String[] attrs = value.split( ",( )*" );

            if ( ( attrs != null ) && ( attrs.length > 0 ) )
            {
                syncRepl.addAttribute( attrs );
            }
        }
    }


    // ── Decode the Size Limit ────────────────────────────────────────────────
    // The officer reads the "sizelimit" field: the maximum number of entries
    // the provider will return per sync operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code sizelimit} option and stores the integer value.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSizeLimit( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setSizeLimit( Integer.parseInt( value ) );
            }
            catch ( NumberFormatException e )
            {
                parserException.addParseException( new ParseException( "Unable to convert size limit value '" + value
                    + "' as an integer.", 0 ) );
            }
        }
    }


    // ── Decode the Time Limit ────────────────────────────────────────────────
    // The officer reads the "timelimit" field: the maximum number of seconds
    // the provider will spend on a search operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code timelimit} option and stores the integer value.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTimeLimit( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setTimeLimit( Integer.parseInt( value ) );
            }
            catch ( NumberFormatException e )
            {
                parserException.addParseException( new ParseException( "Unable to convert time limit value '" + value
                    + "' as an integer.", 0 ) );
            }
        }
    }


    // ── Decode the Schema Checking Flag ──────────────────────────────────────
    // The officer reads the "schemachecking" field: "on" or "off" — whether the
    // sector command validates replicated entries against its local schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code schemachecking} option and stores the {@link SchemaChecking} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSchemaChecking( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setSchemaChecking( SchemaChecking.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Network Timeout ────────────────────────────────────────────
    // The officer reads the "network-timeout" field: how many seconds to wait
    // for a TCP connection to Imperial HQ before giving up.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code network-timeout} option and stores the integer value.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseNetworkTimeout( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setNetworkTimeout( Integer.parseInt( value ) );
            }
            catch ( NumberFormatException e )
            {
                parserException.addParseException( new ParseException( "Unable to convert network timeout value '"
                    + value
                    + "' as an integer.", 0 ) );
            }
        }
    }


    // ── Decode the LDAP Operation Timeout ────────────────────────────────────
    // The officer reads the "timeout" field: how many seconds to wait for any
    // LDAP operation to complete before declaring the HoloNet link unresponsive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code timeout} option and stores the integer value.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTimeout( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setTimeout( Integer.parseInt( value ) );
            }
            catch ( NumberFormatException e )
            {
                parserException.addParseException( new ParseException( "Unable to convert timeout value '" + value
                    + "' as an integer.", 0 ) );
            }
        }
    }


    // ── Decode the Bind Method ────────────────────────────────────────────────
    // The officer reads the "bindmethod" field: "simple" (password) or "sasl"
    // (Kerberos / DIGEST-MD5 etc.) — how the sector command authenticates
    // itself to Imperial HQ.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code bindmethod} option and stores the {@link BindMethod} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseBindMethod( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setBindMethod( BindMethod.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Bind DN ───────────────────────────────────────────────────
    // The officer reads the "binddn" field: the DN used as the identity when
    // authenticating to Imperial HQ with simple bind.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code binddn} option and stores the DN string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseBindDn( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setBindDn( value );
        }
    }


    // ── Decode the SASL Mechanism ─────────────────────────────────────────────
    // The officer reads the "saslmech" field: the name of the SASL mechanism
    // (e.g. "DIGEST-MD5" or "GSSAPI") used for authentication.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code saslmech} option and stores the mechanism name string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSaslMech( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setSaslMech( value );
        }
    }


    // ── Decode the SASL Authentication Identity ───────────────────────────────
    // The officer reads the "authcid" field: the identity used for SASL
    // authentication — who the sector command claims to be.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code authcid} option and stores the authentication identity string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseAuthcId( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setAuthcid( value );
        }
    }


    // ── Decode the SASL Authorisation Identity ────────────────────────────────
    // The officer reads the "authzid" field: the identity the sector command
    // wants to act as — it may be different from the authentication identity.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code authzid} option and stores the authorisation identity string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseAuthzId( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setAuthzid( value );
        }
    }


    // ── Decode the Credentials ────────────────────────────────────────────────
    // The officer reads the "credentials" field: the password or shared secret
    // used to authenticate to Imperial HQ.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code credentials} option and stores the password string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseCredentials( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setCredentials( value );
        }
    }


    // ── Decode the SASL Realm ─────────────────────────────────────────────────
    // The officer reads the "realm" field: the authentication realm — the
    // organisational domain within which the credentials are valid.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code realm} option and stores the realm string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseRealm( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setRealm( value );
        }
    }


    // ── Decode the SASL Security Properties ──────────────────────────────────
    // The officer reads the "secprops" field: a comma-separated list of SASL
    // security properties (e.g. minimum SSF) to enforce on the connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code secprops} option and stores the security properties string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSecProps( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setSecProps( value );
        }
    }


    // ── Decode the TCP Keep-Alive Configuration ───────────────────────────────
    // The officer reads the "keepalive" field: "idle:probes:interval" — the TCP
    // keep-alive settings that prevent the long-lived HoloNet link from going
    // silent and being cut by a network firewall.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code keepalive} option and stores the {@link KeepAlive} object.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseKeepAlive( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setKeepAlive( KeepAlive.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the StartTLS Policy ────────────────────────────────────────────
    // The officer reads the "starttls" field: "yes" or "critical" — whether to
    // upgrade the plain-text HoloNet connection to an encrypted channel, and
    // whether to abort if the upgrade fails.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code starttls} option and stores the {@link StartTls} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseStartTls( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setStartTls( StartTls.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the TLS Client Certificate Path ────────────────────────────────
    // The officer reads the "tls_cert" field: the file path to the sector
    // command's TLS client certificate used to prove its identity to HQ.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_cert} option and stores the file path string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsCert( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setTlsCert( value );
        }
    }


    // ── Decode the TLS Private Key Path ──────────────────────────────────────
    // The officer reads the "tls_key" field: the path to the private key that
    // goes with the client certificate — the sector command's secret identity
    // credential.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_key} option and stores the file path string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsKey( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setTlsKey( value );
        }
    }


    // ── Decode the CA Certificate File Path ──────────────────────────────────
    // The officer reads the "tls_cacert" field: the path to the CA certificate
    // used to verify that Imperial HQ's TLS certificate is genuine.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_cacert} option and stores the file path string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsCacert( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setTlsCacert( value );
        }
    }


    // ── Decode the CA Certificate Directory Path ──────────────────────────────
    // The officer reads the "tls_cacertdir" field: a directory containing
    // multiple CA certificate files — a whole filing cabinet of trusted issuers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_cacertdir} option and stores the directory path string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsCacertDir( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setTlsCacertDir( value );
        }
    }


    // ── Decode the TLS Certificate Requirement ────────────────────────────────
    // The officer reads the "tls_reqcert" field: "never", "allow", "try", or
    // "demand" — how strictly the sector command validates HQ's certificate.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_reqcert} option and stores the {@link TlsReqCert} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsReqCert( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setTlsReqcert( TlsReqCert.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the TLS Cipher Suite ───────────────────────────────────────────
    // The officer reads the "tls_ciphersuite" field: which cipher algorithms are
    // allowed for the encrypted HoloNet channel.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_ciphersuite} option and stores the cipher suite string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsCipherSuite( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setTlsCipherSuite( value );
        }
    }


    // ── Decode the TLS CRL Check Policy ──────────────────────────────────────
    // The officer reads the "tls_crlcheck" field: "none", "peer", or "all" —
    // whether to check HQ's certificate against a revocation list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code tls_crlcheck} option and stores the {@link TlsCrlCheck} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseTlsCrlCheck( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setTlsCrlcheck( TlsCrlCheck.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Decode the Accesslog Base DN ──────────────────────────────────────────
    // The officer reads the "logbase" field: the root DN of the accesslog
    // database used when syncdata=accesslog — where HQ keeps its own change log.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code logbase} option and stores the DN string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseLogBase( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setLogBase( value );
        }
    }


    // ── Decode the Accesslog Filter ───────────────────────────────────────────
    // The officer reads the "logfilter" field: the LDAP filter restricting
    // which change-log entries to replicate when using the accesslog sync source.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code logfilter} option and stores the filter string.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseLogFilter( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            syncRepl.setLogFilter( value );
        }
    }


    // ── Decode the Sync Data Source ───────────────────────────────────────────
    // The officer reads the "syncdata" field: "default", "accesslog", or
    // "changelog" — which data source on the provider generates the replication
    // stream that the sector command consumes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the {@code syncdata} option and stores the {@link SyncData} constant.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position.
     * @param syncRepl        the bean to populate.
     * @param parserException accumulator for any parse errors.
     */
    private void parseSyncData( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        String value = getQuotedOrNotQuotedOptionValue( chars, pos, syncRepl, parserException );

        if ( value != null )
        {
            try
            {
                syncRepl.setSyncData( SyncData.parse( value ) );
            }
            catch ( ParseException e )
            {
                parserException.addParseException( e );
            }
        }
    }


    // ── Scan Forward Until the '=' Delimiter ──────────────────────────────────
    // After recognising a keyword we need to consume any whitespace before the
    // '=' sign and then the '=' itself.  Returns true if we found an '='; false
    // if we hit end-of-input or a non-whitespace non-'=' character first.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Advances past optional whitespace to find the {@code '='} that separates a
     * keyword from its value.  Consumes the {@code '='} and returns {@code true}
     * on success.  Returns {@code false} if any other non-whitespace character
     * is found first, or if the input ends before an {@code '='} is seen.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position (mutated).
     * @param syncRepl        unused here; included for API consistency.
     * @param parserException unused here; included for API consistency.
     * @return                {@code true} if {@code '='} was found and consumed.
     */
    private boolean findEqual( char[] chars, Position pos, SyncRepl syncRepl, SyncReplParserException parserException )
    {
        char c = Strings.charAt( chars, pos.start );
        do
        {
            // Whitespace
            if ( Character.isWhitespace( c ) )
            {
                pos.start++;
            }
            // '=' char
            else if ( c == '=' )
            {
                pos.start++;
                return true;
            }
            else
            {
                return false;
            }
        }
        while ( ( c = Strings.charAt( chars, pos.start ) ) != '\0' );

        return false;
    }


    // ── Extract a Value That May or May Not Be Quoted ─────────────────────────
    // Values in a syncrepl directive can appear bare ("simple") or enclosed in
    // double quotes ("dc=example,dc=com") or single quotes (rarely).  Within
    // quotes, the matching quote can be escaped with a backslash.  We call
    // findEqual() first; then we scan character by character collecting the
    // value until we hit whitespace (unquoted) or the closing quote (quoted).
    // If no value is found we record a ParseException in the accumulator and
    // return null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the value for a keyword, handling both bare and quoted forms.
     * We first call {@link #findEqual} to skip whitespace and consume the
     * {@code '='} delimiter.  Then we scan the value, which may be:
     * <ul>
     *   <li>bare — terminated by the next whitespace character; or</li>
     *   <li>quoted — enclosed in matching {@code "…"} or {@code '…'}, with the
     *       quote character escapable as {@code \"} or {@code \'}.</li>
     * </ul>
     * If no value is found (empty or missing), we log a
     * {@link ParseException} in the accumulator and return {@code null}.
     *
     * @param chars           the directive as a character array.
     * @param pos             current scanning position (mutated).
     * @param syncRepl        unused here; included for API consistency.
     * @param parserException accumulator; receives an error if no value is found.
     * @return                the extracted value string, or {@code null} on error.
     */
    private String getQuotedOrNotQuotedOptionValue( char[] chars, Position pos, SyncRepl syncRepl,
        SyncReplParserException parserException )
    {
        if ( findEqual( chars, pos, syncRepl, parserException ) )
        {
            char quoteChar = '\0';
            boolean isInQuotes = false;
            char c = Strings.charAt( chars, pos.start );
            char[] v = new char[chars.length - pos.start];
            int current = 0;

            do
            {
                if ( ( current == 0 ) && !isInQuotes )
                {
                    // Whitespace
                    if ( Character.isWhitespace( c ) )
                    {
                        // We ignore all whitespaces until we find the start of the value
                        pos.start++;
                        continue;
                    }
                    // Double quotes (") or single quotes (')
                    else if ( ( c == '"' ) || ( c == '\'' ) )
                    {
                        isInQuotes = true;
                        quoteChar = c;
                        pos.start++;
                        continue;
                    }
                    // Any other char is part of a value
                    else
                    {
                        v[current++] = c;
                        pos.start++;
                    }
                }
                else
                {
                    if ( isInQuotes )
                    {
                        // Double quotes (") or single quotes (')
                        if ( quoteChar == c )
                        {
                            isInQuotes = false;
                            pos.start++;
                            continue;
                        }
                        // Checking for escaped quotes
                        else if ( c == '\\' )
                        {
                            // Double quotes (")
                            if ( ( quoteChar == '"' ) && ( Strings.areEquals( chars, pos.start, "\\\"" ) >= 0 ) )
                            {
                                v[current++] = '"';
                                pos.start += 2;
                                continue;
                            }
                            // Single quotes (')
                            else if ( ( quoteChar == '\'' ) && ( Strings.areEquals( chars, pos.start, "\\'" ) >= 0 ) )
                            {
                                v[current++] = '\'';
                                pos.start += 2;
                                continue;
                            }
                        }
                        // Any other char is part of a value
                        else
                        {
                            v[current++] = c;
                            pos.start++;
                        }
                    }
                    else
                    {
                        // Whitespace
                        if ( Character.isWhitespace( c ) )
                        {
                            // Once we have found the start of the value, the first whitespace is the exit
                            break;
                        }
                        // Any other char is part of a value
                        else
                        {
                            v[current++] = c;
                            pos.start++;
                        }
                    }
                }
            }
            while ( ( c = Strings.charAt( chars, pos.start ) ) != '\0' );

            // Checking the resulting value
            if ( current == 0 )
            {
                parserException.addParseException( new ParseException( "Couldn't find the value for option '"
                    + KEYWORD_RID
                    + "'.", pos.start ) );
                return null;
            }

            char[] value = new char[current];
            System.arraycopy( v, 0, value, 0, current );

            // Getting the value as a String
            return new String( value );
        }
        else
        {
            parserException.addParseException( new ParseException( "Couldn't find the value for option '" + KEYWORD_RID
                + "'.", pos.start ) );
            return null;
        }
    }
}
