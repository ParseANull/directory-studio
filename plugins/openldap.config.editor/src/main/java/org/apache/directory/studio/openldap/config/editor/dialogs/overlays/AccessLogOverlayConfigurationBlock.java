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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.FilterWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.apache.directory.studio.openldap.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.openldap.common.ui.model.LogOperationEnum;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.LogOperationsWidget;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.editor.dialogs.PurgeTimeSpan;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAccessLogConfig;


// Like the Imperial construction crews assembling the access-log surveillance
// module onto the second Death Star — wiring the database target pointer, the
// success-only filter switch, the log-operations checklist, the attribute
// roster for capturing old values, the LDAP filter for pre-image recording,
// and the log-purge age and interval spinners — we build the AccessLog overlay
// configuration block that records every operation the directory performs.
/**
 * This class implements the configuration block for the Access Log overlay.
 * We present a database DN picker, an "only log successful requests" checkbox,
 * a log-operations selection widget, an attributes table (for old-value capture),
 * a filter widget, and day/hour/minute/second spinners for purge age and purge
 * interval; we read/write all of these to and from the {@link OlcAccessLogConfig}
 * model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AccessLogOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcAccessLogConfig>
{
    /** The attributes list */
    private List<String> attributes = new ArrayList<>();

    // UI widgets
    private EntryWidget databaseEntryWidget;
    private Button onlyLogSuccessfulRequestsCheckbox;
    private LogOperationsWidget logOperationsWidget;
    private TableViewer attributesTableViewer;
    private Button addAttributeButton;
    private Button deleteAttributeButton;
    private FilterWidget filterWidget;
    private Spinner purgeAgeDaysSpinner;
    private Spinner purgeAgeHoursSpinner;
    private Spinner purgeAgeMinutesSpinner;
    private Spinner purgeAgeSecondsSpinner;
    private Spinner purgeIntervalDaysSpinner;
    private Spinner purgeIntervalHoursSpinner;
    private Spinner purgeIntervalMinutesSpinner;
    private Spinner purgeIntervalSecondsSpinner;

    // Listeners
    private ISelectionChangedListener attributesTableViewerSelectionChangedListener = event ->
            deleteAttributeButton.setEnabled( !attributesTableViewer.getSelection().isEmpty() );

            private SelectionListener addAttributeButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            AttributeDialog dialog = new AttributeDialog( addAttributeButton.getShell(), browserConnection );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String attribute = dialog.getAttribute();

                if ( !attributes.contains( attribute ) )
                {
                    attributes.add( attribute );
                    attributesTableViewer.refresh();
                    attributesTableViewer.setSelection( new StructuredSelection( attribute ) );
                }
            }
        }
    };
    private SelectionListener deleteAttributeButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                String selectedAttribute = ( String ) selection.getFirstElement();

                attributes.remove( selectedAttribute );
                attributesTableViewer.refresh();
            }
        }
    };


    // Like the construction crew initializing a fresh access-log module with
    // no database target, no operation filters, and no purge schedule — a blank
    // slate ready for the administrator to configure — we create the block with
    // a new empty OlcAccessLogConfig.
    /**
     * Creates a new AccessLogOverlayConfigurationBlock with a fresh, empty
     * {@link OlcAccessLogConfig} as the backing model.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param browserConnection the browser connection used for DN lookups and schema access
     */
    public AccessLogOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection )
    {
        super( dialog, browserConnection );
        setOverlay( new OlcAccessLogConfig() );
    }


    // Like the crew installing an already-configured access-log module that
    // already points at a database and has operation filters and purge schedule
    // pre-set, we accept an existing OlcAccessLogConfig and store it — defaulting
    // to a fresh one if null was passed.
    /**
     * Creates a new AccessLogOverlayConfigurationBlock backed by the given
     * {@link OlcAccessLogConfig}. If {@code overlay} is {@code null} we create
     * a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param browserConnection the browser connection used for DN lookups and schema access
     * @param overlay the existing access-log overlay config to edit, or {@code null}
     */
    public AccessLogOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection,
        OlcAccessLogConfig overlay )
    {
        super( dialog, browserConnection );

        if ( overlay == null )
        {
            setOverlay( new OlcAccessLogConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew building the access-log module's full control
    // panel — the database selector, the success-only toggle, the log-operations
    // group, the attributes group, the filter group, and the purge-schedule group —
    // we create all block content widgets here and wire their listeners.
    /**
     * Creates the block content area with a database DN entry widget, an
     * "Only log successful requests" checkbox, and four sub-groups: Log
     * Operations, Attributes, Filter, and Log Purge (with day/hour/minute/second
     * spinners for both age and interval).
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // Database
        BaseWidgetUtils.createLabel( composite, "Database:", 1 );
        databaseEntryWidget = new EntryWidget( getDialog().getBrowserConnection() );
        databaseEntryWidget.createWidget( composite );
        databaseEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Successful requests
        onlyLogSuccessfulRequestsCheckbox = BaseWidgetUtils.createCheckbox( composite, "Only log successful requests",
            3 );

        createLogOperationsGroup( composite );
        createAttributesGroup( composite );
        createFilterGroup( composite );
        createPurgeGroup( composite );
    }


    // Like the crew wiring in the log-operations selection panel — the
    // sub-board that lets the administrator tick which LDAP operation types
    // (add, abandon, session, etc.) the overlay should record — we build the
    // Log Operations group with a default selection of ADD, ABANDON, SESSION.
    /**
     * Creates the "Log Operations" group containing a {@link LogOperationsWidget}
     * pre-populated with ADD, ABANDON, and SESSION as the initial selection.
     *
     * @param parent the parent composite to add the group to
     */
    private void createLogOperationsGroup( Composite parent )
    {
        // Log Operations Group
        Group logOperationsGroup = BaseWidgetUtils.createGroup( parent, "Log Operations", 3 );

        // Log Operations Widget
        logOperationsWidget = new LogOperationsWidget();
        logOperationsWidget.create( logOperationsGroup );
        logOperationsWidget.getControl().setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );
        List<LogOperationEnum> logOperations = new ArrayList<>();
        logOperations.add( LogOperationEnum.ADD );
        logOperations.add( LogOperationEnum.ABANDON );
        logOperations.add( LogOperationEnum.SESSION );
        logOperationsWidget.setInput( logOperations );
    }


    // Like the crew installing the attribute-capture roster panel — the
    // sub-board that lists which attribute types the overlay should record
    // as "old values" before a modify or delete changes them — we build the
    // Attributes group with its table viewer and Add/Delete buttons.
    /**
     * Creates the "Attributes" group containing a {@link TableViewer} for
     * selecting which attributes' old values are captured, with Add and Delete
     * buttons wired to their respective listeners.
     *
     * @param parent the parent composite to add the group to
     */
    private void createAttributesGroup( Composite parent )
    {
        // Attributes Group
        Group attributesGroup = BaseWidgetUtils.createGroup( parent, "Attributes", 3 );
        GridLayout attributesCompositeGridLayout = new GridLayout( 2, false );
        attributesCompositeGridLayout.verticalSpacing = 0;
        attributesGroup.setLayout( attributesCompositeGridLayout );

        // Attributes TableViewer
        attributesTableViewer = new TableViewer( attributesGroup );
        GridData tableViewerGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        tableViewerGridData.heightHint = 20;
        tableViewerGridData.widthHint = 100;
        attributesTableViewer.getControl().setLayoutData( tableViewerGridData );
        attributesTableViewer.setContentProvider( new ArrayContentProvider() );
        attributesTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public Image getImage( Object element )
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_ATTRIBUTE );
            }
        } );
        attributesTableViewer.setInput( attributes );
        attributesTableViewer.addSelectionChangedListener( attributesTableViewerSelectionChangedListener );

        // Attribute Add Button
        addAttributeButton = BaseWidgetUtils.createButton( attributesGroup, "Add...", 1 );
        addAttributeButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        addAttributeButton.addSelectionListener( addAttributeButtonSelectionListener );

        // Attribute Delete Button
        deleteAttributeButton = BaseWidgetUtils.createButton( attributesGroup, "Delete", 1 );
        deleteAttributeButton.setEnabled( false );
        deleteAttributeButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        deleteAttributeButton.addSelectionListener( deleteAttributeButtonSelectionListener );
    }


    // Like the crew installing the entry-filter scanner — the sub-panel
    // that lets the operator specify an LDAP filter so the overlay only
    // captures old values for entries that match — we build the Filter group
    // with a FilterWidget wired to the dialog's browser connection.
    /**
     * Creates the "Filter" group containing a {@link FilterWidget} that lets
     * the administrator specify an LDAP filter to restrict which entries have
     * their old values recorded.
     *
     * @param parent the parent composite to add the group to
     */
    private void createFilterGroup( Composite parent )
    {
        // Filter Group
        Group filterGroup = BaseWidgetUtils.createGroup( parent, "Filter", 3 );
        GridLayout filterGroupGridLayout = new GridLayout( 2, false );
        filterGroupGridLayout.marginHeight = 0;
        filterGroupGridLayout.verticalSpacing = 0;
        filterGroup.setLayout( filterGroupGridLayout );

        filterWidget = new FilterWidget();
        filterWidget.setFilter( "" );
        filterWidget.setBrowserConnection( getDialog().getBrowserConnection() );
        filterWidget.createWidget( filterGroup );
    }


    // Like the crew setting up the log-purge control bank — the sub-panel
    // with age and interval spinners for days, hours, minutes, and seconds —
    // so the station's housekeeping routines know how old log entries can get
    // and how often to sweep them away, we build the Log Purge group.
    /**
     * Creates the "Log Purge" group containing two rows of four spinners each
     * (days, hours, minutes, seconds) for the purge age threshold and the purge
     * interval period, with column header labels beneath the spinners.
     *
     * @param parent the parent composite to add the group to
     */
    private void createPurgeGroup( Composite parent )
    {
        // Purge Group
        Group purgeGroup = BaseWidgetUtils.createGroup( parent, "Log Purge", 3 );
        GridLayout purgeCompositeGridLayout = new GridLayout( 5, false );
        purgeCompositeGridLayout.verticalSpacing = 0;
        purgeGroup.setLayout( purgeCompositeGridLayout );

        // Age Label
        BaseWidgetUtils.createLabel( purgeGroup, "Age:", 1 );

        // Age Days Spinner
        purgeAgeDaysSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeAgeDaysSpinner.setMinimum( 0 );
        purgeAgeDaysSpinner.setMaximum( 99999 );

        // Age Hours Spinner
        purgeAgeHoursSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeAgeHoursSpinner.setMinimum( 0 );
        purgeAgeHoursSpinner.setMaximum( 23 );

        // Age Minutes Spinner
        purgeAgeMinutesSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeAgeMinutesSpinner.setMinimum( 0 );
        purgeAgeMinutesSpinner.setMaximum( 59 );

        // Age Seconds Spinner
        purgeAgeSecondsSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeAgeSecondsSpinner.setMinimum( 0 );
        purgeAgeSecondsSpinner.setMaximum( 59 );

        // Interval Label
        BaseWidgetUtils.createLabel( purgeGroup, "Interval:", 1 );

        // Interval Days Spinner
        purgeIntervalDaysSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeIntervalDaysSpinner.setMinimum( 0 );
        purgeIntervalDaysSpinner.setMaximum( 99999 );

        // Interval Hours Spinner
        purgeIntervalHoursSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeIntervalHoursSpinner.setMinimum( 0 );
        purgeIntervalHoursSpinner.setMaximum( 23 );

        // Interval Minutes Spinner
        purgeIntervalMinutesSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeIntervalMinutesSpinner.setMinimum( 0 );
        purgeIntervalMinutesSpinner.setMaximum( 59 );

        // Interval Seconds Spinner
        purgeIntervalSecondsSpinner = new Spinner( purgeGroup, SWT.BORDER );
        purgeIntervalSecondsSpinner.setMinimum( 0 );
        purgeIntervalSecondsSpinner.setMaximum( 59 );

        // Days Hours Minutes Seconds Labels
        BaseWidgetUtils.createSpacer( purgeGroup, 1 );
        BaseWidgetUtils.createLabel( purgeGroup, "Days", 1 );
        BaseWidgetUtils.createLabel( purgeGroup, "Hours", 1 );
        BaseWidgetUtils.createLabel( purgeGroup, "Minutes", 1 );
        BaseWidgetUtils.createLabel( purgeGroup, "Seconds", 1 );
    }


    // Like the crew reading the access-log module's current settings out of
    // the station's configuration record and populating every control on the
    // panel — the database DN, the success-only toggle, the operation checkboxes,
    // the attributes roster, the filter string, and the purge age and interval
    // spinners — we push each overlay value into its corresponding widget.
    /**
     * Refreshes all block widgets from the current {@link OlcAccessLogConfig},
     * populating the database DN, success-only checkbox, log-operations widget,
     * attributes table, filter widget, and purge age/interval spinners from
     * the overlay. Fields with absent values are left at their defaults.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            // Database
            databaseEntryWidget.setInput( overlay.getOlcAccessLogDB() );

            //
            // Only log successful requests
            //
            Boolean onlyLogSuccessfulRequests = overlay.getOlcAccessLogSuccess();

            if ( onlyLogSuccessfulRequests != null )
            {
                onlyLogSuccessfulRequestsCheckbox.setSelection( onlyLogSuccessfulRequests.booleanValue() );
            }
            else
            {
                onlyLogSuccessfulRequestsCheckbox.setSelection( false );
            }

            //
            // Log operations
            //
            List<String> logOperationsValues = overlay.getOlcAccessLogOps();

            if ( ( logOperationsValues != null ) && !logOperationsValues.isEmpty() )
            {
                logOperationsWidget.setInput( getAccessLogOperations( logOperationsValues ) );
            }
            else
            {
                logOperationsWidget.setInput( null );
            }

            //
            // Attributes
            //
            List<String> attributeValues = overlay.getOlcAccessLogOldAttr();

            if ( ( attributeValues != null ) && !attributeValues.isEmpty() )
            {
                for ( String attribute : attributeValues )
                {
                    attributes.add( attribute );
                }
            }

            attributesTableViewer.refresh();

            //
            // Filter
            //
            String filter = overlay.getOlcAccessLogOld();

            if ( filter != null )
            {
                filterWidget.setFilter( filter );
            }

            //
            // Purge
            //
            String accessLogPurge = overlay.getOlcAccessLogPurge();

            if ( !Strings.isEmpty( accessLogPurge ) )
            {
                // Splitting age and interval purge time spans
                String[] accessLogPurgeValues = accessLogPurge.split( " " );

                // Checking if we got the appropriate number of members
                if ( accessLogPurgeValues.length == 2 )
                {
                    try
                    {
                        // Purge age time span
                        PurgeTimeSpan purgeAgeTimeSpan = new PurgeTimeSpan( accessLogPurgeValues[0] );
                        purgeAgeDaysSpinner.setSelection( purgeAgeTimeSpan.getDays() );
                        purgeAgeHoursSpinner.setSelection( purgeAgeTimeSpan.getHours() );
                        purgeAgeMinutesSpinner.setSelection( purgeAgeTimeSpan.getMinutes() );
                        purgeAgeSecondsSpinner.setSelection( purgeAgeTimeSpan.getSeconds() );

                        // Purge interval time span
                        PurgeTimeSpan purgeIntervalTimeSpan = new PurgeTimeSpan( accessLogPurgeValues[1] );
                        purgeIntervalDaysSpinner.setSelection( purgeIntervalTimeSpan.getDays() );
                        purgeIntervalHoursSpinner.setSelection( purgeIntervalTimeSpan.getHours() );
                        purgeIntervalMinutesSpinner.setSelection( purgeIntervalTimeSpan.getMinutes() );
                        purgeIntervalSecondsSpinner.setSelection( purgeIntervalTimeSpan.getSeconds() );
                    }
                    catch ( ParseException e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else
                {
                    // We didn't have 2 members in the string
                    // TODO error
                }
            }
        }
    }


    // Like the crew writing all the updated access-log settings back into
    // the station's configuration record so they take effect — saving the
    // database DN, the success flag, the operation list, the attribute roster,
    // the filter string, and the composed purge time-span string — we persist
    // every widget value into the OlcAccessLogConfig model.
    /**
     * Saves the current widget values back into the {@link OlcAccessLogConfig},
     * writing the database DN, success-only flag, log operations list, attributes
     * list, filter string, and composed purge value. Also saves dialog settings
     * for the database and filter widgets.
     */
    public void save()
    {
        if ( overlay != null )
        {
            // Database
            overlay.setOlcAccessLogDB( databaseEntryWidget.getDn() );

            // Only log successful requests
            overlay.setOlcAccessLogSuccess( onlyLogSuccessfulRequestsCheckbox.getSelection() );

            // Log operations
            overlay.setOlcAccessLogOps( getAccessLogOperationsValues() );

            // Attributes
            overlay.setOlcAccessLogOldAttr( attributes );

            // Filter
            overlay.setOlcAccessLogOld( filterWidget.getFilter() );

            // Purge
            overlay.setOlcAccessLogPurge( getPurgeValue() );
        }

        // Saving dialog settings
        databaseEntryWidget.saveDialogSettings();
        filterWidget.saveDialogSettings();
    }


    // Like the crew translating the log-operations selection from the widget's
    // internal enum representation into the string tokens that the OlcAccessLogConfig
    // model expects — one operation name string per selected operation — we build
    // the list of string values to write back to the overlay.
    /**
     * Converts the currently selected {@link LogOperationEnum} values from the
     * log-operations widget into a list of their string representations for
     * storage in the {@link OlcAccessLogConfig}.
     *
     * @return a list of log-operation name strings, one per selected operation
     */
    private List<String> getAccessLogOperationsValues()
    {
        List<String> accessLogOperations = new ArrayList<>();
        List<LogOperationEnum> logOperations = logOperationsWidget.getSelectedOperationsList();

        for ( LogOperationEnum logOperation : logOperations )
        {
            // Converting log operation to string
            accessLogOperations.add( logOperation.toString() );
        }

        return accessLogOperations;
    }


    // Like the crew translating the string tokens stored in the overlay config
    // back into the enum values that the log-operations widget understands so
    // it can display them with the correct checkboxes ticked, we parse each
    // string and look up the corresponding LogOperationEnum.
    /**
     * Converts a list of log-operation name strings (as stored in the
     * {@link OlcAccessLogConfig}) into a list of {@link LogOperationEnum} values
     * for display in the log-operations widget. Unknown strings are silently skipped.
     *
     * @param logOperationsValues the raw string values from the overlay config
     * @return a list of recognized {@link LogOperationEnum} values
     */
    private List<LogOperationEnum> getAccessLogOperations( List<String> logOperationsValues )
    {
        List<LogOperationEnum> logOperations = new ArrayList<>();

        for ( String logOperationValue : logOperationsValues )
        {
            // Converting log operation from a string
            LogOperationEnum logOperation = LogOperationEnum.fromString( logOperationValue );

            if ( logOperation != null )
            {
                logOperations.add( logOperation );
            }
        }

        return logOperations;
    }


    // Like the crew composing the purge time-span string that the overlay
    // config expects by concatenating the age span and the interval span
    // with a space between them, we assemble the complete purge value.
    /**
     * Builds the complete purge configuration string by concatenating the
     * age {@link PurgeTimeSpan} and the interval {@link PurgeTimeSpan}
     * separated by a space, as expected by the {@code olcAccessLogPurge} attribute.
     *
     * @return the composed purge string in the form "age interval"
     */
    private String getPurgeValue()
    {
        return getPurgeAgeTimeSpan().toString() + " " + getPurgeIntervalTimeSpan().toString();
    }


    // Like the crew reading the age spinners and packaging their values into
    // a PurgeTimeSpan object so the purge string composer can format them
    // into the standard D+HH:MM:SS notation the overlay expects, we build
    // the age time span from the four age spinner values.
    /**
     * Reads the purge-age day, hour, minute, and second spinner values and
     * packages them into a {@link PurgeTimeSpan} object.
     *
     * @return a {@link PurgeTimeSpan} representing the configured purge age threshold
     */
    private PurgeTimeSpan getPurgeAgeTimeSpan()
    {
        PurgeTimeSpan purgeAgeTimeSpan = new PurgeTimeSpan();

        purgeAgeTimeSpan.setDays( purgeAgeDaysSpinner.getSelection() );
        purgeAgeTimeSpan.setHours( purgeAgeHoursSpinner.getSelection() );
        purgeAgeTimeSpan.setMinutes( purgeAgeMinutesSpinner.getSelection() );
        purgeAgeTimeSpan.setSeconds( purgeAgeSecondsSpinner.getSelection() );

        return purgeAgeTimeSpan;
    }


    // Like the crew reading the interval spinners and packaging their values
    // into a PurgeTimeSpan so the composer can format them alongside the age
    // span, we build the purge interval time span from the four interval
    // spinner values.
    /**
     * Reads the purge-interval day, hour, minute, and second spinner values
     * and packages them into a {@link PurgeTimeSpan} object.
     *
     * @return a {@link PurgeTimeSpan} representing the configured purge sweep interval
     */
    private PurgeTimeSpan getPurgeIntervalTimeSpan()
    {
        PurgeTimeSpan purgeInternalTimeSpan = new PurgeTimeSpan();

        purgeInternalTimeSpan.setDays( purgeIntervalDaysSpinner.getSelection() );
        purgeInternalTimeSpan.setHours( purgeIntervalHoursSpinner.getSelection() );
        purgeInternalTimeSpan.setMinutes( purgeIntervalMinutesSpinner.getSelection() );
        purgeInternalTimeSpan.setSeconds( purgeIntervalSecondsSpinner.getSelection() );

        return purgeInternalTimeSpan;
    }
}
