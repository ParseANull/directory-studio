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


// ── CLASS: OlcValSortConfig — Lando Sorting Cloud City's Trade Ledgers ────────
// Lando doesn't just store the trade records in Cloud City's ledgers — he sorts
// them. Tibanna gas contracts are sorted by value descending; crew assignments
// by name ascending. Each ledger (each attribute) has its own sort rule.
// The valsort overlay does the same for LDAP: it sorts multi-valued attribute
// values returned in search results according to configured rules. OlcValSortConfig
// holds the list of sort rules (olcValSortAttr), each specifying an attribute name,
// a base DN scope, and a sort method.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcValSortConfig} object class (note: the original Javadoc
 * incorrectly says olcRefintConfig), which configures the OpenLDAP value sort (valsort)
 * overlay. The valsort overlay sorts the values of specified multi-valued attributes
 * before returning them in search results, according to alphabetic or numeric sort order.
 * Think of this as Lando maintaining sorted trade ledgers in Cloud City.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcValSortConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcValSortAttr' attribute.
     */
    @ConfigurationElement(attributeType = "olcValSortAttr", isOptional = false, version="2.4.0")
    private List<String> olcValSortAttr = new ArrayList<>();


    // ── Default Constructor — Lando Opens the Valsort Ledger Department ───────────
    // Lando opens a new department dedicated to sorted attributes and stamps it
    // with the overlay type name ("valsort") so OpenLDAP loads the right plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcValSortConfig with the overlay type set to "valsort".
     *
     * <p>For example — Lando opens the valsort department:</p>
     * <pre>
     *   OlcValSortConfig valsort = new OlcValSortConfig();
     *   valsort.getOlcOverlay(); // "valsort"
     * </pre>
     */
    public OlcValSortConfig()
    {
        super();
        olcOverlay = "valsort";
    }


    // ── Copy Constructor — Lando Copies the Ledger Config ────────────────────────
    // Lando copies the valsort config so the editor can modify it without altering
    // the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcValSortConfig.
     *
     * <p>For example — Lando copies the ledger config:</p>
     * <pre>
     *   OlcValSortConfig copy = new OlcValSortConfig( originalValSortConfig );
     * </pre>
     *
     * @param o  the OlcValSortConfig to copy
     */
    public OlcValSortConfig( OlcValSortConfig o )
    {
        super();
        olcValSortAttr = o.olcValSortAttr;
    }


    // ── addOlcValSortAttr — Lando Adds a Sort Rule to the Ledger ─────────────────
    // Lando adds a new sort rule to his ledger: "sort the 'member' attribute values
    // under ou=groups,dc=example,dc=com alphabetically ascending."
    // Each string is an OlcValSortValue: "attribute baseDn [weighted] sortMethod".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more olcValSortAttr rule strings to the sort rule list.
     * Each string defines one attribute-sorting rule, in the format:
     * {@code <attribute> "<baseDn>" [weighted] <sortMethod>}.
     *
     * <p>For example — Lando adds a sort rule:</p>
     * <pre>
     *   valsortConfig.addOlcValSortAttr(
     *       "member \"ou=groups,dc=example,dc=com\" alpha-ascend" );
     * </pre>
     *
     * @param strings  the olcValSortAttr rule strings to add
     */
    public void addOlcValSortAttr( String... strings )
    {
        for ( String string : strings )
        {
            olcValSortAttr.add( string );
        }
    }


    // ── clearOlcValSortAttr — Lando Wipes the Sort Rules ─────────────────────────
    // Lando erases all sort rules from his ledger, typically before a full reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all olcValSortAttr entries from the sort rule list.
     *
     * <p>For example — Lando clears the ledger rules:</p>
     * <pre>
     *   valsortConfig.clearOlcValSortAttr();
     * </pre>
     */
    public void clearOlcValSortAttr()
    {
        olcValSortAttr.clear();
    }


    // ── getOlcValSortAttr — Lando Reads All Sort Rules ───────────────────────────
    // Lando reads the full list of attribute sort rules from his ledger for display
    // or serialization.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of olcValSortAttr rule strings defining per-attribute sort orders.
     *
     * <p>For example — Lando reads the sort rules:</p>
     * <pre>
     *   List&lt;String&gt; rules = valsortConfig.getOlcValSortAttr();
     * </pre>
     *
     * @return  the live list of sort rule strings; never null
     */
    public List<String> getOlcValSortAttr()
    {
        return olcValSortAttr;
    }


    // ── setOlcValSortAttr — Lando Replaces All Sort Rules ────────────────────────
    // Lando replaces the entire sort rule list with a new one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire list of olcValSortAttr rules.
     *
     * <p>For example — Lando loads a new rule set:</p>
     * <pre>
     *   valsortConfig.setOlcValSortAttr( newRuleList );
     * </pre>
     *
     * @param olcValSortAttr  the new list of sort rule strings
     */
    public void setOlcValSortAttr( List<String> olcValSortAttr )
    {
        this.olcValSortAttr = olcValSortAttr;
    }


    // ── copy — Lando Duplicates the Valsort Config ────────────────────────────────
    // Lando copies the entire sort config so the editor can work on a safe copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcValSortConfig.
     *
     * <p>For example — Lando duplicates the config:</p>
     * <pre>
     *   OlcValSortConfig copy = valsortConfig.copy();
     * </pre>
     *
     * @return  a new OlcValSortConfig with the same field values as this one
     */
    @Override
    public OlcValSortConfig copy()
    {
        return new OlcValSortConfig( this );
    }
}
