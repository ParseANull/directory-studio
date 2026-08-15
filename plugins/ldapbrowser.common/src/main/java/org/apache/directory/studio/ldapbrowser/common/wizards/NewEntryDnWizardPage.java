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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Ava;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.DnBuilderWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.ListContentProposalProvider;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: NewEntryDnWizardPage — YODA TEACHES LUKE TO NAME HIS PLACE IN THE FORCE
// Deep in the Dagobah swamp, Yoda sits Luke down for a deceptively simple lesson:
// "Know your position in the Force, you must.  Not just what you are — but where
// you stand, relative to those who came before."  Luke has to name his RDN (who
// he is: cn=Luke) and locate his parent DN (where he is: ou=Jedi,dc=galaxy).
// Before moving on, Yoda checks the Force — does the parent really exist?
// Does Luke's chosen position conflict with someone already there?
// This wizard page encodes exactly that lesson: compose the DN by picking an
// RDN attribute/value and a parent entry, then verify both on the server before
// letting the user advance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The third page of {@link NewEntryWizard} — lets the user compose the
 * Distinguished Name (DN) for the new entry.
 * For regular entries, a {@link DnBuilderWidget} lets the user pick an RDN
 * attribute and value, then select a parent DN.
 * For context entries, a simple combo pre-populated with naming contexts lets
 * the user type or select the full root DN.
 * Before advancing, we verify on the server that the parent exists and that
 * the full DN does not already exist.
 * Think of this page as Yoda's naming lesson — Luke must say who he is and
 * where in the directory tree he belongs before the training continues.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryDnWizardPage extends WizardPage implements WidgetModifyListener
{

    /** The wizard. */
    private NewEntryWizard wizard;

    /** The Dn builder widget. */
    private DnBuilderWidget dnBuilderWidget;

    /** The context entry Dn combo. */
    private Combo contextEntryDnCombo;

    /** The content proposal adapter for the context entry Dn combo. */
    private ContentProposalAdapter contextEntryDnComboCPA;


    // ── Yoda Prepares the Naming Lesson Clearing ──────────────────────────────
    // Yoda clears a flat spot in the swamp undergrowth and sets up two possible
    // lesson layouts: one for a regular entry (RDN + parent picker) and one for
    // a context entry (just type the full root DN).  The title and description
    // differ between the two modes, so Yoda chooses the right words up front.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewEntryDnWizardPage} and sets the title and
     * description appropriate for the wizard mode (context entry or regular entry).
     * The page starts incomplete — the user must enter a valid DN before Next
     * becomes available.
     *
     * <p>For example — Yoda picks the right lesson plan based on Luke's task:</p>
     * <pre>
     *   if ( wizard.isNewContextEntry() )
     *     setDescription( "Enter the DN of the new context entry" );
     *   else
     *     setDescription( "Select a parent DN and enter an RDN" );
     * </pre>
     *
     * @param pageName  Internal wizard page identifier.
     * @param wizard    The parent {@link NewEntryWizard} coordinating all pages.
     */
    public NewEntryDnWizardPage( String pageName, NewEntryWizard wizard )
    {
        super( pageName );
        setTitle( Messages.getString( "NewEntryDnWizardPage.DistinguishedName" ) ); //$NON-NLS-1$
        if ( wizard.isNewContextEntry() )
        {
            setDescription( Messages.getString( "NewEntryDnWizardPage.EnterDN" ) ); //$NON-NLS-1$
        }
        else
        {
            setDescription( Messages.getString( "NewEntryDnWizardPage.SelectParent" ) ); //$NON-NLS-1$
        }
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_ENTRY_WIZARD ) );
        setPageComplete( false );

        this.wizard = wizard;
    }


    // ── The Naming Lesson Ends — Yoda Cleans Up the Clearing ─────────────────
    // As Luke leaves Dagobah, Yoda sweeps the clearing: the DN builder widget's
    // listener is removed and all its resources are freed so nothing leaks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases the resources held by the {@link DnBuilderWidget}, if one was
     * created.  Removes the widget-modify listener before disposing so we don't
     * get stray events after the page is gone.
     *
     * <p>For example — Yoda clears the lesson clearing after Luke heads to Bespin:</p>
     * <pre>
     *   dnBuilderWidget.removeWidgetModifyListener( this );
     *   dnBuilderWidget.dispose();
     * </pre>
     */
    public void dispose()
    {
        if ( dnBuilderWidget != null )
        {
            dnBuilderWidget.removeWidgetModifyListener( this );
            dnBuilderWidget.dispose();
            dnBuilderWidget = null;
        }
        super.dispose();
    }


    // ── Yoda Checks Whether Luke Named and Located Himself Correctly ──────────
    // Yoda listens to Luke's answer: for a context entry, the full DN must be
    // non-empty and syntactically valid; for a regular entry, both the RDN and
    // the parent DN must be set.  If either is missing, Yoda waits; otherwise
    // the lesson is saved to the prototype and Next lights up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current DN input and enables or disables the Next button.
     * Also calls {@link #saveState()} to persist a valid DN to the prototype.
     *
     * <p>For example — Yoda accepts Luke's answer only when it is complete:</p>
     * <pre>
     *   // context entry mode: DN must be non-empty and syntactically valid
     *   // regular entry mode: RDN and parentDn must both be non-null
     * </pre>
     */
    private void validate()
    {
        if ( wizard.isNewContextEntry() && !"".equals( contextEntryDnCombo.getText() ) //$NON-NLS-1$
            && Dn.isValid( contextEntryDnCombo.getText() ) )
        {
            setPageComplete( true );
            saveState();
        }
        else if ( !wizard.isNewContextEntry() && dnBuilderWidget.getRdn() != null
            && dnBuilderWidget.getParentDn() != null )
        {
            setPageComplete( true );
            saveState();
        }
        else
        {
            setPageComplete( false );
        }
    }


    // ── Yoda Recalls Luke's Previous Answer to Pre-Fill the Lesson ───────────
    // When Luke returns to this clearing, Yoda reminds him what he last said —
    // the prototype entry's current DN is used to pre-fill the DN builder or
    // the context-entry combo so Luke doesn't have to start from scratch.
    // For a context entry, Yoda also lists all known naming contexts as hints.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the DN input controls from the prototype entry's current DN.
     * For context entries: loads naming contexts from the root DSE into the
     * combo and pre-selects the prototype's DN if it matches one.
     * For regular entries: passes the parent DN and RDN from the prototype
     * to the {@link DnBuilderWidget}.
     *
     * <p>For example — Yoda reads Luke's notebook and pre-fills the exercise:</p>
     * <pre>
     *   // context entry: contextEntryDnCombo.setItems( namingContextValues )
     *   // regular entry: dnBuilderWidget.setInput( connection, attrNames, rdn, parentDn )
     * </pre>
     */
    private void loadState()
    {
        DummyEntry newEntry = wizard.getPrototypeEntry();

        if ( wizard.isNewContextEntry() )
        {
            IAttribute attribute = wizard.getSelectedConnection().getRootDSE().getAttribute(
                SchemaConstants.NAMING_CONTEXTS_AT );
            if ( attribute != null )
            {
                String[] values = attribute.getStringValues();

                // content proposals
                contextEntryDnComboCPA.setContentProposalProvider( new ListContentProposalProvider( values ) );

                // fill namingContext values into combo
                contextEntryDnCombo.setItems( values );

                // preset combo text
                if ( Arrays.asList( values ).contains( newEntry.getDn().getName() ) )
                {
                    contextEntryDnCombo.setText( newEntry.getDn().getName() );
                }
            }
        }
        else
        {
            Collection<AttributeType> atds = SchemaUtils.getAllAttributeTypeDescriptions( newEntry );
            String[] attributeNames = SchemaUtils.getNames( atds ).toArray( ArrayUtils.EMPTY_STRING_ARRAY );

            Dn parentDn = null;

            boolean hasSelectedEntry = wizard.getSelectedEntry() != null;
            boolean newEntryParentDnNotNullOrEmpty = !Dn.isNullOrEmpty( newEntry.getDn().getParent() );

            if ( hasSelectedEntry )
            {
                boolean newEntryDnEqualsSelectedEntryDn = newEntry.getDn().equals( wizard.getSelectedEntry().getDn() );

                if ( newEntryDnEqualsSelectedEntryDn && newEntryParentDnNotNullOrEmpty )
                {
                    parentDn = newEntry.getDn().getParent();
                }
                else
                {
                    parentDn = wizard.getSelectedEntry().getDn();
                }
            }
            else if ( newEntryParentDnNotNullOrEmpty )
            {
                parentDn = newEntry.getDn().getParent();
            }

            Rdn rdn = newEntry.getRdn();

            dnBuilderWidget.setInput( wizard.getSelectedConnection(), attributeNames, rdn, parentDn );
        }
    }


    // ── Yoda Records Luke's Chosen Position in the Galaxy ─────────────────────
    // Yoda writes Luke's answer into the training scroll — old RDN values are
    // erased first, then the new DN is set, and the RDN attribute values are
    // added back to the prototype entry so the attributes page sees them.
    // Event firing is suspended during the write to avoid feedback loops.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes the DN from the input controls into the prototype entry.
     * First removes the old RDN attributes from the prototype, then sets the
     * new DN, then adds the new RDN's attribute values back to the prototype
     * so the attributes page can display them.
     * All changes are made with event firing suspended to avoid spurious updates.
     *
     * <p>For example — Yoda erases Luke's old coordinates and writes the new ones:</p>
     * <pre>
     *   // remove old RDN attr values from prototype
     *   newEntry.setDn( newDn );
     *   // add new RDN attr values to prototype
     * </pre>
     */
    private void saveState()
    {
        DummyEntry newEntry = wizard.getPrototypeEntry();

        try
        {
            EventRegistry.suspendEventFiringInCurrentThread();

            // remove old Rdn
            if ( newEntry.getRdn().size() > 0 )
            {
                Iterator<Ava> atavIterator = newEntry.getRdn().iterator();
                while ( atavIterator.hasNext() )
                {
                    Ava atav = atavIterator.next();
                    IAttribute attribute = newEntry.getAttribute( atav.getType() );
                    if ( attribute != null )
                    {
                        IValue[] values = attribute.getValues();
                        for ( int v = 0; v < values.length; v++ )
                        {
                            if ( values[v].getStringValue().equals( atav.getValue().getNormalized() ) )
                            {
                                attribute.deleteValue( values[v] );
                            }
                        }

                        // If we have removed all the values of the attribute,
                        // then we also need to remove this attribute from the
                        // entry.
                        // This test has been added to fix DIRSTUDIO-222
                        if ( attribute.getValueSize() == 0 )
                        {
                            newEntry.deleteAttribute( attribute );
                        }
                    }
                }
            }

            // set new Dn
            Dn dn;

            if ( wizard.isNewContextEntry() )
            {
                try
                {
                    dn = new Dn( contextEntryDnCombo.getText() );
                }
                catch ( LdapInvalidDnException e )
                {
                    dn = Dn.EMPTY_DN;
                }
            }
            else
            {
                try
                {
                    dn = dnBuilderWidget.getParentDn().add( dnBuilderWidget.getRdn() );
                }
                catch ( LdapInvalidDnException lide )
                {
                    // Do nothing
                    dn = Dn.EMPTY_DN;
                }
            }
            newEntry.setDn( dn );

            // add new Rdn
            if ( dn.getRdn().size() > 0 )
            {
                Iterator<Ava> atavIterator = dn.getRdn().iterator();
                while ( atavIterator.hasNext() )
                {
                    Ava atav = atavIterator.next();
                    IAttribute rdnAttribute = newEntry.getAttribute( atav.getType() );
                    if ( rdnAttribute == null )
                    {
                        rdnAttribute = new Attribute( newEntry, atav.getType() );
                        newEntry.addAttribute( rdnAttribute );
                    }
                    Object rdnValue = atav.getValue().getNormalized();
                    String[] stringValues = rdnAttribute.getStringValues();
                    if ( !Arrays.asList( stringValues ).contains( rdnValue ) )
                    {
                        rdnAttribute.addValue( new Value( rdnAttribute, rdnValue ) );
                    }
                }
            }

        }
        finally
        {
            EventRegistry.resumeEventFiringInCurrentThread();
        }
    }


    // ── Luke Arrives at Yoda's Training Spot — Load the Current State ─────────
    // Luke jogs over to the clearing — Yoda immediately reads back the last
    // known DN from the training scroll and pre-fills the inputs, then checks
    // whether what's there is already valid.  The focus goes to the input
    // control so Luke can type right away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when this page becomes visible.
     * Pre-populates the DN inputs from the prototype entry via {@link #loadState()},
     * then validates to update the Next button, and focuses the input control.
     *
     * <p>For example — Luke jogs up; Yoda hands him the pre-filled exercise sheet:</p>
     * <pre>
     *   if ( visible ) { loadState(); validate(); inputControl.setFocus(); }
     * </pre>
     *
     * @param visible  {@code true} when this page is being shown, {@code false} when hidden.
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            loadState();
            validate();

            if ( wizard.isNewContextEntry() )
            {
                contextEntryDnCombo.setFocus();
            }
        }
    }


    // ── Yoda Checks Whether Luke Is Ready for the Next Trial ──────────────────
    // Yoda doesn't run to the next cave just to see whether the path is clear —
    // he simply checks whether Luke's current answer is complete.  This fast
    // check avoids triggering a server read just to decide whether Next is enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only when the page is complete, without triggering
     * the {@link #getNextPage()} server-validation logic.
     * We override this to avoid unnecessary {@link ReadEntryRunnable} executions
     * while the user is still composing the DN.
     *
     * <p>For example — Yoda confirms Luke is ready before sending him to the cave:</p>
     * <pre>
     *   return isPageComplete(); // no server call — just a state check
     * </pre>
     *
     * @return  {@code true} if the page is complete and Next can be pressed.
     */
    @Override
    public boolean canFlipToNextPage()
    {
        return isPageComplete();
    }


    // ── Yoda Verifies the Path Before Leading Luke Onward ─────────────────────
    // Before Yoda leads Luke to the next training site, he stretches out with
    // the Force to verify the path: does the parent location actually exist?
    // Is the spot Luke chose already occupied by someone else?  Only when both
    // checks pass does Yoda step aside and wave Luke forward.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the composed DN against the server before advancing.
     * For regular entries: verifies that the parent DN exists and that the
     * full DN does not already exist.
     * For context entries: verifies that the chosen DN does not already exist.
     * Shows an error dialog and returns {@code null} if any check fails.
     *
     * <p>For example — Yoda checks two things with the Force before moving on:</p>
     * <pre>
     *   ReadEntryRunnable check1 = new ReadEntryRunnable( conn, parentDn );
     *   // → error if parent does not exist
     *   ReadEntryRunnable check2 = new ReadEntryRunnable( conn, fullDn );
     *   // → error if entry already exists
     * </pre>
     *
     * @return  The next {@link IWizardPage}, or {@code null} if a server check fails.
     */
    @Override
    public IWizardPage getNextPage()
    {
        if ( !wizard.isNewContextEntry() )
        {
            dnBuilderWidget.validate();

            Rdn rdn = dnBuilderWidget.getRdn();
            Dn parentDn = dnBuilderWidget.getParentDn();

            try
            {
                final Dn dn = parentDn.add( rdn );

                // check if parent exists
                ReadEntryRunnable readEntryRunnable1 = new ReadEntryRunnable( wizard.getSelectedConnection(), parentDn );
                RunnableContextRunner.execute( readEntryRunnable1, getContainer(), false );
                IEntry parentEntry = readEntryRunnable1.getReadEntry();

                if ( parentEntry == null )
                {
                    getShell().getDisplay().syncExec( () ->
                        {
                            MessageDialog
                                .openError( getShell(),
                                    Messages.getString( "NewEntryDnWizardPage.Error" ), //$NON-NLS-1$
                                    NLS
                                        .bind(
                                            Messages.getString( "NewEntryDnWizardPage.ParentDoesNotExist" ), dnBuilderWidget.getParentDn().toString() ) ); //$NON-NLS-1$
                        }
                    );

                    return null;
                }

                // check that new entry does not exists yet
                ReadEntryRunnable readEntryRunnable2 = new ReadEntryRunnable( wizard.getSelectedConnection(), dn );
                RunnableContextRunner.execute( readEntryRunnable2, getContainer(), false );
                IEntry entry = readEntryRunnable2.getReadEntry();

                if ( entry != null )
                {
                    getShell().getDisplay().syncExec( () ->
                        {
                            MessageDialog
                                .openError(
                                    getShell(),
                                    Messages.getString( "NewEntryDnWizardPage.Error" ), NLS.bind( Messages.getString( "NewEntryDnWizardPage.EntryAlreadyExists" ), dn.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    );

                    return null;
                }
            }
            catch ( LdapInvalidDnException lide )
            {
                return null;
            }
        }
        else
        {
            try
            {
                final Dn dn = new Dn( contextEntryDnCombo.getText() );

                // check that new entry does not exists yet
                ReadEntryRunnable readEntryRunnable2 = new ReadEntryRunnable( wizard.getSelectedConnection(), dn );
                RunnableContextRunner.execute( readEntryRunnable2, getContainer(), false );
                IEntry entry = readEntryRunnable2.getReadEntry();
                if ( entry != null )
                {
                    getShell().getDisplay().syncExec( () ->
                        {
                            MessageDialog
                                .openError(
                                    getShell(),
                                    Messages.getString( "NewEntryDnWizardPage.Error" ), NLS.bind( Messages.getString( "NewEntryDnWizardPage.EntryAlreadyExists" ), dn.toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    );

                    return null;
                }
            }
            catch ( LdapInvalidDnException e )
            {
                return null;
            }
        }

        return super.getNextPage();
    }


    // ── Yoda Sets Up the Naming Lesson Layout ─────────────────────────────────
    // Yoda arranges two possible lesson formats: for a context entry, a single
    // combo drop-down shows the known naming contexts; for a regular entry,
    // the full DnBuilderWidget appears with its RDN and parent-DN sections.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT controls for this page.
     * For context entries: creates a combo pre-wired with content proposals for
     * known naming contexts from the root DSE.
     * For regular entries: creates a {@link DnBuilderWidget} that lets the user
     * pick an RDN attribute/value and browse for the parent DN.
     *
     * <p>For example — Yoda draws two possible lesson formats on the ground:</p>
     * <pre>
     *   // context entry: [ Full DN combo ▼ ]
     *   // regular entry: [ RDN picker ] + [ Parent DN browser ]
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    public void createControl( Composite parent )
    {
        if ( wizard.isNewContextEntry() )
        {
            // the combo
            Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );
            contextEntryDnCombo = BaseWidgetUtils.createCombo( composite, ArrayUtils.EMPTY_STRING_ARRAY, 0, 1 );
            contextEntryDnCombo.addModifyListener( event -> validate() );

            // attach content proposal behavior
            contextEntryDnComboCPA = new ContentProposalAdapter( contextEntryDnCombo, new ComboContentAdapter(), null,
                null, null );
            contextEntryDnComboCPA.setFilterStyle( ContentProposalAdapter.FILTER_NONE );
            contextEntryDnComboCPA.setProposalAcceptanceStyle( ContentProposalAdapter.PROPOSAL_REPLACE );

            setControl( composite );
        }
        else
        {
            dnBuilderWidget = new DnBuilderWidget( true, true );
            dnBuilderWidget.addWidgetModifyListener( this );
            Composite composite = dnBuilderWidget.createContents( parent );
            setControl( composite );
        }
    }


    // ── Luke Adjusts His Answer — Yoda Re-Evaluates ───────────────────────────
    // Luke changes the RDN or parent DN in the DN builder — Yoda immediately
    // re-reads the answer and decides whether Next should light up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fired when the {@link DnBuilderWidget} content changes.
     * Re-validates the page so the Next button reflects the current DN state.
     *
     * <p>For example — Luke writes a new RDN; Yoda checks the updated answer:</p>
     * <pre>
     *   public void widgetModified( WidgetModifyEvent event ) { validate(); }
     * </pre>
     *
     * @param event  The widget-modify event from the DN builder; not used directly.
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    // ── Yoda Notes Luke's Last Preferred DN Layout ────────────────────────────
    // At the end of the session, Yoda jots down which parent DN Luke used most
    // recently so next time the same spot is pre-filled as the default.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current DN builder widget's dialog settings (e.g. last-used
     * parent DN) so they survive across wizard invocations.
     * Called by {@link NewEntryWizard#performFinish()} for regular (non-context)
     * entries only.
     *
     * <p>For example — Yoda records Luke's last-used parent location:</p>
     * <pre>
     *   dnBuilderWidget.saveDialogSettings();
     * </pre>
     */
    public void saveDialogSettings()
    {
        if ( !wizard.isNewContextEntry() )
        {
            dnBuilderWidget.saveDialogSettings();
        }
    }

}
