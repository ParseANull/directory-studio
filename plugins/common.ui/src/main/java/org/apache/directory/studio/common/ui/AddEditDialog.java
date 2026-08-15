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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

// ── CLASS: AddEditDialog — JEDI COUNCIL DELIBERATION CHAMBER ────────────────
// Like the Jedi Council convening to review a mission brief, this class is the
// base for all dialogs that let the user add a brand-new element or revise an
// existing one inside a TableWidget.  Subclasses fill in the specifics of what
// each mission — er, element — looks like.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We use this abstract class as the foundation for every dialog tied to the
 * Add or Edit action of a TableWidget.  Extend us to build a concrete dialog
 * for any element type {@code E} your table needs to manage.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 *
 * @param <E> The Element type stored in the table
 */
public abstract class AddEditDialog<E> extends Dialog
{
    /** The edited Element, if any */
    private E editedElement;

    /** The table's elements */
    private List<E> elements;

    /** The position of the selected element, if we have any */
    private int selectedPosition;

    /** A flag set to true when the dialog is opened using the Add button */
    private boolean isAdd = false;

    /** A flag used to tell if the okButton must be disabled */
    protected boolean okDisabled = false;

    // ── CONSTRUCTOR AddEditDialog — OPENING THE COUNCIL CHAMBER ─────────────
    // We open the Council chamber doors by wiring ourselves up to the parent
    // shell, readying the dialog for whatever the Jedi — or the user — decides
    // to do next.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new instance and attach it to the given parent shell so the
     * dialog knows where to appear on screen.
     *
     * @param parentShell the shell that will own this dialog
     */
    protected AddEditDialog( Shell parentShell )
    {
        super( parentShell );
    }

    // ── METHOD initDialog — READING THE MISSION BRIEF ───────────────────────
    // Before the Council session begins, every Jedi reads the mission brief.
    // Subclasses implement this to pre-populate dialog fields with the data
    // from the element being edited, so the user sees the right starting point.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the dialog by loading the current state of the edited
     * element into our UI controls.  Subclasses must implement this to fill
     * in the right fields before the dialog becomes visible.
     */
    protected abstract void initDialog();


    // ── METHOD createButtonBar — ARMING THE COUNCIL'S CONTROL PANEL ─────────
    // The Council's control panel lights up when a Jedi enters, but certain
    // switches stay dark until the right conditions are met.  Here we build
    // the button bar and optionally disable the OK button when okDisabled is
    // true, preventing premature confirmation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We override this so we can disable the OK button right from the start
     * when {@code okDisabled} is set to {@code true}.  This lets subclasses
     * block confirmation until the user has entered valid data.
     *
     * @param parent the composite that hosts the button bar
     * @return the button bar control
     */
    protected Control createButtonBar( Composite parent )
    {
        Control buttonBar = super.createButtonBar( parent );

        if ( okDisabled )
        {
            Button okButton = getButton( IDialogConstants.OK_ID );
            okButton.setEnabled( false );
        }

        return buttonBar;
    }


