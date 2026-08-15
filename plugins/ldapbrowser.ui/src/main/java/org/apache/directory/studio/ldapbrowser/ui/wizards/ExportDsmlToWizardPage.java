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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportDsmlWizard.ExportDsmlWizardSaveAsType;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: ExportDsmlToWizardPage — YODA DECIDES WHERE AND HOW TO SET IT DOWN ─
// Yoda must not only choose where to set the X-wing (the file path) but also
// decide WHICH side up to land it: entry-first (RESPONSE — the LDAP entries
// themselves) or query-first (REQUEST — the search parameters that would
// reproduce the result set). This extra "Save as" radio-button group is the
// unique addition this page makes on top of the base To page.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "To" page of the DSML export wizard: picks the destination .xml file
 * and the DSML variant (RESPONSE or REQUEST).
 * Extends {@link ExportBaseToPage} with a "Save as" radio group that lets the
 * user choose between DSML response format (searchResultEntry elements) and
 * DSML request format (searchRequest element). The choice is pushed back to
 * the wizard via {@link ExportDsmlWizard#setSaveAsType}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportDsmlToWizardPage extends ExportBaseToPage
{
    /** The associated wizard */
    private ExportDsmlWizard wizard;

    /** The extensions used by DSML files*/
    private static final String[] EXTENSIONS = new String[]
        { "*.xml", "*" }; //$NON-NLS-1$ //$NON-NLS-2$


    // ── Yoda Checks the DSML Landing Requirements ────────────────────────────────
    // The file must be an XML file, and the wizard reference is kept so the
    // Save-As radio buttons can update the wizard's saveAsType directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportDsmlToWizardPage with the DSML wizard icon and a
     * reference to the wizard for pushing the saveAsType back.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent DSML export wizard.
     */
    public ExportDsmlToWizardPage( String pageName, ExportDsmlWizard wizard )
    {
        super( pageName, wizard );
        this.wizard = wizard;
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_EXPORT_DSML_WIZARD ) );
    }


    // ── Yoda Lays Out the DSML Landing Zone ──────────────────────────────────────
    // The page has the standard file-selector from the base class, then adds
    // a "Save as" radio group: RESPONSE (entries) or REQUEST (query parameters).
    // Selecting a radio immediately updates the wizard's saveAsType so
    // performFinish() sees the latest choice.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using the base-class file browser, then appends a
     * "Save as" group with two radio buttons: "DSML Response" (pre-selected,
     * writes searchResultEntry elements) and "DSML Request" (writes a
     * searchRequest element). Each button updates the wizard's
     * {@link ExportDsmlWizard#saveAsType} immediately on selection.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );

        Composite saveAsOuterComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 3 );
        Group saveAsGroup = BaseWidgetUtils.createGroup( saveAsOuterComposite, Messages
            .getString( "ExportDsmlToWizardPage.SaveAs" ), 1 ); //$NON-NLS-1$
        Composite saveAsComposite = BaseWidgetUtils.createColumnContainer( saveAsGroup, 2, 1 );

        final Button saveAsDsmlResponseButton = BaseWidgetUtils.createRadiobutton( saveAsComposite, Messages
            .getString( "ExportDsmlToWizardPage.DSMLResponse" ), 2 ); //$NON-NLS-1$
        saveAsDsmlResponseButton.setSelection( true );
        wizard.setSaveAsType( ExportDsmlWizardSaveAsType.RESPONSE );
        saveAsDsmlResponseButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( saveAsDsmlResponseButton.getSelection() )
                {
                    wizard.setSaveAsType( ExportDsmlWizardSaveAsType.RESPONSE );
                }
            }
        } );
        BaseWidgetUtils.createRadioIndent( saveAsComposite, 1 );
        BaseWidgetUtils.createWrappedLabel( saveAsComposite, Messages
            .getString( "ExportDsmlToWizardPage.SearchSaveAsResponse" ), 1 ); //$NON-NLS-1$

        final Button saveAsDsmlRequestButton = BaseWidgetUtils.createRadiobutton( saveAsComposite, Messages
            .getString( "ExportDsmlToWizardPage.DSMLRequest" ), 2 ); //$NON-NLS-1$
        saveAsDsmlRequestButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( saveAsDsmlRequestButton.getSelection() )
                {
                    wizard.setSaveAsType( ExportDsmlWizardSaveAsType.REQUEST );
                }
            }
        } );
        BaseWidgetUtils.createRadioIndent( saveAsComposite, 1 );
        BaseWidgetUtils.createWrappedLabel( saveAsComposite, Messages
            .getString( "ExportDsmlToWizardPage.SearchSaveAsRequest" ), 1 ); //$NON-NLS-1$
    }


    // ── The DSML Landing Pad Accepts XML Files ────────────────────────────────────
    // DSML is always XML, so the file browser filters to *.xml and *.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the DSML save dialog.
     *
     * @return  {@code ["*.xml", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The DSML Format Name ──────────────────────────────────────────────────────
    // Error messages say "please enter a DSML file" — format-specific and clear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised format name "DSML" for use in page titles and error messages.
     *
     * @return  the string "DSML".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportDsmlToWizardPage.DSML" ); //$NON-NLS-1$
    }
}
