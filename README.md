How to run the programmes
=========================

First of all build all projects:

	$ mvn clean install
	

The map-reduce-povray programme can be executed locally via:

	$ $HADOOP_HOME/bin/hadoop jar map-reduce-povray/target/map-reduce-povray-1.0.jar mapReducePovray.Povray <input-dir> <output-dir> <uri-of-pov-file>
	
The map-reduce-povray-ui gui programme can be executed locally via:

	$ cd map-reduce-povray-ui
	$ mvn exec:java
