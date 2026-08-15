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
package org.apache.directory.studio.schemaeditor.view;


import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ViewUtils — LEIA'S HOLOGRAM MESSAGE TO OBI-WAN ────────────────────
// Princess Leia crouches in front of R2-D2 and records an urgent message
// projected as a shimmering blue hologram — an error in distress, a warning
// to be cautious, or vital information the Rebellion needs.
// Obi-Wan receives it, watches it, and decides whether to act.
// This utility class is that hologram projector: every method here prepares
// and fires a dialog at the user, carrying text they urgently need to see —
// whether it is an error, a warning, a question, or just information.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A static utility class that packages up the most common UI helper operations
 * needed across the Schema Editor view layer: formatting alias lists,
 * validating LDAP names, and firing the right flavour of message dialog.
 * Think of this class as the hologram projector Leia uses in her message to
 * Obi-Wan: it ensures the right words reach the user at the right moment, in
 * the right style — error in red, warning in amber, information in blue.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ViewUtils
{
    /** The Black Color */
    public static final Color COLOR_BLACK = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
        .getDisplay().getSystemColor( SWT.COLOR_BLACK );

    /** The Red Color */
    public static final Color COLOR_RED = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
        .getSystemColor( SWT.COLOR_RED );


    // ── LEIA COMPRESSES HER MESSAGE BEFORE TRANSMITTING ──────────────────────
    // Leia needs to pack everything she wants to say into one tight hologram
    // recording — not three separate messages but a single coherent broadcast.
    // We do the same: given a list of alias strings, we join them into one
    // comma-separated string ready to display in a single UI field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Joins a list of alias strings into a single comma-separated display string,
     * ready to drop into a text field or label.
     * LDAP schema elements can have multiple aliases (e.g. {@code cn} and
     * {@code commonName} for the same attribute type), and we often need to
     * show them all at once rather than picking just one.
     * An empty list produces an empty string; a single alias produces that alias
     * with no trailing comma.
     *
     * <p>For example — Leia broadcasts all the Rebel base names in one transmission:</p>
     * <pre>
     *   aliases = ["Yavin IV", "Echo Base", "Home One"]
     *   result  = "Yavin IV, Echo Base, Home One"
     * </pre>
     *
     * @param aliases  the list of alias strings to concatenate; must not be {@code null}
     * @return         a comma-separated string containing all aliases, or an empty
     *                 string if the list is empty
     */
    public static String concateAliases( List<String> aliases )
    {
        StringBuffer sb = new StringBuffer();
        if ( aliases.size() > 0 )
        {
            sb.append( aliases.get( 0 ) );
            for ( int i = 1; i < aliases.size(); i++ )
            {
                sb.append( ", " ); //$NON-NLS-1$
                sb.append( aliases.get( i ) );
            }
        }

        return sb.toString();
    }


    // ── LEIA CONFIRMS SHE IS TRANSMITTING TO THE RIGHT PERSON ────────────────
    // Before sending the hologram, Leia's droid checks the recipient's ID
    // against the encryption key — is this really General Kenobi, or an
    // Imperial intercept?
    // We do the same: before letting a name through, we verify it matches the
    // RFC 2252 allowed-characters pattern so we don't store something malformed
    // in the LDAP directory.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether a proposed schema element name is syntactically legal
     * according to RFC 2252 (LDAP Attribute Syntax Definitions).
     * LDAP names have strict rules — they must start with a letter and contain
     * only letters, digits, and hyphens — and we need to enforce this before
     * saving to the directory to avoid corrupting the schema.
     * The allowed-character regex is stored in the messages properties file
     * under the key {@code ViewUtils.AllowedCharacters}.
     *
     * <p>For example — Leia verifies the recipient before transmitting:</p>
     * <pre>
     *   verifyName("cn")          → true   (valid LDAP name)
     *   verifyName("123invalid")  → false  (must start with a letter)
     *   verifyName("bad name!")   → false  (spaces and ! are not allowed)
     * </pre>
     *
     * @param name  the proposed LDAP name to validate; must not be {@code null}
     * @return      {@code true} if the name is syntactically valid per RFC 2252,
     *              {@code false} otherwise
     */
    public static boolean verifyName( String name )
    {
        return name.matches( Messages.getString( "ViewUtils.AllowedCharacters" ) ); //$NON-NLS-1$
    }


    // ── LEIA SENDS AN URGENT DISTRESS SIGNAL ─────────────────────────────────
    // Something has gone badly wrong aboard the Tantive IV — the ship is under
    // attack, the data is compromised, a critical system has failed.
    // Leia fires off a bright red error hologram so that whoever receives it
    // knows this is not a drill.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an error dialog with the given title and message and waits for the
     * user to acknowledge it before returning.
     * Use this when something has gone wrong that the user must know about —
     * a failed save, an invalid schema element, an unrecoverable state.
     *
     * <p>For example — Leia's distress hologram plays in full red:</p>
     * <pre>
     *   title   = "Schema Save Failed"
     *   message = "Could not write schema to disk: disk full."
     *   → Error dialog appears; user clicks OK; method returns true.
     * </pre>
     *
     * @param title    the dialog window title, shown in the title bar
     * @param message  the error message body shown to the user
     * @return         {@code true} if the user pressed OK, {@code false} otherwise
     */
    public static boolean displayErrorMessageDialog( String title, String message )
    {
        return displayMessageDialog( MessageDialog.ERROR, title, message );
    }


    // ── LEIA SENDS A CAUTION ADVISORY ────────────────────────────────────────
    // Not a full emergency — the ship is intact — but something is amiss and
    // the Rebellion should know about it before proceeding.
    // Leia records a yellow-tinged hologram: "Proceed, but be careful out there."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a warning dialog with the given title and message and waits for
     * the user to acknowledge it before returning.
     * Use this when the situation is not immediately catastrophic but the user
     * should be aware of a potential problem before they continue.
     *
     * <p>For example — Leia sends a caution advisory to the fleet:</p>
     * <pre>
     *   title   = "Schema Inconsistency Detected"
     *   message = "Attribute type 'cn' is missing a syntax definition."
     *   → Warning dialog appears; user clicks OK; method returns true.
     * </pre>
     *
     * @param title    the dialog window title
     * @param message  the warning message body
     * @return         {@code true} if the user pressed OK, {@code false} otherwise
     */
    public static boolean displayWarningMessageDialog( String title, String message )
    {
        return displayMessageDialog( MessageDialog.WARNING, title, message );
    }


    // ── LEIA'S FULL HOLOGRAM BRIEFING ────────────────────────────────────────
    // "Help me, Obi-Wan Kenobi. You're my only hope." — a clear, calm message
    // carrying the information the recipient needs, with no alarm attached.
    // This is just a status update, not a crisis; the Rebellion can act on it
    // at their own pace.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an informational dialog with the given title and message and waits
     * for the user to acknowledge it before returning.
     * Use this for neutral status updates the user should know about — not
     * errors, not warnings, just facts that are good to have.
     *
     * <p>For example — Leia delivers her briefing hologram:</p>
     * <pre>
     *   title   = "Import Complete"
     *   message = "12 schemas were imported successfully."
     *   → Information dialog appears; user clicks OK; method returns true.
     * </pre>
     *
     * @param title    the dialog window title
     * @param message  the informational message body
     * @return         {@code true} if the user pressed OK, {@code false} otherwise
     */
    public static boolean displayInformationMessageDialog( String title, String message )
    {
        return displayMessageDialog( MessageDialog.INFORMATION, title, message );
    }


    // ── LEIA ASKS OBI-WAN TO MAKE A DECISION ─────────────────────────────────
    // "Will you help me?" — Leia doesn't just broadcast; she asks a direct
    // question and needs a direct answer before the Rebellion can act.
    // This dialog waits for the user to say Yes or No.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a question dialog with the given title and message, waits for the
     * user to click Yes or No, and returns their choice.
     * Use this when the code needs a binary decision from the user before it
     * can proceed — "Delete this schema?" requires a confirmed yes, not an
     * implicit continue.
     *
     * <p>For example — Leia asks Obi-Wan for a decision:</p>
     * <pre>
     *   title   = "Delete Schema"
     *   message = "Are you sure you want to delete 'inetOrgPerson'?"
     *   → Question dialog appears; user clicks Yes → returns true.
     *                             user clicks No  → returns false.
     * </pre>
     *
     * @param title    the dialog window title
     * @param message  the question to put to the user
     * @return         {@code true} if the user pressed Yes/OK,
     *                 {@code false} if they pressed No or closed the dialog
     */
    public static boolean displayQuestionMessageDialog( String title, String message )
    {
        return displayMessageDialog( MessageDialog.QUESTION, title, message );
    }


    // ── THE HOLOGRAM PROJECTOR FIRES UP ──────────────────────────────────────
    // Regardless of which kind of message Leia is sending — distress, caution,
    // briefing, or question — the same projector hardware fires the hologram
    // into the room, with the message style being the only thing that changes.
    // This private method is that shared projector: it handles the actual SWT
    // dialog call so every public method above stays focused on its specific
    // purpose without duplicating boilerplate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link MessageDialog} of the specified kind using the active
     * workbench shell, then blocks until the user dismisses it.
     * This is the single place where we actually call
     * {@link MessageDialog#open}, keeping the four public display methods above
     * thin and readable.
     * We use {@link SWT#NONE} for the style bits because the dialog kind
     * already controls the button set (OK for info/error/warning, Yes/No for
     * questions).
     *
     * <p>For example — Leia's projector fires regardless of message type:</p>
     * <pre>
     *   kind = MessageDialog.ERROR   → red error dialog
     *   kind = MessageDialog.WARNING → yellow warning dialog
     *   kind = MessageDialog.QUESTION → yes/no question dialog
     * </pre>
     *
     * @param kind     one of the {@link MessageDialog} kind constants
     *                 (ERROR, WARNING, INFORMATION, QUESTION, etc.)
     * @param title    the dialog window title
     * @param message  the message body
     * @return         {@code true} if the user pressed OK or Yes,
     *                 {@code false} otherwise
     */
    private static boolean displayMessageDialog( int kind, String title, String message )
    {
        return MessageDialog.open( kind, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title,
            message, SWT.NONE );
    }
}
