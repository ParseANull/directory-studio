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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: ValuePropertyPage — LUKE'S BINARY SUNSET ON TATOOINE ───────────────
// Luke stands at his viewpoint and studies one specific detail of the landscape —
// not the whole horizon, just a single rock formation he wants to understand fully:
// what kind of rock it is (String or Binary), how big it is (bytes/chars), and
// what it actually looks like up close (the raw data).
// An LDAP attribute value is exactly like that rock: it belongs to an attribute
// (the formation type / description), it has a type (string vs binary), a size,
// and raw data.  This property page lays all of that out in a clean read-only
// view — no editing, just the full picture.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page displaying detailed information about a single
 * {@link IValue} from an LDAP entry's attribute.
 * Shows the attribute description (e.g. {@code cn}, {@code mail}), whether
 * the value is a String or Binary, its size in characters and bytes, and the
 * raw data itself.
 * String values get a scrollable multi-line text widget; binary values display
 * a simple "(Binary)" label.
 * Think of this as Luke's binary sunset for a single value — the full, clear
 * picture of exactly what this piece of directory data is.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValuePropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

    /** The description text. */
    private Text descriptionText;

    /** The value text. */
    private Text valueText;

    /** The type text. */
    private Text typeText;

    /** The size text. */
    private Text sizeText;


    // ── LUKE ARRIVES UNENCUMBERED ─────────────────────────────────────────────
    // Luke settles in without a datapad or any editing tools — this is a view,
    // not a workshop.  No Apply, no Defaults.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and suppresses the Default and Apply buttons.
     * The value page is entirely read-only; there is nothing to apply or reset.
     *
     * <p>For example — Luke arrives to watch, not to tinker:</p>
     * <pre>
     *   noDefaultAndApplyButton() → clean informational view, no editable controls
     * </pre>
     */
    public ValuePropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── LUKE STUDIES THE ROCK FORMATION IN DETAIL ─────────────────────────────
    // Luke examines four things: what category the formation belongs to
    // (attribute description), whether it's a string or binary rock, how big it
    // is (size in chars and bytes), and what it actually looks like up close
    // (the raw data, scrollable if it's a long string).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI: attribute description, value type, size,
     * and raw data fields.
     * If the value is a string, the data field is a scrollable multi-line
     * read-only {@link Text} widget sized to roughly half the standard dialog
     * width; if binary, it's a single-line label showing "(Binary)".
     * All fields are populated from the {@link IValue} extracted from the
     * selection element via {@link #getValue(Object)}.
     *
     * <p>For example — Luke studies the rock formation in detail:</p>
     * <pre>
     *   Attribute Description: cn
     *   Value Type: String
     *   Value Size: 5 characters (5 bytes)
     *   Data: Admin
     *
     *   Attribute Description: userPassword
     *   Value Type: Binary
     *   Value Size: 20 bytes
     *   Data: (Binary)
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The parent composite (the page fills it directly).
     */
    protected Control createContents( Composite parent )
    {
        IValue value = getValue( getElement() );

        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
        Composite mainGroup = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "ValuePropertyPage.AttributeDescription" ), 1 ); //$NON-NLS-1$
        descriptionText = BaseWidgetUtils.createLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "ValuePropertyPage.ValueType" ), 1 ); //$NON-NLS-1$
        typeText = BaseWidgetUtils.createLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "ValuePropertyPage.ValueSize" ), 1 ); //$NON-NLS-1$
        sizeText = BaseWidgetUtils.createLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "ValuePropertyPage.Data" ), 1 ); //$NON-NLS-1$
        if ( value != null && value.isString() )
        {
            valueText = new Text( mainGroup, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
            valueText.setFont( JFaceResources.getFont( JFaceResources.TEXT_FONT ) );
            GridData gd = new GridData( GridData.FILL_BOTH );
            gd.widthHint = convertHorizontalDLUsToPixels( ( int ) ( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 ) );
            gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 4 );
            valueText.setLayoutData( gd );
            valueText.setBackground( parent.getBackground() );
        }
        else
        {
            valueText = BaseWidgetUtils.createLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$
        }

        if ( value != null )
        {
            super.setMessage( Messages.getString( "ValuePropertyPage.Value" ) //$NON-NLS-1$
                + org.apache.directory.studio.connection.core.Utils.shorten( value.toString(), 30 ) );

            descriptionText.setText( value.getAttribute().getDescription() );
            // valueText.setText(LdifUtils.mustEncode(value.getBinaryValue())?"Binary":value.getStringValue());
            valueText.setText( value.isString() ? value.getStringValue() : Messages
                .getString( "ValuePropertyPage.Binary" ) ); //$NON-NLS-1$
            typeText
                .setText( value.isString() ? Messages.getString( "ValuePropertyPage.String" ) : Messages.getString( "ValuePropertyPage.Binary" ) ); //$NON-NLS-1$ //$NON-NLS-2$

            int bytes = value.getBinaryValue().length;
            int chars = value.isString() ? value.getStringValue().length() : 0;
            String size = value.isString() ? chars
                + ( chars > 1 ? Messages.getString( "ValuePropertyPage.Characters" ) : Messages.getString( "ValuePropertyPage.Character" ) ) : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            size += Utils.formatBytes( bytes );
            sizeText.setText( size );
        }

        return parent;
    }


    // ── LUKE LOCATES THE EXACT ROCK ───────────────────────────────────────────
    // Before Luke can study a formation, he needs to find it — Eclipse hands us
    // an opaque "element" object and we use the IAdaptable pattern to extract the
    // actual IValue from it.  IAdaptable is Eclipse's version of asking "do you
    // know how to give me an X?" — if yes, it returns one; if no, null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the {@link IValue} from the given selection element using
     * Eclipse's {@link IAdaptable} mechanism.
     * Returns {@code null} if the element is not adaptable or has no
     * {@link IValue} adapter.
     *
     * <p>For example — Luke locates the exact rock formation:</p>
     * <pre>
     *   element instanceof IAdaptable → getAdapter(IValue.class) → IValue
     *   element not adaptable         → null
     * </pre>
     *
     * @param element  The raw selection element from the workbench selection;
     *                 typically wraps an {@link IValue} via {@link IAdaptable}.
     * @return         The {@link IValue}, or {@code null} if not found.
     */
    private static IValue getValue( Object element )
    {
        IValue value = null;
        if ( element instanceof IAdaptable )
        {
            value = ( IValue ) ( ( IAdaptable ) element ).getAdapter( IValue.class );
        }
        return value;
    }

}
