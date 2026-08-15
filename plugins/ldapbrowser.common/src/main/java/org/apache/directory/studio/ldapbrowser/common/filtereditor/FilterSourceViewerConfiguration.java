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


import org.apache.directory.studio.ldapbrowser.common.widgets.DialogContentAssistant;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: FilterSourceViewerConfiguration — R2 CONFIGURES DEEP-SPACE SCANNING ──
// Before the Battle of Yavin, R2-D2 methodically configures every system on the
// X-wing: targeting computer, sensor array, nav computer, life support readouts,
// and the inter-ship comm link. Each sub-system is initialised lazily — R2 only
// powers it up when it's actually needed — and they all share the same underlying
// parser and schema connection.
// We do the same: this class is the central configuration hub for the LDAP filter
// source viewer. Eclipse asks us for each feature (syntax highlighting, content
// assist, formatting, hover, reconciling, auto-edit) and we lazily create and
// return the right sub-component, all wired to the same parser and connection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Configures all features of the LDAP filter source viewer — syntax
 * highlighting, content assist, formatter, hover tooltips, error annotation
 * reconciler, and smart auto-edit. Eclipse calls the {@code get*()} methods on
 * demand, so each sub-component is created lazily on first use.
 * Think of this class as R2-D2 setting up every instrument on Luke's X-wing
 * before the Yavin attack run: one class, all the instruments, each activated
 * only when the pilot needs it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterSourceViewerConfiguration extends SourceViewerConfiguration
{

    /** The current connection, used to retrieve schema information. */
    private IBrowserConnection connection;

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The presentation reconciler, used for syntax highlighting. */
    private PresentationReconciler presentationReconciler;

    /** The damager repairer, used for syntax highlighting. */
    private FilterDamagerRepairer damagerRepairer;

    /** The reconciler, used to maintain error annotations. */
    private MonoReconciler reconciler;

    /** The reconciling strategy, used to maintain error annotations. */
    private FilterReconcilingStrategy reconcilingStrategy;

    /** The text hover, used to display error message tooltips. */
    private FilterTextHover textHover;

    /** The auto edit strategy, used for smart parentesis handling. */
    private FilterAutoEditStrategy[] autoEditStrategies;

    /** The formatter, used to format the filter. */
    private ContentFormatter formatter;

    /** The formatting strategy, used to format the filter. */
    private FilterFormattingStrategy formattingStrategy;

    /** The content assistant, used for content proposals. */
    private DialogContentAssistant contentAssistant;

    /** The content assist processor, used for content proposals. */
    private FilterContentAssistProcessor contentAssistProcessor;


    // ── R2 LOADS THE MISSION BRIEFING ────────────────────────────────────────
    // Before Luke climbs into the cockpit, R2 receives the mission parameters:
    // which parser to use for the filter data and which LDAP server connection
    // to pull schema information from. He stores them for later sub-system init.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new configuration wired to the given parser and LDAP
     * connection. All sub-components are created lazily on their first use;
     * this constructor just stores the two shared dependencies.
     *
     * @param parser      the filter parser shared by all sub-components — it
     *                    holds the current parse model for the filter being edited
     * @param connection  the LDAP browser connection we use to load the schema
     *                    for content assist and hover tooltips; may be
     *                    {@code null} if no server is connected
     */
    public FilterSourceViewerConfiguration( LdapFilterParser parser, IBrowserConnection connection )
    {
        this.parser = parser;
        this.connection = connection;
    }


    // ── R2 SWAPS THE MISSION TARGET ───────────────────────────────────────────
    // Mid-mission, the Rebellion relays new target coordinates to R2. He
    // updates the nav computer and the sensor array simultaneously so both
    // sub-systems work against the new target without delay.
    // We propagate the new connection to the content-assist processor and the
    // hover provider so schema-aware features update immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the active LDAP connection and propagates the new schema to all
     * sub-components that use it (content-assist processor and text hover). Call
     * this whenever the user connects to a different LDAP server or disconnects.
     *
     * @param connection  the new LDAP browser connection, or {@code null} if
     *                    disconnecting
     */
    public void setConnection( IBrowserConnection connection )
    {
        this.connection = connection;
        contentAssistProcessor.setSchema( connection == null ? null : connection.getSchema() );
        textHover.setSchema( connection == null ? null : connection.getSchema() );
    }


    // ── R2 ACTIVATES THE TARGETING COMPUTER ──────────────────────────────────
    // Luke asks for the targeting computer. R2 powers it up if it isn't on yet
    // (creating the damager/repairer first), then returns the live display unit.
    // We lazily create the FilterDamagerRepairer and PresentationReconciler that
    // provide syntax highlighting for the filter editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IPresentationReconciler} that provides syntax
     * highlighting for the filter editor. We lazily create a
     * {@link FilterDamagerRepairer} and a {@link PresentationReconciler} on the
     * first call and cache them for subsequent calls.
     *
     * @param sourceViewer  the source viewer requesting the reconciler
     * @return              the presentation reconciler (never {@code null})
     */
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
    {
        if ( damagerRepairer == null )
        {
            damagerRepairer = new FilterDamagerRepairer( parser );
        }
        if ( presentationReconciler == null )
        {
            presentationReconciler = new PresentationReconciler();
            presentationReconciler.setDamager( damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE );
            presentationReconciler.setRepairer( damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE );
        }
        return presentationReconciler;
    }


    // ── R2 ACTIVATES THE SENSOR TOOLTIP READOUT ──────────────────────────────
    // When Luke hovers over an enemy position, R2 lights up the sensor readout
    // with relevant data. He creates the hover sensor on first use and feeds it
    // the current mission schema so it displays accurate information.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ITextHover} that displays error messages and schema
     * information in a tooltip when the user hovers over a token in the filter.
     * Lazily created and cached.
     *
     * @param sourceViewer  the source viewer (not used beyond API requirement)
     * @param contentType   the content type (not used — the filter editor has
     *                      only one content type)
     * @return              the text hover (never {@code null})
     */
    public ITextHover getTextHover( ISourceViewer sourceViewer, String contentType )
    {
        if ( textHover == null )
        {
            textHover = new FilterTextHover( parser );
            textHover.setSchema( connection == null ? null : connection.getSchema() );
        }
        return textHover;
    }


    // ── R2 ACTIVATES THE ERROR-DETECTION SCANNER ──────────────────────────────
    // R2's long-range scanner continuously monitors all systems for errors and
    // reports them to the pilot. He starts it up on first request and keeps it
    // running in the background.
    // We lazily create the MonoReconciler (backed by FilterReconcilingStrategy)
    // that continuously updates error annotations as the user types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IReconciler} that maintains red-squiggle error
     * annotations as the user edits the filter. Lazily creates a
     * {@link FilterReconcilingStrategy} and a {@link MonoReconciler} on first
     * call.
     *
     * @param sourceViewer  the source viewer requesting the reconciler
     * @return              the reconciler (never {@code null})
     */
    public IReconciler getReconciler( ISourceViewer sourceViewer )
    {
        if ( reconcilingStrategy == null )
        {
            reconcilingStrategy = new FilterReconcilingStrategy( sourceViewer, parser );
        }
        if ( reconciler == null )
        {
            reconciler = new MonoReconciler( reconcilingStrategy, false );
        }
        return reconciler;
    }


    // ── R2 ACTIVATES THE AUTO-PILOT PARENTHESIS SYSTEM ───────────────────────
    // R2's auto-pilot can seal blast doors on its own. He wires it up on first
    // request and returns the active array so Eclipse can call it on every
    // keystroke.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the array of {@link IAutoEditStrategy} instances for the filter
     * editor. We use a single {@link FilterAutoEditStrategy} that handles smart
     * parenthesis insertion and deletion. Lazily created and cached.
     *
     * @param sourceViewer  the source viewer requesting the strategies
     * @param contentType   the content type (not used)
     * @return              a one-element array containing our auto-edit strategy
     */
    public IAutoEditStrategy[] getAutoEditStrategies( ISourceViewer sourceViewer, String contentType )
    {
        if ( autoEditStrategies == null )
        {
            autoEditStrategies = new FilterAutoEditStrategy[]
                { new FilterAutoEditStrategy( parser ) };
        }
        return autoEditStrategies;
    }


    // ── R2 ACTIVATES THE DATA-BANK FORMATTER ─────────────────────────────────
    // When the pilot requests a clean readout of the nav data, R2 activates
    // the formatter — setting it up on first use and handing back the live unit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IContentFormatter} that pretty-prints the LDAP filter.
     * Lazily creates a {@link FilterFormattingStrategy} and a
     * {@link ContentFormatter} on first call.
     *
     * @param sourceViewer  the source viewer requesting the formatter
     * @return              the content formatter (never {@code null})
     */
    public IContentFormatter getContentFormatter( ISourceViewer sourceViewer )
    {
        if ( formattingStrategy == null )
        {
            formattingStrategy = new FilterFormattingStrategy( sourceViewer, parser );
        }
        if ( formatter == null )
        {
            formatter = new ContentFormatter();
            formatter.enablePartitionAwareFormatting( false );
            formatter.setFormattingStrategy( formattingStrategy, IDocument.DEFAULT_CONTENT_TYPE );
        }
        return formatter;
    }


    // ── R2 ACTIVATES THE AUTOCOMPLETE HOLOGRAM PROJECTOR ─────────────────────
    // When the pilot needs route suggestions, R2 fires up the holographic
    // projector and configures it: auto-insert the best route, activate on any
    // relevant key press, keep the display above the entry field. All lazily.
    // We create the FilterContentAssistProcessor and DialogContentAssistant,
    // configure auto-activation and positioning, and cache them for later calls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IContentAssistant} that provides autocomplete proposals
     * for the filter editor. Lazily creates the
     * {@link FilterContentAssistProcessor} and {@link DialogContentAssistant} on
     * first call, configured with auto-activation enabled, a 100 ms delay, and
     * context-information displayed above the cursor.
     *
     * @param sourceViewer  the source viewer requesting content assist
     * @return              the content assistant (never {@code null})
     */
    public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
    {
        if ( contentAssistProcessor == null )
        {
            contentAssistProcessor = new FilterContentAssistProcessor( sourceViewer, parser );
            contentAssistProcessor.setSchema( connection == null ? null : connection.getSchema() );
        }
        if ( contentAssistant == null )
        {
            contentAssistant = new DialogContentAssistant();
            contentAssistant.enableAutoInsert( true );
            contentAssistant.setContentAssistProcessor( contentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE );
            contentAssistant.enableAutoActivation( true );
            contentAssistant.setAutoActivationDelay( 100 );

            contentAssistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );
            contentAssistant.setInformationControlCreator( getInformationControlCreator( sourceViewer ) );

        }
        return contentAssistant;
    }


    // ── R2 CONSTRUCTS THE COCKPIT INFORMATION PANEL ───────────────────────────
    // R2 builds the heads-up information display window that appears whenever
    // a pilot needs more detail about a highlighted target — a wrapping text
    // panel that fits in any shell.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a factory for the information popup control used by content assist
     * and hover. We create a simple {@link DefaultInformationControl} with word
     * wrapping enabled, which is sufficient for LDAP schema descriptions.
     *
     * @param sourceViewer  the source viewer (used to locate the parent shell)
     * @return              an {@link IInformationControlCreator} that produces
     *                      a wrapping text popup
     */
    public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer )
    {
        return new IInformationControlCreator()
        {
            public IInformationControl createInformationControl( Shell parent )
            {
                return new DefaultInformationControl( parent, SWT.WRAP, null );
            }
        };
    }
}
