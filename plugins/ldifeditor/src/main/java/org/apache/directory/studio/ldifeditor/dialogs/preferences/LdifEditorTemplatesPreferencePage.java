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

package org.apache.directory.studio.ldifeditor.dialogs.preferences;


import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;


// ── CLASS: LdifEditorTemplatesPreferencePage — REBEL SNIPPET BINDER ──────────
// R2-D2 carries a binder of standard LDIF snippets (add-record, modify-record,
// etc.) that any Rebel operator can pull up with a keystroke.
// This preference page surfaces Eclipse's standard template management UI
// pre-wired to the LDIF editor's template store and context-type registry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link TemplatePreferencePage} for managing LDIF completion templates.
 * Pre-wired to the LDIF editor's {@link org.eclipse.jface.text.templates.persistence.TemplateStore}
 * and {@link org.eclipse.jface.text.templates.ContextTypeRegistry}, both owned
 * by {@link LdifEditorActivator}.
 * Think of this as R2-D2's snippet binder: create, edit, and delete LDIF
 * completion templates through the standard Eclipse template UI.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorTemplatesPreferencePage extends TemplatePreferencePage
{

    // ── CONSTRUCT AND WIRE TO THE LDIF TEMPLATE STORE ─────────────────────────
    // R2-D2 opens the binder and registers which section headings apply.
    /**
     * Creates a new templates preference page wired to the LDIF editor's
     * preference store, template store, and context-type registry.
     */
    public LdifEditorTemplatesPreferencePage()
    {
        super();
        super.setPreferenceStore( LdifEditorActivator.getDefault().getPreferenceStore() );
        super.setTemplateStore( LdifEditorActivator.getDefault().getLdifTemplateStore() );
        super.setContextTypeRegistry( LdifEditorActivator.getDefault().getLdifTemplateContextTypeRegistry() );
    }

}
