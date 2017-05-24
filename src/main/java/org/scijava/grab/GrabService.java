/*-
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2017 Board of Regents of the University of
 * Wisconsin-Madison and Hadrien Mary.
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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.scijava.service.SciJavaService;

/**
 * Interface for services which acquire dependencies at runtime.
 *
 * @author Hadrien Mary
 */
public interface GrabService extends SciJavaService {

	void addResolver(Map<String, Object> args);

	Map<String, Map<String, List<String>>> dependencies();

	/** Global flag to ignore checksums. By default it is set to false. */
	boolean getDisableChecksums();

	/**
	 * This is a static access auto download enabler. It will set the
	 * 'autoDownload' value to the passed in arguments map if not already set. If
	 * 'autoDownload' is set the value will not be adjusted.
	 * <p>
	 * This applies to the grab and resolve calls.
	 * <p>
	 * If it is set to false, only previously downloaded grapes will be used. This
	 * may cause failure in the grape call if the library has not yet been
	 * downloaded
	 * <p>
	 * If it is set to true, then any jars not already downloaded will
	 * automatically be downloaded. Also, any versions expressed as a range will
	 * be checked for new versions and downloaded (with dependencies) if found.
	 * <p>
	 * By default it is set to true.
	 */
	boolean getEnableAutoDownload();

	/**
	 * This is a static access kill-switch. All of the static shortcut methods in
	 * this class will not work if this property is set to false. By default it is
	 * set to true.
	 */
	boolean isGrabEnabled();

	void grab(String endorsed);

	void grab(Map<String, Object> dependency);

	void grab(Map<String, Object> args, Map... dependencies);

	Map[] listDependencies(ClassLoader cl);

	URI[] resolve(Map<String, Object> args, Map... dependencies);

	URI[] resolve(Map<String, Object> args, List depsInfo, Map... dependencies);

	/**
	 * Sets global flag to ignore checksums. By default it is set to false.
	 */
	void setDisableChecksums(boolean disableChecksums);

	/**
	 * This toggles the auto download feature. It will set the 'autoDownload'
	 * value to the passed in arguments map if not already set. If 'autoDownload'
	 * is set the value will not be adjusted.
	 * <p>
	 * This applies to the grab and resolve calls.
	 * <p>
	 * If it is set to false, only previously downloaded grapes will be used. This
	 * may cause failure in the grape call if the library has not yet been
	 * downloaded.
	 * <p>
	 * If it is set to true, then any jars not already downloaded will
	 * automatically be downloaded. Also, any versions expressed as a range will
	 * be checked for new versions and downloaded (with dependencies) if found. By
	 * default it is set to true.
	 */
	void setEnableAutoDownload(boolean enableAutoDownload);

	/**
	 * This is a static access kill-switch. All of the static shortcut methods in
	 * this class will not work if this property is set to false. By default it is
	 * set to true.
	 */
	void setGrabEnabled(boolean grabEnabled);

}
