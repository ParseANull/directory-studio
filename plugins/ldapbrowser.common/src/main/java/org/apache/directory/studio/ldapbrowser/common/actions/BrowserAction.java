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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.apache.directory.studio.connection.ui.actions.StudioAction;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserEntryPage;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserSearchResultPage;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: BrowserAction — VADER COMMANDING FROM THE EXECUTOR'S BRIDGE ────────
// On the bridge of the Executor, Vader stands at the center — every officer
// reports their status to him, and he issues commands to the fleet. He always
// knows what's selected (which ships are in position) and decides whether an
// action is enabled (whether conditions are right to strike). Concrete actions
// like "fire" or "intercept" extend his command framework without rebuilding
// the whole bridge.
// BrowserAction is that bridge: it holds all the current selection state
// (selected entries, attributes, values, searches, etc.) and provides the
// abstract contract every concrete LDAP browser action must implement.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all actions in the LDAP browser and entry editor.
 * Manages the current selection — entries, attributes, values, searches,
 * bookmarks, and more — and defines the contract that concrete actions must
 * fulfill: {@link #getText()}, {@link #getImageDescriptor()},
 * {@link #getCommandId()}, {@link #isEnabled()}, and {@link #run()}.
 * Think of this class as Vader's command bridge: it tracks what the fleet
 * has selected and lets concrete subcommands decide what to do with it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class BrowserAction extends StudioAction implements IWorkbenchWindowActionDelegate
{
    /** The selected Browser View Categories */
    private BrowserCategory[] selectedBrowserViewCategories;

    /** The selected Entries */
    private IEntry[] selectedEntries;

    /** The selected Browser Entry Pages */
    private BrowserEntryPage[] selectedBrowserEntryPages;

    /** The selected Searches */
    private ISearch[] selectedSearches;

    /** The selected Search Results */
    private ISearchResult[] selectedSearchResults;

    /** The selected Browser Search Result Pages */
    private BrowserSearchResultPage[] selectedBrowserSearchResultPages;

    /** The selected Bookmarks */
    private IBookmark[] selectedBookmarks;

    /** The selected Attributes */
    private IAttribute[] selectedAttributes;

    /** The selected Attribute Hierarchies */
    private AttributeHierarchy[] selectedAttributeHierarchies;

    /** The selected Values */
    private IValue[] selectedValues;

    /** The selectec LDIF Model */
    private LdifFile selectedLdifModel;

    /** The selected LDIF Containers */
    private LdifContainer[] selectedLdifContainers;

    /** The selected LDIF Parts */
    private LdifPart[] selectedLdifParts;

    /** The selected properties. */
    protected String[] selectedProperties;

    /** The input */
    private Object input;


    // ── VADER STEPS ONTO THE BRIDGE — COMMAND STATE INITIALIZED ──────────────
    // Vader boards the Executor and assumes his position at the center console.
    // Before any orders are issued, every status board is set to "no target"
    // — empty arrays, null model. This constructor calls init() to set up that
    // clean starting state so subclasses don't inherit stale selection data.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserAction and initializes all selection state to empty.
     * Subclasses should call this via {@code super()} (implicitly or explicitly)
     * before setting up their own state. Calls {@link #init()} internally.
     */
    protected BrowserAction()
    {
        init();
    }


    // ── VADER RECEIVES THE WINDOW ASSIGNMENT — BRIDGE CALIBRATED ─────────────
    // The Emperor's fleet reassigns Vader to a new sector window. The bridge
    // crew runs a quick system check (init()) to make sure all instruments are
    // zeroed for the new assignment. Eclipse calls this when a global action
    // delegate is bound to a workbench window.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window as a
     * global action delegate. We reset all selection state to clean via
     * {@link #init()} — the window parameter is accepted but not used directly.
     *
     * <p>For example — Vader reporting to a new command window:</p>
     * <pre>
     *   action.init( workbenchWindow ); // clears all targeting data
     * </pre>
     *
     * @param window  the workbench window this action is bound to — accepted
     *                to satisfy the {@link IWorkbenchWindowActionDelegate} contract.
     */
    @Override
    public void init( IWorkbenchWindow window )
    {
        init();
    }


    // ── VADER RELAYS THE COMMAND THROUGH THE INTERCOM ─────────────────────────
    // Vader's second-in-command receives the raw IAction signal from Eclipse's
    // command framework and passes it straight to Vader's own run() method.
    // This bridge method exists because IWorkbenchWindowActionDelegate passes
    // an IAction wrapper, but our own run() doesn't need it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Relays an Eclipse command framework invocation to our own {@link #run()}.
     * The {@link IAction} parameter is the Eclipse wrapper — we ignore it and
     * just call {@link #run()} directly because our subclasses do all the
     * real work there.
     *
     * @param action  the Eclipse action wrapper — not used, just forwarded.
     */
    @Override
    public void run( IAction action )
    {
        this.run();
    }


    // ── VADER UPDATES HIS TARGETING DISPLAY ───────────────────────────────────
    // A new ISelection arrives from the sector scanner. Vader updates every
    // status board on the bridge: which entries are in range, which attributes
    // are highlighted, which values are targeted. Then he re-evaluates whether
    // the strike is enabled and updates the action's text label.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes. We extract
     * every type of selectable object (entries, attributes, values, searches,
     * bookmarks, etc.) from the selection using {@link BrowserSelectionUtils}
     * and cache them in our fields. Then we update the action's enabled state
     * and text label so the menu item or toolbar button reflects reality.
     *
     * <p>For example — Vader updating every sensor board as the fleet repositions:</p>
     * <pre>
     *   selectionChanged( action, newSelection );
     *   // all getSelectedXxx() now return fresh data
     * </pre>
     *
     * @param action     the Eclipse action wrapper whose enabled state we update.
     * @param selection  the new workbench selection to extract beans from.
     */
    @Override
    public void selectionChanged( IAction action, ISelection selection )
    {
        setSelectedBrowserViewCategories( BrowserSelectionUtils.getBrowserViewCategories( selection ) );
        setSelectedEntries( BrowserSelectionUtils.getEntries( selection ) );
        setSelectedBrowserEntryPages( BrowserSelectionUtils.getBrowserEntryPages( selection ) );
        setSelectedSearchResults( BrowserSelectionUtils.getSearchResults( selection ) );
        setSelectedBrowserSearchResultPages( BrowserSelectionUtils.getBrowserSearchResultPages( selection ) );
        setSelectedBookmarks( BrowserSelectionUtils.getBookmarks( selection ) );

        setSelectedSearches( BrowserSelectionUtils.getSearches( selection ) );

        setSelectedAttributes( BrowserSelectionUtils.getAttributes( selection ) );
        setSelectedAttributeHierarchies( BrowserSelectionUtils.getAttributeHierarchie( selection ) );
        setSelectedValues( BrowserSelectionUtils.getValues( selection ) );

        action.setEnabled( this.isEnabled() );
        action.setText( this.getText() );
        action.setToolTipText( this.getText() );
    }


    // ── VADER ANNOUNCES THE COMMAND NAME ──────────────────────────────────────
    // Every order Vader issues has a code name — "prepare ion cannons", "close
    // the blast doors." This abstract method forces each concrete action to
    // provide the human-readable label shown in menus and tooltips.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for this action — what the user sees in the
     * right-click menu or toolbar tooltip. Each subclass provides its own label.
     *
     * @return the menu/toolbar label, or {@code null} if none.
     */
    public abstract String getText();


    // ── VADER'S INSIGNIA IDENTIFIES THE COMMAND ───────────────────────────────
    // Each of Vader's commands has an insignia — a visual symbol that appears
    // on the sector display. This method returns the icon shown next to the
     // action in Eclipse menus and toolbars.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action as an {@link ImageDescriptor}.
     * Each subclass provides its own icon using constants from
     * {@link org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants}.
     *
     * @return the image descriptor, or {@code null} if this action has no icon.
     */
    public abstract ImageDescriptor getImageDescriptor();


    // ── VADER BROADCASTS ON THE SECURE CHANNEL ID ─────────────────────────────
    // Each of Vader's command frequencies is a unique channel ID — used by the
    // Eclipse key-binding system to wire keyboard shortcuts to the right action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action, used to bind keyboard
     * shortcuts and other triggers to it. Usually one of the CMD_ constants
     * from {@link org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants}.
     *
     * @return the command ID string.
     */
    public abstract String getCommandId();


    // ── VADER CHECKS IF THE STRIKE IS AUTHORIZED ──────────────────────────────
    // Vader evaluates the tactical situation: are the right targets in range?
    // Is the weapon charged? This method lets each concrete action decide
    // whether the current selection makes the action valid. The result controls
    // whether the menu item or toolbar button is greyed out.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this action should currently be enabled (clickable).
     * Each subclass inspects the current selection fields (e.g., {@link #getSelectedEntries()})
     * to decide whether it has enough context to run. Returns {@code false} if
     * the action would fail or make no sense with the current selection.
     *
     * @return {@code true} if enabled; {@code false} if greyed out.
     */
    public abstract boolean isEnabled();


    // ── VADER EXECUTES THE ORDER ───────────────────────────────────────────────
    // The command is given, the fleet moves. Each concrete action implements
    // this method to do its specific work — open a dialog, issue an LDAP
    // operation, update the model. The abstract declaration here forces
    // subclasses to provide the actual behavior.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Executes this action. Called when the user clicks the menu item, toolbar
     * button, or presses the keyboard shortcut. Each subclass defines what
     * "running" means for their specific operation.
     */
    public abstract void run();


    // ── VADER ZEROS ALL STATUS BOARDS ─────────────────────────────────────────
    // Before Vader takes the bridge, the crew clears every targeting console
    // to ensure no stale data lingers from the previous watch. This private
    // method sets every selection field to an empty array or null so no
    // accidental state leaks between selections.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Resets all selection state to empty arrays and null. Called from the
     * constructor and from {@link #init(IWorkbenchWindow)} to start clean.
     * Ensures that any selection-inspection methods return empty results
     * rather than stale data from a previous event.
     */
    private void init()
    {
        selectedBrowserViewCategories = new BrowserCategory[0];
        selectedEntries = new IEntry[0];
        selectedBrowserEntryPages = new BrowserEntryPage[0];
        selectedSearches = new ISearch[0];
        selectedSearchResults = new ISearchResult[0];
        selectedBrowserSearchResultPages = new BrowserSearchResultPage[0];
        selectedBookmarks = new IBookmark[0];
        selectedAttributes = new IAttribute[0];
        selectedAttributeHierarchies = new AttributeHierarchy[0];
        selectedValues = new IValue[0];

        selectedLdifModel = null;
        selectedLdifContainers = new LdifContainer[0];
        selectedLdifParts = new LdifPart[0];

        selectedProperties = new String[0];

        input = null;
    }


    // ── VADER ABANDONS THE BRIDGE — ALL CONSOLES CLEARED ─────────────────────
    // Vader leaves the Executor — all status boards are wiped, all targeting
    // locks released. This dispose method resets selection state to empty when
    // the action is no longer needed, allowing garbage collection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We reset all selection fields to empty arrays and null to help the GC
     * and to ensure no stale references hold onto model objects after the
     * action is gone.
     */
    @Override
    public void dispose()
    {
        selectedBrowserViewCategories = new BrowserCategory[0];
        selectedEntries = new IEntry[0];
        selectedBrowserEntryPages = new BrowserEntryPage[0];
        selectedSearches = new ISearch[0];
        selectedSearchResults = new ISearchResult[0];
        selectedBrowserSearchResultPages = new BrowserSearchResultPage[0];
        selectedBookmarks = new IBookmark[0];
        selectedAttributes = new IAttribute[0];
        selectedAttributeHierarchies = new AttributeHierarchy[0];
        selectedValues = new IValue[0];

        selectedLdifModel = null;
        selectedLdifContainers = new LdifContainer[0];
        selectedLdifParts = new LdifPart[0];

        selectedProperties = new String[0];

        input = null;
    }


    // ── VADER LOCATES THE ACTIVE BRIDGE WINDOW ────────────────────────────────
    // To open a dialog or display an error, we need the current active Shell —
    // the SWT parent window. This method asks the Eclipse workbench for the
    // currently focused shell, which is where dialogs should be parented.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently active SWT {@link Shell}. Dialogs opened by
     * subclasses should be parented to this shell so they appear in the right
     * window and get the correct modal blocking behavior.
     *
     * @return the active shell from {@link PlatformUI#getWorkbench()}.
     */
    protected Shell getShell()
    {
        return PlatformUI.getWorkbench().getDisplay().getActiveShell();
    }


    // ── VADER CHECKS WHICH ATTRIBUTES ARE ON THE TARGETING SCREEN ────────────
    // The attribute display on the bridge shows whatever's currently highlighted.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attributes currently selected in the active view.
     * Each {@link IAttribute} represents one attribute (e.g., "mail") from
     * one or more LDAP entries.
     *
     * @return the selected attributes array; never null, may be empty.
     */
    public IAttribute[] getSelectedAttributes()
    {
        return selectedAttributes;
    }


    // ── VADER UPDATES THE ATTRIBUTE TARGETING SCREEN ─────────────────────────
    // The sensor operator pipes new attribute data to Vader's console.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected LDAP attributes. Called by
     * {@link #selectionChanged} after extracting attribute objects from the
     * JFace selection.
     *
     * @param selectedAttributes  the new set of selected attributes to cache;
     *                            must not be null — use an empty array instead.
     */
    public void setSelectedAttributes( IAttribute[] selectedAttributes )
    {
        this.selectedAttributes = selectedAttributes;
    }


    // ── VADER CHECKS THE BOOKMARK MANIFEST ────────────────────────────────────
    // Bookmarks are pre-saved entry references — like saved coordinates in the
    // nav computer. This getter tells the action which bookmarks are targeted.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP bookmarks currently selected in the active view.
     * Bookmarks are named shortcuts to specific LDAP entries.
     *
     * @return the selected bookmarks array; never null, may be empty.
     */
    public IBookmark[] getSelectedBookmarks()
    {
        return selectedBookmarks;
    }


    // ── VADER UPDATES THE BOOKMARK MANIFEST ───────────────────────────────────
    // New bookmark coordinates are piped in from the nav computer.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected bookmarks. Called by {@link #selectionChanged}.
     *
     * @param selectedBookmarks  the new set of selected bookmarks; must not be null.
     */
    public void setSelectedBookmarks( IBookmark[] selectedBookmarks )
    {
        this.selectedBookmarks = selectedBookmarks;
    }


    // ── VADER SCANS THE BROWSER CATEGORY DISPLAY ─────────────────────────────
    // The category nodes (DIT, Searches, Bookmarks) in the browser view are
    // like the high-level fleet groupings on Vader's sector map.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser view category nodes currently selected. Categories
     * are the top-level grouping nodes in the browser tree (DIT, Searches,
     * Bookmarks).
     *
     * @return the selected categories; never null, may be empty.
     */
    public BrowserCategory[] getSelectedBrowserViewCategories()
    {
        return selectedBrowserViewCategories;
    }


    // ── VADER UPDATES THE SECTOR MAP GROUPINGS ────────────────────────────────
    // The sector map is updated with the latest grouping selections.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected browser view category nodes.
     * Called by {@link #selectionChanged}.
     *
     * @param selectedBrowserViewCategories  the new set of selected categories;
     *                                       must not be null.
     */
    public void setSelectedBrowserViewCategories( BrowserCategory[] selectedBrowserViewCategories )
    {
        this.selectedBrowserViewCategories = selectedBrowserViewCategories;
    }


    // ── VADER READS THE ENTRY TARGETING DATA ─────────────────────────────────
    // Entries are the actual ships in range — the specific LDAP directory
    // objects the user has highlighted in the tree view.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entries currently selected in the active view.
     * An {@link IEntry} represents one node in the LDAP directory tree
     * with a distinct DN and a set of attributes.
     *
     * @return the selected entries; never null, may be empty.
     */
    public IEntry[] getSelectedEntries()
    {
        return selectedEntries;
    }


    // ── VADER UPDATES THE ENTRY TARGETING DATA ────────────────────────────────
    // Fresh entry coordinates come in from the sensor sweep.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected LDAP entries. Called by {@link #selectionChanged}.
     *
     * @param selectedEntries  the new set of selected entries; must not be null.
     */
    public void setSelectedEntries( IEntry[] selectedEntries )
    {
        this.selectedEntries = selectedEntries;
    }


    // ── VADER READS THE ACTIVE SEARCH ASSIGNMENTS ─────────────────────────────
    // Saved searches are like standing patrol routes — each has a name, a base
    // DN, and a filter. This getter returns which searches are currently selected.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP searches currently selected in the active view.
     * An {@link ISearch} represents a saved LDAP search with its parameters
     * (base DN, filter, scope, returning attributes).
     *
     * @return the selected searches; never null, may be empty.
     */
    public ISearch[] getSelectedSearches()
    {
        return selectedSearches;
    }


    // ── VADER UPDATES THE PATROL ROUTE ASSIGNMENTS ────────────────────────────
    // The nav officer updates the active patrol route display.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected searches. Called by {@link #selectionChanged}.
     *
     * @param selectedSearches  the new set of selected searches; must not be null.
     */
    public void setSelectedSearches( ISearch[] selectedSearches )
    {
        this.selectedSearches = selectedSearches;
    }


    // ── VADER READS THE SEARCH RESULT MANIFEST ────────────────────────────────
    // Search results are the entries returned by a completed patrol scan.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP search results currently selected. An {@link ISearchResult}
     * wraps an entry returned by a specific search operation.
     *
     * @return the selected search results; never null, may be empty.
     */
    public ISearchResult[] getSelectedSearchResults()
    {
        return selectedSearchResults;
    }


    // ── VADER UPDATES THE SEARCH RESULT MANIFEST ──────────────────────────────
    // The sensor readout pipes in the latest patrol results.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected search results. Called by {@link #selectionChanged}.
     *
     * @param selectedSearchResults  the new set of selected results; must not be null.
     */
    public void setSelectedSearchResults( ISearchResult[] selectedSearchResults )
    {
        this.selectedSearchResults = selectedSearchResults;
    }


    // ── VADER READS THE VALUE TARGETING DATA ──────────────────────────────────
    // Values are the finest-grained targets — specific attribute values within
    // a specific entry. Like the exact coordinates of the exhaust port.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP attribute values currently selected. An {@link IValue}
     * is a single value within a single attribute within a single entry.
     *
     * @return the selected values; never null, may be empty.
     */
    public IValue[] getSelectedValues()
    {
        return selectedValues;
    }


    // ── VADER UPDATES THE VALUE TARGETING DATA ────────────────────────────────
    // New precision targeting coordinates are piped in.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected LDAP attribute values. Called by {@link #selectionChanged}.
     *
     * @param selectedValues  the new set of selected values; must not be null.
     */
    public void setSelectedValues( IValue[] selectedValues )
    {
        this.selectedValues = selectedValues;
    }


    // ── VADER READS THE INPUT SOURCE ──────────────────────────────────────────
    // "Input" here is the context object injected into the action from
    // outside — like the editor's current document. Used by actions that
    // are driven by an editor's input rather than a tree selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the optional input object for this action. Some actions (e.g.,
     * those bound to an entry editor) receive their context via this input
     * rather than from the JFace selection.
     *
     * @return the input object, or {@code null} if none was set.
     */
    public Object getInput()
    {
        return input;
    }


    // ── VADER SETS THE INPUT SOURCE ───────────────────────────────────────────
    // The editor's document is injected into Vader's command context.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the optional input object for this action. Called by action proxies
     * or editors that need to give the action a context object independent of
     * the JFace workbench selection.
     *
     * @param input  the context object to inject; may be {@code null}.
     */
    public void setInput( Object input )
    {
        this.input = input;
    }


    // ── VADER READS THE LDIF CONTAINER TARGETING DATA ─────────────────────────
    // LDIF containers are blocks in an LDIF file — change records, content
    // records. These are targets in the LDIF editor view.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF containers currently selected (in the LDIF editor view).
     * An {@link LdifContainer} is a single record block in an LDIF file.
     *
     * @return the selected LDIF containers; never null, may be empty.
     */
    public LdifContainer[] getSelectedLdifContainers()
    {
        return selectedLdifContainers;
    }


    // ── VADER UPDATES THE LDIF CONTAINER DATA ─────────────────────────────────
    // New LDIF block targets are identified from the LDIF editor selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected LDIF containers. Called when the LDIF editor's
     * selection changes.
     *
     * @param selectedLdifContainers  the new set of selected containers; must not be null.
     */
    public void setSelectedLdifContainers( LdifContainer[] selectedLdifContainers )
    {
        this.selectedLdifContainers = selectedLdifContainers;
    }


    // ── VADER READS THE LDIF DOCUMENT MODEL ───────────────────────────────────
    // The LDIF model is the parsed representation of the entire LDIF file —
    // like the full tactical map of all enemy positions at once.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF file model currently selected in the LDIF editor.
     * An {@link LdifFile} is the parsed top-level representation of an
     * entire LDIF document.
     *
     * @return the selected LDIF model, or {@code null} if not in an LDIF context.
     */
    public LdifFile getSelectedLdifModel()
    {
        return selectedLdifModel;
    }


    // ── VADER UPDATES THE LDIF DOCUMENT MODEL ─────────────────────────────────
    // A new LDIF document is opened in the editor — the full tactical map changes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDIF file model for the current LDIF editor context.
     *
     * @param selectedLdifModel  the LDIF file model, or {@code null} to clear.
     */
    public void setSelectedLdifModel( LdifFile selectedLdifModel )
    {
        this.selectedLdifModel = selectedLdifModel;
    }


    // ── VADER READS THE LDIF PARTS IN RANGE ───────────────────────────────────
    // LDIF parts are the individual tokens inside an LDIF container — like
    // specific subsystem readings within a single enemy ship's telemetry.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDIF parts (individual tokens/lines) currently selected
     * in the LDIF editor. An {@link LdifPart} is a constituent piece of an
     * LDIF container, such as a single attribute value line.
     *
     * @return the selected LDIF parts; never null, may be empty.
     */
    public LdifPart[] getSelectedLdifParts()
    {
        return selectedLdifParts;
    }


    // ── VADER UPDATES THE LDIF PARTS IN RANGE ────────────────────────────────
    // The LDIF editor highlights new individual token targets.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected LDIF parts. Called when the LDIF editor's
     * selection changes at the token level.
     *
     * @param selectedLdifParts  the new set of selected LDIF parts; must not be null.
     */
    public void setSelectedLdifParts( LdifPart[] selectedLdifParts )
    {
        this.selectedLdifParts = selectedLdifParts;
    }


    // ── VADER READS THE BROWSER ENTRY PAGE DATA ───────────────────────────────
    // When there are too many entries to show at once, they're split across
    // pages — like dividing a large fleet into squadrons for display.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser entry page nodes currently selected. Entry pages
     * are the pagination nodes shown in the browser tree when a parent has
     * more children than the folding threshold.
     *
     * @return the selected entry pages; never null, may be empty.
     */
    public BrowserEntryPage[] getSelectedBrowserEntryPages()
    {
        return selectedBrowserEntryPages;
    }


    // ── VADER UPDATES THE BROWSER ENTRY PAGE DATA ─────────────────────────────
    // A different page of entries is selected in the browser tree.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected browser entry page nodes.
     * Called by {@link #selectionChanged}.
     *
     * @param selectedBrowserEntryPages  the new set of selected entry pages;
     *                                   must not be null.
     */
    public void setSelectedBrowserEntryPages( BrowserEntryPage[] selectedBrowserEntryPages )
    {
        this.selectedBrowserEntryPages = selectedBrowserEntryPages;
    }


    // ── VADER READS THE SEARCH RESULT PAGE DATA ───────────────────────────────
    // Same paging idea but for search results — large result sets get split
    // into pages in the browser tree.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser search result page nodes currently selected. These
     * are pagination nodes shown when a search returns more results than
     * the folding threshold.
     *
     * @return the selected search result pages; never null, may be empty.
     */
    public BrowserSearchResultPage[] getSelectedBrowserSearchResultPages()
    {
        return selectedBrowserSearchResultPages;
    }


    // ── VADER UPDATES THE SEARCH RESULT PAGE DATA ─────────────────────────────
    // A different page of search results is selected.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected browser search result page nodes.
     * Called by {@link #selectionChanged}.
     *
     * @param selectedBrowserSearchResultPages  the new set of selected result pages;
     *                                          must not be null.
     */
    public void setSelectedBrowserSearchResultPages( BrowserSearchResultPage[] selectedBrowserSearchResultPages )
    {
        this.selectedBrowserSearchResultPages = selectedBrowserSearchResultPages;
    }


    // ── VADER READS THE ATTRIBUTE HIERARCHY DISPLAY ───────────────────────────
    // Attribute hierarchies group related attributes together (e.g., all the
    // "name" aliases). Like grouping related ship classes on the sector map.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute hierarchies currently selected. An
     * {@link AttributeHierarchy} groups an attribute with its sub-type
     * attributes in the LDAP schema hierarchy.
     *
     * @return the selected attribute hierarchies; never null, may be empty.
     */
    public AttributeHierarchy[] getSelectedAttributeHierarchies()
    {
        return selectedAttributeHierarchies;
    }


    // ── VADER UPDATES THE ATTRIBUTE HIERARCHY DISPLAY ─────────────────────────
    // The schema hierarchy sensor feeds new grouping data to the bridge.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected attribute hierarchies. Called by
     * {@link #selectionChanged}.
     *
     * @param ahs  the new set of selected attribute hierarchies; must not be null.
     */
    public void setSelectedAttributeHierarchies( AttributeHierarchy[] ahs )
    {
        this.selectedAttributeHierarchies = ahs;
    }


    // ── VADER READS THE PROPERTY DISPLAY ──────────────────────────────────────
    // Properties are string-keyed metadata about the current context —
    // used by the properties view actions to know which property page to open.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string properties currently selected. These are typically
     * property page IDs used by the Properties action to open the right page
     * in Eclipse's Properties dialog.
     *
     * @return the selected properties array; never null, may be empty.
     */
    public String[] getSelectedProperties()
    {
        return selectedProperties;
    }


    // ── VADER UPDATES THE PROPERTY DISPLAY ───────────────────────────────────
    // A new set of property identifiers is recorded for the current selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the currently selected property strings.
     *
     * @param selectedProperties  the new set of property strings; must not be null.
     */
    public void setSelectedProperties( String[] selectedProperties )
    {
        this.selectedProperties = selectedProperties;
    }
}
