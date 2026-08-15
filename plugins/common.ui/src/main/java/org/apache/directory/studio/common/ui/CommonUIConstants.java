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

package org.apache.directory.studio.common.ui;


// ── CLASS: CommonUIConstants — REBEL ALLIANCE EQUIPMENT MANIFEST ─────────────
// Every Rebel base keeps a manifest listing all equipment identifiers: ship
// registry codes, supply crate labels, and comm channel keys.  This class is
// our version of that manifest for the common.ui plugin — a sealed catalog of
// constant strings used throughout the codebase.  Nobody can instantiate it;
// you just reference the constants directly.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We define all constant values used across the common.ui plugin in one place.
 * This includes the plugin ID, image paths, color preference keys, and the
 * color-scheme preference key.  The class is final and has a private constructor
 * so it can never be instantiated or subclassed — constants only.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class CommonUIConstants
{
    // ── CONSTRUCTOR CommonUIConstants — SEALING THE MANIFEST VAULT ───────────
    // The manifest vault is sealed: nobody can create an instance of this class.
    // This is a standard utility-class pattern — all members are static, so
    // there is never a reason to construct one of these objects.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We keep this constructor private to prevent anyone from instantiating
     * this constants-only class.  All fields are static; just reference them
     * directly via {@code CommonUIConstants.SOME_CONSTANT}.
     */
    private CommonUIConstants()
    {
    }

    /** The plug-in ID */
    public static final String PLUGIN_ID = CommonUIConstants.class.getPackage().getName();

    /** The pull-down image */
    public static final String IMG_PULLDOWN = "resources/icons/pulldown.gif"; //$NON-NLS-1$

    /*
     * Names of semantic colors. Actual color values are theme specific and defined in default.css and dark.css.
     */
    public static final String DEFAULT_COLOR = "defaultColor";
    public static final String DISABLED_COLOR = "disabledColor";
    public static final String ERROR_COLOR = "errorColor";
    public static final String COMMENT_COLOR = "commentColor";
    public static final String KEYWORD_1_COLOR = "keyword1Color";
    public static final String KEYWORD_2_COLOR = "keyword2Color";
    public static final String OBJECT_CLASS_COLOR = "objectClassColor";
    public static final String ATTRIBUTE_TYPE_COLOR = "attributeTypeColor";
    public static final String VALUE_COLOR = "valueColor";
    public static final String OID_COLOR = "oidColor";
    public static final String SEPARATOR_COLOR = "separatorColor";
    public static final String ADD_COLOR = "addColor";
    public static final String DELETE_COLOR = "deleteColor";
    public static final String MODIFY_COLOR = "modifyColor";
    public static final String RENAME_COLOR = "renameColor";

    public static final String IMG_INFORMATION = "resources/icons/information.gif"; //$NON-NLS-1$

    /** Preference key that remembers the last applied Base16 color scheme id. */
    public static final String COLOR_SCHEME_ID = "colorSchemeId";

}
