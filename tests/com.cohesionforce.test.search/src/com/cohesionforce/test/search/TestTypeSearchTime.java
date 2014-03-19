package com.cohesionforce.test.search;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.examples.extlibrary.Book;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Writer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.cohesionforce.search.EMFIndex;
import com.cohesionforce.search.SearchResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTypeSearchTime {

	private EMFIndex index;

	private void searchLucene(int number) throws IOException {
		System.out.println();
		System.out.println("Testing Type Search Times for Lucene with index"
				+ number);
		URL file = Activator.getContext().getBundle()
				.getEntry("index" + number);
		String url = null;
		url = FileLocator.toFileURL(file).getFile();
		index = new EMFIndex();
		index.initialize(url);

		// Search for books
		long start = System.currentTimeMillis();
		List<SearchResult> results = index.search(EXTLibraryPackage.Literals.BOOK);
		long stop = System.currentTimeMillis();
		float time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + results.size() + " Books in " + time
				+ " seconds");

		// Search for writers
		start = System.currentTimeMillis();
		results = index.search(EXTLibraryPackage.Literals.WRITER);
		stop = System.currentTimeMillis();
		time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + results.size() + " Writers in " + time
				+ " seconds");
	}

	private void searchFiles(int number) throws IOException {
		System.out.println();
		System.out.println("Testing Type Search Times for files with output"
				+ number);
		URL dir = Activator.getContext().getBundle()
				.getEntry("output" + number);

		File directory = new File(FileLocator.toFileURL(dir).getFile());

		EXTLibraryPackage.eINSTANCE.eClass();
		int count = 0;
		long start = System.currentTimeMillis();
		for (final File file : directory.listFiles()) {

			if(file.getName().equals(".gitignore"))
				continue;
			
			ResourceSet resourceSet = new ResourceSetImpl();

			URI uri = URI.createFileURI(file.toString());
			try {
				XMLResource xmiResource = (XMLResource) resourceSet
						.getResource(uri, true);

				EObject object = xmiResource.getContents().get(0);

				assertNotNull(object);
				if (object instanceof Book) {
					count++;
				}
			} catch (Exception e) {
			}
		}
		long stop = System.currentTimeMillis();
		float time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + count + " Books in " + time + " seconds");

		count = 0;
		start = System.currentTimeMillis();
		for (final File file : directory.listFiles()) {

			if(file.getName().equals(".gitignore"))
				continue;
			
			ResourceSet resourceSet = new ResourceSetImpl();

			URI uri = URI.createFileURI(file.toString());
			try {
				XMLResource xmiResource = (XMLResource) resourceSet
						.getResource(uri, true);

				EObject object = xmiResource.getContents().get(0);

				assertNotNull(object);
				if (object instanceof Writer) {
					count++;
				}
			} catch (Exception e) {
			}
		}
		stop = System.currentTimeMillis();
		time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + count + " Writers in " + time
				+ " seconds");
	}

	@Test
	public void test0Warmup() throws IOException {
		System.out.println();
		System.out.println("Warmup test");
		for(int i = 0; i < 100000; i++)
			System.out.print(".");
	}

	@Test
	public void test1LuceneSearchTimes() throws IOException {
		searchLucene(1);
	}

	@Test
	public void test1FileSearchTimes() throws IOException {
		searchFiles(1);
	}

	@Test
	public void test2LuceneSearchTimes() throws IOException {
		searchLucene(2);
	}

	@Test
	public void test2FileSearchTimes() throws IOException {
		searchFiles(2);
	}

	@Test
	public void test3LuceneSearchTimes() throws IOException {
		searchLucene(3);
	}

	@Test
	public void test3FileSearchTimes() throws IOException {
		searchFiles(3);
	}

	@Test
	public void test4LuceneSearchTimes() throws IOException {
		searchLucene(4);
	}

	@Test
	public void test4FileSearchTimes() throws IOException {
		searchFiles(4);
	}

	@Test
	public void test5LuceneSearchTimes() throws IOException {
		searchLucene(5);
	}

	@Test
	public void test5FileSearchTimes() throws IOException {
		searchFiles(5);
	}

	@Test
	public void test6LuceneSearchTimes() throws IOException {
		searchLucene(6);
	}

	@Test
	public void test6FileSearchTimes() throws IOException {
		searchFiles(6);
	}

	@Test
	public void test7LuceneSearchTimes() throws IOException {
		searchLucene(7);
	}

	@Test
	public void test7FileSearchTimes() throws IOException {
		searchFiles(7);
	}

	@Test
	public void test8LuceneSearchTimes() throws IOException {
		searchLucene(8);
	}

	@Test
	public void test8FileSearchTimes() throws IOException {
		searchFiles(8);
	}

	@Test
	public void test9LuceneSearchTimes() throws IOException {
		searchLucene(9);
	}

	@Test
	public void test9FileSearchTimes() throws IOException {
		searchFiles(9);
	}

	@Test
	public void testa1LuceneSearchTimes() throws IOException {
		searchLucene(10);
	}

	@Test
	public void testa1FileSearchTimes() throws IOException {
		searchFiles(10);
	}

}