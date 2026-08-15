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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.FilterWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: FilterWidgetDialog — R2 SCANNING SENSOR DATA ──────────────────────
// R2-D2 sweeps his sensor array across the Death Star's data feeds, building up
// the right search expression to locate what the Rebels need.  Each scan narrows
// down the results — too broad and he drowns in noise, too narrow and he misses
// the target.  He validates the expression in real time and refuses to fire it
// until the syntax checks out.
// A FilterWidgetDialog does exactly this: it wraps the compact {@link FilterWidget}
// (a combo with history) inside a dialog, validates the filter in real time,
// shows an error hint if the syntax is wrong, and only enables OK when the
// expression is clean.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that lets the user enter an LDAP search filter using the
 * {@link FilterWidget} — a compact combo box that remembers recent filters and
 * supports schema-aware content-assist.  Unlike the heavier {@link FilterDialog},
 * this one doesn't have a full source viewer or a Format button; it is intended
 * for situations where a quick filter entry is needed without a lot of ceremony.
 * Think of it as R2 scanning sensor data to build the right filter expression —
 * fast, focused, and validated before it fires.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterWidgetDialog extends Dialog
{

    /** The title */
    private String title;

    /** The connection, used for attribute completion. */
    private IBrowserConnection connection;

    /** The filter widget. */
    private FilterWidget filterWidget;

    /** The filter. */
    private String filter;

    /** The error message label. */
    private Label errorMessageLabel;


    // ── R2 POWERS UP HIS SENSOR ARRAY ────────────────────────────────────────
    // R2 initialises his sensor suite with a pre-loaded search pattern so he
    // doesn't start from scratch — if he already knows roughly what to look for
    // he starts there and refines it.
    // We capture the initial filter text, the connection for schema completion,
    // and configure the shell to be resizable so longer filter strings don't get
    // cut off.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FilterWidgetDialog ready to let the user enter or refine an
     * LDAP filter.  The initial filter is pre-loaded into the widget so the user
     * can see and edit their previous expression rather than starting from blank.
     *
     * <p>For example — R2 loads the last known search pattern:</p>
     * <pre>
     *   FilterWidgetDialog dialog = new FilterWidgetDialog(
     *       shell, "Refine Filter", "(uid=l*)", connection);
     *   if (dialog.open() == OK) { String result = dialog.getFilter(); }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param title        dialog title text shown in the title bar
     * @param filter       the initial LDAP filter to pre-populate; may be {@code null} or empty
     * @param connection   the LDAP browser connection for schema-aware attribute name completion
     */
    public FilterWidgetDialog( Shell parentShell, String title, String filter, IBrowserConnection connection )
    {
        super( parentShell );
        this.title = title;
        this.filter = filter;
        this.connection = connection;
        setShellStyle( SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
    }


    // ── R2 REPORTS THE FINAL SCAN RESULT ─────────────────────────────────────
    // After the scan R2 transmits the cleaned-up search expression back to the
    // Rebels so they can issue the actual query to the Death Star's systems.
    // Callers retrieve the accepted filter string here after OK is pressed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP filter string that was active when the user pressed OK.
     * If the user cancelled the dialog this returns the original filter that was
     * passed to the constructor (i.e. no change).
     *
     * <p>For example — R2 transmits the final scan pattern:</p>
     * <pre>
     *   String filter = dialog.getFilter();
     *   // "(objectClass=groupOfNames)" — ready to use in an LDAP search
     * </pre>
     *
     * @return  the current LDAP filter string; never {@code null} after OK
     */
    public String getFilter()
    {
        return filter;
    }


    // ── R2 LABELS HIS SENSOR DISPLAY ─────────────────────────────────────────
    // R2 stamps the display header so everyone on the bridge knows what they are
    // looking at — "Filter Sensor Array, not navigation controls."
    // We apply the window title and the filter-editor icon to the shell.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the title text and icon to the dialog shell before it is shown.
     * The icon visually identifies this as a filter-editing dialog rather than a
     * generic input box.
     *
     * <p>For example — R2 labels the sensor display:</p>
     * <pre>
     *   shell.setText("Refine Filter");
     *   shell.setIcon(FILTER_EDITOR_ICON);
     * </pre>
     *
     * @param newShell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( title );
        newShell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_FILTER_EDITOR ) );
    }


    // ── R2 LOCKS IN THE SCAN PARAMETERS ──────────────────────────────────────
    // R2 locks the scan pattern into the targeting computer and saves it to his
    // history log so he can recall it instantly on the next mission.
    // On OK we capture the current filter text and persist it to the dialog's
    // history so it appears in the combo's drop-down next time.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK.  We read the current filter text from the
     * widget and save it to the dialog settings (history) so the user's recent
     * filters appear in future combo drop-downs.  On Cancel we do nothing — the
     * superclass handles closing.
     *
     * <p>For example — R2 locks and logs the scan pattern:</p>
     * <pre>
     *   filter = filterWidget.getFilter();   // capture
     *   filterWidget.saveDialogSettings();   // persist to history
     * </pre>
     *
     * @param buttonId  the ID of the pressed button — {@link IDialogConstants#OK_ID} or CANCEL
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            filter = filterWidget.getFilter();
            filterWidget.saveDialogSettings();
        }

        // call super implementation
        super.buttonPressed( buttonId );
    }


    // ── R2 DEPLOYS THE SENSOR ARRAY ──────────────────────────────────────────
    // R2 unfolds his sensor suite — the compact combo field with history drop-
    // down, the attribute auto-complete, and the status indicator that shows
    // whether the current expression is syntactically valid.
    // We build the FilterWidget, wire up a change listener that runs validation
    // on every keystroke, and create the error label underneath.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the content area: a {@link FilterWidget} with schema-aware
     * auto-complete and a small error label below it.  Every time the filter
     * text changes we call {@link #validate()} to update the OK button and the
     * error label in real time.
     *
     * <p>For example — R2 deploys his sensor array:</p>
     * <pre>
     *   filterWidget = new FilterWidget(initialFilter);
     *   filterWidget.setBrowserConnection(connection);   // schema for completion
     *   filterWidget.addModifyListener(() -> validate()); // live validation
     *   errorLabel = createLabel("Enter a valid LDAP filter");
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the top-level composite containing the filter widget and error label
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        Composite inner = new Composite( composite, SWT.NONE );
        GridLayout gridLayout = new GridLayout( 2, false );
        inner.setLayout( gridLayout );
        gd = new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        inner.setLayoutData( gd );

        filterWidget = new FilterWidget( filter != null ? filter : "" ); //$NON-NLS-1$
        filterWidget.createWidget( inner );
        filterWidget.setBrowserConnection( connection );
        filterWidget.setFocus();
        filterWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        errorMessageLabel = BaseWidgetUtils.createLabel( inner, Messages
            .getString( "FilterWidgetDialog.EnterValidFilter" ), 2 ); //$NON-NLS-1$

        validate();

        return composite;
    }


    // ── R2 CHECKS WHETHER THE SCAN EXPRESSION IS VALID ───────────────────────
    // Before firing the sensor array R2 runs a syntax check — garbage in means
    // garbage out, and the Death Star won't respond to a malformed query.  He
    // lights up the FIRE button only when the expression is clean, and shows a
    // status message explaining what is wrong otherwise.
    // We enable the OK button only when the FilterWidget reports a non-null
    // (syntactically valid) filter, and toggle the error label accordingly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current filter text and updates the UI accordingly.  If the
     * {@link FilterWidget} returns a non-null filter the expression is
     * syntactically valid — we enable OK and clear the error label.  If the
     * widget returns {@code null} the filter is malformed — we disable OK and
     * show the "enter a valid filter" hint.
     *
     * <p>For example — R2 checks the scan expression:</p>
     * <pre>
     *   if (filterWidget.getFilter() != null) {
     *       okButton.setEnabled(true);
     *       errorLabel.setText("");
     *   } else {
     *       okButton.setEnabled(false);
     *       errorLabel.setText("Enter a valid LDAP filter");
     *   }
     * </pre>
     */
    protected void validate()
    {
        if ( getButton( IDialogConstants.OK_ID ) != null )
        {
            getButton( IDialogConstants.OK_ID ).setEnabled( filterWidget.getFilter() != null );
        }
        errorMessageLabel.setText( filterWidget.getFilter() == null ? Messages
            .getString( "FilterWidgetDialog.EnterValidFilter" ) : "" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
