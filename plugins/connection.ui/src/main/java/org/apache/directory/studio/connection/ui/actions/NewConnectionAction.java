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
import org.apache.directory.studio.connection.ui.wizards.NewConnectionWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.osgi.framework.Bundle;


/**
 * Launches the appropriate New Connection wizard. When more than one connection
 * type is registered via the {@code connectionWizards} extension point a
 * type-picker dialog is shown first; otherwise the single registered wizard
 * opens directly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionAction extends StudioAction
{
    private static final String EP_ID =
        "org.apache.directory.studio.connection.ui.connectionWizards"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     */
    public void run()
    {
        List<IConfigurationElement> wizards = getRegisteredWizards();

        IConfigurationElement choice;
        if ( wizards.isEmpty() )
        {
            // No contributions yet (e.g. first startup) — fall back to built-in LDAP wizard.
            openBuiltInLdapWizard();
            return;
        }
        else if ( wizards.size() == 1 )
        {
            choice = wizards.get( 0 );
        }
        else
        {
            choice = promptForType( wizards );
            if ( choice == null )
            {
                return; // user cancelled
            }
        }

        openWizard( choice );
    }


    private List<IConfigurationElement> getRegisteredWizards()
    {
        List<IConfigurationElement> result = new ArrayList<>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint ep = registry.getExtensionPoint( EP_ID );
        if ( ep != null )
        {
            for ( IConfigurationElement ce : ep.getConfigurationElements() )
            {
                result.add( ce );
            }
        }
        return result;
    }


    private IConfigurationElement promptForType( List<IConfigurationElement> wizards )
    {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
            getShell(), new WizardTypeLabelProvider() );
        dialog.setTitle( Messages.getString( "NewConnectionAction.NewConnection" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "NewConnectionAction.SelectConnectionType" ) ); //$NON-NLS-1$
        dialog.setElements( wizards.toArray() );
        dialog.setInitialSelections( wizards.get( 0 ) );
        dialog.setMultipleSelection( false );

        if ( dialog.open() != Window.OK )
        {
            return null;
        }
        return ( IConfigurationElement ) dialog.getFirstResult();
    }


    private void openWizard( IConfigurationElement ce )
    {
        try
        {
            INewWizard wizard = ( INewWizard ) ce.createExecutableExtension( "class" ); //$NON-NLS-1$
            List<Object> selection = new ArrayList<>();
            selection.addAll( Arrays.asList( getSelectedConnectionFolders() ) );
            selection.addAll( Arrays.asList( getSelectedConnections() ) );
            wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( selection ) );
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            dialog.open();
        }
        catch ( CoreException e )
        {
            ConnectionUIPlugin.getDefault().getLog().error(
                "Failed to open connection wizard", e ); //$NON-NLS-1$
        }
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

    private static final class WizardTypeLabelProvider extends LabelProvider
    {
        private final List<Image> created = new ArrayList<>();


        @Override
        public String getText( Object element )
        {
            return ( ( IConfigurationElement ) element ).getAttribute( "label" ); //$NON-NLS-1$
        }


        @Override
        public Image getImage( Object element )
        {
            String iconPath = ( ( IConfigurationElement ) element ).getAttribute( "icon" ); //$NON-NLS-1$
            if ( iconPath == null )
            {
                return null;
            }
            String contributorId = ( ( IConfigurationElement ) element ).getContributor().getName();
            Bundle bundle = Platform.getBundle( contributorId );
            if ( bundle == null )
            {
                return null;
            }
            URL url = FileLocator.find( bundle, new Path( iconPath ), null );
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
