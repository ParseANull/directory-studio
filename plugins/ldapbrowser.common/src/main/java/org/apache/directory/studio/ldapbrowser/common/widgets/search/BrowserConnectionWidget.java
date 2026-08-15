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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SelectBrowserConnectionDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


// ── CLASS: BrowserConnectionWidget — Han Locks In Hyperspace Coordinates for the Falcon ──
// Han Solo sits at the Millennium Falcon's nav console in the Mos Eisley spaceport,
// Obi-Wan watching over his shoulder. The readout shows the current destination locked in;
// a single button lets him punch up the nav computer and pick a different jump target.
// This widget does the same thing for LDAP: a read-only text field shows the active
// server connection, and a Browse button opens a dialog to select a different one.
// ──────────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for picking an {@link IBrowserConnection} — the live LDAP server
 * connection that a search will run against. It renders as a read-only text field
 * (showing the connection name) next to a Browse button that opens
 * {@link SelectBrowserConnectionDialog}.
 * Think of this class as Han's nav console: the text field shows the locked-in
 * destination; the button lets you dial a new one.
 * Used by {@link SearchPageWrapper} at the top of every search form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConnectionWidget extends AbstractWidget
{

    /** The connection text, displays the selected connection */
    private Text browserConnectionText;

    /** The connection browse button, opens the dialog */
    private Button connectionBrowseButton;

    /** The selected connection */
    private IBrowserConnection selectedBrowserConnection;


    // ── Han Punches In the Alderaan Coordinates Before Take-Off ─────────────────────
    // Obi-Wan hands Han the destination: "Alderaan — and step on it."
    // Han loads the coordinates into the nav computer so they're ready the moment
    // the widget renders, saving the user from having to pick the connection manually.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget that starts with a specific connection already selected.
     * Use this when you know which LDAP server the dialog should point at —
     * for example, when opening the properties of an existing saved search.
     *
     * <p>For example — Han pre-loads the destination:</p>
     * <pre>
     *   navComputer.setDestination( Alderaan );
     *   // text field shows "Alderaan" immediately on render
     * </pre>
     *
     * @param connection  The {@link IBrowserConnection} to show when the widget first
     *                   appears. May be null if you'd rather start blank.
     */
    public BrowserConnectionWidget( IBrowserConnection connection )
    {
        this.selectedBrowserConnection = connection;
    }


    // ── Han Fires Up the Falcon With No Destination Set ──────────────────────────────
    // Sometimes Han just wants to get off Tatooine and figure out the jump coordinates
    // later. The nav computer is blank; he'll dial in the destination when he's ready.
    // We start with null so the user must pick a connection before searching.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget with no connection pre-selected. The user must press Browse
     * and pick one before the form becomes valid. Handy for brand-new search dialogs.
     *
     * <p>For example — Han takes off with an empty nav computer:</p>
     * <pre>
     *   navComputer.setDestination( null ); // user picks later
     * </pre>
     */
    public BrowserConnectionWidget()
    {
        this.selectedBrowserConnection = null;
    }


    // ── Han Builds the Nav Console Panel on the Falcon's Dashboard ───────────────────
    // Han snaps the nav computer display and the "Set Coordinates" button into the
    // dashboard. When the button is pressed, the nav computer dialog slides open and
    // Han picks a new destination; the display updates and the rest of the ship knows.
    // We create the read-only text field and the Browse button, wiring the button to
    // open SelectBrowserConnectionDialog and call notifyListeners() on success.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT controls — a read-only text field and a "Browse..." button —
     * inside the given parent composite. Call this exactly once after construction.
     * The Browse button opens {@link SelectBrowserConnectionDialog}; picking a connection
     * there updates the text field and fires change events to the parent form.
     *
     * <p>For example — Han wires up his nav console:</p>
     * <pre>
     *   displayPanel = new ReadOnlyText( dashboard );
     *   browseButton = new Button( "Set Coordinates" );
     *   browseButton.onClick( () -> openNavComputerDialog() );
     * </pre>
     *
     * @param parent  The SWT composite that will host these controls.
     *                Must have a suitable grid layout already applied.
     */
    public void createWidget( final Composite parent )
    {
        // Text
        browserConnectionText = BaseWidgetUtils.createReadonlyText( parent, "", 1 ); //$NON-NLS-1$

        // Button
        connectionBrowseButton = BaseWidgetUtils.createButton( parent, Messages
            .getString( "BrowserConnectionWidget.BrowseButton" ), 1 ); //$NON-NLS-1$
        connectionBrowseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                SelectBrowserConnectionDialog dialog = new SelectBrowserConnectionDialog( parent.getShell(), Messages
                    .getString( "BrowserConnectionWidget.SelectConnection" ), selectedBrowserConnection ); //$NON-NLS-1$
                dialog.open();
                IBrowserConnection browserConnection = dialog.getSelectedBrowserConnection();
                if ( browserConnection != null )
                {
                    setBrowserConnection( browserConnection );
                    notifyListeners();
                }
            }
        } );

        // initial values
        setBrowserConnection( selectedBrowserConnection );
    }


    // ── Han Reads the Destination off the Nav Display ────────────────────────────────
    // Chewie asks "Where are we headed?" Han glances at the nav readout and reports back.
    // We simply return whichever IBrowserConnection is currently stored in the field.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected {@link IBrowserConnection}, or {@code null} if
     * none has been chosen yet. The search form calls this to know which server to
     * run the search against.
     *
     * <p>For example — Han checks the nav readout:</p>
     * <pre>
     *   destination = navComputer.getCurrentDestination();
     *   // "Alderaan" — or null if not set
     * </pre>
     *
     * @return  The active connection, or {@code null} if the user hasn't picked one.
     */
    public IBrowserConnection getBrowserConnection()
    {
        return selectedBrowserConnection;
    }


    // ── Han Dials In a New Jump Destination ──────────────────────────────────────────
    // Leia says "Change course — we're going to Bespin instead."
    // Han punches the new coordinates into the nav computer and the display refreshes.
    // We store the new connection and update the text field with its display name.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the active connection and refreshes the text field to
     * show its name. Use this to initialise or override the selection from outside
     * the widget — for example, when loading a saved search that has a specific server.
     *
     * <p>For example — Han enters new coordinates mid-flight:</p>
     * <pre>
     *   navComputer.setDestination( Bespin );
     *   display.setText( "Bespin" );
     * </pre>
     *
     * @param connection  The connection to select. If null, the text field is cleared.
     */
    public void setBrowserConnection( IBrowserConnection connection )
    {
        selectedBrowserConnection = connection;
        browserConnectionText.setText( selectedBrowserConnection != null
            && selectedBrowserConnection.getConnection() != null ? selectedBrowserConnection.getConnection().getName()
            : "" ); //$NON-NLS-1$
    }


    // ── Han Powers Down the Nav Console ──────────────────────────────────────────────
    // When the Falcon is docked in the Death Star bay and nothing can fly, Han flips
    // the console to standby — the display dims and the button stops responding.
    // We propagate the enabled/disabled state to both the text field and the button.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the widget controls in one shot. When disabled, the text
     * field and the Browse button are both greyed out and non-interactive. Useful
     * when the form enters a read-only mode (e.g. viewing a running search's details).
     *
     * <p>For example — Han powers down the console:</p>
     * <pre>
     *   navDisplay.setEnabled( false );
     *   setCoordinatesButton.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to make the controls interactive; {@code false} to grey them.
     */
    public void setEnabled( boolean b )
    {
        browserConnectionText.setEnabled( b );
        connectionBrowseButton.setEnabled( b );
    }

}
