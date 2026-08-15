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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;


// ── CLASS: AttributeInfo — R2-D2'S ATTRIBUTE PANEL FOR ONE ENTRY ─────────────
// R2-D2 keeps a small panel for each entry he's plugged into: a flag saying
// "have I pulled all the attributes yet?" and a map keyed by OID string holding
// every attribute value.  This panel lives in BrowserConnection so that entry
// objects themselves stay tiny in memory — R2-D2 carries the heavy parts.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the loaded attribute state for a single {@link AbstractEntry}.
 * Stored in {@link BrowserConnection}'s attribute info map rather than
 * directly on the entry to minimise per-entry memory usage.
 *
 * <p>Think of this as R2-D2's attribute panel for one entry — an initialized
 * flag plus a map from OID string to {@link IAttribute}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeInfo implements Serializable
{

    private static final long serialVersionUID = -298229262461058833L;

    /** The attributes initialized flag. */
    protected volatile boolean attributesInitialized = false;

    /** The attribute map. */
    protected volatile Map<String, IAttribute> attributeMap = new LinkedHashMap<String, IAttribute>();


    // ── R2-D2 Creates A Fresh Empty Attribute Panel ───────────────────────────────
    /**
     * Creates a new instance of AttributeInfo.
     * The attributes-initialized flag starts {@code false} and the attribute
     * map starts empty.
     */
    public AttributeInfo()
    {
    }

}
