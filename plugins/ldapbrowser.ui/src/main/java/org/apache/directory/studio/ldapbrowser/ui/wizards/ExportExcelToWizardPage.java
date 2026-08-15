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


// ── CLASS: ExportExcelToWizardPage — YODA SETS THE EXCEL X-WING DOWN ─────────
// Yoda sets the X-wing down on the Excel landing pad: a *.xls file.
// The page includes a "See Text Formats" link that opens the XLS preference
// tab and a warning that Excel has row/column limits — so Yoda reminds us
// that even the Force has practical constraints when dealing with Office formats.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "To" page of the Excel export wizard: picks the destination .xls file.
 * Extends {@link ExportBaseToPage} with Excel-specific extension filters (*.xls, *),
 * a "See Text Formats" link for the XLS preference tab, and a warning label
 * about Excel's row/column limits.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportExcelToWizardPage extends ExportBaseToPage
{

    /** The extensions used by Excel files */
    private static final String[] EXTENSIONS = new String[]
        { "*.xls", "*" }; //$NON-NLS-1$ //$NON-NLS-2$


    // ── Yoda Checks the Excel Pad Requirements ────────────────────────────────────
    // The Excel wizard icon marks the format; extensions filter to *.xls.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportExcelToWizardPage with the Excel wizard icon.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportExcelToWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard );
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_XLS_WIZARD ) );
    }


    // ── Yoda Surveys the Excel Landing Zone ───────────────────────────────────────
    // Beyond the standard file selector, we add a link to Excel formatting
    // preferences and a warning about Excel's inherent row/column count limits.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using the base-class file browser, then adds:
     * a "See Text Formats" hyperlink to the XLS preference tab, and a
     * wrapped warning label reminding the user that Excel has limits on the
     * number of rows and columns that can be exported.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );

        BaseWidgetUtils.createSpacer( composite, 3 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        String text = Messages.getString( "ExportExcelToWizardPage.SeeTextFormats" ); //$NON-NLS-1$
        Link link = BaseWidgetUtils.createLink( composite, text, 2 );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(), BrowserUIConstants.PREFERENCEPAGEID_TEXTFORMATS,
                    null, TextFormatsPreferencePage.XLS_TAB ).open();
            }
        } );

        BaseWidgetUtils.createSpacer( composite, 3 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createWrappedLabel( composite, Messages.getString( "ExportExcelToWizardPage.WarningExcel" ), 2 ); //$NON-NLS-1$
    }


    // ── The Excel Pad Accepts These Surfaces ──────────────────────────────────────
    // Only *.xls and *.* are valid Excel landing pads.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the Excel save dialog.
     *
     * @return  {@code ["*.xls", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The Excel Format Name ─────────────────────────────────────────────────────
    // Error messages reference the "Excel" format by name.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised format name "Excel" for use in page titles and
     * error messages.
     *
     * @return  the string "Excel".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportExcelToWizardPage.Excel" ); //$NON-NLS-1$
    }

}
