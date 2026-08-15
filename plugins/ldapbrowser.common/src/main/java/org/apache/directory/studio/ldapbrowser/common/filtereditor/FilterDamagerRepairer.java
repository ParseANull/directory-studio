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


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;


// ── CLASS: FilterDamagerRepairer — R2 REPAIRS THE SHIELD GENERATOR ────────────
// During the Battle of Endor, R2-D2 gets blasted by an AT-ST shot and his
// dome smokes — but moments later he's back on his feet, running his self-repair
// sequence to restore power to the shield generator. Every circuit gets
// re-assessed and coloured back to its correct status.
// That is exactly what we do here: whenever the filter text changes (the
// "damage"), we re-parse the whole filter and re-apply syntax highlighting
// colours (the "repair") to every token so the display stays correct.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides syntax highlighting for the LDAP filter editor by implementing
 * both Eclipse's {@link IPresentationDamager} and {@link IPresentationRepairer}
 * interfaces. When any part of the filter text changes, we mark the whole
 * partition as damaged and then re-paint it from scratch by re-parsing the
 * filter and assigning a {@link StyleRange} to every token.
 * Think of this class as R2-D2 repairing the shield generator: after every hit
 * (document edit), he runs a full diagnostic and restores everything to its
 * correct colour-coded state.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterDamagerRepairer implements IPresentationDamager, IPresentationRepairer
{

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The document. */
    private IDocument document;


    // ── R2 POWERS UP HIS REPAIR TOOLKIT ───────────────────────────────────────
    // At the start of the Battle of Endor, R2 runs a quick self-test, stows
    // his fusion welder, and clears his damage log so he's ready to respond
    // the moment the first laser bolt strikes.
    // We store the parser and initialise the document reference to null —
    // Eclipse will call setDocument() before we need it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterDamagerRepairer} holding a reference to the
     * filter parser. We don't have a document yet at construction time — Eclipse
     * provides it via {@link #setDocument(IDocument)} before the first edit.
     *
     * @param parser  the filter parser we use to tokenise the filter string and
     *                determine each token's type and colour
     */
    public FilterDamagerRepairer( LdapFilterParser parser )
    {
        this.parser = parser;
        this.document = null;
    }


    // ── R2 CONNECTS TO THE SHIELD GENERATOR'S POWER BUS ──────────────────────
    // On Endor, R2 plugs into the shield generator's main power bus so he can
    // monitor and repair any circuit in real time as the battle unfolds around him.
    // We store the document reference so we can call {@code document.get()} when
    // Eclipse asks us to re-paint.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Receives the document to monitor. Eclipse calls this during initialisation,
     * before any edits occur, so we have a reference to the document content when
     * we need to re-parse it during repair.
     *
     * @param document  the document backing the filter editor
     */
    public void setDocument( IDocument document )
    {
        this.document = document;
    }


    // ── R2 REPORTS THE FULL BLAST ZONE ────────────────────────────────────────
    // When an AT-ST bolt hits the shield generator, R2 doesn't try to isolate
    // which exact circuit was damaged — LDAP filter syntax is too interleaved
    // for that. He reports the entire generator section as the damage zone so
    // the repair cycle covers everything.
    // We return the entire partition (the full filter) as the damage region
    // because any token change can affect syntax highlighting anywhere.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the region of the document that needs to be re-coloured after the
     * given document event. For LDAP filters we always declare the entire
     * partition as damaged — a change anywhere (e.g. adding a {@code &} near the
     * start) can affect how every following token is coloured.
     *
     * @param partition                        the content partition to assess
     * @param event                            the document change that caused the damage
     * @param documentPartitioningChanged      whether the partitioning itself changed
     * @return                                 {@code partition} — the whole partition
     *                                         is always re-painted
     */
    public IRegion getDamageRegion( ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged )
    {
        return partition;
    }


    // ── R2 RUNS THE FULL REPAIR CYCLE ─────────────────────────────────────────
    // R2 activates his fusion welder, re-assesses every circuit in the shield
    // generator, and applies the correct power level to each one — parenthesis
    // circuits get bold, AND/OR/NOT circuits get keyword blue, attribute names
    // get attribute-type orange, and so on.
    // We re-parse the filter, iterate every token, and add a StyleRange that
    // paints each token in its designated colour and weight.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Re-parses the filter string and applies a {@link StyleRange} for every
     * token, painting the editor with full syntax highlighting. Eclipse calls
     * this after the damage region has been determined — we clear the existing
     * presentation and rebuild it from scratch.
     * <p>
     * Token-to-colour mapping:
     * <ul>
     *   <li>Parentheses → bold default colour</li>
     *   <li>{@code &}, {@code |}, {@code !} → bold keyword colour</li>
     *   <li>Filter operators ({@code =}, {@code <=}, etc.) → bold separator colour</li>
     *   <li>Attribute names → attribute-type colour</li>
     *   <li>Values → value colour</li>
     *   <li>Everything else → default colour</li>
     * </ul>
     * </p>
     *
     * @param presentation  the Eclipse presentation object we populate with
     *                      {@link StyleRange} objects
     * @param damage        the damaged region to repair (currently not used —
     *                      we always repaint the whole filter)
     */
    public void createPresentation( TextPresentation presentation, ITypedRegion damage )
    {
        TextAttribute DEFAULT_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.DEFAULT_COLOR) );
        TextAttribute AND_OR_NOT_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.KEYWORD_1_COLOR), null, SWT.BOLD );
        TextAttribute ATTRIBUTE_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.ATTRIBUTE_TYPE_COLOR) );
        TextAttribute FILTER_TYPE_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.SEPARATOR_COLOR), null, SWT.BOLD );
        TextAttribute VALUE_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.VALUE_COLOR) );
        TextAttribute PARENTHESIS_TEXT_ATTRIBUTE = new TextAttribute( getColor(CommonUIConstants.DEFAULT_COLOR), null, SWT.BOLD );

        // parse the filter
        parser.parse( this.document.get() );

        // get tokens
        LdapFilterToken[] tokens = parser.getModel().getTokens();

        // syntax highlighting
        for ( int i = 0; i < tokens.length; i++ )
        {
            switch ( tokens[i].getType() )
            {
                case LdapFilterToken.LPAR:
                case LdapFilterToken.RPAR:
                    this.addStyleRange( presentation, tokens[i], PARENTHESIS_TEXT_ATTRIBUTE );
                    break;
                case LdapFilterToken.AND:
                case LdapFilterToken.OR:
                case LdapFilterToken.NOT:
                    this.addStyleRange( presentation, tokens[i], AND_OR_NOT_TEXT_ATTRIBUTE );
                    break;
                case LdapFilterToken.EQUAL:
                case LdapFilterToken.GREATER:
                case LdapFilterToken.LESS:
                case LdapFilterToken.APROX:
                case LdapFilterToken.PRESENT:
                case LdapFilterToken.SUBSTRING:
                case LdapFilterToken.EXTENSIBLE_DNATTR_COLON:
                case LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID_COLON:
                case LdapFilterToken.EXTENSIBLE_EQUALS_COLON:
                    this.addStyleRange( presentation, tokens[i], FILTER_TYPE_TEXT_ATTRIBUTE );
                    break;
                case LdapFilterToken.ATTRIBUTE:
                case LdapFilterToken.EXTENSIBLE_ATTRIBUTE:
                case LdapFilterToken.EXTENSIBLE_DNATTR:
                case LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID:
                    this.addStyleRange( presentation, tokens[i], ATTRIBUTE_TEXT_ATTRIBUTE );
                    break;
                case LdapFilterToken.VALUE:
                    this.addStyleRange( presentation, tokens[i], VALUE_TEXT_ATTRIBUTE );
                    break;
                default:
                    this.addStyleRange( presentation, tokens[i], DEFAULT_TEXT_ATTRIBUTE );
            }
        }
    }


    private Color getColor( String name )
    {
        return CommonUIPlugin.getDefault().getColor( name );
    }


    // ── R2 WELDS A CIRCUIT BACK TO ITS CORRECT COLOUR ────────────────────────
    // For each individual circuit in the shield generator, R2 applies the right
    // power level — he checks the circuit length first; a zero-length circuit
    // doesn't need any work and gets skipped to avoid wasting energy.
    // We only add a StyleRange if the token has actual length; zero-length tokens
    // have no visible characters and adding a range for them would be a no-op
    // that could confuse Eclipse's presentation model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link StyleRange} to {@code presentation} for the given token,
     * using the colours and font style from {@code textAttribute}. We skip tokens
     * of zero length because they have no visible characters to paint.
     *
     * @param presentation   the Eclipse presentation object to update
     * @param token          the filter token whose offset and length define the
     *                       character range to style
     * @param textAttribute  the colour and style to apply to the range
     */
    private void addStyleRange( TextPresentation presentation, LdapFilterToken token, TextAttribute textAttribute )
    {
        if ( token.getLength() > 0 )
        {
            StyleRange range = new StyleRange( token.getOffset(), token.getLength(), textAttribute.getForeground(),
                textAttribute.getBackground(), textAttribute.getStyle() );
            presentation.addStyleRange( range );
        }
    }

}
