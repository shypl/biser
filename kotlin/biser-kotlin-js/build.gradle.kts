import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

version = "1.1.3"

plugins {
	id("kotlin2js")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.ktjs:ktjs-common:0.10.0")
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		moduleKind = "umd"
		sourceMap = true
		sourceMapEmbedSources = "always"
	}
}