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
package org.apache.directory.studio.aciitemeditor.model;


import org.apache.directory.api.ldap.aci.protectedItem.AllAttributeValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AllUserAttributeTypesAndValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AllUserAttributeTypesItem;
import org.apache.directory.api.ldap.aci.protectedItem.AttributeTypeItem;
import org.apache.directory.api.ldap.aci.protectedItem.AttributeValueItem;
import org.apache.directory.api.ldap.aci.protectedItem.ClassesItem;
import org.apache.directory.api.ldap.aci.protectedItem.EntryItem;
import org.apache.directory.api.ldap.aci.protectedItem.MaxImmSubItem;
import org.apache.directory.api.ldap.aci.protectedItem.MaxValueCountItem;
import org.apache.directory.api.ldap.aci.protectedItem.RangeOfValuesItem;
import org.apache.directory.api.ldap.aci.protectedItem.RestrictedByItem;
import org.apache.directory.api.ldap.aci.protectedItem.SelfValueItem;
import org.apache.directory.studio.aciitemeditor.valueeditors.AttributeTypeAndValueValueEditor;
import org.apache.directory.studio.aciitemeditor.valueeditors.AttributeTypeValueEditor;
import org.apache.directory.studio.aciitemeditor.valueeditors.FilterValueEditor;
import org.apache.directory.studio.aciitemeditor.valueeditors.MaxValueCountValueEditor;
import org.apache.directory.studio.aciitemeditor.valueeditors.RestrictedByValueEditor;
import org.apache.directory.studio.valueeditors.TextValueEditor;
import org.apache.directory.studio.valueeditors.integer.IntegerValueEditor;


// ── CLASS: ProtectedItemWrapperFactory — THE ISB MANIFEST PRINT SHOP ──────────
// Before an ISB officer can review a clearance manifest, the print shop
// pre-prints one row for every possible resource category, leaving the
// value fields blank ready for the officer to fill in.
// This factory is that print shop: it stamps out all 12 protected-item rows
// with their correct editors pre-wired.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Factory that creates the full set of {@link ProtectedItemWrapper} instances for
 * the protected-items table viewer.
 * Produces one wrapper per ACI protected-item category (12 in total), each
 * pre-wired with the appropriate value editor so the table is ready to use.
 * Think of this class as the ISB manifest print shop: it stamps out every
 * resource-category row before the officer sits down to tick and configure them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ProtectedItemWrapperFactory
{

    // ── STAMP OUT ALL 12 MANIFEST ROWS ───────────────────────────────────────
    // The print shop runs through the full list of resource categories — entry,
    // allUserAttributeTypes, attributeType, etc. — and produces one pre-printed
    // row for each, binding the correct value editor to rows that need one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns all 12 {@link ProtectedItemWrapper} instances, one for
     * each ACI protected-item category, ready to be fed to the protected-items
     * table viewer.
     * Each wrapper has its value editor (or {@code null} for categories with no
     * configurable value) and its ACI prefix/suffix already set.
     *
     * <p>For example — the table gets its rows during dialog initialisation:</p>
     * <pre>
     *   ProtectedItemWrapper[] rows = ProtectedItemWrapperFactory.createProtectedItemWrappers();
     *   tableViewer.setInput(rows);
     * </pre>
     *
     * @return an array of 12 {@link ProtectedItemWrapper} objects in canonical ACI order
     */
    public static ProtectedItemWrapper[] createProtectedItemWrappers()
    {
        ProtectedItemWrapper[] protectedItemWrappers = new ProtectedItemWrapper[]
            {
                // entry
                new ProtectedItemWrapper( EntryItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // allUserAttributeTypes
                new ProtectedItemWrapper( AllUserAttributeTypesItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // attributeType { 1.2.3, cn }
                new ProtectedItemWrapper( AttributeTypeItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new AttributeTypeValueEditor() ),

                // allAttributeValues { 1.2.3, cn }
                new ProtectedItemWrapper( AllAttributeValuesItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new AttributeTypeValueEditor() ),

                // attributeType
                new ProtectedItemWrapper( AllUserAttributeTypesAndValuesItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    null ),

                // attributeValue { ou=people, cn=Ersin }
                new ProtectedItemWrapper( AttributeValueItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new AttributeTypeAndValueValueEditor() ),

                // selfValue { 1.2.3, cn }
                new ProtectedItemWrapper( SelfValueItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new AttributeTypeValueEditor() ),

                // rangeOfValues (cn=E*)
                new ProtectedItemWrapper( RangeOfValuesItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new FilterValueEditor() ),

                // maxValueCount { { type 10.11.12, maxCount 10 }, { maxCount 20, type 11.12.13  } }
                new ProtectedItemWrapper( MaxValueCountItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new MaxValueCountValueEditor() ),

                // maxImmSub 3
                new ProtectedItemWrapper( MaxImmSubItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new IntegerValueEditor() ),

                // restrictedBy { { type 10.11.12, valuesIn ou }, { valuesIn cn, type 11.12.13  } }
                new ProtectedItemWrapper( RestrictedByItem.class, true, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new RestrictedByValueEditor() ),

                // classes and : { item: xyz , or:{item:X,item:Y}   }
                new ProtectedItemWrapper( ClassesItem.class, false, "", //$NON-NLS-1$
                    "", //$NON-NLS-1$
                    new TextValueEditor() // TODO: RefinementValueEditor
                ),

        };

        return protectedItemWrappers;
    }

}
