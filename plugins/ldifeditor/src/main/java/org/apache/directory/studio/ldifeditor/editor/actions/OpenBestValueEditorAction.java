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


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.valueeditors.AbstractDialogValueEditor;
import org.apache.directory.studio.valueeditors.IValueEditor;


// ── CLASS: OpenBestValueEditorAction — R2-D2 PICKS THE BEST ROUTE ─────────────
// R2-D2 surveys all available escape routes from the Death Star and picks the
// one most likely to succeed for the current situation — he does not just
// suggest the first route in the list.
// OpenBestValueEditorAction does the same: it asks the ValueEditorManager for
// the "current" best editor for the selected attribute type, and if that one
// is not a dialog editor, walks the alternatives until it finds one that can
// open a dialog and also produce a non-null raw value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens the best-matching value editor for the currently selected
 * {@link org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine}.
 * Resolves the editor by first asking
 * {@link org.apache.directory.studio.valueeditors.ValueEditorManager#getCurrentValueEditor},
 * then falling back through the alternative list until an
 * {@link AbstractDialogValueEditor} with a non-null raw value is found.
 * Think of this as R2-D2 choosing the best escape route automatically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenBestValueEditorAction extends AbstractOpenValueEditorAction
{

    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code OpenBestValueEditorAction} bound to {@code editor}.
     *
     * @param editor  the LDIF editor this action operates on
     */
    public OpenBestValueEditorAction( LdifEditor editor )
    {
        super( editor );
    }


    // ── PICK THE BEST EDITOR AND REFRESH APPEARANCE ──────────────────────────
    // R2 checks the schema, finds the current best editor, and if it cannot
    // open a dialog, walks the alternatives until he finds one that can.
    /**
     * {@inheritDoc}
     *
     * <p>Selects the best available {@link IValueEditor} for the selected
     * attribute, updates the action's label and image, and sets enablement
     * accordingly.</p>
     */
    public void update()
    {
        super.setEnabled( isEditableLineSelected() );

        // determine value editor
        IBrowserConnection connection = getConnection();
        String attributeDescription = getAttributeDescription();

        if ( attributeDescription != null )
        {
            valueEditor = valueEditorManager.getCurrentValueEditor( connection.getSchema(), attributeDescription );
            Object rawValue = getValueEditorRawValue();
            if ( !( valueEditor instanceof AbstractDialogValueEditor ) || rawValue == null )
            {
                IValueEditor[] vps = valueEditorManager.getAlternativeValueEditors( connection.getSchema(),
                    attributeDescription );
                for ( int i = 0; i < vps.length
                    && ( !( valueEditor instanceof AbstractDialogValueEditor ) || rawValue == null ); i++ )
                {
                    valueEditor = vps[i];
                    rawValue = getValueEditorRawValue();
                }
            }
        }

        if ( valueEditor != null )
        {
            setText( valueEditor.getValueEditorName() );
            setImageDescriptor( valueEditor.getValueEditorImageDescriptor() );
        }
    }

}
