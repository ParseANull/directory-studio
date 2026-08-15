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


import java.util.List;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.common.ui.widgets.FileBrowserWidget;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAuditlogConfig;


// Like the Imperial construction crews assembling the audit log module
// onto the second Death Star so every command issued on the station is
// recorded to a log file for later review, we build the AuditLog overlay
// configuration block that lets the administrator point the overlay at
// the LDIF log file where all directory changes will be captured.
/**
 * This class implements the configuration block for the AuditLog overlay.
 * We present a single file-browser field so the administrator can select
 * the target log file path, and we read/write that value to and from the
 * {@link OlcAuditlogConfig} model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuditLogOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcAuditlogConfig>
{
    // UI widgets
    private FileBrowserWidget fileBrowserWidget;


    // Like the construction crew initializing a fresh audit-log module
    // with no prior log file configured, we create the block with a new
    // empty OlcAuditlogConfig so there is always a non-null overlay to
    // populate later.
    /**
     * Creates a new AuditLogOverlayConfigurationBlock with a fresh, empty
     * {@link OlcAuditlogConfig} as the backing model.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     */
    public AuditLogOverlayConfigurationBlock( OverlayDialog dialog )
    {
        super( dialog );
        setOverlay( new OlcAuditlogConfig() );
    }


    // Like the crew slotting an already-configured audit-log module into
    // the station's infrastructure, we accept an existing OlcAuditlogConfig
    // and store it — defaulting to a fresh one if null was passed.
    /**
     * Creates a new AuditLogOverlayConfigurationBlock backed by the given
     * {@link OlcAuditlogConfig}. If {@code overlay} is {@code null} we
     * create a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param overlay the existing AuditLog overlay config to edit, or {@code null}
     */
    public AuditLogOverlayConfigurationBlock( OverlayDialog dialog, OlcAuditlogConfig overlay )
    {
        super( dialog );

        if ( overlay == null )
        {
            setOverlay( new OlcAuditlogConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew installing the log file selector panel
    // into the audit module so the operator can pick the destination path
    // for audit records, we create a file-browser widget with LDIF and
    // log file filters.
    /**
     * Creates the block content area with a "Log File:" label and a
     * file-browser widget filtered for {@code .ldif} and {@code .log} files.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        BaseWidgetUtils.createLabel( composite, "Log File:", 1 );

        fileBrowserWidget = new FileBrowserWidget( "", new String[]
            { ".ldif", ".log" }, FileBrowserWidget.TYPE_OPEN );
        fileBrowserWidget.createWidget( composite );
    }


    // Like the crew reading the existing log file path out of the station's
    // configuration record and setting the selector widget to show that path,
    // we pull the first entry from the audit log file list in the overlay
    // and push it into the file-browser widget.
    /**
     * Refreshes the file-browser widget from the current {@link OlcAuditlogConfig},
     * populating the file path from the overlay's log file list if one is present.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            List<String> auditLogFilesList = overlay.getOlcAuditlogFile();

            if ( auditLogFilesList != null && !auditLogFilesList.isEmpty() )
            {
                fileBrowserWidget.setFilename( auditLogFilesList.get( 0 ) );
            }
        }
    }


    // Like the crew updating the audit module's configuration record with
    // the log file path the administrator selected, we read the current
    // file path from the widget and write it back into the overlay config —
    // clearing any previous value first.
    /**
     * Saves the current file-browser widget value back into the
     * {@link OlcAuditlogConfig}, replacing any previous log file entry.
     * If the selected filename is empty, the log file list is simply cleared.
     */
    public void save()
    {
        if ( overlay != null )
        {
            overlay.clearOlcAuditlogFile();

            String filename = fileBrowserWidget.getFilename();

            if ( !Strings.isEmpty( filename ) )
            {
                overlay.addOlcAuditlogFile( filename );
            }
        }
    }
}
