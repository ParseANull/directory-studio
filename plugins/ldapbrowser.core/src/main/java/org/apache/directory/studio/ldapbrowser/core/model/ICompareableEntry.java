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

package org.apache.directory.studio.ldapbrowser.core.model;


// ── CLASS: ICompareableEntry — OBI-WAN SENSING A FAMILIAR FORCE SIGNATURE ────
// Obi-Wan can look at two Force-sensitive beings and recognise whether they
// share the same aura — "Is this really Anakin, or a dark-side echo of him?"
// The comparison ability is a distinct skill that only certain Jedi carry.
// This interface is a tagging marker: by implementing it, an entry says "I can
// be used in LDAP COMPARE operations."  Sorting and compare code can then
// check {@code instanceof ICompareableEntry} to know this is an entry they
// can safely order or compare.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Tagging interface that marks an {@link IEntry} as being usable in LDAP COMPARE
 * operations and in sorted collections.
 * Any entry type that implements this interface signals to sorting and comparison
 * utilities that it is safe to apply ordering logic to it.
 * Think of this as Obi-Wan's ability to sense Force-signatures: only certain
 * objects carry the marker that allows comparison.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ICompareableEntry extends IEntry
{
}
