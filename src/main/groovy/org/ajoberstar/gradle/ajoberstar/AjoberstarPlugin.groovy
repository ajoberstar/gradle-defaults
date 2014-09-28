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
package org.ajoberstar.gradle.ajoberstar

import nl.javadude.gradle.plugins.license.License

import org.ajoberstar.grgit.Grgit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class AjoberstarPlugin implements Plugin<Project> {
	void apply(Project project) {
		AjoberstarExtension extension = project.extensions.create('ajoberstar', AjoberstarExtension, project)
		project.plugins.apply('organize-imports')
		addGhPagesConfig(project, extension)
		addJavaConfig(project, extension)
		addGroovyConfig(project, extension)
		addScalaConfig(project, extension)
		addLicenseConfig(project, extension)
		addPublishingConfig(project, extension)
		addReleaseConfig(project, extension)
		addOrderingRules(project, extension)
	}

	private void addGhPagesConfig(Project project, AjoberstarExtension extension) {
		project.plugins.apply('org.ajoberstar.github-pages')
		project.githubPages {
			repoUri = extension.repoUri
			pages {
				from 'src/gh-pages'
			}
		}
	}

	private void addJavaConfig(Project project, AjoberstarExtension extension) {
		project.plugins.withId('java') {
			project.plugins.apply('jacoco')
			project.plugins.apply('sonar-runner')
			project.plugins.apply('eclipse')
			project.plugins.apply('idea')

			project.githubPages {
				pages {
					from(project.javadoc.outputs.files) {
						into 'docs/javadoc'
					}
				}
			}

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

	private void addGroovyConfig(Project project, AjoberstarExtension extension) {
		project.plugins.withId('groovy') {
			project.sonarRunner.sonarProperties {
				property 'sonar.language', 'grvy'
			}

			project.githubPages {
				pages {
					from(project.groovydoc.outputs.files) {
						into 'docs/groovydoc'
					}
				}
			}

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

	private void addScalaConfig(Project project, AjoberstarExtension extension) {
		project.plugins.withId('scala') {
			project.githubPages {
				pages {
					from(project.scaladoc.outputs.files) {
						into 'docs/scaladoc'
					}
				}
			}

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

	private void addLicenseConfig(Project project, AjoberstarExtension extension) {
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

	private void addPublishingConfig(Project project, AjoberstarExtension extension) {
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
								appendNode('name', extension.friendlyName)
								appendNode('description', extension.description)
								appendNode('url', "http://github.com/ajoberstar/${project.name}")
								appendNode('licenses').with {
									appendNode('license').with {
										appendNode('name', extension.licenseName)
										appendNode('url', extension.licenseUrl)
									}
								}
								appendNode('developers').with {
									appendNode('developer').with {
										appendNode('id', 'ajoberstar')
										appendNode('name', 'Andrew Oberstar')
										appendNode('email', 'andrew@ajoberstar.org')
									}
								}
								appendNode('scm').with {
									appendNode('connection', "scm:git:${extension.repoUri}")
									appendNode('developerConnection', "scm:git:${extension.repoUri}")
									appendNode('url', extension.repoUri)
								}
							}
						}
					}
				}
				repositories {
					mavenLocal()
				}
			}

			project.plugins.apply('bintray')
			if (project.hasProperty('bintrayUser') && project.hasProperty('bintrayKey')) {
				project.afterEvaluate {
					project.bintray {
						user = project.bintrayUser
						key = project.bintrayKey
						publications = ['main']
						pkg {
							repo = extension.bintrayRepo
							name = "${project.group}:${project.name}"
							desc = extension.description
							licenses = extension.bintrayLicenses
							labels = extension.bintrayLabels
							signFiles = true
							if (project.hasProperty('gpgPassphrase')) {
								signPassphrase = project.gpgPassphrase
							}
						}
					}
				}
			}
		}
	}

	private void addReleaseConfig(Project project, AjoberstarExtension extension) {
		project.plugins.apply('org.ajoberstar.release-opinion')
		project.release {
			grgit = Grgit.open(project.file('.'))
		}
		project.tasks.release.dependsOn 'clean', 'build', 'publishGhPages'

		project.plugins.withId('bintray') {
			project.tasks.release.dependsOn 'bintrayUpload'
		}
	}

	private void addOrderingRules(Project project, AjoberstarExtension extension) {
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
}
