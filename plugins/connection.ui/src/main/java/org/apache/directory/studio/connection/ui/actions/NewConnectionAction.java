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

package org.apache.directory.studio.connection.ui.actions;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.IConnectionTypeContribution;
import org.apache.directory.studio.connection.ui.wizards.NewConnectionWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;


/**
 * Launches the appropriate New Connection wizard. When more than one connection
 * type is registered as an OSGi {@link IConnectionTypeContribution} service, a
 * type-picker dialog is shown first; otherwise the single registered wizard
 * opens directly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionAction extends StudioAction
{
    /**
     * {@inheritDoc}
     */
    public void run()
    {
        List<IConnectionTypeContribution> contributions =
            ConnectionUIPlugin.getDefault().getConnectionTypeContributions();

        IConnectionTypeContribution choice;
        if ( contributions.isEmpty() )
        {
            openBuiltInLdapWizard();
            return;
        }
        else if ( contributions.size() == 1 )
        {
            choice = contributions.get( 0 );
        }
        else
        {
            choice = promptForType( contributions );
            if ( choice == null )
            {
                return;
            }
        }

        openWizard( choice );
    }


    private IConnectionTypeContribution promptForType( List<IConnectionTypeContribution> contributions )
    {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
            getShell(), new ContributionLabelProvider() );
        dialog.setTitle( Messages.getString( "NewConnectionAction.NewConnection" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "NewConnectionAction.SelectConnectionType" ) ); //$NON-NLS-1$
        dialog.setElements( contributions.toArray() );
        dialog.setInitialSelections( contributions.get( 0 ) );
        dialog.setMultipleSelection( false );

        if ( dialog.open() != Window.OK )
        {
            return null;
        }
        return ( IConnectionTypeContribution ) dialog.getFirstResult();
    }


    private void openWizard( IConnectionTypeContribution contribution )
    {
        List<Object> selection = new ArrayList<>();
        selection.addAll( Arrays.asList( getSelectedConnectionFolders() ) );
        selection.addAll( Arrays.asList( getSelectedConnections() ) );
        INewWizard wizard = contribution.createWizard();
        wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( selection ) );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    private void openBuiltInLdapWizard()
    {
        List<Object> selection = new ArrayList<>();
        selection.addAll( Arrays.asList( getSelectedConnectionFolders() ) );
        selection.addAll( Arrays.asList( getSelectedConnections() ) );
        NewConnectionWizard wizard = new NewConnectionWizard();
        wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( selection ) );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // --- StudioAction overrides -----------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        return Messages.getString( "NewConnectionAction.NewConnection" ); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_CONNECTION_ADD );
    }


    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnabled()
    {
        return true;
    }


    // --- Inner label provider for the type-picker dialog ----------------------

    private static final class ContributionLabelProvider extends LabelProvider
    {
        private final List<Image> created = new ArrayList<>();


        @Override
        public String getText( Object element )
        {
            return ( ( IConnectionTypeContribution ) element ).getLabel();
        }


        @Override
        public Image getImage( Object element )
        {
            URL url = ( ( IConnectionTypeContribution ) element ).getIconUrl();
            if ( url == null )
            {
                return null;
            }
            Image img = ImageDescriptor.createFromURL( url ).createImage( false );
            if ( img != null )
            {
                created.add( img );
            }
            return img;
        }


        @Override
        public void dispose()
        {
            for ( Image img : created )
            {
                img.dispose();
            }
            created.clear();
            super.dispose();
        }
    }
}
