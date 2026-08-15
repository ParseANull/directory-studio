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
package org.apache.directory.studio.common.ui;

import java.util.Comparator;

import org.eclipse.jface.viewers.LabelProvider;

// ── CLASS: TableDecorator — REBEL ALLIANCE WORKSHOP TOOLBOX ─────────────────
// In the Rebel workshop, every job requires the right combination of tools:
// a comparator to sort the parts into the right order, and a label maker to
// stamp a readable name on each one.  This abstract class bundles both roles
// into a single "toolbox" that the TableWidget can hand to its viewer without
// needing to juggle two separate objects.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We combine a {@link LabelProvider} and a {@link Comparator} into one object
 * so the TableWidget has a single reference that handles both display labeling
 * and element ordering.  Subclasses must implement {@link #compare} to define
 * the sort order; label logic is inherited from {@code LabelProvider} and can
 * be overridden as needed.  Each decorator also carries a reference to the
 * {@link AddEditDialog} used to create or modify elements of type {@code E}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @param <E> The element type being handled by this decorator
 */
public abstract class TableDecorator<E> extends LabelProvider implements Comparator<E>
{
    /** The Dialog instance */
    private AddEditDialog<E> dialog;

    // ── CONSTRUCTOR TableDecorator — OPENING THE WORKSHOP TOOLBOX ────────────
    // The workshop is open for business: we initialize the toolbox with no
    // dialog attached yet.  The caller is expected to set the dialog via
    // setDialog before the TableWidget starts using us.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new, empty decorator.  The associated {@link AddEditDialog}
     * is not set here — call {@link #setDialog} before handing this decorator
     * to a TableWidget.
     */
    public TableDecorator()
    {
    }


    // ── METHOD getDialog — GRABBING THE WORKSHOP'S EDITING TOOL ─────────────
    // The workshop master hands over the specific editing tool assigned to this
    // toolbox.  We return the dialog that will be opened whenever the user
    // wants to add or edit an element in the table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link AddEditDialog} that the TableWidget should open when
     * the user clicks Add or Edit.  May be {@code null} if not yet configured.
     *
     * @return the add/edit dialog, or {@code null} if not set
     */
    public AddEditDialog<E> getDialog()
    {
        return dialog;
    }


    // ── METHOD setDialog — HANGING THE EDITING TOOL ON THE WORKSHOP WALL ─────
    // We mount the editing tool on the designated peg in the workshop so it is
    // always ready when the TableWidget needs to open it.  Call this once
    // during setup before the table becomes visible to the user.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We attach the given {@link AddEditDialog} to this decorator so the
     * TableWidget knows which dialog to open for add and edit operations.
     *
     * @param dialog the dialog to associate with this decorator
     */
    public void setDialog( AddEditDialog<E> dialog )
    {
        this.dialog = dialog;
    }


    // ── METHOD compare — SORTING PARTS ON THE WORKSHOP BENCH ────────────────
    // The workshop master lines up the parts in the right order before assembly.
    // Subclasses decide the ordering rule: negative means e1 comes first,
    // positive means e2 comes first, and zero means they are equivalent.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We compare two elements of type {@code E} to determine their relative
     * order in the table.  Return a negative value when {@code e1} sorts before
     * {@code e2}, a positive value when it sorts after, and zero when they are
     * considered equal.
     *
     * @param e1 the first element to compare
     * @param e2 the second element to compare
     * @return a negative integer, zero, or a positive integer as described
     */
    public abstract int compare( E e1, E e2 );
}
