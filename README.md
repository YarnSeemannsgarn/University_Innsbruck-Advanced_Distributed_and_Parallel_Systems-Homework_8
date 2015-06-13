How to run the programme
========================

Build it first:

	$ mvn clean install
	
Then run it

	$ $HADOOP_HOME/bin/hadoop jar target/map-reduce-povray-0.0.1-SNAPSHOT.jar MapReducePovray.Povray <input-dir> <output-dir>
	
