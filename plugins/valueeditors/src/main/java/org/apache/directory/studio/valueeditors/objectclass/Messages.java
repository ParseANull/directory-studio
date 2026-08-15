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
package org.apache.directory.studio.valueeditors.objectclass;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — YODA'S JEDI ARCHIVES PHRASE LIBRARY ────────────────────
// Deep in the Jedi Temple archives on Coruscant, Yoda's keeper of texts holds
// every classification label — "(structural)", "(abstract)", "(auxiliary)",
// "(obsolete)" — indexed by code so any Council member can retrieve the exact
// wording for any classification without ambiguity.
// This class is that phrase library for the objectclass sub-package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the objectclass value-editor sub-package
 * from its {@code messages.properties} resource bundle.
 * Every label appended to an object class name in the display (e.g. "person
 * (structural)") comes through here so the text is translatable.
 * Think of this class as Yoda's Jedi archives phrase library — every classification
 * label is indexed and retrievable on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── The Archive Keeper Looks Up a Classification Label ────────────────────
    // A Council member asks the keeper for the label text for "(structural)".
    // The keeper checks the archive index and returns the localised text.
    // If the code isn't indexed, the keeper stamps "!key!" so nobody is left blank.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the objectclass sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — the archive keeper retrieves a classification label:</p>
     * <pre>
     *   String label = Messages.getString("ObjectClassValueEditor.Structural");
     *   // → " (structural)"
     * </pre>
     *
     * @param key  The property key as defined in messages.properties.
     * @return     The localised string, or {@code !key!} if the key is not found.
     */
    public static String getString( String key )
    {
        try
        {
            return RESOURCE_BUNDLE.getString( key );
        }
        catch ( MissingResourceException e )
        {
            return '!' + key + '!';
        }
    }
}
