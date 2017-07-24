package com.cognifide.gradle.aem.deploy

import com.cognifide.gradle.aem.internal.PropertyParser
import com.cognifide.gradle.aem.pkg.ComposeTask
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.BooleanUtils
import org.gradle.api.Project

@JsonIgnoreProperties(ignoreUnknown = true)
class ListResponse private constructor() {

    companion object {
        fun fromJson(json: String): ListResponse {
            return ObjectMapper().readValue(json, ListResponse::class.java)
        }
    }

    lateinit var results: List<ListResult>

    fun resolvePath(project: Project): String? {
        return PathResolver.values()
                .asSequence()
                .map { it.resolve(project, this) }
                .firstOrNull { it != null }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ListResult {
        lateinit var path: String

        lateinit var downloadName: String

        lateinit var group: String

        lateinit var name: String

        lateinit var version: String
    }

    enum class PathResolver {

        BY_PROJECT_PROPS() {
            override fun resolve(project: Project, response: ListResponse): String? {
                var path: String? = null

                val projectName = PropertyParser(project).name
                val props = "[group=${project.group}][name=$projectName][version=${project.version}]"

                project.logger.info("Trying to find package by project properties: $props")

                val result = response.results.find { result ->
                    (result.group == project.group) && (result.name == projectName) && (result.version == project.version)
                }

                if (result != null) {
                    path = result.path
                    project.logger.info("Package found by project properties: $props")
                } else {
                    project.logger.info("Package cannot be found by project properties: $props")
                }

                return path
            }

        },
        BY_CONVENTION_PATH() {
            override fun resolve(project: Project, response: ListResponse): String? {
                var path: String? = null

                val projectName = PropertyParser(project).name
                val conventionPaths = listOf(
                        "/etc/packages/${project.group}/${(project.tasks.getByName(ComposeTask.NAME) as ComposeTask).archiveName}",
                        "/etc/packages/${project.group}/$projectName-${project.version}.zip"
                )

                for (conventionPath in conventionPaths) {
                    project.logger.info("Trying to find package by convention path '$conventionPath'.")

                    val result = response.results.find { result -> result.path == conventionPath }
                    if (result != null) {
                        path = result.path
                        project.logger.info("Package found by convention path.")
                        break
                    } else {
                        project.logger.info("Package cannot be found by convention path.")
                    }
                }

                return path
            }
        },
        BY_DOWNLOAD_NAME() {
            override fun resolve(project: Project, response: ListResponse): String? {
                var path: String? = null

                val projectName = PropertyParser(project).name
                val downloadName = "$projectName-${project.version}.zip"

                if (BooleanUtils.toBoolean(project.properties.getOrElse("aem.deploy.skipDownloadName", { "true" }) as String?)) {
                    project.logger.info("Finding package by download name '$downloadName' is skipped.")
                } else {
                    project.logger.warn("Trying to find package by download name '$downloadName' which can collide with other packages.")

                    val result = response.results.find { result -> result.downloadName == downloadName }
                    if (result != null) {
                        path = result.path
                        project.logger.info("Package found by download name.")
                    } else {
                        project.logger.info("Package cannot be found by download name.")
                    }
                }

                return path
            }
        };

        abstract fun resolve(project: Project, response: ListResponse): String?

    }

}