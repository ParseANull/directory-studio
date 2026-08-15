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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.ldapbrowser.common.actions.DeleteAllValuesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.NewAttributeAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.EntryEditorActionProxy;
import org.apache.directory.studio.utils.ActionUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: EntryEditorWidgetActionGroupWithAttribute — Leia Extends The Yavin Briefing ───
// The main briefing (EntryEditorWidgetActionGroup) covered the basics: copy, paste, delete.
// But then Leia steps up to the holographic display and extends it — this editor also lets
// you add whole new attribute types (New Attribute), wipe all values from an attribute at
// once (Delete All Values), and rename the attribute description entirely.
// So this subclass is the briefing room with Leia's extensions: all of Dodonna's pilots
// plus three more for attribute-level operations, with an updated toolbar and context menu.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Extended action group used when the entry editor also needs to support attribute-level
 * operations — specifically creating new attribute types, deleting all values in an attribute,
 * and editing the attribute description (renaming the attribute type).
 * Used on the "Attributes" page of the New Entry wizard and the full entry editor where
 * attribute-type editing is permitted.
 * Think of this as Leia extending Dodonna's Yavin briefing with three extra mission roles.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetActionGroupWithAttribute extends EntryEditorWidgetActionGroup
{

    /** The Constant editAttributeDescriptionAction. */
    private static final String EDIT_ATTRIBUTE_DESCRIPTION_ACTION = "editAttributeDescriptionAction"; //$NON-NLS-1$

    /** The Constant newAttributeAction. */
    private static final String NEW_ATTRIBUTE_ACTION = "newAttributeAction"; //$NON-NLS-1$

    /** The Constant deleteAllValuesAction. */
    private static final String DELETE_ALL_VALUES_ACTION = "deleteAllValuesAction"; //$NON-NLS-1$


    // ── Leia Assigns The Additional Pilots ────────────────────────────────────────────────
    // After the base briefing creates the standard action roster, Leia adds three more:
    // an action for editing an attribute description, one for creating a brand new attribute,
    // and one for wiping all values from an attribute in one shot.
    // All three are stored in the inherited entryEditorActionMap under their own keys.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the extended action group by first calling the parent constructor (all standard
     * actions) and then adding three attribute-specific actions to the shared action map:
     * <ul>
     *   <li>{@code EDIT_ATTRIBUTE_DESCRIPTION_ACTION} — rename an attribute type</li>
     *   <li>{@code NEW_ATTRIBUTE_ACTION} — add a brand-new attribute to the entry</li>
     *   <li>{@code DELETE_ALL_VALUES_ACTION} — remove every value from the selected attribute</li>
     * </ul>
     *
     * <p>For example — Leia extends the roster:</p>
     * <pre>
     *   entryEditorActionMap.put( EDIT_ATTRIBUTE_DESCRIPTION_ACTION, ... );
     *   entryEditorActionMap.put( NEW_ATTRIBUTE_ACTION, ... );
     *   entryEditorActionMap.put( DELETE_ALL_VALUES_ACTION, ... );
     * </pre>
     *
     * @param mainWidget     the entry editor widget this group belongs to
     * @param configuration  provides the viewer, value editor manager, and preferences
     */
    public EntryEditorWidgetActionGroupWithAttribute( EntryEditorWidget mainWidget,
        EntryEditorWidgetConfiguration configuration )
    {
        super( mainWidget, configuration );
        TreeViewer viewer = mainWidget.getViewer();

        entryEditorActionMap.put( EDIT_ATTRIBUTE_DESCRIPTION_ACTION, new EntryEditorActionProxy( viewer,
            new EditAttributeDescriptionAction( viewer ) ) );
        entryEditorActionMap.put( NEW_ATTRIBUTE_ACTION, new EntryEditorActionProxy( viewer, new NewAttributeAction() ) );
        entryEditorActionMap.put( DELETE_ALL_VALUES_ACTION, new EntryEditorActionProxy( viewer,
            new DeleteAllValuesAction() ) );
    }


    // ── Leia Posts The Extended Toolbar Roster ─────────────────────────────────────────────
    // The extended toolbar adds two extra buttons beyond the base group: "New Attribute" sits
    // next to "New Value" so both creation options are always visible, and "Delete All Values"
    // sits next to "Delete" for destructive operations.  Leia updating the mission board.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the toolbar with the full set of actions including the attribute-specific ones.
     * Layout: New Value, New Attribute, separator, Delete, Delete All Values, separator, Quick Filter.
     *
     * <p>For example — Leia's extended toolbar:</p>
     * <pre>
     *   [New Value] [New Attribute] | [Delete] [Delete All Values] | [Quick Filter]
     * </pre>
     *
     * @param toolBarManager  the Eclipse toolbar manager to populate
     */
    @Override
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( ( IAction ) entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        toolBarManager.add( ( IAction ) entryEditorActionMap.get( NEW_ATTRIBUTE_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( ( IAction ) this.entryEditorActionMap.get( DELETE_ACTION ) );
        toolBarManager.add( ( IAction ) this.entryEditorActionMap.get( DELETE_ALL_VALUES_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( this.showQuickFilterAction );
        toolBarManager.update( true );
    }


    // ── Leia Updates The Right-Click Briefing With Attribute Operations ───────────────────
    // The context menu gains "New Attribute" at the top (before "New Value"), the "Delete All
    // Values" option moves into an "Advanced" submenu (to avoid accidental mass-deletion), and
    // the "Edit Attribute Description" item appears in the edit section.  Leia's extended
    // mission briefing adds Rogue Squadron to the standard Red/Gold squadron assignments.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the right-click context menu with the full attribute-aware set of actions.
     * Compared to the base group's menu: "New Attribute" is prepended, "Delete All Values"
     * is tucked inside an "Advanced" submenu, and "Edit Attribute Description" appears in
     * the edit section between the value-editor options.
     *
     * <p>For example — Leia's extended right-click menu:</p>
     * <pre>
     *   New Attribute
     *   New Value
     *   ---
     *   Copy | Paste | Delete | Select All
     *   Advanced ▶  [Delete All Values]
     *   ---
     *   Edit Attribute Description
     *   Edit Value / Edit Value With ▶
     *   ---
     *   Properties
     * </pre>
     *
     * @param menuManager  the menu manager to populate; already cleared by the menu listener
     */
    @Override
    protected void contextMenuAboutToShow( IMenuManager menuManager )
    {
        // new
        menuManager.add( ( IAction ) entryEditorActionMap.get( NEW_ATTRIBUTE_ACTION ) );
        menuManager.add( ( IAction ) entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        menuManager.add( new Separator() );

        // copy, paste, delete
        menuManager.add( ( IAction ) entryEditorActionMap.get( COPY_ACTION ) );
        menuManager.add( ( IAction ) entryEditorActionMap.get( PASTE_ACTION ) );
        menuManager.add( ( IAction ) entryEditorActionMap.get( DELETE_ACTION ) );
        menuManager.add( ( IAction ) entryEditorActionMap.get( SELECT_ALL_ACTION ) );
        MenuManager copyMenuManager = new MenuManager( Messages
            .getString( "EntryEditorWidgetActionGroupWithAttribute.Advanced" ) ); //$NON-NLS-1$
        copyMenuManager.add( ( IAction ) entryEditorActionMap.get( DELETE_ALL_VALUES_ACTION ) );
        menuManager.add( copyMenuManager );
        menuManager.add( new Separator() );

        // edit
        menuManager.add( ( IAction ) entryEditorActionMap.get( EDIT_ATTRIBUTE_DESCRIPTION_ACTION ) );
        super.addEditMenu( menuManager );
        menuManager.add( new Separator() );

        // properties
        menuManager.add( ( IAction ) entryEditorActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }


    // ── Leia Patches The Extended Pilots Into The Alliance Comms ─────────────────────────
    // In addition to the standard Ctrl+C/V/Delete handlers from the base group, we also
    // register keyboard shortcuts for "New Attribute" and "Edit Attribute Description"
    // so users can invoke them without right-clicking.
    // Leia opening two extra comm channels for Rogue Squadron alongside the standard ones.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Activates global Eclipse action handlers for all base actions plus the two
     * attribute-specific ones: New Attribute and Edit Attribute Description.
     * These become reachable via their configured keyboard shortcuts while this editor has focus.
     */
    @Override
    public void activateGlobalActionHandlers()
    {
        super.activateGlobalActionHandlers();

        IAction naa = ( IAction ) entryEditorActionMap.get( NEW_ATTRIBUTE_ACTION );
        ActionUtils.activateActionHandler( naa );
        IAction eada = ( IAction ) entryEditorActionMap.get( EDIT_ATTRIBUTE_DESCRIPTION_ACTION );
        ActionUtils.activateActionHandler( eada );
    }


    // ── Leia Closes The Extra Comm Channels ───────────────────────────────────────────────
    // When the editor loses focus, we deactivate the extra shortcut handlers alongside all
    // the standard ones from the base group — Rogue Squadron stands down along with Red/Gold.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Deactivates global Eclipse action handlers for all base actions plus the two
     * attribute-specific ones: New Attribute and Edit Attribute Description.
     * Called when the editor loses focus so shortcuts revert to whatever the focused view needs.
     */
    @Override
    public void deactivateGlobalActionHandlers()
    {
        super.deactivateGlobalActionHandlers();

        IAction naa = ( IAction ) entryEditorActionMap.get( NEW_ATTRIBUTE_ACTION );
        ActionUtils.deactivateActionHandler( naa );
        IAction eada = ( IAction ) entryEditorActionMap.get( EDIT_ATTRIBUTE_DESCRIPTION_ACTION );
        ActionUtils.deactivateActionHandler( eada );
    }

}
