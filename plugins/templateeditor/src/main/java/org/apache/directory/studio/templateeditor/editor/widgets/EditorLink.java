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
package org.apache.directory.studio.templateeditor.editor.widgets;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.model.widgets.TemplateLink;


// ── CLASS: EditorLink — THE TANTIVE IV COMMS HYPERLINK PANEL ─────────────────────
// On the Tantive IV, the communications panel displays hyperlinks to fleet comms
// frequencies and sector maps — the operator can click a link and the system
// opens the external channel. This widget does the same: it scans the LDAP
// attribute value for URLs and email addresses, wraps them in SWT Link {@code <a>}
// tags, and opens the system browser when clicked. Non-link text is displayed as
// plain text. The regex scanner runs in a pass-through loop so it correctly
// handles multiple links embedded in a single attribute value.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A hyperlink display widget that finds URLs and email addresses in an LDAP
 * attribute value and renders them as clickable SWT {@link Link} elements.
 * Clicking a link opens it in the Eclipse external browser. Email addresses
 * are prefixed with {@code mailto:} before being opened.
 * Think of this as the Tantive IV comms hyperlink panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorLink extends EditorWidget<TemplateLink>
{
    /** The Regex for matching an URL */
    private static final String REGEX_URL = "([a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]+)"; //$NON-NLS-1$

    /** The Regex for matching an email address*/
    private static final String REGEX_EMAIL_ADDRESS = "([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4})"; //$NON-NLS-1$

    /** The link widget */
    private Link link;


    // ── CONSTRUCTOR: INSTALL THE COMMS PANEL ─────────────────────────────────────
    // The technician installs the hyperlink comms panel. It reads from the LDAP
    // attribute type declared in templateLink, or from a static value configured
    // in the template.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorLink} bound to the given template link model.
     *
     * @param editor        the owning entry editor
     * @param templateLink  the template model specifying attribute type or static value
     * @param toolkit       the form toolkit
     */
    public EditorLink( IEntryEditor editor, TemplateLink templateLink, FormToolkit toolkit )
    {
        super( templateLink, editor, toolkit );
    }


    // ── CREATE WIDGET: POWER UP THE COMMS PANEL ───────────────────────────────────
    /**
     * Creates the SWT {@link Link} widget, fills it with the current attribute value
     * (with URLs/emails wrapped in link tags), and attaches the click listener.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        // Adding the listeners
        addListeners();

        return composite;
    }


    // ── INIT WIDGET: BUILD THE LINK DISPLAY ──────────────────────────────────────
    /**
     * Creates the SWT {@link Link} widget and sets its layout data.
     *
     * @param parent  the parent composite
     * @return the parent composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the link widget
        link = new Link( parent, SWT.NONE );
        link.setLayoutData( getGridata() );

        return parent;
    }


    // ── UPDATE WIDGET: REFRESH THE LINK TEXT ─────────────────────────────────────
    // We re-read the attribute value (or static template value), scan it for URLs
    // and email addresses, wrap the found ones in {@code <a>} tags, and set the
    // result on the SWT Link widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Re-reads the LDAP attribute value (or static template value), wraps found
     * URLs and email addresses in SWT Link tags, and updates the display.
     */
    private void updateWidget()
    {
        // Checking is we need to display a value taken from the entry
        // or use the given value
        String attributeType = getWidget().getAttributeType();
        if ( attributeType != null )
        {
            link.setText( addLinksTags( EditorWidgetUtils.getConcatenatedValues( getEntry(), attributeType ) ) );
        }
        else
        {
            link.setText( addLinksTags( getWidget().getValue() ) );
        }
    }


    // ── ADD LISTENERS: WIRE THE CLICK HANDLER ────────────────────────────────────
    // When the operator clicks a link, we check if it's a URL or email address and
    // open the appropriate external channel. If the URL is null or malformed, we
    // log the error and show an error dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches an {@link SWT#Selection} listener that opens clicked links in the
     * Eclipse external browser. Email addresses are prefixed with {@code mailto:}.
     */
    private void addListeners()
    {
        link.addListener( SWT.Selection, new Listener()
        {
            public void handleEvent( Event event )
            {
                // Creating the URL
                String url = null;

                // Getting the text that was clicked
                String text = event.text;
                if ( isUrl( text ) )
                {
                    url = text;
                }
                else if ( isEmailAddress( text ) )
                {
                    url = "mailto:" + text; //$NON-NLS-1$
                }

                if ( url != null )
                {
                    try
                    {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL( new URL( url ) );
                    }
                    catch ( Exception e )
                    {
                        // Logging the error
                        EntryTemplatePluginUtils.logError( e, "An error occurred while opening the link.", //$NON-NLS-1$
                            new Object[0] );

                        // Launching an error dialog
                        MessageDialog
                            .openError(
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                Messages.getString( "EditorLink.ErrorMessageDialogTitle" ), Messages.getString( "EditorLink.ErrorMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                else
                {
                    // Logging the error
                    EntryTemplatePluginUtils.logError( null, "An error occurred while opening the link. URL is null.", //$NON-NLS-1$
                        new Object[0] );

                    // Launching an error dialog
                    MessageDialog
                        .openError(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.getString( "EditorLink.ErrorMessageDialogTitle" ), Messages.getString( "EditorLink.ErrorMessageDialogMessage" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } );
    }


    // ── ADD LINKS TAGS: WRAP URLS AND EMAILS IN LINK TAGS ────────────────────────
    // We scan the string for URLs and email addresses using regex, then wrap each
    // found instance in SWT Link {@code <a>...</a>} tags. The rest of the string
    // passes through unchanged. Gracefully falls back to the original string if
    // index arithmetic goes wrong.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Scans the given string for URLs and email addresses and wraps each in
     * {@code <a>...</a>} tags so the SWT {@link Link} widget renders them as
     * clickable links.
     *
     * @param s  the string to process; may contain URLs and email addresses
     * @return the string with link tags inserted around URLs and emails
     */
    private static String addLinksTags( String s )
    {
        List<String> links = new ArrayList<String>();

        // Getting the URLs
        links.addAll( Arrays.asList( getUrls( s ) ) );

        // Getting the email addresses
        links.addAll( Arrays.asList( getEmailAddresses( s ) ) );

        // Creating the final string
        StringBuilder sb = new StringBuilder();
        try
        {
            // Inserting link tags
            int start = 0;
            for ( String link : links )
            {
                int indexOfLink = s.indexOf( link );
                sb.append( s.subSequence( start, indexOfLink ) );
                sb.append( "<a>" ); //$NON-NLS-1$
                sb.append( link );
                sb.append( "</a>" ); //$NON-NLS-1$

                start = indexOfLink + link.length();
            }
            sb.append( s.substring( start, s.length() ) );
        }
        catch ( StringIndexOutOfBoundsException e )
        {
            // In case we hit a wrong index, we fail gracefully by
            // returning the original string
            return s;
        }

        // Returning the final string
        return sb.toString();
    }


    // ── GET URLS: FIND ALL URLS IN THE STRING ────────────────────────────────────
    /**
     * Finds all URL strings in {@code s} using the URL regex pattern.
     *
     * @param s  the string to scan
     * @return an array of found URL strings; empty if none
     */
    private static String[] getUrls( String s )
    {
        return getMatchingStrings( s, Pattern.compile( REGEX_URL + ".*" ) ); //$NON-NLS-1$
    }


    // ── GET EMAIL ADDRESSES: FIND ALL EMAILS IN THE STRING ───────────────────────
    /**
     * Finds all email address strings in {@code s} using the email regex pattern.
     *
     * @param s  the string to scan
     * @return an array of found email address strings; empty if none
     */
    private static String[] getEmailAddresses( String s )
    {
        return getMatchingStrings( s, Pattern.compile( REGEX_EMAIL_ADDRESS + ".*" ) ); //$NON-NLS-1$
    }


    // ── GET MATCHING STRINGS: APPLY A PATTERN AND COLLECT MATCHES ────────────────
    // We walk the string character by character, trying the pattern at each
    // position and collecting group(1) when it matches. This handles multiple
    // URLs or emails embedded in the same string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Applies {@code p} at each position in {@code s}, collecting the first capture
     * group from each match. Returns all collected strings as an array.
     *
     * @param s  the string to scan
     * @param p  the compiled pattern (must have one capture group)
     * @return an array of matched strings; empty if none
     */
    private static String[] getMatchingStrings( String s, Pattern p )
    {
        List<String> matchingStrings = new ArrayList<String>();

        while ( s.length() > 0 )
        {
            Matcher m = p.matcher( s );

            if ( m.matches() )
            {
                String link = m.group( 1 );
                matchingStrings.add( link );
                s = s.substring( link.length() );
            }
            else
            {
                s = s.substring( 1 );
            }
        }

        return matchingStrings.toArray( new String[0] );
    }


    // ── IS URL: CHECK IF THE STRING IS A URL ─────────────────────────────────────
    /**
     * Returns {@code true} if the given string matches the URL regex pattern exactly.
     *
     * @param s  the string to check
     * @return {@code true} if it's a URL
     */
    private boolean isUrl( String s )
    {
        return Pattern.matches( REGEX_URL, s );
    }


    // ── IS EMAIL ADDRESS: CHECK IF THE STRING IS AN EMAIL ────────────────────────
    /**
     * Returns {@code true} if the given string matches the email address regex pattern.
     *
     * @param s  the string to check
     * @return {@code true} if it's an email address
     */
    private boolean isEmailAddress( String s )
    {
        return Pattern.matches( REGEX_EMAIL_ADDRESS, s );
    }


    // ── UPDATE: REFRESH THE LINK DISPLAY ─────────────────────────────────────────
    /**
     * Refreshes the link display from the current LDAP working copy.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — the SWT Link widget is disposed by its parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
