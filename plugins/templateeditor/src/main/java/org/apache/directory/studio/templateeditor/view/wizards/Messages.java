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
package org.apache.directory.studio.templateeditor.view.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — EMPEROR'S STEP-BY-STEP PLAN TRANSLATION DESK ──────────────
// The Emperor's plan to bring Luke to the dark side is laid out in a series of
// carefully worded directives — each step precisely phrased to nudge Luke forward.
// This class is the translation desk that looks up each directive by its key from
// the wizards message bundle, so the wizard UI can display localised text at every
// step without hard-coding a single string in the source.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localised strings from the wizards message bundle
 * ({@code org.apache.directory.studio.templateeditor.view.wizards.messages}).
 * Returns {@code !key!} when a key is missing rather than throwing an exception,
 * keeping the UI partially functional even with an incomplete bundle.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.view.wizards.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── PRIVATE CONSTRUCTOR: PREVENT INSTANTIATION ────────────────────────────────
    // The Emperor doesn't allow his translation desk to be copied — it is a pure
    // utility with no instance state. The private constructor enforces that.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a static utility and must not be instantiated.
     */
    private Messages()
    {
    }


    // ── GET STRING: LOOK UP A MESSAGE BY KEY ──────────────────────────────────────
    // The Emperor's clerk looks up the directive by key. If the directive is missing
    // from the bundle, the clerk returns the key wrapped in exclamation marks as a
    // visible placeholder rather than crashing the plan.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given key from the wizards message bundle.
     * If the key is not found, returns {@code !key!} so missing strings are immediately
     * visible in the UI without causing a runtime exception.
     *
     * @param key  the message bundle key to look up
     * @return the localised string, or {@code !key!} if the key is missing
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
