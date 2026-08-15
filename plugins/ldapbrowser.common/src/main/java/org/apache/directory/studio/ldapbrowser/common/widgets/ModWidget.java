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

package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.util.ArrayList;
import java.util.Arrays;

import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


// ── CLASS: ModWidget — MON MOTHMA PLANNING THE BATTLE OF ENDOR ───────────────
// Mon Mothma stands at the war room table and builds the attack plan operation by operation:
// "Add Gold Squadron, replace the shield generator assignment, delete the ground team."
// ModWidget lets you compose an LDAP modify request the same way — each operation is a
// "ModSpec" with an operation type (add/replace/delete), an attribute, and one or more values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A composite widget for building an LDAP {@code modify} operation interactively.
 * An LDAP modify request is made up of one or more "modification specs", each saying:
 * "add/replace/delete this attribute with these values." ModWidget renders those specs
 * as a scrollable list of grouped form rows with +/- buttons to add or remove both
 * specs and individual value lines.
 * Think of Mon Mothma at the Endor briefing table: she can add new operation groups,
 * replace existing ones, remove them, and at the end she calls {@link #getLdifFragment()}
 * to get the full LDIF changeset ready to send.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModWidget extends AbstractWidget
{
    /** The scrolled composite */
    private ScrolledComposite scrolledComposite;

    /** The composite that contains the ModSpecs */
    private Composite composite;

    /** The list of ModSpecs */
    private ArrayList<ModSpec> modSpecList = new ArrayList<ModSpec>();

    /** The list content proposal provider */
    private ListContentProposalProvider listContentProposalProvider;

    /** The resulting LDIF */
    private String ldif;

    // Listeners
    private ModifyListener modifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            validate( true );
        }
    };


    // ── MON MOTHMA OPENS THE SCHEMA BRIEFING ─────────────────────────────────────
    // Mon Mothma is handed the complete Alliance roster before she can plan operations.
    // She sorts the attribute names alphabetically and loads them into the autocomplete provider.
    // We extract attribute type names from the schema so the mod-attribute combos can autocomplete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ModWidget and initializes its autocomplete provider with all attribute type
     * names available in the given schema.
     * The names are sorted alphabetically before being loaded so the autocomplete list reads cleanly.
     *
     * <p>For example — Mon Mothma reviews the Alliance roster:</p>
     * <pre>
     *   attributeDescriptions = SchemaUtils.getNamesAsArray(schema.getAttributeTypeDescriptions());
     *   Arrays.sort(attributeDescriptions);  // alphabetical order
     *   // autocomplete now knows: "cn", "mail", "sn", "uid", ...
     * </pre>
     *
     * @param schema  The LDAP schema for the current connection; its attribute types populate
     *                the autocomplete drop-down on each mod-attribute combo.
     */
    public ModWidget( Schema schema )
    {
        String[] attributeDescriptions = SchemaUtils.getNamesAsArray( schema.getAttributeTypeDescriptions() );
        Arrays.sort( attributeDescriptions );
        listContentProposalProvider = new ListContentProposalProvider( attributeDescriptions );
    }


    // ── MON MOTHMA ADJOURNS THE WAR ROOM ─────────────────────────────────────────
    // After the mission is complete, Mon Mothma closes the briefing room — nothing left to clean up.
    // This lifecycle hook is required by AbstractWidget but has no resources to release here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this widget and releases held resources.
     * Currently a no-op — the SWT widget tree handles its own disposal — but exists as a
     * required lifecycle hook for the {@link AbstractWidget} contract.
     *
     * <p>For example — Mon Mothma adjourns:</p>
     * <pre>
     *   "The meeting is over. Dismiss the staff."
     *   // Nothing to tear down explicitly.
     * </pre>
     */
    public void dispose()
    {
    }


    // ── MON MOTHMA READS THE FINAL BATTLE ORDERS ──────────────────────────────────
    // After building the operation plan, Mon Mothma hands the final LDIF fragment to the
    // fleet communications officer to transmit to the server.
    // We return the LDIF built by getLdifFragment() — or null if nothing is ready yet.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF fragment representing the current modify operation.
     * The fragment starts with {@code changetype: modify} and lists each modification spec.
     * This is computed fresh by {@link #getLdifFragment()} — calling this getter returns
     * the internally cached value, which may be stale if the UI was changed without calling
     * getLdifFragment() again.
     *
     * <p>For example — Mon Mothma reads the orders:</p>
     * <pre>
     *   ldif = "changetype: modify\nadd: mail\nmail: leia@endor.gov\n-\n"
     * </pre>
     *
     * @return  The cached LDIF fragment string, or {@code null} if none has been built yet.
     */
    public String getLdif()
    {
        return ldif;
    }


    // ── MON MOTHMA SETS UP THE WAR ROOM TABLE ────────────────────────────────────
    // Mon Mothma arranges the holographic briefing table — scrollable, expandable,
    // with room for as many operation groups as the mission requires.
    // We build the ScrolledComposite container and add the first default ModSpec.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the SWT composite for this widget inside a scrollable container.
     * Creates a {@link ScrolledComposite} so the list of modification specs can grow
     * beyond the visible area. Adds one initial empty ModSpec row so the user has
     * something to fill in immediately.
     *
     * <p>For example — Mon Mothma sets up the briefing table:</p>
     * <pre>
     *   scrolledComposite  ← outer scrollable container
     *     composite        ← inner column container with 3 columns
     *       [ModSpec 1]    ← first operation row, added by addInitialModSpec()
     * </pre>
     *
     * @param parent  The SWT parent composite to attach the scrolled container to.
     * @return        The {@link ScrolledComposite} wrapping all the mod-spec rows.
     */
    public Composite createContents( Composite parent )
    {
        // Creating the scrolled composite containing all UI
        scrolledComposite = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
        scrolledComposite.setLayout( new GridLayout() );
        scrolledComposite.setExpandHorizontal( true );
        scrolledComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Creating the composite
        composite = BaseWidgetUtils.createColumnContainer( scrolledComposite, 3, 1 );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        scrolledComposite.setContent( composite );

        addInitialModSpec();

        validate( false );

        return scrolledComposite;
    }


    // ── MON MOTHMA REVIEWS THE CURRENT OPERATION PLAN ─────────────────────────────
    // Mon Mothma checks the board: are there enough operations? Can each one be deleted
    // individually, or does the last one have to stay? Do value lines need delete protection?
    // We enable/disable delete buttons based on list sizes and optionally notify listeners.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current state of the widget by updating button enabled states.
     * If there is only one ModSpec, its delete button is disabled (you can't remove the last one).
     * Same logic applies per ModSpec: if there is only one value line, its delete button is disabled.
     * Optionally fires {@code notifyListeners()} so the parent dialog can refresh its OK button.
     *
     * <p>For example — Mon Mothma reviews the board:</p>
     * <pre>
     *   if (modSpecList.size() > 1) modSpec.modDeleteButton.setEnabled(true);
     *   else                        modSpec.modDeleteButton.setEnabled(false);
     *   if (notifyListeners) notifyListeners();
     * </pre>
     *
     * @param notifyListeners  When {@code true}, fires {@code notifyListeners()} after updating
     *                         button states; pass {@code false} during initial setup to avoid
     *                         premature notifications.
     */
    public void validate( boolean notifyListeners )
    {
        for ( int i = 0; i < modSpecList.size(); i++ )
        {
            ModSpec modSpec = ( ModSpec ) modSpecList.get( i );
            if ( modSpecList.size() > 1 )
            {
                modSpec.modDeleteButton.setEnabled( true );
            }
            else
            {
                modSpec.modDeleteButton.setEnabled( false );
            }
            for ( int k = 0; k < modSpec.valueLineList.size(); k++ )
            {
                ValueLine valueLine = ( ValueLine ) modSpec.valueLineList.get( k );
                if ( modSpec.valueLineList.size() > 1 )
                {
                    valueLine.valueDeleteButton.setEnabled( true );
                }
                else
                {
                    valueLine.valueDeleteButton.setEnabled( false );
                }
            }
        }

        if ( notifyListeners )
        {
            notifyListeners();
        }
    }


    // ── MON MOTHMA OPENS THE BRIEFING WITH THE FIRST OPERATION ───────────────────
    // Every battle plan needs at least one operation — Mon Mothma always starts with one.
    // She calls addModSpec(0) to create the opening entry on the board.
    // This convenience method seeds the form with one empty ModSpec at position 0.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Seeds the widget with one empty modification spec at position 0.
     * Called during {@link #createContents} so the user sees a usable form immediately
     * rather than a completely blank screen.
     *
     * <p>For example — Mon Mothma opens the briefing:</p>
     * <pre>
     *   "We start with one operation. The fleet can add more as needed."
     *   addModSpec(0);
     * </pre>
     */
    private void addInitialModSpec()
    {
        addModSpec( 0 );
    }


    // ── MON MOTHMA INSERTS A NEW OPERATION INTO THE PLAN ─────────────────────────
    // A commander requests a new operation slot at a specific position in the battle plan.
    // Mon Mothma inserts it and shuffles the following operations down to maintain order.
    // We use the SWT reparenting trick to reorder composites in the layout.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a new empty ModSpec at the given index in the modification list.
     * Because SWT places widgets in creation order, we use a reparenting trick to reorder
     * any existing ModSpecs that should appear after the new one.
     * After insertion, the composite size is recomputed to fit the new row.
     *
     * <p>For example — Mon Mothma inserts a new operation:</p>
     * <pre>
     *   "Add a 'replace mail' block between operations 2 and 3."
     *   // new ModSpec created, existing ones from index onward reparented to follow it
     *   composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
     * </pre>
     *
     * @param index  The 0-based position at which to insert the new ModSpec.
     */
    private void addModSpec( int index )
    {
        // Getting the array of modification specs
        ModSpec[] modSpecs = ( ModSpec[] ) modSpecList.toArray( new ModSpec[modSpecList.size()] );

        // Adding a new modification spec
        ModSpec newModSpec = createModSpec( true );
        modSpecList.add( newModSpec );

        if ( modSpecs.length > 0 )
        {
            for ( int i = index; i < modSpecs.length; i++ )
            {
                ModSpec modSpec = modSpecs[i];

                // That's a trick to relocate the modification spec
                // beneath the newly created one
                modSpec.modGroup.setParent( scrolledComposite );
                modSpec.modAddButton.setParent( scrolledComposite );
                modSpec.modDeleteButton.setParent( scrolledComposite );
                modSpec.modGroup.setParent( composite );
                modSpec.modAddButton.setParent( composite );
                modSpec.modDeleteButton.setParent( composite );

                // Same trick to update the id in the list
                modSpecList.remove( modSpec );
                modSpecList.add( modSpec );
            }
        }

        composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }


    // ── MON MOTHMA ASSEMBLES A SINGLE OPERATION BLOCK ────────────────────────────
    // Mon Mothma picks up a blank operation card and fills in the structure:
    // an operation type (add/replace/delete), an attribute name, value lines, and +/- buttons.
    // We build the SWT Group, combos, buttons, and optionally the first value line.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and returns a single ModSpec — the full SWT UI for one modification operation.
     * Each ModSpec consists of:
     * <ul>
     *   <li>A read-only combo for the operation type (add/replace/delete)</li>
     *   <li>A free-form combo with autocomplete for the attribute name</li>
     *   <li>"+"/"-" buttons to add/remove this ModSpec from the list</li>
     *   <li>Optionally an initial ValueLine inside the group</li>
     * </ul>
     * All interactive widgets are wired to the shared {@code modifyListener}.
     *
     * <p>For example — Mon Mothma fills in an operation card:</p>
     * <pre>
     *   modType        = "add"    (drop-down: add / replace / delete)
     *   modAttribute   = "mail"   (attribute type, with autocomplete from schema)
     *   [+ value line] → "leia@endor.gov"
     * </pre>
     *
     * @param addFirstValueLine  When {@code true}, adds one empty value line inside the group
     *                           so the user can type a value right away.
     * @return                   The fully initialized {@link ModSpec} with all SWT widgets attached.
     */
    private ModSpec createModSpec( boolean addFirstValueLine )
    {
        ModSpec modSpec = new ModSpec();

        modSpec.modGroup = BaseWidgetUtils.createGroup( composite, "", 1 ); //$NON-NLS-1$
        modSpec.modGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite modSpecComposite = BaseWidgetUtils.createColumnContainer( modSpec.modGroup, 2, 1 );
        modSpec.modType = BaseWidgetUtils.createReadonlyCombo( modSpecComposite, new String[]
            { "add", "replace", "delete" }, 0, 1 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        modSpec.modType.setLayoutData( new GridData() );
        modSpec.modType.addModifyListener( modifyListener );

        // attribute combo with field decoration and content proposal
        modSpec.modAttributeCombo = BaseWidgetUtils.createCombo( modSpecComposite, new String[0], -1, 1 );
        new ExtendedContentAssistCommandAdapter( modSpec.modAttributeCombo, new ComboContentAdapter(),
            listContentProposalProvider, null, null, true );
        modSpec.modAttributeCombo.addModifyListener( modifyListener );

        // add button with listener
        modSpec.modAddButton = new Button( composite, SWT.PUSH );
        modSpec.modAddButton.setText( "  +   " ); //$NON-NLS-1$
        modSpec.modAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = modSpecList.size();
                for ( int i = 0; i < modSpecList.size(); i++ )
                {
                    ModSpec modSpec = modSpecList.get( i );
                    if ( modSpec.modAddButton == e.widget )
                    {
                        index = i + 1;
                    }
                }

                addModSpec( index );

                validate( true );
            }
        } );

        // delete button with listener
        modSpec.modDeleteButton = new Button( composite, SWT.PUSH );
        modSpec.modDeleteButton.setText( "  −  " ); //$NON-NLS-1$
        modSpec.modDeleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = 0;
                for ( int i = 0; i < modSpecList.size(); i++ )
                {
                    ModSpec modSpec = modSpecList.get( i );
                    if ( modSpec.modDeleteButton == e.widget )
                    {
                        index = i;
                    }
                }

                deleteModSpec( index );

                validate( true );
            }
        } );

        if ( addFirstValueLine )
        {
            addValueLine( modSpec, 0, false );
        }

        return modSpec;
    }


    // ── MON MOTHMA CANCELS AN OPERATION ───────────────────────────────────────────
    // A commander reports that one operation is no longer viable — Mon Mothma strikes it
    // from the board and collapses the gap so the remaining operations stay in sequence.
    // We remove the ModSpec from the list, dispose its SWT widgets, and resize the composite.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the ModSpec at the given index, disposes all its SWT widgets, and resizes
     * the inner composite to close the gap.
     *
     * <p>For example — Mon Mothma strikes an operation from the board:</p>
     * <pre>
     *   modSpecList.remove(index);
     *   modSpec.modGroup.dispose();
     *   modSpec.modAddButton.dispose();
     *   modSpec.modDeleteButton.dispose();
     *   composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
     * </pre>
     *
     * @param index  The 0-based index of the ModSpec to remove.
     */
    private void deleteModSpec( int index )
    {
        ModSpec modSpec = modSpecList.remove( index );
        if ( modSpec != null )
        {
            modSpec.modGroup.dispose();
            modSpec.modAddButton.dispose();
            modSpec.modDeleteButton.dispose();

            composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
    }


    // ── MON MOTHMA ADDS A VALUE ROW TO AN OPERATION ───────────────────────────────
    // Each operation can have multiple values — Mon Mothma adds a new value slot below
    // the existing ones, using the SWT reparenting trick to maintain visual order.
    // We insert a new ValueLine at the given index inside the given ModSpec's value list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a new empty ValueLine at the given index within the given ModSpec's value list.
     * Uses the SWT reparenting trick (same as {@link #addModSpec}) to keep the visual
     * order of existing value lines consistent with their list order.
     * Optionally resizes the composite afterward if the caller needs the layout updated immediately.
     *
     * <p>For example — Mon Mothma adds a value slot:</p>
     * <pre>
     *   "Operation 'add mail' needs a second email address."
     *   addValueLine(modSpec, 1, true);  // new empty value line at position 1
     * </pre>
     *
     * @param modSpec     The ModSpec to add the value line to.
     * @param index       The 0-based position within the ModSpec's value list to insert at.
     * @param updateSize  When {@code true}, recomputes and sets the composite size immediately.
     */
    private void addValueLine( ModSpec modSpec, int index, boolean updateSize )
    {
        ValueLine[] valueLines = modSpec.valueLineList.toArray( new ValueLine[modSpec.valueLineList.size()] );

        ValueLine newValueLine = createValueLine( modSpec );
        modSpec.valueLineList.add( newValueLine );

        if ( valueLines.length > 0 )
        {
            for ( int i = index; i < valueLines.length; i++ )
            {
                ValueLine valueLine = valueLines[i];

                // That's a trick to relocate the value line
                // beneath the newly created one
                Composite parentComposite = valueLine.valueComposite.getParent();
                valueLine.valueComposite.setParent( scrolledComposite );
                valueLine.valueComposite.setParent( parentComposite );

                // Same trick to update the id in the list
                modSpec.valueLineList.remove( valueLine );
                modSpec.valueLineList.add( valueLine );
            }
        }

        if ( updateSize )
        {
            composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
    }


    // ── MON MOTHMA CREATES A SINGLE VALUE ROW ─────────────────────────────────────
    // Each value line is a text input flanked by + and - buttons — simple, consistent.
    // Mon Mothma stamps out the row, wires the buttons, and returns it ready for use.
    // We build the SWT composite, text, and buttons, and attach the shared modifyListener.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single ValueLine inside the given ModSpec's group — one text field for a
     * value, plus +/- buttons to add/remove value lines.
     * All three widgets are wrapped in a small composite and listeners are wired so every
     * change to the text triggers {@link #validate(boolean)}.
     *
     * <p>For example — Mon Mothma stamps out a value row:</p>
     * <pre>
     *   [ leia@endor.gov              ] [ + ] [ − ]
     * </pre>
     *
     * @param modSpec  The modification spec this value line belongs to; needed by the +/- button
     *                 listeners to add/delete from the correct value list.
     * @return         The fully initialized {@link ValueLine} ready to be inserted into the composite.
     */
    private ValueLine createValueLine( final ModSpec modSpec )
    {
        final ValueLine valueLine = new ValueLine();

        // text field
        valueLine.valueComposite = BaseWidgetUtils.createColumnContainer( modSpec.modGroup, 3, 1 );
        valueLine.valueText = BaseWidgetUtils.createText( valueLine.valueComposite, "", 1 ); //$NON-NLS-1$
        valueLine.valueText.addModifyListener( modifyListener );

        // add button with listener
        valueLine.valueAddButton = new Button( valueLine.valueComposite, SWT.PUSH );
        valueLine.valueAddButton.setText( "  +   " ); //$NON-NLS-1$
        valueLine.valueAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = modSpec.valueLineList.size();
                for ( int i = 0; i < modSpec.valueLineList.size(); i++ )
                {
                    ValueLine valueLine = modSpec.valueLineList.get( i );
                    if ( valueLine.valueAddButton == e.widget )
                    {
                        index = i + 1;
                    }
                }

                addValueLine( modSpec, index, true );

                validate( true );
            }
        } );

        // delete button with listener
        valueLine.valueDeleteButton = new Button( valueLine.valueComposite, SWT.PUSH );
        valueLine.valueDeleteButton.setText( "  −  " ); //$NON-NLS-1$
        valueLine.valueDeleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                int index = 0;
                for ( int i = 0; i < modSpec.valueLineList.size(); i++ )
                {
                    ValueLine valueLine = modSpec.valueLineList.get( i );
                    if ( valueLine.valueDeleteButton == e.widget )
                    {
                        index = i;
                    }
                }

                deleteValueLine( modSpec, index );

                validate( true );
            }
        } );

        return valueLine;
    }


    // ── MON MOTHMA REMOVES A VALUE FROM AN OPERATION ─────────────────────────────
    // A commander reports that one of the target values is no longer needed.
    // Mon Mothma crosses it off the operation card and recomputes the available space.
    // We dispose the value line's composite and resize the scrolled content area.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the ValueLine at the given index from the ModSpec's value list, disposes its
     * SWT composite, and recomputes the inner composite size.
     *
     * <p>For example — Mon Mothma crosses off a value:</p>
     * <pre>
     *   modSpec.valueLineList.remove(index).valueComposite.dispose();
     *   composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
     * </pre>
     *
     * @param modSpec  The modification spec whose value list we're editing.
     * @param index    The 0-based index of the ValueLine to remove.
     */
    private void deleteValueLine( ModSpec modSpec, int index )
    {
        ValueLine valueLine = ( ValueLine ) modSpec.valueLineList.remove( index );
        if ( valueLine != null )
        {
            valueLine.valueComposite.dispose();

            composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
    }


    // ── MON MOTHMA TRANSMITS THE COMPLETE BATTLE ORDERS ──────────────────────────
    // The plan is finalized — Mon Mothma reads each operation block in sequence and
    // transcribes them into the standard LDIF protocol format for transmission.
    // We iterate modSpecList and build a changetype: modify LDIF fragment.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the LDIF fragment representing all current modification specs.
     * The fragment starts with {@code changetype: modify} and then lists each ModSpec as:
     * <pre>
     *   add: mail
     *   mail: leia@endor.gov
     *   -
     * </pre>
     * Value lines with empty text are silently skipped. The result is ready to be appended
     * to an LDIF entry record and sent to the LDAP server.
     *
     * <p>For example — Mon Mothma transmits the orders:</p>
     * <pre>
     *   "changetype: modify\n"
     *   "add: mail\nmail: leia@endor.gov\n-\n"
     *   "replace: description\ndescription: Rebel Leader\n-\n"
     * </pre>
     *
     * @return  The complete LDIF modify fragment as a String; never {@code null} but may be
     *          just the {@code changetype: modify} header if all specs are empty.
     */
    public String getLdifFragment()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "changetype: modify" ).append( BrowserCoreConstants.LINE_SEPARATOR ); //$NON-NLS-1$

        ModSpec[] modSpecs = ( ModSpec[] ) modSpecList.toArray( new ModSpec[modSpecList.size()] );

        if ( modSpecs.length > 0 )
        {
            for ( int i = 0; i < modSpecs.length; i++ )
            {
                ModSpec modSpec = modSpecs[i];

                // get values
                String type = modSpec.modType.getText();
                String attribute = modSpec.modAttributeCombo.getText();
                String[] values = new String[modSpec.valueLineList.size()];
                for ( int k = 0; k < values.length; k++ )
                {
                    values[k] = ( ( ValueLine ) modSpec.valueLineList.get( k ) ).valueText.getText();
                }

                // build ldif
                sb.append( type ).append( ": " ).append( attribute ).append( BrowserCoreConstants.LINE_SEPARATOR ); //$NON-NLS-1$
                for ( int k = 0; k < values.length; k++ )
                {
                    if ( values[k].length() > 0 )
                    {
                        sb.append( attribute ).append( ": " ).append( values[k] ).append( //$NON-NLS-1$
                            BrowserCoreConstants.LINE_SEPARATOR );
                    }
                }
                sb.append( "-" ).append( BrowserCoreConstants.LINE_SEPARATOR ); //$NON-NLS-1$
                // sb.append(BrowserCoreConstants.NEWLINE);
            }
        }

        return sb.toString();
    }

    /**
     * The Class ModSpec is a wrapper for all input elements
     * of an modification. It contains a combo for the modify
     * operation, a combo for the attribute to modify,
     * value lines and + and - buttons to add and remove
     * other modifications. It looks like this:
     * <pre>
     * ----------------------------------
     * | operation v | attribute type v |--------
     * ------------------------ --------| + | - |
     * | value                  | + | - |--------
     * ----------------------------------
     * </pre>
     */
    private class ModSpec
    {
        /** The mod group. */
        private Group modGroup;

        /** The mod type. */
        private Combo modType;

        /** The modification attribute. */
        private Combo modAttributeCombo;

        /** The mod add button. */
        private Button modAddButton;

        /** The mod delete button. */
        private Button modDeleteButton;

        /** The value line list. */
        private ArrayList<ValueLine> valueLineList = new ArrayList<ValueLine>();;
    }

    /**
     * The Class ValueLine is a wrapper for all input elements
     * of an value line. It contains an input field for the value
     * and + and - buttons to add and remove other value lines.
     * It looks like this:
     * <pre>
     * -------------------------------------
     * | value                     | + | - |
     * -------------------------------------
     * </pre>
     */
    private class ValueLine
    {
        /** The value composite. */
        private Composite valueComposite;

        /** The value text. */
        private Text valueText;

        /** The value add button. */
        private Button valueAddButton;

        /** The value delete button. */
        private Button valueDeleteButton;
    }
}
