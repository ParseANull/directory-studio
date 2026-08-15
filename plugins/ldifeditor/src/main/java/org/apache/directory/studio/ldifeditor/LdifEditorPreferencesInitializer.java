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

package org.apache.directory.studio.ldifeditor;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


// ── CLASS: LdifEditorPreferencesInitializer — REBEL BASE CONFIGURATION DEFAULTS
// Before the fleet deploys the Alliance quartermaster sets every console
// to factory defaults: folding enabled, auto-wrap on, colours calibrated
// to the standard war-room palette.
// This initializer does the same thing for the LDIF editor: it writes the
// out-of-the-box default values to the Eclipse preference store so the editor
// is usable the moment a user installs the plugin.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractPreferenceInitializer} that writes factory-default values
 * for every LDIF editor preference into the plugin's {@link IPreferenceStore}.
 * Eclipse calls this once when the store is first accessed with no persisted
 * values present.
 * Think of this as the Rebel quartermaster calibrating every console before
 * the fleet ships out.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorPreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── WRITE ALL DEFAULT PREFERENCES ─────────────────────────────────────────
    // The quartermaster walks every console in order — folding, double-click,
    // content assist, execute options, then syntax colours — and sets the dials.
    // We call store.setDefault() for each preference key defined in
    // LdifEditorConstants.
    /**
     * {@inheritDoc}
     *
     * <p>Writes factory-default values for all LDIF editor preferences including
     * folding, double-click behaviour, content-assist, execute options, and all
     * syntax colouring preferences.</p>
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = LdifEditorActivator.getDefault().getPreferenceStore();

        // LDIF Editor
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FORMATTER_AUTOWRAP, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS, false );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_DOUBLECLICK_USELDIFDOUBLECLICK, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_INSERTSINGLEPROPOSALAUTO, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_ENABLEAUTOACTIVATION, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_AUTOACTIVATIONDELAY, 200 );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_CONTENTASSIST_SMARTINSERTATTRIBUTEINMODSPEC, true );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_UPDATEIFENTRYEXISTS, false );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_OPTIONS_CONTINUEONERROR, true );

        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.COMMENT_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_COMMENT
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.NORMAL );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_KEYWORD
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.KEYWORD_1_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_KEYWORD
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_DN
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.DEFAULT_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_DN
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_ATTRIBUTE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.ATTRIBUTE_TYPE_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_ATTRIBUTE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUETYPE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.SEPARATOR_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUETYPE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.VALUE_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_VALUE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.NORMAL );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEADD
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.ADD_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEADD
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODIFY
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.MODIFY_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODIFY
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEDELETE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.DELETE_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEDELETE
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
        setDefaultColor( store, LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODDN
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_RGB_SUFFIX,
            CommonUIConstants.RENAME_COLOR );
        store.setDefault( LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_CHANGETYPEMODDN
            + LdifEditorConstants.PREFERENCE_LDIFEDITOR_SYNTAX_STYLE_SUFFIX, SWT.BOLD );
    }


    // ── RESOLVE AND STORE A DEFAULT COLOUR ────────────────────────────────────
    // The technician looks up the named colour in the common palette and
    // converts it to the RGB triple the preference store needs.
    /**
     * Looks up a named colour from the common UI plugin and stores its RGB
     * triple as the default value for {@code preferenceName}.
     * If the colour is not found the store retains its built-in string default.
     *
     * @param store           the preference store to update
     * @param preferenceName  the preference key to set
     * @param colorName       the common-UI colour name to resolve
     */
    private void setDefaultColor( IPreferenceStore store, String preferenceName, String colorName )
    {
        Color color = CommonUIPlugin.getDefault().getColor( colorName );
        if ( color == null )
        {
            store.setDefault( preferenceName, IPreferenceStore.STRING_DEFAULT_DEFAULT );
        }
        else
        {
            PreferenceConverter.setDefault( store, preferenceName, color.getRGB() );
        }
    }

}
