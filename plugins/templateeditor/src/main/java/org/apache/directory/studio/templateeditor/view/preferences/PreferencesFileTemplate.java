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
package org.apache.directory.studio.templateeditor.view.preferences;


import org.apache.directory.studio.templateeditor.model.AbstractTemplate;


// ── CLASS: PreferencesFileTemplate — PALPATINE'S PENDING-ORDER ON THE WORKBENCH ──
// When Palpatine drafts a new standing order that hasn't yet been committed to the
// Empire's permanent records, he keeps it on his workbench under a temporary file
// path. This class represents a template that the user has picked in the Import
// wizard but hasn't yet been written back to the plugin's real template folder.
// It's essentially a staging area template — same structure as a FileTemplate, but
// identified by an absolute path string rather than a java.io.File object.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A {@link AbstractTemplate} subclass used exclusively within the preferences page
 * to represent a template file that the user selected for import. The template is
 * held here as a staged candidate until the user clicks "OK", at which point
 * {@link PreferencesTemplatesManager#saveModifications()} writes it into the plugin's
 * real template store.
 *
 * <p>Think of this as Palpatine's pending standing order — drafted but not yet
 * ratified:</p>
 * <pre>
 *   PreferencesFileTemplate staged = new PreferencesFileTemplate();
 *   staged.setFilePath( "/home/user/my-template.xml" );
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreferencesFileTemplate extends AbstractTemplate
{
    /** The absolute path to the file */
    private String filePath;


    // ── GET FILE PATH: RETRIEVE THE STAGING PATH ──────────────────────────────────
    // Palpatine reads the temporary file path from the workbench — the absolute
    // location of the template XML file selected by the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute filesystem path to the template XML file selected by
     * the user during import.
     *
     * @return the absolute file path string; may be {@code null} if not yet set
     */
    public String getFilePath()
    {
        return filePath;
    }


    // ── SET FILE PATH: RECORD THE STAGING PATH ────────────────────────────────────
    // Palpatine stamps the absolute path onto the pending order so the import
    // machinery knows where to read the template XML from.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the absolute filesystem path to the template XML file.
     *
     * @param filePath  the absolute path string to the template file on disk
     */
    public void setFilePath( String filePath )
    {
        this.filePath = filePath;
    }
}
