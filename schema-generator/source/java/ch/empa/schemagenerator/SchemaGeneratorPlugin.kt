/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.empa.schemagenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createFile

internal interface SchemaGeneratorPluginExtension {
    val annotationClass: Property<Class<out Annotation>>
    val outputPath: Property<Path>
    val packageName: Property<String>
}

class SchemaGeneratorPlugin : Plugin<Project> {

    private fun getPackageRegex(packageName: String): Regex {
        return Regex(packageName.replace(".", "\\.") + ".*")
    }

    private fun getAllClassesInPackage(project: Project, packageName: String): List<Class<*>>{
        project.logger.warn("Getting all annotated classes")
        val packageRegex = getPackageRegex(packageName)
        val classes = project.buildDir.resolve("classes").walk().filter{
            file ->
            val relativeName = project.buildDir.resolve("classes/java/main").toPath().relativize(file.parentFile.toPath()).toString()
            println("${relativeName}, ${file.parent}, ${file.parent.matches(packageRegex)}")
                file.parentFile.resolve(project.buildDir).name.matches(packageRegex)
        }
        classes?.forEach { println(it.name) }
        val classLoader = project.buildscript.classLoader
        return classes.map { classLoader.loadClass(it.nameWithoutExtension) }.toList()
    }

    private fun filterClasses(classes: List<Class<*>>, annotationClass: Class<out Annotation>): List<Class<*>> {
        return classes.filter { it.isAnnotationPresent(annotationClass) }
    }

    override fun apply(project: Project) {
        project.logger.warn("Applying SchemaGeneratorPlugin")
        val extConfig = project.extensions.create("schemaGenerator", SchemaGeneratorPluginExtension::class.java)
        val generateTask = project.task("generateSchema").doFirst { task: Task? ->
            project.logger.warn("Generating schema")
            filterClasses(getAllClassesInPackage(project, extConfig.packageName.get()), extConfig.annotationClass.get()).forEach { project.logger.info(it.name) }
            println("Hello world: ${extConfig.annotationClass.get()}")
            val outputFile = extConfig.outputPath.get().toAbsolutePath()
            outputFile.toFile().writer().use { writer ->
                writer.write("Hello world: ${extConfig.annotationClass.get()}")
            }
        }.mustRunAfter("compileJava")
    }
}
