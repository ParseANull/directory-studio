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
package org.apache.directory.studio.openldap.config.acl.widgets.composites;


import org.apache.directory.studio.openldap.config.acl.model.AclWhoClause;


// ── CLASS: WhoClauseComposite — WHO CLAUSE DIRECTIVE FORM CONTRACT ────────────
// Grand Moff Tarkin issues a specialised contract for all who-clause directive
// forms. It extends the base ClauseComposite contract but carries the generic
// who-clause type parameter so the type system can distinguish who-clause composites
// from what-clause composites. Currently the interface adds no extra methods —
// the type parameter alone is its distinguishing feature.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface for who-clause composites. Extends {@link ClauseComposite}
 * with a who-clause type parameter so the widget system can distinguish
 * who-clause panels from what-clause panels at compile time.
 *
 * <p>Think of this interface as Grand Moff Tarkin's specialised who-clause
 * directive form contract — the type parameter alone marks a composite as
 * belonging to the "who" side of an ACL rule.</p>
 *
 * @param <C>  The concrete who-clause type (must extend {@link AclWhoClause}).
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface WhoClauseComposite<C extends AclWhoClause> extends ClauseComposite
{
}
