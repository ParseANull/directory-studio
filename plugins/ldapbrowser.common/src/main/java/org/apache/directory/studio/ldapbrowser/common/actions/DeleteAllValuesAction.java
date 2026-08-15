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


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;


// ── CLASS: DeleteAllValuesAction — CLONE TROOPERS WIPING A SQUAD UNDER ORDER 66
// When Order 66 is issued, a clone trooper doesn't stop at shooting one Jedi —
// he targets the entire squad. DeleteAllValuesAction works the same way: when
// exactly one value of a multi-value attribute is selected, this action expands
// the scope to every value of that attribute and deletes all of them in one shot.
// It's the difference between "remove this one email address" and "remove the
// entire mail attribute." The base DeleteAction handles the warning dialog and
// execution; this class just overrides getValues() to return the full attribute
// set instead of the single selected value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A targeted variant of {@link DeleteAction} that deletes all values of an
 * attribute when exactly one value from a multi-value attribute is selected.
 * This effectively removes the entire attribute (all its values) rather than
 * just the selected value. Enabled only when one value is selected and its
 * attribute has more than one value — otherwise there is nothing "extra" to remove.
 * Think of this class as the Order 66 for multi-value attributes: one trigger,
 * wipe the whole squad.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteAllValuesAction extends DeleteAction
{

    private static final Collection<IValue> EMPTY_VALUES = new HashSet<IValue>();


    // ── THE CLONE TROOPER REPORTS FOR DUTY ────────────────────────────────────
    // Default constructor — no configuration needed. The trooper awaits the
    // order; the action logic lives entirely in the overridden methods below.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DeleteAllValuesAction. No arguments required — all
     * targeting logic is in the overridden {@link #getValues()} method.
     */
    public DeleteAllValuesAction()
    {
    }


    // ── ORDER 66 EXECUTED — DELEGATE TO BASE ──────────────────────────────────
    // The actual deletion logic lives in the base class: gather targets, warn,
    // confirm, and execute. This override just ensures the call chain works
    // correctly with the overridden getValues() below.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Executes the delete-all-values operation. Delegates to
     * {@link DeleteAction#run()}, which collects targets via {@link #getValues()},
     * shows a confirmation dialog, and dispatches the deletion.
     *
     * <p>For example — the order arrives and the trooper executes:</p>
     * <pre>
     *   super.run(); // all values of the attribute are targeted
     * </pre>
     */
    public void run()
    {
        super.run();
    }


    // ── THE TROOPER IDENTIFIES THE ENTIRE ATTRIBUTE SQUAD ─────────────────────
    // The label names the specific attribute when exactly one value is selected
    // (e.g., "Delete Attribute 'mail'"), giving the user precise context. When
    // the selection is ambiguous, it falls back to a generic "Delete Attribute"
    // label.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action. When exactly one value is
     * selected, the label names the specific attribute (e.g., "Delete Attribute
     * 'mail'"); otherwise, it shows the generic "Delete Attribute" label.
     *
     * @return the localized action label, with or without the attribute name.
     */
    public String getText()
    {
        if ( getSelectedValues().length == 1 )
        {
            return NLS
                .bind(
                    Messages.getString( "DeleteAllValuesAction.DeleteAttributeX" ), getSelectedValues()[0].getAttribute().getDescription() ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "DeleteAllValuesAction.DeleteAttribute" ); //$NON-NLS-1$
        }
    }


    // ── THE TROOPER HOLDS UP THE DELETE-ALL INSIGNIA ──────────────────────────
    // Uses the same "delete all" icon as DeleteAllAction — visually marks this
    // as a bulk operation.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "delete all" icon from the browser's image registry, the
     * same icon used by {@link DeleteAllAction}.
     *
     * @return the delete-all image descriptor.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_DELETE_ALL );
    }


    // ── NO STANDARD KEYBINDING ────────────────────────────────────────────────
    // This action has no keyboard shortcut — it's menu-only.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no Eclipse command ID and no
     * keyboard shortcut binding.
     *
     * @return null.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── THE TROOPER CHECKS THAT THE ORDER IS VALID ────────────────────────────
    // Before firing, the trooper confirms that there is actually something to
    // shoot. Delegates to the base class isEnabled(), which checks whether the
    // expanded values set (from overridden getValues()) is non-empty.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there are values to delete. Delegates to
     * {@link DeleteAction#isEnabled()}, which uses our overridden
     * {@link #getValues()} — so this is only enabled when exactly one value
     * of a multi-value attribute is selected.
     *
     * @return {@code true} if the action is currently actionable.
     */
    public boolean isEnabled()
    {
        return super.isEnabled();
    }


    // ── THE TROOPER EXPANDS THE TARGET FROM ONE VALUE TO ALL VALUES ───────────
    // This is the key override: instead of targeting just the selected value,
    // we grab all values of the same attribute. This is only done when exactly
    // one value is selected (no attributes) and that attribute has more than
    // one value — otherwise returning empty means "don't do anything."
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all values of the attribute that the single selected value belongs
     * to. Only operates when exactly one value is selected (not a whole attribute),
     * no attributes are directly selected, and the attribute has more than one
     * value (otherwise a plain Delete would suffice). Returns an empty collection
     * in all other cases.
     *
     * <p>For example — Order 66: one trigger, the whole squad is targeted:</p>
     * <pre>
     *   // user selected "john@example.com" from a multi-value "mail" attribute
     *   // this method returns all values of the "mail" attribute, not just the one
     *   values.addAll( Arrays.asList( selectedValue.getAttribute().getValues() ) );
     * </pre>
     *
     * @return all values of the selected value's attribute, or an empty collection
     *         if the conditions for bulk-delete are not met.
     */
    protected Collection<IValue> getValues()
    {
        if ( getSelectedAttributes().length == 0 && getSelectedValues().length == 1
            && getSelectedValues()[0].getAttribute().getValueSize() > 1 )
        {
            Collection<IValue> values = new HashSet<IValue>();
            values.addAll( Arrays.asList( getSelectedValues()[0].getAttribute().getValues() ) );
            return values;
        }
        else
        {
            return EMPTY_VALUES;
        }
    }
}
