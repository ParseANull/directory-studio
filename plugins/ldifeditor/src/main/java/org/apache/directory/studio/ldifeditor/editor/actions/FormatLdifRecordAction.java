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
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: FormatLdifRecordAction — REBEL ARCHIVIST TIDIES SELECTED RECORDS ───
// The archivist only needs to tidy the records the operator has highlighted,
// not the whole file.  They excise those records from the text, re-format them
// in place, stitch the document back together, and restore the scroll position.
// FormatLdifRecordAction handles exactly that surgical replacement: it formats
// only the selected containers, leaving the rest of the document untouched.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that re-formats only the currently selected {@link LdifRecord}s,
 * leaving the rest of the document untouched.
 * Enabled only when every selected container is a {@link LdifRecord}.
 * Think of this as the Rebel archivist tidying individual paragraphs rather
 * than the whole file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FormatLdifRecordAction extends AbstractLdifAction
{

    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code FormatLdifRecordAction} bound to {@code editor}.
     *
     * @param editor  the LDIF editor this action operates on
     */
    public FormatLdifRecordAction( LdifEditor editor )
    {
        super( Messages.getString( "FormatLdifRecordAction.FormatRecord" ), editor ); //$NON-NLS-1$
    }


    // ── FORMAT SELECTED RECORDS ───────────────────────────────────────────────
    // The archivist excises the selected records, formats each in turn, and
    // stitches everything back together.
    /**
     * {@inheritDoc}
     *
     * <p>Replaces the text span from the first to the last selected container with
     * each container's re-formatted string, then restores the source-viewer
     * top-line index.</p>
     */
    protected void doRun()
    {

        LdifContainer[] containers = super.getSelectedLdifContainers();
        if ( containers.length > 0 )
        {
            IDocument document = editor.getDocumentProvider().getDocument( editor.getEditorInput() );
            String old = document.get();
            StringBuffer sb = new StringBuffer();
            sb.append( old.substring( 0, containers[0].getOffset() ) );

            for ( int i = 0; i < containers.length; i++ )
            {
                LdifContainer container = containers[i];
                sb.append( container.toFormattedString( Utils.getLdifFormatParameters() ) );
            }

            sb.append( old.substring( containers[containers.length - 1].getOffset()
                + containers[containers.length - 1].getLength(), old.length() ) );

            ISourceViewer sourceViewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
            int topIndex = sourceViewer.getTopIndex();
            document.set( sb.toString() );
            sourceViewer.setTopIndex( topIndex );
        }
    }


    // ── RECOMPUTE ENABLEMENT ──────────────────────────────────────────────────
    // Disabled if any selected container is not a proper record (e.g. a comment
    // or error container).
    /**
     * {@inheritDoc}
     *
     * <p>Enabled only when all selected containers are {@link LdifRecord}
     * instances.</p>
     */
    public void update()
    {
        LdifContainer[] ldifContainers = super.getSelectedLdifContainers();
        for ( int i = 0; i < ldifContainers.length; i++ )
        {
            LdifContainer container = ldifContainers[i];
            if ( !( container instanceof LdifRecord ) )
            {
                super.setEnabled( false );
                return;
            }
        }

        super.setEnabled( true );
    }

}
