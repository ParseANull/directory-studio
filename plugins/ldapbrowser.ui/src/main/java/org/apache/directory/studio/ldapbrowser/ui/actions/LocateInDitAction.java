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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: LocateInDitAction — R2-D2 QUERIES AND SCROLLS TO THE ENTRY ────────
// R2-D2 plugs into the Death Star's computer terminal, queries for the exact
// system address he needs, and then navigates the ship to that location — even
// if the coordinates aren't cached locally, he'll fetch them from the server
// in the background. This abstract class handles the whole locate-and-navigate
// pipeline: resolve the connection and DN (delegated to subclasses), check the
// local entry cache, fetch from the LDAP server if needed, and finally scroll
// the browser tree to the found entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base action that navigates the LDAP browser tree to a specific
 * entry identified by a connection and DN.
 * Subclasses implement {@link #getConnectionAndDn()} to supply the target;
 * this class handles the rest: cache lookup, background fetch if needed, and
 * scrolling the {@link BrowserView} to the entry.
 * The Browser view is opened automatically if it is not currently visible.
 * Think of this class as R2-D2's core navigation subroutine — subclasses just
 * tell him where to go, and he handles all the terminal-querying mechanics.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LocateInDitAction extends BrowserAction
{
    // ── R2 Executes the Full Navigate Sequence ────────────────────────────────
    // R2 gets the target address from the subclass, checks his local cache,
    // and if the entry isn't there yet, he fires a background job to fetch it
    // from the server. Once the entry is in hand, he calls {@link #openInBrowser}
    // to scroll the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the locate operation: asks the subclass for a connection and DN,
     * checks the entry cache, and either navigates immediately or schedules a
     * background fetch followed by navigation on the UI thread.
     * This method is declared {@code final} — the navigation pipeline should not
     * be overridden; override {@link #getConnectionAndDn()} instead.
     */
    public final void run()
    {
        ConnectionAndDn connectionAndDn = getConnectionAndDn();
        if ( connectionAndDn != null )
        {
            IBrowserConnection connection = connectionAndDn.connection;
            Dn dn = connectionAndDn.dn;

            IEntry entry = connection.getEntryFromCache( dn );
            if ( entry != null )
            {
                openInBrowser( entry );
            }
            else
            {
                ReadEntryRunnable runnable = new ReadEntryRunnable( connection, dn );
                StudioBrowserJob job = new StudioBrowserJob( runnable );
                job.addJobChangeListener( new JobChangeAdapter()
                {
                    @Override
                    public void done( IJobChangeEvent event )
                    {
                        IEntry readEntry = runnable.getReadEntry();
                        if ( readEntry != null )
                        {
                            Display.getDefault().asyncExec( () -> openInBrowser( readEntry ) );
                        }
                    }
                } );
                job.execute();
            }
        }
    }


    // ── R2 Scrolls the Browser to the Entry ──────────────────────────────────
    // R2 has the entry; now he pilots the browser view to the right location —
    // opening the view if it isn't visible, then calling select() to scroll and
    // highlight the target entry in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link BrowserView} if necessary, then calls {@link BrowserView#select}
     * to scroll the tree to and highlight the given entry.
     * Must be called on the UI thread — use {@link Display#asyncExec} if calling
     * from a background job completion handler.
     *
     * @param entry  the entry to scroll to and highlight in the browser tree
     */
    private void openInBrowser( IEntry entry )
    {
        String targetId = BrowserView.getId();
        IViewPart targetView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
            targetId );
        if ( targetView == null )
        {
            try
            {
                targetView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    targetId, null, IWorkbenchPage.VIEW_ACTIVATE );
            }
            catch ( PartInitException e )
            {
            }
        }
        if ( targetView instanceof BrowserView )
        {
            ( ( BrowserView ) targetView ).select( entry );
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate( targetView );
        }
    }


    // ── R2 Returns His Command Port ID ───────────────────────────────────────
    // R2 checks the registry: this action is bound to the
    // {@code BrowserUIConstants.CMD_LOCATE_IN_DIT} command ID, which gives it
    // a keyboard shortcut configured elsewhere.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for the "locate in DIT" command, which
     * allows a keyboard shortcut to be bound to this action via the commands
     * extension point.
     *
     * @return  the command ID string; never {@code null}
     */
    public String getCommandId()
    {
        return BrowserUIConstants.CMD_LOCATE_IN_DIT;
    }


    // ── R2 Verifies He Has a Valid Target ────────────────────────────────────
    // R2 won't start the navigation sequence without a valid address — he
    // checks whether {@link #getConnectionAndDn()} would return something
    // non-null before declaring himself ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when {@link #getConnectionAndDn()} returns a non-null
     * value, meaning the action has a valid connection and DN to navigate to.
     *
     * @return  {@code true} if the action is applicable in the current context
     */
    public boolean isEnabled()
    {
        return getConnectionAndDn() != null;
    }


    // ── R2 Delegates to Subclass for Target Address ───────────────────────────
    // R2 can't navigate without coordinates — this abstract method is the hook
    // that subclasses implement to supply the connection and DN to navigate to.
    // Return null to indicate the action should be disabled for this selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConnectionAndDn} bundle that identifies the target entry
     * to navigate to, or {@code null} if the current selection does not yield
     * a navigable target.
     * Subclasses implement this to extract the relevant connection and DN from
     * whatever context they operate in (selected values, clipboard, Connections
     * view, etc.).
     *
     * @return  the target connection and DN, or {@code null} if not applicable
     */
    protected abstract ConnectionAndDn getConnectionAndDn();

    // ── CLASS: ConnectionAndDn — R2 Bundles Ship Plus Address ─────────────────
    // R2 needs two pieces of information to navigate: which ship (connection)
    // and which terminal address (DN). This inner class holds both together
    // as a simple pair that gets passed from subclass to {@link #run()}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A simple value-object pairing an {@link IBrowserConnection} with a
     * {@link Dn} — the two pieces of information needed to locate an entry in
     * the directory tree.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    protected class ConnectionAndDn
    {
        /** The connection */
        private IBrowserConnection connection;

        /** The Dn */
        private Dn dn;


        // ── R2 Packages the Navigation Bundle ────────────────────────────────
        // R2 wraps the connection and DN together so they travel as one unit
        // from the subclass all the way to the navigation logic in {@link #run()}.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code ConnectionAndDn} pairing the given connection
         * and DN.
         *
         * @param connection  the browser connection for the server hosting the entry; must not be null
         * @param dn          the distinguished name of the entry to navigate to; must not be null
         */
        protected ConnectionAndDn( IBrowserConnection connection, Dn dn )
        {
            this.connection = connection;
            this.dn = dn;
        }
    }
}
