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
package org.apache.directory.studio.openldap.config.editor.dialogs.overlays;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcPPolicyConfig;


// Like the Imperial construction crews assembling the password-policy
// enforcement module onto the second Death Star — wiring in the default
// policy pointer, the cleartext hashing switch, the forward-update relay,
// and the lockout enforcer — we build the PasswordPolicy overlay configuration
// block that governs how the server enforces password rules across the directory.
/**
 * This class implements the configuration block for the Password Policy overlay.
 * We present a default-policy entry widget and three boolean checkboxes
 * (forward updates, hash cleartext, use lockout), and we read/write all four
 * values to and from the {@link OlcPPolicyConfig} model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordPolicyOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcPPolicyConfig>
{
    // UI widgets
    private EntryWidget defaultPolicyEntryWidget;
    private Button forwardUpdatesCheckbox;
    private Button hashCleartextCheckbox;
    private Button useLockoutCheckbox;


    // Like the crew initializing a fresh password-policy module with no
    // prior configuration, we create the block with a new empty OlcPPolicyConfig
    // so there is always a non-null overlay to populate.
    /**
     * Creates a new PasswordPolicyOverlayConfigurationBlock with a fresh,
     * empty {@link OlcPPolicyConfig} as the backing model.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     */
    public PasswordPolicyOverlayConfigurationBlock( OverlayDialog dialog )
    {
        super( dialog );
        setOverlay( new OlcPPolicyConfig() );
    }


    // Like the crew installing a pre-configured password-policy module that
    // already has settings from a previous deployment, we accept an existing
    // OlcPPolicyConfig and store it — defaulting to a fresh one if null.
    /**
     * Creates a new PasswordPolicyOverlayConfigurationBlock backed by the
     * given {@link OlcPPolicyConfig}. If {@code overlay} is {@code null} we
     * create a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param browserConnection the connection used for DN lookups
     * @param overlay the existing password-policy overlay config to edit, or {@code null}
     */
    public PasswordPolicyOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection,
        OlcPPolicyConfig overlay )
    {
        super( dialog, browserConnection );

        if ( overlay == null )
        {
            setOverlay( new OlcPPolicyConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the crew installing the password-policy control panel with a
    // DN picker for the default policy entry and three toggle switches
    // for the forward-updates, hash-cleartext, and use-lockout options,
    // we create the block content widgets here.
    /**
     * Creates the block content area with a "Default Policy" DN entry widget
     * and three checkboxes for Forward Updates, Hash Cleartext, and Use Lockout.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // Default Policy
        BaseWidgetUtils.createLabel( composite, "Default Policy:", 1 );
        defaultPolicyEntryWidget = new EntryWidget( browserConnection, Dn.EMPTY_DN );
        defaultPolicyEntryWidget.createWidget( composite );
        defaultPolicyEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Forward Updates
        forwardUpdatesCheckbox = BaseWidgetUtils.createCheckbox( composite, "Forward Updates", 3 );

        // Hash Cleartext
        hashCleartextCheckbox = BaseWidgetUtils.createCheckbox( composite, "Hash Cleartext", 3 );

        // Use Lockout
        useLockoutCheckbox = BaseWidgetUtils.createCheckbox( composite, "Use Lockout", 3 );
    }


    // Like the crew reading the station's current password-policy settings
    // out of the configuration record and displaying them in the control
    // panel so the administrator can see what's already configured, we
    // push each overlay field value into the corresponding UI widget.
    /**
     * Refreshes the block widgets from the current {@link OlcPPolicyConfig},
     * populating the default policy DN and setting the three boolean checkboxes.
     * Boolean fields default to unchecked when the overlay value is {@code null}.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            // Default Policy
            defaultPolicyEntryWidget.setInput( overlay.getOlcPPolicyDefault() );

            // Forward Updates
            Boolean forwardUpdates = overlay.getOlcPPolicyForwardUpdates();

            if ( forwardUpdates != null )
            {
                forwardUpdatesCheckbox.setSelection( forwardUpdates.booleanValue() );
            }
            else
            {
                forwardUpdatesCheckbox.setSelection( false );
            }

            // Hash Cleartext
            Boolean hashCleartext = overlay.getOlcPPolicyHashCleartext();

            if ( hashCleartext != null )
            {
                hashCleartextCheckbox.setSelection( hashCleartext.booleanValue() );
            }
            else
            {
                hashCleartextCheckbox.setSelection( false );
            }

            // Use Lockout
            Boolean useLockout = overlay.getOlcPPolicyUseLockout();

            if ( useLockout != null )
            {
                useLockoutCheckbox.setSelection( useLockout.booleanValue() );
            }
            else
            {
                useLockoutCheckbox.setSelection( false );
            }
        }
    }


    // Like the crew copying the updated password-policy settings from the
    // control panel back into the station's configuration record so they
    // take effect on the next sync, we read each widget value and write it
    // into the OlcPPolicyConfig model object.
    /**
     * Saves the current widget values back into the {@link OlcPPolicyConfig},
     * writing the default policy DN and the three boolean flags. Also saves
     * the entry widget's dialog settings for future DN suggestions.
     */
    public void save()
    {
        if ( overlay != null )
        {
            // Default Policy
            overlay.setOlcPPolicyDefault( defaultPolicyEntryWidget.getDn() );

            // Forward Updates
            overlay.setOlcPPolicyForwardUpdates( forwardUpdatesCheckbox.getSelection() );

            // Hash Cleartext
            overlay.setOlcPPolicyHashCleartext( hashCleartextCheckbox.getSelection() );

            // Use Lockout
            overlay.setOlcPPolicyUseLockout( useLockoutCheckbox.getSelection() );
        }

        // Saving dialog settings
        defaultPolicyEntryWidget.saveDialogSettings();
    }
}
