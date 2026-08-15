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
package org.apache.directory.studio.templateeditor.view.wizards;


import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;


// ── CLASS: AbstractWizardPage — EMPEROR'S STEP-BY-STEP PLAN BASE STEP ─────────────
// Every step in the Emperor's plan to bring Luke to the dark side follows the same
// pattern: either the step is blocked by an error (Luke hasn't accepted yet) or it
// is complete but carries a warning (Luke is hesitating). This base class captures
// that shared pattern — a {@link WizardPage} that knows how to display error and
// warning messages consistently across every wizard step we build.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all wizard pages in the template editor. Extends
 * {@link WizardPage} and adds two convenience methods — {@link #displayErrorMessage}
 * and {@link #displayWarningMessage} — that centralise the error/warning display
 * and page-completion logic so concrete pages don't repeat the same pattern.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractWizardPage extends WizardPage
{
    // ── CONSTRUCTOR (FULL): NAME + TITLE + IMAGE ───────────────────────────────────
    // The Emperor plans this step with a name, a title banner, and an image.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new wizard page with the given name, title, and image.
     *
     * @param pageName    the internal name of the page (used by the wizard framework)
     * @param title       the title shown in the wizard header, or {@code null} for none
     * @param titleImage  the image shown in the wizard header, or {@code null} for none
     */
    protected AbstractWizardPage( String pageName, String title, ImageDescriptor titleImage )
    {
        super( pageName, title, titleImage );
    }


    // ── CONSTRUCTOR (NAME-ONLY): NAME WITHOUT TITLE OR IMAGE ──────────────────────
    // The Emperor plans this step with just a name — title and image are filled in
    // by the subclass.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new wizard page with the given name and no title or image.
     *
     * @param pageName  the internal name of the page (used by the wizard framework)
     */
    protected AbstractWizardPage( String pageName )
    {
        super( pageName );
    }


    // ── DISPLAY ERROR MESSAGE: BLOCK THE STEP ─────────────────────────────────────
    // The Emperor's courier delivers an error — the step is blocked until Luke
    // complies. A non-null message sets the error banner and marks the page
    // incomplete; a null message clears the error and unblocks the "Finish" button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows an error message in the wizard's error area and marks the page as
     * incomplete (blocking "Finish"). Pass {@code null} to clear the error and
     * re-enable completion.
     *
     * @param message  the error text to display, or {@code null} to clear the error
     */
    protected void displayErrorMessage( String message )
    {
        setErrorMessage( message );
        setPageComplete( message == null );
    }


    // ── DISPLAY WARNING MESSAGE: WARN BUT ALLOW THE STEP TO PROCEED ──────────────
    // The Emperor notes a warning — Luke is hesitating — but the plan can still
    // proceed. The error area is cleared, a warning banner appears, and the page
    // remains completable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears any error message, shows a warning message in the wizard's message
     * area, and keeps the page complete (so the user can still click "Finish").
     *
     * @param message  the warning text to display
     */
    protected void displayWarningMessage( String message )
    {
        setErrorMessage( null );
        setMessage( message, DialogPage.WARNING );
        setPageComplete( true );
    }
}
