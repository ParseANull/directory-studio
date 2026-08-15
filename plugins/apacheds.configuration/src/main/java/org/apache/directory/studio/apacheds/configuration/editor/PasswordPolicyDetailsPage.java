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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.apache.directory.server.config.beans.PasswordPolicyBean;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


// ── CLASS: PasswordPolicyDetailsPage — THE IMPERIAL SECURITY OFFICER'S BRIEFING PANEL ───
// An Imperial security officer sits down at the right-hand panel of the registry console
// and opens the full dossier for whichever clearance rule was just selected on the roster.
// She can tweak every setting — lockout durations, password quality rules, expiry windows —
// and her edits flow straight back into the in-memory configuration model.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The details panel shown on the right side of the Password Policies master/details view.
 * It renders every configurable field of a single {@link PasswordPolicyBean} — identity,
 * quality rules, expiration settings, behaviour options, and lockout parameters — and
 * commits user edits back to the bean on every change.
 * Think of this class as the Imperial security officer's editing station: she picks a
 * policy from the roster on the left and this panel opens its full dossier for inspection
 * and amendment.
 *
 * <pre>
 * .-------------------------------------------.
 * | Password Policy Details                   |
 * +-------------------------------------------+
 * | Set the properties of the password Policy |
 * |  [X] Enabled                              |
 * |  ID :          [//////////]               |
 * |  Description : [////////////////////////] |
 * .-------------------------------------------.
 * | Quality / Expiration / Options / Lockout  |
 * +-------------------------------------------+
 * |   ... (full layout in source comments)    |
 * +-------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordPolicyDetailsPage implements IDetailsPage
{
    /** The associated Master Details Block */
    private PasswordPoliciesMasterDetailsBlock masterDetailsBlock;

    /** The Managed Form */
    private IManagedForm mform;

    /** The input password policy */
    private PasswordPolicyBean passwordPolicy;

    // UI Widgets
    private Button enabledCheckbox;
    private Text idText;
    private Text descriptionText;
    private ComboViewer checkQualityComboViewer;
    private Text validatorText;
    private Button minimumLengthCheckbox;
    private Text minimumLengthText;
    private Button maximumLengthCheckbox;
    private Text maximumLengthText;
    private Text minimumAgeText;
    private Text maximumAgeText;
    private Button expireWarningCheckbox;
    private Text expireWarningText;
    private Button graceAuthenticationLimitCheckbox;
    private Text graceAuthenticationLimitText;
    private Button graceExpireCheckbox;
    private Text graceExpireText;
    private Button mustChangeCheckbox;
    private Button allowUserChangeCheckbox;
    private Button safeModifyCheckbox;
    private Button lockoutCheckbox;
    private Text lockoutDurationText;
    private Text maxFailureText;
    private Text failureCountIntervalText;
    private Button inHistoryCheckbox;
    private Text inHistoryText;
    private Button maxIdleCheckbox;
    private Text maxIdleText;
    private Text minimumDelayText;
    private Text maximumDelayText;

    // Listeners
    /** The Text Modify Listener */
    private ModifyListener textModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    /** The button Selection Listener */
    private SelectionListener buttonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    /** The viewer Selection Changed Listener */
    private ISelectionChangedListener viewerSelectionChangedListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    private VerifyListener integerVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };

    private ISelectionChangedListener checkQualityComboViewerSelectionChangedListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            StructuredSelection selection = ( StructuredSelection ) checkQualityComboViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                CheckQuality checkQuality = ( CheckQuality ) selection.getFirstElement();

                if ( checkQuality == CheckQuality.DISABLED )
                {
                    minimumLengthCheckbox.setEnabled( false );
                    minimumLengthText.setEnabled( false );
                    maximumLengthCheckbox.setEnabled( false );
                    maximumLengthText.setEnabled( false );
                }
                else
                {
                    int minimumLength = 0;
                    int maximumLength = 0;

                    try
                    {
                        minimumLength = Integer.parseInt( minimumLengthText.getText() );
                    }
                    catch ( NumberFormatException e )
                    {
                        // Nothing to do.
                    }

                    try
                    {
                        maximumLength = Integer.parseInt( maximumLengthText.getText() );
                    }
                    catch ( NumberFormatException e )
                    {
                        // Nothing to do.
                    }

                    minimumLengthCheckbox.setEnabled( true );
                    minimumLengthText.setEnabled( minimumLength != 0 );
                    maximumLengthCheckbox.setEnabled( true );
                    maximumLengthText.setEnabled( maximumLength != 0 );
                }
            }
        }
    };

    private SelectionListener minimumLengthCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            minimumLengthText.setEnabled( minimumLengthCheckbox.getSelection() );
        }
    };

    private SelectionListener maximumLengthCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            maximumLengthText.setEnabled( maximumLengthCheckbox.getSelection() );
        }
    };

    private SelectionListener expireWarningCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            expireWarningText.setEnabled( expireWarningCheckbox.getSelection() );
        }
    };

    private SelectionListener graceAuthenticationLimitCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            graceAuthenticationLimitText.setEnabled( graceAuthenticationLimitCheckbox.getSelection() );
        }
    };

    private SelectionListener graceExpireCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            graceExpireText.setEnabled( graceExpireCheckbox.getSelection() );
        }
    };

    private SelectionListener maxIdleCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            maxIdleText.setEnabled( maxIdleCheckbox.getSelection() );
        }
    };

    private SelectionListener inHistoryCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            inHistoryText.setEnabled( inHistoryCheckbox.getSelection() );
        }
    };


    // ── Plugging Into The Master Details Block ────────────────────────────────────────────
    // A freshly assigned Imperial security officer walks into the briefing station and
    // registers herself with the registry's master board so it knows where to send
    // selection events when a policy row is clicked.
    // We store the master block reference so we can later notify it when edits happen.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new PasswordPolicyDetailsPage and links it to its parent master/details block.
     * The block reference is used to call {@code setEditorDirty()} whenever the officer
     * commits a change to the policy bean.
     *
     * <p>For example — the Imperial security officer reports for duty at the briefing station:</p>
     * <pre>
     *   She checks in with the registry's master block, which hands her its ID.
     *   Now whenever she edits a field she can ping the block to mark the file as modified.
     * </pre>
     *
     * @param pmdb  the {@link PasswordPoliciesMasterDetailsBlock} that owns this details page
     */
    public PasswordPolicyDetailsPage( PasswordPoliciesMasterDetailsBlock pmdb )
    {
        masterDetailsBlock = pmdb;
    }


    // ── Assembling The Full Briefing Panel ────────────────────────────────────────────────
    // The Imperial security officer sits down and lays out five specialised sub-panels on
    // her briefing station: identity, quality checks, expiration rules, options, and lockout.
    // Each sub-panel handles a different dimension of the password policy dossier.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all five visual sections of the details panel inside the given parent composite.
     * This is the main layout method called once by Eclipse when the detail page is first
     * shown; it delegates each section to its own {@code create*Section} method.
     *
     * <p>For example — the officer lays out her briefing station with five dedicated zones:</p>
     * <pre>
     *   Zone 1: identity (ID, description, enabled flag).
     *   Zone 2: quality (check level, validator, min/max length).
     *   Zone 3: expiration (min/max age, expire warning, grace periods).
     *   Zone 4: options (must change, allow user change, safe modify).
     *   Zone 5: lockout (lockout duration, failure count, history, delays).
     * </pre>
     *
     * @param parent  the SWT composite to build the sections inside
     */
    public void createContents( Composite parent )
    {
        FormToolkit toolkit = mform.getToolkit();
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 5;
        layout.leftMargin = 5;
        layout.rightMargin = 2;
        layout.bottomMargin = 2;
        parent.setLayout( layout );

        // Depending on if the PP is enabled or disabled, we will
        // expose the configuration

        createDetailsSection( toolkit, parent );
        createQualitySection( toolkit, parent );
        createExpirationSection( toolkit, parent );
        createOptionsSection( toolkit, parent );
        createLockoutSection( toolkit, parent );
    }


    // ── Building The Identity Zone ────────────────────────────────────────────────────────
    // The top zone of the briefing station shows the basic identity fields: an enabled
    // toggle, the policy's unique ID, and a plain-English description.
    // These are the fields Vader would check first to confirm a policy's credentials.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Password Policy Details" section — the top block of the details panel —
     * containing the Enabled checkbox, the ID field, and the Description field.
     * The ID and Description fields are disabled for the default policy (it can't be renamed).
     *
     * <p>For example — Vader checks the identity panel on a clearance dossier:</p>
     * <pre>
     *   He reads: Enabled = checked, ID = "strictPolicy", Description = "VIP access rules".
     *   If it were the default policy those two fields would be greyed out — untouchable.
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit used to create styled widgets
     * @param parent   the parent composite to attach this section to
     */
    private void createDetailsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Password Policy Details" );
        section.setDescription( "Set the properties of the password policy." );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite client = toolkit.createComposite( section );
        toolkit.paintBordersFor( client );

        GridLayout glayout = new GridLayout( 2, false );
        client.setLayout( glayout );
        section.setClient( client );

        // Enabled Checkbox
        enabledCheckbox = toolkit.createButton( client, "Enabled", SWT.CHECK );
        enabledCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // ID Text
        toolkit.createLabel( client, "ID:" );
        idText = toolkit.createText( client, "" );
        idText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Description Text
        toolkit.createLabel( client, "Description:" );
        descriptionText = toolkit.createText( client, "" );
        descriptionText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Building The Password Quality Zone ────────────────────────────────────────────────
    // The quality zone is where the Empire's password standards are enforced: how strictly
    // should quality be checked, what validator runs, and what are the min/max length limits?
    // Disabling quality checking greys out the length fields — no point configuring them.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Quality" section of the details panel, covering the check-quality level
     * (disabled / relaxed / strict), a custom validator class name, and optional minimum
     * and maximum password-length constraints.
     * When quality checking is disabled, the length fields are automatically greyed out.
     *
     * <p>For example — an Imperial quality inspector configures acceptable password strength:</p>
     * <pre>
     *   She sets quality to "Strict", points to a custom validator, and requires 8–20 chars.
     *   If she switches quality to "Disabled" the length fields dim — they'd be ignored anyway.
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the parent composite
     */
    private void createQualitySection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Quality" );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 2, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Check Quality (pwdCheckQuality)
        toolkit.createLabel( composite, "Check Quality:" );
        checkQualityComboViewer = new ComboViewer( composite );
        checkQualityComboViewer.setContentProvider( new ArrayContentProvider() );
        checkQualityComboViewer.setInput( new CheckQuality[]
            { CheckQuality.DISABLED, CheckQuality.RELAXED, CheckQuality.STRICT } );
        checkQualityComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Validator
        toolkit.createLabel( composite, "Validator:" );
        validatorText = toolkit.createText( composite, "" );
        validatorText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Minimum Length (pwdMinLength)
        minimumLengthCheckbox = toolkit.createButton( composite, "Enable Minimum Length", SWT.CHECK );
        minimumLengthCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );
        Composite minimumLengthRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Number of characters:" );
        minimumLengthText = toolkit.createText( minimumLengthRadioIndentComposite, "" );
        minimumLengthText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Maximum Length (pwdMaxLength)
        maximumLengthCheckbox = toolkit.createButton( composite, "Enable Maximum Length", SWT.CHECK );
        maximumLengthCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );
        Composite maximumLengthRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Number of characters:" );
        maximumLengthText = toolkit.createText( maximumLengthRadioIndentComposite, "" );
        maximumLengthText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── Building The Expiration Zone ──────────────────────────────────────────────────────
    // Password expiration is like the Empire's clearance badge renewal cycle — badges
    // expire after a maximum age, can't be changed too soon (minimum age), and owners get
    // a warning before expiry plus a grace window after it.
    // This section builds all those time-based controls.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Expiration" section covering minimum and maximum password age, an
     * optional expiry-warning period, an optional grace authentication limit, and an
     * optional grace-expire interval.
     * Controls whose checkbox is unchecked are greyed out automatically.
     *
     * <p>For example — an Imperial timekeeper sets the clearance-badge renewal schedule:</p>
     * <pre>
     *   Badges can't be renewed until 86400 seconds have passed (min age).
     *   They expire after 2592000 seconds (max age = 30 days).
     *   Officers get a 600-second warning before expiry and 5 grace logins after.
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the parent composite
     */
    private void createExpirationSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Expiration" );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 2, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Minimum Age (pwdMinAge)
        toolkit.createLabel( composite, "Minimum Age (seconds):" );
        minimumAgeText = toolkit.createText( composite, "" );
        minimumAgeText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Maximum Age (pwdMaxAge)
        toolkit.createLabel( composite, "Maximum Age (seconds):" );
        maximumAgeText = toolkit.createText( composite, "" );
        maximumAgeText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Expire Warning (pwdExpireWarning)
        expireWarningCheckbox = toolkit.createButton( composite, "Enable Expire Warning", SWT.CHECK );
        expireWarningCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 3, 1 ) );
        Composite expireWarningRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Number of seconds:" );
        expireWarningText = toolkit.createText( expireWarningRadioIndentComposite, "" );
        expireWarningText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Grace Authentication Limit (pwdGraceAuthNLimit)
        graceAuthenticationLimitCheckbox = toolkit.createButton( composite, "Enable Grace Authentication Limit",
            SWT.CHECK );
        graceAuthenticationLimitCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 3, 1 ) );
        Composite graceAuthenticationLimitRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Number of times:" );
        graceAuthenticationLimitText = toolkit.createText( graceAuthenticationLimitRadioIndentComposite, "" );
        graceAuthenticationLimitText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Grace Expire (pwdGraceExpire)
        graceExpireCheckbox = toolkit.createButton( composite, "Enable Grace Expire", SWT.CHECK );
        graceExpireCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 3, 1 ) );
        Composite graceExpireRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Interval (seconds):" );
        graceExpireText = toolkit.createText( graceExpireRadioIndentComposite, "" );
        graceExpireText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── Building The Behaviour Options Zone ───────────────────────────────────────────────
    // This zone is like the Empire's policy handbook annex — three quick on/off toggles
    // that govern how users interact with their own passwords: are they forced to change
    // on first login, can they change at will, and must they supply the old password first?
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Options" section with three boolean flags: "Must Change" (force a change
     * on next login), "Allow User Change" (let users change their own password), and
     * "Safe Modify" (require the current password when changing).
     *
     * <p>For example — the Empire's handbook annex defines self-service password rules:</p>
     * <pre>
     *   New recruits must change their password on first login (Must Change = on).
     *   Veterans can change theirs any time (Allow User Change = on).
     *   Everyone must prove they know the old one first (Safe Modify = on).
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the parent composite
     */
    private void createOptionsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Options" );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 2, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Must Change (pwdMustChange)
        mustChangeCheckbox = toolkit.createButton( composite, "Enable Must Change", SWT.CHECK );
        mustChangeCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );

        // Allow User Change (pwdAllowUserChange)
        allowUserChangeCheckbox = toolkit.createButton( composite, "Enable Allow User Change", SWT.CHECK );
        allowUserChangeCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );

        // Safe Modify (pwdSafeModify)
        safeModifyCheckbox = toolkit.createButton( composite, "Enable Safe Modify", SWT.CHECK );
        safeModifyCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );
    }


    // ── Building The Lockout Zone ──────────────────────────────────────────────────────────
    // The lockout zone is the Empire's brig — too many failed login attempts and the account
    // goes into lockout for a configurable duration.  We also configure history (can't reuse
    // old passwords), idle timeout, and login-attempt delays.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Lockout" section covering account lockout (duration, max failures, failure
     * count window), maximum idle time, password history depth, and minimum/maximum login-retry
     * delays.
     * This is the most widget-dense section — it governs what happens when authentication
     * goes wrong repeatedly.
     *
     * <p>For example — the Empire's brig administrator sets the detention schedule:</p>
     * <pre>
     *   Five consecutive failures lock the account for 0 seconds (permanent until admin reset).
     *   The failure counter resets after 30 seconds of inactivity.
     *   The last 5 passwords are remembered — recycling old ones is treason.
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the parent composite
     */
    private void createLockoutSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Lockout" );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 2, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Lockout (pwdLockout)
        lockoutCheckbox = toolkit.createButton( composite, "Enable Lockout", SWT.CHECK );
        lockoutCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );

        // Lockout Duration (pwdLockoutDuration)
        toolkit.createLabel( composite, "Lockout Duration (seconds):" );
        lockoutDurationText = toolkit.createText( composite, "" );
        lockoutDurationText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Max Failure (pwdMaxFailure)
        toolkit.createLabel( composite, "Maximum Consecutive Failures (count):" );
        maxFailureText = toolkit.createText( composite, "" );
        maxFailureText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Failure Count Interval (pwdFailureCountInterval)
        toolkit.createLabel( composite, "Failure Count Interval (seconds):" );
        failureCountIntervalText = toolkit.createText( composite, "" );
        failureCountIntervalText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Max Idle (pwdMaxIdle)
        maxIdleCheckbox = toolkit.createButton( composite, "Enable Maximum Idle", SWT.CHECK );
        maxIdleCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 3, 1 ) );
        Composite maxIdleCheckboxRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Interval (seconds):" );
        maxIdleText = toolkit.createText( maxIdleCheckboxRadioIndentComposite, "" );
        maxIdleText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // In History (pwdInHistory)
        inHistoryCheckbox = toolkit.createButton( composite, "Enable In History", SWT.CHECK );
        inHistoryCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );
        Composite inHistoryRadioIndentComposite = createRadioIndentComposite( toolkit, composite,
            "Used passwords stored in history:" );
        inHistoryText = toolkit.createText( inHistoryRadioIndentComposite, "" );
        inHistoryText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Minimum delay (pwdMinDelay)
        toolkit.createLabel( composite, "Minimum Delay (seconds):" );
        minimumDelayText = toolkit.createText( composite, "" );
        minimumDelayText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // Maximum Delay (pwdMaxDelay)
        toolkit.createLabel( composite, "Maximum Delay (seconds):" );
        maximumDelayText = toolkit.createText( composite, "" );
        maximumDelayText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── Building An Indented Sub-Control Row ──────────────────────────────────────────────
    // When a checkbox enables an optional numeric field, we indent the field visually to
    // show that it belongs to the checkbox — like a sub-item in an Imperial form.
    // This helper creates that indented three-column composite with a spacer and a label.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a small indented composite that holds a spacer, a label, and space for a
     * caller-supplied text field — used to visually subordinate numeric inputs under their
     * controlling checkbox.
     * Returns the composite so the caller can add its text widget into it.
     *
     * <p>For example — an Imperial form designer indents the "Number of characters" field:</p>
     * <pre>
     *   [X] Enable Minimum Length
     *       Number of characters: [____]   ← this indented row is what we build here
     * </pre>
     *
     * @param toolkit  the Eclipse Forms toolkit
     * @param parent   the composite to attach the indented row to
     * @param text     the label text to show next to the indented control
     * @return         the indented composite (caller appends the actual input widget)
     */
    private Composite createRadioIndentComposite( FormToolkit toolkit, Composite parent, String text )
    {
        Composite composite = toolkit.createComposite( parent );
        GridLayout gridLayout = new GridLayout( 3, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        toolkit.createLabel( composite, "   " );
        toolkit.createLabel( composite, text );

        return composite;
    }


    // ── Arming Every Control On The Briefing Panel ────────────────────────────────────────
    // The Imperial technician runs a cable from every widget on the panel to the central
    // commit-and-dirty pipeline, so that any change — a keystroke, a checkbox tick, a
    // combo selection — immediately feeds back into the config model and lights up the
    // editor's save button.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all the event listeners (modify, selection, verify) to every widget on the
     * details panel.
     * Called at the end of {@link #refresh()} after we've populated the widgets from the
     * model, so listeners don't fire spuriously during the population phase.
     *
     * <p>For example — the technician wires every panel control to the command pipeline:</p>
     * <pre>
     *   Every text field gets a ModifyListener that commits and marks dirty on each keystroke.
     *   Every checkbox gets a SelectionListener that does the same on toggle.
     *   Numeric fields also get a VerifyListener that blocks non-digit input.
     * </pre>
     */
    private void addListeners()
    {
        enabledCheckbox.addSelectionListener( buttonSelectionListener );
        idText.addModifyListener( textModifyListener );
        descriptionText.addModifyListener( textModifyListener );
        checkQualityComboViewer.addSelectionChangedListener( viewerSelectionChangedListener );
        checkQualityComboViewer.addSelectionChangedListener( checkQualityComboViewerSelectionChangedListener );
        validatorText.addModifyListener( textModifyListener );
        minimumLengthCheckbox.addSelectionListener( buttonSelectionListener );
        minimumLengthCheckbox.addSelectionListener( minimumLengthCheckboxSelectionListener );
        minimumLengthText.addModifyListener( textModifyListener );
        minimumLengthText.addVerifyListener( integerVerifyListener );
        maximumLengthCheckbox.addSelectionListener( buttonSelectionListener );
        maximumLengthCheckbox.addSelectionListener( maximumLengthCheckboxSelectionListener );
        maximumLengthText.addModifyListener( textModifyListener );
        maximumLengthText.addVerifyListener( integerVerifyListener );
        minimumAgeText.addModifyListener( textModifyListener );
        minimumAgeText.addVerifyListener( integerVerifyListener );
        maximumAgeText.addModifyListener( textModifyListener );
        maximumAgeText.addVerifyListener( integerVerifyListener );
        expireWarningCheckbox.addSelectionListener( buttonSelectionListener );
        expireWarningCheckbox.addSelectionListener( expireWarningCheckboxSelectionListener );
        expireWarningText.addModifyListener( textModifyListener );
        expireWarningText.addVerifyListener( integerVerifyListener );
        graceAuthenticationLimitCheckbox.addSelectionListener( buttonSelectionListener );
        graceAuthenticationLimitCheckbox.addSelectionListener( graceAuthenticationLimitCheckboxSelectionListener );
        graceAuthenticationLimitText.addModifyListener( textModifyListener );
        graceAuthenticationLimitText.addVerifyListener( integerVerifyListener );
        graceExpireCheckbox.addSelectionListener( buttonSelectionListener );
        graceExpireCheckbox.addSelectionListener( graceExpireCheckboxSelectionListener );
        graceExpireText.addModifyListener( textModifyListener );
        graceExpireText.addVerifyListener( integerVerifyListener );
        mustChangeCheckbox.addSelectionListener( buttonSelectionListener );
        allowUserChangeCheckbox.addSelectionListener( buttonSelectionListener );
        safeModifyCheckbox.addSelectionListener( buttonSelectionListener );
        lockoutCheckbox.addSelectionListener( buttonSelectionListener );
        lockoutDurationText.addModifyListener( textModifyListener );
        lockoutDurationText.addVerifyListener( integerVerifyListener );
        maxFailureText.addModifyListener( textModifyListener );
        maxFailureText.addVerifyListener( integerVerifyListener );
        failureCountIntervalText.addModifyListener( textModifyListener );
        failureCountIntervalText.addVerifyListener( integerVerifyListener );
        maxIdleCheckbox.addSelectionListener( buttonSelectionListener );
        maxIdleCheckbox.addSelectionListener( maxIdleCheckboxSelectionListener );
        maxIdleText.addModifyListener( textModifyListener );
        maxIdleText.addVerifyListener( integerVerifyListener );
        inHistoryCheckbox.addSelectionListener( buttonSelectionListener );
        inHistoryCheckbox.addSelectionListener( inHistoryCheckboxSelectionListener );
        inHistoryText.addModifyListener( textModifyListener );
        inHistoryText.addVerifyListener( integerVerifyListener );
        minimumDelayText.addModifyListener( textModifyListener );
        minimumDelayText.addVerifyListener( integerVerifyListener );
        maximumDelayText.addModifyListener( textModifyListener );
        maximumDelayText.addVerifyListener( integerVerifyListener );
    }


    // ── Disarming Every Control Before A Data Reload ──────────────────────────────────────
    // Before we repopulate the widgets from a newly selected policy, we cut all the listener
    // wires — otherwise every setText() and setSelection() call would trigger a commit,
    // writing half-formed data back into the model before we've finished loading.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all event listeners from every widget on the panel.
     * Must be called at the start of {@link #refresh()} before we push new model data into
     * the widgets — otherwise the modify/selection listeners would fire on every
     * {@code setText()} and incorrectly commit partial data back to the bean.
     *
     * <p>For example — the technician cuts all cables before swapping the briefing dossier:</p>
     * <pre>
     *   She disconnects the commit pipeline so that loading new values into the widgets
     *   doesn't accidentally write those same values back into the model mid-load.
     *   Once loading is done, addListeners() re-arms everything.
     * </pre>
     */
    private void removeListeners()
    {
        enabledCheckbox.removeSelectionListener( buttonSelectionListener );
        idText.removeModifyListener( textModifyListener );
        descriptionText.removeModifyListener( textModifyListener );
        checkQualityComboViewer.removeSelectionChangedListener( viewerSelectionChangedListener );
        checkQualityComboViewer.removeSelectionChangedListener( checkQualityComboViewerSelectionChangedListener );
        validatorText.removeModifyListener( textModifyListener );
        minimumLengthCheckbox.removeSelectionListener( buttonSelectionListener );
        minimumLengthCheckbox.removeSelectionListener( minimumLengthCheckboxSelectionListener );
        minimumLengthText.removeModifyListener( textModifyListener );
        minimumLengthText.removeVerifyListener( integerVerifyListener );
        maximumLengthCheckbox.removeSelectionListener( buttonSelectionListener );
        maximumLengthCheckbox.removeSelectionListener( maximumLengthCheckboxSelectionListener );
        maximumLengthText.removeModifyListener( textModifyListener );
        maximumLengthText.removeVerifyListener( integerVerifyListener );
        minimumAgeText.removeModifyListener( textModifyListener );
        minimumAgeText.removeVerifyListener( integerVerifyListener );
        maximumAgeText.removeModifyListener( textModifyListener );
        maximumAgeText.removeVerifyListener( integerVerifyListener );
        expireWarningCheckbox.removeSelectionListener( buttonSelectionListener );
        expireWarningCheckbox.removeSelectionListener( expireWarningCheckboxSelectionListener );
        expireWarningText.removeModifyListener( textModifyListener );
        expireWarningText.removeVerifyListener( integerVerifyListener );
        graceAuthenticationLimitCheckbox.removeSelectionListener( buttonSelectionListener );
        graceAuthenticationLimitCheckbox.removeSelectionListener( graceAuthenticationLimitCheckboxSelectionListener );
        graceAuthenticationLimitText.removeModifyListener( textModifyListener );
        graceAuthenticationLimitText.removeVerifyListener( integerVerifyListener );
        graceExpireCheckbox.removeSelectionListener( buttonSelectionListener );
        graceExpireCheckbox.removeSelectionListener( graceExpireCheckboxSelectionListener );
        graceExpireText.removeModifyListener( textModifyListener );
        graceExpireText.removeVerifyListener( integerVerifyListener );
        mustChangeCheckbox.removeSelectionListener( buttonSelectionListener );
        allowUserChangeCheckbox.removeSelectionListener( buttonSelectionListener );
        safeModifyCheckbox.removeSelectionListener( buttonSelectionListener );
        lockoutCheckbox.removeSelectionListener( buttonSelectionListener );
        lockoutDurationText.removeModifyListener( textModifyListener );
        lockoutDurationText.removeVerifyListener( integerVerifyListener );
        maxFailureText.removeModifyListener( textModifyListener );
        maxFailureText.removeVerifyListener( integerVerifyListener );
        failureCountIntervalText.removeModifyListener( textModifyListener );
        failureCountIntervalText.removeVerifyListener( integerVerifyListener );
        maxIdleCheckbox.removeSelectionListener( buttonSelectionListener );
        maxIdleCheckbox.removeSelectionListener( maxIdleCheckboxSelectionListener );
        maxIdleText.removeModifyListener( textModifyListener );
        maxIdleText.removeVerifyListener( integerVerifyListener );
        inHistoryCheckbox.removeSelectionListener( buttonSelectionListener );
        inHistoryCheckbox.removeSelectionListener( inHistoryCheckboxSelectionListener );
        inHistoryText.removeModifyListener( textModifyListener );
        inHistoryText.removeVerifyListener( integerVerifyListener );
        minimumDelayText.removeModifyListener( textModifyListener );
        minimumDelayText.removeVerifyListener( integerVerifyListener );
        maximumDelayText.removeModifyListener( textModifyListener );
        maximumDelayText.removeVerifyListener( integerVerifyListener );
    }


    // ── Receiving A New Policy Selection ──────────────────────────────────────────────────
    // The officer on the master roster taps a different policy row and the briefing panel
    // gets the news: "here is the new dossier, get ready to display it."
    // We extract the bean from the selection and call refresh() to repaint the panel.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the Eclipse forms framework when the master-list selection changes.
     * We extract the newly selected {@link PasswordPolicyBean} (or set it to {@code null}
     * for an empty/multi selection), then call {@link #refresh()} to repopulate the panel.
     *
     * <p>For example — the officer swaps the open dossier on her station:</p>
     * <pre>
     *   She clicks "strictPolicy" on the roster; the selection event fires.
     *   We pull out the strictPolicy bean and refresh the panel to show its fields.
     *   If nothing is selected we clear the panel by setting the bean to null.
     * </pre>
     *
     * @param part       the form part that fired the event (unused here)
     * @param selection  the new selection from the master table viewer
     */
    public void selectionChanged( IFormPart part, ISelection selection )
    {
        IStructuredSelection ssel = ( IStructuredSelection ) selection;
        if ( ssel.size() == 1 )
        {
            passwordPolicy = ( PasswordPolicyBean ) ssel.getFirstElement();
        }
        else
        {
            passwordPolicy = null;
        }
        refresh();
    }


    // ── Flushing The Panel Values Back Into The Model ─────────────────────────────────────
    // The officer finishes editing and presses Save — at that moment every value shown on
    // screen needs to be written back into the underlying PasswordPolicyBean so the config
    // model reflects what the user actually typed.
    // We read each widget and push its value to the bean, guarding against parse failures.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads every widget on the panel and pushes its current value into the in-memory
     * {@link PasswordPolicyBean}.
     * Called on every user interaction (via the modify/selection listeners) so the model
     * stays in sync with the UI at all times, not just at explicit save.
     *
     * <p>For example — the officer logs every field from the briefing panel into the dossier:</p>
     * <pre>
     *   She reads the Enabled checkbox, the ID text, the quality combo, the lockout duration...
     *   Each value goes straight into the corresponding setter on the policy bean.
     *   Unparseable numeric fields safely default to 0 rather than crashing.
     * </pre>
     *
     * @param onSave  {@code true} when called as part of an explicit editor save operation,
     *                {@code false} for live incremental updates
     */
    public void commit( boolean onSave )
    {
        if ( passwordPolicy != null )
        {
            // Enabled
            passwordPolicy.setEnabled( enabledCheckbox.getSelection() );

            // ID
            passwordPolicy.setPwdId( ServerConfigurationEditorUtils.checkEmptyString( idText.getText() ) );

            // Description
            passwordPolicy
                .setDescription( ServerConfigurationEditorUtils.checkEmptyString( descriptionText.getText() ) );

            // Check Quality
            passwordPolicy.setPwdCheckQuality( getPwdCheckQuality() );

            // Validator
            passwordPolicy
                .setPwdValidator( ServerConfigurationEditorUtils.checkEmptyString( validatorText.getText() ) );

            // Mininum Length
            if ( minimumLengthCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdMinLength( Integer.parseInt( minimumLengthText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdMinLength( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdMinLength( 0 );
            }

            // Maximum Length
            if ( maximumLengthCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdMaxLength( Integer.parseInt( maximumLengthText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdMaxLength( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdMaxLength( 0 );
            }

            // Minimum Age
            try
            {
                passwordPolicy.setPwdMinAge( Integer.parseInt( minimumAgeText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdMinAge( 0 );
            }

            // Maximum Age
            try
            {
                passwordPolicy.setPwdMaxAge( Integer.parseInt( maximumAgeText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdMaxAge( 0 );
            }

            // Expire Warning
            if ( expireWarningCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdExpireWarning( Integer.parseInt( expireWarningText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdExpireWarning( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdExpireWarning( 0 );
            }

            // Grace Authentication Limit
            if ( graceAuthenticationLimitCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdGraceAuthNLimit( Integer.parseInt( graceAuthenticationLimitText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdGraceAuthNLimit( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdGraceAuthNLimit( 0 );
            }

            // Grace Expire
            if ( graceExpireCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdGraceExpire( Integer.parseInt( graceExpireText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdGraceExpire( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdGraceExpire( 0 );
            }

            // Must Change
            passwordPolicy.setPwdMustChange( mustChangeCheckbox.getSelection() );

            // Allow User Change
            passwordPolicy.setPwdAllowUserChange( allowUserChangeCheckbox.getSelection() );

            // Safe Modify
            passwordPolicy.setPwdSafeModify( safeModifyCheckbox.getSelection() );

            // Lockout
            passwordPolicy.setPwdLockout( lockoutCheckbox.getSelection() );

            // Lockout Duration
            try
            {
                passwordPolicy.setPwdLockoutDuration( Integer.parseInt( lockoutDurationText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdLockoutDuration( 0 );
            }

            // Max Failure
            try
            {
                passwordPolicy.setPwdMaxFailure( Integer.parseInt( maxFailureText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdMaxFailure( 0 );
            }

            // Failure Count Interval
            try
            {
                passwordPolicy.setPwdFailureCountInterval( Integer.parseInt( failureCountIntervalText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdFailureCountInterval( 0 );
            }

            // Max Idle
            if ( maxIdleCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdMaxIdle( Integer.parseInt( maxIdleText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdMaxIdle( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdMaxIdle( 0 );
            }

            // In History
            if ( inHistoryCheckbox.getSelection() )
            {
                try
                {
                    passwordPolicy.setPwdInHistory( Integer.parseInt( inHistoryText.getText() ) );
                }
                catch ( NumberFormatException e )
                {
                    passwordPolicy.setPwdInHistory( 0 );
                }
            }
            else
            {
                passwordPolicy.setPwdInHistory( 0 );
            }

            // Minimum Delay
            try
            {
                passwordPolicy.setPwdMinDelay( Integer.parseInt( minimumDelayText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdMinDelay( 0 );
            }

            // Maximum Delay
            try
            {
                passwordPolicy.setPwdMaxDelay( Integer.parseInt( maximumDelayText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                passwordPolicy.setPwdMaxDelay( 0 );
            }
        }
    }


    // ── Reading The Quality Combo Selection ───────────────────────────────────────────────
    // The combo viewer holds DISABLED / RELAXED / STRICT; we translate the currently
    // selected enum constant into its integer representation (0, 1, or 2) for the bean.
    // If nothing is selected we default to DISABLED (0) — the safest fallback.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current selection from the check-quality combo and returns its integer value.
     * Returns {@link CheckQuality#DISABLED} (0) if the combo has no selection — this
     * prevents a null-pointer situation during commit.
     *
     * <p>For example — the officer checks which quality level the combo is dialled to:</p>
     * <pre>
     *   Combo shows "Strict" → returns 2.
     *   Combo shows "Relaxed" → returns 1.
     *   Combo is empty (shouldn't happen, but) → returns 0 (Disabled).
     * </pre>
     *
     * @return  the integer value of the currently selected {@link CheckQuality} (0, 1, or 2)
     */
    private int getPwdCheckQuality()
    {
        IStructuredSelection selection = ( StructuredSelection ) checkQualityComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            CheckQuality checkQuality = ( CheckQuality ) selection.getFirstElement();

            return checkQuality.getValue();
        }

        return CheckQuality.DISABLED.getValue();
    }


    // ── Cleaning Up When The Panel Closes ─────────────────────────────────────────────────
    // When the officer's station is decommissioned, it shuts itself down cleanly.
    // We have nothing to dispose of in this implementation, but the interface requires it.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Lifecycle method called when this details page is disposed.
     * We have no resources to clean up here, so this is intentionally empty.
     *
     * @see IDetailsPage#dispose()
     */
    public void dispose()
    {
    }


    // ── Registering The Managed Form ──────────────────────────────────────────────────────
    // Before any widgets can be built, the Eclipse forms framework hands us the managed
    // form that owns the toolkit and lifecycle.  We store it for use in createContents().
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the {@link IManagedForm} reference so we can access the toolkit when building
     * the panel's widgets in {@link #createContents(Composite)}.
     * Eclipse calls this before calling {@code createContents}, so the form is always
     * available by the time we need it.
     *
     * @param form  the managed form that owns this details page
     */
    public void initialize( IManagedForm form )
    {
        this.mform = form;
    }


    // ── Reporting Dirty State ─────────────────────────────────────────────────────────────
    // The panel itself never reports dirty — it delegates that responsibility to the master
    // block's setEditorDirty() call, which updates the top-level editor's save state.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} because this details page does not maintain its own dirty
     * state — dirtiness is tracked at the editor level via {@code setEditorDirty()}.
     *
     * @return  {@code false} always
     */
    public boolean isDirty()
    {
        return false;
    }


    // ── Reporting Staleness ───────────────────────────────────────────────────────────────
    // The panel doesn't track whether it has fallen out of sync with the model independently;
    // the master/details framework handles that through selection change events.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} because staleness is handled by the selection-change flow
     * rather than by this page tracking model version numbers.
     *
     * @return  {@code false} always
     */
    public boolean isStale()
    {
        return false;
    }


    // ── Repainting The Briefing Panel ─────────────────────────────────────────────────────
    // The officer opens a new policy dossier and the briefing panel repaints to show its
    // contents: first cutting the listener cables, then loading every field from the bean,
    // then re-arming the cables so future edits are captured.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all widgets from the currently selected {@link PasswordPolicyBean}.
     * The sequence is always: remove listeners → populate widgets → add listeners, to
     * avoid spurious commits during the population phase.
     * If the current policy is the default one, the ID and Description fields are disabled.
     *
     * <p>For example — the officer opens a freshly selected clearance dossier:</p>
     * <pre>
     *   She cuts the commit pipeline, then reads each field from the bean into the widget.
     *   Numeric fields with value 0 show checkboxes as unchecked (feature = disabled).
     *   When done she re-arms the pipeline so future edits flow back to the model.
     * </pre>
     */
    public void refresh()
    {
        removeListeners();

        if ( passwordPolicy != null )
        {
            // Checking if this is the default password policy
            boolean isDefaultPasswordPolicy = PasswordPoliciesPage.isDefaultPasswordPolicy( passwordPolicy );

            // Enabled
            enabledCheckbox.setSelection( passwordPolicy.isEnabled() );

            // ID
            idText.setText( ServerConfigurationEditorUtils.checkNull( passwordPolicy.getPwdId() ) );
            idText.setEnabled( !isDefaultPasswordPolicy );

            // Description
            descriptionText.setText( ServerConfigurationEditorUtils.checkNull( passwordPolicy.getDescription() ) );
            descriptionText.setEnabled( !isDefaultPasswordPolicy );

            // Check Quality
            checkQualityComboViewer.setSelection( new StructuredSelection( CheckQuality.valueOf( passwordPolicy
                .getPwdCheckQuality() ) ) );

            // Validator
            validatorText.setText( ServerConfigurationEditorUtils.checkNull( passwordPolicy.getPwdValidator() ) );

            // Mininum Length
            int minimumLength = passwordPolicy.getPwdMinLength();
            minimumLengthCheckbox.setSelection( minimumLength != 0 );
            minimumLengthText.setText( "" + minimumLength );

            // Maximum Length
            int maximumLength = passwordPolicy.getPwdMaxLength();
            maximumLengthCheckbox.setSelection( maximumLength != 0 );
            maximumLengthText.setText( "" + maximumLength );

            if ( getPwdCheckQuality() == 0 )
            {
                minimumLengthCheckbox.setEnabled( false );
                minimumLengthText.setEnabled( false );
                maximumLengthCheckbox.setEnabled( false );
                maximumLengthText.setEnabled( false );
            }
            else
            {
                minimumLengthCheckbox.setEnabled( true );
                minimumLengthText.setEnabled( minimumLength != 0 );
                maximumLengthCheckbox.setEnabled( true );
                maximumLengthText.setEnabled( maximumLength != 0 );
            }

            // Minimum Age
            minimumAgeText.setText( "" + passwordPolicy.getPwdMinAge() );

            // Maximum Age
            maximumAgeText.setText( "" + passwordPolicy.getPwdMaxAge() );

            // Expire Warning
            int expireWarning = passwordPolicy.getPwdExpireWarning();
            expireWarningCheckbox.setSelection( expireWarning != 0 );
            expireWarningText.setText( "" + expireWarning );
            expireWarningText.setEnabled( expireWarning != 0 );

            // Grace Authentication Limit
            int graceAuthenticationLimit = passwordPolicy.getPwdGraceAuthNLimit();
            graceAuthenticationLimitCheckbox.setSelection( graceAuthenticationLimit != 0 );
            graceAuthenticationLimitText.setText( "" + graceAuthenticationLimit );
            graceAuthenticationLimitText.setEnabled( graceAuthenticationLimit != 0 );

            // Grace Expire
            int graceExpire = passwordPolicy.getPwdGraceExpire();
            graceExpireCheckbox.setSelection( graceExpire != 0 );
            graceExpireText.setText( "" + graceExpire );
            graceExpireText.setEnabled( graceExpire != 0 );

            // Must Change
            mustChangeCheckbox.setSelection( passwordPolicy.isPwdMustChange() );

            // Allow User Change
            allowUserChangeCheckbox.setSelection( passwordPolicy.isPwdAllowUserChange() );

            // Safe Modify
            safeModifyCheckbox.setSelection( passwordPolicy.isPwdSafeModify() );

            // Lockout
            lockoutCheckbox.setSelection( passwordPolicy.isPwdLockout() );

            // Lockout Duration
            lockoutDurationText.setText( "" + passwordPolicy.getPwdLockoutDuration() );

            // Max Failure
            maxFailureText.setText( "" + passwordPolicy.getPwdMaxFailure() );

            // Failure Count Interval
            failureCountIntervalText.setText( "" + passwordPolicy.getPwdFailureCountInterval() );

            // Max Idle
            int maxIdle = passwordPolicy.getPwdMaxIdle();
            maxIdleCheckbox.setSelection( maxIdle != 0 );
            maxIdleText.setText( "" + maxIdle );
            maxIdleText.setEnabled( maxIdle != 0 );

            // In History
            int inHistory = passwordPolicy.getPwdInHistory();
            inHistoryCheckbox.setSelection( inHistory != 0 );
            inHistoryText.setText( "" + inHistory );
            inHistoryText.setEnabled( inHistory != 0 );

            // Minimum Delay
            minimumDelayText.setText( "" + passwordPolicy.getPwdMinDelay() );

            // Maximum Delay
            maximumDelayText.setText( "" + passwordPolicy.getPwdMaxDelay() );
        }

        addListeners();
    }


    // ── Directing Keyboard Focus ───────────────────────────────────────────────────────────
    // When the panel becomes active, keyboard focus should land somewhere sensible so the
    // officer can start typing immediately — currently this is a no-op placeholder.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets keyboard focus when this details page becomes active.
     * Currently a no-op — the commented-out call shows the original intent was to focus
     * the ID text field.
     */
    public void setFocus()
    {
        //        idText.setFocus();
    }


    // ── Refusing External Form Input ──────────────────────────────────────────────────────
    // If someone tries to push an external object into this form panel, we politely decline.
    // The panel only cares about the selection coming from its own master list.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} because this details page does not accept externally
     * pushed form input — it only reacts to master-list selections.
     *
     * @param input  the input object being offered (ignored)
     * @return       {@code false} always
     */
    public boolean setFormInput( Object input )
    {
        return false;
    }

    // ── CLASS: CheckQuality — THE IMPERIAL QUALITY CLEARANCE LEVEL ────────────────────────
    // Vader's security division uses three tiers for password quality enforcement: off,
    // relaxed (check but accept unknown), and strict (reject anything that fails).
    // This enum maps those three tiers to the integer values the LDAP ppolicy attribute
    // pwdCheckQuality actually stores in the directory.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Enum representing the three possible values of the LDAP {@code pwdCheckQuality} attribute.
     * Each constant wraps the integer value that ApacheDS stores in the config XML, and
     * provides a friendly display string for the combo viewer.
     * Think of this enum as Vader's three-tier quality enforcement scale: off, lenient, or
     * ruthlessly strict.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private enum CheckQuality
    {
        DISABLED(0),
        RELAXED(1),
        STRICT(2);

        /** The value */
        private int value;


        // ── Storing The Quality Integer Value ─────────────────────────────────────────────
        // Each tier is backed by an integer (0, 1, 2) that gets persisted to the config file.
        // The constructor just stashes that integer for later retrieval.
        // ─────────────────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@link CheckQuality} constant backed by the given integer value.
         *
         * <p>For example — the Empire stamps a tier number on each quality level badge:</p>
         * <pre>
         *   DISABLED gets stamp "0", RELAXED "1", STRICT "2".
         * </pre>
         *
         * @param value  the integer value as stored in the ApacheDS config file
         */
        private CheckQuality( int value )
        {
            this.value = value;
        }


        // ── Exposing The Integer Value ────────────────────────────────────────────────────
        // The commit method needs the integer to push into the bean; this getter exposes it.
        // ─────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns the integer representation of this quality level for use in the
         * {@link PasswordPolicyBean#setPwdCheckQuality(int)} setter.
         *
         * @return  0 for DISABLED, 1 for RELAXED, 2 for STRICT
         */
        public int getValue()
        {
            return value;
        }


        // ── Looking Up A Quality Constant By Integer ──────────────────────────────────────
        // During refresh() we read an integer from the bean and need to find the matching
        // enum constant so we can set the combo selection correctly.
        // We scan all constants and throw if nothing matches — an unknown integer is a bug.
        // ─────────────────────────────────────────────────────────────────────────────────
        /**
         * Looks up the {@link CheckQuality} constant whose integer value matches {@code value}.
         * Throws {@link IllegalArgumentException} if no constant matches — this indicates a
         * corrupt or unknown config value.
         *
         * <p>For example — the officer looks up which quality tier corresponds to value 2:</p>
         * <pre>
         *   She scans the tier register; value 2 matches STRICT.
         *   An unrecognised value triggers an alarm — something in the config is wrong.
         * </pre>
         *
         * @param value  the integer to look up (expected: 0, 1, or 2)
         * @return       the matching {@link CheckQuality} constant
         * @throws IllegalArgumentException  if no constant has that integer value
         */
        public static CheckQuality valueOf( int value )
        {
            for ( CheckQuality checkQuality : CheckQuality.class.getEnumConstants() )
            {
                if ( checkQuality.getValue() == value )
                {
                    return checkQuality;
                }
            }

            throw new IllegalArgumentException( "There is no CheckQuality value for :" + value );
        }


        // ── Producing A Human-Readable Name ──────────────────────────────────────────────
        // The combo viewer calls toString() to show a label for each constant.
        // We return a capitalised plain-English name rather than the raw enum identifier.
        // ─────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns a user-friendly display name for the quality level, suitable for showing
         * in the combo viewer dropdown.
         *
         * @return  "Disabled", "Relaxed", or "Strict"
         */
        public String toString()
        {
            switch ( this )
            {
                case DISABLED:
                    return "Disabled";
                case RELAXED:
                    return "Relaxed";
                case STRICT:
                    return "Strict";
            }

            return super.toString();
        }
    }
}
