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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: AbstractWidget — REBEL ALLIANCE BASE COMMUNICATIONS RELAY ──────────
// Every Rebel base has a communications relay that keeps a list of operators
// who need to know when something changes.  When a new signal arrives, the
// relay fires an event to every registered operator so they can react.  This
// class is that relay for SWT widgets: it manages a list of
// {@link WidgetModifyListener} registrations and broadcasts a
// {@link WidgetModifyEvent} whenever a subclass widget changes.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We provide the base plumbing for listener registration and event notification
 * that all our custom widgets share.  Subclasses call {@link #notifyListeners}
 * whenever their state changes, and any interested party can register or
 * deregister via {@link #addWidgetModifyListener} and
 * {@link #removeWidgetModifyListener}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractWidget
{
    /** The listener list */
    protected List<WidgetModifyListener> modifyListenerList;


    // ── CONSTRUCTOR AbstractWidget — POWERING UP THE COMMUNICATIONS RELAY ─────
    // We initialize the relay with a small pre-allocated list of operator slots
    // so the first few registrations do not trigger any memory reallocation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We initialize the listener list with a small initial capacity of three.
     * Most widgets have only a handful of listeners, so this avoids unnecessary
     * resizing.
     */
    protected AbstractWidget()
    {
        modifyListenerList = new ArrayList<WidgetModifyListener>( 3 );
    }


    // ── METHOD addWidgetModifyListener — REGISTERING A NEW RELAY OPERATOR ─────
    // A new operator reports for duty at the communications relay.  We add them
    // to the operator list only if they have not already registered — we do not
    // want duplicate notifications going to the same listener.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We register the given listener to receive modification events from this
     * widget.  If the listener is already registered, we ignore the call to
     * prevent duplicate notifications.
     *
     * @param listener the listener to register
     */
    public void addWidgetModifyListener( WidgetModifyListener listener )
    {
        if ( !modifyListenerList.contains( listener ) )
        {
            modifyListenerList.add( listener );
        }
    }


    // ── METHOD removeWidgetModifyListener — RELIEVING A RELAY OPERATOR ────────
    // The operator is relieved from duty at the relay.  We remove them from the
    // list only if they are currently registered — no harm done if they were
    // never on the roster to begin with.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We remove the given listener so it no longer receives modification events.
     * If the listener is not currently registered, we do nothing.
     *
     * @param listener the listener to remove
     */
    public void removeWidgetModifyListener( WidgetModifyListener listener )
    {
        if ( modifyListenerList.contains( listener ) )
        {
            modifyListenerList.remove( listener );
        }
    }


    // ── METHOD notifyListeners — BROADCASTING THE SIGNAL TO ALL OPERATORS ─────
    // The relay fires a signal to every registered operator simultaneously.
    // We create a single event object and pass it to each listener in the list
    // so all interested parties learn about the change in one sweep.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We fire a {@link WidgetModifyEvent} to every currently registered
     * listener.  Subclasses call this whenever their internal state changes
     * and they want observers to know about it.
     */
    protected void notifyListeners()
    {
        WidgetModifyEvent event = new WidgetModifyEvent( this );

        for ( WidgetModifyListener listener : modifyListenerList )
        {
            listener.widgetModified( event );
        }
    }
}
