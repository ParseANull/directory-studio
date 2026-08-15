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
package org.apache.directory.studio.aciitemeditor.sourceeditor;


import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: ACIFormattingStrategy — C-3PO TIDYING THE DIPLOMATIC DISPATCH ─────
// When a diplomatic dispatch arrives in garbled form — all on one line, no
// indentation — C-3PO re-formats it: new paragraph after each comma, deeper
// indentation for each nested clause, and simple inner clauses kept on a
// single line.
// ACIFormattingStrategy is that formatting pass for raw ACI text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link IFormattingStrategy} that pretty-prints ACI text in the source editor.
 * The rules are:
 * <ul>
 *   <li>A new line after every comma (outside quoted strings)</li>
 *   <li>A new line and increased indent after every opening brace</li>
 *   <li>A new line and decreased indent before every closing brace</li>
 *   <li>Simple expressions (no nested braces, at most one comma) stay on one line</li>
 * </ul>
 * Think of this class as C-3PO tidying a diplomatic dispatch: he re-paragraphs
 * every clause so Grand Moff Tarkin can read it at a glance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIFormattingStrategy implements IFormattingStrategy
{
    /** The Constant INDENT_STRING. */
    public static final String INDENT_STRING = "    "; //$NON-NLS-1$

    /** The Constant NEWLINE. */
    public static final String NEWLINE = BrowserCoreConstants.LINE_SEPARATOR;

    /** The source viewer. */
    private ISourceViewer sourceViewer;


    // ── BIND TO THE SOURCE VIEWER ─────────────────────────────────────────────
    // C-3PO sits down at the source viewer's console so he knows which document
    // to reformat when the Format command fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIFormattingStrategy} bound to the given source viewer.
     * The viewer's document is replaced in-place when {@link #format} is called.
     *
     * <p>For example — the source viewer configuration creates one of these:</p>
     * <pre>
     *   IFormattingStrategy strategy = new ACIFormattingStrategy(sourceViewer);
     *   formatter.setFormattingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
     * </pre>
     *
     * @param sourceViewer  the source viewer whose document will be formatted
     */
    public ACIFormattingStrategy( ISourceViewer sourceViewer )
    {
        this.sourceViewer = sourceViewer;
    }


    // ── REFORMAT THE DOCUMENT ─────────────────────────────────────────────────
    // C-3PO takes the entire dispatch, runs his formatting rules over it, and
    // replaces the original text with the tidied version.
    // The Eclipse API passes content/indentation arguments, but we ignore them
    // and operate directly on the document to handle the whole ACI string at once.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String format( String content, boolean isLineStart, String indentation, int[] positions )
    {
        String oldContent = sourceViewer.getDocument().get();
        String newContent = internFormat( oldContent );
        sourceViewer.getDocument().set( newContent );

        return null;
    }


    // ── FORMATTER LIFECYCLE (NOT USED) ────────────────────────────────────────
    // C-3PO does not need a warm-up or cool-down pass; these lifecycle hooks
    // are empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void formatterStarts( String initialIndentation )
    {
    }


    /**
     * {@inheritDoc}
     */
    public void formatterStops()
    {
    }


    // ── WALK THE TEXT AND APPLY THE RULES ────────────────────────────────────
    // C-3PO reads the dispatch character by character, tracking quote state,
    // indent level, and whether the current clause is simple enough to stay
    // on one line.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Core formatting logic: scans {@code content} character by character and
     * rebuilds it with proper indentation and line breaks.
     *
     * @param content  the raw ACI string to format
     * @return         the formatted ACI string
     */
    private String internFormat( String content )
    {
        StringBuilder sb = new StringBuilder();

        // flag to track if a new line was started
        boolean newLineStarted = true;

        // flag to track if we are within a quoted string
        boolean inQuotedString = false;

        // flag to track if the current expression is appended in one-line mode
        boolean oneLineMode = false;

        // the current indent
        int indent = 0;

        int contentLength = content.length();

        for ( int i = 0; i < contentLength; i++ )
        {
            char c = content.charAt( i );

            // track quotes
            if ( c == '"' )
            {
                inQuotedString ^= true;
            }

            if ( ( c == '{' ) && !inQuotedString )
            {
                // check one-line mode
                oneLineMode = checkInOneLine( i, content );

                if ( oneLineMode )
                {
                    // no new line in one-line mode
                    sb.append( c );
                    newLineStarted = false;
                }
                else
                {
                    // start a new line, but avoid blank lines if there are multiple opened curlies
                    if ( !newLineStarted )
                    {
                        sb.append( NEWLINE );
                        for ( int x = 0; x < indent; x++ )
                        {
                            sb.append( INDENT_STRING );
                        }
                    }

                    // append the curly
                    sb.append( c );

                    // start a new line and increment indent
                    sb.append( NEWLINE );
                    newLineStarted = true;
                    indent++;

                    for ( int x = 0; x < indent; x++ )
                    {
                        sb.append( INDENT_STRING );
                    }
                }
            }
            else if ( ( c == '}' ) && !inQuotedString )
            {
                if ( oneLineMode )
                {
                    // no new line in one-line mode
                    sb.append( c );
                    newLineStarted = false;

                    // closed curly indicates end of one-line mode
                    oneLineMode = false;
                }
                else
                {
                    // decrement indent
                    indent--;

                    // start a new line, but avoid blank lines if there are multiple closed curlies
                    if ( newLineStarted )
                    {
                        // delete one indent
                        sb.delete( sb.length() - INDENT_STRING.length(), sb.length() );
                    }
                    else
                    {
                        sb.append( NEWLINE );

                        for ( int x = 0; x < indent; x++ )
                        {
                            sb.append( INDENT_STRING );
                        }
                    }

                    // append the curly
                    sb.append( c );

                    // start a new line
                    sb.append( NEWLINE );
                    newLineStarted = true;

                    for ( int x = 0; x < indent; x++ )
                    {
                        sb.append( INDENT_STRING );
                    }
                }
            }
            else if ( ( c == ',' ) && !inQuotedString )
            {
                // start new line on comma
                if ( oneLineMode )
                {
                    sb.append( c );
                    newLineStarted = false;
                }
                else
                {
                    sb.append( c );

                    sb.append( NEWLINE );
                    newLineStarted = true;

                    for ( int x = 0; x < indent; x++ )
                    {
                        sb.append( INDENT_STRING );
                    }
                }
            }
            else if ( Character.isWhitespace( c ) )
            {
                char c1 = 'A';

                if ( i + 1 < contentLength )
                {
                    c1 = content.charAt( i + 1 );
                }

                if ( ( !newLineStarted ) &&
                     // ignore space after starting a new line
                     ( c != '\n' ) && ( c != '\r' ) &&
                     // ignore new lines
                     !Character.isWhitespace( c1 ) && ( c1 != '\n' ) && ( c1 != '\r' ) )
                     // compress whitespaces
                {
                    sb.append( c );
                }
            }

            else
            {
                // default case: append the char
                sb.append( c );
                newLineStarted = false;
            }
        }

        return sb.toString();
    }


    // ── DECIDE IF A CLAUSE FITS ON ONE LINE ──────────────────────────────────
    // C-3PO looks ahead from an opening brace: if the clause has no nested braces
    // and at most one comma, he keeps the whole thing on a single line for
    // readability (e.g. simple permission tuples).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks ahead from index {@code i} (an opening brace) to determine whether
     * the expression it starts should be kept on a single line.
     * Returns {@code true} if the expression has no nested braces and at most
     * one comma (outside quoted strings).
     *
     * @param i        index of the opening brace in {@code content}
     * @param content  the full ACI string being formatted
     * @return         {@code true} if the expression can stay on one line
     */
    private boolean checkInOneLine( int i, String content )
    {
        // flag to track if we are within a quoted string
        boolean inQuote = false;

        // counter for commas
        int commaCounter = 0;

        int contentLength = content.length();
        for ( int k = i + 1; k < contentLength; k++ )
        {
            char c = content.charAt( k );

            // track quotes
            if ( c == '"' )
            {
                inQuote ^= true;
            }

            // open curly indicates nested expression
            if ( ( c == '{' ) && !inQuote )
            {
                return false;
            }

            // closing curly indicates end of expression
            if ( c == '}' && !inQuote )
            {
                return true;
            }

            // allow only single comma in an expression in one line
            if ( c == ',' && !inQuote )
            {
                commaCounter++;
                if ( commaCounter > 1 )
                {
                    return false;
                }
            }
        }

        return false;
    }

}
