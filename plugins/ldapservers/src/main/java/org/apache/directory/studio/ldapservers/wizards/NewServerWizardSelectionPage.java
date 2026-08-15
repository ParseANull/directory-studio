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
package org.apache.directory.studio.ldapservers.wizards;


import java.util.regex.Pattern;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.LdapServersPlugin;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;


// ── CLASS: NewServerWizardSelectionPage — THE PARTS CATALOGUE AND REQUISITION FORM ────────
// Step 1 of the Imperial requisition process: the officer browses the parts catalogue tree
// (filterable by keyword), picks a specific server type, and types in a name for the new
// installation.  Validation blocks Finish if no type is selected or the name is blank or taken.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Page 1 of the New Server Wizard.
 * Shows a filterable tree of available {@link LdapServerAdapterExtension}s grouped by vendor,
 * and a text field for the new server's name.
 * Auto-suggests a name when an adapter is selected; validates that the name is non-empty and
 * unique before allowing Finish.
 * Think of it as the Imperial parts catalogue and requisition form combined.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerWizardSelectionPage extends WizardPage
{
    /** The servers handler */
    private LdapServersManager ldapServersManager;

    /** The content provider of the TreeViewer */
    private LdapServerAdapterExtensionsContentProvider contentProvider;

    /** The label provider of the TreeViewer */
    private LdapServerAdapterExtensionsLabelProvider labelProvider;

    // UI fields
    private Label filterLabel;
    private Text filterText;
    private TreeViewer ldapServerAdaptersTreeViewer;
    private Text serverNameText;


    // ── Opening The Requisition Form ──────────────────────────────────────────────────────────
    // The officer opens the form and sees: "Create an LDAP Server — choose the type and name."
    // The form starts incomplete until both a server type and a valid name are entered.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the page and sets its title, description, banner image, and initial incomplete state.
     * Grabs a reference to {@link LdapServersManager} for name-uniqueness checking.
     */
    public NewServerWizardSelectionPage()
    {
        super( NewServerWizardSelectionPage.class.getCanonicalName() );
        setTitle( Messages.getString( "NewServerWizardSelectionPage.CreateAnLdapServer" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewServerWizardSelectionPage.ChooseTypeOfServerAndSpecifyName" ) ); //$NON-NLS-1$
        setImageDescriptor( LdapServersPlugin.getDefault().getImageDescriptor(
            LdapServersPluginConstants.IMG_SERVER_NEW_WIZARD ) );
        setPageComplete( false );
        ldapServersManager = LdapServersManager.getDefault();
    }


    // ── Building The Page's Widget Tree ──────────────────────────────────────────────────────
    // The page has three sections: a filter text box (to narrow the catalogue), a tree viewer
    // showing all available server types, and a server-name text field.
    // A live filter re-applies on every keystroke using a regex pattern match.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page's widgets: filter text, filterable adapter tree, and server name field.
     * Registers all listeners via {@link #addListeners()} and sets the page's control.
     *
     * @param parent  the parent composite provided by the wizard container
     */
    public void createControl( Composite parent )
    {
        // Creating the composite to hold the UI
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );

        // Filter Label
        filterLabel = BaseWidgetUtils.createLabel( composite,
            Messages.getString( "NewServerWizardSelectionPage.SelectServerType" ), 2 ); //$NON-NLS-1$
        filterLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Filter Text
        filterText = new Text( composite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL );
        filterText.setMessage( Messages.getString( "NewServerWizardSelectionPage.TypeFilterHere" ) ); //$NON-NLS-1$
        filterText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // LDAP Server Adapters Tree Viewer
        ldapServerAdaptersTreeViewer = new TreeViewer( new Tree( composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.BORDER ) );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 );
        gd.heightHint = 90;
        ldapServerAdaptersTreeViewer.getTree().setLayoutData( gd );
        contentProvider = new LdapServerAdapterExtensionsContentProvider();
        ldapServerAdaptersTreeViewer.setContentProvider( contentProvider );
        labelProvider = new LdapServerAdapterExtensionsLabelProvider();
        ldapServerAdaptersTreeViewer.setLabelProvider( labelProvider );
        ldapServerAdaptersTreeViewer.setInput( "LDAP Server Adapters Tree Viewer Input" ); //$NON-NLS-1$
        ldapServerAdaptersTreeViewer.expandAll();
        ldapServerAdaptersTreeViewer.addFilter( new ViewerFilter()
        {
            public boolean select( Viewer viewer, Object parentElement, Object element )
            {
                // The current element is a Vendor
                if ( element instanceof String )
                {
                    Object[] children = contentProvider.getChildren( element );
                    for ( Object child : children )
                    {
                        String label = labelProvider.getText( child );

                        return getFilterPattern().matcher( label ).matches();
                    }
                }
                // The current element is an LdapServerAdapterExtension
                else if ( element instanceof LdapServerAdapterExtension )
                {
                    String label = labelProvider.getText( element );

                    return getFilterPattern().matcher( label ).matches();
                }

                return false;
            }


            /**
             * Gets the filter pattern.
             *
             * @return
             *      the filter pattern
             */
            private Pattern getFilterPattern()
            {
                String filter = filterText.getText();

                return Pattern.compile( ( ( filter == null ) ? ".*" : ".*" + filter + ".*" ), Pattern.CASE_INSENSITIVE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } );

        // Filler
        Label filler = new Label( composite, SWT.NONE );
        filler.setLayoutData( new GridData( SWT.NONE, SWT.NONE, true, false, 2, 1 ) );

        // Server Name Label
        BaseWidgetUtils.createLabel( composite, Messages.getString( "NewServerWizardSelectionPage.ServerName" ), 1 ); //$NON-NLS-1$

        // Server Name Text
        serverNameText = BaseWidgetUtils.createText( composite, "", 1 ); //$NON-NLS-1$

        // Adding listeners
        addListeners();

        // Setting the control on the composite and setting focus
        setControl( composite );
        composite.setFocus();
    }


    // ── Wiring Up The Live-Validation Listeners ───────────────────────────────────────────────
    // Three things trigger validation: typing in the filter box (re-applies the filter but
    // doesn't change selection), selecting a server type (auto-fills the name), and typing
    // in the server-name field (checks non-empty and uniqueness).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers modify and selection listeners on the filter text, adapter tree, and name field.
     * The filter listener refreshes the tree on each keystroke.
     * The selection listener auto-generates a unique name and re-validates.
     * The name listener re-validates on each keystroke.
     */
    private void addListeners()
    {
        // Filter Text
        filterText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                // Refreshing the LDAP Server Adapters Tree Viewer
                ldapServerAdaptersTreeViewer.refresh();
                ldapServerAdaptersTreeViewer.expandAll();
            }
        } );

        // LDAP Server Adapters Tree Viewer
        ldapServerAdaptersTreeViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                // Assigning an automatic name to the LDAP Server based on the selected LDAP Server Adapter Extension
                serverNameText.setText( getServerName( ( StructuredSelection ) ldapServerAdaptersTreeViewer
                    .getSelection() ) );

                //                getContainer().updateButtons();

                validate();
            }


            /**
             * Get a name for the server based on the current selection.
             *
             * @param selection
             *      the current selection
             * @return
             *      a name for the server based on the current selection
             */
            private String getServerName( StructuredSelection selection )
            {
                if ( !selection.isEmpty() )
                {
                    Object selectedObject = selection.getFirstElement();
                    if ( selectedObject instanceof LdapServerAdapterExtension )
                    {
                        // Getting the name of the LDAP Server Adapter Extension
                        String serverName = labelProvider.getText( selection.getFirstElement() );

                        // Checking if the name if available
                        if ( ldapServersManager.isNameAvailable( serverName ) )
                        {
                            return serverName;
                        }
                        else
                        {
                            // The name is not available, looking for another name
                            String newServerName = serverName;

                            for ( int i = 2; !ldapServersManager.isNameAvailable( newServerName ); i++ )
                            {
                                newServerName = serverName + " (" + i + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                            }

                            return newServerName;
                        }
                    }
                }

                // Returning an empty string if the selection is empty or if the current selection is a vendor
                return ""; //$NON-NLS-1$
            }
        } );

        // Server Name Text
        serverNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );
    }


    // ── Validating The Form Before Finish Is Allowed ──────────────────────────────────────────
    // The requisition can't be filed until: (a) a server type leaf is selected, and (b) the
    // name is non-empty and not already taken by another server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the selection and name fields.
     * Sets an error message and marks the page incomplete if:
     * <ul>
     *   <li>No adapter extension is selected (or only a vendor heading is selected).</li>
     *   <li>The server name is empty.</li>
     *   <li>The server name is already taken by another server.</li>
     * </ul>
     * Clears the error and marks the page complete when all checks pass.
     */
    private void validate()
    {
        displayErrorMessage( null );

        // LDAP Server Adapters Tree Viewer
        StructuredSelection selection = ( StructuredSelection ) ldapServerAdaptersTreeViewer.getSelection();
        if ( selection.isEmpty() )
        {
            displayErrorMessage( Messages.getString( "NewServerWizardSelectionPage.ChooseTypeOfServerToCreate" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            Object selectedObject = selection.getFirstElement();
            if ( selectedObject instanceof String )
            {
                displayErrorMessage( Messages.getString( "NewServerWizardSelectionPage.ChooseTypeOfServerToCreate" ) ); //$NON-NLS-1$
                return;
            }
        }

        // Server Name Text
        String name = serverNameText.getText();
        if ( ( name != null ) )
        {
            if ( "".equals( name ) ) //$NON-NLS-1$
            {
                displayErrorMessage( Messages.getString( "NewServerWizardSelectionPage.EnterANameForLdapServer" ) ); //$NON-NLS-1$
                return;
            }
            if ( !ldapServersManager.isNameAvailable( name ) )
            {
                displayErrorMessage( Messages
                    .getString( "NewServerWizardSelectionPage.LdapServerWithSameNameAlreadyExists" ) ); //$NON-NLS-1$
                return;
            }
        }
    }


    // ── Showing Or Clearing The Error Banner ─────────────────────────────────────────────────
    // Any validation failure drives an error message into the banner and disables Finish.
    // Passing null clears the banner and re-enables Finish.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Displays an error message in the wizard page header banner and marks the page incomplete.
     * Passing {@code null} clears the error and marks the page complete.
     *
     * @param message  the error text, or {@code null} to clear
     */
    protected void displayErrorMessage( String message )
    {
        setErrorMessage( message );
        setPageComplete( message == null );
    }


    // ── Reading Back The Entered Server Name ─────────────────────────────────────────────────
    // Called by NewServerWizard.performFinish() to get the name to assign to the new server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server name the user has typed into the name field.
     *
     * @return the server name string (may be empty if the user hasn't typed one yet)
     */
    public String getServerName()
    {
        return serverNameText.getText();
    }


    // ── Reading Back The Selected Adapter Extension ────────────────────────────────────────────
    // Called by NewServerWizard.performFinish() and getConfigurationPage() to get the adapter.
    // Returns null if the selection is empty or if only a vendor heading is selected.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapServerAdapterExtension} currently selected in the tree,
     * or {@code null} if nothing is selected or only a vendor string is selected.
     *
     * @return the selected extension, or {@code null}
     */
    public LdapServerAdapterExtension getLdapServerAdapterExtension()
    {
        StructuredSelection selection = ( StructuredSelection ) ldapServerAdaptersTreeViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            Object selectedObject = selection.getFirstElement();
            if ( selectedObject instanceof LdapServerAdapterExtension )
            {
                return ( LdapServerAdapterExtension ) selectedObject;
            }
        }

        return null;
    }
}
