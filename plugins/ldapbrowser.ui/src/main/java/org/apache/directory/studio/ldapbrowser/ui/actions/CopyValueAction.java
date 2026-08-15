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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;


// ── CLASS: CopyValueAction — C-3PO ENCODES THE VALUE INTO COMMON TONGUE ─────
// C-3PO is called upon not just to translate language, but to re-encode data
// into whatever format the situation demands — raw UTF-8 for organics, base64
// for transmitting binary over text channels, hex for diagnostics, LDIF for
// protocol interop. This action does the same: it takes raw attribute values
// from the selected LDAP entry and copies them to the clipboard in whichever
// encoding the user requested.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies the raw bytes or display text of selected LDAP attribute values to
 * the system clipboard, re-encoded in one of several formats: UTF-8, base64,
 * hex, LDIF attribute-value line, or the value editor's display representation.
 * The encoding is fixed at construction time via the {@link Mode} enum.
 * Think of this as C-3PO translating the value into whatever encoding the
 * receiving ship's computers need — same data, different dialect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyValueAction extends BrowserAction
{

    public enum Mode
    {
        /**
         * UTF8 Mode.
         */
        UTF8,

        /**
         * Base64 Mode.
         */
        BASE64,

        /**
         * Hexadecimal Mode.
         */
        HEX,

        /**
         * LDIF Mode.
         */
        LDIF,

        /**
         * Display mode, copies the display value.
         */
        DISPLAY,
    }

    private Mode mode;

    private ValueEditorManager valueEditorManager;


    // ── C-3PO Sets His Encoding Dialect ──────────────────────────────────────
    // Before C-3PO can translate, he has to know the target dialect: is this
    // base64 for a binary channel, hex for a diagnostic readout, or UTF-8 for
    // a human to read? He locks in the mode at construction time.
    // We also receive the {@link ValueEditorManager} so we can call the
    // registered value editor for DISPLAY mode translations.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyValueAction} bound to the given encoding mode.
     * Each instance produces one specific encoding — create separate instances
     * for each encoding you want to offer in a menu.
     *
     * @param mode                the target encoding (UTF-8, base64, hex, LDIF, or display)
     * @param valueEditorManager  the manager that resolves the current value editor for
     *                            display-mode encoding; needed for the {@code DISPLAY} mode only
     */
    public CopyValueAction( Mode mode, ValueEditorManager valueEditorManager )
    {
        this.mode = mode;
        this.valueEditorManager = valueEditorManager;
    }


    // ── C-3PO Announces the Encoding Mode ────────────────────────────────────
    // C-3PO tells the crew which translation he's offering: "Copy Value as UTF-8",
    // "Copy Value as Base64", "Copy Value as Hex", and so on — singular or plural
    // depending on how many values are selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised menu label for this action, reflecting both the
     * encoding mode and whether one or multiple values are selected.
     *
     * @return  the display name; never {@code null}
     */
    public String getText()
    {
        switch ( mode )
        {
            case UTF8:
                return getValueSet().size() > 1 ? Messages.getString( "CopyValueAction.CopyValuesUTF" ) : Messages.getString( "CopyValueAction.CopyValueUTF" ); //$NON-NLS-1$ //$NON-NLS-2$
            case BASE64:
                return getValueSet().size() > 1 ? Messages.getString( "CopyValueAction.CopyValuesBase" ) : Messages.getString( "CopyValueAction.CopyValueBase" ); //$NON-NLS-1$ //$NON-NLS-2$
            case HEX:
                return getValueSet().size() > 1 ? Messages.getString( "CopyValueAction.VopyValuesHex" ) : Messages.getString( "CopyValueAction.CopyValueHex" ); //$NON-NLS-1$ //$NON-NLS-2$
            case LDIF:
                return getValueSet().size() > 1 ? Messages.getString( "CopyValueAction.CopyValuePairs" ) : Messages.getString( "CopyValueAction.CopyValuePair" ); //$NON-NLS-1$ //$NON-NLS-2$
            case DISPLAY:
                return getValueSet().size() > 1 ? Messages.getString( "CopyValueAction.CopyDisplayValues" ) : Messages.getString( "CopyValueAction.CopyDisplayValue" ); //$NON-NLS-1$ //$NON-NLS-2$
            default:
                return Messages.getString( "CopyValueAction.CopyValue" ); //$NON-NLS-1$
        }
    }


    // ── C-3PO Selects the Right Encoding Badge ───────────────────────────────
    // C-3PO picks the correct insignia for the encoding he's offering — the UTF-8
    // badge, the base64 badge, the hex badge, and so on — so each menu entry is
    // visually distinct.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the icon representing this encoding mode.
     * Each mode has a distinct icon registered in the plugin's image registry.
     *
     * @return  the appropriate {@link ImageDescriptor}, or {@code null} for unrecognised modes
     */
    public ImageDescriptor getImageDescriptor()
    {
        switch ( mode )
        {
            case UTF8:
                return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_UTF8 );
            case BASE64:
                return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_BASE64 );
            case HEX:
                return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_HEX );
            case LDIF:
                return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_LDIF );
            case DISPLAY:
                return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_DISPLAY );
            default:
                return null;
        }
    }


    // ── C-3PO Checks His Encoding Command Registry ───────────────────────────
    // C-3PO has no registered keyboard shortcut for this encoding operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut binding.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── C-3PO Confirms There's Something Worth Encoding ──────────────────────
    // C-3PO won't transmit an empty message — he checks that at least one
    // value is selected (or a search result whose DN we can encode instead).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if there is at least one attribute value in scope,
     * or at least one search result (whose DN can be encoded in the absence of
     * attribute values).
     *
     * @return  {@code true} if the action can execute with the current selection
     */
    public boolean isEnabled()
    {
        return getValueSet().size() > 0 || getSelectedSearchResults().length > 0;
    }


    // ── C-3PO Encodes All Values and Copies Them ─────────────────────────────
    // C-3PO processes each value in the set through the chosen encoding, joins
    // them with newlines, and transmits the whole batch over the comm channel
    // (the clipboard). If no values are found but a search result row is selected,
    // he falls back to encoding the row's DN instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Encodes all selected attribute values (or the selected search result's DN
     * if no values are in scope) in the configured mode and copies the result to
     * the system clipboard as plain text.
     * Multiple values are newline-separated (except LDIF mode, which produces
     * self-separating lines).
     */
    public void run()
    {
        StringBuffer text = new StringBuffer();
        Set<IValue> valueSet = getValueSet();
        if ( !valueSet.isEmpty() )
        {
            for ( Iterator<IValue> iterator = valueSet.iterator(); iterator.hasNext(); )
            {
                IValue value = iterator.next();
                switch ( mode )
                {
                    case UTF8:
                        text.append( LdifUtils.utf8decode( value.getBinaryValue() ) );
                        if ( iterator.hasNext() )
                        {
                            text.append( BrowserCoreConstants.LINE_SEPARATOR );
                        }
                        break;
                    case BASE64:
                        text.append( LdifUtils.base64encode( value.getBinaryValue() ) );
                        if ( iterator.hasNext() )
                        {
                            text.append( BrowserCoreConstants.LINE_SEPARATOR );
                        }
                        break;
                    case HEX:
                        text.append( LdifUtils.hexEncode( value.getBinaryValue() ) );
                        if ( iterator.hasNext() )
                        {
                            text.append( BrowserCoreConstants.LINE_SEPARATOR );
                        }
                        break;
                    case LDIF:
                        text.append( ModelConverter.valueToLdifAttrValLine( value ).toFormattedString(
                            Utils.getLdifFormatParameters() ) );
                        break;
                    case DISPLAY:
                        IValueEditor ve = valueEditorManager.getCurrentValueEditor( value );
                        String displayValue = ve.getDisplayValue( value );
                        text.append( displayValue );
                        if ( iterator.hasNext() )
                        {
                            text.append( BrowserCoreConstants.LINE_SEPARATOR );
                        }
                        break;
                }
            }
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            Dn dn = getSelectedSearchResults()[0].getDn();
            switch ( mode )
            {
                case UTF8:
                case DISPLAY:
                    text.append( dn.getName() );
                    break;
                case BASE64:
                    text.append( LdifUtils.base64encode( LdifUtils.utf8encode( dn.getName() ) ) );
                    break;
                case HEX:
                    text.append( LdifUtils.hexEncode( LdifUtils.utf8encode( dn.getName() ) ) );
                    break;
                case LDIF:
                    text.append( ModelConverter.dnToLdifDnLine( dn )
                        .toFormattedString( Utils.getLdifFormatParameters() ) );
                    break;
            }
        }

        if ( text.length() > 0 )
        {
            CopyAction.copyToClipboard( new Object[]
                { text.toString() }, new Transfer[]
                { TextTransfer.getInstance() } );
        }
    }


    // ── C-3PO Gathers All Values He Needs to Translate ───────────────────────
    // C-3PO walks the entire selection — attribute hierarchies, attributes, and
    // individual values — collecting every distinct value into an ordered set
    // before he starts encoding. Order matters: we want the output to be stable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the ordered set of {@link IValue} objects from the
     * current selection, covering attribute hierarchies, attributes, and
     * individually selected values.
     * Duplicate value references are silently de-duplicated.
     *
     * @return  a non-null, possibly empty ordered set of values to encode
     */
    protected Set<IValue> getValueSet()
    {
        Set<IValue> valueSet = new LinkedHashSet<IValue>();
        for ( AttributeHierarchy ah : getSelectedAttributeHierarchies() )
        {
            for ( IAttribute att : ah )
            {
                valueSet.addAll( Arrays.asList( att.getValues() ) );
            }
        }
        for ( IAttribute att : getSelectedAttributes() )
        {
            valueSet.addAll( Arrays.asList( att.getValues() ) );
        }
        valueSet.addAll( Arrays.asList( getSelectedValues() ) );
        return valueSet;
    }
}
