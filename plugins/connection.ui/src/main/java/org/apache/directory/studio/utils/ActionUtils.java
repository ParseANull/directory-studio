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

package org.apache.directory.studio.utils;


import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;


// ── CLASS: ActionUtils — R2-D2 WIRES UP THE SHIP'S CONTROL INTERFACE ─────────────
// When R2-D2 plugs into a starship's computer, he activates the relevant control
// handlers so the pilot's buttons actually do something — and when he unplugs,
// he deactivates them so stray inputs don't trigger commands.
// ActionUtils is the same idea for Eclipse keyboard shortcuts and global action
// handlers.  "Activating" an action means wrapping it in an ActionHandler and
// registering it with the workbench ICommandService so keyboard shortcuts route to
// it.  "Deactivating" it removes that registration — but only if the handler that
// is currently installed actually belongs to the action we're removing (to avoid
// accidentally unregistering someone else's handler).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods for registering and deregistering Eclipse
 * {@link IAction} objects as workbench command handlers.
 *
 * <p>Eclipse actions (JFace {@link IAction}) can be wired into the global
 * command framework via an {@link ActionHandler} so that keyboard shortcuts
 * defined in {@code plugin.xml} are routed to the action.  This class makes that
 * plumbing a one-liner for callers.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ActionUtils
{
    // ── CONSTRUCTOR — UTILITY CLASS ───────────────────────────────────────────────
    /**
     * Private constructor — this is a utility class and must not be instantiated.
     */
    private ActionUtils()
    {
    }


    // ── DEACTIVATE ACTION HANDLER ─────────────────────────────────────────────────
    /**
     * Deregisters the global command handler for the given action.
     *
     * <p>The handler is only removed if the command's current handler is an
     * {@link ActionHandler} wrapping exactly this {@code action} instance (identity
     * check via {@code ==}), or if there is a non-{@code ActionHandler} handler
     * installed.  This prevents us from accidentally deregistering a handler that
     * was registered by a different part of the workbench.</p>
     *
     * <p>Does nothing if the workbench {@link ICommandService} is not available or
     * if {@link IAction#getActionDefinitionId()} returns a command that does not
     * exist.</p>
     *
     * @param action The {@link IAction} whose global command handler should be
     *               removed.
     */
    public static void deactivateActionHandler( IAction action )
    {
        ICommandService commandService = PlatformUI.getWorkbench().getAdapter(
            ICommandService.class );

        if ( commandService != null )
        {
            // ── LOOK UP THE COMMAND ───────────────────────────────────────────────
            Command command = commandService.getCommand( action.getActionDefinitionId() );
            IHandler handler = command.getHandler();

            if ( handler instanceof ActionHandler )
            {
                ActionHandler actionHandler = ( ActionHandler ) handler;

                // ── ONLY DEREGISTER OUR OWN HANDLER ──────────────────────────────
                // If a different part registered its own handler for this command we
                // leave it alone — only remove ours.
                // ──────────────────────────────────────────────────────────────────
                if ( actionHandler.getAction() == action )
                {
                    command.setHandler( null );
                }
            }
            else if ( handler != null )
            {
                command.setHandler( null );
            }
        }
    }


    // ── ACTIVATE ACTION HANDLER ───────────────────────────────────────────────────
    /**
     * Registers the given action as the global command handler for its associated
     * command ID.
     *
     * <p>The action's {@link IAction#getActionDefinitionId()} is used to look up the
     * Eclipse command.  A new {@link ActionHandler} is created and set on that
     * command, so any keyboard shortcut bound to the command in {@code plugin.xml}
     * will now invoke this action.</p>
     *
     * <p>Does nothing if the workbench {@link ICommandService} is not available.</p>
     *
     * @param action The {@link IAction} to register as the active command handler.
     */
    public static void activateActionHandler( IAction action )
    {
        ICommandService commandService = PlatformUI.getWorkbench().getAdapter(
            ICommandService.class );

        if ( commandService != null )
        {
            // ── WRAP AND REGISTER ─────────────────────────────────────────────────
            ActionHandler actionHandler = new ActionHandler( action );
            commandService.getCommand( action.getActionDefinitionId() ).setHandler( actionHandler );
        }
    }
}
