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


import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ScopeDialog — YODA EXPLAINS LEVELS OF FORCE MASTERY ───────────────
// On Dagobah, Yoda explains the Force to Luke in three levels: feel the
// immediate moment (just yourself — object scope), reach out to what is directly
// around you (the near field — one level), or expand your awareness to the
// entire universe (the whole subtree).  "Size matters not" — but depth does.
// LDAP searches have the same three scopes: OBJECT (just the base entry),
// ONELEVEL (the base entry's direct children), or SUBTREE (the base entry and
// all its descendants).  This dialog asks the user which scope to apply when
// copying entries, so we know how deep to go.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that asks the user to choose the copy scope — that is, how deep into
 * the LDAP subtree we should copy.  The three options map directly to the LDAP
 * {@link SearchScope} values: OBJECT (just the entry itself), ONELEVEL (the
 * entry and its direct children), and SUBTREE (everything from the entry
 * downward).
 * Think of this class as Yoda explaining the three levels of Force awareness:
 * "Choose, you must — how deep to feel the Force."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ScopeDialog extends Dialog
{

    /** The dialog title. */
    private String dialogTitle;

    /** The multiple entries selected flag. */
    private boolean multipleEntriesSelected;

    /** The scope. */
    private SearchScope scope;

    /** The object scope button. */
    private Button objectScopeButton;

    /** The onelevel scope button. */
    private Button onelevelScopeButton;

    /** The subtree scope button. */
    private Button subtreeScopeButton;


    // ── YODA OPENS THE TRAINING SESSION ──────────────────────────────────────
    // Yoda invites Luke to sit and prepares to explain the three levels — but
    // first he asks: "One student, or many?"  If there are multiple students
    // the labels change slightly ("only the copied entries" vs "only the copied
    // entry") to match the plurality.
    // We note the dialog title and whether multiple entries are selected so we
    // can produce grammatically correct button labels.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ScopeDialog.  The {@code multipleEntriesSelected} flag
     * controls the label text on the radio buttons — singular ("the copied
     * entry") vs plural ("the copied entries") — because the dialog is used
     * for both single and multi-selection copy operations.
     *
     * <p>For example — Yoda checks whether he has one student or many:</p>
     * <pre>
     *   ScopeDialog dialog = new ScopeDialog(shell, "Select Copy Depth", true);
     *   if (dialog.open() == OK) {
     *       SearchScope scope = dialog.getScope();
     *   }
     * </pre>
     *
     * @param parentShell             the shell that owns this dialog
     * @param dialogTitle             the title to display in the window title bar
     * @param multipleEntriesSelected {@code true} if more than one entry is being copied — affects button labels
     */
    public ScopeDialog( Shell parentShell, String dialogTitle, boolean multipleEntriesSelected )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.dialogTitle = dialogTitle;
        this.multipleEntriesSelected = multipleEntriesSelected;
    }


    // ── YODA WRITES HIS LESSON TITLE ON THE STONE ────────────────────────────
    // Before the session begins Yoda scratches the lesson title into the stone
    // with his walking stick: "Levels of Force Awareness — Select Copy Depth."
    // We set the window title from the constructor parameter.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title before the shell is shown.
     *
     * <p>For example — Yoda titles the lesson:</p>
     * <pre>
     *   shell.setText("Select Copy Depth");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( dialogTitle );
    }


    // ── LUKE TELLS YODA WHICH LEVEL HE CHOOSES ───────────────────────────────
    // Luke says: "I want the full subtree — all of it!"  Yoda records the
    // answer and the training session concludes.
    // On OK we read which radio button is selected and store the corresponding
    // SearchScope so the caller can retrieve it with {@link #getScope()}.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user confirms with OK.  We check which radio button is
     * selected and store the corresponding {@link SearchScope}: OBJECT if the
     * first button is checked, ONELEVEL if the second, or SUBTREE otherwise.
     *
     * <p>For example — Luke announces his choice:</p>
     * <pre>
     *   scope = objectScopeButton.getSelection() ? OBJECT
     *         : onelevelScopeButton.getSelection() ? ONELEVEL : SUBTREE;
     * </pre>
     */
    protected void okPressed()
    {
        scope = objectScopeButton.getSelection() ? SearchScope.OBJECT
            : onelevelScopeButton.getSelection() ? SearchScope.ONELEVEL : SearchScope.SUBTREE;
        super.okPressed();
    }


    // ── YODA PREPARES THE YES AND NO STONES ──────────────────────────────────
    // At the end of the lesson Yoda places two smooth stones in front of Luke:
    // one to confirm ("This level I choose"), one to abandon the lesson.
    // We create OK and Cancel buttons — neither is the default so the user must
    // make a deliberate choice.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates OK and Cancel buttons.  Neither is flagged as the default button
     * because we want the user to make a conscious scope selection rather than
     * accidentally confirming with Enter.
     *
     * <p>For example — Yoda places the decision stones:</p>
     * <pre>
     *   createButton(OK,     defaultButton=false);
     *   createButton(CANCEL, defaultButton=false);
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── YODA DESCRIBES THE THREE LEVELS ──────────────────────────────────────
    // Yoda draws three concentric circles in the mud: the innermost for the
    // student alone, the next for the student and those immediately beside them,
    // the outermost for the student and everything beneath in the Force.
    // We draw three radio buttons inside a labeled group, adjusting the text
    // for singular or plural depending on how many entries are being copied.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: a group box containing three radio buttons
     * for the three LDAP scopes.  The label text on each button adapts based on
     * whether one or multiple entries were selected for copying.  The first
     * button (OBJECT scope) is selected by default.
     *
     * <p>For example — Yoda describes the three levels:</p>
     * <pre>
     *   radio("Only the copied entry")              // OBJECT — just yourself
     *   radio("Copied entry and direct children")   // ONELEVEL — you + immediate students
     *   radio("Whole subtree")                      // SUBTREE — the entire galaxy
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        Group group = BaseWidgetUtils.createGroup( composite, Messages.getString( "ScopeDialog.SelectCopyDepth" ), 1 ); //$NON-NLS-1$
        objectScopeButton = new Button( group, SWT.RADIO );
        objectScopeButton.setSelection( true );
        objectScopeButton.setText( multipleEntriesSelected ? Messages.getString( "ScopeDialog.OnlyCopiedEntries" ) //$NON-NLS-1$
            : Messages.getString( "ScopeDialog.OnlyCopiedEntry" ) ); //$NON-NLS-1$
        onelevelScopeButton = new Button( group, SWT.RADIO );
        onelevelScopeButton.setText( multipleEntriesSelected ? Messages
            .getString( "ScopeDialog.CopiedEntriesAndDirectChildren" ) //$NON-NLS-1$
            : Messages.getString( "ScopeDialog.CopiedEntryAndDirectChildren" ) ); //$NON-NLS-1$
        subtreeScopeButton = new Button( group, SWT.RADIO );
        subtreeScopeButton.setText( multipleEntriesSelected ? Messages.getString( "ScopeDialog.WholeSubtrees" ) //$NON-NLS-1$
            : Messages.getString( "ScopeDialog.WholeSubtree" ) ); //$NON-NLS-1$

        applyDialogFont( composite );
        return composite;
    }


    // ── YODA REPORTS BACK WHICH LEVEL LUKE CHOSE ─────────────────────────────
    // After the session Obi-Wan's Force ghost appears and asks: "How far did he
    // reach?"  Yoda answers with the exact scope Luke chose.
    // Callers retrieve the selected scope here after the dialog closes with OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SearchScope} the user selected.  Call this after the
     * dialog closes with OK; the default before any selection is made is
     * effectively OBJECT (the first radio button).
     *
     * <p>For example — Obi-Wan asks Yoda how far Luke reached:</p>
     * <pre>
     *   SearchScope scope = dialog.getScope();
     *   // SearchScope.SUBTREE — he reached the whole galaxy
     * </pre>
     *
     * @return  the chosen {@link SearchScope}; never {@code null} after OK
     */
    public SearchScope getScope()
    {
        return scope;
    }

}
