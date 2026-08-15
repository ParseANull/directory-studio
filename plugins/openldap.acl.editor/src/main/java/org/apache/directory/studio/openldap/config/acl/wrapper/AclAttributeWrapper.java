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

import org.apache.directory.studio.openldap.config.acl.model.AclAttribute;

// ── CLASS: AclAttributeWrapper — C-3PO TRANSLATING FOR THE JAWA MARKET ────────
// C-3PO stands between the crew and the Jawas, translating raw droids into
// something the crew can work with: clone them, compare them, sort them,
// display them in the UI. This wrapper does the same for AclAttribute — it
// adds the cloning, equals/hashCode, Comparable, and toString contract that
// the table widget and dialog machinery need, without touching the core model.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin wrapper around {@link AclAttribute} that makes it usable as a table
 * row element. Adds {@link Cloneable}, {@link Comparable}, {@link Object#equals}
 * and {@link Object#hashCode} so the JFace table widget and add/edit dialogs
 * can work with attribute items naturally.
 * Think of this class as C-3PO standing between the raw {@link AclAttribute}
 * model and the Imperial table widget — translating, comparing, and presenting
 * the data in the format the UI demands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAttributeWrapper implements Cloneable, Comparable<AclAttributeWrapper>
{
    /** The AclAttribute */
    private AclAttribute aclAttribute;

    // ── Default Constructor: Wrap the Default Attribute ───────────────────────
    // C-3PO picks up the first available droid off the Jawa sandcrawler —
    // extensibleObject — when no specific model is supplied.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around a default {@link AclAttribute} whose name is
     * {@code extensibleObject}. Use this when the dialog creates a new row
     * before the user has typed anything.
     */
    public AclAttributeWrapper()
    {
        // Default to ExtensibleObject
        aclAttribute = new AclAttribute( "extensibleObject", null );
    }

    // ── Wrapping an Existing Attribute ────────────────────────────────────────
    // The crew hands C-3PO a specific droid and he wraps it in his translation
    // protocol so the table widget can handle it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link AclAttribute}.
     *
     * <p>For example — C-3PO taking responsibility for a specific droid:</p>
     * <pre>
     *   AclAttributeWrapper w = new AclAttributeWrapper(new AclAttribute("uid", conn));
     * </pre>
     *
     * @param aclAttribute  The attribute model to wrap; must not be {@code null}.
     */
    public AclAttributeWrapper( AclAttribute aclAttribute )
    {
        this.aclAttribute = aclAttribute;
    }


    // ── Reading the Wrapped Attribute ─────────────────────────────────────────
    // C-3PO hands back the raw droid when someone needs to inspect the model
    // directly rather than the translation wrapper.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wrapped {@link AclAttribute} model object.
     *
     * <p>For example — retrieving the model to read its schema information:</p>
     * <pre>
     *   AclAttribute attr = wrapper.getAclAttribute();
     *   attr.isObjectClass(); // true for "@inetOrgPerson"
     * </pre>
     *
     * @return  The wrapped {@link AclAttribute}; never {@code null} after construction.
     */
    public AclAttribute getAclAttribute()
    {
        return aclAttribute;
    }

    // ── Replacing the Wrapped Attribute Model ─────────────────────────────────
    // The dialog replaces the model when the user saves a new selection.
    // C-3PO swaps out the droid he is translating for.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the wrapped {@link AclAttribute} with a new model. Called by the
     * dialog when the user clicks OK with a different attribute selection.
     *
     * @param aclAttribute  The new attribute model.
     */
    public void setAclAttribute( AclAttribute aclAttribute )
    {
        this.aclAttribute = aclAttribute;
    }

    // ── Replacing the Attribute by Name ───────────────────────────────────────
    // A shorthand for when we only have the name string and want C-3PO to
    // build a new attribute model from it on the spot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the wrapped attribute by constructing a new {@link AclAttribute}
     * from the given name (with no connection, so no schema lookup). Convenient
     * when only the name string is known at the call site.
     *
     * <p>For example — replacing the wrapped attribute from a text field value:</p>
     * <pre>
     *   wrapper.setAclAttribute("@inetOrgPerson");
     *   wrapper.getAclAttribute().isObjectClass(); // true
     * </pre>
     *
     * @param name  The attribute name string (may include @ or ! prefix).
     */
    public void setAclAttribute( String name )
    {
        this.aclAttribute = new AclAttribute( name, null );
    }


    // ── Cloning the Wrapper for the Add/Edit Dialog ───────────────────────────
    // C-3PO makes an exact copy of himself (hypothetically) so the dialog
    // can edit the copy without touching the original until the user clicks OK.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a shallow clone of this wrapper. The table widget uses this to
     * create a working copy for the add/edit dialog so changes can be discarded
     * if the user cancels.
     *
     * <p>For example — the dialog cloning the selected row before editing:</p>
     * <pre>
     *   AclAttributeWrapper copy = wrapper.clone();
     *   // edit copy in the dialog; on OK replace wrapper with copy
     * </pre>
     *
     * @return  A cloned {@link AclAttributeWrapper}; {@code null} if cloning somehow fails.
     */
    public AclAttributeWrapper clone()
    {
        try
        {
            return (AclAttributeWrapper)super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            return null;
        }
    }


    // ── Checking Equality Between Two Attribute Wrappers ─────────────────────
    // C-3PO checks whether two droids are logically the same: same name,
    // same type category. We use this so the table widget can detect duplicates.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Two wrappers are equal if they wrap attributes with the same name
     * (case-insensitive) and the same type category (both are attribute types,
     * or both are objectClass / objectClass-exclusion entries). This lets the
     * add dialog enforce uniqueness in the attribute list.
     *
     * <p>For example — preventing duplicate entries:</p>
     * <pre>
     *   wrapper("uid").equals(wrapper("uid")); // true
     *   wrapper("uid").equals(wrapper("cn"));  // false
     * </pre>
     *
     * @param that  The object to compare against.
     * @return      {@code true} if the wrapped attributes are logically equivalent.
     */
    public boolean equals( Object that )
    {
        // Quick test
        if ( this == that )
        {
            return true;
        }

        if ( that instanceof AclAttributeWrapper )
        {
            AclAttributeWrapper thatInstance = (AclAttributeWrapper)that;

            return aclAttribute.getName().equalsIgnoreCase( thatInstance.aclAttribute.getName() ) &&
                   ( aclAttribute.isAttributeType() && thatInstance.aclAttribute.isAttributeType() ||
                     ( ( aclAttribute.isObjectClass() || aclAttribute.isObjectClassNotAllowed() ) &&
                         ( thatInstance.aclAttribute.isObjectClass() || thatInstance.aclAttribute.isObjectClassNotAllowed() ) ) );
        }
        else
        {
            return false;
        }
    }


    // ── Computing the Hash Code for Map/Set Use ───────────────────────────────
    // C-3PO computes a compact numeric fingerprint for the wrapped droid so
    // hash-based collections can store and retrieve it efficiently.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code consistent with {@link #equals(Object)}: based on
     * the wrapped attribute's name. Allows wrappers to be stored in {@code HashMap}
     * or {@code HashSet} correctly.
     *
     * @return  A hash code for this wrapper.
     */
    public int hashCode()
    {
        int h = 37;

        if ( aclAttribute != null )
        {
            h += h*17 + aclAttribute.getName().hashCode();
        }

        return h;
    }


    // ── Comparing Two Wrappers for Sort Order ─────────────────────────────────
    // C-3PO sorts droids alphabetically by designation so they appear in
    // a consistent order in the table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares this wrapper to another by the attribute name (case-insensitive).
     * Used to sort the table rows alphabetically.
     *
     * <p>For example — sorting a list of attribute wrappers:</p>
     * <pre>
     *   Collections.sort(wrapperList);
     *   // wrapperList is now in alphabetical attribute-name order
     * </pre>
     *
     * @param that  The other wrapper to compare against.
     * @return      A negative, zero, or positive integer as per {@link Comparable#compareTo}.
     */
    public int compareTo( AclAttributeWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        // Check the AclAttribute
        return aclAttribute.getName().compareToIgnoreCase( that.getAclAttribute().getName() );
    }


    // ── Converting the Wrapper to Display Text ────────────────────────────────
    // C-3PO produces a human-readable description of the droid — with proper
    // prefixes for ObjectClass ("@") and exclusion ("!") types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to the wrapped {@link AclAttribute#toString()} method to produce
     * the display string for the table cell — including any "@" or "!" prefix
     * as appropriate.
     *
     * <p>For example — the table cell showing the attribute in display form:</p>
     * <pre>
     *   wrapper("uid").toString()            // → "uid"
     *   wrapper("@inetOrgPerson").toString() // → "@inetOrgPerson"
     * </pre>
     *
     * @return  The formatted attribute string.
     */
    public String toString()
    {
        return aclAttribute.toString();
    }
}