    // ── METHOD addNewElement — ENROLLING A NEW PADAWAN ──────────────────────
    // When the Council decides to bring in a fresh recruit, this method is
    // called to prepare a blank element ready for the user to fill in.
    // Subclasses define what "blank" means for their specific element type.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We call this when the user clicks Add, so subclasses can initialize a
     * fresh, empty element and store it via {@link #setEditedElement}.  Think
     * of it as handing the user a blank mission form to fill out.
     */
    public abstract void addNewElement();


    // ── METHOD getEditedElement — RETRIEVING THE MISSION DOSSIER ────────────
    // The Council retrieves the current dossier from the archives so it can
    // be examined.  We return whatever element was placed in the dialog for
    // editing, or null if none has been set yet.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the element currently being edited, or {@code null} if no
     * element has been set.  Call this after the dialog closes with OK to get
     * the user's result.
     *
     * @return the element being edited, or {@code null}
     */
    public E getEditedElement()
    {
        return editedElement;
    }


    // ── METHOD setEditedElement — FILING THE MISSION DOSSIER ────────────────
    // The Council files the dossier we hand it so the dialog knows which
    // mission it is reviewing.  We store the element here so subclasses and
    // callers can later retrieve it with getEditedElement.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We store the element that the dialog should display for editing.  Call
     * this before opening the dialog so the user sees the right data to
     * modify.
     *
     * @param editedElement the element to edit
     */
    public final void setEditedElement( E editedElement )
    {
        this.editedElement = editedElement;
    }

    // ── METHOD getSelectedPosition — LOCATING THE CHOSEN SEAT ───────────────
    // In the Council chamber, each Jedi sits at a numbered seat.  We return
    // the index of the row that was selected in the table when this dialog
    // was opened, so callers know where in the list the action is happening.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the zero-based index of the table row that was selected when
     * this dialog was opened, which tells us where the edited element lives
     * in the list.
     *
     * @return the selected row index
     */
    public int getSelectedPosition()
    {
        return selectedPosition;
    }


    // ── METHOD setSelectedPosition — ASSIGNING THE COUNCIL SEAT ─────────────
    // Before the Council session, we assign each delegate to a specific seat
    // so everyone knows the order.  We record the table row index here so the
    // dialog can pass it back to the caller after editing.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We record which row in the table was selected so we can later report
     * back the position of the element that was edited or added.
     *
     * @param selectedPosition the zero-based row index to remember
     */
    public void setSelectedPosition( int selectedPosition )
    {
        this.selectedPosition = selectedPosition;
    }


    // ── METHOD setElements — LOADING THE FULL MISSION ROSTER ────────────────
    // The Council is handed the complete list of active missions so it can
    // check for duplicates or enforce ordering rules.  We make a defensive
    // copy to avoid outside changes affecting our internal state mid-session.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We store a copy of the table's current element list so dialogs can
     * check what already exists before adding or modifying entries.  We copy
     * defensively so the original list stays untouched.
     *
     * @param elements the elements currently in the TableWidget
     */
    public void setElements( List<E> elements )
    {
        this.elements = new ArrayList<E>();
        this.elements.addAll( elements );
    }


    // ── METHOD getElements — CONSULTING THE MISSION ROSTER ──────────────────
    // The Council reviews the full roster of active missions.  We return our
    // internal copy of the element list so subclasses can inspect existing
    // entries while the dialog is open.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return our internal copy of the element list, which subclasses can
     * use to validate new entries or detect duplicates before confirming.
     *
     * @return the list of elements from the TableWidget
     */
    protected List<E> getElements()
    {
        return elements;
    }


    // ── METHOD setAdd — SWITCHING TO NEW-MISSION MODE ───────────────────────
    // The Council raises the green flag: we are here to brief a brand-new
    // mission, not review an old one.  Setting this flag tells the dialog it
    // is operating in Add mode.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We flip the dialog into Add mode by setting the {@code isAdd} flag to
     * {@code true}.  Call this before opening the dialog when the user clicked
     * the Add button.
     */
    public void setAdd()
    {
        isAdd = true;
    }


    // ── METHOD setEdit — SWITCHING TO EDIT-MISSION MODE ─────────────────────
    // The Council lowers the green flag and raises the amber one: we are here
    // to revise an existing mission, not create a new one.  Setting this flag
    // tells the dialog it is operating in Edit mode.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We flip the dialog into Edit mode by setting the {@code isAdd} flag to
     * {@code false}.  Call this before opening the dialog when the user clicked
     * the Edit button.
     */
    public void setEdit()
    {
        isAdd = false;
    }


    // ── METHOD isAdd — CHECKING WHICH COUNCIL SESSION WE ARE IN ─────────────
    // A quick glance at the chamber's flag tells every Jedi whether this is a
    // new-mission briefing or a review of prior orders.  We return true when
    // the dialog was opened via the Add button.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} when this dialog was opened in Add mode (the user
     * clicked Add), or {@code false} when it was opened in Edit mode.  Use
     * this to distinguish between creating and updating an element.
     *
     * @return {@code true} if we are adding a new element
     */
    public boolean isAdd()
    {
        return isAdd;
    }
}
