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
package org.apache.directory.studio.templateeditor.model;


import java.io.File;


// ── CLASS: FileTemplate — THE DEATH STAR BLUEPRINTS ON A DATA CARTRIDGE ──────────
// When Princess Leia slipped the Death Star blueprints into R2-D2, those plans
// existed as a physical file on a data cartridge. This class is that cartridge:
// a template whose XML definition lives on the filesystem (typically in the plugin's
// data folder). The only thing we add beyond AbstractTemplate is a reference to
// the {@link File} where the XML is stored so the template manager knows where to
// read and write it.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Template} whose definition is stored as an XML file on disk, typically
 * in the plugin's data folder. The {@link File} reference lets the template manager
 * persist changes and reload the template after a restart. Think of this as the
 * Death Star blueprints on a data cartridge.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FileTemplate extends AbstractTemplate
{
    /** The associated file */
    private File file;


    // ── GET FILE: RETRIEVE THE CARTRIDGE LOCATION ─────────────────────────────────
    /**
     * Returns the filesystem {@link File} where this template's XML definition is
     * stored.
     *
     * @return the file; may be {@code null} if not yet set
     */
    public File getFile()
    {
        return file;
    }


    // ── SET FILE: LOAD THE DATA CARTRIDGE ─────────────────────────────────────────
    /**
     * Sets the filesystem {@link File} for this template's XML definition.
     *
     * @param file  the file to associate with this template
     */
    public void setFile( File file )
    {
        this.file = file;
    }
}
