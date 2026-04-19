package io.github.freehij.mcartifact

import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.json.JsonSlurper
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class McArtifactExtension {
    String version
    String environment
}

class McArtifactPlugin implements Plugin<Project> {
    void apply(Project project) {
        def ext = project.extensions.create('mcartifact', McArtifactExtension)
        project.afterEvaluate {
            def key = "${ext.version}_${ext.environment}"
            def url = "https://raw.githubusercontent.com/freehij/resources/refs/heads/main/versions.json"
            def json = new JsonSlurper().parse(new URL(url))
            if (!json[key]) throw new RuntimeException("Key $key not found in versions.json")
            def downloadUrl = json[key]
            def fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1)
            def cacheDir = new File(project.gradle.gradleUserHomeDir, ".gradle/mcartifacts/$key")
            def artifactFile = new File(cacheDir, fileName)
            if (!artifactFile.exists()) {
                artifactFile.parentFile.mkdirs()
                artifactFile << new URL(downloadUrl).openStream()
            }
            def dependencyFile = artifactFile
            if (ext.environment == "server") {
                def mergedFile = new File(cacheDir, "merged-${fileName}")
                if (!mergedFile.exists()) {
                    def innerPath = "META-INF/versions/${ext.version}/server-${ext.version}.jar"
                    def tempInner = File.createTempFile("inner-server-${ext.version}", ".jar")
                    tempInner.deleteOnExit()
                    new ZipFile(artifactFile).withCloseable { outer ->
                        def innerEntry = outer.getEntry(innerPath)
                        if (!innerEntry) throw new RuntimeException("Inner jar not found at $innerPath")
                        tempInner.withOutputStream { os ->
                            os << outer.getInputStream(innerEntry)
                        }
                    }
                    mergedFile.parentFile.mkdirs()
                    new ZipOutputStream(new FileOutputStream(mergedFile)).withCloseable { zos ->
                        new ZipFile(tempInner).withCloseable { innerZip ->
                            innerZip.entries().each { entry ->
                                if (!entry.directory) {
                                    zos.putNextEntry(new ZipEntry(entry.name))
                                    zos << innerZip.getInputStream(entry)
                                    zos.closeEntry()
                                }
                            }
                        }
                        new ZipFile(artifactFile).withCloseable { outerZip ->
                            outerZip.entries().each { entry ->
                                if (!entry.directory && entry.name.startsWith("net/minecraft/bundler/")) {
                                    zos.putNextEntry(new ZipEntry(entry.name))
                                    zos << outerZip.getInputStream(entry)
                                    zos.closeEntry()
                                }
                            }
                        }
                    }
                    tempInner.delete()
                }
                dependencyFile = mergedFile
            }
            project.dependencies.add('compileOnly', project.files(dependencyFile))
            project.tasks.register('downloadMcArtifact') {
                doLast { println "Artifact available at: ${dependencyFile.absolutePath}" }
            }
        }
    }
}