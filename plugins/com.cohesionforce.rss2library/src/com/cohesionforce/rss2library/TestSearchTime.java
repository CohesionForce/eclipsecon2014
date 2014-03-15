package com.cohesionforce.rss2library;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.examples.extlibrary.Book;
import org.eclipse.emf.examples.extlibrary.EXTLibraryFactory;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.cohesionforce.search.EMFIndex;
import com.cohesionforce.search.SearchResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSearchTime {

	private static EMFIndex index;

	public void searchLucene(int number) throws IOException {
		System.out.println();
		System.out.println("Testing Title Search Times for Lucene with index"+number);

		try {
			URL file = Activator.getContext().getBundle().getEntry("index"+ number);
			String url = null;
			url = FileLocator.toFileURL(file).getFile();
			index = new EMFIndex();
			index.initialize(url);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Look for books with the term "leader"
		long start = System.currentTimeMillis();
		List<SearchResult> results = index.search(EXTLibraryPackage.Literals.BOOK,
				EXTLibraryPackage.Literals.BOOK__TITLE, "leader");
		long stop = System.currentTimeMillis();
		float time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + results.size() + " Books in " + time
				+ " seconds");
		for (final SearchResult result : results) {
			System.out.println(result
					.getValue(EXTLibraryPackage.Literals.BOOK__TITLE));
		}
	}

	public void searchFiles(int number) throws IOException {
		System.out.println();
		System.out.println("Testing Title Search Times for files with output"+number);
		ArrayList<String> titles = new ArrayList<String>();

		URL dir = Activator.getContext().getBundle().getEntry("output"+number);

		File directory = new File(FileLocator.toFileURL(dir).getFile());

		EXTLibraryFactory.eINSTANCE.eClass();
		int count = 0;
		long start = System.currentTimeMillis();
		for (final File file : directory.listFiles()) {

			ResourceSet resourceSet = new ResourceSetImpl();

			URI uri = URI.createFileURI(file.toString());
			try {
				XMLResource xmiResource = (XMLResource) resourceSet
						.getResource(uri, true);

				EObject object = xmiResource.getContents().get(0);

				assertNotNull(object);
				if (object instanceof Book) {
					Book book = (Book) object;
					if (book.getTitle().contains("leader")) {
						titles.add(book.getTitle());
						count++;
					}
				}
			} catch (Exception e) {
			}
		}
		long stop = System.currentTimeMillis();
		float time = ((float) (stop - start)) / 1000f;
		System.out.println("Found " + count + " Books in " + time + " seconds");
		for (final String title : titles) {
			System.out.println(title);
		}
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
