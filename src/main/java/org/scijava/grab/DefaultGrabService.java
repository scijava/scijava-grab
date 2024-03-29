/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2017 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
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
			final GrapeEngine instance = grapeEngine();
			if (instance != null) {
				instance.grab(endorsed);
			}
		}
	}

	@Override
	public void grab(final Map<String, Object> dependency) {
		if (grabEnabled) {
			final GrapeEngine instance = grapeEngine();
			if (instance != null) {
				if (!dependency.containsKey(AUTO_DOWNLOAD_SETTING)) {
					dependency.put(AUTO_DOWNLOAD_SETTING, autoDownload);
				}
				if (!dependency.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					dependency.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}

				if (!dependency.containsKey("classLoader")) {
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
			final GrapeEngine instance = grapeEngine();
			if (instance != null) {
				if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
					args.put(AUTO_DOWNLOAD_SETTING, autoDownload);
				}
				if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}

				if (!args.containsKey("classLoader")) {
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
			final GrapeEngine instance = grapeEngine();
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
			final GrapeEngine instance = grapeEngine();
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
			final GrapeEngine instance = grapeEngine();
			if (instance != null) {
				maps = instance.listDependencies(cl);
			}
		}
		return maps == null ? new Map[0] : maps;
	}

	@Override
	public void addResolver(final Map<String, Object> args) {
		if (grabEnabled) {
			final GrapeEngine instance = grapeEngine();
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

		// NB: Used by Groovy internally; without this, we'll suffer as follows:
		// groovy.lang.MissingPropertyException: No such property: exclusiveGrabArgs for class: org.scijava.grab.DefaultGrabService$GrapeSciJava
		// at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.unwrap(ScriptBytecodeAdapter.java:53)
		@SuppressWarnings("unused")
		private Map<String, List<String>> exclusiveGrabArgs =
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
		public ClassLoader chooseClassLoader(
			@SuppressWarnings("rawtypes") final Map args)
		{
			ClassLoader loader = (ClassLoader) args.get("classLoader");

			if (this.isValidTargetClassLoader(loader)) {
				if (args.get("refObject") == null) {
					if (!args.containsKey("calleeDepth")) {
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

		private boolean isValidTargetClassLoader(final ClassLoader loader) {
			return loader == null ? false : loader.getClass() == ClassLoader.class;
		}
	}

	// -- Helper methods --

	private GrapeEngine grapeEngine() {
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
