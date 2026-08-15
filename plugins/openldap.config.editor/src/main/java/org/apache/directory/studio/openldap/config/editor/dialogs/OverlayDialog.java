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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.common.ui.model.OverlayTypeEnum;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.AccessLogOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.AuditLogOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.MemberOfOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.PasswordPolicyOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.ReferentialIntegrityOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.RewriteRemapOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.SyncProvOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.overlays.ValueSortingOverlayConfigurationBlock;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAccessLogConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAuditlogConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcMemberOf;
import org.apache.directory.studio.openldap.config.model.overlay.OlcPPolicyConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRefintConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRwmConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcSyncProvConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcValSortConfig;


// Like Princess Leia's hologram projecting a compact but complete picture
// of the Rebellion's status so commanders can choose the right action,
// we present a top-level overlay configuration dialog that lets the
// administrator pick an overlay type and then edit its specific settings
// all in one unified view — swapping configuration blocks on the fly.
/**
 * The OverlayDialog is used to edit the configuration of an overlay. The user
 * will select the overlay to configure in a Combo, and we swap in the
 * appropriate configuration block depending on the selected overlay type.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OverlayDialog extends Dialog
{
    /** The instance */
    private OverlayDialog instance;

    /** The flag to allow the overlay type selection */
    private boolean allowOverlayTypeSelection;

    /** The overlay configuration */
    private OlcOverlayConfig overlay;

    /** The configuration block */
    private OverlayDialogConfigurationBlock<? extends OlcOverlayConfig> configurationBlock;

    /** The connection */
    private IBrowserConnection browserConnection;

    // UI widgets
    private Combo overlayTypeCombo;
    private Composite configurationComposite;
    private Composite configurationInnerComposite;

    // Listeners
    private SelectionListener overlayTypeComboViewerSelectionChangedListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            OverlayTypeEnum type = OverlayTypeEnum.getOverlay( overlayTypeCombo.getText() );

            switch ( type )
            {
                case AUDIT_LOG:
                    overlay = new OlcAuditlogConfig();
                    configurationBlock = new AuditLogOverlayConfigurationBlock( instance, ( OlcAuditlogConfig ) overlay );
                    break;

                case MEMBER_OF:
                    overlay = new OlcMemberOf();
                    configurationBlock = new MemberOfOverlayConfigurationBlock( instance, browserConnection,
                        ( OlcMemberOf ) overlay );
                    break;

                case PASSWORD_POLICY:
                    overlay = new OlcPPolicyConfig();
                    configurationBlock = new PasswordPolicyOverlayConfigurationBlock( instance, browserConnection,
                        ( OlcPPolicyConfig ) overlay );
                    break;

                case REFERENTIAL_INTEGRITY:
                    overlay = new OlcRefintConfig();
                    configurationBlock = new ReferentialIntegrityOverlayConfigurationBlock( instance,
                        browserConnection, ( OlcRefintConfig ) overlay );
                    break;

                case REWRITE_REMAP:
                    overlay = new OlcRwmConfig();
                    configurationBlock = new RewriteRemapOverlayConfigurationBlock( instance,
                        browserConnection, ( OlcRwmConfig ) overlay );
                    break;

                case SYNC_PROV:
                    overlay = new OlcSyncProvConfig();
                    configurationBlock = new SyncProvOverlayConfigurationBlock( instance, ( OlcSyncProvConfig ) overlay );
                    break;

                case VALUE_SORTING:
                    overlay = new OlcValSortConfig();
                    configurationBlock = new ValueSortingOverlayConfigurationBlock( instance, browserConnection,
                        ( OlcValSortConfig ) overlay );
                    break;

                case ACCESS_LOG:
                default:
                    overlay = new OlcAccessLogConfig();
                    configurationBlock = new AccessLogOverlayConfigurationBlock( instance, browserConnection,
                        ( OlcAccessLogConfig ) overlay );
                    break;
            }

            refreshOverlayContent();
            autoresizeDialog();
        }
    };


    // Like Leia's hologram appearing without any pre-loaded content so the
    // operator can choose which overlay to configure from scratch, we set
    // up the dialog with the RESIZE style and keep the instance reference
    // so inner listeners can reach back to the dialog.
    /**
     * Creates a new instance of OverlayDialog with no pre-selected overlay.
     * The RESIZE style lets the operator expand the dialog when configuration
     * blocks grow the content area.
     *
     * @param parentShell the parent shell
     */
    public OverlayDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        instance = this;
    }


    // Like Leia's hologram launching with an explicit permission flag that
    // controls whether the viewer can switch the message channel, we accept
    // a flag to lock or unlock the overlay type selection combo.
    /**
     * Creates a new instance of OverlayDialog, optionally allowing the
     * operator to change the overlay type via the type combo.
     *
     * @param parentShell the parent shell
     * @param allowOverlayTypeSelection {@code true} if the operator may switch the overlay type
     */
    public OverlayDialog( Shell parentShell, boolean allowOverlayTypeSelection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        instance = this;
        this.allowOverlayTypeSelection = allowOverlayTypeSelection;
    }


    // Like labeling the hologram channel so the viewer knows which overlay
    // they're configuring, we stamp the shell title from getDialogText()
    // before the dialog becomes visible.
    /**
     * Configures the dialog shell by setting its title to the overlay-specific
     * text returned by {@link #getDialogText()}.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( getDialogText() );
    }


    // Like reading the overlay type name off the mission briefing scroll
    // before writing it on the hologram header, we build the title string
    // from the overlay type or fall back to a generic label when no overlay
    // is set yet.
    /**
     * Builds the dialog title string. When an overlay is already set we
     * include the overlay type name; otherwise we return a generic label.
     *
     * @return the dialog title text
     */
    private String getDialogText()
    {
        if ( overlay != null )
        {
            return NLS.bind( "{0} Overlay Configuration", getOverlayType( overlay ) );
        }
        else
        {
            return "Overlay Configuration";
        }
    }


    // Like Leia finalizing the hologram content and locking it for transmission
    // once the operator clicks OK, we ask the active configuration block to
    // persist its settings before delegating to the superclass OK handler.
    /**
     * Saves the configuration block's current state when the operator confirms,
     * then delegates to the superclass {@code okPressed()} to close the dialog.
     */
    @Override
    protected void okPressed()
    {
        if ( configurationBlock != null )
        {
            configurationBlock.save();
        }

        super.okPressed();
    }


    // Like Leia's hologram projecting both the overlay type selector at the top
    // and a dynamically swapped configuration panel below so the operator sees
    // the right settings for whatever overlay they pick, we build the full
    // dialog content area and wire up the combo listener.
    /**
     * Builds the dialog content area. When {@code allowOverlayTypeSelection} is
     * set we show the type combo and a separator above the configuration block;
     * otherwise we go straight to the block. We also default to ACCESS_LOG if
     * no overlay has been pre-set.
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // Checking if we need to show the overlay type selection
        if ( allowOverlayTypeSelection )
        {
            createOverlayTypeSelection( composite );

            BaseWidgetUtils.createSeparator( composite, 1 );
        }

        // Creating the configuration composites
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );
        createConfigurationInnerComposite();

        // Checking for empty overlay
        if ( overlay == null )
        {
            // Assigning a default one
            overlay = new OlcAccessLogConfig();

            // Select the correct value on the combo viewer (if required)
            if ( allowOverlayTypeSelection )
            {
                overlayTypeCombo.setText( OverlayTypeEnum.ACCESS_LOG.getName() );
            }
        }

        // Initializing the dialog with the overlay
        initWithOverlay();

        // Adding the listener on the combo viewer  (if required)
        if ( allowOverlayTypeSelection )
        {
            overlayTypeCombo.addSelectionListener( overlayTypeComboViewerSelectionChangedListener );
        }

        applyDialogFont( composite );

        return composite;
    }


    // Like placing the channel selector control at the top of the hologram
    // projector so the operator can switch between overlay types with one
    // click, we create a two-column composite with a "Type:" label and
    // the overlay type combo.
    /**
     * Creates the overlay type selection area containing a "Type:" label
     * and the combo populated with all available overlay type names.
     *
     * @param parent the parent composite to attach the selection area to
     */
    private void createOverlayTypeSelection( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );
        BaseWidgetUtils.createLabel( composite, "Type:", 1 );

        overlayTypeCombo = BaseWidgetUtils.createCombo( composite, OverlayTypeEnum.getNames(), 1, 1 );
    }


    // Like an intelligence analyst mapping the overlay config object back
    // to the enum value printed on the mission brief, we inspect the object's
    // runtime type and return the matching OverlayTypeEnum constant.
    /**
     * Returns the {@link OverlayTypeEnum} constant that matches the given overlay
     * config object's runtime type, or {@code null} if the type is not recognized.
     *
     * @param overlay the overlay config object to inspect
     * @return the matching overlay type enum, or {@code null}
     */
    public static OverlayTypeEnum getOverlayType( OlcOverlayConfig overlay )
    {
        if ( overlay instanceof OlcAccessLogConfig )
        {
            return OverlayTypeEnum.ACCESS_LOG;
        }
        else if ( overlay instanceof OlcAuditlogConfig )
        {
            return OverlayTypeEnum.AUDIT_LOG;
        }
        else if ( overlay instanceof OlcMemberOf )
        {
            return OverlayTypeEnum.MEMBER_OF;
        }
        else if ( overlay instanceof OlcPPolicyConfig )
        {
            return OverlayTypeEnum.PASSWORD_POLICY;
        }
        else if ( overlay instanceof OlcRefintConfig )
        {
            return OverlayTypeEnum.REFERENTIAL_INTEGRITY;
        }
        else if ( overlay instanceof OlcRwmConfig )
        {
            return OverlayTypeEnum.REWRITE_REMAP;
        }
        else if ( overlay instanceof OlcSyncProvConfig )
        {
            return OverlayTypeEnum.SYNC_PROV;
        }
        else if ( overlay instanceof OlcValSortConfig )
        {
            return OverlayTypeEnum.VALUE_SORTING;
        }

        return null;
    }


    // Like rolling out a fresh hologram projection panel whenever the operator
    // switches overlays, we create a new inner composite inside the outer
    // configuration composite to host the next block's widgets.
    /**
     * Creates a fresh inner composite inside the configuration composite.
     * Called during initial setup and whenever the overlay type is changed.
     */
    private void createConfigurationInnerComposite()
    {
        configurationInnerComposite = BaseWidgetUtils.createColumnContainer( configurationComposite, 1, 1 );
    }


    // Like powering down the old hologram projection before bringing up
    // the new one so stale widgets don't bleed through, we dispose the
    // existing inner composite and null the reference.
    /**
     * Disposes the current configuration inner composite if one exists,
     * releasing its SWT resources and nulling the reference.
     */
    private void disposeConfigurationInnerComposite()
    {
        if ( configurationInnerComposite != null )
        {
            configurationInnerComposite.dispose();
            configurationInnerComposite = null;
        }
    }


    // Like Leia loading the correct message content into the projector
    // based on the overlay type she selected from the briefing dossier,
    // we inspect the current overlay object and instantiate the right
    // configuration block before refreshing the displayed content.
    /**
     * Initializes the dialog by instantiating the configuration block that
     * matches the current overlay object's type, then calls
     * {@link #refreshOverlayContent()} to display its widgets.
     */
    private void initWithOverlay()
    {
        if ( overlay instanceof OlcAccessLogConfig )
        {
            configurationBlock = new AccessLogOverlayConfigurationBlock( this, browserConnection,
                ( OlcAccessLogConfig ) overlay );
        }
        else if ( overlay instanceof OlcAuditlogConfig )
        {
            configurationBlock = new AuditLogOverlayConfigurationBlock( this, ( OlcAuditlogConfig ) overlay );
        }
        else if ( overlay instanceof OlcMemberOf )
        {
            configurationBlock = new MemberOfOverlayConfigurationBlock( this, browserConnection,
                ( OlcMemberOf ) overlay );
        }
        else if ( overlay instanceof OlcPPolicyConfig )
        {
            configurationBlock = new PasswordPolicyOverlayConfigurationBlock( this, browserConnection,
                ( OlcPPolicyConfig ) overlay );
        }
        else if ( overlay instanceof OlcRefintConfig )
        {
            configurationBlock = new ReferentialIntegrityOverlayConfigurationBlock( this, browserConnection,
                ( OlcRefintConfig ) overlay );
        }
        else if ( overlay instanceof OlcRwmConfig )
        {
            configurationBlock = new RewriteRemapOverlayConfigurationBlock( this, browserConnection,
                ( OlcRwmConfig ) overlay );
        }
        else if ( overlay instanceof OlcSyncProvConfig )
        {
            configurationBlock = new SyncProvOverlayConfigurationBlock( this, ( OlcSyncProvConfig ) overlay );
        }
        else if ( overlay instanceof OlcValSortConfig )
        {
            configurationBlock = new ValueSortingOverlayConfigurationBlock( this, browserConnection,
                ( OlcValSortConfig ) overlay );
        }

        refreshOverlayContent();
    }


    // Like retrieving the current overlay configuration object so the caller
    // can inspect which overlay is being edited, we hand back the stored
    // overlay reference.
    /**
     * Returns the current overlay configuration object being edited.
     *
     * @return the overlay configuration, or {@code null} if none is set
     */
    public OlcOverlayConfig getOverlay()
    {
        return overlay;
    }


    // Like the operator loading a pre-selected overlay into the hologram
    // projector before it powers on so it opens at the right settings,
    // we store the provided overlay object for use during initialization.
    /**
     * Sets the overlay configuration that this dialog will edit.
     *
     * @param overlay the overlay configuration to set
     */
    public void setOverlay( OlcOverlayConfig overlay )
    {
        this.overlay = overlay;
    }


    // Like calling the ship's docking bay to adjust the hologram display
    // size after swapping in a new overlay block that has more or fewer
    // controls than the previous one, we pack the shell to resize it
    // around the new content.
    /**
     * Forces the dialog shell to resize itself around the current content
     * by calling {@link Shell#pack()}.
     */
    private void autoresizeDialog()
    {
        this.getShell().pack();
    }


    // Like swapping out the hologram's data feed and re-rendering the display
    // whenever the operator switches overlay types, we tear down the old inner
    // composite, build a fresh one, load the new block's widgets, and
    // trigger a layout pass so everything lines up correctly.
    /**
     * Refreshes the configuration content area by disposing the old inner
     * composite, creating a new one, populating it with the current
     * configuration block's widgets, refreshing the block, and updating
     * the dialog title.
     */
    private void refreshOverlayContent()
    {
        // Disposing existing configuration inner composite and creating a new one
        disposeConfigurationInnerComposite();
        createConfigurationInnerComposite();

        // Displaying the specific settings
        configurationBlock.createBlockContent( configurationInnerComposite );
        configurationBlock.refresh();
        configurationComposite.layout();

        // Changing the dialog title
        getShell().setText( getDialogText() );
    }


    // Like checking which LDAP connection the hologram projector is tuned
    // to so other components know where to send their queries, we return
    // the stored browser connection reference.
    /**
     * Returns the browser connection used for schema lookups and DN browsing.
     *
     * @return the browser connection
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // Like tuning the hologram projector to the correct LDAP connection
    // before the operator starts browsing directory objects inside it,
    // we store the given browser connection for later use by overlay
    // configuration blocks that need schema information.
    /**
     * Sets the browser connection used by overlay configuration blocks
     * for schema lookups and DN browsing.
     *
     * @param browserConnection the browser connection to set
     */
    public void setBrowserConnection( IBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;
    }
}
