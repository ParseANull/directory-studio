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

package org.apache.directory.studio.connection.core.event;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


// ── CLASS: ConnectionEventRegistry — REBEL COMMAND'S MISSION BROADCAST CENTER ──
// When Rebel High Command has news, they don't tell each pilot separately in
// person — they broadcast a coded dispatch and every pilot who has subscribed
// to that channel receives it via their own courier.
// This class is that broadcast center: static fire* methods push events to all
// registered listeners, each delivered by the listener's own EventRunner
// (which controls what thread the notification lands on).
// Per-thread suspension lets long-running jobs (like bulk edits) block the
// broadcast while they work, then re-enable it when done.  The rate-limit
// warning (>10 events/sec) helps us notice when something is spinning.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central pub/sub hub for all connection and connection-folder state changes.
 * Callers register {@link ConnectionUpdateListener}s along with an {@link EventRunner}
 * that controls notification delivery (e.g. a JFace async-exec runner for the UI thread).
 * Static {@code fire*} methods then broadcast events to every registered listener,
 * each via that listener's own runner so threading is handled correctly.
 *
 * <p>Key design points:</p>
 * <ul>
 *   <li>Event firing can be suspended per-thread so bulk operations don't spam listeners.</li>
 *   <li>A rate-limit warning is logged when more than 10 events fire per second.</li>
 *   <li>The inner {@link EventManager} clones the listener map before iterating to avoid
 *       {@link java.util.ConcurrentModificationException} if a listener is removed during
 *       event delivery.</li>
 * </ul>
 * Think of this as Rebel High Command's encrypted broadcast channel: every registered
 * pilot (listener) gets the news, delivered by their own courier (runner), unless they
 * have specifically gone radio-silent (suspended firing) for a mission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionEventRegistry
{

    /** Thread IDs for which event firing is currently suspended. */
    private static List<Long> suspendedEventFiringThreads = new ArrayList<Long>();

    /**
     * Lock object used to synchronize individual event deliveries inside
     * {@link EventManager#fire(EventRunnableFactory)}.
     */
    protected static Object lock = new Object();

    /** Rolling window of event firing timestamps (for rate-limit checks). */
    private static List<Long> fireTimeStamps = new ArrayList<Long>();

    /** Monotonically increasing count of all events fired since startup. */
    private static long fireCount = 0L;


    // ── IS EVENT FIRING SUSPENDED — CHECK RADIO-SILENCE STATUS ────────────────────
    // Obi-Wan's Force sensitivity sometimes gets overwhelmed by too many
    // disturbances hitting him at once — he warns the crew if that happens.
    // We check whether the current thread has suspended its event firing (like a
    // bulk-edit job), and if not, we count the event, prune old timestamps from the
    // rolling window, and warn if we're seeing more than 10 events per second.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if event firing is suspended in the calling thread.
     * As a side effect, increments the event counter and logs a warning if more
     * than 10 events have fired in the last second (possible UI refresh storm).
     *
     * @return  {@code true} if the current thread has suspended event firing.
     */
    protected static boolean isEventFiringSuspendedInCurrentThread()
    {
        boolean suspended = suspendedEventFiringThreads.contains( Thread.currentThread().getId() );

        // count the number of fired event in the last second
        // if more then five per second: print a warning
        if ( !suspended )
        {
            fireCount++;

            synchronized ( fireTimeStamps )
            {
                long now = System.currentTimeMillis();

                // remove all time stamps older than one second
                for ( Iterator<Long> it = fireTimeStamps.iterator(); it.hasNext(); )
                {
                    Long ts = it.next();
                    if ( ts + 1000 < now )
                    {
                        it.remove();
                    }
                    else
                    {
                        break;
                    }
                }

                fireTimeStamps.add( now );

                if ( fireTimeStamps.size() > 10 )
                {
                    String message = "Warning: More then " + fireTimeStamps.size() + " events were fired per second!"; //$NON-NLS-1$ //$NON-NLS-2$
                    ConnectionCorePlugin.getDefault().getLog().log(
                        new Status( IStatus.WARNING, ConnectionCoreConstants.PLUGIN_ID, message,
                            new Exception( message ) ) );
                }
            }
        }

        return suspended;
    }


    // ── GET FIRE COUNT — HOW MANY DISPATCHES HAVE WE SENT? ────────────────────────
    // We return the total number of events that have been fired since this
    // static class was loaded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the total number of events fired by this registry since JVM startup.
     * Useful in tests to verify that events did or did not fire.
     *
     * @return  The cumulative event-fire count.
     */
    public static long getFireCount()
    {
        return fireCount;
    }


    // ── RESUME EVENT FIRING — TAKE THE CURRENT THREAD OFF RADIO-SILENCE ───────────
    // The bulk job is done, so we remove this thread from the suspended list
    // and let events flow again.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Re-enables event firing in the calling thread.
     * Should be called in a {@code finally} block after
     * {@link #suspendEventFiringInCurrentThread()}.
     */
    public static void resumeEventFiringInCurrentThread()
    {
        synchronized ( suspendedEventFiringThreads )
        {
            suspendedEventFiringThreads.remove( Thread.currentThread().getId() );
        }
    }


    // ── SUSPEND EVENT FIRING — PUT THE CURRENT THREAD ON RADIO-SILENCE ────────────
    // During a big bulk operation we don't want a barrage of intermediate events
    // hitting listeners — we silence this thread until the operation finishes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents event firing from the calling thread until
     * {@link #resumeEventFiringInCurrentThread()} is called.
     * Use this at the start of a bulk operation and resume in a {@code finally} block.
     */
    public static void suspendEventFiringInCurrentThread()
    {
        synchronized ( suspendedEventFiringThreads )
        {
            suspendedEventFiringThreads.add( Thread.currentThread().getId() );
        }
    }

    /** The single EventManager instance for all ConnectionUpdateListener registrations. */
    private static final EventManager<ConnectionUpdateListener, EventRunner> connectionUpdateEventManager = new EventManager<ConnectionUpdateListener, EventRunner>();


    // ── ADD CONNECTION UPDATE LISTENER — TUNE IN TO THE BROADCAST CHANNEL ─────────
    // We register a pilot (listener) and assign them a courier (runner) to deliver
    // their dispatches on the right thread.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link ConnectionUpdateListener} to receive connection and folder events.
     * Each listener gets an {@link EventRunner} that controls the notification delivery thread.
     * A listener is only registered once — duplicate additions are silently ignored.
     *
     * @param listener  The listener to register.
     * @param runner    The runner that will deliver events to this listener.
     */
    public static void addConnectionUpdateListener( ConnectionUpdateListener listener, EventRunner runner )
    {
        connectionUpdateEventManager.addListener( listener, runner );
    }


    // ── REMOVE CONNECTION UPDATE LISTENER — SIGN OFF THE BROADCAST CHANNEL ────────
    // The pilot is leaving the channel — we remove them from the roster so they
    // stop receiving dispatches.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters a previously registered {@link ConnectionUpdateListener}.
     * No-op if the listener is not currently registered.
     *
     * @param listener  The listener to remove.
     */
    public static void removeConnectionUpdateListener( ConnectionUpdateListener listener )
    {
        connectionUpdateEventManager.removeListener( listener );
    }


    // ── FIRE CONNECTION OPENED — BROADCAST: A SHIP HAS MADE CONTACT ───────────────
    // Rebel Command sends the "connection opened" dispatch to all registered listeners.
    // Each listener gets it via their own courier (EventRunner).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection has been opened.
     *
     * @param connection  The newly opened connection.
     * @param source      The source of the event (for informational purposes).
     */
    public static void fireConnectionOpened( final Connection connection, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionOpened( connection );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE CONNECTION CLOSED — BROADCAST: A SHIP HAS GONE DARK ─────────────────
    // Rebel Command sends the "connection closed" dispatch to all registered listeners.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection has been closed.
     *
     * @param connection  The now-closed connection.
     * @param source      The source of the event.
     */
    public static void fireConnectionClosed( final Connection connection, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionClosed( connection );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE CONNECTION UPDATED — BROADCAST: A SHIP'S MANIFEST HAS CHANGED ────────
    // Someone edited the connection's parameters — name, host, auth — and we
    // broadcast that change to all listeners so they can refresh their views.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection's
     * configuration has been updated.
     *
     * @param connection  The connection whose parameters changed.
     * @param source      The source of the event.
     */
    public static void fireConnectionUpdated( final Connection connection, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionUpdated( connection );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE CONNECTION ADDED — BROADCAST: A NEW SHIP HAS JOINED THE FLEET ────────
    // A new connection has been saved to the ConnectionManager — we broadcast
    // that so the UI (Connections view) can add it to the list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a new connection has
     * been added to the connection manager.
     *
     * @param connection  The newly added connection.
     * @param source      The source of the event.
     */
    public static void fireConnectionAdded( final Connection connection, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionAdded( connection );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE CONNECTION REMOVED — BROADCAST: A SHIP HAS LEFT THE FLEET ────────────
    // A connection was deleted from the ConnectionManager — we broadcast so
    // the UI removes it from the connections list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection has
     * been removed from the connection manager.
     *
     * @param connection  The removed connection.
     * @param source      The source of the event.
     */
    public static void fireConnectionRemoved( final Connection connection, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionRemoved( connection );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE FOLDER MODIFIED — BROADCAST: A BINDER'S CONTENTS CHANGED ─────────────
    // Chewie rearranged the binders — a folder's name or membership changed, so
    // we broadcast so the tree viewer refreshes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection folder
     * has been modified (name changed, connections moved, etc.).
     *
     * @param connectionFolder  The modified folder.
     * @param source            The source of the event.
     */
    public static void fireConnectonFolderModified( final ConnectionFolder connectionFolder, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionFolderModified( connectionFolder );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE FOLDER ADDED — BROADCAST: CHEWIE MADE A NEW BINDER ──────────────────
    // A new folder was created — we broadcast so the tree viewer inserts it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a new connection
     * folder has been added.
     *
     * @param connectionFolder  The newly added folder.
     * @param source            The source of the event.
     */
    public static void fireConnectonFolderAdded( final ConnectionFolder connectionFolder, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionFolderAdded( connectionFolder );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── FIRE FOLDER REMOVED — BROADCAST: CHEWIE TOSSED A BINDER ──────────────────
    // A folder was deleted — we broadcast so the tree viewer removes it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link ConnectionUpdateListener}s that a connection folder
     * has been removed.
     *
     * @param connectionFolder  The removed folder.
     * @param source            The source of the event.
     */
    public static void fireConnectonFolderRemoved( final ConnectionFolder connectionFolder, final Object source )
    {
        EventRunnableFactory<ConnectionUpdateListener> factory = new EventRunnableFactory<ConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final ConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.connectionFolderRemoved( connectionFolder );
                    }
                };
            }
        };
        connectionUpdateEventManager.fire( factory );
    }


    // ── INNER CLASS: EventManager — THE SUBSCRIPTION DATABASE + COURIER DISPATCHER ──
    // Rebel Command keeps a database of which pilots have signed up for which
    // channels, and which courier carries their dispatches.
    // This inner class manages that database and dispatches events through it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Generic listener registry and event dispatcher.
     * Maintains a {@code Map<L, EventRunner>} where L is the listener type
     * and the runner controls delivery threading.
     * The {@link #fire(EventRunnableFactory)} method clones the map before
     * iterating to prevent concurrent-modification issues.
     *
     * @param <L>  The listener type.
     * @param <R>  The event runner type (must extend {@link EventRunner}).
     */
    public static class EventManager<L, R extends EventRunner>
    {
        /** Map from listener instance to its delivery runner. */
        private Map<L, EventRunner> listeners = new HashMap<L, EventRunner>();


        // ── ADD LISTENER — SIGN A PILOT UP FOR THE CHANNEL ────────────────────────
        // We only add if not already present — we don't want double-delivery.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Registers a listener with the given runner.
         * If the listener is already registered, this is a no-op (we don't add duplicates).
         *
         * @param listener  The listener to register.  Must not be {@code null}.
         * @param runner    The runner to deliver events to this listener.  Must not be {@code null}.
         */
        public void addListener( L listener, R runner )
        {
            assert listener != null;
            assert runner != null;

            synchronized ( listeners )
            {
                if ( !listeners.containsKey( listener ) )
                {
                    listeners.put( listener, runner );
                }
            }
        }


        // ── REMOVE LISTENER — PULL A PILOT OFF THE CHANNEL ────────────────────────
        // We remove the listener if they're there; otherwise silently do nothing.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Unregisters the given listener.
         * If the listener is not registered, this is a no-op.
         *
         * @param listener  The listener to remove.
         */
        public void removeListener( L listener )
        {
            synchronized ( listeners )
            {
                if ( listeners.containsKey( listener ) )
                {
                    listeners.remove( listener );
                }
            }
        }


        // ── FIRE — DISPATCH TO ALL REGISTERED LISTENERS ───────────────────────────
        // We check if event firing is suspended in this thread; if so, we return
        // immediately.  Otherwise we clone the listener map (so removals during
        // delivery don't cause exceptions), create a personalized runnable for each
        // listener via the factory, and hand it to that listener's runner.
        // ──────────────────────────────────────────────────────────────────────────
        /**
         * Dispatches the event described by the given factory to all registered listeners.
         * Each listener receives its own {@link EventRunnable} and is notified via its
         * registered {@link EventRunner}.
         * If event firing is suspended in the current thread, this method returns immediately.
         *
         * @param factory  Factory that creates a personalized {@link EventRunnable} for each listener.
         */
        public void fire( EventRunnableFactory<L> factory )
        {
            if ( isEventFiringSuspendedInCurrentThread() )
            {
                return;
            }

            Map<L, EventRunner> clone = new HashMap<L, EventRunner>( listeners );
            for ( final L listener : clone.keySet() )
            {
                EventRunner runner = clone.get( listener );
                synchronized ( lock )
                {
                    EventRunnable runnable = factory.createEventRunnable( listener );
                    runner.execute( runnable );
                }
            }
        }
    }
}
