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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: LdifEditorContentAssistPreferencePage — R2-D2 SUGGESTION SETTINGS ──
// Before the Battle of Yavin R2-D2's suggestion circuits are calibrated:
// how long to wait before popping up escape-route proposals, whether to
// insert the only option automatically, and whether to be clever about
// attribute names inside modification specs.
// This preference page exposes those same knobs for the LDIF editor's
// content-assist feature.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link PreferencePage} for the LDIF editor's content-assist settings.
 * Controls auto-insertion of single proposals, auto-activation with configurable
 * delay, and smart attribute insertion in modification specs.
 * Think of this as the calibration panel for R2-D2's suggestion circuits.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorContentAssistPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    /** Auto-insert the single proposal when it is the only option. */
    private Button insertSingleProposalAutoButton;

    /** Enable auto-activation of content assist after a short delay. */
    private Button enableAutoActivationButton;

    /** Label for the auto-activation delay spinner. */
    private Label autoActivationDelayLabel;

    /** Text field for the auto-activation delay in milliseconds. */
    private Text autoActivationDelayText;

    /** "ms" suffix label for the delay field. */
    private Label autoActivationDelayMs;

    /** Auto-insert attribute name in modification specs. */
    private Button smartInsertAttributeInModspecButton;


    // ── CONSTRUCT THE PREFERENCE PAGE ─────────────────────────────────────────
    // R2-D2 reports to the calibration station.
    /**
     * Creates a new content-assist preference page and binds it to the
     * LDIF editor preference store.
     */
    public LdifEditorContentAssistPreferencePage()
    {
        super( Messages.getString( "LdifEditorContentAssistPreferencePage.ContentAssist" ) ); //$NON-NLS-1$
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
    // R2-D2's technician installs the insert-single, auto-activation, delay,
    // and smart-insert controls in the calibration panel.
    /**
     * {@inheritDoc}
     *
     * <p>Creates a single Content Assist group containing four controls:
     * insert-single-proposal, enable auto-activation, auto-activation delay,
     * and smart-insert-attribute-in-modspec.</p>
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
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        composite.setLayoutData( gd );

        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );

        Group caGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ), Messages
            .getString( "LdifEditorContentAssistPreferencePage.ContentAssist" ), 1 ); //$NON-NLS-1$

        insertSingleProposalAutoButton = BaseWidgetUtils.createCheckbox( caGroup, Messages
            .getString( "LdifEditorContentAssistPreferencePage.InsertSingleProposalAutomatically" ), 1 ); //$NON-NLS-1$
        insertSingleProposalAutoButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO ) );

        enableAutoActivationButton = BaseWidgetUtils.createCheckbox( caGroup, Messages
            .getString( "LdifEditorContentAssistPreferencePage.EnableAutoAction" ), 1 ); //$NON-NLS-1$
        enableAutoActivationButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION ) );
        enableAutoActivationButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                checkEnabled();
            }
        } );

        Composite autoActivationDelayComposite = BaseWidgetUtils.createColumnContainer( caGroup, 4, 1 );
        BaseWidgetUtils.createRadioIndent( autoActivationDelayComposite, 1 );
        autoActivationDelayLabel = BaseWidgetUtils.createLabel( autoActivationDelayComposite, Messages
            .getString( "LdifEditorContentAssistPreferencePage.AutoActivationDelay" ), //$NON-NLS-1$
            1 );
        autoActivationDelayText = BaseWidgetUtils.createText( autoActivationDelayComposite, "", 4, 1 ); //$NON-NLS-1$
        autoActivationDelayText.setText( getPreferenceStore().getString(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY ) );
        autoActivationDelayText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( autoActivationDelayText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );
        autoActivationDelayMs = BaseWidgetUtils.createLabel( autoActivationDelayComposite, Messages
            .getString( "LdifEditorContentAssistPreferencePage.MilliSecons" ), 1 ); //$NON-NLS-1$

        smartInsertAttributeInModspecButton = BaseWidgetUtils.createCheckbox( caGroup, Messages
            .getString( "LdifEditorContentAssistPreferencePage.SmartInsertAttributeName" ), 1 ); //$NON-NLS-1$
        smartInsertAttributeInModspecButton.setSelection( getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_SMARTINSERTATTRIBUTEINMODSPEC ) );

        checkEnabled();

        return composite;
    }


    // ── ENABLE / DISABLE SUB-OPTIONS ──────────────────────────────────────────
    // When auto-activation is disabled the delay field and its labels grey out.
    /**
     * Enables or disables the delay field and its labels based on whether
     * auto-activation is enabled.
     */
    private void checkEnabled()
    {
        autoActivationDelayLabel.setEnabled( enableAutoActivationButton.getSelection() );
        autoActivationDelayText.setEnabled( enableAutoActivationButton.getSelection() );
        autoActivationDelayMs.setEnabled( enableAutoActivationButton.getSelection() );
    }


    // ── SAVE PREFERENCES ──────────────────────────────────────────────────────
    // R2-D2's technician writes the updated calibration values to memory.
    /**
     * {@inheritDoc}
     *
     * <p>Persists all content-assist settings to the preference store.</p>
     */
    public boolean performOk()
    {

        getPreferenceStore().setValue(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO,
            this.insertSingleProposalAutoButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION,
            this.enableAutoActivationButton.getSelection() );
        getPreferenceStore().setValue( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY,
            this.autoActivationDelayText.getText() );
        getPreferenceStore().setValue(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_SMARTINSERTATTRIBUTEINMODSPEC,
            this.smartInsertAttributeInModspecButton.getSelection() );

        return true;
    }


    // ── RESTORE DEFAULTS ──────────────────────────────────────────────────────
    // Factory reset: R2-D2's calibration reverts to the default settings.
    /**
     * {@inheritDoc}
     *
     * <p>Reloads default values from the preference store into all controls
     * and re-evaluates enabled states.</p>
     */
    protected void performDefaults()
    {

        insertSingleProposalAutoButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO ) );
        enableAutoActivationButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION ) );
        autoActivationDelayText.setText( getPreferenceStore().getDefaultString(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY ) );
        smartInsertAttributeInModspecButton.setSelection( getPreferenceStore().getDefaultBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_SMARTINSERTATTRIBUTEINMODSPEC ) );

        super.performDefaults();

        checkEnabled();
    }

}
