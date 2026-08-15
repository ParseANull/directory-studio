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


// ── CLASS: ValueEditorsConstants — THE REBEL ALLIANCE EQUIPMENT MANIFEST ─────
// Before every mission, the Rebel quartermaster hands out a laminated parts list:
// every tool, every gadget, every bay number is on that sheet so the crew never
// argues about where the ion cannon is stored.  Every plugin in the codebase can
// look up image paths and setting keys from this single source of truth.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds shared constants — image resource paths, plugin ID, and dialog-settings
 * keys — for the entire valueeditors plugin.
 * Rather than scattering magic strings across a dozen files, we centralise them
 * here so a single rename fixes everything.  Think of this as the Rebel Alliance's
 * equipment manifest: every item has a canonical label, and the crew just looks it up.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ValueEditorsConstants
{
    // ── Private Constructor: Sealing the Manifest ────────────────────────────
    // The Rebel quartermaster never lets anyone edit the master parts list
    // directly — it's printed and sealed under glass.
    // We enforce the same rule by making this class non-instantiable; it's
    // purely a namespace for constants, not an object to be created.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — prevents anyone from creating an instance of this
     * constants-only class.
     * All members are {@code static final}; there is never a reason to instantiate it.
     */
    private ValueEditorsConstants()
    {
    }

    /** The plug-in ID */
    public static final String PLUGIN_ID = ValueEditorsConstants.class.getPackage().getName();

    /** The relative path to the image editor icon */
    public static final String IMG_IMAGEEDITOR = "resources/icons/imageeditor.gif"; //$NON-NLS-1$

    /** The relative path to the address editor icon */
    public static final String IMG_ADDRESSEDITOR = "resources/icons/addresseditor.gif"; //$NON-NLS-1$

    /** The relative path to the Dn editor icon */
    public static final String IMG_DNEDITOR = "resources/icons/dneditor.gif"; //$NON-NLS-1$

    /** The relative path to the password editor icon */
    public static final String IMG_PASSWORDEDITOR = "resources/icons/passwordeditor.gif"; //$NON-NLS-1$

    /** The relative path to the generalized time editor icon */
    public static final String IMG_GENERALIZEDTIMEEDITOR = "resources/icons/generalizedtimeeditor.gif"; //$NON-NLS-1$

    /** The relative path to the object class editor icon */
    public static final String IMG_OCDEDITOR = "resources/icons/objectclasseditor.png"; //$NON-NLS-1$

    /** The relative path to the integer editor icon */
    public static final String IMG_INTEGEREDITOR = "resources/icons/integereditor.gif"; //$NON-NLS-1$

    /** The relative path to the administrative role editor icon */
    public static final String IMG_ADMINISTRATIVEROLEEDITOR = "resources/icons/administrativeroleeditor.gif"; //$NON-NLS-1$

    /** The relative path to the certificate editor icon */
    public static final String IMG_CERTIFICATEEDITOR = "resources/icons/certificateeditor.png"; //$NON-NLS-1$

    /** The relative path to the text field error icon */
    public static final String IMG_TEXTFIELD_ERROR = "resources/icons/textfield_error.png"; //$NON-NLS-1$

    /** The relative path to the text field ok icon */
    public static final String IMG_TEXTFIELD_OK = "resources/icons/textfield_ok.png"; //$NON-NLS-1$

    /** The dialogs settings for the Date Editor "Discard fraction" checkbox */
    public static final String DIALOGSETTING_KEY_DATE_EDITOR_DISCARD_FRACTION = "dateEditorDiscardFraction"; //$NON-NLS-1$
}
