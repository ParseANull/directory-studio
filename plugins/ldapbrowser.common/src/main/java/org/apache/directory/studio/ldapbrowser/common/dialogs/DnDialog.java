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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: DnDialog — C-3PO COMPOSES A DIPLOMATIC ADDRESS ────────────────────
// C-3PO is fluent in over six million forms of communication, and when diplomacy
// is needed he constructs exactly the right formal address: protocol, recipient,
// and full title — every component precisely ordered.
// A DN (Distinguished Name) is the LDAP equivalent: a fully-qualified, formally
// structured path like "cn=Luke,ou=Rebels,dc=galaxy,dc=org" that uniquely
// locates an entry.  This dialog lets the user browse the directory tree and
// assemble that address, one component at a time — single target or a whole list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that lets the user browse the LDAP directory tree and pick one DN
 * (Distinguished Name) or a list of DNs.  It is used by the DN value editor
 * whenever the user wants to edit a DN-typed attribute — for example, the
 * {@code member} attribute of a group entry.
 * Think of this class as C-3PO constructing a formal diplomatic address string:
 * precise, fully qualified, and absolutely correct in every part.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */

public class DnDialog extends Dialog
{

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The entry widget. */
    private EntryWidget entryWidget;

    /** The connection. */
    private IBrowserConnection connection;

    /** One dn (single-select mode) or multiple DNs (multi-select mode). */
    private Dn[] dns;

    /** True when the dialog operates in multi-select mode. */
    private boolean multiSelect;


    // ── C-3PO PREPARES A SINGLE-RECIPIENT COMMUNIQUÉ ─────────────────────────
    // C-3PO receives a simple assignment: address one specific dignitary, Leia
    // Organa, Senator of Alderaan.  He selects the correct salutation template —
    // single-recipient mode — and pre-fills the address with her known location.
    // We do the same: single-select mode, one optional starting DN, ready for the
    // user to confirm or adjust before accepting.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DnDialog in single-select mode.  The user will see one
     * entry-browse widget and can pick or type exactly one DN.  Use
     * {@link #getDn()} after OK to retrieve the result.
     *
     * <p>For example — C-3PO drafts a single address:</p>
     * <pre>
     *   // Protocol: one recipient, pre-addressed to "Senator Leia Organa"
     *   DnDialog dialog = new DnDialog(shell, "Select Member",
     *       "Pick an entry", connection, existingDn);
     *   if (dialog.open() == OK) { Dn chosen = dialog.getDn(); }
     * </pre>
     *
     * @param parentShell  the shell that will own this dialog window
     * @param title        dialog title shown in the title bar
     * @param description  optional label text above the entry widget; may be {@code null}
     * @param connection   the LDAP connection to browse — C-3PO needs to know which directory to look in
     * @param dn           the DN to pre-populate the widget with; {@code null} means start blank
     */
    public DnDialog( Shell parentShell, String title, String description, IBrowserConnection connection, Dn dn )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = title;
        this.description = description;
        this.connection = connection;
        this.dns = dn != null ? new Dn[]{ dn } : new Dn[0];
        this.multiSelect = false;
    }


    // ── C-3PO PREPARES A MULTI-RECIPIENT BROADCAST ───────────────────────────
    // The Rebel Alliance needs to contact every cell simultaneously: C-3PO
    // switches to multi-recipient mode and pre-loads the existing contact list
    // so nothing already entered is accidentally lost.
    // We switch the EntryWidget to its list form, supporting many DNs at once —
    // useful for editing group membership attributes like {@code member}.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DnDialog in multi-select mode.  The user sees a list widget
     * and can add, remove, or rearrange multiple DNs.  Use {@link #getDns()}
     * after OK to retrieve the full list.  An empty list is a valid result —
     * it means the user cleared all members.
     *
     * <p>For example — C-3PO drafts a multi-recipient communiqué:</p>
     * <pre>
     *   // Protocol: multiple recipients, pre-loaded from existing member list
     *   DnDialog dialog = new DnDialog(shell, "Edit Members",
     *       "Manage group members", connection, existingDns);
     *   if (dialog.open() == OK) { Dn[] chosen = dialog.getDns(); }
     * </pre>
     *
     * @param parentShell  the shell that will own this dialog window
     * @param title        dialog title shown in the title bar
     * @param description  optional label text above the list widget; may be {@code null}
     * @param connection   the LDAP connection to browse
     * @param dns          the initial list of DNs to pre-populate; {@code null} or empty array starts blank
     */
    public DnDialog( Shell parentShell, String title, String description, IBrowserConnection connection, Dn[] dns )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = title;
        this.description = description;
        this.connection = connection;
        this.dns = dns != null ? dns : new Dn[0];
        this.multiSelect = true;
    }


