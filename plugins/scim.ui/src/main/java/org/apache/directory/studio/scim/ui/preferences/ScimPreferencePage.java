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
package org.apache.directory.studio.scim.ui.preferences;


import org.apache.directory.studio.scim.ui.ScimUIConstants;
import org.apache.directory.studio.scim.ui.ScimUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;


public class ScimPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Text scimVersionText;
    private Text authMethodText;
    private Text pageSizeText;
    private Text timeoutMsText;
    private Button autoDiscoverButton;


    @Override
    public void init( IWorkbench workbench )
    {
    }


    @Override
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );

        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID );

        new Label( composite, SWT.NONE ).setText( "Default SCIM Version:" );
        scimVersionText = new Text( composite, SWT.BORDER );
        scimVersionText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        scimVersionText.setText( prefs.get( ScimUIConstants.PREF_DEFAULT_SCIM_VERSION, "V2_0" ) );

        new Label( composite, SWT.NONE ).setText( "Default Auth Method:" );
        authMethodText = new Text( composite, SWT.BORDER );
        authMethodText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        authMethodText.setText( prefs.get( ScimUIConstants.PREF_DEFAULT_AUTH_METHOD, "bearer" ) );

        new Label( composite, SWT.NONE ).setText( "Default Page Size:" );
        pageSizeText = new Text( composite, SWT.BORDER );
        pageSizeText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        pageSizeText.setText( prefs.get( ScimUIConstants.PREF_DEFAULT_PAGE_SIZE, "100" ) );

        new Label( composite, SWT.NONE ).setText( "Default Timeout (ms):" );
        timeoutMsText = new Text( composite, SWT.BORDER );
        timeoutMsText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        timeoutMsText.setText( prefs.get( ScimUIConstants.PREF_DEFAULT_TIMEOUT_MS, "30000" ) );

        new Label( composite, SWT.NONE ).setText( "Auto-Discover Schemas:" );
        autoDiscoverButton = new Button( composite, SWT.CHECK );
        autoDiscoverButton.setSelection(
            Boolean.parseBoolean( prefs.get( ScimUIConstants.PREF_AUTO_DISCOVER_SCHEMAS, "true" ) ) );

        return composite;
    }


    @Override
    public boolean performOk()
    {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode( ScimUIPlugin.PLUGIN_ID );
        prefs.put( ScimUIConstants.PREF_DEFAULT_SCIM_VERSION, scimVersionText.getText() );
        prefs.put( ScimUIConstants.PREF_DEFAULT_AUTH_METHOD, authMethodText.getText() );
        prefs.put( ScimUIConstants.PREF_DEFAULT_PAGE_SIZE, pageSizeText.getText() );
        prefs.put( ScimUIConstants.PREF_DEFAULT_TIMEOUT_MS, timeoutMsText.getText() );
        prefs.put( ScimUIConstants.PREF_AUTO_DISCOVER_SCHEMAS,
            Boolean.toString( autoDiscoverButton.getSelection() ) );
        try
        {
            prefs.flush();
        }
        catch ( BackingStoreException e )
        {
            // best-effort flush
        }
        return true;
    }


    @Override
    protected void performDefaults()
    {
        scimVersionText.setText( "V2_0" );
        authMethodText.setText( "bearer" );
        pageSizeText.setText( "100" );
        timeoutMsText.setText( "30000" );
        autoDiscoverButton.setSelection( true );
        super.performDefaults();
    }
}
