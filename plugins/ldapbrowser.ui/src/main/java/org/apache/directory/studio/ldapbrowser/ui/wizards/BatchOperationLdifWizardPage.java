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
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldifeditor.widgets.LdifEditorWidget;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: BatchOperationLdifWizardPage — C-3PO DECODES THE LDIF SCROLL ──────
// C-3PO reads the ancient LDIF scroll and checks its grammar before the
// mission proceeds. This page gives the user an embedded LDIF editor
// pre-filled with "dn: cn=dummy\nchangetype: modify\n" — they write the
// attribute change lines and C-3PO (the LdifEditorWidget parser) validates
// the syntax in real time. The dummy DN prefix is protected from editing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Optional page of the batch operation wizard shown when the user chose
 * "Execute LDIF changetype" on the type page.
 * Presents an embedded read/write LDIF editor pre-populated with a dummy DN
 * and a {@code changetype: modify} header. The user writes the modify fragment
 * (the attribute lines); the dummy DN line is protected from editing by a
 * VerifyListener. The page is complete only when all containers in the LDIF
 * model are syntactically valid.
 * Think of C-3PO reading the LDIF scroll and confirming its grammar before
 * handing it to the clone troopers for execution.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BatchOperationLdifWizardPage extends WizardPage implements WidgetModifyListener
{

    private static final String LDIF_DN_PREFIX = "dn: cn=dummy" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    private static final String LDIF_INITIAL = "changetype: modify" + BrowserCoreConstants.LINE_SEPARATOR; //$NON-NLS-1$

    private BatchOperationWizard wizard;

    private LdifEditorWidget ldifEditorWidget;


    // ── C-3PO Opens the Scroll ────────────────────────────────────────────────────
    // C-3PO unrolls the scroll, examines the opening lines, and readies himself
    // to validate whatever the user writes after them.
    // We start incomplete because the user needs to write the actual fragment.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BatchOperationLdifWizardPage, starting incomplete so the user
     * must enter a valid LDIF fragment before the wizard can proceed.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent batch operation wizard; queried in {@link #isPageComplete}
     *                  to short-circuit validation when this page is irrelevant.
     */
    public BatchOperationLdifWizardPage( String pageName, BatchOperationWizard wizard )
    {
        super( pageName );
        super.setTitle( Messages.getString( "BatchOperationLdifWizardPage.LDIFFragment" ) ); //$NON-NLS-1$
        super.setDescription( Messages.getString( "BatchOperationLdifWizardPage.PleaseEnterLDIFFragment" ) ); //$NON-NLS-1$
        // super.setImageDescriptor(BrowserUIPlugin.getDefault().getImageDescriptor(BrowserUIConstants.IMG_ENTRY_WIZARD));
        super.setPageComplete( false );

        this.wizard = wizard;
    }


    // ── C-3PO Puts Down the Scroll ───────────────────────────────────────────────
    // When the reading session ends, C-3PO sets down the scroll and releases
    // the display resources he was using.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Disposes the LDIF editor widget to release SWT resources.
     */
    public void dispose()
    {
        ldifEditorWidget.dispose();
        super.dispose();
    }


    private void validate()
    {
        LdifFile model = ldifEditorWidget.getLdifModel();
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


    // ── C-3PO Checks Whether He's Needed ─────────────────────────────────────────
    // If the type page says the user chose "Modify" or "Delete", C-3PO can stay
    // in the background — this page is irrelevant and automatically complete.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code true} if the type page's operation type is not
     * {@code OPERATION_TYPE_CREATE_LDIF} (this page is skipped), or if the
     * parent's {@code isPageComplete()} would return true (the user has written
     * a valid LDIF fragment).
     *
     * @return  {@code true} if the page can be bypassed or is filled correctly.
     */
    public boolean isPageComplete()
    {

        if ( wizard.getTypePage().getOperationType() != BatchOperationTypeWizardPage.OPERATION_TYPE_CREATE_LDIF )
        {
            return true;
        }

        return super.isPageComplete();
    }


    // ── C-3PO Unfurls the Writing Surface ────────────────────────────────────────
    // C-3PO lays out the editing surface with the protected opening lines already
    // written. The user fills in the rest; a VerifyListener prevents editing
    // the dummy DN prefix.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates a fill-both grid layout holding the LDIF editor widget, pre-seeded
     * with a dummy DN and a {@code changetype: modify} line.
     * A VerifyListener blocks edits that would overwrite the first
     * {@code LDIF_DN_PREFIX.length()} characters so the dummy header stays intact.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        ldifEditorWidget = new LdifEditorWidget( null, LDIF_DN_PREFIX + LDIF_INITIAL, true );
        ldifEditorWidget.createWidget( composite );
        ldifEditorWidget.addWidgetModifyListener( this );

        ldifEditorWidget.getSourceViewer().getTextWidget().addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( e.start < LDIF_DN_PREFIX.length() || e.end < LDIF_DN_PREFIX.length() )
                {
                    e.doit = false;
                }
            }
        } );

        validate();

        setControl( composite );
    }


    // ── C-3PO Extracts the Useful Part ───────────────────────────────────────────
    // C-3PO strips off the dummy DN header and hands back just the changetype
    // lines — the fragment the wizard will prepend to each real DN in the batch.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF fragment written by the user, with the dummy DN prefix
     * stripped out. The wizard prepends each target entry's real DN before this
     * fragment to build the complete LDIF change record.
     *
     * @return  the user-written LDIF changetype fragment without the dummy DN.
     */
    public String getLdifFragment()
    {
        return ldifEditorWidget.getLdifModel().toRawString().replaceAll( LDIF_DN_PREFIX, "" ); //$NON-NLS-1$
    }


    // ── C-3PO Re-Validates After Every Keystroke ──────────────────────────────────
    // Every time the user types a character, C-3PO checks the grammar again.
    // We delegate to validate() which sets page complete accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the LDIF editor widget whenever its content changes.
     * We re-validate so the wizard's "Next"/"Finish" button reflects
     * whether the current LDIF is syntactically valid.
     *
     * @param event  the widget-modify event (unused; we re-read the model directly).
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }

}
