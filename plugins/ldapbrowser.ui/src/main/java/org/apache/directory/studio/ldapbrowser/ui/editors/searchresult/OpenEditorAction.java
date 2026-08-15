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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import java.util.Arrays;

import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;


// ── CLASS: OpenEditorAction — Clone Trooper Assigned a Specific Weapon ────────
// Not all troopers carry the same weapon — some are snipers, some carry rocket
// launchers, some have flamethrowers.  This action is the specialist trooper:
// it's handed a specific value editor at construction time and only uses that one,
// regardless of what the "best editor" logic might prefer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens a specific, named value editor on the currently selected cell.
 * Unlike {@link OpenBestEditorAction} which auto-selects the most appropriate editor,
 * this one is hard-wired to a particular {@link IValueEditor} instance — it appears
 * as an "alternative editor" option in the context menu's "Edit Value" submenu.
 * Think of this as the specialist trooper who always carries their assigned weapon
 * and only engages when called upon specifically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEditorAction extends AbstractOpenEditorAction
{

    /** The value editor. */
    private IValueEditor valueEditor;


    // ── Specialist Trooper Receives Their Assigned Weapon ────────────────────
    // The armorer hands this trooper their specific blaster and says "you carry this one."
    // We wire in the specific editor at construction time and immediately set it
    // as the cell editor in the parent — it never changes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action bound to a specific value editor.
     * The editor's cell editor widget is immediately set on the superclass so it's
     * ready to be installed into the table viewer when the action runs.
     *
     * @param viewer             the JFace TableViewer showing search results
     * @param cursor             tracks the currently selected cell
     * @param valueEditorManager manages all available value editors
     * @param valueEditor        the specific editor this action will always open
     * @param actionGroup        manages global action handler lifecycle
     */
    public OpenEditorAction( TableViewer viewer, SearchResultEditorCursor cursor,
        ValueEditorManager valueEditorManager, IValueEditor valueEditor, SearchResultEditorActionGroup actionGroup )
    {
        super( viewer, cursor, valueEditorManager, actionGroup );
        super.cellEditor = valueEditor.getCellEditor();
        this.valueEditor = valueEditor;
    }


    // ── Trooper Reports Their Weapon Assignment ───────────────────────────────
    // The CO asks "what are you armed with?" — the trooper names their specific
    // weapon.  The action group needs this to de-duplicate the context menu
    // (don't show an alternative that's the same as the best editor).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the specific value editor this action is bound to.
     * The action group uses this to filter out editors that duplicate the best editor
     * from the "alternative editors" submenu.
     *
     * @return the {@link IValueEditor} instance fixed at construction time
     */
    public IValueEditor getValueEditor()
    {
        return valueEditor;
    }


    // ── Specialist Trooper Marks Their Weapon as the User's Choice ────────────
    // Before opening the cell editor, the trooper flags to the ValueEditorManager
    // "the user explicitly picked me" — so if the value is later saved, the
    // manager knows this was a deliberate choice rather than the automatic one.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Marks this editor as the user's explicit choice before delegating to the parent.
     * Calling {@link ValueEditorManager#setUserSelectedValueEditor} before {@code super.run()}
     * ensures the manager remembers the user's preference for this editing session.
     */
    public void run()
    {
        valueEditorManager.setUserSelectedValueEditor( valueEditor );
        super.run();
    }


    // ── Trooper Returns Weapon to Armory ─────────────────────────────────────
    // Mission over — the trooper hands back their specialist weapon before leaving.
    // We null out our editor reference and let the superclass clean up.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the value editor reference and delegates to the superclass.
     * Call this when the action group is being disposed.
     */
    public void dispose()
    {
        valueEditor = null;
        super.dispose();
    }


    // ── Specialist Trooper Has No Standing Order ID ───────────────────────────
    // Specialist troopers work on specific assignments, not standing orders —
    // they don't have a global keybinding, only context-menu presence.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no global command ID.
     * Alternative value editors appear only in the context menu, not via keybinding.
     *
     * @return {@code null}
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Trooper Wears Their Specialist Badge ──────────────────────────────────
    // The specialist trooper is identified by their distinctive insignia — the icon
    // comes from the value editor itself so the menu item is visually self-identifying.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon from the fixed value editor.
     * The context menu uses this to make each alternative editor visually distinct.
     *
     * @return the value editor's image descriptor; never null while the editor is alive
     */
    public ImageDescriptor getImageDescriptor()
    {
        return valueEditor.getValueEditorImageDescriptor();
    }


    // ── Trooper Announces Their Specialty ─────────────────────────────────────
    // "I'm the password trooper" — the label comes directly from the editor so
    // the context menu item reads like the editor's own name.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name from the fixed value editor.
     *
     * @return the value editor's display name (e.g. "Password Editor", "Image Editor")
     */
    public String getText()
    {
        return valueEditor.getValueEditorName();
    }


    // ── Specialist Trooper Checks if Their Weapon Fits the Target ────────────
    // The specialist only steps forward if their weapon is on the approved list
    // for this target AND they can actually get a raw value out of the attribute.
    // If neither condition holds, they stand aside.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only when this specific editor is an alternative option
     * for the selected attribute and can produce a raw value from it.
     * We require exactly one result and property selected, and verify the editor
     * appears in the alternative editors list for the selected attribute hierarchy.
     *
     * @return {@code true} if this editor can handle the currently selected cell
     */
    public boolean isEnabled()
    {
        if ( getSelectedSearchResults().length == 1 && getSelectedProperties().length == 1
            && viewer.getCellModifier().canModify( getSelectedSearchResults()[0], getSelectedProperties()[0] ) )
        {
            IValueEditor[] alternativeVps;
            if ( getSelectedAttributeHierarchies().length == 0 )
            {
                return false;
            }
            else
            {
                AttributeHierarchy ah = getSelectedAttributeHierarchies()[0];
                alternativeVps = valueEditorManager.getAlternativeValueEditors( ah );
                return Arrays.asList( alternativeVps ).contains( valueEditor ) && valueEditor.getRawValue( ah ) != null;
            }
        }
        else
        {
            return false;
        }
    }

}
