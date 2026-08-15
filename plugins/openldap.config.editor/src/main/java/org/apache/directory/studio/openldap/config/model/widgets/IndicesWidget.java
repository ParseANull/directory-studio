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
package org.apache.directory.studio.openldap.config.model.widgets;



import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.editor.wrappers.DbIndexDecorator;
import org.apache.directory.studio.openldap.config.editor.wrappers.DbIndexWrapper;


// ── CLASS: IndicesWidget — Lando Running Cloud City's Trade Index Tables ──────
// Lando keeps an index of every trade deal in Cloud City — sorted, searchable,
// queryable at a moment's notice. He can add new index entries, edit existing ones,
// or delete stale ones through a clean table UI.
// IndicesWidget is the SWT table control that the editor uses to manage LDAP
// database index entries (OlcDbIndex). It wraps the generic TableWidget with the
// DbIndexDecorator so users can add/edit/remove index definitions visually.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT table widget for managing LDAP database index definitions (OlcDbIndex).
 * Provides an Add/Edit/Delete table UI for the indices configured on a BDB or MDB database.
 * Think of this as Lando's index management desk in Cloud City — all the trade deals,
 * neatly organized and editable on demand.
 *
 * <pre>
 * Attributes
 * +----------------------------+
 * | Index 1                    | (Add...)
 * | Index 2                    | (Edit...)
 * |                            | (Delete)
 * +----------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class IndicesWidget extends TableWidget<DbIndexWrapper>
{
    // ── Constructor — Lando Assigns a Decorator for the Index Table ───────────────
    // Lando pairs his index table with the right decorator (DbIndexDecorator) that
    // knows how to render and edit each DbIndexWrapper row in the table.
    // We pass the browser connection so the decorator can resolve attribute types from
    // the live schema when the user is editing an index definition.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new IndicesWidget bound to the given LDAP browser connection.
     * The browser connection is used by the DbIndexDecorator to provide schema-aware
     * attribute type suggestions when the user adds or edits an index.
     *
     * <p>For example — Lando assigns a decorator for the index table:</p>
     * <pre>
     *   IndicesWidget widget = new IndicesWidget( browserConnection );
     *   widget.createWidget( parent );
     * </pre>
     *
     * @param browserConnection  the IBrowserConnection providing schema context for index editing
     */
    public IndicesWidget( IBrowserConnection browserConnection )
    {
        super( new DbIndexDecorator( null, browserConnection ) );
    }
}
