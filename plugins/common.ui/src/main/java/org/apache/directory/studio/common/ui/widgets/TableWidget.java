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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.Messages;
import org.apache.directory.studio.common.ui.TableDecorator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: TableWidget — REBEL ALLIANCE MISSION CONTROL ROSTER BOARD ─────────
// The mission control roster board at Rebel HQ lists every active mission in
// order, with buttons to Add a new mission, Edit an existing one, Delete a
// completed one, and move missions Up or Down the priority list.  This widget
// is exactly that board: a JFace TableViewer backed by a live element list,
// with Add/Edit/Delete buttons and optional Up/Down ordering controls.
// Everything funnels through a TableDecorator that knows how to label and
// sort the elements, and an AddEditDialog for the actual data-entry work.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide a self-contained SWT composite that wraps a JFace TableViewer
 * together with Add, Edit, Delete, and (optionally) Up and Down buttons.  A
 * {@link TableDecorator} supplies the label provider, comparator, and dialog
 * for the element type {@code E}.  Callers supply the initial element list via
 * {@link #setElements} and retrieve the result via {@link #getElements}.
 *
 * <pre>
 * +--------------------------------------+
 * | Element 1                            | (Add... )
 * | Element 2                            | (Edit...)
 * |                                      | (Delete )
 * +--------------------------------------+
 * </pre>
 *
 * The elements can optionally be ordered:
 *
 * <pre>
 * Note : This class contain codes from the Apache PDF box project ('sort' method)
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TableWidget<E> extends AbstractWidget
{
    /** The element list */
    private List<E> elements = new ArrayList<E>();

    /** The current selection index, if any */
    private int currentSelection;

    /** A flag set to tell if we have a Edit button */
    private boolean hasEdit;

    /** A flag set when the table is ordered (ie, it has a Up and Down buttons) */
    private boolean isOrdered;

    /** A flag that says if teh table is enabled or disabled */
    private boolean isEnabled = true;

    /** The flag set when the table is ordered */
    private static final boolean ORDERED = true;

    /** The flag set when the table is not ordered */
    private static final boolean UNORDERED = false;

    // UI widgets
    private Composite composite;
    private Table elementTable;
    private TableViewer elementTableViewer;

    // The buttons
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    /** The decorator */
    private TableDecorator<E> decorator;

    // A listener on the Elements table, that modifies the button when an Element is selected
    private ISelectionChangedListener tableViewerSelectionChangedListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            if ( isEnabled )
            {
                int selectionLine = elementTableViewer.getTable().getSelectionIndex();

                if ( selectionLine == currentSelection )
                {
                    // We have selected the line twice, deselect the line
                    elementTableViewer.getTable().deselect( selectionLine );
                    currentSelection = -1;
                }
                else
                {
                    currentSelection = selectionLine;
                    StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();

                    if ( hasEdit )
                    {
                        editButton.setEnabled( !selection.isEmpty() );
                    }

                    deleteButton.setEnabled( !selection.isEmpty() );

                    if ( isOrdered )
                    {
                        // We can't enable the UP button when we don't have any element in the table,
                        // or when we have only one, or when the selection is the first one in the table
                        upButton.setEnabled( !selection.isEmpty() && ( elements.size() > 1 ) && ( selectionLine > 0 ) );

                        // We can't enable the DOWN button when we don't have any element in the table,
                        // or when we have only one element, or when the selected element is the last one
                        downButton.setEnabled( !selection.isEmpty() && ( elements.size() > 1 ) && ( selectionLine < elements.size() - 1 ) );
                    }
                }
            }
        }
    };


    // A listener on the Element table, that reacts to a doubleClick : it's opening the Element editor
    private IDoubleClickListener tableViewerDoubleClickListener = new IDoubleClickListener()
    {
        public void doubleClick( DoubleClickEvent event )
        {
            if ( isEnabled )
            {
                editElement();
            }
        }
    };


    // A listener on the Add button, which opens the Element addition editor
    private SelectionListener addButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            addElement();
        }
    };


    // A listener on the Edit button, that open the Element editor
    private SelectionListener editButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            editElement();
        }
    };


    // A listener on the Delete button, which delete the selected Element
    private SelectionListener deleteButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            deleteElement();
        }
    };


    // A listener on the Up button, that move the selected elemnt up one position
    private SelectionListener upButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            upElement();
        }
    };


    // A listener on the Down button, that move the selected element down one position
    private SelectionListener downButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            downElement();
        }
    };


    // ── CONSTRUCTOR TableWidget — OPENING THE MISSION ROSTER BOARD ────────────
    // We open the roster board and attach the decorator toolbox — the decorator
    // carries the label provider, comparator, and dialog we will need whenever
    // the user interacts with the table.  We start with no selected row (-1).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new TableWidget with the given decorator, which provides the
     * label provider, comparator, and add/edit dialog for elements of type
     * {@code E}.  Call one of the {@code createWidget*} methods to build the
     * actual SWT controls.
     *
     * @param decorator the decorator that supplies labeling, sorting, and the dialog
     */
    public TableWidget( TableDecorator<E> decorator )
    {
        this.decorator = decorator;
        currentSelection = -1;
    }


    // ── METHOD createWidgetWithEdit — ASSEMBLING THE FULL ROSTER BOARD ────────
    // We assemble the full three-button roster board: Add, Edit, and Delete.
    // The Edit button lets the user revise an existing mission entry rather
    // than having to delete and re-add it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build a TableWidget composite with Add, Edit, and Delete buttons.
     * Use this variant when the user must be able to modify existing elements
     * without removing and re-adding them.
     *
     * <pre>
     * +--------------------------------------+
     * | Element 1                            | (Add... )
     * | Element 2                            | (Edit...)
     * |                                      | (Delete )
     * +--------------------------------------+
     * </pre>
     *
     * @param parent  the parent composite
     * @param toolkit the form toolkit for styling, or {@code null} for plain SWT
     */
    public void createWidgetWithEdit( Composite parent, FormToolkit toolkit )
    {
        createWidget( parent, toolkit, true, UNORDERED );
    }


    // ── METHOD createWidgetNoEdit — ASSEMBLING THE TWO-BUTTON ROSTER BOARD ───
    // We assemble the simpler two-button roster board: Add and Delete only.
    // Use this when elements cannot be modified once added — only added or
    // removed entirely.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build a TableWidget composite with Add and Delete buttons only.
     * Use this variant when elements are either created fresh or removed — no
     * in-place editing.
     *
     * <pre>
     * +--------------------------------------+
     * | Element 1                            | (Add... )
     * | Element 2                            | (Delete )
     * |                                      |
     * +--------------------------------------+
     * </pre>
     *
     * @param parent  the parent composite
     * @param toolkit the form toolkit for styling, or {@code null} for plain SWT
     */
    public void createWidgetNoEdit( Composite parent, FormToolkit toolkit )
    {
        createWidget( parent, toolkit, false, UNORDERED );
    }


    // ── METHOD createOrderedWidgetWithEdit — FULL FIVE-BUTTON PRIORITY BOARD ──
    // We assemble the complete five-button priority board: Add, Edit, Delete,
    // and Up and Down for reordering.  Use this when the order of elements
    // matters — like a list of SASL mechanisms tried in priority order.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build a TableWidget composite with Add, Edit, Delete, Up, and Down
     * buttons.  Use this when both editing and reordering of elements are
     * required.
     *
     * <pre>
     * +--------------------------------------+
     * | Element 1                            | (Add... )
     * | Element 2                            | (Edit...)
     * |                                      | (Delete )
     * |                                      | ---------
     * |                                      | (Up... )
     * |                                      | (Down.. )
     * +--------------------------------------+
     * </pre>
     *
     * @param parent  the parent composite
     * @param toolkit the form toolkit for styling, or {@code null} for plain SWT
     */
    public void createOrderedWidgetWithEdit( Composite parent, FormToolkit toolkit )
    {
        createWidget( parent, toolkit, true, ORDERED );
    }


    // ── METHOD createOrderedWidgetNoEdit — FOUR-BUTTON PRIORITY BOARD ─────────
    // We assemble a four-button priority board: Add, Delete, Up, and Down but
    // no Edit button.  Elements can be removed and reordered but not modified
    // once they are on the board.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build a TableWidget composite with Add, Delete, Up, and Down buttons.
     * Use this when elements need ordering but not in-place editing.
     *
     * <pre>
     * +--------------------------------------+
     * | Element 1                            | (Add... )
     * | Element 2                            | (Delete )
     * |                                      | ---------
     * |                                      | (Up... )
     * |                                      | (Down.. )
     * +--------------------------------------+
     * </pre>
     *
     * @param parent  the parent composite
     * @param toolkit the form toolkit for styling, or {@code null} for plain SWT
     */
    public void createOrderedWidgetNoEdit( Composite parent, FormToolkit toolkit )
    {
        createWidget( parent, toolkit, true, ORDERED );
    }


    // ── METHOD createWidget (private) — WIRING UP THE ROSTER BOARD ───────────
    // This is the single private method that all the public factory methods
    // delegate to.  We build the composite, create the table viewer, wire up
    // the listeners, and conditionally add buttons based on the hasEdit and
    // isOrdered flags.  The result is a fully functional mission roster board.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We build the complete TableWidget UI: a two-column composite containing
     * the table viewer on the left and stacked buttons on the right.  The
     * specific set of buttons depends on {@code hasEdit} and {@code isOrdered}.
     * We wire all button and table listener callbacks here.
     *
     * @param parent    the parent composite
     * @param toolkit   the form toolkit, or {@code null} for plain SWT widgets
     * @param hasEdit   {@code true} to include an Edit button
     * @param isOrdered {@code true} to include Up and Down ordering buttons
     */
    private void createWidget( Composite parent, FormToolkit toolkit, boolean hasEdit, boolean isOrdered )
    {
        this.hasEdit = hasEdit;
        this.isOrdered = isOrdered;

        // Composite
        if ( toolkit != null )
        {
            composite = toolkit.createComposite( parent );
        }
        else
        {
            composite = new Composite( parent, SWT.NONE );
        }

        // First, define a grid of 3 columns (two for the table, one for the buttons)
        GridLayout compositeGridLayout = new GridLayout( 2, false );
        compositeGridLayout.marginHeight = compositeGridLayout.marginWidth = 0;
        composite.setLayout( compositeGridLayout );

        // Create the Element Table and Table Viewer
        if ( toolkit != null )
        {
            elementTable = toolkit.createTable( composite, SWT.NULL );
        }
        else
        {
            elementTable = new Table( composite, SWT.NULL );
        }

        // Define the table size and height. It will span on 3 to 5 lines,
        // depending on the number of buttons
        int nbLinesSpan = 3;

        if ( isOrdered )
        {
            // If it's an ordered table, we add 3 line s: one for Up, one for Down and one for the separator
            nbLinesSpan += 3;
        }

        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, nbLinesSpan );
        elementTable.setLayoutData( gd );

        // Create the index TableViewer
        elementTableViewer = new TableViewer( elementTable );
        elementTableViewer.setContentProvider( new ArrayContentProvider() );

        // The LabelProvider
        elementTableViewer.setLabelProvider( decorator );
        elementTableViewer.addSelectionChangedListener( tableViewerSelectionChangedListener );

        // Listeners : we want to catch changes and double clicks (if we have an edit button)
        if ( hasEdit )
        {
            elementTableViewer.addDoubleClickListener( tableViewerDoubleClickListener );
        }

        // Inject the existing elements
        elementTableViewer.setInput( elements );

        GridData buttonGd = new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1 );
        buttonGd.widthHint = 60;

        // Create the Add Button and its listener
        if ( toolkit != null )
        {
            addButton = toolkit.createButton( composite, Messages.getString( "CommonUIWidgets.AddButton" ), SWT.PUSH );
            addButton.setLayoutData( buttonGd );
        }
        else
        {
            addButton = BaseWidgetUtils.createButton( composite, Messages.getString( "CommonUIWidgets.AddButton" ), 1 );
            addButton.setLayoutData( buttonGd );
        }

        addButton.setLayoutData( buttonGd );
        addButton.addSelectionListener( addButtonListener );

        // Create the Edit Button and its listener, if requested
        if ( hasEdit )
        {
            if ( toolkit != null )
            {
                editButton = toolkit.createButton( composite, Messages.getString( "CommonUIWidgets.EditButton" ), SWT.PUSH );
            }
            else
            {
                editButton = BaseWidgetUtils.createButton( composite, Messages.getString( "CommonUIWidgets.EditButton" ), SWT.PUSH );
            }

            // It's not enabled unless we have selected an element
            editButton.setEnabled( false );
            editButton.setLayoutData( buttonGd );
            editButton.addSelectionListener( editButtonListener );
        }

        // Create the Delete Button and its listener
        if ( toolkit != null )
        {
            deleteButton = toolkit.createButton( composite, Messages.getString( "CommonUIWidgets.DeleteButton" ), SWT.PUSH );
        }
        else
        {
            deleteButton = BaseWidgetUtils.createButton( composite, Messages.getString( "CommonUIWidgets.DeleteButton" ), SWT.PUSH );
        }

        // It's not selected unless we have selected an index
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( buttonGd );
        deleteButton.addSelectionListener( deleteButtonListener );

        // Create the Up and Down button, if requested
        if ( isOrdered )
        {
            Label separator = BaseWidgetUtils.createSeparator( composite, 1 );
            separator.setLayoutData( new GridData( SWT.NONE, SWT.BEGINNING, false, false ) );

            // Create the Up Button and its listener
            if ( toolkit != null )
            {
                upButton = toolkit.createButton( composite, Messages.getString( "CommonUIWidgets.UpButton" ), SWT.PUSH );
            }
            else
            {
                upButton = BaseWidgetUtils.createButton( composite, Messages.getString( "CommonUIWidgets.UpButton" ), SWT.PUSH );
            }

            // It's not selected unless we have selected an index
            upButton.setEnabled( false );
            upButton.setLayoutData( buttonGd );
            //upButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
            upButton.addSelectionListener( upButtonListener );

            // Create the Down Button and its listener
            if ( toolkit != null )
            {
                downButton = toolkit.createButton( composite, Messages.getString( "CommonUIWidgets.DownButton" ), SWT.PUSH );
            }
            else
            {
                downButton = BaseWidgetUtils.createButton( composite, Messages.getString( "CommonUIWidgets.DownButton" ), SWT.PUSH );
            }

            // It's not selected unless we have selected an index
            downButton.setEnabled( false );
            downButton.setLayoutData( buttonGd );
            //downButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
            downButton.addSelectionListener( downButtonListener );
        }
    }


    // ── METHOD enable — ACTIVATING THE ROSTER BOARD CONTROLS ─────────────────
    // Mission control switches the roster board back on after it was locked
    // down.  We re-enable the Add, Up, and Down buttons and set our isEnabled
    // flag so selection events start updating Edit and Delete again.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We re-enable the Add button (and Up/Down buttons if present) and set the
     * internal enabled flag so selection-change events start enabling Edit and
     * Delete based on the current selection.  Call this when the table should
     * become interactive again.
     */
    public void enable()
    {
        if ( addButton != null )
        {
            addButton.setEnabled( true );
        }

        if ( upButton != null )
        {
            upButton.setEnabled( true );
        }

        if ( downButton != null )
        {
            downButton.setEnabled( true );
        }

        isEnabled = true;
    }


    // ── METHOD disable — LOCKING DOWN THE ROSTER BOARD ────────────────────────
    // Mission control locks down the roster board — all buttons go dark and
    // the selection listener stops reacting.  Use this when the table should be
    // read-only, for example while a background operation is running.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We disable all buttons (Add, Edit, Delete, Up, Down) and clear the
     * internal enabled flag so selection events no longer re-enable any of
     * them.  Call this when the table should be temporarily read-only.
     */
    public void disable()
    {
        if ( addButton != null )
        {
            addButton.setEnabled( false );
        }

        if ( deleteButton != null )
        {
            deleteButton.setEnabled( false );
        }

        if ( editButton != null )
        {
            editButton.setEnabled( false );
        }

        if ( upButton != null )
        {
            upButton.setEnabled( false );
        }

        if ( downButton != null )
        {
            downButton.setEnabled( false );
        }

        isEnabled = false;
    }

    // ── METHOD getControl — RETRIEVING THE ROSTER BOARD PANEL ────────────────
    // We hand over the outermost composite panel so layout managers and parent
    // composites can treat this entire widget as a single placeable unit.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the root {@link Control} of this widget so callers can place
     * it inside a parent composite's layout.
     *
     * @return the root composite of this TableWidget
     */
    public Control getControl()
    {
        return composite;
    }

    /* --------------------------------------------------------------------------------------------------------------- */
    /* Taken from the Apache PdfBox project,                                                                           */
    /* @author UWe Pachler                                                                                             */
    /* --------------------------------------------------------------------------------------------------------------- */
    // ── METHOD sort — SORTING THE ROSTER BY PRIORITY ─────────────────────────
    // Mission control sorts the entire roster board using the given comparator.
    // Lists with fewer than two entries are already sorted, so we skip them.
    // For larger lists we delegate to our quicksort implementation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We sort the given list in place using the given comparator.  Lists with
     * fewer than two elements are left unchanged.  This implementation uses a
     * quicksort algorithm adapted from the Apache PDFBox project.
     *
     * @param <T>  the element type
     * @param list the list to sort in place
     * @param cmp  the comparator that defines the ordering
     */
    public static <T> void sort( List<T> list, Comparator<T> cmp )
    {
        int size = list.size();

        if ( size < 2 )
        {
            return;
        }

        quicksort( list, cmp, 0, size - 1 );
    }


    private static <T> void quicksort( List<T> list, Comparator<T> cmp, int left, int right )
    {
        if ( left < right )
        {
            int splitter = split( list, cmp, left, right );
            quicksort( list, cmp, left, splitter - 1 );
            quicksort( list, cmp, splitter + 1, right );
        }
    }


    private static <T> void swap( List<T> list, int i, int j )
    {
        T tmp = list.get( i );
        list.set( i, list.get( j ) );
        list.set( j, tmp );
    }


    private static <T> int split( List<T> list, Comparator<T> cmp, int left, int right )
    {
        int i = left;
        int j = right - 1;
        T pivot = list.get( right );

        do
        {
            while ( ( cmp.compare( list.get( i ), pivot ) <= 0 ) && ( i < right ) )
            {
                ++i;
            }

            while ( ( cmp.compare( pivot, list.get( j ) ) <= 0 ) && ( j > left ) )
            {
                --j;
            }

            if ( i < j )
            {
                swap( list, i, j );
            }
        } while ( i < j );

        if ( cmp.compare( pivot, list.get( i ) ) < 0 )
        {
            swap( list, i, right );
        }

        return i;
    }
    /* --------------------------------------------------------------------------------------------------------------- */
    /* End of the QuickSort implementation taken  from the Apache PdfBox project,                                      */
    /* --------------------------------------------------------------------------------------------------------------- */


    // ── METHOD setElements — LOADING THE FULL MISSION ROSTER ─────────────────
    // Mission control loads an entirely new roster onto the board.  We clear
    // the old list, sort the new elements using the decorator's comparator,
    // then refresh the table viewer so the UI reflects the new state.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We replace the current element list with the given one, sorting it
     * through the decorator's comparator before storing.  The table viewer is
     * refreshed immediately.  Passing {@code null} or an empty list clears
     * the table.
     *
     * @param elements the new list of elements to display
     */
    public void setElements( List<E> elements )
    {
        this.elements.clear();

        if ( ( elements != null ) && ( elements.size() > 0 ) )
        {
            sort( elements, decorator );
            this.elements.addAll( elements );
        }

        elementTableViewer.refresh();
    }


    // ── METHOD getElements — READING THE CURRENT ROSTER ──────────────────────
    // Mission control reads off the current roster so callers can inspect or
    // persist the list.  We hand back a defensive copy so external modifications
    // do not corrupt our internal state between operations.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return a defensive copy of the current element list.  Modifying the
     * returned list has no effect on the table's internal state.  Returns
     * {@code null} only when our internal list is somehow null (which should
     * not happen in normal use).
     *
     * @return a copy of the current element list, or {@code null}
     */
    public List<E> getElements()
    {
        if ( elements != null )
        {
            List<E> copy = new ArrayList<E>( elements.size() );

            copy.addAll( elements );

            return copy;
        }

        return null;
    }


    // ── METHOD addElement (private) — ENROLLING A NEW MISSION ────────────────
    // The Add button fires: we open the add dialog, and if the user confirms
    // we insert the new element into the list at the right position.  For
    // ordered tables we update the prefix of every following element to keep
    // the numbering consecutive.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We handle an Add button click by opening the add/edit dialog in add mode.
     * If the user confirms, we insert the new element at the appropriate position
     * in the list (after the current selection for unordered tables, or at the
     * selection index for ordered ones) and notify listeners.
     */
    private void addElement()
    {
        AddEditDialog<E> dialog = decorator.getDialog();
        dialog.setAdd();
        dialog.addNewElement();
        dialog.setElements( elements );

        // Inject the position if we have a selected value
        StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();
        int insertionPos = elements.size();

        if ( !selection.isEmpty() )
        {
            insertionPos = elementTableViewer.getTable().getSelectionIndex();
        }

        // Open the Dialog, and process the addition if it went fine
        if ( decorator.getDialog().open() == Dialog.OK )
        {
            E newElement = decorator.getDialog().getEditedElement();

            if ( !elements.contains( newElement ) )
            {
                String elementStr = newElement.toString();
                int pos = 0;

                if ( isOrdered )
                {
                    // The table is ordered, insert the element at the right position
                    ((OrderedElement)newElement).setPrefix( insertionPos );
                    elements.add( insertionPos, newElement );

                    // Move up the following elements
                    for ( int i = insertionPos + 1; i < elements.size(); i++ )
                    {
                        E element = elements.get( i );
                        ((OrderedElement)element).incrementPrefix();
                    }
                }
                else
                {
                    if ( selection.isEmpty() )
                    {
                        // no selected element, add at the end
                        pos = elements.size();
                    }
                    else
                    {
                        pos = elementTableViewer.getTable().getSelectionIndex() + 1;
                    }

                    elements.add( pos, newElement );
                }

                elementTableViewer.refresh();
                elementTableViewer.setSelection( new StructuredSelection( elementStr ) );
            }

            notifyListeners();
        }
    }


    // ── METHOD editElement (private) — REVISING A MISSION ON THE BOARD ────────
    // The Edit button fires (or the user double-clicks): we open the edit dialog
    // pre-filled with the selected element.  If the user confirms the changes,
    // we replace the element in the list and refresh the viewer.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We handle an Edit button click or double-click by opening the add/edit
     * dialog in edit mode, pre-filled with the selected element.  If the user
     * confirms, we replace the original element with the edited version in the
     * list, taking care of ordering constraints, and notify listeners.
     */
    private void editElement()
    {
        StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            AddEditDialog<E> dialog = decorator.getDialog();
            dialog.setEdit();

            E selectedElement = (E)selection.getFirstElement();
            int editPosition = elementTableViewer.getTable().getSelectionIndex();
            dialog.setEditedElement( selectedElement );
            dialog.setSelectedPosition( editPosition );

            // Open the element dialog, with the selected index
            if ( decorator.getDialog().open() == Dialog.OK )
            {
                E newElement = dialog.getEditedElement();

                if ( !isOrdered )
                {
                    // Check to see if the modified element does not already exist
                    if ( elements.contains( newElement ) )
                    {
                        // Remove the original element
                        elements.remove( selectedElement );

                        // Replace the existing element with the new one
                        elements.remove( newElement );

                        int pos = 0;

                        for ( E element : elements )
                        {
                            if ( decorator.compare( element, newElement ) > 0 )
                            {
                                break;
                            }
                            else
                            {
                                pos++;
                            }
                        }

                        elements.add( pos, newElement );
                    }
                    else
                    {
                        // We will remove the modified element, and replace it with the new element
                        // Replace the old element by the new one
                        elements.remove( editPosition );
                        elements.add( editPosition, newElement );
                    }
                }
                else
                {
                    // Remove the original element
                    elements.remove( selectedElement );

                    elements.add( editPosition, newElement );
                }

                elementTableViewer.refresh();
                elementTableViewer.setSelection( new StructuredSelection( newElement.toString() ) );

                notifyListeners();
            }
        }
    }


    // ── METHOD deleteElement (private) — REMOVING A MISSION FROM THE BOARD ───
    // The Delete button fires: we remove the selected element from the list.
    // For ordered tables we also decrement the prefix of every element that
    // follows the deleted one so the numbering stays consecutive.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We handle a Delete button click by removing the selected element from the
     * list.  For ordered tables we decrement the prefix of every subsequent
     * element to keep the ordering sequence contiguous.  The table viewer is
     * refreshed and listeners are notified.
     */
    private void deleteElement()
    {
        StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            // If the table is ordered, we need to decrement the prefix of all the following elements
            if ( isOrdered )
            {
                int selectedPosition = elementTableViewer.getTable().getSelectionIndex();

                for ( int i = selectedPosition + 1; i < elements.size(); i++ )
                {
                    E nextElement = elements.get( i );
                    ((OrderedElement)nextElement).decrementPrefix();
                    elements.set( i - 1, nextElement );
                }

                elements.remove( elements.size() - 1 );
            }
            else
            {
                int selectedPosition = elementTableViewer.getTable().getSelectionIndex();
                elements.remove( selectedPosition );
            }

            elementTableViewer.refresh();
            notifyListeners();
        }
    }


    // ── METHOD upElement (private) — PROMOTING A MISSION UP THE PRIORITY LIST ─
    // The Up button fires: we swap the selected element with the one directly
    // above it on the board and update both elements' prefix values so the
    // ordering stays correct.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We move the selected element one position up in the ordered list by
     * swapping it with the element immediately above it.  Both elements'
     * prefix values are updated, the viewer is refreshed, and listeners are
     * notified.
     */
    private void upElement()
    {
        StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            // Get the line the selected element is in. We will move it up
            int selectionLine = elementTableViewer.getTable().getSelectionIndex();

            // The selected element
            E selectedElement = (E)selection.getFirstElement();

            // Decrease the prefix
            ((OrderedElement)selectedElement).decrementPrefix();

            // Just swap the elements which is just before with the selected one
            E previousElement = getElements().get( selectionLine - 1 );

            // Increase the prefix
            ((OrderedElement)previousElement).incrementPrefix();

            elements.remove( selectionLine - 1 );
            elements.add( selectionLine, previousElement );

            // Refresh the table now
            elementTableViewer.refresh();
            elementTableViewer.setSelection( new StructuredSelection( selectedElement ) );

            notifyListeners();
        }
    }


    // ── METHOD downElement (private) — DEMOTING A MISSION DOWN THE LIST ───────
    // The Down button fires: we swap the selected element with the one directly
    // below it and update both elements' prefix values so the ordering is
    // consistent with the new positions.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We move the selected element one position down in the ordered list by
     * swapping it with the element immediately below it.  Both elements' prefix
     * values are updated, the viewer is refreshed, and listeners are notified.
     */
    private void downElement()
    {
        StructuredSelection selection = ( StructuredSelection ) elementTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            // Get the line the selected element is in. We will move it down
            int selectionLine = elementTableViewer.getTable().getSelectionIndex();

            // The selected element
            E selectedElement = (E)selection.getFirstElement();

            // Increase the prefix
            ((OrderedElement)selectedElement).incrementPrefix();

            // Just swap the elements which is just after with the selected one
            E previousElement = getElements().get( selectionLine + 1 );

            // Decrease the prefix
            ((OrderedElement)previousElement).decrementPrefix();

            elements.remove( selectionLine + 1 );
            elements.add( selectionLine, previousElement );

            // refresh the table now
            elementTableViewer.refresh();
            elementTableViewer.setSelection( new StructuredSelection( selectedElement ) );

            notifyListeners();
        }
    }
}
