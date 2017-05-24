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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.reflection.ReflectionUtils;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import groovy.grape.GrapeEngine;
import groovy.grape.GrapeIvy;

/**
 * Default implementation of {@link GrabService}.
 * <p>
 * Heavily adapted from <a href=
 * "https://github.com/apache/groovy/blob/master/src/main/groovy/grape/Grape.java">
 * Grape.java</a>.
 * </p>
 *
 * @author Hadrien Mary
 */
@Plugin(type = Service.class)
public class DefaultGrabService extends AbstractService implements GrabService {

	public static final String AUTO_DOWNLOAD_SETTING = "autoDownload";
	public static final String DISABLE_CHECKSUMS_SETTING = "disableChecksums";
	public static final String SYSTEM_PROPERTIES_SETTING = "systemProperties";

	@Parameter
	private Context context;

	private boolean grabEnabled = Boolean.valueOf(System.getProperty(
		"scijava.grab.enable", "true"));

	private boolean autoDownload = Boolean.valueOf(System.getProperty(
		"scijava.grab.autoDownload", "true"));

	private boolean disableChecksums = Boolean.valueOf(System.getProperty(
		"scijava.grab.disableChecksums", "false"));

	private GrapeEngine grapeEngine;

	@Override
	public boolean isGrabEnabled() {
		return grabEnabled;
	}

	@Override
	public void setGrabEnabled(final boolean enableGrab) {
		this.grabEnabled = enableGrab;
	}

	@Override
	public boolean getEnableAutoDownload() {
		return autoDownload;
	}

	@Override
	public void setEnableAutoDownload(final boolean enableAutoDownload) {
		this.autoDownload = enableAutoDownload;
	}

	@Override
	public boolean getDisableChecksums() {
		return disableChecksums;
	}

	@Override
	public void setDisableChecksums(final boolean disableChecksums) {
		this.disableChecksums = disableChecksums;
	}

	@Override
	public void grab(final String endorsed) {
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				instance.grab(endorsed);
			}
		}
	}

	@Override
	public void grab(final Map<String, Object> dependency) {
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!dependency.containsKey(AUTO_DOWNLOAD_SETTING)) {
					dependency.put(AUTO_DOWNLOAD_SETTING, autoDownload);
				}
				if (!dependency.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					dependency.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}

				if (!dependency.keySet().contains("classLoader")) {
					dependency.put("classLoader", this.context().getClass()
						.getClassLoader());
				}

				instance.grab(dependency);
			}
		}
	}

	@Override
	public void grab(final Map<String, Object> args, final Map... dependencies) {
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
					args.put(AUTO_DOWNLOAD_SETTING, autoDownload);
				}
				if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}

				if (!args.keySet().contains("classLoader")) {
					args.put("classLoader", this.context().getClass().getClassLoader());
				}

				instance.grab(args, dependencies);
			}
		}
	}

	@Override
	public Map<String, Map<String, List<String>>> dependencies() {
		Map<String, Map<String, List<String>>> grapes = null;
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				grapes = instance.enumerateGrapes();
			}
		}
		return grapes == null ? Collections.emptyMap() : grapes;
	}

	@Override
	public URI[] resolve(final Map<String, Object> args,
		final Map... dependencies)
	{
		return resolve(args, null, dependencies);
	}

	@Override
	public URI[] resolve(final Map<String, Object> args, final List depsInfo,
		final Map... dependencies)
	{
		URI[] uris = null;
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
					args.put(AUTO_DOWNLOAD_SETTING, autoDownload);
				}
				if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}
				uris = instance.resolve(args, depsInfo, dependencies);
			}
		}
		return uris == null ? new URI[0] : uris;
	}

	@Override
	public Map[] listDependencies(final ClassLoader cl) {
		Map[] maps = null;
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				maps = instance.listDependencies(cl);
			}
		}
		return maps == null ? new Map[0] : maps;
	}

	@Override
	public void addResolver(final Map<String, Object> args) {
		if (grabEnabled) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				instance.addResolver(args);
			}
		}
	}

	// -- Helper classes --

	/**
	 * An extension of {@link GrapeIvy} which can use any {@link ClassLoader} (not
	 * only {@link groovy.lang.GroovyClassLoader}).
	 */
	private static class GrapeSciJava extends GrapeIvy {

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
			return loader == null ? false : loader.getClass() == ClassLoader.class;
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

	// -- Helper methods --

	private GrapeEngine getGrapeEngine() {
		if (grapeEngine == null) {
			// Set some settings for Ivy
			System.setProperty("groovy.grape.report.downloads", "true");
			System.setProperty("grape.root", Paths.get(System.getProperty(
				"user.home"), ".scijava").toString());

			// Initialize the GrapeEngine
			grapeEngine = new GrapeSciJava();
		}
		return grapeEngine;
	}

}
