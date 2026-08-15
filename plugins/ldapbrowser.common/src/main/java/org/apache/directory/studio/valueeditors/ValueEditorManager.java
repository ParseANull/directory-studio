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

package org.apache.directory.studio.valueeditors;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ── CLASS: ValueEditorManager — LANDO ROUTES CARGO TO THE RIGHT DEPARTMENT ───
// In Cloud City's operations center, Lando Calrissian stands at the dispatch
// console routing incoming cargo to the right department: tibanna gas goes to
// processing, tourist baggage goes to hospitality, diplomatic packages go to the
// protocol office. He takes preferences into account (VIPs get priority routing),
// can list all available departments, and hands callers the best handler for
// whatever type of cargo just arrived. This class does exactly that for LDAP
// attribute values: it inspects each value's type and routes it to the
// appropriate IValueEditor — text, hex, DN, image, or a specialist plugin.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central dispatcher that maps LDAP attribute values to the right {@link IValueEditor}.
 * It reads user preferences, consults the LDAP schema, and applies a priority chain
 * to pick the best editor: user-forced choice first, then attribute-specific
 * preferences, then syntax-based preferences, then a sensible default (text or hex).
 * Plugin-contributed editors are registered via the
 * {@code org.apache.directory.studio.valueeditors} extension point, discovered at
 * construction time, and kept in a class-name-keyed map for fast lookup.
 * Think of this class as Lando's operations center — he always knows which
 * department handles which type of cargo, and he adjusts routing on the fly
 * when someone with authority overrides the default assignment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueEditorManager
{
    private static final String ATTRIBUTE_TYPE = "attributeType"; //$NON-NLS-1$

    private static final String ATTRIBUTE = "attribute"; //$NON-NLS-1$

    private static final String SYNTAX_OID = "syntaxOID"; //$NON-NLS-1$

    private static final String SYNTAX = "syntax"; //$NON-NLS-1$

    private static final String ICON = "icon"; //$NON-NLS-1$

    private static final String NAME = "name"; //$NON-NLS-1$

    private static final String CLASS = "class"; //$NON-NLS-1$

    /** The extension point ID for value editors */
    private static final String EXTENSION_POINT = BrowserCommonConstants.EXTENSION_POINT_VALUE_EDITORS;

    /** The composite used to create the value editors **/
    private Composite parent;

    /**
     * The value editor explicitly selected by the user. If this
     * member is not null it is always returned as current value editor.
     */
    private IValueEditor userSelectedValueEditor;

    /** The special value editor for multi-valued attributes */
    private MultivaluedValueEditor multiValuedValueEditor;

    /** The special value editor to edit the entry in an wizard */
    private EntryValueEditor entryValueEditor;

    /** The special value editor to rename the entry */
    private RenameValueEditor renameValueEditor;

    /** The default string editor for single-line values */
    private IValueEditor defaultStringSingleLineValueEditor;

    /** The default string editor for multi-line values */
    private IValueEditor defaultStringMultiLineValueEditor;

    /** The default binary editor */
    private IValueEditor defaultBinaryValueEditor;

    /** A map containing all available value editors. */
    private Map<String, IValueEditor> class2ValueEditors;


    // ── LANDO OPENS THE OPERATIONS CENTER AND STAFFS ALL DEPARTMENTS ─────────────
    // Lando walks into the Cloud City operations center, activates every department
    // that was registered in the city manifest (the Eclipse extension registry),
    // and stands up the three always-present departments: multi-cargo, entry editing,
    // and renaming. Finally he records the default handlers for plain text and
    // binary cargo so routing decisions are instant from this point forward.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the manager and eagerly creates every registered value editor.
     * We scan the {@code org.apache.directory.studio.valueeditors} extension point
     * to discover plugin-contributed editors, instantiate them all, and index them
     * by class name. We also set up the three "special" editors (multivalued, entry,
     * rename) and grab references to the default text and hex editors so we can
     * return them quickly during routing decisions.
     *
     * <p>For example — Lando opens the operations center:</p>
     * <pre>
     *   Lando: "All departments, report in."
     *   [Each plugin-contributed editor is instantiated and registered]
     *   Lando: "Multi-cargo department — online. Entry editor — online."
     *   Lando: "Rename department — online. Default text and hex handlers — ready."
     * </pre>
     *
     * @param parent               the SWT composite used as the parent for any editors
     *                             that create SWT controls (in-place editors need this)
     * @param useEntryValueEditor  if {@code true}, the entry editor department is staffed;
     *                             some views don't need it and pass {@code false}
     * @param useRenameValueEditor if {@code true}, the rename editor department is staffed;
     *                             again, not every context needs this
     */
    public ValueEditorManager( Composite parent, boolean useEntryValueEditor, boolean useRenameValueEditor )
    {
        this.parent = parent;
        userSelectedValueEditor = null;

        // init value editor map
        class2ValueEditors = new HashMap<String, IValueEditor>();
        Collection<IValueEditor> valueEditors = createValueEditors( parent );

        for ( IValueEditor valueEditor : valueEditors )
        {
            class2ValueEditors.put( valueEditor.getClass().getName(), valueEditor );
        }

        // special case: multivalued editor
        multiValuedValueEditor = new MultivaluedValueEditor( this.parent, this );
        multiValuedValueEditor.setValueEditorName( Messages.getString( "ValueEditorManager.MulitivaluedEditor" ) ); //$NON-NLS-1$
        multiValuedValueEditor.setValueEditorImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_MULTIVALUEDEDITOR ) );

        // special case: entry editor
        if ( useEntryValueEditor )
        {
            entryValueEditor = new EntryValueEditor( this.parent, this );
            entryValueEditor.setValueEditorName( Messages.getString( "ValueEditorManager.EntryEditor" ) ); //$NON-NLS-1$
            entryValueEditor.setValueEditorImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
                BrowserCommonConstants.IMG_ENTRY_EDITOR ) );
        }

        // special case: rename editor
        if ( useRenameValueEditor )
        {
            renameValueEditor = new RenameValueEditor( this.parent, this );
            renameValueEditor.setValueEditorName( Messages.getString( "ValueEditorManager.RenameEditor" ) ); //$NON-NLS-1$
            renameValueEditor.setValueEditorImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
                BrowserCommonConstants.IMG_RENAME ) );
        }

        // get default editors from value editor map
        defaultStringSingleLineValueEditor = class2ValueEditors.get( InPlaceTextValueEditor.class.getName() );
        defaultStringMultiLineValueEditor = class2ValueEditors.get( TextValueEditor.class.getName() );
        defaultBinaryValueEditor = class2ValueEditors.get( HexValueEditor.class.getName() );
    }


    // ── LANDO SHUTS DOWN ALL DEPARTMENTS ────────────────────────────────────────
    // The Empire has arrived. Lando activates the evacuation and tells every
    // department to power down in an orderly fashion. Each editor is disposed,
    // SWT resources are released, and the parent composite reference is nulled
    // so we don't accidentally use it after the Eclipse workbench closes the view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shuts down all value editors and releases their SWT/JFace resources.
     * We null out the {@code parent} reference after disposal so any lingering
     * calls to this manager fail fast rather than touching a disposed widget.
     * Call this when the view or editor that owns this manager is closing.
     *
     * <p>For example — Lando orders the evacuation:</p>
     * <pre>
     *   Lando: "The Empire's here. All departments — shut down, orderly fashion."
     *   [Each editor.dispose() is called in sequence]
     *   Lando: "Operations center is offline. Clear the decks."
     * </pre>
     */
    public void dispose()
    {
        if ( parent != null )
        {
            userSelectedValueEditor = null;
            multiValuedValueEditor.dispose();

            if ( entryValueEditor != null )
            {
                entryValueEditor.dispose();
            }

            if ( renameValueEditor != null )
            {
                renameValueEditor.dispose();
            }

            defaultStringSingleLineValueEditor.dispose();
            defaultStringMultiLineValueEditor.dispose();
            defaultBinaryValueEditor.dispose();

            for ( IValueEditor ve : class2ValueEditors.values() )
            {
                ve.dispose();
            }

            parent = null;
        }
    }


    // ── LANDO ACCEPTS A VIP OVERRIDE FOR A SPECIFIC DEPARTMENT ──────────────────
    // A VIP passenger arrives with a letter from the Emperor: "All cargo from this
    // passenger must go through Department X — no exceptions." Lando pins the note
    // to the dispatch board. From this moment, every routing call returns Department X
    // regardless of what type the cargo is. Pass null to remove the override.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Forces all subsequent {@code getCurrentValueEditor} calls to return the given
     * editor, overriding the normal schema-based routing. This is how the "Open with
     * ▶ Hex Editor" right-click action works — the user explicitly selects an editor
     * and we pin it here. Pass {@code null} to clear the override and go back to
     * automatic routing.
     *
     * <p>For example — Lando pins the VIP routing note:</p>
     * <pre>
     *   VIP note: "Route everything through the Hex Editor, no matter what type."
     *   Lando: [pins note] "Understood. All routing defers to the Hex Editor until revoked."
     *   Later: setUserSelectedValueEditor(null)
     *   Lando: [removes note] "Back to standard routing. Departments, resume normal ops."
     * </pre>
     *
     * @param userSelectedValueEditor  the editor to force for all routing decisions,
     *                                 or {@code null} to clear the override
     */
    public void setUserSelectedValueEditor( IValueEditor userSelectedValueEditor )
    {
        this.userSelectedValueEditor = userSelectedValueEditor;
    }


    // ── LANDO CHECKS WHETHER A VIP OVERRIDE IS IN EFFECT ────────────────────────
    // Lando glances at the dispatch board to see if there's a pinned VIP note
    // overriding normal routing. If there is, he reports which department it names.
    // If the board is clear, he returns null and routing proceeds normally.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the editor the user explicitly selected via the "Open with ▶" menu,
     * or {@code null} if no override is currently active. Callers (like
     * {@link RenameValueEditor}) use this to detect the override so they can
     * temporarily clear it and avoid infinite recursion.
     *
     * <p>For example — Lando checks the board:</p>
     * <pre>
     *   Lando: "Is there a VIP override pinned right now?"
     *   Board: "Yes — 'Hex Editor, by user request'."
     *   Lando: "Got it. I'll honour that override in the next routing decision."
     * </pre>
     *
     * @return  the user-selected {@link IValueEditor}, or {@code null} if none is set
     */
    public IValueEditor getUserSelectedValueEditor()
    {
        return userSelectedValueEditor;
    }


    // ── LANDO ROUTES CARGO BY SCHEMA AND ATTRIBUTE TYPE ─────────────────────────
    // A cargo manifest arrives with the attribute type written on the label. Lando
    // checks the dispatch board: is there a VIP override? If yes, route there. Next
    // he checks if anyone has filed a custom preference for this exact attribute OID
    // or name. Then he checks the attribute's syntax. If still no match, he looks at
    // whether the cargo is binary (goes to the Hex Editor) or text (goes to the
    // standard text editor).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the best {@link IValueEditor} for a given attribute type, consulting
     * the user-override, attribute preferences, and syntax preferences in that order
     * before falling back to the default text or hex editor based on the syntax.
     *
     * <p>Priority chain:</p>
     * <ol>
     *   <li>User-selected (forced) editor — highest priority</li>
     *   <li>Attribute-type preference (by OID then by name) from preferences</li>
     *   <li>Syntax preference (by numeric OID) from preferences</li>
     *   <li>Default Hex Editor for binary syntaxes, default Text Editor otherwise</li>
     * </ol>
     *
     * <p>For example — Lando routes by attribute type:</p>
     * <pre>
     *   Cargo manifest: "attributeType=jpegPhoto"
     *   Lando: "No VIP override. Checking attribute preferences... jpegPhoto → Image Editor."
     *   Lando: "Routing to Image Editor."
     * </pre>
     *
     * @param schema         the LDAP schema we use to resolve OIDs and syntax information
     * @param attributeType  the attribute type name or OID whose cargo we're routing
     * @return               the most appropriate {@link IValueEditor} for this attribute
     */
    public IValueEditor getCurrentValueEditor( Schema schema, String attributeType )
    {
        // check user-selected (forced) value editor
        if ( userSelectedValueEditor != null )
        {
            return userSelectedValueEditor;
        }

        AttributeType atd = schema.getAttributeTypeDescription( attributeType );
        // check attribute preferences
        Map<String, String> attributeValueEditorMap = BrowserCommonActivator.getDefault().getValueEditorsPreferences()
            .getAttributeValueEditorMap();

        String oidStr = Strings.toLowerCase( atd.getOid() );

        if ( atd.getOid() != null && attributeValueEditorMap.containsKey( oidStr ) )
        {
            return ( IValueEditor ) class2ValueEditors.get( attributeValueEditorMap.get( oidStr ) );
        }
        List<String> names = atd.getNames();

        for ( String name : names )
        {
            String nameStr = Strings.toLowerCase( name );

            if ( attributeValueEditorMap.containsKey( nameStr ) )
            {
                return ( IValueEditor ) class2ValueEditors.get( attributeValueEditorMap.get( nameStr ) );
            }
        }

        // check syntax preferences
        String syntaxNumericOid = SchemaUtils.getSyntaxNumericOidTransitive( atd, schema );
        Map<String, String> syntaxValueEditorMap = BrowserCommonActivator.getDefault().getValueEditorsPreferences()
            .getSyntaxValueEditorMap();

        String syntaxtNumericOidStr = Strings.toLowerCase( syntaxNumericOid );

        if ( ( syntaxNumericOid != null ) && syntaxValueEditorMap.containsKey( syntaxtNumericOidStr ) )
        {
            return ( IValueEditor ) class2ValueEditors.get( syntaxValueEditorMap.get( syntaxtNumericOidStr ) );
        }

        // return default
        LdapSyntax lsd = schema.getLdapSyntaxDescription( syntaxNumericOid );

        if ( SchemaUtils.isBinary( lsd ) )
        {
            return defaultBinaryValueEditor;
        }
        else
        {
            return defaultStringSingleLineValueEditor;
        }
    }


    // ── LANDO ROUTES CARGO USING AN ENTRY'S SCHEMA ──────────────────────────────
    // Same routing logic, but instead of handing Lando a schema directly, the caller
    // hands him an IEntry and he extracts the schema from the entry's connection.
    // A convenience overload so callers don't have to dig out the schema themselves.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Convenience overload of {@link #getCurrentValueEditor(Schema, String)} that
     * extracts the schema from the entry's browser connection rather than requiring
     * the caller to pass it explicitly. Everything else is identical.
     *
     * <p>For example — Lando looks up the schema from the entry:</p>
     * <pre>
     *   Cargo: "I have an entry but I don't know its schema separately."
     *   Lando: "No problem — I'll pull the schema from your connection and route normally."
     * </pre>
     *
     * @param entry          the LDAP entry whose connection schema we use for routing
     * @param attributeType  the attribute type name or OID being routed
     * @return               the most appropriate {@link IValueEditor} for this attribute
     * @see #getCurrentValueEditor( Schema, String )
     */
    public IValueEditor getCurrentValueEditor( IEntry entry, String attributeType )
    {
        return getCurrentValueEditor( entry.getBrowserConnection().getSchema(), attributeType );
    }


    // ── LANDO ROUTES A SPECIFIC VALUE WITH EXTRA SPECIAL-CASE CHECKS ────────────
    // A concrete cargo item arrives — not just a manifest label, but the actual
    // value. Lando applies the usual routing, then checks two special cases:
    // objectClass values always go to the Entry Editor department, and RDN-part
    // values always go to the Rename department (Vader's orders — don't argue).
    // Finally, if routing landed on the single-line text editor, he peeks at the
    // value content itself: if it has newlines, he upgrades it to the multi-line
    // editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the best {@link IValueEditor} for a specific {@link IValue},
     * applying the standard schema-based routing plus two hard-coded special cases:
     * {@code objectClass} values are always sent to the entry editor, and RDN-part
     * values are always sent to the rename editor. If standard routing picks the
     * single-line text editor but the actual string contains newlines, we upgrade
     * to the multi-line text editor.
     *
     * <p>For example — Lando routes a concrete cargo item:</p>
     * <pre>
     *   Cargo: value of attribute 'cn', which happens to be the RDN part.
     *   Lando: "Standard schema says text editor... but this is an RDN part."
     *   Lando: "By standing order, RDN cargo goes to the Rename department."
     *   Cargo: value of 'description' containing a paragraph with newlines.
     *   Lando: "Single-line text editor was selected, but I see newlines in the content."
     *   Lando: "Upgrading to multi-line text editor."
     * </pre>
     *
     * @param value  the concrete LDAP value whose editor we're resolving
     * @return       the most appropriate {@link IValueEditor} for this value
     * @see #getCurrentValueEditor( Schema, String )
     */
    public IValueEditor getCurrentValueEditor( IValue value )
    {
        IAttribute attribute = value.getAttribute();
        IValueEditor ve = getCurrentValueEditor( attribute.getEntry(), attribute.getDescription() );

        // special case objectClass: always return entry editor
        if ( userSelectedValueEditor == null )
        {
            if ( attribute.isObjectClassAttribute() && ( entryValueEditor != null ) )
            {
                return entryValueEditor;
            }

            // special case Rdn attribute: always return rename editor
            if ( value.isRdnPart() && ( renameValueEditor != null ) )
            {
                return renameValueEditor;
            }
        }

        // here the value is known, we can check for single-line or multi-line
        if ( ve == defaultStringSingleLineValueEditor )
        {
            String stringValue = value.getStringValue();

            if ( ( stringValue.indexOf( '\n' ) == -1 ) && ( stringValue.indexOf( '\r' ) == -1 ) )
            {
                ve = defaultStringSingleLineValueEditor;
            }
            else
            {
                ve = defaultStringMultiLineValueEditor;
            }
        }

        return ve;
    }


    // ── LANDO ROUTES A FULL ATTRIBUTE HIERARCHY ──────────────────────────────────
    // A pallet of mixed cargo arrives (an AttributeHierarchy). Lando inspects the
    // pallet: if it's objectClass, it goes to the Entry Editor. If it's one clean
    // RDN value, it goes to Rename. If it's a single-value cargo in a single
    // attribute, he delegates to the per-value routing. Otherwise the pallet goes
    // to the Multi-cargo department to handle the complexity.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the best {@link IValueEditor} for an {@link AttributeHierarchy} — a
     * structure that may contain one or more attributes and their values. We walk
     * through several special cases (null, objectClass, RDN, single-value) before
     * falling back to the multi-valued editor for everything else.
     *
     * <p>For example — Lando inspects the pallet:</p>
     * <pre>
     *   Pallet: null — Lando: "No cargo here. I'll return null."
     *   Pallet: objectClass attribute — Lando: "Always routes to the Entry Editor."
     *   Pallet: single RDN value — Lando: "Always routes to the Rename Editor."
     *   Pallet: single value, no special case — Lando: "Delegate to per-value routing."
     *   Pallet: multiple values — Lando: "Multi-cargo department."
     * </pre>
     *
     * @param attributeHierarchy  the attribute hierarchy to route; may be {@code null}
     * @return                    the most appropriate {@link IValueEditor}, or {@code null}
     *                            if {@code attributeHierarchy} is {@code null}
     * @see #getCurrentValueEditor( Schema, String )
     */
    public IValueEditor getCurrentValueEditor( AttributeHierarchy attributeHierarchy )
    {
        if ( attributeHierarchy == null )
        {
            return null;
        }
        else if ( ( userSelectedValueEditor == null ) && attributeHierarchy.getAttribute().isObjectClassAttribute()
            && entryValueEditor != null )
        {
            // special case objectClass: always return entry editor
            return entryValueEditor;
        }
        else if ( ( userSelectedValueEditor == entryValueEditor ) && ( entryValueEditor != null ) )
        {
            // special case objectClass: always return entry editor
            return entryValueEditor;
        }
        else if ( ( attributeHierarchy.size() == 1 ) && ( attributeHierarchy.getAttribute().getValueSize() == 0 ) )
        {
            return getCurrentValueEditor( attributeHierarchy.getAttribute().getEntry(), attributeHierarchy
                .getAttribute().getDescription() );
        }
        else if ( ( attributeHierarchy.size() == 1 ) &&
                  ( attributeHierarchy.getAttribute().getValueSize() == 1 ) &&
                  attributeHierarchy.getAttributeDescription().equalsIgnoreCase(
                      attributeHierarchy.getAttribute().getValues()[0].getAttribute().getDescription() ) )
        {
            // special case Rdn: always return MV-editor
            if ( ( userSelectedValueEditor == null ) && attributeHierarchy.getAttribute().getValues()[0].isRdnPart() )
            {
                if ( renameValueEditor != null )
                {
                    return renameValueEditor;
                }
                else
                {
                    return multiValuedValueEditor;
                }
            }

            return getCurrentValueEditor( attributeHierarchy.getAttribute().getValues()[0] );
        }
        else
        {
            return multiValuedValueEditor;
        }
    }


    // ── LANDO LISTS ALTERNATIVE DEPARTMENTS FOR AN ENTRY'S ATTRIBUTE ─────────────
    // A dispatcher asks: "What other departments could handle this cargo type, besides
    // the one you already assigned?" Lando extracts the schema from the entry and
    // delegates to the schema-based overload to assemble the alternatives list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the alternative {@link IValueEditor} choices for a given attribute on a
     * given entry. Extracts the schema from the entry's connection and delegates to
     * {@link #getAlternativeValueEditors(Schema, String)}.
     *
     * <p>For example — Lando lists alternatives via the entry:</p>
     * <pre>
     *   Dispatcher: "What else could handle 'description' on this entry?"
     *   Lando: [extracts schema from entry's connection] "Let me check the alternatives..."
     * </pre>
     *
     * @param entry          the entry whose connection provides the schema
     * @param attributeName  the attribute name whose alternative editors we want
     * @return               an array of alternative {@link IValueEditor}s
     */
    public IValueEditor[] getAlternativeValueEditors( IEntry entry, String attributeName )
    {
        Schema schema = entry.getBrowserConnection().getSchema();

        return getAlternativeValueEditors( schema, attributeName );
    }


    // ── LANDO LISTS ALTERNATIVE DEPARTMENTS BY SCHEMA ───────────────────────────
    // Given a schema and an attribute name, Lando builds the alternatives roster:
    // binary attributes get hex first, then text options; string attributes get text
    // first, then hex. He appends the multi-cargo department and removes whichever
    // department was already assigned as the primary.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the list of alternative {@link IValueEditor}s for a given attribute
     * type, using the schema to determine whether the attribute is binary or string.
     * The current (primary) editor is excluded from the alternatives so the user
     * doesn't see a redundant duplicate. The multi-valued editor is always included
     * as a final alternative.
     *
     * <p>For example — Lando lists alternatives from the schema:</p>
     * <pre>
     *   Attribute: 'jpegPhoto' (binary)
     *   Lando: "Primary: Image Editor. Alternatives: Hex, Single-line text, Multi-line text, MV."
     *   Attribute: 'description' (string)
     *   Lando: "Primary: Single-line text. Alternatives: Multi-line text, Hex, MV."
     * </pre>
     *
     * @param schema         the LDAP schema used to determine binary vs. string classification
     * @param attributeName  the attribute whose alternative editors we want
     * @return               an array of alternative {@link IValueEditor}s, minus the current one
     */
    public IValueEditor[] getAlternativeValueEditors( Schema schema, String attributeName )
    {
        List<IValueEditor> alternativeList = new ArrayList<IValueEditor>();

        AttributeType atd = schema.getAttributeTypeDescription( attributeName );

        if ( SchemaUtils.isBinary( atd, schema ) )
        {
            alternativeList.add( defaultBinaryValueEditor );
            alternativeList.add( defaultStringSingleLineValueEditor );
            alternativeList.add( defaultStringMultiLineValueEditor );
        }
        else if ( SchemaUtils.isString( atd, schema ) )
        {
            alternativeList.add( defaultStringSingleLineValueEditor );
            alternativeList.add( defaultStringMultiLineValueEditor );
            alternativeList.add( defaultBinaryValueEditor );
        }

        alternativeList.add( multiValuedValueEditor );

        alternativeList.remove( getCurrentValueEditor( schema, attributeName ) );

        return alternativeList.toArray( new IValueEditor[alternativeList.size()] );
    }


    // ── LANDO LISTS ALTERNATIVES FOR A CONCRETE VALUE ───────────────────────────
    // Same idea, but this time we have an actual value in hand, not just a label.
    // We inspect the value itself to determine its type, build the alternatives list,
    // remove the currently assigned department, and hand back what's left.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the list of alternative {@link IValueEditor}s for a concrete
     * {@link IValue}. We inspect whether the value is binary or string and build
     * the alternatives roster accordingly, excluding the already-selected editor.
     *
     * <p>For example — Lando inspects the actual cargo:</p>
     * <pre>
     *   Value: binary JPEG data
     *   Lando: "This is binary. Alternatives: Hex, Single-line, Multi-line, MV."
     *   Value: plain string
     *   Lando: "This is text. Alternatives: Single-line, Multi-line, Hex, MV."
     * </pre>
     *
     * @param value  the concrete LDAP value whose alternative editors we want
     * @return       an array of alternative {@link IValueEditor}s, minus the current one
     */
    public IValueEditor[] getAlternativeValueEditors( IValue value )
    {
        List<IValueEditor> alternativeList = new ArrayList<IValueEditor>();

        if ( value.isBinary() )
        {
            alternativeList.add( defaultBinaryValueEditor );
            alternativeList.add( defaultStringSingleLineValueEditor );
            alternativeList.add( defaultStringMultiLineValueEditor );
        }
        else if ( value.isString() )
        {
            alternativeList.add( defaultStringSingleLineValueEditor );
            alternativeList.add( defaultStringMultiLineValueEditor );
            alternativeList.add( defaultBinaryValueEditor );
        }

        alternativeList.add( multiValuedValueEditor );

        alternativeList.remove( getCurrentValueEditor( value ) );

        return alternativeList.toArray( new IValueEditor[alternativeList.size()] );
    }


    // ── LANDO LISTS ALTERNATIVES FOR A HIERARCHY — WITH SPECIAL CASES ────────────
    // Lando checks the pallet: if it's null, there are zero alternatives. If any
    // value on the pallet is an RDN part, the only alternative is the multi-cargo
    // department (no other department is allowed to touch RDN cargo by standing
    // order). If it's an objectClass attribute, same deal — no alternatives. For
    // everything else, standard alternatives logic applies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns alternative {@link IValueEditor}s for an {@link AttributeHierarchy},
     * enforcing the special-case constraints: RDN values only have the multi-valued
     * editor as an alternative, objectClass values have no alternatives, and for
     * everything else we delegate to the per-value or per-attribute overloads.
     *
     * <p>For example — Lando checks the pallet for special cargo:</p>
     * <pre>
     *   Pallet: null — Lando: "Nothing here. Zero alternatives."
     *   Pallet: RDN value — Lando: "Only the multi-cargo department can touch this."
     *   Pallet: objectClass — Lando: "No alternatives. Entry editor only, by standing order."
     *   Pallet: normal value — Lando: "Standard alternatives roster applies."
     * </pre>
     *
     * @param ah  the attribute hierarchy to inspect; if {@code null} we return an empty array
     * @return    an array of alternative {@link IValueEditor}s (may be empty, never {@code null})
     */
    public IValueEditor[] getAlternativeValueEditors( AttributeHierarchy ah )
    {
        if ( ah == null )
        {
            return new IValueEditor[0];
        }

        // special case Rdn: no alternative to the rename editor, except the MV editor
        // perhaps this should be moved somewhere else
        if ( multiValuedValueEditor != null )
        {
            for ( IAttribute attribute : ah )
            {
                for ( IValue value : attribute.getValues() )
                {
                    if ( value.isRdnPart() )
                    {
                        return new IValueEditor[]
                            { multiValuedValueEditor };
                    }
                }
            }
        }

        // special case objectClass: no alternative to the entry editor
        // perhaps this should be moved somewhere else
        for ( IAttribute attribute : ah )
        {
            if ( attribute.isObjectClassAttribute() )
            {
                return new IValueEditor[0];
            }
        }

        if ( ( ah.size() == 1 ) && ( ah.getAttribute().getValueSize() == 0 ) )
        {
            return getAlternativeValueEditors( ah.getAttribute().getEntry(), ah.getAttribute().getDescription() );
        }
        else if ( ( ah.size() == 1 ) &&
                  ( ah.getAttribute().getValueSize() == 1 ) &&
                  ah.getAttributeDescription().equalsIgnoreCase(
                      ah.getAttribute().getValues()[0].getAttribute().getDescription() ) )
        {
            return getAlternativeValueEditors( ah.getAttribute().getValues()[0] );
        }
        else
        {
            return new IValueEditor[0];
        }
    }


    // ── LANDO PRODUCES THE FULL ROSTER OF ALL DEPARTMENTS ───────────────────────
    // The operations director asks Lando: "Give me a complete list of every department
    // in Cloud City." Lando uses a LinkedHashSet to ensure stable ordering and no
    // duplicates, then walks through every editor — defaults first, then plugins,
    // then the special ones — and returns the full roster.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns every {@link IValueEditor} known to this manager as an array —
     * the default single-line and multi-line text editors, the default binary
     * editor, all plugin-contributed editors, the multi-valued editor, and the
     * optional entry and rename editors. A {@link LinkedHashSet} ensures stable
     * ordering and no duplicates (since defaults are also in the class map).
     *
     * <p>For example — Lando reads the full department roster:</p>
     * <pre>
     *   Lando: "Single-line text — check. Multi-line text — check. Hex — check."
     *   Lando: "All plugin departments — check. Multi-cargo — check."
     *   Lando: "Entry editor — check. Rename editor — check. That's everyone."
     * </pre>
     *
     * @return  all available {@link IValueEditor} instances as an array
     */
    public IValueEditor[] getAllValueEditors()
    {
        // use a set to avoid double entries
        Set<IValueEditor> list = new LinkedHashSet<IValueEditor>();

        list.add( defaultStringSingleLineValueEditor );
        list.add( defaultStringMultiLineValueEditor );
        list.add( defaultBinaryValueEditor );

        list.addAll( class2ValueEditors.values() );

        list.add( multiValuedValueEditor );

        if ( entryValueEditor != null )
        {
            list.add( entryValueEditor );
        }

        if ( renameValueEditor != null )
        {
            list.add( renameValueEditor );
        }

        return list.toArray( new IValueEditor[list.size()] );
    }


    // ── LANDO POINTS TO THE BINARY CARGO DEPARTMENT ─────────────────────────────
    // "Which department handles raw binary cargo?" Lando points to the Hex Editor
    // without hesitation — it's the default handler for anything that can't be
    // safely decoded as UTF-8 text.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default binary {@link IValueEditor} — currently a HexEditor.
     * Use this when you need to display or edit a binary LDAP attribute value and
     * you don't need the full routing logic of {@link #getCurrentValueEditor}.
     *
     * <p>For example — Lando points to the hex department:</p>
     * <pre>
     *   Dispatcher: "Where do raw binary values go?"
     *   Lando: "Hex Editor, bay three. Always."
     * </pre>
     *
     * @return  the default binary {@link IValueEditor} (a HexEditor instance)
     */
    public IValueEditor getDefaultBinaryValueEditor()
    {
        return defaultBinaryValueEditor;
    }


    // ── LANDO POINTS TO THE TEXT CARGO DEPARTMENT ───────────────────────────────
    // "Which department handles standard text cargo?" Lando gestures toward the
    // multi-line TextValueEditor — the default for any string attribute that needs
    // more than a single-line inline editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default string (multi-line) {@link IValueEditor} — currently a
     * TextValueEditor. Use this when you want the "plain text" editor without
     * running through the full routing priority chain.
     *
     * <p>For example — Lando points to the text department:</p>
     * <pre>
     *   Dispatcher: "Where do standard text values go?"
     *   Lando: "Multi-line Text Editor, bay one. Standard string cargo."
     * </pre>
     *
     * @return  the default multi-line string {@link IValueEditor} (a TextValueEditor instance)
     */
    public IValueEditor getDefaultStringValueEditor()
    {
        return defaultStringMultiLineValueEditor;
    }


    // ── LANDO POINTS TO THE MULTI-CARGO DEPARTMENT ──────────────────────────────
    // When a single attribute has more than one value, or when multiple attributes
    // need to be edited together, that pallet goes to the multi-valued editor
    // department — the one that knows how to show a list and let the user add,
    // edit, or remove individual items.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link MultivaluedValueEditor} — the editor that handles
     * attributes with multiple values or complex attribute hierarchies. The
     * multi-valued editor is always instantiated (never null), unlike the entry
     * and rename editors which are optional.
     *
     * <p>For example — Lando points to the multi-cargo bay:</p>
     * <pre>
     *   Dispatcher: "We have a pallet with four values for 'telephoneNumber'."
     *   Lando: "Multi-cargo department, bay two. They handle the list UI."
     * </pre>
     *
     * @return  the shared {@link MultivaluedValueEditor} instance
     */
    public MultivaluedValueEditor getMultiValuedValueEditor()
    {
        return multiValuedValueEditor;
    }


    // ── LANDO POINTS TO THE ENTRY EDITING DEPARTMENT ─────────────────────────────
    // When the objectClass attribute is selected, we need to open a wizard that edits
    // the whole entry's class membership — not just a single value. That's the entry
    // editor's job. This getter hands callers that department directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EntryValueEditor}, which handles the {@code objectClass}
     * attribute by opening a wizard that lets the user add or remove structural and
     * auxiliary object classes for the whole entry. Returns {@code null} if this
     * manager was created without the entry editor (i.e., {@code useEntryValueEditor}
     * was {@code false} in the constructor).
     *
     * <p>For example — Lando points to the entry editing bay:</p>
     * <pre>
     *   Dispatcher: "objectClass cargo just arrived."
     *   Lando: "Entry Editor department — they handle the whole entry schema wizard."
     * </pre>
     *
     * @return  the {@link EntryValueEditor}, or {@code null} if not configured
     */
    public EntryValueEditor getEntryValueEditor()
    {
        return entryValueEditor;
    }


    // ── LANDO POINTS TO THE RENAME DEPARTMENT ───────────────────────────────────
    // When an RDN attribute is selected, we need the rename editor — it opens the
    // rename dialog and fires a modrdn job. This getter hands callers the rename
    // department's reference directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link RenameValueEditor}, which handles RDN-part attribute
     * values by opening the rename dialog and executing a background modrdn job.
     * Returns {@code null} if this manager was created without the rename editor.
     *
     * <p>For example — Lando points to the rename bay:</p>
     * <pre>
     *   Dispatcher: "An RDN attribute value is being edited."
     *   Lando: "Rename department — they file the new name with the directory."
     * </pre>
     *
     * @return  the {@link RenameValueEditor}, or {@code null} if not configured
     */
    public RenameValueEditor getRenameValueEditor()
    {
        return renameValueEditor;
    }


    // ── LANDO INSTANTIATES ALL DEPARTMENTS FROM THE CITY MANIFEST ───────────────
    // Lando consults the official Cloud City department manifest (the Eclipse
    // extension registry), reads each registered editor's class name and metadata,
    // and instantiates every department. If any department fails to start up, we
    // log an error and move on — one bad plugin shouldn't break everything else.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@link ValueEditorExtension} descriptors from the extension registry,
     * instantiates each editor via {@link IConfigurationElement#createExecutableExtension},
     * and sets its name and icon. Any editor that fails to instantiate is logged and
     * skipped — we don't want one bad plugin to prevent the rest from loading.
     *
     * <p>For example — Lando opens the city manifest:</p>
     * <pre>
     *   Manifest entry: "class=ImageValueEditor, name='Image Editor', icon='image.png'"
     *   Lando: "Creating the Image Editor department... success. Adding to roster."
     *   Manifest entry: "class=BrokenValueEditor"
     *   Lando: "Failed to start BrokenValueEditor. Logging error and skipping."
     * </pre>
     *
     * @param parent  the SWT composite parent for in-place editors that need a widget parent
     * @return        a {@link Collection} of successfully instantiated {@link IValueEditor}s
     */
    private Collection<IValueEditor> createValueEditors( Composite parent )
    {
        Collection<IValueEditor> valueEditors = new ArrayList<IValueEditor>();

        Collection<ValueEditorExtension> valueEditorExtensions = getValueEditorExtensions();

        for ( ValueEditorExtension vee : valueEditorExtensions )
        {
            try
            {
                IValueEditor valueEditor = ( IValueEditor ) vee.member.createExecutableExtension( CLASS );
                valueEditor.create( parent );
                valueEditor.setValueEditorName( vee.name );
                valueEditor.setValueEditorImageDescriptor( vee.icon );
                valueEditors.add( valueEditor );
            }
            catch ( Exception e )
            {
                BrowserCommonActivator.getDefault().getLog().log(
                    new Status( IStatus.ERROR, BrowserCommonConstants.PLUGIN_ID, 1, Messages
                        .getString( "ValueEditorManager.UnableToCreateValueEditor" ) //$NON-NLS-1$
                        + vee.className, e ) );
            }
        }

        return valueEditors;
    }


    // ── LANDO SCANS THE REGISTRY FOR ALL REGISTERED DEPARTMENTS ─────────────────
    // Before opening day, Lando queries the Empire-wide registry (the OSGi extension
    // registry) to find every department that has registered itself under the
    // valueeditors extension point. He deduplicates by class name (a plugin can
    // register the same class for multiple syntaxes in separate elements) and builds
    // a combined extension descriptor for each.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Queries the Eclipse extension registry for all contributions to the
     * {@code org.apache.directory.studio.valueeditors} extension point and
     * assembles them into {@link ValueEditorExtension} descriptors. Multiple
     * extension elements can reference the same class (to register it for different
     * syntaxes or attribute types), so we deduplicate by class name using a
     * {@link LinkedHashMap} — preserving insertion order while merging the metadata.
     *
     * <p>For example — Lando scans the registry:</p>
     * <pre>
     *   Registry: "ImageValueEditor — handles syntaxOID 1.3.6.1.4.1.1466.115.121.1.28"
     *   Registry: "ImageValueEditor — also handles attributeType 'photo'"
     *   Lando: "Same department, two cargo types. Merging into one descriptor."
     * </pre>
     *
     * @return  a {@link Collection} of {@link ValueEditorExtension} descriptors,
     *          one per unique editor class, with merged syntax OIDs and attribute types
     */
    public static Collection<ValueEditorExtension> getValueEditorExtensions()
    {
        Map<String, ValueEditorExtension> valueEditorExtensions = new LinkedHashMap<>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint( EXTENSION_POINT );
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();

        // For each extension:
        for ( IConfigurationElement member : members )
        {
            String className = member.getAttribute( CLASS );
            String name = member.getAttribute( NAME );
            String iconPath = member.getAttribute( ICON );

            ValueEditorExtension proxy;
            if ( valueEditorExtensions.containsKey( className ) )
            {
                proxy = valueEditorExtensions.get( className );
            }
            else
            {
                proxy = new ValueEditorExtension();
                proxy.className = className;
                proxy.member = member;
                valueEditorExtensions.put( className, proxy );
            }

            if ( name != null )
            {
                proxy.name = name;
            }

            if ( iconPath != null )
            {
                IExtension extension = member.getDeclaringExtension();
                String extendingPluginId = extension.getNamespaceIdentifier();
                proxy.icon = AbstractUIPlugin.imageDescriptorFromPlugin( extendingPluginId, iconPath );
                if ( proxy.icon == null )
                {
                    proxy.icon = ImageDescriptor.getMissingImageDescriptor();
                }
            }

            IConfigurationElement[] children = member.getChildren();

            for ( IConfigurationElement child : children )
            {
                String type = child.getName();

                if ( SYNTAX.equals( type ) )
                {
                    String syntaxOID = child.getAttribute( SYNTAX_OID );
                    proxy.syntaxOids.add( syntaxOID );
                }
                else if ( ATTRIBUTE.equals( type ) )
                {
                    String attributeType = child.getAttribute( ATTRIBUTE_TYPE );
                    proxy.attributeTypes.add( attributeType );
                }
            }
        }

        return valueEditorExtensions.values();
    }

    // ── CLASS: ValueEditorExtension — LANDO'S DEPARTMENT MANIFEST ENTRY ──────────
    // Each slot in the department manifest is a little data card: class name, display
    // name, icon, and which cargo types (syntax OIDs, attribute type names) this
    // department handles. Lando reads these cards from the registry and uses them to
    // instantiate the actual department editors.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * A simple data holder (bean) that captures everything declared in a single
     * {@code org.apache.directory.studio.valueeditors} extension point contribution:
     * the editor class name, its display name, its icon, the syntax OIDs it handles,
     * and the attribute type names it handles. Multiple extension elements for the
     * same class are merged into one instance.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public static class ValueEditorExtension
    {
        /** The name. */
        public String name = null;

        /** The icon. */
        public ImageDescriptor icon = null;

        /** The class name. */
        public String className = null;

        /** The syntax oids. */
        public Collection<String> syntaxOids = new ArrayList<String>( 3 );

        /** The attribute types. */
        public Collection<String> attributeTypes = new ArrayList<String>( 3 );

        /** The configuration element. */
        private IConfigurationElement member = null;


        // ── LANDO READS A DEPARTMENT'S MANIFEST CARD ALOUD ──────────────────────────
        // Lando picks up the department's card and reads it aloud for the record:
        // class name, display name, which attribute types it handles, which syntax OIDs
        // it covers. This is the official summary of one editor's registration data.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Returns a human-readable summary of this extension descriptor —
         * useful for debugging when you want to see what got loaded from the
         * extension registry. Format: {@code <name, className, {attrTypes}, {syntaxOids}>}.
         *
         * <p>For example — Lando reads the department card:</p>
         * <pre>
         *   Lando: "&lt;Image Editor, o.a.d.s.valueeditors.ImageValueEditor,
         *            {photo, jpegPhoto}, {1.3.6.1.4.1.1466.115.121.1.28}&gt;"
         * </pre>
         *
         * @return  a formatted string summarising this extension's metadata
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append( '<' );

            sb.append( name ).append( ", " );
            sb.append( className );

            if ( ( attributeTypes != null ) && ( attributeTypes.size() > 0 ) )
            {
                sb.append( ", {" );
                boolean isFirst = true;

                for ( String attributeType : attributeTypes )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( ", " );
                    }

                    sb.append( attributeType );
                }

                sb.append( '}' );
            }


            if ( ( syntaxOids != null ) && ( syntaxOids.size() > 0 ) )
            {
                sb.append( ", {" );
                boolean isFirst = true;

                for ( String syntaxOid : syntaxOids )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( ", " );
                    }

                    sb.append( syntaxOid );
                }

                sb.append( '}' );
            }

            sb.append( '>' );

            return sb.toString();
        }
    }
}
