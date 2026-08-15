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

package org.apache.directory.studio.valueeditors.integer;


import java.math.BigDecimal;

import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: IntegerValueEditor — C-3PO's Odds Calculator ──────────────────────
// C-3PO's calculation module sits ready in the background, showing current odds in the
// attribute table and opening the full calculation console when Han wants to adjust them.
// This editor does the same: shows the integer inline, validates it, and delegates edits
// to IntegerDialog when the user double-clicks.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A value editor for LDAP attributes that store integer values (stored as strings in LDAP).
 * In the attribute table we display the value as-is (it is already a plain number string);
 * when the user double-clicks we open {@link IntegerDialog} for validated adjustment.
 * Think of this class as C-3PO's odds calculator: it keeps the current figure visible,
 * refuses to accept anything that is not a real integer, and reports the result precisely.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class IntegerValueEditor extends AbstractDialogStringValueEditor
{
    // ── Han Says "Open The Calculation Console, Threepio" ─────────────────────────
    // Han wants to adjust the odds figure, so C-3PO opens his full calculation console.
    // If the current value is malformed C-3PO starts from zero rather than crashing.
    // We open IntegerDialog with the parsed value; on OK we write the new integer string back.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link IntegerDialog} so the user can view and modify the current integer value.
     * If the current attribute value is not a valid integer (empty, or malformed from a previous
     * edit), we treat it as zero and flag it as a new value so the dialog always saves on OK.
     * On OK we write the new value back as a string, which is how LDAP stores integers.
     *
     * <p>For example — Han asks C-3PO to open the odds calculator:</p>
     * <pre>
     *   Han: "Pull up the probability console, Threepio."
     *   C-3PO parses the current value. If malformed: "Starting from zero, sir."
     *   Han adjusts and clicks OK. C-3PO stores the new figure.
     * </pre>
     *
     * @param shell the SWT shell to use as the dialog's parent window
     * @return {@code true} if the user confirmed a changed value, {@code false} if they cancelled
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof String )
        {
            BigDecimal integer = null;
            boolean isNewOrMalformedValue = false;

            try
            {
                integer = new BigDecimal( ( String ) value );
            }
            catch ( NumberFormatException e )
            {
                integer = new BigDecimal( 0 );
                isNewOrMalformedValue = true;
            }

            IntegerDialog dialog = new IntegerDialog( shell, integer );
            if ( dialog.open() == IntegerDialog.OK && ( dialog.isDirty() || isNewOrMalformedValue ) )
            {
                BigDecimal newValue = dialog.getInteger();
                if ( newValue != null )
                {
                    setValue( newValue.toString() );
                    return true;
                }
            }
        }

        return false;
    }
}
