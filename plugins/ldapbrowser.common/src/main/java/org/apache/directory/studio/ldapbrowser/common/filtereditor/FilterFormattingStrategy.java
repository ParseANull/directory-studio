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

package org.apache.directory.studio.ldapbrowser.common.filtereditor;


import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapAndFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterExtensibleComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterItemComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapNotFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapOrFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: FilterFormattingStrategy — R2 ORGANISES THE FALCON'S DATA BANKS ───
// After the escape from the Death Star, Han's Millennium Falcon is a mess —
// navigation coordinates, hyperdrive sequences, and cargo manifests all jumbled
// together in R2's memory. R2 methodically reorganises every data bank into
// a clean, indented hierarchical structure so any crew member can read it fast.
// We do exactly that: given a valid LDAP filter crammed on one line, we walk
// its parse tree and produce a neatly indented multi-line representation where
// nested sub-filters are indented under their parent.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements Eclipse's {@link IFormattingStrategy} to pretty-print LDAP filter
 * strings in the filter editor. When the user triggers Format (Ctrl+Shift+F),
 * we take the current filter, check it is syntactically valid, and rewrite it
 * with consistent indentation — one level per nesting depth.
 * Think of this class as R2-D2 reorganising the Falcon's chaotic data banks
 * into clean, readable structure.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterFormattingStrategy implements IFormattingStrategy
{

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The source viewer. */
    private ISourceViewer sourceViewer;


    // ── R2 POWERS UP THE DATA REORGANISER ────────────────────────────────────
    // R2 jacks into the Falcon's main computer and stores references to both
    // the data index (parser) and the display screen (source viewer) so he can
    // read the current data and write the clean version back to the screen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterFormattingStrategy} wired to the given
     * source viewer and parser. We need the parser to access the already-parsed
     * filter model and the source viewer to write the formatted result back
     * into the editor document.
     *
     * @param sourceViewer  the source viewer whose document will be overwritten
     *                      with the formatted filter text
     * @param parser        the filter parser holding the current parse model —
     *                      we call {@link LdapFilterParser#getModel()} to get
     *                      the filter tree
     */
    public FilterFormattingStrategy( ISourceViewer sourceViewer, LdapFilterParser parser )
    {
        this.parser = parser;
        this.sourceViewer = sourceViewer;
    }


    // ── R2 ACKNOWLEDGES THE REORGANISATION REQUEST ────────────────────────────
    // A crew member gives R2 the starting indent level before the job begins.
    // R2 beeps in acknowledgement — there's nothing to configure for a filter,
    // so this is a polite no-op.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse before the formatting pass begins, passing the initial
     * indentation context. We don't use it for filter formatting — filters always
     * start at indent level zero regardless of surrounding context.
     *
     * @param initialIndentation  the indentation in effect at the start of the
     *                            formatted region (not used)
     */
    public void formatterStarts( String initialIndentation )
    {
    }


    // ── R2 REORGANISES ALL THE DATA BANKS ────────────────────────────────────
    // R2 checks that the Falcon's nav computer data is structurally sound before
    // reorganising — corrupted data gets left alone. For valid data he builds
    // the clean layout and writes it straight back to the display screen,
    // bypassing the normal edit channel.
    // We write the formatted result directly to the document via the source
    // viewer rather than returning the string, because that's the only way to
    // replace the entire document content atomically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Formats the current filter and writes the result directly into the editor
     * document. We only format if the filter model is non-null and valid — an
     * invalid (partially typed) filter is left as-is to avoid destroying the
     * user's work in progress. The method always returns {@code null} because
     * we update the document ourselves rather than returning a replacement string.
     *
     * <p>For example — R2 reorganises the Falcon's nav-computer data:</p>
     * <pre>
     *   Input:  "(&(cn=Luke)(|(sn=Sky*)(mail=l@*))"
     *   Output: "(&amp;\n    (cn=Luke)\n    (|\n        (sn=Sky*)\n        (mail=l@*)\n    )\n)"
     * </pre>
     *
     * @param content        the filter string to format (not used — we read from
     *                       the parser model instead)
     * @param isLineStart    whether we are at the start of a line (not used)
     * @param indentation    the current indentation context (not used)
     * @param positions      position array to update (not used)
     * @return               {@code null} always — we write directly to the document
     */
    public String format( String content, boolean isLineStart, String indentation, int[] positions )
    {
        // this.parser.parse(content);
        LdapFilter model = parser.getModel();
        if ( model != null && model.isValid() )
        {
            sourceViewer.getDocument().set( getFormattedFilter( model, 0 ) );
        }

        return null;
    }


    // ── R2 FORMATS A SINGLE DATA BANK RECURSIVELY ────────────────────────────
    // For each data bank R2 finds, he checks what type it is — a simple record,
    // a NOT-negated block, an AND-cluster, or an OR-cluster — and lays it out
    // with the correct indent level, recursing into nested banks as needed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively builds the formatted string for a single {@link LdapFilter}
     * and all its nested sub-filters. Simple ({@code item}) and extensible
     * filters are placed on a single line. Compound filters ({@code &}, {@code |},
     * {@code !}) are split across multiple lines with each child indented one
     * level deeper.
     *
     * <p>For example — R2 formats a nested AND block:</p>
     * <pre>
     *   (&amp;
     *       (cn=Luke)
     *       (sn=Skywalker)
     *   )
     * </pre>
     *
     * @param filter  the filter node to format
     * @param indent  the current indent depth (0 = top level; increments for
     *                each nesting level)
     * @return        the formatted string for {@code filter} including all
     *                children, with leading indentation applied
     */
    private String getFormattedFilter( LdapFilter filter, int indent )
    {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0; i < indent; i++ )
        {
            sb.append( FilterAutoEditStrategy.INDENT_STRING );
        }

        LdapFilterComponent fc = filter.getFilterComponent();
        if ( fc instanceof LdapFilterItemComponent )
        {
            sb.append( '(' ).append( ( ( LdapFilterItemComponent ) fc ).toString() ).append( ')' );
        }
        else if ( fc instanceof LdapFilterExtensibleComponent )
        {
            sb.append( '(' ).append( ( ( LdapFilterExtensibleComponent ) fc ).toString() ).append( ')' );
        }
        else if ( fc instanceof LdapNotFilterComponent )
        {
            sb.append( "(!" ); //$NON-NLS-1$
            LdapNotFilterComponent lnfc = ( LdapNotFilterComponent ) fc;
            if ( lnfc.getFilters().length > 0
                && lnfc.getFilters()[0].getFilterComponent() instanceof LdapFilterItemComponent )
            {
                sb.append( getFormattedFilter( ( lnfc ).getFilters()[0], 0 ) );
            }
            else
            {
                sb.append( '\n' );
                sb.append( getFormattedFilter( ( lnfc ).getFilters()[0], indent + 1 ) );
                sb.append( '\n' );
                for ( int i = 0; i < indent; i++ )
                    sb.append( FilterAutoEditStrategy.INDENT_STRING );
            }
            sb.append( ')' );
        }
        else if ( fc instanceof LdapAndFilterComponent )
        {
            sb.append( "(&" ); //$NON-NLS-1$
            sb.append( '\n' );
            LdapFilter[] filters = ( ( LdapAndFilterComponent ) fc ).getFilters();
            for ( int i = 0; i < filters.length; i++ )
            {
                sb.append( getFormattedFilter( filters[i], indent + 1 ) );
                sb.append( '\n' );
            }
            for ( int i = 0; i < indent; i++ )
                sb.append( FilterAutoEditStrategy.INDENT_STRING );
            sb.append( ')' );
        }
        else if ( fc instanceof LdapOrFilterComponent )
        {
            sb.append( "(|" ); //$NON-NLS-1$
            sb.append( '\n' );
            LdapFilter[] filters = ( ( LdapOrFilterComponent ) fc ).getFilters();
            for ( int i = 0; i < filters.length; i++ )
            {
                sb.append( getFormattedFilter( filters[i], indent + 1 ) );
                sb.append( '\n' );
            }
            for ( int i = 0; i < indent; i++ )
                sb.append( FilterAutoEditStrategy.INDENT_STRING );
            sb.append( ')' );
        }

        return sb.toString();
    }


    // ── R2 SIGNALS THAT THE REORGANISATION IS COMPLETE ───────────────────────
    // Once R2 finishes reorganising the data banks, he sends an "all clear"
    // signal to the crew and powers down the formatter. Nothing extra to do.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse after the formatting pass ends. There is nothing to
     * clean up for filter formatting, so this is a no-op.
     */
    public void formatterStops()
    {
    }

}
