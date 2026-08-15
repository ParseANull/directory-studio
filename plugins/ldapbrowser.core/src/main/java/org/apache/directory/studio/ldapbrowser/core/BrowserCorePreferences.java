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

package org.apache.directory.studio.ldapbrowser.core;


import java.util.HashSet;
import java.util.Set;

import org.apache.directory.studio.ldapbrowser.core.model.schema.BinaryAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.schema.BinarySyntax;
import org.apache.directory.studio.ldapbrowser.core.model.schema.ObjectClassIconPair;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.core.runtime.Preferences;


// ── CLASS: BrowserCorePreferences — MACE WINDU CONSULTING THE JEDI CODEX ────
// Mace Windu keeps the Jedi Codex — the authoritative rulebook that tells every
// Jedi what syntax is allowed, which artefacts must be treated as physical
// (binary), and what icons represent each order of being.
// He can retrieve the current rules, replace them, and restore the defaults
// that were established when the Order was founded.
// This class is that Codex for the browser core: it persists and retrieves the
// binary-attribute list, binary-syntax list, and object-class icon mappings
// from Eclipse's preference store.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides typed access to the browser-core plugin's preference store for the
 * settings that the model layer actually needs at runtime.
 * The three main preference groups managed here are:
 * <ol>
 *   <li>Binary attributes — attribute type OIDs/names whose values are raw
 *       bytes (e.g., {@code jpegPhoto}, {@code userCertificate}).</li>
 *   <li>Binary syntaxes — LDAP syntax OIDs that imply binary encoding.</li>
 *   <li>Object-class icon pairs — mappings from object-class names to display
 *       icons in the browser tree.</li>
 * </ol>
 * Each group has get/set accessors for both current values and defaults.
 * Think of this class as Mace Windu guarding the Jedi Codex — the authoritative
 * source of what is binary and what icon each class of entry deserves.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCorePreferences
{
    private Set<String> binaryAttributeCache;

    private Set<String> binarySyntaxCache;


    // ── Mace Reads The Binary-Artefact Manifest ───────────────────────────────────
    // Mace consults the Codex for the list of physical artefacts that must be
    // handled as solid objects — "These cannot be transmitted as pure Force energy;
    // they must be packaged as cargo."
    // We build a fast lookup set (upper-cased for case-insensitive matching) from
    // the stored binary-attribute list, caching it to avoid repeated deserialization.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OIDs and names of all binary attributes as an upper-cased
     * {@link Set} for O(1) lookup.
     * Binary attributes hold raw bytes (think {@code jpegPhoto}), not text, so
     * the network layer must decode them differently.
     * The result is cached after the first call; call
     * {@link #setBinaryAttributes} to invalidate the cache.
     *
     * <p>For example — Mace checks whether an attribute is binary:</p>
     * <pre>
     *   Set&lt;String&gt; binaryOids = prefs.getUpperCasedBinaryAttributeOidsAndNames();
     *   boolean isBinary = binaryOids.contains("JPEGPHOTO");
     * </pre>
     *
     * @return a {@link Set} of upper-cased OID/name strings; never {@code null}.
     */
    public Set<String> getUpperCasedBinaryAttributeOidsAndNames()
    {
        if ( binaryAttributeCache == null )
        {
            binaryAttributeCache = new HashSet<String>();
            BinaryAttribute[] binaryAttributes = getBinaryAttributes();
            for ( BinaryAttribute binaryAttribute : binaryAttributes )
            {
                if ( binaryAttribute.getAttributeNumericOidOrName() != null )
                {
                    binaryAttributeCache.add( binaryAttribute.getAttributeNumericOidOrName().toUpperCase() );
                }
            }
        }
        return binaryAttributeCache;
    }


    // ── Mace Reads The Full Binary-Artefact List From The Codex ──────────────────
    // Mace retrieves the complete written list of binary artefacts from the Codex
    // shelves — each item as a structured {@link BinaryAttribute} record.
    // We load from the Eclipse preference store (which serializes the array).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full array of {@link BinaryAttribute} records from the
     * preference store.
     * Each entry pairs a human-readable name or OID with the "treat as binary"
     * flag so the rendering layer knows how to display it.
     *
     * <p>For example — Mace reads the artefact list:</p>
     * <pre>
     *   BinaryAttribute[] bas = prefs.getBinaryAttributes();
     *   // bas[0].getAttributeNumericOidOrName() might be "2.5.4.36" (userCertificate)
     * </pre>
     *
     * @return the array of binary attributes; never {@code null} (may be empty).
     */
    public BinaryAttribute[] getBinaryAttributes()
    {
        BinaryAttribute[] binaryAttributes = ( BinaryAttribute[] ) load( BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES );
        return binaryAttributes;
    }


    // ── Mace Updates The Binary-Artefact List ────────────────────────────────────
    // When the High Council decides a new artefact must be handled as binary,
    // Mace rewrites that section of the Codex and invalidates any cached summaries
    // so they're rebuilt fresh next time someone asks.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists a new array of binary attributes to the preference store and
     * clears the internal cache so the next read gets fresh data.
     * Call this from the preferences UI page when the user edits the list.
     *
     * <p>For example — Mace updates the Codex:</p>
     * <pre>
     *   prefs.setBinaryAttributes(new BinaryAttribute[]{ new BinaryAttribute("jpegPhoto") });
     *   // binaryAttributeCache is now null — rebuilt on next getUpperCased... call
     * </pre>
     *
     * @param binaryAttributes  the new array of binary attributes to store;
     *                          must not be {@code null}.
     */
    public void setBinaryAttributes( BinaryAttribute[] binaryAttributes )
    {
        store( BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES, binaryAttributes );
        binaryAttributeCache = null;
    }


    // ── Mace Reads The Default Binary-Artefact List ───────────────────────────────
    // Even before the High Council edits anything, the Codex ships with a
    // canonical set of binary artefacts established at founding.  Mace can always
    // retrieve those original defaults.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default array of {@link BinaryAttribute} records — the
     * values set by {@link BrowserCorePreferencesInitializer} at plugin startup
     * before the user makes any changes.
     * Useful when implementing a "restore defaults" button on the prefs page.
     *
     * <p>For example — restoring the founding Codex:</p>
     * <pre>
     *   BinaryAttribute[] defaults = prefs.getDefaultBinaryAttributes();
     *   prefs.setBinaryAttributes(defaults);
     * </pre>
     *
     * @return the default binary attributes array; never {@code null}.
     */
    public BinaryAttribute[] getDefaultBinaryAttributes()
    {
        BinaryAttribute[] binaryAttributes = ( BinaryAttribute[] ) loadDefault( BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES );
        return binaryAttributes;
    }


    // ── Mace Writes The Default Binary-Artefact List ─────────────────────────────
    // At the founding of the Order, the first Grand Master writes the default
    // rules into the Codex.  That's what the initializer calls — once, at startup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the default {@link BinaryAttribute} array into the preference store.
     * Called by {@link BrowserCorePreferencesInitializer} during plugin startup
     * to establish the baseline before any user customisation.
     *
     * <p>For example — writing the defaults at founding:</p>
     * <pre>
     *   prefs.setDefaultBinaryAttributes(new BinaryAttribute[]{ ... });
     * </pre>
     *
     * @param defaultBinaryAttributes  the default values to register; the
     *                                 preference store uses these when the
     *                                 current value has never been set.
     */
    public void setDefaultBinaryAttributes( BinaryAttribute[] defaultBinaryAttributes )
    {
        storeDefault( BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES, defaultBinaryAttributes );
    }


    // ── Mace Checks Which Syntaxes Are Physical ───────────────────────────────────
    // Mace reads the Codex's section on physical (binary) syntaxes and builds a
    // fast-lookup set so individual syntax OIDs can be checked in O(1).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OIDs of all binary syntaxes as an upper-cased {@link Set} for
     * fast lookup.
     * An LDAP syntax is "binary" when its values are raw bytes rather than text
     * (e.g., the Certificate syntax, OID {@code 1.3.6.1.4.1.1466.115.121.1.8}).
     * The result is cached until {@link #setBinarySyntaxes} is called.
     *
     * <p>For example — Mace checks a syntax OID:</p>
     * <pre>
     *   boolean isBinary =
     *     prefs.getUpperCasedBinarySyntaxOids().contains("1.3.6.1.4.1.1466.115.121.1.8".toUpperCase());
     * </pre>
     *
     * @return an upper-cased {@link Set} of syntax OID strings; never {@code null}.
     */
    public Set<String> getUpperCasedBinarySyntaxOids()
    {
        if ( binarySyntaxCache == null )
        {
            binarySyntaxCache = new HashSet<String>();
            BinarySyntax[] binarySyntaxes = getBinarySyntaxes();
            for ( BinarySyntax binarySyntax : binarySyntaxes )
            {
                if ( binarySyntax.getSyntaxNumericOid() != null )
                {
                    binarySyntaxCache.add( binarySyntax.getSyntaxNumericOid().toUpperCase() );
                }
            }
        }
        return binarySyntaxCache;
    }


    // ── Mace Reads The Full Binary-Syntax List ────────────────────────────────────
    // Mace retrieves the structured list of syntax records that are marked as
    // physical in the Codex.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full array of {@link BinarySyntax} records from the preference
     * store.
     * Each entry holds a syntax OID that tells the rendering layer to treat
     * values of that syntax as binary data.
     *
     * <p>For example — Mace reads the syntax list:</p>
     * <pre>
     *   BinarySyntax[] syntaxes = prefs.getBinarySyntaxes();
     * </pre>
     *
     * @return the array of binary syntaxes; never {@code null}.
     */
    public BinarySyntax[] getBinarySyntaxes()
    {
        BinarySyntax[] binarySyntaxes = ( BinarySyntax[] ) load( BrowserCoreConstants.PREFERENCE_BINARY_SYNTAXES );
        return binarySyntaxes;
    }


    // ── Mace Updates The Binary-Syntax List ──────────────────────────────────────
    // The Council adds a new syntax to the physical list and Mace rewrites that
    // section, then discards the cached lookup table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists a new array of binary syntaxes and clears the internal cache.
     *
     * <p>For example — Mace updates the syntax list:</p>
     * <pre>
     *   prefs.setBinarySyntaxes(new BinarySyntax[]{ new BinarySyntax("1.3.6.1...") });
     * </pre>
     *
     * @param binarySyntaxes  the new binary syntax array to persist.
     */
    public void setBinarySyntaxes( BinarySyntax[] binarySyntaxes )
    {
        store( BrowserCoreConstants.PREFERENCE_BINARY_SYNTAXES, binarySyntaxes );
        binarySyntaxCache = null;
    }


    // ── Mace Reads The Default Binary-Syntax List ─────────────────────────────────
    // Mace retrieves the founding defaults for the binary-syntax section.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default {@link BinarySyntax} array set at plugin startup by
     * {@link BrowserCorePreferencesInitializer}.
     *
     * <p>For example — Mace restores founding rules:</p>
     * <pre>
     *   prefs.setBinarySyntaxes(prefs.getDefaultBinarySyntaxes());
     * </pre>
     *
     * @return the default syntax array; never {@code null}.
     */
    public BinarySyntax[] getDefaultBinarySyntaxes()
    {
        BinarySyntax[] binarySyntaxes = ( BinarySyntax[] ) loadDefault( BrowserCoreConstants.PREFERENCE_BINARY_SYNTAXES );
        return binarySyntaxes;
    }


    // ── Mace Writes The Default Binary-Syntax List ───────────────────────────────
    // The founding Grand Master writes the default syntax rules at the Order's
    // inception — called once by the initialiser at startup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the default {@link BinarySyntax} array into the preference store.
     * Called by {@link BrowserCorePreferencesInitializer} at startup.
     *
     * @param defaultBinarySyntaxes  the default binary syntax values.
     */
    public void setDefaultBinarySyntaxes( BinarySyntax[] defaultBinarySyntaxes )
    {
        storeDefault( BrowserCoreConstants.PREFERENCE_BINARY_SYNTAXES, defaultBinarySyntaxes );
    }


    // ── Mace Reads The Object-Class Icon Assignments ─────────────────────────────
    // The Codex also lists which icon represents each class of being — Jedi
    // get the blue lightsaber symbol, Sith get the red one.
    // We retrieve the object-class → icon mappings from the preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current array of {@link ObjectClassIconPair} records.
     * Each pair maps an LDAP object-class name (like {@code person}) to a
     * plugin-relative icon path so the browser tree can show meaningful icons.
     *
     * <p>For example — Mace looks up the icon for "inetOrgPerson":</p>
     * <pre>
     *   ObjectClassIconPair[] icons = prefs.getObjectClassIcons();
     * </pre>
     *
     * @return the icon pairs array; never {@code null}.
     */
    public ObjectClassIconPair[] getObjectClassIcons()
    {
        ObjectClassIconPair[] ocIcons = ( ObjectClassIconPair[] ) load( BrowserCoreConstants.PREFERENCE_OBJECT_CLASS_ICONS );
        return ocIcons;
    }


    // ── Mace Writes New Object-Class Icon Assignments ─────────────────────────────
    // The Council reassigns which symbol represents which order — Mace rewrites
    // the heraldry section of the Codex.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists a new array of object-class icon pairs to the preference store.
     * Call this from the preferences UI when the user edits the icon mappings.
     *
     * @param ocIcons  the new icon-pair array; must not be {@code null}.
     */
    public void setObjectClassIcons( ObjectClassIconPair[] ocIcons )
    {
        store( BrowserCoreConstants.PREFERENCE_OBJECT_CLASS_ICONS, ocIcons );
    }


    // ── Mace Reads The Default Object-Class Icon Assignments ──────────────────────
    // Mace retrieves the founding heraldry from the Codex — the defaults
    // established before any user customisation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default {@link ObjectClassIconPair} array.
     * Useful for "restore defaults" in the preferences UI.
     *
     * @return the default icon pairs; never {@code null}.
     */
    public ObjectClassIconPair[] getDefaultObjectClassIcon()
    {
        ObjectClassIconPair[] ocIcons = ( ObjectClassIconPair[] ) loadDefault( BrowserCoreConstants.PREFERENCE_OBJECT_CLASS_ICONS );
        return ocIcons;
    }


    // ── Mace Writes The Default Object-Class Icon Assignments ────────────────────
    // The founding Grand Master establishes the heraldry at inception.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the default {@link ObjectClassIconPair} array into the preference store.
     *
     * @param ocIcons  the default icon pairs to register.
     */
    public void setDefaultObjectClassIcons( ObjectClassIconPair[] ocIcons )
    {
        storeDefault( BrowserCoreConstants.PREFERENCE_OBJECT_CLASS_ICONS, ocIcons );
    }


    // ── Mace Retrieves A Page From The Codex ─────────────────────────────────────
    // Mace opens the Codex to the relevant page, deserializes the structured data
    // stored there, and returns the object.  He uses this privately for all three
    // preference groups above.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads and deserializes the preference value stored under the given key.
     * We use Java serialization (via {@link Utils#deserialize}) to convert the
     * stored string back into the array of model objects.
     *
     * <p>For example — Mace reads a preference page:</p>
     * <pre>
     *   Object val = load(BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES);
     *   BinaryAttribute[] attrs = (BinaryAttribute[]) val;
     * </pre>
     *
     * @param key  the preference key (one of the {@code PREFERENCE_*} constants
     *             from {@link BrowserCoreConstants}).
     * @return the deserialized object; the caller must cast to the expected type.
     */
    private static Object load( String key )
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();
        String s = store.getString( key );
        return Utils.deserialize( s );
    }


    // ── Mace Writes A Page Back Into The Codex ───────────────────────────────────
    // Mace serializes the updated object and writes the encoded form back into
    // the relevant Codex page, then saves the whole Codex to disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given object and stores it in the preference store under
     * the given key, then flushes the store to disk.
     *
     * <p>For example — Mace writes a preference page:</p>
     * <pre>
     *   store(BrowserCoreConstants.PREFERENCE_BINARY_ATTRIBUTES, binaryAttrs);
     * </pre>
     *
     * @param key  the preference key.
     * @param o    the object to serialize and store.
     */
    private static void store( String key, Object o )
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();
        String s = Utils.serialize( o );
        store.setValue( key, s );
        BrowserCorePlugin.getDefault().savePluginPreferences();
    }


    // ── Mace Reads A Default Page From The Codex ─────────────────────────────────
    // Mace reads the default section of the Codex — the entries written at
    // founding, not the current ones the Council may have overwritten.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads and deserializes the default preference value for the given key.
     * Defaults are set by {@link BrowserCorePreferencesInitializer} at startup.
     *
     * @param key  the preference key.
     * @return the deserialized default object; caller must cast appropriately.
     */
    private static Object loadDefault( String key )
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();
        String s = store.getDefaultString( key );
        return Utils.deserialize( s );
    }


    // ── Mace Writes A Default Page Into The Codex ────────────────────────────────
    // Mace writes the founding rules into the default section of the Codex —
    // these are the values the store falls back to if the current section is
    // cleared.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes the given object and stores it as the default preference value
     * for the given key.
     * Called during plugin startup by {@link BrowserCorePreferencesInitializer}.
     *
     * @param key  the preference key.
     * @param o    the default object to serialize and register.
     */
    private static void storeDefault( String key, Object o )
    {
        Preferences store = BrowserCorePlugin.getDefault().getPluginPreferences();
        String s = Utils.serialize( o );
        store.setDefault( key, s );
        BrowserCorePlugin.getDefault().savePluginPreferences();
    }
}
