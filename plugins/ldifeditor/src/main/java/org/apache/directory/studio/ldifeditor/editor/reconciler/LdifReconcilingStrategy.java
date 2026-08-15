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

package org.apache.directory.studio.ldifeditor.editor.reconciler;


import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifeditor.editor.LdifOutlinePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


// ── CLASS: LdifReconcilingStrategy — REBEL BASE PERIMETER SWEEP ───────────────
// After every burst of typing the Rebel base runs a perimeter sweep: the
// outline index is refreshed, error markers are re-stamped, and the folding
// doors are re-positioned.
// LdifReconcilingStrategy is that sweep: it delegates to
// LdifAnnotationUpdater and LdifFoldingRegionUpdater, and nudges the outline
// page — all dispatched on the SWT event thread so the UI stays responsive.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IReconcilingStrategy} and {@link IReconcilingStrategyExtension}
 * for the LDIF editor.
 * All three entry points ({@link #reconcile(DirtyRegion,IRegion)},
 * {@link #reconcile(IRegion)}, {@link #initialReconcile()}) funnel into a
 * single private {@code reconcile()} that dispatches
 * {@link LdifAnnotationUpdater#updateAnnotations()},
 * {@link LdifFoldingRegionUpdater#updateFoldingRegions()}, and a
 * {@link LdifOutlinePage#refresh()} on the SWT event thread.
 * Think of this as the base perimeter sweep run 500 ms after every keystroke.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension
{

    /** The LDIF editor this strategy watches. */
    private ILdifEditor editor;

    // private IDocument document;
    // private IProgressMonitor progressMonitor;

    /** Updates code-folding annotations after each reconcile. */
    private LdifFoldingRegionUpdater foldingUpdater;

    /** Updates error annotations after each reconcile. */
    private LdifAnnotationUpdater annotationUpdater;


    // ── CONSTRUCT AND WIRE UP UPDATERS ────────────────────────────────────────
    /**
     * Creates a new reconciling strategy for {@code editor}, constructing the
     * annotation and folding region updaters.
     *
     * @param editor  the LDIF editor to reconcile
     */
    public LdifReconcilingStrategy( ILdifEditor editor )
    {
        this.editor = editor;

        this.annotationUpdater = new LdifAnnotationUpdater( this.editor );
        this.foldingUpdater = new LdifFoldingRegionUpdater( this.editor );

    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    /**
     * Disposes both the annotation and folding region updaters.
     */
    public void dispose()
    {
        this.annotationUpdater.dispose();
        this.foldingUpdater.dispose();
    }


    // ── DOCUMENT CHANGED ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — the document reference is not stored.</p>
     */
    public void setDocument( IDocument document )
    {
        // this.document = document;
    }


    // ── PROGRESS MONITOR ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — progress reporting is not used.</p>
     */
    public void setProgressMonitor( IProgressMonitor monitor )
    {
        // this.progressMonitor = monitor;
    }


    // ── INCREMENTAL RECONCILE ─────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #reconcile()} — a full sweep is always performed.</p>
     */
    public void reconcile( DirtyRegion dirtyRegion, IRegion subRegion )
    {
        reconcile();
    }


    // ── PARTITION RECONCILE ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #reconcile()} — a full sweep is always performed.</p>
     */
    public void reconcile( IRegion partition )
    {
        reconcile();
    }


    // ── INITIAL RECONCILE ─────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #reconcile()} to perform the first full sweep when
     * the editor opens.</p>
     */
    public void initialReconcile()
    {
        reconcile();
    }


    // ── PRIVATE COORDINATOR ───────────────────────────────────────────────────
    /**
     * The single reconcile entry point — delegates to
     * {@link #notifyEnvironment()}.
     */
    private void reconcile()
    {
        notifyEnvironment();
    }


    // ── DISPATCH UI UPDATES ───────────────────────────────────────────────────
    // All three consumers — outline page, annotation updater, folding updater —
    // must run on the SWT event thread, so we dispatch a single Runnable.
    /**
     * Dispatches outline refresh, annotation update, and folding region update
     * asynchronously on the SWT event thread.
     */
    private void notifyEnvironment()
    {

        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {

                // notify outline
                IContentOutlinePage outline = ( IContentOutlinePage ) editor.getAdapter( IContentOutlinePage.class );
                if ( outline instanceof LdifOutlinePage )
                {
                    ( ( LdifOutlinePage ) outline ).refresh();
                }

                // notify annotation updater
                annotationUpdater.updateAnnotations();

                // notify folding updater
                foldingUpdater.updateFoldingRegions();

            }
        } );
    }

}
