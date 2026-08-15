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


import org.apache.directory.studio.schemaeditor.Activator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


// ── CLASS: SchemaSourceViewerConfiguration — BUILDING THE SECOND DEATH STAR ──
// In Return of the Jedi, the Emperor oversees construction of the second Death Star:
// the weapon (the scanner), the targeting system (the damager), the repair drones
// (the repairer) all assembled together into a single devastating weapon system.
// Our configuration does the same: it assembles the PresentationReconciler, wires
// in the SchemaCodeScanner as both the damager and the repairer, and hands the whole
// thing back to the SourceViewer so schema text instantly lights up with colours.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link SourceViewerConfiguration} that wires syntax highlighting into a
 * {@link SchemaSourceViewer}. Specifically, it builds a {@link PresentationReconciler}
 * that uses the plugin's shared {@link SchemaCodeScanner} as both damager and repairer
 * for the default document content type. When the user edits text, the reconciler
 * "damages" the affected region and then "repairs" it by re-running the scanner and
 * applying the correct colours.
 * Think of it as the Death Star construction plan: every component (scanner, damager,
 * repairer) is built and connected in the right order so the whole weapon fires correctly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaSourceViewerConfiguration extends SourceViewerConfiguration
{
    // ── ASSEMBLING THE WEAPON SYSTEM ──────────────────────────────────────────────
    // The Emperor's engineers bolt the targeting laser (scanner) to the power conduit
    // (reconciler) and connect both the damage sensor and the repair beam to it. When
    // the viewer receives text, the reconciler fires the scanner over it and the
    // correct colours appear. The whole assembly is returned to the SourceViewer.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the {@link IPresentationReconciler} that drives syntax
     * highlighting in the schema source viewer. We use a single
     * {@link DefaultDamagerRepairer} backed by the plugin's shared
     * {@link SchemaCodeScanner}, and register it for the default document content type
     * so it fires on all schema text.
     *
     * <p>For example — the Death Star's weapon system comes online:</p>
     * <pre>
     *   PresentationReconciler assembled.
     *   DefaultDamagerRepairer built around SchemaCodeScanner.
     *   Damager and repairer both wired to IDocument.DEFAULT_CONTENT_TYPE.
     *   When a user types in the viewer, the scanner re-tokenises the changed
     *   region and the correct token colours are repainted immediately.
     * </pre>
     *
     * @param sourceViewer  the viewer that will use this reconciler
     * @return              a fully configured {@link IPresentationReconciler} ready for use
     */
    @Override
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );

        // Creating the damager/repairer for code
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer( Activator.getDefault().getSchemaCodeScanner() );
        reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
        reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );

        return reconciler;
    }
}
