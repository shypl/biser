val tool = configurations.create("tool")

dependencies {
	tool(project(":biser-compiler"))
}

task<JavaExec>("compile") {
	group = "build"
	main = "org.shypl.biser.compiler.Main"
	classpath = tool
}