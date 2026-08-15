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

package org.apache.directory.studio.valueeditors.adtime;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ActiveDirectoryTimeValueEditor — R2-D2 ANNOUNCING THE MISSION CLOCK ─
// R2-D2 reads the Imperial FILETIME from the captured datapad and announces it
// in plain Galactic Standard ("15 March 2023, 14:32 GST") so the crew can
// understand it without knowing the Imperial tick format.
// When Luke wants to change the timestamp, R2-D2 opens his full clock console
// (ActiveDirectoryTimeValueDialog) and lets Luke pick the new time visually.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for Active Directory timestamp attributes such as
 * {@code pwdLastSet}, {@code accountExpires}, {@code lastLogon},
 * {@code lastLogoff}, {@code lastLogonTimeStamp}, and {@code badPasswordTime}.
 * For display we convert the raw AD FILETIME integer to a localised date/time
 * string.  For editing we open {@link ActiveDirectoryTimeValueDialog} which
 * provides a graphical date/time picker.
 * Think of this as R2-D2's timestamp announcer: he reads the Imperial ticks and
 * speaks the date in plain language, then opens the clock console on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ActiveDirectoryTimeValueEditor extends AbstractDialogStringValueEditor
{
    // ── R2-D2 Reads the Timestamp Aloud ──────────────────────────────────────
    // Luke asks what the "pwdLastSet" value means; R2-D2 reads the raw number
    // "133000000000000000" and announces "15 Mar 2023, 2:32 PM (local time)".
    // The special value "0" means "never set" so R2-D2 just says "0" for that.
    // We format the FILETIME as a locale-aware date/time string, or fall back
    // to the raw number if it can't be parsed or if raw-values mode is on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable representation of the AD timestamp for the table cell.
     * Normally we format it as a locale-specific date/time string followed by the
     * raw number in parentheses (e.g. "15 Mar 2023 14:32 BST (133000000000000000)").
     * When the value is {@code "0"} (the AD "never" sentinel) we return it as-is.
     * In raw-values mode we return the raw number unchanged.
     *
     * <p>For example — R2-D2 announces a password-last-set time:</p>
     * <pre>
     *   LDAP stored:  "133000000000000000"
     *   Displayed as: "15 Mar 2023 14:32:00 BST (133000000000000000)"
     *
     *   LDAP stored:  "0"
     *   Displayed as: "0"   (AD sentinel meaning "never set")
     * </pre>
     *
     * @param value  The LDAP attribute value containing the raw AD FILETIME string.
     * @return       A human-friendly date/time string, or the raw number if unparseable.
     */
    @Override
    public String getDisplayValue( IValue value )
    {
        String displayValue = super.getDisplayValue( value );

        if ( !showRawValues() )
        {
            // Special case for the "0" value where we don't want to display date
            // that makes no sense ("0" being the default value, it happened a lot).
            if ( "0".equals( displayValue ) ) //$NON-NLS-1$
            {
                return displayValue;
            }

            DateFormat targetFormat = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.LONG );

            try
            {
                long adTimeValue = Long.parseLong( displayValue );
                Date date = ActiveDirectoryTimeUtils.convertToCalendar( adTimeValue ).getTime();
                displayValue = targetFormat.format( date ) + " (" + displayValue + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            catch ( NumberFormatException e )
            {
                // show the raw value in that case
            }
        }

        return displayValue;
    }


    // ── R2-D2 Opens the Clock Console ────────────────────────────────────────
    // Luke wants to change a timestamp, so R2-D2 opens the full clock console
    // with the existing value pre-loaded.  If the raw number is garbled, R2-D2
    // asks Luke whether to start fresh from today's date instead.
    // We parse the existing FILETIME string, handle the bogus-value case with a
    // confirmation dialog, then open ActiveDirectoryTimeValueDialog for editing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link ActiveDirectoryTimeValueDialog} so the user can edit the
     * AD timestamp with a graphical date/time picker.
     * If the current string value is not a valid FILETIME integer, we ask the user
     * whether they want to proceed with today's date as the fallback.
     * Returns {@code true} if the user confirmed a new value; {@code false} if
     * they cancelled.
     *
     * <p>For example — R2-D2 opens the clock console:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // The attribute now holds the new AD FILETIME string
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for the dialog.
     * @return       {@code true} if the value was updated; {@code false} if cancelled.
     */
    @Override
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof String )
        {
            String s = ( String ) value;
            long adTimeValue = 0;

            if ( !EMPTY.equals( s ) ) //$NON-NLS-1$
            {
                // Trying to parse the value
                try
                {
                    adTimeValue = Long.parseLong( s );
                }
                catch ( NumberFormatException pe )
                {
                    // The value could not be parsed correctly

                    // Displaying an error window indicating to the user that the value is bogus
                    // and asking him if he wants to continue to edit the value with current date and time selected
                    if ( MessageDialog.openConfirm( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages
                        .getString( "ActiveDirectoryTimeValueEditor.BogusDateAndTimeValue" ), NLS.bind( //$NON-NLS-1$
                        Messages.getString( "ActiveDirectoryTimeValueEditor.TheValueIsBogus" ), new String[] //$NON-NLS-1$
                        { s } ) ) )
                    {
                        // Generating today's date and time
                        adTimeValue = ActiveDirectoryTimeUtils.convertToActiveDirectoryTime( Calendar.getInstance() );
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else
            {
                // Generating today's date and time
                adTimeValue = ActiveDirectoryTimeUtils.convertToActiveDirectoryTime( Calendar.getInstance() );
            }

            // Creating and opening the dialog
            ActiveDirectoryTimeValueDialog dialog = new ActiveDirectoryTimeValueDialog( shell, adTimeValue );

            if ( dialog.open() == ActiveDirectoryTimeValueDialog.OK )
            {
                setValue( Long.toString( dialog.getValue() ) ); //$NON-NLS-1$

                return true;
            }
        }

        return false;
    }
}
