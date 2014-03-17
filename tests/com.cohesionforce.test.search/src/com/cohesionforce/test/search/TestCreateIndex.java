package com.cohesionforce.test.search;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.examples.extlibrary.EXTLibraryFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.cohesionforce.search.EMFIndex;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCreateIndex {

	private EMFIndex index;

	@Test
	public void test1IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output1");
		indexFiles(1);
	}

	@Test
	public void test2IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output2");
		indexFiles(2);
	}

	@Test
	public void test3IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output3");
		indexFiles(3);
	}

	@Test
	public void test4IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output4");
		indexFiles(4);
	}

	@Test
	public void test5IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output5");
		indexFiles(5);
	}

	@Test
	public void test6IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output6");
		indexFiles(6);
	}

	@Test
	public void test7IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output7");
		indexFiles(7);
	}

	@Test
	public void test8IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output8");
		indexFiles(8);
	}

	@Test
	public void test9IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output9");
		indexFiles(9);
	}

	@Test
	public void testa1IndexOutput() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of all files in output10");
		indexFiles(10);
	}

	private void indexFiles(int number) throws IOException {
		URL fileURL = Activator.getContext().getBundle()
				.getEntry("index" + number);
		String url = null;
		url = FileLocator.toFileURL(fileURL).getFile();
		index = new EMFIndex();
		index.initialize(url);
		index.clearIndex();

		URL dir = Activator.getContext().getBundle()
				.getEntry("output" + number);
		index.clearIndex();
		index.setHoldCommits(true);
		File directory = new File(FileLocator.toFileURL(dir).getFile());

		EXTLibraryFactory.eINSTANCE.eClass();

		int count = 0;
		long start = System.currentTimeMillis();
		for (File file : directory.listFiles()) {

			URI uri = URI.createFileURI(file.toString());
			try {
				ResourceSet resourceSet = new ResourceSetImpl();
				XMLResource xmiResource = (XMLResource) resourceSet
						.getResource(uri, true);

				EObject object = xmiResource.getContents().get(0);

				assertNotNull(object);
				index.indexContents(object);
				count++;
			} catch (Exception e) {
				file.delete();
				e.printStackTrace();
			}
		}
		index.setHoldCommits(false);
		long stop = System.currentTimeMillis();
		System.out.println("Indexed " + count + " files in "
				+ String.valueOf(stop - start) + "ms");

	}
}
