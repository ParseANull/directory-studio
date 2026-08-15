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
package org.apache.directory.studio.openldap.config.model;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// ── CLASS: ConfigurationElement — C-3PO TRANSLATES THE JAWA DIALECT ──────────
// In the Jawa sandcrawler, C-3PO is the only one who can decode what the Jawas
// are actually saying about each droid they sell — what type it is, whether it's
// optional inventory, what its default value is. This annotation plays exactly
// that role: it sits on each Java field and tells our I/O layer "here's the LDAP
// attribute name, here's the default, here's whether it's required."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Annotation that marks a field in a config bean as a serializable LDAP configuration
 * attribute, and carries the metadata our I/O layer needs to read and write it.
 * Think of this as C-3PO whispering to R2 exactly how to translate a field into
 * LDAP-speak and back again.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationElement
{
    // ── C-3PO Names The Droid Type ────────────────────────────────────────────────
    // C-3PO inspects the droid fresh off the sandcrawler and announces its official
    // designation — "This is an R2 unit, model olcDatabase." Without that designation
    // nobody knows which slot in the LDAP entry this field maps to.
    // The parallel: attributeType() tells the reader/writer the exact LDAP attribute
    // name to use when serializing this Java field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute type name that this field maps to — e.g. "olcSizeLimit".
     * The I/O layer uses this to know which LDAP attribute to read from or write to when
     * converting between Java objects and LDAP entries.
     *
     * <p>For example — C-3PO announces the droid type on the sandcrawler ramp:</p>
     * <pre>
     *   Jawa: *incomprehensible clicking*
     *   C-3PO: "It says this unit's attribute type is 'olcSizeLimit', Master Luke."
     *   Luke: "Great, now we know where to put it in the entry."
     * </pre>
     *
     * @return  the LDAP attribute type string, or an empty string if not set
     */
    String attributeType() default "";


    // ── C-3PO Recalls The Factory Default ────────────────────────────────────────
    // When Luke asks what happens if a droid has no special programming loaded,
    // C-3PO knows the factory default behavior and recites it verbatim. We need
    // that same fallback for fields that OpenLDAP will assume a value for when
    // none is explicitly written to the config.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation of the default value for this attribute.
     * When the field is null but OpenLDAP assumes a default, this lets us know
     * what that default is so we don't write unnecessary values to the entry.
     *
     * <p>For example — C-3PO recites the factory default programming:</p>
     * <pre>
     *   Luke: "What if nobody set olcSizeLimit?"
     *   C-3PO: "The factory default is '500', Master Luke. No need to write it."
     * </pre>
     *
     * @return  the default value as a string, or empty string if there is no default
     */
    String defaultValue() default "";


    // ── C-3PO Checks If The Droid Is Optional Cargo ──────────────────────────────
    // The Jawas have some droids they'll sell if asked, and some they insist on
    // including in every deal. C-3PO knows which is which. Likewise, some LDAP
    // attributes are optional (may or may not appear in the entry) and some are
    // required for the entry to be valid at all.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this attribute is optional in the LDAP entry — it may
     * or may not be present. Returns {@code false} if it's required, meaning the entry
     * is invalid without it.
     *
     * <p>For example — C-3PO explains which droids are negotiable:</p>
     * <pre>
     *   C-3PO: "The R2 unit is optional — the Jawas will sell without it."
     *   C-3PO: "But olcDatabase is NOT optional. No database type, no valid entry."
     * </pre>
     *
     * @return  {@code true} if the attribute may be absent from the entry, {@code false} if it must be present
     */
    boolean isOptional() default true;


    // ── C-3PO Identifies The Entry's Name Tag ────────────────────────────────────
    // Every droid in the sandcrawler has one special identifier stamped on its chest
    // that's its primary name — that's what goes in the sale manifest header. In LDAP,
    // the RDN (relative distinguished name) is that primary identifier. This flag
    // marks which field carries the RDN value for this config entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this field's attribute type and value form the RDN
     * (Relative Distinguished Name) of the LDAP entry — basically, it's the field
     * that makes the entry's address unique within its parent.
     *
     * <p>For example — C-3PO points to the primary ID stamp:</p>
     * <pre>
     *   C-3PO: "This field, olcDatabase={0}mdb, is the RDN. It goes in the DN."
     *   C-3PO: "All other fields are just attributes on the entry, not part of its address."
     * </pre>
     *
     * @return  {@code true} if this field is the RDN attribute, {@code false} otherwise
     */
    boolean isRdn() default false;

    // ── C-3PO Notes The Protocol Version Required ─────────────────────────────────
    // C-3PO checks the droid's manufacturing stamp to confirm it's compatible with
    // the current moisture-farm power grid — some droids need a newer grid version.
    // Similarly, some LDAP attributes only appeared in certain OpenLDAP releases, so
    // we track the minimum version that introduced this field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the minimum OpenLDAP version string in which this attribute was introduced,
     * e.g. "2.4.36". The I/O layer uses this to decide whether to include the attribute
     * when targeting an older server that doesn't know about it yet.
     *
     * <p>For example — C-3PO checks the manufacturing stamp:</p>
     * <pre>
     *   C-3PO: "olcListenerThreads requires OpenLDAP 2.4.36 or newer, Master Luke."
     *   C-3PO: "If the server is older, we must omit this attribute or it will reject the entry."
     * </pre>
     *
     * @return  the version string, e.g. "2.4.0" or "2.4.36"
     */
    String version();
}
