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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EMF Index is a class that uses EMF Annotations to determine how an instance
 * of an EMF model should be indexed using Apache Lucene.
 * 
 */
public class EMFIndex {

	private final Logger logger = LoggerFactory.getLogger(EMFIndex.class);
	private StandardAnalyzer analyzer;
	private IndexWriter writer;
	private FSDirectory fsDir;
	protected boolean holdCommits = false;
	
	protected Version version = Version.LUCENE_44;

	public final int MAX_SEARCH_RESULT = 100000;
	
	public void setHoldCommits(boolean value) throws IOException {
		holdCommits = value;
		if(!holdCommits && writer != null)
		{
			writer.commit();
		}
	}
	
	/**
	 * Initialize the search using the directory. The directory will be created
	 * if it does not exist.
	 * 
	 * @param directory
	 *            - the directory to use for the index
	 * @throws IOException
	 *             for problems creating the directory or the index
	 */
	public void initialize(String directory) throws IOException {

		boolean isNew = false;
		File dir = new File(directory);
		if (!dir.exists()) {
			boolean created = dir.mkdir();
			if(!created)
			{
				logger.error("Could not create directory {}", dir);
				return;
			}
			isNew = true;
		}

		fsDir = FSDirectory.open(dir);

		analyzer = new StandardAnalyzer(version);
		IndexWriterConfig config = new IndexWriterConfig(version, analyzer);
		writer = new IndexWriter(fsDir, config);

		// If this is a new directory, we will need to create an empty commit to
		// initialize the index
		if (isNew) {
			writer.commit();
		}
	}

