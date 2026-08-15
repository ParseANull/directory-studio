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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;


// ── CLASS: SearchResultEditorCellModifier — Han Adjusting the Falcon's Controls ──
// In the asteroid field, Han Solo reaches across the Falcon's console mid-flight
// to flip switches: can the shield take another hit (canModify)? What's the current
// shield reading (getValue)? And when he makes an adjustment, the ship responds
// immediately (modify).  The controls can do different things depending on what
// state the system is in.
// This class is that console: three methods that the JFace table viewer calls to
// check, read, and write individual cell values in the search result table.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements {@link ICellModifier} for the search result editor's table viewer.
 * JFace calls {@link #canModify} before showing a cell editor, {@link #getValue}
 * to seed the editor with the current value, and {@link #modify} to apply the
 * user's change back to the LDAP model.
 * Think of this as Han at the Falcon's controls — reading readouts, checking
 * whether a system can take input, and firing adjustments when it can.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorCellModifier implements ICellModifier
{

    /** The value editor manager. */
    private ValueEditorManager valueEditorManager;

    /** The cursor */
    private SearchResultEditorCursor cursor;


    // ── Han Takes His Seat at the Console ────────────────────────────────────
    // Han slides into the pilot's chair and grabs the controls — the value editor
    // manager tells him which system responds to which input, and the cursor tells
    // him where his hand is on the board.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the cell modifier with its two key collaborators.
     *
     * @param valueEditorManager determines the right editor widget for each LDAP attribute type
     * @param cursor             tracks which table cell is currently under the editing cursor
     */
    public SearchResultEditorCellModifier( ValueEditorManager valueEditorManager, SearchResultEditorCursor cursor )
    {
        this.valueEditorManager = valueEditorManager;
        this.cursor = cursor;
    }


    // ── Han Steps Away From the Console ──────────────────────────────────────
    // Mission over — Han releases the controls so the console can be powered down.
    // We null out the value editor manager to release the reference.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the value editor manager reference.
     * Call this when the search result editor is closing.
     */
    public void dispose()
    {
        valueEditorManager = null;
    }


    // ── Han Checks Whether the System Can Take Input ──────────────────────────
    // Han glances at the shield readout — can this panel take more hits?
    // The DN column is always read-only (you can't edit a distinguished name from here),
    // and for other columns we ask the value editor manager if it can produce a raw value.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the specified cell can be edited.
     * The DN column is always read-only.  For other columns we build a synthetic
     * attribute hierarchy if the attribute doesn't exist yet (so the user can add
     * a new value), then ask the value editor manager whether it can supply a raw value.
     *
     * <p>For example — Han checks the control:</p>
     * <pre>
     *   canModify(result, "cn")  → true if value editor can produce raw value
     *   canModify(result, "dn")  → false — DN column is always read-only
     * </pre>
     *
     * @param element  the {@link ISearchResult} row object from the table
     * @param property the column property name (attribute description or "dn")
     * @return {@code true} if the cell can be edited inline
     */
    public boolean canModify( Object element, String property )
    {
        if ( element instanceof ISearchResult && property != null )
        {
            ISearchResult result = ( ISearchResult ) element;
            AttributeHierarchy ah = result.getAttributeWithSubtypes( property );

            // check Dn
            if ( BrowserUIConstants.DN.equals( property ) )
            {
                return false;
            }

            // attribute dummy
            if ( ah == null )
            {
                ah = new AttributeHierarchy( result.getEntry(), property, new IAttribute[]
                    { new Attribute( result.getEntry(), property ) } );
            }

            // call value editor
            return valueEditorManager.getCurrentValueEditor( ah ).getRawValue( ah ) != null;
        }
        else
        {
            return false;
        }
    }


    // ── Han Reads the Current Instrument Reading ──────────────────────────────
    // Before Han adjusts anything, he reads the current value from the display.
    // We read from the working copy (the cursor's cloned entry) rather than the
    // live entry, so in-flight edits don't corrupt the real data mid-session.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current raw value of the cell, ready to seed the cell editor.
     * We read from the cursor's working copy (a clone of the live entry) so the
     * user can cancel edits without corrupting the actual LDAP entry.
     * Returns {@code null} if the cell isn't modifiable.
     *
     * <p>For example — Han reads the shield display before adjusting:</p>
     * <pre>
     *   Object raw = getValue(result, "cn");
     *   // raw is the value the editor widget will show initially
     * </pre>
     *
     * @param element  the {@link ISearchResult} row
     * @param property the column property name
     * @return the raw value for the cell editor to display, or {@code null} if not modifiable
     */
    public Object getValue( Object element, String property )
    {
        if ( element instanceof ISearchResult && property != null )
        {
            // perform modifications on the clone
            ISearchResult result = cursor.getSelectedSearchResult();
            AttributeHierarchy ah = result.getAttributeWithSubtypes( property );

            if ( !canModify( element, property ) )
            {
                return null;
            }

            if ( ah == null )
            {
                ah = new AttributeHierarchy( result.getEntry(), property, new IAttribute[]
                    { new Attribute( result.getEntry(), property ) } );
            }

            return valueEditorManager.getCurrentValueEditor( ah ).getRawValue( ah );
        }
        else
        {
            return null;
        }
    }


    // ── Han Makes the Adjustment and the Ship Responds ────────────────────────
    // Han flips the switch and the Falcon's systems update immediately — but the
    // ship doesn't send anything to the Empire yet.  We update the in-memory model
    // (the entry clone); the actual LDAP write happens later via the entry-update listener.
    // Three cases: add a new value, delete an existing one, or modify an existing one.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Applies the user's edit to the in-memory LDAP model.
     * We use {@link CompoundModification} to add, delete, or change the attribute
     * value.  We do NOT write to the directory server here — the actual LDAP modify
     * operation is triggered later by the entry-update listener in {@code SearchResultEditor}.
     *
     * <p>For example — Han adjusts the controls:</p>
     * <pre>
     *   if (attribute absent and new value present) → createValue()
     *   if (attribute present and new value null)   → deleteValues()
     *   if (attribute present and new value given)  → modifyValue()
     * </pre>
     *
     * @param element    the table row ({@link Item} wrapper or raw {@link ISearchResult})
     * @param property   the column property name (attribute description)
     * @param newRawValue the new raw value from the cell editor, or {@code null} to delete
     */
    public void modify( Object element, String property, Object newRawValue )
    {
        if ( element instanceof Item )
        {
            element = ( ( Item ) element ).getData();
        }

        if ( element instanceof ISearchResult && property != null )
        {
            // perform modifications on the clone
            ISearchResult result = cursor.getSelectedSearchResult();
            AttributeHierarchy ah = result.getAttributeWithSubtypes( property );

            // switch operation:
            if ( ah == null && newRawValue != null )
            {
                new CompoundModification().createValue( result.getEntry(), property, newRawValue );
            }
            else if ( ah != null && newRawValue == null )
            {
                List<IValue> values = new ArrayList<IValue>();
                for ( IAttribute attribute : ah.getAttributes() )
                {
                    for ( IValue value : attribute.getValues() )
                    {
                        values.add( value );
                    }
                }
                new CompoundModification().deleteValues( values );
            }
            else if ( ah != null && ah.size() == 1 && ah.getAttribute().getValueSize() == 1 && newRawValue != null )
            {
                new CompoundModification().modifyValue( ah.getAttribute().getValues()[0], newRawValue );
            }
        }
    }

}
