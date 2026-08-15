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
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapNotFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapOrFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;


// ── CLASS: FilterAutoEditStrategy — R2-D2 SEALS BLAST DOORS ──────────────────
// Aboard the Death Star, R2-D2 intercepts corridor commands in real time and
// automatically seals the blast doors the moment a droid passes through, keeping
// every corridor balanced and sealed without any crew intervention.
// That is exactly what we do here: we intercept every keystroke in the filter
// editor and auto-insert or auto-delete the matching parenthesis so the filter
// stays balanced at all times.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides smart auto-editing for the LDAP filter editor — specifically, it
 * keeps parentheses balanced as the user types.
 * When you type {@code (}, we automatically add the closing {@code )}; when you
 * delete the last character inside a pair of parentheses, we remove both of
 * them. Think of this class as R2-D2 sealing blast doors: every opening gets a
 * matching close, hands-free.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterAutoEditStrategy extends DefaultIndentLineAutoEditStrategy implements IAutoEditStrategy
{

    /** The Constant INDENT_STRING. */
    public static final String INDENT_STRING = "    "; //$NON-NLS-1$

    /** The filter parser. */
    private LdapFilterParser parser;


    // ── R2 WIRES INTO THE CORRIDOR CONTROL PANEL ──────────────────────────────
    // On the Death Star, R2-D2 plugs into the computer interface port to gain
    // control of all blast-door operations in the corridor network.
    // He needs to know the layout before he can start sealing doors intelligently.
    // We store the parser here so every later auto-edit decision is informed by
    // the current state of the parsed filter tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterAutoEditStrategy} wired to the given parser.
     * We need the parser so we can inspect the live filter model and decide
     * whether a given keystroke warrants an auto-inserted parenthesis.
     *
     * @param parser  the filter parser that holds the current parse tree —
     *                we call it every time the document changes
     */
    public FilterAutoEditStrategy( LdapFilterParser parser )
    {
        this.parser = parser;
    }


    // ── R2 INTERCEPTS THE DOOR COMMAND ────────────────────────────────────────
    // The Death Star's door controller sends a raw command (open door X).
    // R2-D2 intercepts it, augments it with the matching close command, and
    // forwards the modified instruction back into the system transparently.
    // Eclipse calls this method for every edit command before it hits the
    // document — we translate the raw command into our smarter version.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Eclipse calls this method before every document edit so we can intercept
     * and modify the command. We delegate the heavy logic to
     * {@link #customizeAutoEditParameters} and then copy the results back into
     * the Eclipse {@link DocumentCommand}.
     *
     * <p>For example — R2 intercepts a raw door signal and upgrades it:</p>
     * <pre>
     *   User types '(' at offset 5
     *   R2 intercepts: raw command = { text:"(", offset:5 }
     *   R2 augments:   command     = { text:"()", offset:5, caretOffset:6 }
     *   Door seals behind the droid automatically.
     * </pre>
     *
     * @param d  the document being edited — we call {@code d.get()} to read the
     *           current filter string
     * @param c  the pending edit command — we mutate its fields in place to
     *           inject our auto-edit behaviour
     */
    public void customizeDocumentCommand( IDocument d, DocumentCommand c )
    {
        super.customizeDocumentCommand( d, c );
        AutoEditParameters aep = new AutoEditParameters( c.text, c.offset, c.length, c.caretOffset, c.shiftsCaret );
        customizeAutoEditParameters( d.get(), aep );
        c.offset = aep.offset;
        c.length = aep.length;
        c.text = aep.text;
        c.caretOffset = aep.caretOffset;
        c.shiftsCaret = aep.shiftsCaret;
    }


    // ── R2 DECIDES WHICH DOORS TO SEAL ────────────────────────────────────────
    // With the corridor map in memory, R2 looks at where each droid is standing
    // and decides which blast doors to seal: doors before unprotected gaps get
    // closed, orphaned open doors get paired, and already-sealed corridors are
    // left alone.
    // This is our core logic: given the current filter text and an edit, we
    // figure out exactly what auto-insert or auto-delete is needed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Core auto-edit logic — inspects the current filter string and the pending
     * edit, then adjusts the {@link AutoEditParameters} to auto-insert or
     * auto-delete parentheses as needed.
     * <p>
     * We handle three main cases: typing {@code (} auto-appends {@code )},
     * typing a logical operator ({@code &}, {@code |}, {@code !}) auto-appends
     * {@code ()}, and deleting the last character inside {@code (…)} removes
     * the surrounding parens too.
     * </p>
     *
     * <p>For example — R2 checks the corridor map:</p>
     * <pre>
     *   currentFilter = "(cn=J"
     *   aep.text = ")" (user typed closing paren manually)
     *   R2 sees the filter is already balanced — no extra seal needed.
     *   aep unchanged; Eclipse inserts exactly what the user typed.
     * </pre>
     *
     * @param currentFilter  the full filter string currently in the editor —
     *                       we parse it to build the filter tree
     * @param aep            the mutable edit parameters we adjust in place;
     *                       offsets, lengths, and text may all change
     */
    public void customizeAutoEditParameters( String currentFilter, AutoEditParameters aep )
    {
        parser.parse( currentFilter );
        LdapFilter filter = parser.getModel().getFilter( aep.offset );
        if ( filter == null )
        {
            return;
        }

        // check balanced parenthesis
        int balanced = 0;
        for ( int i = 0; i < currentFilter.length(); i++ )
        {
            if ( currentFilter.charAt( i ) == '(' )
            {
                balanced++;
            }
            else if ( currentFilter.charAt( i ) == ')' )
            {
                balanced--;
            }
        }

        if ( aep.length > 0 && ( aep.text == null || "".equals( aep.text ) ) ) //$NON-NLS-1$
        {
            // delete surrounding parenthesis after deleting the last character
            if ( filter.toString().length() - aep.length == 2 && filter.getStartToken() != null
                && filter.getStopToken() != null
                && aep.offset >= filter.getStartToken().getOffset() + filter.getStartToken().getLength()
                && aep.offset + aep.length <= filter.getStopToken().getOffset() )
            {
                if ( filter.toString().length() - aep.length == 2 )
                {
                    aep.offset -= 1;
                    aep.length += 2;
                    aep.caretOffset = aep.offset;
                    aep.shiftsCaret = false;
                }
            }

            // delete closing parenthesis after deleting the opening parenthesis
            if ( filter.toString().length() - aep.length == 1 && filter.getStartToken() != null
                && filter.getStopToken() != null && aep.offset == filter.getStartToken().getOffset() )
            {
                aep.length += 1;
                aep.caretOffset = aep.offset;
                aep.shiftsCaret = false;
            }

        }

        if ( ( aep.length == 0 || aep.length == currentFilter.length() ) && aep.text != null && !"".equals( aep.text ) ) //$NON-NLS-1$
        {
            boolean isNewFilter = aep.text.equals( "(" ); //$NON-NLS-1$
            boolean isNewNestedFilter = aep.text.equals( "&" ) || aep.text.equals( "|" ) || aep.text.equals( "!" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            boolean isSurroundNew = false;
            boolean isSurroundNested = false;
            boolean isSurroundBeforeOtherFilter = false;
            boolean isSurroundAfterOtherFilter = false;
            if ( !Character.isWhitespace( aep.text.charAt( 0 ) )
                && !aep.text.startsWith( "(" ) && !aep.text.endsWith( ")" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                // isSurroundNew
                isSurroundNew = aep.offset == 0;

                // isSurroundNested
                if ( filter.getStartToken() != null
                    && ( filter.getFilterComponent() instanceof LdapAndFilterComponent
                        || filter.getFilterComponent() instanceof LdapOrFilterComponent || filter.getFilterComponent() instanceof LdapNotFilterComponent ) )
                {
                    LdapFilterComponent fc = filter.getFilterComponent();
                    LdapFilter[] filters = fc.getFilters();

                    if ( filters.length == 0 && aep.offset > fc.getStartToken().getOffset() )
                    {
                        // no nested filter yet
                        isSurroundNested = true;
                    }

                    if ( filters.length > 0 && aep.offset > fc.getStartToken().getOffset()
                        && aep.offset < filters[0].getStartToken().getOffset() )
                    {
                        // before first nested filter
                        isSurroundNested = true;
                    }

                    if ( filters.length > 0 && aep.offset > filters[filters.length - 1].getStopToken().getOffset()
                        && aep.offset <= filter.getStopToken().getOffset() )
                    {
                        // after last nested filter
                        isSurroundNested = true;
                    }

                    for ( int i = 0; i < filters.length; i++ )
                    {
                        if ( filters.length > i + 1 )
                        {
                            if ( aep.offset > filters[i].getStopToken().getOffset()
                                && aep.offset <= filters[i + 1].getStopToken().getOffset() )
                            {
                                // between nested filter
                                isSurroundNested = true;
                            }
                        }
                    }
                }

                // isSurroundBeforeOtherFilter
                isSurroundBeforeOtherFilter = filter.getStartToken() != null
                    && aep.offset == filter.getStartToken().getOffset();

                // isSurroundAfterOtherFilter
                isSurroundAfterOtherFilter = filter.getStopToken() != null
                    && aep.offset == filter.getStopToken().getOffset()
                    && ( filter.getFilterComponent() instanceof LdapAndFilterComponent
                        || filter.getFilterComponent() instanceof LdapOrFilterComponent || filter.getFilterComponent() instanceof LdapNotFilterComponent );
            }

            // add opening parenthesis '('
            if ( isSurroundNew || isSurroundNested || isSurroundAfterOtherFilter || isSurroundBeforeOtherFilter )
            {
                aep.text = "(" + aep.text; //$NON-NLS-1$
                aep.caretOffset = aep.offset + aep.text.length();
                aep.shiftsCaret = false;
            }

            // add parenthesis for nested filters
            if ( isNewNestedFilter )
            {
                aep.text = aep.text + "()"; //$NON-NLS-1$
                aep.caretOffset = aep.offset + aep.text.length() - 1;
                aep.shiftsCaret = false;
            }

            // add closing parenthesis ')'
            if ( isNewFilter || isSurroundNew || isSurroundNested || isSurroundAfterOtherFilter
                || isSurroundBeforeOtherFilter )
            {
                if ( balanced == 0 )
                {
                    aep.text = aep.text + ")"; //$NON-NLS-1$
                    if ( aep.caretOffset == -1 )
                    {
                        aep.caretOffset = aep.offset + aep.text.length() - 1;
                        aep.shiftsCaret = false;
                    }
                }
            }

            // translate tab to IDENT_STRING
            if ( aep.text.equals( "\t" ) ) //$NON-NLS-1$
            {
                aep.text = INDENT_STRING;
            }
        }
    }

    // ── CLASS: AutoEditParameters — R2'S DOOR COMMAND PACKET ─────────────────
    // R2-D2 bundles every door command into a compact packet: which door, what
    // operation, where to leave the crew after. Passing it as a single object
    // lets him hand the packet off cleanly without losing any field.
    // We use this simple value-object to pass all the mutable edit parameters
    // through our logic without fighting Eclipse's final DocumentCommand fields.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A simple mutable value object that carries all the parameters of a pending
     * edit. We use it instead of mutating an Eclipse {@link DocumentCommand}
     * directly (some fields are final). Think of it as R2-D2's door-command
     * packet: one clean bundle that gets adjusted as it flows through the
     * auto-edit logic.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public static class AutoEditParameters
    {

        /** The text. */
        public String text;

        /** The offset. */
        public int offset;

        /** The length. */
        public int length;

        /** The caret offset. */
        public int caretOffset;

        /** The shifts caret flag. */
        public boolean shiftsCaret;


        // ── R2 INITIALISES THE COMMAND PACKET ─────────────────────────────────
        // Before sealing any doors, R2 assembles a fresh command packet from the
        // raw signal he intercepted off the Death Star's corridor control bus.
        // He copies every field into the packet so nothing gets lost in transit.
        // We mirror that: snapshot all incoming edit parameters into this object
        // so downstream logic can freely mutate them without touching the original.
        // ───────────────────────────────────────────────────────────────────────
        /**
         * Constructs a new {@code AutoEditParameters} by snapshotting all fields
         * from the incoming edit event. We copy everything so the auto-edit logic
         * can freely mutate these values without touching Eclipse's own objects.
         *
         * @param text         the text the user is inserting (may be empty for a delete)
         * @param offset       character offset in the document where the edit happens
         * @param length       number of characters being replaced (0 for a pure insert)
         * @param caretOffset  where Eclipse should place the caret after the edit;
         *                     -1 means "let Eclipse decide"
         * @param shiftsCaret  whether Eclipse should shift the caret normally after
         *                     applying the command
         */
        public AutoEditParameters( String text, int offset, int length, int caretOffset, boolean shiftsCaret )
        {
            this.text = text;
            this.offset = offset;
            this.length = length;
            this.caretOffset = caretOffset;
            this.shiftsCaret = shiftsCaret;
        }
    }

}
