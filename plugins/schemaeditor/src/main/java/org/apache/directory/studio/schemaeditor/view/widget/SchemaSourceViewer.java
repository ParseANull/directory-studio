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
package org.apache.directory.studio.schemaeditor.view.widget;


import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: SchemaSourceViewer — VADER'S SUIT WRAPS THE MAN INSIDE ────────────
// Darth Vader's armoured suit does not replace Anakin — it wraps him, amplifying
// his presence and giving him capabilities the man alone could not have. Without
// the suit, he cannot breathe; without Anakin, the suit is empty. Our class does
// the same: Eclipse's generic SourceViewer is the man inside, and SchemaSourceViewer
// is the suit — a thin shell that takes any SourceViewer and immediately dresses it
// in schema-aware configuration (syntax highlighting, token colouring, all of it).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A specialised Eclipse {@link SourceViewer} pre-configured for displaying LDAP
 * schema definition file content with syntax highlighting. We extend {@code SourceViewer}
 * and immediately call {@code configure()} in every constructor so callers never have
 * to remember to wire the configuration themselves.
 * Think of it as Vader's suit: the base SourceViewer is the raw capability, and
 * {@link SchemaSourceViewerConfiguration} is the armour that gives it schema superpowers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaSourceViewer extends SourceViewer
{
    // ── SUITING UP: FULL VIEWER WITH OVERVIEW RULER ───────────────────────────────
    // Vader's suit includes the full life-support system: chest panel, breathing
    // apparatus, overview readout. This constructor gives us the full rig — a vertical
    // ruler on the left, an overview ruler on the right (if enabled), and the schema
    // configuration applied immediately so the viewer is ready to display schema text.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a fully-featured SchemaSourceViewer with a vertical ruler and an
     * optional overview ruler, then immediately applies {@link SchemaSourceViewerConfiguration}
     * for syntax highlighting. Use this when embedding the viewer in an editor that
     * needs the full set of Eclipse ruler decorations.
     *
     * <p>For example — Vader's suit with full life support:</p>
     * <pre>
     *   new SchemaSourceViewer(parent, vertRuler, overviewRuler, true, SWT.V_SCROLL)
     *   // Schema text is displayed with syntax highlighting from the first character.
     * </pre>
     *
     * @param parent                    the SWT parent composite
     * @param verticalRuler             the vertical ruler widget shown on the left margin
     * @param overviewRuler             the overview ruler shown on the right margin
     * @param showAnnotationsOverview   {@code true} to make the overview ruler visible
     * @param styles                    SWT style bits forwarded to the underlying StyledText
     */
    public SchemaSourceViewer( Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
        boolean showAnnotationsOverview, int styles )
    {
        super( parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles );
        this.configure( new SchemaSourceViewerConfiguration() );
    }


    // ── SUITING UP: LIGHTWEIGHT VIEWER WITHOUT OVERVIEW RULER ────────────────────
    // Sometimes Vader skips the full ceremonial armour and just wears the basics —
    // no overview readout, just the essential breathing apparatus. This constructor
    // creates a simpler viewer with only a vertical ruler, still fully configured for
    // schema syntax highlighting, suitable for lightweight read-only display panels.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a lightweight SchemaSourceViewer with only a vertical ruler (no
     * overview ruler), then immediately applies {@link SchemaSourceViewerConfiguration}.
     * Use this variant for read-only schema preview panels where the full editor
     * decoration is unnecessary overhead.
     *
     * <p>For example — Vader in minimal kit:</p>
     * <pre>
     *   new SchemaSourceViewer(parent, ruler, SWT.READ_ONLY | SWT.V_SCROLL)
     *   // Schema text shown with syntax highlighting, no annotation overview.
     * </pre>
     *
     * @param parent  the SWT parent composite
     * @param ruler   the vertical ruler widget shown on the left margin
     * @param styles  SWT style bits forwarded to the underlying StyledText
     */
    public SchemaSourceViewer( Composite parent, IVerticalRuler ruler, int styles )
    {
        super( parent, ruler, styles );
        this.configure( new SchemaSourceViewerConfiguration() );
    }
}
