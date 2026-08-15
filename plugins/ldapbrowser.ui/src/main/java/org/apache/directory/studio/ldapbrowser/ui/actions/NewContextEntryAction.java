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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.common.wizards.NewContextEntryWizard;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;


// ── CLASS: NewContextEntryAction — LAYING THE DEATH STAR'S KEEL ──────────────
// Every Death Star had to start somewhere: before the superlaser, the hangars,
// and the thousands of corridors, engineers laid the very first structural ring —
// the keel — that everything else would attach to.  In LDAP, a "context entry"
// (also called a naming context root) is exactly that: the topmost entry in a
// directory partition, the anchor that all other entries hang from.
// NewContextEntryAction launches the wizard that creates that foundational entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Launches the New Context Entry Wizard to create a root-level LDAP entry (a
 * naming context) in the connected directory server.
 * A context entry is the topmost node in a directory partition — you must create
 * one before you can add child entries beneath it.
 * Think of this class as the Death Star's keel-laying crew: they build the very
 * first structural element that everything else depends on.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewContextEntryAction extends NewEntryAction
{

    // ── No-Arg Constructor: Crew Assembles With No Window Reference ──────────────
    // A construction crew can assemble in the yards before a specific workbench
    // window is assigned to them — they'll get their orders later.  The no-arg
    // constructor lets Eclipse instantiate us via the plugin.xml extension point
    // before a window is known; init() or the window constructor wires it in later.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a NewContextEntryAction with no associated workbench window.
     * Eclipse typically uses this form when constructing actions declared in
     * plugin.xml; the window is injected separately via {@code init(IWorkbenchWindow)}.
     */
    public NewContextEntryAction()
    {
    }


    // ── Crew Assigned To A Specific Workbench ────────────────────────────────────
    // When the Emperor assigns a construction crew to a specific battle station
    // workbench, they know exactly which station's context they're operating in.
    // This constructor does the same: it wires us directly to a known window so
    // we can correctly resolve the shell and active page.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a NewContextEntryAction tied to a specific workbench window.
     * Use this form when you already know the active window at construction time —
     * for instance, when adding the action to a window's toolbar or menu.
     *
     * @param window  the workbench window this action operates within; used to
     *                resolve the shell for any dialogs we open
     */
    public NewContextEntryAction( IWorkbenchWindow window )
    {
        super( window );
    }


    // ── Keel Blueprint Specifies A Context Entry ─────────────────────────────────
    // The keel blueprint isn't the same as the blueprint for an interior room —
    // it specifies a unique structural element.  getWizard() overrides the parent's
    // NewEntryWizard with a NewContextEntryWizard that knows how to create the
    // special root-level naming-context entry rather than an ordinary child entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wizard that guides the user through creating a context entry.
     * We override the parent's default {@code NewEntryWizard} with a
     * {@code NewContextEntryWizard} because the creation flow for a naming-context
     * root is slightly different — it doesn't have a parent entry to inherit from.
     *
     * @return a fresh {@link NewContextEntryWizard} instance
     */
    protected INewWizard getWizard()
    {
        return new NewContextEntryWizard();
    }


    // ── Blueprint Title: "New Context Entry" ────────────────────────────────────
    // Every blueprint has a title line that distinguishes it from other plans.
    // We override getText() to return the "New Context Entry" label rather than
    // the parent's "New Entry" label, so the menu item is unambiguous.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action, distinguishing it from the
     * plain "New Entry" action in menus.
     *
     * @return the localised label for creating a context (naming-context root) entry
     */
    public String getText()
    {
        return Messages.getString( "NewEntryAction.NewContextEntry" ); //$NON-NLS-1$
    }


    // ── Keel Has Its Own Icon ────────────────────────────────────────────────────
    // The keel blueprint carried the same "add entry" icon as all other structural
    // blueprints — they're all still entries, just at different levels.
    // We share the IMG_ENTRY_ADD icon with NewEntryAction.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon.
     * We use the same entry-add icon as the plain New Entry action, because at the
     * UI level a context entry looks just like any other new entry to the user.
     *
     * @return the image descriptor for the entry-add icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_ENTRY_ADD );
    }

}
