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

package org.apache.directory.studio.openldap.common.ui.widgets;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SelectEntryDialog;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.Messages;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: EntryWidget — REBEL SCOUT SHIP LOCATING ENTRIES IN THE GALAXY MAP ─
// Picture a Rebel scout ship scanning the galaxy hologram to locate a specific
// planet (LDAP entry) by its coordinates (DN). The ship's console has three
// controls: a free-text coordinate input with a drop-down history of previously
// visited systems, an optional "None" checkbox for when no destination is needed,
// and a Browse button that opens the full SelectEntryDialog star chart. Whenever
// the pilot changes coordinates or clicks Browse, we fire change listeners so
// the mission planner stays informed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide an LDAP entry selection widget composed of a DN combo with history,
 * an optional "None" checkbox, and a Browse button. We extend
 * {@link AbstractWidget} so listeners are notified on changes. We can operate
 * against a live {@link IBrowserConnection} to read entries and open the
 * {@link SelectEntryDialog} for interactive selection.
 *
 * <p>The EntryWidget could be used to select an entry.
 * It is composed
 * <ul>
 * <li>a combo to manually enter an Dn or to choose one from
 *     the history
 * <li>an up button to switch to the parent's Dn
 * <li>a browse button to open a {@link SelectEntryDialog}
 * </ul>
 * </p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryWidget extends AbstractWidget
{
    /** The connection. */
    private IBrowserConnection browserConnection;

    /** The flag to show the "None" checkbox or not */
    private boolean showNoneCheckbox;

    /** The selected Dn. */
    private Dn dn;

    /** The enabled state */
    private boolean enabled = true;

    // UI widgets
    private Composite composite;
    private Button noneCheckbox;
    private Combo dnCombo;
    private Button entryBrowseButton;

    // Listeners
    private SelectionAdapter noneCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            noneCheckboxSelected( noneCheckbox.getSelection() );
            notifyListeners();
        }
    };

    private ModifyListener dnComboListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            try
            {
                dn = new Dn( dnCombo.getText() );
            }
            catch ( LdapInvalidDnException e1 )
            {
                dn = null;
            }

            internalSetEnabled();
            notifyListeners();
        }
    };

    private SelectionAdapter entryBrowseButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            if ( browserConnection != null )
            {
                // get root entry
                IEntry rootEntry = browserConnection.getRootDSE();

                // get initial entry
                IEntry entry = rootEntry;

                if ( ( dn != null ) && ( dn.size() > 0 ) )
                {
                    entry = browserConnection.getEntryFromCache( dn );

                    if ( entry == null )
                    {
                        ReadEntryRunnable runnable = new ReadEntryRunnable( browserConnection, dn );
                        RunnableContextRunner.execute( runnable, null, true );
                        entry = runnable.getReadEntry();
                    }
                }

                // open dialog
                SelectEntryDialog dialog = new SelectEntryDialog( entryBrowseButton.getShell(), Messages
                    .getString( "EntryWidget.SelectDN" ), rootEntry, entry ); //$NON-NLS-1$
                dialog.open();
                IEntry selectedEntry = dialog.getSelectedEntry();

                // get selected Dn
                if ( selectedEntry != null )
                {
                    dn = selectedEntry.getDn();
                    dnChanged();
                    internalSetEnabled();
                    notifyListeners();
                }
            }
        }
    };


    // ── CONSTRUCTOR: EntryWidget() — BLANK SCOUT MANIFEST ────────────────────
    // We create a scout with no connection and no initial destination. Both the
    // connection and the DN can be supplied later via {@link #setInput(Dn)}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link EntryWidget} with no browser connection and no
     * initial DN. Both can be supplied through subsequent calls.
     */
    public EntryWidget()
    {
        this.browserConnection = null;
        this.dn = null;
    }


    // ── CONSTRUCTOR: EntryWidget(IBrowserConnection) — DOCKING AT A STATION ───
    // We record the connection so the Browse button can contact the LDAP server
    // to open the SelectEntryDialog, but we leave the DN unset.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link EntryWidget} pre-configured with the given
     * browser connection, but with no initial DN.
     *
     * @param browserConnection  the LDAP browser connection to use for browsing
     */
    public EntryWidget( IBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;
    }


    // ── CONSTRUCTOR: EntryWidget(IBrowserConnection, Dn) — SETTING COORDINATES
    // We record both the connection and the initial destination DN so the widget
    // shows the right entry as soon as it is created.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link EntryWidget} pre-configured with the given
     * browser connection and initial DN.
     *
     * @param browserConnection  the LDAP browser connection to use for browsing
     * @param dn                 the initial DN to display
     */
    public EntryWidget( IBrowserConnection browserConnection, Dn dn )
    {
        this.browserConnection = browserConnection;
        this.dn = dn;
    }


    // ── CONSTRUCTOR: EntryWidget(IBrowserConnection, Dn, boolean) — FULL BRIEF
    // We record all three configuration values: connection, initial DN, and
    // whether to show the "None" checkbox for marking an absent destination.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link EntryWidget} with a browser connection, an
     * initial DN, and a flag controlling whether the "None" checkbox is shown.
     *
     * @param browserConnection  the LDAP browser connection to use for browsing
     * @param dn                 the initial DN to display
     * @param showNoneCheckbox   {@code true} to show a "None" checkbox
     */
    public EntryWidget( IBrowserConnection browserConnection, Dn dn, boolean showNoneCheckbox )
    {
        this.browserConnection = browserConnection;
        this.dn = dn;
        this.showNoneCheckbox = showNoneCheckbox;
    }


    // ── METHOD: createWidget(Composite) — DEPLOYING THE SCOUT (NO TOOLKIT) ────
    // We delegate to the toolkit-aware overload with {@code null} so there is
    // always one code path to maintain.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's SWT controls inside the given parent without a
     * {@link FormToolkit}. Delegates to
     * {@link #createWidget(Composite, FormToolkit)}.
     *
     * @param parent  the parent {@link Composite}
     */
    public void createWidget( Composite parent )
    {
        createWidget( parent, null );
    }


    // ── METHOD: createWidget(Composite, FormToolkit) — DEPLOYING THE SCOUT ────
    // We build the composite that holds the optional None checkbox, the DN
    // combo with its history, and the Browse button. We wire up all the
    // listeners and then synchronize the initial UI state via dnChanged() and
    // internalSetEnabled().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create all SWT controls for this widget inside the given parent,
     * optionally adapting them with a {@link FormToolkit} for Eclipse Forms.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void createWidget( Composite parent, FormToolkit toolkit )
    {
        // Composite
        if ( toolkit != null )
        {
            composite = toolkit.createComposite( parent );
        }
        else
        {
            composite = new Composite( parent, SWT.NONE );
        }

        GridLayout compositeGridLayout = new GridLayout( getNumberOfColumnsForComposite(), false );
        compositeGridLayout.marginHeight = compositeGridLayout.marginWidth = 0;
        compositeGridLayout.verticalSpacing = 0;
        composite.setLayout( compositeGridLayout );

        // None Checbox
        if ( showNoneCheckbox )
        {
            if ( toolkit != null )
            {
                noneCheckbox = toolkit.createButton( composite, "None", SWT.CHECK );
            }
            else
            {
                noneCheckbox = BaseWidgetUtils.createCheckbox( composite, "None", 1 );
            }
        }

        // Dn combo
        dnCombo = BaseWidgetUtils.createCombo( composite, new String[0], -1, 1 );

        if ( toolkit != null )
        {
            toolkit.adapt( dnCombo );
        }

        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        gd.widthHint = 50;
        dnCombo.setLayoutData( gd );

        // Dn history
        String[] history = HistoryUtils.load( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_DN_HISTORY );
        dnCombo.setItems( history );

        // Browse button
        if ( toolkit != null )
        {
            entryBrowseButton = toolkit.createButton( composite,
                Messages.getString( "EntryWidget.BrowseButton" ), SWT.PUSH ); //$NON-NLS-1$
        }
        else
        {
            entryBrowseButton = BaseWidgetUtils.createButton( composite,
                Messages.getString( "EntryWidget.BrowseButton" ), 1 ); //$NON-NLS-1$
        }

        dnChanged();
        internalSetEnabled();
        addListeners();
    }


    // ── METHOD: addListeners — ACTIVATING THE SCOUT'S SENSORS ────────────────
    // We attach the pre-built listener instances to all three interactive
    // controls so changes to the None checkbox, the DN combo, or the Browse
    // button are all captured.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach our pre-built selection and modify listeners to the checkbox,
     * the DN combo, and the Browse button.
     */
    private void addListeners()
    {
        if ( showNoneCheckbox )
        {
            noneCheckbox.addSelectionListener( noneCheckboxListener );
        }

        dnCombo.addModifyListener( dnComboListener );
        entryBrowseButton.addSelectionListener( entryBrowseButtonListener );
    }


    // ── METHOD: removeListeners — SILENCING THE SCOUT'S SENSORS ──────────────
    // We detach all listeners before programmatically changing the DN (e.g., in
    // setInput) so those internal updates don't fire spurious change events.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We remove our listeners from all interactive controls to allow silent
     * programmatic updates without triggering change notifications.
     */
    private void removeListeners()
    {
        if ( showNoneCheckbox )
        {
            noneCheckbox.removeSelectionListener( noneCheckboxListener );
        }

        dnCombo.removeModifyListener( dnComboListener );
        entryBrowseButton.removeSelectionListener( entryBrowseButtonListener );
    }


    // ── METHOD: getNumberOfColumnsForComposite — COUNTING THE CONSOLE PANELS ──
    // We return 3 if the None checkbox is shown (checkbox + combo + button),
    // or 2 if it is hidden (combo + button).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the number of columns the inner composite needs: 3 when the
     * None checkbox is visible, 2 otherwise.
     *
     * @return the column count for the inner {@link GridLayout}
     */
    private int getNumberOfColumnsForComposite()
    {
        if ( showNoneCheckbox )
        {
            return 3;
        }
        else
        {
            return 2;
        }
    }


    // ── METHOD: dnChanged — UPDATING THE HOLOGRAM DISPLAY ────────────────────
    // We synchronize the combo text and the None checkbox to match the current
    // {@code dn} value, keeping all three controls in a consistent state without
    // firing external change events.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We synchronize the combo text and (if shown) the None checkbox to reflect
     * the current {@link #dn} value. Called internally after any DN change.
     */
    private void dnChanged()
    {
        if ( dnCombo != null && entryBrowseButton != null )
        {
            if ( showNoneCheckbox )
            {
                boolean noneSelected = ( dn == null );
                noneCheckbox.setSelection( noneSelected );
                noneCheckboxSelected( noneSelected );
            }

            dnCombo.setText( dn != null ? dn.getName() : "" ); //$NON-NLS-1$
        }
    }


    // ── METHOD: noneCheckboxSelected — TOGGLING THE "NO DESTINATION" FLAG ─────
    // When the None checkbox is checked the DN combo and Browse button are
    // disabled (no destination needed). When unchecked they are re-enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable or disable the DN combo and Browse button based on the state of
     * the None checkbox. When {@code state} is {@code true} the entry controls
     * are disabled.
     *
     * @param state  {@code true} if None is selected, {@code false} otherwise
     */
    private void noneCheckboxSelected( boolean state )
    {
        dnCombo.setEnabled( !state );
        entryBrowseButton.setEnabled( !state );
    }


    // ── METHOD: setEnabled — ACTIVATING OR GROUNDING THE SCOUT ───────────────
    // We record the new enabled state and refresh all child controls via
    // internalSetEnabled(). If enabling, we also call dnChanged() to restore
    // the checkbox and combo to a consistent state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the overall enabled state of the widget and refresh all child
     * controls accordingly. When {@code enabled} is {@code true} we also
     * re-synchronize the UI via {@link #dnChanged()}.
     *
     * @param enabled  {@code true} to enable the widget, {@code false} to disable it
     */
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;

        if ( enabled )
        {
            this.dnChanged();
        }

        internalSetEnabled();
    }


    // ── METHOD: internalSetEnabled — FINE-TUNING CONTROL STATES ──────────────
    // We apply the enabled flag to each control, honouring the None checkbox
    // logic: if None is checked, the DN combo and Browse button stay disabled
    // regardless of the overall enabled state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We apply the current {@link #enabled} flag to all child controls,
     * respecting the None checkbox state when it is visible.
     */
    private void internalSetEnabled()
    {
        if ( showNoneCheckbox )
        {
            noneCheckbox.setEnabled( enabled );

            if ( dn == null )
            {
                dnCombo.setEnabled( false );
                entryBrowseButton.setEnabled( false );
            }
            else
            {
                dnCombo.setEnabled( enabled );
                entryBrowseButton.setEnabled( ( browserConnection != null ) && enabled );
            }
        }
        else
        {
            dnCombo.setEnabled( enabled );
            entryBrowseButton.setEnabled( ( browserConnection != null ) && enabled );
        }
    }


    // ── METHOD: saveDialogSettings — LOGGING THE VISITED SYSTEM ──────────────
    // We append the current DN text to the shared DN history so it appears in
    // other DN pickers across the Eclipse session.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We save the current DN text into the shared DN history dialog settings,
     * so the value is available as a history suggestion in other DN widgets.
     */
    public void saveDialogSettings()
    {
        HistoryUtils.save( BrowserCommonActivator.getDefault().getDialogSettings(),
            BrowserCommonConstants.DIALOGSETTING_KEY_DN_HISTORY, this.dnCombo.getText() );
    }


    // ── METHOD: getDn — READING THE CURRENT DESTINATION COORDINATES ───────────
    // We return the current DN, but if the None checkbox is checked we return
    // {@code null} — no destination is set. An invalid DN typed by the user also
    // results in {@code null} because the modify listener clears it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the currently selected or entered {@link Dn}, or {@code null}
     * if the None checkbox is checked or the entered text is an invalid DN.
     *
     * @return the current {@link Dn}, or {@code null}
     */
    public Dn getDn()
    {
        if ( showNoneCheckbox && noneCheckbox.getSelection() )
        {
            return null;
        }

        return dn;
    }


    // ── METHOD: getBrowserConnection — READING THE SCOUT'S STATION LINK ───────
    // We return the connection object so callers can determine whether a live
    // LDAP link is available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link IBrowserConnection} this widget uses for live
     * LDAP browsing.
     *
     * @return the browser connection, or {@code null} if none was provided
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── METHOD: setInput — PLOTTING A NEW COURSE ──────────────────────────────
    // We update the internal DN without firing change events (by temporarily
    // removing and re-adding listeners), then call dnChanged() to synchronize
    // the UI. We skip the update if the supplied DN is already the current one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We programmatically set the displayed {@link Dn} without triggering
     * change listener notifications. If the supplied DN is already the current
     * one, we do nothing.
     *
     * @param dn  the new DN to display
     */
    public void setInput( Dn dn )
    {
        if ( this.dn != dn )
        {
            this.dn = dn;
            removeListeners();
            dnChanged();
            addListeners();
        }
    }


    // ── METHOD: getControl — HANDING OVER THE SCOUT'S CONSOLE ────────────────
    // We return the top-level composite so the parent layout can position the
    // entire entry widget as a unit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the top-level {@link Control} (a {@link Composite}) for this
     * widget so the parent layout can size and position it.
     *
     * @return the primary composite control
     */
    public Control getControl()
    {
        return composite;
    }
}
