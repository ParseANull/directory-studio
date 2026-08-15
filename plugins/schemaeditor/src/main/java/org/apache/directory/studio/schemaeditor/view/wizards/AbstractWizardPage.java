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


import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;


// ── CLASS: AbstractWizardPage — Vader's Life-Support Suit ────────────────────
// Vader's suit is the essential infrastructure that keeps Anakin alive and
// functional — it provides the shared life-support that every specific action
// (fighting, commanding, Force-choking) depends on without duplicating it.
// This abstract class is that suit: a layer of shared plumbing that every
// concrete wizard page in this plugin puts on before doing its own work.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The common base class for all wizard pages in the Schema Editor plugin.
 * It extends JFace's {@link WizardPage} and adds a couple of shared helper
 * methods so we don't have to copy the same error/warning display logic into
 * every concrete page.
 * Think of this class as Vader's suit: the raw power (our page logic) lives
 * in the subclasses, but this base layer provides the life-support every one
 * of them needs to survive.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractWizardPage extends WizardPage
{
    // ── Suiting Up With Title And Image ──────────────────────────────────────
    // The first time we see Vader in the suit, the medical droids bolt on every
    // component in sequence: chest plate, helmet, respirator — name, title,
    // and emblem, all locked in at construction time.
    // The specific action here is snapping the iconic helmet down, completing
    // the transformation from broken Anakin to the dark enforcer.
    // We do the same: calling the parent constructor to wire up the page's
    // identity — name, display title, and the decorative image shown in the
    // wizard header.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a wizard page with a full identity: a logical name, a human-readable
     * title, and an optional header image.
     * We use this variant when we want the wizard to display a branded icon in
     * the top-right corner of the page (most of our pages do this).
     *
     * <p>For example — Vader steps into the suit fully equipped:</p>
     * <pre>
     *   medicalDroid.attach("chestPlate");   // pageName  — internal id
     *   medicalDroid.attach("helmet");       // title     — shown in wizard header
     *   medicalDroid.attach("respirator");   // titleImage— icon in header corner
     * </pre>
     *
     * @param pageName    the internal id of this page — Eclipse uses it to track
     *                    navigation, so it must be unique within the wizard.
     * @param title       the human-readable title shown at the top of the wizard
     *                    dialog; pass {@code null} to show nothing.
     * @param titleImage  an image descriptor for the decorative icon in the
     *                    wizard header; pass {@code null} if we don't need one.
     */
    protected AbstractWizardPage( String pageName, String title, ImageDescriptor titleImage )
    {
        super( pageName, title, titleImage );
    }


    // ── Minimum Viable Suit — Name Only ──────────────────────────────────────
    // Sometimes the medical droids just get the breathing apparatus on Vader
    // and nothing else — enough to keep him going without the full regalia.
    // Just the respirator: page name alone, no title banner, no decorative image.
    // We use this stripped-down constructor when the page is simple enough that
    // it doesn't need its own header branding.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a wizard page with only a logical name — no title text and no
     * header image.
     * This is the lightweight version of the constructor for pages that keep
     * their header area empty.
     *
     * <p>For example — Vader breathes but wears no helmet yet:</p>
     * <pre>
     *   medicalDroid.attach("respirator");  // pageName only — bare minimum identity
     *   // chestPlate and helmet come later, or not at all
     * </pre>
     *
     * @param pageName  the internal id of this page — same rules as the full
     *                  constructor; must be unique within the wizard.
     */
    protected AbstractWizardPage( String pageName )
    {
        super( pageName );
    }


    // ── Vader's Warning Klaxon — Hard Stop ───────────────────────────────────
    // When something goes wrong on the bridge of the Executor, every alarm
    // sounds and all forward progress halts until the problem is resolved.
    // A red error banner appears across the wizard header and the "Next"/"Finish"
    // buttons grey out — the user cannot proceed.
    // We call this whenever validation fails: missing input, bad path, whatever
    // would blow up when the wizard tries to do its work.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows an error message in the wizard header and locks the page so the user
     * cannot proceed until they fix whatever triggered the error.
     * Passing {@code null} clears the error and re-enables the page — we use
     * that pattern to reset state after the user corrects the problem.
     *
     * <p>For example — Vader's bridge klaxon fires and everything stops:</p>
     * <pre>
     *   if ( hyperdriveFailure ) {
     *       displayErrorMessage( "Hyperdrive offline — find another way out." );
     *       // wizard's Finish button is now disabled
     *   }
     *   // once engineers fix it:
     *   displayErrorMessage( null ); // klaxon silenced, navigation resumes
     * </pre>
     *
     * @param message  the error text to show — describes what's wrong and hints
     *                 at how to fix it; pass {@code null} to clear the error.
     */
    protected void displayErrorMessage( String message )
    {
        setMessage( null, DialogPage.NONE );
        setErrorMessage( message );
        setPageComplete( message == null );
    }


    // ── Vader's Yellow Advisory Light — Proceed With Care ────────────────────
    // Not every alarm on the Executor is a full red-stop; sometimes it's a
    // yellow advisory — "something's off, but we can still move forward."
    // A yellow warning banner shows in the wizard header, yet the "Next" and
    // "Finish" buttons remain active so the user can continue if they choose to.
    // We use this for non-fatal conditions: odd-but-valid input, potential
    // gotchas, things worth flagging without blocking.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows a warning message in the wizard header without blocking the user
     * from proceeding — the page stays complete even while the warning is visible.
     * Use this for soft alerts where we want to inform but not stop the user.
     *
     * <p>For example — the Executor's yellow advisory light flickers on:</p>
     * <pre>
     *   displayWarningMessage(
     *       "Shield generators at 40% — battle stations still operational." );
     *   // wizard's Finish button remains enabled despite the warning
     * </pre>
     *
     * @param message  the warning text to display; unlike errors, a non-null
     *                 message here does NOT disable the page.
     */
    protected void displayWarningMessage( String message )
    {
        setErrorMessage( null );
        setMessage( message, DialogPage.WARNING );
        setPageComplete( true );
    }
}
