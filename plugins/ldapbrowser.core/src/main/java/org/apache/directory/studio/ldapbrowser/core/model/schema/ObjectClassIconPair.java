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

package org.apache.directory.studio.ldapbrowser.core.model.schema;


// ── CLASS: ObjectClassIconPair — JEDI ARCHIVES VISUAL EMBLEM REGISTRY ────────
// The Jedi Archives maintain an emblem registry: each known rank (object class)
// maps to a visual crest (icon path).  A Padawan's entry shows a Padawan badge;
// a Master's entry shows a lightsaber crest.  ObjectClassIconPair binds one or
// more object class OIDs to the path of the icon that represents them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Bean that pairs one or more LDAP object class OIDs with the path to the icon
 * that should be displayed for entries bearing those object classes.
 *
 * <p>Think of this as an emblem assignment in the Jedi Archives — object class
 * OIDs on one side, icon path on the other.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassIconPair
{

    /** The object classes numeric OIDs. */
    private String[] objectClassNumericOIDs;

    /** The path to the icon. */
    private String iconPath;


    // ── Archivist Stamps A Blank Emblem Record ───────────────────────────────────
    // An archivist pulls a blank emblem card — no OIDs, no icon yet assigned.
    // Config and serialization frameworks need this no-arg form to round-trip.
    // Fields default to null; setters must be called before any getter is useful.
    // Prefer the two-arg constructor when both fields are known at creation time.
    /**
     * Creates a new instance of ObjectClassIconPair.
     */
    public ObjectClassIconPair()
    {
    }


    // ── Archivist Files A Fully Filled Emblem Card In One Stroke ────────────────
    // A master archivist writes the OIDs and icon path onto the card immediately.
    // Both fields are stamped at construction; no blank-state risk.
    // The super() call is retained as a reminder that Object is the root ancestor.
    // This is the preferred factory when both the class OIDs and icon are known.
    /**
     * Creates a new instance of ObjectClassIconPair.
     *
     * @param objectClassNumericOIDs the object class numeric OIDs
     * @param iconPath the icon path
     */
    public ObjectClassIconPair( String[] objectClassNumericOIDs, String iconPath )
    {
        super();
        this.objectClassNumericOIDs = objectClassNumericOIDs;
        this.iconPath = iconPath;
    }


    // ── Archivist Reads The Object Class OID List From The Emblem Card ───────────
    // The emblem registry asks: "which OIDs does this icon cover?"
    // The archivist returns the full array — may contain one OID or several.
    // The UI matches an entry's object classes against this array to pick an icon.
    // Returns the live array reference; callers should not mutate it.
    /**
     * Gets the object class numeric OIDs.
     *
     * @return the object class numeric OIDs
     */
    public String[] getOcNumericOids()
    {
        return objectClassNumericOIDs;
    }


    // ── Archivist Replaces The OID List On The Emblem Card ───────────────────────
    // A master archivist updates the card after a schema reorganisation —
    // the old OID array is discarded and the new one is written in its place.
    // All subsequent getOcNumericOids calls return the updated array.
    // Config frameworks use this setter during post-construction initialisation.
    /**
     * Sets the object class numeric OIDs.
     *
     * @param objectClassNumericOIDs the object class numeric OIDs
     */
    public void setOcNumericOids( String[] objectClassNumericOIDs )
    {
        this.objectClassNumericOIDs = objectClassNumericOIDs;
    }


    // ── Archivist Reads The Icon Path From The Emblem Card ───────────────────────
    // The UI asks: "what icon should I display for this set of object classes?"
    // The archivist reads the right column — the plugin-relative icon path.
    // The caller loads the image resource from this path via the bundle registry.
    // Returns the raw path string exactly as stored; never resolved to an Image.
    /**
     * Gets the icon path.
     *
     * @return the icon path
     */
    public String getIconPath()
    {
        return iconPath;
    }


    // ── Archivist Updates The Icon Path On The Emblem Card ───────────────────────
    // A master archivist swaps the icon — say, after a visual redesign.
    // The old path is replaced with the new plugin-relative resource path.
    // All subsequent getIconPath calls return the updated path.
    // Config frameworks use this setter during post-construction initialisation.
    /**
     * Sets the icon path.
     *
     * @param iconPath the new icon path
     */
    public void setIconPath( String iconPath )
    {
        this.iconPath = iconPath;
    }

}
