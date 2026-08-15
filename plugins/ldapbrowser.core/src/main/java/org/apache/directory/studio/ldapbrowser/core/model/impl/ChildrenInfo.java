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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import java.io.Serializable;
import java.util.Set;

import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: ChildrenInfo — LANDO'S WING MANIFEST FOR ONE CLOUD CITY SECTOR ───
// Lando keeps a manifest for each sector of Cloud City: has it been surveyed?
// Who currently lives there?  Are there more residents beyond what's been
// loaded?  Which patrol droids are waiting to fetch the first and next pages?
// ChildrenInfo is that manifest — a compact bundle of child state stored off
// the entry itself in BrowserConnection's children map so entries stay lean.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the loaded children state for a single {@link AbstractEntry}.
 * Stored in {@link BrowserConnection}'s children info map rather than directly
 * on the entry to keep per-entry memory usage low.
 *
 * <p>Think of this as Lando's wing manifest for one Cloud City sector —
 * initialized flag, resident set, a "more residents" flag, and two patrol
 * runnables for paged loading.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ChildrenInfo implements Serializable
{

    private static final long serialVersionUID = -4642987611142312896L;

    /** The children initialized flag. */
    protected volatile boolean childrenInitialized = false;

    /** The children set. */
    protected volatile Set<IEntry> childrenSet = null;

    /** The has more children flag. */
    protected volatile boolean hasMoreChildren = false;

    /** The runnable used to fetch the top page of children. */
    protected StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable;

    /** The runnable used to fetch the next page of children. */
    protected StudioConnectionBulkRunnableWithProgress nextPageChildrenRunnable;


    // ── Lando Opens A Fresh Empty Wing Manifest ───────────────────────────────────
    /**
     * Creates a new instance of ChildrenInfo.
     * All flags start {@code false}, the children set starts {@code null},
     * and both paging runnables start {@code null}.
     */
    public ChildrenInfo()
    {
    }

}
