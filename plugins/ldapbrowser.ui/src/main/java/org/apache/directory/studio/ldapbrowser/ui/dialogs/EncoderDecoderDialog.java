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

package org.apache.directory.studio.ldapbrowser.ui.dialogs;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: EncoderDecoderDialog — LEIA'S HOLOGRAM MESSAGE ────────────────────
// Princess Leia recorded her desperate plea inside R2-D2: a single message that
// could be decoded by whoever received it and presented in multiple forms —
// audio, holographic projection, text transcript.  This dialog does the same for
// LDAP attribute values: enter the data in any encoding (ISO-8859-1, UTF-8,
// Base64) and we instantly project all the other representations side-by-side.
// Like Leia trusting R2 with her most sensitive data, we trust this dialog to
// faithfully translate between every encoding form without corrupting a byte.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A utility dialog that lets users convert a byte string between ISO-8859-1,
 * UTF-8, hex, and Base64 representations live as they type.
 * We need this because LDAP attribute values can be stored in any of these
 * encodings, and developers often need to quickly cross-check or translate a
 * value without reaching for an external tool.
 * Think of this dialog as Leia's hologram: the same data projected in every
 * form the receiver might need — hologram, audio, text — all at once.
 */
public class EncoderDecoderDialog extends Dialog
{

    private Text iso88591Text;

    private Text iso88591HexText;

    private Text utf8Text;

    private Text utf8HexText;

    private Text base64Text;

    private Text errorText;

    private boolean inModify = false;


