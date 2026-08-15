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


import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;


// ── CLASS: EntryEditorWidgetCellModifier — Han Grabs The Controls Mid-Asteroid-Field ─────
// In The Empire Strikes Back Han yanks the Falcon off autopilot while threading the asteroid
// field.  For each hazard the computer says "can I maneuver there?" — Han checks manually.
// When Chewie reads the sensor value Han reads it too — raw data, no decoration.
// And when Han decides to adjust a trajectory, he does it with one sharp move —
// CommModification.modifyValue() — committing the change right there.
// That's this class: the ICellModifier bridge between the JFace TreeViewer and the LDAP
// model — checking editability, providing raw cell values, and committing modifications.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Implements JFace's {@link ICellModifier} for the entry editor's tree viewer.
 * This is the bridge between the SWT table and the LDAP attribute-value model:
 * it decides which cells are editable, supplies the raw value for the cell editor,
 * and commits the user's edit back to the model via {@link CompoundModification}.
 * Think of this as Han at the Falcon's manual controls — he checks whether a
 * maneuver is possible, reads the current sensor value, and executes the adjustment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetCellModifier implements ICellModifier
{

    /** The value editor manager. */
    private ValueEditorManager valueEditorManager;


    // ── Han Takes The Controls ────────────────────────────────────────────────────────────
    // Han gets handed the ValueEditorManager — the system that knows which editor applies
    // to each attribute value type.  Without it, Han can't check whether a value is editable
    // or get its raw representation for the inline editor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new cell modifier wired to the given {@link ValueEditorManager}.
     * The manager is consulted in {@link #canModify}, {@link #getValue}, and {@link #modify}
     * to determine the appropriate editor for each attribute value type.
     *
     * <p>For example — Han grabs the cockpit controls:</p>
     * <pre>
     *   this.valueEditorManager = valueEditorManager;
     *   // now we can ask: which editor handles this value? can we modify it?
     * </pre>
     *
     * @param valueEditorManager  the manager that knows which value editor to use for each
     *                            LDAP attribute type; must not be {@code null} during use
     */
    public EntryEditorWidgetCellModifier( ValueEditorManager valueEditorManager )
    {
        this.valueEditorManager = valueEditorManager;
    }


    // ── Han Powers Down The Controls ──────────────────────────────────────────────────────
    // When the entry editor widget closes, we null out the manager reference so the garbage
    // collector can reclaim everything.  Han powers down the manual control panel after landing.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the {@link ValueEditorManager}.
     * Call this when the entry editor widget is being disposed so the manager can be
     * garbage collected — Han parking the Falcon and powering down all systems.
     */
    public void dispose()
    {
        valueEditorManager = null;
    }


    // ── Han Checks Whether He Can Take Control Of A Specific Thruster ────────────────────
    // Before Han grabs a control, he checks: is this an element he can actually modify?
    // The "key" column (attribute type name) is always read-only — you can't change the
    // type label inline here.  The "value" column is editable only if the current value
    // editor for this attribute type says it has a value to work with.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given {@code element} at the given {@code property}
     * (column) is editable.
     * <ul>
     *   <li>The key column (attribute type name) is never editable inline.</li>
     *   <li>The value column is editable if the value editor for this attribute type reports
     *       that it has a value — i.e. the value is not a placeholder or dummy.</li>
     * </ul>
     *
     * <p>For example — Han's editability check:</p>
     * <pre>
     *   if ( KEY_COLUMN )   return false;   // attribute name is read-only
     *   if ( VALUE_COLUMN ) return valueEditorManager.getCurrentValueEditor( v ).hasValue( v );
     *   return false; // unknown column
     * </pre>
     *
     * @param element   the model object for the row (expected to be an {@link IValue})
     * @param property  the column name — one of {@code KEY_COLUMN_NAME} or {@code VALUE_COLUMN_NAME}
     * @return {@code true} if the cell may be edited inline
     */
    public boolean canModify( Object element, String property )
    {
        if ( ( element instanceof IValue ) && ( valueEditorManager != null ) )
        {
            IValue attributeValue = ( IValue ) element;

            if ( EntryEditorWidgetTableMetadata.KEY_COLUMN_NAME.equals( property ) )
            {
                return false;
            }

            if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                return valueEditorManager.getCurrentValueEditor( attributeValue ).hasValue( attributeValue );
            }
        }

        return false;
    }


    // ── Han Reads The Sensor Readout For A Specific Control ───────────────────────────────
    // Chewie calls out a sensor value; Han reads it off the display — raw, unformatted,
    // as the computer stores it.  For the key column Han reads the attribute description
    // string; for the value column Han asks the current value editor for the raw representation
    // (which might be text, a byte array, or a custom object depending on the editor type).
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw cell value for the given {@code element} and {@code property}.
     * This is the value handed to the cell editor as its initial input:
     * <ul>
     *   <li>Key column → the attribute type description string (e.g. {@code "cn"})</li>
     *   <li>Value column → the raw value from {@code valueEditorManager.getCurrentValueEditor().getRawValue()}</li>
     *   <li>Unknown column → empty string</li>
     * </ul>
     *
     * <p>For example — Han reads the sensor:</p>
     * <pre>
     *   KEY_COLUMN   → "mail"               // attribute type name
     *   VALUE_COLUMN → "han@falcon.galaxy"  // raw string (or byte[], etc.)
     * </pre>
     *
     * @param element   the model object for the row (expected to be an {@link IValue})
     * @param property  the column name — one of {@code KEY_COLUMN_NAME} or {@code VALUE_COLUMN_NAME}
     * @return the raw value suitable for initialising the cell editor, or {@code null} if
     *         {@code element} is not an {@link IValue} or the manager is gone
     */
    public Object getValue( Object element, String property )
    {
        if ( ( element instanceof IValue ) && ( valueEditorManager != null ) )
        {
            IValue attributeValue = ( IValue ) element;
            Object returnValue;
            if ( EntryEditorWidgetTableMetadata.KEY_COLUMN_NAME.equals( property ) )
            {
                returnValue = attributeValue.getAttribute().getDescription();
            }
            else if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                returnValue = this.valueEditorManager.getCurrentValueEditor( attributeValue ).getRawValue(
                    attributeValue );
            }
            else
            {
                returnValue = ""; //$NON-NLS-1$
            }
            return returnValue;
        }
        else
        {
            return null;
        }
    }


    // ── Han Executes The Maneuver — Commits The Edit ──────────────────────────────────────
    // After the user confirms their edit, JFace calls this to apply the new value.
    // Han checks that the element is a valid IValue (unwrapping SWT Item wrappers first),
    // that the new raw value is not null (null means the editor was cancelled), and that
    // the property is the value column.  If everything checks out, he calls
    // CompoundModification.modifyValue() to atomically replace the old value with the new one.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Commits a cell editor result to the LDAP model.
     * JFace may wrap the element in an SWT {@link Item}; we unwrap it first.
     * If the new raw value is not {@code null} and the element is an {@link IValue}, we call
     * {@link CompoundModification#modifyValue(IValue, Object)} to apply the change.
     * A {@code null} {@code newRawValue} means the edit was cancelled — nothing happens.
     *
     * <p>For example — Han adjusts the trajectory:</p>
     * <pre>
     *   if ( element instanceof Item ) element = ((Item) element).getData();
     *   if ( newRawValue != null && element instanceof IValue ) {
     *       new CompoundModification().modifyValue( (IValue) element, newRawValue );
     *   }
     * </pre>
     *
     * @param element      the model object (or SWT {@link Item} wrapping it) for the edited row
     * @param property     the column name — only {@code VALUE_COLUMN_NAME} triggers a modification
     * @param newRawValue  the new value from the cell editor, or {@code null} if editing was cancelled
     */
    public void modify( Object element, String property, Object newRawValue )
    {
        if ( element instanceof Item )
        {
            element = ( ( Item ) element ).getData();
        }

        if ( ( newRawValue != null ) && ( element instanceof IValue ) && ( valueEditorManager != null ) )
        {
            IValue oldValue = ( IValue ) element;

            if ( EntryEditorWidgetTableMetadata.VALUE_COLUMN_NAME.equals( property ) )
            {
                new CompoundModification().modifyValue( oldValue, newRawValue );
            }
        }
    }

}
