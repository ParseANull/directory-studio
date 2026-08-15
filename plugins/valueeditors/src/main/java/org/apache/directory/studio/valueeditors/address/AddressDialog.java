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

package org.apache.directory.studio.valueeditors.address;


import java.util.regex.Pattern;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: AddressDialog — C-3PO TRANSLATING THE MOS EISLEY CARGO MANIFEST ──
// On Tatooine, C-3PO reads the multi-line cargo manifest from a Jawa trader.
// The document uses "$" signs as line-break separators (Jawa filing convention),
// so C-3PO spreads each line out in front of Han so it's readable.  When Han
// confirms the contents, C-3PO re-packs the "$"-separated format for filing.
// This dialog does exactly that: LDAP postal addresses use "$" as a line separator
// (RFC 4517 syntax 1.3.6.1.4.1.1466.115.121.1.41), so we decode that into a
// multi-line text widget for editing, then re-encode on OK.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog that lets the user edit an LDAP postal address in a human-friendly
 * multi-line text box.
 * LDAP stores postal addresses as a single string with {@code $} acting as a
 * line separator — for example {@code "123 Main St$Anytown$CA 90210"}.  We decode
 * that into one-line-per-line, let the user edit freely, then re-encode on OK.
 * Used by {@link AddressValueEditor} to open the editing surface.
 * Think of this class as C-3PO's translation session: he decodes the filing
 * format into something Han can read, takes Han's edits, then re-encodes for the archive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AddressDialog extends Dialog
{

    /** The initial address. */
    private String initialAddress;

    /** The return address. */
    private String returnAddress;

    /** The text widget. */
    private Text text;

    /** The postal address decoder. */
    private CharSequenceTranslator decoder;

    /** The postal address encoder. */
    private CharSequenceTranslator encoder;

    /** The checkbox to strip trailing whitespace. */
    private Button stripWhitespaceCheckbox;

    // ── C-3PO Opens the Cargo Manifest ───────────────────────────────────────
    // C-3PO receives the raw "$"-encoded Jawa document and a reference to the
    // ship's cockpit window (the parent shell) so he knows where to display it.
    // He prepares his decoder and encoder modules before the briefing begins.
    // We store the raw LDAP address string and wire up the encode/decode helpers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new AddressDialog ready to edit the given LDAP postal address.
     * We store the raw address (which uses {@code $} as a line separator) and
     * set up the codec pair that converts between the LDAP wire format and the
     * human-readable multi-line form shown in the text widget.
     *
     * <p>For example — C-3PO prepares to translate a cargo manifest:</p>
     * <pre>
     *   AddressDialog dialog = new AddressDialog(shell, "123 Main St$Anytown$CA 90210");
     *   // The dialog will display three lines in its text box
     * </pre>
     *
     * @param parentShell     The SWT shell that owns this dialog — used for positioning.
     * @param initialAddress  The raw LDAP postal address string (with {@code $} separators).
     */
    public AddressDialog( Shell parentShell, String initialAddress )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialAddress = initialAddress;
        this.returnAddress = null;
        this.decoder = Utils.createPostalAddressDecoder( BrowserCoreConstants.LINE_SEPARATOR );
        this.encoder = Utils.createPostalAddressEncoder( BrowserCoreConstants.LINE_SEPARATOR );
    }


    // ── C-3PO Titles the Briefing Window ─────────────────────────────────────
    // C-3PO labels the top of the parchment "Cargo Manifest — Address Section"
    // and clips the fleet icon to the corner so everyone knows which ship it belongs to.
    // We set the dialog's title text and window icon here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and toolbar icon.
     * Eclipse calls this just before the dialog window is made visible.
     *
     * <p>For example — C-3PO labels the translation session window:</p>
     * <pre>
     *   shell.setText("Address Editor");
     *   shell.setImage(addressEditorIcon);
     * </pre>
     *
     * @param shell  The SWT Shell we're configuring.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AddressDialog.AddressEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_ADDRESSEDITOR ) );
    }


    // ── C-3PO Lays Out the OK/Cancel Controls ────────────────────────────────
    // C-3PO places his "Accept Translation" and "Reject Translation" buttons on
    // the table, but also adds a "Strip trailing punctuation?" checkbox because
    // Jawa scribes are notorious for trailing spaces.
    // We inject a strip-whitespace checkbox into the standard JFace button bar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds our custom "Strip trailing whitespace" checkbox to the standard
     * JFace button bar, alongside the usual OK and Cancel buttons.
     * The checkbox is selected by default because trailing spaces in LDAP
     * postal addresses are almost always accidental.
     *
     * <p>For example — C-3PO's button tray setup:</p>
     * <pre>
     *   [ Strip trailing whitespace (checked) ]  [ OK ]  [ Cancel ]
     * </pre>
     *
     * @param parent  The composite that hosts the button bar.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        ((GridLayout) parent.getLayout()).numColumns = 2;
        stripWhitespaceCheckbox = BaseWidgetUtils.createCheckbox( parent, Messages.getString( "AddressDialog.StripWhitespace" ), 2 ); //$NON-NLS-1$
        stripWhitespaceCheckbox.setSelection( true );
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    private static final Pattern TRAILING_WHITESPACE = Pattern.compile( "\\s+$", Pattern.MULTILINE ); //$NON-NLS-1$


    // ── C-3PO Re-encodes and Files the Document ───────────────────────────────
    // Han approves the cargo list and C-3PO re-packs it into the official "$"
    // separated format for the Jawa archive, optionally trimming each line first.
    // We read the multi-line text, optionally strip trailing whitespace per line,
    // then re-encode to LDAP wire format and store the result.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK — we grab the edited text, optionally strip
     * trailing whitespace from each line, re-encode the result into the LDAP
     * {@code $}-separated wire format, and store it as {@code returnAddress}.
     * Callers retrieve the final value via {@link #getAddress()}.
     *
     * <p>For example — C-3PO finalises the translation:</p>
     * <pre>
     *   rawText = "123 Main St  \nAnytown  \nCA 90210";
     *   // After strip-whitespace:
     *   encoded = "123 Main St$Anytown$CA 90210";
     * </pre>
     */
    @Override
    protected void okPressed()
    {
        String lines = text.getText();
        if ( stripWhitespaceCheckbox.getSelection() ) {
            lines = TRAILING_WHITESPACE.matcher(lines).replaceAll( "" ); //$NON-NLS-1$
        }
        returnAddress = encoder.translate( lines );
        super.okPressed();
    }


    // ── C-3PO Spreads Out the Address for Reading ────────────────────────────
    // C-3PO unrolls the cargo manifest on the table and expands each "$" marker
    // into a physical line break so Han can read each street line separately.
    // We create the scrollable multi-line Text widget and populate it with the
    // decoded address.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main editing area of the dialog — a multi-line, scrollable
     * {@link Text} widget pre-populated with the decoded (human-readable) form
     * of the postal address.
     * The widget is sized generously so even long addresses with many lines
     * are comfortable to read and edit.
     *
     * <p>For example — C-3PO lays out the decoded manifest:</p>
     * <pre>
     *   123 Main St
     *   Anytown
     *   CA 90210
     * </pre>
     *
     * @param parent  The parent composite provided by JFace's dialog framework.
     * @return        The top-level composite containing our text widget.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        // text widget
        text = new Text( composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        text.setText( decoder.translate( initialAddress ) );
        // GridData gd = new GridData(GridData.GRAB_HORIZONTAL |
        // GridData.HORIZONTAL_ALIGN_FILL);
        gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        text.setLayoutData( gd );

        applyDialogFont( composite );
        return composite;
    }


    // ── C-3PO Hands Over the Re-encoded Document ──────────────────────────────
    // The translation session is complete; C-3PO passes the re-encoded cargo
    // manifest back to Han for filing in the ship's records.
    // The caller retrieves the LDAP wire-format address here after the dialog closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the edited postal address in LDAP wire format (with {@code $}
     * separators) after the user has clicked OK.
     * If the user cancelled, or if OK was pressed but the address was empty,
     * this returns {@code null}.
     *
     * <p>For example — Han retrieves the filed cargo address:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       String ldapAddress = dialog.getAddress();
     *       // → "123 Main St$Anytown$CA 90210"
     *   }
     * </pre>
     *
     * @return  The LDAP-encoded postal address string, or {@code null} if cancelled.
     */
    public String getAddress()
    {
        return returnAddress;
    }

}
