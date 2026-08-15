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

package org.apache.directory.studio.ldifeditor.dialogs;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroup;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroupWithAttribute;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetUniversalListener;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifparser.model.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifContentRecord;
import org.apache.directory.studio.ldifparser.model.container.LdifRecord;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: LdifEntryEditorDialog — REBEL BRIEFING-ROOM TABLE ─────────────────
// In the Great Temple the Alliance commanders spread a single record across the
// briefing table and edit it attribute by attribute before filing it away.
// LdifEntryEditorDialog does the same: it converts one LDIF record into an
// in-memory IEntry, lets the user edit it through the standard EntryEditorWidget,
// then converts it back to LDIF when they confirm.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal {@link Dialog} that lets the user view and edit a single LDIF record
 * (either a {@link LdifContentRecord} or a {@link LdifChangeAddRecord}) using
 * the standard {@link EntryEditorWidget}.
 * The record is converted to an in-memory {@link IEntry} on open, edited, then
 * converted back on OK.  If the DN is invalid the dialog shows an error message
 * and provides only OK (read-only mode).
 * Think of this as the Alliance briefing table: spread one communiqué out flat,
 * edit the fields, then fold it back up.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEntryEditorDialog extends Dialog
{

    // ── CONSTANTS ──────────────────────────────────────────────────────────────
    /** Title displayed in the shell title bar. */
    public static final String DIALOG_TITLE = Messages.getString( "LdifEntryEditorDialog.LDIFRecordEditor" ); //$NON-NLS-1$

    /** Maximum dialog width in pixels. */
    public static final int MAX_WIDTH = 450;

    /** Maximum dialog height in pixels. */
    public static final int MAX_HEIGHT = 250;

    /** The browser connection providing schema context. */
    private IBrowserConnection browserConnection;

    /** The original read-only flag so we can restore it on close. */
    private boolean originalReadOnlyFlag;

    /** The LDIF record being edited (updated on OK). */
    private LdifRecord ldifRecord;

    /** In-memory entry derived from the LDIF record. */
    private IEntry entry;

    /** Entry editor widget configuration. */
    private EntryEditorWidgetConfiguration configuration;

    /** Entry editor action group. */
    private EntryEditorWidgetActionGroup actionGroup;

    /** Entry editor main widget. */
    private EntryEditorWidget mainWidget;

    /** Listener that keeps the action handlers consistent with the selection. */
    private EntryEditorWidgetUniversalListener universalListener;

    /** Token used to activate and deactivate shortcuts in the editor */
    private IContextActivation contextActivation;


    // ── CONSTRUCT FOR A CONTENT RECORD ────────────────────────────────────────
    // Cassian spreads the content communiqué on the table and calls in
    // the editing team.
    /**
     * Creates a dialog for editing a {@link LdifContentRecord}.
     *
     * @param parentShell       the parent shell
     * @param browserConnection the browser connection (may be {@code null})
     * @param ldifRecord        the content record to edit
     */
    public LdifEntryEditorDialog( Shell parentShell, IBrowserConnection browserConnection, LdifContentRecord ldifRecord )
    {
        this( parentShell, browserConnection, ldifRecord, null );
    }


    // ── CONSTRUCT FOR A CHANGE-ADD RECORD ─────────────────────────────────────
    // Cassian spreads the change-add communiqué on the table.
    /**
     * Creates a dialog for editing a {@link LdifChangeAddRecord}.
     *
     * @param parentShell       the parent shell
     * @param browserConnection the browser connection (may be {@code null})
     * @param ldifRecord        the change-add record to edit
     */
    public LdifEntryEditorDialog( Shell parentShell, IBrowserConnection browserConnection,
        LdifChangeAddRecord ldifRecord )
    {
        this( parentShell, browserConnection, ldifRecord, null );
    }


    // ── SHARED PRIVATE CONSTRUCTOR ────────────────────────────────────────────
    // The Rebellion's archivist converts the raw communiqué into a structured
    // entry object ready for the editing team.
    /**
     * Shared private constructor: stores the connection, converts the LDIF record
     * to an {@link IEntry}, and falls back to a {@link DummyConnection} if no
     * connection is provided.
     *
     * @param parentShell       the parent shell
     * @param browserConnection the browser connection, or {@code null}
     * @param ldifRecord        the record to edit
     * @param s                 unused discriminator (disambiguates overloads)
     */
    private LdifEntryEditorDialog( Shell parentShell, IBrowserConnection browserConnection, LdifRecord ldifRecord,
        String s )
    {
        super( parentShell );
        setShellStyle( getShellStyle() | SWT.RESIZE );
        this.ldifRecord = ldifRecord;

        this.browserConnection = browserConnection != null ? browserConnection : new DummyConnection(
            Schema.DEFAULT_SCHEMA );

        try
        {
            if ( ldifRecord instanceof LdifContentRecord )
            {
                entry = ModelConverter.ldifContentRecordToEntry( ( LdifContentRecord ) this.ldifRecord,
                    this.browserConnection );
            }
            else if ( ldifRecord instanceof LdifChangeAddRecord )
            {
                entry = ModelConverter.ldifChangeAddRecordToEntry( ( LdifChangeAddRecord ) this.ldifRecord,
                    this.browserConnection );
            }
        }
        catch ( LdapInvalidDnException e )
        {
            entry = null;
        }
    }


    // ── CONFIGURE THE SHELL ───────────────────────────────────────────────────
    // The adjutant stamps the title and icon on the briefing-room door.
    /**
     * Sets the shell title and icon.
     *
     * @param shell  the dialog shell
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
        shell.setImage( LdifEditorActivator.getDefault().getImage( LdifEditorConstants.IMG_BROWSER_LDIFEDITOR ) );
    }


    // ── CREATE THE BUTTON BAR ─────────────────────────────────────────────────
    // If the entry is valid we offer OK and Cancel; if the DN was invalid
    // we can only show OK (no Cancel needed — there is nothing to discard).
    /**
     * Creates OK and (if the entry is valid) Cancel buttons.
     *
     * @param parent  the button bar composite
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        if ( entry != null )
        {
            createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
        }

        getShell().update();
        getShell().layout( true, true );
    }


    // ── HANDLE BUTTON PRESSES ─────────────────────────────────────────────────
    // When the commander says "Confirmed" we convert the edited entry back
    // to an LDIF record and file it.
    /**
     * On OK, converts the edited {@link IEntry} back to the appropriate
     * {@link LdifRecord} subtype.  Cancel discards changes.
     *
     * @param buttonId  the button identifier
     */
    protected void buttonPressed( int buttonId )
    {

        if ( IDialogConstants.OK_ID == buttonId && entry != null )
        {
            if ( this.ldifRecord instanceof LdifContentRecord )
            {
                this.ldifRecord = ModelConverter.entryToLdifContentRecord( entry );
            }
            else if ( this.ldifRecord instanceof LdifChangeAddRecord )
            {
                this.ldifRecord = ModelConverter.entryToLdifChangeAddRecord( entry );
            }
        }

        super.buttonPressed( buttonId );
    }


    // ── OPEN THE DIALOG ───────────────────────────────────────────────────────
    // Before opening the briefing-room door the duty officer sets the
    // connection to read-only so nobody accidentally modifies the live directory.
    /**
     * Saves the original read-only flag and temporarily sets the connection to
     * read-only before opening the dialog.
     */
    public void create()
    {
        super.create();

        if ( browserConnection.getConnection() != null )
        {
            originalReadOnlyFlag = browserConnection.getConnection().isReadOnly();
            browserConnection.getConnection().setReadOnly( true );
        }
    }


    // ── CLOSE THE DIALOG ──────────────────────────────────────────────────────
    // After the briefing is over the connection read-only flag is restored to
    // whatever it was before we walked in.
    /**
     * Closes the dialog, restoring the connection's original read-only state.
     *
     * @return {@code true} if the dialog was closed
     */
    public boolean close()
    {
        boolean returnValue = super.close();
        if ( returnValue )
        {
            this.dispose();

            if ( browserConnection.getConnection() != null )
            {
                browserConnection.getConnection().setReadOnly( originalReadOnlyFlag );
            }
        }
        return returnValue;
    }


    // ── DISPOSE RESOURCES ─────────────────────────────────────────────────────
    // The briefing team packs up, the holographic projector is powered down,
    // and the shortcut context is deactivated.
    /**
     * Disposes the entry editor widget, action group, configuration, and
     * deactivates the keyboard context.
     */
    public void dispose()
    {
        if ( this.configuration != null )
        {
            this.universalListener.dispose();
            this.universalListener = null;
            this.mainWidget.dispose();
            this.mainWidget = null;
            this.actionGroup.deactivateGlobalActionHandlers();
            this.actionGroup.dispose();
            this.actionGroup = null;
            this.configuration.dispose();
            this.configuration = null;

            if ( contextActivation != null )
            {
                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextService.deactivateContext( contextActivation );
                contextActivation = null;
            }
        }
    }


    // ── BUILD THE DIALOG CONTENT AREA ─────────────────────────────────────────
    // The archivist unfolds the entry on the table, wires up all the
    // editing tools, and activates keyboard shortcuts.  If the DN was
    // invalid a plain error label is shown instead.
    /**
     * Creates the dialog content area.
     * If the entry is valid, creates the full {@link EntryEditorWidget} with
     * actions and listeners.  If the DN was invalid, shows an error label.
     *
     * @param parent  the parent composite
     * @return        the created control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        if ( entry == null )
        {
            String message = Messages.getString( "LdifEntryEditorDialog.InvalidDnCantEditEntry" ); //$NON-NLS-1$
            BaseWidgetUtils.createLabel( composite, message, 1 );
        }
        else
        {
            // create configuration
            configuration = new EntryEditorWidgetConfiguration();

            // create main widget
            mainWidget = new EntryEditorWidget( configuration );
            mainWidget.createWidget( composite );
            mainWidget.getViewer().getTree().setFocus();

            // create actions
            actionGroup = new EntryEditorWidgetActionGroupWithAttribute( mainWidget, configuration );
            actionGroup.fillToolBar( mainWidget.getToolBarManager() );
            actionGroup.fillMenu( mainWidget.getMenuManager() );
            actionGroup.fillContextMenu( mainWidget.getContextMenuManager() );
            IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                IContextService.class );
            contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_DIALOGS );
            actionGroup.activateGlobalActionHandlers();

            // hack to activate the action handlers when changing the selection
            mainWidget.getViewer().addSelectionChangedListener( new ISelectionChangedListener()
            {
                public void selectionChanged( SelectionChangedEvent event )
                {
                    actionGroup.deactivateGlobalActionHandlers();
                    actionGroup.activateGlobalActionHandlers();
                }
            } );

            // create the listener
            universalListener = new EntryEditorWidgetUniversalListener( mainWidget.getViewer(), configuration,
                actionGroup, actionGroup.getOpenDefaultEditorAction() );

            universalListener.setInput( entry );
        }

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE EDITED LDIF RECORD ─────────────────────────────────────────
    // Once the briefing is over the archivist hands back the final version
    // of the communiqué.
    /**
     * Returns the (possibly modified) {@link LdifRecord} after the dialog closes.
     *
     * @return the LDIF record
     */
    public LdifRecord getLdifRecord()
    {
        return ldifRecord;
    }

}
