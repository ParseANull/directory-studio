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


import org.apache.directory.studio.aciitemeditor.Activator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


// ── CLASS: ACISourceViewerConfiguration — THE ISB COMMAND CONSOLE SETUP ─────
// When Grand Moff Tarkin sits at his command console, an orderly plugs in the
// colour-coded display, the autocomplete terminal, and the document-formatter
// module — making the console fully operational.
// ACISourceViewerConfiguration is that setup pass: it plugs syntax highlighting,
// content assist, and formatting into the ACI SourceViewer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link SourceViewerConfiguration} for the ACI source editor.
 * Wires together three subsystems for the source viewer:
 * <ul>
 *   <li>{@link IPresentationReconciler} — drives syntax colouring via {@link ACICodeScanner}</li>
 *   <li>{@link IContentAssistant} — provides snippet proposals via {@link ACIContentAssistProcessor}</li>
 *   <li>{@link IContentFormatter} — pretty-prints ACI text via {@link ACIFormattingStrategy}</li>
 * </ul>
 * Think of this class as the ISB command console setup: one class plugs all
 * three modules in so the editor is fully operational.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACISourceViewerConfiguration extends SourceViewerConfiguration
{
    // ── INSTALL SYNTAX HIGHLIGHTING ───────────────────────────────────────────
    // The orderly plugs in the colour-coded display: a PresentationReconciler
    // backed by ACICodeScanner so every keyword, grant, and deny glows in its
    // designated colour the moment the cursor moves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );

        // Creating the damager/repairer for code
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer( Activator.getDefault().getAciCodeScanner() );
        reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
        reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );

        return reconciler;
    }


    // ── INSTALL CONTENT ASSIST ────────────────────────────────────────────────
    // The orderly plugs in the autocomplete terminal: a ContentAssistant wired
    // to ACIContentAssistProcessor so the user gets snippet proposals after
    // 500 ms idle time, and the popup is stacked below the cursor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
    {
        //        ContentAssistant assistant = new ContentAssistant();
        ContentAssistant assistant = new DialogContentAssistant();
        IContentAssistProcessor aciContentAssistProcessor = new ACIContentAssistProcessor();

        assistant.setContentAssistProcessor( aciContentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE );
        assistant.enableAutoActivation( true );
        assistant.setAutoActivationDelay( 500 );
        assistant.setProposalPopupOrientation( IContentAssistant.PROPOSAL_STACKED );
        assistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );

        return assistant;
    }


    // ── INSTALL DOCUMENT FORMATTER ────────────────────────────────────────────
    // The orderly plugs in the document-formatter module: a ContentFormatter
    // backed by ACIFormattingStrategy so the Format command re-indents the whole
    // ACI text in one pass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public IContentFormatter getContentFormatter( ISourceViewer sourceViewer )
    {
        ContentFormatter formatter = new ContentFormatter();
        IFormattingStrategy formattingStrategy = new ACIFormattingStrategy( sourceViewer );
        formatter.enablePartitionAwareFormatting( false );
        formatter.setFormattingStrategy( formattingStrategy, IDocument.DEFAULT_CONTENT_TYPE );
        return formatter;
    }
}
