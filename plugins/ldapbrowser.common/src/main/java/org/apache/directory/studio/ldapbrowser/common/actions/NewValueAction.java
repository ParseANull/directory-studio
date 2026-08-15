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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: NewValueAction — LUKE LEARNS A NEW FORCE ABILITY ON DAGOBAH ───────
// On Dagobah, Yoda pushes Luke to add new skills to his repertoire — lifting
// rocks, feeling living things, extending his reach into the Force beyond what
// he could do before.  Each training session adds a new capability to an
// existing foundation.  That's exactly what we do here: we append a fresh empty
// value slot to an existing LDAP attribute so the user can type in another piece
// of data.  The attribute (the Force) already exists; we're just expanding it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Handles the "New Value" action in the LDAP entry editor.  When triggered,
 * this action adds a new empty value placeholder to whichever attribute is
 * currently selected, allowing the user to type in an additional value.
 *
 * <p>LDAP attributes can be multi-valued — for example, a {@code mail}
 * attribute can hold several email addresses.  This action is how we add
 * another one.</p>
 *
 * <p>Think of this class as Yoda coaching Luke to unlock another Force
 * technique: the attribute (Luke's Force connection) already exists, and we
 * just open a new channel within it.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewValueAction extends BrowserAction
{
    // ── Luke Arrives at Dagobah, Ready to Train ───────────────────────────────
    // Luke lands his X-wing in the swamp and steps out — no special gear, just
    // him, open to whatever training Yoda has in store.
    // We do the same: initialize with no frills, letting the base class handle
    // the standard setup so we're ready to act when called upon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code NewValueAction}.  Delegates straight to the
     * parent constructor — the base class wires up all the standard Eclipse
     * action plumbing (text, icon, enablement, etc.).
     */
    public NewValueAction()
    {
        super();
    }


    // ── Luke Completes His Training Session ───────────────────────────────────
    // After each session with Yoda, Luke rests and the swamp returns to quiet.
    // Everything is properly wound down — no loose energy, no open channels.
    // We do the same here: clean up resources so nothing is left dangling.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up this action when it's no longer needed.  We hand off to the
     * parent's dispose method, which handles listener removal and any other
     * teardown the base class owns.
     */
    public void dispose()
    {
        super.dispose();
    }


    // ── Yoda Says "Now — Reach Out Again" ─────────────────────────────────────
    // Yoda points at a new target — a different rock, a different exercise —
    // and Luke extends his connection to the Force into that new space.
    // We find the right attribute and punch in a new empty value slot for the
    // user to fill.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: locates the target attribute from whatever is
     * currently selected (a value, an attribute, or an attribute hierarchy),
     * then calls {@code addEmptyValue()} to append a blank value slot that the
     * user can immediately edit.
     *
     * <p>For example — Yoda extends Luke's training to a new ability:</p>
     * <pre>
     *   Yoda: "Reach out.  Feel it — a new path in the Force."
     *   Luke closes his eyes, extends, and a new connection snaps open.
     *   The attribute now has one more value slot, waiting to be filled.
     * </pre>
     */
    public void run()
    {
        IAttribute attribute = null;
        if ( getSelectedValues().length == 1 )
        {
            attribute = getSelectedValues()[0].getAttribute();
        }
        else if ( getSelectedAttributes().length == 1 )
        {
            attribute = getSelectedAttributes()[0];
        }
        else if ( getSelectedAttributeHierarchies().length == 1 )
        {
            attribute = getSelectedAttributeHierarchies()[0].getAttribute();
        }

        attribute.addEmptyValue();
    }


    // ── Yoda Labels the New Technique ────────────────────────────────────────
    // Yoda names each new skill plainly: "Telekinesis.  Levitation.  Force
    // push." — no flowery language, just a clear name so Luke knows what it is.
    // We return the localized label so menus and tooltips read clearly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action from the message bundle.
     * This is what appears in menus and toolbar tooltips — something like
     * "New Value".
     *
     * @return the localized action label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "NewValueAction.NewValue" ); //$NON-NLS-1$
    }


    // ── Luke's Training Satchel Has a Recognizable Mark ──────────────────────
    // Yoda's training kit has a distinctive look — Luke can pick it out from a
    // shelf of gear in an instant.
    // We return the "add value" icon so users spot this action at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action, pulled from the plugin's image
     * registry.  Shown in toolbar buttons and menu items.
     *
     * @return the {@link ImageDescriptor} for the add-value icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_VALUE_ADD );
    }


    // ── The Training Exercise Has an Official Name in the Logs ───────────────
    // The Rebel Alliance keeps a log of every skill Jedi trainees unlock, each
    // indexed by a unique ID so the command framework can look them up.
    // We return our Eclipse command ID for exactly the same reason.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.  Eclipse uses this to
     * wire us into keybindings and the command framework — our unique slot in
     * the global action registry.
     *
     * @return the command ID string {@code BrowserCommonConstants.CMD_ADD_VALUE}
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.CMD_ADD_VALUE;
    }


    // ── Luke Can Only Train When the Setup Is Right ───────────────────────────
    // Yoda won't start a new exercise if Luke's already mid-lift, or if
    // multiple targets are in play — the conditions have to be clean and
    // unambiguous before they begin.
    // We only enable this action when exactly one attribute or value is selected
    // and no search results are muddying the context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled right now.  We need a
     * clear, single-attribute context — either exactly one value selected, or
     * exactly one attribute selected, or exactly one attribute hierarchy from a
     * search result.  Multiple selections or mixed selections disable the
     * action because we wouldn't know which attribute to add the value to.
     *
     * @return {@code true} if conditions are right to add a new value;
     *         {@code false} otherwise
     */
    public boolean isEnabled()
    {
        return ( getSelectedSearchResults().length == 0 && getSelectedAttributes().length == 0 && getSelectedValues().length == 1 )

            || ( getSelectedSearchResults().length == 0 && getSelectedValues().length == 0 && getSelectedAttributes().length == 1 )

            || ( getSelectedSearchResults().length == 1 && getSelectedValues().length == 0
                && getSelectedAttributes().length == 0 && getSelectedAttributeHierarchies().length == 1 );
    }
}
