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


import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterConfigurationPage;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterConfigurationPageModifyListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


// ── CLASS: NewServerWizardConfigurationPage — THE ENGINEERING SPEC FORM IN THE WIZARD ────
// After picking the server type (ApacheDS 2.0), the requisition officer fills in the
// engineering spec form: port numbers, installation path, etc.
// This class wraps the adapter's own configuration page inside a JFace WizardPage so it
// appears as a standard wizard step — with a title, description, image, and error message.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link WizardPage} wrapper around an adapter's {@link LdapServerAdapterConfigurationPage}.
 * Pulls the page's ID, title, description, and image from the configuration page, then
 * delegates all UI creation to it.
 * Implements {@link LdapServerAdapterConfigurationPageModifyListener} to forward validation
 * state back to the wizard (error message, page-complete flag).
 * Think of it as the engineering spec form sheet in the New Server Wizard.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerWizardConfigurationPage extends WizardPage implements
    LdapServerAdapterConfigurationPageModifyListener
{
    /** The configuration page */
    private LdapServerAdapterConfigurationPage configurationPage;


    // ── Wrapping The Configuration Page In A Wizard Step ─────────────────────────────────────
    // The wizard step takes all its metadata from the configuration page it wraps.
    // We also register ourselves as the modify listener so validation changes propagate to the
    // wizard's Finish button.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wizard page backed by the given adapter configuration page.
     * Copies the ID, title, description, image, and page-complete state from the configuration page.
     * Registers {@code this} as the modify listener so validation updates flow to the wizard.
     *
     * @param configurationPage  the adapter-specific configuration page to wrap
     */
    public NewServerWizardConfigurationPage( LdapServerAdapterConfigurationPage configurationPage )
    {
        super( configurationPage.getId() );
        setTitle( configurationPage.getTitle() );
        setDescription( configurationPage.getDescription() );
        setImageDescriptor( configurationPage.getImageDescriptor() );
        setPageComplete( configurationPage.isPageComplete() );

        this.configurationPage = configurationPage;
        configurationPage.setModifyListener( this );
    }


    // ── Asking The Configuration Page To Build Its Widgets ────────────────────────────────────
    // The wizard framework calls createControl() to build this page's widget tree.
    // We delegate entirely to the wrapped configuration page, then set the resulting control
    // as the wizard page's control and give it focus.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates widget creation to the wrapped {@link LdapServerAdapterConfigurationPage}.
     * Sets the returned control as the wizard page's control and gives it focus.
     *
     * @param parent  the parent composite provided by the wizard container
     */
    public void createControl( Composite parent )
    {
        // Creating the control for the configuration page
        Control control = configurationPage.createControl( parent );

        // Setting the control and the focus
        setControl( control );
        control.setFocus();
    }


    // ── Saving Configuration Settings Into The New Server Object ──────────────────────────────
    // When performFinish() runs, it asks us to commit the form fields into the new server.
    // We pass this straight through to the configuration page.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the configuration form values into the given server object.
     * Delegates to {@link LdapServerAdapterConfigurationPage#saveConfiguration(LdapServer)}.
     *
     * @param ldapServer  the newly created server to store the configuration into
     */
    public void saveConfiguration( LdapServer ldapServer )
    {
        configurationPage.saveConfiguration( ldapServer );
    }


    // ── Reacting To Configuration Page Field Changes ──────────────────────────────────────────
    // When the engineer changes a field on the configuration page, we receive this callback
    // and push the new error message and page-complete flag into the wizard framework.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the wrapped configuration page whenever a field is modified.
     * Forwards the configuration page's current error message and page-complete state to
     * the wizard container so it can update the Finish button and error banner.
     */
    public void configurationPageModified()
    {
        setErrorMessage( configurationPage.getErrorMessage() );
        setPageComplete( configurationPage.isPageComplete() );
    }
}
