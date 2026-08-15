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
package org.apache.directory.studio.schemaeditor.view.wrappers.difference;


// ── CLASS: WrapperState — Palpatine Issuing Order 66 ─────────────────────────
// In the Chancellor's chambers, Palpatine touches the holographic comm and issues
// a single coded directive — Order 66 — that instantly assigns every clone a new
// operational state: execute, eliminate, change course.
// WrapperState works the same way: it is the coded directive stamped on every
// difference wrapper to tell the UI exactly what happened to that schema element —
// was it untouched, newly added, changed, or removed?
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Describes the change status of a schema element between two schema versions.
 * When we compare an original schema against a modified one, every element gets
 * one of these four labels so the diff viewer knows how to display it —
 * unchanged rows look normal, added rows are highlighted green, modified rows
 * amber, and removed rows red.
 * Think of each constant as one of Palpatine's orders: a single word that
 * immediately tells the clones — and our UI — exactly what to do with this element.
 */
public enum WrapperState
{
    /** The element exists in both versions and has not changed at all. */
    IDENTICAL,

    /** The element is present in the modified version but not in the original — it was created. */
    ADDED,

    /** The element exists in both versions but its definition has changed. Note: the original enum
     *  value has a typo ("MODFIED") which we preserve here to avoid breaking serialised data. */
    MODFIED,

    /** The element was present in the original version but has been removed in the modified one. */
    REMOVED
}
