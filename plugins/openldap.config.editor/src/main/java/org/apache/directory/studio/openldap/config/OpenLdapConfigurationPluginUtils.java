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
package org.apache.directory.studio.openldap.config;

import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;

// ── CLASS: OpenLdapConfigurationPluginUtils — C-3PO Translating Jawa Dialect ─
// On Tatooine, C-3PO (fluent in six million forms of communication) listens to
// the Jawas' incomprehensible rapid-fire chatter and translates it into something
// Luke and Owen Lars can actually understand — stripping the noise, extracting
// the signal, and reformatting it for the audience.
// OpenLDAP config values often have ordering prefixes like "{0}value" or postfixes
// like "value{3}" — syntactic noise the server uses for ordering but which editors
// don't want to show users. We strip those, parse them, and generally clean up
// OpenLDAP's internal string formats so the rest of the plugin can work with
// clean, human-readable values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * General-purpose utility methods for working with OpenLDAP config strings and lists.
 * OpenLDAP's cn=config stores ordered values using "{n}" prefixes or postfixes
 * (e.g. "{0}include" or "cn=config{1}"), which we need to strip, detect, and parse.
 * This class provides all those string-manipulation utilities, plus helpers for
 * extracting first values from lists and concatenating lists to display strings.
 * Think of us as C-3PO on Tatooine: translating the noisy internal format into
 * clean, readable output for the rest of the plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapConfigurationPluginUtils
{
    // ── C-3PO Declines To Translate Himself ───────────────────────────────────
    // "I am not programmed to respond in that area." C-3PO doesn't translate
    // his own internal workings — he's a pure service. This utility class has
    // no instance state, so we prevent instantiation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a pure static utility class. All methods
     * are static; there's no state to hold in an instance.
     */
    private OpenLdapConfigurationPluginUtils()
    {
        // Nothing to do
    }


    // ── C-3PO Strips The Jawa Order Code From The Front ──────────────────────
    // The Jawas shout a lot of prefix codes before getting to the actual item
    // name. C-3PO listens past the "{0}" prefix chatter and extracts just the
    // meaningful part: whatever comes after the closing "}" character.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the ordering prefix from the given string if one is present.
     * OpenLDAP ordering prefixes look like "{0}value" or "{-1}value". After
     * stripping, you get back just "value". If there's no prefix we return
     * the original string unchanged.
     *
     * @param s  the string to strip (may or may not have an ordering prefix)
     * @return   the string without the "{n}" prefix, or the original string if no prefix
     */
    public static String stripOrderingPrefix( String s )
    {
        if ( hasOrderingPrefix( s ) )
        {
            int indexOfClosingCurlyBracket = s.indexOf( '}' );

            if ( indexOfClosingCurlyBracket != -1 )
            {
                return s.substring( indexOfClosingCurlyBracket + 1 );
            }
        }

        return s;
    }


    // ── C-3PO Strips The Jawa Order Code From The Back ───────────────────────
    // Some Jawa listings shout the order code after the item name rather than
    // before it. C-3PO handles this too — he clips the trailing "{n}" postfix
    // and returns just the clean item name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the ordering postfix from the given string if one is present.
     * OpenLDAP ordering postfixes look like "value{0}". After stripping you get
     * back just "value". If there's no postfix we return the original string unchanged.
     *
     * @param s  the string to strip (may or may not have an ordering postfix)
     * @return   the string without the "{n}" postfix, or the original string if no postfix
     */
    public static String stripOrderingPostfix( String s )
    {
        if ( hasOrderingPostfix( s ) )
        {
            int indexOfOpeningCurlyBracket = s.indexOf( '{' );

            if ( indexOfOpeningCurlyBracket != -1 )
            {
                return s.substring( 0, indexOfOpeningCurlyBracket );
            }
        }

        return s;
    }


    // ── C-3PO Checks Whether The Jawa Is Using A Prefix Code ─────────────────
    // Before attempting a full parse, C-3PO does a quick check: is the Jawa
    // actually using a prefix ordering code, or are they speaking plain Basic?
    // We delegate to parseOrderingPrefix — if it returns non-null, there's a prefix.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given string starts with an ordering prefix like "{0}".
     * Useful as a guard before calling {@link #stripOrderingPrefix} or
     * {@link #getOrderingPrefix}. Delegates to the internal parser.
     *
     * @param s  the string to check
     * @return   {@code true} if a "{n}" prefix is present, {@code false} otherwise
     */
    public static boolean hasOrderingPrefix( String s )
    {
        return parseOrderingPrefix( s ) != null;
    }


    // ── C-3PO Checks Whether The Jawa Is Using A Postfix Code ────────────────
    // Quick sanity check before the full parse — does this value end with a Jawa
    // ordering postfix code? C-3PO can tell at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given string ends with an ordering postfix like "{0}".
     * Useful as a guard before calling {@link #stripOrderingPostfix} or
     * {@link #getOrderingPostfix}. Delegates to the internal parser.
     *
     * @param s  the string to check
     * @return   {@code true} if a "{n}" postfix is present, {@code false} otherwise
     */
    public static boolean hasOrderingPostfix( String s )
    {
        return parseOrderingPostfix( s ) != null;
    }


    // ── C-3PO Decodes The Prefix Order Number ────────────────────────────────
    // The Jawa prefix format is "{n}" where n is an integer (possibly negative).
    // C-3PO carefully parses character by character: open brace, optional minus,
    // digits, close brace. Returns null if the format doesn't match.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses an ordering prefix of the form {@code ^\{-?\d+\}.*$} from the start
     * of the string and returns the integer value, or null if the string doesn't
     * start with a valid prefix. This is the internal workhorse for
     * {@link #hasOrderingPrefix} and {@link #getOrderingPrefix}.
     *
     * @param prefixString  the string to parse
     * @return              the integer ordering value, or null if no valid prefix found
     */
    private static Integer parseOrderingPrefix( String prefixString )
    {
        if ( prefixString == null )
        {
            return null;
        }

        int pos = 0;

        if ( !Strings.isCharASCII( prefixString, pos++, '{') )
        {
            return null;
        }

        boolean positive = true;
        int prefix = 0;

        if ( Strings.isCharASCII( prefixString, pos, '-') )
        {
            positive = false;
            pos++;
        }

        char car;

        while ( ( car = Strings.charAt( prefixString, pos++ ) ) != '\0' )
        {
            switch ( car )
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    prefix = prefix * 10 + car - '0';
                    break;

                case '}' :
                    if ( positive )
                    {
                        return prefix;
                    }
                    else
                    {
                        return -prefix;
                    }

                default :
                    return null;
            }
        }

        return null;
    }


    // ── C-3PO Decodes The Postfix Order Number ────────────────────────────────
    // Same idea, but the Jawa is calling out the order number after the item name.
    // C-3PO scans forward to find the "{", then parses the digits until "}", just
    // like the prefix parser but starting from a different position.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses an ordering postfix of the form {@code ^.*\{-?\d+\}$} from the end
     * of the string and returns the integer value, or null if the string doesn't
     * end with a valid postfix. This is the internal workhorse for
     * {@link #hasOrderingPostfix} and {@link #getOrderingPostfix}.
     *
     * @param postfixString  the string to parse
     * @return               the integer ordering value, or null if no valid postfix found
     */
    private static Integer parseOrderingPostfix( String postfixString )
    {
        if ( postfixString == null )
        {
            return null;
        }

        int pos = -1;

        for ( int idx = 0; idx < postfixString.length(); idx++ )
        {
            if ( Strings.isCharASCII( postfixString, idx, '{') )
            {
                pos = idx + 1;
                break;
            }
        }

        if ( pos == -1 )
        {
            return null;
        }

        boolean positive = true;
        int prefix = 0;

        if ( Strings.isCharASCII( postfixString, pos, '-') )
        {
            positive = false;
        }

        char car;

        while ( ( car = Strings.charAt( postfixString, pos++ ) ) != '\0' )
        {
            switch ( car )
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    prefix = prefix * 10 + car - '0';
                    break;

                case '}' :
                    if ( positive )
                    {
                        return prefix;
                    }
                    else
                    {
                        return -prefix;
                    }

                default :
                    return null;
            }
        }

        return null;
    }


    // ── C-3PO Reads Out The Prefix Priority Number ───────────────────────────
    // Once C-3PO has confirmed there's a prefix code, the crew wants to know
    // the actual priority number — not just "yes there's a prefix" but "what
    // number is it?". We parse it and return -1 if absent (a safe sentinel).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the integer value of the ordering prefix in the given string, or -1
     * if no valid prefix is present. The prefix format is "{n}" at the start of
     * the string where n can be negative.
     *
     * <p>For example — C-3PO reads "{3}olcDatabase: mdb" and announces:</p>
     * <pre>
     *   getOrderingPrefix("{3}olcDatabase: mdb") == 3
     *   getOrderingPrefix("olcDatabase: mdb")    == -1  // no prefix
     * </pre>
     *
     * @param prefixString  the string to extract the prefix from
     * @return              the ordering integer, or -1 if none is present
     */
    public static int getOrderingPrefix( String prefixString )
    {
        Integer orderingPrefix = parseOrderingPrefix( prefixString );

        if ( orderingPrefix == null )
        {
            return -1;
        }
        else
        {
            return orderingPrefix;
        }
    }


    // ── C-3PO Reads Out The Postfix Priority Number ───────────────────────────
    // Same as the prefix version, but for postfix-encoded ordering. C-3PO reads
    // the trailing code and converts it to an integer the crew can use for sorting.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the integer value of the ordering postfix in the given string, or -1
     * if no valid postfix is present. The postfix format is "{n}" at the end of
     * the string where n can be negative.
     *
     * @param postfixString  the string to extract the postfix from
     * @return               the ordering integer, or -1 if none is present
     */
    public static int getOrderingPostfix( String postfixString )
    {
        Integer orderingPostfix = parseOrderingPostfix( postfixString );

        if ( orderingPostfix == null )
        {
            return -1;
        }
        else
        {
            return orderingPostfix;
        }
    }


    // ── C-3PO Picks The First Item Off The Manifest ───────────────────────────
    // The Jawas rattle off a list of items. C-3PO translates the first one for
    // the crew — because often that's all they need: just the lead value. Returns
    // null cleanly if the list is empty or null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first String in the given list, or null if the list is null or empty.
     * Handy when a config attribute is multi-valued but we only want to display or
     * use the primary value.
     *
     * @param values  the list to pull the first element from
     * @return        the first string in the list, or null if none
     */
    public static String getFirstValueString( List<String> values )
    {
        if ( ( values != null ) && !values.isEmpty() )
        {
            return values.get( 0 );
        }

        return null;
    }


    // ── C-3PO Translates The First DN Off The Manifest ────────────────────────
    // Same as above but for a list of Dn objects — C-3PO converts the first DN
    // into its string representation so the UI can display it without knowing
    // anything about the Dn type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation of the first {@link Dn} in the given list,
     * or null if the list is null or empty. Useful when a config attribute stores
     * a list of DNs but we only need to show or use the first one.
     *
     * @param values  the list of Dn objects to read the first element from
     * @return        the first DN as a string, or null if none
     */
    public static String getFirstValueDn( List<Dn> values )
    {
        if ( ( values != null ) && !values.isEmpty() )
        {
            return values.get( 0 ).toString();
        }

        return null;
    }


    // ── C-3PO Joins All The Jawa Chatter Into One Sentence ───────────────────
    // When the crew needs a summary of everything the Jawas said, C-3PO doesn't
    // read each item separately — he joins them all into a single comma-separated
    // sentence that humanoids can scan at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Joins a list of strings into a single comma-space-separated string.
     * Useful for displaying multi-valued config attributes as a single summary
     * line in the editor UI. An empty list returns an empty string.
     *
     * @param list  the list of strings to join
     * @return      a single string with all values joined by ", "
     */
    public static String concatenate( List<String> list )
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( String string : list )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            sb.append( string );
        }

        return sb.toString();
    }
}
