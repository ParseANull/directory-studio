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

package org.apache.directory.studio.view;


// ── CLASS: ImageKeys — The Rebel Briefing Room's Holographic Map Paths ────────
// In the Yavin briefing room, Mon Mothma's team projects holographic images of
// the Death Star trench, the exhaust port, and the attack run — each image
// referenced by a precise coordinate path in the holographic catalogue.
// We do the same: every toolbar and menu icon in the rcp plugin is referenced
// by the string path constants defined here rather than hard-coded inline.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Defines the resource-relative paths for every icon used in the rcp plugin.
 * Keeping paths here instead of scattered through action classes means we only
 * fix a renamed icon file in one place.
 * Think of this as the holographic image catalogue for the Rebel briefing room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImageKeys
{
    // Images for Actions
    public static final String ABOUT = "resources/icons/about.png"; //$NON-NLS-1$
    public static final String INTRO = "resources/icons/intro.gif"; //$NON-NLS-1$
    public static final String MANAGE_CONFIGURATION = "resources/icons/manage-configuration.png"; //$NON-NLS-1$
    public static final String REPORT_BUG = "resources/icons/bug-report.png"; //$NON-NLS-1$
    public static final String SEARCH_UPDATES = "resources/icons/search-updates.png"; //$NON-NLS-1$
    public static final String SHOW_PREFERENCES = "resources/icons/preferences.png"; //$NON-NLS-1$
}
