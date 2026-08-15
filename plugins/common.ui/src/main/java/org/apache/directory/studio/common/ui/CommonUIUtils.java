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
package org.apache.directory.studio.common.ui;


import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.framework.Bundle;


// ── CLASS: CommonUIUtils — C-3PO TRANSLATING AND ASSISTING ──────────────────
// C-3PO is fluent in over six million forms of communication, and so are we.
// This utility class translates raw strings and booleans into polished UI
// elements: error dialogs, warning popups, styled text fields, and null-safe
// string conversions.  Every other part of the application calls us when it
// needs a clear message delivered to the user without fuss.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide a collection of static helper methods that the whole application
 * relies on for common UI tasks: opening message dialogs, creating form text
 * fields, and safely converting values to display strings.  Think of us as
 * the protocol droid that handles all the communication details so the rest
 * of the code does not have to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommonUIUtils
{
    // ── METHOD openErrorDialog (message only) — BROADCASTING A RED ALERT ─────
    // C-3PO raises the alarm with a standard "Error" title when something has
    // gone wrong.  We look up the localized title string and delegate to the
    // two-argument overload so the alert reaches the user without ceremony.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open an error {@link MessageDialog} using the default localized title
     * and the given message text.  Use this when you have an error to report
     * but do not need a custom title.
     *
     * @param message the error message to display
     */
    public static void openErrorDialog( String message )
    {
        openErrorDialog( Messages.getString( "CommonUIUtils.Error" ), message ); //$NON-NLS-1$
    }


    // ── METHOD openErrorDialog (title + message) — SENDING THE RED ALERT ─────
    // C-3PO delivers the red-alert message with the full title and text you
    // specify.  We construct the dialog, attach it to the active workbench
    // window, and open it — blocking until the user dismisses it with OK.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open an error {@link MessageDialog} with your custom title and message.
     * The dialog is modal and blocks until the user clicks OK.
     *
     * @param title   the dialog title
     * @param message the error message to display
     */
    public static void openErrorDialog( String title, String message )
    {
        MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            title, null, message, MessageDialog.ERROR, new String[]
                { IDialogConstants.OK_LABEL }, MessageDialog.OK );
        dialog.open();
    }


    // ── METHOD openInformationDialog (message only) — SHARING A HOLO-BRIEFING ─
    // C-3PO passes along an informational bulletin using the default "Information"
    // title.  We delegate to the two-argument overload with the localized title
    // so callers do not have to worry about i18n themselves.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open an informational {@link MessageDialog} with the default localized
     * title and the given message text.  Use this for non-critical status
     * updates the user needs to acknowledge.
     *
     * @param message the informational message to display
     */
    public static void openInformationDialog( String message )
    {
        openInformationDialog( Messages.getString( "CommonUIUtils.Information" ), message ); //$NON-NLS-1$
    }


    // ── METHOD openInformationDialog (title + message) — DELIVERING THE BRIEFING
    // C-3PO delivers the full holo-briefing with a custom title and message.
    // We open a modal information dialog that the user must acknowledge before
    // they can continue whatever they were doing.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open an informational {@link MessageDialog} with your custom title
     * and message.  The dialog is modal and blocks until the user clicks OK.
     *
     * @param title   the dialog title
     * @param message the informational message to display
     */
    public static void openInformationDialog( String title, String message )
    {
        MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            title, null, message, MessageDialog.INFORMATION, new String[]
                { IDialogConstants.OK_LABEL }, MessageDialog.OK );
        dialog.open();
    }


    // ── METHOD openWarningDialog (message only) — RAISING A YELLOW ALERT ─────
    // C-3PO sounds the yellow alert: something needs attention but is not yet
    // catastrophic.  We use the default localized "Warning" title and let the
    // two-argument method do the actual dialog construction.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open a warning {@link MessageDialog} with the default localized title
     * and the given message.  Use this when you need to alert the user to a
     * potential problem without blocking them with an error.
     *
     * @param message the warning message to display
     */
    public static void openWarningDialog( String message )
    {
        openWarningDialog( Messages.getString( "CommonUIUtils.Warning" ), message ); //$NON-NLS-1$
    }


    // ── METHOD openWarningDialog (title + message) — SENDING THE YELLOW ALERT ─
    // C-3PO delivers the full yellow-alert message with your custom title.  We
    // open a modal warning dialog and wait for the user to click OK before
    // allowing the mission to continue.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open a warning {@link MessageDialog} with your custom title and message.
     * The dialog is modal and blocks until the user clicks OK.
     *
     * @param title   the dialog title
     * @param message the warning message to display
     */
    public static void openWarningDialog( String title, String message )
    {
        MessageDialog dialog = new MessageDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            title, null, message, MessageDialog.WARNING, new String[]
                { IDialogConstants.OK_LABEL }, MessageDialog.OK );
        dialog.open();
    }


    // ── METHOD isIDEEnvironment — CHECKING IF WE ARE AT REBEL HQ OR A OUTPOST ─
    // C-3PO checks whether we are at the full Rebel headquarters (Eclipse IDE)
    // or a stripped-down field outpost (RCP application).  The presence of the
    // RCP bundle tells us which environment we are running in.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We check whether we are running inside the full Eclipse IDE by looking
     * for the {@code org.apache.directory.studio.rcp} bundle.  If that bundle
     * is absent, we are in the IDE environment.  Returns {@code true} for IDE,
     * {@code false} for standalone RCP.
     *
     * @return {@code true} if running in the Eclipse IDE environment
     */
    public static boolean isIDEEnvironment()
    {
        Bundle bundle = Platform.getBundle( "org.apache.directory.studio.rcp" );
        return bundle == null;
    }


    // ── METHOD createText (with GridData default) — ASSEMBLING A TEXT CONSOLE ─
    // C-3PO assembles a labeled text input station: he slaps a label on the
    // left, wires up a scrolling text field on the right, and attaches the
    // modify listener so any changes are immediately reported back.  If the
    // value is null, he defaults to an empty string so the field is never blank.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a labeled {@link Text} field using the given toolkit and wire
     * it up to the provided modify listener.  The text fills its parent
     * composite horizontally.  A {@code null} default value becomes an empty
     * string.  Limits are ignored when {@code limit} is negative.
     *
     * @param toolkit      the form toolkit for styling
     * @param composite    the parent composite
     * @param label        the label text to display beside the field
     * @param defaultValue the initial value, or {@code null} for empty
     * @param limit        the maximum character count, ignored if negative
     * @param listener     the listener to notify on text changes, or {@code null}
     * @return the configured Text widget
     */
    public static Text createText( FormToolkit toolkit, Composite composite, String label, String defaultValue, int limit, ModifyListener listener  )
    {
        toolkit.createLabel( composite, label );
        String value = "";

        if ( defaultValue != null )
        {
            value = defaultValue;
        }

        Text text = toolkit.createText( composite, value );
        text.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        if ( limit >= 0 )
        {
            text.setTextLimit( limit );
        }

        // Attach a listener to check the value
        if ( listener != null )
        {
            text.addModifyListener( listener );
        }

        return text;
    }


    // ── METHOD createText (with custom GridData) — ASSEMBLING A CUSTOM CONSOLE ─
    // C-3PO builds the same labeled text station but this time you hand him the
    // exact grid layout specifications.  This lets callers control the size and
    // alignment of the field rather than accepting the default fill behaviour.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a labeled {@link Text} field using the given toolkit and apply
     * the caller-supplied {@link GridData} for layout.  Otherwise behaviour is
     * identical to the simpler overload: null default becomes empty string,
     * negative limit is ignored, null listener is skipped.
     *
     * @param toolkit      the form toolkit for styling
     * @param composite    the parent composite
     * @param label        the label text to display beside the field
     * @param defaultValue the initial value, or {@code null} for empty
     * @param limit        the maximum character count, ignored if negative
     * @param gridData     the layout data to apply to the text field
     * @param listener     the listener to notify on text changes, or {@code null}
     * @return the configured Text widget
     */
    public static Text createText( FormToolkit toolkit, Composite composite, String label, String defaultValue, int limit, GridData gridData, ModifyListener listener  )
    {
        toolkit.createLabel( composite, label );
        String value = "";

        if ( defaultValue != null )
        {
            value = defaultValue;
        }

        Text text = toolkit.createText( composite, value );
        text.setLayoutData( gridData );

        if ( limit >= 0 )
        {
            text.setTextLimit( limit );
        }

        // Attach a listener to check the value
        if ( listener != null )
        {
            text.addModifyListener( listener );
        }

        return text;
    }


    // ── METHOD getTextValue (String) — TRANSLATING NULL INTO SILENCE ─────────
    // C-3PO never lets a null value make it into a conversation — that would
    // confuse everyone.  We replace null with an empty string so UI fields
    // always get a safe, displayable value to show.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the given string as-is if it is not null, or an empty string
     * if it is.  This is a null-safe helper for populating text fields where
     * {@code null} would cause a NullPointerException or display "null".
     *
     * @param value the string to sanitize, may be {@code null}
     * @return the original value, or {@code ""} if the value is {@code null}
     */
    public static String getTextValue( String value )
    {
        if ( value == null )
        {
            return "";
        }
        else
        {
            return value;
        }
    }


    // ── METHOD getTextValue (int) — SILENCING THE ZERO SIGNAL ───────────────
    // C-3PO knows that a reading of zero often means "not configured" rather
    // than a real value of zero.  We translate a zero int to an empty string
    // so text fields show blank instead of a misleading "0".
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return an empty string when the given integer is zero (treating zero
     * as "not set"), or the integer's string representation otherwise.  Use
     * this when you want a text field to appear blank rather than show "0"
     * for an uninitialized numeric preference.
     *
     * @param value the integer to convert
     * @return {@code ""} when the value is zero, otherwise {@link Integer#toString(int)}
     */
    public static String getTextValue( int value )
    {
        if ( value == 0 )
        {
            return "";
        }
        else
        {
            return Integer.toString( value );
        }
    }
}
