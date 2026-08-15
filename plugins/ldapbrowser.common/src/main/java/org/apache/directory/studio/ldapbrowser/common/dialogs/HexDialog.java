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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: HexDialog — LUKE READS THE ANCIENT JEDI TEXTS ─────────────────────
// On Ahch-To, Luke discovers the ancient Jedi texts — raw, encoded wisdom that
// looks like impenetrable symbols to the untrained eye.  He can read them as
// they are (the raw encoded form), or he can attempt to translate them into plain
// language if they happen to be legible.  Either way, he might want to carry a
// copy off the island or load texts from another source.
// Binary LDAP attribute values are much the same: raw bytes that we display in
// hex notation (the dual-column hex + ASCII view), with options to edit as text
// if the bytes are valid UTF-8, load from a file, or save to disk.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog for viewing and editing binary LDAP attribute values.  It renders
 * the raw bytes as a classic hex dump (16 bytes per row, hex on the left, ASCII
 * on the right) and offers three extra actions: "Edit as Text" (opens a
 * {@link TextDialog} — only available when the bytes are valid UTF-8), "Load"
 * (replaces the current bytes with the contents of a file), and "Save" (writes
 * the current bytes to disk).
 * Think of this class as Luke reading the ancient Jedi texts: raw encoded wisdom
 * that can be examined, copied to safety, or supplemented from another source.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HexDialog extends Dialog
{

    public static final String LOAD_FILE_NAME_TOOLTIP = "LoadFileName";

    /** The default title. */
    private static final String DIALOG_TITLE = Messages.getString( "HexDialog.HexEditor" ); //$NON-NLS-1$

    /** The button ID for the edit as text button. */
    private static final int EDIT_AS_TEXT_BUTTON_ID = 9997;

    /** The button ID for the load button. */
    private static final int LOAD_BUTTON_ID = 9998;

    /** The button ID for the save button. */
    private static final int SAVE_BUTTON_ID = 9999;

    /** Hidden text to set the filename, used for UI tests. */
    private Text loadFilenameText;

    /** The current data. */
    private byte[] currentData;

    /** The return data. */
    private byte[] returnData;

    /** The text field with the binary data. */
    private Text hexText;


    // ── LUKE PICKS UP THE FIRST TEXT ─────────────────────────────────────────
    // Luke lifts the first Jedi text off the stone altar and holds it up to the
    // light — he doesn't know yet what's encoded inside, but he holds it carefully
    // and prepares to study it.
    // We capture the initial binary data and make the dialog resizable so long
    // byte arrays have enough screen space to be comfortable.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new HexDialog pre-loaded with the binary data to display.  The
     * dialog is resizable because hex dumps can be long and users often want to
     * see more rows at once.
     *
     * <p>For example — Luke picks up the ancient text:</p>
     * <pre>
     *   HexDialog dialog = new HexDialog(shell, jpegPhotoBytes);
     *   if (dialog.open() == OK) {
     *       byte[] modified = dialog.getData();
     *   }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param initialData  the binary data to display; must not be {@code null}
     */
    public HexDialog( Shell parentShell, byte[] initialData )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.currentData = initialData;
    }


    // ── LUKE ACTS ON WHAT HE READS ────────────────────────────────────────────
    // Luke's response to the Jedi texts depends on which lesson he reaches:
    // confirm understanding (OK), translate into plain speech (Edit as Text),
    // preserve a copy on the island (Save), or study a different text (Load).
    // We route each button ID to its corresponding action: commit, text-edit,
    // save-to-file, load-from-file, or cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches button presses to the appropriate handler.  On OK we commit the
     * current byte array as the return value.  On "Edit as Text" we open a
     * {@link TextDialog} and feed the result back as updated bytes.  On Save we
     * write the bytes to a user-chosen file.  On Load we read a file and replace
     * the current bytes.  On Cancel we leave {@code returnData} null so callers
     * know nothing changed.
     *
     * <p>For example — Luke reacts to each lesson:</p>
     * <pre>
     *   case OK:             returnData = currentData;    // confirmed
     *   case EDIT_AS_TEXT:   openTextDialog();            // translate
     *   case SAVE:           writeToFile(currentData);    // preserve
     *   case LOAD:           currentData = readFromFile();// study new text
     *   default:             returnData = null;           // cancelled
     * </pre>
     *
     * @param buttonId  the SWT button ID that was activated
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            returnData = currentData;
        }
        else if ( buttonId == EDIT_AS_TEXT_BUTTON_ID )
        {
            TextDialog dialog = new TextDialog( getShell(), new String( currentData, StandardCharsets.UTF_8 ) );
            if ( dialog.open() == TextDialog.OK )
            {
                String text = dialog.getText();
                currentData = text.getBytes( StandardCharsets.UTF_8 );
                hexText.setText( toFormattedHex( currentData ) );
            }
        }
        else if ( buttonId == SAVE_BUTTON_ID )
        {
            FileDialog fileDialog = new FileDialog( getShell(), SWT.SAVE );
            fileDialog.setText( Messages.getString( "HexDialog.SaveData" ) ); //$NON-NLS-1$
            String returnedFileName = fileDialog.open();
            if ( returnedFileName != null )
            {
                try
                {
                    File file = new File( returnedFileName );
                    FileUtils.writeByteArrayToFile( file, currentData );
                }
                catch ( IOException e )
                {
                    ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                        new Status( IStatus.ERROR, BrowserCommonConstants.PLUGIN_ID, IStatus.ERROR, Messages
                            .getString( "HexDialog.CantWriteToFile" ), e ) ); //$NON-NLS-1$
                }
            }
        }
        else if ( buttonId == LOAD_BUTTON_ID )
        {
            FileDialog fileDialog = new FileDialog( getShell(), SWT.OPEN );
            fileDialog.setText( Messages.getString( "HexDialog.LoadData" ) ); //$NON-NLS-1$
            String returnedFileName = fileDialog.open();
            if ( returnedFileName != null )
            {
                loadFile( returnedFileName );
            }
        }
        else
        {
            returnData = null;
        }

        super.buttonPressed( buttonId );
    }


    // ── LUKE FETCHES A TEXT FROM STORAGE ─────────────────────────────────────
    // Luke retrieves one of the other ancient texts from its hiding place —
    // he reads every symbol and replaces the current scroll with the new one.
    // We read the named file into a byte array and update the hex display, or
    // report an error if the file can't be read.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Reads the file at the given path into the current byte array and refreshes
     * the hex display.  If the file cannot be read (e.g. permissions error, file
     * not found) we report the error through the connection UI plugin's exception
     * handler so the user sees a proper error dialog rather than a silent failure.
     *
     * <p>For example — Luke retrieves a text from the hollow tree:</p>
     * <pre>
     *   currentData = FileUtils.readFileToByteArray(new File(fileName));
     *   hexText.setText(toFormattedHex(currentData));
     * </pre>
     *
     * @param fileName  the absolute filesystem path to the file to load
     */
    private void loadFile( String fileName )
    {
        try
        {
            File file = new File( fileName );
            currentData = FileUtils.readFileToByteArray( file );
            hexText.setText( toFormattedHex( currentData ) );
        }
        catch ( IOException e )
        {
            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException(
                new Status( IStatus.ERROR, BrowserCommonConstants.PLUGIN_ID, IStatus.ERROR, Messages
                    .getString( "HexDialog.CantReadFile" ), e ) ); //$NON-NLS-1$
        }
    }


    // ── LUKE CHECKS WHETHER THE TEXT CAN BE READ ALOUD ───────────────────────
    // Some Jedi texts are written in a script Luke can read aloud; others are
    // encoded in a cipher that produces gibberish if you try to speak it.
    // Luke checks first — no point attempting a recitation of binary noise.
    // We decode the bytes as UTF-8 and look for the Unicode replacement character
    // (U+FFFD) — if it appears, the bytes are not valid UTF-8 and the "Edit as
    // Text" button should be suppressed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the byte array can be safely interpreted as a
     * UTF-8 text string — i.e. there are no invalid byte sequences that would
     * produce the Unicode replacement character (U+FFFD) during decoding.
     * We use this to decide whether to offer the "Edit as Text" button; showing
     * it for a JPEG or certificate would just confuse the user.
     *
     * <p>For example — Luke checks whether the text is legible:</p>
     * <pre>
     *   if (isEditable(bytes)) {
     *       createButton(EDIT_AS_TEXT_BUTTON_ID, "Edit as Text");
     *   }
     * </pre>
     *
     * @param b  the byte array to test; {@code null} returns {@code false}
     * @return   {@code true} if the bytes form valid UTF-8 with no replacement characters
     */
    private boolean isEditable( byte[] b )
    {
        if ( b == null )
        {
            return false;
        }

        return !( new String( b, StandardCharsets.UTF_8 ).contains( "�" ) );
    }


    // ── LUKE SETS THE READING ROOM TITLE ─────────────────────────────────────
    // Luke walks into the sacred reading chamber and the title above the door
    // reads "Jedi Archives — Hex View."  Without that sign visitors would not
    // know what they were looking at.
    // We set the window title and the hex-editor icon on the shell.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the default "Hex Editor" title and the hex-editor icon to the
     * dialog shell before it is shown.
     *
     * <p>For example — Luke enters the reading chamber:</p>
     * <pre>
     *   shell.setText("Hex Editor");
     *   shell.setIcon(HEX_EDITOR_ICON);
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
        shell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_HEXEDITOR ) );
    }


    // ── LUKE ARRANGES HIS READING TOOLS ──────────────────────────────────────
    // Luke sets up his reading table: a hidden slot for the file-path probe (used
    // by automated tests), then the translation button if the text is legible,
    // then the Load and Save scrolls, and finally the OK/Cancel seals.
    // We build the custom button bar in left-to-right order: hidden filename text
    // (for UI automation), optional "Edit as Text", Load, Save, OK, Cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the button bar with our custom set of action buttons.  We add a
     * hidden {@link Text} widget (used only by UI tests to trigger file loads
     * programmatically) then optionally "Edit as Text" (only for valid UTF-8
     * data), followed by Load, Save, OK, and Cancel.
     *
     * <p>For example — Luke lays out his reading tools:</p>
     * <pre>
     *   [hidden-test-field]  [Edit as Text?]  [Load]  [Save]  [OK]  [Cancel]
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        ((GridLayout) parent.getLayout()).numColumns++;
        loadFilenameText = new Text( parent, SWT.NONE );
        loadFilenameText.setToolTipText( LOAD_FILE_NAME_TOOLTIP );
        loadFilenameText.setBackground( parent.getBackground() );
        loadFilenameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                loadFile( loadFilenameText.getText() );
            }
        } );

        if ( isEditable( currentData ) )
        {
           createButton( parent, EDIT_AS_TEXT_BUTTON_ID, Messages.getString( "HexDialog.EditAsText" ), false ); //$NON-NLS-1$
        }

        createButton( parent, LOAD_BUTTON_ID, Messages.getString( "HexDialog.LoadDataButton" ), false ); //$NON-NLS-1$
        createButton( parent, SAVE_BUTTON_ID, Messages.getString( "HexDialog.SaveDataButton" ), false ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── LUKE OPENS THE SCROLL ON THE READING TABLE ───────────────────────────
    // Luke unrolls the Jedi text across the stone table, the ancient symbols
    // spreading out in the dim light of the island hut — two columns: the encoded
    // symbols on the left, their approximate spoken equivalents on the right.
    // We create a read-only Text widget showing the hex dump using a monospaced
    // font so the columns stay aligned.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the content area: a read-only, scrollable text widget displaying the
     * hex dump of the binary data.  We use the platform's monospaced "text" font
     * so each character takes exactly the same width and the hex / ASCII columns
     * line up cleanly.
     *
     * <p>For example — Luke unrolls the scroll on the reading table:</p>
     * <pre>
     *   hexText = new Text(composite, READ_ONLY | H_SCROLL | V_SCROLL);
     *   hexText.setFont(MONOSPACED_FONT);
     *   hexText.setText(toFormattedHex(currentData));
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the composite containing the hex-dump text widget
     */
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );

        hexText = new Text( composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
        hexText.setFont( JFaceResources.getFont( JFaceResources.TEXT_FONT ) );

        hexText.setText( toFormattedHex( currentData ) );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( ( int ) ( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 1.4 ) );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        hexText.setLayoutData( gd );

        applyDialogFont( composite );
        return composite;
    }


    // ── LUKE DECIPHERS THE ANCIENT SYMBOLS LINE BY LINE ──────────────────────
    // Luke reads each row of the Jedi text: on the left, the encoded glyphs in
    // pairs (hex digits), with a mid-row gap after the eighth pair; on the right,
    // the closest-matching spoken sounds (printable ASCII), with a dot where the
    // symbol has no spoken equivalent.
    // We implement exactly this layout: 16 bytes per row, hex on the left with an
    // extra space after byte 8, then a four-space separator, then the ASCII column
    // with a dot for non-printable bytes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Converts a raw byte array into a human-readable hex dump string.  Each row
     * shows 16 bytes: the left half is hex pairs separated by spaces (with an
     * extra space after byte 8 for readability), and the right half shows the
     * corresponding ASCII characters (printing a dot for non-printable bytes).
     * The last row is padded with spaces if it contains fewer than 16 bytes so
     * the ASCII column always starts at the same horizontal position.
     *
     * <p>For example — Luke reads the glyphs and their sounds:</p>
     * <pre>
     *   91 a1 08 23 42 b1 c1 15  52 d1 f0 24 33 62 72 82     ...#B... R..$3br.
     *   09 0a 16 17 18 19 1a 25  26 27 28 29 2a 34 35 36     .......% &amp;'()*456
     * </pre>
     *
     * @param data  the raw bytes to format; must not be {@code null}
     * @return      the formatted hex dump string, with CRLF line endings
     */
    private String toFormattedHex( byte[] data )
    {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < data.length; i++ )
        {
            // get byte
            int b = ( int ) data[i];
            if ( b < 0 )
            {
                b = 256 + b;
            }

            // format to hex, optionally prepend a 0
            String s = Integer.toHexString( b );
            if ( s.length() == 1 )
            {
                s = "0" + s; //$NON-NLS-1$
            }

            // space between hex numbers
            sb.append( s ).append( " " ); //$NON-NLS-1$

            // extra space after 8 hex numbers
            if ( ( i + 1 ) % 8 == 0 && ( i + 1 ) % 16 != 0 )
            {
                sb.append( " " ); //$NON-NLS-1$
            }

            // if end of data is reached then fill with spaces
            if ( i == data.length - 1 )
            {
                while ( ( i + 1 ) % 16 != 0 )
                {
                    sb.append( "   " ); //$NON-NLS-1$
                    if ( ( i + 1 ) % 8 == 0 )
                    {
                        sb.append( " " ); //$NON-NLS-1$
                    }
                    i++;
                }
            }

            // print ASCII characters after 16 hex numbers
            if ( ( i + 1 ) % 16 == 0 )
            {
                sb.append( "    " ); //$NON-NLS-1$
                for ( int x = i - 16 + 1; x <= i && x < data.length; x++ )
                {
                    // print ASCII character if printable
                    // otherwise print a dot
                    if ( data[x] > 32 && data[x] < 127 )
                    {
                        sb.append( ( char ) data[x] );
                    }
                    else
                    {
                        sb.append( '.' );
                    }

                    // space after 8 characters
                    if ( ( x + 1 ) % 8 == 0 )
                    {
                        sb.append( " " ); //$NON-NLS-1$
                    }
                }
            }

            // start new line after 16 hex numbers
            if ( ( i + 1 ) % 16 == 0 )
            {
                sb.append( "\r\n" ); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }


    // ── LUKE HANDS THE TEXT TO THE REBELS ────────────────────────────────────
    // After studying the Jedi texts Luke hands his annotated copy to the Alliance
    // — the knowledge they need to plan the next mission.
    // Callers retrieve the (possibly modified) byte array here after the dialog
    // closes with OK.  If cancelled, this returns {@code null}.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the (possibly modified) byte array when the user confirmed with OK,
     * or {@code null} if the user cancelled.  After OK the returned bytes reflect
     * any edits made via "Edit as Text" or any file loaded during the session.
     *
     * <p>For example — Luke hands the text to the Alliance:</p>
     * <pre>
     *   byte[] result = dialog.getData();
     *   if (result != null) { attribute.setValue(result); }
     * </pre>
     *
     * @return  the final byte array, or {@code null} if the dialog was cancelled
     */
    public byte[] getData()
    {
        return returnData;
    }
}
