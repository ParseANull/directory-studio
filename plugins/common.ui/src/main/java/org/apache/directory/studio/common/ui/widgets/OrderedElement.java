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
package org.apache.directory.studio.common.ui.widgets;

// ── CLASS: OrderedElement — REBEL ALLIANCE MISSION PRIORITY PROTOCOL ─────────
// On the Rebel Alliance's mission board, every mission has a numbered priority
// tag so the pilots know which order to fly them.  Any element that implements
// this interface carries such a tag — a "prefix" that encodes its position in
// an ordered list.  The TableWidget calls these methods when the user clicks
// Up, Down, or reorders the list.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We define the contract for elements stored in an ordered TableWidget.
 * Implementors carry an integer prefix that represents their position in the
 * list.  The TableWidget calls our methods to reorder elements when the user
 * clicks the Up and Down buttons.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface OrderedElement
{
    // ── METHOD incrementPrefix — MOVING UP THE PRIORITY BOARD ────────────────
    // A pilot's mission is promoted on the board — we decrease the numeric
    // prefix (lower number = higher priority / earlier position) to move this
    // element toward the top of the list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We decrease this element's position prefix by one, effectively moving it
     * one slot toward the top (front) of the ordered list.  The TableWidget
     * calls this when the user clicks the Up button.
     */
    void incrementPrefix();


    // ── METHOD decrementPrefix — DEMOTING DOWN THE PRIORITY BOARD ────────────
    // A pilot's mission is bumped down the board — we increase the numeric
    // prefix to push this element toward the bottom of the list, making room
    // for higher-priority missions above it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We increase this element's position prefix by one, effectively moving it
     * one slot toward the bottom (end) of the ordered list.  The TableWidget
     * calls this when the user clicks the Down button.
     */
    void decrementPrefix();


    // ── METHOD setPrefix — ASSIGNING A SPECIFIC PRIORITY SLOT ────────────────
    // Mission control assigns this element a specific slot on the priority board.
    // We store the given value directly, overriding whatever prefix the element
    // had before.  Used during insertion and bulk reordering.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We set this element's position prefix to the given value directly.  This
     * is used when inserting a new element at a specific position or when
     * rebuilding the prefix sequence after a delete or reorder operation.
     *
     * @param prefix the new prefix value to assign
     */
    void setPrefix( int prefix );
}
