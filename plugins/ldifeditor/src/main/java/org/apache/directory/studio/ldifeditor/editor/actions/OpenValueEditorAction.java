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

package org.apache.directory.studio.ldifeditor.editor.actions;


import java.util.Arrays;

import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.valueeditors.IValueEditor;


// ── CLASS: OpenValueEditorAction — SPECIALIST ROUTE SELECTED MANUALLY ─────────
// When the operator wants a specific escape route — not R2's recommendation —
// they pick it by name from the "Edit Value With" sub-menu.  Each item in that
// sub-menu corresponds to one OpenValueEditorAction bound to a specific
// IValueEditor.
// OpenValueEditorAction is enabled only when its particular editor is on the
// list of valid alternatives for the selected attribute type.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens a specific {@link IValueEditor} for the currently selected
 * {@link org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine}.
 * Enabled only when the bound editor is in the list of alternative editors for
 * the selected attribute type and a non-null raw value can be produced.
 * Think of this as a named escape-route chosen manually from the sub-menu.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenValueEditorAction extends AbstractOpenValueEditorAction
{

    // ── CONSTRUCT WITH SPECIFIC EDITOR ───────────────────────────────────────
    /**
     * Creates a new {@code OpenValueEditorAction} that will open
     * {@code valueEditor}.
     *
     * @param editor       the LDIF editor this action operates on
     * @param valueEditor  the specific value editor to open
     */
    public OpenValueEditorAction( LdifEditor editor, IValueEditor valueEditor )
    {
        super( editor );
        super.valueEditor = valueEditor;
    }


    // ── RECOMPUTE ENABLEMENT AND APPEARANCE ──────────────────────────────────
    // The action checks whether its specific editor is among the valid
    // alternatives and whether the raw value resolves, then updates its label
    // and icon.
    /**
     * {@inheritDoc}
     *
     * <p>Enabled when an editable line is selected, the bound editor is in the
     * alternative-editor list for the attribute type, and a non-null raw value
     * can be produced.  Always updates the label and image from the value
     * editor.</p>
     */
    public void update()
    {
        String attributeDescription = getAttributeDescription();
        Object rawValue = getValueEditorRawValue();

        if ( isEditableLineSelected() )
        {
            IValueEditor[] alternativeVps = this.editor.getValueEditorManager().getAlternativeValueEditors(
                getConnection().getSchema(), attributeDescription );
            super.setEnabled( Arrays.asList( alternativeVps ).contains( this.valueEditor ) && rawValue != null );
        }
        else
        {
            super.setEnabled( false );
        }

        setText( valueEditor.getValueEditorName() );
        setImageDescriptor( valueEditor.getValueEditorImageDescriptor() );
    }

}
