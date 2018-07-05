import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin") version "1.2.51" apply false
	id("ru.capjack.degos.publish") version "1.5.0" apply false
}

subprojects {
	group = "org.shypl.biser"
	
	repositories {
		maven("http://artifactory.capjack.ru/public")
	}
	
	plugins.withType<KotlinPluginWrapper>() {
		configure<JavaPluginConvention> {
			sourceCompatibility = JavaVersion.VERSION_1_8
		}
		tasks.withType<KotlinCompile> {
			kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
		}
	}
}