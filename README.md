The programme allows to render [povray](http://www.povray.org/) files on a [hadoop](http://hadoop.apache.org/) cluster.

The programme is splitted into two subprogrammes:

1. map-reduce-povray: contains the the implementation of the Mapper and the Reducer
1. map-reduce-povray-ui: contains a gui implementation to start a MapReduce job in an AWS Elastic MapReduce (EMR) cluster.

How to run the subprogrammes
============================

First of all build all projects:

	$ mvn clean install

The map-reduce-povray programme can be executed on a local hadoop cluster via:

	$ $HADOOP_HOME/bin/hadoop jar map-reduce-povray/target/map-reduce-povray-1.0.jar mapReducePovray.Povray <input-dir> <output-dir> <uri-of-pov-file>

The programme can also be executed on EMR. Therefore the EMR cluster must be created, so that the jar can be uploaded. The pov-file, which should be rendered, can be uploaded e.g. to a S3 bucket.

The map-reduce-povray-ui gui programme can be executed via:

	$ cd map-reduce-povray-ui
	$ mvn exec:java
