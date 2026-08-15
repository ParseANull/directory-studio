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
package org.apache.directory.studio.openldap.common.ui.dialogs;


import java.text.ParseException;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.UnixPermissions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: UnixPermissionsDialog — IMPERIAL CHECKPOINT SETTING ACCESS LEVELS ─
// Picture a Death Star checkpoint officer configuring who gets access to which
// corridors: Owner, Group, and Others each get their own set of Read/Write/
// Execute passes. The officer can either tick the checkboxes directly or type
// an octal code directly into the scanner — both views stay synchronized at
// all times so neither representation is ever stale.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We present a dialog for editing Unix file-permission values. We show nine
 * checkboxes (read/write/execute for owner, group, and others) alongside an
 * octal text field; both representations stay in sync as the user edits either
 * one. Unix permissions are stored as three octal digits, one per entity:
 *
 * <ul>
 * <li>users</li>
 * <li>group</li>
 * <li>other</li>
 * </ul>
 *
 * with the following permissions:
 *
 * <ul>
 * <li>read</li>
 * <li>write</li>
 * <li>execute</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UnixPermissionsDialog extends Dialog
{
    /** The octal value */
    private String value;

    // UI widgets
    private Button ownerReadCheckbox;
    private Button ownerWriteCheckbox;
    private Button ownerExecuteCheckbox;
    private Button groupReadCheckbox;
    private Button groupWriteCheckbox;
    private Button groupExecuteCheckbox;
    private Button othersReadCheckbox;
    private Button othersWriteCheckbox;
    private Button othersExecuteCheckbox;
    private Text octalNotationText;

    // The octal verifier only accepts values between 0 and 7.
    private VerifyListener octalNotationTextVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            if ( !e.text.matches( "[0-7]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };


    private ModifyListener octalNotationTextModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            resetChecboxSelection();

            try
            {
                UnixPermissions perm = new UnixPermissions( octalNotationText.getText() );

                removeListeners();
                setCheckboxesValue( perm );
                addListeners();
            }
            catch ( ParseException e1 )
            {
                // Nothing to do
            }
        }
    };


    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            UnixPermissions perm = new UnixPermissions();

            perm.setOwnerRead( ownerReadCheckbox.getSelection() );
            perm.setOwnerWrite( ownerWriteCheckbox.getSelection() );
            perm.setOwnerExecute( ownerExecuteCheckbox.getSelection() );
            perm.setGroupRead( groupReadCheckbox.getSelection() );
            perm.setGroupWrite( groupWriteCheckbox.getSelection() );
            perm.setGroupExecute( groupExecuteCheckbox.getSelection() );
            perm.setOthersRead( othersReadCheckbox.getSelection() );
            perm.setOthersWrite( othersWriteCheckbox.getSelection() );
            perm.setOthersExecute( othersExecuteCheckbox.getSelection() );

            removeListeners();
            setOctalValue( perm );
            addListeners();
        }
    };


    // ── CONSTRUCTOR: UnixPermissionsDialog(Shell) — OPENING A BLANK CHECKPOINT ─
    // The checkpoint starts with all access passes revoked (every bit is 0).
    // The officer will configure permissions from scratch. The shell is resizable
    // so the layout can be adjusted as needed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create an UnixPermissionsDialog with no initial value — all checkboxes
     * start unchecked and the octal field defaults to "0000".
     *
     * @param parentShell  the parent shell
     */
    public UnixPermissionsDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // ── CONSTRUCTOR: UnixPermissionsDialog(Shell, String) — RESTORING A SAVED PASS
    // The checkpoint officer reopens a previously saved configuration: the
    // existing octal string is parsed and both the checkboxes and the text field
    // are pre-populated to reflect the stored value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create an UnixPermissionsDialog pre-initialized with an existing
     * permission value. The value may be in octal, decimal, or symbolic format;
     * we parse it during {@link #initialize()}.
     *
     * @param parentShell  the parent shell
     * @param value        the initial permission value string
     */
    public UnixPermissionsDialog( Shell parentShell, String value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.value = value;
    }


    // ── METHOD: configureShell — POSTING THE CHECKPOINT SIGN ─────────────────
    // We stamp the title on the dialog window so the officer knows which
    // checkpoint station they are at before they start configuring access levels.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Unix Permissions Dialog" );
    }


    // ── METHOD: okPressed — ISSUING THE FINAL ACCESS PASS ────────────────────
    // When the officer confirms, we parse the octal text field one last time to
    // produce the canonical four-digit octal string. If the field contains
    // garbage we default to "0000" (no access) rather than crashing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        try
        {
            UnixPermissions perm = new UnixPermissions( octalNotationText.getText() );
            value = perm.getOctalValue();
        }
        catch ( ParseException e )
        {
            value = "0000";
        }

        super.okPressed();
    }


    // ── METHOD: createDialogArea — BUILDING THE CHECKPOINT STATION ────────────
    // We assemble the full dialog interior: the permission checkbox grid,
    // the octal notation field, initial values, and all the listeners that keep
    // both views synchronized. The font is applied last so everything scales
    // consistently.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        createPermissionsArea( composite );
        createOctalNotationArea( composite );

        initialize();

        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // ── METHOD: initialize — LOADING THE SAVED PASS CONFIGURATION ────────────
    // If a prior value was supplied we attempt to parse it and populate both
    // views. A parse failure resets everything to zero — the "all access denied"
    // default — rather than leaving the UI in an ambiguous state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We parse the initial {@code value} string and synchronize both the
     * checkboxes and the octal text field. If parsing fails or the value is
     * null we reset everything to the zero-permission default.
     */
    private void initialize()
    {
        if ( value != null )
        {
            try
            {
                UnixPermissions perm = new UnixPermissions( value );

                setCheckboxesValue( perm );
                setOctalValue( perm );
            }
            catch ( ParseException e )
            {
                resetChecboxSelection();
                setOctalValue( new UnixPermissions() );
            }
        }
        else
        {
            resetChecboxSelection();
            setOctalValue( new UnixPermissions() );
        }
    }


    // ── METHOD: setCheckboxesValue — UPDATING THE ACCESS PASS SWITCHES ────────
    // Each checkbox is set to match the corresponding boolean flag in the
    // UnixPermissions model. We call this both during initialization and
    // whenever the officer types a new octal code.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We synchronize all nine permission checkboxes to the boolean flags in the
     * supplied {@link UnixPermissions} object.
     *
     * @param perm  the Unix permissions model to read from
     */
    private void setCheckboxesValue( UnixPermissions perm )
    {
        ownerReadCheckbox.setSelection( perm.isOwnerRead() );
        ownerWriteCheckbox.setSelection( perm.isOwnerWrite() );
        ownerExecuteCheckbox.setSelection( perm.isOwnerExecute() );
        groupReadCheckbox.setSelection( perm.isGroupRead() );
        groupWriteCheckbox.setSelection( perm.isGroupWrite() );
        groupExecuteCheckbox.setSelection( perm.isGroupExecute() );
        othersReadCheckbox.setSelection( perm.isOthersRead() );
        othersWriteCheckbox.setSelection( perm.isOthersWrite() );
        othersExecuteCheckbox.setSelection( perm.isOthersExecute() );
    }


    // ── METHOD: setOctalValue — UPDATING THE SCANNER READOUT ─────────────────
    // We push the four-digit octal string from the permissions model into the
    // text field so the numeric display always matches the checkbox state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We update the octal notation text field to reflect the value computed by
     * the supplied {@link UnixPermissions} object.
     *
     * @param perm  the Unix permissions model to read from
     */
    private void setOctalValue( UnixPermissions perm )
    {
        octalNotationText.setText( perm.getOctalValue() );
    }


    // ── METHOD: resetChecboxSelection — REVOKING ALL PASSES ──────────────────
    // When the octal field contains an invalid value we clear every checkbox
    // rather than leave the UI in a state that doesn't match any valid
    // permission set.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We uncheck all nine permission checkboxes, effectively setting every
     * permission bit to zero. We call this when an invalid octal value is entered.
     */
    private void resetChecboxSelection()
    {
        ownerReadCheckbox.setSelection( false );
        ownerWriteCheckbox.setSelection( false );
        ownerExecuteCheckbox.setSelection( false );
        groupReadCheckbox.setSelection( false );
        groupWriteCheckbox.setSelection( false );
        groupExecuteCheckbox.setSelection( false );
        othersReadCheckbox.setSelection( false );
        othersWriteCheckbox.setSelection( false );
        othersExecuteCheckbox.setSelection( false );
    }


    // ── METHOD: createPermissionsArea — INSTALLING THE ACCESS PASS PANEL ──────
    // We lay out the nine checkboxes in three labeled rows (Owner, Group,
    // Others) each containing three checkboxes (Read, Write, Execute). This
    // gives the officer an intuitive visual map of who can do what.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the symbolic permission group with three labeled rows of three
     * checkboxes each — Owner, Group, and Others — providing an intuitive
     * interface for setting file permissions.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createPermissionsArea( Composite parent )
    {
        Group symbolicNotationGroup = BaseWidgetUtils.createGroup( parent, "Permissions", 1 );
        symbolicNotationGroup.setLayout( new GridLayout( 2, false ) );

        BaseWidgetUtils.createLabel( symbolicNotationGroup, "Owner:", 1 );
        Composite ownerComposite = BaseWidgetUtils.createColumnContainer( symbolicNotationGroup, 3, true, 1 );
        ownerReadCheckbox = BaseWidgetUtils.createCheckbox( ownerComposite, "Read", 1 );
        ownerWriteCheckbox = BaseWidgetUtils.createCheckbox( ownerComposite, "Write", 1 );
        ownerExecuteCheckbox = BaseWidgetUtils.createCheckbox( ownerComposite, "Execute", 1 );

        BaseWidgetUtils.createLabel( symbolicNotationGroup, "Group:", 1 );
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( symbolicNotationGroup, 3, true, 1 );
        groupReadCheckbox = BaseWidgetUtils.createCheckbox( groupComposite, "Read", 1 );
        groupWriteCheckbox = BaseWidgetUtils.createCheckbox( groupComposite, "Write", 1 );
        groupExecuteCheckbox = BaseWidgetUtils.createCheckbox( groupComposite, "Execute", 1 );

        BaseWidgetUtils.createLabel( symbolicNotationGroup, "Others:", 1 );
        Composite othersComposite = BaseWidgetUtils.createColumnContainer( symbolicNotationGroup, 3, true, 1 );
        othersReadCheckbox = BaseWidgetUtils.createCheckbox( othersComposite, "Read", 1 );
        othersWriteCheckbox = BaseWidgetUtils.createCheckbox( othersComposite, "Write", 1 );
        othersExecuteCheckbox = BaseWidgetUtils.createCheckbox( othersComposite, "Execute", 1 );
    }


    // ── METHOD: createOctalNotationArea — INSTALLING THE SCANNER ─────────────
    // The numeric scanner field shows the four-digit octal code and allows
    // direct entry. A verify listener refuses any character that isn't in the
    // range 0-7 so the officer can't accidentally type an invalid code.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the octal notation area: a labeled group containing a four-
     * character text field pre-populated with "0000". A verify listener
     * restricts input to digits 0-7 only.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createOctalNotationArea( Composite parent )
    {
        Group octalNotationGroup = BaseWidgetUtils.createGroup( parent, "Octal Notation", 1 );
        octalNotationText = BaseWidgetUtils.createText( octalNotationGroup, "0000", 1 );
        octalNotationText.setTextLimit( 4 );
    }


    // ── METHOD: addListeners — CONNECTING ALL THE CHECKPOINT SENSORS ──────────
    // Every checkbox and the octal text field get their listeners attached here.
    // When a checkbox changes we recompute the octal value; when the text field
    // changes we update the checkboxes. We suspend listeners during programmatic
    // updates to avoid infinite loops.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach the selection and modify listeners to all nine checkboxes and
     * the octal text field. We also attach the verify listener to restrict
     * octal input to valid characters.
     */
    private void addListeners()
    {
        ownerReadCheckbox.addSelectionListener( checkboxSelectionListener );
        ownerWriteCheckbox.addSelectionListener( checkboxSelectionListener );
        ownerExecuteCheckbox.addSelectionListener( checkboxSelectionListener );
        groupReadCheckbox.addSelectionListener( checkboxSelectionListener );
        groupWriteCheckbox.addSelectionListener( checkboxSelectionListener );
        groupExecuteCheckbox.addSelectionListener( checkboxSelectionListener );
        othersReadCheckbox.addSelectionListener( checkboxSelectionListener );
        othersWriteCheckbox.addSelectionListener( checkboxSelectionListener );
        othersExecuteCheckbox.addSelectionListener( checkboxSelectionListener );
        octalNotationText.addVerifyListener( octalNotationTextVerifyListener );
        octalNotationText.addModifyListener( octalNotationTextModifyListener );
    }


    // ── METHOD: removeListeners — SUSPENDING THE SENSORS ─────────────────────
    // Before we programmatically update the UI we detach all listeners so our
    // changes don't trigger cascading events. We call addListeners() again
    // immediately after to restore normal operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We detach all selection, modify, and verify listeners from the UI widgets.
     * We call this before programmatic updates to prevent infinite event loops,
     * always re-attaching with {@link #addListeners()} afterwards.
     */
    private void removeListeners()
    {
        ownerReadCheckbox.removeSelectionListener( checkboxSelectionListener );
        ownerWriteCheckbox.removeSelectionListener( checkboxSelectionListener );
        ownerExecuteCheckbox.removeSelectionListener( checkboxSelectionListener );
        groupReadCheckbox.removeSelectionListener( checkboxSelectionListener );
        groupWriteCheckbox.removeSelectionListener( checkboxSelectionListener );
        groupExecuteCheckbox.removeSelectionListener( checkboxSelectionListener );
        othersReadCheckbox.removeSelectionListener( checkboxSelectionListener );
        othersWriteCheckbox.removeSelectionListener( checkboxSelectionListener );
        othersExecuteCheckbox.removeSelectionListener( checkboxSelectionListener );
        octalNotationText.removeVerifyListener( octalNotationTextVerifyListener );
        octalNotationText.removeModifyListener( octalNotationTextModifyListener );
    }


    // ── METHOD: getSymbolicValue — READING THE CHECKPOINT BADGE FORMAT ────────
    // We convert the stored octal value into the traditional Unix symbolic
    // string (e.g., "-rwxr-x---") so callers that need the symbolic form
    // don't have to do the conversion themselves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the symbolic representation of the stored permission value
     * (e.g., "-rwxr-x---"), computed by the {@link UnixPermissions} model. If
     * the stored value cannot be parsed we return the zero-permission symbolic
     * form.
     *
     * @return the symbolic permission string, no file-type prefix
     */
    public String getSymbolicValue()
    {

        UnixPermissions perm = null;
        try
        {
            perm = new UnixPermissions( value );
        }
        catch ( ParseException e )
        {
            perm = new UnixPermissions();
        }

        return perm.getSymbolicValue();
    }


    // ── METHOD: getOctalValue — RETRIEVING THE BADGE CODE ────────────────────
    // After OK is pressed the caller fetches the four-digit octal string here.
    // This is the canonical storage format for Unix permissions in OpenLDAP
    // configuration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the four-digit octal string representation of the confirmed
     * permission value (e.g., "0755"). This is set when OK is pressed.
     *
     * @return the octal permission value
     */
    public String getOctalValue()
    {
        return value;
    }


    // ── METHOD: getDecimalValue — TRANSLATING THE BADGE TO DECIMAL ───────────
    // Some callers prefer to store permissions as a decimal integer. We parse
    // the octal string and return the equivalent base-10 value as a String so
    // the caller doesn't have to deal with Integer.parseInt themselves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We convert the stored octal permission value to its decimal equivalent and
     * return it as a String. For example, octal "0755" becomes "493".
     *
     * @return the decimal representation of the permission value
     */
    public String getDecimalValue()
    {
        return "" + Integer.parseInt( value, 8 );
    }
}
