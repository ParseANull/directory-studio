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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: EntryPropertyPage — LUKE'S BINARY SUNSET ON TATOOINE ───────────────
// Luke stands at his viewpoint and sees the full panorama: the DN of the planet
// (where it sits in the galaxy), the URL you'd use to navigate there, who created
// it and when, who last modified it and when, how big it is, how many children
// it has, and how many attributes and values it carries.
// This property page is that same panorama for an LDAP entry — the complete
// overview, with refresh buttons so Luke can re-check conditions that might have
// changed since the page was first opened.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse property page displaying all metadata for a selected LDAP entry.
 * Shows DN, LDAP URL, operational timestamps (createTimestamp, creatorsName,
 * modifyTimestamp, modifiersName), and sizing statistics (byte size, child count,
 * attribute count, value count).
 * Two refresh buttons let the user reload operational attributes or the full
 * entry on demand without closing the page.
 * Think of this page as Luke's binary sunset — the complete entry panorama laid
 * out for inspection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

    /** The dn text. */
    private Text dnText;

    /** The url text. */
    private Text urlText;

    /** The ct text. */
    private Text ctText;

    /** The cn text. */
    private Text cnText;

    /** The mt text. */
    private Text mtText;

    /** The mn text. */
    private Text mnText;

    /** The reload cmi button. */
    private Button reloadCmiButton;

    /** The size text. */
    private Text sizeText;

    /** The children text. */
    private Text childrenText;

    /** The attributes text. */
    private Text attributesText;

    /** The values text. */
    private Text valuesText;

    /** The include operational attributes button. */
    private Button includeOperationalAttributesButton;

    /** The reload entry button. */
    private Button reloadEntryButton;


    // ── LUKE STEPS UP TO THE VIEWPOINT ────────────────────────────────────────
    // Luke walks out and claims his spot — no Apply, no Defaults buttons clutter
    // his view.  He's here to see, not to configure.
    // This is a read-only information page; we hide the default and apply buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the property page and suppresses the Default and Apply buttons.
     * The page is informational; entry metadata is not something the user edits here.
     *
     * <p>For example — Luke arrives without tools, just his eyes:</p>
     * <pre>
     *   noDefaultAndApplyButton() → clean read-only view
     * </pre>
     */
    public EntryPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── LUKE TAKES IN THE FULL ENTRY PANORAMA ─────────────────────────────────
    // Luke's viewpoint has two panels: "Create/Modify Information" (who made this
    // and when, with a refresh button for operational attributes) and "Sizing
    // Information" (how big is it, how many children, with a full reload button).
    // We build the same two-panel layout here, wired to entryUpdated() which fills
    // every field from the live entry data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the property page UI: a header section (DN, URL), a
     * Create/Modify Information group (four operational attribute fields plus a
     * "Refresh" button for operational attributes), and a Sizing Information group
     * (size, children, attributes, values, include-operational checkbox, and a full
     * reload button).
     * Calls {@link #entryUpdated(IEntry)} at the end to populate all fields.
     *
     * <p>For example — Luke's panoramic viewpoint layout:</p>
     * <pre>
     *   DN:  uid=jedi,ou=people,dc=force,dc=com
     *   URL: ldap://yoda.force.com/uid=jedi,…
     *   Create/Modify group:
     *     Create Timestamp: 2025-05-04T00:00:00Z
     *     Creators Name: cn=admin,dc=force,dc=com
     *     ...  [Refresh]
     *   Sizing group:
     *     Entry Size: 1.2 kB, Children: 3, Attributes: 12, Values: 18
     *     [✓] Include operational attributes  [Refresh]
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's property dialog.
     * @return        The composite we built.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Composite mainGroup = BaseWidgetUtils.createColumnContainer( BaseWidgetUtils.createColumnContainer( composite,
            1, 1 ), 2, 1 );
        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "EntryPropertyPage.DN" ), 1 ); //$NON-NLS-1$
        dnText = BaseWidgetUtils.createWrappedLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$
        GridData dnTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        dnTextGridData.widthHint = 300;
        dnText.setLayoutData( dnTextGridData );

        BaseWidgetUtils.createLabel( mainGroup, Messages.getString( "EntryPropertyPage.URL" ), 1 ); //$NON-NLS-1$
        urlText = BaseWidgetUtils.createWrappedLabeledText( mainGroup, "", 1 ); //$NON-NLS-1$
        GridData urlTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        urlTextGridData.widthHint = 300;
        urlText.setLayoutData( urlTextGridData );

        Group cmiGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "EntryPropertyPage.CreateModifyinformation" ), 1 ); //$NON-NLS-1$
        Composite cmiComposite = BaseWidgetUtils.createColumnContainer( cmiGroup, 3, 1 );

        BaseWidgetUtils.createLabel( cmiComposite, Messages.getString( "EntryPropertyPage.CreateTimestamp" ), 1 ); //$NON-NLS-1$
        ctText = BaseWidgetUtils.createLabeledText( cmiComposite, "", 2 ); //$NON-NLS-1$
        GridData ctTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        ctTextGridData.widthHint = 300;
        ctText.setLayoutData( ctTextGridData );

        BaseWidgetUtils.createLabel( cmiComposite, Messages.getString( "EntryPropertyPage.CreatorsName" ), 1 ); //$NON-NLS-1$
        cnText = BaseWidgetUtils.createLabeledText( cmiComposite, "", 2 ); //$NON-NLS-1$
        GridData cnTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        cnTextGridData.widthHint = 300;
        cnText.setLayoutData( cnTextGridData );

        BaseWidgetUtils.createLabel( cmiComposite, Messages.getString( "EntryPropertyPage.ModifyTimestamp" ), 1 ); //$NON-NLS-1$
        mtText = BaseWidgetUtils.createLabeledText( cmiComposite, "", 2 ); //$NON-NLS-1$
        GridData mtTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        mtTextGridData.widthHint = 300;
        mtText.setLayoutData( mtTextGridData );

        BaseWidgetUtils.createLabel( cmiComposite, Messages.getString( "EntryPropertyPage.ModifiersName" ), 1 ); //$NON-NLS-1$
        mnText = BaseWidgetUtils.createLabeledText( cmiComposite, "", 1 ); //$NON-NLS-1$
        GridData mnTextGridData = new GridData( GridData.FILL_HORIZONTAL );
        mnTextGridData.widthHint = 300;
        mnText.setLayoutData( mnTextGridData );

        reloadCmiButton = BaseWidgetUtils.createButton( cmiComposite, "", 1 ); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        gd.horizontalAlignment = SWT.RIGHT;
        reloadCmiButton.setLayoutData( gd );
        reloadCmiButton.addSelectionListener( new SelectionListener()
        {
            public void widgetSelected( SelectionEvent e )
            {
                reloadOperationalAttributes();
            }


            public void widgetDefaultSelected( SelectionEvent e )
            {
            }
        } );

        Group sizingGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "EntryPropertyPage.SizingInformation" ), 1 ); //$NON-NLS-1$
        Composite sizingComposite = BaseWidgetUtils.createColumnContainer( sizingGroup, 3, 1 );

        BaseWidgetUtils.createLabel( sizingComposite, Messages.getString( "EntryPropertyPage.EntrySize" ), 1 ); //$NON-NLS-1$
        sizeText = BaseWidgetUtils.createLabeledText( sizingComposite, "", 2 ); //$NON-NLS-1$
        GridData sizeTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        sizeTextGridData.widthHint = 300;
        sizeText.setLayoutData( sizeTextGridData );

        BaseWidgetUtils.createLabel( sizingComposite, Messages.getString( "EntryPropertyPage.NumberOfChildren" ), 1 ); //$NON-NLS-1$
        childrenText = BaseWidgetUtils.createLabeledText( sizingComposite, "", 2 ); //$NON-NLS-1$
        GridData childrenTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        childrenTextGridData.widthHint = 300;
        childrenText.setLayoutData( childrenTextGridData );

        BaseWidgetUtils.createLabel( sizingComposite, Messages.getString( "EntryPropertyPage.NumberOfAttributes" ), 1 ); //$NON-NLS-1$
        attributesText = BaseWidgetUtils.createLabeledText( sizingComposite, "", 2 ); //$NON-NLS-1$
        GridData attributesTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        attributesTextGridData.widthHint = 300;
        attributesText.setLayoutData( attributesTextGridData );

        BaseWidgetUtils.createLabel( sizingComposite, Messages.getString( "EntryPropertyPage.NumberOfValues" ), 1 ); //$NON-NLS-1$
        valuesText = BaseWidgetUtils.createLabeledText( sizingComposite, "", 2 ); //$NON-NLS-1$
        GridData valuesTextGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        valuesTextGridData.widthHint = 300;
        valuesText.setLayoutData( valuesTextGridData );

        includeOperationalAttributesButton = BaseWidgetUtils.createCheckbox( sizingComposite, Messages
            .getString( "EntryPropertyPage.IncludeoperationalAttributes" ), 2 ); //$NON-NLS-1$
        includeOperationalAttributesButton.addSelectionListener( new SelectionListener()
        {
            public void widgetSelected( SelectionEvent e )
            {
                entryUpdated( getEntry( getElement() ) );
            }


            public void widgetDefaultSelected( SelectionEvent e )
            {
            }
        } );

        reloadEntryButton = BaseWidgetUtils.createButton( sizingComposite, "", 1 ); //$NON-NLS-1$
        gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        gd.horizontalAlignment = SWT.RIGHT;
        reloadEntryButton.setLayoutData( gd );
        reloadEntryButton.addSelectionListener( new SelectionListener()
        {
            public void widgetSelected( SelectionEvent e )
            {
                reloadEntry();
            }


            public void widgetDefaultSelected( SelectionEvent e )
            {
            }
        } );

        entryUpdated( getEntry( getElement() ) );

        return composite;
    }


    // ── LUKE REFOCUSES ON THE OPERATIONAL DETAILS ─────────────────────────────
    // Luke squints at the horizon to get operational details — how long ago did
    // the sun rise (createTimestamp), who lit it (creatorsName)?  He refreshes
    // his view by marking that he wants the full operational data and re-fetching
    // from the server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the entry's operational attributes from the LDAP server and refreshes
     * the Create/Modify Information group's text fields.
     * Triggered by the "Refresh" button in the CMI group.
     * Sets {@code initOperationalAttributes = true} on the entry so the server
     * includes them in the fetch.
     *
     * <p>For example — Luke re-checks the operational conditions at the horizon:</p>
     * <pre>
     *   entry.setInitOperationalAttributes(true) →
     *   InitializeAttributesRunnable runs → creatorsName, modifyTimestamp updated
     * </pre>
     */
    private void reloadOperationalAttributes()
    {
        IEntry entry = EntryPropertyPage.getEntry( getElement() );
        entry.setInitOperationalAttributes( true );
        InitializeAttributesRunnable runnable = new InitializeAttributesRunnable( entry );
        RunnableContextRunner.execute( runnable, null, true );
        entryUpdated( entry );
    }


    // ── LUKE TAKES A FRESH LOOK AT THE WHOLE LANDSCAPE ────────────────────────
    // Sometimes the scene has changed since Luke first stepped up to the viewpoint
    // — new entries added, children reorganized.  He reloads the full landscape:
    // children list first, then all attributes, so the sizing numbers are accurate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the entry's full attribute set (including operational) and its
     * children list from the LDAP server, then refreshes all sizing fields.
     * Triggered by the "Refresh" button in the Sizing Information group.
     * Runs two runnables sequentially: first children, then attributes.
     *
     * <p>For example — Luke takes a completely fresh look at the landscape:</p>
     * <pre>
     *   InitializeChildrenRunnable runs → child count updated
     *   InitializeAttributesRunnable runs → attribute/value counts updated
     *   entryUpdated(entry) → all fields repainted
     * </pre>
     */
    private void reloadEntry()
    {
        IEntry entry = EntryPropertyPage.getEntry( getElement() );
        entry.setInitOperationalAttributes( true );
        InitializeChildrenRunnable runnable1 = new InitializeChildrenRunnable( false, entry );
        InitializeAttributesRunnable runnable2 = new InitializeAttributesRunnable( entry );
        RunnableContextRunner.execute( runnable1, null, true );
        RunnableContextRunner.execute( runnable2, null, true );
        entryUpdated( entry );
    }


    // ── LUKE IDENTIFIES WHICH PLANET HE'S LOOKING AT ─────────────────────────
    // Luke knows he's on Tatooine but needs to extract the formal address — the
    // DN — from whatever Eclipse hands him as the selection element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the {@link IEntry} from the given Eclipse selection element using
     * the {@link IAdaptable} adapter mechanism.
     * Returns {@code null} if the element doesn't adapt to {@link IEntry}.
     * This is package-visible (not private) because {@link SchemaPropertyPage} and
     * {@link RootDSEPropertyPage} reuse it to get the connection.
     *
     * <p>For example — Luke identifies his coordinates:</p>
     * <pre>
     *   element.getAdapter(IEntry.class) → IEntry(uid=jedi,…) → page populated
     * </pre>
     *
     * @param element  The Eclipse selection element; typically an {@link IAdaptable}.
     * @return         The {@link IEntry} for the selected LDAP entry, or {@code null}.
     */
    static IEntry getEntry( Object element )
    {
        IEntry entry = null;
        if ( element instanceof IAdaptable )
        {
            entry = ( IEntry ) ( ( IAdaptable ) element ).getAdapter( IEntry.class );
        }
        return entry;
    }


    // ── LUKE CHECKS IF THE VIEWING SPOT IS STILL ACCESSIBLE ──────────────────
    // If a dust storm rolled in and obscured the viewpoint while Luke was away,
    // the sunset widget would be disposed and unusable.  We check this before
    // anyone tries to repaint the screen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the page's DN text widget has been disposed,
     * which happens when the property dialog is closed.
     * Used by callers (like EntryPropertyPageUniversalListener) to know whether
     * they can still push updates into this page.
     *
     * <p>For example — Luke checks if the viewpoint is still clear:</p>
     * <pre>
     *   dialog closed → dnText disposed → isDisposed() = true → no more updates
     * </pre>
     *
     * @return  {@code true} if the root text widget is disposed.
     */
    public boolean isDisposed()
    {
        return this.dnText.isDisposed();
    }


    // ── LUKE EXTRACTS A CLEAR READING FROM THE DATA ───────────────────────────
    // Luke can read both suns' colors even when the atmosphere distorts things;
    // if an attribute is null (the data hasn't been fetched yet) he substitutes
    // a dash rather than leaving the field blank or throwing an NPE.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string value of an attribute, or {@code "-"} if the attribute is
     * null (meaning it hasn't been fetched from the server yet).
     * Used for the operational attribute fields where the server may not have
     * returned createTimestamp etc.
     *
     * <p>For example — Luke reports a sun reading even when partially obscured:</p>
     * <pre>
     *   attribute null (not fetched) → "-"
     *   attribute present → attribute.getStringValue()
     * </pre>
     *
     * @param att  The attribute to read, may be {@code null}.
     * @return     The attribute's string value, or {@code "-"} if null.
     */
    private String getNonNullStringValue( IAttribute att )
    {
        String value = null;
        if ( att != null )
        {
            value = att.getStringValue();
        }
        return value != null ? value : "-"; //$NON-NLS-1$
    }


    // ── LUKE REFRESHES THE FULL PANORAMIC VIEW ────────────────────────────────
    // The landscape updates — a new child settlement appeared, the size changed —
    // so Luke's viewpoint needs repainting.  We repopulate every text field
    // from the entry's current state, counting attributes and values on the fly
    // (optionally including operational attributes based on the checkbox).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Repopulates all text fields in this page from the current state of the
     * given entry.
     * Skips the update silently if the DN text widget is already disposed (the
     * dialog was closed).
     * Counts attributes, values, and bytes dynamically; optionally includes
     * operational attributes in the count based on the checkbox state.
     *
     * <p>For example — Luke repaints the panorama after the landscape changes:</p>
     * <pre>
     *   entry updated → dnText, urlText, ctText, cnText, mtText, mnText refreshed
     *   attribute/value/size counts recalculated
     *   childrenText shows "5+" if there are more children than fetched
     * </pre>
     *
     * @param entry  The LDAP entry to read current data from; must not be null.
     */
    private void entryUpdated( IEntry entry )
    {

        if ( !this.dnText.isDisposed() )
        {
            setMessage( Messages.getString( "EntryPropertyPage.Entry" ) + entry.getDn().getName() ); //$NON-NLS-1$

            dnText.setText( entry.getDn().getName() );
            urlText.setText( entry.getUrl().toString() );
            ctText.setText( getNonNullStringValue( entry.getAttribute( SchemaConstants.CREATE_TIMESTAMP_AT ) ) );
            cnText.setText( getNonNullStringValue( entry.getAttribute( SchemaConstants.CREATORS_NAME_AT ) ) );
            mtText.setText( getNonNullStringValue( entry.getAttribute( SchemaConstants.MODIFY_TIMESTAMP_AT ) ) );
            mnText.setText( getNonNullStringValue( entry.getAttribute( SchemaConstants.MODIFIERS_NAME_AT ) ) );
            reloadCmiButton.setText( Messages.getString( "EntryPropertyPage.Refresh" ) ); //$NON-NLS-1$

            int attCount = 0;
            int valCount = 0;
            int bytes = 0;

            IAttribute[] allAttributes = entry.getAttributes();
            if ( allAttributes != null )
            {
                for ( int attIndex = 0; attIndex < allAttributes.length; attIndex++ )
                {
                    if ( !allAttributes[attIndex].isOperationalAttribute()
                        || includeOperationalAttributesButton.getSelection() )
                    {
                        attCount++;
                        IValue[] allValues = allAttributes[attIndex].getValues();
                        for ( int valIndex = 0; valIndex < allValues.length; valIndex++ )
                        {
                            if ( !allValues[valIndex].isEmpty() )
                            {
                                valCount++;
                                bytes += allValues[valIndex].getBinaryValue().length;
                            }
                        }
                    }
                }
            }

            reloadEntryButton.setText( Messages.getString( "EntryPropertyPage.Refresh" ) ); //$NON-NLS-1$
            if ( !entry.isChildrenInitialized() )
            {
                childrenText.setText( Messages.getString( "EntryPropertyPage.NotChecked" ) ); //$NON-NLS-1$
            }
            else
            {
                childrenText.setText( ( entry.hasMoreChildren() ? NLS.bind( Messages
                    .getString( "EntryPropertyPage.ChildrenFetched" ), new Object[] { entry.getChildrenCount() } ) //$NON-NLS-1$
                    : Integer.toString( entry.getChildrenCount() ) ) );
            }
            attributesText.setText( "" + attCount ); //$NON-NLS-1$
            valuesText.setText( "" + valCount ); //$NON-NLS-1$
            sizeText.setText( Utils.formatBytes( bytes ) );
        }
    }

}
