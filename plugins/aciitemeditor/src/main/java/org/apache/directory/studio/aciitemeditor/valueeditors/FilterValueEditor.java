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

package org.apache.directory.studio.aciitemeditor.valueeditors;


import org.apache.directory.studio.ldapbrowser.common.dialogs.FilterWidgetDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: FilterValueEditor — ISB SEARCH-FILTER ENTRY TERMINAL ───────────────
// The ISB terminal for the "filter" protected-item row shows a filter builder
// dialog so the officer can compose an LDAP search filter specifying which
// directory entries the ACI item applies to.
// FilterValueEditor is that terminal: open the filter builder, return the
// finished filter string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for the {@code filter} protected-item
 * category in the ACI visual editor.
 * Opens a {@link FilterWidgetDialog} so the user can build an LDAP search
 * filter, and stores the confirmed filter string as the new cell value.
 * Think of this as the ISB search-filter terminal: compose a filter in a
 * dedicated builder dialog, get a validated result string back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterValueEditor extends AbstractDialogStringValueEditor
{

    private static final String EMPTY = ""; //$NON-NLS-1$


    // ── OPEN THE FILTER BUILDER DIALOG ────────────────────────────────────────
    // The ISB terminal opens a full-featured filter builder dialog.
    // If the officer confirms a non-empty filter, we store it and return true.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link FilterWidgetDialog} and, if the user confirms with a
     * non-empty filter, stores it as the new cell value.
     *
     * <p>For example — the table composite opens this editor on double-click:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → FilterWidgetDialog opens
     *   // user builds "(objectClass=person)"
     *   // → setValue("(objectClass=person)") is called on this editor
     * </pre>
     *
     * @param shell  the parent shell for the dialog
     * @return       {@code true} if the user confirmed a non-empty filter string
     */
    public boolean openDialog( Shell shell )
    {
        Object value = getValue();
        if ( value instanceof FilterValueEditorRawValueWrapper )
        {
            FilterValueEditorRawValueWrapper wrapper = ( FilterValueEditorRawValueWrapper ) value;
            FilterWidgetDialog dialog = new FilterWidgetDialog( shell, Messages
                .getString( "FilterValueEditor.dialog.title" ), wrapper.filter, //$NON-NLS-1$
                wrapper.connection );
            if ( dialog.open() == TextDialog.OK && !EMPTY.equals( dialog.getFilter() ) )
            {
                setValue( dialog.getFilter() );
                return true;
            }
        }
        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER ───────────────────────────────────────────
    // We bundle the connection (for schema access in the filter builder) and
    // the current filter string into a wrapper so the dialog can pre-fill.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link FilterValueEditorRawValueWrapper} containing the connection
     * and the current filter string from {@code value}'s string representation.
     * Returns {@code null} if the value is unavailable.
     *
     * @param value  the LDAP attribute value to wrap
     * @return       the raw value wrapper, or {@code null}
     */
    public Object getRawValue( IValue value )
    {
        if ( value != null )
        {
            return getRawValue( value.getAttribute().getEntry().getBrowserConnection(),
                                value.getStringValue() );
        }
        else
        {
            return null;
        }
    }


    // ── ASSEMBLE CONNECTION + FILTER ──────────────────────────────────────────
    // The filter builder needs the browser connection to provide schema context
    // for attribute name completion.  We pack it alongside the filter string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Bundles {@code connection} and the string {@code value} into a raw value
     * wrapper.
     *
     * @param connection  the browser connection for schema context
     * @param value       the current filter string
     * @return            the wrapper, or {@code null} if the connection is missing
     *                    or the value is not a string
     */
    private Object getRawValue( IBrowserConnection connection, Object value )
    {
        if ( connection == null || !( value instanceof String ) )
        {
            return null;
        }

        String filterValue = ( String ) value;
        FilterValueEditorRawValueWrapper wrapper = new FilterValueEditorRawValueWrapper( connection, filterValue );
        return wrapper;
    }

    // ── CLASS: FilterValueEditorRawValueWrapper — CONNECTION + FILTER BUNDLE ──
    // A private DTO carrying the connection and the filter string so they travel
    // together from getRawValue() to openDialog() without losing context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that passes the connection and the current filter string to
     * the {@link FilterWidgetDialog}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class FilterValueEditorRawValueWrapper
    {
        /**
         * The connection, used in FilterDialog to build the list
         * with possible attribute types.
         */
        private IBrowserConnection connection;

        /** The filter, used as initial value in FilterDialog. */
        private String filter;


        // ── BUNDLE THE PIECES ─────────────────────────────────────────────────
        // Pack connection and filter into the wrapper so they reach the dialog
        // as a single object.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code FilterValueEditorRawValueWrapper}.
         *
         * @param connection  the browser connection for the filter builder
         * @param filter      the current LDAP filter string
         */
        private FilterValueEditorRawValueWrapper( IBrowserConnection connection, String filter )
        {
            this.connection = connection;
            this.filter = filter;
        }
    }
}
