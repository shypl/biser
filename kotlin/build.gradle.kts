import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin") version "1.2.41" apply false
	id("ru.capjack.degos.publish") version "1.5.0" apply false
}

subprojects {
	group = "org.shypl.biser"
	version = "1.0.0-SNAPSHOT"
	
	repositories {
		mavenLocal()
		maven("http://artifactory.capjack.ru/public")
	}
	
	plugins.withId("java") {
		configure<JavaPluginConvention> {
			sourceCompatibility = JavaVersion.VERSION_1_8
		}
		tasks.withType<KotlinCompile> {
			kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
		}
	}
}