    // ── LEIA RECORDS HER MESSAGE INTO R2 ──────────────────────────────────────
    // Leia approaches R2-D2 in the Tantive IV corridor, about to record her
    // holographic plea to Obi-Wan; before she can speak, she has to initialize
    // R2's memory slot and configure the projection settings.
    // We do the same here: configure the dialog shell to be resizable and wire
    // it up to the parent window so it appears in the right place on screen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EncoderDecoderDialog} attached to the given parent shell.
     * We make the shell resizable right away because the text fields here can get
     * wide for long Base64 or hex strings.
     *
     * <p>For example — Leia initializes R2's recording module:</p>
     * <pre>
     *   Leia selects "record hologram" → R2 allocates memory →
     *   dialog is constructed and registered with Eclipse's window manager
     * </pre>
     *
     * @param parentShell  The Eclipse shell that owns this dialog; used for
     *                     centering and focus management.
     */
    public EncoderDecoderDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // ── R2 LABELS THE HOLOGRAM RECORDING ──────────────────────────────────────
    // R2-D2 stamps the recording with a title so whoever receives it knows what
    // it contains before they play it; without the label it's just an anonymous
    // data cylinder in a sea of other cylinders.
    // We set the window title here so the user sees "LDAP Encode/Decoder" in
    // the title bar rather than a blank or generic "Dialog" label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title to the localized "LDAP Encode/Decoder" string.
     * Eclipse calls this before the dialog becomes visible; if we skip it the
     * window has no title, which looks broken.
     *
     * <p>For example — R2 labels the data cylinder:</p>
     * <pre>
     *   stamp("SECRET PLANS") → cylinder is labeled → Obi-Wan knows what he's looking at
     *   configureShell(shell) → shell.setText("LDAP Encode/Decoder")
     * </pre>
     *
     * @param shell  The SWT Shell whose title bar we're setting.
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "EncoderDecoderDialog.LDAPEncodeDecoder" ) ); //$NON-NLS-1$
        //shell.setImage( BrowserUIPlugin.getDefault().getImage( BrowserUIConstants.IMG_IMAGEEDITOR ) );
    }


    // ── LEIA'S MESSAGE IS PROJECTED IN EVERY FORM ─────────────────────────────
    // R2 projects the hologram; simultaneously the Rebellion's comm system
    // renders the same message as audio, text transcript, and encrypted data —
    // every representation derived live from the same source bytes.
    // We build the dialog's text fields here, one per encoding, and attach
    // modify listeners so typing in any field immediately updates all the others.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's UI: five text fields showing the same data in
     * ISO-8859-1, ISO hex, UTF-8, UTF-8 hex, and Base64, plus an error display.
     * Modify listeners on the editable fields keep every representation in sync
     * as the user types — changing ISO text instantly recomputes UTF-8 and Base64.
     * The {@code inModify} flag prevents listener re-entrancy when we programmatically
     * set field values during a listener callback.
     *
     * <p>For example — R2 projects all representations simultaneously:</p>
     * <pre>
     *   User types "Ä" in ISO field →
     *     ISO hex updates to "c4"
     *     UTF-8 field shows "Ä" (different bytes)
     *     Base64 updates to "xA=="
     * </pre>
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The top-level composite we built; JFace adds it to the dialog.
     */
    protected Control createDialogArea( Composite parent )
    {

        Composite composite2 = ( Composite ) super.createDialogArea( parent );
        GridData gd1 = new GridData( GridData.FILL_BOTH );
        gd1.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd1.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite2.setLayoutData( gd1 );

        Composite composite = BaseWidgetUtils.createColumnContainer( composite2, 2, 1 );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Label iso8859Label = new Label( composite, SWT.NONE );
        iso8859Label.setText( Messages.getString( "EncoderDecoderDialog.ISOColon" ) ); //$NON-NLS-1$
        iso88591Text = new Text( composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        GridData gd = new GridData( GridData.FILL_BOTH );
        iso88591Text.setLayoutData( gd );

        Label iso8859HexLabel = new Label( composite, SWT.NONE );
        iso8859HexLabel.setText( Messages.getString( "EncoderDecoderDialog.ISOHex" ) ); //$NON-NLS-1$
        iso88591HexText = new Text( composite, SWT.BORDER | SWT.READ_ONLY );
        iso88591HexText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        Label utf8Label = new Label( composite, SWT.NONE );
        utf8Label.setText( Messages.getString( "EncoderDecoderDialog.UTF" ) ); //$NON-NLS-1$
        utf8Text = new Text( composite, SWT.BORDER );
        utf8Text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        Label utf8HexLabel = new Label( composite, SWT.NONE );
        utf8HexLabel.setText( Messages.getString( "EncoderDecoderDialog.UTFHex" ) ); //$NON-NLS-1$
        utf8HexText = new Text( composite, SWT.BORDER | SWT.READ_ONLY );
        utf8HexText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        Label base64Label = new Label( composite, SWT.NONE );
        base64Label.setText( Messages.getString( "EncoderDecoderDialog.BASE" ) ); //$NON-NLS-1$
        base64Text = new Text( composite, SWT.BORDER );
        base64Text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        errorText = new Text( composite, SWT.BORDER | SWT.READ_ONLY );
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        errorText.setLayoutData( gd );

        iso88591Text.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                if ( !inModify )
                {
                    inModify = true;
                    try
                    {
                        String isoString = iso88591Text.getText();
                        byte[] isoBytes = isoString.getBytes( "ISO-8859-1" ); //$NON-NLS-1$
                        String utf8String = new String( isoBytes, "UTF-8" ); //$NON-NLS-1$

                        iso88591HexText.setText( LdifUtils.hexEncode( isoBytes ) );
                        utf8Text.setText( utf8String );
                        utf8HexText.setText( LdifUtils.hexEncode( utf8String.getBytes( "UTF-8" ) ) ); //$NON-NLS-1$
                        base64Text.setText( LdifUtils.base64encode( isoBytes ) );
                        errorText.setText( "" ); //$NON-NLS-1$
                    }
                    catch ( Exception ex )
                    {
                        errorText.setText( ex.getMessage() );
                        ex.printStackTrace();
                    }
                    finally
                    {
                        inModify = false;
                    }
                }
            }
        } );

        utf8Text.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                if ( !inModify )
                {
                    inModify = true;
                    try
                    {
                        String utf8String = utf8Text.getText();
                        byte[] utf8Bytes = utf8String.getBytes( "UTF-8" ); //$NON-NLS-1$
                        String isoString = new String( utf8Bytes, "ISO-8859-1" ); //$NON-NLS-1$

                        iso88591Text.setText( isoString );
                        iso88591HexText.setText( LdifUtils.hexEncode( isoString.getBytes( "ISO-8859-1" ) ) ); //$NON-NLS-1$
                        utf8HexText.setText( LdifUtils.hexEncode( utf8Bytes ) );
                        base64Text.setText( LdifUtils.base64encode( utf8Bytes ) );
                        errorText.setText( "" ); //$NON-NLS-1$
                    }
                    catch ( Exception ex )
                    {
                        errorText.setText( ex.getMessage() );
                        ex.printStackTrace();
                    }
                    finally
                    {
                        inModify = false;
                    }
                }
            }
        } );

        base64Text.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                if ( !inModify )
                {
                    inModify = true;
                    try
                    {
                        byte[] base64Bytes = LdifUtils.base64decodeToByteArray( base64Text.getText() );
                        String isoString = new String( base64Bytes, "ISO-8859-1" ); //$NON-NLS-1$
                        String utf8String = LdifUtils.utf8decode( base64Bytes );

                        iso88591Text.setText( isoString );
                        iso88591HexText.setText( LdifUtils.hexEncode( isoString.getBytes( "ISO-8859-1" ) ) ); //$NON-NLS-1$
                        utf8Text.setText( utf8String );
                        utf8HexText.setText( LdifUtils.hexEncode( utf8String.getBytes( "UTF-8" ) ) ); //$NON-NLS-1$
                        errorText.setText( "" ); //$NON-NLS-1$
                    }
                    catch ( Exception ex )
                    {
                        errorText.setText( ex.getMessage() );
                        ex.printStackTrace();
                    }
                    finally
                    {
                        inModify = false;
                    }
                }
            }
        } );

        return composite;
    }

}
