/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates. 
 * 
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fortify.plugin.jenkins.steps.types;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.steps.Validators;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class JavaScanType extends ProjectScanType {
	private String javaVersion;
	private String javaClasspath;
	private String javaSrcFiles;
	private String javaAddOptions;

	@DataBoundConstructor
	public JavaScanType() {
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public String getJavaClasspath() {
		return javaClasspath;
	}

	public String getJavaSrcFiles() {
		return javaSrcFiles;
	}

	public String getJavaAddOptions() {
		return javaAddOptions;
	}

	@DataBoundSetter
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	@DataBoundSetter
	public void setJavaClasspath(String javaClasspath) {
		this.javaClasspath = javaClasspath;
	}

	@DataBoundSetter
	public void setJavaSrcFiles(String javaSrcFiles) {
		this.javaSrcFiles = javaSrcFiles;
	}

	@DataBoundSetter
	public void setJavaAddOptions(String javaAddOptions) {
		this.javaAddOptions = javaAddOptions;
	}

	@Extension @Symbol("fortifyJava")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(JavaScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.JavaScanType_DisplayName();
		}

		public ListBoxModel doFillJavaVersionItems() {
			ListBoxModel items = new ListBoxModel();
			items.add("1.5", "1.5");
			items.add("1.6", "1.6");
			items.add("1.7", "1.7");
			items.add("1.8", "1.8");
			items.add("1.9", "1.9");
			items.add("10", "10");
			items.add("11", "11");
			items.add("12", "12");

			return items;
		}

		public FormValidation doCheckJavaSrcFiles(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}
	}

}
