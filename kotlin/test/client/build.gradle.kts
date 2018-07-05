/*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
	id("kotlin2js")
	id("kotlin-dce-js")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation(project(":biser-kotlin-js"))
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		outputFile = "$buildDir/classes/kotlin/main/app.js"
		moduleKind = "amd"
		sourceMap = true
		sourceMapEmbedSources = "always"
	}
}

tasks.withType<KotlinJsDce> {
	dceOptions.devMode = properties.containsKey("dev")
	doFirst { classpath += configurations.runtimeClasspath }
}

val webOutputDir = "$buildDir/web"
val webStaticDir = "$projectDir/src/main/web"

task<Copy>("web-static") {
	from(webStaticDir)
	into(webOutputDir)
}

task<Copy>("web-dce") {
	dependsOn("runDceKotlinJs")
	from("$buildDir/kotlin-js-min/main")
	into("$webOutputDir/js")
}

task("web-wrappers") {}

configurations.runtimeClasspath.filter { it.name.startsWith("ktjs-wrapper") }.forEach { file: File ->
	val name = file.name.take(file.name.indexOf('-', "ktjs-wrapper-".length))
	val task = task<Copy>("web-wrapper-$name") {
		from(zipTree(file))
		into("$webOutputDir/js/$name")
		include("js/**")
		includeEmptyDirs = false
		eachFile { path = path.substring(3) }
	}
	
	tasks["web-wrappers"].dependsOn.add(task)
}

task("web") {
	dependsOn("web-dce", "web-wrappers", "web-static")
	group = "build"
}

*/
