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

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.api.sonar.runner.SonarRunnerPlugin
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import com.jfrog.bintray.gradle.BintrayPlugin
import nl.javadude.gradle.plugins.license.License

import org.ajoberstar.gradle.git.plugins.GithubPagesPlugin
import org.ajoberstar.gradle.imports.OrganizeImportsPlugin

class AjoberstarPlugin implements Plugin<Project> {
	void apply(Project project) {
		AjoberstarExtension extension = project.extensions.create('ajoberstar', AjoberstarExtension, project)
		project.plugins.apply(OrganizeImportsPlugin)
		addGhPagesConfig(project, extension)
		addJavaConfig(project, extension)
		addGroovyConfig(project, extension)
		addLicenseConfig(project, extension)
		addPublishingConfig(project, extension)
	}

	private void addGhPagesConfig(Project project, AjoberstarExtension extension) {
		project.plugins.apply(GithubPagesPlugin)
		project.githubPages {
			repoUri = extension.repoUri
			pages {
				from 'src/gh-pages'
			}
		}
	}

	private void addJavaConfig(Project project, AjoberstarExtension extension) {
		project.plugins.withType(JavaPlugin) {
			project.plugins.apply(JacocoPlugin)
			project.plugins.apply(SonarRunnerPlugin)
			project.plugins.apply(EclipsePlugin)
			project.plugins.apply(IdeaPlugin)

			project.jacoco.toolVersion = '0.7.0.201403182114'

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
		project.plugins.withType(GroovyPlugin) {
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
		project.plugins.apply(MavenPublishPlugin)
		project.publishing {
			publications {
				main(MavenPublication) {
					project.plugins.withType(JavaPlugin) {
						from project.components.java
						artifact project.sourcesJar
						artifact project.javadocJar
					}

					project.plugins.withType(GroovyPlugin) {
						artifact project.groovydocJar
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

		project.plugins.apply(BintrayPlugin)
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
