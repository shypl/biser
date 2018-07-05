import org.gradle.jvm.tasks.Jar

version = "1.4.0-SNAPSHOT"

plugins {
	kotlin("jvm")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.apache.commons:commons-lang3:3.4")
	implementation("org.shypl.common:common-java:1.0.5-SNAPSHOT")
}

tasks.withType<Jar> {
	manifest {
		attributes["Main-Class"] = "org.shypl.biser.compiler.Main"
	}
	from(
		configurations.runtimeClasspath.map { if(it.isDirectory()) it else zipTree(it) }
	)
}