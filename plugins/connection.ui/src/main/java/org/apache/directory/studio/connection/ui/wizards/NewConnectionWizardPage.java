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

package org.apache.directory.studio.connection.ui.wizards;


import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.ui.ConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageModifyListener;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: NewConnectionWizardPage — ONE PAGE ON THE REBEL FLEET INTAKE FORM ──────
// Each tab in the fleet intake form (network address, auth codes, options…) needs
// to be wrapped as a JFace WizardPage so the wizard framework can navigate between
// them with Back/Next and track their completion state.  NewConnectionWizardPage is
// that thin wrapper.  It holds a reference to a ConnectionParameterPage, which does
// the actual layout and validation, and it implements ConnectionParameterPageModifyListener
// so the parameter page can push messages and valid-state up to the wizard container.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Thin {@link WizardPage} adapter that wraps a single {@link ConnectionParameterPage}
 * for use inside the {@link NewConnectionWizard}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates a minimal composite and delegates to
 *       {@link ConnectionParameterPage#init} to build the actual page content.</li>
 *   <li>Implements {@link ConnectionParameterPageModifyListener} so the parameter
 *       page can push validation messages and completeness state up to the wizard
 *       chrome (title area, Next/Finish buttons).</li>
 *   <li>Forwards {@link #getTestConnectionParameters()} to the owning
 *       {@link NewConnectionWizard} so the test button can snapshot parameters
 *       across all pages.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionWizardPage extends WizardPage implements ConnectionParameterPageModifyListener
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The parent wizard — needed to delegate {@link #getTestConnectionParameters()}. */
    private NewConnectionWizard wizard;

    /** The parameter page that provides the actual content and validation. */
    private ConnectionParameterPage page;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link NewConnectionWizardPage} wrapping the given parameter page.
     *
     * <p>Uses the parameter page's name, description, and the connection-wizard
     * banner image from the plugin image registry.</p>
     *
     * @param wizard The parent {@link NewConnectionWizard}.
     * @param page   The {@link ConnectionParameterPage} to wrap.
     */
    public NewConnectionWizardPage( NewConnectionWizard wizard, ConnectionParameterPage page )
    {
        super( page.getPageName() );
        setTitle( page.getPageName() );
        setDescription( page.getPageDescription() );
        setImageDescriptor( ConnectionUIPlugin.getDefault().getImageDescriptor(
            ConnectionUIConstants.IMG_CONNECTION_WIZARD ) );
        setPageComplete( false );

        this.wizard = wizard;
        this.page = page;
    }


    // ── SET VISIBLE ───────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>When this page becomes visible (the user clicks Next/Back to it),
     * we give keyboard focus to the parameter page's first input field.</p>
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            page.setFocus();
        }
    }


    // ── CREATE CONTROL ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Creates a single-column composite and delegates to
     * {@link ConnectionParameterPage#init} to populate it.  The parameter page
     * is initialised without a pre-existing {@link ConnectionParameter} (passing
     * {@code null}) since this is a creation wizard, not an edit dialog.</p>
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gridLayout = new GridLayout( 1, false );
        composite.setLayout( gridLayout );

        // ── DELEGATE CONTENT TO THE PARAMETER PAGE ────────────────────────────────
        // Pass null for connectionParameter — the page will create sensible defaults.
        // ──────────────────────────────────────────────────────────────────────────
        page.init( composite, this, null );
        setControl( composite );
    }


    // ── CONNECTION PARAMETER PAGE MODIFIED ────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Called by the wrapped parameter page whenever a field changes.  We
     * propagate the page's message (warning takes precedence over info), error
     * message, and completeness state to the wizard chrome, then poke the
     * wizard container to refresh the Next/Finish button state.</p>
     */
    public void connectionParameterPageModified()
    {
        // ── PROPAGATE MESSAGE ─────────────────────────────────────────────────────
        // Warning messages take precedence over info messages; null clears both.
        // ──────────────────────────────────────────────────────────────────────────
        if ( page.getMessage() != null )
        {
            setMessage( page.getMessage() );
        }
        else if ( page.getInfoMessage() != null )
        {
            setMessage( page.getInfoMessage() );
        }
        else
        {
            setMessage( null );
        }

        setErrorMessage( page.getErrorMessage() );
        setPageComplete( page.isValid() );

        // ── REFRESH WIZARD BUTTONS ────────────────────────────────────────────────
        IWizardContainer container = getContainer();

        if ( ( container != null ) && ( container.getCurrentPage() != null ) )
        {
            container.updateButtons();
        }
    }


    // ── GET TEST CONNECTION PARAMETERS ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Forwards to {@link NewConnectionWizard#getTestConnectionParameters()}
     * so the test button can snapshot parameters across all pages, not just this
     * one.</p>
     *
     * @return A {@link ConnectionParameter} built from the current values of all
     *         parameter pages.
     */
    public ConnectionParameter getTestConnectionParameters()
    {
        return wizard.getTestConnectionParameters();
    }
}
