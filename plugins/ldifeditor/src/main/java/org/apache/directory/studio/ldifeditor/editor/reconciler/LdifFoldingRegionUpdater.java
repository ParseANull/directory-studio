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

package org.apache.directory.studio.ldifeditor.editor.reconciler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.ILdifEditor;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.container.LdifCommentContainer;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;
import org.apache.directory.studio.ldifparser.model.lines.LdifNonEmptyLineBase;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


// ── CLASS: LdifFoldingRegionUpdater — REBEL BASE HATCH CONTROLLER ─────────────
// The Rebel base has collapsible blast doors over each section of the
// corridor: comments, records, and wrapped lines each have their own door.
// When the base-wide folding preference changes the hatch controller goes
// through every corridor and opens or closes each door accordingly.
// LdifFoldingRegionUpdater computes the set of foldable regions from the parsed
// LDIF model and synchronises the ProjectionAnnotationModel whenever a folding
// preference changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Keeps the Eclipse code-folding annotation model in sync with the parsed LDIF
 * model and the folding preferences.
 * Listens for preference changes and calls {@link #updateFoldingRegions()} to
 * recompute the {@link ProjectionAnnotation}s for every container and wrapped
 * line, diffing against the existing model to minimise churn.
 * Think of this as the Rebel base hatch controller: it opens and closes
 * collapsible sections whenever the operator changes the folding settings.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifFoldingRegionUpdater implements IPropertyChangeListener
{

    /** The LDIF editor whose projection annotation model we manage. */
    private ILdifEditor editor;


    // ── CONSTRUCT AND SUBSCRIBE ───────────────────────────────────────────────
    // The hatch controller registers its preference listener at startup so
    // it reacts to folding preference changes immediately.
    /**
     * Creates a new folding region updater for {@code editor} and subscribes
     * to the LDIF preference store so that folding changes are applied live.
     *
     * @param editor  the LDIF editor whose folding to manage
     */
    public LdifFoldingRegionUpdater( ILdifEditor editor )
    {
        this.editor = editor;

        LdifEditorActivator.getDefault().getPreferenceStore().addPropertyChangeListener( this );
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    /**
     * Removes the preference-change listener.
     */
    public void dispose()
    {
        LdifEditorActivator.getDefault().getPreferenceStore().removePropertyChangeListener( this );
    }


    // ── REACT TO PREFERENCE CHANGES ───────────────────────────────────────────
    // When any of the four folding preferences changes the hatch controller
    // recomputes all regions immediately.
    /**
     * {@inheritDoc}
     *
     * <p>Triggers {@link #updateFoldingRegions()} when any of the four LDIF
     * folding preferences changes.</p>
     */
    public void propertyChange( PropertyChangeEvent event )
    {
        if ( LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE.equals( event.getProperty() )
            || LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS.equals( event.getProperty() )
            || LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS.equals( event.getProperty() )
            || LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES.equals( event.getProperty() ) )
        {
            this.updateFoldingRegions();
        }
    }


    // ── RECOMPUTE AND SYNC FOLDING REGIONS ───────────────────────────────────
    // The hatch controller surveys every corridor in the base, computes the
    // desired door state, diffs against what is currently open or closed, and
    // issues the minimum set of open/close commands.
    /**
     * Recomputes the full set of foldable regions from the LDIF model and
     * synchronises the {@link ProjectionAnnotationModel}: adds new annotations,
     * removes stale ones.
     * No-ops if the viewer or annotation model is unavailable.
     */
    public void updateFoldingRegions()
    {

        ISourceViewer viewer = ( ISourceViewer ) editor.getAdapter( ISourceViewer.class );
        if ( viewer == null )
            return;

        IDocument document = viewer.getDocument();

        try
        {
            ProjectionAnnotationModel projectionAnnotationModel = ( ProjectionAnnotationModel ) editor
                .getAdapter( ProjectionAnnotationModel.class );
            if ( projectionAnnotationModel == null )
                return;

            // create folding regions of current LDIF model; mark comments
            // and folded lines as collapsed
            Map<Position, ProjectionAnnotation> positionToAnnotationMap = createFoldingRegions( editor.getLdifModel(), document );

            // compare with current annotation model (--> toAdd, toDelete)
            List<Annotation> annotationsToDeleteList = new ArrayList<Annotation>();
            Map<ProjectionAnnotation, Position> annotationsToAddMap = new HashMap<ProjectionAnnotation, Position>();
            this.computeDifferences( projectionAnnotationModel, positionToAnnotationMap, annotationsToDeleteList,
                annotationsToAddMap );
            Annotation[] annotationsToDelete = ( Annotation[] ) annotationsToDeleteList
                .toArray( new Annotation[annotationsToDeleteList.size()] );

            // update annotation model
            if ( !annotationsToDeleteList.isEmpty() || !annotationsToAddMap.isEmpty() )
            {
                projectionAnnotationModel.modifyAnnotations( annotationsToDelete, annotationsToAddMap,
                    new Annotation[0] );
            }

        }
        catch ( BadLocationException e )
        {
            e.printStackTrace();
        }
    }


    // ── DIFF AGAINST EXISTING MODEL ───────────────────────────────────────────
    // Any position already in the model that we computed is kept; the rest is
    // deleted.  New positions not yet in the model are added.
    /**
     * Computes the difference between the current annotation model and the
     * desired {@code positionToAnnotationMap}:
     * <ul>
     *   <li>annotations whose position is in the map are retained (removed from
     *       the map to avoid re-adding them);</li>
     *   <li>annotations whose position is not in the map are scheduled for
     *       deletion;</li>
     *   <li>remaining map entries are scheduled for addition.</li>
     * </ul>
     *
     * @param model                    the current projection annotation model
     * @param positionToAnnotationMap  desired position-to-annotation mapping
     *                                 (modified in place)
     * @param annotationsToDeleteList  output: annotations to remove
     * @param annotationsToAddMap      output: annotations to add
     */
    private void computeDifferences( ProjectionAnnotationModel model, Map<Position, ProjectionAnnotation> positionToAnnotationMap,
        List<Annotation> annotationsToDeleteList, Map<ProjectionAnnotation, Position> annotationsToAddMap )
    {
        for ( Iterator<Annotation> iter = model.getAnnotationIterator(); iter.hasNext(); )
        {
            Annotation annotation = iter.next();

            if ( annotation instanceof ProjectionAnnotation )
            {
                Position position = model.getPosition( ( Annotation ) annotation );

                if ( positionToAnnotationMap.containsKey( position ) )
                {
                    positionToAnnotationMap.remove( position );
                }
                else
                {
                    annotationsToDeleteList.add( annotation );
                }
            }
        }

        for ( Map.Entry<Position, ProjectionAnnotation> entry : positionToAnnotationMap.entrySet() )
        {
            annotationsToAddMap.put( entry.getValue(), entry.getKey() );
        }
    }


    // ── BUILD THE DESIRED FOLDING REGION MAP ──────────────────────────────────
    // The hatch controller walks every container and every wrapped line to
    // compute which doors should exist and whether they should start collapsed.
    /**
     * Creates all folding regions for the given LDIF model.
     * {@link LdifCommentContainer}s and wrapped lines are marked as collapsed
     * according to the current preference settings.
     *
     * @param model     the parsed LDIF model
     * @param document  the document (used to convert offsets to line numbers)
     * @return          a map from {@link Position} to {@link ProjectionAnnotation}
     * @throws BadLocationException if a computed offset is invalid
     */
    private Map<Position, ProjectionAnnotation> createFoldingRegions( LdifFile model, IDocument document ) throws BadLocationException
    {
        Map<Position, ProjectionAnnotation> positionToAnnotationMap = new HashMap<Position, ProjectionAnnotation>();
        List<LdifContainer> containers = model.getContainers();

        boolean ENABLE_FOLDING = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_ENABLE );
        boolean FOLD_COMMENTS = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDCOMMENTS );
        boolean FOLD_RECORDS = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDRECORDS );
        boolean FOLD_WRAPPEDLINES = LdifEditorActivator.getDefault().getPreferenceStore().getBoolean(
            LdifEditorConstants.PREFERENCE_LDIFEDITOR_FOLDING_INITIALLYFOLDWRAPPEDLINES );

        if ( ENABLE_FOLDING )
        {
            for ( LdifContainer ldifContainer : containers )
            {
                int containerStartLine = document.getLineOfOffset( ldifContainer.getOffset() );
                int containerEndLine = -1;
                LdifPart[] parts = ldifContainer.getParts();

                for ( int j = parts.length - 1; j >= 0; j-- )
                {
                    if ( containerEndLine == -1
                        && ( !( parts[j] instanceof LdifSepLine ) || ( ldifContainer instanceof LdifCommentContainer && j < parts.length - 1 ) ) )
                    {
                        containerEndLine = document.getLineOfOffset( parts[j].getOffset() + parts[j].getLength() - 1 );
                        // break;
                    }

                    if ( parts[j] instanceof LdifNonEmptyLineBase )
                    {
                        LdifNonEmptyLineBase line = ( LdifNonEmptyLineBase ) parts[j];

                        if ( line.isFolded() )
                        {
                            Position position = new Position( line.getOffset(), line.getLength() );
                            // ProjectionAnnotation annotation = new
                            // ProjectionAnnotation(true);
                            ProjectionAnnotation annotation = new ProjectionAnnotation( FOLD_WRAPPEDLINES );
                            positionToAnnotationMap.put( position, annotation );
                        }
                    }
                }

                if ( containerStartLine < containerEndLine )
                {
                    int start = document.getLineOffset( containerStartLine );
                    int end = document.getLineOffset( containerEndLine ) + document.getLineLength( containerEndLine );
                    Position position = new Position( start, end - start );
                    ProjectionAnnotation annotation = new ProjectionAnnotation( FOLD_RECORDS
                        || ( FOLD_COMMENTS && ldifContainer instanceof LdifCommentContainer ) );
                    positionToAnnotationMap.put( position, annotation );
                }
            }
        }

        return positionToAnnotationMap;
    }

}
