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

package org.apache.directory.studio.ldifeditor.dialogs.preferences;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: LdifEditorPreferencePage — REBEL CONSOLE SETTINGS PANEL ────────────
// Before the Battle of Yavin the Alliance ops team adjusts the main console:
// they configure how the war-room displays fold records, what happens on
// double-click, and what the execute command does if an entry already exists.
// LdifEditorPreferencePage is that settings panel for the LDIF editor: folding,
// double-click behaviour, and execute options all live here.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Main Eclipse {@link PreferencePage} for the LDIF editor.
 * Controls folding (enable, initially fold comments / records / wrapped lines),
 * double-click behaviour, and execute options (update if exists, continue on error).
 * Think of this as the Rebel ops team's main console settings panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    // private Button autoWrapButton;

    /** The enable folding button. */
    private Button enableFoldingButton;

    /** The initially fold label. */
    private Label initiallyFoldLabel;

    /** The initially fold comments button. */
    private Button initiallyFoldCommentsButton;

    /** The initially fold records button. */
    private Button initiallyFoldRecordsButton;

    /** The initially fold wrapped lines button. */
    private Button initiallyFoldWrappedLinesButton;

    /** The use ldif double click button. */
    private Button useLdifDoubleClickButton;

    /** The update if entry exists button. */
    private Button updateIfEntryExistsButton;

    /** The continue on error button. */
    private Button continueOnErrorButton;


    // ── CONSTRUCT THE PREFERENCE PAGE ─────────────────────────────────────────
    // The ops officer reports to the console and sets the preference store.
    /**
     * Creates a new {@code LdifEditorPreferencePage} and binds it to the
     * LDIF editor preference store.
     */
    public LdifEditorPreferencePage()
    {
        super( Messages.getString( "LdifEditorPreferencePage.LDIFEditor" ) ); //$NON-NLS-1$
        super.setPreferenceStore( LdifEditorActivator.getDefault().getPreferenceStore() );
    }


    // ── INITIALISE ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void init( IWorkbench workbench )
    {
    }


    // ── BUILD THE PAGE CONTENTS ───────────────────────────────────────────────
    // The ops team wires up two navigation links (text editor, text formats),
    // then creates three groups: Folding, Double-Click Behaviour, and Execute Options.
    /**
     * {@inheritDoc}
     *
     * <p>Creates navigation links to related preference pages, then three groups:
     * Folding, Double-Click Behaviour, and Execute Options.</p>
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        String text = Messages.getString( "LdifEditorPreferencePage.LinkToTextEditors" ); //$NON-NLS-1$
        Link link = BaseWidgetUtils.createLink( composite, text, 1 );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(),
                    "org.eclipse.ui.preferencePages.GeneralTextEditor", null, null ); //$NON-NLS-1$
            }
        } );
        String text2 = Messages.getString( "LdifEditorPreferencePage.LinkToTextFormats" ); //$NON-NLS-1$
        Link link2 = BaseWidgetUtils.createLink( composite, text2, 1 );
        link2.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(), LdifEditorConstants.PREFERENCEPAGEID_TEXTFORMATS,
                    null, null ); //$NON-NLS-1$
            }
        } );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Group foldGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "LdifEditorPreferencePage.Folding" ), 1 ); //$NON-NLS-1$

        enableFoldingButton = BaseWidgetUtils.createCheckbox( foldGroup, Messages
            .getString( "LdifEditorPreferencePage.EnableFolding" ), 1 ); //$NON-NLS-1$
        enableFoldingButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE ) );
        enableFoldingButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                checkEnabled();
            }
        } );

        Composite initiallyFoldComposiste = BaseWidgetUtils.createColumnContainer( foldGroup, 4, 1 );
        initiallyFoldLabel = BaseWidgetUtils.createLabel( initiallyFoldComposiste, Messages
            .getString( "LdifEditorPreferencePage.InitiallyFold" ), 1 ); //$NON-NLS-1$
        initiallyFoldCommentsButton = BaseWidgetUtils.createCheckbox( initiallyFoldComposiste, Messages
            .getString( "LdifEditorPreferencePage.Comments" ), 1 ); //$NON-NLS-1$
        initiallyFoldCommentsButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS ) );
        initiallyFoldRecordsButton = BaseWidgetUtils.createCheckbox( initiallyFoldComposiste, Messages
            .getString( "LdifEditorPreferencePage.Records" ), 1 ); //$NON-NLS-1$
        initiallyFoldRecordsButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS ) );
        initiallyFoldWrappedLinesButton = BaseWidgetUtils.createCheckbox( initiallyFoldComposiste, Messages
            .getString( "LdifEditorPreferencePage.WrappedLines" ), 1 ); //$NON-NLS-1$
        initiallyFoldWrappedLinesButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES ) );

        BaseWidgetUtils.createSpacer( composite, 1 );

        Group doubleClickGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "LdifEditorPreferencePage.DoubleClickBehaviour" ), 1 ); //$NON-NLS-1$
        useLdifDoubleClickButton = BaseWidgetUtils.createCheckbox( doubleClickGroup, Messages
            .getString( "LdifEditorPreferencePage.SelectWholeAttributeOnDoubleClick" ), 1 ); //$NON-NLS-1$
        useLdifDoubleClickButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK ) );

        BaseWidgetUtils.createSpacer( composite, 1 );

        // Options
        Group optionsGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "LdifEditorPreferencePage.ExecuteOptions" ), 1 ); //$NON-NLS-1$

        updateIfEntryExistsButton = BaseWidgetUtils.createCheckbox( optionsGroup, Messages
            .getString( "LdifEditorPreferencePage.UpdateExistingEntries" ), 1 ); //$NON-NLS-1$
        updateIfEntryExistsButton.setToolTipText( Messages
            .getString( "LdifEditorPreferencePage.UpdateExistingEntriesToolTip1" ) //$NON-NLS-1$
            + Messages.getString( "LdifEditorPreferencePage.UpdateExistingEntriesToolTip2" ) ); //$NON-NLS-1$
        updateIfEntryExistsButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_UPDATEIFENTRYEXISTS ) );

        continueOnErrorButton = BaseWidgetUtils.createCheckbox( optionsGroup, Messages
            .getString( "LdifEditorPreferencePage.ContinueOnError" ), 1 ); //$NON-NLS-1$
        continueOnErrorButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_CONTINUEONERROR ) );

        checkEnabled();

        return composite;
    }


    // ── ENABLE / DISABLE SUB-OPTIONS ──────────────────────────────────────────
    // When folding is turned off the "initially fold X" sub-options grey out;
    // when it is turned back on they become active again.
    /**
     * Enables or disables the "initially fold" sub-options based on whether
     * folding itself is enabled.
     */
    private void checkEnabled()
    {
        initiallyFoldLabel.setEnabled( enableFoldingButton.getSelection() );
        initiallyFoldCommentsButton.setEnabled( enableFoldingButton.getSelection() );
        initiallyFoldRecordsButton.setEnabled( enableFoldingButton.getSelection() );
        initiallyFoldWrappedLinesButton.setEnabled( enableFoldingButton.getSelection() );
    }


    // ── SAVE PREFERENCES ──────────────────────────────────────────────────────
    // The ops officer files the updated settings into the preference store.
    /**
     * {@inheritDoc}
     *
     * <p>Persists all settings to the preference store.</p>
     */
    public boolean performOk()
    {
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE,
            enableFoldingButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS,
            initiallyFoldCommentsButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS,
            initiallyFoldRecordsButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES,
            initiallyFoldWrappedLinesButton.getSelection() );

        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK,
            useLdifDoubleClickButton.getSelection() );

        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_UPDATEIFENTRYEXISTS,
            updateIfEntryExistsButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_CONTINUEONERROR,
            continueOnErrorButton.getSelection() );

        return true;
    }


    // ── RESTORE DEFAULTS ──────────────────────────────────────────────────────
    // The quartermaster resets the console to factory configuration.
    /**
     * {@inheritDoc}
     *
     * <p>Reloads default values from the preference store into all controls
     * and re-evaluates enabled states.</p>
     */
    protected void performDefaults()
    {
        enableFoldingButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE ) );
        initiallyFoldCommentsButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS ) );
        initiallyFoldRecordsButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS ) );
        initiallyFoldWrappedLinesButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES ) );

        useLdifDoubleClickButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK ) );

        updateIfEntryExistsButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_UPDATEIFENTRYEXISTS ) );
        continueOnErrorButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_CONTINUEONERROR ) );

        super.performDefaults();

        checkEnabled();
    }

}
