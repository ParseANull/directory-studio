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


import java.util.Calendar;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


// ── CLASS: ActiveDirectoryTimeValueDialog — R2-D2'S MISSION CLOCK CONSOLE ────
// After escaping Tatooine, R2-D2 opens his chronometer console so the crew can
// set mission timestamps.  The console shows three panels: hour/minute/second
// spinners, a graphical calendar for the date, and a raw FILETIME field for
// those who know the Imperial tick format.  All three stay in sync — change
// one and R2-D2 immediately updates the other two.
// This dialog is that console: three linked views of one Active Directory timestamp.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for editing an Active Directory (FILETIME) timestamp.
 * Active Directory stores dates as 64-bit integers (100-nanosecond ticks since
 * 1 Jan 1601).  This dialog exposes three linked views of the same moment:
 * an HH:MM:SS spinner row, a graphical calendar, and a raw-value text field.
 * Changing any one view immediately updates the other two.
 * Used by {@link ActiveDirectoryTimeValueEditor} when the user opens an
 * AD timestamp attribute for editing.
 * Think of this as R2-D2's mission clock console — three instruments, one timestamp.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ActiveDirectoryTimeValueDialog extends Dialog
{
    /** The value */
    private long value;

    //
    // UI Fields
    //

    // Time
    private Spinner hoursSpinner;
    private Spinner minutesSpinner;
    private Spinner secondsSpinner;

    // Date
    private DateTime dateCalendar;

    // Raw value
    private Text rawValueText;

    //
    // Listeners
    //

    /**
     * The modify listener of the hours field.
     */
    private ModifyListener hoursModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            updateValueFromNonRawFields();

            removeListeners();
            updateRawFields();
            addListeners();
        }
    };

    /**
     * The modify listener of the minutes field.
     */
    private ModifyListener minutesModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            updateValueFromNonRawFields();

            removeListeners();
            updateRawFields();
            addListeners();
        }
    };

    /**
     * The modify listener of the seconds field.
     */
    private ModifyListener secondsModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            updateValueFromNonRawFields();

            removeListeners();
            updateRawFields();
            addListeners();
        }
    };

    /**
     * The selection listener of the calendar.
     */
    private SelectionListener dateSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            updateValueFromNonRawFields();

            removeListeners();
            updateRawFields();
            addListeners();
        }
    };

    /**
     * The modify listener of the raw field.
     */
    private ModifyListener rawValueModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            try
            {
                value = Long.parseLong( rawValueText.getText() );

                removeListeners();
                updateNonRawFields();
                addListeners();
            }
            catch ( NumberFormatException e1 )
            {
                return;
            }
        }
    };

    private VerifyListener rawValueVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            // Prevent the user from entering anything but an integer
            if ( !e.text.matches( "(-)?([0-9])*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };


    // ── R2-D2 Receives the Imperial Timestamp ────────────────────────────────
    // R2-D2 powers up his chronometer console and loads a specific FILETIME
    // tick count from the mission briefing datapad.
    // We store the given AD timestamp and delegate to the resizable-shell constructor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an ActiveDirectoryTimeValueDialog pre-loaded with the given AD FILETIME value.
     * Useful when an existing timestamp needs to be edited rather than created from scratch.
     *
     * <p>For example — R2-D2 loads a specific mission timestamp:</p>
     * <pre>
     *   long existingAdTime = 132000000000000000L;
     *   ActiveDirectoryTimeValueDialog dialog =
     *       new ActiveDirectoryTimeValueDialog(shell, existingAdTime);
     * </pre>
     *
     * @param parentShell  The SWT shell that owns this dialog.
     * @param value        The initial AD FILETIME value (100-ns ticks since 1601-01-01).
     */
    public ActiveDirectoryTimeValueDialog( Shell parentShell, long value )
    {
        this( parentShell );
        this.value = value;
    }


    // ── R2-D2 Defaults to "Now" ───────────────────────────────────────────────
    // R2-D2 opens the chronometer console without a pre-loaded timestamp, so he
    // defaults to the current mission time (right now) as the starting value.
    // We convert the current system time to AD FILETIME so the UI starts sensibly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an ActiveDirectoryTimeValueDialog defaulting to the current date and time.
     * Useful when creating a brand-new timestamp attribute value — we seed it
     * with "now" so the user sees something reasonable rather than epoch zero.
     *
     * <p>For example — R2-D2 starts the console at the present moment:</p>
     * <pre>
     *   ActiveDirectoryTimeValueDialog dialog =
     *       new ActiveDirectoryTimeValueDialog(shell);
     *   // Pre-filled with today's date and current time
     * </pre>
     *
     * @param parentShell  The SWT shell that owns this dialog.
     */
    public ActiveDirectoryTimeValueDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.value = ActiveDirectoryTimeUtils.convertToActiveDirectoryTime( Calendar.getInstance() );
    }


    // ── R2-D2 Labels the Console Window ──────────────────────────────────────
    // R2-D2 lights up the console header: "Date and Time Editor" in blue LEDs,
    // with the standard generalised-time icon in the corner.
    // We set the dialog title and icon on the SWT shell here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and icon.
     * Eclipse calls this just before the dialog becomes visible.
     *
     * @param shell  The SWT Shell we're configuring.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ActiveDirectoryTimeValueDialog.DateAndTimeEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_GENERALIZEDTIMEEDITOR ) );
    }


    // ── R2-D2 Assembles the Three Instrument Panels ───────────────────────────
    // R2-D2 snaps the three panels into the console frame: the time spinners on
    // the left, the calendar grid on the right, and the raw-value display below.
    // He then loads the initial timestamp and wires up the cross-panel listeners.
    // We build the three sections, initialise them from the current value, and
    // attach the listeners that keep all three in sync.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full dialog content: a two-column layout with time spinners and
     * a graphical calendar above, and a raw FILETIME text field below a separator.
     * After constructing the widgets, we initialise them from the current {@code value}
     * and wire up listeners so any change in any panel propagates to the other two.
     *
     * @param parent  The parent composite provided by JFace.
     * @return        The top-level composite containing all three panels.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // Main composites
        Composite composite = ( Composite ) super.createDialogArea( parent );
        Composite dualComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Creating dialog areas
        createTimeDialogArea( dualComposite );
        createDateDialogArea( dualComposite );
        createRawValueDialogArea( dualComposite );

        // Initializing with initial value
        initWithInitialValue();

        // Adding listeners
        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // ── R2-D2 Installs the Time Spinners ─────────────────────────────────────
    // R2-D2 fits the hours, minutes, and seconds dials into the left column of
    // the console, each labelled with a colon separator.
    // We create three spinners with appropriate min/max ranges for time fields.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Time" section — three {@link Spinner} widgets (HH, MM, SS)
     * arranged in a row with colon labels between them.
     * Hours run 0–23; minutes and seconds run 0–59.
     *
     * @param parent  The composite to host the time section.
     */
    private void createTimeDialogArea( Composite parent )
    {
        // Label
        Label timeLabel = new Label( parent, SWT.NONE );
        timeLabel.setText( Messages.getString( "ActiveDirectoryTimeValueDialog.Time" ) ); //$NON-NLS-1$

        Composite rightComposite = BaseWidgetUtils.createColumnContainer( parent, 5, 1 );

        // Hours
        hoursSpinner = new Spinner( rightComposite, SWT.BORDER );
        hoursSpinner.setMinimum( 0 );
        hoursSpinner.setMaximum( 23 );
        hoursSpinner.setTextLimit( 2 );
        hoursSpinner.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );

        Label label1 = BaseWidgetUtils.createLabel( rightComposite, ":", 1 ); //$NON-NLS-1$
        label1.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

        // Minutes
        minutesSpinner = new Spinner( rightComposite, SWT.BORDER );
        minutesSpinner.setMinimum( 0 );
        minutesSpinner.setMaximum( 59 );
        minutesSpinner.setTextLimit( 2 );
        minutesSpinner.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

        Label label2 = BaseWidgetUtils.createLabel( rightComposite, ":", 1 ); //$NON-NLS-1$
        label2.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

        // Seconds
        secondsSpinner = new Spinner( rightComposite, SWT.BORDER );
        secondsSpinner.setMinimum( 0 );
        secondsSpinner.setMaximum( 59 );
        secondsSpinner.setTextLimit( 2 );
        secondsSpinner.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );
    }


    // ── R2-D2 Installs the Calendar Grid ─────────────────────────────────────
    // R2-D2 slots the graphical calendar display into the right column of the
    // console — a month grid the user can click to pick a date visually.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Date" section — an SWT {@link DateTime} calendar widget that
     * shows a full month grid for visual date selection.
     *
     * @param parent  The composite to host the date section.
     */
    private void createDateDialogArea( Composite parent )
    {
        // Label
        Label dateLabel = BaseWidgetUtils.createLabel( parent,
            Messages.getString( "ActiveDirectoryTimeValueDialog.Date" ), 1 ); //$NON-NLS-1$
        dateLabel.setLayoutData( new GridData( SWT.NONE, SWT.TOP, false, false ) );

        Composite rightComposite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Calendar
        dateCalendar = new DateTime( rightComposite, SWT.CALENDAR | SWT.BORDER );
        dateCalendar.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── R2-D2 Adds the Raw FILETIME Display ──────────────────────────────────
    // Below the main console panels, R2-D2 provides a direct-access text field
    // showing the raw Imperial FILETIME integer for power users who know the format.
    // Only digits (and an optional leading minus) are accepted.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Raw value" section — a text field that shows and accepts the
     * raw Active Directory FILETIME integer directly.
     * The field is separated from the pickers above by a horizontal rule and
     * validates input to integers only (including an optional leading minus sign).
     *
     * @param parent  The composite to host the raw value section.
     */
    private void createRawValueDialogArea( Composite parent )
    {
        // Separator
        Label separatorLabel = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        // Label
        BaseWidgetUtils.createLabel( parent, Messages.getString( "ActiveDirectoryTimeValueDialog.RawValue" ), 1 ); //$NON-NLS-1$

        // Text
        rawValueText = BaseWidgetUtils.createText( parent, "", 1 ); //$NON-NLS-1$
    }


    // ── R2-D2 Loads the Starting Timestamp ───────────────────────────────────
    // R2-D2 reads the mission briefing's initial timestamp and sets all three
    // console panels to that moment so the crew sees a consistent starting state.
    // We push the current value into both the picker panels and the raw text field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates all three sections (time spinners, calendar, raw field) from the
     * current internal {@code value}.  Called once during dialog creation.
     */
    private void initWithInitialValue()
    {
        updateNonRawFields();
        updateRawFields();
    }


    // ── R2-D2 Pushes the Value to the Visual Panels ──────────────────────────
    // After the raw FILETIME field changes, R2-D2 translates the new tick count
    // into a human date/time and refreshes the spinner and calendar displays.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the time spinners and calendar to reflect the current {@code value}.
     * Called whenever the raw text field changes, so the visual pickers stay in sync.
     */
    private void updateNonRawFields()
    {
        // Getting the calendar
        Calendar calendar = getCalendarFromValue();

        // Time
        hoursSpinner.setSelection( calendar.get( Calendar.HOUR_OF_DAY ) );
        minutesSpinner.setSelection( calendar.get( Calendar.MINUTE ) );
        secondsSpinner.setSelection( calendar.get( Calendar.SECOND ) );

        // Date
        dateCalendar.setDate( calendar.get( Calendar.YEAR ), calendar.get( Calendar.MONTH ), calendar
            .get( Calendar.DAY_OF_MONTH ) );
    }


    // ── R2-D2 Updates the Raw Tick Display ───────────────────────────────────
    // After the user picks a new date or adjusts a spinner, R2-D2 re-encodes
    // the result as Imperial FILETIME and updates the raw number display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the raw FILETIME text field to reflect the current {@code value}.
     * Called whenever the visual pickers (spinners or calendar) change, so the
     * raw display stays in sync.
     */
    private void updateRawFields()
    {
        // Raw value
        rawValueText.setText( "" + value ); //$NON-NLS-1$
    }


    // ── R2-D2 Arms the Cross-Panel Listeners ─────────────────────────────────
    // R2-D2 wires up the signal cables between the three console panels so a
    // change to any one of them triggers an update in the others.
    // We attach modify/selection listeners to every widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all modify and selection listeners to the dialog's widgets.
     * We detach them temporarily during programmatic updates (to avoid infinite
     * feedback loops) and re-attach them afterwards — see {@link #removeListeners()}.
     */
    private void addListeners()
    {
        // Hours
        hoursSpinner.addModifyListener( hoursModifyListener );

        // Minutes
        minutesSpinner.addModifyListener( minutesModifyListener );

        // Seconds
        secondsSpinner.addModifyListener( secondsModifyListener );

        // Calendar
        dateCalendar.addSelectionListener( dateSelectionListener );

        // Raw value
        rawValueText.addModifyListener( rawValueModifyListener );
        rawValueText.addVerifyListener( rawValueVerifyListener );
    }


    // ── R2-D2 Disconnects the Signal Cables ──────────────────────────────────
    // While R2-D2 is updating multiple panels at once, he temporarily cuts the
    // signal cables so each update doesn't trigger the others in a feedback loop.
    // We remove all listeners before any programmatic widget update.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all modify and selection listeners from the dialog's widgets.
     * We do this before any programmatic widget update that shouldn't
     * trigger cross-panel synchronisation, then re-attach with {@link #addListeners()}.
     */
    private void removeListeners()
    {
        // Hours
        hoursSpinner.removeModifyListener( hoursModifyListener );

        // Minutes
        minutesSpinner.removeModifyListener( minutesModifyListener );

        // Seconds
        secondsSpinner.removeModifyListener( secondsModifyListener );

        // Calendar
        dateCalendar.removeSelectionListener( dateSelectionListener );

        // Raw value
        rawValueText.removeModifyListener( rawValueModifyListener );
        rawValueText.removeVerifyListener( rawValueVerifyListener );
    }


    // ── R2-D2 Reads the Visual Panels and Updates His Internal Clock ─────────
    // After the user changes a spinner or picks a calendar date, R2-D2 reads
    // all three time components, assembles them into a single moment, and
    // re-encodes it as an AD FILETIME for storage.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Rebuilds the internal {@code value} from the current state of the time
     * spinners and calendar widget.
     * Called whenever any visual picker changes, so the AD FILETIME stays
     * consistent with what the user sees.
     */
    private void updateValueFromNonRawFields()
    {
        // Getting the calendar
        Calendar calendar = getCalendarFromValue();

        // Time
        calendar.set( Calendar.HOUR_OF_DAY, hoursSpinner.getSelection() );
        calendar.set( Calendar.MINUTE, minutesSpinner.getSelection() );
        calendar.set( Calendar.SECOND, secondsSpinner.getSelection() );

        // Date
        calendar.set( Calendar.YEAR, dateCalendar.getYear() );
        calendar.set( Calendar.MONTH, dateCalendar.getMonth() );
        calendar.set( Calendar.DAY_OF_MONTH, dateCalendar.getDay() );

        value = ActiveDirectoryTimeUtils.convertToActiveDirectoryTime( calendar );
    }


    // ── R2-D2 Converts His Internal Tick Count to a Calendar ─────────────────
    // R2-D2 needs a Calendar object to read individual fields like year, month,
    // and hour — so he converts his raw AD FILETIME to a Calendar first.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link Calendar} corresponding to the current internal {@code value}.
     * Delegates to {@link ActiveDirectoryTimeUtils#convertToCalendar(long)}.
     *
     * @return  A {@code Calendar} matching the current AD FILETIME value.
     */
    private Calendar getCalendarFromValue()
    {
        return ActiveDirectoryTimeUtils.convertToCalendar( value );
    }


    // ── R2-D2 Transmits the Final Timestamp ──────────────────────────────────
    // The mission clock session is complete; R2-D2 transmits the confirmed
    // FILETIME tick count back to the Falcon's navigation computer.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the AD FILETIME value as confirmed by the user when they clicked OK.
     * This is the 64-bit integer that should be stored back into the LDAP attribute.
     *
     * <p>For example — the editor retrieves the confirmed timestamp:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       long adTime = dialog.getValue();
     *       // Store adTime as the new attribute value
     *   }
     * </pre>
     *
     * @return  The confirmed AD FILETIME value (100-ns ticks since 1601-01-01).
     */
    public long getValue()
    {
        return value;
    }
}
