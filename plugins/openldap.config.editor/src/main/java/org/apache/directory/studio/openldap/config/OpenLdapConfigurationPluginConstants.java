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
package org.apache.directory.studio.openldap.config;

// ── CLASS: OpenLdapConfigurationPluginConstants — Palpatine's Imperial Archives
// Deep in the Imperial Palace, Palpatine maintains the archives: a centralized,
// structured registry of every identifier, code, and reference needed to run the
// Empire. Everything is catalogued here — ship classifications, protocol codes,
// insignia paths. Nothing in the Empire operates without consulting these records.
// This interface is our archives: every string constant the plugin needs — image
// paths, plugin IDs, wizard registration codes — lives here. Other classes import
// this interface and use these constants directly, keeping magic strings out of
// the logic code.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central registry of all named constants used throughout the OpenLDAP Configuration
 * Editor plugin. Keeping constants here rather than scattered across classes means
 * we have one place to update when paths or IDs change.
 * Think of this as Palpatine's Imperial archives — the authoritative, governed record
 * of every identifier and resource path in the plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface OpenLdapConfigurationPluginConstants
{
    /** The OSGi plug-in ID — same as the package name, used for logging and extension points. */
    String PLUGIN_ID = OpenLdapConfigurationPluginConstants.class.getPackage().getName();

    // ------
    // IMAGES
    // ------
    String IMG_ATTRIBUTE = "resources/icons/attribute.gif"; //$NON-NLS-1$
    String IMG_DATABASE = "resources/icons/database.gif"; //$NON-NLS-1$
    String IMG_DISABLED_DATABASE = "resources/icons/disabledDatabase.gif"; //$NON-NLS-1$
    String IMG_EDITOR = "resources/icons/editor.gif"; //$NON-NLS-1$
    String IMG_EXPORT = "resources/icons/export.gif"; //$NON-NLS-1$
    String IMG_INDEX = "resources/icons/index.png"; //$NON-NLS-1$
    String IMG_INFORMATION = "resources/icons/information.gif"; //$NON-NLS-1$
    String IMG_IMPORT = "resources/icons/import.gif"; //$NON-NLS-1$
    String IMG_OVERLAY = "resources/icons/overlay.gif"; //$NON-NLS-1$
    String IMG_LDAP_SERVER = "resources/icons/server.gif"; //$NON-NLS-1$

    public static final String WIZARD_NEW_OPENLDAP_CONFIG = OpenLdapConfigurationPlugin.getDefault().getPluginProperties()
        .getString( "NewWizards_NewOpenLdapConfigurationFileWizard_id" ); //$NON-NLS-1$
}
