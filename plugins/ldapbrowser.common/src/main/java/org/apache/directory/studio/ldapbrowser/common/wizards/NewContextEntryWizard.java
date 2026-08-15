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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;


// ── CLASS: NewContextEntryWizard — REBELS ESTABLISH ECHO BASE ON HOTH ────────
// In "The Empire Strikes Back", the Rebel Alliance lands on the frozen planet
// Hoth and stakes out Echo Base from scratch — no pre-existing structure,
// no parent outpost; they pick the spot, assign it a code, and declare it
// the root of all operations on this world.
// This wizard does exactly that for LDAP: it creates a brand-new context
// entry (the top of a naming context like dc=example,dc=com) with no parent
// in the directory, just a user-typed full DN and the object classes to go
// with it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A specialised variant of {@link NewEntryWizard} for creating a new LDAP
 * context entry — the root of a naming context such as {@code dc=example,dc=com}.
 * It skips the parent-selection step because context entries have no parent in
 * the directory sense; the user simply types the full DN directly.
 * Think of this class as the Rebels landing on Hoth and declaring it home
 * base — nobody told them where to park, they chose the spot themselves
 * and built everything on top of it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewContextEntryWizard extends NewEntryWizard
{

    // ── Rebel Construction Crews Touch Down on Hoth ───────────────────────────
    // The Rebel transports break atmosphere over Hoth and begin landing
    // procedures with no ceremony — the moment the ramp drops, Echo Base
    // construction has begun.
    // Our constructor is just as straightforward: it delegates entirely to
    // the parent wizard and relies on {@link #init} to set everything else up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewContextEntryWizard} with defaults inherited from
     * {@link NewEntryWizard}.
     * The window title and connection context are set later during
     * {@link #init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)},
     * once we know which directory we're dropping into.
     *
     * <p>For example — the Rebels land and immediately start base construction:</p>
     * <pre>
     *   NewContextEntryWizard wizard = new NewContextEntryWizard();
     *   // parent wizard sets up progress monitor, read-only guard, etc.
     *   wizard.init( workbench, selection ); // now we know our "Hoth"
     * </pre>
     */
    public NewContextEntryWizard()
    {
        super();
    }


    // ── Echo Base Receives Its Alliance Sector Code ───────────────────────────
    // General Rieekan assigns Echo Base a sector code in the Alliance's
    // central registry so that any command post can look it up by name.
    // This method returns the Eclipse wizard registry ID so the platform can
    // locate and launch this wizard from menus or extension points.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard registry ID for this wizard.
     * The platform uses this string to look up and launch the wizard from
     * menu contributions, keybindings, or plugin extension points.
     *
     * <p>For example — Rieekan registers Echo Base in the Alliance database:</p>
     * <pre>
     *   String id = NewContextEntryWizard.getId();
     *   // → BrowserCommonConstants.WIZARD_NEW_CONTEXT_ENTRY_WIZARD
     * </pre>
     *
     * @return  The wizard's unique ID string from {@link BrowserCommonConstants}.
     */
    public static String getId()
    {
        return BrowserCommonConstants.WIZARD_NEW_CONTEXT_ENTRY_WIZARD;
    }


    // ── Rieekan Confirms This Is the Main Base, Not a Forward Post ───────────
    // General Rieekan makes it clear that Echo Base is the primary installation
    // on Hoth — not a forward scouting post that reports to a larger base.
    // This method flags to the rest of the wizard machinery that we are creating
    // a root context entry (full DN typed by the user, no parent lookup needed).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Declares that this wizard creates a context (root) entry rather than a
     * regular child entry.
     * When {@code true}, the DN wizard page shows a simple free-text combo for
     * the full DN instead of the parent-selector-plus-RDN-builder widget.
     *
     * <p>For example — Rieekan confirms Hoth is the Alliance's top-level base:</p>
     * <pre>
     *   if ( wizard.isNewContextEntry() ) {
     *     // skip parent lookup — user types the full root DN directly
     *   }
     * </pre>
     *
     * @return  Always {@code true} — this wizard always targets a context entry.
     */
    public boolean isNewContextEntry()
    {
        return true;
    }

}