	/**
	 * Searches the index for an attribute with a matching value.
	 * 
	 * @param feature
	 *            - the attribute to match when searching
	 * @param value
	 *            - the value to match when searching
	 * @return a list of search results
	 * @throws IllegalArgumentException
	 *             if any parameters are null, or if the attribute is not marked
	 *             with the search annotation
	 * @throws IOException
	 *             if there are issues reading the index
	 */
	public List<SearchResult> search(EStructuralFeature feature, String value)
			throws IllegalArgumentException, IOException {

		if (feature == null) {
			throw new IllegalArgumentException("Attribute cannot be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}

		List<SearchResult> rvalue = new ArrayList<SearchResult>();
		boolean tokenize = false;
		EAnnotation annotation = feature.getEAnnotation(EMFIndexUtil.SOURCE);
		if (annotation != null) {
			if (annotation.getDetails().containsKey(EMFIndexUtil.TOKENIZE_KEY)) {
				tokenize = true;
			}
		} else {
			// Bail out early if this feature should not be indexed
			throw new IllegalArgumentException(
					"Attribute is not annotated to be indexed");
		}

		String key = EMFIndexUtil.getKey(feature);
		DirectoryReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		try {

			Query query = null;

			if (tokenize) {
				QueryParser parser = new QueryParser(version, key, analyzer);
				query = parser.parse(value);
			} else {
				Term term = new Term(key, value);
				query = new TermQuery(term);
			}

			ScoreDoc[] hits = searcher.search(query, null, MAX_SEARCH_RESULT).scoreDocs;

			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = searcher.doc(hits[i].doc);
				SearchResult result = new SearchResult(hitDoc);
				rvalue.add(result);
				logger.debug(hitDoc.toString());
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
		} finally {
			reader.close();
		}
		return rvalue;
	}

	/**
	 * Searches the index for an attribute with a matching value and a matching
	 * eclass
	 * 
	 * @param eclass
	 *            - the EClass to match when searching
	 * @param attr
	 *            - the EAttribute to match when searching
	 * @param value
	 *            - the value to match when searching
	 * @return a list of search results
	 * @throws IllegalArgumentException
	 *             if any parameters are null, or if the attribute is not marked
	 *             with the search annotation
	 * @throws IOException
	 *             if there are issues reading the index
	 */
	public List<SearchResult> search(EClass eclass, EAttribute attr,
			String value) throws IllegalArgumentException, IOException {

		if (eclass == null) {
			throw new IllegalArgumentException("EClass cannot be null");
		}
		if (attr == null) {
			throw new IllegalArgumentException("Attribute cannot be null");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}

		EAnnotation annotation = eclass.getEAnnotation(EMFIndexUtil.SOURCE);
		if (annotation == null) {
			// Bail out early if this feature should not be indexed
			throw new IllegalArgumentException(
					"EClass is not annotated to be indexed");
		}
		List<SearchResult> rvalue = new ArrayList<SearchResult>();
		boolean tokenize = false;
		annotation = attr.getEAnnotation(EMFIndexUtil.SOURCE);
		if (annotation != null) {
			if (annotation.getDetails().containsKey(EMFIndexUtil.TOKENIZE_KEY)) {
				tokenize = true;
			}
		} else {
			// Bail out early if this feature should not be indexed
			throw new IllegalArgumentException(
					"Attribute is not annotated to be indexed");
		}

		String key = EMFIndexUtil.getKey(attr);

		DirectoryReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		try {

			BooleanQuery bquery = new BooleanQuery();

			TermQuery classQuery = new TermQuery(new Term(
					EMFIndexUtil.ETYPE_KEY, eclass.getName()));
			bquery.add(classQuery, Occur.MUST);

			Query query = null;
			if (tokenize) {
				QueryParser parser = new QueryParser(version, key, analyzer);
				query = parser.parse(value);
			} else {
				Term term = new Term(key, value);
				query = new TermQuery(term);
			}
			bquery.add(query, Occur.MUST);

			ScoreDoc[] hits = searcher.search(bquery, null, MAX_SEARCH_RESULT).scoreDocs;
			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = searcher.doc(hits[i].doc);
				SearchResult result = new SearchResult(hitDoc);
				rvalue.add(result);
				logger.debug(hitDoc.toString());
			}
		} catch (ParseException e) {
			logger.error(e.getMessage());
		} finally {
			reader.close();
		}
		return rvalue;
	}

	/**
	 * Searches the index for a matching eclass
	 * 
	 * @param eclass
	 *            - the EClass to match when searching
	 * @return a list of search results
	 * @throws IllegalArgumentException
	 *             if the eclass is null or is not annotated for indexing
	 * @throws IOException
	 *             if there are issues reading the index
	 */
	public List<SearchResult> search(EClass eclass)
			throws IllegalArgumentException, IOException {

		if (eclass == null) {
			throw new IllegalArgumentException("EClass cannot be null");
		}
		EAnnotation annotation = eclass.getEAnnotation(EMFIndexUtil.SOURCE);
		if (annotation == null) {
			throw new IllegalArgumentException(
					"EClass is not annotated for indexing");
		}
		List<SearchResult> rvalue = new ArrayList<SearchResult>();

		DirectoryReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		try {
			TermQuery classQuery = new TermQuery(new Term(
					EMFIndexUtil.ETYPE_KEY, eclass.getName()));

			ScoreDoc[] hits = searcher.search(classQuery, null, MAX_SEARCH_RESULT).scoreDocs;
			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = searcher.doc(hits[i].doc);
				SearchResult result = new SearchResult(hitDoc);
				rvalue.add(result);
				logger.debug(hitDoc.toString());
			}
		} finally {
			reader.close();
		}
		return rvalue;
	}

