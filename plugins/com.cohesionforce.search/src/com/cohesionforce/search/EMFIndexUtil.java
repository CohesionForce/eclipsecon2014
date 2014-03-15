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

import org.eclipse.emf.ecore.EStructuralFeature;

public class EMFIndexUtil {

	public final static String DOCUMENT_URI_KEY = "doc_uri";
	public final static String FRAGMENT_URI_KEY = "fragment_uri";
	public final static String RECURSE_KEY = "recurse";
	public final static String TOKENIZE_KEY = "tokenize";
	public final static String INDEX_KEY = "index";
	public final static String ETYPE_KEY = "etype";
	public final static String SOURCE = "http://www.cohesionforce.com/search";

	protected final static StringBuilder builder = new StringBuilder();
	
	public static synchronized String getKey(EStructuralFeature feature)
	{
		builder.setLength(0);
		builder.append(feature.getContainerClass()
				.getName());
		builder.append("-");
		builder.append(feature.getName());
		return builder.toString();
	}
}
