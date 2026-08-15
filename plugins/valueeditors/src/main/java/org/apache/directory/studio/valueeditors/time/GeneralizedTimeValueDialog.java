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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


// ── CLASS: GeneralizedTimeValueDialog — MON MOTHMA'S MISSION CLOCK CONSOLE ────
// At Rebel Alliance headquarters, Mon Mothma's mission clock console lets a
// coordinator set the precise timestamp for a mission briefing.  The console
// has four linked panels: time spinners (HH:MM:SS), a calendar for the date,
// a timezone drop-down (UTC offsets plus continent/city names from Java), and a
// raw GeneralizedTime field that shows the LDAP-format result in real time.
// Edit any panel and the others update automatically — bidirectionally.
// A "Discard Fraction" checkbox strips milliseconds before saving.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for editing an LDAP GeneralizedTime attribute value
 * (syntax OID 1.3.6.1.4.1.1466.115.121.1.24).
 * GeneralizedTime is the standard LDAP timestamp format, e.g.
 * {@code "20231215120000Z"}.
 * The dialog provides four linked panels: time spinners (hours, minutes, seconds),
 * a calendar date picker, a timezone combo (UTC offsets and continent/city IDs),
 * and a raw text field showing the GeneralizedTime string.
 * Editing any panel updates the others in real time.
 * The "Discard Fraction" checkbox removes the milliseconds component before
 * committing; this is remembered across sessions.
 * Think of this as Mon Mothma's mission clock console — every panel is kept in
 * sync and the raw format is always visible.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GeneralizedTimeValueDialog extends Dialog
{
    /** The value */
    private GeneralizedTime value;

    /** The list, containing all time zones, bound to the combo viewer */
    private ArrayList<TimeZone> allTimezonesList = new ArrayList<TimeZone>();

    /** The UTC times zones map */
    private Map<Integer, TimeZone> utcTimezonesMap = new HashMap<Integer, TimeZone>();

    //
    // UI Fields
    //

    // Time
    private Spinner hoursSpinner;
    private Spinner minutesSpinner;
    private Spinner secondsSpinner;

    // Date
    private DateTime dateCalendar;

    // Time zone
    private ComboViewer timezoneComboViewer;

    // Raw value
    private Text rawValueText;

    // Raw validator
    private Label rawValueValidatorImage;

    // Discard fraction checkbox
    private Button discardFractionCheckbox;

    /** The OK button of the dialog */
    private Button okButton;

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
     * The selection changed listener of the time zone combo.
     */
    private ISelectionChangedListener timezoneSelectionChangedListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
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
                value = new GeneralizedTime( rawValueText.getText() );

                removeListeners();
                updateNonRawFields();
                addListeners();

                validateRawValue( true );
            }
            catch ( ParseException e1 )
            {
                validateRawValue( false );

                return;
            }
        }
    };

    private SelectionListener discardFractionCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            removeListeners();
            updateRawFields();
            addListeners();
        }
    };


    // ── Mon Mothma Opens the Mission Clock Console ────────────────────────────
    // The console is initialised with the mission's existing timestamp.
    // If none is provided (brand-new mission slot), we default to the current
    // date/time so the coordinator doesn't start with an empty field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new GeneralizedTimeValueDialog.
     * If {@code value} is {@code null}, the console starts with the current
     * date and time so the coordinator always has a valid starting point.
     *
     * <p>For example — the console opens for a new timestamp:</p>
     * <pre>
     *   GeneralizedTimeValueDialog dialog = new GeneralizedTimeValueDialog(shell, null);
     *   dialog.open();  // opens pre-filled with "now"
     * </pre>
     *
     * @param parentShell  The SWT shell that owns this dialog.
     * @param value        The initial GeneralizedTime value, or {@code null} for now.
     */
    public GeneralizedTimeValueDialog( Shell parentShell, GeneralizedTime value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.value = value;

        // If the initial value is null, we take the current date/time
        if ( this.value == null )
        {
            this.value = new GeneralizedTime( Calendar.getInstance() );
        }
    }


    // ── Mon Mothma Labels the Console ─────────────────────────────────────────
    // The console is labelled "Date and Time Editor" and receives the Rebel
    // mission clock icon.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and icon.
     *
     * @param shell  The SWT Shell to configure.
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "GeneralizedTimeValueDialog.DateAndTimeEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_GENERALIZEDTIMEEDITOR ) );
    }


    // ── Mon Mothma Places the Confirmation Buttons ────────────────────────────
    // The console places OK and Cancel.  OK is disabled while the raw value is
    // not parseable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons.
     * OK starts enabled; it is disabled by {@link #validateRawValue(boolean)} if
     * the raw value becomes unparseable during editing.
     *
     * @param parent  The composite hosting the button bar.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── Mon Mothma Commits the Final Timestamp ────────────────────────────────
    // When OK is pressed, if the "Discard Fraction" checkbox is ticked we strip
    // the milliseconds from the value before persisting, and we save the user's
    // "discard fraction" preference for next time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when OK is pressed.
     * If "Discard Fraction" is checked, the millisecond component is zeroed out
     * before saving.  Persists the "discard fraction" preference to dialog settings.
     */
    protected void okPressed()
    {
        // Checking if we need to discard the fraction
        if ( discardFractionCheckbox.getSelection() )
        {
            // Removing the fraction from the value
            Calendar calendar = value.getCalendar();
            calendar.set( Calendar.MILLISECOND, 0 );

            value = new GeneralizedTime( calendar );
        }

        // Saving the dialog settings
        ValueEditorsActivator.getDefault().getDialogSettings()
            .put( ValueEditorsConstants.DIALOGSETTING_KEY_DATE_EDITOR_DISCARD_FRACTION,
                discardFractionCheckbox.getSelection() );

        super.okPressed();
    }


    // ── Mon Mothma Builds the Four-Panel Console ──────────────────────────────
    // The main composite holds four panels stacked in a two-column grid:
    // Time (HH:MM:SS spinners), Date (calendar widget), Timezone (combo),
    // and Raw Value (text + validator image + "Discard Fraction" checkbox).
    // All four panels are wired together with bidirectional listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content — four linked panels for time, date, timezone,
     * and raw GeneralizedTime value.
     * Returns the top-level composite.
     *
     * <p>For example — the four-panel console layout:</p>
     * <pre>
     *   Time:     [ 12 ] : [ 00 ] : [ 00 ]
     *   Date:     [  calendar widget  ]
     *   Timezone: [ UTC ▼             ]
     *   ─────────────────────────────────
     *   Raw:      [ 20231215120000Z ✓ ]
     *             [ ] Discard fraction
     * </pre>
     *
     * @param parent  The parent composite provided by JFace.
     * @return        The top-level composite containing all panels.
     */
    protected Control createDialogArea( Composite parent )
    {
        // Main composites
        Composite composite = ( Composite ) super.createDialogArea( parent );
        Composite dualComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Creating dialog areas
        createTimeDialogArea( dualComposite );
        createDateDialogArea( dualComposite );
        createTimeZoneDialogArea( dualComposite );
        createRawValueDialogArea( dualComposite );

        // Getting the dialog settings
        discardFractionCheckbox.setSelection( ValueEditorsActivator.getDefault().getDialogSettings()
            .getBoolean( ValueEditorsConstants.DIALOGSETTING_KEY_DATE_EDITOR_DISCARD_FRACTION ) );

        // Initializing with initial value
        initWithInitialValue();

        // Adding listeners
        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // ── Mon Mothma Builds the Time Spinner Panel ──────────────────────────────
    // Three spinners for HH, MM, and SS — standard 24-hour clock format.
    // Spinners are constrained: hours 0–23, minutes 0–59, seconds 0–59.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Time" panel — three spinners for hours (0–23), minutes
     * (0–59), and seconds (0–59), separated by colon labels.
     *
     * @param parent  The parent composite for the time panel.
     */
    private void createTimeDialogArea( Composite parent )
    {
        // Label
        Label timeLabel = new Label( parent, SWT.NONE );
        timeLabel.setText( Messages.getString( "GeneralizedTimeValueDialog.Time" ) ); //$NON-NLS-1$

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


    // ── Mon Mothma Builds the Date Calendar Panel ─────────────────────────────
    // A native SWT calendar widget lets the coordinator click a date.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Date" panel — a native SWT {@link DateTime} calendar widget
     * for picking the year, month, and day.
     *
     * @param parent  The parent composite for the date panel.
     */
    private void createDateDialogArea( Composite parent )
    {
        // Label
        Label dateLabel = BaseWidgetUtils.createLabel( parent,
            Messages.getString( "GeneralizedTimeValueDialog.Date" ), 1 ); //$NON-NLS-1$
        dateLabel.setLayoutData( new GridData( SWT.NONE, SWT.TOP, false, false ) );

        Composite rightComposite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Calendar
        dateCalendar = new DateTime( rightComposite, SWT.CALENDAR | SWT.BORDER );
        dateCalendar.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── Mon Mothma Builds the Timezone Panel ──────────────────────────────────
    // A combo viewer lists all UTC-offset pseudo-zones first (UTC-12 … UTC+14),
    // followed by all continent/city zones (Africa/…, America/…, etc.) sorted
    // by ID.  The JVM's TimeZone.getAvailableIDs() provides the continent list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Timezone" panel — a JFace combo viewer listing UTC-offset
     * zones first (UTC-12 through UTC+14) and then continent/city timezone IDs.
     * The timezone ID string is used as the display label.
     *
     * @param parent  The parent composite for the timezone panel.
     */
    private void createTimeZoneDialogArea( Composite parent )
    {
        // Label
        BaseWidgetUtils.createLabel( parent, Messages.getString( "GeneralizedTimeValueDialog.Timezone" ), 1 ); //$NON-NLS-1$

        // Combo viewer
        timezoneComboViewer = new ComboViewer( parent );
        GridData timezoneGridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
        timezoneGridData.widthHint = 50;
        timezoneComboViewer.getCombo().setLayoutData( timezoneGridData );

        // Adding ContentProvider, LabelProvider
        timezoneComboViewer.setContentProvider( new ArrayContentProvider() );
        timezoneComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                return ( ( TimeZone ) element ).getID();
            }
        } );

        // Initializing the time zones list and map
        initAllTimezones();

        timezoneComboViewer.setInput( allTimezonesList );
    }


    // ── Mon Mothma Initialises the Full Timezone Catalogue ────────────────────
    // All UTC-offset pseudo-zones are added first (in offset order), then all
    // continent/city zones from Java's runtime are appended, sorted by ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates {@link #allTimezonesList} and {@link #utcTimezonesMap} by calling
     * both {@link #initUtcTimezones()} and {@link #initContinentsAndCitiesTimezones()}.
     */
    private void initAllTimezones()
    {
        initUtcTimezones();
        initContinentsAndCitiesTimezones();
    }


    // ── Mon Mothma Registers All UTC-Offset Zones ─────────────────────────────
    // Each UTC offset from -12 to +14 (including half-hour and quarter-hour
    // offsets) is registered as a SimpleTimeZone and added to the combo list.
    // UTC+0 maps to the standard "UTC" zone.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers all standard UTC-offset time zones
     * (UTC-12 through UTC+14, including half-hour variants like UTC+5:30).
     * UTC+0 is mapped to the standard {@code "UTC"} zone; all others use
     * {@link SimpleTimeZone} with the appropriate raw offset.
     */
    private void initUtcTimezones()
    {
        addUtcTimezone( "UTC-12", -1 * ( 12 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-11", -1 * ( 11 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-10", -1 * ( 10 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-9:30", -1 * ( ( ( 9 * 60 ) + 30 ) * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-9", -1 * ( 9 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-8", -1 * ( 8 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-7", -1 * ( 7 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-6", -1 * ( 6 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-5", -1 * ( 5 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-4:30", -1 * ( ( ( 4 * 60 ) + 30 ) * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-4", -1 * ( 4 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-3:30", -1 * ( ( ( 3 * 60 ) + 30 ) * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-3", -1 * ( 3 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-2", -1 * ( 2 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC-1", -1 * ( 1 * 60 * 60 * 1000 ) ); //$NON-NLS-1$
        addUtcTimezone( "UTC", 0 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+1", 1 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+2", 2 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+3", 3 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+3:30", ( ( 3 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+4", 4 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+4:30", ( ( 4 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+5", 5 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+5:30", ( ( 5 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+5:45", ( ( 5 * 60 ) + 45 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+6", 6 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+6:30", ( ( 6 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+7", 7 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+8", 8 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+8:45", ( ( 8 * 60 ) + 45 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+9", 9 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+9:30", ( ( 9 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+10", 10 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+10:30", ( ( 10 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+11", 11 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+11:30", ( ( 11 * 60 ) + 30 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+12", 12 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+12:45", ( ( 12 * 60 ) + 45 ) * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+13", 13 * 60 * 60 * 1000 ); //$NON-NLS-1$
        addUtcTimezone( "UTC+14", 14 * 60 * 60 * 1000 ); //$NON-NLS-1$
    }


    // ── Mon Mothma Adds a UTC-Offset Zone to the Catalogue ────────────────────
    // Creates the TimeZone object for the given UTC-offset ID and raw offset
    // (in milliseconds), adds it to the full list, and stores it in the lookup
    // map so updateNonRawFields() can find the right entry by raw offset.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a UTC-offset {@link TimeZone} and registers it in both
     * {@link #allTimezonesList} and {@link #utcTimezonesMap}.
     * A raw offset of {@code 0} uses the standard {@code "UTC"} zone; all
     * other offsets use a {@link SimpleTimeZone}.
     *
     * @param id         The display ID for the timezone (e.g. {@code "UTC+5:30"}).
     * @param rawOffset  The raw UTC offset in milliseconds.
     */
    private void addUtcTimezone( String id, int rawOffset )
    {
        TimeZone tz = rawOffset == 0 ? TimeZone.getTimeZone( "UTC" ) : new SimpleTimeZone( rawOffset, id ); //$NON-NLS-1$

        allTimezonesList.add( tz );
        utcTimezonesMap.put( rawOffset, tz );
    }


    // ── Mon Mothma Adds Continent/City Timezones to the Catalogue ────────────
    // We query Java's runtime for all available timezone IDs and filter to
    // those that start with a continent prefix (Africa, America, Asia, etc.).
    // They are sorted alphabetically by ID before being appended to the full list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates {@link #allTimezonesList} with all continent/city time zone IDs
     * available from the JVM (Africa/*, America/*, Asia/*, Atlantic/*,
     * Australia/*, Europe/*, Indian/*, Pacific/*), sorted alphabetically by ID.
     */
    private void initContinentsAndCitiesTimezones()
    {
        List<TimeZone> continentsAndCitiesTimezonesList = new ArrayList<TimeZone>();

        // Getting all e time zones from the following continents :
        //     * Africa
        //     * America
        //     * Asia
        //     * Atlantic
        //     * Australia
        //     * Europe
        //     * Indian
        //     * Pacific
        for ( String timezoneId : TimeZone.getAvailableIDs() )
        {
            if ( timezoneId.matches( "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*" ) ) //$NON-NLS-1$
            {
                continentsAndCitiesTimezonesList.add( TimeZone.getTimeZone( timezoneId ) );
            }
        }

        // Sorting the list by ID
        Collections.sort( continentsAndCitiesTimezonesList, new Comparator<TimeZone>()
        {
            public int compare( final TimeZone a, final TimeZone b )
            {
                return a.getID().compareTo( b.getID() );
            }
        } );

        allTimezonesList.addAll( continentsAndCitiesTimezonesList );
    }


    // ── Mon Mothma Builds the Raw Value Panel ─────────────────────────────────
    // The raw-value panel shows a separator, a text field displaying the
    // GeneralizedTime string, a validator icon (green check or red cross),
    // and the "Discard Fraction" checkbox.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Raw Value" panel — a separator, a text field showing the
     * raw GeneralizedTime string, a validator image indicating whether the
     * string is parseable, and a "Discard Fraction" checkbox.
     *
     * @param parent  The parent composite for the raw-value panel.
     */
    private void createRawValueDialogArea( Composite parent )
    {
        // Separator
        Label separatorLabel = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        // Label
        BaseWidgetUtils.createLabel( parent, Messages.getString( "GeneralizedTimeValueDialog.RawValue" ), 1 ); //$NON-NLS-1$

        // Raw composite
        Composite rawValueComposite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // Text
        rawValueText = BaseWidgetUtils.createText( rawValueComposite, "", 1 ); //$NON-NLS-1$

        // Validator image
        rawValueValidatorImage = new Label( rawValueComposite, SWT.NONE );

        // Discard fraction checkbox
        discardFractionCheckbox = BaseWidgetUtils.createCheckbox( parent,
            Messages.getString( "GeneralizedTimeValueDialog.DiscardFraction" ), 2 );

        validateRawValue( true );
    }


    // ── Mon Mothma Populates the Console With the Initial Value ───────────────
    // After all panels are built, we push the initial GeneralizedTime value
    // into both the structured panels (spinners, calendar, timezone) and the
    // raw text field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the initial {@link GeneralizedTime} value into all UI panels.
     * Called once after the dialog area is fully constructed.
     */
    private void initWithInitialValue()
    {
        updateNonRawFields();
        updateRawFields();
    }


    // ── Mon Mothma Syncs the Structured Panels From the Model ─────────────────
    // Reads the current GeneralizedTime value and pushes it into the spinner,
    // calendar, and timezone combo.  The timezone is matched by raw UTC offset;
    // if no matching UTC pseudo-zone exists, the combo selection is cleared.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the time spinners, date calendar, and timezone combo to reflect
     * the current {@link #value}.
     * The timezone is matched against {@link #utcTimezonesMap} by raw offset;
     * the combo is cleared if no match is found (for continent/city zones whose
     * DST offset differs from their raw offset).
     */
    private void updateNonRawFields()
    {
        Calendar calendar = value.getCalendar();

        // Time
        hoursSpinner.setSelection( calendar.get( Calendar.HOUR_OF_DAY ) );
        minutesSpinner.setSelection( calendar.get( Calendar.MINUTE ) );
        secondsSpinner.setSelection( calendar.get( Calendar.SECOND ) );

        // Date
        dateCalendar.setDate( calendar.get( Calendar.YEAR ), calendar.get( Calendar.MONTH ), calendar
            .get( Calendar.DAY_OF_MONTH ) );

        // Time zone
        TimeZone timezone = utcTimezonesMap.get( new Integer( calendar.getTimeZone().getRawOffset() ) );
        if ( timezone == null )
        {
            timezoneComboViewer.setSelection( null );
        }
        else
        {
            timezoneComboViewer.setSelection( new StructuredSelection( timezone ) );
        }
    }


    // ── Mon Mothma Syncs the Raw Text Field From the Model ────────────────────
    // Formats the current value as a GeneralizedTime string (with or without the
    // fractional-seconds component, depending on the checkbox) and writes it into
    // the raw text field.  Then validates the field to update the validator icon.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the raw text field to reflect the current {@link #value}.
     * If "Discard Fraction" is checked, the sub-second precision is omitted.
     * Always calls {@link #validateRawValue(boolean) validateRawValue(true)}
     * afterwards to ensure the validator icon shows green.
     */
    private void updateRawFields()
    {
        // Raw value
        if ( discardFractionCheckbox.getSelection() )
        {
            rawValueText.setText( value.toGeneralizedTimeWithoutFraction() );
        }
        else
        {
            rawValueText.setText( value.toGeneralizedTime() );
        }

        validateRawValue( true );
    }


    // ── Mon Mothma Validates the Raw String and Controls OK ───────────────────
    // A green-check icon indicates the raw string is parseable; a red-cross
    // indicates it isn't.  OK is enabled only when the string is valid.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the validator icon and enables or disables the OK button.
     * Shows a green check icon when {@code bool} is {@code true} (valid);
     * a red cross icon when {@code false} (invalid).
     *
     * @param bool  {@code true} if the raw value is parseable; {@code false} otherwise.
     */
    private void validateRawValue( boolean bool )
    {
        if ( bool )
        {
            rawValueValidatorImage.setImage( ValueEditorsActivator.getDefault().getImage(
                ValueEditorsConstants.IMG_TEXTFIELD_OK ) );
        }
        else
        {
            rawValueValidatorImage.setImage( ValueEditorsActivator.getDefault().getImage(
                ValueEditorsConstants.IMG_TEXTFIELD_ERROR ) );
        }

        if ( okButton != null && !okButton.isDisposed() )
        {
            okButton.setEnabled( bool );
        }
    }


    // ── Mon Mothma Attaches the Bidirectional Sensors ─────────────────────────
    // Listeners on each panel fire updateValueFromNonRawFields() (to push panel
    // changes into the model) then updateRawFields() (to reflect the model in the
    // raw text).  The raw-text listener fires updateNonRawFields() instead so
    // typed changes propagate back to the structured panels.
    // Listeners are removed before any programmatic update and re-added after to
    // prevent feedback loops.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches listeners to all interactive widgets.
     * Changes to spinners, the calendar, or the timezone combo update the model
     * and then the raw text field.
     * Changes to the raw text field parse a new {@link GeneralizedTime} and update
     * the structured panels.
     * Listeners are temporarily removed during programmatic updates to prevent
     * infinite update loops.
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

        // Time zone
        timezoneComboViewer.addSelectionChangedListener( timezoneSelectionChangedListener );

        // Raw value
        rawValueText.addModifyListener( rawValueModifyListener );

        // Discard fraction checkbox
        discardFractionCheckbox.addSelectionListener( discardFractionCheckboxSelectionListener );
    }


    // ── Mon Mothma Detaches the Sensors During Programmatic Updates ───────────
    // Before we programmatically update any panel (to prevent the listener on
    // that panel from firing back and causing an update loop), we remove all
    // listeners, perform the update, then call addListeners() again.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all listeners from all interactive widgets.
     * Called before any programmatic widget update to prevent listener feedback
     * loops; always followed by a matching call to {@link #addListeners()}.
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

        // Time zone
        timezoneComboViewer.removeSelectionChangedListener( timezoneSelectionChangedListener );

        // Raw value
        rawValueText.removeModifyListener( rawValueModifyListener );

        // Discard fraction checkbox
        discardFractionCheckbox.removeSelectionListener( discardFractionCheckboxSelectionListener );
    }


    // ── Mon Mothma Reads the Structured Panels Back Into the Model ────────────
    // Reads the current spinner values, calendar date, and timezone selection
    // and applies them to the Calendar object inside the current GeneralizedTime
    // value.  We retain the existing GeneralizedTime object (preserving any
    // sub-second precision) and only update its Calendar fields.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current state of the spinners, calendar, and timezone combo and
     * applies the changes to the underlying {@link GeneralizedTime} model by
     * mutating its embedded {@link Calendar}.
     * We update the calendar in place rather than creating a new
     * {@link GeneralizedTime} so that any sub-second precision in the original
     * value is preserved.
     */
    private void updateValueFromNonRawFields()
    {
        // Retain the format of the GeneralizedTime value
        // by only updating its calendar object.
        Calendar calendar = value.getCalendar();

        // Time
        calendar.set( Calendar.HOUR_OF_DAY, hoursSpinner.getSelection() );
        calendar.set( Calendar.MINUTE, minutesSpinner.getSelection() );
        calendar.set( Calendar.SECOND, secondsSpinner.getSelection() );

        // Date
        calendar.set( Calendar.YEAR, dateCalendar.getYear() );
        calendar.set( Calendar.MONTH, dateCalendar.getMonth() );
        calendar.set( Calendar.DAY_OF_MONTH, dateCalendar.getDay() );

        // Time zone
        StructuredSelection selection = ( StructuredSelection ) timezoneComboViewer.getSelection();
        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            calendar.setTimeZone( ( TimeZone ) selection.getFirstElement() );
        }
    }


    // ── Mon Mothma Hands the Final Timestamp to the Caller ───────────────────
    // After OK is pressed, callers retrieve the committed GeneralizedTime here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link GeneralizedTime} value after the dialog has been
     * closed with OK.
     * If "Discard Fraction" was checked, the millisecond component has been
     * zeroed out before this method is called.
     *
     * <p>For example — the caller retrieves the committed timestamp:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       GeneralizedTime gt = dialog.getGeneralizedTime();
     *       // write gt.toGeneralizedTime() to the attribute
     *   }
     * </pre>
     *
     * @return  The committed {@link GeneralizedTime} value.
     */
    public GeneralizedTime getGeneralizedTime()
    {
        return value;
    }
}
