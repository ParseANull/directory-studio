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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcSyncProvConfig;


// Like the Imperial construction crews assembling the SyncProv replication
// module onto the second Death Star — setting the checkpoint threshold so
// the station's ledger is flushed after N operations or M minutes, sizing
// the session log to hold enough history for consumer catch-up, and toggling
// the "skip present phase" and "honor reload hint" switches — we build the
// SyncProv overlay configuration block that governs how provider-side
// replication behaves.
/**
 * This class implements the configuration block for the SyncProv overlay.
 * We present text fields for checkpoint operations count and checkpoint
 * interval minutes, a session log operations count, and two boolean
 * checkboxes (skip present phase, honor reload hint flag), and we
 * read/write all of these to and from the {@link OlcSyncProvConfig} model
 * object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyncProvOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcSyncProvConfig>
{
    // UI widgets
    private Text checkpointOperationsText;
    private Text checkpointMinutesText;
    private Text sessionLogOperationsText;
    private Button skipPresentPhaseButton;
    private Button honorReloadHintFlagButton;


    // Like the construction crew initializing a fresh SyncProv module with
    // no prior replication settings, we create the block with a new empty
    // OlcSyncProvConfig so there is always a non-null overlay to populate.
    /**
     * Creates a new SyncProvOverlayConfigurationBlock with a fresh, empty
     * {@link OlcSyncProvConfig} as the backing model.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     */
    public SyncProvOverlayConfigurationBlock( OverlayDialog dialog )
    {
        super( dialog );
        setOverlay( new OlcSyncProvConfig() );
    }


    // Like the crew slotting a pre-configured SyncProv module into the
    // station's infrastructure, we accept an existing OlcSyncProvConfig
    // and store it — defaulting to a fresh one if null was passed.
    /**
     * Creates a new SyncProvOverlayConfigurationBlock backed by the given
     * {@link OlcSyncProvConfig}. If {@code overlay} is {@code null} we
     * create a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param overlay the existing SyncProv overlay config to edit, or {@code null}
     */
    public SyncProvOverlayConfigurationBlock( OverlayDialog dialog, OlcSyncProvConfig overlay )
    {
        super( dialog );

        if ( overlay == null )
        {
            setOverlay( new OlcSyncProvConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew installing the SyncProv control panel
    // with its checkpoint configuration row, session log row, and the
    // two behavioral toggle switches, we create all the block content
    // widgets here.
    /**
     * Creates the block content area with a checkpoint row (operations count
     * and minutes), a session log operations text field, a "Skip Present Phase"
     * checkbox, and an "Honor Reload Hint flag" checkbox.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Checkpoint
        Composite checkpointComposite = BaseWidgetUtils.createColumnContainer( composite, 5, 1 );
        BaseWidgetUtils.createLabel( checkpointComposite, "New checkpoint after", 1 );
        checkpointOperationsText = createIntegerText( checkpointComposite, "", 1 );
        BaseWidgetUtils.createLabel( checkpointComposite, "operations or", 1 );
        checkpointMinutesText = createIntegerText( checkpointComposite, "", 1 );
        BaseWidgetUtils.createLabel( checkpointComposite, "minutes", 1 );

        // Session Log
        Composite sessionLogComposite = BaseWidgetUtils.createColumnContainer( composite, 3, 1 );
        BaseWidgetUtils.createLabel( sessionLogComposite, "Session log holds", 1 );
        sessionLogOperationsText = createIntegerText( sessionLogComposite, "", 1 );
        BaseWidgetUtils.createLabel( sessionLogComposite, "operations", 1 );

        // No Present
        skipPresentPhaseButton = BaseWidgetUtils.createCheckbox( composite, "Skip Present Phase", 1 );

        // Reload Hint
        honorReloadHintFlagButton = BaseWidgetUtils.createCheckbox( composite, "Honor Reload Hint flag", 1 );
    }


    // Like the crew fabricating a numeric input panel that automatically
    // rejects non-digit characters so the operator can't accidentally enter
    // letters where an operation count or minute value is required, we create
    // an integer-only text widget with a verify listener and a fixed width.
    /**
     * Creates a text widget that only accepts digit characters, with a fixed
     * 40-pixel width hint, suitable for numeric count and interval fields.
     *
     * @param parent the parent composite
     * @param text the initial text to display
     * @param span the horizontal span in the parent's grid layout
     * @return a new integer-only text widget
     */
    private Text createIntegerText( Composite parent, String text, int span )
    {
        Text integerText = BaseWidgetUtils.createText( parent, text, span );

        integerText.addVerifyListener( event ->
            {
                if ( !event.text.matches( "[0-9]*" ) )
                {
                    event.doit = false;
                }
            } );

        GridData gd = new GridData();
        gd.widthHint = 40;
        integerText.setLayoutData( gd );

        return integerText;
    }


    // Like the crew reading the station's current SyncProv settings out of
    // the configuration record and displaying them in the control panel so
    // the administrator can see what's already set, we push each overlay
    // field value into the corresponding UI widget.
    /**
     * Refreshes the block widgets from the current {@link OlcSyncProvConfig},
     * populating the checkpoint operation count, checkpoint minutes, session
     * log operations count, and the two boolean checkboxes from the overlay.
     * Text fields are cleared when the corresponding overlay value is absent.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            // Checkpoint
            String checkpointConfiguration = overlay.getOlcSpCheckpoint();

            if ( ( checkpointConfiguration != null ) && ( !checkpointConfiguration.isEmpty() ) )
            {
                String[] checkpointConfigurationElements = checkpointConfiguration.split( " " );

                if ( checkpointConfigurationElements.length == 2 )
                {
                    // Checkpoint Operations
                    try
                    {

                        int checkpointOperations = Integer.parseInt( checkpointConfigurationElements[0] );
                        checkpointOperationsText.setText( Integer.toString( checkpointOperations ) );
                    }
                    catch ( NumberFormatException e )
                    {
                        // TODO
                        checkpointOperationsText.setText( "" );
                    }

                    // Checkpoint Minutes
                    try
                    {

                        int checkpointMinutes = Integer.parseInt( checkpointConfigurationElements[1] );
                        checkpointMinutesText.setText( Integer.toString( checkpointMinutes ) );
                    }
                    catch ( NumberFormatException e )
                    {
                        // TODO
                        checkpointMinutesText.setText( "" );
                    }
                }
                else
                {
                    // TODO
                    checkpointOperationsText.setText( "" );
                    checkpointMinutesText.setText( "" );
                }
            }
            else
            {
                // TODO
                checkpointOperationsText.setText( "" );
                checkpointMinutesText.setText( "" );
            }

            // Session Log
            Integer sessionLogOperations = overlay.getOlcSpSessionlog();

            if ( sessionLogOperations != null )
            {
                sessionLogOperationsText.setText( "" + sessionLogOperations );
            }
            else
            {
                // TODO
                sessionLogOperationsText.setText( "" );
            }

            // No Present
            skipPresentPhaseButton.setSelection( overlay.getOlcSpNoPresent() );

            // Reload Hint
            honorReloadHintFlagButton.setSelection( overlay.getOlcSpReloadHint() );
        }
    }


    // Like the crew copying the updated SyncProv settings from the control
    // panel back into the station's configuration record so they take effect,
    // we read each widget value and write it into the OlcSyncProvConfig model
    // — clearing fields that the operator left blank.
    /**
     * Saves the current widget values back into the {@link OlcSyncProvConfig},
     * writing the checkpoint string (if both fields are non-empty), the session
     * log operations count, and the two boolean flags.
     */
    public void save()
    {
        if ( overlay != null )
        {
            // Checkpoint
            String checkpointOperations = checkpointOperationsText.getText();
            String checkpointMinutes = checkpointMinutesText.getText();

            if ( ( checkpointOperations != null ) && ( !checkpointOperations.isEmpty() )
                && ( checkpointMinutes != null ) && ( !checkpointMinutes.isEmpty() ) )
            {
                overlay.setOlcSpCheckpoint( checkpointOperations + " " + checkpointMinutes );
            }
            else
            {
                overlay.setOlcSpCheckpoint( null );
            }

            // Session Log
            String sessionLogOperations = sessionLogOperationsText.getText();

            if ( ( sessionLogOperations != null ) && ( !sessionLogOperations.isEmpty() ) )
            {
                try
                {
                    overlay.setOlcSpSessionlog( Integer.parseInt( sessionLogOperations ) );
                }
                catch ( NumberFormatException e )
                {
                    overlay.setOlcSpSessionlog( null );
                }
            }
            else
            {
                overlay.setOlcSpSessionlog( null );
            }

            // No Present
            overlay.setOlcSpNoPresent( skipPresentPhaseButton.getSelection() );

            // Reload Hint
            overlay.setOlcSpReloadHint( honorReloadHintFlagButton.getSelection() );
        }
    }
}
