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


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ScimAuthPage extends WizardPage
{
    private static final String AUTH_BEARER = "Bearer Token";
    private static final String AUTH_OAUTH2 = "OAuth2 Client Credentials";
    private static final String AUTH_BASIC = "Basic";

    private Combo authMethodCombo;

    private Group bearerGroup;
    private Text bearerTokenText;

    private Group oauth2Group;
    private Text oauth2EndpointText;
    private Text oauth2ClientIdText;
    private Text oauth2ClientSecretText;
    private Text oauth2ScopeText;

    private Group basicGroup;
    private Text basicUsernameText;
    private Text basicPasswordText;


    public ScimAuthPage()
    {
        super( "ScimAuthPage" );
        setTitle( "Authentication" );
        setDescription( "Configure authentication for the SCIM server." );
    }


    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );

        new Label( composite, SWT.NONE ).setText( "Authentication Method:" );
        authMethodCombo = new Combo( composite, SWT.READ_ONLY );
        authMethodCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        authMethodCombo.setItems( new String[]{ AUTH_BEARER, AUTH_OAUTH2, AUTH_BASIC } );
        authMethodCombo.select( 0 );

        bearerGroup = new Group( composite, SWT.NONE );
        bearerGroup.setText( "Bearer Token" );
        bearerGroup.setLayout( new GridLayout( 2, false ) );
        bearerGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 2, 1 ) );
        new Label( bearerGroup, SWT.NONE ).setText( "Token:" );
        bearerTokenText = new Text( bearerGroup, SWT.BORDER );
        bearerTokenText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        oauth2Group = new Group( composite, SWT.NONE );
        oauth2Group.setText( "OAuth2 Client Credentials" );
        oauth2Group.setLayout( new GridLayout( 2, false ) );
        oauth2Group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 2, 1 ) );
        new Label( oauth2Group, SWT.NONE ).setText( "Token Endpoint:" );
        oauth2EndpointText = new Text( oauth2Group, SWT.BORDER );
        oauth2EndpointText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        new Label( oauth2Group, SWT.NONE ).setText( "Client ID:" );
        oauth2ClientIdText = new Text( oauth2Group, SWT.BORDER );
        oauth2ClientIdText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        new Label( oauth2Group, SWT.NONE ).setText( "Client Secret:" );
        oauth2ClientSecretText = new Text( oauth2Group, SWT.BORDER | SWT.PASSWORD );
        oauth2ClientSecretText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        new Label( oauth2Group, SWT.NONE ).setText( "Scope:" );
        oauth2ScopeText = new Text( oauth2Group, SWT.BORDER );
        oauth2ScopeText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        basicGroup = new Group( composite, SWT.NONE );
        basicGroup.setText( "Basic Authentication" );
        basicGroup.setLayout( new GridLayout( 2, false ) );
        basicGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 2, 1 ) );
        new Label( basicGroup, SWT.NONE ).setText( "Username:" );
        basicUsernameText = new Text( basicGroup, SWT.BORDER );
        basicUsernameText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        new Label( basicGroup, SWT.NONE ).setText( "Password:" );
        basicPasswordText = new Text( basicGroup, SWT.BORDER | SWT.PASSWORD );
        basicPasswordText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        authMethodCombo.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                updateGroupVisibility();
            }
        } );

        updateGroupVisibility();
        setControl( composite );
        setPageComplete( true );
    }


    private void updateGroupVisibility()
    {
        String selected = authMethodCombo.getText();
        bearerGroup.setVisible( AUTH_BEARER.equals( selected ) );
        ( ( GridData ) bearerGroup.getLayoutData() ).exclude = !AUTH_BEARER.equals( selected );
        oauth2Group.setVisible( AUTH_OAUTH2.equals( selected ) );
        ( ( GridData ) oauth2Group.getLayoutData() ).exclude = !AUTH_OAUTH2.equals( selected );
        basicGroup.setVisible( AUTH_BASIC.equals( selected ) );
        ( ( GridData ) basicGroup.getLayoutData() ).exclude = !AUTH_BASIC.equals( selected );
        bearerGroup.getParent().layout( true, true );
    }


    public String getAuthMethod()
    {
        String selected = authMethodCombo.getText();
        if ( AUTH_OAUTH2.equals( selected ) )
        {
            return "oauth2cc";
        }
        if ( AUTH_BASIC.equals( selected ) )
        {
            return "basic";
        }
        return "bearer";
    }


    public String getBearerToken()
    {
        return bearerTokenText.getText();
    }


    public String getOauth2TokenEndpoint()
    {
        return oauth2EndpointText.getText();
    }


    public String getOauth2ClientId()
    {
        return oauth2ClientIdText.getText();
    }


    public String getOauth2ClientSecret()
    {
        return oauth2ClientSecretText.getText();
    }


    public String getOauth2Scope()
    {
        return oauth2ScopeText.getText();
    }


    public String getBasicUsername()
    {
        return basicUsernameText.getText();
    }


    public String getBasicPassword()
    {
        return basicPasswordText.getText();
    }
}
