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

package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ObjectClassSelectionDialog — LEIA'S HOLOGRAM TO LUKE ───────────────
// On the second Death Star, Princess Leia appears in Luke's vision during his
// meditation, urging him to choose the right path — which rebel faction to align
// with, which course of action will save the most lives.
// This dialog does the same thing for object classes: it projects a filtered list
// of LDAP object classes onto the screen and asks the user to pick the right one.
// The search box narrows the list as Luke focuses his thoughts, and a schema-name
// strip shows which registry the highlighted class belongs to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal selection dialog that lets the user pick a single object class from the schema.
 * An object class defines the shape of an LDAP entry — which attributes it must have
 * (MUST), which it may have (MAY), and what its structural role is. This dialog
 * presents the full list (or a filtered subset) with a live search box and shows which
 * schema the highlighted class belongs to.
 * Think of this dialog as Leia's hologram to Luke: it appears, shows a focused set
 * of choices, and hands the user's selection back to the caller when confirmed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassSelectionDialog extends Dialog
{
    /** The selected object class */
    private ObjectClass selectedObjectClass;

    /** The hidden Object Classes */
    private List<ObjectClass> hiddenObjectClasses;

    // UI Fields
    private Text searchText;
    private Table objectClassesTable;
    private TableViewer objectClassesTableViewer;
    private Label schemaIconLabel;
    private Label schemaNameLabel;
    private Button chooseButton;


    // ── Luke Powers Up His Meditation Chamber ─────────────────────────────────
    // Luke settles into meditation, clearing his mind so Leia's vision can reach him.
    // He doesn't know which object class he'll be shown yet — the hidden-classes list
    // starts empty so every class in the schema is available for consideration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog, ready to display all object classes from the schema.
     * We initialise the hidden-classes list to empty; callers can add entries via
     * {@link #setHiddenObjectClasses} before opening the dialog to suppress classes
     * that shouldn't be selectable in the current context.
     */
    public ObjectClassSelectionDialog()
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        hiddenObjectClasses = new ArrayList<ObjectClass>();
    }


    // ── Leia Labels the Vision ────────────────────────────────────────────────
    // Before the vision begins, Luke's senses tell him what kind of message is
    // coming: "Object Class Selection." He knows to pay attention to the class
    // registry, not the attribute types.
    // We set the window title to the localised "Object Class Selection" string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the window title bar text to the localised object-class-selection label.
     * JFace calls this before the dialog becomes visible.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "ObjectClassSelectionDialog.ClassSelection" ) ); //$NON-NLS-1$
    }


    // ── Leia Projects the Full Roster of Object Classes ───────────────────────
    // The vision shows Luke every available object class from the schema registry —
    // a long, scrollable list. He can type in the search field to narrow it down.
    // As he highlights one, a schema-badge strip at the bottom tells him which
    // part of the LDAP spec that class came from.
    // We build all three zones (search, table, schema strip) and wire the viewers,
    // listeners, and initial input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a search text field, a filtered/sorted table of
     * matching object classes, and a small schema-name strip at the bottom.
     * The {@link TableViewer} is backed by {@link ObjectClassSelectionDialogContentProvider}
     * (filtering) and {@link ObjectClassSelectionDialogLabelProvider} (labels + icons).
     * We force an empty-string search at the end so the viewer loads the full list immediately.
     *
     * @param parent  the parent composite supplied by the JFace Dialog framework
     * @return        the assembled composite, handed back to JFace for embedding
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout( 1, false );
        composite.setLayout( layout );

        Label chooseLabel = new Label( composite, SWT.NONE );
        chooseLabel.setText( Messages.getString( "ObjectClassSelectionDialog.ChooseClass" ) ); //$NON-NLS-1$
        chooseLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        searchText = new Text( composite, SWT.BORDER | SWT.SEARCH );
        searchText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        searchText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                setSearchInput( searchText.getText() );
            }
        } );
        searchText.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( e.keyCode == SWT.ARROW_DOWN )
                {
                    objectClassesTable.setFocus();
                }
            }
        } );

        Label matchingLabel = new Label( composite, SWT.NONE );
        matchingLabel.setText( Messages.getString( "ObjectClassSelectionDialog.MatchingClasses" ) ); //$NON-NLS-1$
        matchingLabel.setLayoutData( new GridData( SWT.FILL, SWT.None, true, false ) );

        objectClassesTable = new Table( composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.FULL_SELECTION | SWT.HIDE_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.heightHint = 148;
        gridData.minimumHeight = 148;
        gridData.widthHint = 350;
        gridData.minimumWidth = 350;
        objectClassesTable.setLayoutData( gridData );
        objectClassesTable.addMouseListener( new MouseAdapter()
        {
            public void mouseDoubleClick( MouseEvent e )
            {
                if ( objectClassesTable.getSelectionIndex() != -1 )
                {
                    okPressed();
                }
            }
        } );

        objectClassesTableViewer = new TableViewer( objectClassesTable );
        objectClassesTableViewer
            .setContentProvider( new ObjectClassSelectionDialogContentProvider( hiddenObjectClasses ) );
        objectClassesTableViewer.setLabelProvider( new DecoratingLabelProvider(
            new ObjectClassSelectionDialogLabelProvider(), Activator.getDefault().getWorkbench().getDecoratorManager()
                .getLabelDecorator() ) );
        objectClassesTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) objectClassesTableViewer.getSelection();
                if ( selection.isEmpty() )
                {
                    if ( ( chooseButton != null ) && ( !chooseButton.isDisposed() ) )
                    {
                        chooseButton.setEnabled( false );
                    }
                    schemaIconLabel.setImage( Activator.getDefault().getImage( PluginConstants.IMG_TRANSPARENT_16X16 ) );
                    schemaNameLabel.setText( "" ); //$NON-NLS-1$
                }
                else
                {
                    if ( ( chooseButton != null ) && ( !chooseButton.isDisposed() ) )
                    {
                        chooseButton.setEnabled( true );
                    }
                    schemaIconLabel.setImage( Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA ) );
                    schemaNameLabel.setText( ( ( ObjectClass ) selection.getFirstElement() ).getSchemaName() );
                }
            }
        } );

        // Schema Composite
        Composite schemaComposite = new Composite( composite, SWT.BORDER );
        schemaComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        GridLayout schemaCompositeGridLayout = new GridLayout( 2, false );
        schemaCompositeGridLayout.horizontalSpacing = 0;
        schemaCompositeGridLayout.verticalSpacing = 0;
        schemaCompositeGridLayout.marginWidth = 2;
        schemaCompositeGridLayout.marginHeight = 2;
        schemaComposite.setLayout( schemaCompositeGridLayout );

        // Schema Icon Label
        schemaIconLabel = new Label( schemaComposite, SWT.NONE );
        GridData schemaIconLabelGridData = new GridData( SWT.NONE, SWT.BOTTOM, false, false );
        schemaIconLabelGridData.widthHint = 18;
        schemaIconLabelGridData.heightHint = 16;
        schemaIconLabel.setLayoutData( schemaIconLabelGridData );
        schemaIconLabel.setImage( Activator.getDefault().getImage( PluginConstants.IMG_TRANSPARENT_16X16 ) );

        // Schema Name Label
        schemaNameLabel = new Label( schemaComposite, SWT.NONE );
        schemaNameLabel.setLayoutData( new GridData( SWT.FILL, SWT.BOTTOM, true, false ) );
        schemaNameLabel.setText( "" ); //$NON-NLS-1$

        // We need to force the input to load the complete list of attribute types
        setSearchInput( "" ); //$NON-NLS-1$

        return composite;
    }


    // ── Leia Offers Luke Two Responses ───────────────────────────────────────
    // Luke can choose to follow the vision (Choose) or dismiss it (Cancel).
    // The Choose button is the default — it's what Leia came to ask for — but
    // it stays disabled until Luke actually highlights an object class in the list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog's button bar with a "Choose" button mapped to OK_ID
     * and a standard Cancel button.
     * We check the viewer's current selection immediately after creating the buttons
     * so the Choose button starts disabled if nothing is highlighted yet.
     *
     * @param parent  the button bar composite JFace hands us
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        chooseButton = createButton( parent, IDialogConstants.OK_ID, Messages
            .getString( "ObjectClassSelectionDialog.Choose" ), true ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        StructuredSelection selection = ( StructuredSelection ) objectClassesTableViewer.getSelection();
        if ( selection.isEmpty() )
        {
            if ( ( chooseButton != null ) && ( !chooseButton.isDisposed() ) )
            {
                chooseButton.setEnabled( false );
            }
        }
        else
        {
            if ( ( chooseButton != null ) && ( !chooseButton.isDisposed() ) )
            {
                chooseButton.setEnabled( true );
            }
        }
    }


    // ── Luke Acts on the Vision ───────────────────────────────────────────────
    // Luke decides to trust Leia's guidance and steps forward. But he can't act
    // on an empty vision — if nothing was highlighted in the list when he pressed
    // Choose, we show an error dialog and leave the window open. With a valid
    // selection, we store the chosen class and let JFace close the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles the user pressing the "Choose" / OK button.
     * We validate that something is actually selected before storing it and closing.
     * If the selection is somehow empty we show an error dialog and return early —
     * the window stays open for the user to fix the selection.
     * On a valid selection we store the chosen {@link ObjectClass} and call
     * {@code super.okPressed()} to signal success.
     */
    protected void okPressed()
    {
        StructuredSelection selection = ( StructuredSelection ) objectClassesTableViewer.getSelection();

        if ( selection.isEmpty() )
        {
            MessageDialog.openError( getShell(), Messages.getString( "ObjectClassSelectionDialog.InvalidSelection" ), //$NON-NLS-1$
                Messages.getString( "ObjectClassSelectionDialog.MustChooseClass" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            selectedObjectClass = ( ObjectClass ) selection.getFirstElement();
        }

        super.okPressed();
    }


    // ── Luke Recalls Leia's Chosen Path ──────────────────────────────────────
    // After the vision fades, Luke holds onto the answer Leia brought him —
    // the specific object class he should use. The caller retrieves that answer
    // to apply it to the schema definition being built.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the object class the user selected and confirmed with the Choose button.
     * Call this after the dialog closes with a {@code Window.OK} return code.
     * Returns {@code null} if the dialog was cancelled or OK was never pressed.
     *
     * @return  the selected {@link ObjectClass}, or {@code null} if none was chosen
     */
    public ObjectClass getSelectedObjectClass()
    {
        return selectedObjectClass;
    }


    // ── Leia Marks Certain Classes as Off-Limits ──────────────────────────────
    // Leia's vision doesn't show Luke every option — some object classes are already
    // in use in the current context and showing them again would be misleading.
    // Callers use this method to replace the hidden-classes list before opening.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the hidden-classes list with the given list, preventing those classes from
     * appearing in the selection table.
     * Typically called before {@link #open()} to suppress classes already used as
     * superclasses or already included in the current definition.
     *
     * @param list  the object classes to suppress; replaces any previous exclusion list
     */
    public void setHiddenObjectClasses( List<ObjectClass> list )
    {
        hiddenObjectClasses = list;
    }


    // ── Leia Adds More Classes to the Excluded List ────────────────────────────
    // When the excluded list comes as an array rather than a list, Leia adds
    // each entry individually — same result, different packaging.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends each object class in the given array to the hidden-classes list.
     * Use this when you have an array of classes to exclude rather than a list.
     *
     * @param objectClasses  the classes to add to the exclusion list
     */
    public void setHiddenObjectClasses( ObjectClass[] objectClasses )
    {
        for ( ObjectClass objectClass : objectClasses )
        {
            hiddenObjectClasses.add( objectClass );
        }
    }


    // ── Luke Focuses the Vision with a Search Term ────────────────────────────
    // Luke concentrates on a specific word — "person", "inet" — and the vision
    // snaps to show only object classes that match. The first match is automatically
    // highlighted so he doesn't have to scroll before the Choose button lights up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes a new search string into the table viewer, triggering the content provider
     * to re-query and filter the object class list.
     * After re-filtering we auto-select the first result so the Choose button is
     * immediately enabled without requiring a separate click.
     *
     * @param searchString  the text the user typed; an empty string shows all classes
     */
    private void setSearchInput( String searchString )
    {
        objectClassesTableViewer.setInput( searchString );

        Object firstElement = objectClassesTableViewer.getElementAt( 0 );
        if ( firstElement != null )
        {
            objectClassesTableViewer.setSelection( new StructuredSelection( firstElement ), true );
        }
    }


    // ── Leia's Vision Fades — Luke Returns to the Present ────────────────────
    // When the meditation ends, Luke releases the vision. The holographic projection
    // of every candidate object class dissolves, freeing the memory of the Force
    // for whatever comes next.
    // We dispose all SWT widgets and null every reference to release OS handles
    // before handing control back to JFace's close machinery.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all SWT resources held by this dialog and then closes the window.
     * SWT widgets hold native OS resources that won't be released by the garbage
     * collector — we have to dispose them explicitly. We null every reference after
     * disposing so any accidental post-close access fails fast as a NullPointerException.
     *
     * @return  {@code true} if the dialog was closed successfully
     */
    public boolean close()
    {
        hiddenObjectClasses.clear();
        hiddenObjectClasses = null;

        objectClassesTableViewer = null;

        objectClassesTable.dispose();
        objectClassesTable = null;

        searchText.dispose();
        searchText = null;

        schemaIconLabel.dispose();
        schemaIconLabel = null;

        schemaNameLabel.dispose();
        schemaNameLabel = null;

        return super.close();
    }
}
