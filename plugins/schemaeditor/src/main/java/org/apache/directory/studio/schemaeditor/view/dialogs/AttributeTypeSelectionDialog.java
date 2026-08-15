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

import org.apache.directory.api.ldap.model.schema.AttributeType;
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


// ── CLASS: AttributeTypeSelectionDialog — LEIA'S HOLOGRAM MESSAGE ─────────────
// Princess Leia projects herself as a hologram out of R2-D2, delivering a plea
// to whoever is watching: "Help me, Obi-Wan Kenobi. You're my only hope."
// She's asking the viewer to make a choice — pick the right person for the mission.
// This dialog does the same: it pops up, displays a filtered list of attribute types
// from the schema, asks the user to pick one, and delivers that choice back to the
// caller when the user presses "Choose." The search box is R2's filtering intelligence —
// it narrows the hologram's candidates in real time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal selection dialog that lets the user pick a single attribute type from the schema.
 * An attribute type defines what kind of data an LDAP entry can hold — for example, "cn" for
 * common name, or "mail" for email address. This dialog presents the full list (or a filtered
 * subset) with a live search box and shows which schema the highlighted type belongs to.
 * Think of this dialog as Leia's hologram: it appears, presents a clear set of choices,
 * and hands back the user's selection when they confirm.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeSelectionDialog extends Dialog
{
    /** The selected attribute type */
    private AttributeType selectedAttributeType;

    /** The hidden attribute types */
    private List<AttributeType> hiddenAttributeTypes;

    // UI Fields
    private Text searchText;
    private Table attributeTypesTable;
    private TableViewer attributeTypesTableViewer;
    private Label schemaIconLabel;
    private Label schemaNameLabel;
    private Button chooseButton;


    // ── R2 Prepares to Transmit the Hologram ─────────────────────────────────
    // R2-D2 powers up his holographic projector. He doesn't have a recipient yet —
    // he just makes sure his hidden-message filter is empty so the full candidate
    // list is available to whoever he finds to show it to.
    // We wire up to the active workbench shell and initialise an empty list of
    // attribute types to hide — callers can populate that list before opening us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog, ready to display all attribute types from the schema.
     * We initialise the hidden-types list to empty; callers can add entries via
     * {@link #setHiddenAttributeTypes} before opening the dialog to suppress types
     * that shouldn't be selectable in the current context.
     */
    public AttributeTypeSelectionDialog()
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        hiddenAttributeTypes = new ArrayList<AttributeType>();
    }


    // ── Leia Labels the Hologram Transmission ────────────────────────────────
    // The hologram projector displays "CLASSIFIED TRANSMISSION — HELP REQUESTED"
    // in the title bar so that whoever intercepts it knows exactly what kind of
    // message they're looking at.
    // We set the dialog shell's title to the localised "Attribute Type Selection" string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the window title bar text to the localised attribute-type-selection label.
     * JFace calls this before the dialog becomes visible.
     *
     * @param newShell  the freshly created Shell that JFace hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "AttributeTypeSelectionDialog.TypeSelection" ) ); //$NON-NLS-1$
    }


    // ── Leia Projects the Full List of Candidates ────────────────────────────
    // The hologram flickers to life and shows Obi-Wan a roster of potential allies.
    // There's a search field at the top so he can quickly filter by name, a scrollable
    // list of matching attribute types in the middle, and a status strip at the bottom
    // showing which schema the highlighted type belongs to.
    // We build all three zones here and wire the viewers, listeners, and initial input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: a search text field, a filtered/sorted table of
     * matching attribute types, and a small schema-name strip at the bottom.
     * The {@link TableViewer} is backed by {@link AttributeTypeSelectionDialogContentProvider}
     * (which does the filtering) and {@link AttributeTypeSelectionDialogLabelProvider}
     * (which converts each type to a human-readable string with its OID).
     * We force an empty-string search input at the end so the viewer loads the full list immediately.
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
        chooseLabel.setText( Messages.getString( "AttributeTypeSelectionDialog.ChooseAType" ) ); //$NON-NLS-1$
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
                    attributeTypesTable.setFocus();
                }
            }
        } );

        Label matchingLabel = new Label( composite, SWT.NONE );
        matchingLabel.setText( Messages.getString( "AttributeTypeSelectionDialog.MatchingTypes" ) ); //$NON-NLS-1$
        matchingLabel.setLayoutData( new GridData( SWT.FILL, SWT.None, true, false ) );

        attributeTypesTable = new Table( composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.FULL_SELECTION | SWT.HIDE_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.heightHint = 148;
        gridData.minimumHeight = 148;
        gridData.widthHint = 350;
        gridData.minimumWidth = 350;
        attributeTypesTable.setLayoutData( gridData );
        attributeTypesTable.addMouseListener( new MouseAdapter()
        {
            public void mouseDoubleClick( MouseEvent e )
            {
                if ( attributeTypesTable.getSelectionIndex() != -1 )
                {
                    okPressed();
                }
            }
        } );

        attributeTypesTableViewer = new TableViewer( attributeTypesTable );
        attributeTypesTableViewer.setContentProvider( new AttributeTypeSelectionDialogContentProvider(
            hiddenAttributeTypes ) );
        attributeTypesTableViewer.setLabelProvider( new DecoratingLabelProvider(
            new AttributeTypeSelectionDialogLabelProvider(), Activator.getDefault().getWorkbench()
                .getDecoratorManager().getLabelDecorator() ) );
        attributeTypesTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) attributeTypesTableViewer.getSelection();
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
                    schemaNameLabel.setText( ( ( AttributeType ) selection.getFirstElement() ).getSchemaName() );
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


    // ── Leia Offers Only Two Responses ───────────────────────────────────────
    // Obi-Wan can choose to help (Choose) or walk away (Cancel). Leia makes the
    // "Choose" button the default — it's the whole point of the hologram.
    // The Choose button is disabled until the viewer reports a non-empty selection,
    // because "Help me, nobody" is not a valid mission briefing.
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
            .getString( "AttributeTypeSelectionDialog.Choose" ), true ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        StructuredSelection selection = ( StructuredSelection ) attributeTypesTableViewer.getSelection();
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


    // ── Obi-Wan Accepts the Mission ───────────────────────────────────────────
    // Obi-Wan decides to answer the call. But before the mission begins he makes
    // sure there really is a recipient named — you can't go rescue nobody.
    // If the viewer selection is somehow empty when OK fires (which shouldn't
    // happen given our button logic, but defensive coding is wise), we pop an
    // error dialog rather than crashing. Otherwise we latch the selected type
    // and let the JFace OK machinery close the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles the user pressing the "Choose" / OK button.
     * We validate that something is actually selected before storing it and closing.
     * If the selection is empty we show an error dialog and leave the window open —
     * pressing Choose with nothing highlighted is user error, not a crash condition.
     * On a valid selection we store the chosen {@link AttributeType} and call
     * {@code super.okPressed()} to close and signal success.
     */
    protected void okPressed()
    {
        StructuredSelection selection = ( StructuredSelection ) attributeTypesTableViewer.getSelection();
        if ( selection.isEmpty() )
        {
            MessageDialog.openError( getShell(), Messages.getString( "AttributeTypeSelectionDialog.InvalidSelection" ), //$NON-NLS-1$
                Messages.getString( "AttributeTypeSelectionDialog.MustChooseType" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            selectedAttributeType = ( AttributeType ) selection.getFirstElement();
        }

        super.okPressed();
    }


    // ── Obi-Wan Receives Leia's Final Answer ──────────────────────────────────
    // The mission is over — Obi-Wan (the caller) reaches in and retrieves the
    // answer Leia encoded in the hologram: the attribute type that was selected.
    // This is what the whole dialog was built for.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type the user selected and confirmed with the Choose button.
     * Call this after the dialog closes with a Window.OK return code.
     * Returns {@code null} if the dialog was cancelled or if OK was never pressed
     * (though in normal use the caller should check the dialog's return code first).
     *
     * @return  the selected {@link AttributeType}, or {@code null} if none was chosen
     */
    public AttributeType getSelectedAttributeType()
    {
        return selectedAttributeType;
    }


    // ── Leia Marks Certain Candidates as Off-Limits ───────────────────────────
    // Not every member of the Rebellion is available for this particular mission —
    // some are already assigned elsewhere. Leia's briefing specifically excludes
    // them from the list of candidates Obi-Wan sees.
    // Callers use this method to suppress attribute types that are already in use
    // in the current context and shouldn't be offered as options.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the hidden-types list with the given list, preventing those types from
     * appearing in the selection table.
     * Typically called before {@link #open()} to suppress types already used elsewhere
     * in an object class definition or schema construct.
     *
     * @param list  the attribute types to suppress; replaces any previous exclusion list
     */
    public void setHiddenAttributeTypes( List<AttributeType> list )
    {
        hiddenAttributeTypes = list;
    }


    // ── Leia Adds More Names to the Excluded List ────────────────────────────
    // Sometimes Leia adds individual names to the exclusion list one at a time
    // from an array — she hands the clerk the assignment list and they tick off
    // each name as unavailable for this particular mission.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends each attribute type in the given array to the hidden-types list.
     * Use this when you have an array of types to exclude rather than a list —
     * they're added individually rather than replacing the existing exclusions.
     *
     * @param attributeTypes  the types to add to the exclusion list
     */
    public void setHiddenAttributeTypes( AttributeType[] attributeTypes )
    {
        for ( AttributeType objectClass : attributeTypes )
        {
            hiddenAttributeTypes.add( objectClass );
        }
    }


    // ── R2 Filters the Hologram's Candidate Roster ────────────────────────────
    // R2-D2 feeds the search string into his filtering algorithm and the hologram
    // snaps to show only matching candidates. The first match is automatically
    // highlighted so Obi-Wan doesn't have to scroll to a result.
    // We push the search string into the viewer as input — the content provider
    // does the pattern matching — then auto-select the first element if one exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes a new search string into the table viewer, triggering the content provider
     * to re-query and filter the attribute type list.
     * After re-filtering we auto-select the first result so the Choose button
     * is immediately enabled without requiring an explicit click.
     *
     * @param searchString  the text the user typed in the search field; an empty string shows all types
     */
    private void setSearchInput( String searchString )
    {
        attributeTypesTableViewer.setInput( searchString );

        Object firstElement = attributeTypesTableViewer.getElementAt( 0 );
        if ( firstElement != null )
        {
            attributeTypesTableViewer.setSelection( new StructuredSelection( firstElement ), true );
        }
    }


    // ── R2 Powers Down the Holographic Projector ──────────────────────────────
    // When the mission briefing is over, R2 powers down the projector, clears
    // the hologram from memory, and stows his cables. Leaving the projector running
    // wastes resources and can interfere with later transmissions.
    // We dispose all SWT widgets and null their references to free OS handles,
    // then hand control back to the JFace close machinery.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all SWT resources held by this dialog and then closes the window.
     * SWT widgets hold native OS resources — if we just let them go out of scope
     * the OS handle leak can pile up over a long session. We null out every reference
     * after disposing to help the garbage collector and to catch any accidental
     * post-close access as a NullPointerException rather than a silent stale-widget bug.
     *
     * @return  {@code true} if the dialog was closed successfully
     */
    public boolean close()
    {
        hiddenAttributeTypes.clear();
        hiddenAttributeTypes = null;

        attributeTypesTableViewer = null;

        attributeTypesTable.dispose();
        attributeTypesTable = null;

        searchText.dispose();
        searchText = null;

        schemaIconLabel.dispose();
        schemaIconLabel = null;

        schemaNameLabel.dispose();
        schemaNameLabel = null;

        return super.close();
    }
}
