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
package org.apache.directory.studio.openldap.config.acl.sourceeditor;


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

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPlugin;


// ── CLASS: OpenLdapAclSourceViewerConfiguration — CASSIAN'S TERMINAL SETUP ───
// When Cassian first sits at the stolen Imperial terminal he configures it for
// intelligence work: syntax highlighting is switched on so Imperial keywords
// glow in colour, auto-complete fires up so the rebellion's typing is faster,
// and a formatting pass is enabled so the stolen text can be tidied with one
// button click. This configuration class wires all three services into the
// JFace SourceViewer: presentation reconciler (syntax colouring), content
// assistant (Ctrl+Space completions), and content formatter (the Format button).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SourceViewerConfiguration} for the OpenLDAP ACL source editor.
 * Provides three JFace text editor services:
 * <ol>
 *   <li>Syntax coloring via {@link OpenLdapAclCodeScanner} (keywords, strings, default)</li>
 *   <li>Content assist via {@link OpenLdapContentAssistProcessor} with 500 ms auto-activation</li>
 *   <li>Formatting via {@link OpenLdapAclFormattingStrategy} (inserts newlines before "by")</li>
 * </ol>
 * Think of this class as Cassian configuring his rebel intelligence terminal:
 * colour-coded display, auto-complete suggestions, and a one-click tidy-up pass.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclSourceViewerConfiguration extends SourceViewerConfiguration
{
    // ── Wiring Up the Syntax Highlighter ──────────────────────────────────────
    // Cassian switches on the colour-coded display. The presentation reconciler
    // uses the plugin's shared code scanner to colour keywords, strings, and
    // plain text in different colours.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the presentation reconciler that provides syntax colouring for the
     * ACL source editor. Uses the plugin-shared {@link OpenLdapAclCodeScanner} as
     * the damager/repairer for the default content type, which means changes in any
     * part of the document re-colour the whole default partition.
     *
     * <p>For example — Cassian switching on the colour display:</p>
     * <pre>
     *   IPresentationReconciler rec = config.getPresentationReconciler(viewer);
     *   viewer.configure(config); // installs reconciler automatically
     * </pre>
     *
     * {@inheritDoc}
     *
     * @param sourceViewer  The source viewer to configure.
     * @return              The configured {@link IPresentationReconciler}.
     */
    public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer )
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );

        // Creating the damager/repairer for code
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer( OpenLdapAclEditorPlugin.getDefault()
            .getCodeScanner() );
        reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
        reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );

        return reconciler;
    }


    // ── Wiring Up the Content Assistant ──────────────────────────────────────
    // Cassian enables the auto-complete service: it fires automatically 500 ms
    // after the last keystroke, uses a stacked proposal popup, and is backed by
    // the template-based OpenLdapContentAssistProcessor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content assistant for the ACL source editor. Configures:
     * <ul>
     *   <li>Processor: {@link OpenLdapContentAssistProcessor} (template-based)</li>
     *   <li>Auto-activation: enabled with a 500 ms delay</li>
     *   <li>Proposal popup: stacked below the caret</li>
     *   <li>Context info: above the caret</li>
     * </ul>
     * Uses {@link DialogContentAssistant} instead of the plain {@link ContentAssistant}
     * so that the Ctrl+Space keyboard handler is registered and deregistered
     * automatically on focus gain/loss.
     *
     * {@inheritDoc}
     *
     * @param sourceViewer  The source viewer to configure.
     * @return              The configured {@link IContentAssistant}.
     */
    public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
    {
        ContentAssistant assistant = new DialogContentAssistant();
        IContentAssistProcessor aciContentAssistProcessor = new OpenLdapContentAssistProcessor();

        assistant.setContentAssistProcessor( aciContentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE );
        assistant.enableAutoActivation( true );
        assistant.setAutoActivationDelay( 500 );
        assistant.setProposalPopupOrientation( IContentAssistant.PROPOSAL_STACKED );
        assistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );

        return assistant;
    }


    // ── Wiring Up the Content Formatter ──────────────────────────────────────
    // Cassian enables the Format button: one click and the "by" clauses each
    // get their own line, courtesy of OpenLdapAclFormattingStrategy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content formatter for the ACL source editor. Uses a
     * {@link ContentFormatter} with {@link OpenLdapAclFormattingStrategy} for the
     * default content type. Partition-aware formatting is disabled so the entire
     * document is reformatted in one pass.
     *
     * <p>For example — Cassian's one-click tidy-up pass:</p>
     * <pre>
     *   IContentFormatter fmt = config.getContentFormatter(viewer);
     *   fmt.format(viewer.getDocument(), new Region(0, doc.getLength()));
     *   // → "by" clauses now each on their own line
     * </pre>
     *
     * {@inheritDoc}
     *
     * @param sourceViewer  The source viewer to configure.
     * @return              The configured {@link IContentFormatter}.
     */
    public IContentFormatter getContentFormatter( ISourceViewer sourceViewer )
    {
        ContentFormatter formatter = new ContentFormatter();
        IFormattingStrategy formattingStrategy = new OpenLdapAclFormattingStrategy( sourceViewer );
        formatter.enablePartitionAwareFormatting( false );
        formatter.setFormattingStrategy( formattingStrategy, IDocument.DEFAULT_CONTENT_TYPE );

        return formatter;
    }
}
