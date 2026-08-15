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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.swt.graphics.Color;


// ── CLASS: FilterReconcilingStrategy — R2 RECONCILES CORRUPTED NAV-COMPUTER DATA ──
// Aboard the Millennium Falcon after jumping to lightspeed, R2-D2 discovers
// that the nav-computer data is corrupted — some hyperspace coordinates are
// wrong and others are missing entirely. R2 runs a reconciliation sweep:
// he clears all the old error markers, re-validates every coordinate, and
// plants fresh red squiggle annotations on every corrupted entry so Han can
// see exactly what needs fixing.
// That is what we do here: after every edit, we clear the old error annotations
// from the filter editor, re-examine the parser's error list, and re-plant
// red squiggly underlines under every invalid filter fragment.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Maintains error annotations (red squiggly underlines) in the LDAP filter
 * editor. Eclipse's reconciler calls us periodically after edits; we clear
 * the old annotations, re-examine the current parse result, and add a fresh
 * annotation for each invalid filter or error token.
 * Think of this class as R2-D2 reconciling corrupted nav-computer data: after
 * every jump (edit), he sweeps for bad coordinates and marks them clearly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterReconcilingStrategy implements IReconcilingStrategy
{

    /** The source viewer. */
    private ISourceViewer sourceViewer;

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The paint manager. */
    private PaintManager paintManager;

    // ── R2 INITIALISES HIS NAV-COMPUTER DIAGNOSTIC TOOLS ─────────────────────
    // Before the Falcon enters hyperspace, R2 connects to the nav computer and
    // the ship's display system, and clears his list of known errors so the
    // reconciliation sweep starts from scratch.
    // We store the source viewer and parser, and leave paintManager null so
    // we can lazily set it up on the first setDocument() call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterReconcilingStrategy} connected to the given
     * source viewer and parser. The paint manager (which handles bracket-matching
     * and error-annotation painting) is set up lazily on the first call to
     * {@link #setDocument(IDocument)}.
     *
     * @param sourceViewer  the source viewer hosting the filter editor — we use
     *                      its annotation model to place and remove annotations
     * @param parser        the filter parser whose error state we inspect on
     *                      every reconcile cycle
     */
    public FilterReconcilingStrategy( ISourceViewer sourceViewer, LdapFilterParser parser )
    {
        this.sourceViewer = sourceViewer;
        this.parser = parser;
        this.paintManager = null;
    }


    // ── R2 WIRES UP THE DIAGNOSTIC DISPLAY ───────────────────────────────────
    // The first time the Falcon's nav computer is connected, R2 sets up the
    // error display panel: he creates a new annotation log if one doesn't
    // exist, registers the red-marker painter, and wires up the bracket-matching
    // highlight so corrupted coordinates glow in the display.
    // We lazily create the annotation model and wire up the AnnotationPainter
    // and MatchingCharacterPainter on the first call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Receives the document and performs one-time setup of the annotation and
     * painting infrastructure. If the source viewer doesn't yet have an
     * annotation model, we create one. We then (once only) wire up:
     * <ul>
     *   <li>An {@link AnnotationPainter} that draws red squiggles under error
     *       annotations.</li>
     *   <li>A {@link MatchingCharacterPainter} that highlights matched
     *       parentheses.</li>
     * </ul>
     *
     * @param document  the document to reconcile — stored by the source viewer,
     *                  not by us directly
     */
    public void setDocument( IDocument document )
    {
        if ( sourceViewer.getAnnotationModel() == null )
        {
            IAnnotationModel model = new AnnotationModel();
            sourceViewer.setDocument( sourceViewer.getDocument(), model );
        }

        // add annotation painter
        Color annotationColor = CommonUIPlugin.getDefault().getColor( CommonUIConstants.KEYWORD_1_COLOR );
        if ( paintManager == null && annotationColor != null
            && sourceViewer.getAnnotationModel() instanceof IAnnotationModelExtension )
        {
            AnnotationPainter ap = new AnnotationPainter( sourceViewer, null );
            ap.addAnnotationType( "DEFAULT" ); //$NON-NLS-1$
            ap.setAnnotationTypeColor( "DEFAULT", //$NON-NLS-1$
                CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR ) );
            sourceViewer.getAnnotationModel().addAnnotationModelListener( ap );

            FilterCharacterPairMatcher cpm = new FilterCharacterPairMatcher( sourceViewer, parser );
            MatchingCharacterPainter mcp = new MatchingCharacterPainter( sourceViewer, cpm );
            mcp.setColor( annotationColor );

            paintManager = new PaintManager( sourceViewer );
            paintManager.addPainter( ap );
            paintManager.addPainter( mcp );
        }
    }


    // ── R2 RUNS A TARGETED RECONCILIATION FOR A DIRTY REGION ─────────────────
    // When a specific sector of the nav computer is flagged as dirty, R2 runs
    // a reconciliation pass — but for our purposes the whole filter is always
    // re-examined, so we just delegate to the full-partition reconcile.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse's incremental reconciler when a specific dirty region
     * changes. Since LDAP filter errors can appear anywhere in response to any
     * change, we always reconcile the full document by delegating to
     * {@link #reconcile(IRegion)}.
     *
     * @param dirtyRegion  the region that changed (not used — we reconcile the
     *                     whole document)
     * @param subRegion    a sub-region of the dirty region (not used)
     */
    public void reconcile( DirtyRegion dirtyRegion, IRegion subRegion )
    {
        reconcile( dirtyRegion );
    }


    // ── R2 SWEEPS THE ENTIRE NAV COMPUTER FOR ERRORS ─────────────────────────
    // R2 clears all the old error markers from the display, then walks every
    // entry in the nav computer's error log. For each corrupted coordinate he
    // plants a fresh red annotation at the right position on the display.
    // We clear the annotation model, then re-add annotations for every invalid
    // filter and every ERROR token that doesn't already overlap an existing
    // invalid-filter annotation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Performs a full reconciliation pass. We:
     * <ol>
     *   <li>Remove all existing error annotations from the annotation model.</li>
     *   <li>Add an annotation for every invalid filter in the parse result,
     *       tracking the positions we annotated.</li>
     *   <li>Add an annotation for every ERROR token that doesn't already overlap
     *       one of those positions (to avoid double-marking).</li>
     * </ol>
     * The result is a set of red squiggles under exactly the characters that
     * the parser identified as syntactically invalid.
     *
     * @param partition  the document region to reconcile (not used — we always
     *                   examine the full parse result)
     */
    public void reconcile( IRegion partition )
    {

        LdapFilterToken[] tokens = parser.getModel().getTokens();

        // annotations
        if ( sourceViewer.getAnnotationModel() instanceof IAnnotationModelExtension )
        {
            ( ( IAnnotationModelExtension ) sourceViewer.getAnnotationModel() ).removeAllAnnotations();

            List<Position> positionList = new ArrayList<Position>();

            LdapFilter[] invalidFilters = parser.getModel().getInvalidFilters();
            for ( int i = 0; i < invalidFilters.length; i++ )
            {
                if ( invalidFilters[i].getStartToken() != null )
                {
                    int start = invalidFilters[i].getStartToken().getOffset();
                    int stop = invalidFilters[i].getStopToken() != null ? invalidFilters[i].getStopToken().getOffset()
                        + invalidFilters[i].getStopToken().getLength()
                        : start
                            + invalidFilters[i].getStartToken().getLength();

                    Annotation annotation = new Annotation( "DEFAULT", true, invalidFilters[i].toString() ); //$NON-NLS-1$
                    Position position = new Position( start, stop - start );
                    positionList.add( position );
                    sourceViewer.getAnnotationModel().addAnnotation( annotation, position );
                }
            }

            for ( int i = 0; i < tokens.length; i++ )
            {
                if ( tokens[i].getType() == LdapFilterToken.ERROR )
                {

                    boolean overlaps = false;
                    for ( int k = 0; k < positionList.size(); k++ )
                    {
                        Position pos = positionList.get( k );
                        if ( pos.overlapsWith( tokens[i].getOffset(), tokens[i].getLength() ) )
                        {
                            overlaps = true;
                            break;
                        }
                    }
                    if ( !overlaps )
                    {
                        Annotation annotation = new Annotation( "DEFAULT", true, tokens[i].getValue() ); //$NON-NLS-1$
                        Position position = new Position( tokens[i].getOffset(), tokens[i].getLength() );
                        sourceViewer.getAnnotationModel().addAnnotation( annotation, position );
                    }
                }
            }
        }
    }

}
