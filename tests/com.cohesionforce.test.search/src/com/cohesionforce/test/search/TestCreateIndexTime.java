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
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.cohesionforce.search.EMFIndex;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCreateIndexTime {

	private static EMFIndex index;

	@BeforeClass
	public static void setupIndex() {
		try {
			URL file = Activator.getContext().getBundle().getEntry("index");
			String url = null;
			url = FileLocator.toFileURL(file).getFile();
			index = new EMFIndex();
			index.initialize(url);
			index.clearIndex();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test1IndexDirectoryWithCommits() throws IOException {
		System.out.println();
		System.out.println("Testing Indexing of 100 files");
		URL dir = Activator.getContext().getBundle().getEntry("output");

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
				System.out.println("Indexed " + count + " " + uri);
				count++;
				if (count == 100)
					break;
			} catch (Exception e) {
				file.delete();
				e.printStackTrace();
			}
		}
		long stop = System.currentTimeMillis();
		System.out.println("Indexed " + count + " files in "
				+ String.valueOf((stop - start) / 1000) + "s");
	}

	@Test
	public void test2IndexDirectoryWithoutCommits() throws IOException {
		System.out.println();
		System.out
				.println("Testing Indexing of 100 files while turning off commits");
		URL dir = Activator.getContext().getBundle().getEntry("output");

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
				System.out.println("Indexed " + count + " " + uri);
				count++;
				if (count == 100)
					break;
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
