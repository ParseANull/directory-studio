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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class ScimCapabilityPage extends WizardPage
{
    private Button supportsDiscoveryButton;
    private Button supportsBulkButton;
    private Button bulkVendorSetButton;
    private Combo membershipModelCombo;
    private Button membershipModelVendorSetButton;
    private Combo paginationStyleCombo;
    private Button supportsFilterButton;
    private Button supportsSortingButton;
    private Button supportsETagButton;


    public ScimCapabilityPage()
    {
        super( "ScimCapabilityPage" );
        setTitle( "Provider Capabilities" );
        setDescription( "Configure the capability profile for the SCIM provider." );
    }


    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );

        new Label( composite, SWT.NONE ).setText( "Supports Discovery:" );
        supportsDiscoveryButton = new Button( composite, SWT.CHECK );

        new Label( composite, SWT.NONE ).setText( "Supports Bulk:" );
        supportsBulkButton = new Button( composite, SWT.CHECK );

        new Label( composite, SWT.NONE ).setText( "Bulk Vendor-Set:" );
        bulkVendorSetButton = new Button( composite, SWT.CHECK );
        bulkVendorSetButton.setEnabled( false );

        new Label( composite, SWT.NONE ).setText( "Membership Model:" );
        membershipModelCombo = new Combo( composite, SWT.READ_ONLY );
        membershipModelCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        membershipModelCombo.setItems( new String[]{ "USER_HAS_GROUPS", "GROUP_HAS_MEMBERS", "BOTH" } );
        membershipModelCombo.select( 0 );

        new Label( composite, SWT.NONE ).setText( "Membership Model Vendor-Set:" );
        membershipModelVendorSetButton = new Button( composite, SWT.CHECK );
        membershipModelVendorSetButton.setEnabled( false );

        new Label( composite, SWT.NONE ).setText( "Pagination Style:" );
        paginationStyleCombo = new Combo( composite, SWT.READ_ONLY );
        paginationStyleCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        paginationStyleCombo.setItems( new String[]{ "INDEX_BASED", "CURSOR_BASED" } );
        paginationStyleCombo.select( 0 );

        new Label( composite, SWT.NONE ).setText( "Supports Filter:" );
        supportsFilterButton = new Button( composite, SWT.CHECK );

        new Label( composite, SWT.NONE ).setText( "Supports Sorting:" );
        supportsSortingButton = new Button( composite, SWT.CHECK );

        new Label( composite, SWT.NONE ).setText( "Supports ETag:" );
        supportsETagButton = new Button( composite, SWT.CHECK );

        new Label( composite, SWT.NONE );
        Button discoverButton = new Button( composite, SWT.PUSH );
        discoverButton.setText( "Discover" );
        discoverButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                MessageDialog.openInformation( getShell(), "Discovery", "Discovery not yet available." );
            }
        } );

        setControl( composite );
        setPageComplete( true );
    }


    public String getCapabilityProfileJson()
    {
        return "{\"scimVersion\":\"" + "V2_0"
            + "\",\"supportsDiscovery\":" + supportsDiscoveryButton.getSelection()
            + ",\"supportsBulk\":" + supportsBulkButton.getSelection()
            + ",\"bulkVendorSet\":" + bulkVendorSetButton.getSelection()
            + ",\"membershipModel\":\"" + membershipModelCombo.getText() + "\""
            + ",\"membershipModelVendorSet\":" + membershipModelVendorSetButton.getSelection()
            + ",\"paginationStyle\":\"" + paginationStyleCombo.getText() + "\""
            + ",\"supportsFilter\":" + supportsFilterButton.getSelection()
            + ",\"supportsSorting\":" + supportsSortingButton.getSelection()
            + ",\"supportsETag\":" + supportsETagButton.getSelection()
            + "}";
    }
}
