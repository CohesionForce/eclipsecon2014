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

import org.apache.lucene.document.Document;
import org.eclipse.emf.ecore.EStructuralFeature;

public class SearchResult {

	private String documentURI;

	private String fragmentURI;

	private String eclass;
	
	private Document document;

	public SearchResult(Document doc)
	{
		this.document = doc;
		documentURI = document.get(EMFIndexUtil.DOCUMENT_URI_KEY);
		fragmentURI = document.get(EMFIndexUtil.FRAGMENT_URI_KEY);
		eclass = document.get(EMFIndexUtil.ETYPE_KEY);
	}
	
	public String getDocumentURI() {
		return documentURI;
	}

	public String getFragmentURI() {
		return fragmentURI;
	}

	public String getEclass() {
		return eclass;
	}

	public String getValue(EStructuralFeature feature)
	{
		String key = EMFIndexUtil.getKey(feature);
		String rvalue = document.get(key);
		return rvalue;
	}
}
