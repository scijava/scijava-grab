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

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import groovy.grape.GrapeEngine;

/**
 * Facade to GrapeEngine. Kindly stolen from
 * https://github.com/apache/groovy/blob/master/src/main/groovy/grape/Grape.
 * java.
 */
@Plugin(type = Service.class)
public class DefaultGrabService extends AbstractService implements GrabService {

	public static final String AUTO_DOWNLOAD_SETTING = "autoDownload";
	public static final String DISABLE_CHECKSUMS_SETTING = "disableChecksums";
	public static final String SYSTEM_PROPERTIES_SETTING = "systemProperties";

	@Parameter
	private Context context;

	private boolean enableGrapes = Boolean.valueOf(System.getProperty(
		"org.scijava.grape.enable", "true"));
	private boolean enableAutoDownload = Boolean.valueOf(System.getProperty(
		"org.scijava.grape.autoDownload", "true"));
	private boolean disableChecksums = Boolean.valueOf(System.getProperty(
		"org.scijava.grape.disableChecksums", "false"));

	private GrapeEngine grapeEngine = null;

	@Override
	public boolean getEnableGrapes() {
		return enableGrapes;
	}

	@Override
	public void setEnableGrapes(final boolean enableGrapes) {
		this.enableGrapes = enableGrapes;
	}

	@Override
	public boolean getEnableAutoDownload() {
		return enableAutoDownload;
	}

	@Override
	public void setEnableAutoDownload(final boolean enableAutoDownload) {
		this.enableAutoDownload = enableAutoDownload;
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
	public GrapeEngine getGrapeEngine() {
		if (this.grapeEngine == null) {

			// Set some settings for Ivy
			System.setProperty("groovy.grape.report.downloads", "true");
			System.setProperty("grape.root", Paths.get(System.getProperty(
				"user.home"), ".scijava").toString());

			// Initialize the GrapeEngine
			this.grapeEngine = new GrapeSciJava();
		}
		return grapeEngine;
	}

	@Override
	public void grab(final String endorsed) {
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				instance.grab(endorsed);
			}
		}
	}

	@Override
	public void grab(final Map<String, Object> dependency) {
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!dependency.containsKey(AUTO_DOWNLOAD_SETTING)) {
					dependency.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
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
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
					args.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
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
	public Map<String, Map<String, List<String>>> enumerateGrapes() {
		Map<String, Map<String, List<String>>> grapes = null;
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				grapes = instance.enumerateGrapes();
			}
		}
		if (grapes == null) {
			return Collections.emptyMap();
		}
		else {
			return grapes;
		}
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
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
					args.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
				}
				if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
					args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
				}
				uris = instance.resolve(args, depsInfo, dependencies);
			}
		}
		if (uris == null) {
			return new URI[0];
		}
		else {
			return uris;
		}
	}

	@Override
	public Map[] listDependencies(final ClassLoader cl) {
		Map[] maps = null;
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				maps = instance.listDependencies(cl);
			}
		}
		if (maps == null) {
			return new Map[0];
		}
		else {
			return maps;
		}

	}

	@Override
	public void addResolver(final Map<String, Object> args) {
		if (enableGrapes) {
			final GrapeEngine instance = getGrapeEngine();
			if (instance != null) {
				instance.addResolver(args);
			}
		}
	}
}
