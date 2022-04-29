/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2017 - 2022 SciJava developers.
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

package org.scijava.grab.script.process;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.scijava.grab.GrabService;
import org.scijava.log.LogService;
import org.scijava.parse.ParseService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptModule;
import org.scijava.script.process.ScriptCallback;
import org.scijava.script.process.ScriptProcessor;

/**
 * A {@link ScriptProcessor} which handles {@code #@dependency} and
 * {@code #@repository} directives.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = ScriptProcessor.class)
public class GrabScriptProcessor implements ScriptProcessor {

	@Parameter
	private GrabService grabService;

	@Parameter
	private ParseService parser;

	@Parameter
	private LogService log;

	private ScriptInfo info;
	private List<String> grabArgs;
	private List<String> resolveArgs;

	// -- ScriptProcessor methods --

	@Override
	public void begin(final ScriptInfo scriptInfo) {
		info = scriptInfo;
	}

	@Override
	public String process(final String line) {
		if (!line.matches("(?i)^#@ *(dependency|repository)\\(.*")) return line;

		final int paren = line.indexOf('(');
		final String directive = line.substring(2, paren).toLowerCase();
		final String arg = line.substring(paren);

		switch (directive) {
			case "dependency":
				if (grabArgs == null) grabArgs = new ArrayList<>();
				grabArgs.add(arg);
				break;
			case "repository":
				if (resolveArgs == null) resolveArgs = new ArrayList<>();
				resolveArgs.add(arg);
				break;
		}

		return "";
	}

	@Override
	public void end() {
		if (grabArgs == null && resolveArgs == null) return;

		info.callbacks().add(new ScriptCallback() {

			@Override
			public void invoke(final ScriptModule module) throws ScriptException {
				if (resolveArgs != null) {
					for (final String arg : resolveArgs) {
						grabService.resolve(parser.parse(arg).asMap());
					}
				}
				if (grabArgs != null) {
					for (final String arg : grabArgs) {
						grabService.grab(parser.parse(arg).asMap());
					}
				}
			}
		});
	}
}
