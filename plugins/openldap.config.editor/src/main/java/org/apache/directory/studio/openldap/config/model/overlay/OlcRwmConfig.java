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
package org.apache.directory.studio.openldap.config.model.overlay;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcRwmConfig — C-3PO Translating Between Two Directory Dialects ───
// When the Rebel Alliance needs to communicate with the Gungans, C-3PO steps in
// as translator: he maps words (attributes) and concepts (object classes) from one
// dialect to another, rewrites the DN structure so the target side understands, and
// normalizes the output so no ambiguity leaks through.
// The rwm (Rewrite/Remap) overlay does exactly that for LDAP: it intercepts
// operations and rewrites attribute names, object class names, and DNs on the fly
// so two directories with different schemas can interoperate transparently.
// OlcRwmConfig holds the mapping rules (olcRwmMap), the rewrite rules (olcRwmRewrite),
// and behavioural flags like normalization and Tree-Filter support.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcRwmConfig} object class (note: the Javadoc in the
 * original source incorrectly says olcPPolicyConfig), which configures the OpenLDAP
 * rewrite/remap (rwm) overlay.
 * The rwm overlay intercepts LDAP operations and applies attribute/objectclass mapping
 * rules and DN rewrite rules, letting a proxy front-end translate between two schemas.
 * Think of this as C-3PO translating between two directory dialects — "I am fluent
 * in over six million forms of LDAP schema."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcRwmConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcRwmMap' attribute.
     */
    @ConfigurationElement(attributeType = "olcRwmMap", version="2.4.0")
    private List<String> olcRwmMap = new ArrayList<>();

    /**
     * Field for the 'olcRwmNormalizeMapped' attribute.
     */
    @ConfigurationElement(attributeType = "olcRwmNormalizeMapped", version="2.4.0")
    private Boolean olcRwmNormalizeMapped;

    /**
     * Field for the 'olcRwmRewrite' attribute.
     */
    @ConfigurationElement(attributeType = "olcRwmRewrite", version="2.4.0")
    private List<String> olcRwmRewrite = new ArrayList<>();

    /**
     * Field for the 'olcRwmTFSupport' attribute.
     */
    @ConfigurationElement(attributeType = "olcRwmTFSupport", version="2.4.0")
    private String olcRwmTFSupport;


    // ── Default Constructor — C-3PO Boots Up the Translation Module ──────────────
    // C-3PO powers up his translation module with the overlay type name ("rwm")
    // so OpenLDAP loads the right translation plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcRwmConfig with the overlay type set to "rwm".
     *
     * <p>For example — C-3PO boots up the translation module:</p>
     * <pre>
     *   OlcRwmConfig rwm = new OlcRwmConfig();
     *   rwm.getOlcOverlay(); // "rwm"
     * </pre>
     */
    public OlcRwmConfig()
    {
        super();
        olcOverlay = "rwm";
    }


    // ── Copy Constructor — C-3PO Duplicates His Translation Rulebook ─────────────
    // C-3PO makes a copy of his entire rulebook — all map entries and rewrite rules —
    // so the editor can safely modify the copy without altering the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcRwmConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — C-3PO copies his rulebook:</p>
     * <pre>
     *   OlcRwmConfig copy = new OlcRwmConfig( originalRwmConfig );
     * </pre>
     *
     * @param o  the OlcRwmConfig to copy
     */
    public OlcRwmConfig( OlcRwmConfig o )
    {
        super( o );
        olcRwmMap = o.olcRwmMap;
        olcRwmNormalizeMapped = o.olcRwmNormalizeMapped;
        olcRwmRewrite = o.olcRwmRewrite;
        olcRwmTFSupport = o.olcRwmTFSupport;
    }


    // ── addOlcRwmMap — C-3PO Adds a Translation Entry ────────────────────────────
    // C-3PO adds a new phrase-to-phrase mapping (e.g., "attribute uid login") to his
    // translation dictionary so he knows how to remap that schema element.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcRwmMap rule strings to the attribute/objectclass mapping list.
     * Each string follows the format "attribute|objectclass localName foreignName".
     *
     * <p>For example — C-3PO adds a translation entry:</p>
     * <pre>
     *   rwmConfig.addOlcRwmMap( "attribute uid login" );
     *   rwmConfig.addOlcRwmMap( "objectclass inetOrgPerson person" );
     * </pre>
     *
     * @param strings  the olcRwmMap rule strings to add
     */
    public void addOlcRwmMap( String... strings )
    {
        for ( String string : strings )
        {
            olcRwmMap.add( string );
        }
    }


    // ── addOlcRwmRewrite — C-3PO Adds a DN Rewrite Rule ──────────────────────────
    // C-3PO adds a structural rewrite rule that transforms DNs from the local
    // naming convention to the remote one (or vice versa).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcRwmRewrite rule strings to the DN rewrite rule list.
     * Each string defines a regular-expression-based rule for transforming DNs
     * as they pass through the overlay.
     *
     * <p>For example — C-3PO adds a DN rewrite rule:</p>
     * <pre>
     *   rwmConfig.addOlcRwmRewrite( "rewriteRule \"(.+),dc=old,dc=com\" \"$1,dc=new,dc=com\"" );
     * </pre>
     *
     * @param strings  the olcRwmRewrite rule strings to add
     */
    public void addOlcRwmRewrite( String... strings )
    {
        for ( String string : strings )
        {
            olcRwmRewrite.add( string );
        }
    }


    // ── clearOlcRwmMap — C-3PO Wipes the Translation Dictionary ─────────────────
    // C-3PO erases all phrase-mapping entries from his translation dictionary —
    // useful when reloading the map from scratch.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all olcRwmMap entries from the mapping list.
     *
     * <p>For example — C-3PO wipes the dictionary:</p>
     * <pre>
     *   rwmConfig.clearOlcRwmMap();
     * </pre>
     */
    public void clearOlcRwmMap()
    {
        olcRwmMap.clear();
    }


    // ── clearOlcRwmRewrite — C-3PO Wipes the Rewrite Rules ───────────────────────
    // C-3PO erases all DN rewrite rules — useful before a full reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all olcRwmRewrite entries from the rewrite rule list.
     *
     * <p>For example — C-3PO clears the rewrite rules:</p>
     * <pre>
     *   rwmConfig.clearOlcRwmRewrite();
     * </pre>
     */
    public void clearOlcRwmRewrite()
    {
        olcRwmRewrite.clear();
    }


    // ── getOlcRwmMap — C-3PO Reads the Translation Dictionary ────────────────────
    // C-3PO reads the full attribute/objectclass mapping list for display or processing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of olcRwmMap rule strings defining attribute and objectclass
     * name mappings between the local and remote schemas.
     *
     * <p>For example — C-3PO reads the dictionary:</p>
     * <pre>
     *   List&lt;String&gt; maps = rwmConfig.getOlcRwmMap();
     *   // ["attribute uid login", "objectclass inetOrgPerson person"]
     * </pre>
     *
     * @return  the live list of mapping rule strings; never null
     */
    public List<String> getOlcRwmMap()
    {
        return olcRwmMap;
    }


    // ── getOlcRwmNormalizeMapped — C-3PO Checks the Normalization Flag ────────────
    // C-3PO checks whether mapped values should be normalized to their canonical form
    // after translation, ensuring no ambiguity slips through.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether mapped attribute/objectclass names should be normalized after mapping.
     * If TRUE, the overlay normalizes the mapped values (useful if the remote schema
     * uses non-canonical forms).
     *
     * <p>For example — C-3PO checks the normalization flag:</p>
     * <pre>
     *   Boolean normalize = rwmConfig.getOlcRwmNormalizeMapped();
     * </pre>
     *
     * @return  TRUE to normalize mapped values, FALSE to leave as-is, null if unconfigured
     */
    public Boolean getOlcRwmNormalizeMapped()
    {
        return olcRwmNormalizeMapped;
    }


    // ── getOlcRwmRewrite — C-3PO Reads the DN Rewrite Rules ──────────────────────
    // C-3PO reads the full list of DN rewrite rules he applies to transform DNs
    // between the local and remote naming conventions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of olcRwmRewrite rule strings defining DN transformation rules.
     *
     * <p>For example — C-3PO reads the rewrite rules:</p>
     * <pre>
     *   List&lt;String&gt; rewrites = rwmConfig.getOlcRwmRewrite();
     * </pre>
     *
     * @return  the live list of rewrite rule strings; never null
     */
    public List<String> getOlcRwmRewrite()
    {
        return olcRwmRewrite;
    }


    // ── getOlcRwmTFSupport — C-3PO Checks the Tree Filter Support Mode ───────────
    // C-3PO checks whether to support tree filter assertions in the translated
    // LDAP search operations — "yes", "no", or "discover" (auto-detect from server).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcRwmTFSupport setting, which controls support for Tree Filter
     * assertions in proxied LDAP operations. Valid values are "yes", "no", or "discover".
     *
     * <p>For example — C-3PO checks tree-filter support:</p>
     * <pre>
     *   String tfSupport = rwmConfig.getOlcRwmTFSupport(); // "yes" / "no" / "discover"
     * </pre>
     *
     * @return  the TF support mode string, or null if not configured
     */
    public String getOlcRwmTFSupport()
    {
        return olcRwmTFSupport;
    }


    // ── setOlcRwmMap — C-3PO Replaces the Translation Dictionary ─────────────────
    // C-3PO replaces the whole translation dictionary with a new list of entries.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire olcRwmMap list.
     *
     * <p>For example — C-3PO loads a new translation dictionary:</p>
     * <pre>
     *   rwmConfig.setOlcRwmMap( newMapList );
     * </pre>
     *
     * @param olcRwmMap  the new list of mapping rule strings
     */
    public void setOlcRwmMap( List<String> olcRwmMap )
    {
        this.olcRwmMap = olcRwmMap;
    }


    // ── setOlcRwmNormalizeMapped — C-3PO Enables or Disables Normalization ────────
    // C-3PO enables or disables post-mapping normalization.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether mapped values should be normalized after translation.
     *
     * <p>For example — C-3PO enables normalization:</p>
     * <pre>
     *   rwmConfig.setOlcRwmNormalizeMapped( Boolean.TRUE );
     * </pre>
     *
     * @param olcRwmNormalizeMapped  TRUE to normalize, FALSE to leave as-is
     */
    public void setOlcRwmNormalizeMapped( Boolean olcRwmNormalizeMapped )
    {
        this.olcRwmNormalizeMapped = olcRwmNormalizeMapped;
    }


    // ── setOlcRwmRewrite — C-3PO Loads a New Rewrite Rulebook ────────────────────
    // C-3PO loads a completely new set of DN rewrite rules.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire olcRwmRewrite list with the given rules.
     *
     * <p>For example — C-3PO loads a new rewrite rulebook:</p>
     * <pre>
     *   rwmConfig.setOlcRwmRewrite( newRewriteList );
     * </pre>
     *
     * @param olcRwmRewrite  the new list of rewrite rule strings
     */
    public void setOlcRwmRewrite( List<String> olcRwmRewrite )
    {
        this.olcRwmRewrite = olcRwmRewrite;
    }


    // ── setOlcRwmTFSupport — C-3PO Sets the Tree Filter Support Mode ─────────────
    // C-3PO configures whether he supports tree filter assertions in translated operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcRwmTFSupport mode ("yes", "no", or "discover").
     *
     * <p>For example — C-3PO sets TF support to auto-detect:</p>
     * <pre>
     *   rwmConfig.setOlcRwmTFSupport( "discover" );
     * </pre>
     *
     * @param olcRwmTFSupport  the tree filter support mode string
     */
    public void setOlcRwmTFSupport( String olcRwmTFSupport )
    {
        this.olcRwmTFSupport = olcRwmTFSupport;
    }


    // ── copy — C-3PO Duplicates His Entire Translation Module ────────────────────
    // C-3PO creates an exact copy of his translation module config for the editor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcRwmConfig.
     *
     * <p>For example — C-3PO duplicates his translation module:</p>
     * <pre>
     *   OlcRwmConfig copy = rwmConfig.copy();
     * </pre>
     *
     * @return  a new OlcRwmConfig with the same field values as this one
     */
    @Override
    public OlcRwmConfig copy()
    {
        return new OlcRwmConfig( this );
    }
}
