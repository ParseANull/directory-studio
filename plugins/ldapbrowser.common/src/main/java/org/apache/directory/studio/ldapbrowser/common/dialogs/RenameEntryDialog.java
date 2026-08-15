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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.widgets.DnBuilderWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: RenameEntryDialog — OBI-WAN BECOMES BEN KENOBI ────────────────────
// After the fall of the Republic, Obi-Wan Kenobi adopts a new identity: he
// becomes Ben Kenobi of Tatooine.  He is still the same person — same Force
// abilities, same memories — but his name, his RDN in the directory of the
// galaxy, is different now.  The parent path (Tatooine, Outer Rim) stays the
// same; only the first component of his address changes.
// Renaming an LDAP entry is exactly this operation: a ModifyDN request that
// changes the entry's RDN while keeping everything else.  This dialog asks the
// user to type the new RDN and, by default, to delete the old RDN attribute value
// so no trace of "Obi-Wan" remains in the directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that lets the user supply a new RDN (Relative Distinguished Name)
 * for an LDAP entry.  In LDAP terms this is a ModifyDN operation — the entry
 * stays in the same place in the tree but its name changes.  We show the current
 * RDN pre-filled in a {@link DnBuilderWidget} and let the user edit it; the OK
 * button stays disabled until the new RDN is syntactically valid.
 * Think of this class as the moment Obi-Wan adopts the alias "Ben Kenobi":
 * same entity, same parent, but a different identifier at the front of the DN.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameEntryDialog extends Dialog implements WidgetModifyListener
{

    /** The "Delete old Rdn" dialog setting . */
    private static final String DELETE_OLD_RDN_DIALOGSETTING_KEY = RenameEntryDialog.class.getName() + ".deleteOldRdn"; //$NON-NLS-1$

    /** The dialog title. */
    private static final String DIALOG_TITLE = Messages.getString( "RenameEntryDialog.RenameEntry" ); //$NON-NLS-1$

    /** The entry to rename. */
    private IEntry entry;

    /** The dn builder widget. */
    private DnBuilderWidget dnBuilderWidget;

    /** The ok button. */
    private Button okButton;

    /** The new rdn. */
    private Rdn rdn;

    /** The initialization flag */
    private boolean initialized = false;


    // ── OBI-WAN PREPARES HIS NEW IDENTITY ────────────────────────────────────
    // Obi-Wan sits down and decides on a name: something simple, something that
    // won't draw attention.  He starts with a blank alias and will fill in the
    // details once the dialog opens.  He also checks whether his previous order
    // (to delete the old name) is still in effect — and sets the default if
    // nothing is recorded yet.
    // We capture the entry to rename, default rdn to null, and initialise the
    // "delete old RDN" preference if it was never set before.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new RenameEntryDialog for the given entry.  We read (or
     * initialise) the "delete old RDN" preference from the dialog settings so
     * the checkbox reflects the user's last choice — defaulting to {@code true}
     * because keeping the old RDN value rarely makes sense.
     *
     * <p>For example — Obi-Wan prepares to choose a new name:</p>
     * <pre>
     *   RenameEntryDialog dialog = new RenameEntryDialog(shell, entry);
     *   if (dialog.open() == OK) {
     *       Rdn newRdn = dialog.getRdn();
     *   }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param entry        the LDAP entry whose RDN we want to change
     */
    public RenameEntryDialog( Shell parentShell, IEntry entry )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.entry = entry;
        this.rdn = null;

        if ( BrowserCommonActivator.getDefault().getDialogSettings().get( DELETE_OLD_RDN_DIALOGSETTING_KEY ) == null )
        {
            BrowserCommonActivator.getDefault().getDialogSettings().put( DELETE_OLD_RDN_DIALOGSETTING_KEY, true );
        }
    }


    // ── OBI-WAN LABELS THE DOOR OF HIS HUT ───────────────────────────────────
    // Ben Kenobi's hut has a simple sign: "Rename Entry."  Visitors know they
    // are in the right place to change a name, not to search or delete.
    // We set the dialog window title before the shell is shown.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the "Rename Entry" title to the dialog shell.  Eclipse calls this
     * early in the window lifecycle, before the content area is built.
     *
     * <p>For example — Obi-Wan labels his new home:</p>
     * <pre>
     *   shell.setText("Rename Entry");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
    }


    // ── OBI-WAN PUTS AWAY THE NAME GUIDE ─────────────────────────────────────
    // Once Ben Kenobi has committed to the alias he puts away the naming guide
    // and releases any resources that were keeping it open — no loose threads
    // back to his old identity.
    // We remove the widget modify listener and dispose the DnBuilderWidget to
    // avoid leaking SWT resources or event listener references.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up the {@link DnBuilderWidget} when the dialog closes.  We remove
     * the modify listener first so it is not called during disposal, then dispose
     * the widget to release its SWT resources.
     *
     * <p>For example — Obi-Wan puts away the naming guide:</p>
     * <pre>
     *   dnBuilderWidget.removeWidgetModifyListener(this);
     *   dnBuilderWidget.dispose();
     *   return super.close();
     * </pre>
     *
     * @return  {@code true} if the dialog was successfully closed
     */
    public boolean close()
    {
        dnBuilderWidget.removeWidgetModifyListener( this );
        dnBuilderWidget.dispose();
        return super.close();
    }


    // ── OBI-WAN COMMITS TO THE NEW NAME ──────────────────────────────────────
    // Ben Kenobi shakes hands on the new name and it is done — from this point
    // forward the directory will know him only as Ben.
    // On OK we read the final RDN from the widget and let the superclass close
    // the dialog; the caller retrieves the result with {@link #getRdn()}.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We read the new RDN from the
     * {@link DnBuilderWidget} and store it so {@link #getRdn()} can return it
     * to the caller.
     *
     * <p>For example — Obi-Wan commits to Ben Kenobi:</p>
     * <pre>
     *   rdn = dnBuilderWidget.getRdn();   // "cn=Ben Kenobi"
     *   super.okPressed();
     * </pre>
     */
    protected void okPressed()
    {
        rdn = dnBuilderWidget.getRdn();
        super.okPressed();
    }


    // ── TATOOINE COUNCIL PREPARES THE NAME CHANGE SEALS ──────────────────────
    // The Tatooine registry office prepares two seals: one to confirm the name
    // change (OK), one to abandon the process (Cancel).  There is no middle
    // ground when changing an identity.
    // We create OK (default, initially disabled) and Cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons.  OK is the default but starts disabled
     * because the widget has not yet validated the new RDN — it becomes enabled
     * once the user enters a valid RDN in {@link #widgetModified(WidgetModifyEvent)}.
     *
     * <p>For example — the registry prepares the confirmation seals:</p>
     * <pre>
     *   okButton = createButton(OK, disabled);
     *   createButton(CANCEL);
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── OBI-WAN FILLS IN THE NAME CHANGE FORM ────────────────────────────────
    // Obi-Wan sits at the registry clerk's desk and fills in the form: current
    // name at the top, then the new name below — drawn from the full set of
    // attribute types the clerk recognises for this region (the schema).
    // We build the description label and the DnBuilderWidget pre-seeded with the
    // entry's current RDN and all known attribute types for the connection schema.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: a description label, then a
     * {@link DnBuilderWidget} in "RDN-only" mode pre-populated with the entry's
     * current RDN.  All attribute type names from the connection schema are
     * loaded into the widget so the user gets auto-complete.  After the widget
     * is ready we set the {@code initialized} flag so resize-triggered pack
     * events are handled correctly.
     *
     * <p>For example — Obi-Wan fills in the name change form:</p>
     * <pre>
     *   label("Enter the new RDN for the entry:");
     *   String[] allAttrNames = SchemaUtils.getNames(allAtds).toArray();
     *   dnBuilderWidget.setInput(connection, allAttrNames, entry.getRdn(), null);
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 3 / 2;
        composite.setLayoutData( gd );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "RenameEntryDialog.RenameEntryDescription" ), 1 ); //$NON-NLS-1$

        dnBuilderWidget = new DnBuilderWidget( true, false );
        dnBuilderWidget.addWidgetModifyListener( this );
        dnBuilderWidget.createContents( composite );
        Collection<AttributeType> allAtds = SchemaUtils.getAllAttributeTypeDescriptions( entry );
        String[] allAttributeNames = SchemaUtils.getNames( allAtds ).toArray( ArrayUtils.EMPTY_STRING_ARRAY );
        dnBuilderWidget.setInput( entry.getBrowserConnection(), allAttributeNames, entry.getRdn(), null );

        applyDialogFont( composite );

        initialized = true;

        return composite;
    }


    // ── THE CLERK CHECKS WHETHER THE NAME IS VALID AND REPACKS THE FORM ──────
    // Every time Obi-Wan edits a character in the new name, the clerk checks:
    // is this a syntactically valid RDN?  If so, the approval seal lights up.
    // The clerk also repacks the form if it has grown or shrunk so all columns
    // still align — only after the form is fully initialised, though.
    // We enable OK only when the widget produces a non-null RDN and, once
    // initialised, we call shell.pack() to keep the layout tidy.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called whenever the {@link DnBuilderWidget} changes.  We enable OK only
     * when the widget holds a valid (non-null) RDN.  We also call
     * {@link Shell#pack()} after the dialog is fully initialised so the layout
     * adjusts if the widget grows or shrinks — this prevents parts of the widget
     * being clipped when the user expands or changes the RDN structure.
     *
     * <p>For example — the clerk validates and repacks the form:</p>
     * <pre>
     *   okButton.setEnabled(dnBuilderWidget.getRdn() != null);
     *   if (initialized) getShell().pack();  // keep layout tidy
     * </pre>
     *
     * @param event  the widget modification event — we just use it as a trigger
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        if ( okButton != null )
        {
            okButton.setEnabled( dnBuilderWidget.getRdn() != null );
        }

        // Forcing the redraw of the whole dialog
        if ( initialized && ( getShell() != null ) && ( !getShell().isDisposed() ) )
        {
            getShell().pack();
        }
    }


    // ── THE REBELLION ASKS: WHAT IS BEN'S NEW NAME? ──────────────────────────
    // Years later, Luke needs to know the alias — "Who was Obi-Wan Kenobi before
    // he became Ben?"  The registry hands over the finalised RDN.
    // Callers retrieve the chosen new RDN here after the dialog closes with OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new RDN the user entered.  Call this after the dialog closes
     * with OK; returns {@code null} if the user cancelled or did not supply a
     * valid RDN.
     *
     * <p>For example — Luke asks for the alias:</p>
     * <pre>
     *   Rdn newRdn = dialog.getRdn();  // e.g. Rdn("cn=Ben Kenobi")
     *   ldapConnection.rename(entry.getDn(), newRdn, deleteOldRdn);
     * </pre>
     *
     * @return  the new {@link Rdn}, or {@code null} if the dialog was cancelled
     */
    public Rdn getRdn()
    {
        return rdn;
    }

}
