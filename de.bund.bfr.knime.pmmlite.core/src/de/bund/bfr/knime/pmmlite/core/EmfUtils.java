/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.pmmlite.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.bund.bfr.knime.pmmlite.core.common.CommonPackage;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.data.DataPackage;
import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;

public class EmfUtils {

	private EmfUtils() {
	}

	public static String toXml(EObject obj) {
		return obj != null ? listToXml(Arrays.asList(obj)) : null;
	}

	public static <T extends EObject> T fromXml(String xml, Class<T> type) {
		List<T> list = listFromXml(xml, type);

		return list.size() == 1 ? list.get(0) : null;
	}

	public static String listToXml(List<? extends EObject> list) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Resource resource = createResource();

			resource.getContents().addAll(list);
			resource.save(outputStream, null);

			return outputStream.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T extends EObject> List<T> listFromXml(String xml, Class<T> type) {
		if (xml == null) {
			return new ArrayList<>();
		}

		try (ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
			Resource resource = createResource();

			resource.load(in, null);

			List<T> list = new ArrayList<>();

			for (EObject eObj : resource.getContents()) {
				list.add(type.cast(eObj));
			}

			return new ArrayList<>(EcoreUtil.copyAll(list));
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public static String identifiableToXml(Identifiable obj) {
		return obj != null ? identifiableListToXml(Arrays.asList(obj)) : null;
	}

	public static <T extends Identifiable> T identifiableFromXml(String xml, Class<T> type) {
		List<T> list = identifiableListFromXml(xml, type);

		return list.size() == 1 ? list.get(0) : null;
	}

	public static String identifiableListToXml(List<? extends Identifiable> list) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Map<String, EObject> byId = new LinkedHashMap<>();

			for (EObject obj : getObjectsWithReferences(list)) {
				byId.put(EcoreUtil.getID(obj), obj);
			}

			Resource resource = createResource();

			resource.getContents().addAll(byId.values());
			resource.save(outputStream, null);

			return outputStream.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T extends Identifiable> List<T> identifiableListFromXml(String xml, Class<T> type) {
		if (xml == null) {
			return new ArrayList<>();
		}

		try (ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
			Resource resource = createResource();

			resource.load(in, null);

			List<T> list = new ArrayList<>();

			if (!resource.getContents().isEmpty()) {
				EObject firstObj = resource.getContents().get(0);

				for (EObject eObj : resource.getContents()) {
					if (eObj.eClass() == firstObj.eClass()) {
						list.add(type.cast(eObj));
					}
				}
			}

			return new ArrayList<>(EcoreUtil.copyAll(list));
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	private static List<EObject> getObjectsWithReferences(Collection<? extends EObject> objects) {
		List<EObject> allReferences = new ArrayList<>();

		if (!objects.isEmpty()) {
			allReferences.addAll(objects);
			allReferences.addAll(getObjectsWithReferences(EcoreUtil.CrossReferencer.find(objects).keySet()));
		}

		return allReferences;
	}

	private static Resource createResource() {
		ResourceSet resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("*", new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(CommonPackage.eNS_URI, CommonPackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(DataPackage.eNS_URI, DataPackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(ModelsPackage.eNS_URI, ModelsPackage.eINSTANCE);

		XMIResource resource = (XMIResource) resourceSet.createResource(URI.createURI("file:///null"));

		resource.setEncoding(StandardCharsets.UTF_8.name());

		return resource;
	}
}
