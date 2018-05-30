import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.shypl.biser:biser-csi-java:1.3.0-SNAPSHOT")
	implementation("org.shypl.biser:biser-csi-netty:1.1.0-SNAPSHOT")
	implementation("ch.qos.logback:logback-classic:1.1.7")
}

configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}