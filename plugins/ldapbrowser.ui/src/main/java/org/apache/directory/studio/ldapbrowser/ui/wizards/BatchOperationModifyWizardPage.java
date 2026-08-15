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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.util.List;

import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.ModWidget;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.parser.LdifParser;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: BatchOperationModifyWizardPage — BUILDING THE SECOND DEATH STAR ───
// Imperial engineers don't type raw blueprints — they use the construction UI
// to assemble the super-laser specifications attribute by attribute. This page
// is that construction UI: the ModWidget presents a graphical add/replace/delete
// attribute modification editor. The result is converted to an LDIF modify
// fragment and validated in real time, just as Imperial engineers run structural
// checks on each new section before it's bolted on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Optional page of the batch operation wizard shown when the user chose
 * "Modify" on the type page. Presents the {@link ModWidget} GUI that lets
 * the user build LDAP modify operations (add/replace/delete attributes)
 * without writing raw LDIF. The resulting fragment is validated by parsing it
 * against a dummy DN so syntax errors are caught before the operation runs.
 * Think of Imperial engineers assembling the Death Star's super-laser
 * specification section by section — each attribute change is checked for
 * structural soundness before being committed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationModifyWizardPage extends WizardPage implements WidgetModifyListener
{
    /** The wizard */
    private BatchOperationWizard wizard;

    /** The ModWidget */
    private ModWidget modWidget;


    // ── Engineers Set Up the Construction Table ───────────────────────────────────
    // The construction table is laid out with the connection's schema loaded
    // so attribute type suggestions are accurate.
    // We start incomplete; the user must add at least one valid modification.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationModifyWizardPage, starting incomplete.
     * The schema from the wizard's connection is passed to the ModWidget so
     * attribute-type suggestions are accurate for the target server.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent batch operation wizard, queried for the connection schema.
     */
    public BatchOperationModifyWizardPage( String pageName, BatchOperationWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "BatchOperationModifyWizardPage.DefineModification" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "BatchOperationModifyWizardPage.PleaseDefineModifications" ) ); //$NON-NLS-1$
        // super.setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ENTRY_WIZARD));
        super.setPageComplete( false );

        this.wizard = wizard;
    }


    // ── Engineers Lay Out the Schematics ─────────────────────────────────────────
    // The construction table unfolds and the schema-aware modification widget
    // appears, ready for the engineers to start specifying changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates the {@link ModWidget} inside a fill-both grid layout, initialised
     * with the connection's schema (or the default schema if no connection is
     * available). Immediately validates so the page state is consistent.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        modWidget = new ModWidget( wizard.getConnection() != null ? wizard.getConnection().getSchema()
            : Schema.DEFAULT_SCHEMA );
        modWidget.createContents( composite );
        modWidget.addWidgetModifyListener( this );

        validate();

        setControl( composite );

    }


    // ── Engineers Read Off the Completed Section ──────────────────────────────────
    // The section is assembled and ready — the LDIF fragment is extracted
    // so the wizard can prepend DNs and form the full change records.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF modify fragment assembled by the ModWidget.
     * The wizard prepends each target entry's DN to this fragment to build
     * a complete LDIF change record per entry.
     *
     * @return  the LDIF modify fragment (e.g., "changetype: modify\nadd: cn\ncn: ...\n-").
     */
    public String getLdifFragment()
    {
        return modWidget.getLdifFragment();
    }


    // ── Engineers Re-Check the Blueprint After Every Change ───────────────────────
    // Every time a new attribute modification is added or removed, the structural
    // checkers verify the section's validity against the schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the ModWidget after each user change. Re-validates the current
     * fragment so the "Next"/"Finish" button reflects validity.
     *
     * @param event  the widget-modify event (unused; we re-read from the widget).
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    /**
     * Validates the page
     */
    private void validate()
    {
        String dummyLdif = "dn: cn=dummy" + BrowserCoreConstants.LINE_SEPARATOR + modWidget.getLdifFragment(); //$NON-NLS-1$

        LdifFile model = new LdifParser().parse( dummyLdif );

        List<LdifContainer> containers = model.getContainers();

        if ( containers.size() == 0 )
        {
            setPageComplete( false );
            return;
        }

        for ( LdifContainer ldifContainer : containers )
        {
            if ( !ldifContainer.isValid() )
            {
                setPageComplete( false );

                return;
            }
        }

        setPageComplete( true );
    }
}
