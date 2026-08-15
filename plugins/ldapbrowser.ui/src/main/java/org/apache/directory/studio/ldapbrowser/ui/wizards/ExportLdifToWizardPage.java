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


// ── CLASS: ExportLdifToWizardPage — C-3PO DELIVERS THE LDIF SCROLL ───────────
// C-3PO rolls up the finished LDIF scroll and delivers it to the specified
// destination. The page asks for a *.ldif file path and includes a shortcut
// to the LDIF text-format preferences so users can configure line endings
// and encoding before they export.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "To" page of the LDIF export wizard: picks the destination .ldif file.
 * Extends {@link ExportBaseToPage} with LDIF-specific extension filters (*.ldif, *)
 * and a "See Text Formats" hyperlink that opens the LDIF tab of the text-format
 * preference page for configuring line separators and encoding.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportLdifToWizardPage extends ExportBaseToPage
{

    /** The extensions used by LDIF files */
    private static final String[] EXTENSIONS = new String[]
        { "*.ldif", "*" }; //$NON-NLS-1$ //$NON-NLS-2$


    // ── C-3PO Checks the LDIF Delivery Point ─────────────────────────────────────
    // The delivery point must accept LDIF files; the wizard icon marks the format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportLdifToWizardPage with the LDIF wizard icon.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportLdifToWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard );
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_LDIF_WIZARD ) );
    }


    // ── C-3PO Lays Out the LDIF Delivery Panel ────────────────────────────────────
    // The standard file-selector from the base class is shown, plus a shortcut
    // to the LDIF text-format preferences for configuring line endings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using the base-class file browser, then adds a
     * "See Text Formats" hyperlink that opens the LDIF tab of the text-format
     * preference page so the user can configure line separators before exporting.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );

        BaseWidgetUtils.createSpacer( composite, 3 );

        BaseWidgetUtils.createSpacer( composite, 1 );
        String text = Messages.getString( "ExportLdifToWizardPage.SeeTextFormats" ); //$NON-NLS-1$
        Link link = BaseWidgetUtils.createLink( composite, text, 2 );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(), BrowserUIConstants.PREFERENCEPAGEID_TEXTFORMATS,
                    null, TextFormatsPreferencePage.LDIF_TAB ).open();
            }
        } );
    }


    // ── The LDIF Pad Accepts These Extensions ────────────────────────────────────
    // LDIF files land on *.ldif or *.* pads.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the LDIF save dialog.
     *
     * @return  {@code ["*.ldif", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The LDIF Format Name ──────────────────────────────────────────────────────
    // Error messages say "please enter an LDIF file" — specific and clear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised format name "LDIF" for use in page titles and
     * error messages.
     *
     * @return  the string "LDIF".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportLdifToWizardPage.LDIF" ); //$NON-NLS-1$
    }

}
