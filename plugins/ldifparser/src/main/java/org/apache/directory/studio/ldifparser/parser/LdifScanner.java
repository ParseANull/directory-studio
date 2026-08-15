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

package org.apache.directory.studio.ldifparser.parser;


import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;


// RFC 2849
//
// ldif-file = ldif-content / ldif-changes
// ldif-content = version-spec 1*(1*SEP ldif-attrval-record)
// ldif-changes = version-spec 1*(1*SEP ldif-change-record)
// ldif-attrval-record = dn-spec SEP 1*attrval-spec
// ldif-change-record = dn-spec SEP *control changerecord
// version-spec = "version:" FILL version-number
// version-number = 1*DIGIT
// ; version-number MUST be "1" for the
// ; LDIF format described in this document.
// dn-spec = "dn:" (FILL distinguishedName /
// ":" FILL base64-distinguishedName)
// distinguishedName = SAFE-STRING
// ; a distinguished name, as defined in [3]
// base64-distinguishedName = BASE64-UTF8-STRING
// ; a distinguishedName which has been base64
// ; encoded (see note 10, below)
// rdn = SAFE-STRING
// ; a relative distinguished name, defined as
// ; <name-component> in [3]
// base64-rdn = BASE64-UTF8-STRING
// ; an rdn which has been base64 encoded (see
// ; note 10, below)
// control = "control:" FILL ldap-oid ; controlType
// 0*1(1*SPACE ("true" / "false")) ; criticality
// 0*1(value-spec) ; controlValue
// SEP
// ; (See note 9, below)
// ldap-oid = 1*DIGIT 0*1("." 1*DIGIT)
// ; An LDAPOID, as defined in [4]
// attrval-spec = AttributeDescription value-spec SEP
// value-spec = ":" ( FILL 0*1(SAFE-STRING) /
// ":" FILL (BASE64-STRING) /
// "<" FILL url)
// ; See notes 7 and 8, below
// url = <a Uniform Resource Locator,
// as defined in [6]>
// ; (See Note 6, below)
// AttributeDescription = AttributeType [";" options]
// ; Definition taken from [4]
// AttributeType = ldap-oid / (ALPHA *(attr-type-chars))
// options = option / (option ";" options)
// option = 1*opt-char
// attr-type-chars = ALPHA / DIGIT / "-"
// opt-char = attr-type-chars
// changerecord = "changetype:" FILL
// (change-add / change-delete /
// change-modify / change-moddn)
// change-add = "add" SEP 1*attrval-spec
// change-delete = "delete" SEP
// change-moddn = ("modrdn" / "moddn") SEP
// "newrdn:" ( FILL rdn /
// ":" FILL base64-rdn) SEP
// "deleteoldrdn:" FILL ("0" / "1") SEP
// 0*1("newsuperior:"
// ( FILL distinguishedName /
// ":" FILL base64-distinguishedName) SEP)
// change-modify = "modify" SEP *mod-spec
// mod-spec = ("add:" / "delete:" / "replace:")
// FILL AttributeDescription SEP
// *attrval-spec
// "-" SEP
// SPACE = %x20
// ; ASCII SP, space
// FILL = *SPACE
// SEP = (CR LF / LF)
// CR = %x0D
// ; ASCII CR, carriage return
// LF = %x0A
// ; ASCII LF, line feed
// ALPHA = %x41-5A / %x61-7A
// ; A-Z / a-z
// DIGIT = %x30-39
// ; 0-9
// UTF8-1 = %x80-BF
// UTF8-2 = %xC0-DF UTF8-1
// UTF8-3 = %xE0-EF 2UTF8-1
// UTF8-4 = %xF0-F7 3UTF8-1
// UTF8-5 = %xF8-FB 4UTF8-1
// UTF8-6 = %xFC-FD 5UTF8-1
// SAFE-CHAR = %x01-09 / %x0B-0C / %x0E-7F
// ; any value <= 127 decimal except NUL, LF,
// ; and CR
// SAFE-INIT-CHAR = %x01-09 / %x0B-0C / %x0E-1F /
// %x21-39 / %x3B / %x3D-7F
// ; any value <= 127 except NUL, LF, CR,
// ; SPACE, colon (":", ASCII 58 decimal)
// ; and less-than ("<" , ASCII 60 decimal)
// SAFE-STRING = [SAFE-INIT-CHAR *SAFE-CHAR]
// UTF8-CHAR = SAFE-CHAR / UTF8-2 / UTF8-3 /
// UTF8-4 / UTF8-5 / UTF8-6
// UTF8-STRING = *UTF8-CHAR
// BASE64-UTF8-STRING = BASE64-STRING
// ; MUST be the base64 encoding of a
// ; UTF8-STRING
// BASE64-CHAR = %x2B / %x2F / %x30-39 / %x3D / %x41-5A /
// %x61-7A
// ; +, /, 0-9, =, A-Z, and a-z
// ; as specified in [5]
// BASE64-STRING = [*(BASE64-CHAR)]