	/**
	 * Clears all documents from the index
	 */
	public void clearIndex() {
		logger.debug("Clearing the lucene index for {}", fsDir.getDirectory());
		try {
			writer.deleteAll();
			writer.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the number of documents contained in the index
	 */
	public int getDocumentCount() {
		return writer.numDocs();
	}

	/**
	 * Iterates through the objects children and creates an index for any object
	 * marked with the Index annotation.
	 */
	public void indexContents(EObject obj) {
		logger.debug("Indexing contents of {}", obj.eClass().getName());
		EAnnotation annotation = obj.eClass().getEAnnotation(
				EMFIndexUtil.SOURCE);
		if (annotation != null
				&& annotation.getDetails().containsKey(EMFIndexUtil.INDEX_KEY)) {
			try {
				createDocument(obj);
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		TreeIterator<EObject> itr = obj.eAllContents();
		while (itr.hasNext()) {
			EObject eobject = itr.next();
			annotation = eobject.eClass().getEAnnotation(EMFIndexUtil.SOURCE);
			if (annotation != null
					&& annotation.getDetails().containsKey(
							EMFIndexUtil.INDEX_KEY)) {
				try {
					createDocument(eobject);
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage());
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Adds the feature to the documents index
	 */
	protected void indexFeature(Document doc, EStructuralFeature feature,
			EObject object) {

		boolean recurse = false;
		boolean tokenize = false;
		boolean index = false;

		EAnnotation annotation = feature.getEAnnotation(EMFIndexUtil.SOURCE);
		if (annotation != null) {
			if (annotation.getDetails().containsKey(EMFIndexUtil.RECURSE_KEY)) {
				recurse = true;
			}
			if (annotation.getDetails().containsKey(EMFIndexUtil.TOKENIZE_KEY)) {
				tokenize = true;
			}
			if (annotation.getDetails().containsKey(EMFIndexUtil.INDEX_KEY)) {
				index = true;
			}
		} else {
			// Bail out early if this feature should not be indexed
			return;
		}

		// Key to be used if this feature is indexed
		String key = EMFIndexUtil.getKey(feature);

		if (feature.getUpperBound() == 1 && feature instanceof EReference) {
			EObject reference = (EObject) object.eGet(feature);

			if (reference != null) {

				// Index the reference if annotated
				if (index) {
					String value = reference.eResource().getURIFragment(
							reference);
					logger.debug("Adding reference: {}, value: {}", key, value);
					doc.add(new StringField(key, value, Store.YES));
				}

				// Recurse the reference if annotated
				if (recurse) {
					for (EStructuralFeature refFeature : reference.eClass()
							.getEAllStructuralFeatures()) {
						indexFeature(doc, refFeature, reference);
					}
				}
			}

		} else if (feature.getUpperBound() != 1
				&& feature instanceof EReference) {
			@SuppressWarnings("unchecked")
			Collection<EObject> collection = (Collection<EObject>) object
					.eGet(feature);
			for (EObject reference : collection) {
				// Index the reference if annotated
				if (index) {
					String value = reference.eResource()
							.getURIFragment(feature);
					logger.debug("Adding reference: {}, value: {}", key, value);
					doc.add(new StringField(key, value, Store.YES));
				}

				// Recurse the reference if annotated
				if (recurse) {
					for (EStructuralFeature refFeature : reference.eClass()
							.getEAllStructuralFeatures()) {
						indexFeature(doc, refFeature, reference);
					}
				}
			}
		} else if (feature.getUpperBound() == 1
				&& feature instanceof EAttribute) {
			Object value = object.eGet(feature);
			Class<?> instanceClass = feature.getEType().getInstanceClass();
			logger.debug("Adding attribute: {}, value: {}", key, value);
			if (feature.getEType().getInstanceClass().equals(double.class)) {
				doc.add(new DoubleField(key, ((Double) value).doubleValue(),
						Store.YES));
			} else if (instanceClass.equals(float.class)) {
				doc.add(new FloatField(key, ((Float) value).floatValue(),
						Store.YES));
			} else if (instanceClass.equals(int.class)) {
				doc.add(new IntField(key, ((Integer) value).intValue(),
						Store.YES));
			} else if (instanceClass.equals(long.class)) {
				doc.add(new LongField(key, ((Long) value).longValue(),
						Store.YES));
			} else if (instanceClass.equals(String.class)) {
				if (tokenize) {
					doc.add(new TextField(key, (String) value, Store.YES));
				} else {
					doc.add(new StringField(key, (String) value, Store.YES));
				}
			} else if (value != null) {
				if (tokenize) {
					doc.add(new TextField(key, value.toString(), Store.YES));
				} else {
					doc.add(new StringField(key, value.toString(), Store.YES));
				}
			}
		} else if (feature.getUpperBound() != 1
				&& feature instanceof EAttribute) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) object
					.eGet(feature);
			Class<?> instanceClass = feature.getEType().getInstanceClass();
			for (Object value : collection) {
				logger.debug("Adding attribute: {}, value: {}", key, value);
				if (feature.getEType().getInstanceClass().equals(double.class)) {
					doc.add(new DoubleField(key,
							((Double) value).doubleValue(), Store.YES));
				} else if (instanceClass.equals(float.class)) {
					doc.add(new FloatField(key, ((Float) value).floatValue(),
							Store.YES));
				} else if (instanceClass.equals(int.class)) {
					doc.add(new IntField(key, ((Integer) value).intValue(),
							Store.YES));
				} else if (instanceClass.equals(long.class)) {
					doc.add(new LongField(key, ((Long) value).longValue(),
							Store.YES));
				} else if (instanceClass.equals(String.class)) {
					if (tokenize) {
						doc.add(new TextField(key, (String) value, Store.YES));
					} else {
						doc.add(new StringField(key, (String) value, Store.YES));
					}
				} else if (value != null) {
					if (tokenize) {
						doc.add(new TextField(key, value.toString(), Store.YES));
					} else {
						doc.add(new StringField(key, value.toString(),
								Store.YES));
					}
				}
			}
		}
	}

	/**
	 * Finds a document matching the object and returns the query used
	 * 
	 * @param object
	 *            to search for in the index
	 * @return the query that returns the document
	 * @throws IllegalArgumentException
	 *             if the object is null
	 * @throws IOException
	 *             if there are problems searching the index
	 */
	protected Query findDocument(EObject object)
			throws IllegalArgumentException, IOException {

		if (object == null) {
			throw new IllegalArgumentException("EObject cannot be null");
		}

		Query rvalue = null;

		DirectoryReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		try {

			BooleanQuery bquery = new BooleanQuery();

			TermQuery classQuery = new TermQuery(new Term(
					EMFIndexUtil.ETYPE_KEY, object.eClass().getName()));
			bquery.add(classQuery, Occur.MUST);

			TermQuery uriQuery = new TermQuery(new Term(
					EMFIndexUtil.DOCUMENT_URI_KEY, object.eResource().getURI()
							.toString()));
			bquery.add(uriQuery, Occur.MUST);

			TermQuery fragmentQuery = new TermQuery(new Term(
					EMFIndexUtil.FRAGMENT_URI_KEY, object.eResource()
							.getURIFragment(object)));
			bquery.add(fragmentQuery, Occur.MUST);

			ScoreDoc[] hits = searcher.search(bquery, null, 1).scoreDocs;
			// Iterate through the results:
			if (hits.length > 0) {
				rvalue = bquery;
			}
		} finally {
			reader.close();
		}
		return rvalue;
	}

	/**
	 * Creates a new document and adds the features of the object to the
	 * document's indices.
	 * 
	 * @param obj
	 *            - the object to add to the index
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void createDocument(EObject obj) throws IllegalArgumentException,
			IOException {

		if (obj == null) {
			throw new IllegalArgumentException("Object cannot be null");
		}
		EAnnotation annotation = obj.eClass().getEAnnotation(
				EMFIndexUtil.SOURCE);
		if (annotation == null) {
			return;
		}

		if (obj.eResource() != null && obj.eResource().getURI() != null) {

			Query query = findDocument(obj);
			if (query != null) {
				logger.debug("Deleting existing index for {}:{}", obj
						.eResource().getURI(),
						obj.eResource().getURIFragment(obj));
				writer.deleteDocuments(query);
			}

			logger.debug("Creating lucene index for {}:{}", obj.eResource()
					.getURI(), obj.eResource().getURIFragment(obj));

			Document doc = new Document();
			doc.add(new StringField(EMFIndexUtil.DOCUMENT_URI_KEY, obj
					.eResource().getURI().toString(), Store.YES));
			doc.add(new StringField(EMFIndexUtil.FRAGMENT_URI_KEY, obj
					.eResource().getURIFragment(obj), Store.YES));
			doc.add(new StringField(EMFIndexUtil.ETYPE_KEY, obj.eClass()
					.getName(), Store.YES));

			for (EStructuralFeature feature : obj.eClass()
					.getEAllStructuralFeatures()) {
				indexFeature(doc, feature, obj);
			}

			try {
				writer.addDocument(doc);
				if(!holdCommits) {
					writer.commit();
				}
			} catch (CorruptIndexException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * Deletes a document matching the EObject
	 * 
	 * @param obj
	 * @throws IllegalArgumentException
	 *             if EObject is null or the EObject is not contained in a
	 *             resource
	 * @throws IOException
	 *             if there are issues saving the index
	 */
	public void deleteDocument(EObject obj) throws IllegalArgumentException,
			IOException {
		if (obj == null) {
			throw new IllegalArgumentException("EObject cannot be null");
		}
		if (obj.eResource() == null) {
			throw new IllegalArgumentException(
					"EObject must be contained in a Resource");
		}
		
		Query query = findDocument(obj);
		if (query != null) {
			logger.debug("Deleting existing index for {}:{}", obj.eResource()
					.getURI(), obj.eResource().getURIFragment(obj));
			writer.deleteDocuments(query);
			if(!holdCommits) {
				writer.commit();
			}
		}
		
		DirectoryReader reader = DirectoryReader.open(fsDir);

		ArrayList<String> names = new ArrayList<String>();
		for(AtomicReaderContext context: reader.leaves())
		{
			for(FieldInfo fi: context.reader().getFieldInfos())
			{
				if(!names.contains(fi.name))
				{
					names.add(fi.name);
				}

			}
		}
		if(names.size() > 0)
		{
			MultiFieldQueryParser parser = new MultiFieldQueryParser(version, names.toArray(new String[] {}), analyzer);
			try {
				query = parser.parse(obj.eResource().getURIFragment(obj));
				IndexSearcher searcher = new IndexSearcher(reader);
				
				ScoreDoc[] hits = searcher.search(query, null, MAX_SEARCH_RESULT).scoreDocs;
				for(ScoreDoc hit: hits)
				{
					Document hitDoc = searcher.doc(hit.doc);
					logger.debug("Hanging reference in: {}", hitDoc.getField(EMFIndexUtil.DOCUMENT_URI_KEY).stringValue());
				}
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
			
		}
	}

	/**
	 * Deletes a document matching the URI
	 * 
	 * @param uri - the URI of the object reference to delete
	 * @throws IllegalArgumentException
	 *             if the uri is null
	 * @throws IOException
	 *             if there are issues saving the index
	 */
	public void deleteDocument(URI uri) throws IllegalArgumentException,
			IOException {
		if (uri == null) {
			throw new IllegalArgumentException("URI cannot be null");
		}
		
		DirectoryReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		try {

			TermQuery uriQuery = new TermQuery(new Term(
					EMFIndexUtil.DOCUMENT_URI_KEY, uri.toString()));


			ScoreDoc[] hits = searcher.search(uriQuery, null, 1).scoreDocs;
			// Iterate through the results:
			if (hits.length > 0) {
				writer.deleteDocuments(uriQuery);
				if(!holdCommits) {
					writer.commit();
				}
			}
		} finally {
			reader.close();
		}
	}
}
