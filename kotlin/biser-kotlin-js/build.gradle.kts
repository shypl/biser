import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

version = "1.0.2-SNAPSHOT"

plugins {
	id("kotlin2js")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.ktjs:ktjs-common:0.3.0-SNAPSHOT")
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		moduleKind = "umd"
		sourceMap = true
		sourceMapEmbedSources = "always"
	}
}