    // ── C-3PO PERSONALISES THE ENVELOPE ──────────────────────────────────────
    // Before sending any message C-3PO writes the sender's header on the
    // envelope: title, crest, and the correct protocol seal.
    // Here we apply the dialog window title and the DN-editor icon so the shell
    // looks like a proper DN-editing tool rather than a generic dialog.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the title text and icon for the dialog shell.  Eclipse calls this
     * early in the window-creation lifecycle, before the content area is built.
     * We set the window title from the constructor parameter and stamp it with
     * the DN-editor icon so users can immediately recognise the dialog's purpose.
     *
     * <p>For example — C-3PO seals the communiqué with the Rebellion's crest:</p>
     * <pre>
     *   shell.setTitle("Select Distinguished Name");
     *   shell.setIcon(DN_EDITOR_ICON);
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure before it is made visible
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( title );
        shell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_DNEDITOR ) );
    }


    // ── C-3PO SEALS AND SENDS THE FINAL DRAFT ────────────────────────────────
    // Once Leia approves the message C-3PO finalises it: he records the exact
    // wording in the outbox and — for single-address messages — saves the
    // address to his personal contact log so he need not re-type it next time.
    // On OK we pull the current DN(s) from the widget and, for single-select,
    // persist the history so the auto-complete works next time.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We grab the current DN(s) from the
     * entry widget and store them so {@link #getDn()} / {@link #getDns()} can
     * return them to the caller.  In single-select mode we also save the dialog
     * settings (browse history) so next time the user opens this dialog they get
     * their recent choices in the drop-down.
     *
     * <p>For example — C-3PO archives the finalised address:</p>
     * <pre>
     *   dns = entryWidget.getDns();           // capture the result
     *   if (!multiSelect) contactLog.save();  // remember it for next time
     *   super.okPressed();
     * </pre>
     */
    protected void okPressed()
    {
        dns = entryWidget.getDns();
        if ( !multiSelect )
        {
            entryWidget.saveDialogSettings();
        }
        super.okPressed();
    }


    // ── C-3PO ACTIVATES THE SEND BUTTON ──────────────────────────────────────
    // After composing the message C-3PO checks: is there a valid recipient? Only
    // then does he light up the "SEND" button — no point sending a blank address.
    // We build the button bar then immediately call {@link #updateWidgets()} so
    // the OK button starts in the right enabled/disabled state.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK / Cancel button bar and then immediately validates the
     * current state so the OK button is correctly enabled or disabled right from
     * the start.  Without the {@link #updateWidgets()} call, the button would
     * briefly be enabled even when the widget contains no valid DN.
     *
     * <p>For example — C-3PO checks the address before enabling SEND:</p>
     * <pre>
     *   Control bar = super.createButtonBar(parent);
     *   validate();   // enable SEND only if address is non-empty
     *   return bar;
     * </pre>
     *
     * @param parent  the composite into which Eclipse wants us to place the button bar
     * @return        the completed button-bar control
     */
    protected Control createButtonBar( Composite parent )
    {
        Control control = super.createButtonBar( parent );
        updateWidgets();
        return control;
    }


    // ── C-3PO LAYS OUT THE DIPLOMATIC DOCUMENT ───────────────────────────────
    // C-3PO spreads out the blank form: a description of what is needed at the
    // top, then the address field(s) below — single-line for one recipient,
    // multi-line list for a whole delegation.
    // We build the composite layout: optional description label, then the
    // EntryWidget configured for single or multi-select mode.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the visual content of the dialog: an optional description label,
     * followed by the entry-browse widget.  In multi-select mode we make the
     * widget taller so the list has room to breathe.  We also wire up a
     * {@link WidgetModifyListener} so the OK button reacts instantly when the
     * user changes the selection.
     *
     * <p>For example — C-3PO lays out the address form:</p>
     * <pre>
     *   label("Select the entry to reference:");
     *   if (multiSelect) {
     *       entryWidget = new EntryWidget(connection, existingDns);  // list form
     *   } else {
     *       entryWidget = new EntryWidget(connection, singleDn);     // single-line form
     *   }
     * </pre>
     *
     * @param parent  the parent composite provided by Eclipse's dialog framework
     * @return        the top-level composite we built with all the widgets inside it
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 3 / 2;
        if ( multiSelect )
        {
            gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        }
        composite.setLayoutData( gd );

        if ( description != null )
        {
            BaseWidgetUtils.createLabel( composite, description, 1 );
        }

        Composite innerComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        if ( multiSelect )
        {
            entryWidget = new EntryWidget( connection, dns );
            innerComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        }
        else
        {
            entryWidget = new EntryWidget( connection, dns.length == 0 ? null : dns[0] );
        }

        entryWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                updateWidgets();
            }
        } );
        entryWidget.createWidget( innerComposite );

        applyDialogFont( composite );
        return composite;
    }


    // ── C-3PO CHECKS WHETHER THE ADDRESS IS DELIVERABLE ──────────────────────
    // C-3PO refuses to seal an envelope with an empty or illegible address —
    // that would just embarrass the Alliance.  He checks: if there is a valid
    // recipient, light up the SEND button; if not, dim it.
    // In single-select mode a non-empty DN must be present; in multi-select
    // an empty list is acceptable (clearing all members is legitimate).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the OK button based on whether the current entry
     * widget state represents a valid, complete DN selection.  In single-select
     * mode the widget must contain a non-empty DN.  In multi-select mode we
     * always keep OK enabled because an empty list is a valid outcome (the user
     * may want to clear the attribute).
     *
     * <p>For example — C-3PO checks deliverability:</p>
     * <pre>
     *   if (multiSelect || entryWidget.getDn() != null && !dn.isEmpty()) {
     *       okButton.setEnabled(true);
     *   } else {
     *       okButton.setEnabled(false);
     *   }
     * </pre>
     */
    private void updateWidgets()
    {
        if ( getButton( IDialogConstants.OK_ID ) != null )
        {
            if ( multiSelect )
            {
                // Always allow confirming in multi-select mode (empty list is valid)
                getButton( IDialogConstants.OK_ID ).setEnabled( true );
            }
            else
            {
                getButton( IDialogConstants.OK_ID ).setEnabled(
                    entryWidget.getDn() != null && !"".equals( entryWidget.getDn().toString() ) ); //$NON-NLS-1$
            }
        }
    }


    // ── C-3PO READS BACK THE FINALISED ADDRESS ───────────────────────────────
    // The rebel courier arrives to collect the message: "Which dignitary did you
    // address this to?"  C-3PO opens the outbox and reads the single recipient.
    // Callers in single-select mode ask for the chosen DN here after the dialog
    // closes — we return the first (and only) element from the internal array.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single DN chosen by the user (single-select mode).  If the
     * user cancelled or the widget was empty, this returns {@code null}.
     * Always call this after the dialog returns {@code OK}.
     *
     * <p>For example — retrieving C-3PO's chosen address:</p>
     * <pre>
     *   Dn selected = dialog.getDn();   // "cn=Luke,ou=Rebels,dc=galaxy,dc=org"
     * </pre>
     *
     * @return  the selected {@link Dn}, or {@code null} if none was chosen
     */
    public Dn getDn()
    {
        return dns.length == 0 ? null : dns[0];
    }


    // ── C-3PO READS BACK THE FULL RECIPIENTS LIST ────────────────────────────
    // The Alliance secretary asks C-3PO: "And who are all the people we need to
    // notify?"  C-3PO hands over the complete, ordered list — every DN he was
    // given or that the user added.
    // Callers in multi-select mode use this to get the complete updated list.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full array of DNs chosen by the user (multi-select mode).
     * This is never {@code null} — if the user cleared all entries it returns
     * an empty array.  Always call this after the dialog returns {@code OK}.
     *
     * <p>For example — retrieving C-3PO's full recipient list:</p>
     * <pre>
     *   Dn[] members = dialog.getDns();   // all selected DNs
     *   attribute.setValues(members);
     * </pre>
     *
     * @return  array of selected {@link Dn} objects; never {@code null}
     */
    public Dn[] getDns()
    {
        return dns;
    }

}
