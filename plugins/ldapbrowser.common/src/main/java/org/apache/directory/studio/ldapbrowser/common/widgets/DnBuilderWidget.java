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
import java.util.Iterator;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.DnUtils;
import org.apache.directory.studio.connection.ui.widgets.ExtendedContentAssistCommandAdapter;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: DnBuilderWidget — LANDO RUNNING CLOUD CITY ────────────────────────
// Lando manages Cloud City with its floating platforms, rotating staff, and dynamic
// departments — each one can be added, reorganized, or removed as the operation grows.
// DnBuilderWidget does the same for LDAP DNs: a dynamic list of RDN lines you can
// add or delete on the fly, plus a parent-DN field that anchors where in the tree we live.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A composite widget for constructing a full LDAP Distinguished Name (DN) from its parts.
 * A DN looks like {@code cn=John,ou=People,dc=example,dc=com} — it's the unique address
 * of an entry in the LDAP directory tree. This widget lets you specify:
 * <ul>
 *   <li>The <em>parent DN</em> — where in the tree the new entry will live.</li>
 *   <li>The <em>RDN</em> (Relative Distinguished Name) — the name of the entry itself,
 *       which can be multi-valued (e.g. {@code cn=John+uid=jdoe}).</li>
 * </ul>
 * Think of this class as Lando running Cloud City: he manages a dynamic set of departments
 * (RDN lines) that can grow or shrink, watches for changes, and always keeps the overall
 * address of the city (the preview DN) up to date.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnBuilderWidget extends AbstractWidget implements ModifyListener
{

    /** The attribute names that could be selected from drop-down list. */
    private String[] attributeNames;

    /** True if the Rdn input elements should be shown. */
    private boolean showRDN;

    /** True if the parent Dn input elements should be shown. */
    private boolean showParent;

    /** The shell. */
    private Shell shell;

    /** The selected parent Dn. */
    private Dn parentDn;

    /** The entry widget label. */
    private Label parentEntryLabel;

    /** The entry widget to enter/select the parent Dn. */
    private EntryWidget parentEntryWidget;

    /** The Rdn label */
    private Label rdnLabel;

    /** The composite that contains the RdnLines. */
    private Composite rdnComposite;

    /** The resulting Rdn. */
    private Rdn rdn;

    /** The list of RdnLines. */
    private ArrayList<RdnLine> rdnLineList;

    /** The preview label. */
    private Label previewLabel;

    /** The preview text. */
    private Text previewText;

    // Listeners
    private SelectionListener rdnAddButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            int index = rdnLineList.size();
            for ( int i = 0; i < rdnLineList.size(); i++ )
            {
                RdnLine rdnLine = rdnLineList.get( i );
                if ( rdnLine.rdnAddButton == e.widget )
                {
                    index = i + 1;
                }
            }
            addRdnLine( rdnComposite, index );

            validate();
        }
    };

    private SelectionListener rdnDeleteButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            int index = 0;
            for ( int i = 0; i < rdnLineList.size(); i++ )
            {
                RdnLine rdnLine = rdnLineList.get( i );
                if ( rdnLine.rdnDeleteButton == e.widget )
                {
                    index = i;
                }
            }
            deleteRdnLine( rdnComposite, index );

            validate();
        }
    };


    // ── LANDO SETS UP THE CITY'S CONTROL CENTRE ──────────────────────────────────
    // Lando is handed the keys to Cloud City and told which sections to show the visitors.
    // He decides upfront: do guests see the RDN naming section? The parent-location section?
    // We store those two flags so createContents() knows what to render.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a DnBuilderWidget and configures which sections to display.
     * Both sections are optional: you might only need to pick a parent (for moving an entry),
     * or only need to define the RDN (for renaming), or both (for creating a new entry).
     *
     * <p>For example — Lando briefing his staff:</p>
     * <pre>
     *   "Show them the naming section? Yes."
     *   "Show them where the city is anchored in the galaxy? Yes."
     *   // Both sections rendered when createContents() is called.
     * </pre>
     *
     * @param showRDN     When {@code true}, the RDN input section (attribute type + value rows) is rendered.
     * @param showParent  When {@code true}, the parent-DN picker (EntryWidget) is rendered.
     */
    public DnBuilderWidget( boolean showRDN, boolean showParent )
    {
        this.showRDN = showRDN;
        this.showParent = showParent;
    }


    // ── LANDO CLOSES THE OFFICE ───────────────────────────────────────────────────
    // Lando hands back the keys and steps away — Cloud City doesn't need tearing down,
    // just an acknowledgement that we're done here.
    // This method is a lifecycle hook from AbstractWidget; nothing to clean up right now.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this widget and releases any held resources.
     * Currently a no-op — the SWT widget tree handles its own disposal — but the
     * method exists as a required lifecycle hook for the {@link AbstractWidget} contract.
     *
     * <p>For example — Lando steps away:</p>
     * <pre>
     *   "Nothing left to clean up. The city runs itself."
     * </pre>
     */
    public void dispose()
    {
    }


    // ── LANDO BRIEFS HIS STAFF ON THE CURRENT SITUATION ──────────────────────────
    // Lando gathers his department heads and updates them: "Here's the new roster,
    // here's where we sit in the galaxy, and here's our current name structure."
    // We populate all the RDN lines and parent-DN widget with the given initial values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the widget's fields with an initial state.
     * Clears and rebuilds all RDN lines from the given {@code rdn}, updates the parent-DN
     * picker, and refreshes the autocomplete lists with the provided attribute names.
     * Call this after {@link #createContents(Composite)} to seed the form.
     *
     * <p>For example — Lando updates the city manifest:</p>
     * <pre>
     *   "New department names from the schema: cn, sn, uid."
     *   "Current RDN: cn=Lando, uid=lando  — set those fields."
     *   "Parent location: ou=People,dc=cloud,dc=city — set the picker."
     * </pre>
     *
     * @param browserConnection  The active LDAP connection, used by the EntryWidget parent picker.
     * @param attributeNames     The attribute type names available in the drop-down combos.
     * @param rdn                The initial RDN to populate the RDN lines with; may be {@code null} or empty.
     * @param parentDn           The initial parent DN to show in the parent-entry picker; may be {@code null}.
     */
    public void setInput( IBrowserConnection browserConnection, String[] attributeNames, Rdn rdn, Dn parentDn )
    {
        this.attributeNames = attributeNames;
        Rdn currentRdn = rdn;
        Dn currentParentDn = parentDn;

        if ( showRDN )
        {
            for ( int i = 0; i < rdnLineList.size(); i++ )
            {
                RdnLine rdnLine = rdnLineList.get( i );
                String oldName = rdnLine.rdnTypeCombo.getText();
                rdnLine.rdnTypeCombo.setItems( attributeNames );
                rdnLine.rdnNameCPA.setContentProposalProvider( new ListContentProposalProvider( attributeNames ) );
                if ( Arrays.asList( rdnLine.rdnTypeCombo.getItems() ).contains( oldName ) )
                {
                    rdnLine.rdnTypeCombo.setText( oldName );
                }
            }
        }

        if ( showRDN )
        {
            while ( !rdnLineList.isEmpty() )
            {
                deleteRdnLine( rdnComposite, 0 );
            }
            if ( currentRdn == null || currentRdn.size() == 0 )
            {
                addRdnLine( rdnComposite, 0 );
                rdnLineList.get( 0 ).rdnTypeCombo.setFocus();
            }
            else
            {
                int i = 0;
                Iterator<Ava> atavIterator = currentRdn.iterator();
                while ( atavIterator.hasNext() )
                {
                    Ava ava = atavIterator.next();
                    addRdnLine( rdnComposite, i );

                    removeRdnLineListeners( i );

                    rdnLineList.get( i ).rdnTypeCombo.setText( ava.getType() );
                    rdnLineList.get( i ).rdnValueText.setText( (String)ava.getValue().getNormalized() );

                    addRdnLineListeners( i );

                    if ( i == 0 )
                    {
                        RdnLine rdnLine = rdnLineList.get( i );

                        if ( rdnLine.rdnTypeCombo != null ) //$NON-NLS-1$
                        {
                            rdnLine.rdnTypeCombo.setFocus();
                        }
                        else
                        {
                            rdnLine.rdnValueText.selectAll();
                            rdnLine.rdnValueText.setFocus();
                        }
                    }
                    i++;
                }
            }
        }

        if ( showParent )
        {
            parentEntryWidget.setInput( browserConnection, currentParentDn );
        }

        validate();
    }


    // ── LANDO READS THE CURRENT NAME PLATE ────────────────────────────────────────
    // A visitor asks: "What's your city's local name?" Lando reads off the RDN sign.
    // He hands back whatever the RDN lines currently spell out — or null if they're broken.
    // We return the validated Rdn object computed during the last validate() call.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently built RDN, or {@code null} if the RDN fields contain invalid input.
     * The RDN is recomputed on every change by {@link #validate()} — this is just a getter
     * for the last computed result.
     *
     * <p>For example — the city's name plate:</p>
     * <pre>
     *   Rdn rdn = widget.getRdn();
     *   // rdn.toString() → "cn=Lando+uid=lando"  (if both fields are filled correctly)
     * </pre>
     *
     * @return  The current {@link Rdn} assembled from the RDN input lines, or {@code null}
     *          if any RDN field is empty or malformed.
     */
    public Rdn getRdn()
    {
        return rdn;
    }


    // ── LANDO READS THE STAR MAP COORDINATES ─────────────────────────────────────
    // The navigator asks: "Where exactly are you in the galaxy?" Lando points to the map.
    // He reads off the parent DN — the containing tree node — or null if it isn't set yet.
    // We return the Dn object for the parent location last computed by validate().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected parent DN, or {@code null} if the parent field is empty
     * or contains an invalid DN string.
     * The parent DN is resolved during {@link #validate()} from the EntryWidget's contents.
     *
     * <p>For example — the galaxy coordinates for Cloud City:</p>
     * <pre>
     *   Dn parent = widget.getParentDn();
     *   // parent.toString() → "ou=Locations,dc=galaxy,dc=far,dc=away"
     * </pre>
     *
     * @return  The current parent {@link Dn}, or {@code null} if not yet set or invalid.
     */
    public Dn getParentDn()
    {
        return parentDn;
    }


    // ── LANDO BUILDS THE CONTROL ROOM ─────────────────────────────────────────────
    // Lando outfits Cloud City's main control room: a screen for the galaxy position (parent DN),
    // a panel of department slots (RDN lines), and a big display showing the full address.
    // We construct the SWT composite tree — labels, entry widget, RDN composite, preview text.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the SWT composite for this widget.
     * Depending on the flags passed to the constructor, this renders:
     * <ul>
     *   <li>A parent-DN picker (label + {@link EntryWidget})</li>
     *   <li>An RDN section (label + dynamic rows of attribute-type/value combos)</li>
     *   <li>A read-only preview text showing the assembled DN or any current error</li>
     * </ul>
     * Call {@link #setInput} after this to populate the fields with initial values.
     *
     * <p>For example — Lando builds the control room:</p>
     * <pre>
     *   "Parent section? Yes — show the galaxy map widget."
     *   "RDN section? Yes — show the department-name rows."
     *   "Preview? Yes — live display of the full address."
     * </pre>
     *
     * @param parent  The SWT parent composite to attach our controls to.
     * @return        The top-level {@link Composite} containing all child controls.
     */
    public Composite createContents( Composite parent )
    {
        this.shell = parent.getShell();

        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // draw parent
        if ( showParent )
        {
            parentEntryLabel = BaseWidgetUtils.createLabel( composite,
                Messages.getString( "DnBuilderWidget.Parent" ), 1 ); //$NON-NLS-1$
            parentEntryWidget = new EntryWidget();
            parentEntryWidget.createWidget( composite );
            parentEntryWidget.addWidgetModifyListener( event -> validate() );

            BaseWidgetUtils.createSpacer( composite, 3 );
        }

        // draw Rdn group
        if ( showRDN )
        {
            rdnLabel = BaseWidgetUtils.createLabel( composite, Messages.getString( "DnBuilderWidget.RDN" ), 1 ); //$NON-NLS-1$
            rdnComposite = BaseWidgetUtils.createColumnContainer( composite, 5, 2 );
            rdnLineList = new ArrayList<>();
            BaseWidgetUtils.createSpacer( composite, 3 );
        }

        // draw dn/rdn preview
        if ( showRDN )
        {
            previewLabel = BaseWidgetUtils.createLabel( composite, showParent ? Messages
                .getString( "DnBuilderWidget.DNPreview" ) : Messages.getString( "DnBuilderWidget.RDNPreview" ), 1 ); //$NON-NLS-1$ //$NON-NLS-2$
            previewText = BaseWidgetUtils.createReadonlyText( composite, "", 2 ); //$NON-NLS-1$
            BaseWidgetUtils.createSpacer( composite, 3 );
        }

        return composite;
    }


    // ── LANDO'S SECRETARY REPORTS A CHANGE ────────────────────────────────────────
    // An aide rushes in: "Something in the RDN or parent field just changed, sir!"
    // Lando simply says, "Run the numbers." — we re-validate everything.
    // This ModifyListener callback triggers validate() whenever any text field changes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by SWT whenever any watched text field changes its content.
     * Simply delegates to {@link #validate()} to recompute the RDN, parent DN, and preview.
     * We implement {@link ModifyListener} so we can attach the same handler to multiple fields.
     *
     * <p>For example — the aide delivers the news:</p>
     * <pre>
     *   "Boss, someone typed 'Lando' in the cn field."
     *   validate();  // Lando reviews the whole city name again.
     * </pre>
     *
     * @param e  The SWT modify event; we don't inspect it — any change triggers a full revalidation.
     */
    public void modifyText( ModifyEvent e )
    {
        validate();
    }


    // ── LANDO SAVES THE CITY'S LOCATION HISTORY ───────────────────────────────────
    // Before closing the control room for the night, Lando tells his assistant to record
    // the recent parent-DN entries so they can be pre-filled next time.
    // We delegate to the EntryWidget to persist its recently-used DN history.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the history of parent-DN entries typed into the EntryWidget so they
     * appear as suggestions in future dialog sessions.
     * This is typically called when the dialog containing this widget is closed successfully.
     *
     * <p>For example — Lando records the session logs:</p>
     * <pre>
     *   "Log today's parent-DN choices for next time."
     *   parentEntryWidget.saveDialogSettings();
     * </pre>
     */
    public void saveDialogSettings()
    {
        if ( parentEntryWidget != null )
        {
            parentEntryWidget.saveDialogSettings();
        }
    }


    // ── LANDO REVIEWS THE FULL CITY STATUS REPORT ─────────────────────────────────
    // Lando pulls all the department heads together and checks the city's state:
    // Is the RDN valid? Is the parent location reachable? Does the full address make sense?
    // He updates the preview display and fires off notifications to anyone watching.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current state of all input fields and refreshes the preview.
     * Reads the RDN lines and parent-DN picker, tries to build an {@link Rdn} and a {@link Dn},
     * and updates the read-only preview text with either the assembled DN string or an error
     * message. Also fires {@code notifyListeners()} so parent dialogs can update their
     * OK-button enabled state.
     *
     * <p>For example — Lando's status review:</p>
     * <pre>
     *   rdn = DnUtils.composeRdn(rdnTypes, rdnValues);  // "cn=Lando+uid=lando"
     *   parentDn = parentEntryWidget.getDn();           // "ou=People,dc=cloud,dc=city"
     *   dn = parentDn.add(rdn);                         // full address assembled
     *   previewText.setText(dn.getName());              // display it
     *   notifyListeners();                              // OK button re-evaluates
     * </pre>
     */
    public void validate()
    {
        Exception rdnE = null;

        if ( showRDN )
        {
            try
            {
                // calculate Rdn
                String[] rdnTypes = new String[rdnLineList.size()];
                String[] rdnValues = new String[rdnLineList.size()];

                for ( int i = 0; i < rdnLineList.size(); i++ )
                {
                    RdnLine rdnLine = rdnLineList.get( i );
                    rdnTypes[i] = rdnLine.rdnTypeCombo.getText();
                    rdnValues[i] = rdnLine.rdnValueText.getText();

                    if ( rdnLineList.size() > 1 )
                    {
                        rdnLine.rdnDeleteButton.setEnabled( true );
                    }
                    else
                    {
                        rdnLine.rdnDeleteButton.setEnabled( false );
                    }
                }

                rdn = DnUtils.composeRdn( rdnTypes, rdnValues );
            }
            catch ( Exception e )
            {
                rdnE = e;
                rdn = null;
            }
        }

        Exception parentE = null;

        if ( showParent )
        {
            try
            {
                // calculate Dn
                parentDn = parentEntryWidget.getDn();
            }
            catch ( Exception e )
            {
                parentE = e;
                parentDn = null;
            }
        }

        String s = ""; //$NON-NLS-1$

        if ( rdnE != null )
        {
            s += rdnE.getMessage() != null ? rdnE.getMessage() : Messages.getString( "DnBuilderWidget.ErrorInRDN" ); //$NON-NLS-1$
        }

        if ( parentE != null )
        {
            s += ", " + parentE.getMessage() != null ? parentE.getMessage() : Messages.getString( "DnBuilderWidget.ErrorInParentDN" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if ( previewText != null )
        {
            if ( s.length() > 0 )
            {
                previewText.setText( s );
            }
            else
            {
                Dn dn;

                if ( showParent && showRDN )
                {
                    try
                    {
                        dn = parentDn.add( rdn );
                    }
                    catch ( LdapInvalidDnException lide )
                    {
                        // Do nothing
                        dn = Dn.EMPTY_DN;
                    }
                }
                else if ( showParent )
                {
                    dn = parentDn;
                }
                else if ( showRDN )
                {
                    try
                    {
                        dn = new Dn( rdn );
                    }
                    catch ( LdapInvalidDnException lide )
                    {
                        // Do nothing
                        dn = Dn.EMPTY_DN;
                    }
                }
                else
                {
                    dn = Dn.EMPTY_DN;
                }

                previewText.setText( dn.getName() );
            }
        }

        notifyListeners();
    }


    // ── LANDO OPENS A NEW DEPARTMENT ──────────────────────────────────────────────
    // Lando decides the city needs another department slot in the naming structure.
    // He re-renders the whole RDN section, inserting the new row at exactly the right spot.
    // We tear down and recreate all RDN line widgets so the SWT layout stays consistent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a new empty RDN line at the specified position in the RDN composite.
     * Because SWT layouts place widgets in creation order, we rebuild all existing lines
     * before and after the insertion point so the visual order matches the list order.
     * After insertion, the shell is re-laid-out to accommodate the new row.
     *
     * <p>For example — Lando opens a new department:</p>
     * <pre>
     *   "We need a uid slot between cn and sn. Rebuild the roster."
     *   // All existing rows are recreated; the new row appears at index 1.
     *   shell.layout(true, true);
     * </pre>
     *
     * @param rdnComposite  The SWT composite holding all RDN line widgets.
     * @param index         The 0-based position at which to insert the new line.
     */
    private void addRdnLine( Composite rdnComposite, int index )
    {
        RdnLine[] rdnLines = rdnLineList.toArray( new RdnLine[rdnLineList.size()] );

        if ( rdnLines.length > 0 )
        {
            for ( int i = 0; i < rdnLines.length; i++ )
            {
                RdnLine oldRdnLine = rdnLines[i];

                // remember values
                String oldName = oldRdnLine.rdnTypeCombo.getText();
                String oldValue = oldRdnLine.rdnValueText.getText();

                // delete old
                oldRdnLine.rdnTypeCombo.dispose();
                oldRdnLine.rdnEqualsLabel.dispose();
                oldRdnLine.rdnValueText.dispose();
                oldRdnLine.rdnAddButton.dispose();
                oldRdnLine.rdnDeleteButton.dispose();
                rdnLineList.remove( oldRdnLine );

                // add new
                RdnLine newRdnLine = createRdnLine( rdnComposite );
                rdnLineList.add( newRdnLine );

                // restore value
                newRdnLine.rdnTypeCombo.setText( oldName );
                newRdnLine.rdnValueText.setText( oldValue );

                // check
                if ( index == i + 1 )
                {
                    RdnLine rdnLine = createRdnLine( rdnComposite );
                    rdnLineList.add( rdnLine );
                }
            }
        }
        else
        {
            RdnLine rdnLine = createRdnLine( rdnComposite );
            rdnLineList.add( rdnLine );
        }

        shell.layout( true, true );
    }


    // ── LANDO STAFFS A SINGLE DEPARTMENT SLOT ────────────────────────────────────
    // Each department in Cloud City has a name (attribute type), an equals sign on the door,
    // a value placard, and two buttons: one to open another slot, one to close this one.
    // We create those five SWT widgets, wire up content-assist and listeners, and return the line.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a single RDN input row and wires it up with listeners and content-assist.
     * An RDN row contains: a drop-down combo for the attribute type (e.g. "cn"),
     * an "=" label, a text field for the value, a "+" button to add another row,
     * and a "−" button to remove this row.
     *
     * <p>For example — Lando opens a new department office:</p>
     * <pre>
     *   [  cn ▼  ] [ = ] [ Lando Calrissian    ] [ + ] [ − ]
     * </pre>
     *
     * @param rdnComposite  The parent composite to attach the new row's widgets to.
     * @return              The fully initialized {@link RdnLine} holding references to all five widgets.
     */
    private RdnLine createRdnLine( final Composite rdnComposite )
    {
        final RdnLine rdnLine = new RdnLine();

        rdnLine.rdnTypeCombo = new Combo( rdnComposite, SWT.DROP_DOWN | SWT.BORDER );
        GridData gd = new GridData();
        gd.widthHint = 180;
        rdnLine.rdnTypeCombo.setLayoutData( gd );
        rdnLine.rdnNameCPA = new ExtendedContentAssistCommandAdapter( rdnLine.rdnTypeCombo, new ComboContentAdapter(),
            new ListContentProposalProvider( attributeNames ), null, null, true );

        rdnLine.rdnEqualsLabel = new Label( rdnComposite, SWT.NONE );
        rdnLine.rdnEqualsLabel.setText( "=" ); //$NON-NLS-1$

        rdnLine.rdnValueText = new Text( rdnComposite, SWT.BORDER );
        gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
        rdnLine.rdnValueText.setLayoutData( gd );

        rdnLine.rdnAddButton = new Button( rdnComposite, SWT.PUSH );
        rdnLine.rdnAddButton.setText( "  +   " ); //$NON-NLS-1$

        rdnLine.rdnDeleteButton = new Button( rdnComposite, SWT.PUSH );
        rdnLine.rdnDeleteButton.setText( "  −  " ); //$NON-NLS-1$

        if ( attributeNames != null )
        {
            rdnLine.rdnTypeCombo.setItems( attributeNames );
        }

        addRdnLineListeners( rdnLine );

        return rdnLine;
    }


    // ── LANDO CLOSES A DEPARTMENT ─────────────────────────────────────────────────
    // A department is no longer needed — Lando removes it from the city directory.
    // He disposes all five widgets and triggers a shell re-layout to close the gap.
    // We pull the RdnLine from our list and dispose each child SWT widget.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the RDN input row at the given index and disposes all its SWT widgets.
     * After disposal, the shell is re-laid-out to close the gap left by the removed row.
     *
     * <p>For example — Lando removes a department office:</p>
     * <pre>
     *   rdnLineList.remove(index);  // pull the department from the manifest
     *   rdnLine.rdnTypeCombo.dispose(); rdnLine.rdnValueText.dispose(); // etc.
     *   shell.layout(true, true);   // close the gap in the city layout
     * </pre>
     *
     * @param rdnComposite  The SWT composite that contained the row's widgets.
     * @param index         The 0-based index of the RDN line to remove.
     */
    private void deleteRdnLine( Composite rdnComposite, int index )
    {
        RdnLine rdnLine = rdnLineList.remove( index );
        if ( rdnLine != null )
        {
            rdnLine.rdnTypeCombo.dispose();
            rdnLine.rdnEqualsLabel.dispose();
            rdnLine.rdnValueText.dispose();
            rdnLine.rdnAddButton.dispose();
            rdnLine.rdnDeleteButton.dispose();

            if ( !rdnComposite.isDisposed() )
            {
                shell.layout( true, true );
            }
        }
    }


    // ── LANDO ASSIGNS GUARDS TO A DEPARTMENT BY INDEX ────────────────────────────
    // Lando tells security to watch department #3 — they look it up in the list.
    // We delegate to the overload that takes an RdnLine object directly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches modification and selection listeners to the RDN line at the given list index.
     * Convenience overload that resolves the index to an {@link RdnLine} and delegates.
     *
     * <p>For example — Lando assigns guards by department number:</p>
     * <pre>
     *   addRdnLineListeners(rdnLineList.get(index));
     * </pre>
     *
     * @param index  The 0-based index into {@code rdnLineList} identifying the row to watch.
     */
    private void addRdnLineListeners( int index )
    {
        if ( rdnLineList != null )
        {
            addRdnLineListeners( rdnLineList.get( index ) );
        }
    }


    // ── LANDO POSTS GUARDS AT A SPECIFIC DEPARTMENT DOOR ─────────────────────────
    // The security team hooks up directly to this department's controls.
    // Any activity on the add button, delete button, type combo, or value text notifies Lando.
    // We attach our shared listeners to all four interactive widgets on the RDN line.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches modification and selection listeners to all interactive widgets on an RDN line.
     * This wires the "+"/"-" buttons to the shared add/delete selection listeners and
     * attaches {@code this} (which implements {@link ModifyListener}) to the type combo
     * and value text so any typing triggers {@link #validate()}.
     *
     * <p>For example — guards posted at the department door:</p>
     * <pre>
     *   rdnLine.rdnAddButton.addSelectionListener(rdnAddButtonSelectionListener);
     *   rdnLine.rdnTypeCombo.addModifyListener(this);  // any change → validate()
     * </pre>
     *
     * @param rdnLine  The RDN line whose widgets should be watched; silently ignored if {@code null}.
     */
    private void addRdnLineListeners( RdnLine rdnLine )
    {
        if ( rdnLine != null )
        {
            rdnLine.rdnAddButton.addSelectionListener( rdnAddButtonSelectionListener );
            rdnLine.rdnDeleteButton.addSelectionListener( rdnDeleteButtonSelectionListener );
            rdnLine.rdnTypeCombo.addModifyListener( this );
            rdnLine.rdnValueText.addModifyListener( this );
        }
    }


    // ── LANDO PULLS THE GUARDS FROM A DEPARTMENT BY INDEX ────────────────────────
    // Lando temporarily reassigns the security team so a department can be quietly updated.
    // We look up the line by index and delegate to the object-level overload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all listeners from the RDN line at the given index.
     * Used during {@link #setInput} to silently pre-populate field values without triggering
     * spurious {@link #validate()} calls.
     *
     * <p>For example — Lando pulls the guards so updates can happen quietly:</p>
     * <pre>
     *   removeRdnLineListeners(rdnLineList.get(index));
     * </pre>
     *
     * @param index  The 0-based index into {@code rdnLineList} identifying the row.
     */
    private void removeRdnLineListeners( int index )
    {
        if ( rdnLineList != null )
        {
            removeRdnLineListeners( rdnLineList.get( index ) );
        }
    }


    // ── LANDO STANDS DOWN SECURITY AT A SPECIFIC DOOR ────────────────────────────
    // The security team is told to look the other way while Lando makes updates.
    // We detach all four listeners from the RDN line's interactive widgets.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes all modification and selection listeners from the widgets on an RDN line.
     * Called before programmatically changing field values in {@link #setInput} to prevent
     * those changes from triggering {@link #validate()} prematurely.
     *
     * <p>For example — the guards step aside:</p>
     * <pre>
     *   rdnLine.rdnAddButton.removeSelectionListener(rdnAddButtonSelectionListener);
     *   rdnLine.rdnTypeCombo.removeModifyListener(this);
     * </pre>
     *
     * @param rdnLine  The RDN line to detach listeners from; silently ignored if {@code null}.
     */
    private void removeRdnLineListeners( RdnLine rdnLine )
    {
        if ( rdnLine != null )
        {
            rdnLine.rdnAddButton.removeSelectionListener( rdnAddButtonSelectionListener );
            rdnLine.rdnDeleteButton.removeSelectionListener( rdnDeleteButtonSelectionListener );
            rdnLine.rdnTypeCombo.removeModifyListener( this );
            rdnLine.rdnValueText.removeModifyListener( this );
        }
    }

    /**
     * The Class RdnLine is a wrapper for all input elements
     * of an Rdn line. It contains a combo for the Rdn attribute,
     * an input field for the Rdn value and + and - buttons
     * to add and remove other Rdn lines. It looks like this:
     * <pre>
     * --------------------------------------------------
     * | attribute type v | = | attribute value | + | - |
     * --------------------------------------------------
     * </pre>
     */
    private class RdnLine
    {

        /** The rdn name combo. */
        private Combo rdnTypeCombo;

        /** The content proposal adapter */
        private ContentProposalAdapter rdnNameCPA;

        /** The rdn value text. */
        private Text rdnValueText;

        /** The rdn equals label. */
        private Label rdnEqualsLabel;

        /** The rdn add button. */
        private Button rdnAddButton;

        /** The rdn delete button. */
        private Button rdnDeleteButton;
    }


    // ── LANDO OPENS OR CLOSES THE CITY FOR BUSINESS ──────────────────────────────
    // When the Empire arrives, Lando shuts everything down — all controls go grey.
    // When they leave, he re-enables everything and invites the staff back to work.
    // We enable or disable every child widget so the whole DN builder reacts as one unit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables all child widgets of this DN builder.
     * When disabled, all combos, text fields, and buttons go grey and non-interactive.
     * When re-enabled, focus is moved to the first RDN value text for immediate editing.
     *
     * <p>For example — Lando opens and closes the city:</p>
     * <pre>
     *   setEnabled(false);  // "Evacuate! Everything shuts down."
     *   setEnabled(true);   // "We're back in business — CN field gets focus."
     * </pre>
     *
     * @param b  {@code true} to enable the widget (interactive), {@code false} to disable it (read-only appearance).
     */
    public void setEnabled( boolean b )
    {
        if ( parentEntryWidget != null )
        {
            parentEntryLabel.setEnabled( b );
            parentEntryWidget.setEnabled( b );
        }
        if ( rdnComposite != null && rdnLineList != null )
        {
            rdnLabel.setEnabled( b );
            rdnComposite.setEnabled( b );
            for ( RdnLine rdnLine : rdnLineList )
            {
                rdnLine.rdnTypeCombo.setEnabled( b );
                rdnLine.rdnEqualsLabel.setEnabled( b );
                rdnLine.rdnValueText.setEnabled( b );
                rdnLine.rdnAddButton.setEnabled( b );
                rdnLine.rdnDeleteButton.setEnabled( b && rdnLineList.size() > 1 );
            }
            if ( b )
            {
                rdnLineList.get( 0 ).rdnValueText.setFocus();
            }
        }
        if ( previewText != null )
        {
            previewLabel.setEnabled( b );
            previewText.setEnabled( b );
        }
    }

}
