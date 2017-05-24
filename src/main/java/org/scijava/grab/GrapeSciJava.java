/*
 * Copyright 2017 SciJava.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scijava.grab;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.reflection.ReflectionUtils;

import groovy.grape.GrapeIvy;

/**
 * An extension of {@link GrapeIvy} which can use any {@link ClassLoader} (not
 * only {@link groovy.lang.GroovyClassLoader}).
 *
 * @author Hadrien Mary
 */
public class GrapeSciJava extends GrapeIvy {

	Map<String, List<String>> exclusiveGrabArgs =
		new HashMap<String, List<String>>()
	{

			{
				put("group", Arrays.asList("groupId", "organisation", "organization",
					"org"));
				put("groupId", Arrays.asList("group", "organisation", "organization",
					"org"));
				put("organisation", Arrays.asList("group", "groupId", "organization",
					"org"));
				put("organization", Arrays.asList("group", "groupId", "organisation",
					"org"));
				put("org", Arrays.asList("group", "groupId", "organisation",
					"organization"));
				put("module", Arrays.asList("artifactId", "artifact"));
				put("artifactId", Arrays.asList("module", "artifact"));
				put("artifact", Arrays.asList("module", "artifactId"));
				put("version", Arrays.asList("revision", "rev"));
				put("revision", Arrays.asList("version", "rev"));
				put("rev", Arrays.asList("version", "revision"));
				put("conf", Arrays.asList("scope", "configuration"));
				put("scope", Arrays.asList("conf", "configuration"));
				put("configuration", Arrays.asList("conf", "scope"));

			}
		};

	@Override
	public ClassLoader chooseClassLoader(final Map args) {
		ClassLoader loader = (ClassLoader) args.get("classLoader");

		if (this.isValidTargetClassLoader(loader)) {
			if (args.get("refObject") == null) {
				if (!args.keySet().contains("calleeDepth")) {
					loader = ReflectionUtils.getCallingClass((int) args.get(
						"calleeDepth")).getClassLoader();
				}
				else {
					loader = ReflectionUtils.getCallingClass(1).getClassLoader();
				}
			}

			while (loader != null && !this.isValidTargetClassLoader(loader)) {
				loader = loader.getParent();
			}
			// if (!isValidTargetClassLoader(loader)) {
			// loader = Thread.currentThread().contextClassLoader
			// }
			// if (!isValidTargetClassLoader(loader)) {
			// loader = GrapeIvy.class.classLoader
			// }
			if (!isValidTargetClassLoader(loader)) {
				throw new RuntimeException("No suitable ClassLoader found for grab");
			}
		}
		return loader;
	}

	private boolean isValidTargetClassLoader(final ClassLoader loader) {
		if (loader != null) {
			return loader.getClass() == ClassLoader.class;
		}
		else {
			return false;
		}
	}

	private boolean isValidTargetClassLoaderClass(final Class loaderClass) {
		return isValidTargetClassLoader(loaderClass.getClassLoader());
	}

	@Override
	public File getLocalGrapeConfig() {
		final InputStream configStream = GrapeSciJava.class.getResourceAsStream(
			"grapeConfig.xml");

		// Copy the config file to a temporary file since
		// the Groovy API only accept File object.
		File configTempFile = new File("");
		try {
			configTempFile = File.createTempFile("grapeConfig", ".xml");
			try {
				Files.copy(configStream, configTempFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			}
			catch (final IOException ex) {
				Logger.getLogger(GrapeSciJava.class.getName()).log(Level.SEVERE, null,
					ex);
			}
		}
		catch (final IOException ex) {
			Logger.getLogger(GrapeSciJava.class.getName()).log(Level.SEVERE, null,
				ex);
		}

		return configTempFile;
	}
}