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

package org.apache.directory.studio.valueeditors.time;


import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


// ── CLASS: GeneralizedTimeValueEditor — MON MOTHMA'S MISSION TIMESTAMP READER ─
// Mon Mothma's mission-log display shows timestamps in the locale-aware format
// ("Dec 15, 2023 12:00:00 PM UTC (20231215120000Z)") so every Rebel officer can
// read them without decoding the raw LDAP string.  When an officer needs to edit
// a timestamp, the reader opens the full mission clock console
// (GeneralizedTimeValueDialog).  If the stored value is malformed, the reader
// pops a confirmation asking whether to proceed with today's date instead.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value editor for LDAP GeneralizedTime syntax
 * (OID 1.3.6.1.4.1.1466.115.121.1.24).
 * In the table we format the raw timestamp into a locale-aware string
 * (e.g. {@code "Dec 15, 2023 12:00:00 PM UTC (20231215120000Z)"}) so it is
 * human-readable without losing the raw value.
 * For editing we open {@link GeneralizedTimeValueDialog} which provides
 * time spinners, a calendar, a timezone combo, and a raw text field kept
 * in sync bidirectionally.
 * If the stored string is malformed we ask the user whether to proceed with
 * today's date/time or cancel.
 * Think of this as Mon Mothma's mission timestamp reader — annotates the table
 * and opens the full clock console on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GeneralizedTimeValueEditor extends AbstractDialogStringValueEditor
{

    // ── Mon Mothma Formats the Timestamp for the Mission Log Table ───────────
    // The mission log shows "Dec 15, 2023 12:00:00 PM UTC (20231215120000Z)".
    // We parse the raw string into a GeneralizedTime, format the Date in the
    // user's locale/timezone, and append the raw value in parentheses for
    // reference.  If the string can't be parsed, we show the raw string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a locale-formatted date/time string with the raw GeneralizedTime
     * appended in parentheses.
     * Example: {@code "Dec 15, 2023 12:00:00 PM UTC (20231215120000Z)"}.
     * If the raw value cannot be parsed as GeneralizedTime, the raw string is
     * returned unchanged.
     * In raw-values mode the parent's default display is used.
     *
     * @param value  The LDAP attribute value holding the raw GeneralizedTime string.
     * @return       The formatted display string.
     */
    public String getDisplayValue( IValue value )
    {
        String displayValue = super.getDisplayValue( value );

        if ( !showRawValues() )
        {
            DateFormat targetFormat = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.LONG );

            try
            {
                GeneralizedTime generalizedTime = new GeneralizedTime( displayValue );
                Date date = generalizedTime.getCalendar().getTime();
                displayValue = targetFormat.format( date ) + " (" + displayValue + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            catch ( ParseException pe )
            {
                // show the raw value in that case
            }
        }

        return displayValue;
    }


    // ── Mon Mothma Opens the Mission Clock Console for Editing ────────────────
    // When an officer double-clicks a timestamp cell, the reader tries to parse
    // the current value.  If the value is malformed, a confirmation dialog asks
    // whether to start with today's date instead; if the officer declines, we
    // cancel without opening the clock console.  On OK we write the new
    // GeneralizedTime back to the attribute (with or without fraction).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens {@link GeneralizedTimeValueDialog} for the user to edit the timestamp.
     * If the current value fails to parse as GeneralizedTime, a confirm dialog
     * asks whether to proceed with the current date/time instead.
     * Returns {@code true} if the user clicked OK and a new value was set;
     * {@code false} if the dialog was cancelled or the user declined the bogus-value prompt.
     *
     * <p>For example — the reader opens the clock console:</p>
     * <pre>
     *   boolean changed = editor.openDialog(shell);
     *   if (changed) {
     *       // attribute now holds the new GeneralizedTime string
     *   }
     * </pre>
     *
     * @param shell  The parent SWT shell for GeneralizedTimeValueDialog.
     * @return       {@code true} if a new timestamp was committed; {@code false} otherwise.
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();
        if ( value instanceof String )
        {
            String s = ( String ) value;

            // Trying to parse the value
            GeneralizedTime generalizedTime = null;
            try
            {
                generalizedTime = "".equals( s ) ? null : new GeneralizedTime( s ); //$NON-NLS-1$
            }
            catch ( ParseException pe )
            {
                // The value could not be parsed correctly

                // Displaying an error window indicating to the user that the value is bogus
                // and asking him if he wants to continue to edit the value with current date and time selected
                if ( MessageDialog.openConfirm( PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages
                    .getString( "GeneralizedTimeValueEditor.BogusDateAndTimeValue" ), NLS.bind( //$NON-NLS-1$
                    Messages.getString( "GeneralizedTimeValueEditor.TheValueIsBogus" ), new String[] //$NON-NLS-1$
                    { s } ) ) )
                {
                    // Generating today's date and time
                    generalizedTime = new GeneralizedTime( Calendar.getInstance() );
                }
                else
                {
                    return false;
                }
            }

            // Creating and opening the dialog
            GeneralizedTimeValueDialog dialog = new GeneralizedTimeValueDialog( shell, generalizedTime );
            if ( dialog.open() == GeneralizedTimeValueDialog.OK )
            {
                GeneralizedTime newGeneralizedTime = dialog.getGeneralizedTime();

                // Checking if we need to save the generalized time
                // with or without fraction
                if ( newGeneralizedTime.getFraction() == 0 )
                {
                    setValue( newGeneralizedTime.toGeneralizedTimeWithoutFraction() );
                }
                else
                {
                    setValue( newGeneralizedTime.toGeneralizedTime() );
                }

                return true;
            }
        }

        return false;
    }
}
