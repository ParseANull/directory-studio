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

package org.apache.directory.studio.ldifeditor.editor.actions;


import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: FormatLdifDocumentAction — REBEL ARCHIVIST TIDIES THE WHOLE FILE ───
// The Rebel archivist takes the entire transmission bundle, runs it through the
// standard formatter, and hands it back with every record consistently indented
// and wrapped — without scrolling away from the current view position.
// FormatLdifDocumentAction replaces the entire IDocument content with the
// re-formatted LDIF model text, then restores the source viewer's top-line index
// so the view does not jump.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that re-formats the entire LDIF document using the standard LDIF
 * format parameters.
 * Replaces the full document text with
 * {@link org.apache.directory.studio.ldifparser.model.LdifFile#toFormattedString},
 * then restores the source-viewer scroll position.
 * Think of this as the Rebel archivist auto-indenting every record in the file
 * at once.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FormatLdifDocumentAction extends AbstractLdifAction
{

    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code FormatLdifDocumentAction} bound to {@code editor}.
     *
     * @param editor  the LDIF editor this action operates on
     */
    public FormatLdifDocumentAction( LdifEditor editor )
    {
        super( Messages.getString( "FormatLdifDocumentAction.FormatDocument" ), editor ); //$NON-NLS-1$
    }


    // ── FORMAT THE WHOLE DOCUMENT ─────────────────────────────────────────────
    // The archivist asks the model to serialise itself, replaces the document,
    // and scrolls back to where the operator was looking.
    /**
     * {@inheritDoc}
     *
     * <p>Replaces the full document text with the formatted LDIF model and
     * restores the source viewer's top-line index.</p>
     */
    protected void doRun()
    {
        IDocument document = editor.getDocumentProvider().getDocument( editor.getEditorInput() );
        ISourceViewer sourceViewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
        int topIndex = sourceViewer.getTopIndex();
        document.set( super.getLdifModel().toFormattedString( Utils.getLdifFormatParameters() ) );
        sourceViewer.setTopIndex( topIndex );
    }


    // ── ALWAYS ENABLED ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>No-op — this action is always enabled.</p>
     */
    public void update()
    {
    }

}
