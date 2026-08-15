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
package org.apache.directory.studio.templateeditor;


// ── CLASS: EntryTemplatePluginConstants — THE IMPERIAL CODEBOOK ──────────────────
// Before every mission, the Empire issues a codebook: every officer uses the same
// code names so there is no ambiguity when issuing orders across the fleet.
// "Target: DS-1" means exactly one thing. This interface is the same idea — a
// single canonical source for every string constant the plugin uses. Image paths,
// preference key names, dialog IDs — all live here so nothing is ever hard-coded
// twice in two different classes with slightly different spellings.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Central repository of every string constant used by the Entry Template plugin.
 * Image resource paths ({@code IMG_*}), Eclipse preference store keys
 * ({@code PREF_*}), and dialog memory keys ({@code DIALOG_*}) all live here.
 * Think of this interface as the Imperial codebook: one document, one truth,
 * no ambiguity when passing keys across the plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EntryTemplatePluginConstants
{
    /** The plug-in ID — derived from the package name so it stays in sync */
    String PLUGIN_ID = EntryTemplatePluginConstants.class.getPackage().getName();

    // Images
    String IMG_EXPORT_TEMPLATES_WIZARD = "resources/icons/export_templates_wizard.gif"; //$NON-NLS-1$
    String IMG_FILE = "resources/icons/file.gif"; //$NON-NLS-1$
    String IMG_IMPORT_TEMPLATES_WIZARD = "resources/icons/import_templates_wizard.gif"; //$NON-NLS-1$
    String IMG_NO_IMAGE = "resources/icons/no_image.gif"; //$NON-NLS-1$
    String IMG_OBJECT_CLASS = "resources/icons/object_class.png"; //$NON-NLS-1$
    String IMG_SWITCH_TEMPLATE = "resources/icons/switch_template.gif"; //$NON-NLS-1$
    String IMG_TEMPLATE = "resources/icons/template.gif"; //$NON-NLS-1$
    String IMG_TEMPLATE_DISABLED = "resources/icons/template_disabled.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_ADD_VALUE = "resources/icons/toolbar_add_value.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_BROWSE_FILE = "resources/icons/toolbar_browse_file.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_BROWSE_IMAGE = "resources/icons/toolbar_browse_image.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_CLEAR = "resources/icons/toolbar_clear.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_DELETE_VALUE = "resources/icons/toolbar_delete_value.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_EDIT_PASSWORD = "resources/icons/toolbar_edit_password.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_EDIT_DATE = "resources/icons/toolbar_edit_date.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_EDIT_VALUE = "resources/icons/toolbar_edit_value.gif"; //$NON-NLS-1$
    String IMG_TOOLBAR_SAVE_AS = "resources/icons/toolbar_save_as.gif"; //$NON-NLS-1$

    // Preferences
    String PREF_TEMPLATE_ENTRY_EDITOR_PAGE_ID = "org.apache.directory.studio.templateeditor.view.preferences.TemplateEntryEditorPreferencePage"; //$NON-NLS-1$
    String PREF_TEMPLATES_PRESENTATION = PLUGIN_ID + ".prefs.TemplatesPresentation"; //$NON-NLS-1$
    int PREF_TEMPLATES_PRESENTATION_TEMPLATE = 1;
    int PREF_TEMPLATES_PRESENTATION_OBJECT_CLASS = 2;
    String PREF_DISABLED_TEMPLATES = PLUGIN_ID + ".prefs.DisabledTemplates"; //$NON-NLS-1$
    String PREF_USE_TEMPLATE_EDITOR_FOR = PLUGIN_ID + ".prefs.UseTemplateEditorFor"; //$NON-NLS-1$
    int PREF_USE_TEMPLATE_EDITOR_FOR_ANY_ENTRY = 1;
    int PREF_USE_TEMPLATE_EDITOR_FOR_ENTRIES_WITH_TEMPLATE = 2;
    String PREF_DEFAULT_TEMPLATES = PLUGIN_ID + ".prefs.DefaultTemplates"; //$NON-NLS-1$

    // Dialogs
    String DIALOG_IMPORT_TEMPLATES = PLUGIN_ID + ".dialog.ImportTemplates"; //$NON-NLS-1$
    String DIALOG_EXPORT_TEMPLATES = PLUGIN_ID + ".dialog.ExportTemplates"; //$NON-NLS-1$
}
