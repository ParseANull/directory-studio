/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.dialogs;


import java.util.List;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPlugin;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPluginConstants;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevel;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevelLevelEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevelPrivModifierEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevelPrivilegeEnum;


// ── CLASS: OpenLdapAccessLevelDialog — GRAND MOFF SETTING IMPERIAL CLEARANCES ─
// A Grand Moff sits at his terminal and assigns clearance levels to the who-
// clauses in an ACL. He has two modes: a named level (manage/write/read/…) or
// a custom set of privilege bits (+r, =wrc, -x, …). He may also tick "Self",
// which appends the self modifier. This dialog models exactly that terminal:
// two radio buttons to choose mode, a combo for named level, checkboxes for
// custom privileges, and a modifier group for the operator. The OK button is
// locked until the selection is valid (a real level selected, or at least one
// privilege checked).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal dialog for editing an {@link AclAccessLevel} — the "access level"
 * sub-clause of a who-clause. Offers two modes:
 * <ol>
 *   <li>Named level (manage/write/read/search/compare/auth/disclose/none)</li>
 *   <li>Custom privilege bits with a modifier (=/+/−) and individual privilege
 *       checkboxes (auth/compare/search/read/write)</li>
 * </ol>
 * An optional "Self" checkbox adds the {@code self} modifier before the level.
 * Think of this class as the Grand Moff's clearance assignment terminal — he
 * picks a rank or builds a custom set of clearances, then stamps it on the
 * who-clause.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAccessLevelDialog extends Dialog
{
    /** The array of access levels */
    private Object[] levels = new Object[]
        {
            new AccessLevelComboViewerName(),
            AclAccessLevelLevelEnum.MANAGE,
            AclAccessLevelLevelEnum.WRITE,
            AclAccessLevelLevelEnum.READ,
            AclAccessLevelLevelEnum.SEARCH,
            AclAccessLevelLevelEnum.COMPARE,
            AclAccessLevelLevelEnum.AUTH,
            AclAccessLevelLevelEnum.DISCLOSE,
            AclAccessLevelLevelEnum.NONE,
    };

    /** The access level */
    private AclAccessLevel accessLevel;

    // UI widgets
    private Button okButton;
    private Button selfCheckbox;
    private Button levelRadioButton;
    private ComboViewer levelComboViewer;
    private Button customPrivilegesRadioButton;
    private Button privilegeModifierEqualRadioButton;
    private Button privilegeModifierPlusRadioButton;
    private Button privilegeModifierMinusRadioButton;
    private Button privilegeAuthCheckbox;
    private Button privilegeCompareCheckbox;
    private Button privilegeSearchCheckbox;
    private Button privilegeReadCheckbox;
    private Button privilegeWriteCheckbox;


    // ── Creating the Dialog with an Existing Access Level ─────────────────────
    // The Grand Moff opens the terminal pre-loaded with the current clearance
    // level so the officer can see what is already set before making changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new access level dialog, pre-populated from the given
     * {@link AclAccessLevel}. Uses the active workbench window's shell as the
     * parent.
     *
     * <p>For example — opening the dialog to edit an existing level:</p>
     * <pre>
     *   OpenLdapAccessLevelDialog dlg = new OpenLdapAccessLevelDialog(whoClause.getAccessLevel());
     *   if (dlg.open() == Dialog.OK) {
     *       whoClause.setAccessLevel(dlg.getAccessLevel());
     *   }
     * </pre>
     *
     * @param accessLevel  The access level to edit; may be {@code null} (the dialog
     *                     will create a new one internally in that case).
     */
    public OpenLdapAccessLevelDialog( AclAccessLevel accessLevel )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.accessLevel = accessLevel;
    }


    // ── Configuring the Dialog Shell ───────────────────────────────────────────
    // The terminal window gets its title and icon before it opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to "Access Level Editor" and applies the
     * plugin's editor icon.
     *
     * @param shell  The shell to configure.
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Access Level Editor" );
        shell.setImage( OpenLdapAclEditorPlugin.getDefault().getImage( OpenLdapAclEditorPluginConstants.IMG_EDITOR ) );
    }


    // ── Adding the OK and Cancel Buttons ──────────────────────────────────────
    // The OK button starts disabled — it only lights up once the Grand Moff has
    // made a valid selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the OK and Cancel buttons to the button bar. OK is not the default
     * button — it stays disabled until the selection is valid (either a real
     * named level is chosen or at least one privilege checkbox is checked).
     *
     * @param parent  The button bar composite.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── Building the Dialog Contents and Running Initial Validation ───────────
    // After all widgets are created, run validate() immediately so the OK button
    // starts in the correct enabled/disabled state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates all dialog contents and immediately runs {@link #validate()} so
     * the OK button reflects the initial state of the pre-loaded access level.
     *
     * @param parent  The parent composite.
     * @return        The top-level control.
     */
    protected Control createContents( Composite parent )
    {
        Control control = super.createContents( parent );

        // Validating the dialog
        validate();

        return control;
    }


    // ── Saving the Access Level When the Officer Clicks OK ────────────────────
    // The Grand Moff reads the terminal state and stamps the access level:
    // self flag, named level or custom privileges with a modifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Commits the dialog selections to the {@link AclAccessLevel} model when
     * the user clicks OK. Copies self-flag, named level (or clears it for custom
     * mode), privilege modifier, and privilege bits into the access level bean.
     *
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        // Self
        accessLevel.setSelf( selfCheckbox.getSelection() );

        // Level
        if ( levelRadioButton.getSelection() )
        {
            Object levelSelection = ( ( StructuredSelection ) levelComboViewer.getSelection() ).getFirstElement();
            if ( levelSelection instanceof AclAccessLevelLevelEnum )
            {
                accessLevel.setLevel( ( AclAccessLevelLevelEnum ) levelSelection );
            }
            else
            {
                accessLevel.setLevel( null );
            }
        }
        else
        {
            accessLevel.setLevel( null );
        }

        // Custom privileges
        if ( customPrivilegesRadioButton.getSelection() )
        {
            // Privilege modifier
            accessLevel.setPrivilegeModifier( getPrivilegeModifier() );

            // Privileges
            accessLevel.clearPrivileges();
            addPrivileges();
        }
        else
        {
            accessLevel.setPrivilegeModifier( null );
            accessLevel.clearPrivileges();
        }

        super.okPressed();
    }


    // ── Reading Which Privilege Modifier Is Selected ──────────────────────────
    // Checks which of the three modifier radio buttons is selected and returns
    // the corresponding enum value. Returns null if none is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AclAccessLevelPrivModifierEnum} corresponding to whichever
     * modifier radio button is selected (Equal/Plus/Minus), or {@code null} if none
     * is selected.
     *
     * @return  The selected modifier enum, or {@code null}.
     */
    private AclAccessLevelPrivModifierEnum getPrivilegeModifier()
    {
        if ( privilegeModifierEqualRadioButton.getSelection() )
        {
            return AclAccessLevelPrivModifierEnum.EQUAL;
        }
        else if ( privilegeModifierPlusRadioButton.getSelection() )
        {
            return AclAccessLevelPrivModifierEnum.PLUS;
        }
        else if ( privilegeModifierMinusRadioButton.getSelection() )
        {
            return AclAccessLevelPrivModifierEnum.MINUS;
        }

        return null;
    }


    // ── Reading Which Privilege Checkboxes Are Checked ────────────────────────
    // The Grand Moff reads each checkbox and adds the corresponding privilege
    // to the access level model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds each checked privilege checkbox's corresponding
     * {@link AclAccessLevelPrivilegeEnum} to the access level.
     * Called only when custom privileges mode is active.
     */
    private void addPrivileges()
    {
        // Auth checkbox
        if ( privilegeAuthCheckbox.getSelection() )
        {
            accessLevel.addPrivilege( AclAccessLevelPrivilegeEnum.AUTHENTICATION );
        }

        // Compare checkbox
        if ( privilegeCompareCheckbox.getSelection() )
        {
            accessLevel.addPrivilege( AclAccessLevelPrivilegeEnum.COMPARE );
        }

        // Read checkbox
        if ( privilegeReadCheckbox.getSelection() )
        {
            accessLevel.addPrivilege( AclAccessLevelPrivilegeEnum.READ );
        }

        // Search checkbox
        if ( privilegeSearchCheckbox.getSelection() )
        {
            accessLevel.addPrivilege( AclAccessLevelPrivilegeEnum.SEARCH );
        }

        // Write checkbox
        if ( privilegeWriteCheckbox.getSelection() )
        {
            accessLevel.addPrivilege( AclAccessLevelPrivilegeEnum.WRITE );
        }

    }


    // ── Building the Main Dialog Area ──────────────────────────────────────────
    // The officer assembles the two groups — Self and Level/Privileges — then
    // initialises the widgets from the stored access level and hooks up listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main dialog body. Creates the Self checkbox group and the
     * Level / Custom Privileges group, initialises widget state from the stored
     * {@link AclAccessLevel}, then attaches all change listeners.
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     * @param parent  The parent composite.
     * @return        The top-level control for the dialog body.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        composite.setLayoutData( gd );

        // Creating UI
        createSelfGroup( composite );
        createLevelAndPrivilegesGroup( composite );

        // Initializing the UI with the access level
        initWithAccessLevel();

        // Adding listeners
        addListeners();

        // Setting default focus on the composite
        composite.setFocus();

        applyDialogFont( composite );
        return composite;
    }


    // ── Validating the Current Selection ──────────────────────────────────────
    // The Grand Moff's validation gate: if "Level" mode is active the combo
    // must have a real level selected; if "Custom" mode is active at least one
    // privilege must be checked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the OK button based on the current widget state.
     * In level mode OK is enabled only when a real {@link AclAccessLevelLevelEnum}
     * is selected (not the placeholder). In custom mode OK is enabled when at
     * least one privilege checkbox is checked.
     */
    private void validate()
    {
        if ( levelRadioButton.getSelection() )
        {
            // Getting the selection of the level combo viewer
            Object levelSelection = ( ( StructuredSelection ) levelComboViewer.getSelection() ).getFirstElement();

            // Enabling the OK button only when the selection is a 'real' level
            okButton.setEnabled( levelSelection instanceof AclAccessLevelLevelEnum );
            return;
        }
        else if ( customPrivilegesRadioButton.getSelection() )
        {
            // Enabling the OK button only when at least one of privileges is checked
            okButton.setEnabled( privilegeAuthCheckbox.getSelection() || privilegeCompareCheckbox.getSelection()
                || privilegeSearchCheckbox.getSelection() || privilegeReadCheckbox.getSelection()
                || privilegeWriteCheckbox.getSelection() );
            return;
        }

        // Default case
        okButton.setEnabled( true );
    }


    // ── Creating the Self Checkbox Group ──────────────────────────────────────
    // The "Self" checkbox determines whether the access level applies to the
    // entity itself or to all matching entities.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Self checkbox group at the top of the dialog.
     * When Self is checked, the access level is prefixed with {@code self} in
     * the output ACL text.
     *
     * @param parent  The parent composite.
     */
    private void createSelfGroup( Composite parent )
    {
        Group selfGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        selfGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Self Checkbox
        selfCheckbox = new Button( selfGroup, SWT.CHECK );
        selfCheckbox.setText( "Self" ); //$NON-NLS-1$
    }


    // ── Creating the Level and Privileges Group ────────────────────────────────
    // The main section of the dialog: two radio buttons (Level vs Custom), a
    // combo for the named level, modifier radio buttons, and privilege checkboxes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Access Level and Privilege(s)" group containing the Level/
     * Custom radio buttons, the level combo viewer, modifier radio buttons
     * (Equal/Add/Delete), and individual privilege checkboxes.
     *
     * @param parent  The parent composite.
     */
    private void createLevelAndPrivilegesGroup( Composite parent )
    {
        // Access level and privileges group
        Group levelAndPrivilegesGroup = BaseWidgetUtils.createGroup( parent, "Access Level and Privilege(s)", 1 );
        levelAndPrivilegesGroup.setLayout( new GridLayout( 2, false ) );
        levelAndPrivilegesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Level label and radio button
        levelRadioButton = BaseWidgetUtils.createRadiobutton( levelAndPrivilegesGroup, "Level:", 1 );
        levelComboViewer = new ComboViewer( BaseWidgetUtils.createReadonlyCombo( levelAndPrivilegesGroup,
            new String[0], -1, 1 ) );
        levelComboViewer.setContentProvider( new ArrayContentProvider() );
        levelComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof AccessLevelComboViewerName )
                {
                    return "< Access Level >";
                }
                else if ( element instanceof AclAccessLevelLevelEnum )
                {
                    AclAccessLevelLevelEnum value = ( AclAccessLevelLevelEnum ) element;
                    switch ( value )
                    {
                        case MANAGE:
                            return "Manage";
                        case WRITE:
                            return "Write";
                        case READ:
                            return "Read";
                        case SEARCH:
                            return "Search";
                        case COMPARE:
                            return "Compare";
                        case AUTH:
                            return "Auth";
                        case DISCLOSE:
                            return "Disclose";
                        case NONE:
                            return "None";
                    }
                }

                return super.getText( element );
            }
        } );
        levelComboViewer.setInput( levels );

        // Custom privileges radio button
        customPrivilegesRadioButton = BaseWidgetUtils.createRadiobutton( levelAndPrivilegesGroup,
            "Custom Privilege(s):", 2 );

        // Custom privileges composite
        Composite privilegesTabComposite = BaseWidgetUtils.createColumnContainer( levelAndPrivilegesGroup, 2, 2 );

        // Custom privileges modifier group
        createRadioIndent( privilegesTabComposite );
        Group modifierGroup = BaseWidgetUtils.createGroup( privilegesTabComposite, "Modifier", 1 );
        modifierGroup.setLayout( new GridLayout( 3, true ) );
        modifierGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Custom privileges modifier radio buttons
        privilegeModifierEqualRadioButton = BaseWidgetUtils.createRadiobutton( modifierGroup, "Equal (=)", 1 );
        privilegeModifierEqualRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeModifierPlusRadioButton = BaseWidgetUtils.createRadiobutton( modifierGroup, "Add (+)", 1 );
        privilegeModifierPlusRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeModifierMinusRadioButton = BaseWidgetUtils.createRadiobutton( modifierGroup, "Delete (-)", 1 );
        privilegeModifierMinusRadioButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Custom privileges group
        createRadioIndent( privilegesTabComposite );
        Group privilegesGroup = BaseWidgetUtils.createGroup( privilegesTabComposite, "Privileges", 1 );
        privilegesGroup.setLayout( new GridLayout( 3, true ) );
        privilegesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Custom privileges checkboxes
        privilegeAuthCheckbox = BaseWidgetUtils.createCheckbox( privilegesGroup, "Auth", 1 );
        privilegeAuthCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeCompareCheckbox = BaseWidgetUtils.createCheckbox( privilegesGroup, "Compare", 1 );
        privilegeCompareCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeSearchCheckbox = BaseWidgetUtils.createCheckbox( privilegesGroup, "Search", 1 );
        privilegeSearchCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeReadCheckbox = BaseWidgetUtils.createCheckbox( privilegesGroup, "Read", 1 );
        privilegeReadCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        privilegeWriteCheckbox = BaseWidgetUtils.createCheckbox( privilegesGroup, "Write", 1 );
        privilegeWriteCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Creating an Indentation Label for Nested Radio Buttons ────────────────
    // A small spacer label that indents the modifier and privilege subgroups so
    // they appear visually nested under the "Custom Privilege(s)" radio button.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a blank label with a 10px horizontal indent to the given parent.
     * Used to visually indent the modifier and privilege sub-groups so they appear
     * nested below the "Custom Privilege(s)" radio button.
     *
     * @param parent  The composite to add the indent label to.
     */
    public static void createRadioIndent( Composite parent )
    {
        Label l = new Label( parent, SWT.NONE );
        GridData gd = new GridData();
        gd.horizontalIndent = 10;
        l.setLayoutData( gd );
    }


    // ── Initialising Widgets from the Stored Access Level ─────────────────────
    // When the terminal opens the Grand Moff sees the current clearance settings
    // already selected. If the level is null we create a new empty access level.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises all dialog widgets from the stored {@link AclAccessLevel}. If
     * {@code accessLevel} is {@code null} a fresh one is created. Sets the self
     * checkbox, selects the level combo item or privilege modifier buttons, ticks
     * the appropriate privilege checkboxes, and toggles the radio buttons.
     */
    private void initWithAccessLevel()
    {
        // Creating a boolean to indicate if the level is used (rather than the privileges)
        boolean isLevelUsed = true;

        if ( accessLevel == null )
        {
            // Access level can't be null, creating a new one
            accessLevel = new AclAccessLevel();
        }

        // Self
        selfCheckbox.setSelection( accessLevel.isSelf() );

        // Level
        AclAccessLevelLevelEnum level = accessLevel.getLevel();
        if ( level != null )
        {
            levelComboViewer.setSelection( new StructuredSelection( level ) );
        }
        else
        {
            // Default
            levelComboViewer.setSelection( new StructuredSelection( levels[0] ) );
        }

        // Privilege Modifier
        AclAccessLevelPrivModifierEnum privilegeModifier = accessLevel.getPrivilegeModifier();
        if ( privilegeModifier != null )
        {
            // Level is not used in that case
            isLevelUsed = false;

            privilegeModifierEqualRadioButton.setSelection( AclAccessLevelPrivModifierEnum.EQUAL
                .equals( privilegeModifier ) );
            privilegeModifierPlusRadioButton.setSelection( AclAccessLevelPrivModifierEnum.PLUS
                .equals( privilegeModifier ) );
            privilegeModifierMinusRadioButton.setSelection( AclAccessLevelPrivModifierEnum.MINUS
                .equals( privilegeModifier ) );
        }
        else
        {
            // Default
            privilegeModifierEqualRadioButton.setSelection( true );
            privilegeModifierPlusRadioButton.setSelection( false );
            privilegeModifierMinusRadioButton.setSelection( false );
        }

        // Privileges
        List<AclAccessLevelPrivilegeEnum> privileges = accessLevel.getPrivileges();
        privilegeAuthCheckbox.setSelection( privileges.contains( AclAccessLevelPrivilegeEnum.AUTHENTICATION ) );
        privilegeCompareCheckbox.setSelection( privileges.contains( AclAccessLevelPrivilegeEnum.COMPARE ) );
        privilegeSearchCheckbox.setSelection( privileges.contains( AclAccessLevelPrivilegeEnum.SEARCH ) );
        privilegeReadCheckbox.setSelection( privileges.contains( AclAccessLevelPrivilegeEnum.READ ) );
        privilegeWriteCheckbox.setSelection( privileges.contains( AclAccessLevelPrivilegeEnum.WRITE ) );

        // Setting choice buttons
        levelRadioButton.setSelection( isLevelUsed );
        customPrivilegesRadioButton.setSelection( !isLevelUsed );

        // Setting the enable/disable state for buttons
        setButtonsEnableDisableState();
    }


    // ── Enabling or Disabling Mode-Specific Widgets ────────────────────────────
    // When the officer switches between Level and Custom modes, the irrelevant
    // widgets are disabled so they cannot be accidentally edited.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the level combo and the privilege modifier/checkbox
     * widgets based on whether the Level or Custom Privileges radio button is
     * currently selected. Keeps the two mode groups mutually exclusive in the UI.
     */
    private void setButtonsEnableDisableState()
    {
        boolean isLevelUsed = levelRadioButton.getSelection();

        levelComboViewer.getCombo().setEnabled( isLevelUsed );
        privilegeModifierEqualRadioButton.setEnabled( !isLevelUsed );
        privilegeModifierPlusRadioButton.setEnabled( !isLevelUsed );
        privilegeModifierMinusRadioButton.setEnabled( !isLevelUsed );
        privilegeAuthCheckbox.setEnabled( !isLevelUsed );
        privilegeCompareCheckbox.setEnabled( !isLevelUsed );
        privilegeSearchCheckbox.setEnabled( !isLevelUsed );
        privilegeReadCheckbox.setEnabled( !isLevelUsed );
        privilegeWriteCheckbox.setEnabled( !isLevelUsed );
    }


    // ── Attaching Change Listeners to All Interactive Widgets ─────────────────
    // Every interactive widget fires setButtonsEnableDisableState() or
    // validate() when changed, keeping the OK button in sync at all times.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches selection listeners to the Level/Custom radio buttons, the level
     * combo viewer, and all privilege modifier/checkbox buttons. Each listener
     * calls {@link #setButtonsEnableDisableState()} and/or {@link #validate()} so
     * the OK button tracks validity in real time.
     */
    private void addListeners()
    {
        // Level and custom privileges radio buttons
        SelectionAdapter enableDisableStateAndValidateSelectionAdapter = new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                setButtonsEnableDisableState();
                validate();
            }
        };
        levelRadioButton.addSelectionListener( enableDisableStateAndValidateSelectionAdapter );
        customPrivilegesRadioButton.addSelectionListener( enableDisableStateAndValidateSelectionAdapter );

        // Level combo viewer
        levelComboViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                validate();
            }
        } );

        // Privilege modifier and privileges radio buttons
        SelectionAdapter validateSelectionAdapter = new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        };
        privilegeModifierEqualRadioButton.addSelectionListener( validateSelectionAdapter );
        privilegeModifierPlusRadioButton.addSelectionListener( validateSelectionAdapter );
        privilegeModifierMinusRadioButton.addSelectionListener( validateSelectionAdapter );
        privilegeAuthCheckbox.addSelectionListener( validateSelectionAdapter );
        privilegeCompareCheckbox.addSelectionListener( validateSelectionAdapter );
        privilegeSearchCheckbox.addSelectionListener( validateSelectionAdapter );
        privilegeReadCheckbox.addSelectionListener( validateSelectionAdapter );
        privilegeWriteCheckbox.addSelectionListener( validateSelectionAdapter );
    }


    // ── Retrieving the Updated Access Level After OK ───────────────────────────
    // The who-clause widget retrieves the updated access level from here so it
    // can update its model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AclAccessLevel} after the dialog has been confirmed.
     * The caller should invoke this only after {@code open()} returns {@code OK}.
     *
     * @return  The updated access level bean.
     */
    public AclAccessLevel getAccessLevel()
    {
        return accessLevel;
    }

    // ── Placeholder for the "Select an Access Level" Combo Item ───────────────
    // The first item in the level combo is this placeholder — selecting it keeps
    // OK disabled because it is not a real level enum.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A marker object for the first (placeholder) item in the level combo viewer.
     * Its label is {@code "< Access Level >"} and its presence in the combo
     * selection keeps the OK button disabled.
     */
    private class AccessLevelComboViewerName
    {
    }
}
