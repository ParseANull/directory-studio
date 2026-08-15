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
package org.apache.directory.studio.schemaeditor.view.wizards;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


// ── CLASS: CommitChangesInformationWizardPage — Leia's Hologram Warning ───────
// Before Leia hands R2-D2 the Death Star plans, she records a hologram:
// "Help me, Obi-Wan Kenobi — you're my only hope." It's a clear, urgent
// message delivered before any irreversible action is taken.
// This page does the same thing: it's the first page of the Commit Changes
// wizard, and it exists solely to warn the user — in plain language — that
// what they're about to do will push schema changes to a live server, which
// is a big deal and cannot easily be undone.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The introductory information page of the Commit Changes wizard.
 * It delivers a clear human-readable explanation and a prominent warning icon
 * so users understand the consequences of what they're about to do before they
 * advance to the diff-review page.
 * Think of it as Leia's hologram: a message that must be heard before any
 * action is taken.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommitChangesInformationWizardPage extends WizardPage
{
    // UI Fields

    // ── Recording The Hologram Message ───────────────────────────────────────
    // Leia steps in front of R2-D2's recording lens on the Tantive IV and
    // composes her message — she sets the subject ("Commit Changes"), the
    // urgency ("please read this before proceeding"), and her own identity
    // (the wizard banner image).
    // We set those same three things on this page object so Eclipse knows
    // what to show in the wizard header when this page is active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Configures the page's title, descriptive subtitle, and wizard header
     * image — everything the wizard shell needs to render the header bar.
     * We take no constructor arguments because this is a purely informational
     * page with no user-configurable state.
     *
     * <p>For example — Leia records her hologram message:</p>
     * <pre>
     *   page.setTitle( "Commit Changes" );
     *   page.setDescription( "Please read this before committing." );
     *   page.setImageDescriptor( commitWizardBanner );
     * </pre>
     */
    protected CommitChangesInformationWizardPage()
    {
        super( "CommitChangesInformationWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "CommitChangesInformationWizardPage.CommitChanges" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "CommitChangesInformationWizardPage.PleaseReadInformationBeforeCommitting" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_COMMIT_CHANGES_WIZARD ) );
    }


    // ── Leia's Hologram Plays For Obi-Wan ────────────────────────────────────
    // R2-D2 projects the hologram: Leia's words fill the room alongside the
    // Rebellion crest — text, context, and a symbol conveying the gravity of
    // what's being asked.
    // We build the SWT layout: a wrapping text label that explains what the
    // commit will do, followed by a large warning icon that reinforces "this
    // is serious, read carefully."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the SWT widget tree for this page: an explanatory text block
     * followed by a large warning icon.
     * Eclipse calls this before the page becomes visible; we build everything
     * here rather than in the constructor so that SWT resources are only
     * allocated when the page is actually needed.
     *
     * <p>For example — the hologram plays: text explanation, then the symbol
     * of urgency:</p>
     * <pre>
     *   Label holoText = new Label( composite, SWT.WRAP );
     *   holoText.setText( "You are about to commit schema changes to the server..." );
     *
     *   Label warningIcon = new Label( composite, SWT.NONE );
     *   warningIcon.setImage( rebellionCrestImage );  // big warning — take notice
     * </pre>
     *
     * @param parent  the parent composite provided by Eclipse's wizard framework
     *                — we attach our own composite as a child of it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Information Label
        String informationString = Messages.getString( "CommitChangesInformationWizardPage.YouAreAboutToCommit" ); //$NON-NLS-1$
        Label informationLabel = new Label( composite, SWT.WRAP );
        informationLabel.setText( informationString );
        informationLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, true ) );

        // Warning Label
        Label warningLabel = new Label( composite, SWT.NONE );
        warningLabel.setImage( Activator.getDefault().getImage( PluginConstants.IMG_WARNING_32X32 ) );
        warningLabel.setLayoutData( new GridData( SWT.CENTER, SWT.BOTTOM, true, true ) );

        setControl( composite );
    }

}
