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
package org.apache.directory.studio.aciitemeditor.valueeditors;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: ExclusionDialog — ISB SUBTREE EXCLUSION ENTRY TERMINAL ─────────────
// A subtree specification can exclude certain branches via chopBefore or chopAfter
// exclusions.  The ISB terminal for these entries shows a type combo (chopBefore /
// chopAfter) and an entry DN picker so the officer specifies exactly where to chop.
// ExclusionDialog is that exclusion-entry terminal.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for entering a single subtree exclusion value.
 * An exclusion is an ACI construct of the form
 * {@code chopBefore: "ou=excluded,dc=example,dc=com"} or
 * {@code chopAfter: "..."}.
 * This dialog presents a read-only combo for the exclusion type and a DN
 * entry widget for the target DN.
 * Think of this as the ISB subtree exclusion terminal: pick the chop type,
 * pick the DN, done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class ExclusionDialog extends Dialog
{
    /** The connection. */
    private IBrowserConnection connection;

    /** The base. */
    private Dn base;

    /** The initial type. */
    private String initialType;

    /** The initial Dn */
    private String initalDN;

    /** The return type */
    private String returnType;

    /** The return Dn */
    private String returnDN;

    private static final String EMPTY = ""; //$NON-NLS-1$
    private static final String CHOP_BEFORE = "chopBefore"; //$NON-NLS-1$
    private static final String CHOP_AFTER = "chopAfter"; //$NON-NLS-1$

    // UI Fields
    private Combo typeCombo;
    private EntryWidget entryWidget;


    // ── OPEN THE EXCLUSION TERMINAL ───────────────────────────────────────────
    // The ISB terminal opens pre-filled by parsing the existing exclusion string
    // (format: {@code chopBefore: "dn"}).  If parsing fails, the fields are blank.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ExclusionDialog}, parsing {@code exclusion} to extract
     * the initial type and DN.
     *
     * <p>For example — editing an existing exclusion:</p>
     * <pre>
     *   ExclusionDialog dlg = new ExclusionDialog(
     *       shell, connection, baseDn, "chopAfter: \"ou=A\"");
     *   if (dlg.open() == Dialog.OK) {
     *     String type = dlg.getType();  // "chopAfter"
     *     String dn   = dlg.getDN();    // "ou=A"
     *   }
     * </pre>
     *
     * @param parentShell  the parent SWT shell
     * @param connection   the browser connection for the DN entry widget
     * @param base         the base DN for the entry widget's browse dialog
     * @param exclusion    the existing exclusion string to pre-parse, or empty
     */
    protected ExclusionDialog( Shell parentShell, IBrowserConnection connection, Dn base, String exclusion )
    {
        super( parentShell );
        this.connection = connection;
        this.base = base;

        try
        {
            // for example: chopAfter: "ou=A"
            Pattern pattern = Pattern.compile( "\\s*(chopBefore|chopAfter):\\s*\"(.*)\"\\s*" ); //$NON-NLS-1$
            Matcher matcher = pattern.matcher( exclusion );
            initialType = matcher.matches() ? matcher.group( 1 ) : EMPTY;
            initalDN = matcher.matches() ? matcher.group( 2 ) : EMPTY;
        }
        catch ( Exception e )
        {
            initialType = EMPTY;
            initalDN = EMPTY;
        }
    }


    // ── SET TITLE AND ICON ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ExclusionValueEditor.title" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "ExclusionValueEditor.icon" ) ) ); //$NON-NLS-1$
    }


    // ── COMMIT TYPE AND DN ────────────────────────────────────────────────────
    // Grand Moff confirms the chop type and DN; we save the DN history and
    // snapshot both fields before delegating to the superclass close.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {
        returnType = typeCombo.getText();
        returnDN = entryWidget.getDn().toString();

        // save dn history
        entryWidget.saveDialogSettings();

        super.okPressed();
    }


    // ── BUILD THE TWO-FIELD FORM ──────────────────────────────────────────────
    // The orderly lays out: a type label + read-only combo (chopBefore/chopAfter),
    // then a DN label + EntryWidget (DN picker with browse button).
    // The OK button is disabled until a valid non-empty DN is chosen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );
        composite.setLayout( new GridLayout( 3, false ) );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "ExclusionValueEditor.label.type" ), 1 ); //$NON-NLS-1$
        typeCombo = new Combo( composite, SWT.READ_ONLY );
        String[] types = new String[2];
        types[0] = CHOP_BEFORE;
        types[1] = CHOP_AFTER;

        ComboViewer typeComboViewer = new ComboViewer( typeCombo );
        typeComboViewer.setContentProvider( new ArrayContentProvider() );
        typeComboViewer.setLabelProvider( new LabelProvider() );
        typeComboViewer.setInput( types );
        typeComboViewer.setSelection( new StructuredSelection( CHOP_BEFORE ), true );
        typeComboViewer.setSelection( new StructuredSelection( initialType ), true );
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        gridData.horizontalAlignment = GridData.BEGINNING;
        typeCombo.setLayoutData( gridData );

        BaseWidgetUtils.createLabel( composite, Messages.getString( "ExclusionValueEditor.label.rdn" ), 1 ); //$NON-NLS-1$
        entryWidget = new EntryWidget( connection, null, base, true );
        entryWidget.createWidget( composite );
        try
        {
            Dn dn = new Dn( initalDN );
            entryWidget.setInput( connection, dn, base, true );
        }
        catch ( LdapInvalidDnException e )
        {
        }
        entryWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        validate();

        return composite;
    }


    // ── VALIDATE DN INPUT ─────────────────────────────────────────────────────
    // The OK button stays disabled until the entry widget holds a valid,
    // non-empty DN so the officer cannot commit a blank exclusion.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables the OK button only when the entry widget contains a valid, non-empty DN.
     */
    private void validate()
    {
        boolean valid = entryWidget.getDn() != null && entryWidget.getDn().size() > 0;

        if ( getButton( IDialogConstants.OK_ID ) != null )
        {
            getButton( IDialogConstants.OK_ID ).setEnabled( valid );
        }
    }


    // ── RETURN THE CHOP TYPE ──────────────────────────────────────────────────
    /**
     * Returns the exclusion type ({@code "chopBefore"} or {@code "chopAfter"})
     * selected in the combo, or {@code null} if the dialog was cancelled.
     *
     * @return the exclusion type string, or {@code null}
     */
    public String getType()
    {
        return returnType;
    }


    // ── RETURN THE TARGET DN ──────────────────────────────────────────────────
    /**
     * Returns the target DN entered in the entry widget, or {@code null} if the
     * dialog was cancelled.
     *
     * @return the DN string, or {@code null}
     */
    public String getDN()
    {
        return returnDN;
    }
}
