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


import java.util.Collection;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;


// ── CLASS: OpenBestEditorAction — Clone Trooper with the Right Tool ───────────
// Order 66 goes out but not all troopers carry the same weapon — the smartest
// trooper picks up the weapon best suited to the target before acting.
// This action asks the ValueEditorManager "what's the best editor for this attribute
// type?" and then arms itself with exactly that widget before opening the cell editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the most appropriate value editor for the currently selected cell in the
 * search result table.  We ask {@link ValueEditorManager} which editor fits this
 * attribute type best, cache it, and delegate the actual opening to the superclass.
 * Before editing starts, we also warn the user if the attribute is read-only or
 * not in the entry's schema — giving them a chance to back out.
 * Think of this as the trooper who picks up exactly the right blaster before
 * executing Order 66 on their target.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenBestEditorAction extends AbstractOpenEditorAction
{

    /** The best value editor. */
    private IValueEditor bestValueEditor;


    // ── Trooper Receives Assignment ───────────────────────────────────────────
    // The trooper reports to the armory and checks in — they don't yet know which
    // weapon they'll carry; that's determined at mission time when they know the target.
    // We wire up all the standard collaborators; the best editor is resolved later
    // in isEnabled() when we know what attribute the cursor is over.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action with all the collaborators it needs.
     * We don't resolve the best editor here — that happens in {@link #isEnabled()}
     * once we know what's under the cursor.
     *
     * @param viewer             the JFace TableViewer showing search results
     * @param cursor             tracks the currently selected cell
     * @param valueEditorManager determines the best editor for each attribute type
     * @param actionGroup        manages global action handler activation/deactivation
     */
    public OpenBestEditorAction( TableViewer viewer, SearchResultEditorCursor cursor,
        ValueEditorManager valueEditorManager, SearchResultEditorActionGroup actionGroup )
    {
        super( viewer, cursor, valueEditorManager, actionGroup );
    }


    // ── Trooper Reports What Weapon They're Carrying ──────────────────────────
    // The commanding officer asks which weapon was chosen for this mission.
    // Other parts of the UI (e.g. the action group building the "Edit Value" submenu)
    // need to know which editor was selected so they can exclude it from the
    // "alternative editors" list.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor that was selected as the best fit for the current cell.
     * The action group uses this when populating the "Edit Value" submenu — it shows
     * this editor at the top and excludes it from the alternatives list.
     *
     * @return the {@link IValueEditor} chosen during the last call to {@link #isEnabled()},
     *         or {@code null} if the action has never been enabled
     */
    public IValueEditor getBestValueEditor()
    {
        return this.bestValueEditor;
    }


    // ── Trooper Returns Their Weapon to the Armory ────────────────────────────
    // After the mission the trooper hands back their weapon — we null out the
    // best editor reference so we don't hold stale resources.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the best value editor and delegates to the superclass.
     * Call this when the action group is being torn down to avoid memory leaks.
     */
    public void dispose()
    {
        bestValueEditor = null;
        super.dispose();
    }


    // ── Trooper Has No Standing Order ID ─────────────────────────────────────
    // This action isn't wired to a global command key — it's context-sensitive and
    // appears only in menus, not via keyboard shortcut.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action, or {@code null} if none.
     * We don't register a global command for the "best editor" action; it's driven
     * by context (what's under the cursor) rather than a fixed keybinding.
     *
     * @return {@code null} — no command ID
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Trooper Wears the Right Insignia ─────────────────────────────────────
    // Each trooper type has a different badge — the best-editor action borrows its
    // icon from whatever editor was selected, so the menu item looks like that editor.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action, borrowed from the best value editor.
     * When enabled, the menu item shows the same icon as the chosen editor so the
     * user can recognize it.  Returns {@code null} when disabled.
     *
     * @return the image descriptor from the best value editor, or {@code null} if not enabled
     */
    public ImageDescriptor getImageDescriptor()
    {
        return isEnabled() ? bestValueEditor.getValueEditorImageDescriptor() : null;
    }


    // ── Trooper Announces the Target ─────────────────────────────────────────
    // The trooper calls out the mission target by name — same pattern: the label
    // is taken from the chosen editor so the menu item is self-descriptive.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this action, borrowed from the best value editor.
     * When shown in the context menu the item reads like the editor's own name
     * (e.g. "Text Editor", "Password Editor").  Returns {@code null} when disabled.
     *
     * @return the name from the best value editor, or {@code null} if not enabled
     */
    public String getText()
    {
        return isEnabled() ? bestValueEditor.getValueEditorName() : null;
    }


    // ── Trooper Checks the Target is Viable Before Acting ────────────────────
    // Before pulling the trigger, the trooper confirms: is there exactly one target?
    // Is the target modifiable?  The best editor is resolved here — the trooper
    // picks up their weapon only once they know the mission is a go.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether this action can run right now.
     * We require exactly one selected search result and one selected property, and
     * the cell must be modifiable.  If those conditions hold, we ask the
     * {@link ValueEditorManager} for the best editor and cache it.
     *
     * <p>For example — CT-7567 confirms the target before acting:</p>
     * <pre>
     *   one result selected? yes
     *   one property selected? yes
     *   cell modifiable? yes
     *   → pick best editor → arm cell editor → return true
     * </pre>
     *
     * @return {@code true} if all preconditions are met and a best editor was found
     */
    public boolean isEnabled()
    {
        if ( getSelectedSearchResults().length == 1 && getSelectedProperties().length == 1
            && viewer.getCellModifier().canModify( getSelectedSearchResults()[0], getSelectedProperties()[0] ) )
        {
            if ( getSelectedAttributeHierarchies().length == 0 )
            {
                bestValueEditor = valueEditorManager.getCurrentValueEditor( getSelectedSearchResults()[0].getEntry(),
                    getSelectedProperties()[0] );
            }
            else
            {
                bestValueEditor = valueEditorManager.getCurrentValueEditor( getSelectedAttributeHierarchies()[0] );
            }

            super.cellEditor = bestValueEditor.getCellEditor();
            return true;
        }
        else
        {
            super.cellEditor = null;
            return false;
        }
    }


    // ── Trooper Warns About a Risky Target Before Firing ─────────────────────
    // A wise trooper pauses and asks the CO "are you sure?" when the target might
    // be off-limits — read-only attributes or attributes not in the schema.
    // We show a confirmation dialog in those cases; if the user says no, we abort.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the action, first validating that the selected attribute is safe to edit.
     * If the attribute is marked non-modifiable by the schema, or is not in the
     * entry's subschema, we present a warning dialog and only proceed if the user
     * confirms.  This prevents accidental writes to operational or read-only attributes.
     *
     * <p>For example — the trooper warns: "This target has diplomatic immunity, sir."</p>
     * <pre>
     *   if (attribute is read-only or out of schema) {
     *     show dialog: "Are you sure you want to edit this?"
     *     if (user says no) return;
     *   }
     *   super.run();  // proceed with editing
     * </pre>
     */
    public void run()
    {
        boolean ok = true;

        // validate non-modifiable attributes
        AttributeHierarchy[] attributeHierarchies = getSelectedAttributeHierarchies();
        if ( attributeHierarchies.length == 1 )
        {
            AttributeHierarchy attributeHierarchy = attributeHierarchies[0];
            StringBuffer message = new StringBuffer();

            if ( attributeHierarchy.size() == 1 && attributeHierarchy.getAttribute().getValueSize() == 0 )
            {
                // validate if value is allowed
                IEntry entry = attributeHierarchy.getAttribute().getEntry();
                Collection<AttributeType> allAtds = SchemaUtils.getAllAttributeTypeDescriptions( entry );
                AttributeType atd = attributeHierarchy.getAttribute().getAttributeTypeDescription();
                if ( !allAtds.contains( atd ) )
                {
                    message.append( NLS.bind( Messages.getString( "OpenBestEditorAction.AttributeNotInSubSchema" ), //$NON-NLS-1$
                        attributeHierarchy.getAttribute().getDescription() ) );
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }

            if ( attributeHierarchy.size() == 1
                && attributeHierarchy.getAttribute().getValueSize() == 1
                && attributeHierarchy.getAttributeDescription().equalsIgnoreCase(
                    attributeHierarchy.getAttribute().getValues()[0].getAttribute().getDescription() )
                && !attributeHierarchy.getAttribute().getValues()[0].isRdnPart() )
            {
                // validate non-modifiable attributes
                IValue value = attributeHierarchy.getAttribute().getValues()[0];
                if ( !value.isEmpty() && !SchemaUtils.isModifiable( value.getAttribute().getAttributeTypeDescription() ) )
                {
                    message
                        .append( NLS
                            .bind(
                                Messages.getString( "OpenBestEditorAction.EditValueNotModifiable" ), value.getAttribute().getDescription() ) ); //$NON-NLS-1$
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                    message.append( BrowserCoreConstants.LINE_SEPARATOR );
                }
            }

            if ( message.length() > 0 )
            {
                message.append( Messages.getString( "OpenBestEditorAction.EditValueQuestion" ) ); //$NON-NLS-1$
                ok = MessageDialog.openConfirm( getShell(), getText(), message.toString() );
            }
        }

        if ( ok )
        {
            super.run();
        }
    }

}
