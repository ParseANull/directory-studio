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

package org.apache.directory.studio.ldifeditor.editor;


import org.apache.directory.studio.ldapbrowser.common.widgets.DialogContentAssistant;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.reconciler.LdifReconcilingStrategy;
import org.apache.directory.studio.ldifeditor.editor.text.LdifAnnotationHover;
import org.apache.directory.studio.ldifeditor.editor.text.LdifAutoEditStrategy;
import org.apache.directory.studio.ldifeditor.editor.text.LdifCompletionProcessor;
import org.apache.directory.studio.ldifeditor.editor.text.LdifDamagerRepairer;
import org.apache.directory.studio.ldifeditor.editor.text.LdifDoubleClickStrategy;
import org.apache.directory.studio.ldifeditor.editor.text.LdifPartitionScanner;
import org.apache.directory.studio.ldifeditor.editor.text.LdifTextHover;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;


// ── CLASS: LdifSourceViewerConfiguration — C-3PO'S ANNOTATION ENGINE ─────────
// C-3PO takes the raw LDIF transmission feed, routes each token to the right
// colour channel, pops up escape-route suggestions when asked, and flags errors
// in the margin — all without touching the raw text.
// LdifSourceViewerConfiguration wires all of those features together:
// syntax highlighting (presentation reconciler + damager-repairer), content
// assist (LdifCompletionProcessor), annotation hover, text hover, the
// incremental reconciler for error annotations, and the LDIF auto-edit strategy
// for line continuation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link SourceViewerConfiguration} for the LDIF editor.
 * Configures syntax highlighting ({@link LdifDamagerRepairer}),
 * content assist ({@link LdifCompletionProcessor} / {@link DialogContentAssistant}),
 * annotation hover ({@link LdifAnnotationHover}), text hover ({@link LdifTextHover}),
 * incremental reconciling ({@link LdifReconcilingStrategy}), double-click strategy
 * ({@link LdifDoubleClickStrategy}), and auto-edit strategy
 * ({@link LdifAutoEditStrategy}).
 * Think of this as C-3PO's complete annotation engine: colours, proposals,
 * hover cards, and error markers — all assembled in one place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifSourceViewerConfiguration extends SourceViewerConfiguration
{
    /** The LDIF editor context. */
    private ILdifEditor editor;

    // Error hover and annotations
    /** Hover that shows annotation messages in the ruler area. */
    private LdifAnnotationHover annotationHover;

    /** Hover that shows annotation messages over the text. */
    private LdifTextHover textHover;

    // Presentation Reconciler (syntax highlight)
    /** The presentation reconciler that drives syntax highlighting. */
    private PresentationReconciler presentationReconciler;

    /** The damager-repairer that applies token colours. */
    private LdifDamagerRepairer damagerRepairer;

    // Content Assistent
    /** Whether content assist is enabled for this configuration. */
    private boolean contentAssistEnabled;

    /** The content assistant (lazy-created). */
    private ContentAssistant contentAssistant;

    /** The completion processor that generates proposals. */
    private IContentAssistProcessor contentAssistProcessor;

    /** Custom double-click strategy. */
    private LdifDoubleClickStrategy doubleClickStrategy;

    // Asynchronous Reconciler (annotations)
    /** The asynchronous reconciler that triggers error annotations. */
    private MonoReconciler reconciler;

    /** The reconciling strategy that computes error annotations. */
    private LdifReconcilingStrategy reconcilingStrategy;

    /** Auto-edit strategies (indent + LDIF line continuation). */
    private IAutoEditStrategy[] autoEditStrategies;


    // ── CONSTRUCT THE CONFIGURATION ───────────────────────────────────────────
    // C-3PO reports to the annotation station and notes whether proposal
    // generation is needed.
    /**
     * Creates a new {@code LdifSourceViewerConfiguration}.
     *
     * @param editor               the LDIF editor context
     * @param contentAssistEnabled {@code true} to enable content assist
     */
    public LdifSourceViewerConfiguration( ILdifEditor editor, boolean contentAssistEnabled )
    {
        super();
        this.editor = editor;

        this.contentAssistEnabled = contentAssistEnabled;
    }


    // ── OVERRIDE A TOKEN COLOUR AT RUNTIME ───────────────────────────────────
    // The syntax-colour preference page calls this to apply a live preview
    // change without restarting the editor.
    /**
     * Overrides the colour and style for the token category identified by {@code key},
     * then forces the presentation reconciler to repaint by updating the damager-repairer.
     *
     * @param key    the preference key (e.g. {@code PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT})
     * @param rgb    the new colour
     * @param style  the new SWT font style
     */
    public void setTextAttribute( String key, RGB rgb, int style )
    {
        damagerRepairer.setTextAttribute( key, rgb, style );
    }


    // ── REPORT THE PARTITIONING ID ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the LDIF partitioning ID from
     * {@link LdifDocumentSetupParticipant#LDIF_PARTITIONING}.</p>
     */
    public String getConfiguredDocumentPartitioning( ISourceViewer sourceViewer )
    {
        return LdifDocumentSetupParticipant.LDIF_PARTITIONING;
    }


    // ── REPORT THE SUPPORTED CONTENT TYPES ───────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link IDocument#DEFAULT_CONTENT_TYPE} and
     * {@link LdifPartitionScanner#LDIF_RECORD}.</p>
     */
    public String[] getConfiguredContentTypes( ISourceViewer sourceViewer )
    {
        return new String[]
            { IDocument.DEFAULT_CONTENT_TYPE, LdifPartitionScanner.LDIF_RECORD };
    }


    // ── RETURN THE DOUBLE-CLICK STRATEGY ─────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@link LdifDoubleClickStrategy} (lazy-created).</p>
     */
    public ITextDoubleClickStrategy getDoubleClickStrategy( ISourceViewer sourceViewer, String contentType )
    {
        if ( this.doubleClickStrategy == null )
        {
            this.doubleClickStrategy = new LdifDoubleClickStrategy();
        }
        return this.doubleClickStrategy;
    }


    // ── BUILD THE PRESENTATION RECONCILER ────────────────────────────────────
    // C-3PO installs one damager-repairer for both the default partition and
    // the LDIF_RECORD partition — the same rule set applies throughout.
    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link PresentationReconciler} with a single
     * {@link LdifDamagerRepairer} registered for both content types.</p>
     */
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
    {

        if ( this.presentationReconciler == null )
        {
            this.presentationReconciler = new PresentationReconciler();
            this.presentationReconciler.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );

            damagerRepairer = new LdifDamagerRepairer( this.editor );

            this.presentationReconciler.setDamager( damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE );
            this.presentationReconciler.setRepairer( damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE );

            this.presentationReconciler.setDamager( damagerRepairer, LdifPartitionScanner.LDIF_RECORD );
            this.presentationReconciler.setRepairer( damagerRepairer, LdifPartitionScanner.LDIF_RECORD );
        }

        return this.presentationReconciler;
    }


    // ── BUILD THE INCREMENTAL RECONCILER ─────────────────────────────────────
    // C-3PO wires up the background reconciler that fires error annotations
    // 500 ms after each keystroke.
    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link MonoReconciler} backed by {@link LdifReconcilingStrategy}
     * with a 500 ms delay (lazy-created).</p>
     */
    public IReconciler getReconciler( ISourceViewer sourceViewer )
    {
        if ( this.reconciler == null )
        {
            this.reconcilingStrategy = new LdifReconcilingStrategy( editor );

            // Reconciler reconciler = new Reconciler();
            // reconciler.setIsIncrementalReconciler(true);
            // reconciler.setReconcilingStrategy(strategy,
            // LdifPartitionScanner.LDIF_RECORD);
            // reconciler.setReconcilingStrategy(strategy,
            // IDocument.DEFAULT_CONTENT_TYPE);
            // reconciler.setProgressMonitor(new NullProgressMonitor());
            // reconciler.setDelay(500);
            // return reconciler;

            this.reconciler = new MonoReconciler( this.reconcilingStrategy, true );
            this.reconciler.setProgressMonitor( new NullProgressMonitor() );
            this.reconciler.setDelay( 500 );
        }

        return this.reconciler;
    }


    // ── BUILD THE CONTENT ASSISTANT ───────────────────────────────────────────
    // R2-D2 pops up an escape-route list on Ctrl+Space if content assist is
    // enabled; returns null otherwise.
    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link DialogContentAssistant} backed by
     * {@link LdifCompletionProcessor} if content assist is enabled.
     * Auto-insert, auto-activation, and delay are read from the preference store.
     * Returns {@code null} if content assist is disabled.</p>
     */
    public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
    {
        if ( this.contentAssistEnabled )
        {
            if ( this.contentAssistant == null )
            {
                // this.contentAssistant = new ContentAssistant();
                this.contentAssistant = new DialogContentAssistant();

                this.contentAssistProcessor = new LdifCompletionProcessor( editor, contentAssistant );
                this.contentAssistant.setContentAssistProcessor( this.contentAssistProcessor,
                    LdifPartitionScanner.LDIF_RECORD );
                this.contentAssistant.setContentAssistProcessor( this.contentAssistProcessor,
                    IDocument.DEFAULT_CONTENT_TYPE );
                this.contentAssistant.setDocumentPartitioning( LdifDocumentSetupParticipant.LDIF_PARTITIONING );

                this.contentAssistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );
                this.contentAssistant.setInformationControlCreator( getInformationControlCreator( sourceViewer ) );

                IPreferenceStore store = LdifEditorActivator.getDefault().getPreferenceStore();
                this.contentAssistant.enableAutoInsert( store
                    .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO ) );
                this.contentAssistant.enableAutoActivation( store
                    .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION ) );
                this.contentAssistant.setAutoActivationDelay( store
                    .getInt( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY ) );
                // this.contentAssistant.enableAutoInsert(true);
                // this.contentAssistant.enableAutoActivation(true);
                // this.contentAssistant.setAutoActivationDelay(100);
            }
            return this.contentAssistant;
        }
        else
        {
            return null;
        }
    }


    // ── RETURN THE ANNOTATION HOVER ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@link LdifAnnotationHover} (lazy-created).</p>
     */
    public IAnnotationHover getAnnotationHover( ISourceViewer sourceViewer )
    {
        if ( this.annotationHover == null )
        {
            this.annotationHover = new LdifAnnotationHover( this.editor );
        }
        return this.annotationHover;
    }


    // ── RETURN THE TEXT HOVER ─────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the {@link LdifTextHover} (lazy-created).</p>
     */
    public ITextHover getTextHover( ISourceViewer sourceViewer, String contentType )
    {
        if ( this.textHover == null )
        {
            this.textHover = new LdifTextHover( this.editor );
        }
        return this.textHover;
    }


    // ── RETURN THE AUTO-EDIT STRATEGIES ──────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns an array of two strategies: a
     * {@link DefaultIndentLineAutoEditStrategy} and an
     * {@link LdifAutoEditStrategy} (lazy-created).</p>
     */
    public IAutoEditStrategy[] getAutoEditStrategies( ISourceViewer sourceViewer, String contentType )
    {
        if ( autoEditStrategies == null )
        {
            this.autoEditStrategies = new IAutoEditStrategy[2];
            this.autoEditStrategies[0] = new DefaultIndentLineAutoEditStrategy();
            this.autoEditStrategies[1] = new LdifAutoEditStrategy( this.editor );
        }

        return autoEditStrategies;
    }
}
