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
package org.apache.directory.studio.scim.ui.wizards;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ScimConnectionPage extends WizardPage
{
    private Text connectionNameText;
    private Text baseUrlText;
    private Combo versionCombo;
    private Combo providerCombo;

    private final List<String> providerIds = new ArrayList<>();


    public ScimConnectionPage()
    {
        super( "ScimConnectionPage" );
        setTitle( "SCIM Connection" );
        setDescription( "Enter the connection details for the SCIM server." );
    }


    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );

        new Label( composite, SWT.NONE ).setText( "Connection Name:" );
        connectionNameText = new Text( composite, SWT.BORDER );
        connectionNameText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        connectionNameText.addModifyListener( new ModifyListener()
        {
            @Override
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        new Label( composite, SWT.NONE ).setText( "Base URL:" );
        baseUrlText = new Text( composite, SWT.BORDER );
        baseUrlText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        baseUrlText.addModifyListener( new ModifyListener()
        {
            @Override
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        new Label( composite, SWT.NONE ).setText( "SCIM Version:" );
        versionCombo = new Combo( composite, SWT.READ_ONLY );
        versionCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        versionCombo.setItems( new String[]{ "V1.1", "V2.0" } );
        versionCombo.select( 1 );

        new Label( composite, SWT.NONE ).setText( "Provider Preset:" );
        providerCombo = new Combo( composite, SWT.READ_ONLY );
        providerCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        populateProviders();

        setControl( composite );
        setPageComplete( false );
    }


    private void populateProviders()
    {
        providerIds.clear();
        List<String> labels = new ArrayList<>();
        labels.add( "(Generic)" );
        providerIds.add( "" );

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint( "org.apache.directory.studio.scimAdapters" );
        if ( point != null )
        {
            for ( IExtension ext : point.getExtensions() )
            {
                for ( IConfigurationElement el : ext.getConfigurationElements() )
                {
                    if ( "scimAdapter".equals( el.getName() ) )
                    {
                        String id = el.getAttribute( "id" );
                        String label = el.getAttribute( "name" );
                        if ( label == null || label.isEmpty() )
                        {
                            label = id;
                        }
                        providerIds.add( id != null ? id : "" );
                        labels.add( label != null ? label : "" );
                    }
                }
            }
        }

        providerCombo.setItems( labels.toArray( new String[0] ) );
        providerCombo.select( 0 );
    }


    private void validate()
    {
        String name = connectionNameText.getText().trim();
        String url = baseUrlText.getText().trim();
        if ( name.isEmpty() )
        {
            setErrorMessage( "Connection name must not be empty." );
            setPageComplete( false );
            return;
        }
        if ( url.isEmpty() )
        {
            setErrorMessage( "Base URL must not be empty." );
            setPageComplete( false );
            return;
        }
        if ( !url.startsWith( "http" ) )
        {
            setErrorMessage( "Base URL must start with 'http'." );
            setPageComplete( false );
            return;
        }
        setErrorMessage( null );
        setPageComplete( true );
    }


    public String getConnectionName()
    {
        return connectionNameText.getText().trim();
    }


    public String getBaseUrl()
    {
        return baseUrlText.getText().trim();
    }


    public String getScimVersion()
    {
        int idx = versionCombo.getSelectionIndex();
        return idx == 0 ? "V1_1" : "V2_0";
    }


    public String getProviderId()
    {
        int idx = providerCombo.getSelectionIndex();
        if ( idx >= 0 && idx < providerIds.size() )
        {
            return providerIds.get( idx );
        }
        return "";
    }
}
