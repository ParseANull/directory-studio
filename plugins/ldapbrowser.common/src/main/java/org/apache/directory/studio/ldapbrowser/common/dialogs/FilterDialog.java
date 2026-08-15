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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.filtereditor.FilterSourceViewerConfiguration;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: FilterDialog — R2-D2 AT THE DEATH STAR TERMINAL ───────────────────
// R2-D2 plugs into the Death Star's computer terminal, navigating menus and
// data streams to find and send the right command.  He has a full text interface
// — he can read what's on screen, auto-complete known system IDs, and format the
// command string before firing it at the target system.
// An LDAP filter is just a command string to the directory server: it tells the
// server which entries to return.  This dialog gives the user a rich text editor
// for that filter — syntax highlighting, auto-complete from the schema, and a
// Format button that pretty-prints the filter so it's easy to read.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A full-featured dialog for editing LDAP search filters.  It hosts a JFace
 * {@link SourceViewer} with syntax highlighting and content-assist (schema-
 * aware attribute name completion), plus a Format button that pretty-prints the
 * filter expression for easier reading.
 * Think of this class as R2-D2 at the Death Star terminal: full text access,
 * smart auto-complete, and the ability to clean up a messy command string on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterDialog extends Dialog
{

    /** The default dialog title. */
    private static final String DIALOG_TITLE = Messages.getString( "FilterDialog.FilterEditor" ); //$NON-NLS-1$

    /** The button ID for the format button. */
    private static final int FORMAT_BUTTON_ID = 987654321;

    /** The dialog title. */
    private String title;

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The source viewer. */
    private SourceViewer sourceViewer;

    /** The filter source viewer configuration. */
    private FilterSourceViewerConfiguration configuration;

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The filter. */
    private String filter;


    // ── R2 BOOTS UP AND JACKS IN ─────────────────────────────────────────────
    // R2-D2 rolls up to the terminal, extends his interface probe, and powers
    // on — ready to accept the filter command he's been handed.
    // We capture the initial filter text, the directory connection (for schema-
    // aware completion), and configure the shell to be resizable so long filter
    // strings are comfortable to read and edit.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FilterDialog, wiring up the initial filter text and the
     * LDAP connection needed for schema-aware content-assist.  The shell style
     * is set to resizable so the user can widen the dialog for long filters.
     *
     * <p>For example — R2 jacks into the terminal with mission parameters:</p>
     * <pre>
     *   FilterDialog dialog = new FilterDialog(shell, "Edit Filter",
     *       "(&(objectClass=person)(uid=l*))", connection);
     *   if (dialog.open() == OK) { String result = dialog.getFilter(); }
     * </pre>
     *
     * @param parentShell        the shell that owns this dialog
     * @param title              dialog title shown in the title bar; if {@code null} we use the default "Filter Editor"
     * @param filter             the LDAP filter to pre-populate the editor with; may be empty but not {@code null}
     * @param brwoserConnection  the browser connection used to power schema-aware attribute name completion
     */
    public FilterDialog( Shell parentShell, String title, String filter, IBrowserConnection brwoserConnection )
    {
        super( parentShell );
        this.title = title;
        this.filter = filter;
        this.browserConnection = brwoserConnection;
        this.parser = new LdapFilterParser();
        setShellStyle( SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
    }


    // ── R2 HANDS BACK THE FINISHED COMMAND ───────────────────────────────────
    // The Death Star systems accept the command; R2 extracts the final formatted
    // string from the terminal and passes it back to Luke and the Rebels.
    // Callers retrieve the edited, normalised filter string here after the dialog
    // closes with OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filter string as it stood when the user last pressed OK.  The
     * string is normalised by the LDAP filter parser (e.g. extra whitespace is
     * removed), so it may differ slightly from what the user typed.  Returns the
     * original filter passed to the constructor if the user cancelled.
     *
     * <p>For example — R2 extracts the accepted command string:</p>
     * <pre>
     *   String filter = dialog.getFilter();
     *   // "(&(objectClass=person)(uid=l*))" — ready to send to the server
     * </pre>
     *
     * @return  the edited and normalised LDAP filter string
     */
    public String getFilter()
    {
        return filter;
    }


    // ── R2 LABELS HIS TERMINAL SESSION ───────────────────────────────────────
    // R2 stamps the session header on the display so any observer can see at a
    // glance: "This is the filter editor — not the trash compactor controls."
    // We set the window title and the filter-editor icon.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the title and icon to the dialog shell before it is shown.  We
     * fall back to the default "Filter Editor" title if the caller passed
     * {@code null}.
     *
     * <p>For example — R2 labels the terminal session:</p>
     * <pre>
     *   shell.setText(title != null ? title : "Filter Editor");
     *   shell.setIcon(FILTER_EDITOR_ICON);
     * </pre>
     *
     * @param newShell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( title != null ? title : DIALOG_TITLE );
        newShell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_FILTER_EDITOR ) );
    }


    // ── R2 EXECUTES OR PRETTY-PRINTS THE COMMAND ─────────────────────────────
    // R2 has two jobs at the terminal: send the command for real (OK), or just
    // reformat it to be easier to read (Format).  He knows which button was
    // pressed and acts accordingly.
    // On OK we parse the raw text and normalise it; on Format we reflow the
    // filter through the content formatter without closing the dialog.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Handles button presses.  On OK we parse the editor contents through the
     * LDAP filter parser and store the normalised filter.  On the Format button
     * we reformat the current document in-place using the configured content
     * formatter — the dialog stays open and the user can keep editing.  All
     * other button IDs (Cancel) are forwarded to the superclass.
     *
     * <p>For example — R2 picks the right action for each button:</p>
     * <pre>
     *   case OK:     filter = parser.parse(editor.getText()).toString();
     *   case FORMAT: formatter.format(editor.getDocument());  // stays open
     *   default:     super.buttonPressed(id);
     * </pre>
     *
     * @param buttonId  the SWT button ID — {@link IDialogConstants#OK_ID}, {@link #FORMAT_BUTTON_ID}, or CANCEL
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            parser.parse( sourceViewer.getDocument().get() );
            filter = parser.getModel().toString();
        }
        else if ( buttonId == FORMAT_BUTTON_ID )
        {
            IRegion region = new Region( 0, sourceViewer.getDocument().getLength() );
            configuration.getContentFormatter( sourceViewer ).format( sourceViewer.getDocument(), region );
        }

        // call super implementation
        super.buttonPressed( buttonId );
    }


    // ── R2 ADDS THE FORMAT BUTTON TO HIS CONTROL PANEL ───────────────────────
    // R2 doesn't just have an execute button — he also has a "pretty-print"
    // function so any Rebel can read the command at a glance.  He slots it in
    // next to OK and Cancel on his control panel.
    // We add the Format button to the standard button bar alongside OK/Cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the button bar with an extra Format button appended after OK and
     * Cancel.  The Format button triggers in-place reformatting of the filter
     * text without closing the dialog.
     *
     * <p>For example — R2 adds the pretty-print button to his panel:</p>
     * <pre>
     *   [OK]  [Cancel]  [Format]   ← Format added to the right
     * </pre>
     *
     * @param parent  the composite Eclipse wants us to place the buttons in
     * @return        the completed button-bar control
     */
    protected Control createButtonBar( Composite parent )
    {
        Composite composite = ( Composite ) super.createButtonBar( parent );
        super.createButton( composite, FORMAT_BUTTON_ID, Messages.getString( "FilterDialog.Format" ), false ); //$NON-NLS-1$
        return composite;
    }


    // ── R2 DISPLAYS THE TERMINAL SCREEN ──────────────────────────────────────
    // R2 projects the terminal display — the full text interface with scrollbars,
    // syntax colouring for known command tokens, and a content-assist popup that
    // suggests valid attribute names from the Death Star's own schema.
    // We create the SourceViewer, configure it with the LDAP filter language, load
    // the initial filter text, and immediately run the formatter so the user sees
    // a nicely indented starting point rather than a wall of brackets.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the main content area: a scrollable, syntax-highlighted source
     * viewer pre-loaded with the initial filter.  We also run the content
     * formatter immediately so the filter is readable from the first moment the
     * dialog opens.  Focus is moved to the viewer's text widget so the user can
     * start typing straight away.
     *
     * <p>For example — R2 boots up the full terminal display:</p>
     * <pre>
     *   sourceViewer = new SourceViewer(composite, ruler, H_SCROLL | V_SCROLL);
     *   sourceViewer.configure(filterConfiguration);   // syntax + completion
     *   sourceViewer.setDocument(new Document(filter));
     *   formatter.format(document);                    // pre-indent
     *   sourceViewer.getTextWidget().setFocus();
     * </pre>
     *
     * @param parent  the parent composite Eclipse hands us
     * @return        the top-level composite containing the source viewer
     */
    protected Control createDialogArea( Composite parent )
    {
        // Composite composite = parent;
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        // create and configure source viewer
        sourceViewer = new SourceViewer( composite, new VerticalRuler( 0 ), SWT.H_SCROLL | SWT.V_SCROLL );
        sourceViewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );
        configuration = new FilterSourceViewerConfiguration( parser, browserConnection );
        sourceViewer.configure( configuration );

        // set document
        IDocument document = new Document( filter );
        sourceViewer.setDocument( document );

        // preformat
        IRegion region = new Region( 0, sourceViewer.getDocument().getLength() );
        configuration.getContentFormatter( sourceViewer ).format( sourceViewer.getDocument(), region );

        // set focus to the source viewer
        sourceViewer.getTextWidget().setFocus();

        return composite;
    }

}
