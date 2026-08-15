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

package org.apache.directory.studio.valueeditors;


// ── CLASS: InPlaceTextValueEditor — C-3PO ANNOTATES THE DIPLOMATIC SCROLL ────
// At the Rebel Alliance summit on Yavin IV, C-3PO stands beside a large
// diplomatic scroll pinned to the wall. When a delegate points at a line that
// needs a quick correction, C-3PO uncaps his stylus and edits the text right
// there on the scroll — no side table, no separate document — just a quick
// inline annotation and the scroll is done.
// We do the same: this editor lets users edit a plain-text LDAP attribute value
// directly inside the table cell, without opening any dialog. It inherits all
// the in-place string editing logic from {@link AbstractInPlaceStringValueEditor}
// and adds nothing extra — the base class covers all the standard string cases.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The default in-place editor for plain-text LDAP attribute values. Editing
 * happens directly in the table cell without opening a dialog — think of it as
 * C-3PO making a quick inline annotation on the diplomatic scroll, stylus in
 * hand, right where the delegate is pointing.
 * This class is intentionally empty: all the logic lives in
 * {@link AbstractInPlaceStringValueEditor}. We exist as a concrete, named type
 * so the extension registry can instantiate us for the "default string" case.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceTextValueEditor extends AbstractInPlaceStringValueEditor
{
}
