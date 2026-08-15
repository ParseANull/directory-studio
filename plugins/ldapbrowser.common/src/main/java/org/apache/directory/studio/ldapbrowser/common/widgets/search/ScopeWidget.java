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

package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


// ── CLASS: ScopeWidget — Luke Choosing How Deep Into the Dagobah Cave to Go ─────────
// On Dagobah, Yoda sends Luke into the dark side cave for a test. Luke has to decide
// how far to go: stay at the entrance and examine just that one spot (OBJECT scope),
// walk one level deeper to check the immediate surroundings (ONELEVEL scope),
// or plunge all the way down through every tunnel and chamber (SUBTREE scope).
// In LDAP, scope is the same decision: how deep in the directory tree should the
// server search? This widget presents that choice as three radio buttons.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * An SWT widget for selecting the LDAP search scope — how deep into the directory tree
 * the server should look starting from the search base. Think of the directory as the
 * Dagobah cave system and the scope as how deep Luke is willing to go.
 *
 * <p>The three scopes correspond to:</p>
 * <ul>
 *   <li><b>Object</b> — only the base entry itself (just the cave entrance)</li>
 *   <li><b>One level</b> — the base entry and its immediate children (one level in)</li>
 *   <li><b>Subtree</b> — the base entry and all descendants (the whole cave system)</li>
 * </ul>
 * Used by {@link SearchPageWrapper} in the options section of the search form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ScopeWidget extends AbstractWidget
{

    /** The initial scope. */
    private SearchScope initialScope;

    /** The scope group. */
    private Group scopeGroup;

    /** The scope object button. */
    private Button scopeObjectButton;

    /** The scope onelevel button. */
    private Button scopeOnelevelButton;

    /** The scope subtree button. */
    private Button scopeSubtreeButton;


    // ── Luke Arrives With His Depth Decision Already Made ─────────────────────────────
    // Yoda has already told Luke how deep to go before the session starts.
    // Luke walks up to the cave entrance with his decision locked in — the radio button
    // for the chosen depth will be pre-selected when the widget renders.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget pre-configured with a specific search scope.
     * Use this when editing an existing saved search that already has a scope set,
     * so the user sees the current setting the moment the dialog opens.
     *
     * <p>For example — Luke arrives knowing he's going one level in:</p>
     * <pre>
     *   caveDepth = SearchScope.ONELEVEL;
     *   Luke.commit( caveDepth );
     * </pre>
     *
     * @param initialScope  The scope to pre-select: OBJECT, ONELEVEL, or SUBTREE.
     */
    public ScopeWidget( SearchScope initialScope )
    {
        this.initialScope = initialScope;
    }


    // ── Luke Defaults to the Cautious Approach ────────────────────────────────────────
    // No special instructions from Yoda this time. Luke defaults to the most conservative
    // choice: just check the entrance (OBJECT scope). He can always go deeper if he wants.
    // We default to OBJECT so new search dialogs start with the narrowest scope.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a widget defaulting to {@link SearchScope#OBJECT} — the narrowest scope,
     * which returns only the single base entry. A sensible default for new searches
     * where the user hasn't decided on a depth yet.
     *
     * <p>For example — Luke defaults to staying at the entrance:</p>
     * <pre>
     *   caveDepth = SearchScope.OBJECT; // safest default
     * </pre>
     */
    public ScopeWidget()
    {
        this.initialScope = SearchScope.OBJECT;
    }


    // ── Luke Installs the Depth-Selector Panel at the Cave Entrance ───────────────────
    // At the cave entrance on Dagobah, Luke sets up a three-option decision board:
    // "Just here", "One level", or "All the way down". He attaches a notification to
    // each option so the mission team knows the moment he changes his mind.
    // We build the SWT group with three radio buttons and wire change listeners.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and lays out the SWT controls inside the given parent composite.
     * Creates a labeled group with three radio buttons for Object, One level, and
     * Subtree scopes. Each radio fires a change notification when selected.
     * Call this exactly once after construction.
     *
     * <p>For example — Luke sets up his depth-choice panel:</p>
     * <pre>
     *   panel = new Group( parent, "Scope" );
     *   panel.addRadio( "Object"   );
     *   panel.addRadio( "One Level" );
     *   panel.addRadio( "Subtree"  );
     * </pre>
     *
     * @param parent  The SWT composite that will host the group and radio buttons.
     */
    public void createWidget( Composite parent )
    {

        // Scope group
        scopeGroup = new Group( parent, SWT.NONE );
        scopeGroup.setText( Messages.getString( "ScopeWidget.Scope" ) ); //$NON-NLS-1$
        scopeGroup.setLayout( new GridLayout( 1, false ) );
        scopeGroup.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // Object radio
        scopeObjectButton = new Button( scopeGroup, SWT.RADIO );
        scopeObjectButton.setText( Messages.getString( "ScopeWidget.Object" ) ); //$NON-NLS-1$
        scopeObjectButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        // Onelevel radio
        scopeOnelevelButton = new Button( scopeGroup, SWT.RADIO );
        scopeOnelevelButton.setText( Messages.getString( "ScopeWidget.OneLevel" ) ); //$NON-NLS-1$
        scopeOnelevelButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        // subtree button
        scopeSubtreeButton = new Button( scopeGroup, SWT.RADIO );
        scopeSubtreeButton.setText( Messages.getString( "ScopeWidget.Subtree" ) ); //$NON-NLS-1$
        scopeSubtreeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                notifyListeners();
            }
        } );

        setScope( initialScope );
    }


    // ── Luke Updates His Depth Decision ──────────────────────────────────────────────
    // Yoda calls across the swamp: "Deeper you must go, young Skywalker — Subtree."
    // Luke flips his decision board to Subtree and the radios update to match.
    // We set the appropriate radio button based on the provided scope enum value.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically selects a scope radio button and updates the UI to match.
     * Use this when loading a saved search into the form to restore its scope setting.
     *
     * <p>For example — Yoda overrides Luke's decision:</p>
     * <pre>
     *   caveDepth = SearchScope.SUBTREE;
     *   subtreeRadio.setSelected( true );
     *   otherRadios.setSelected( false );
     * </pre>
     *
     * @param scope  The scope to select. One of {@link SearchScope#OBJECT},
     *               {@link SearchScope#ONELEVEL}, or {@link SearchScope#SUBTREE}.
     */
    public void setScope( SearchScope scope )
    {
        initialScope = scope;
        scopeObjectButton.setSelection( initialScope == SearchScope.OBJECT );
        scopeOnelevelButton.setSelection( initialScope == SearchScope.ONELEVEL );
        scopeSubtreeButton.setSelection( initialScope == SearchScope.SUBTREE );
    }


    // ── Luke Reports His Current Depth Decision ────────────────────────────────────────
    // "How deep are you going, Luke?" Yoda asks. Luke checks which radio is lit and
    // reports back: Subtree, One Level, or Object. If somehow none is selected he
    // defaults to One Level as the middle ground.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the currently selected radio button and returns the matching
     * {@link SearchScope} enum value. The search engine uses this to build the LDAP
     * search request. If no button is selected (which shouldn't happen normally),
     * falls back to ONELEVEL.
     *
     * <p>For example — Luke reports his depth:</p>
     * <pre>
     *   if ( subtreeRadio.selected  ) return SUBTREE;
     *   if ( oneLevelRadio.selected ) return ONELEVEL;
     *   if ( objectRadio.selected   ) return OBJECT;
     *   else                          return ONELEVEL; // fallback
     * </pre>
     *
     * @return  The currently selected {@link SearchScope}. Never null.
     */
    public SearchScope getScope()
    {
        SearchScope scope;

        if ( scopeSubtreeButton.getSelection() )
        {
            scope = SearchScope.SUBTREE;
        }
        else if ( scopeOnelevelButton.getSelection() )
        {
            scope = SearchScope.ONELEVEL;
        }
        else if ( scopeObjectButton.getSelection() )
        {
            scope = SearchScope.OBJECT;
        }
        else
        {
            scope = SearchScope.ONELEVEL;
        }

        return scope;
    }


    // ── Luke's Depth-Decision Panel Goes Dark ────────────────────────────────────────
    // When Yoda signals "no more exploring today," Luke's decision board powers down —
    // the radio buttons grey out and stop responding. He can still read them, but can't
    // change the setting.
    // We propagate the enabled state to the group and all three radio buttons.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the entire widget. The group box and all three radio buttons
     * are toggled in one call. Pass {@code false} to lock the scope selection — useful
     * when showing the read-only properties of a running search.
     *
     * <p>For example — Luke's panel goes dark on Yoda's signal:</p>
     * <pre>
     *   scopeGroup.setEnabled( false );
     *   objectRadio.setEnabled( false );
     *   oneLevelRadio.setEnabled( false );
     *   subtreeRadio.setEnabled( false );
     * </pre>
     *
     * @param b  {@code true} to make the radios interactive; {@code false} to grey them.
     */
    public void setEnabled( boolean b )
    {
        scopeGroup.setEnabled( b );
        scopeObjectButton.setEnabled( b );
        scopeOnelevelButton.setEnabled( b );
        scopeSubtreeButton.setEnabled( b );
    }

}
