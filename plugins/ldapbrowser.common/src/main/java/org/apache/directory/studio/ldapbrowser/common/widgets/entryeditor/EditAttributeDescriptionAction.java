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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.DeleteAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.EntryEditorActionProxy;
import org.apache.directory.studio.ldapbrowser.common.wizards.AttributeWizard;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;


// ── CLASS: EditAttributeDescriptionAction — Han Relabels A Nav Waypoint In The Computer ──
// In The Empire Strikes Back, Han is at the Falcon's nav console and realises he has a
// waypoint saved under the wrong name — it says "Ord Mantell" but should say "Hoth Rendezvous."
// He opens the coordinate editor, types the correct label, confirms, and the nav computer
// renames every value stored under that waypoint to use the new attribute description.
// That's exactly what this action does: it opens the AttributeWizard so the user can rename
// an LDAP attribute type, then calls CompoundModification.renameValues() on all affected values.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Action that lets the user rename an LDAP attribute type (the "key" column) for a
 * selected attribute or set of values in the entry editor.
 * Renaming an attribute description means keeping all the existing values but re-filing
 * them under a different attribute type name — like Han correcting a mislabelled nav waypoint.
 * The action is enabled using the same enablement logic as {@link DeleteAction} (you need to
 * be able to delete values before you can rename them, because rename = delete + re-create).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditAttributeDescriptionAction extends BrowserAction
{

    /** To avoid duplicate implementations of the isEnabled() code we use a delete action */
    private EntryEditorActionProxy deleteActionProxy;


    // ── Han Sets Up The Waypoint Renaming Tool ────────────────────────────────────────────
    // Han builds a proxy to the delete action — not because he wants to delete anything,
    // but because the enablement rules for renaming are identical to the rules for deleting:
    // you need at least one selected value, and that value must be modifiable.
    // Reusing the delete logic saves us from duplicating complex isEnabled() checks.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EditAttributeDescriptionAction tied to the given viewer.
     * We internally create a {@link DeleteAction} proxy purely to borrow its {@code isEnabled()}
     * logic — the permission rules for renaming and deleting are identical (you need a
     * deletable selection to be able to rename it).
     *
     * <p>For example — Han grabs the delete-logic module:</p>
     * <pre>
     *   deleteActionProxy = new EntryEditorActionProxy( viewer, new DeleteAction() );
     *   // isEnabled() will delegate to deleteActionProxy.getAction().isEnabled()
     * </pre>
     *
     * @param viewer  the JFace {@link Viewer} that shows the entry's attributes and values
     */
    public EditAttributeDescriptionAction( Viewer viewer )
    {
        deleteActionProxy = new EntryEditorActionProxy( viewer, new DeleteAction() );
    }


    // ── Han Identifies This Action By Its Command ID ──────────────────────────────────────
    // Eclipse uses command IDs to wire actions to keyboard shortcuts and menus.  This
    // action's ID lets the platform find and invoke it from anywhere — like the Falcon's
    // IFF transponder code so the Rebel base can identify the ship.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command identifier for this action so it can be bound to keyboard
     * shortcuts and menu contributions via the Eclipse command framework.
     *
     * @return the command ID string {@code ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION}
     */
    @Override
    public String getCommandId()
    {
        return BrowserCommonConstants.ACTION_ID_EDIT_ATTRIBUTE_DESCRIPTION;
    }


    // ── Han Has No Icon For The Renaming Tool ─────────────────────────────────────────────
    // The edit-attribute-description action does not have a toolbar icon — it appears in
    // the context menu as a text-only item.  Han's waypoint-rename shortcut has no picture
    // next to it, just the label.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon descriptor for this action.  This action has no icon — it appears as
     * a text-only entry in the context menu — so we return {@code null}.
     *
     * @return {@code null} — no icon for this action
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Han Reads The Menu Label ──────────────────────────────────────────────────────────
    // The label "Edit Attribute Description" appears in the context menu and tells the user
    // what this action does.  Han checking the console button's printed label before pressing it.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name for this action as it appears in the context menu.
     *
     * @return the translated string for "Edit Attribute Description"
     */
    @Override
    public String getText()
    {
        return Messages.getString( "EditAttributeDescriptionAction.EditAttributeDescription" ); //$NON-NLS-1$
    }


    // ── Han Checks Whether The Rename Button Is Lit ───────────────────────────────────────
    // Before Han can rename a waypoint it must already be deletable — you can't rename
    // something you don't have write access to.  We piggyback on the delete action's check
    // rather than duplicating the same selection and permissions logic here.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the rename action is available given the current viewer selection.
     * We delegate to the {@link DeleteAction} proxy because the permission requirements are
     * identical: you need a non-empty, writable selection to rename an attribute.
     *
     * @return {@code true} if renaming is permitted for the current selection
     */
    @Override
    public boolean isEnabled()
    {
        return deleteActionProxy.getAction().isEnabled();
    }


    // ── Han Starts The Rename Sequence ────────────────────────────────────────────────────
    // Han checks what is selected: a whole attribute (rename all its values at once) or
    // individual values (rename just those).  Either way, he calls renameValues() with
    // the appropriate array of IValue objects.  The rename wizard then takes over.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the rename action: determines whether the selection is a whole attribute or
     * individual values, then calls {@link #renameValues(IValue[])} with the appropriate set.
     * If an entire attribute is selected, all its values are renamed together.
     * If individual values are selected, only those values get the new attribute description.
     *
     * <p>For example — Han picks the right set of waypoints:</p>
     * <pre>
     *   if ( getSelectedAttributes().length == 1 )
     *       renameValues( attribute.getValues() );  // all values in the attribute
     *   else
     *       renameValues( getSelectedValues() );    // just the selected values
     * </pre>
     */
    @Override
    public void run()
    {
        if ( getSelectedAttributes().length == 1 )
        {
            renameValues( getSelectedAttributes()[0].getValues() );
        }
        else if ( getSelectedValues().length > 0 )
        {
            renameValues( getSelectedValues() );
        }
    }


    // ── Han Opens The Waypoint Rename Dialog ─────────────────────────────────────────────
    // Han opens the AttributeWizard pre-filled with the current attribute description.
    // The wizard lets the user type a new attribute name.  If they click OK, Han calls
    // CompoundModification.renameValues() which atomically renames every value in the array.
    // If they cancel, nothing changes — Han presses "abort" and the waypoint stays as-is.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link AttributeWizard} dialog pre-populated with the current attribute
     * description, waits for the user to confirm a new name, and then calls
     * {@link CompoundModification#renameValues(IValue[], String)} to apply the change.
     * If the user cancels the wizard, no modification is made.
     *
     * <p>For example — Han renames the waypoint:</p>
     * <pre>
     *   AttributeWizard wizard = new AttributeWizard( "Edit...", true, false,
     *       values[0].getAttribute().getDescription(), entry );
     *   WizardDialog dialog = new WizardDialog( shell, wizard );
     *   if ( dialog.open() == OK ) {
     *       new CompoundModification().renameValues( values, wizard.getAttributeDescription() );
     *   }
     * </pre>
     *
     * @param values  the array of {@link IValue} objects whose attribute description to rename;
     *                all must belong to the same attribute type
     */
    private void renameValues( final IValue[] values )
    {
        AttributeWizard wizard = new AttributeWizard( Messages
            .getString( "EditAttributeDescriptionAction.EditAttributeDescription" ), true, false, //$NON-NLS-1$
            values[0].getAttribute().getDescription(), values[0].getAttribute().getEntry() );
        WizardDialog dialog = new WizardDialog( Display.getDefault().getActiveShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        if ( dialog.open() == Dialog.OK )
        {
            String newAttributeDescription = wizard.getAttributeDescription();
            new CompoundModification().renameValues( values, newAttributeDescription );
        }
    }

}
