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
package org.apache.directory.studio.openldap.config.editor.overlays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

// ── CLASS: ModuleWrapperViewerSorter — Lando Running Cloud City ───────────────
// Lando Calrissian keeps Cloud City running through meticulous prioritization:
// every department is ranked by its contribution to the station's function —
// mining operations come first, atmosphere processing next, then hospitality.
// Nothing is left to chance; the whole city runs in a carefully maintained
// sequence.  ModuleWrapperViewerSorter applies the same discipline to the
// Overlays page module list, sorting wrappers by list name, then list index,
// then individual load order — exactly the sequence OpenLDAP expects.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace ViewerComparator that sorts ModuleWrapper rows in the Overlays page
 * module table by moduleList name, then moduleList index, then load order.
 * Without a sorter the table would display modules in arbitrary insertion order,
 * which could mislead users about the actual load sequence on the server.
 * Think of it as Lando keeping Cloud City's departments in strict priority
 * order so nothing collides and everything runs smoothly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModuleWrapperViewerSorter extends ViewerComparator
{
    // ── compare — Lando Ranks Two Departments ────────────────────────────────
    // Lando sits in his administrator's chair and compares the work orders for
    // two city departments: he checks their parent division name first, then
    // their division number, then their individual priority rating.
    // Only after working through all three levels does he decide which comes
    // before the other in the schedule.  We do exactly the same three-level
    // comparison for ModuleWrapper objects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two ModuleWrapper objects and returns a negative, zero, or
     * positive integer depending on whether e1 sorts before, equal to, or
     * after e2.
     * We use a three-tier key: (1) moduleList name alphabetically, (2) list
     * index numerically, (3) load order numerically.  This mirrors the order
     * in which OpenLDAP processes the modules so the table reflects reality.
     * For non-ModuleWrapper elements we fall back to the parent comparator.
     *
     * <p>For example — Lando ranks two city departments:</p>
     * <pre>
     *   module1 = ("cn=module{0}", index=0, order=1)
     *   module2 = ("cn=module{0}", index=0, order=2)
     *   compare(viewer, module1, module2) → -1  (module1 loads first)
     * </pre>
     *
     * @param viewer  the JFace viewer requesting the comparison — we only use it for the fallback
     * @param e1      the first element to compare, expected to be a ModuleWrapper
     * @param e2      the second element to compare, expected to be a ModuleWrapper
     * @return        negative if e1 comes first, positive if e2 comes first, 0 if equal
     */
    @Override
    public int compare( Viewer viewer, Object e1, Object e2 )
    {
        if ( ( e1 instanceof ModuleWrapper ) && ( e2 instanceof ModuleWrapper ) )
        {
            ModuleWrapper module1 = (ModuleWrapper)e1;
            ModuleWrapper module2 = (ModuleWrapper)e2;

            if ( e1 == e2 )
            {
                // Short circuit...
                return 0;
            }

            // First, compare the moduleList
            int comp = module1.getModuleListName().compareToIgnoreCase( module2.getModuleListName() );

            if ( comp == 0 )
            {
                // Same ModuleList. Check the index
                if ( module1.getModuleListIndex() == module2.getModuleListIndex() )
                {
                    // Same index : check the modules' order
                    if ( module1.getOrder() > module2.getOrder() )
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    // We can get out
                    if ( module1.getModuleListIndex() > module2.getModuleListIndex() )
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
            }
            else
            {
                // The are different, we can get out
                return comp;
            }
        }

        return super.compare( viewer, e1, e2 );
    }
}
