This repository extends the SciJava script framework to support Groovy-style
dependency grabbing at runtime.

Just put it on your classpath, and your SciJava scripts will gain access
to the following script preprocessing directives:

  #@repository("https://path.to/remote/repository")
  #@dependency("com.mycompany:my-artifact:x.y.z")

You can also grab dependencies via Java code via the `GrabService` API.

Dependencies are cached in `~/.scijava/`.
