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
package org.apache.directory.studio.templateeditor.model.widgets;


// ── CLASS: TemplateForm — C-3PO'S TOP-LEVEL IMPERIAL COMMUNIQUÉ ──────────────────
// When C-3PO receives the complete Imperial communiqué — the full, top-level
// message that describes an entire LDAP entry editing form — he starts here. The
// form is the root of the widget tree: everything else (sections, composites,
// checkboxes, text fields) is nested beneath it. It has no parent of its own;
// it's the whole message, not a sub-directive.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Root of the template widget tree. A {@code TemplateForm} is the outermost container
 * parsed from a template XML file; all sections, composites, and leaf widgets are
 * nested inside it. Because it is the root, it has no parent (passes {@code null}
 * to {@link AbstractTemplateWidget}).
 *
 * <p>Think of this as the top-level Imperial communiqué — the envelope that
 * C-3PO opens first, before reading each nested directive inside:</p>
 * <pre>
 *   TemplateForm form = new TemplateForm();
 *   // sections, composites, and leaf widgets are added during XML parsing
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateForm extends AbstractTemplateWidget
{
    // ── CONSTRUCTOR: OPEN THE TOP-LEVEL COMMUNIQUÉ ───────────────────────────────
    // C-3PO opens the outermost Imperial communiqué. There is no enclosing message,
    // so we pass null as the parent — the form is the root of the whole widget tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the root form widget. Passes {@code null} to the superclass because
     * the form has no parent — it is the top of the widget tree.
     */
    public TemplateForm()
    {
        // A template form has no parent
        super( null );
    }
}
