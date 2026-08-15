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

package org.apache.directory.studio.common.ui.widgets;


import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;


// ── CLASS: BaseWidgetUtils — C-3PO ASSEMBLING REBEL WORKSHOP COMPONENTS ───────
// C-3PO knows exactly which tool from the Rebel workshop to grab for any job.
// Whether you need a group panel, a text field, a password box, a combo, a
// button, or a spacer — we have a factory method for it.  We handle all the
// GridData and GridLayout boilerplate so the rest of the codebase can ask for
// a widget by name and get a correctly-configured SWT control without writing
// ten lines of layout code every time.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide a comprehensive set of static factory methods that create and
 * configure common SWT widgets.  Every method applies the standard GridData
 * and layout settings that match the rest of the application's visual
 * language, so you never have to repeat the boilerplate yourself.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BaseWidgetUtils
{
    // ── METHOD createGroup — BUILDING A LABELED PANEL ────────────────────────
    // C-3PO frames off a section of the workshop with a labeled border panel,
    // giving a visual group for related controls.  The group fills both
    // directions and spans the requested number of columns.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Group} that fills its parent both horizontally and
     * vertically and spans the given number of columns.  The group uses a
     * single-column GridLayout internally.  Pass a non-null {@code label} to
     * show a titled border.
     *
     * @param parent the parent composite
     * @param label  the group's border title, or {@code null} for no title
     * @param span   the number of columns this group should span
     * @return the configured Group
     */
    public static Group createGroup( Composite parent, String label, int span )
    {
        Group group = new Group( parent, SWT.NONE );
        GridData gridData = new GridData( GridData.FILL_BOTH );
        gridData.horizontalSpan = span;
        group.setLayoutData( gridData );

        if ( label != null )
        {
            group.setText( label );
        }

        group.setLayout( new GridLayout() );

        return group;
    }


    // ── METHOD createColumnContainer (simple) — PARTITIONING THE WORKSHOP ────
    // C-3PO partitions the workshop into columns using a zero-margin GridLayout.
    // This two-argument overload creates columns of unequal width — pass true
    // for equal-width columns when you need a more uniform grid.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Composite} with a zero-margin {@link GridLayout} of
     * the given column count.  The composite fills its parent horizontally and
     * spans the requested number of parent columns.  Columns are not forced to
     * equal width.
     *
     * @param parent      the parent composite
     * @param columnCount the number of columns in the new layout
     * @param span        the number of parent columns this composite spans
     * @return the configured Composite
     */
    public static Composite createColumnContainer( Composite parent, int columnCount, int span )
    {
        return createColumnContainer( parent, columnCount, false, span );
    }


    // ── METHOD createColumnContainer (with equalWidth flag) — FLEXIBLE GRID ───
    // C-3PO can lay out the workshop partitions as either equal-width bays or
    // variable-width bays depending on the job.  This overload exposes the
    // makeColumnsEqualWidth flag for callers that need a uniform grid.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Composite} with a zero-margin {@link GridLayout} of
     * the given column count, optionally making all columns the same width.
     * The composite fills its parent horizontally and spans the given number
     * of parent columns.
     *
     * @param parent                 the parent composite
     * @param columnCount            the number of columns
     * @param makeColumnsEqualWidth  {@code true} to force equal column widths
     * @param span                   the number of parent columns to span
     * @return the configured Composite
     */
    public static Composite createColumnContainer( Composite parent, int columnCount, boolean makeColumnsEqualWidth,
        int span )
    {
        Composite container = new Composite( parent, SWT.NONE );
        GridLayout gridLayout = new GridLayout( columnCount, makeColumnsEqualWidth );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        container.setLayout( gridLayout );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        container.setLayoutData( gridData );

        return container;
    }


    // ── METHOD createLabel — STICKING A LABEL ON THE WORKSHOP SHELF ──────────
    // C-3PO prints a label and slaps it on the designated shelf.  The label
    // is not wrapped and simply displays its text with no extra styling.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a plain {@link Label} with the given text that spans the
     * requested number of columns in its parent's grid layout.
     *
     * @param parent the parent composite
     * @param text   the text to display in the label
     * @param span   the number of columns this label spans
     * @return the configured Label
     */
    public static Label createLabel( Composite parent, String text, int span )
    {
        Label label = new Label( parent, SWT.NONE );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        label.setLayoutData( gridData );
        label.setText( text );

        return label;
    }


    // ── METHOD createWrappedLabel — PRINTING A MULTI-LINE LABEL ─────────────
    // C-3PO prints a longer annotation that wraps across multiple lines when
    // the text is too wide for a single row.  A width hint of 100 pixels is
    // set so the wrapping threshold is predictable.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a word-wrapping {@link Label} that fills its parent
     * horizontally and spans the requested columns.  A {@code widthHint} of
     * 100 is applied so the label has a stable wrapping point.
     *
     * @param parent the parent composite
     * @param text   the text to display, which may wrap across lines
     * @param span   the number of columns this label spans
     * @return the configured Label
     */
    public static Label createWrappedLabel( Composite parent, String text, int span )
    {
        Label label = new Label( parent, SWT.WRAP );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        gridData.widthHint = 100;
        label.setLayoutData( gridData );
        label.setText( text );

        return label;
    }


    // ── METHOD createText (simple) — INSTALLING A STANDARD INPUT TERMINAL ────
    // C-3PO installs a standard editable text terminal — no password masking,
    // no fixed width, just a plain bordered field that fills its column.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a bordered, editable {@link Text} field that fills its parent
     * horizontally and spans the given number of columns.  The initial text
     * is set to the given value.
     *
     * @param parent the parent composite
     * @param text   the initial text content
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE | SWT.BORDER );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        textWidget.setLayoutData( gridData );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createText (with width) — INSTALLING A FIXED-WIDTH TERMINAL ───
    // C-3PO installs a text terminal calibrated for a specific character width —
    // handy when you need the input field to match a known maximum length like
    // a port number or a short code.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a bordered, editable {@link Text} field with a fixed pixel
     * width based on the given character count and an enforced text limit.
     * The pixel width is estimated as nine times the character count.
     *
     * @param parent    the parent composite
     * @param text      the initial text content
     * @param textWidth the maximum character width (also used for pixel sizing)
     * @param span      the number of columns to span
     * @return the configured Text widget
     */
    public static Text createText( Composite parent, String text, int textWidth, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE | SWT.BORDER );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        gridData.widthHint = 9 * textWidth;
        textWidget.setLayoutData( gridData );
        textWidget.setText( text );
        textWidget.setTextLimit( textWidth );

        return textWidget;
    }


    // ── METHOD createPasswordText — INSTALLING A MASKED INPUT TERMINAL ────────
    // C-3PO installs a masked terminal for classified credentials.  The typed
    // characters are hidden behind bullets so bystanders cannot read the code
    // from across the workshop.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a password-masked, editable {@link Text} field that fills its
     * parent horizontally.  Characters typed into this field are hidden by the
     * platform's standard password mask (usually bullets).
     *
     * @param parent the parent composite
     * @param text   the initial text content (will be masked)
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createPasswordText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE | SWT.BORDER | SWT.PASSWORD );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        textWidget.setLayoutData( gridData );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createReadonlyPasswordText — INSTALLING A LOCKED MASKED TERMINAL
    // C-3PO installs a locked, masked terminal that displays classified
    // credentials in obscured form but prevents any editing.  The user can
    // see that something is there, but cannot change it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a read-only, password-masked {@link Text} field.  The content
     * is visible in masked form (bullets) but cannot be edited.  The background
     * matches the parent so it blends in visually like a label.
     *
     * @param parent the parent composite
     * @param text   the initial text content (will be masked)
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createReadonlyPasswordText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE | SWT.BORDER | SWT.PASSWORD | SWT.READ_ONLY );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createLabeledText (simple) — INSTALLING A COPYABLE DISPLAY ─────
    // C-3PO installs a display terminal that looks like a label but lets the
    // user select and copy the text.  No border, grayed background, and no
    // editing — just read-and-copy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a non-editable {@link Text} that behaves like a label: no
     * border, a background matching the parent, and text the user can select
     * and copy but not modify.  Useful for displaying computed or fixed values.
     *
     * @param parent the parent composite
     * @param text   the text to display
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createLabeledText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createLabeledText (with widthHint) — FIXED-WIDTH COPYABLE DISPLAY
    // Same as the simple labeled text but with an explicit width constraint,
    // useful when you need the display to align within a fixed column.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a non-editable label-like {@link Text} with a fixed width hint
     * and a fill/grow horizontal layout.  The text is selectable and copyable
     * but not editable.
     *
     * @param parent    the parent composite
     * @param text      the text to display
     * @param span      the number of columns to span
     * @param widthHint the preferred pixel width
     * @return the configured Text widget
     */
    public static Text createLabeledText( Composite parent, String text, int span, int widthHint )
    {
        Text textWidget = new Text( parent, SWT.NONE );
        GridData gridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        gridData.horizontalSpan = span;
        gridData.widthHint = widthHint;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createWrappedLabeledText (simple) — WRAPPING COPYABLE DISPLAY ──
    // C-3PO installs a multi-line copyable display: no border, grayed background,
    // and the text wraps automatically so long values are never clipped.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a non-editable, word-wrapping label-like {@link Text}.  Text
     * that is too long for a single line wraps to the next line automatically.
     * The minimum width hint of 10 pixels ensures the SWT layout engine
     * computes the proper wrapping width.
     *
     * @param parent the parent composite
     * @param text   the text to display
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createWrappedLabeledText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.WRAP );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        gridData.widthHint = 10;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createWrappedLabeledText (with widthHint) — CONSTRAINED WRAP ───
    // Same as the simple wrapping display but with an explicit width hint so
    // callers can control when the text starts to wrap.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a non-editable, word-wrapping label-like {@link Text} with an
     * explicit preferred width.  All other behaviour is identical to the
     * simpler overload.
     *
     * @param parent    the parent composite
     * @param text      the text to display
     * @param span      the number of columns to span
     * @param widthHint the preferred pixel width before text starts wrapping
     * @return the configured Text widget
     */
    public static Text createWrappedLabeledText( Composite parent, String text, int span, int widthHint )
    {
        Text textWidget = new Text( parent, SWT.WRAP );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        gridData.widthHint = widthHint;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createReadonlyText — INSTALLING A LOCKED DISPLAY TERMINAL ──────
    // C-3PO installs a bordered display terminal that the user can read and copy
    // from, but cannot type into.  The border distinguishes it from the plain
    // label-like text variants so users know it is a field, just a locked one.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a read-only {@link Text} with a visible border.  The text is
     * selectable and copyable but not editable.  The background matches the
     * parent to signal the read-only state without hiding the field entirely.
     *
     * @param parent the parent composite
     * @param text   the text to display
     * @param span   the number of columns to span
     * @return the configured Text widget
     */
    public static Text createReadonlyText( Composite parent, String text, int span )
    {
        Text textWidget = new Text( parent, SWT.NONE | SWT.BORDER | SWT.READ_ONLY );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        textWidget.setLayoutData( gridData );
        textWidget.setEditable( false );
        textWidget.setBackground( parent.getBackground() );
        textWidget.setText( text );

        return textWidget;
    }


    // ── METHOD createCombo — INSTALLING A FREE-TEXT DROP-DOWN SELECTOR ────────
    // C-3PO installs a drop-down selector that lets the user either pick from
    // the known list or type something custom — handy when the list of options
    // is not exhaustive.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create an editable {@link Combo} pre-loaded with the given items.  The
     * user can select from the list or type a custom value.  We set a visible
     * item count of 20 so long lists are scrollable.
     *
     * @param parent        the parent composite
     * @param items         the initial list items
     * @param selectedIndex the zero-based index to pre-select, or -1 for none
     * @param span          the number of columns to span
     * @return the configured Combo
     */
    public static Combo createCombo( Composite parent, String[] items, int selectedIndex, int span )
    {
        Combo combo = new Combo( parent, SWT.DROP_DOWN | SWT.BORDER );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        combo.setLayoutData( gridData );
        combo.setItems( items );
        combo.select( selectedIndex );
        combo.setVisibleItemCount( 20 );

        return combo;
    }


    // ── METHOD createReadonlyCombo — INSTALLING A LOCKED DROP-DOWN SELECTOR ───
    // C-3PO installs a drop-down that restricts the user to the predefined list —
    // no custom typing allowed.  Use this when only known values are valid, such
    // as a list of supported protocols or fixed enum choices.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a read-only {@link Combo} that only allows selection from the
     * provided list — free-text input is disabled.  The visible item count is
     * set to 20 so long lists remain scrollable.
     *
     * @param parent        the parent composite
     * @param items         the items to populate the combo with
     * @param selectedIndex the zero-based index to pre-select, or -1 for none
     * @param span          the number of columns to span
     * @return the configured Combo
     */
    public static Combo createReadonlyCombo( Composite parent, String[] items, int selectedIndex, int span )
    {
        Combo combo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        combo.setLayoutData( gridData );
        combo.setItems( items );
        combo.select( selectedIndex );
        combo.setVisibleItemCount( 20 );

        return combo;
    }


    // ── METHOD createCheckbox — INSTALLING A YES/NO TOGGLE SWITCH ────────────
    // C-3PO installs a toggle switch on the workshop panel.  The checkbox has a
    // text label beside it and spans the requested number of columns.  Its state
    // defaults to unselected until the caller sets it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Button} with the SWT.CHECK style (a checkbox).  The
     * given text appears as the checkbox's label and it spans the requested
     * number of columns.
     *
     * @param parent the parent composite
     * @param text   the label text shown beside the checkbox
     * @param span   the number of columns to span
     * @return the configured checkbox Button
     */
    public static Button createCheckbox( Composite parent, String text, int span )
    {
        Button checkbox = new Button( parent, SWT.CHECK );
        checkbox.setText( text );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        checkbox.setLayoutData( gridData );

        return checkbox;
    }


    // ── METHOD createRadiobutton — INSTALLING A SINGLE-CHOICE SELECTOR ────────
    // C-3PO installs one of a set of mutually exclusive selector buttons.  In a
    // group of radio buttons, only one can be active at a time — like choosing
    // which weapon system to target.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Button} with the SWT.RADIO style.  Radio buttons in the
     * same parent composite are automatically mutually exclusive.  The given text
     * appears as the button's label.
     *
     * @param parent the parent composite
     * @param text   the label text shown beside the radio button
     * @param span   the number of columns to span
     * @return the configured radio Button
     */
    public static Button createRadiobutton( Composite parent, String text, int span )
    {
        Button radio = new Button( parent, SWT.RADIO );
        radio.setText( text );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        radio.setLayoutData( gridData );

        return radio;
    }


    // ── METHOD createButton — INSTALLING A STANDARD ACTION BUTTON ────────────
    // C-3PO installs a push button sized to the platform's standard button width
    // so it matches every other button in the dialog.  We use a GC to measure
    // the dialog font and compute the pixel width from DLU constants.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a push {@link Button} with a width set to the standard JFace
     * dialog button width.  We compute this by measuring the dialog font with
     * a GC — the GC is always disposed in a finally block to prevent leaks.
     *
     * @param parent the parent composite
     * @param text   the button label
     * @param span   the number of columns to span
     * @return the configured Button
     */
    public static Button createButton( Composite parent, String text, int span )
    {
        GC gc = new GC( parent );

        try
        {
            gc.setFont( JFaceResources.getDialogFont() );
            FontMetrics fontMetrics = gc.getFontMetrics();
            Button button = new Button( parent, SWT.PUSH );
            GridData gridData = new GridData();
            gridData.widthHint = Dialog.convertHorizontalDLUsToPixels( fontMetrics, IDialogConstants.BUTTON_WIDTH );
            gridData.horizontalSpan = span;
            button.setLayoutData( gridData );
            button.setText( text );

            return button;
        }
        finally
        {
            gc.dispose();
        }

    }


    // ── METHOD createRadioIndent — INDENTING BELOW A RADIO GROUP ─────────────
    // C-3PO adds a spacer label with a 22-pixel indent to visually nest controls
    // beneath a radio button, creating the appearance that sub-options belong to
    // the radio choice above them.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create an invisible {@link Label} with a 22-pixel horizontal indent,
     * which is used to align controls that belong visually beneath a radio
     * button.  This creates the common "radio + subordinate options" pattern.
     *
     * @param parent the parent composite
     * @param span   the number of columns to span
     * @return the indent label (invisible but taking up space)
     */
    public static Label createRadioIndent( Composite parent, int span )
    {
        Label label = new Label( parent, SWT.NONE );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        gridData.horizontalIndent = 22;
        label.setLayoutData( gridData );

        return label;
    }


    // ── METHOD createSpacer — INSERTING A BLANK PANEL GAP ────────────────────
    // C-3PO inserts a 1-pixel-tall blank label between sections of the panel
    // to add visual breathing room without a visible dividing line.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create an invisible 1-pixel-tall {@link Label} used as a small
     * vertical spacer between groups of controls in a grid layout.
     *
     * @param parent the parent composite
     * @param span   the number of columns to span
     * @return the spacer label
     */
    public static Label createSpacer( Composite parent, int span )
    {
        Label label = new Label( parent, SWT.NONE );
        GridData gridData = new GridData();
        gridData.horizontalSpan = span;
        gridData.heightHint = 1;
        label.setLayoutData( gridData );

        return label;
    }


    // ── METHOD createSeparator — DRAWING A HORIZONTAL DIVIDING LINE ───────────
    // C-3PO draws a horizontal line across the panel to visually separate two
    // groups of controls — like a chapter break in the mission briefing document.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a horizontal separator {@link Label} that fills the full width
     * of its parent and spans the given number of columns.  Use this to draw
     * a visible dividing line between groups of related controls.
     *
     * @param parent the parent composite
     * @param span   the number of columns to span
     * @return the separator label
     */
    public static Label createSeparator( Composite parent, int span )
    {
        Label label = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
        gridData.horizontalSpan = span;
        label.setLayoutData( gridData );

        return label;
    }


    // ── METHOD createLink — INSTALLING A CLICKABLE HYPERLINK ─────────────────
    // C-3PO installs a clickable hyperlink into the panel — the SWT Link widget
    // supports HTML-style anchor tags so you can embed multiple links in a
    // single text block.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Link} with the given text that fills the available
     * horizontal space and spans the given number of columns.  A minimum width
     * hint of 150 is applied.  Use HTML anchor tags in the text string to embed
     * clickable regions.
     *
     * @param parent the parent composite
     * @param text   the link text (may include HTML anchor tags)
     * @param span   the number of columns to span
     * @return the configured Link
     */
    public static Link createLink( Composite parent, String text, int span )
    {
        Link link = new Link( parent, SWT.NONE );
        link.setText( text );
        GridData gridData = new GridData( SWT.FILL, SWT.BEGINNING, true, false );
        gridData.horizontalSpan = span;
        gridData.widthHint = 150;
        link.setLayoutData( gridData );

        return link;
    }


    // ── METHOD createIntegerText (no description, no width) — BASIC INT FIELD ─
    // C-3PO installs a text terminal pre-wired to reject any non-numeric input.
    // This overload uses the form toolkit for styling and has no description
    // decoration or size constraint.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a {@link Text} field that only accepts digit characters, using
     * the given form toolkit for visual styling.  No description decoration
     * and no explicit width constraint are applied.
     *
     * @param toolkit the form toolkit for styling
     * @param parent  the parent composite
     * @return the configured integer Text widget
     */
    public static Text createIntegerText( FormToolkit toolkit, Composite parent )
    {
        return createIntegerText( toolkit, parent, null, -1 );
    }


    // ── METHOD createIntegerText (with width) — SIZED INT FIELD ──────────────
    // C-3PO installs a digit-only terminal with a specific pixel width so it
    // fits neatly alongside other fixed-size fields in the form.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a digit-only {@link Text} field with the given pixel width and
     * no description decoration.
     *
     * @param toolkit the form toolkit for styling
     * @param parent  the parent composite
     * @param width   the preferred pixel width, or negative for no constraint
     * @return the configured integer Text widget
     */
    public static Text createIntegerText( FormToolkit toolkit, Composite parent, int width )
    {
        return createIntegerText( toolkit, parent, null, width );
    }


    // ── METHOD createIntegerText (with description) — ANNOTATED INT FIELD ─────
    // C-3PO installs a digit-only terminal with an information-icon decoration
    // that shows the given description text when hovered.  No size constraint
    // is applied.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a digit-only {@link Text} field with an info-icon decoration
     * that displays the given description text as a tooltip.  No explicit width
     * is applied.
     *
     * @param toolkit     the form toolkit for styling
     * @param parent      the parent composite
     * @param description the tooltip text for the info decoration, or {@code null}
     * @return the configured integer Text widget
     */
    public static Text createIntegerText( FormToolkit toolkit, Composite parent, String description )
    {
        return createIntegerText( toolkit, parent, description, -1 );
    }


    // ── METHOD createIntegerText (description + width) — FULL INT FIELD ───────
    // C-3PO's most complete integer terminal: digit-only validation, an optional
    // info-icon description decoration, and an optional fixed pixel width.  All
    // simpler overloads delegate here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create the most fully-featured digit-only {@link Text} field.  A
     * {@link VerifyListener} blocks any non-digit input.  If {@code description}
     * is non-empty, we add an information-icon decoration with that tooltip.
     * If {@code width} is non-negative, we apply it as the layout width hint.
     *
     * @param toolkit     the form toolkit for styling
     * @param parent      the parent composite
     * @param description the tooltip for the info decoration, or {@code null}
     * @param width       the preferred pixel width, or negative for no constraint
     * @return the configured integer Text widget
     */
    public static Text createIntegerText( FormToolkit toolkit, Composite parent, String description, int width )
    {
        Text integerText = toolkit.createText( parent, "" ); //$NON-NLS-1$

        integerText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                for ( int i = 0; i < e.text.length(); i++ )
                {
                    if ( !Character.isDigit( e.text.charAt( i ) ) )
                    {
                        e.doit = false;
                        break;
                    }
                }
            }
        } );

        // Add the description, if needed
        if ( ( description != null ) && ( description.length() > 0 ) )
        {
            ControlDecoration monitoringCheckboxDecoration = new ControlDecoration(
                integerText, SWT.CENTER | SWT.RIGHT );
            monitoringCheckboxDecoration.setImage( CommonUIPlugin.getDefault().getImageDescriptor(
                CommonUIConstants.IMG_INFORMATION ).createImage() );
            monitoringCheckboxDecoration.setMarginWidth( 4 );
            monitoringCheckboxDecoration.setDescriptionText( description );
        }

        if ( width >= 0 )
        {
            GridData gridData = new GridData();
            gridData.widthHint = width;
            integerText.setLayoutData( gridData );
        }

        return integerText;
    }


    // ── METHOD setValue (Boolean, Button) — TOGGLING A SWITCH ─────────────────
    // C-3PO flips the switch to the correct position based on the Boolean value.
    // If the value is null, he leaves the switch off rather than throwing an
    // error — null means "not configured" in our world.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We set the selection state of the given checkbox {@link Button} based on
     * the provided Boolean.  A null value is treated as {@code false} so the
     * checkbox is unchecked rather than throwing a NullPointerException.
     *
     * @param value    the Boolean to apply, may be {@code null}
     * @param checkBox the Button (checkbox) to update
     */
    public static void setValue( Boolean value, Button checkBox )
    {
        if ( value != null )
        {
            checkBox.setSelection( value );
        }
        else
        {
            checkBox.setSelection( false );
        }

    }


    // ── METHOD setValue (Integer, Text) — SETTING THE NUMERIC READOUT ─────────
    // C-3PO dials in the correct number on the display terminal.  If the value
    // is null, he clears the display rather than showing "null" — an empty
    // field is always safer than a confusing placeholder.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We display the given Integer value in the given {@link Text} field.  If
     * the value is null we clear the field to an empty string so nothing
     * misleading is shown to the user.
     *
     * @param value     the integer to display, may be {@code null}
     * @param inputText the Text field to update
     */
    public static void setValue( Integer value, Text inputText )
    {
        if ( value != null )
        {
            inputText.setText( value.toString() );
        }
        else
        {
            inputText.setText( "" );
        }
    }


    // ── METHOD setValue (String, Text) — SETTING THE TEXT READOUT ────────────
    // C-3PO types the given string into the display terminal.  A null value
    // clears the display — we never let "null" appear as literal text in a
    // form field.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We display the given String value in the given {@link Text} field.  If
     * the value is null we clear the field to an empty string so the user never
     * sees the literal text "null" in a form field.
     *
     * @param value     the string to display, may be {@code null}
     * @param inputText the Text field to update
     */
    public static void setValue( String value, Text inputText )
    {
        if ( value != null )
        {
            inputText.setText( value );
        }
        else
        {
            inputText.setText( "" );
        }

    }
}
