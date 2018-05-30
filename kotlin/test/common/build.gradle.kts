val tool = configurations.create("tool")

dependencies {
	tool("org.shypl.biser:biser-compiler:1.3.0-SNAPSHOT")
}

task<JavaExec>("compile") {
	group = "build"
	main = "org.shypl.biser.compiler.Main"
	classpath = tool
}