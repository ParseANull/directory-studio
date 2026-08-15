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

package org.apache.directory.studio.ldapbrowser.common.widgets.connection;


import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.ldap.model.url.LdapUrl.Extension;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.ui.AbstractConnectionParameterPage;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection.ModifyMode;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection.ModifyOrder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


// ── CLASS: EditorParameterPage — Han Dials In The Comm System Before Bespin ──────────────
// Han is aboard the Falcon en route to Bespin, tuning the communication subsystem
// before hailing Lando.  The comm has three dials: how to send modification requests
// (replace the whole attribute, or add-then-delete?), the same question for attributes
// with no equality matching rule, and the order of operations (delete old value first
// or add the new value first?).  Get these wrong and the message arrives garbled.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * UI parameter page for configuring how the entry editor sends LDAP modify operations.
 * This covers the modify mode (REPLACE vs ADD+DELETE), a separate mode for attributes
 * without an equality matching rule, and the modify order (delete-first vs add-first).
 * Think of this class as Han's comm calibration panel — the settings here determine
 * exactly how modification requests are formatted before they fly out to the LDAP server.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorParameterPage extends AbstractConnectionParameterPage
{

    private static final String X_MODIFY_MODE = "X-MODIFY-MODE"; //$NON-NLS-1$

    private static final String X_MODIFY_MODE_NO_EMR = "X-MODIFY-MODE-NO-EMR"; //$NON-NLS-1$

    private static final String X_MODIFY_ORDER = "X-MODIFY-ORDER"; //$NON-NLS-1$

    /** The combo for selecting the modify mode */
    private Combo modifyModeCombo;

    /** The combo for selecting the modify mode of attribute with no equality matching rule */
    private Combo modifyModeNoEMRCombo;

    /** The combo for selecting the modify order */
    private Combo modifyOrderCombo;


    // ── Han Sits Down At The Comm Panel ──────────────────────────────────────────────────
    // The comm panel boots up with no settings loaded — blank combos, waiting for the
    // wizard framework to populate them from an existing connection or with defaults.
    // This no-arg constructor is required because Eclipse instantiates it via reflection.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty EditorParameterPage.
     * The Eclipse connection wizard instantiates this via the extension registry, so we
     * need a public no-argument constructor.  The wizard framework will call
     * {@link #loadParameters} and {@link #initListeners} before showing the page.
     */
    public EditorParameterPage()
    {
    }


    // ── Han Reads The Standard Comm Mode Dial ────────────────────────────────────────────
    // The first dial on the comm panel controls how regular attribute values get modified —
    // either the server-default behavior (let the server decide), replace the whole attribute
    // at once, or add the new value and delete the old one as two separate operations.
    // Han checks where the dial is pointing before transmitting.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected modify mode for standard attributes.
     * The modify mode controls how LDAP modify operations are structured when saving an
     * attribute value: DEFAULT lets the server choose, REPLACE sends a single REPLACE
     * operation, ADD_DELETE sends an ADD followed by a DELETE (useful for some servers
     * that reject REPLACE on certain attribute types).
     *
     * <p>For example — Han reads the standard dial:</p>
     * <pre>
     *   ModifyMode.DEFAULT   - server decides how to apply the modification
     *   ModifyMode.REPLACE   - one REPLACE operation covers the new value
     *   ModifyMode.ADD_DELETE - ADD new value, then DELETE old value
     * </pre>
     *
     * @return the selected {@link ModifyMode} for normal attributes
     */
    private ModifyMode getModifyMode()
    {
        return ModifyMode.getByOrdinal( modifyModeCombo.getSelectionIndex() );
    }


    // ── Han Reads The No-EMR Comm Mode Dial ──────────────────────────────────────────────
    // Some attributes don't have an equality matching rule (EMR) — the server can't compare
    // two values for equality.  That makes REPLACE risky (you might not remove the right
    // old value) and DELETE tricky (how do you identify what to delete without equality?).
    // Han has a separate dial for this edge case — a different comm mode for tricky signals.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the selected modify mode for attributes that lack an equality matching rule (EMR).
     * Without an EMR the server cannot compare attribute values for equality, which affects
     * how DELETE operations are constructed.  This separate setting lets us choose a safer
     * strategy (e.g. REPLACE) specifically for those problematic attribute types.
     *
     * <p>For example — Han reads the no-EMR dial:</p>
     * <pre>
     *   ModifyMode.DEFAULT   - same as the standard mode
     *   ModifyMode.REPLACE   - safe choice; avoids DELETE-by-value issues
     *   ModifyMode.ADD_DELETE - explicit add then delete
     * </pre>
     *
     * @return the selected {@link ModifyMode} for attributes without an equality matching rule
     */
    private ModifyMode getModifyModeNoEMR()
    {
        return ModifyMode.getByOrdinal( modifyModeNoEMRCombo.getSelectionIndex() );
    }


    // ── Han Reads The Operation-Order Selector ────────────────────────────────────────────
    // When a modify operation involves both adding a new value and deleting an old one,
    // the order matters for some servers.  Han checks whether to "delete first, then add"
    // or "add first, then delete" — like transmitting an abort code before the new one,
    // or sending the new handshake before dropping the old channel.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the selected modify order, which controls whether DELETE or ADD comes first
     * in an ADD+DELETE modify request.  Some LDAP servers enforce uniqueness and will reject
     * an ADD if the old value still exists, requiring DELETE first; others need ADD first
     * to avoid a temporarily empty attribute.  Han picking the right transmission sequence.
     *
     * <p>For example — Han selects the order:</p>
     * <pre>
     *   ModifyOrder.DELETE_FIRST - send DELETE before ADD
     *   ModifyOrder.ADD_FIRST    - send ADD before DELETE
     * </pre>
     *
     * @return the selected {@link ModifyOrder}
     */
    private ModifyOrder getModifyOrder()
    {
        return ModifyOrder.getByOrdinal( modifyOrderCombo.getSelectionIndex() );
    }


    // ── Han Opens The Comm Configuration Panel ───────────────────────────────────────────
    // The editor parameter page has only one section — the modify settings group.
    // Han opens the comm configuration panel with that single group inside.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the UI for this parameter page by adding the modify-settings group.
     * Called by the Eclipse wizard framework at render time.
     *
     * <p>For example — Han activates the panel:</p>
     * <pre>
     *   addModifyInput( parent ); // the only section on this page
     * </pre>
     *
     * @param parent  the SWT composite the wizard framework provides to host our widgets
     */
    protected void createComposite( Composite parent )
    {
        addModifyInput( parent );
    }


    // ── Han Lays Out The Three Comm Dials ────────────────────────────────────────────────
    // Three drop-down combos appear in the modify group: modify mode, modify mode for
    // no-EMR attributes, and modify order.  Each has a label explaining what it does
    // and a tooltip with more detail — like the labels printed above the Falcon's comm dials.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Modify" UI group containing three labeled read-only combo boxes:
     * <ul>
     *   <li>Modify Mode — how to send changes for ordinary attributes</li>
     *   <li>Modify Mode (No EMR) — same but for attributes without an equality matching rule</li>
     *   <li>Modify Order — DELETE first or ADD first when doing ADD+DELETE</li>
     * </ul>
     * All three combos are read-only (no free-text input) to prevent invalid selections.
     *
     * <p>For example — Han sees three dials in a row:</p>
     * <pre>
     *   [Modify Mode]         [Default v]
     *   [Modify Mode No EMR]  [Default v]
     *   [Modify Order]        [Delete first v]
     * </pre>
     *
     * @param parent  the SWT composite in which to place this group
     */
    private void addModifyInput( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "EditorParameterPage.ModifyGroup" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 2, 1 );

        Label modifyModeLabel = BaseWidgetUtils.createLabel( groupComposite, Messages
            .getString( "EditorParameterPage.ModifyMode" ), 1 ); //$NON-NLS-1$
        modifyModeLabel.setToolTipText( Messages.getString( "EditorParameterPage.ModifyModeTooltip" ) ); //$NON-NLS-1$
        String[] modifyModeItems = new String[]
            { Messages.getString( "EditorParameterPage.ModifyModeDefault" ), //$NON-NLS-1$
                Messages.getString( "EditorParameterPage.ModifyModeReplace" ), //$NON-NLS-1$
                Messages.getString( "EditorParameterPage.ModifyModeAddDel" ) }; //$NON-NLS-1$
        modifyModeCombo = BaseWidgetUtils.createReadonlyCombo( groupComposite, modifyModeItems, 0, 1 );
        modifyModeCombo.setToolTipText( Messages.getString( "EditorParameterPage.ModifyModeTooltip" ) ); //$NON-NLS-1$

        Label modifyModeNoEMRLabel = BaseWidgetUtils.createLabel( groupComposite, Messages
            .getString( "EditorParameterPage.ModifyModeNoEMR" ), 1 ); //$NON-NLS-1$
        modifyModeNoEMRLabel.setToolTipText( Messages.getString( "EditorParameterPage.ModifyModeNoEMRTooltip" ) ); //$NON-NLS-1$
        String[] modifyModeNoEMRItems = new String[]
            { Messages.getString( "EditorParameterPage.ModifyModeDefault" ), //$NON-NLS-1$
                Messages.getString( "EditorParameterPage.ModifyModeReplace" ), //$NON-NLS-1$
                Messages.getString( "EditorParameterPage.ModifyModeAddDel" ) }; //$NON-NLS-1$
        modifyModeNoEMRCombo = BaseWidgetUtils.createReadonlyCombo( groupComposite, modifyModeNoEMRItems, 0, 1 );
        modifyModeNoEMRCombo.setToolTipText( Messages.getString( "EditorParameterPage.ModifyModeNoEMRTooltip" ) ); //$NON-NLS-1$

        Label modifyOrderLabel = BaseWidgetUtils.createLabel( groupComposite, Messages
            .getString( "EditorParameterPage.ModifyOrder" ), 1 ); //$NON-NLS-1$
        modifyOrderLabel.setToolTipText( Messages.getString( "EditorParameterPage.ModifyOrderTooltip" ) ); //$NON-NLS-1$
        String[] modifyOrderItems = new String[]
            { Messages.getString( "EditorParameterPage.ModifyOrderDelFirst" ), //$NON-NLS-1$
                Messages.getString( "EditorParameterPage.ModifyOrderAddFirst" ) }; //$NON-NLS-1$
        modifyOrderCombo = BaseWidgetUtils.createReadonlyCombo( groupComposite, modifyOrderItems, 0, 1 );
        modifyOrderCombo.setToolTipText( Messages.getString( "EditorParameterPage.ModifyOrderTooltip" ) ); //$NON-NLS-1$
    }


    // ── Han Checks The Panel Is Valid ────────────────────────────────────────────────────
    // The three combos on this page only contain valid pre-defined options (read-only),
    // so there's nothing to validate — Han glances at the panel and it's always in a
    // legal state.  No error message needed.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state.  Since all three inputs are read-only combos with
     * pre-defined valid options, this page is always valid and this method does nothing.
     * Han checks the comm panel — all dials are in valid positions, no action required.
     */
    protected void validate()
    {
    }


    // ── Han Restores The Comm Settings From Last Mission ─────────────────────────────────
    // When editing an existing connection, the wizard loads the previously saved modify mode
    // and order back into the combos.  Han pulling up the comm configuration from the last
    // time the Falcon was configured for this particular rendezvous.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the three combo boxes from the given {@link ConnectionParameter} object.
     * Called when the wizard is editing an existing connection — we read the stored ordinal
     * values and select the matching combo entries.
     *
     * <p>For example — Han restores last mission's comm settings:</p>
     * <pre>
     *   int mode = parameter.getExtendedIntProperty( MODIFY_MODE );
     *   modifyModeCombo.select( mode );
     *   // same for modifyModeNoEMR and modifyOrder
     * </pre>
     *
     * @param parameter  the stored connection parameters to restore into the UI
     */
    protected void loadParameters( ConnectionParameter parameter )
    {
        this.connectionParameter = parameter;

        int modifyMode = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE );
        modifyModeCombo.select( modifyMode );
        int modifyModeNoEMR = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR );
        modifyModeNoEMRCombo.select( modifyModeNoEMR );
        int modifyOrder = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER );
        modifyOrderCombo.select( modifyOrder );
    }


    // ── Han Wires The Dials To The Feedback Loop ─────────────────────────────────────────
    // Each combo needs a listener so that changing the selection fires connectionPageModified(),
    // which cascades to validate() and updates the wizard Finish button state.
    // Han connecting each dial's "click" back to the nav computer's confirmation signal.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches {@link SelectionAdapter} listeners to all three combos so that user changes
     * immediately trigger {@link #connectionPageModified()} — keeping the wizard's state
     * in sync with the current selections.
     */
    protected void initListeners()
    {
        modifyModeCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        modifyModeNoEMRCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        modifyOrderCombo.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );
    }


    // ── Han Locks In The Comm Settings ───────────────────────────────────────────────────
    // When the wizard finishes, the framework calls this to write the UI selections into
    // the connection parameter object for storage.  Han encoding his comm dial positions
    // into the Falcon's memory banks so they're ready for the next flight.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current combo selections into the given {@link ConnectionParameter}.
     * The three ordinal values (modify mode, modify mode no-EMR, modify order) are written
     * as extended integer properties.
     *
     * <p>For example — Han saves the three dial positions:</p>
     * <pre>
     *   parameter.setExtendedIntProperty( MODIFY_MODE, getModifyMode().getOrdinal() );
     *   parameter.setExtendedIntProperty( MODIFY_MODE_NO_EMR, getModifyModeNoEMR().getOrdinal() );
     *   parameter.setExtendedIntProperty( MODIFY_ORDER, getModifyOrder().getOrdinal() );
     * </pre>
     *
     * @param parameter  the {@link ConnectionParameter} to write our selections into
     */
    public void saveParameters( ConnectionParameter parameter )
    {
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE, getModifyMode()
            .getOrdinal() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR,
            getModifyModeNoEMR().getOrdinal() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER, getModifyOrder()
            .getOrdinal() );
    }


    // ── Han Saves Nothing Extra To Dialog State ───────────────────────────────────────────
    // This page has no supplementary UI state (no column widths, no remembered window sizes)
    // to persist in Eclipse's dialog-settings store.  Han's comm panel doesn't remember
    // its window position.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves any supplementary page-level settings to Eclipse's dialog-settings store.
     * This page has nothing extra to save, so this method is intentionally empty.
     */
    public void saveDialogSettings()
    {
    }


    // ── Han Has Nothing Specific To Focus ────────────────────────────────────────────────
    // There's no single "most important" field on this page — all three combos are equally
    // relevant.  Han sits back and lets the user pick which dial to turn first.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets keyboard focus when this page becomes active.  This page has no single primary
     * input to focus, so this method is intentionally empty — the first combo will receive
     * focus naturally via the tab order.
     */
    public void setFocus()
    {
    }


    // ── Han Checks Whether Any Comm Dial Has Moved ───────────────────────────────────────
    // The wizard needs to know if the modify settings actually changed so it can decide
    // whether to mark the connection as dirty.  Han comparing the current dial readings
    // against what was saved last time the Falcon left Bespin.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any of the three modify settings differ from the last saved values.
     * The wizard uses this to decide whether the connection needs updating.
     *
     * <p>For example — Han does a quick diff of the dials:</p>
     * <pre>
     *   return modifyMode != getModifyMode().getOrdinal()
     *       || modifyModeNoEMR != getModifyModeNoEMR().getOrdinal()
     *       || modifyOrder != getModifyOrder().getOrdinal();
     * </pre>
     *
     * @return {@code true} if at least one modify parameter has changed
     */
    public boolean areParametersModifed()
    {
        int modifyMode = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE );
        int modifyModeNoEMR = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR );
        int modifyOrder = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER );

        return modifyMode != getModifyMode().getOrdinal() || modifyModeNoEMR != getModifyModeNoEMR().getOrdinal()
            || modifyOrder != getModifyOrder().getOrdinal();
    }


    // ── Han Decides Whether To Drop And Re-Establish The Comm Link ───────────────────────
    // The modify mode and order only affect how individual edit operations are sent — they
    // don't change the underlying LDAP connection itself.  So no reconnect is ever needed
    // when these settings change.  Han just re-calibrates the dials without rebooting the comm.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether changing these settings requires dropping and re-opening the LDAP
     * connection.  Since modify mode and order only affect individual search/modify requests
     * and not the connection setup itself, a reconnect is never required.
     *
     * <p>For example — Han decides no reboot needed:</p>
     * <pre>
     *   if ( connectionParameter == null ) return true; // first-time setup
     *   return false; // comm-dial changes take effect immediately
     * </pre>
     *
     * @return {@code true} only if {@code connectionParameter} is {@code null} (first use),
     *         {@code false} otherwise
     */
    public boolean isReconnectionRequired()
    {
        if ( connectionParameter == null )
        {
            return true;
        }

        return false;
    }


    // ── Han Encodes Comm Settings For Broadcast ───────────────────────────────────────────
    // When exporting a connection as an LDAP URL, we pack the modify parameters into
    // X-extension fields on the URL — only when they differ from the default (ordinal 0).
    // Han encoding his comm-dial positions into the outgoing nav packet for Lando to receive.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the modify mode and order settings as custom extensions on the given
     * {@link LdapUrl}.  Only non-default (non-zero ordinal) values are written to keep the
     * URL compact — the default is always ordinal 0.
     *
     * <p>For example — Han encodes the packet:</p>
     * <pre>
     *   if ( modifyMode != 0 )
     *       ldapUrl.getExtensions().add( new Extension( false, "X-MODIFY-MODE", "1" ) );
     * </pre>
     *
     * @param parameter  the source of truth for the parameter values to encode
     * @param ldapUrl    the URL object to append extension entries to
     */
    public void mergeParametersToLdapURL( ConnectionParameter parameter, LdapUrl ldapUrl )
    {
        int modifyMode = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE );
        if ( modifyMode != 0 )
        {
            ldapUrl.getExtensions().add(
                new Extension( false, X_MODIFY_MODE, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE ) ) );
        }

        int modifyModeNoEMR = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR );
        if ( modifyModeNoEMR != 0 )
        {
            ldapUrl.getExtensions().add(
                new Extension( false, X_MODIFY_MODE_NO_EMR, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR ) ) );
        }

        int modifyOrder = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER );
        if ( modifyOrder != 0 )
        {
            ldapUrl.getExtensions().add(
                new Extension( false, X_MODIFY_ORDER, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER ) ) );
        }
    }


    // ── Han Decodes A Received Comm Packet ────────────────────────────────────────────────
    // When importing a connection from an LDAP URL, we read the X-MODIFY-* extension values
    // and write them back into the connection parameter object.
    // Han decoding a comm packet from Lando and loading those dial settings into the Falcon.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads modify-mode and modify-order settings from the given {@link LdapUrl}'s custom
     * extensions and writes them into the given {@link ConnectionParameter}.  Non-numeric
     * or absent values fall back to safe defaults (DEFAULT mode, DELETE_FIRST order).
     *
     * <p>For example — Han decodes the incoming packet:</p>
     * <pre>
     *   String modeStr = ldapUrl.getExtensionValue( "X-MODIFY-MODE" );
     *   int mode = Integer.valueOf( modeStr ); // e.g. 1 = REPLACE
     *   parameter.setExtendedIntProperty( MODIFY_MODE, mode );
     * </pre>
     *
     * @param ldapUrl    the source LDAP URL containing X-MODIFY-* extension values
     * @param parameter  the target {@link ConnectionParameter} to populate
     */
    public void mergeLdapUrlToParameters( LdapUrl ldapUrl, ConnectionParameter parameter )
    {
        // modify mode, DEFAULT if non-numeric or absent
        String modifyMode = ldapUrl.getExtensionValue( X_MODIFY_MODE );
        try
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE,
                Integer.valueOf( modifyMode ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE, ModifyMode.DEFAULT
                .getOrdinal() );
        }

        // modify mode no EMR, DEFAULT if non-numeric or absent
        String modifyModeNoEMR = ldapUrl.getExtensionValue( X_MODIFY_MODE_NO_EMR );
        try
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR,
                Integer.valueOf( modifyModeNoEMR ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR,
                ModifyMode.DEFAULT.getOrdinal() );
        }

        // modify order, DEL_FIRST if non-numeric or absent
        String modifyOrder = ldapUrl.getExtensionValue( X_MODIFY_ORDER );
        try
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER,
                Integer.valueOf( modifyOrder ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_MODIFY_ORDER,
                ModifyOrder.DELETE_FIRST.getOrdinal() );
        }
    }
}
