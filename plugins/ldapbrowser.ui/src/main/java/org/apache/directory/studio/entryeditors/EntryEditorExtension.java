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

package org.apache.directory.studio.entryeditors;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: EntryEditorExtension — LUKE'S X-WING REPAIR LOG ──────────────────
// Between missions, Luke spreads his tools across the Rebel hangar and catalogues
// every component of his X-wing: engine serial, callsign, weapon config, R2 socket.
// This class is that pre-flight checklist — a plain data record holding all the
// metadata Eclipse needs about one entry editor plugin extension.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A plain data bean holding the metadata registered by one entry editor extension point.
 * Eclipse's plugin registry reads the plugin XML and populates one of these per editor;
 * the {@link EntryEditorManager} then uses the populated bean to discover and
 * instantiate the real editor at runtime.
 * Think of this class as Luke's pre-flight checklist — every field is a slot that must
 * be filled before the X-wing (editor) can actually fly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorExtension
{
    /** The ID. */
    private String id = null;

    /** The name. */
    private String name = null;

    /** The description. */
    private String description = null;

    /** The icon. */
    private ImageDescriptor icon = null;

    /** The class name. */
    private String className = null;

    /** The editor id. */
    private String editorId = null;

    /** The multi window flag. */
    private boolean multiWindow = true;

    /** The priority. */
    private int priority = 0;

    /** The configuration element. */
    private IConfigurationElement member = null;

    /** The editor instance */
    private IEntryEditor editorInstance = null;


    // ── Reading the X-wing's Unique Callsign ────────────────────────────────────
    // Luke checks the nose of his X-wing and reads the squadron identifier painted
    // there — "Red Five" — so everyone knows which ship is which on the comms.
    // We return the unique plugin-defined ID for this editor extension the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique extension ID for this entry editor.
     * The ID comes straight from the plugin XML and is how the {@link EntryEditorManager}
     * looks up a specific editor by name — like calling "Red Five, standing by."
     *
     * @return  the extension ID string, or {@code null} if it hasn't been set yet
     */
    public String getId()
    {
        return id;
    }


    // ── Painting the Callsign on the Hull ───────────────────────────────────────
    // Luke's crew stencils "Red Five" on the fuselage before the mission briefing.
    // Without it, nobody knows which ship to scramble when the order comes.
    // We store the ID the same way so the manager can find this extension later.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the unique extension ID for this entry editor.
     * Called by the initialisation code that reads the plugin XML; not something
     * we call at runtime ourselves.
     *
     * @param id  the extension ID string from the plugin descriptor
     */
    public void setId( String id )
    {
        this.id = id;
    }


    // ── Reading the Ship's Human-Readable Designation ───────────────────────────
    // The hangar board lists "Luke Skywalker's T-65 X-wing" not just "Red Five".
    // That human-readable label is what appears in menus and preference pages.
    // We return that display name here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this entry editor extension.
     * This is the label shown to the user in the "Open With" menu and the
     * entry editors preference page — not the internal ID.
     *
     * @return  the human-readable name, or {@code null} if unset
     */
    public String getName()
    {
        return name;
    }


    // ── Writing the Ship's Designation on the Hangar Board ──────────────────────
    // The hangar crew updates the board with the ship's full name before a mission.
    // We do the same here — store the display name read from the plugin XML.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the display name of this entry editor extension.
     * Called during plugin initialisation when we parse the extension point XML.
     *
     * @param name  the human-readable label for this editor
     */
    public void setName( String name )
    {
        this.name = name;
    }


    // ── Pulling Up the Mission Briefing Notes ───────────────────────────────────
    // Before each mission, the Rebel briefing officer reads out what this particular
    // X-wing configuration is best suited for — trench run, escort, recon.
    // We surface that description so the UI can show tooltip text about this editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable description of this entry editor.
     * Shown as tooltip or help text in the preference page so users know what
     * each editor is good for.
     *
     * @return  the description string, or {@code null} if unset
     */
    public String getDescription()
    {
        return description;
    }


    // ── Writing the Mission Notes into the Log ──────────────────────────────────
    // The briefing officer scribbles the mission notes into the ship's log before
    // departure so anyone who opens it later knows what this configuration does.
    // We store the description from the plugin XML the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the description of this entry editor extension.
     * Called during initialisation from the plugin XML parser.
     *
     * @param description  the description text to store
     */
    public void setDescription( String description )
    {
        this.description = description;
    }


    // ── Reading the Squadron Insignia from the Wing ─────────────────────────────
    // Each X-wing carries a painted squadron crest — the visual identity that
    // appears on the comms screen so pilots can recognise each other at a glance.
    // We return the icon descriptor so the UI can render this editor's toolbar icon.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon descriptor for this entry editor.
     * The UI uses this to render the editor's icon in menus and tab titles.
     *
     * @return  the {@link ImageDescriptor} for the editor icon, or {@code null} if unset
     */
    public ImageDescriptor getIcon()
    {
        return icon;
    }


    // ── Affixing the Squadron Crest to the Fuselage ─────────────────────────────
    // Ground crew paints the Rebel starbird on the hull before the X-wing rolls
    // out — without it, it's just an anonymous grey shape among many.
    // We store the icon descriptor so the UI knows how to render this editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the icon descriptor for this entry editor.
     * Called during initialisation; we look up the icon path from the plugin XML
     * and turn it into an {@link ImageDescriptor}.
     *
     * @param icon  the image descriptor to use for this editor's icon
     */
    public void setIcon( ImageDescriptor icon )
    {
        this.icon = icon;
    }


    // ── Reading the Astromech Droid Class Designation ───────────────────────────
    // Luke checks which droid class is slotted into the R2 socket — not every
    // droid fits, and the wrong class won't interface with the nav computer.
    // We return the fully-qualified Java class name so Eclipse can instantiate it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the fully-qualified Java class name of the editor implementation.
     * Eclipse uses this via {@link IConfigurationElement#createExecutableExtension} to
     * instantiate the actual editor object from the contributing plugin's bundle.
     *
     * @return  the class name string, or {@code null} if unset
     */
    public String getClassName()
    {
        return className;
    }


    // ── Logging Which Droid Goes in the R2 Socket ───────────────────────────────
    // The maintenance log records "R2-D2, IM6 series" so the crew know which droid
    // goes into which ship — swapping them without checking causes navigation errors.
    // We store the class name from the plugin XML so Eclipse can load the right one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the fully-qualified Java class name of the editor implementation.
     * Called during initialisation from the plugin XML.
     *
     * @param className  the fully-qualified class name of the {@link IEntryEditor} implementation
     */
    public void setClassName( String className )
    {
        this.className = className;
    }


    // ── Reading the Cockpit Layout Identifier ───────────────────────────────────
    // Every X-wing variant has a cockpit configuration ID stamped into the avionics
    // — "T-65B" vs "T-70" — which tells the ground crew which control map to load.
    // We return the Eclipse editor part ID the workbench uses to open this editor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor part ID for this entry editor.
     * This is the ID registered in the {@code plugin.xml} {@code <editor>} element;
     * the workbench uses it to open and reference the correct editor part.
     *
     * @return  the editor part ID, or {@code null} if unset
     */
    public String getEditorId()
    {
        return editorId;
    }


    // ── Stamping the Cockpit Configuration into the Avionics Log ───────────────
    // Before the X-wing ships out, the avionics ID is stamped into the flight
    // computer so the control tower can route comms to the right ship variant.
    // We store the Eclipse editor ID from the plugin XML descriptor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Eclipse editor part ID for this entry editor.
     * Called during initialisation from the plugin XML.
     *
     * @param editorId  the Eclipse editor part ID string
     */
    public void setEditorId( String editorId )
    {
        this.editorId = editorId;
    }


    // ── Checking Whether the X-wing Flies in Formation ──────────────────────────
    // Some X-wings are single-sortie scouts — one pilot, one mission, one target.
    // Others fly in full squadrons, multiple ships opening on the same target.
    // We check whether this editor opens a new tab per entry (multi-window) or reuses one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor supports opening multiple independent windows.
     * When {@code true}, each LDAP entry gets its own editor tab; when {@code false},
     * all entries reuse the same single tab, replacing whatever was there before.
     *
     * @return  {@code true} if this editor can open multiple simultaneous windows
     */
    public boolean isMultiWindow()
    {
        return multiWindow;
    }


    // ── Setting the Formation-Flying Configuration ───────────────────────────────
    // The squadron leader designates certain X-wings as solo sorties and others as
    // formation fliers — the configuration gets written into the mission orders.
    // We store the multi-window flag from the plugin XML here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether this editor supports multiple independent windows.
     * Called during initialisation from the plugin XML.
     *
     * @param multiWindow  {@code true} to allow multiple tabs; {@code false} for single-tab mode
     */
    public void setMultiWindow( boolean multiWindow )
    {
        this.multiWindow = multiWindow;
    }


    // ── Reading the Mission Priority Ranking ─────────────────────────────────────
    // High-priority missions scramble first — Red Five gets off the ground before
    // the cargo hauler when the Death Star is inbound. Same logic here: higher
    // priority editors are tried first when we pick the best editor for an entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the priority value for this entry editor.
     * Higher numbers mean higher priority — the {@link EntryEditorManager} sorts
     * by this when deciding which editor to open by default for a given entry.
     *
     * @return  the integer priority; higher wins
     */
    public int getPriority()
    {
        return priority;
    }


    // ── Setting the Scramble Priority in the Mission Orders ──────────────────────
    // General Dodonna writes the scramble order: Red Five first, then Red Leader,
    // then the Y-wings. We record the priority from the plugin XML the same way.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the priority value for this entry editor.
     * Called during initialisation from the plugin XML.
     *
     * @param priority  integer priority; higher means this editor is preferred over lower-priority ones
     */
    public void setPriority( int priority )
    {
        this.priority = priority;
    }


    // ── Pulling the Original Tech Spec from the Parts Drawer ────────────────────
    // The original manufacturer spec sheet lives in the parts drawer — it's the
    // raw source of truth that the hangar crew used to build the repair log.
    // We return the raw Eclipse {@link IConfigurationElement} this bean was built from.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw Eclipse configuration element this extension was read from.
     * Useful if we need to call {@link IConfigurationElement#createExecutableExtension}
     * ourselves — it gives us access to the original plugin XML node.
     *
     * @return  the {@link IConfigurationElement}, or {@code null} if unset
     */
    public IConfigurationElement getMember()
    {
        return member;
    }


    // ── Filing the Original Spec Sheet in the Parts Drawer ──────────────────────
    // After reading the manufacturer spec, the crew files it for future reference
    // so they can look up anything not captured in the repair log itself.
    // We store the raw configuration element for the same reason.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Eclipse configuration element this extension was read from.
     * Called during initialisation; we keep it around in case we need to
     * reach back into the plugin registry for something not pre-parsed.
     *
     * @param member  the {@link IConfigurationElement} node from the extension registry
     */
    public void setMember( IConfigurationElement member )
    {
        this.member = member;
    }


    // ── Checking Whether the Droid Is Already Seated in the Cockpit ─────────────
    // The crew chief peeks into the R2 socket to see if R2 is already plugged in
    // and powered up — no point reinstantiating what's already running.
    // We return the live {@link IEntryEditor} instance if it was already created.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the already-instantiated {@link IEntryEditor} for this extension.
     * We create one instance per extension at startup and reuse it to answer
     * {@code canHandle()} queries — no need to re-instantiate via reflection each time.
     *
     * @return  the live editor instance, or {@code null} if not yet created
     */
    public IEntryEditor getEditorInstance()
    {
        return editorInstance;
    }


    // ── Seating R2-D2 in the X-wing's Astromech Socket ─────────────────────────
    // The ground crew powers up R2 and locks him into the socket during pre-flight.
    // From that point on, the live droid is used for all nav queries without reloading.
    // We store the live editor instance here so it can be queried without re-creation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the live {@link IEntryEditor} instance for this extension.
     * Called once during initialisation after the class is instantiated via
     * {@link IConfigurationElement#createExecutableExtension}.
     *
     * @param editorInstance  the instantiated editor object to cache
     */
    public void setEditorInstance( IEntryEditor editorInstance )
    {
        this.editorInstance = editorInstance;
    }


    // ── Printing the Full X-wing Repair Log for the Debrief ─────────────────────
    // After the mission, Luke reads out the full status of his X-wing over comms:
    // callsign, class, engine ID, weapon state — everything in one line.
    // We do the same, dumping all fields so the log is human-readable at a glance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a debug string listing all fields of this extension bean.
     * Useful for logging — if you're wondering why the wrong editor opened,
     * printing this gives you a snapshot of every field in one shot.
     *
     * @return  a string representation of this extension, listing all field values
     */
    @Override
    public String toString()
    {
        return "EntryEditorExtension [className=" + className + ", description=" + description + ", editorId=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + editorId + ", icon=" + icon + ", id=" + id + ", member=" + member + ", name=" + name + ", priority=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            + priority + ", multiWindow=" + multiWindow + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
