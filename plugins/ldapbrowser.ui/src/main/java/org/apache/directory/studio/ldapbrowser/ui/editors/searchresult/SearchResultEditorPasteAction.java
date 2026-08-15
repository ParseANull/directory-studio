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


import org.apache.directory.studio.common.ui.ClipboardUtils;
import org.apache.directory.studio.ldapbrowser.common.actions.PasteAction;
import org.apache.directory.studio.ldapbrowser.common.dnd.ValuesTransfer;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;


// ── CLASS: SearchResultEditorPasteAction — Clone Troopers Executing Order 66 ──
// Commander Cody receives the order from Palpatine: "Execute Order 66."
// He doesn't question it — he just carries it out, precisely and immediately.
// The clones transfer the right payload (IValues from the clipboard) to the
// right target (the selected attribute cell) and write it to the in-memory model.
// No LDAP network call yet — that happens separately when the entryUpdateListener
// detects the in-memory change.  The clones just move the cargo.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The paste action for the search result editor.
 * It reads {@link IValue} objects from the clipboard and writes them to the
 * currently selected attribute in the search result table.
 * Unlike a generic paste, we only modify the in-memory model here — the
 * {@code SearchResultEditor.entryUpdateListener} detects the change and dispatches
 * the actual LDAP update to the server.
 * Think of the clones executing Order 66: fast, precise, no hesitation, but the
 * real consequence (saving to the directory) comes a moment later.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorPasteAction extends PasteAction
{

    // ── Clone Trooper Reports for Duty ────────────────────────────────────────
    // The trooper is instantiated and stands ready.  No setup needed — the
    // superclass handles everything until the order arrives.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new paste action.
     * Delegates fully to the superclass constructor.
     */
    public SearchResultEditorPasteAction()
    {
        super();
    }


    // ── Clone Trooper Reports the Order Name ──────────────────────────────────
    // The action label tells the user what they're about to do.  If there's one
    // value on the clipboard it says "Paste Value"; multiple → "Paste Values";
    // nothing → just "Paste" (and the action will be disabled anyway).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label for this action.
     * The label is plural when multiple values are on the clipboard, singular
     * for one, and generic "Paste" when nothing pasteable is found.
     *
     * @return the action label string; never null
     */
    public String getText()
    {
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            return values.length > 1 ? Messages.getString( "SearchResultEditorPasteAction.PasteValues" ) : Messages.getString( "SearchResultEditorPasteAction.PasteValue" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return Messages.getString( "SearchResultEditorPasteAction.Paste" ); //$NON-NLS-1$
    }


    // ── Clone Trooper Checks His Orders ───────────────────────────────────────
    // Before acting, the trooper checks whether the order is valid — is there
    // actually something pasteable on the clipboard for the current selection?
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there are {@link IValue} objects on the clipboard
     * that can be pasted into the currently selected attribute.
     * The full eligibility check is in {@link #getValuesToPaste()}.
     *
     * @return {@code true} if the paste operation is valid right now
     */
    public boolean isEnabled()
    {
        if ( getValuesToPaste() != null )
        {
            return true;
        }

        return false;
    }


    // ── Clone Trooper Executes the Order ──────────────────────────────────────
    // The order comes: take the clipboard values, wrap them with the target
    // attribute, and write them to the in-memory model via CompoundModification.
    // The actual directory write happens later — we just move the cargo here.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Pastes the clipboard values into the currently selected attribute.
     * We wrap each clipboard {@link IValue} with the target {@link IAttribute}
     * to produce new values, then call {@link CompoundModification#createValues}
     * to update the in-memory model.
     * The actual LDAP save is triggered by {@code SearchResultEditor.entryUpdateListener}
     * when it sees the resulting {@code ValueAddedEvent}.
     */
    public void run()
    {
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            IAttribute attribute = getSelectedAttributeHierarchies()[0].getAttribute();
            IEntry entry = attribute.getEntry();

            IValue[] newValues = new IValue[values.length];
            for ( int v = 0; v < values.length; v++ )
            {
                newValues[v] = new Value( attribute, values[v].getRawValue() );
            }

            // only modify the model
            // the modification at the directory is done by SearchResultEditor.entryUpdateListener
            new CompoundModification().createValues( entry, newValues );
        }
    }


    // ── Clone Trooper Checks the Cargo Bay ────────────────────────────────────
    // A private helper: confirm that exactly one search result and one single-valued
    // attribute are selected, and that the clipboard holds IValues.  If all checks
    // pass, return the values; otherwise return null to signal "not ready".
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IValue} array to paste, or {@code null} if the selection
     * or clipboard state doesn't meet the required conditions.
     * Conditions:
     * <ul>
     *   <li>Exactly one search result is selected</li>
     *   <li>Exactly one attribute hierarchy (with exactly one attribute) is selected</li>
     *   <li>No other object types (entries, bookmarks, values, etc.) are selected</li>
     *   <li>The clipboard contains an {@code IValue[]} via {@link ValuesTransfer}</li>
     * </ul>
     *
     * @return the values to paste, or {@code null} if conditions are not met
     */
    private IValue[] getValuesToPaste()
    {
        if ( getSelectedEntries().length + getSelectedBookmarks().length + getSelectedValues().length
            + getSelectedAttributes().length + getSelectedSearches().length == 0
            && getSelectedSearchResults().length == 1
            && getSelectedAttributeHierarchies().length == 1
            && getSelectedAttributeHierarchies()[0].size() == 1 )
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
