/*
 * Copyright 2014 the original author or authors.
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
package org.ajoberstar.gradle.defaults

import org.gradle.api.Project

class DefaultsExtension {
	String id

	String orgName
	String orgUrl

	String bintrayRepo
	String bintrayPkg
	String bintrayLabels

	Set developers
	Set contributors

	String siteUrl
	String issuesUrl
	String vcsReadUrl
	String vcsWriteUrl

	String licenseKey
	String licenseName
	String licenseUrl

	String copyrightYears

	DefaultsExtension(Project project) {
		project.afterEvaluate {
			this.bintrayPkg = bintrayPkg ?: "${project.group}:${project.name}"

			this.siteUrl = siteUrl ?: "https://github.com/${id}/${project.name}"
			this.issuesUrl = issuesUrl ?: "${siteUrl}/issues"
			this.vcsReadUrl = vcsReadUrl ?: "${siteUrl}.git"
			this.vcsWriteUrl = vcsWriteUrl ?: "git@github.com:${id}/${project.name}.git"

			this.licenseKey = licenseKey ?: 'Apache-2.0'
			this.licenseName = licenseName ?: 'The Apache Software License, Version 2.0'
			this.licenseUrl = licenseUrl ?: 'http://www.apache.org/licenses/LICENSE-2.0'
		}
	}
}
