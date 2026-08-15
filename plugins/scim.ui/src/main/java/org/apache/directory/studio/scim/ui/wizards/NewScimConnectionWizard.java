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


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.ConnectionProtocol;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewScimConnectionWizard extends Wizard implements INewWizard
{
    private ScimConnectionPage connectionPage;
    private ScimAuthPage authPage;
    private ScimCapabilityPage capabilityPage;
    private ScimSchemaPage schemaPage;


    @Override
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setWindowTitle( "New SCIM Connection" );
    }


    @Override
    public void addPages()
    {
        connectionPage = new ScimConnectionPage();
        authPage = new ScimAuthPage();
        capabilityPage = new ScimCapabilityPage();
        schemaPage = new ScimSchemaPage();
        addPage( connectionPage );
        addPage( authPage );
        addPage( capabilityPage );
        addPage( schemaPage );
    }


    @Override
    public boolean performFinish()
    {
        ConnectionParameter params = new ConnectionParameter();
        params.setName( connectionPage.getConnectionName() );
        params.setConnectionProtocol( ConnectionProtocol.SCIM );
        params.setExtendedProperty( "scim.baseUrl", connectionPage.getBaseUrl() );
        params.setExtendedProperty( "scim.version", connectionPage.getScimVersion() );
        params.setExtendedProperty( "scim.providerId", connectionPage.getProviderId() );
        params.setExtendedProperty( "scim.authMethod", authPage.getAuthMethod() );
        params.setExtendedProperty( "scim.bearerToken", authPage.getBearerToken() );
        params.setExtendedProperty( "scim.oauth2.tokenEndpoint", authPage.getOauth2TokenEndpoint() );
        params.setExtendedProperty( "scim.oauth2.clientId", authPage.getOauth2ClientId() );
        params.setExtendedProperty( "scim.oauth2.clientSecret", authPage.getOauth2ClientSecret() );
        params.setExtendedProperty( "scim.oauth2.scope", authPage.getOauth2Scope() );
        params.setExtendedProperty( "scim.basic.username", authPage.getBasicUsername() );
        params.setExtendedProperty( "scim.basic.password", authPage.getBasicPassword() );
        params.setExtendedProperty( "scim.capabilityProfile", capabilityPage.getCapabilityProfileJson() );

        Connection connection = new Connection( params );
        ConnectionCorePlugin.getDefault().getConnectionManager().addConnection( connection );
        return true;
    }
}
