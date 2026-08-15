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


import org.apache.directory.studio.common.ui.ClipboardUtils;
import org.apache.directory.studio.ldapbrowser.common.actions.PasteAction;
import org.apache.directory.studio.ldapbrowser.common.dnd.ValuesTransfer;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;


// ── CLASS: EntryEditorPasteAction — Han Slots A Stolen Data Chip Into The Nav System ─────
// Han has just grabbed a stolen Imperial data chip containing route coordinates.
// He slots it into the Falcon's nav computer and the system reads the waypoints off the chip
// and adds them to the current flight plan — no reboot required, no comms to mission control,
// just a direct write into the local nav model.  The real upload to the Imperial flight network
// happens later, handled separately by the EntryEditorManager.
// That's exactly this class: paste LDAP attribute values from the clipboard into the current
// entry's local model, without touching the actual LDAP server yet.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Paste action for the tabular entry editor — copies {@link IValue} objects from the clipboard
 * into the currently displayed LDAP entry's in-memory model.
 * This intentionally skips the LDAP server update; the actual directory modification is
 * handled asynchronously by the EntryEditorManager once the local model is dirty.
 * Think of this as Han slotting a data chip into the Falcon's nav computer — the waypoints
 * appear in the local flight plan immediately, and the hyperspace jump comes later.
 */
public class EntryEditorPasteAction extends PasteAction
{

    // ── Han Grabs An Empty Chip Slot ──────────────────────────────────────────────────────
    // Han picks up a blank chip carrier before he has any data to put in it.
    // The no-arg constructor just calls the parent constructor; all real work happens in run().
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryEditorPasteAction.
     * All setup is inherited from {@link PasteAction}; no additional initialisation is needed here.
     */
    public EntryEditorPasteAction()
    {
        super();
    }


    // ── Han Reads The Chip Label ──────────────────────────────────────────────────────────
    // The menu item label adapts based on how many values are on the chip: "Paste Value"
    // for one, "Paste Values" for more than one, and a generic "Paste" if the chip is empty
    // or unrecognised.  Han glances at the chip's header to figure out what to call it.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a context-sensitive label for this action based on the clipboard contents.
     * If the clipboard holds a single {@link IValue}, we say "Paste Value."
     * If it holds multiple, we say "Paste Values."
     * If there is nothing pasteable, we fall back to the generic "Paste."
     *
     * <p>For example — Han reads the chip label:</p>
     * <pre>
     *   IValue[] values = getValuesToPaste();
     *   if ( values.length > 1 ) return "Paste Values";
     *   if ( values.length == 1 ) return "Paste Value";
     *   return "Paste"; // nothing on the chip
     * </pre>
     *
     * @return the localised action label appropriate for the current clipboard state
     */
    @Override
    public String getText()
    {
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            return values.length > 1 ? Messages.getString( "EntryEditorPasteAction.PasteValues" ) : Messages.getString( "EntryEditorPasteAction.PasteValue" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return Messages.getString( "EntryEditorPasteAction.Paste" ); //$NON-NLS-1$
    }


    // ── Han Checks Whether The Chip Has Usable Data ──────────────────────────────────────
    // Before lighting up the "paste" option in the menu, we check whether the clipboard
    // actually contains LDAP values — not plain text, not a file path, not an image.
    // Han inspecting the chip to confirm it has compatible nav data before enabling the slot.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the clipboard currently holds {@link IValue} objects that can
     * be pasted into the active entry.  The action is disabled when the clipboard is empty
     * or contains non-LDAP data — Han won't slot in a chip he can't read.
     *
     * @return {@code true} if there are pasteable values on the clipboard
     */
    @Override
    public boolean isEnabled()
    {
        if ( getValuesToPaste() != null )
        {
            return true;
        }

        return false;
    }


    // ── Han Slots The Chip Into The Nav Computer ──────────────────────────────────────────
    // Han pulls the values off the clipboard chip, figures out which entry (IEntry) is
    // currently displayed in the editor, and creates new value objects in that entry's
    // in-memory model.  No LDAP wire operation here — just a local model update.
    // The EntryEditorManager watches the model and will push the change to the server later.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the paste: reads {@link IValue} objects from the clipboard, resolves the
     * target {@link IEntry} from the current editor input, and calls
     * {@link CompoundModification#createValues(IEntry, IValue[])} to add them to the model.
     * The directory is not contacted here — the EntryEditorManager handles that asynchronously.
     *
     * <p>For example — Han slots the chip in:</p>
     * <pre>
     *   IValue[] values = getValuesToPaste();
     *   IEntry target = (IEntry) getInput();          // current entry
     *   new CompoundModification().createValues( target, values );
     *   // model is now dirty; EntryEditorManager will push to server
     * </pre>
     */
    @Override
    public void run()
    {
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            IEntry entry = null;
            if ( getInput() instanceof IEntry )
            {
                entry = ( IEntry ) getInput();
            }
            else if ( getInput() instanceof AttributeHierarchy )
            {
                entry = ( ( AttributeHierarchy ) getInput() ).getEntry();
            }

            if ( entry != null )
            {
                // only modify the model
                // the modification at the directory is done by EntryEditorManager
                new CompoundModification().createValues( entry, values );
            }
        }
    }


    // ── Han Inspects The Clipboard Chip ───────────────────────────────────────────────────
    // Before any of the above methods does anything, they need to know what's actually on
    // the clipboard.  This private helper checks two things: is the editor input a valid
    // target (an IEntry or an AttributeHierarchy)?  And does the clipboard contain IValue[]
    // objects put there by a previous copy/cut?  Both must be true for paste to work.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks conditions and returns the values to paste, or {@code null} if paste is not possible.
     * Two conditions must both hold:
     * <ol>
     *   <li>The editor's current input is an {@link IEntry} or {@link AttributeHierarchy}
     *       (we have a valid destination entry)</li>
     *   <li>The clipboard contains an {@code IValue[]} via {@link ValuesTransfer}
     *       (we have something to paste)</li>
     * </ol>
     *
     * <p>For example — Han verifies the chip and the slot:</p>
     * <pre>
     *   if ( getInput() instanceof IEntry ) {
     *       Object content = ClipboardUtils.getFromClipboard( ValuesTransfer.getInstance() );
     *       if ( content instanceof IValue[] ) return (IValue[]) content;
     *   }
     *   return null; // nothing to paste
     * </pre>
     *
     * @return the array of {@link IValue} objects from the clipboard, or {@code null}
     *         if the preconditions for paste are not met
     */
    private IValue[] getValuesToPaste()
    {
        if ( ( getInput() instanceof IEntry ) || ( getInput() instanceof AttributeHierarchy ) )
        {
            Object content = ClipboardUtils.getFromClipboard( ValuesTransfer.getInstance() );

            if ( content instanceof IValue[] )
            {
                IValue[] values = ( IValue[] ) content;

                return values;
            }
        }

        return null;
    }

}
