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
import org.apache.directory.studio.ldapbrowser.common.wizards.AttributeWizard;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: NewAttributeAction — LUKE FORGES HIS GREEN LIGHTSABER ─────────────
// In Return of the Jedi, Luke retreats to a cave on Tatooine and, with focused
// deliberation, constructs his own green lightsaber from scratch — choosing the
// crystal, assembling the hilt, and bringing something new into existence.
// That's exactly what we do here: we walk the user through a wizard and forge a
// brand-new LDAP attribute, attaching it to the target entry when they confirm.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Handles the "New Attribute" action in the LDAP browser.  When the user
 * triggers this action, we open the {@link AttributeWizard} so they can pick
 * an attribute type, then we add that attribute (plus an empty placeholder
 * value) to the currently selected entry.
 *
 * <p>Think of this class as Luke constructing his green lightsaber: we guide
 * the user step by step through choosing what kind of attribute they want, and
 * once they confirm, we bring that attribute into existence on the entry.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeAction extends BrowserAction
{
    // ── Luke Selects His Kyber Crystal ───────────────────────────────────────
    // In the cave on Tatooine, Luke reaches into his component kit and begins
    // the careful work — the first step before the lightsaber exists at all.
    // We similarly initialize a clean action instance, ready to be configured.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code NewAttributeAction}.  Nothing fancy here — we
     * just call the parent constructor and let the base class set up the
     * standard action plumbing (text, image, enablement state, etc.).
     */
    public NewAttributeAction()
    {
        super();
    }


    // ── Luke Sets Down the Unfinished Hilt ───────────────────────────────────
    // After his meditation, Luke carefully packs away his tools — the cave is
    // cleared, but nothing is wasted; the work simply stops cleanly.
    // We do the same: release any resources we're holding so nothing leaks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up this action when it's no longer needed.  We delegate straight
     * up to the parent, which handles listener de-registration and other
     * teardown so we don't leave dangling references lying around.
     */
    public void dispose()
    {
        super.dispose();
    }


    // ── Luke Ignites the Completed Lightsaber ────────────────────────────────
    // After all the preparation, Luke presses the activator — the green blade
    // snaps to life for the first time, real and ready to use.
    // We open the wizard, and if the user confirms, the new attribute is born.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: figures out which LDAP entry to work with, opens
     * the {@link AttributeWizard} in a blocking dialog, and — if the user
     * clicks OK — adds the new attribute (with an empty value slot) to that
     * entry.
     *
     * <p>For example — Luke completes the lightsaber construction:</p>
     * <pre>
     *   Luke presses the activator on the new hilt.
     *   The green blade extends — solid, real, confirmed.
     *   He attaches it to his belt: the weapon is now part of his kit.
     * </pre>
     *
     * <p>We resolve the target entry from whatever is currently selected
     * (the editor input, a selected entry, a selected attribute, or a selected
     * value — in that priority order), then run the wizard.</p>
     */
    public void run()
    {

        IEntry entry = null;
        if ( getInput() instanceof IEntry )
        {
            entry = ( IEntry ) getInput();
        }
        else if ( getSelectedEntries().length > 0 )
        {
            entry = getSelectedEntries()[0];
        }
        else if ( getSelectedAttributes().length > 0 )
        {
            entry = getSelectedAttributes()[0].getEntry();
        }
        else if ( getSelectedValues().length > 0 )
        {
            entry = getSelectedValues()[0].getAttribute().getEntry();
        }

        if ( entry != null )
        {
            AttributeWizard wizard = new AttributeWizard(
                Messages.getString( "NewAttributeAction.NewAttribute" ), true, true, null, entry ); //$NON-NLS-1$
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            if ( dialog.open() == WizardDialog.OK )
            {
                String newAttributeDescription = wizard.getAttributeDescription();
                if ( newAttributeDescription != null && !"".equals( newAttributeDescription ) ) //$NON-NLS-1$
                {
                    IAttribute att = entry.getAttribute( newAttributeDescription );
                    if ( att == null )
                    {
                        att = new Attribute( entry, newAttributeDescription );
                        entry.addAttribute( att );
                    }

                    att.addEmptyValue();
                }
            }
        }
    }


    // ── Luke Names His Weapon ────────────────────────────────────────────────
    // A lightsaber without a name in the inventory is just a glowing stick.
    // Luke needs everyone to know what this thing is called.
    // We return the localized label so menus and tooltips display it correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action, pulled from the message
     * bundle so it works correctly in all locales.  This is what shows up in
     * menus and toolbar tooltips — something like "New Attribute".
     *
     * @return the localized action label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "NewAttributeAction.NewAttributeLabel" ); //$NON-NLS-1$
    }


    // ── Luke's Lightsaber Has a Distinctive Green Glow ───────────────────────
    // You can spot Luke's weapon at a glance by its color — it's visually
    // distinct from every other lightsaber in the room.
    // Our icon serves the same purpose: a quick visual cue in the toolbar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to display for this action in toolbars and menus.
     * We pull the "add attribute" image from the plugin's image registry.
     *
     * @return the {@link ImageDescriptor} for the add-attribute icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_ATTRIBUTE_ADD );
    }


    // ── Luke's Lightsaber Is Registered in the Rebel Arsenal ─────────────────
    // The Rebellion keeps a catalog of every weapon so the right one can be
    // retrieved by ID — "item 7, green blade, Skywalker model."
    // We return the Eclipse command ID that links us to the keybinding system.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.  The command ID is how
     * Eclipse ties us to keyboard shortcuts and the command framework — think
     * of it as our unique slot in the global action registry.
     *
     * @return the command ID string {@code BrowserCommonConstants.CMD_ADD_ATTRIBUTE}
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.CMD_ADD_ATTRIBUTE;
    }


    // ── Luke Can Only Forge When He Has Materials ─────────────────────────────
    // Luke can't build a lightsaber mid-battle with no workbench and no entry
    // to attach it to — he needs a proper context first.
    // We only enable this action when there's actually a target entry around.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled right now.  We need at
     * least one target entry to add the attribute to — either as the editor
     * input, as a selected entry, or derivable from a selected attribute or
     * value.  We also block the action when the selection is a search result
     * with attributes (that's read-only context).
     *
     * @return {@code true} if a valid target entry exists; {@code false} otherwise
     */
    public boolean isEnabled()
    {

        if ( ( getSelectedSearchResults().length == 1 && getSelectedAttributes().length > 0 ) )
        {
            return false;
        }

        return ( ( getInput() instanceof IEntry ) || getSelectedEntries().length == 1
            || getSelectedAttributes().length > 0 || getSelectedValues().length > 0 );
    }
}
