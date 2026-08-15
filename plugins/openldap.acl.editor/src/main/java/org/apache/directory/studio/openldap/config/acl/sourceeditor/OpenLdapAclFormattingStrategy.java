/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.sourceeditor;


import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: OpenLdapAclFormattingStrategy — CASSIAN TIDYING UP THE STOLEN FILES ─
// After Cassian retrieves the raw Imperial ACL text, Jyn notices the "by" clauses
// are all jammed onto one line — hard to read, easy to misinterpret. Cassian's
// formatting pass inserts a newline before each "by" keyword (as long as it isn't
// inside a quoted string) and collapses consecutive empty lines, giving each
// who-clause its own visual row. The cleaned-up text is written back into the
// source viewer document so the officer can review it clearly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link IFormattingStrategy} for the OpenLDAP ACL source editor. When
 * the user clicks the Format button, this strategy rewrites the document so that
 * each {@code by} keyword starts on its own line. The only formatting rule is:
 * <ul>
 *   <li>Insert a newline before {@code by} when not inside a quoted string.</li>
 *   <li>Collapse multiple consecutive blank lines into one.</li>
 * </ul>
 *
 * <p>Think of this class as Cassian's clean-up pass over the stolen Imperial
 * file — tidying the layout before handing it to the rebellion.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclFormattingStrategy implements IFormattingStrategy
{
    /** The Constant NEWLINE. */
    public static final String NEWLINE = BrowserCoreConstants.LINE_SEPARATOR;

    /** The source viewer. */
    private ISourceViewer sourceViewer;


    // ── Constructing the Strategy with a Source Viewer ────────────────────────
    // Cassian needs a reference to the document he will be cleaning up so that
    // the format() method can read the current content and write the result back.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new formatting strategy bound to the given source viewer.
     * The viewer's document is read and replaced in place when {@link #format} is
     * invoked.
     *
     * @param sourceViewer  The source viewer whose document content will be reformatted.
     */
    public OpenLdapAclFormattingStrategy( ISourceViewer sourceViewer )
    {
        this.sourceViewer = sourceViewer;
    }


    // ── Triggering the Format Pass ─────────────────────────────────────────────
    // The Format button in the dialog button bar triggers this method via the
    // Eclipse content formatter framework. We ignore the {@code content} parameter
    // — the real content is retrieved directly from the source viewer document.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reformats the source viewer's document. The {@code content} parameter is
     * ignored; the actual content is retrieved via {@link ISourceViewer#getDocument()}.
     * The reformatted string is written back to the document in place.
     *
     * <p>For example — Cassian reformatting a cramped ACL line:</p>
     * <pre>
     *   // Before: "access to * by users read by * none"
     *   // After:  "access to *\nby users read\nby * none"
     * </pre>
     *
     * @param content        Ignored — the source viewer document is used instead.
     * @param isLineStart    Whether the formatter is at the start of a line (not used).
     * @param indentation    The indentation string (not used).
     * @param positions      Positions to track (not used).
     * @return               Always {@code null} (the document is updated in place).
     */
    public String format( String content, boolean isLineStart, String indentation, int[] positions )
    {
        String oldContent = sourceViewer.getDocument().get();
        String newContent = internFormat( oldContent );
        sourceViewer.getDocument().set( newContent );

        return null;
    }


    // ── Lifecycle: Formatter Starts ───────────────────────────────────────────
    // No initialisation needed — our strategy is stateless aside from the viewer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the framework before formatting begins. No action needed here.
     *
     * {@inheritDoc}
     *
     * @param initialIndentation  The initial indentation (ignored).
     */
    public void formatterStarts( String initialIndentation )
    {
    }


    // ── Lifecycle: Formatter Stops ────────────────────────────────────────────
    // No teardown needed — our strategy is stateless.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the framework after formatting ends. No action needed here.
     *
     * {@inheritDoc}
     */
    public void formatterStops()
    {
    }


    // ── The Internal Format Pass ───────────────────────────────────────────────
    // Cassian walks through the content character by character, tracking whether
    // he is inside a quoted string (where "by" is a literal, not a keyword). Every
    // time he sees "by" at a non-line-start position outside quotes, he inserts a
    // newline before it. He also collapses consecutive newlines into one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Core formatting logic. Walks the string character by character and inserts
     * a newline before each {@code by} keyword that appears outside a quoted string
     * and is not already at the start of a line. Also collapses consecutive blank
     * lines.
     *
     * <p>For example — the internal formatter inserting newlines before "by":</p>
     * <pre>
     *   internFormat("access to * by users read by * none")
     *   // → "access to *\nby users read\nby * none"
     * </pre>
     *
     * @param content  The raw ACL text to reformat.
     * @return         The reformatted ACL text.
     */
    private String internFormat( String content )
    {
        StringBuffer sb = new StringBuffer();

        // Flag to track if we are within a quoted string
        boolean inQuotedString = false;

        // Flag to track if a new line was started
        boolean newLineStarted = true;

        // Char index
        int i = 0;

        int contentLength = content.length();
        while ( i < contentLength )
        {
            char currentChar = content.charAt( i );

            // Tracking quotes
            if ( currentChar == '"' )
            {
                inQuotedString = !inQuotedString;
            }
            else if ( newLineStarted && ( ( currentChar == '\n' ) || ( currentChar == '\r' ) ) )
            {
                // Compress multiple newlines
                i++;
                continue;
            }

            // Checking if we're not in a quoted text
            if ( !inQuotedString )
            {
                // Getting the next char (if available)
                char nextChar = 0;
                if ( ( i + 1 ) < contentLength )
                {
                    nextChar = content.charAt( i + 1 );
                }

                // Checking if we have the "by" keyword
                if ( ( !newLineStarted ) && ( currentChar == 'b' ) && ( nextChar == 'y' ) )
                {
                    sb.append( NEWLINE );
                    newLineStarted = true;
                }
                else
                {
                    // Tracking new line
                    newLineStarted = ( ( currentChar == '\n' ) || ( currentChar == '\r' ) );
                }
            }

            sb.append( currentChar );
            i++;
        }

        return sb.toString();
    }
}
