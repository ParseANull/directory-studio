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

package org.apache.directory.studio.ldifeditor.editor.actions;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;


// ── CLASS: OpenDefaultValueEditorAction — DEFAULT ESCAPE ROUTE DISPATCHER ─────
// The default "Edit Value" command in the context menu is a thin proxy that
// calls whatever R2 decided was the best route — the operator does not need to
// know which specialist editor will open.
// OpenDefaultValueEditorAction just mirrors the state of
// OpenBestValueEditorAction and delegates execution to it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action registered as the default "Edit Value" command.
 * Mirrors the label, image, and enabled state of its
 * {@link OpenBestValueEditorAction} proxy and delegates {@link #doRun()} to it.
 * Think of this as the default escape-route button that calls the best
 * specialist without the operator having to choose.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenDefaultValueEditorAction extends AbstractLdifAction
{

    /** The best-value-editor action this delegates to. */
    private OpenBestValueEditorAction proxy;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code OpenDefaultValueEditorAction} bound to {@code editor}
     * and proxying {@code proxy}.
     *
     * @param editor  the LDIF editor this action operates on
     * @param proxy   the {@link OpenBestValueEditorAction} to mirror and delegate to
     */
    public OpenDefaultValueEditorAction( LdifEditor editor, OpenBestValueEditorAction proxy )
    {
        super( Messages.getString( "OpenDefaultValueEditorAction.EditValue" ), editor ); //$NON-NLS-1$
        super.setActionDefinitionId( BrowserCommonConstants.ACTION_ID_EDIT_VALUE );
        this.proxy = proxy;
    }


    // ── MIRROR PROXY STATE ────────────────────────────────────────────────────
    // The dispatcher mirrors the proxy's current enabled state and icon so the
    // context menu always shows the right appearance.
    /**
     * {@inheritDoc}
     *
     * <p>Updates the proxy, then mirrors its enabled state and image
     * descriptor.</p>
     */
    public void update()
    {
        proxy.update();
        setEnabled( proxy.isEnabled() );
        setImageDescriptor( proxy.getImageDescriptor() );
    }


    // ── DELEGATE EXECUTION ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link OpenBestValueEditorAction#run()}.</p>
     */
    protected void doRun()
    {
        proxy.run();
    }

}
