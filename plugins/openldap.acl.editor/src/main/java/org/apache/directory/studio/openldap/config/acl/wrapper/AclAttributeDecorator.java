/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.wrapper;

import org.apache.directory.studio.common.ui.TableDecorator;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.acl.dialogs.AclAttributeDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

// ── CLASS: AclAttributeDecorator — C-3PO PRESENTING THE MANIFEST TO THE CREW ─
// C-3PO doesn't just translate the ACL attribute — he also presents it to the
// crew in the right format: an image icon on the left, a human-readable label
// on the right, and a sort order so the list stays tidy. This decorator plugs
// into JFace's TableViewer machinery to provide exactly that: label text,
// an optional icon, comparison order, and the dialog used to edit or add a row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link TableDecorator} that controls how {@link AclAttributeWrapper}
 * objects appear in the attribute table widget. Provides the label text, an
 * optional icon, and comparison logic for sorting. Also wires up the
 * {@link AclAttributeDialog} so the table widget knows which dialog to open
 * when the user clicks Add or Edit.
 * Think of this class as C-3PO standing at the table, presenting each droid's
 * credentials in a clear, sortable format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAttributeDecorator extends TableDecorator<AclAttributeWrapper>
{
    /** The associated image, if any */
    private Image image;

    // ── Wiring the Decorator to the Add/Edit Dialog ───────────────────────────
    // C-3PO is introduced to the edit terminal at Cloud City. He registers the
    // dialog that opens when the user wants to add or change an attribute row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new decorator and registers the {@link AclAttributeDialog} as
     * the add/edit dialog for the table widget. The dialog is pre-loaded with
     * the given shell and connection so it can open schema lookup dropdowns.
     *
     * <p>For example — C-3PO registering his services at the table control panel:</p>
     * <pre>
     *   AclAttributeDecorator dec = new AclAttributeDecorator(shell, connection);
     *   tableWidget.setDecorator(dec);
     * </pre>
     *
     * @param parentShell  The parent Shell for the add/edit dialog.
     * @param connection   The LDAP browser connection used for schema lookups.
     */
    public AclAttributeDecorator( Shell parentShell, IBrowserConnection connection )
    {
        setDialog( new AclAttributeDialog( parentShell, connection ) );
    }


    // ── Storing the Table Row Icon ─────────────────────────────────────────────
    // C-3PO attaches the right badge icon to every row in the table — the same
    // icon for all attribute rows in this decorator instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the {@link Image} used as the row icon in the table. All rows
     * managed by this decorator share the same icon.
     *
     * @param image  The image to use; may be {@code null} for no icon.
     */
    public void setImage( Image image )
    {
        this.image = image;
    }


    // ── Providing the Display Label for a Row ─────────────────────────────────
    // C-3PO reads out the attribute descriptor string when the table widget
    // needs to know what text to show in the cell.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for the given table element. Delegates to
     * {@link AclAttributeWrapper#getAclAttribute()#toString()} which produces
     * the prefixed attribute name (e.g. {@code uid}, {@code @inetOrgPerson}).
     *
     * <p>For example — C-3PO reading out the attribute name for the table cell:</p>
     * <pre>
     *   decorator.getText(wrapper("@inetOrgPerson")); // → "@inetOrgPerson"
     * </pre>
     *
     * @param element  The table element; expected to be an {@link AclAttributeWrapper}.
     * @return         The display string, or the super-class default for unknown types.
     */
    public String getText( Object element )
    {
        if ( element instanceof AclAttributeWrapper )
        {
            return ( ( AclAttributeWrapper ) element ).getAclAttribute().toString();
        }

        return super.getText( element );
    };


    // ── Providing the Row Icon ────────────────────────────────────────────────
    // C-3PO retrieves the icon for the row — always the same image for all
    // attribute rows handled by this decorator instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image for the given table element. Currently returns the
     * single image set by {@link #setImage(Image)} for every row (or {@code null}
     * if no image has been set).
     *
     * @param element  The table element (unused; same image for all rows).
     * @return         The row icon, or {@code null} if none was set.
     */
    public Image getImage( Object element )
    {
        return image;
    };


    // ── Comparing Two Rows for Sort Order ─────────────────────────────────────
    // C-3PO compares two attribute rows so the table sorter can keep them in
    // alphabetical order. Null-safe: null rows sort after non-null ones.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link AclAttributeWrapper} rows for sorting. Delegates to
     * {@link AclAttributeWrapper#compareTo(AclAttributeWrapper)} which compares
     * by attribute name (case-insensitive). Null-safe: a null left operand is
     * considered less than everything; two nulls are equal.
     *
     * <p>For example — the JFace sorter calling compare:</p>
     * <pre>
     *   compare(wrapper("uid"), wrapper("cn")); // → positive (uid &gt; cn)
     *   compare(wrapper("cn"), wrapper("uid")); // → negative (cn &lt; uid)
     * </pre>
     *
     * @param e1  The first wrapper; may be {@code null}.
     * @param e2  The second wrapper; may be {@code null}.
     * @return    A negative, zero, or positive integer.
     */
    @Override
    public int compare( AclAttributeWrapper e1, AclAttributeWrapper e2 )
    {
        if ( e1 != null )
        {
            if ( e2 == null )
            {
                return 1;
            }
            else
            {
                return e1.compareTo( e2 );
            }
        }
        else
        {
            if ( e2 == null )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
