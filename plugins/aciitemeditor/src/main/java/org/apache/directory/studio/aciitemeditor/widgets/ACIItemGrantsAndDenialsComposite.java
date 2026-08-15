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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.api.ldap.aci.GrantAndDenial;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


// ── CLASS: ACIItemGrantsAndDenialsComposite — GRAND MOFF'S PERMISSION PANEL ───
// Inside each permission row of the ACI directive the Grand Moff must specify
// which micro-operations are granted and which are denied.  The panel shows a
// tree with three categories (Read, Modify, Advanced) and per-row checkboxes
// that cycle through unspecified → grant → deny → unspecified.
// Undo/Redo buttons give him a safety net.
// ACIItemGrantsAndDenialsComposite is that permission panel.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} presenting a tree of LDAP micro-operations grouped
 * into Read, Modify, and Advanced categories, each with a three-state
 * (unspecified / grant / deny) checkbox.
 * Provides Grant All, Deny All, Deselect All, Undo, and Redo buttons.
 * Used inside {@link ACIItemItemPermissionsComposite} and
 * {@link ACIItemUserPermissionsComposite} to compose the grants-and-denials
 * set for a single permission row.
 * Think of this as the Grand Moff's micro-operation panel: pick each operation,
 * grant or deny, undo if you change your mind.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemGrantsAndDenialsComposite extends Composite
{
    /** The tree viewer containing all grants and denials */
    private TreeViewer treeViewer = null;

    /** The undo button */
    private Button undoButton = null;

    /** The redo button */
    private Button redoButton = null;

    /** Colum 1 */
    private static final String PERMISSION = Messages.getString( "ACIItemGrantsAndDenialsComposite.column1.header" ); //$NON-NLS-1$

    /** Colum2 */
    private static final String STATE = Messages.getString( "ACIItemGrantsAndDenialsComposite.column2.header" ); //$NON-NLS-1$

    /** The colums */
    private static final String[] COLUMNS = new String[]
        { PERMISSION, STATE };

    /** The undo/redo stack size */
    private static final int MAX_STACK_SIZE = 25;

    /** Used as input for the tree viewer */
    private GrantAndDenialCategory[] grantAndDenialCategories = new GrantAndDenialCategory[]
        {
            new GrantAndDenialCategory(
                Messages.getString( "ACIItemGrantsAndDenialsComposite.category.read" ), true, new GrantAndDenialWrapper[] //$NON-NLS-1$
                    {
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_BROWSE, GrantAndDenial.DENY_BROWSE ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_READ, GrantAndDenial.DENY_READ ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_COMPARE, GrantAndDenial.DENY_COMPARE ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_FILTER_MATCH, GrantAndDenial.DENY_FILTER_MATCH ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_RETURN_DN, GrantAndDenial.DENY_RETURN_DN ) } ),
            new GrantAndDenialCategory(
                Messages.getString( "ACIItemGrantsAndDenialsComposite.category.modify" ), true, new GrantAndDenialWrapper[] //$NON-NLS-1$
                    { new GrantAndDenialWrapper( GrantAndDenial.GRANT_ADD, GrantAndDenial.DENY_ADD ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_MODIFY, GrantAndDenial.DENY_MODIFY ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_REMOVE, GrantAndDenial.DENY_REMOVE ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_RENAME, GrantAndDenial.DENY_RENAME ) } ),
            new GrantAndDenialCategory(
                Messages.getString( "ACIItemGrantsAndDenialsComposite.category.advanced" ), false, new GrantAndDenialWrapper[] //$NON-NLS-1$
                    {
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_EXPORT, GrantAndDenial.DENY_EXPORT ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_IMPORT, GrantAndDenial.DENY_IMPORT ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_INVOKE, GrantAndDenial.DENY_INVOKE ),
                        new GrantAndDenialWrapper( GrantAndDenial.GRANT_DISCLOSE_ON_ERROR,
                            GrantAndDenial.DENY_DISCLOSE_ON_ERROR ) } ) };

    // ── CLASS: GrantAndDenialCategory — MICRO-OPERATION CATEGORY ─────────────
    // A category groups related micro-operations (Read, Modify, Advanced)
    // for display as a collapsible tree node.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Groups related grant/deny wrappers under a named, collapsible tree node.
     */
    private class GrantAndDenialCategory
    {
        /** The category name, displayed in tree */
        private String name;

        /** The initial expanded state */
        private boolean expanded;

        /** The grants and denials wrappers display under this category */
        private GrantAndDenialWrapper[] grantAndDenialWrappers;


        /**
         * Creates a new {@code GrantAndDenialCategory}.
         *
         * @param name                  the category name shown as the tree node label
         * @param expanded              {@code true} if the node should be initially expanded
         * @param grantAndDenialWrappers the micro-operation wrappers in this category
         */
        private GrantAndDenialCategory( String name, boolean expanded, GrantAndDenialWrapper[] grantAndDenialWrappers )
        {
            this.name = name;
            this.expanded = expanded;
            this.grantAndDenialWrappers = grantAndDenialWrappers;
        }
    }

    // ── CLASS: GrantAndDenialWrapper — THREE-STATE MICRO-OPERATION ROW ────────
    // Each leaf row in the tree tracks one micro-operation with three states:
    // unspecified (null), grant, or deny.  Undo/redo stacks allow reverting
    // bulk changes applied by the Grant All / Deny All buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tracks the three-state (unspecified / grant / deny) selection for a single
     * LDAP micro-operation and maintains undo/redo stacks.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class GrantAndDenialWrapper
    {
        /** The grant */
        private GrantAndDenial grant;

        /** The denial */
        private GrantAndDenial denial;

        /** The current state: null=not specified, grant or denial */
        private GrantAndDenial activeGrantAndDenial;

        /** List containing previous states of activeGrandAndDenial */
        private List<GrantAndDenial> undoStack;

        /** List containing "future" states of activeGrandAndDenial */
        private List<GrantAndDenial> redoStack;


        /**
         * Creates a new {@code GrantAndDenialWrapper} for a grant/deny pair.
         *
         * @param grant   the grant constant for this micro-operation
         * @param denial  the deny constant for this micro-operation
         */
        private GrantAndDenialWrapper( GrantAndDenial grant, GrantAndDenial denial )
        {
            this.grant = grant;
            this.denial = denial;
            this.activeGrantAndDenial = null;
            undoStack = new LinkedList<GrantAndDenial>();
            redoStack = new LinkedList<GrantAndDenial>();
        }
    }


    // ── CONSTRUCT THE GRANTS-AND-DENIALS TREE ────────────────────────────────
    // The tree and button panel are built inside a two-column grid.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemGrantsAndDenialsComposite}.
     * Builds the two-column layout, creates the tree viewer, and adds the
     * Grant All / Deny All / Deselect All / Undo / Redo buttons.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemGrantsAndDenialsComposite( Composite parent, int style )
    {
        super( parent, style );

        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = false;
        layout.numColumns = 2;
        setLayout( layout );

        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        setLayoutData( layoutData );

        GridData labelGridData = new GridData();
        labelGridData.horizontalSpan = 2;
        labelGridData.verticalAlignment = GridData.CENTER;
        labelGridData.grabExcessHorizontalSpace = true;
        labelGridData.horizontalAlignment = GridData.FILL;

        Label label = new Label( this, SWT.NONE );
        label.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.description" ) ); //$NON-NLS-1$
        label.setLayoutData( labelGridData );

        createTree();

        createButtonComposite();
    }


    // ── BUILD THE TREE VIEWER ─────────────────────────────────────────────────
    // The two-column tree shows micro-operation names in column 1 and
    // state icons (grant / deny / unspecified) in column 2.
    // Clicking a state cell cycles through the three states.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TreeViewer} with two columns,
     * a checkbox cell editor for the STATE column, and the appropriate
     * content and label providers.
     */
    private void createTree()
    {
        GridData tableGridData = new GridData( GridData.FILL_BOTH );
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.grabExcessVerticalSpace = true;
        tableGridData.verticalAlignment = GridData.FILL;
        tableGridData.horizontalAlignment = GridData.FILL;
        //tableGridData.heightHint = 100;

        Tree tree = new Tree( this, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
            | SWT.HIDE_SELECTION );
        tree.setHeaderVisible( true );
        tree.setLayoutData( tableGridData );
        tree.setLinesVisible( true );

        TreeColumn treeColumn1 = new TreeColumn( tree, SWT.LEFT, 0 );
        treeColumn1.setText( COLUMNS[0] );
        treeColumn1.setWidth( 160 );
        TreeColumn treeColumn2 = new TreeColumn( tree, SWT.LEFT, 1 );
        treeColumn2.setText( COLUMNS[1] );
        treeColumn2.setWidth( 80 );

        treeViewer = new TreeViewer( tree );
        treeViewer.setUseHashlookup( true );

        treeViewer.setColumnProperties( COLUMNS );

        ICellModifier cellModifier = new GrantsAndDenialsCellModifier();
        treeViewer.setCellModifier( cellModifier );
        CellEditor[] cellEditors = new CellEditor[]
            { null, new CheckboxCellEditor( tree ), null };
        treeViewer.setCellEditors( cellEditors );

        treeViewer.setContentProvider( new GrantsAndDenialsContentProvider() );
        treeViewer.setLabelProvider( new GrantsAndDenialsLabelProvider() );
        treeViewer.setInput( grantAndDenialCategories );

        // set expanded state
        List<GrantAndDenialCategory> expandedList = new ArrayList<GrantAndDenialCategory>();
        for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
        {
            if ( grantAndDenialCategory.expanded )
            {
                expandedList.add( grantAndDenialCategory );
            }
        }
        treeViewer.setExpandedElements( expandedList.toArray() );
    }


    // ── BUILD THE BUTTON PANEL ────────────────────────────────────────────────
    // Grant All, Deny All, Deselect All set all rows at once; each call
    // snapshots the current state to the undo stack first.
    // Undo / Redo pop or push from the per-wrapper stacks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the vertical button panel with Grant All, Deny All, Deselect All,
     * Undo, and Redo buttons.
     */
    private void createButtonComposite()
    {
        GridData deselectAllButtonGridData = new GridData();
        deselectAllButtonGridData.horizontalAlignment = GridData.FILL;
        deselectAllButtonGridData.grabExcessHorizontalSpace = false;
        deselectAllButtonGridData.verticalAlignment = GridData.BEGINNING;
        deselectAllButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData denyAllButtonGridData = new GridData();
        denyAllButtonGridData.horizontalAlignment = GridData.FILL;
        denyAllButtonGridData.grabExcessHorizontalSpace = false;
        denyAllButtonGridData.verticalAlignment = GridData.BEGINNING;
        denyAllButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData grantAllButtonGridData = new GridData();
        grantAllButtonGridData.horizontalAlignment = GridData.FILL;
        grantAllButtonGridData.grabExcessHorizontalSpace = false;
        grantAllButtonGridData.verticalAlignment = GridData.BEGINNING;
        grantAllButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData undoButtonGridData = new GridData();
        undoButtonGridData.horizontalAlignment = GridData.FILL;
        undoButtonGridData.grabExcessHorizontalSpace = false;
        undoButtonGridData.verticalAlignment = GridData.BEGINNING;
        undoButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData redoButtonGridData = new GridData();
        redoButtonGridData.horizontalAlignment = GridData.FILL;
        redoButtonGridData.grabExcessHorizontalSpace = false;
        redoButtonGridData.verticalAlignment = GridData.BEGINNING;
        redoButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.CENTER;
        gridData.grabExcessHorizontalSpace = false;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = GridData.FILL;

        Composite buttonComposite = new Composite( this, SWT.NONE );
        buttonComposite.setLayoutData( gridData );
        buttonComposite.setLayout( gridLayout );

        Button grantAllButton = new Button( buttonComposite, SWT.NONE );
        grantAllButton.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.grantAll.button" ) ); //$NON-NLS-1$
        grantAllButton.setLayoutData( grantAllButtonGridData );
        grantAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                backup();
                for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
                {
                    for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.grant;
                    }
                }
                treeViewer.refresh();
            }
        } );

        Button denyAllButton = new Button( buttonComposite, SWT.NONE );
        denyAllButton.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.denyAll.button" ) ); //$NON-NLS-1$
        denyAllButton.setLayoutData( denyAllButtonGridData );
        denyAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                backup();
                for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
                {
                    for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.denial;
                    }
                }
                treeViewer.refresh();
            }
        } );

        Button deselectAllButton = new Button( buttonComposite, SWT.NONE );
        deselectAllButton.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.deselectAll.button" ) ); //$NON-NLS-1$
        deselectAllButton.setLayoutData( deselectAllButtonGridData );
        deselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                backup();
                for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
                {
                    for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = null;
                    }
                }
                treeViewer.refresh();
            }
        } );

        undoButton = new Button( buttonComposite, SWT.NONE );
        undoButton.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.undo.button" ) ); //$NON-NLS-1$
        undoButton.setLayoutData( undoButtonGridData );
        undoButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                undo();
                treeViewer.refresh();
            }
        } );
        undoButton.setEnabled( false );

        redoButton = new Button( buttonComposite, SWT.NONE );
        redoButton.setText( Messages.getString( "ACIItemGrantsAndDenialsComposite.redo.button" ) ); //$NON-NLS-1$
        redoButton.setLayoutData( redoButtonGridData );
        redoButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                redo();
                treeViewer.refresh();
            }
        } );
        redoButton.setEnabled( false );

    }

    // ── CLASS: GrantsAndDenialsCellModifier — STATE CELL CYCLE HANDLER ────────
    // Clicking the STATE cell cycles: unspecified → grant → deny → unspecified.
    // Only GrantAndDenialWrapper rows in the STATE column are editable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@link ICellModifier} that cycles the state of a {@link GrantAndDenialWrapper}
     * through unspecified → grant → deny → unspecified on each click.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class GrantsAndDenialsCellModifier implements ICellModifier
    {

        /**
         * Only GrantAndDenialWrappers and the STATE colum is modifiable.
         *
         * @param element   the element
         * @param property  the property
         * @return          {@code true} if the cell is modifiable
         */
        public boolean canModify( Object element, String property )
        {
            if ( element instanceof GrantAndDenialWrapper )
            {
                return property.equals( STATE );
            }

            return false;
        }


        /**
         * The used CheckboxCellEditor accepts only Booleans.
         *
         * @param element   the element
         * @param property  the property
         * @return          {@code Boolean.TRUE} always (the checkbox editor requires it)
         */
        public Object getValue( Object element, String property )
        {
            if ( ( element instanceof GrantAndDenialWrapper ) && property.equals( STATE ) )
            {
                return Boolean.TRUE;
            }

            return null;
        }


        /**
         * Cycles the wrapper's state: {@code null} → grant → deny → {@code null}.
         * Saves the previous state to the undo stack before changing.
         *
         * @param element   the tree element (or its {@link Item} wrapper)
         * @param value     unused (the click is the signal)
         * @param property  the column property name
         */
        public void modify( Object element, String property, Object value )
        {
            Object target = element;

            if ( element instanceof Item )
            {
                target = ( ( Item ) element ).getData();
            }

            if ( target instanceof GrantAndDenialWrapper )
            {
                GrantAndDenialWrapper grantAndDenialWrapper = ( GrantAndDenialWrapper ) target;

                if ( property.equals( STATE ) )
                {
                    backup();
                    if ( grantAndDenialWrapper.activeGrantAndDenial == null )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.grant;
                    }
                    else if ( grantAndDenialWrapper.activeGrantAndDenial == grantAndDenialWrapper.grant )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.denial;
                    }
                    else if ( grantAndDenialWrapper.activeGrantAndDenial == grantAndDenialWrapper.denial )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = null;
                    }
                }
            }

            treeViewer.refresh();
        }

    }

    // ── CLASS: GrantsAndDenialsContentProvider — TREE STRUCTURE PROVIDER ──────
    // GrantAndDenialCategory items have children (the wrappers); wrappers are
    // leaves.  The content provider exposes this two-level hierarchy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@link ITreeContentProvider} that exposes the two-level structure:
     * categories as roots and wrappers as leaves.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class GrantsAndDenialsContentProvider extends ArrayContentProvider implements ITreeContentProvider
    {

        /**
         * Only GrantAndDenialCategories have children.
         *
         * @param parentElement  the parent element
         * @return               the child wrappers, or {@code null}
         */
        public Object[] getChildren( Object parentElement )
        {
            if ( parentElement instanceof GrantAndDenialCategory )
            {
                GrantAndDenialCategory cat = ( GrantAndDenialCategory ) parentElement;
                return cat.grantAndDenialWrappers;
            }

            return null;
        }


        /**
         * Not used.
         *
         * @param element  the element
         * @return         {@code null}
         */
        public Object getParent( Object element )
        {
            return null;
        }


        /**
         * Only GrantAndDenialCategories have children.
         *
         * @param element  the element
         * @return         {@code true} if {@code element} is a category
         */
        public boolean hasChildren( Object element )
        {
            return ( element instanceof GrantAndDenialCategory );
        }

    }

    // ── CLASS: GrantsAndDenialsLabelProvider — ICON + TEXT RENDERER ───────────
    // Column 1 shows the category name or micro-operation name.
    // Column 2 shows a grant icon, deny icon, or unspecified icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@link ITableLabelProvider} that renders category names and micro-operation
     * names in column 1, and state icons (grant / deny / unspecified) in column 2.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class GrantsAndDenialsLabelProvider extends LabelProvider implements ITableLabelProvider
    {

        /**
         * The STATE is displayed as image.
         *
         * @param element      the element
         * @param columnIndex  the column index
         * @return             the icon for the current state, or {@code null}
         */
        public Image getColumnImage( Object element, int columnIndex )
        {
            if ( element instanceof GrantAndDenialWrapper )
            {
                GrantAndDenialWrapper grantAndDenialWrapper = ( GrantAndDenialWrapper ) element;
                switch ( columnIndex )
                {
                    case 0:
                        return null;
                    case 1:
                        if ( grantAndDenialWrapper.activeGrantAndDenial == null )
                        {
                            return Activator.getDefault().getImage(
                                Messages.getString( "ACIItemGrantsAndDenialsComposite.unspecified.icon" ) ); //$NON-NLS-1$
                        }
                        else if ( grantAndDenialWrapper.activeGrantAndDenial == grantAndDenialWrapper.grant )
                        {
                            return Activator.getDefault().getImage(
                                Messages.getString( "ACIItemGrantsAndDenialsComposite.grant.icon" ) ); //$NON-NLS-1$
                        }
                        else if ( grantAndDenialWrapper.activeGrantAndDenial == grantAndDenialWrapper.denial )
                        {
                            return Activator.getDefault().getImage(
                                Messages.getString( "ACIItemGrantsAndDenialsComposite.deny.icon" ) ); //$NON-NLS-1$
                        }
                    case 2:
                        return null;
                }
            }
            return null;
        }


        /**
         * Returns GrantAndDenialCategory name or the MicroOperation name.
         *
         * @param element      the element
         * @param columnIndex  the column index
         * @return             the text for the cell
         */
        public String getColumnText( Object element, int columnIndex )
        {
            if ( element instanceof GrantAndDenialCategory )
            {
                if ( columnIndex == 0 )
                {
                    GrantAndDenialCategory cat = ( GrantAndDenialCategory ) element;
                    return cat.name;
                }
            }
            else if ( ( element instanceof GrantAndDenialWrapper ) && ( columnIndex == 0 ) )
            {
                GrantAndDenialWrapper wrapper = ( GrantAndDenialWrapper ) element;

                return wrapper.grant.getMicroOperation().getName();
            }

            return ""; //$NON-NLS-1$
        }

    }


    // ── POPULATE FROM A COLLECTION ────────────────────────────────────────────
    /**
     * Populates the tree from the given collection of active {@link GrantAndDenial}
     * values, matching each to the corresponding wrapper and setting its state.
     *
     * @param grantsAndDenials  the active grants and denials to display
     */
    public void setGrantsAndDenials( Collection<GrantAndDenial> grantsAndDenials )
    {
        for ( GrantAndDenial grantAndDenial : grantsAndDenials )
        {
            for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
            {
                for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
                {
                    if ( grantAndDenialWrapper.grant == grantAndDenial )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.grant;
                    }
                    else if ( grantAndDenialWrapper.denial == grantAndDenial )
                    {
                        grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.denial;
                    }
                }
            }
        }

        treeViewer.refresh();
    }


    // ── COLLECT ACTIVE GRANTS AND DENIALS ─────────────────────────────────────
    /**
     * Returns the collection of {@link GrantAndDenial} values currently active
     * (i.e., not in the unspecified state) in the tree.
     *
     * @return the active grants and denials
     * @throws ParseException  not thrown; declared for interface compatibility
     */
    public Collection<GrantAndDenial> getGrantsAndDenials() throws ParseException
    {
        Collection<GrantAndDenial> grantsAndDenials = new ArrayList<GrantAndDenial>();

        for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
        {
            for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
            {
                if ( grantAndDenialWrapper.activeGrantAndDenial != null )
                {
                    grantsAndDenials.add( grantAndDenialWrapper.activeGrantAndDenial );
                }
            }
        }

        return grantsAndDenials;
    }


    // ── UNDO THE LAST CHANGE ──────────────────────────────────────────────────
    /**
     * Pops the previous state from each wrapper's undo stack and pushes the
     * current state to the redo stack.  Updates button enabled states.
     */
    private void undo()
    {
        for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
        {
            for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
            {
                if ( !grantAndDenialWrapper.undoStack.isEmpty() )
                {
                    grantAndDenialWrapper.redoStack.add( 0, grantAndDenialWrapper.activeGrantAndDenial );
                    grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.undoStack.remove( 0 );
                }

                undoButton.setEnabled( !grantAndDenialWrapper.undoStack.isEmpty() );
                redoButton.setEnabled( !grantAndDenialWrapper.redoStack.isEmpty() );
            }
        }
    }


    // ── REDO THE LAST UNDONE CHANGE ───────────────────────────────────────────
    /**
     * Pops the next state from each wrapper's redo stack and pushes the
     * current state to the undo stack.  Updates button enabled states.
     */
    private void redo()
    {
        for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
        {
            for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
            {
                if ( !grantAndDenialWrapper.redoStack.isEmpty() )
                {
                    grantAndDenialWrapper.undoStack.add( 0, grantAndDenialWrapper.activeGrantAndDenial );
                    grantAndDenialWrapper.activeGrantAndDenial = grantAndDenialWrapper.redoStack.remove( 0 );
                }

                undoButton.setEnabled( !grantAndDenialWrapper.undoStack.isEmpty() );
                redoButton.setEnabled( !grantAndDenialWrapper.redoStack.isEmpty() );
            }
        }
    }


    // ── SNAPSHOT CURRENT STATE ────────────────────────────────────────────────
    /**
     * Saves the current state of all wrappers to their undo stacks and clears
     * the redo stacks.  Called before any bulk mutation (Grant All, Deny All,
     * Deselect All, or individual cell click).
     */
    private void backup()
    {
        for ( GrantAndDenialCategory grantAndDenialCategory : grantAndDenialCategories )
        {
            for ( GrantAndDenialWrapper grantAndDenialWrapper : grantAndDenialCategory.grantAndDenialWrappers )
            {
                if ( grantAndDenialWrapper.undoStack.size() == MAX_STACK_SIZE )
                {
                    grantAndDenialWrapper.undoStack.remove( grantAndDenialWrapper.undoStack.size() - 1 );
                }
                grantAndDenialWrapper.undoStack.add( 0, grantAndDenialWrapper.activeGrantAndDenial );
                grantAndDenialWrapper.redoStack.clear();

                undoButton.setEnabled( !grantAndDenialWrapper.undoStack.isEmpty() );
                redoButton.setEnabled( !grantAndDenialWrapper.redoStack.isEmpty() );
            }
        }
    }

}
