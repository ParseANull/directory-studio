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


// ── CLASS: ExportCsvToWizardPage — YODA SETS DOWN THE X-WING ─────────────────
// Yoda decides where to set the X-wing down — a specific landing pad with the
// right surface. The CSV To page picks the landing pad: a .csv or .txt file.
// It also includes a shortcut link to the CSV text-format preferences so the
// user can control delimiters and quoting before committing to the export.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "To" page of the CSV export wizard: picks the destination CSV file.
 * Extends {@link ExportBaseToPage} with CSV-specific file extension filters
 * (*.csv, *.txt, *) and a "See Text Formats" hyperlink that opens the CSV
 * tab of the text-format preference page.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCsvToWizardPage extends ExportBaseToPage
{

    /** The extensions used by CSV files */
    private static final String[] EXTENSIONS = new String[]
        { "*.csv", "*.txt", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


    // ── Yoda Checks the Landing Pad Requirements ──────────────────────────────────
    // A CSV X-wing can only land on a CSV pad — the wizard icon marks the format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportCsvToWizardPage with the CSV wizard icon set.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportCsvToWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard );
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_CSV_WIZARD ) );
    }


    // ── Yoda Surveys the Landing Zone ────────────────────────────────────────────
    // The landing zone includes the standard file-selector from the base class,
    // plus a shortcut to the CSV formatting preferences so users can configure
    // delimiters before they export.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using the base-class file browser, then adds a
     * "See Text Formats" hyperlink that opens the CSV tab of the text-format
     * preference page so the user can confirm delimiter settings before exporting.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );

        BaseWidgetUtils.createSpacer( composite, 3 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        String text = Messages.getString( "ExportCsvToWizardPage.SeeTextFormats" ); //$NON-NLS-1$
        Link link = BaseWidgetUtils.createLink( composite, text, 2 );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(), BrowserUIConstants.PREFERENCEPAGEID_TEXTFORMATS,
                    null, TextFormatsPreferencePage.CSV_TAB ).open();
            }
        } );
    }


    // ── The Landing Pad Accepts These Surfaces ────────────────────────────────────
    // CSV files land on *.csv, *.txt, or * pads.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the CSV save dialog.
     *
     * @return  {@code ["*.csv", "*.txt", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The Landing Pad Has a Format Name ────────────────────────────────────────
    // Error messages say "please enter a CSV file" — not a generic file message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised file-format name "CSV" used in page titles and
     * error messages.
     *
     * @return  the string "CSV".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportCsvToWizardPage.CVS" ); //$NON-NLS-1$
    }

}
