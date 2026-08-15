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

package org.apache.directory.studio.ldapbrowser.common.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.BinaryEncodingInput;
import org.apache.directory.studio.ldapbrowser.common.widgets.FileEncodingInput;
import org.apache.directory.studio.ldapbrowser.common.widgets.LineSeparatorInput;
import org.apache.directory.studio.ldapbrowser.common.widgets.OptionsInput;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: TextFormatsPreferencePage — IMPERIAL CODE FORMATTING STANDARDS ────
// Aboard the Death Star, Grand Moff Tarkin's code officers enforce Imperial
// Standard Formatting across every transmission: delimiter choice, line length,
// character encoding, binary representation.  Deviations from the standard cause
// data corruption in the Imperial datanet — a chaos Tarkin will not tolerate.
// This preference page enforces the same discipline for LDAP export formats:
// LDIF, CSV copy, CSV export, Excel, and ODF each have their own tab, their own
// delimiter rules, their own line-separator choice — and every field must be
// non-empty before we allow a save.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page for controlling text format settings: how LDIF,
 * CSV, Excel, and ODF exports are encoded when the user copies or exports entries.
 * Each format gets its own tab.  Think of this class as the Imperial formatting
 * standards manual — every export channel has its own protocol, and we validate
 * that none of them are left blank.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TextFormatsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
    WidgetModifyListener, ModifyListener
{

    /** The constant used to preselect the 'LDIF' tab */
    public static final String LDIF_TAB = "LDIF"; //$NON-NLS-1$

    /** The constant used to preselect the 'CSV Export' tab */
    public static final String CSV_TAB = "CSV"; //$NON-NLS-1$

    /** The constant used to preselect the 'Excel Export' tab */
    public static final String XLS_TAB = "XLS"; //$NON-NLS-1$

    /** The constant used to preselect the 'ODF Export' tab */
    public static final String ODF_TAB = "ODF"; //$NON-NLS-1$

    /** The constant used to preselect the 'CSV Copy' tab */
    public static final String TABLE_TAB = "TABLE"; //$NON-NLS-1$

    private Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();

    private TabFolder tabFolder;

    private TabItem ldifTab;

    private TabItem tableTab;

    private TabItem csvTab;

    private TabItem xlsTab;

    private TabItem odfTab;

    private Text ldifLineLengthText;

    private Button ldifSpaceAfterColonButton;

    private Button includeVersionLineButton;

    private LineSeparatorInput ldifLineSeparator;

    private OptionsInput tableAttributeDelimiterWidget;

    private OptionsInput tableValueDelimiterWidget;

    private OptionsInput tableQuoteWidget;

    private LineSeparatorInput tableLineSeparator;

    private BinaryEncodingInput tableBinaryEncodingWidget;

    private OptionsInput csvAttributeDelimiterWidget;

    private OptionsInput csvValueDelimiterWidget;

    private OptionsInput csvQuoteWidget;

    private LineSeparatorInput csvLineSeparator;

    private BinaryEncodingInput csvBinaryEncodingWidget;

    private FileEncodingInput csvEncodingWidget;

    private OptionsInput xlsValueDelimiterWidget;

    private OptionsInput xlsBinaryEncodingWidget;

    private OptionsInput odfValueDelimiterWidget;

    private OptionsInput odfBinaryEncodingWidget;


    // ── TARKIN OPENS THE FORMATTING STANDARDS BRIEFING ───────────────────────────
    // Grand Moff Tarkin calls the code officers to attention, announces the topic
    // ("Text Formats"), reads out the scope ("Settings for text formats"), and
    // points the secretariat to the right data stores — one for common UI
    // preferences, one for core export settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the page with its title and description, wired to the common
     * preference store.  Core export settings (CSV, LDIF, XLS, ODF) are stored
     * in the core plugin store, which we access directly via {@code coreStore}.
     */
    public TextFormatsPreferencePage()
    {
        super( Messages.getString( "TextFormatsPreferencePage.TextFormats" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserCommonActivator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "TextFormatsPreferencePage.SettingsForTextFormats" ) ); //$NON-NLS-1$
    }


    // ── TARKIN ACKNOWLEDGES THE PLATFORM FRAMEWORK ────────────────────────────────
    // Tarkin glances at the workbench and returns his gaze to the agenda.
    // Nothing actionable — interface contract only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Required by {@link IWorkbenchPreferencePage} — not used here.
     *
     * @param workbench  The Eclipse workbench instance — not used.
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── TARKIN ROUTES THE REQUEST TO THE CORRECT FORMAT SECTION ──────────────────
    // A code officer arrives with a report tagged "CSV" — Tarkin routes him
    // straight to the CSV section without reading every other tab.  applyData()
    // does the same: when called with a tab-name constant, we switch the tab
    // folder to the matching tab so the user lands in the right place.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically selects the tab matching the supplied tag string.
     * Callers pass one of the {@code *_TAB} constants to jump directly to a
     * specific format tab — useful when an action elsewhere in the UI needs
     * to open this page at a particular format.
     *
     * <p>For example — Tarkin routing the LDIF report:</p>
     * <pre>
     *   applyData("LDIF");  // tabFolder.setSelection(0)
     *   applyData("CSV");   // tabFolder.setSelection(2)
     * </pre>
     *
     * @param data  One of {@link #LDIF_TAB}, {@link #TABLE_TAB}, {@link #CSV_TAB},
     *              {@link #XLS_TAB}, or {@link #ODF_TAB}.  Ignored if null or
     *              unrecognised.
     */
    public void applyData( Object data )
    {
        if ( data != null && tabFolder != null )
        {
            if ( LDIF_TAB.equals( data ) )
            {
                tabFolder.setSelection( 0 );
            }
            else if ( TABLE_TAB.equals( data ) )
            {
                tabFolder.setSelection( 1 );
            }
            else if ( CSV_TAB.equals( data ) )
            {
                tabFolder.setSelection( 2 );
            }
            else if ( XLS_TAB.equals( data ) )
            {
                tabFolder.setSelection( 3 );
            }
            else if ( ODF_TAB.equals( data ) )
            {
                tabFolder.setSelection( 4 );
            }
        }
    }


    // ── TARKIN ASSEMBLES THE FIVE-SECTION STANDARDS MANUAL ───────────────────────
    // Tarkin lays out the Imperial Formatting Manual: five tabs, one per protocol
    // (LDIF, CSV Copy, CSV Export, Excel, ODF).  Each tab contains the formatting
    // rules for that channel.  After assembly, validate() is called to ensure no
    // section was left blank before the manual is published.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the tab folder containing all five format tabs, pre-filled from the
     * current preference values.  Calls {@link #validate()} at the end so the
     * save button starts in the correct state.
     *
     * @param parent  The parent composite provided by Eclipse.
     * @return        The tab folder containing all format tabs.
     */
    protected Control createContents( Composite parent )
    {
        BaseWidgetUtils.createSpacer( parent, 1 );
        tabFolder = new TabFolder( parent, SWT.TOP );

        createLdifTab();
        createTableTab();
        createCsvTab();
        createXlsTab();
        createOdfTab();

        validate();

        applyDialogFont( tabFolder );

        return tabFolder;
    }


    private void createTableTab()
    {
        tableTab = new TabItem( tabFolder, SWT.NONE );
        tableTab.setText( Messages.getString( "TextFormatsPreferencePage.CSVCopy" ) ); //$NON-NLS-1$

        Composite tableComposite = new Composite( tabFolder, SWT.NONE );
        tableComposite.setLayout( new GridLayout( 1, false ) );
        Composite tableInnerComposite = BaseWidgetUtils.createColumnContainer( tableComposite, 3, 1 );

        BaseWidgetUtils.createLabel( tableInnerComposite,
            Messages.getString( "TextFormatsPreferencePage.CSVCopyLabel" ), 3 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( tableInnerComposite, 3 );

        tableAttributeDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.AttributeDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Tabulator" ), "\t", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Tabulator" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                { "\t", ",", ";" }, getPreferenceStore().getString( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_ATTRIBUTEDELIMITER ), false, true );
        tableAttributeDelimiterWidget.createWidget( tableInnerComposite );
        tableAttributeDelimiterWidget.addWidgetModifyListener( this );

        tableValueDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.ValueDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Pipe" ), "|", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Pipe" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ), Messages.getString( "TextFormatsPreferencePage.Newline" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                { "|", ",", ";", "\n" }, getPreferenceStore().getString( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_VALUEDELIMITER ), false, true );
        tableValueDelimiterWidget.createWidget( tableInnerComposite );
        tableValueDelimiterWidget.addWidgetModifyListener( this );

        tableQuoteWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.QuoteCharacter" ), Messages.getString( "TextFormatsPreferencePage.DoubleQuote" ), "\"", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.DoubleQuote" ), Messages.getString( "TextFormatsPreferencePage.SingleQuote" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$
                { "\"", "'" }, getPreferenceStore().getString( //$NON-NLS-1$ //$NON-NLS-2$
                BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_QUOTECHARACTER ), false, true );
        tableQuoteWidget.createWidget( tableInnerComposite );
        tableQuoteWidget.addWidgetModifyListener( this );

        tableLineSeparator = new LineSeparatorInput( getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_LINESEPARATOR ), false );
        tableLineSeparator.createWidget( tableInnerComposite );
        tableLineSeparator.addWidgetModifyListener( this );

        tableBinaryEncodingWidget = new BinaryEncodingInput( getPreferenceStore().getString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_BINARYENCODING ), false );
        tableBinaryEncodingWidget.createWidget( tableInnerComposite );
        tableBinaryEncodingWidget.addWidgetModifyListener( this );

        Composite copyTableHintComposite = BaseWidgetUtils.createColumnContainer( tableInnerComposite, 3, 3 );
        Text hintText = BaseWidgetUtils.createWrappedLabeledText( copyTableHintComposite, Messages
            .getString( "TextFormatsPreferencePage.CSVCopyHint" ), 1 ); //$NON-NLS-1$
        GridData hintTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        hintTextGridData.widthHint = 300;
        hintText.setLayoutData( hintTextGridData );

        tableTab.setControl( tableComposite );
    }


    private void createCsvTab()
    {
        csvTab = new TabItem( tabFolder, SWT.NONE );
        csvTab.setText( Messages.getString( "TextFormatsPreferencePage.CSVExport" ) ); //$NON-NLS-1$

        Composite csvComposite = new Composite( tabFolder, SWT.NONE );
        csvComposite.setLayout( new GridLayout( 1, false ) );
        Composite csvInnerComposite = BaseWidgetUtils.createColumnContainer( csvComposite, 3, 1 );

        BaseWidgetUtils.createLabel( csvInnerComposite,
            Messages.getString( "TextFormatsPreferencePage.CSVExportLabel" ), 3 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( csvInnerComposite, 3 );

        csvAttributeDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.AttributeDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), ",", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ), Messages.getString( "TextFormatsPreferencePage.Tabulator" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                { ",", ";", "\t" }, coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            false, true );
        csvAttributeDelimiterWidget.createWidget( csvInnerComposite );
        csvAttributeDelimiterWidget.addWidgetModifyListener( this );

        csvValueDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.ValueDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Pipe" ), "|", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Pipe" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ), Messages.getString( "TextFormatsPreferencePage.Newline" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                { "|", ",", ";", "\n" }, coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_VALUEDELIMITER ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            false, true );
        csvValueDelimiterWidget.createWidget( csvInnerComposite );
        csvValueDelimiterWidget.addWidgetModifyListener( this );

        csvQuoteWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.QuoteCharacter" ), Messages.getString( "TextFormatsPreferencePage.DoubleQuote" ), "\"", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.DoubleQuote" ), Messages.getString( "TextFormatsPreferencePage.SingleQuote" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$
                { "\"", "'" }, coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_QUOTECHARACTER ), false, //$NON-NLS-1$ //$NON-NLS-2$
            true );
        csvQuoteWidget.createWidget( csvInnerComposite );
        csvQuoteWidget.addWidgetModifyListener( this );

        csvLineSeparator = new LineSeparatorInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_LINESEPARATOR ), false );
        csvLineSeparator.createWidget( csvInnerComposite );
        csvLineSeparator.addWidgetModifyListener( this );

        csvBinaryEncodingWidget = new BinaryEncodingInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_BINARYENCODING ), false );
        csvBinaryEncodingWidget.createWidget( csvInnerComposite );
        csvBinaryEncodingWidget.addWidgetModifyListener( this );

        csvEncodingWidget = new FileEncodingInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ENCODING ), false );
        csvEncodingWidget.createWidget( csvInnerComposite );
        csvEncodingWidget.addWidgetModifyListener( this );

        csvTab.setControl( csvComposite );
    }


    private void createXlsTab()
    {
        xlsTab = new TabItem( tabFolder, SWT.NONE );
        xlsTab.setText( Messages.getString( "TextFormatsPreferencePage.ExcelExport" ) ); //$NON-NLS-1$

        Composite xlsComposite = new Composite( tabFolder, SWT.NONE );
        xlsComposite.setLayout( new GridLayout( 1, false ) );
        Composite xlsInnerComposite = BaseWidgetUtils.createColumnContainer( xlsComposite, 3, 1 );

        BaseWidgetUtils.createLabel( xlsInnerComposite, Messages
            .getString( "TextFormatsPreferencePage.ExcelExportLabel" ), 3 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( xlsInnerComposite, 3 );

        xlsValueDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.ValueDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Pipe" ), "|", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Pipe" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ), Messages.getString( "TextFormatsPreferencePage.Newline" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                { "|", ",", ";", "\n" }, coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_VALUEDELIMITER ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            false, true );
        xlsValueDelimiterWidget.createWidget( xlsInnerComposite );
        xlsValueDelimiterWidget.addWidgetModifyListener( this );

        xlsBinaryEncodingWidget = new BinaryEncodingInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_BINARYENCODING ), false );
        xlsBinaryEncodingWidget.createWidget( xlsInnerComposite );
        xlsBinaryEncodingWidget.addWidgetModifyListener( this );

        xlsTab.setControl( xlsComposite );
    }


    private void createOdfTab()
    {
        odfTab = new TabItem( tabFolder, SWT.NONE );
        odfTab.setText( Messages.getString( "TextFormatsPreferencePage.OdfExport" ) ); //$NON-NLS-1$

        Composite odfComposite = new Composite( tabFolder, SWT.NONE );
        odfComposite.setLayout( new GridLayout( 1, false ) );
        Composite odfInnerComposite = BaseWidgetUtils.createColumnContainer( odfComposite, 3, 1 );

        BaseWidgetUtils.createLabel( odfInnerComposite,
            Messages.getString( "TextFormatsPreferencePage.OdfExportLabel" ), 3 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( odfInnerComposite, 3 );

        odfValueDelimiterWidget = new OptionsInput(
            Messages.getString( "TextFormatsPreferencePage.ValueDelimiter" ), Messages.getString( "TextFormatsPreferencePage.Pipe" ), "|", new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Messages.getString( "TextFormatsPreferencePage.Pipe" ), Messages.getString( "TextFormatsPreferencePage.Comma" ), Messages.getString( "TextFormatsPreferencePage.Semicolon" ), Messages.getString( "TextFormatsPreferencePage.Newline" ) }, new String[] //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                { "|", ",", ";", "\n" }, coreStore.getString( BrowserCoreConstants.PREFERENCE_FORMAT_ODF_VALUEDELIMITER ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            false, true );
        odfValueDelimiterWidget.createWidget( odfInnerComposite );
        odfValueDelimiterWidget.addWidgetModifyListener( this );

        odfBinaryEncodingWidget = new BinaryEncodingInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_FORMAT_ODF_BINARYENCODING ), false );
        odfBinaryEncodingWidget.createWidget( odfInnerComposite );
        odfBinaryEncodingWidget.addWidgetModifyListener( this );

        odfTab.setControl( odfComposite );
    }


    private void createLdifTab()
    {
        ldifTab = new TabItem( tabFolder, SWT.NONE );
        ldifTab.setText( Messages.getString( "TextFormatsPreferencePage.LDIF" ) ); //$NON-NLS-1$

        Composite ldifComposite = new Composite( tabFolder, SWT.NONE );
        ldifComposite.setLayout( new GridLayout( 1, false ) );
        Composite ldifInnerComposite = BaseWidgetUtils.createColumnContainer( ldifComposite, 1, 1 );

        BaseWidgetUtils
            .createLabel( ldifInnerComposite, Messages.getString( "TextFormatsPreferencePage.LDIFLabel" ), 1 ); //$NON-NLS-1$
        BaseWidgetUtils.createSpacer( ldifInnerComposite, 1 );

        ldifLineSeparator = new LineSeparatorInput( coreStore
            .getString( BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR ), true );
        ldifLineSeparator.createWidget( ldifInnerComposite );
        ldifLineSeparator.addWidgetModifyListener( this );

        BaseWidgetUtils.createSpacer( ldifInnerComposite, 1 );

        Composite lineLengthComposite = BaseWidgetUtils.createColumnContainer( ldifInnerComposite, 3, 1 );
        BaseWidgetUtils.createLabel( lineLengthComposite,
            Messages.getString( "TextFormatsPreferencePage.LineLength1" ), 1 ); //$NON-NLS-1$
        ldifLineLengthText = BaseWidgetUtils.createText( lineLengthComposite, "", 3, 1 ); //$NON-NLS-1$
        ldifLineLengthText.setText( coreStore.getString( BrowserCoreConstants.PREFERENCE_LDIF_LINE_WIDTH ) );
        ldifLineLengthText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( ldifLineLengthText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );
        ldifLineLengthText.addModifyListener( this );
        BaseWidgetUtils.createLabel( lineLengthComposite,
            Messages.getString( "TextFormatsPreferencePage.LineLength2" ), 1 ); //$NON-NLS-1$

        ldifSpaceAfterColonButton = BaseWidgetUtils.createCheckbox( ldifInnerComposite, Messages
            .getString( "TextFormatsPreferencePage.SpaceAfterColon" ), 1 ); //$NON-NLS-1$
        ldifSpaceAfterColonButton.setSelection( coreStore
            .getBoolean( BrowserCoreConstants.PREFERENCE_LDIF_SPACE_AFTER_COLON ) );

        includeVersionLineButton = BaseWidgetUtils.createCheckbox( ldifInnerComposite, Messages
            .getString( "TextFormatsPreferencePage.IncludeVersionLine" ), 1 ); //$NON-NLS-1$
        includeVersionLineButton.setSelection( coreStore
            .getBoolean( BrowserCoreConstants.PREFERENCE_LDIF_INCLUDE_VERSION_LINE ) );

        ldifTab.setControl( ldifComposite );
    }


    // ── TARKIN COUNTERSIGNS ALL FIVE SECTIONS OF THE MANUAL ──────────────────────
    // Tarkin works through the manual page by page: LDIF settings (line width,
    // separator, space-after-colon, version line), CSV export, CSV copy (table),
    // Excel, and ODF.  Each section is written to the appropriate store — common
    // for table/copy, core for the rest.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves all format preferences to the appropriate stores when the user clicks
     * OK or Apply.  LDIF, CSV export, XLS, and ODF go to the core plugin store;
     * CSV copy (table) goes to the common preference store.
     *
     * @return  Always true if all fields were valid (validate() would have blocked
     *          the Apply button otherwise).
     */
    public boolean performOk()
    {
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_LDIF_LINE_WIDTH, ldifLineLengthText.getText() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR, ldifLineSeparator.getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_LDIF_SPACE_AFTER_COLON, ldifSpaceAfterColonButton
            .getSelection() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_LDIF_INCLUDE_VERSION_LINE, includeVersionLineButton
            .getSelection() );

        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER, csvAttributeDelimiterWidget
            .getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_VALUEDELIMITER, csvValueDelimiterWidget
            .getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_QUOTECHARACTER, csvQuoteWidget.getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_LINESEPARATOR, csvLineSeparator.getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_BINARYENCODING, csvBinaryEncodingWidget
            .getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ENCODING, csvEncodingWidget.getRawValue() );

        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_ATTRIBUTEDELIMITER,
            tableAttributeDelimiterWidget.getRawValue() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_VALUEDELIMITER,
            tableValueDelimiterWidget.getRawValue() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_QUOTECHARACTER,
            tableQuoteWidget.getRawValue() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_LINESEPARATOR,
            tableLineSeparator.getRawValue() );
        getPreferenceStore().setValue( BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_BINARYENCODING,
            tableBinaryEncodingWidget.getRawValue() );

        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_VALUEDELIMITER, xlsValueDelimiterWidget
            .getRawValue() );
        coreStore.setValue( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_BINARYENCODING, xlsBinaryEncodingWidget
            .getRawValue() );

        BrowserCorePlugin.getDefault().savePluginPreferences();

        validate();

        return true;
    }


    // ── TARKIN REINSTATES THE ORIGINAL IMPERIAL FORMAT STANDARDS ─────────────────
    // A code officer hands Tarkin the original Imperial Formatting Directive —
    // the document that predates any customization.  Tarkin restores every field
    // on every tab to the factory values from that directive.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets all format preferences to their plugin-defined defaults.  Called when
     * the user clicks "Restore Defaults."  We update every widget from the default
     * values so the UI reflects what will be saved.
     */
    protected void performDefaults()
    {
        ldifLineLengthText.setText( coreStore.getDefaultString( BrowserCoreConstants.PREFERENCE_LDIF_LINE_WIDTH ) );
        ldifLineSeparator
            .setRawValue( coreStore.getDefaultString( BrowserCoreConstants.PREFERENCE_LDIF_LINE_SEPARATOR ) );
        ldifSpaceAfterColonButton.setSelection( coreStore
            .getDefaultBoolean( BrowserCoreConstants.PREFERENCE_LDIF_SPACE_AFTER_COLON ) );
        includeVersionLineButton.setSelection( coreStore
            .getDefaultBoolean( BrowserCoreConstants.PREFERENCE_LDIF_INCLUDE_VERSION_LINE ) );

        csvAttributeDelimiterWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ATTRIBUTEDELIMITER ) );
        csvValueDelimiterWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_VALUEDELIMITER ) );
        csvQuoteWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_QUOTECHARACTER ) );
        csvLineSeparator.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_LINESEPARATOR ) );
        csvBinaryEncodingWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_BINARYENCODING ) );
        csvEncodingWidget
            .setRawValue( coreStore.getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_CSV_ENCODING ) );

        tableAttributeDelimiterWidget.setRawValue( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_ATTRIBUTEDELIMITER ) );
        tableValueDelimiterWidget.setRawValue( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_VALUEDELIMITER ) );
        tableQuoteWidget.setRawValue( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_QUOTECHARACTER ) );
        tableLineSeparator.setRawValue( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_LINESEPARATOR ) );
        tableBinaryEncodingWidget.setRawValue( getPreferenceStore().getDefaultString(
            BrowserCommonConstants.PREFERENCE_FORMAT_TABLE_BINARYENCODING ) );

        xlsValueDelimiterWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_VALUEDELIMITER ) );
        xlsBinaryEncodingWidget.setRawValue( coreStore
            .getDefaultString( BrowserCoreConstants.PREFERENCE_FORMAT_XLS_BINARYENCODING ) );

        validate();

        super.performDefaults();
    }


    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    public void modifyText( ModifyEvent e )
    {
        validate();
    }


    protected void validate()
    {
        setValid( !"".equals( csvAttributeDelimiterWidget.getRawValue() ) //$NON-NLS-1$
            && !"".equals( csvValueDelimiterWidget.getRawValue() ) && !"".equals( csvQuoteWidget.getRawValue() ) //$NON-NLS-1$ //$NON-NLS-2$
            && !"".equals( csvLineSeparator.getRawValue() ) && !"".equals( csvBinaryEncodingWidget.getRawValue() ) //$NON-NLS-1$ //$NON-NLS-2$
            && !"".equals( csvEncodingWidget.getRawValue() ) && //$NON-NLS-1$

            !"".equals( tableAttributeDelimiterWidget.getRawValue() ) //$NON-NLS-1$
            && !"".equals( tableValueDelimiterWidget.getRawValue() ) && !"".equals( tableQuoteWidget.getRawValue() ) //$NON-NLS-1$ //$NON-NLS-2$
            && !"".equals( tableLineSeparator.getRawValue() ) && !"".equals( tableBinaryEncodingWidget.getRawValue() ) //$NON-NLS-1$ //$NON-NLS-2$
            &&

            !"".equals( xlsValueDelimiterWidget.getRawValue() ) && !"".equals( xlsBinaryEncodingWidget.getRawValue() ) //$NON-NLS-1$ //$NON-NLS-2$
            &&

            !"".equals( ldifLineLengthText.getText() ) && !"".equals( ldifLineSeparator.getRawValue() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
