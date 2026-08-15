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
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.DnBuilderWidget;
import org.apache.directory.studio.ldapbrowser.core.jobs.EntryExistsCopyStrategyDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: EntryExistsCopyStrategyDialogImpl — HAN SOLO'S BLOCKED ESCAPE ─────
// Han Solo is trying to get the Millennium Falcon out of the Death Star's
// docking bay — but every route seems blocked: troopers here, a tractor beam
// there.  He has to pick a strategy fast: bail out, sneak through, blast the
// obstacle, or find another route entirely.
// We face the same problem during a copy operation: we arrive at the destination
// and find an entry with the same DN already exists.  This dialog asks the user
// to choose: stop (break), skip it (ignore), bulldoze it (overwrite), or squeeze
// by under a new name (rename).  Every door is worth trying.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * When we copy an LDAP entry to a location where an entry with the same DN
 * already exists, we can't just crash into it — we need a plan.  This dialog
 * implements {@link EntryExistsCopyStrategyDialog} and asks the user to choose
 * one of four strategies: break out of the copy, ignore and continue, overwrite
 * the existing entry, or rename the incoming entry to a new RDN.
 * Think of it as Han Solo deciding which escape route to take when every door
 * out of the Death Star looks closed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryExistsCopyStrategyDialogImpl extends Dialog implements EntryExistsCopyStrategyDialog
{

    /** The dialog title. */
    private String dialogTitle = Messages.getString( "EntryExistsCopyStrategyDialogImpl.SelectCopyStrategy" ); //$NON-NLS-1$

    /** The break button. */
    private Button breakButton;

    /** The ignore button. */
    private Button ignoreButton;

    /** The overwrite button. */
    private Button overwriteButton;

    /** The rename button. */
    private Button renameButton;
    //
    //    /** The remember check box. */
    //    private Button rememberCheckbox;

    /** The Dn builder widget. */
    private DnBuilderWidget dnBuilderWidget;

    /** The new Rdn. */
    private Rdn rdn;

    /** The strategy */
    private EntryExistsCopyStrategy strategy;

    /** The remember flag */
    private boolean isRememberStrategy;

    private IBrowserConnection browserConnection;

    private Dn dn;


    // ── HAN SPOTS THE BLOCKED CORRIDOR ───────────────────────────────────────
    // Han skids to a halt at the docking bay entrance — Stormtroopers everywhere.
    // He keeps his blaster ready and defaults to "abort the whole thing" until
    // someone gives him a better option.
    // We initialise the strategy to BREAK so that if something goes wrong before
    // the user even confirms, we fail safe rather than overwriting blindly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryExistsCopyStrategyDialogImpl.  We default the strategy
     * to {@link EntryExistsCopyStrategy#BREAK} so a dismissed dialog never
     * silently overwrites anything — the caller must see an explicit choice.
     * The dialog is resizable so users can see long DN strings without truncation.
     *
     * <p>For example — Han defaults to "abort" when he sees trouble:</p>
     * <pre>
     *   strategy = BREAK;  // abort by default — safe fallback
     *   // user will override this by picking a button and clicking OK
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog window
     */
    public EntryExistsCopyStrategyDialogImpl( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        strategy = EntryExistsCopyStrategy.BREAK;
    }


    // ── HAN READS THE SIGN ON THE DOOR ───────────────────────────────────────
    // Han glances at the door label: "Docking Bay 94 — choose your exit."
    // He needs to know which room he is in before making a move.
    // We set the shell title so the user can see they are in a copy-strategy
    // decision, not some unrelated dialog.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title before the shell is shown.  Eclipse calls
     * this as part of the window lifecycle — we use it to label the window
     * clearly so users understand what decision they are being asked to make.
     *
     * <p>For example — Han reads the bay marker:</p>
     * <pre>
     *   shell.setText("Select Copy Strategy");  // "Docking Bay 94"
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( dialogTitle );
    }


    // ── HAN PICKS AN ESCAPE ROUTE AND COMMITS ────────────────────────────────
    // Han checks which door Chewie is standing next to and yells the call:
    // "Stop shooting, ignore them, blast them, or find the service corridor!"
    // He captures the decision and — if renaming — notes the new corridor number.
    // On OK we record which radio button is selected and store the new RDN if
    // the user chose the rename route.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We inspect which radio button is selected
     * and store the corresponding {@link EntryExistsCopyStrategy}.  If the user
     * chose RENAME_AND_CONTINUE we also grab the new RDN they typed in the DN
     * builder widget — that RDN will be used to re-name the incoming entry so it
     * no longer clashes with the existing one.
     *
     * <p>For example — Han chooses the service corridor:</p>
     * <pre>
     *   if (renameButton.isSelected()) {
     *       strategy = RENAME_AND_CONTINUE;
     *       rdn = dnBuilderWidget.getRdn();  // the new corridor number
     *   }
     * </pre>
     */
    protected void okPressed()
    {
        rdn = null;
        //        isRememberStrategy = rememberCheckbox.getSelection() && rememberCheckbox.isEnabled();

        if ( breakButton.getSelection() )
        {
            strategy = EntryExistsCopyStrategy.BREAK;
        }
        else if ( ignoreButton.getSelection() )
        {
            strategy = EntryExistsCopyStrategy.IGNORE_AND_CONTINUE;
        }
        else if ( overwriteButton.getSelection() )
        {
            strategy = EntryExistsCopyStrategy.OVERWRITE_AND_CONTINUE;
        }
        else if ( renameButton.getSelection() )
        {
            strategy = EntryExistsCopyStrategy.RENAME_AND_CONTINUE;
            rdn = dnBuilderWidget.getRdn();
        }

        super.okPressed();
    }


    // ── LEIA PREPARES THE ESCAPE PLAN OPTIONS ────────────────────────────────
    // Leia quickly briefs the team on the options before they split up — she
    // keeps the briefing to essentials: "OK or Cancel, nothing else."
    // We only create OK and Cancel — no additional buttons needed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates just OK and Cancel buttons for the button bar.  We don't need any
     * extra actions beyond confirming or abandoning the strategy choice.
     *
     * <p>For example — Leia limits the debrief to two outcomes:</p>
     * <pre>
     *   createButton(OK);      // "We go now"
     *   createButton(CANCEL);  // "Abort — regroup"
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── HAN SURVEYS ALL FOUR EXITS ────────────────────────────────────────────
    // Han scans the room: four doors — stop here, sneak past, blast through, or
    // duck into the service corridor with a new badge.  Chewie guards the rename
    // widget; it only unlocks when Han picks the rename door.
    // We build the four radio buttons and a conditional DN-builder widget that
    // only activates when the user selects "rename".
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a description of the conflict (which DN
     * already exists), then four mutually-exclusive radio buttons for the
     * strategies, and a DN-builder widget under the "rename" radio that lets the
     * user enter a new RDN for the incoming entry.
     *
     * <p>For example — Han evaluates all four escape routes:</p>
     * <pre>
     *   label("Entry 'cn=Luke,ou=Rebels' already exists — what now?");
     *   radioButton(BREAK);    // stop
     *   radioButton(IGNORE);   // skip and move on
     *   radioButton(OVERWRITE);// blast through
     *   radioButton(RENAME);   // service corridor — new badge required
     *   dnBuilderWidget.createContents(group);  // unlocked only for RENAME
     * </pre>
     *
     * @param parent  the composite Eclipse hands us to populate
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        String text = NLS.bind(
            Messages.getString( "EntryExistsCopyStrategyDialogImpl.SelectCopyStrategyDescription" ), dn.getName() ); //$NON-NLS-1$
        BaseWidgetUtils.createLabel( composite, text, 1 );

        Composite group2 = BaseWidgetUtils.createGroup( composite, "", 1 ); //$NON-NLS-1$
        Composite group = BaseWidgetUtils.createColumnContainer( group2, 2, 1 );

        SelectionListener listener = new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                validate();
            }
        };

        breakButton = BaseWidgetUtils.createRadiobutton( group, Messages
            .getString( "EntryExistsCopyStrategyDialogImpl.StopCopyProcess" ), 2 ); //$NON-NLS-1$
        breakButton.setSelection( true );
        breakButton.addSelectionListener( listener );

        ignoreButton = BaseWidgetUtils.createRadiobutton( group, Messages
            .getString( "EntryExistsCopyStrategyDialogImpl.IgnoreEntryAndContinue" ), 2 ); //$NON-NLS-1$
        ignoreButton.addSelectionListener( listener );

        overwriteButton = BaseWidgetUtils.createRadiobutton( group, Messages
            .getString( "EntryExistsCopyStrategyDialogImpl.OverwriteEntryAndContinue" ), 2 ); //$NON-NLS-1$
        overwriteButton.addSelectionListener( listener );

        renameButton = BaseWidgetUtils.createRadiobutton( group, Messages
            .getString( "EntryExistsCopyStrategyDialogImpl.RenameEntryAndContinue" ), 2 ); //$NON-NLS-1$
        renameButton.addSelectionListener( listener );

        BaseWidgetUtils.createRadioIndent( group, 1 );
        dnBuilderWidget = new DnBuilderWidget( true, false );
        dnBuilderWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );
        dnBuilderWidget.createContents( group );
        dnBuilderWidget.setInput( browserConnection, SchemaUtils.getNamesAsArray( browserConnection.getSchema()
            .getAttributeTypeDescriptions() ), dn.getRdn(), null );

        //        rememberCheckbox = BaseWidgetUtils.createCheckbox( composite, "Remember decision", 2 );

        validate();

        applyDialogFont( composite );
        return composite;
    }


    // ── CHEWIE GUARDS THE SERVICE CORRIDOR ───────────────────────────────────
    // Chewie only lets someone through the service corridor door if Han has
    // actually chosen that route — otherwise the door stays locked.
    // We enable the DN builder widget (and require a valid RDN) only when the
    // rename button is selected; all other strategies leave it disabled.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the DN builder widget and the OK button based on the
     * currently selected radio button.  When RENAME is selected the user must
     * supply a valid new RDN before OK is available; for all other strategies
     * the DN widget is locked and OK stays enabled unconditionally.
     *
     * <p>For example — Chewie checks Han's choice:</p>
     * <pre>
     *   if (renameButton.isSelected()) {
     *       dnWidget.setEnabled(true);
     *       okButton.setEnabled(dnWidget.getRdn() != null);
     *   } else {
     *       dnWidget.setEnabled(false);
     *       // OK is always available for the other three routes
     *   }
     * </pre>
     */
    private void validate()
    {
        if ( renameButton.getSelection() )
        {
            dnBuilderWidget.setEnabled( true );
            getButton( IDialogConstants.OK_ID ).setEnabled( dnBuilderWidget.getRdn() != null );
        }
        else
        {
            dnBuilderWidget.setEnabled( false );
        }
        //        rememberCheckbox.setEnabled( overwriteButton.getSelection() || ignoreButton.getSelection() );
    }


    // ── HAN GIVES THE ORDER — PUNCH IT ───────────────────────────────────────
    // "Punch it, Chewie!" — but the Falcon is a background operation and the UI
    // thread can't just block waiting for it.  Han shouts the order on the UI
    // thread (syncExec) and waits for the jump to hyperspace to begin.
    // We must run the dialog on the SWT UI thread because all widget operations
    // must happen there; syncExec ensures we block the calling thread until the
    // user picks a strategy.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens the dialog in a thread-safe way.  The copy job that triggers this
     * dialog may be running on a background worker thread, but all SWT widget
     * operations must happen on the UI thread.  We use
     * {@link Display#syncExec(Runnable)} to hop onto the UI thread, show the
     * dialog, and block until the user dismisses it — then we return the result
     * back to the calling thread.
     *
     * <p>For example — Han punches it from a background thread:</p>
     * <pre>
     *   Display.getDefault().syncExec(() -> {
     *       result = super.open();   // blocks until user chooses
     *   });
     *   return result;
     * </pre>
     *
     * @return  the dialog return code — {@link Dialog#OK} or {@link Dialog#CANCEL}
     */
    public int open()
    {
        final int[] result = new int[1];
        Display.getDefault().syncExec( new Runnable()
        {
            public void run()
            {
                result[0] = EntryExistsCopyStrategyDialogImpl.super.open();
            }
        } );
        return result[0];
    }


    // ── THE REBELS DEBRIEF: WHICH ROUTE DID HAN TAKE? ────────────────────────
    // After the escape, Leia asks for the mission report: "Which way did you go?"
    // Han reports back — break, ignore, overwrite, or rename.
    // The copy job calls this to find out what it should do with the conflicting
    // entry.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the strategy the user selected.  Call this after the dialog closes
     * with {@code OK} to decide how the copy job should handle the conflicting
     * entry.  Defaults to {@link EntryExistsCopyStrategy#BREAK} if the user
     * cancelled without choosing.
     *
     * <p>For example — debrief after the escape:</p>
     * <pre>
     *   switch (dialog.getStrategy()) {
     *     case BREAK:                 // Han parked the Falcon and walked away
     *     case IGNORE_AND_CONTINUE:   // Han snuck past
     *     case OVERWRITE_AND_CONTINUE:// Han blasted through
     *     case RENAME_AND_CONTINUE:   // Han used a fake badge
     *   }
     * </pre>
     *
     * @return  the chosen {@link EntryExistsCopyStrategy}; never {@code null}
     */
    public EntryExistsCopyStrategy getStrategy()
    {
        return strategy;
    }


    // ── HAN HANDS OVER THE FAKE BADGE NUMBER ─────────────────────────────────
    // After choosing the service corridor Han passes Chewie the new badge number
    // so they can print it before going through — without it, the door scanner
    // won't accept them.
    // When the strategy is RENAME, the copy job needs the new RDN to rename the
    // incoming entry before placing it in the destination.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new RDN the user typed when they chose the RENAME strategy.
     * If any other strategy was chosen, or the dialog was cancelled, this returns
     * {@code null}.  The copy job uses this RDN to give the incoming entry a
     * fresh name that doesn't clash with the existing entry.
     *
     * <p>For example — Chewie receives the new badge number:</p>
     * <pre>
     *   Rdn newRdn = dialog.getRdn();   // e.g. "cn=Luke_copy"
     *   incomingEntry.setRdn(newRdn);
     * </pre>
     *
     * @return  the new {@link Rdn} for the renamed entry, or {@code null} if not applicable
     */
    public Rdn getRdn()
    {
        return rdn;
    }


    // ── HAN CHECKS: DO WE REMEMBER THIS ROUTE FOR NEXT TIME? ─────────────────
    // After a successful escape, Han logs the route in his mental map — next time
    // they come back he won't need to debate which door to take.
    // This tells the copy job whether the user asked their strategy to be
    // remembered for subsequent conflicts in the same operation.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user asked to remember their strategy for the rest of
     * the current copy operation.  When {@code true} the copy job should not
     * show this dialog again for subsequent conflicts — it should reuse the same
     * strategy automatically.  Currently the "remember" checkbox is commented out,
     * so this always returns {@code false}.
     *
     * <p>For example — Han logs the route for future runs:</p>
     * <pre>
     *   if (dialog.isRememberSelection()) {
     *       copyJob.setDefaultStrategy(dialog.getStrategy());
     *   }
     * </pre>
     *
     * @return  {@code true} if the user wants to re-use this strategy for all remaining conflicts
     */
    public boolean isRememberSelection()
    {
        return isRememberStrategy;
    }


    // ── LEIA BRIEFS HAN ON WHICH ENTRY IS BLOCKED ────────────────────────────
    // Before Han can choose a route Leia needs to tell him exactly which door is
    // blocked — "Docking Bay 94, entry cn=Leia, ou=Alderaan" — so he can assess
    // all four options with full information.
    // The copy job calls this before opening the dialog to supply the context: the
    // connection and the DN of the conflicting entry.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Tells the dialog which entry already exists at the destination.  The copy
     * job must call this before calling {@link #open()} — without it, the dialog
     * has no idea what conflict it is asking about and will throw a
     * {@code NullPointerException} when it tries to build the label.
     *
     * <p>For example — Leia briefs Han with exact coordinates:</p>
     * <pre>
     *   dialog.setExistingEntry(connection, Dn.of("cn=Luke,ou=Rebels,dc=galaxy,dc=org"));
     *   dialog.open();
     * </pre>
     *
     * @param browserConnection  the connection the entry lives in — needed to populate the RDN builder
     * @param dn                 the DN of the entry that already exists at the copy destination
     */
    public void setExistingEntry( IBrowserConnection browserConnection, Dn dn )
    {
        this.browserConnection = browserConnection;
        this.dn = dn;
    }

}
