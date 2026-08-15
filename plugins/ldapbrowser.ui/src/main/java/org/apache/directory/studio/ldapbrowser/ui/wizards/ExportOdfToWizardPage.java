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
import org.apache.directory.studio.ldapbrowser.common.dialogs.preferences.TextFormatsPreferencePage;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: ExportOdfToWizardPage — YODA SETS THE ODF X-WING DOWN ─────────────
// Yoda sets the X-wing on the ODS landing pad: a *.ods Open Document Spreadsheet.
// Like the Excel To page, it includes a "See Text Formats" link and an ODF-specific
// warning about row/column limits — because even LibreOffice has boundaries,
// though admittedly more generous than Excel's.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "To" page of the ODF export wizard: picks the destination .ods file.
 * Extends {@link ExportBaseToPage} with ODF extension filters (*.ods, *),
 * a "See Text Formats" hyperlink for the ODF preference tab, and a warning
 * label about ODF spreadsheet limits.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportOdfToWizardPage extends ExportBaseToPage
{

    /** The extensions used by ODF files */
    private static final String[] EXTENSIONS = new String[]
        { "*.ods", "*" }; //$NON-NLS-1$ //$NON-NLS-2$


    // ── Yoda Checks the ODS Pad Requirements ─────────────────────────────────────
    // The ODF wizard icon marks the format; extensions filter to *.ods.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportOdfToWizardPage with the ODF wizard icon.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportOdfToWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard );
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_ODF_WIZARD ) );
    }


    // ── Yoda Surveys the ODS Landing Zone ────────────────────────────────────────
    // Standard file-selector plus a text-format preferences link and an ODF
    // limit warning — mirrors the Excel To page structure exactly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using the base-class file browser, then adds:
     * a "See Text Formats" hyperlink to the ODF preference tab, and a wrapped
     * warning label about ODF spreadsheet row/column limits.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );

        BaseWidgetUtils.createSpacer( composite, 3 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        String text = Messages.getString( "ExportOdfToWizardPage.SeeTextFormats" ); //$NON-NLS-1$
        Link link = BaseWidgetUtils.createLink( composite, text, 2 );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(), BrowserUIConstants.PREFERENCEPAGEID_TEXTFORMATS,
                    null, TextFormatsPreferencePage.ODF_TAB ).open();
            }
        } );

        BaseWidgetUtils.createSpacer( composite, 3 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createWrappedLabel( composite, Messages.getString( "ExportOdfToWizardPage.WarningOdf" ), 2 ); //$NON-NLS-1$
    }


    // ── The ODS Pad Accepts These Extensions ──────────────────────────────────────
    // ODF spreadsheets use *.ods extension.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the ODF save dialog.
     *
     * @return  {@code ["*.ods", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The ODF Format Name ───────────────────────────────────────────────────────
    // Error messages say "please enter an ODF file" — clear and specific.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised format name "ODF" for use in page titles and
     * error messages.
     *
     * @return  the string "ODF".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportOdfToWizardPage.Odf" ); //$NON-NLS-1$
    }

}
