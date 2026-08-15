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
package org.apache.directory.studio.openldap.config.acl;


// ── CLASS: OpenLdapAclEditorPluginConstants — IMPERIAL SECURITY BUREAU CODEBOOK ──
// Palpatine's ISB maintains a strict codebook of identifiers so that every
// facility, every icon, every directive references the same canonical key.
// No strings floating around loose — every ID is registered here so the whole
// Empire speaks the same language when referencing the ACL editor plugin.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralised home for every constant the OpenLDAP ACL Editor plugin uses:
 * plugin IDs, template IDs, icon paths, and dialog-settings keys. Having them
 * all in one place means we update one spot and the whole plugin picks it up.
 * Think of this class as the Imperial Security Bureau codebook — every facility
 * and directive gets a registered, canonical identifier so nothing gets lost
 * in translation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class OpenLdapAclEditorPluginConstants
{
    // ── Preventing Construction — No Instances Needed ────────────────────────
    // The ISB codebook is not a thing you instantiate, it is a reference you
    // consult. So we block the default constructor to keep this class purely
    // static and document that decision explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — no instances of a constants class, ever. We make
     * it explicit so a future reader knows this was deliberate.
     */
    private OpenLdapAclEditorPluginConstants()
    {
    }

    /** The plug-in ID */
    public static final String PLUGIN_ID = OpenLdapAclEditorPluginConstants.class.getPackage().getName();

    /** The ID for OpenLDAP ACL Template */
    public static String TEMPLATE_ID = OpenLdapAclEditorPlugin.getDefault().getPluginProperties()
        .getString( "CtxType_Template_id" ); //$NON-NLS-1$

    public static String IMG_ADD = "resources/icons/add.gif"; //$NON-NLS-1$
    public static String IMG_DELETE = "resources/icons/delete.gif"; //$NON-NLS-1$
    public static String IMG_DOWN = "resources/icons/down.png"; //$NON-NLS-1$
    public static String IMG_EDITOR = "resources/icons/editor.gif"; //$NON-NLS-1$
    public static String IMG_KEYWORD = "resources/icons/keyword.gif"; //$NON-NLS-1$
    public static String IMG_UP = "resources/icons/up.png"; //$NON-NLS-1$

    public static String DIALOGSETTING_KEY_ATTRIBUTES_HISTORY = "attributesHistory"; //$NON-NLS-1$
}
