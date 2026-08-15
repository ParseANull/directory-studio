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


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ScimSchemaPage extends WizardPage
{
    private Text schemaText;


    public ScimSchemaPage()
    {
        super( "ScimSchemaPage" );
        setTitle( "Schemas" );
        setDescription( "Optionally review or discover schemas for the SCIM provider." );
    }


    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 1, false ) );

        Label noteLabel = new Label( composite, SWT.WRAP );
        noteLabel.setText( "Schemas will be auto-discovered on connect." );
        noteLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        schemaText = new Text( composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
        schemaText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        schemaText.setEditable( false );

        Button discoverButton = new Button( composite, SWT.PUSH );
        discoverButton.setText( "Discover Schemas" );
        discoverButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        discoverButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                MessageDialog.openInformation( getShell(), "Discover Schemas",
                    "Schema discovery is not yet available. Schemas will be auto-discovered on connect." );
            }
        } );

        setControl( composite );
        setPageComplete( true );
    }
}
