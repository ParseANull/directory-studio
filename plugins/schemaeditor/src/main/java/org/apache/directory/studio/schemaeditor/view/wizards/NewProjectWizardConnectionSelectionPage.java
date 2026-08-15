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
package org.apache.directory.studio.schemaeditor.view.wizards;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.ui.widgets.ConnectionActionGroup;
import org.apache.directory.studio.connection.ui.widgets.ConnectionConfiguration;
import org.apache.directory.studio.connection.ui.widgets.ConnectionUniversalListener;
import org.apache.directory.studio.connection.ui.widgets.ConnectionWidget;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


// ── CLASS: NewProjectWizardConnectionSelectionPage — HAN AT THE FALCON CONSOLE ─
// Han Solo sits at the Millennium Falcon's navigation console in Mos Eisley,
// scanning the list of registered jump coordinates.  He has to pick one specific
// destination before he can punch it — he can't just "go to space" in general.
// This page works the same way: we show the user a list of all their registered
// LDAP connections and ask them to pick one before we can fetch the live schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard page where the user picks which LDAP connection to use for an online project.
 * An "online project" reads its schema directly from a live LDAP server, so we
 * need to know which server to connect to — that's what this page collects.
 * Think of this page as Han Solo's nav console: all the known hyperspace routes
 * (connections) are listed and he — or rather the user — has to select one
 * before we can jump to lightspeed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewProjectWizardConnectionSelectionPage extends AbstractWizardPage
{
    private ConnectionConfiguration configuration;
    private ConnectionUniversalListener universalListener;

    // UI Fields
    private ConnectionWidget connectionWidget;
    private ConnectionActionGroup actionGroup;


    // ── Han Powers Up The Navigation Console ─────────────────────────────────
    // Han flips on the Falcon's navigation computer, setting the page title on
    // its screen and noting that no destination has been selected yet.
    // We do the same here: initialize the wizard page with its title, description
    // and icon, and mark it incomplete until the user picks a connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this connection-selection wizard page.
     * Sets the page title, description, and icon, and marks the page as
     * incomplete so the wizard won't let the user proceed without picking one.
     *
     * <p>For example — Han fires up the nav computer in Mos Eisley:</p>
     * <pre>
     *   title       = "Create Schema Project"
     *   description = "Please select a connection to use for this project."
     *   pageComplete = false  // "I don't have coordinates yet, Chewie."
     * </pre>
     */
    protected NewProjectWizardConnectionSelectionPage()
    {
        super( "NewProjectWizardConnectionSelectionPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewProjectWizardConnectionSelectionPage.CreateSchemaProject" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewProjectWizardConnectionSelectionPage.PleaseSelectConnection" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_NEW_WIZARD ) );
        setPageComplete( false );
    }


    // ── Han Lays Out The Star Charts On The Console ──────────────────────────
    // Han spreads the holographic star charts across the Falcon's navigation
    // table — every known system is visible, colour-coded and interactive.  He
    // wires up the selection sensor so the moment he touches a system the
    // readout updates.
    // We build the connection-picker widget here and hook up a listener so that
    // picking a connection immediately triggers validation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page — specifically the {@link ConnectionWidget}
     * that lists all registered LDAP connections the user can choose from.
     * We also wire up a selection-changed listener so that the page validates
     * itself (and enables/disables "Next") every time the selection changes.
     *
     * <p>For example — Han wires the star-chart display to the selection sensor:</p>
     * <pre>
     *   connectionWidget = new ConnectionWidget( configuration, null );
     *   connectionWidget.setInput( connectionFolderManager );
     *   connectionWidget.addSelectionChangedListener( e -> validatePage() );
     * </pre>
     *
     * @param parent  the SWT container Eclipse gives us to put our widgets inside
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout() );

        // Choose A Connection Label
        Label label = new Label( composite, SWT.NONE );
        label.setText( Messages.getString( "NewProjectWizardConnectionSelectionPage.ChooseConnection" ) ); //$NON-NLS-1$
        label.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Creating configuration
        configuration = new ConnectionConfiguration();

        // Creating Connection Widget
        connectionWidget = new ConnectionWidget( configuration, null );
        connectionWidget.createWidget( composite );
        connectionWidget.setInput( ConnectionCorePlugin.getDefault().getConnectionFolderManager() );

        connectionWidget.getViewer().addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                validatePage();
            }
        } );

        // creating the listener
        universalListener = new ConnectionUniversalListener( connectionWidget.getViewer() );

        // create actions and context menu (and register global actions)
        actionGroup = new ConnectionActionGroup( connectionWidget, configuration );
        actionGroup.fillToolBar( connectionWidget.getToolBarManager() );
        actionGroup.fillMenu( connectionWidget.getMenuManager() );
        actionGroup.fillContextMenu( connectionWidget.getContextMenuManager() );
        actionGroup.activateGlobalActionHandlers();

        initFields();

        setControl( composite );
    }


    // ── The Nav Computer Boots To A Blank Slate ──────────────────────────────
    // When Han first turns on the nav computer it shows no destination selected
    // — the screen is clear and the jump-readiness light is red.
    // We mirror that: clear any stale error messages and mark the page as
    // incomplete so the wizard won't advance without a selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the page to its blank initial state — no error shown, no
     * connection selected, page marked incomplete.
     * We call this right after building the widgets so the page starts in a
     * clean, consistent state regardless of what Eclipse remembers from a
     * previous run.
     *
     * <p>For example — the nav computer resets to standby:</p>
     * <pre>
     *   displayErrorMessage( null );   // clear the error banner
     *   setPageComplete( false );      // "Still waiting for coordinates, Captain."
     * </pre>
     */
    private void initFields()
    {
        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── Han Checks The Jump Coordinates Are Valid ────────────────────────────
    // Before Han punches the hyperdrive, the nav computer cross-checks the
    // entered coordinates against its database.  If nothing is entered, a
    // warning flashes: "No destination selected — can't make the jump."
    // We do the same: inspect the current selection in the connection widget
    // and show an error if nothing is picked.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the page state and updates the error banner and page-complete flag.
     * If no connection is selected in the widget we show an error telling the
     * user they need to pick one before proceeding.
     * Called every time the selection in the connection list changes.
     *
     * <p>For example — the nav computer warns when no system is highlighted:</p>
     * <pre>
     *   if ( selection.isEmpty() ) {
     *       displayErrorMessage( "No connection selected — can't jump!" );
     *   } else {
     *       displayErrorMessage( null );  // all clear
     *   }
     * </pre>
     */
    private void validatePage()
    {
        ISelection selection = connectionWidget.getViewer().getSelection();
        if ( selection.isEmpty() )
        {
            displayErrorMessage( Messages
                .getString( "NewProjectWizardConnectionSelectionPage.ErrorNoConnectionSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── Han Reads The Selected Destination Off The Screen ───────────────────
    // Han glances at the nav computer and reads off the selected hyperspace
    // coordinates to Chewie: "Alderaan system, sector 7G — punch it."
    // We do the same: pull the first selected item from the connection widget
    // and return it as a typed {@link Connection} object.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection the user selected in the connection list widget.
     * The wizard's {@code performFinish()} calls this to know which server to
     * connect to when fetching the live schema.
     *
     * <p>For example — Han reads the chosen destination off the nav screen:</p>
     * <pre>
     *   Connection dest = connectionWidget.getViewer().getSelection().getFirstElement();
     *   // "Alderaan — got it.  Chewie, make the jump!"
     * </pre>
     *
     * @return the {@link Connection} the user selected; {@code null} if nothing is selected
     */
    public Connection getSelectedConnection()
    {
        return ( Connection ) ( ( StructuredSelection ) connectionWidget.getViewer().getSelection() ).getFirstElement();
    }


    // ── Han Powers Down The Navigation Console ───────────────────────────────
    // After arriving at Alderaan (or finding its debris), Han shuts down the
    // nav computer — releasing the nav charts, the sensor listeners, and the
    // action shortcuts so they don't linger in memory.
    // We do the same: dispose every UI resource this page created so we don't
    // leak SWT handles.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up all resources this page allocated when it was built.
     * SWT and JFace resources like configurations, listeners, and action groups
     * don't get garbage-collected automatically — we have to dispose them
     * explicitly or Eclipse leaks widget handles.
     *
     * <p>For example — Han powers down all nav systems after landing:</p>
     * <pre>
     *   actionGroup.dispose();       // cut the action shortcuts
     *   configuration.dispose();     // release the configuration model
     *   universalListener.dispose(); // stop listening to connection changes
     *   connectionWidget.dispose();  // tear down the widget tree
     * </pre>
     */
    public void dispose()
    {
        if ( configuration != null )
        {
            actionGroup.dispose();
            actionGroup = null;
            configuration.dispose();
            configuration = null;
            universalListener.dispose();
            universalListener = null;
            connectionWidget.dispose();
            connectionWidget = null;
        }

        super.dispose();
    }
}
