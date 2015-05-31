/*
 * Copyright 2014-2015 the original author or authors.
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

import nl.javadude.gradle.plugins.license.License

import org.ajoberstar.grgit.Grgit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class DefaultsPlugin implements Plugin<Project> {

	void apply(Project project) {
		DefaultsExtension extension = project.extensions.create('defaults', DefaultsExtension, project)
		project.plugins.apply('org.ajoberstar.organize-imports')

		addGhPagesConfig(project, extension)
		addReleaseConfig(project, extension)

		project.allprojects { prj ->
			addJavaConfig(prj, extension)
			addGroovyConfig(prj, extension)
			addScalaConfig(prj, extension)
			addLicenseConfig(prj, extension)
			addMavenPublishingConfig(prj, extension)
            addBintrayPublishingConfig(project, extension)
			addOrderingRules(prj, extension)
		}
	}


	private void addGhPagesConfig(Project project, DefaultsExtension extension) {
		project.plugins.apply('org.ajoberstar.github-pages')

		def addOutput = { task ->
			project.githubPages.pages.from(task.outputs.files) {
				into "docs${task.path}".replace(':', '/')
			}
		}

		project.allprojects { prj ->
			prj.plugins.withId('java') { addOutput(prj.javadoc) }
			prj.plugins.withId('groovy') { addOutput(prj.groovydoc) }
			prj.plugins.withId('scala') { addOutput(prj.scaladoc) }
		}

		project.afterEvaluate {
			project.githubPages {
				repoUri = extension.vcsWriteUrl
				pages {
					from 'src/gh-pages'
				}
			}
		}
	}

	private void addJavaConfig(Project project, DefaultsExtension extension) {
		project.plugins.withId('java') {
			project.plugins.apply('jacoco')
			project.plugins.apply('sonar-runner')
			project.plugins.apply('eclipse')
			project.plugins.apply('idea')

			Task sourcesJar = project.tasks.create('sourcesJar', Jar)
			sourcesJar.with {
				classifier = 'sources'
				from project.sourceSets.main.allSource
			}

			Task javadocJar = project.tasks.create('javadocJar', Jar)
			javadocJar.with {
				classifier = 'javadoc'
				from project.tasks.javadoc.outputs.files
			}

			project.artifacts {
				archives sourcesJar
				archives javadocJar
			}
		}
	}

	private void addGroovyConfig(Project project, DefaultsExtension extension) {
		project.plugins.withId('groovy') {
			Task groovydocJar = project.tasks.create('groovydocJar', Jar)
			groovydocJar.with {
				classifier = 'groovydoc'
				from project.tasks.groovydoc.outputs.files
			}

			project.artifacts {
				archives groovydocJar
			}
		}

	}

	private void addScalaConfig(Project project, DefaultsExtension extension) {
		project.plugins.withId('scala') {
			Task scaladocJar = project.tasks.create('scaladocJar', Jar)
			scaladocJar.with {
				classifier = 'scaladoc'
				from project.tasks.scaladoc.outputs.files
			}

			project.artifacts {
				archives scaladocJar
			}
		}
	}

	private void addLicenseConfig(Project project, DefaultsExtension extension) {
		project.plugins.apply('license')
		project.afterEvaluate {
			project.license {
				header = project.rootProject.file('gradle/HEADER')
				strictCheck = true
				useDefaultMappings = false
				mapping 'groovy', 'SLASHSTAR_STYLE'
				mapping 'java', 'SLASHSTAR_STYLE'
				ext.year = extension.copyrightYears
			}
			project.tasks.withType(License) {
				exclude '**/*.properties'
			}
		}
	}

	private void addReleaseConfig(Project project, DefaultsExtension extension) {
		project.plugins.apply('org.ajoberstar.release-opinion')
		project.release {
			grgit = Grgit.open(project.file('.'))
		}
        def releaseTask = project.tasks.release
        releaseTask.dependsOn 'publishGhPages'
        project.allprojects { prj ->
            prj.plugins.withId('org.gradle.base') {
                releaseTask.dependsOn prj.clean, prj.build
            }
            prj.plugins.withId('com.jfrog.bintray') {
                releaseTask.dependsOn prj.bintrayUpload
            }
        }
	}

	private void addOrderingRules(Project project, DefaultsExtension extension) {
		def clean = project.tasks['clean']
		project.tasks.all { task ->
			if (task != clean) {
				task.shouldRunAfter clean
			}
		}

		def build = project.tasks['build']
		project.tasks.all { task ->
			if (task.group == 'publishing') {
				task.shouldRunAfter build
			}
		}
	}

	private void addMavenPublishingConfig(Project project, DefaultsExtension extension) {
		project.plugins.withId('maven-publish') {
			project.publishing {
				publications {
					main(MavenPublication) {
						project.plugins.withId('java') {
							from project.components.java
							artifact project.sourcesJar
							artifact project.javadocJar
						}

						project.plugins.withId('groovy') {
							artifact project.groovydocJar
						}

						project.plugins.withId('scala') {
							artifact project.scaladocJar
						}

						pom.withXml {
							asNode().with {
								appendNode('name', project.name)
								appendNode('description', project.description)
								appendNode('url', extension.siteUrl)
								if (extension.orgName) {
									appendNode('organization').with {
										appendNode('name', extension.orgName)
										appendNode('url', extension.orgUrl)
									}
								}
								appendNode('licenses').with {
									appendNode('license').with {
										appendNode('name', extension.licenseName)
										appendNode('url', extension.licenseUrl)
									}
								}
								if (extension.developers) {
									appendNode('developers').with { node ->
										extension.developers.each { developer ->
											node.appendNode('developer').with {
												appendNode('id', developer.id)
												appendNode('name', developer.name)
												appendNode('email', developer.email)
											}
										}
									}
								}
								if (extension.contributors) {
									appendNode('contributors').with { node ->
										extension.contributors.each { contributor ->
											node.appendNode('contributor').with {
												appendNode('id', contributor.id)
												appendNode('name', contributor.name)
												appendNode('email', contributor.email)
											}
										}
									}
								}
								appendNode('scm').with {
									appendNode('connection', "scm:git:${extension.vcsReadUrl}")
									appendNode('developerConnection', "scm:git:${extension.vcsWriteUrl}")
									appendNode('url', extension.siteUrl)
								}
							}
						}
					}
				}
				repositories {
					mavenLocal()
				}
			}
		}
	}

	private void addBintrayPublishingConfig(Project project, DefaultsExtension extension) {
		project.plugins.withId('com.jfrog.bintray') {
			if (project.hasProperty('bintrayUser') && project.hasProperty('bintrayKey')) {
				project.afterEvaluate {
					def bintrayAttributes = [:]
					if (extension.bintrayLabels.contains('gradle')) {
						def pluginIds = project.fileTree(
							dir: 'src/main/resources/META-INF/gradle-plugins',
							include: '*.properties').collect { file -> file.name[0..(file.name.lastIndexOf('.') - 1)] }
						bintrayAttributes = ['gradle-plugin': pluginIds.collect { "${it}:${project.group}:${project.name}" }]
					}

					project.bintray {
						user = project.bintrayUser
						key = project.bintrayKey

						publications = ['main']

						publish = true
						pkg {
							if (extension.orgName) {
								userOrg = extension.id
							}
							repo = extension.bintrayRepo
							name = extension.bintrayPkg
							desc = project.description
							websiteUrl = extension.siteUrl
							issueTrackerUrl = extension.issuesUrl
							vcsUrl = extension.vcsReadUrl
							licenses = [extension.licenseKey]
							labels = extension.bintrayLabels
							publicDownloadNumbers = true
							version {
								vcsTag = "v${project.version}"
								attributes = bintrayAttributes
								gpg {
									sign = true
									if (project.hasProperty('gpgPassphrase')) {
										passphrase = project.gpgPassphrase
									}
								}
								mavenCentralSync {
									if (project.hasProperty('sonatypeUsername')) {
										user = project.sonatypeUsername
									}
									if (project.hasProperty('sonatypePassword')) {
										password = project.sonatypePassword
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