// ── CLASS: LdifScanner — C-3PO'S LOW-LEVEL CHARACTER SCANNER ─────────────────
// Before C-3PO can translate a Rebel transmission he must break the raw byte
// stream into recognisable signal fragments: keyword tokens, separator tokens,
// value tokens, OID tokens.  He reads character by character, looks ahead
// for folding continuations (a newline followed by a space means "this line
// continues"), and groups characters into labelled tokens.
// LdifScanner is that low-level reader: it holds a buffered Reader, a position
// counter, and a family of match*() methods that each try to consume one type
// of RFC 2849 lexical unit and return an LdifToken on success or null on
// failure.  The parser never sees raw characters — only the labelled tokens
// this scanner produces.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Low-level character scanner for LDIF input.
 * Implements the RFC 2849 lexical layer: reads from a {@link Reader},
 * maintains a rolling character buffer, handles LDIF line-folding
 * (continuation lines that begin with a space), and exposes a set of
 * {@code match*()} methods — one per lexical category.
 * Each {@code match*()} method tries to consume its token at the current
 * position.  If it succeeds it returns an {@link LdifToken}; if it fails it
 * unreads whatever it consumed and returns {@code null}.
 * The {@link LdifParser} drives this scanner at a higher level, calling
 * {@code match*()} in the order dictated by the grammar.
 * Think of the scanner as C-3PO's character-level perception module — he reads
 * each signal pulse, detects folding continuations, and assembles labelled
 * lexical units for the grammar layer above.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifScanner
{

    /** The upstream character source. */
    private Reader ldifReader;

    /** Temporary buffer used when reading blocks from the Reader. */
    private char[] buffer = new char[256];

    /**
     * Rolling string buffer that accumulates characters read from
     * {@link #ldifReader}.
     */
    private StringBuffer ldifBuffer;

    /** The absolute offset of {@link #ldifBuffer}'s first character. */
    private int ldifBufferOffset;

    /** The current read position (absolute, 0-based). */
    private int pos;


    // ── CONSTRUCTOR / INITIALISATION ──────────────────────────────────────────
    // ── Create the Scanner ────────────────────────────────────────────────────
    // C-3PO powers up his perception module before any transmission arrives.
    // We construct the scanner without associating it with any input yet.
    /**
     * Creates a scanner that is not yet connected to any input.
     * Call {@link #setLdif(Reader)} before invoking any {@code match*()}
     * method.
     */
    public LdifScanner()
    {
        super();
    }


    // ── Point the Scanner at a New Input Stream ───────────────────────────────
    // A new transmission arrives; C-3PO resets all his pointers and starts
    // reading from the beginning.
    // We reset the position and buffer so the scanner is ready to read from
    // the supplied Reader.
    /**
     * Connects the scanner to a new input stream and resets all state.
     * After calling this method, {@code match*()} calls read from
     * {@code ldifReader}.
     *
     * @param ldifReader  the character stream to scan
     */
    public void setLdif( Reader ldifReader )
    {
        this.ldifReader = ldifReader;
        this.pos = -1;

        this.ldifBuffer = new StringBuffer();
        this.ldifBufferOffset = 0;
    }


    // ── LOW-LEVEL READ / UNREAD ───────────────────────────────────────────────
    // ── Peek at the Character at the Current Position ─────────────────────────
    // C-3PO reads the next signal pulse from the buffer without advancing; if
    // the buffer doesn't yet contain that position he reads more bytes from the
    // upstream Reader.
    // We fill the buffer as needed, then return the character at pos.
    /**
     * Returns the character at the current position without advancing.
     * Fills the internal buffer from {@link #ldifReader} if necessary.
     *
     * @return the character at position {@link #pos}
     * @throws EOFException if the position is past the end of the stream
     */
    char currentChar() throws EOFException
    {

        // check and fill buffer
        try
        {
            int num = 0;
            while ( ldifBufferOffset + ldifBuffer.length() <= pos && num > -1 )
            {
                num = this.ldifReader.read( buffer );
                if ( num > -1 )
                {
                    ldifBuffer.append( buffer, 0, num );
                }
            }
        }
        catch ( IOException e )
        {
        }

        if ( 0 <= pos && pos < ldifBufferOffset + ldifBuffer.length() )
        {
            try
            {
                return ldifBuffer.charAt( pos - ldifBufferOffset );
            }
            catch ( RuntimeException e )
            {
                e.printStackTrace();
                throw e;
            }
        }
        else
        {
            throw new EOFException();
        }
    }


    // ── Detect and Consume an LDIF Fold Continuation ─────────────────────────
    // LDIF allows a long value to be wrapped across multiple physical lines
    // by putting a space at the start of the continuation line.  C-3PO detects
    // the newline+space sequence and splices it into the working buffer so the
    // caller sees the fold characters rather than an unexpected line-break.
    // We look one character ahead; if we see a newline followed by a space we
    // append the fold sequence to sb, otherwise we restore the position.
    /**
     * Looks ahead for an LDIF line-fold sequence (newline immediately followed
     * by a space).  If found, appends the fold sequence ({@code \n },
     * {@code \r\n }, etc.) to {@code sb} and advances {@link #pos}; otherwise
     * leaves pos unchanged.
     *
     * @param sb  the buffer that is accumulating the current token
     */
    void addFolding( StringBuffer sb )
    {

        int oldPos = pos;

        try
        {
            pos++;
            char c = currentChar();
            if ( c == '\n' || c == '\r' )
            {
                StringBuffer temp = new StringBuffer( 3 );
                temp.append( c );
                if ( c == '\r' )
                {
                    pos++;
                    c = currentChar();
                    if ( c == '\n' )
                    {
                        temp.append( c );
                    }
                    else
                    {
                        pos--;
                    }
                }
                else if ( c == '\n' )
                {
                    pos++;
                    c = currentChar();
                    if ( c == '\r' )
                    {
                        temp.append( c );
                    }
                    else
                    {
                        pos--;
                    }
                }

                pos++;
                c = currentChar();
                if ( c == ' ' )
                {
                    // space after newline, continue
                    temp.append( c );
                    sb.append( temp );
                }
                else
                {
                    for ( int i = 0; i < temp.length(); i++ )
                    {
                        pos--;
                    }
                    pos--;
                }
            }
            else
            {
                pos--;
            }
        }
        catch ( EOFException e )
        {
            // reset position
            pos = oldPos;
        }

    }


    // ── Advance to the Next Character ─────────────────────────────────────────
    // C-3PO advances one position in the stream, appending the character to his
    // working buffer, and then checks whether a fold continuation follows.
    // We increment pos, read the character, append it plus any fold sequence
    // to sb, and return the character.  On EOF we decrement pos and re-throw.
    /**
     * Reads the next character from the input stream, appends it (plus any
     * LDIF fold sequence) to {@code sb}, and returns it.
     * Also checks for a fold sequence (SEP + SPACE) after the character; if
     * found, the fold characters are appended to {@code sb} as well.
     *
     * @param sb  the buffer to append to
     * @return the next character
     * @throws EOFException if the stream is exhausted
     */
    public char read( StringBuffer sb ) throws EOFException
    {
        try
        {

            // get next char
            pos++;
            char c = currentChar();
            sb.append( c );

            // folding
            addFolding( sb );

            return c;
        }
        catch ( EOFException e )
        {
            pos--;
            throw e;
        }
    }


    // ── Detect and Remove a Fold Sequence at the End of the Buffer ────────────
    // The mirror image of addFolding: when unread() is called, C-3PO checks
    // whether the working buffer ends with a fold sequence and strips it off,
    // restoring pos to the position before the fold.
    // We walk pos backwards and delete the fold sequence from sb if present.
    /**
     * Removes a previously-appended fold sequence from {@code sb} and adjusts
     * {@link #pos} accordingly.  This is the inverse of {@link #addFolding}.
     *
     * @param sb  the buffer from which to remove the fold sequence
     */
    void removeFolding( StringBuffer sb )
    {

        int oldPos = pos;

        try
        {
            char c = currentChar();
            pos--;
            if ( c == ' ' )
            {
                StringBuffer temp = new StringBuffer();
                temp.insert( 0, c );
                c = currentChar();
                pos--;

                if ( c == '\n' || c == '\r' )
                {
                    if ( c == '\r' )
                    {
                        temp.insert( 0, c );
                        c = currentChar();
                        pos--;
                        if ( c == '\n' )
                        {
                            temp.insert( 0, c );
                        }
                        else
                        {
                            pos++;
                        }
                    }
                    else if ( c == '\n' )
                    {
                        temp.insert( 0, c );
                        c = currentChar();
                        pos--;
                        if ( c == '\r' )
                        {
                            temp.insert( 0, c );
                        }
                        else
                        {
                            pos++;
                        }
                    }

                    sb.delete( sb.length() - temp.length(), sb.length() );
                }
                else
                {
                    pos++;
                    pos++;
                }
            }
            else
            {
                pos++;
            }
        }
        catch ( EOFException e )
        {
            // reset position
            pos = oldPos;
        }
    }


    // ── Step Back One Character ───────────────────────────────────────────────
    // C-3PO realises the signal fragment he just consumed doesn't belong to the
    // current token; he puts it back so the next match*() call can try.
    // We strip any fold sequence, decrement pos, and remove the last character
    // from sb.
    /**
     * Reverses the previous {@link #read(StringBuffer)} call: removes any
     * preceding fold sequence, decrements {@link #pos}, and deletes the last
     * character from {@code sb}.
     *
     * @param sb  the buffer from which to remove the last character
     */
    public void unread( StringBuffer sb )
    {
        removeFolding( sb );

        if ( pos > -1 )
        {
            pos--;

            if ( sb.length() > 0 )
            {
                sb.deleteCharAt( sb.length() - 1 );
            }
        }
    }


    // ── STRING-LEVEL HELPERS ──────────────────────────────────────────────────
    // ── Read a Full Line Starting with a Given Prefix ────────────────────────
    // Given a starting keyword (e.g. "#"), we consume the keyword itself and
    // then everything until the next newline.
    // We call getWord then getContent and concatenate.
    /**
     * Reads the given {@code start} string then consumes everything up to
     * (but not including) the next newline.
     *
     * @param start  the required prefix
     * @return the full matched string, or {@code null} if the prefix was not
     *         found
     */
    private String getFullLine( String start )
    {
        String s1 = this.getWord( start );
        if ( s1 != null )
        {
            String s2 = getContent( false );
            return s2 != null ? s1 + s2 : s1;
        }
        else
        {
            return null;
        }
    }


    // ── Read Everything Until the Next Newline ────────────────────────────────
    // The content of a line is everything after the keyword/separator up to
    // (but not including) the end-of-line marker.  If the content is empty and
    // allowEmptyContent is false, we return null.
    // We loop reading characters until we hit a newline, then unread the
    // newline and return whatever we collected.
    /**
     * Reads characters until the next newline (or EOF), returning the
     * collected string.
     * The newline is left in the stream (unreads).
     *
     * @param allowEmptyContent  if {@code true}, returns an empty string rather
     *                           than {@code null} when nothing precedes the
     *                           newline
     * @return the content string, or {@code null} if nothing was read and
     *         {@code allowEmptyContent} is {@code false}
     */
    private String getContent( boolean allowEmptyContent )
    {

        StringBuffer sb = new StringBuffer( 256 );

        try
        {
            char c = ' ';
            while ( c != '\n' && c != '\r' )
            {
                c = read( sb );
            }
            unread( sb );

        }
        catch ( EOFException e )
        {
        }

        return sb.length() > 0 || allowEmptyContent ? sb.toString() : null;
    }


    // ── Match a Literal Keyword (Case-Insensitive) ────────────────────────────
    // Many LDIF keywords are case-insensitive ("dn", "version", "changetype").
    // We consume each character and compare case-insensitively; on the first
    // mismatch we unread everything we consumed and return null.
    // We loop over the characters of word; on success return the matched string.
    /**
     * Attempts to match {@code word} case-insensitively at the current
     * position.  Returns the matched string (preserving the source casing) on
     * success, or unreads and returns {@code null} on failure.
     *
     * @param word  the keyword to match
     * @return the matched string, or {@code null}
     */
    private String getWord( String word )
    {
        StringBuffer sb = new StringBuffer();

        // read
        try
        {
            boolean matches = true;
            for ( int i = 0; i < word.length(); i++ )
            {

                char c = read( sb );
                if ( Character.toUpperCase( c ) != Character.toUpperCase( word.charAt( i ) ) )
                {
                    matches = false;
                    unread( sb );
                    break;
                }
            }

            if ( matches )
            {
                return sb.toString();
            }
        }
        catch ( EOFException e )
        {
        }

        // unread
        while ( sb.length() > 0 )
        {
            unread( sb );
        }
        return null;
    }


    // ── Match a Keyword That Must Be Followed by a Colon ─────────────────────
    // Keywords like "dn", "version", "control" must be followed immediately by
    // a colon (or by EOF/newline for incomplete lines).  We try word + ":" first,
    // and if that fails we allow a bare word at EOF or newline.
    // We try getWord(word+":") and unread the colon; if that fails we try
    // getWord(word) and accept only EOF or newline as the next character.
    /**
     * Matches {@code word} and verifies that a colon immediately follows.
     * The colon is left in the stream.  Also accepts {@code word} at end-of-file
     * or end-of-line for error-tolerance.
     *
     * @param word  the keyword to match
     * @return the keyword string (without the colon), or {@code null}
     */
    private String getWordTillColon( String word )
    {

        String wordWithColon = word + ":"; //$NON-NLS-1$
        String line = getWord( wordWithColon );
        if ( line != null )
        {
            StringBuffer sb = new StringBuffer( line );
            unread( sb );
            return sb.toString();
        }

        // allow eof and sep
        line = getWord( word );
        if ( line != null )
        {
            StringBuffer sb = new StringBuffer( line );
            try
            {
                char c = read( sb );
                unread( sb );
                if ( c == '\r' || c == '\n' )
                {
                    return sb.toString();
                }
                else
                {
                    while ( sb.length() > 0 )
                    {
                        unread( sb );
                    }
                    return null;
                }
            }
            catch ( EOFException e )
            {
                return sb.toString();
            }
        }

        return null;
    }


    // ── Free Consumed Buffer Memory ───────────────────────────────────────────
    // The rolling buffer can grow unboundedly if we never trim it.  After each
    // top-level match we discard all characters before the current position.
    // We trim ldifBuffer to keep only the last few characters around pos.
    /**
     * Trims the internal string buffer to release memory for characters that
     * are now before the current position and will never be unread.
     */
    private void flushBuffer()
    {
        if ( this.ldifBufferOffset < this.pos && this.ldifBuffer.length() > 0 )
        {
            int delta = Math.min( pos - this.ldifBufferOffset, this.ldifBuffer.length() );
            delta--;
            this.ldifBuffer.delete( 0, delta );
            this.ldifBufferOffset += delta;
        }
    }


    // ── MATCH METHODS ─────────────────────────────────────────────────────────
    // ── Consume Remainder of a Malformed Line ────────────────────────────────
    // When a line is partially valid (e.g. "dn: something garbage") the parser
    // calls matchCleanupLine() to consume the rest before moving on.
    // We use getContent + matchSep to consume the remainder, then return an
    // UNKNOWN token.
    /**
     * Consumes everything up to (and including) the next line separator.
     * Used for error recovery when a partially-matched line is malformed.
     *
     * @return an {@link LdifToken#UNKNOWN} token, or {@code null} if nothing
     *         remains
     */
    public LdifToken matchCleanupLine()
    {
        this.flushBuffer();

        String line = getContent( false );
        LdifToken sep = matchSep();

        if ( line != null || sep != null )
        {
            if ( line == null )
                line = ""; //$NON-NLS-1$

            if ( sep != null )
                line += sep.getValue();

            return new LdifToken( LdifToken.UNKNOWN, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Consume Any Unrecognised Line ─────────────────────────────────────────
    // When the parser reaches a line that does not match any grammar rule,
    // it calls matchOther() to consume it so parsing can continue.
    // We consume content + optional sep and return an UNKNOWN token.
    /**
     * Matches any non-empty content on the current line (used as a catch-all
     * when no specific grammar rule matches).
     *
     * @return an {@link LdifToken#UNKNOWN} token, or {@code null} if the
     *         current position is at a line boundary or EOF
     */
    public LdifToken matchOther()
    {
        this.flushBuffer();

        String line = getContent( false );
        if ( line != null )
        {
            LdifToken sep = matchSep();
            if ( sep != null )
                line += sep.getValue();
            return new LdifToken( LdifToken.UNKNOWN, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match End of File ─────────────────────────────────────────────────────
    // At the end of the stream there is nothing left to read; C-3PO detects
    // the EOF and returns a special token to signal completion.
    // We try to read one character; if it throws EOFException we return an
    // EOF token.
    /**
     * Returns an {@link LdifToken#EOF} token if the stream is exhausted,
     * or {@code null} if more input remains.
     *
     * @return an EOF token, or {@code null}
     */
    public LdifToken matchEOF()
    {
        this.flushBuffer();

        StringBuffer sb = new StringBuffer( 1 );
        try
        {
            read( sb );
            unread( sb );
            return null;
        }
        catch ( EOFException e )
        {
            return new LdifToken( LdifToken.EOF, "", pos + 1 ); //$NON-NLS-1$
        }

    }


    // ── Match a Line Separator ────────────────────────────────────────────────
    // LDIF line endings are LF or CRLF (and some implementations use LFCR).
    // C-3PO checks for either variant and normalises them as a single SEP token.
    // We read the first character; if it's \n or \r we try to read the matching
    // second character.
    /**
     * Matches an LDIF line separator ({@code LF}, {@code CRLF}, or
     * {@code LFCR}).
     *
     * @return a {@link LdifToken#SEP} token, or {@code null} if the current
     *         character is not a line separator
     */
    public LdifToken matchSep()
    {
        this.flushBuffer();

        try
        {
            StringBuffer sb = new StringBuffer();
            char c = read( sb );
            if ( c == '\n' || c == '\r' )
            {

                // check for two-char-linebreak
                try
                {
                    if ( c == '\r' )
                    {
                        c = read( sb );
                        if ( c != '\n' )
                        {
                            unread( sb );
                        }
                    }
                    else if ( c == '\n' )
                    {
                        c = read( sb );
                        if ( c != '\r' )
                        {
                            unread( sb );
                        }
                    }
                }
                catch ( EOFException e )
                {
                }

                return new LdifToken( LdifToken.SEP, sb.toString(), pos - sb.length() + 1 );
            }
            else
            {
                unread( sb );
            }
        }
        catch ( EOFException e )
        {
        }

        return null;
    }


    // ── Match a Comment Line ──────────────────────────────────────────────────
    // An LDIF comment starts with "#" and extends to the end of the line.
    // We read the "#" and then the rest of the line.
    /**
     * Matches an LDIF comment ({@code # ...}).
     *
     * @return a {@link LdifToken#COMMENT} token, or {@code null} if the current
     *         line does not start with {@code #}
     */
    public LdifToken matchComment()
    {
        this.flushBuffer();

        String line = getFullLine( "#" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.COMMENT, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the Version Spec Keyword ───────────────────────────────────────
    // The "version" keyword precedes the version number and must be followed
    // by a colon.
    // We use getWordTillColon and return a VERSION_SPEC token.
    /**
     * Matches the {@code "version"} keyword (case-insensitive) followed by a
     * colon.
     *
     * @return a {@link LdifToken#VERSION_SPEC} token, or {@code null}
     */
    public LdifToken matchVersionSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "version" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.VERSION_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the DN Spec Keyword ─────────────────────────────────────────────
    // Every LDIF record starts with "dn:" — the distinguished name spec.
    // We use getWordTillColon and return a DN_SPEC token.
    /**
     * Matches the {@code "dn"} keyword (case-insensitive) followed by a colon.
     *
     * @return a {@link LdifToken#DN_SPEC} token, or {@code null}
     */
    public LdifToken matchDnSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "dn" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.DN_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the Control Spec Keyword ────────────────────────────────────────
    // Change records can carry "control:" lines before the changetype.
    // We use getWordTillColon and return a CONTROL_SPEC token.
    /**
     * Matches the {@code "control"} keyword (case-insensitive) followed by a
     * colon.
     *
     * @return a {@link LdifToken#CONTROL_SPEC} token, or {@code null}
     */
    public LdifToken matchControlSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "control" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CONTROL_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the Changetype Spec Keyword ─────────────────────────────────────
    // The "changetype" keyword tells us which kind of change operation follows.
    // We use getWordTillColon and return a CHANGETYPE_SPEC token.
    /**
     * Matches the {@code "changetype"} keyword (case-insensitive) followed by
     * a colon.
     *
     * @return a {@link LdifToken#CHANGETYPE_SPEC} token, or {@code null}
     */
    public LdifToken matchChangeTypeSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "changetype" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the Changetype Value Keyword ────────────────────────────────────
    // After "changetype:" comes one of the five operation keywords.  We try
    // them in order and return the first match.
    // We try getWord() for each variant and return the appropriate token type.
    /**
     * Matches one of the LDIF changetype keywords: {@code add}, {@code modify},
     * {@code delete}, {@code moddn}, or {@code modrdn} (case-insensitive).
     *
     * @return a CHANGETYPE_* token, or {@code null} if no changetype keyword
     *         matches
     */
    public LdifToken matchChangeType()
    {
        this.flushBuffer();

        String line = getWord( "add" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_ADD, line, pos - line.length() + 1 );
        }
        line = getWord( "modify" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_MODIFY, line, pos - line.length() + 1 );
        }
        line = getWord( "delete" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_DELETE, line, pos - line.length() + 1 );
        }
        line = getWord( "moddn" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_MODDN, line, pos - line.length() + 1 );
        }
        line = getWord( "modrdn" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.CHANGETYPE_MODDN, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match a Control Criticality Flag ─────────────────────────────────────
    // After the OID on a control line, an optional criticality word may appear
    // (preceded by spaces).  We skip leading spaces then try "true" and "false".
    // We consume spaces, then try getWord("true") and getWord("false").
    /**
     * Matches an optional control criticality value ({@code true} or
     * {@code false}), optionally preceded by spaces.
     *
     * @return a {@link LdifToken#CONTROL_CRITICALITY_TRUE} or
     *         {@link LdifToken#CONTROL_CRITICALITY_FALSE} token, or
     *         {@code null}
     */
    public LdifToken matchCriticality()
    {
        this.flushBuffer();

        StringBuffer sb = new StringBuffer();

        String s = getWord( " " ); //$NON-NLS-1$
        while ( s != null )
        {
            sb.append( s );
            s = getWord( " " ); //$NON-NLS-1$
        }

        String t = getWord( "true" ); //$NON-NLS-1$
        if ( t != null )
        {
            sb.append( t );
            return new LdifToken( LdifToken.CONTROL_CRITICALITY_TRUE, sb.toString(), pos - sb.length() + 1 );
        }
        String f = getWord( "false" ); //$NON-NLS-1$
        if ( f != null )
        {
            sb.append( f );
            return new LdifToken( LdifToken.CONTROL_CRITICALITY_FALSE, sb.toString(), pos - sb.length() + 1 );
        }

        while ( sb.length() > 0 )
        {
            unread( sb );
        }

        return null;
    }


    // ── Match a Decimal Number ────────────────────────────────────────────────
    // Numbers appear in the version-spec line and deleteoldrdn lines.
    // We read digits until the first non-digit and return what we collected.
    /**
     * Matches one or more decimal digit characters.
     *
     * @return a {@link LdifToken#NUMBER} token, or {@code null}
     */
    public LdifToken matchNumber()
    {
        this.flushBuffer();

        try
        {
            StringBuffer sb = new StringBuffer();
            char c = read( sb );
            if ( '0' <= c && c <= '9' )
            {

                try
                {
                    while ( '0' <= c && c <= '9' )
                    {
                        c = read( sb );
                    }
                    unread( sb );
                }
                catch ( EOFException e )
                {
                }

                return new LdifToken( LdifToken.NUMBER, sb.toString(), pos - sb.length() + 1 );
            }
            else
            {
                unread( sb );
            }
        }
        catch ( EOFException e )
        {
        }

        return null;
    }


    // ── Match an LDAP OID ─────────────────────────────────────────────────────
    // OIDs consist of digits and dots (e.g. "2.16.840.1.113730.3.4.2").
    // We read digits and dots until we hit a non-digit/non-dot character.
    /**
     * Matches an LDAP OID (digits and dots, starting with a digit).
     *
     * @return a {@link LdifToken#OID} token, or {@code null}
     */
    public LdifToken matchOid()
    {
        this.flushBuffer();

        try
        {
            StringBuffer sb = new StringBuffer();
            char c = read( sb );
            if ( '0' <= c && c <= '9' )
            {

                try
                {
                    while ( '0' <= c && c <= '9' || c == '.' )
                    {
                        c = read( sb );
                    }
                    unread( sb );
                }
                catch ( EOFException e )
                {
                }

                return new LdifToken( LdifToken.OID, sb.toString(), pos - sb.length() + 1 );
            }
            else
            {
                unread( sb );
            }
        }
        catch ( EOFException e )
        {
        }

        return null;
    }


    // ── Match an Attribute Description ───────────────────────────────────────
    // An attribute description is letters, digits, hyphens, dots, semicolons,
    // and underscores — starting with a letter or digit.
    // We read as many valid characters as possible and return the token.
    /**
     * Matches an LDAP attribute description
     * ({@code AttributeType [";" options]}).
     * The first character must be a letter or digit; subsequent characters may
     * also include {@code .}, {@code ;}, {@code -}, and {@code _}.
     *
     * @return a {@link LdifToken#ATTRIBUTE} token, or {@code null}
     */
    public LdifToken matchAttributeDescription()
    {
        this.flushBuffer();

        try
        {
            StringBuffer sb = new StringBuffer();
            char c = read( sb );
            if ( 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9' )
            {

                try
                {
                    while ( 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9' || c == '.'
                        || c == ';' || c == '-' || c == '_' )
                    {
                        c = read( sb );
                    }
                    unread( sb );
                }
                catch ( EOFException e )
                {
                }

                return new LdifToken( LdifToken.ATTRIBUTE, sb.toString(), pos - sb.length() + 1 );
            }
            else
            {
                unread( sb );
            }
        }
        catch ( EOFException e )
        {
        }

        return null;
    }


    // ── Match a Mod-Type Spec Keyword ─────────────────────────────────────────
    // The first line of a mod-spec must start with one of "add", "replace", or
    // "delete".  We try them in order and return the matching token type.
    /**
     * Matches one of the modification type keywords: {@code "add"},
     * {@code "replace"}, or {@code "delete"} (case-insensitive).
     *
     * @return a MODTYPE_*_SPEC token, or {@code null} if none matched
     */
    public LdifToken matchModTypeSpec()
    {
        this.flushBuffer();

        String line = getWord( "add" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODTYPE_ADD_SPEC, line, pos - line.length() + 1 );
        }
        line = getWord( "replace" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODTYPE_REPLACE_SPEC, line, pos - line.length() + 1 );
        }
        line = getWord( "delete" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODTYPE_DELETE_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the Mod-Spec Separator Dash ────────────────────────────────────
    // The closing dash "-" terminates each mod-spec inside a modify record.
    // We try to match a single literal "-" character.
    /**
     * Matches the single {@code "-"} separator that closes a modification
     * specification.
     *
     * @return a {@link LdifToken#MODTYPE_SEP} token, or {@code null}
     */
    public LdifToken matchModSep()
    {
        this.flushBuffer();

        String line = getWord( "-" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODTYPE_SEP, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match a Value Type Separator ──────────────────────────────────────────
    // After a keyword like an attribute name we expect either ":" (safe), "::"
    // (base64), or ":<" (URL).  We read the ":" then peek at the next character.
    // We read one or two separator characters plus optional leading spaces and
    // return the appropriate VALUE_TYPE_* token.
    /**
     * Matches an LDIF value type separator: {@code ":"} (safe string),
     * {@code "::"} (Base64), or {@code ":<"} (URL).  Leading spaces after the
     * separator are consumed and included in the token value.
     *
     * @return a VALUE_TYPE_* token, or {@code null} if the current character is
     *         not {@code :}
     */
    public LdifToken matchValueType()
    {
        this.flushBuffer();

        try
        {
            StringBuffer sb = new StringBuffer();
            char c = read( sb );
            if ( c == ':' )
            {

                int tokenType = LdifToken.VALUE_TYPE_SAFE;
                try
                {
                    c = read( sb );
                    if ( c == ':' )
                    {
                        tokenType = LdifToken.VALUE_TYPE_BASE64;
                    }
                    else if ( c == '<' )
                    {
                        tokenType = LdifToken.VALUE_TYPE_URL;
                    }
                    else
                    {
                        tokenType = LdifToken.VALUE_TYPE_SAFE;
                        unread( sb );
                    }

                    c = read( sb );
                    while ( c == ' ' )
                    {
                        c = read( sb );
                    }
                    unread( sb );

                }
                catch ( EOFException e )
                {
                }

                return new LdifToken( tokenType, sb.toString(), pos - sb.length() + 1 );
            }
            else
            {
                unread( sb );
            }
        }
        catch ( EOFException e )
        {
        }

        return null;
    }


    // ── Match the Value Payload ───────────────────────────────────────────────
    // After the value-type separator, everything until the newline is the value
    // (possibly including fold continuations).
    // We call getContent(true) and return a VALUE token.
    /**
     * Matches everything up to the next unfolded newline as a value payload.
     * Fold continuations (newline + space) are included in the token value.
     *
     * @return a {@link LdifToken#VALUE} token, or {@code null} if the current
     *         position is at a line boundary
     */
    public LdifToken matchValue()
    {
        this.flushBuffer();

        String line = getContent( true );
        if ( line != null )
        {
            return new LdifToken( LdifToken.VALUE, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the newrdn Spec Keyword ─────────────────────────────────────────
    // "newrdn" introduces the new relative DN in a moddn change record.
    // We use getWordTillColon and return a MODDN_NEWRDN_SPEC token.
    /**
     * Matches the {@code "newrdn"} keyword (case-insensitive) followed by a
     * colon.
     *
     * @return a {@link LdifToken#MODDN_NEWRDN_SPEC} token, or {@code null}
     */
    public LdifToken matchNewrdnSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "newrdn" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODDN_NEWRDN_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the deleteoldrdn Spec Keyword ───────────────────────────────────
    // "deleteoldrdn" carries the boolean flag indicating whether to delete the
    // old RDN after renaming.
    // We use getWordTillColon and return a MODDN_DELOLDRDN_SPEC token.
    /**
     * Matches the {@code "deleteoldrdn"} keyword (case-insensitive) followed
     * by a colon.
     *
     * @return a {@link LdifToken#MODDN_DELOLDRDN_SPEC} token, or {@code null}
     */
    public LdifToken matchDeleteoldrdnSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "deleteoldrdn" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODDN_DELOLDRDN_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }


    // ── Match the newsuperior Spec Keyword ────────────────────────────────────
    // "newsuperior" optionally specifies the new parent DN in a moddn record.
    // We use getWordTillColon and return a MODDN_NEWSUPERIOR_SPEC token.
    /**
     * Matches the {@code "newsuperior"} keyword (case-insensitive) followed by
     * a colon.
     *
     * @return a {@link LdifToken#MODDN_NEWSUPERIOR_SPEC} token, or {@code null}
     */
    public LdifToken matchNewsuperiorSpec()
    {
        this.flushBuffer();

        String line = getWordTillColon( "newsuperior" ); //$NON-NLS-1$
        if ( line != null )
        {
            return new LdifToken( LdifToken.MODDN_NEWSUPERIOR_SPEC, line, pos - line.length() + 1 );
        }

        return null;
    }

}
