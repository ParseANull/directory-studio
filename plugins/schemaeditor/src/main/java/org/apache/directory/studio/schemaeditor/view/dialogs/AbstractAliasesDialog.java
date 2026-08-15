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

package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


// ── CLASS: AbstractAliasesDialog — LANDO RUNNING CLOUD CITY ──────────────────
// Lando Calrissian runs Cloud City's enormous bureaucracy: issuing permits,
// revoking them, renaming residents, and making sure no two citizens claim the
// same ID badge. Every rule he enforces keeps the city from descending into chaos.
// We do the same here — we let users add, edit, and remove aliases on an LDAP
// schema element, while making sure no alias collides with one already in the registry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base dialog for managing the aliases list on an LDAP schema element (attribute type or object class).
 * Aliases are the alternative human-readable names for a schema element — for example, an attribute
 * might be known as both "commonName" and "cn". This dialog provides the full add/edit/remove UI
 * backed by live duplicate-detection and name-format validation.
 * Think of this class as Lando's permit office in Cloud City: it holds the master list of names,
 * lets you issue new ones, revoke old ones, and blocks any name that's already taken somewhere
 * else in the galaxy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAliasesDialog extends Dialog
{
    /** The aliases List */
    private List<String> initialLowerCasedAliases = new ArrayList<String>();
    private List<String> aliases = new ArrayList<String>();
    private List<String> lowerCasedAliases = new ArrayList<String>();

    /** The listener used to override the listerner on the RETURN key */
    private Listener returnKeyListener = new Listener()
    {
        public void handleEvent( Event event )
        {
            if ( event.detail == SWT.TRAVERSE_RETURN )
            {
                event.detail = SWT.TRAVERSE_TAB_NEXT;
                closeTableEditor();
            }
        }
    };

    // UI Fields
    private Table aliasesTable;
    private TableEditor tableEditor;
    private Button addButton;
    private Button editButton;
    private Button removeButton;
    private Composite errorComposite;
    private Image errorImage;
    private Label errorLabel;


    // ── Lando Opens the Registry Books ───────────────────────────────────────
    // When Lando takes over Cloud City, the first thing he does is open the
    // resident registry and copy every existing permit into his own ledger.
    // He needs that snapshot so he can later tell whether a new applicant
    // is asking for a name that was already on the books when he arrived.
    // We do exactly that here — we snapshot the incoming alias list in both
    // original and lower-cased form so our duplicate-check has a baseline.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog, taking an initial snapshot of the current aliases list.
     * We keep a lower-cased copy alongside the display copy because alias comparisons
     * in LDAP are case-insensitive — "cn" and "CN" are the same alias.
     * The snapshot of initial aliases lets us exempt them from the "already taken"
     * check: if "cn" was already on the element, the user shouldn't get an error
     * just for leaving it there.
     *
     * @param aliases  the existing aliases on the schema element, or {@code null} if there are none yet
     */
    public AbstractAliasesDialog( List<String> aliases )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        if ( aliases != null )
        {
            for ( String alias : aliases )
            {
                initialLowerCasedAliases.add( Strings.toLowerCase( alias ) );
                this.aliases.add( alias );
                lowerCasedAliases.add( Strings.toLowerCase( alias ) );
            }
        }
    }


    // ── Lando Builds the Permit Office Counter ───────────────────────────────
    // Lando doesn't just open the registry — he builds a proper reception desk
    // with a big display board listing all current permits, buttons for issuing
    // new ones, editing existing ones, or revoking them, and a red warning light
    // that flashes when someone tries to register a name already in use.
    // This method assembles that entire UI: the table of aliases, the three action
    // buttons, and the error composite that stays hidden until something goes wrong.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the visual content area of the dialog: the aliases table, Add/Edit/Remove buttons,
     * and an error strip that appears when a duplicate or invalid alias is detected.
     * SWT's {@link TableEditor} is used so the user can type directly inside a table row
     * rather than in a separate pop-up text field — it feels more like a spreadsheet.
     * After building all the widgets we populate the table from our list, wire up the
     * listeners, and run the first alias-check pass.
     *
     * @param parent  the parent composite supplied by the JFace Dialog framework
     * @return        the top-level composite we just built, handed back to JFace to embed in the dialog
     */
    protected Control createDialogArea( Composite parent )
    {
        // Creating the composite
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Aliases Label
        Label aliasesLabel = new Label( composite, SWT.NONE );
        aliasesLabel.setText( Messages.getString( "AbstractAliasesDialog.Aliases" ) ); //$NON-NLS-1$
        aliasesLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, true, 2, 1 ) );

        // Aliases Table
        aliasesTable = new Table( composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
            | SWT.HIDE_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        gridData.heightHint = 90;
        gridData.minimumHeight = 90;
        gridData.widthHint = 200;
        gridData.minimumWidth = 200;
        aliasesTable.setLayoutData( gridData );

        // Aliases Table Editor
        tableEditor = new TableEditor( aliasesTable );
        tableEditor.horizontalAlignment = SWT.LEFT;
        tableEditor.grabHorizontal = true;
        tableEditor.minimumWidth = 200;

        // Add Button
        addButton = new Button( composite, SWT.PUSH );
        addButton.setText( Messages.getString( "AbstractAliasesDialog.Add" ) ); //$NON-NLS-1$
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );

        // Edit Button
        editButton = new Button( composite, SWT.PUSH );
        editButton.setText( Messages.getString( "AbstractAliasesDialog.Edit" ) ); //$NON-NLS-1$
        editButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        editButton.setEnabled( false );

        // Remove Button
        removeButton = new Button( composite, SWT.PUSH );
        removeButton.setText( Messages.getString( "AbstractAliasesDialog.Remove" ) ); //$NON-NLS-1$
        removeButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        removeButton.setEnabled( false );

        // Error Composite
        errorComposite = new Composite( composite, SWT.NONE );
        errorComposite.setLayout( new GridLayout( 2, false ) );
        errorComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        errorComposite.setVisible( false );

        // Error Image
        errorImage = PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK );
        Label label = new Label( errorComposite, SWT.NONE );
        label.setImage( errorImage );
        label.setSize( 16, 16 );

        // Error Label
        errorLabel = new Label( errorComposite, SWT.NONE );
        errorLabel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        errorLabel.setText( getAliasAlreadyExistsErrorMessage() );

        // Filling the Table with the given aliases
        fillAliasesTable();

        // Listeners initialization
        initListeners();

        // Checking the aliases
        checkAliases();

        return composite;
    }


    // ── Lando Posts the Board of Current Permits ─────────────────────────────
    // Every morning Lando clears the bulletin board and re-posts the current
    // permit holders in alphabetical order so everyone in Cloud City knows
    // exactly who holds what name today.
    // We do the same: clear the SWT table, then add one row per alias in our list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clears the SWT aliases table and refills it from our in-memory {@code aliases} list.
     * We call this after any mutation — add, edit, or remove — to keep the visual table
     * in sync with the data we'll return when the user hits OK.
     */
    private void fillAliasesTable()
    {
        aliasesTable.removeAll();
        aliasesTable.setItemCount( 0 );
        for ( String alias : aliases )
        {
            TableItem newItem = new TableItem( aliasesTable, SWT.NONE );
            newItem.setText( alias );
        }
    }


    // ── Lando Assigns Staff to Every Desk ────────────────────────────────────
    // Lando doesn't just build the permit office and walk away — he stations
    // guards at the door (key listeners), clerks at the counter (selection and
    // double-click listeners), and a supervisor who handles the right-click menu.
    // Each person knows exactly what to do when an event comes in.
    // This method registers all the SWT event listeners on the table and buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires up all SWT event listeners for the dialog's interactive widgets.
     * Specifically: keyboard Delete/Backspace on the table removes selected aliases;
     * a single click closes any open inline editor and refreshes button states;
     * a double-click opens the inline text editor on the clicked row; the right-click
     * context menu offers a Remove option; and the three buttons delegate to the
     * appropriate add/edit/remove helpers.
     */
    private void initListeners()
    {
        aliasesTable.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( ( e.keyCode == SWT.DEL ) || ( e.keyCode == Action.findKeyCode( "BACKSPACE" ) ) ) //$NON-NLS-1$
                {
                    removeSelectedAliases();
                    fillAliasesTable();
                    updateButtonsState();
                    checkAliases();
                }
            }
        } );
        aliasesTable.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                closeTableEditor();
                updateButtonsState();
            }
        } );
        aliasesTable.addListener( SWT.MouseDoubleClick, new Listener()
        {
            public void handleEvent( Event event )
            {
                openTableEditor( aliasesTable.getItem( aliasesTable.getSelectionIndex() ) );
            }
        } );

        // Aliases Table's Popup Menu
        Menu menu = new Menu( getShell(), SWT.POP_UP );
        aliasesTable.setMenu( menu );
        MenuItem removeMenuItem = new MenuItem( menu, SWT.PUSH );
        removeMenuItem.setText( Messages.getString( "AbstractAliasesDialog.Remove" ) ); //$NON-NLS-1$
        removeMenuItem.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_TOOL_DELETE ) );
        removeMenuItem.addListener( SWT.Selection, new Listener()
        {
            public void handleEvent( Event event )
            {
                removeSelectedAliases();
                fillAliasesTable();
                updateButtonsState();
                checkAliases();
            }
        } );

        // Add Button
        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addANewAlias();
            }
        } );

        // Edit Button
        editButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                openTableEditor( aliasesTable.getItem( aliasesTable.getSelectionIndex() ) );
            }
        } );

        // Remove Button
        removeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                removeSelectedAliases();
                fillAliasesTable();
                updateButtonsState();
                checkAliases();
            }
        } );
    }


    // ── Lando Checks Which Desks Are Open ────────────────────────────────────
    // When a citizen walks up to the counter, Lando's staff check whether there's
    // something selected in the queue before enabling the Edit and Remove windows.
    // No selection means nobody's in line — those desks stay closed.
    // We mirror that logic: if at least one table row is selected, we enable the
    // Edit and Remove buttons; otherwise we grey them out.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Edit and Remove buttons depending on whether the user
     * has selected at least one row in the aliases table.
     * We don't want those buttons clickable when nothing is highlighted — it would
     * just throw a NullPointerException trying to edit row index -1.
     */
    private void updateButtonsState()
    {
        if ( aliasesTable.getSelectionCount() >= 1 )
        {
            editButton.setEnabled( true );
            removeButton.setEnabled( true );
        }
        else
        {
            editButton.setEnabled( false );
            removeButton.setEnabled( false );
        }
    }


    // ── Lando Revokes the Selected Permits ───────────────────────────────────
    // When a Cloud City resident loses their permit — maybe they broke the rules,
    // maybe they're leaving — Lando removes them from both the public board
    // and the internal registry ledger (case-insensitive), so the name is freed
    // up for someone else to claim.
    // We do the same: for every selected table row we remove the alias from both
    // the display list and the lower-cased duplicate-detection list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes every currently selected alias from our in-memory alias lists.
     * We remove from both {@code aliases} (for display and final output) and
     * {@code lowerCasedAliases} (for case-insensitive duplicate detection).
     * After this call, {@link #fillAliasesTable()} should be called to refresh the UI.
     */
    private void removeSelectedAliases()
    {
        TableItem[] selectedItems = aliasesTable.getSelection();
        for ( TableItem item : selectedItems )
        {
            aliases.remove( item.getText() );
            lowerCasedAliases.remove( Strings.toLowerCase( item.getText() ) );
        }
    }


    // ── Lando Issues a Blank Permit Slip ─────────────────────────────────────
    // When someone new wants to register in Cloud City, Lando hands them a blank
    // form and sits them down at the counter so they can fill in their name right
    // away. The form is pre-selected so they can start typing immediately.
    // We add a blank row to the table and immediately open the inline text editor
    // on it so the user can type the new alias without any extra clicking.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a blank row to the aliases table and opens the inline text editor on it
     * so the user can type the new alias name right away.
     * The actual alias isn't saved to our list until the user types something —
     * the {@link ModifyListener} on the editor handles that incrementally.
     */
    private void addANewAlias()
    {
        TableItem item = new TableItem( aliasesTable, SWT.NONE );
        item.setText( "" ); //$NON-NLS-1$
        openTableEditor( item );
    }


    // ── Lando Sits Down at the Editing Desk ──────────────────────────────────
    // When a resident wants to correct a typo in their registered name, Lando
    // dismisses whoever was at the desk before, slides a fresh form in front
    // of the applicant, and lets them edit their entry in place. The form has
    // all the existing text pre-selected so they can overwrite it immediately.
    // We open an SWT Text widget right inside the table row, pre-select its
    // contents, and attach listeners so every keystroke updates the alias list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an inline {@link Text} editor embedded inside the given {@link TableItem},
     * letting the user edit the alias text directly in the table row.
     * Any previous editor is disposed first. A {@link ModifyListener} keeps our in-memory
     * list in sync as the user types, and pressing Enter closes the editor and commits.
     * We also install a display-level {@link SWT#TRAVERSE} filter so that pressing
     * Enter inside the inline editor doesn't accidentally close the whole dialog.
     *
     * @param item  the table row to open for inline editing; if {@code null} we return immediately
     */
    private void openTableEditor( TableItem item )
    {
        // Clean up any previous editor control
        Control oldEditor = tableEditor.getEditor();
        if ( oldEditor != null )
            oldEditor.dispose();

        if ( item == null )
            return;

        // The control that will be the editor must be a child of the Table
        Text newEditor = new Text( aliasesTable, SWT.NONE );
        newEditor.setText( item.getText() );
        newEditor.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                saveTableEditorText();
            }
        } );
        newEditor.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( ( e.keyCode == Action.findKeyCode( "RETURN" ) ) || ( e.keyCode == SWT.KEYPAD_CR ) ) //$NON-NLS-1$
                {
                    closeTableEditor();
                }
            }
        } );
        newEditor.selectAll();
        newEditor.setFocus();
        tableEditor.setEditor( newEditor, item, 0 );
        Activator.getDefault().getWorkbench().getDisplay().addFilter( SWT.Traverse, returnKeyListener );
    }


    // ── Lando Updates the Registry Mid-Edit ──────────────────────────────────
    // While the resident is still writing their new name on the form, Lando's
    // clerk is already updating the internal ledger in pencil — ready to ink it
    // in the moment the resident lifts their pen. If the new name is blank,
    // the entry gets removed rather than left as an empty line.
    // We mirror this: on every keystroke in the inline editor, we remove the old
    // alias from both lists and add the new one (if it's non-empty), then run
    // the duplicate/format check so the error strip updates live.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current text from the active inline {@link TableEditor} and synchronises it
     * with our in-memory alias lists, replacing the old value with the new one.
     * If the new text is empty we just remove the old value without adding anything,
     * which effectively discards a row the user cleared out.
     * After updating the lists we call {@link #checkAliases()} so the error strip reflects
     * the latest state without the user having to close the editor first.
     */
    private void saveTableEditorText()
    {
        Text text = ( Text ) tableEditor.getEditor();
        if ( ( text != null ) && ( !text.isDisposed() ) )
        {
            TableItem item = tableEditor.getItem();
            String oldText = item.getText();
            String newText = text.getText();
            if ( !oldText.equals( newText ) )
            {
                aliases.remove( oldText );
                lowerCasedAliases.remove( Strings.toLowerCase( oldText ) );
                if ( !newText.equals( "" ) ) //$NON-NLS-1$
                {
                    aliases.add( newText );
                    lowerCasedAliases.add( Strings.toLowerCase( newText ) );
                }
                item.setText( newText );
            }
        }
        checkAliases();
    }


    // ── Lando Stamps the Form and Files It ───────────────────────────────────
    // When the resident finishes writing, Lando's clerk takes back the pen,
    // stamps "FILED" on the form, and removes the editing desk from the counter.
    // The Return-key intercept that was in place during editing is also removed
    // so it doesn't interfere with the rest of the dialog.
    // We do the final save of whatever text is in the editor, then dispose the
    // Text widget and remove our display-level Traverse filter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Commits the current text in the inline editor to our alias lists and then disposes
     * the editor widget, returning the table to its normal non-editing state.
     * Also removes the display-level {@link SWT#TRAVERSE} filter we installed in
     * {@link #openTableEditor} so the Enter key behaves normally again everywhere else.
     */
    private void closeTableEditor()
    {
        Text text = ( Text ) tableEditor.getEditor();
        if ( ( text != null ) && ( !text.isDisposed() ) )
        {
            saveTableEditorText();
            text.dispose();
        }
        Activator.getDefault().getWorkbench().getDisplay().removeFilter( SWT.Traverse, returnKeyListener );
    }


    // ── Lando Checks for Duplicate Permits ───────────────────────────────────
    // Before Cloud City can accept a new registration, Lando cross-checks it
    // against every permit already on file across the whole registry — not just
    // the ones this resident already holds. If there's a clash, or the name
    // contains illegal characters, the red warning light above the counter
    // flashes and the applicant must fix it before proceeding.
    // We iterate over every alias in our list and make those same two checks:
    // duplicates across the schema registry, and LDAP name-format validity.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current alias list and updates the error strip at the bottom of the dialog.
     * Two kinds of problems can light up the error: (1) an alias that's already registered
     * somewhere else in the schema — detected by calling the abstract {@link #isAliasAlreadyTaken}
     * hook — and (2) an alias that contains characters that aren't valid in an LDAP schema name,
     * detected by {@link PluginUtils#verifyName}.
     * Aliases that were already on the element when the dialog opened are exempt from the
     * "already taken" check — we don't want to block the user from keeping their own names.
     * The error composite is hidden when everything looks clean.
     */
    private void checkAliases()
    {
        errorComposite.setVisible( false );

        for ( String alias : aliases )
        {
            if ( ( isAliasAlreadyTaken( alias ) )
                && ( !initialLowerCasedAliases.contains( Strings.toLowerCase( alias ) ) ) )
            {
                errorComposite.setVisible( true );
                errorLabel.setText( getAliasAlreadyExistsErrorMessage() );
                return;
            }
            else if ( !PluginUtils.verifyName( alias ) )
            {
                errorComposite.setVisible( true );
                errorLabel.setText( NLS.bind( Messages.getString( "AbstractAliasesDialog.InvalidAlias" ), new String[] //$NON-NLS-1$
                    { alias } ) );
                return;
            }
        }
    }


    // ── Lando Names the Office Window ────────────────────────────────────────
    // Lando puts a sign on the office door so every visitor knows exactly which
    // department they've walked into — no confusion about whether this is the
    // permit office or the billing department.
    // We set the dialog shell's title bar text to the appropriate "Edit Alias" label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the title bar text on the dialog shell.
     * JFace calls this before the shell is made visible, so whatever we set here
     * is what the user sees in the window title.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AbstractAliasesDialog.EditAlias" ) ); //$NON-NLS-1$
    }


    // ── Lando Hands Over the Permit Book ─────────────────────────────────────
    // When the meeting is over and the user clicks OK, Lando hands the caller
    // the completed permit book — the final, committed list of approved aliases.
    // The caller can then hand those aliases to the schema element being edited.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the final list of aliases as an array, ready to be stored back on the schema element.
     * Call this after the dialog closes with OK — before that the list is still in flux.
     *
     * @return  a {@code String[]} containing every alias the user confirmed; never {@code null}
     *          but may be empty if the user removed them all
     */
    public String[] getAliases()
    {
        return aliases.toArray( new String[0] );
    }


    // ── Lando Delegates the Error Wording ────────────────────────────────────
    // Lando can't write the exact error message himself — he doesn't know whether
    // this office handles attribute type permits or object class permits. He
    // delegates that wording to the department manager (the concrete subclass).
    // Subclasses provide the specific "already exists" error message text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable error message to display when the user types an alias that's
     * already registered elsewhere in the schema.
     * We're abstract here because the message differs slightly between attribute types and
     * object classes — the concrete subclass knows which flavour to return.
     *
     * @return  a non-null, non-empty localised error string
     */
    protected abstract String getAliasAlreadyExistsErrorMessage();


    // ── Lando Delegates the Registry Check ───────────────────────────────────
    // Lando's office can check for duplicates, but it needs to call the right
    // department registry — attribute types are registered in one book, object
    // classes in another. The department manager (concrete subclass) knows which
    // registry to query for a given alias string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the given alias is already registered on a different schema element
     * of the same kind (attribute type or object class).
     * We delegate to the concrete subclass because the registry lookup differs: attribute
     * types and object classes live in separate namespaces inside the schema handler.
     *
     * @param alias  the alias string to look up, in its original (non-lower-cased) form
     * @return       {@code true} if the alias is already claimed by another schema element,
     *               {@code false} if it's free to use
     */
    protected abstract boolean isAliasAlreadyTaken( String alias );
}
