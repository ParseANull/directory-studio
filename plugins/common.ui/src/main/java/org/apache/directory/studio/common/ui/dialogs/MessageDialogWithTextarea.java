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
package org.apache.directory.studio.common.ui.dialogs;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: MessageDialogWithTextarea — REBELLION BRIEFING WITH SCROLLING REPORT
// After the Battle of Yavin, General Dodonna not only gave the pilots a short
// verbal briefing but also handed out a detailed scrollable mission dossier.
// This dialog does the same: it shows a concise message up top and then
// displays a read-only, scrollable text area below so the user can read the
// full details without the window becoming impossibly tall.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We extend {@link MessageDialog} to add a resizable, read-only text area
 * below the main message.  Use this when you need to display a short summary
 * and a longer detail block (such as a stack trace or diff output) in the
 * same dialog without overwhelming the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MessageDialogWithTextarea extends MessageDialog
{

    private String detailMessage;
    private Text textArea;


    // ── CONSTRUCTOR MessageDialogWithTextarea — ASSEMBLING THE BRIEFING PACKET ─
    // We assemble the briefing packet: the short headline goes into the standard
    // message dialog, and we stash the long detail text for later when
    // createCustomArea builds the scrollable dossier below it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a resizable information dialog with a headline message and a
     * longer detail text that appears in a scrollable text area below it.
     *
     * @param parentShell   the shell that will own this dialog
     * @param title         the dialog window title
     * @param message       the short headline message shown above the text area
     * @param detailMessage the full detail text shown in the scrollable text area
     */
    public MessageDialogWithTextarea( Shell parentShell, String title, String message, String detailMessage )
    {
        super( parentShell, title, null, message, INFORMATION, new String[]
            { IDialogConstants.OK_LABEL }, OK );
        setShellStyle( SWT.RESIZE );
        this.detailMessage = detailMessage;
    }


    // ── METHOD createCustomArea — UNROLLING THE MISSION DOSSIER ──────────────
    // Below the headline we unroll the full mission dossier — a read-only,
    // scrollable text area pre-filled with the detail message.  The monospaced
    // font makes stack traces and formatted output much easier to read.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the scrollable text area that appears below the standard message
     * dialog content.  The area is read-only and uses a monospaced font, making
     * it suitable for displaying stack traces, logs, or other structured text.
     * The size is proportional to the dialog's minimum message area width.
     *
     * @param parent the composite that hosts the custom area
     * @return the text area control
     * {@inheritDoc}
     */
    @Override
    protected Control createCustomArea( Composite parent )
    {
        textArea = new Text( parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
        textArea.setFont( JFaceResources.getFont( JFaceResources.TEXT_FONT ) );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( ( int ) ( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 ) );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 4 );
        textArea.setLayoutData( gd );
        //textArea.setBackground( parent.getBackground() );
        textArea.setText( detailMessage );

        return textArea;
    }

}
