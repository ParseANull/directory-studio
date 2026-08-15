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


import java.util.HashMap;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: TextDialog — MON MOTHMA READS THE ALLIANCE MISSION BRIEFING ────────
// Mon Mothma stands at the podium and reads the Alliance mission briefing aloud:
// every line is plain human text, scrollable, wrappable.  She can toggle the
// briefing between "prose mode" (word-wrap on, comfortable reading) and "raw
// format mode" (word-wrap off, preserving exact line breaks for technical data).
// When she is done the briefing is sealed and returned to the archives.
// A TextDialog does exactly this: it presents a multi-line text area for viewing
// or editing a plain-text LDAP attribute value, with a "Wrap Lines" toggle
// button that switches between wrapped and unwrapped display modes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog with a scrollable, editable text area for plain-text LDAP attribute
 * values.  A "Wrap Lines" toggle button switches the text widget between
 * word-wrapped and horizontal-scrollbar modes without losing the current
 * content.  The dialog is also maximisable (SWT.MAX) so long values can be
 * read comfortably.
 * Think of this class as Mon Mothma reading the mission briefing: the text is
 * always there, just formatted for the current viewing mode.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TextDialog extends Dialog
{
    /** The dialog title. */
    private static final String DIALOG_TITLE = Messages.getString( "TextDialog.TextEditor" ); //$NON-NLS-1$

    /** The initial value. */
    private String initialValue;

    /** The return value. */
    private String returnValue;

    /** The button ID for the save button. */
    private static final int TOGGLE_BUTTON_ID = 9999;

    /**
     * Collection of buttons created by the <code>createButton</code> method.
     */
   private HashMap<Integer, Button> buttons = new HashMap<>();

    /** The text area. */
    private Text text;

    private int defaultTextStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;

    /** The check box to enable line wrap */
    GridData gd = new GridData();


    // ── MON MOTHMA TAKES THE PODIUM ───────────────────────────────────────────
    // Mon Mothma receives the briefing document — the initial text — and prepares
    // to read it.  She makes the podium resizable and maximisable so even the
    // longest Alliance briefing can be read without squinting.
    // We capture the initial text value, make the shell resizable and maximisable,
    // and initialise returnValue to null so callers know if the dialog was
    // cancelled without looking at the initial value.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new TextDialog pre-loaded with the given text.  The shell is
     * configured as resizable and maximisable so long attribute values can be
     * read comfortably.  {@code returnValue} starts as {@code null} and is only
     * set when the user confirms with OK.
     *
     * <p>For example — Mon Mothma takes the podium with the briefing document:</p>
     * <pre>
     *   TextDialog dialog = new TextDialog(shell, "This is the plan...");
     *   if (dialog.open() == OK) {
     *       String edited = dialog.getText();
     *   }
     * </pre>
     *
     * @param parentShell  the shell that owns this dialog
     * @param initialValue the text to display; must not be {@code null}
     */
    public TextDialog( Shell parentShell, String initialValue )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE | SWT.MAX );
        this.initialValue = initialValue;
        this.returnValue = null;
    }


    // ── MON MOTHMA ADDS A CUSTOM BUTTON TO THE PODIUM ────────────────────────
    // Mon Mothma's podium has a special toggle switch — "prose mode / raw mode"
    // — in addition to the standard "Approved" and "Dismissed" buttons.  The
    // switch is a toggle-style button, not a regular push button, so it stays
    // pressed when activated.  We need a custom createButton to support the
    // SWT.TOGGLE style alongside standard SWT.PUSH buttons.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a button in the button bar with the given SWT style.  The
     * overridden two-argument form delegates here, passing {@link SWT#PUSH} as
     * the default style.  This allows us to create the "Wrap Lines" toggle
     * button with {@link SWT#TOGGLE} style while keeping the standard OK/Cancel
     * buttons as normal push buttons.
     *
     * <p>For example — Mon Mothma installs the toggle switch next to OK:</p>
     * <pre>
     *   createButton(TOGGLE_BUTTON_ID, "Wrap Lines", false, SWT.TOGGLE);
     *   createButton(OK,              "OK",          false, SWT.PUSH);
     * </pre>
     *
     * @param parent        the button-bar composite
     * @param id            the button ID used in {@link #buttonPressed(int)}
     * @param label         the button label text
     * @param defaultButton {@code true} if this should be the shell's default button
     * @param style         the SWT button style, e.g. {@link SWT#PUSH} or {@link SWT#TOGGLE}
     * @return              the created {@link Button}
     */
    @Override
   protected Button createButton( Composite parent, int id, String label, boolean defaultButton )
    {
       return createButton( parent, id, label, defaultButton, SWT.PUSH );
   }

   protected Button createButton( Composite parent, int id, String label, boolean defaultButton, int style )
   {
       // increment the number of columns in the button bar
       ( ( GridLayout ) parent.getLayout() ).numColumns++;
       Button button = new Button( parent, style );
       button.setText( label );
       button.setFont( JFaceResources.getDialogFont() );
       button.setData( Integer.valueOf( id ) );
       button.addSelectionListener(
           new SelectionAdapter()
           {
               @Override
               public void widgetSelected( SelectionEvent event )
               {
                   buttonPressed( ( ( Integer ) event.widget.getData() ).intValue() );
               }
           });

       if ( defaultButton )
       {
           Shell shell = parent.getShell();

           if ( shell != null )
           {
               shell.setDefaultButton( button );
           }
       }

       buttons.put( Integer.valueOf( id ), button );
       setButtonLayoutData( button );

       return button;
   }


    // ── MON MOTHMA TITLES THE BRIEFING ROOM ──────────────────────────────────
    // A nameplate on the briefing room door: "Text Editor."  Visitors know they
    // are entering a plain-text editing session, not the hex viewer next door.
    // We set the window title and the text-editor icon on the shell.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Applies the "Text Editor" title and icon to the dialog shell.
     *
     * <p>For example — Mon Mothma labels the briefing room door:</p>
     * <pre>
     *   shell.setText("Text Editor");
     *   shell.setIcon(TEXT_EDITOR_ICON);
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( DIALOG_TITLE );
        shell.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_TEXTEDITOR ) );
    }


    // ── MON MOTHMA INSTALLS THE TOGGLE SWITCH ────────────────────────────────
    // Mon Mothma's custom podium needs a special button: the "Wrap Lines" toggle
    // that flips between prose mode and raw-format mode.  Standard dialog
    // infrastructure only creates SWT.PUSH buttons, so we override to add ours
    // before the standard OK/Cancel.
    // We add the TOGGLE-style Wrap button first, then OK and Cancel.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the button bar with the "Wrap Lines" toggle button, followed by the
     * standard OK and Cancel buttons.  The toggle button uses {@link SWT#TOGGLE}
     * style so it stays visually "pressed" when wrap mode is active.
     *
     * <p>For example — Mon Mothma installs the toggle switch on the podium:</p>
     * <pre>
     *   [Wrap Lines (toggle)]  [OK]  [Cancel]
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, TOGGLE_BUTTON_ID, Messages.getString( "TextDialog.WrapLines" ), false, SWT.TOGGLE ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── MON MOTHMA SWITCHES READING MODES ────────────────────────────────────
    // Mon Mothma presses the toggle switch: the briefing reflows from raw format
    // to prose mode (or back again).  The current text is captured before the
    // widget is destroyed and recreated with the new style — nothing is lost.
    // On the TOGGLE button we dispose the old Text widget and create a new one
    // with (or without) SWT.WRAP while preserving the current content.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Handles button presses.  When the "Wrap Lines" toggle button is clicked we
     * capture the current text, dispose the old {@link Text} widget, and
     * re-create it with or without {@link SWT#WRAP} depending on the toggle's
     * pressed state.  We then request a layout refresh so the new widget fits
     * the composite correctly.  OK and Cancel are handled by the superclass.
     *
     * <p>For example — Mon Mothma toggles prose mode:</p>
     * <pre>
     *   case TOGGLE:
     *       String current = text.getText();
     *       text.dispose();
     *       createText(composite, current, wrapButton.getSelection());
     *       text.requestLayout();
     * </pre>
     *
     * @param buttonId  the SWT button ID — {@code TOGGLE_BUTTON_ID}, {@link IDialogConstants#OK_ID}, or CANCEL
     */
    protected void buttonPressed( int buttonId )
    {
       if ( TOGGLE_BUTTON_ID == buttonId )
       {
           String currentValue = text.getText();
           Composite composite = text.getParent();
           text.dispose();
           createText(composite, currentValue, getButton( TOGGLE_BUTTON_ID ).getSelection() );
           text.requestLayout();
        }

        super.buttonPressed( buttonId );
    }


    // ── MON MOTHMA RETRIEVES A BUTTON BY ID ──────────────────────────────────
    // Mon Mothma's custom podium keeps its own button registry — the standard
    // Eclipse button map doesn't track our toggle button, so we maintain our own
    // HashMap and override getButton() to look there instead.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Button} with the given ID, looking up from our own
     * {@code buttons} map.  We override this because we register buttons using
     * a custom flow (including the toggle button) and the superclass map would
     * not have them all.
     *
     * <p>For example — Mon Mothma retrieves the wrap toggle:</p>
     * <pre>
     *   Button wrapButton = getButton(TOGGLE_BUTTON_ID);
     *   boolean isWrapped = wrapButton.getSelection();
     * </pre>
     *
     * @param id  the button ID to look up
     * @return    the {@link Button} registered under that ID, or {@code null} if not found
     */
    @Override
    protected Button getButton( int id )
    {
       return buttons.get( Integer.valueOf( id ) );
   }


    // ── MON MOTHMA SEALS THE FINAL BRIEFING ──────────────────────────────────
    // Mon Mothma finishes reading, seals the document, and hands it to the
    // Alliance archivist: "Here is the final text — file it."
    // On OK we capture the current text widget content as the return value so
    // callers can retrieve the edited string.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We read the current content of the text
     * widget and store it as {@code returnValue} — the caller retrieves it via
     * {@link #getText()}.
     *
     * <p>For example — Mon Mothma seals and files the briefing:</p>
     * <pre>
     *   returnValue = text.getText();
     *   super.okPressed();
     * </pre>
     */
    @Override
    protected void okPressed()
    {
        returnValue = text.getText();
        super.okPressed();
    }


    // ── MON MOTHMA UNFOLDS THE BRIEFING ON THE PODIUM ────────────────────────
    // Mon Mothma spreads the briefing document across the podium — a large text
    // area, scrollable, comfortable to read.  The initial text is loaded and the
    // composite is set to fill all available space.
    // We create the composite and call createText to build the initial Text widget
    // in unwrapped mode.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: a composite that fills all available space,
     * containing the scrollable text widget created by {@link #createText}.  The
     * widget starts in unwrapped (horizontal-scroll) mode.
     *
     * <p>For example — Mon Mothma unfolds the briefing:</p>
     * <pre>
     *   Composite composite = super.createDialogArea(parent);
     *   createText(composite, initialValue, false);  // unwrapped by default
     *   return composite;
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the composite containing the text area
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );

        composite.setLayoutData( new GridData( SWT.FILL,SWT.FILL,true,true ) );

        // text widget
        createText( composite, this.initialValue, false );

        return composite;
    }


    // ── MON MOTHMA RESETS THE BRIEFING FORMAT ────────────────────────────────
    // When Mon Mothma toggles the reading mode she tears off the old sheet,
    // reformats the same words onto a new sheet — wrapped or unwrapped — and
    // places it on the podium.  The content never changes, only the layout.
    // We create a new Text widget inside the given composite with or without
    // SWT.WRAP and apply the standard sizing hints.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates (or recreates) the {@link Text} widget inside {@code composite}
     * with the requested wrap mode.  Called both from {@link #createDialogArea}
     * (initial creation) and from {@link #buttonPressed} when the toggle fires.
     * The size hints ensure the widget starts large enough to be useful.
     *
     * <p>For example — Mon Mothma reformats the briefing sheet:</p>
     * <pre>
     *   if (wrap) text = new Text(composite, defaultStyle | SWT.WRAP);
     *   else      text = new Text(composite, defaultStyle);
     *   text.setText(value);
     *   text.setLayoutData(largeSizeHint);
     * </pre>
     *
     * @param composite  the parent composite that will own the new Text widget
     * @param value      the text content to display; replaces whatever was there before
     * @param wrap       {@code true} to enable word wrap; {@code false} for horizontal-scroll mode
     */
     protected void createText( Composite composite, String value, boolean wrap )
     {
         if ( wrap )
         {
             text = new Text( composite, defaultTextStyle | SWT.WRAP);
         }
         else
         {
             text = new Text( composite, defaultTextStyle );
         }

         text.setText( value );
         gd = new GridData( SWT.FILL,SWT.FILL,true,true );
         gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 2);
         gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
         text.setLayoutData( gd );
         applyDialogFont( composite );
    }


    // ── THE ARCHIVIST RETRIEVES THE SEALED BRIEFING ───────────────────────────
    // After Mon Mothma seals the document and the session ends, the archivist
    // asks for the final text to file.  If she cancelled mid-briefing, there is
    // nothing to file — null is returned.
    // Callers retrieve the edited text here after the dialog closes with OK.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the text as it stood when the user pressed OK, or {@code null} if
     * the user cancelled.  The returned value may differ from the initial value
     * if the user edited the text area.
     *
     * <p>For example — the archivist retrieves the sealed briefing:</p>
     * <pre>
     *   String result = dialog.getText();
     *   if (result != null) { attribute.setValue(result); }
     * </pre>
     *
     * @return  the final text content, or {@code null} if the dialog was cancelled
     */
    public String getText()
    {
        return returnValue;
    }
}
