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
package org.apache.directory.studio.templateeditor.actions;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.eclipse.jface.action.Action;


// ── CLASS: SimpleActionProxy — LANDO STANDING IN FOR HAN ─────────────────────────
// When Han Solo is in carbonite, Lando steps into his role: he wears the same
// cape, speaks for him in negotiations, and pulls off the same heist. But Lando
// isn't Han — he's a proxy who delegates everything back to Han's playbook.
// This class is exactly that: a standard JFace {@link Action} that wraps a
// {@link BrowserAction} (which has its own API) and delegates every call —
// run(), isEnabled() — through to the underlying BrowserAction. This lets us
// add browser-aware actions to JFace menus and toolbars that only understand
// the standard Action interface.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Adapter that wraps a {@link BrowserAction} as a standard JFace {@link Action}.
 * The LDAP Browser layer uses its own action API ({@link BrowserAction}) while
 * JFace menus and toolbars expect the standard {@link Action} API. This proxy
 * bridges the gap by copying the text, image, and command ID from the wrapped
 * action and delegating {@link #run()} and {@link #isEnabled()} calls through.
 * Think of this as Lando standing in for Han — same role, different person.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SimpleActionProxy extends Action
{
    /** The {@link BrowserAction}*/
    protected BrowserAction action;


    // ── CONSTRUCTOR (STYLE): LANDO TAKES HAN'S ROLE WITH A SPECIFIC STYLE ────────
    // Lando borrows Han's jacket and hat (text and image) and gets a specific
    // briefing on how to act (style: push button, check box, etc.). We copy the
    // visual attributes from the BrowserAction so the proxy looks identical in
    // any JFace menu.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the given {@link BrowserAction} as a JFace {@link Action} with the
     * specified style override. Copies label, image descriptor, and command ID
     * from the wrapped action.
     *
     * <p>For example — Lando takes Han's role with a specific style:</p>
     * <pre>
     *   new SimpleActionProxy(propertiesAction, Action.AS_PUSH_BUTTON);
     *   // Looks and acts like the BrowserAction in any JFace menu or toolbar.
     * </pre>
     *
     * @param action  the {@link BrowserAction} to delegate to
     * @param style   the JFace action style constant (e.g. {@link Action#AS_PUSH_BUTTON})
     */
    public SimpleActionProxy( BrowserAction action, int style )
    {
        super( action.getText(), style );
        this.action = action;

        super.setImageDescriptor( action.getImageDescriptor() );
        super.setActionDefinitionId( action.getCommandId() );
    }


    // ── CONSTRUCTOR (DEFAULT): LANDO TAKES HAN'S ROLE AS-IS ─────────────────────
    // Lando uses Han's default approach — same style, same everything. Just a
    // simpler constructor that infers the style from the wrapped BrowserAction.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the given {@link BrowserAction} using its own declared style.
     * Convenience overload of {@link #SimpleActionProxy(BrowserAction, int)}.
     *
     * <p>For example — Lando takes Han's role exactly as-is:</p>
     * <pre>
     *   new SimpleActionProxy(propertiesAction);
     *   // Uses propertiesAction.getStyle() automatically.
     * </pre>
     *
     * @param action  the {@link BrowserAction} to delegate to
     */
    public SimpleActionProxy( BrowserAction action )
    {
        this( action, action.getStyle() );
    }


    // ── RUN: LANDO EXECUTES HAN'S PLAY ───────────────────────────────────────────
    // Lando follows Han's script exactly: when the order comes in, he passes it
    // straight to Han's action object. If the underlying action is null (Han never
    // showed up), we just do nothing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates the run call to the wrapped {@link BrowserAction}. No-op if the
     * wrapped action is {@code null}.
     *
     * <p>For example — Lando executes Han's play:</p>
     * <pre>
     *   action.run();  // "Han would do it this way."
     * </pre>
     */
    public void run()
    {
        if ( action != null )
        {
            action.run();
        }
    }


    // ── IS ENABLED: LANDO CHECKS IF HAN WOULD SAY YES ────────────────────────────
    // Lando checks Han's notes: "Is this the right time to act?" If there's no
    // playbook (action is null), the answer is always no.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates the enabled-state check to the wrapped {@link BrowserAction}.
     * Returns {@code false} if the wrapped action is {@code null}.
     *
     * @return {@code true} if the wrapped action is enabled; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        if ( action != null )
        {
            return action.isEnabled();
        }
        else
        {
            return false;
        }
    }
}
