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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SubtreeValueEditor — ISB SECURE-ZONE SELECTOR ─────────────────────
// The ISB terminal for the "subtree" protected-item (or user-class) row opens
// the full SubtreeSpecificationDialog so the officer can define the precise
// slice of the directory to which the ACI item applies: base DN, depth limits,
// exclusions, and an optional filter.
// SubtreeValueEditor is the IValueEditor that opens that terminal.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractDialogStringValueEditor} for the {@code subtreeSpecification}
 * attribute in the ACI visual editor.
 * Opens a {@link SubtreeSpecificationDialog} when activated and stores the
 * serialised subtree specification string as the new cell value.
 * Think of this as the ISB secure-zone selector: parse the existing spec,
 * open the full editor, store the confirmed result.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SubtreeValueEditor extends AbstractDialogStringValueEditor
{
    /** Convenience constant for empty-string comparisons used by the dialog. */
    static final String EMPTY = ""; //$NON-NLS-1$

    private boolean refinementOrFilterVisible;

    private boolean useLocalName;


    // ── FULL-FEATURE CONSTRUCTOR ──────────────────────────────────────────────
    // Default constructor used by the ValueEditorManager via reflection;
    // both refinement/filter and local-name mode are enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default constructor used by the {@link ValueEditorManager}.
     * Enables both the refinement/filter panel and local-name mode.
     */
    public SubtreeValueEditor()
    {
        this.refinementOrFilterVisible = true;
        this.useLocalName = true;
    }


    // ── CONFIGURABLE CONSTRUCTOR ──────────────────────────────────────────────
    // Used by ACI composite widgets that want to control which panels are shown
    // and whether the base DN is displayed as a local or absolute name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code SubtreeValueEditor} with explicit control over the
     * optional panels.
     *
     * @param refinementOrFilterVisible  {@code true} to show the refinement/filter panel
     * @param useLocalName               {@code true} to show the base DN as a local name
     */
    public SubtreeValueEditor( boolean refinementOrFilterVisible, boolean useLocalName )
    {
        this.refinementOrFilterVisible = refinementOrFilterVisible;
        this.useLocalName = useLocalName;
    }


    // ── OPEN THE SUBTREE SPECIFICATION DIALOG ─────────────────────────────────
    // The ISB terminal opens the full SubtreeSpecificationDialog pre-filled from
    // the wrapper.  If the officer confirms, we store the serialised spec string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link SubtreeSpecificationDialog} and, if the user confirms,
     * stores the returned specification string as the new cell value.
     *
     * <p>For example — opening from the subtree protected-item row:</p>
     * <pre>
     *   cellEditor.activate();
     *   // → SubtreeSpecificationDialog opens pre-filled
     *   // user adjusts base and minimum depth
     *   // → setValue("{ base \"ou=people\", minimum 1 }") is called
     * </pre>
     *
     * @param shell  the parent SWT shell
     * @return       {@code true} if the user confirmed a non-null specification
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();
        if ( value instanceof SubtreeSpecificationValueWrapper )
        {
            SubtreeSpecificationValueWrapper wrapper = ( SubtreeSpecificationValueWrapper ) value;

            SubtreeSpecificationDialog dialog = new SubtreeSpecificationDialog( shell, wrapper.connection,
                wrapper.subentryDn, wrapper.subtreeSpecification, refinementOrFilterVisible, useLocalName );
            if ( dialog.open() == TextDialog.OK && dialog.getSubtreeSpecificationValue() != null )
            {
                setValue( dialog.getSubtreeSpecificationValue() );
                return true;
            }
        }
        return false;
    }


    // ── BUILD THE RAW VALUE WRAPPER ───────────────────────────────────────────
    // We extract the connection, entry DN, and string value from the IValue
    // and wrap them in a SubtreeSpecificationValueWrapper for the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link SubtreeSpecificationValueWrapper} containing the
     * connection, subentry DN, and specification string from {@code value}.
     * Returns {@code null} if the super class returns a non-string raw value.
     *
     * @param value  the LDAP attribute value to wrap
     * @return       the wrapper, or {@code null}
     */
    public Object getRawValue( IValue value )
    {
        Object o = super.getRawValue( value );
        if ( o instanceof String )
        {
            IBrowserConnection connection = value.getAttribute().getEntry().getBrowserConnection();
            Dn dn = value.getAttribute().getEntry().getDn();
            return new SubtreeSpecificationValueWrapper( connection, dn, value.getStringValue() );
        }

        return null;
    }

    // ── CLASS: SubtreeSpecificationValueWrapper — CONNECTION + DN + SPEC BUNDLE
    // A private DTO that carries the three contextual pieces to the dialog so
    // it can pre-populate the base DN entry widget and the spec parser.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private DTO that bundles the browser connection, subentry DN, and raw
     * subtree specification string for use by {@link SubtreeSpecificationDialog}.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class SubtreeSpecificationValueWrapper
    {
        /** The connection, used in DnDialog to browse for an entry */
        private IBrowserConnection connection;

        /** The subentry's Dn */
        private Dn subentryDn;

        /** The subtreeSpecification */
        private String subtreeSpecification;


        // ── BUNDLE THE THREE PIECES ───────────────────────────────────────────
        /**
         * Creates a new {@code SubtreeSpecificationValueWrapper}.
         *
         * @param connection             the browser connection for DN browsing
         * @param subentryDn             the DN of the subentry owning the attribute
         * @param subtreeSpecification   the raw subtree specification string
         */
        private SubtreeSpecificationValueWrapper( IBrowserConnection connection, Dn subentryDn,
            String subtreeSpecification )
        {
            this.connection = connection;
            this.subentryDn = subentryDn;
            this.subtreeSpecification = subtreeSpecification;
        }


        // ── STRING REPRESENTATION ─────────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return subtreeSpecification == null ? "" : subtreeSpecification; //$NON-NLS-1$
        }

    }
}
