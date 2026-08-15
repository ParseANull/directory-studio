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

package org.apache.directory.studio.ldapbrowser.common;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.schema.AttributeValueEditorRelation;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SyntaxValueEditorRelation;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: ValueEditorsPreferences — LANDO RUNNING CLOUD CITY ─────────────────
// Lando Calrissian runs Cloud City like a finely tuned operation: he knows
// which department handles tibanna gas, which handles mining, and which handles
// guest services. When someone needs something, Lando consults his ledger and
// routes them to exactly the right specialist — no confusion, no wrong doors.
// This class is Lando's ledger for value editors: it tracks which LDAP
// attribute type maps to which editor class, and which LDAP syntax OID maps to
// which editor class. Both live and default mappings are managed here.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the user's configured mappings between LDAP attribute types/syntaxes
 * and the value editor classes that should handle them.
 * When the entry editor needs to display or edit a value, it asks this class:
 * "which editor should I open for attribute type 'mail'?" and gets back a class
 * name it can instantiate.
 * Think of this class as Lando running Cloud City — routing every request to
 * the right department based on a well-maintained ledger.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueEditorsPreferences
{

    /** The attribute value editor cache. */
    private Map<String, String> attributeValueEditorCache;

    /** The syntax value editor cache. */
    private Map<String, String> syntaxValueEditorCache;


    // ── LANDO READS THE ATTRIBUTE DEPARTMENT DIRECTORY ────────────────────────
    // Lando pulls out the Cloud City department directory and flips to the
    // section listing which department handles each type of cargo (attribute).
    // The first time this is called, he assembles that section from the raw
    // relations data; after that, he just hands you the pre-built lookup map.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns a map from attribute OID/type name (lowercased) to the fully
     * qualified class name of the value editor that should handle it.
     * Built lazily from {@link #getAttributeValueEditorRelations()} on first
     * access, then cached until the next call to
     * {@link #setAttributeValueEditorRelations}.
     *
     * <p>For example — Lando consulting the cargo-routing directory:</p>
     * <pre>
     *   Map&lt;String, String&gt; map = prefs.getAttributeValueEditorMap();
     *   String editorClass = map.get( "cn" );  // "InPlaceTextValueEditor"
     * </pre>
     *
     * @return a live {@link Map} from lowercase attribute name/OID to editor
     *         class name; never null but may be empty if no relations are configured.
     */
    public Map<String, String> getAttributeValueEditorMap()
    {
        if ( attributeValueEditorCache == null )
        {
            attributeValueEditorCache = new HashMap<String, String>();
            AttributeValueEditorRelation[] relations = getAttributeValueEditorRelations();
            for ( int i = 0; i < relations.length; i++ )
            {
                if ( relations[i].getAttributeNumericOidOrType() != null )
                {
                    attributeValueEditorCache.put( Strings.toLowerCase( relations[i].getAttributeNumericOidOrType() ),
                        relations[i].getValueEditorClassName() );
                }
            }
        }
        return attributeValueEditorCache;
    }


    // ── LANDO READS THE FULL ATTRIBUTE ASSIGNMENT LEDGER ──────────────────────
    // The full Cloud City ledger shows every attribute-to-department assignment
    // as a detailed record — not just the quick-lookup table, but each individual
    // pairing. Lando deserializes the preference store entry to reconstruct the
    // original array of relation objects. He also handles a legacy naming issue
    // from an older version of the ledger.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full array of {@link AttributeValueEditorRelation} objects
     * currently stored in the Eclipse preference store. Each relation pairs one
     * attribute name or OID with one value editor class name.
     * Handles a migration rename from the old "AttributeValueProviderRelation"
     * class name (used before version 1.1.0) to the current name so old
     * preference files still load correctly.
     *
     * <p>For example — Lando reading every cargo assignment from the master log:</p>
     * <pre>
     *   AttributeValueEditorRelation[] rels = prefs.getAttributeValueEditorRelations();
     *   for ( AttributeValueEditorRelation r : rels ) {
     *       System.out.println( r.getAttributeNumericOidOrType() + " -> " + r.getValueEditorClassName() );
     *   }
     * </pre>
     *
     * @return the array of attribute-to-editor relations deserialized from
     *         the preference store; may be empty but not null.
     */
    public AttributeValueEditorRelation[] getAttributeValueEditorRelations()
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = store.getString( BrowserCommonConstants.PREFERENCE_ATTRIBUTE_VALUEEDITOR_RELATIONS );
        // Migration issue from 1.0.1 to 1.1.0 (DIRSTUDIO-287): class AttributeValueProviderRelation
        // was renamed to AttributeValueEditorRelation, to be able to load the old configuration it
        // is necessary to replace the old class name with the new class name.
        s = s.replaceAll( "AttributeValueProviderRelation", "AttributeValueEditorRelation" ); //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll( "valueProviderClassname", "valueEditorClassName" ); //$NON-NLS-1$ //$NON-NLS-2$
        AttributeValueEditorRelation[] aver = ( AttributeValueEditorRelation[] ) Utils.deserialize( s );
        return aver;
    }


    // ── LANDO UPDATES THE ATTRIBUTE ASSIGNMENT LEDGER ─────────────────────────
    // Lando revises the department assignments for certain cargo types — maybe
    // a new editor plugin just landed and he needs to reroute accordingly.
    // He serializes the new relation array back to the preference store and
    // invalidates the quick-lookup cache so it gets rebuilt on next access.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the stored attribute-to-editor relations with the given array,
     * persisting them to the Eclipse preference store. Clears the attribute
     * editor cache so it will be rebuilt from the new data on next access.
     *
     * <p>For example — Lando updating Cloud City's cargo routing after a new
     * ship docks:</p>
     * <pre>
     *   prefs.setAttributeValueEditorRelations( newRelations );
     *   // cache is now null; next getAttributeValueEditorMap() rebuilds it
     * </pre>
     *
     * @param attributeValueEditorRelations  the new array of attribute-to-editor
     *                                       pairings to store; must not be null.
     */
    public void setAttributeValueEditorRelations( AttributeValueEditorRelation[] attributeValueEditorRelations )
    {
        store( BrowserCommonConstants.PREFERENCE_ATTRIBUTE_VALUEEDITOR_RELATIONS, attributeValueEditorRelations );
        attributeValueEditorCache = null;
    }


    // ── LANDO CHECKS THE FACTORY DEFAULTS ─────────────────────────────────────
    // Before Cloud City was customized, the Bespin Tibanna Corporation shipped
    // a standard configuration guide — the factory defaults. Lando keeps a copy
    // separate from the working ledger so he can always compare or reset. This
    // method reads those factory defaults for attribute-editor pairings.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default (factory) array of {@link AttributeValueEditorRelation}
     * objects — the ones set by {@link BrowserCommonPreferencesInitializer} at
     * plugin startup. These defaults represent the value-editor extensions
     * registered via the extension point before any user customization.
     * Useful when the user wants to reset to defaults on the preference page.
     *
     * <p>For example — Lando consulting the original Cloud City spec sheet:</p>
     * <pre>
     *   AttributeValueEditorRelation[] defaults =
     *       prefs.getDefaultAttributeValueEditorRelations();
     * </pre>
     *
     * @return the default attribute-to-editor relation array; may be empty but
     *         not null.
     */
    public AttributeValueEditorRelation[] getDefaultAttributeValueEditorRelations()
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = store.getDefaultString( BrowserCommonConstants.PREFERENCE_ATTRIBUTE_VALUEEDITOR_RELATIONS );
        // Migration issue from 1.0.1 to 1.1.0 (DIRSTUDIO-287): class AttributeValueProviderRelation
        // was renamed to AttributeValueEditorRelation, to be able to load the old configuration it
        // is necessary to replace the old class name with the new class name.
        s = s.replaceAll( "AttributeValueProviderRelation", "AttributeValueEditorRelation" ); //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll( "valueProviderClassname", "valueEditorClassName" ); //$NON-NLS-1$ //$NON-NLS-2$
        AttributeValueEditorRelation[] aver = ( AttributeValueEditorRelation[] ) Utils.deserialize( s );
        return aver;
    }


    // ── LANDO LOCKS IN THE FACTORY DEFAULTS ───────────────────────────────────
    // When Lando first opens Cloud City, he records the standard configuration
    // as the baseline default. This method does the same — it persists the
    // default attribute-editor relations into the preference store's defaults
    // section so they can be queried or restored later.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Saves the given array as the default attribute-to-editor relations in
     * the Eclipse preference store. Called by
     * {@link BrowserCommonPreferencesInitializer} at startup to record the
     * built-in extension-point bindings as the baseline defaults.
     *
     * <p>For example — Lando inscribing Cloud City's opening configuration into
     * the permanent record:</p>
     * <pre>
     *   prefs.setDefaultAttributeValueEditorRelations( builtInRelations );
     * </pre>
     *
     * @param attributeValueEditorRelations  the array of default relations to
     *                                       store; must not be null.
     */
    public void setDefaultAttributeValueEditorRelations( AttributeValueEditorRelation[] attributeValueEditorRelations )
    {
        storeDefault( BrowserCommonConstants.PREFERENCE_ATTRIBUTE_VALUEEDITOR_RELATIONS, attributeValueEditorRelations );
    }


    // ── LANDO READS THE SYNTAX DEPARTMENT DIRECTORY ───────────────────────────
    // Lando flips to the other half of the directory — the section for syntax
    // OIDs rather than attribute names. Some cargo is identified by its type
    // code (OID), not its name, and this map handles that lookup path. Built
    // lazily and cached until the syntax relations change.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns a map from syntax OID (lowercased) to the fully qualified class
     * name of the value editor that handles values of that syntax.
     * Built lazily from {@link #getSyntaxValueEditorRelations()} on first access,
     * cached until the next call to {@link #setSyntaxValueEditorRelations}.
     *
     * <p>For example — Lando looking up which department handles a particular
     * cargo classification code:</p>
     * <pre>
     *   Map&lt;String, String&gt; map = prefs.getSyntaxValueEditorMap();
     *   String editorClass = map.get( "1.3.6.1.4.1.1466.115.121.1.15" ); // DirectoryString
     * </pre>
     *
     * @return a live {@link Map} from lowercase syntax OID to editor class name;
     *         never null but may be empty.
     */
    public Map<String, String> getSyntaxValueEditorMap()
    {
        if ( syntaxValueEditorCache == null )
        {
            syntaxValueEditorCache = new HashMap<String, String>();
            SyntaxValueEditorRelation[] relations = getSyntaxValueEditorRelations();
            for ( int i = 0; i < relations.length; i++ )
            {
                if ( relations[i].getSyntaxOID() != null )
                {
                    syntaxValueEditorCache.put( Strings.toLowerCase( relations[i].getSyntaxOID() ), relations[i]
                        .getValueEditorClassName() );
                }
            }
        }
        return syntaxValueEditorCache;
    }


    // ── LANDO UPDATES THE SYNTAX ASSIGNMENT LEDGER ────────────────────────────
    // A new set of syntax-to-department rules arrives from Bespin HQ. Lando
    // serializes them into the preference store and clears the syntax cache so
    // the next lookup rebuilds from the fresh data.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the stored syntax-to-editor relations with the given array,
     * persisting them to the Eclipse preference store. Clears the syntax editor
     * cache so it rebuilds on next access.
     *
     * <p>For example — Lando revising Cloud City's syntax classification rules:</p>
     * <pre>
     *   prefs.setSyntaxValueEditorRelations( newSyntaxRelations );
     * </pre>
     *
     * @param syntaxValueEditorRelations  the new array of syntax OID-to-editor
     *                                   pairings; must not be null.
     */
    public void setSyntaxValueEditorRelations( SyntaxValueEditorRelation[] syntaxValueEditorRelations )
    {
        store( BrowserCommonConstants.PREFERENCE_SYNTAX_VALUEPEDITOR_RELATIONS, syntaxValueEditorRelations );
        syntaxValueEditorCache = null;
    }


    // ── LANDO READS THE FULL SYNTAX ASSIGNMENT LEDGER ─────────────────────────
    // Lando opens the current working ledger for syntax-to-department pairings,
    // deserializing it from the preference store with the same migration handling
    // as the attribute version — old "SyntaxValueProviderRelation" class names
    // get translated to the current name on the fly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full array of {@link SyntaxValueEditorRelation} objects
     * currently stored in the Eclipse preference store. Each relation pairs
     * one syntax OID with one value editor class name.
     * Handles the same 1.0.1-to-1.1.0 migration rename as the attribute variant.
     *
     * <p>For example — Lando reading every syntax-routing assignment from the
     * master log:</p>
     * <pre>
     *   SyntaxValueEditorRelation[] rels = prefs.getSyntaxValueEditorRelations();
     * </pre>
     *
     * @return the array of syntax-to-editor relations; may be empty but not null.
     */
    public SyntaxValueEditorRelation[] getSyntaxValueEditorRelations()
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = store.getString( BrowserCommonConstants.PREFERENCE_SYNTAX_VALUEPEDITOR_RELATIONS );
        // Migration issue from 1.0.1 to 1.1.0 (DIRSTUDIO-287): class SyntaxValueProviderRelation
        // was renamed to SyntaxValueEditorRelation, to be able to load the old configuration it
        // is necessary to replace the old class name with the new class name.
        s = s.replaceAll( "SyntaxValueProviderRelation", "SyntaxValueEditorRelation" ); //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll( "valueProviderClassname", "valueEditorClassName" ); //$NON-NLS-1$ //$NON-NLS-2$
        SyntaxValueEditorRelation[] sver = ( SyntaxValueEditorRelation[] ) Utils.deserialize( s );
        return sver;
    }


    // ── LANDO CHECKS THE SYNTAX FACTORY DEFAULTS ──────────────────────────────
    // Lando consults the original Cloud City spec for syntax classifications —
    // the baseline rules before any customization. These defaults come from
    // the preference store's default section and represent the built-in
    // extension-point bindings as installed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default (factory) array of {@link SyntaxValueEditorRelation}
     * objects, as stored by {@link BrowserCommonPreferencesInitializer}.
     * Used by the preferences UI when the user wants to revert syntax editor
     * assignments to the out-of-the-box configuration.
     *
     * <p>For example — Lando reading the original Cloud City spec for syntax routing:</p>
     * <pre>
     *   SyntaxValueEditorRelation[] defaults =
     *       prefs.getDefaultSyntaxValueEditorRelations();
     * </pre>
     *
     * @return the default syntax-to-editor relation array; may be empty but not null.
     */
    public SyntaxValueEditorRelation[] getDefaultSyntaxValueEditorRelations()
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = store.getDefaultString( BrowserCommonConstants.PREFERENCE_SYNTAX_VALUEPEDITOR_RELATIONS );
        // Migration issue from 1.0.1 to 1.1.0 (DIRSTUDIO-287): class SyntaxValueProviderRelation
        // was renamed to SyntaxValueEditorRelation, to be able to load the old configuration it
        // is necessary to replace the old class name with the new class name.
        s = s.replaceAll( "SyntaxValueProviderRelation", "SyntaxValueEditorRelation" ); //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll( "valueProviderClassname", "valueEditorClassName" ); //$NON-NLS-1$ //$NON-NLS-2$
        SyntaxValueEditorRelation[] sver = ( SyntaxValueEditorRelation[] ) Utils.deserialize( s );
        return sver;
    }


    // ── LANDO LOCKS IN THE SYNTAX FACTORY DEFAULTS ────────────────────────────
    // Lando inscribes the initial syntax rules into Cloud City's permanent
    // baseline record. This is called at startup by the preferences initializer
    // to record the built-in extension-point syntax bindings as the defaults
    // users can reset to.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Saves the given array as the default syntax-to-editor relations in
     * the Eclipse preference store's defaults section.
     * Called by {@link BrowserCommonPreferencesInitializer} at startup.
     *
     * <p>For example — Lando writing Cloud City's original syntax spec into the
     * permanent record:</p>
     * <pre>
     *   prefs.setDefaultSyntaxValueEditorRelations( builtInSyntaxRelations );
     * </pre>
     *
     * @param syntaxValueEditorRelations  the array of default syntax-to-editor
     *                                   pairings to record; must not be null.
     */
    public void setDefaultSyntaxValueEditorRelations( SyntaxValueEditorRelation[] syntaxValueEditorRelations )
    {
        storeDefault( BrowserCommonConstants.PREFERENCE_SYNTAX_VALUEPEDITOR_RELATIONS, syntaxValueEditorRelations );
    }


    // ── LANDO'S SECRETARY SERIALIZES THE LEDGER ENTRY ────────────────────────
    // Lando's administrator takes any object, serializes it into a string, and
    // writes it into the preference store under the right key. This private
    // helper is used by all the "set" methods so they share a single
    // serialization path.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given object to a string and stores it in the Eclipse
     * preference store under the given key. Used internally by
     * {@link #setAttributeValueEditorRelations} and
     * {@link #setSyntaxValueEditorRelations}.
     *
     * <p>For example — Lando's admin writing a new routing rule to the ledger:</p>
     * <pre>
     *   store( PREFERENCE_ATTRIBUTE_VALUEEDITOR_RELATIONS, relations );
     * </pre>
     *
     * @param key  the preference store key to write to — one of the
     *             {@code PREFERENCE_} constants from {@link BrowserCommonConstants}.
     * @param o    the object to serialize and store; must be serializable.
     */
    private static void store( String key, Object o )
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = Utils.serialize( o );
        store.setValue( key, s );
    }


    // ── LANDO'S SECRETARY WRITES THE BASELINE DEFAULT ────────────────────────
    // Same as above, but writes to the "defaults" section of the preference
    // store rather than the live values section — this is how factory defaults
    // differ from user-modified values in Eclipse's preference system.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given object and stores it as the default value for the
     * given preference key. Used internally by
     * {@link #setDefaultAttributeValueEditorRelations} and
     * {@link #setDefaultSyntaxValueEditorRelations}.
     *
     * <p>For example — Lando's admin recording the baseline default for a rule:</p>
     * <pre>
     *   storeDefault( PREFERENCE_SYNTAX_VALUEPEDITOR_RELATIONS, defaultRelations );
     * </pre>
     *
     * @param key  the preference store key whose default to set.
     * @param o    the object to serialize and store as the default.
     */
    private static void storeDefault( String key, Object o )
    {
        IPreferenceStore store = BrowserCommonActivator.getDefault().getPreferenceStore();
        String s = Utils.serialize( o );
        store.setDefault( key, s );
    }
}
