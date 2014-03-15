/*******************************************************************************
 * Copyright (c) 2013 CohesionForce Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CohesionForce Inc - initial API and implementation
 *******************************************************************************/
package com.cohesionforce.search;

import java.io.IOException;
import java.util.Collections;

import org.apache.lucene.search.Query;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceIndex extends EMFIndex implements
		IResourceChangeListener, IResourceDeltaVisitor {

	private final Logger logger = LoggerFactory.getLogger(WorkspaceIndex.class);
	private IWorkspaceRoot root;

	private static WorkspaceIndex index = null;

	public static synchronized WorkspaceIndex getInstance() {
		if (index == null) {
			index = new WorkspaceIndex();
		}
		return index;
	}

	private WorkspaceIndex() {
		root = ResourcesPlugin.getWorkspace().getRoot();
		root.getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.POST_CHANGE);
		IPath path = root.getLocation();
		String directory = path.toString().concat("/.index");
		try {
			initialize(directory);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public boolean isIndexed(EObject object) throws IllegalArgumentException,
			IOException {
		boolean rvalue = false;
		Query query = this.findDocument(object);
		if (query != null)
			rvalue = true;
		return rvalue;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent evt) {
		try {
			evt.getDelta().accept(this);
		} catch (CoreException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		EObject object = null;
		if (!resource.getName().startsWith(".")
				&& (resource.getName().endsWith(".kms") || (resource.getName()
						.endsWith("ed")))) {
			URI uri = URI.createFileURI(resource.getLocationURI().getPath());

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				try {
					ResourceSet resourceSet = new ResourceSetImpl();
					Resource xmiResource;

					xmiResource = (XMIResource) resourceSet.getResource(uri,
							true);
					xmiResource.load(Collections.EMPTY_MAP);

					object = xmiResource.getContents().get(0);
					indexContents(object);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				break;
			case IResourceDelta.REMOVED:
				try {
					deleteDocument(uri);
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage());
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
				break;
			default:
				break;
			}
		}
		return true; // visit the children
	}
}
