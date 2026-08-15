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
import java.util.Collection;

import org.apache.directory.api.ldap.aci.ACIItem;
import org.apache.directory.api.ldap.aci.ACIItemParser;
import org.apache.directory.api.ldap.aci.ItemFirstACIItem;
import org.apache.directory.api.ldap.aci.ItemPermission;
import org.apache.directory.api.ldap.aci.ProtectedItem;
import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.api.ldap.aci.UserFirstACIItem;
import org.apache.directory.api.ldap.aci.UserPermission;
import org.apache.directory.api.ldap.model.constants.AuthenticationLevel;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ACIItemVisualEditorComposite — THE GRAND MOFF'S FULL DIRECTIVE FORM ─
// When the Grand Moff reviews a security directive in full structured form he
// sees a scrolled panel: header fields at the top, then either the userFirst
// block (user-classes + user-permissions) or the itemFirst block
// (protected-items + item-permissions) depending on which radio button he chose.
// Flipping the radio hides one block and shows the other.
// ACIItemVisualEditorComposite is that panel.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main visual editor widget for an ACI item.
 * Extends {@link ScrolledComposite} and implements {@link WidgetModifyListener}
 * so it can react to changes in the general header composite.
 * Manages the lifecycle of the five sub-composites and shows/hides the
 * userFirst or itemFirst sub-composites based on the header selection.
 * Think of this as the Grand Moff's directive form: general header at the top,
 * the appropriate sub-sections revealed below as he picks his form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemVisualEditorComposite extends ScrolledComposite implements WidgetModifyListener
{
    /** The inner composite for all the content */
    private Composite composite = null;

    /** The general composite contains id-tag, precedence, auth-level, userFirst/itemFirst */
    private ACIItemGeneralComposite generalComposite = null;

    /** The user classes composite used for userFirst selection */
    private ACIItemUserClassesComposite userFirstUserClassesComposite = null;

    /** The user permission composite used for userFirst selection */
    private ACIItemUserPermissionsComposite userFirstUserPermissionsComposite = null;

    /** The protected items composite used for itemFirst selection */
    private ACIItemProtectedItemsComposite itemFirstProtectedItemsComposite = null;

    /** The item permission composite used for itemFirst selection */
    private ACIItemItemPermissionsComposite itemFirstItemPermissionsComposite = null;


    // ── CONSTRUCT THE SCROLLED DIRECTIVE FORM ────────────────────────────────
    // The five sub-composites are created inside a scrolled container so the
    // form can grow taller than the dialog without truncating.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemVisualEditorComposite}.
     * Builds the scrolled container and all five sub-composites; the general
     * composite's listener is wired to this instance so header changes trigger
     * the userFirst/itemFirst visibility switch.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemVisualEditorComposite( Composite parent, int style )
    {
        super( parent, style | SWT.H_SCROLL | SWT.V_SCROLL );
        setExpandHorizontal( true );
        setExpandVertical( true );

        createComposite();

        setContent( composite );
        setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }


    // ── BUILD THE FIVE SUB-COMPOSITES ─────────────────────────────────────────
    // The orderly stacks the sub-composites vertically inside the scrolled
    // inner composite and registers this as a listener on the general header
    // so radio-button changes trigger widgetModified().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates the inner composite and all five sub-composites, then
     * triggers an initial visibility update via {@link #widgetModified(WidgetModifyEvent)}.
     */
    private void createComposite()
    {
        composite = new Composite( this, SWT.NONE );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        generalComposite = new ACIItemGeneralComposite( composite, SWT.NONE );
        generalComposite.addWidgetModifyListener( this );

        userFirstUserClassesComposite = new ACIItemUserClassesComposite( composite, SWT.NONE );
        userFirstUserPermissionsComposite = new ACIItemUserPermissionsComposite( composite, SWT.NONE );

        itemFirstProtectedItemsComposite = new ACIItemProtectedItemsComposite( composite, SWT.NONE );
        itemFirstItemPermissionsComposite = new ACIItemItemPermissionsComposite( composite, SWT.NONE );

        widgetModified( null );
    }


    // ── REACT TO HEADER CHANGES ───────────────────────────────────────────────
    // When the officer flips the userFirst/itemFirst radio the general composite
    // fires this listener; we show the right pair of sub-composites and hide
    // the other pair, then re-compute the minimum scroll size.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the general header composite whenever the identification tag,
     * precedence, authentication level, or userFirst/itemFirst radio changes.
     * Shows the appropriate sub-composites and hides the others.
     *
     * @param event  the modify event (may be {@code null} on initial call)
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        // switch userFirst / itemFirst
        if ( generalComposite.isItemFirst() && !generalComposite.isUserFirst()
            && !itemFirstProtectedItemsComposite.isVisible() )
        {
            userFirstUserClassesComposite.setVisible( false );
            userFirstUserPermissionsComposite.setVisible( false );
            itemFirstProtectedItemsComposite.setVisible( true );
            itemFirstItemPermissionsComposite.setVisible( true );

            setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            layout( true, true );
        }
        else if ( generalComposite.isUserFirst() && !generalComposite.isItemFirst()
            && !userFirstUserClassesComposite.isVisible() )
        {
            userFirstUserClassesComposite.setVisible( true );
            userFirstUserPermissionsComposite.setVisible( true );
            itemFirstProtectedItemsComposite.setVisible( false );
            itemFirstItemPermissionsComposite.setVisible( false );

            setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            layout( true, true );
        }
        else if ( !generalComposite.isItemFirst() && !generalComposite.isUserFirst() )
        {
            userFirstUserClassesComposite.setVisible( false );
            userFirstUserPermissionsComposite.setVisible( false );
            itemFirstProtectedItemsComposite.setVisible( false );
            itemFirstItemPermissionsComposite.setVisible( false );

            setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            layout( true, true );
        }

    }


    // ── POPULATE FROM AN ACI STRING ───────────────────────────────────────────
    // Parse the ACI string and distribute the parsed values to each sub-composite.
    // The visibility switch fires automatically via widgetModified().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses {@code input} as an ACI item and populates all sub-composites.
     * Triggers a visibility update after population.
     *
     * <p>For example — the tab folder switches to the visual tab:</p>
     * <pre>
     *   visualComposite.setInput(sourceComposite.getInput());
     *   // → all five sub-composites are populated from the parsed ACI item
     * </pre>
     *
     * @param input  the ACI string to parse and display
     * @throws ParseException  if {@code input} is not valid ACI syntax
     */
    public void setInput( String input ) throws ParseException
    {
        ACIItemParser parser = new ACIItemParser( null );
        ACIItem aciItem = parser.parse( input );

        if ( aciItem != null )
        {
            generalComposite.setIdentificationTag( aciItem.getIdentificationTag() );
            generalComposite.setPrecedence( aciItem.getPrecedence() );
            generalComposite.setAuthenticationLevel( aciItem.getAuthenticationLevel() );

            if ( aciItem instanceof ItemFirstACIItem )
            {
                ItemFirstACIItem itemFirstACI = ( ItemFirstACIItem ) aciItem;
                generalComposite.setItemFirst();
                itemFirstProtectedItemsComposite.setProtectedItems( itemFirstACI.getProtectedItems() );
                itemFirstItemPermissionsComposite.setItemPermissions( itemFirstACI.getItemPermissions() );
            }
            else if ( aciItem instanceof UserFirstACIItem )
            {
                UserFirstACIItem userFirstACI = ( UserFirstACIItem ) aciItem;
                generalComposite.setUserFirst();
                userFirstUserClassesComposite.setUserClasses( userFirstACI.getUserClasses() );
                userFirstUserPermissionsComposite.setUserPermissions( userFirstACI.getUserPermission() );
            }
        }

        // force userFirst/itemFirst switch
        widgetModified( null );

    }


    // ── SERIALISE TO ACI STRING ───────────────────────────────────────────────
    // Collect the current values from all sub-composites and assemble an
    // ACIItem object, then return its toString() as the normalised ACI string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects values from all sub-composites and constructs the corresponding
     * {@link ACIItem} object, then returns its {@code toString()} representation.
     *
     * @return the ACI string assembled from the current widget state
     * @throws ParseException  if the assembled state is not representable as a
     *                         valid ACI item (no userFirst or itemFirst selected)
     */
    public String getInput() throws ParseException
    {
        String identificationTag = generalComposite.getIdentificationTag();
        int precedence = generalComposite.getPrecedence();
        AuthenticationLevel authenticationLevel = generalComposite.getAuthenticationLevel();

        ACIItem aciItem = null;
        if ( generalComposite.isUserFirst() )
        {
            Collection<UserClass> userClasses = userFirstUserClassesComposite.getUserClasses();
            Collection<UserPermission> userPermissions = userFirstUserPermissionsComposite.getUserPermissions();
            aciItem = new UserFirstACIItem( identificationTag, precedence, authenticationLevel, userClasses,
                userPermissions );
        }
        else if ( generalComposite.isItemFirst() )
        {
            Collection<ProtectedItem> protectedItems = itemFirstProtectedItemsComposite.getProtectedItems();
            Collection<ItemPermission> itemPermissions = itemFirstItemPermissionsComposite.getItemPermissions();
            aciItem = new ItemFirstACIItem( identificationTag, precedence, authenticationLevel, protectedItems,
                itemPermissions );
        }
        else
        {
            aciItem = null;
        }

        String aci = ""; //$NON-NLS-1$
        if ( aciItem != null )
        {
            aci = aciItem.toString();
        }
        return aci;
    }


    // ── INJECT THE CONNECTION CONTEXT ─────────────────────────────────────────
    // The context is forwarded to the four sub-composites that need it for
    // schema-driven content assist in their value editors.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Passes the connection context to the four sub-composites that need schema
     * access for their embedded value editors.
     *
     * @param context  the value context carrying connection and entry information
     */
    public void setContext( ACIItemValueWithContext context )
    {
        itemFirstProtectedItemsComposite.setContext( context );
        itemFirstItemPermissionsComposite.setContext( context );
        userFirstUserClassesComposite.setContext( context );
        userFirstUserPermissionsComposite.setContext( context );
    }

}
