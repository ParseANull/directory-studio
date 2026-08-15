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
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.dialogs.LdifEntryEditorDialog;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.eclipse.jface.text.IDocument;


// ── CLASS: EditLdifRecordAction — REBEL OFFICER EDITS THE COMMUNIQUÉ ──────────
// The officer pulls up a complete LDIF record in the briefing-room dialog,
// edits its attributes in table form, confirms the changes, and the console
// splices the formatted replacement text back into exactly the right position
// in the transmission file.
// EditLdifRecordAction orchestrates that splice: it deactivates global action
// handlers while the dialog is open (so keyboard shortcuts do not fire in the
// background), then writes the confirmed record back to the IDocument.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens the {@link LdifEntryEditorDialog} for the currently
 * selected {@link LdifContentRecord} or {@link LdifChangeAddRecord}, then
 * replaces that record's text in the document with the edited version if the
 * user confirmed.
 * Think of this as the Rebel officer editing a full communiqué in the
 * briefing-room table and patching the changes back into the transmission file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditLdifRecordAction extends AbstractLdifAction
{

    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditLdifRecordAction} bound to {@code editor}.
     *
     * @param editor  the LDIF editor this action operates on
     */
    public EditLdifRecordAction( LdifEditor editor )
    {
        super( Messages.getString( "EditLdifRecordAction.EditRecord" ), editor ); //$NON-NLS-1$
        super.setActionDefinitionId( LdifEditorConstants.ACTION_ID_EDIT_RECORD );
    }


    // ── OPEN THE DIALOG AND SPLICE BACK ───────────────────────────────────────
    // The officer opens the briefing-room dialog.  On OK the old record text
    // is excised and the new formatted text is sewn back in.
    /**
     * {@inheritDoc}
     *
     * <p>Opens the {@link LdifEntryEditorDialog} for the selected content or
     * change-add record.  On {@code OK}, writes the formatted replacement record
     * back to the document at the original offset.</p>
     */
    protected void doRun()
    {
        LdifContainer[] containers = getSelectedLdifContainers();
        if ( containers.length == 1
            && ( containers[0] instanceof LdifContentRecord || containers[0] instanceof LdifChangeAddRecord ) )
        {

            LdifContainer container = containers[0];

            LdifEntryEditorDialog dialog = null;
            if ( container instanceof LdifContentRecord )
            {
                dialog = new LdifEntryEditorDialog( editor.getEditorSite().getShell(), editor.getConnection(),
                    ( LdifContentRecord ) container );
            }
            else
            {
                dialog = new LdifEntryEditorDialog( editor.getEditorSite().getShell(), editor.getConnection(),
                    ( LdifChangeAddRecord ) container );
            }

            editor.deactivateGlobalActionHandlers();
            if ( dialog.open() == LdifEntryEditorDialog.OK )
            {
                LdifRecord record = dialog.getLdifRecord();

                IDocument document = editor.getDocumentProvider().getDocument( editor.getEditorInput() );
                String old = document.get();
                StringBuffer sb = new StringBuffer();
                sb.append( old.substring( 0, container.getOffset() ) );
                sb.append( record.toFormattedString( Utils.getLdifFormatParameters() ) );
                sb.append( old.substring( container.getOffset() + container.getLength(), old.length() ) );
                document.set( sb.toString() );
            }
            editor.activateGlobalActionHandlers();
        }
    }


    // ── RECOMPUTE ENABLEMENT ──────────────────────────────────────────────────
    // Enabled only when exactly one content or change-add record is selected.
    /**
     * {@inheritDoc}
     *
     * <p>Enabled when exactly one container is selected and it is a
     * {@link LdifContentRecord} or {@link LdifChangeAddRecord}.</p>
     */
    public void update()
    {
        LdifContainer[] containers = getSelectedLdifContainers();
        super.setEnabled( containers.length == 1
            && ( containers[0] instanceof LdifContentRecord || containers[0] instanceof LdifChangeAddRecord ) );
    }

}
