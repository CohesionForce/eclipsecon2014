package com.cohesionforce.test.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.examples.extlibrary.Book;
import org.eclipse.emf.examples.extlibrary.EXTLibraryFactory;
import org.eclipse.emf.examples.extlibrary.Library;
import org.eclipse.emf.examples.extlibrary.Writer;
import org.junit.Test;
import org.w3._2005.atom.AtomFactory;
import org.w3._2005.atom.DocumentRoot;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;

public class TestParseCommand {

	private static Hashtable<String, Library> libraries = new Hashtable<String, Library>();
	private static Hashtable<String, Writer> writers = new Hashtable<String, Writer>();

	private String outputDir = "output1/";

	@Test
	public void testParseRSStoLibrary() throws IOException {
		System.out.println("1-Parsing RSS Atom into Library xmi");
		parseDirectory();
	}

	@Test
	public void testParse2() throws IOException {
		System.out.println("2-Parsing RSS Atom into Library xmi");
		outputDir = "output2/";
		for (int i = 0; i < 2; i++)
			parseDirectory();
	}

	@Test
	public void testParse3() throws IOException {
		System.out.println("3-Parsing RSS Atom into Library xmi");
		outputDir = "output3/";
		for (int i = 0; i < 3; i++)
			parseDirectory();
	}

	@Test
	public void testParse4() throws IOException {
		System.out.println("4-Parsing RSS Atom into Library xmi");
		outputDir = "output4/";
		for (int i = 0; i < 4; i++)
			parseDirectory();
	}

	@Test
	public void testParse5() throws IOException {
		System.out.println("5-Parsing RSS Atom into Library xmi");
		outputDir = "output5/";
		for (int i = 0; i < 5; i++)
			parseDirectory();
	}

	@Test
	public void testParse6() throws IOException {
		System.out.println("6-Parsing RSS Atom into Library xmi");
		outputDir = "output6/";
		for (int i = 0; i < 6; i++)
			parseDirectory();
	}

	@Test
	public void testParse7() throws IOException {
		System.out.println("7-Parsing RSS Atom into Library xmi");
		outputDir = "output7/";
		for (int i = 0; i < 7; i++)
			parseDirectory();
	}

	@Test
	public void testParse8() throws IOException {
		System.out.println("8-Parsing RSS Atom into Library xmi");
		outputDir = "output8/";
		for (int i = 0; i < 8; i++)
			parseDirectory();
	}
	@Test
	public void testParse9() throws IOException {
		System.out.println("9-Parsing RSS Atom into Library xmi");
		outputDir = "output9/";
		for (int i = 0; i < 9; i++)
			parseDirectory();
	}

	@Test
	public void testParse10() throws IOException {
		System.out.println("10-Parsing RSS Atom into Library xmi");
		outputDir = "output10/";
		for (int i = 0; i < 10; i++)
			parseDirectory();
	}

	private void parseDirectory() throws IOException {
		libraries.clear();
		writers.clear();
		URL dir = Activator.getContext().getBundle().getEntry("xml");

		File directory = new File(FileLocator.toFileURL(dir).getFile());

		AtomFactory.eINSTANCE.eClass();

		for (File file : directory.listFiles()) {

			ResourceSet resourceSet = new ResourceSetImpl();

			URI uri = URI.createFileURI(file.toString());
			XMLResource xmiResource = (XMLResource) resourceSet.getResource(
					uri, true);

			EObject object = xmiResource.getContents().get(0);
			assertTrue(object instanceof DocumentRoot);
			assertNotNull(object);
			DocumentRoot root = (DocumentRoot) object;

			FeedType feed = root.getFeed();
			for (EntryType entry : feed.getEntry()) {

				// Create a new book for the entry
				Book book = EXTLibraryFactory.eINSTANCE.createBook();
				book.setTitle(entry.getTitle().getValue());
				String str = entry.getContent().getValue();

				// Be sure to escape all of the html formatting in the
				// string
				str = StringEscapeUtils.unescapeHtml(str);

				// We only want books
				if (!str.matches(".*Format.Books.*")) {
					continue;
				}

				String[] pieces = str.split("<br/>");

				// First piece is the author
				if (pieces.length > 0) {
					String[] authors = pieces[0].split(".");
					if (authors.length == 0) {
						String authorName = pieces[0].trim();
						Writer writer = writers.get(authorName);
						if (writer == null) {
							writer = EXTLibraryFactory.eINSTANCE.createWriter();
							writer.setName(authorName);
							writers.put(authorName, writer);
							saveWriter(writer);
						}
						book.getAuthor().add(writer);
					} else {
						for (String author : authors) {
							String authorName = author.trim();
							Writer writer = writers.get(authorName);
							if (writer == null) {
								writer = EXTLibraryFactory.eINSTANCE
										.createWriter();
								writer.setName(authorName);
								writers.put(authorName, writer);
								saveWriter(writer);
							}
							book.getAuthor().add(writer);
						}
					}
				}

				// Find libraries
				for (String piece : pieces) {
					if (piece.startsWith("Locations:")) {
						String subString = piece.substring(10);
						String[] locations = subString.split(",");
						for (String location : locations) {
							String libraryName = location.trim();
							Library library = libraries.get(libraryName);
							if (library == null) {
								library = EXTLibraryFactory.eINSTANCE
										.createLibrary();
								library.setName(libraryName);
								libraries.put(libraryName, library);
							}
							library.getBooks().add(book);
						}
					}
				}

				// Save the book
				saveBook(book);
			}

			// Now save the libraries
			for (Library library : libraries.values()) {
				saveLibrary(library);
			}

		}
	}

	private void saveLibrary(Library library) {
		String filename = UUID.randomUUID().toString();
		URI uri = URI.createFileURI(outputDir + filename.concat(".xmi"));
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(library);
		try {
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveBook(Book book) {
		String filename = UUID.randomUUID().toString();
		URI uri = URI.createFileURI(outputDir + filename.concat(".xmi"));
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(book);
		try {
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveWriter(Writer writer) {
		String filename = UUID.randomUUID().toString();
		URI uri = URI.createFileURI(outputDir + filename.concat(".xmi"));
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(writer);
		try {
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
