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


import org.apache.directory.studio.ldapbrowser.common.dialogs.SelectBrowserConnectionDialog;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExecuteLdifRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: ExecuteLdifAction — REBEL OPERATOR SENDS THE COMMUNIQUÉ ───────────
// A Rebel operator finishes composing the LDIF transmission, selects a relay
// station (the LDAP connection), and hits the big red button to send it.
// ExecuteLdifAction is that big red button: if there is no active connection
// the operator is prompted to pick one, then the full document text is handed
// to ExecuteLdifRunnable which ships it to the directory server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link Action} that executes the LDIF content of the active
 * {@link LdifEditor} against an LDAP directory.
 * If the editor has no connection the action prompts the user to select one.
 * Execution is dispatched asynchronously via {@link ExecuteLdifRunnable} and
 * {@link StudioBrowserJob}.
 * Think of this as the Rebel operator pressing the big red "transmit" button.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExecuteLdifAction extends Action
{
    /** The LDIF Editor */
    private LdifEditor editor;


    // ── CONSTRUCT THE EXECUTE ACTION ──────────────────────────────────────────
    // The operator loads up the action with its label, icon, and tooltip —
    // and keeps a reference to the editor so it knows where to get the LDIF.
    /**
     * Creates a new {@code ExecuteLdifAction} bound to {@code editor}.
     *
     * @param editor  the LDIF editor whose content will be executed
     */
    public ExecuteLdifAction( LdifEditor editor )
    {
        super(
            Messages.getString( "ExecuteLdifAction.ExecuteLDIF" ), LdifEditorActivator.getDefault().getImageDescriptor( LdifEditorConstants.IMG_EXECUTE ) ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "ExecuteLdifAction.ExecuteLDIF" ) ); //$NON-NLS-1$
        this.editor = editor;
    }


    // ── EXECUTE THE LDIF DOCUMENT ─────────────────────────────────────────────
    // The operator checks whether there is an active relay station.  If not
    // they open the connection-picker dialog.  Once a connection is confirmed
    // the LDIF text is packaged into a runnable and shipped off as a background job.
    /**
     * {@inheritDoc}
     *
     * <p>If the editor has no connection, opens a {@link SelectBrowserConnectionDialog}
     * and binds the selection.  Then packages the editor's LDIF text into an
     * {@link ExecuteLdifRunnable} and executes it via {@link StudioBrowserJob}.</p>
     */
    public void run()
    {
        IBrowserConnection connection = editor.getConnection();

        // Checking if we already have a connection
        if ( connection == null )
        {
            // Requesting the user to select a connection
            SelectBrowserConnectionDialog dialog = new SelectBrowserConnectionDialog( editor.getSite().getShell(),
                Messages.getString( "ExecuteLdifAction.SelectConnection" ), null ); //$NON-NLS-1$
            if ( dialog.open() == SelectBrowserConnectionDialog.OK )
            {
                connection = dialog.getSelectedBrowserConnection();

                if ( connection != null )
                {
                    editor.setConnection( connection, true );
                }
            }

            // Checking a second time if we  have a connection
            if ( connection == null )
            {
                return;
            }
        }

        String ldif = editor.getLdifModel().toRawString();

        IPreferenceStore preferenceStore = LdifEditorActivator.getDefault().getPreferenceStore();
        boolean updateIfEntryExistsButton = preferenceStore
            .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_UPDATEIFENTRYEXISTS );
        boolean continueOnErrorButton = preferenceStore
            .getBoolean( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_CONTINUEONERROR );

        ExecuteLdifRunnable runnable = new ExecuteLdifRunnable( connection, ldif, updateIfEntryExistsButton,
            continueOnErrorButton );
        StudioBrowserJob job = new StudioBrowserJob( runnable );
        job.execute();
    }


    // ── CHECK IF ENABLED ──────────────────────────────────────────────────────
    // The big red button is only live when the editor is open.
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} when an editor is bound.</p>
     */
    public boolean isEnabled()
    {
        return editor != null;
    }
}
