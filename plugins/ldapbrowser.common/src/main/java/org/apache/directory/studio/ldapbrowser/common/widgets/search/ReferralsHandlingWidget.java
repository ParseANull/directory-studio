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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: ReferralsHandlingWidget — R2 Following the Referral Path to the Next Junction ──
// In A New Hope, while navigating the Death Star's computer network, R2-D2 sometimes
// hits a node that says "what you need is not here — follow this pointer to junction 7G."
// In LDAP those are called referrals. R2 has to decide: follow the pointer automatically
// and keep going without interrupting the crew; stop and ask a Rebel officer to manually
// decide whether to follow; or just ignore the pointer entirely and move on.
// This widget presents those three choices as radio buttons.
// ────────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for choosing how the LDAP client should respond when the directory
 * server returns a referral — a pointer to another server or subtree. Think of a
 * referral like a road sign that says "what you want is at junction 7G."
 * Think of this class as R2-D2 at a network junction choosing how to handle that sign.
 *
 * <p>The panel contains a labeled group with up to three radio buttons:</p>
 * <ul>
 *   <li><b>Follow manually</b> — pause and ask the user (optional, hidden in some contexts)</li>
 *   <li><b>Follow automatically</b> — chase the referral without interrupting the user</li>
 *   <li><b>Ignore</b> — treat the referral as if it weren't there and skip it</li>
 * </ul>
 * Used by {@link SearchPageWrapper} in the options section of the search form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReferralsHandlingWidget extends AbstractWidget
{

    /** The initial referrals handling method. */
    private Connection.ReferralHandlingMethod initialReferralsHandlingMethod;

    /** The group. */
    private Group group;

    /** The follow manually button. */
    private Button followManuallyButton;

    /** The follow automatically button. */
    private Button followAutomaticallyButton;

    /** The ignore button. */
    private Button ignoreButton;


    // ── R2 Arrives With a Specific Routing Policy Pre-Loaded ─────────────────────────
    // R2 has been briefed by Leia before the mission: "At any junction marked as a
    // referral, follow automatically — don't stop to ask the crew." He memorises this
    // policy so it's already selected when the widget first renders.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget pre-configured with a specific referral handling policy.
     * Use this when opening the properties dialog for an existing saved search that
     * already has a referral setting stored.
     *
     * <p>For example — R2 receives his routing orders before the mission:</p>
     * <pre>
     *   routingPolicy = ReferralHandlingMethod.FOLLOW;
     *   R2.memory.set( routingPolicy );
     * </pre>
     *
     * @param initialReferralsHandlingMethod  The referral policy to show when the widget
     *                                        first renders. One of FOLLOW, FOLLOW_MANUALLY,
     *                                        or IGNORE.
     */
    public ReferralsHandlingWidget( Connection.ReferralHandlingMethod initialReferralsHandlingMethod )
    {
        this.initialReferralsHandlingMethod = initialReferralsHandlingMethod;
    }


    // ── R2 Boots Up With the Default Routing Policy ───────────────────────────────────
    // No special briefing this time — R2 falls back to the sensible default:
    // follow referrals automatically so the search just works without interrupting anyone.
    // We default to FOLLOW so new search dialogs work smoothly out of the box.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget defaulting to {@link Connection.ReferralHandlingMethod#FOLLOW},
     * meaning referrals are chased automatically. This is the right default for most
     * searches — it produces complete results without requiring the user to intervene.
     *
     * <p>For example — R2 defaults to auto-routing when no special orders exist:</p>
     * <pre>
     *   R2.policy = ReferralHandlingMethod.FOLLOW; // safe default
     * </pre>
     */
    public ReferralsHandlingWidget()
    {
        this.initialReferralsHandlingMethod = Connection.ReferralHandlingMethod.FOLLOW;
    }


    // ── R2 Sets Up His Junction-Decision Panel ────────────────────────────────────────
    // R2 installs the three-option panel at the Death Star junction: a radio labeled
    // "Ask me" (follow manually, only shown if the caller requests it), "Auto-route"
    // (follow automatically), and "Skip" (ignore). Each radio fires a notification so
    // the parent form knows the policy changed.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT controls inside the given parent composite.
     * Creates a labeled group with radio buttons for the three referral policies.
     * The "Follow manually" radio is optional — pass {@code true} to show it (useful
     * in the browser's connection settings) or {@code false} to hide it (simpler for
     * search dialogs that don't need manual control).
     * Call this exactly once after construction.
     *
     * <p>For example — R2 builds the junction-routing panel:</p>
     * <pre>
     *   group = new Group( parent, "Referrals Handling" );
     *   if ( showManual ) group.add( "Follow Manually" );
     *   group.add( "Follow Automatically" );
     *   group.add( "Ignore" );
     * </pre>
     *
     * @param parent                  The SWT composite that will host the controls.
     * @param followManuallyVisible   {@code true} to show the "Follow manually" radio button,
     *                                {@code false} to hide it (the option is then unavailable).
     */
    public void createWidget( Composite parent, boolean followManuallyVisible )
    {
        group = BaseWidgetUtils.createGroup( parent,
            Messages.getString( "ReferralsHandlingWidget.ReferralsHandling" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 1, 1 );

        if ( followManuallyVisible )
        {
            followManuallyButton = BaseWidgetUtils.createRadiobutton( groupComposite, Messages
                .getString( "ReferralsHandlingWidget.FollowManually" ), 1 ); //$NON-NLS-1$
            followManuallyButton.setToolTipText( Messages.getString( "ReferralsHandlingWidget.FollowManuallyTooltip" ) ); //$NON-NLS-1$
            followManuallyButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    notifyListeners();
                }
            } );
        }

        followAutomaticallyButton = BaseWidgetUtils.createRadiobutton( groupComposite, Messages
            .getString( "ReferralsHandlingWidget.FollowAutomatically" ), 1 ); //$NON-NLS-1$
        followAutomaticallyButton.setToolTipText( Messages
            .getString( "ReferralsHandlingWidget.FollowAutomaticallyTooltip" ) ); //$NON-NLS-1$
        followAutomaticallyButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        ignoreButton = BaseWidgetUtils.createRadiobutton( groupComposite, Messages
            .getString( "ReferralsHandlingWidget.Ignore" ), 1 ); //$NON-NLS-1$
        ignoreButton.setToolTipText( Messages.getString( "ReferralsHandlingWidget.IgnoreTooltip" ) ); //$NON-NLS-1$
        ignoreButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        setReferralsHandlingMethod( initialReferralsHandlingMethod );
    }


    // ── R2 Updates His Routing Policy Mid-Mission ─────────────────────────────────────
    // Leia calls over the comm: "Change of plan — ignore all referrals from now on."
    // R2 reaches over and flips the selector to "Skip." If the "Ask me" option isn't
    // visible and FOLLOW_MANUALLY is requested, he falls back to FOLLOW instead.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically sets the referral handling policy and updates the radio buttons
     * to match. If the "Follow manually" radio is not visible and FOLLOW_MANUALLY is
     * requested, we silently fall back to FOLLOW so the form stays consistent.
     * Call this when loading a saved search into the dialog.
     *
     * <p>For example — R2 switches policy mid-mission:</p>
     * <pre>
     *   policy = ReferralHandlingMethod.IGNORE;
     *   ignoreRadio.setSelected( true );
     *   followAutoRadio.setSelected( false );
     * </pre>
     *
     * @param referralsHandlingMethod  The policy to apply. One of FOLLOW, FOLLOW_MANUALLY,
     *                                 or IGNORE. If FOLLOW_MANUALLY is passed but the
     *                                 manual button is hidden, FOLLOW is used instead.
     */
    public void setReferralsHandlingMethod( Connection.ReferralHandlingMethod referralsHandlingMethod )
    {
        initialReferralsHandlingMethod = referralsHandlingMethod;
        if ( followManuallyButton == null && referralsHandlingMethod == ReferralHandlingMethod.FOLLOW_MANUALLY )
        {
            // fall-back to FOLLOW if manually button is invisible
            initialReferralsHandlingMethod = ReferralHandlingMethod.FOLLOW;
        }

        if ( followManuallyButton != null )
        {
            followManuallyButton
                .setSelection( initialReferralsHandlingMethod == Connection.ReferralHandlingMethod.FOLLOW_MANUALLY );
        }
        followAutomaticallyButton
            .setSelection( initialReferralsHandlingMethod == Connection.ReferralHandlingMethod.FOLLOW );
        ignoreButton.setSelection( initialReferralsHandlingMethod == Connection.ReferralHandlingMethod.IGNORE );
    }


    // ── R2 Reports the Current Routing Decision ───────────────────────────────────────
    // The Rebel command asks: "R2, which junction policy is selected?" He checks which
    // radio is lit and reports back: IGNORE, FOLLOW, or FOLLOW_MANUALLY.
    // We read the SWT button selections and map them to the enum.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the currently selected radio button and returns the matching
     * {@link Connection.ReferralHandlingMethod}. The search engine uses this when
     * building the LDAP search request.
     *
     * <p>For example — R2 reports which radio is lit:</p>
     * <pre>
     *   if ( ignoreRadio.selected )      return IGNORE;
     *   if ( followAutoRadio.selected )  return FOLLOW;
     *   else                             return FOLLOW_MANUALLY;
     * </pre>
     *
     * @return  The selected referral handling policy. Never null.
     */
    public Connection.ReferralHandlingMethod getReferralsHandlingMethod()
    {
        if ( ignoreButton.getSelection() )
        {
            return Connection.ReferralHandlingMethod.IGNORE;
        }
        else if ( followAutomaticallyButton.getSelection() )
        {
            return Connection.ReferralHandlingMethod.FOLLOW;
        }
        else
        {
            return Connection.ReferralHandlingMethod.FOLLOW_MANUALLY;
        }
    }


    // ── R2 Powers Down His Junction Panel ────────────────────────────────────────────
    // When the Empire shuts down R2's sector, he can no longer change his routing policy
    // — the panel greys out and the radios stop responding.
    // We propagate the enabled state to the group and all visible radio buttons.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire widget. The group box and all visible radio buttons
     * are toggled in one call. Pass {@code false} to make the referral options read-only —
     * useful when showing the settings for a search that is currently running.
     *
     * <p>For example — R2's junction panel goes dark:</p>
     * <pre>
     *   junctionGroup.setEnabled( false );
     *   allRadios.forEach( r -> r.setEnabled( false ) );
     * </pre>
     *
     * @param b  {@code true} to enable the controls; {@code false} to grey them out.
     */
    public void setEnabled( boolean b )
    {
        group.setEnabled( b );
        if ( followManuallyButton != null )
        {
            followManuallyButton.setEnabled( b );
        }
        followAutomaticallyButton.setEnabled( b );
        ignoreButton.setEnabled( b );
    }

}
