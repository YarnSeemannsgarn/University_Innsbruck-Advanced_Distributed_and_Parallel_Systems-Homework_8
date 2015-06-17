The programme allows to render [Povray](http://www.povray.org/) files on a [Hadoop](http://hadoop.apache.org/) cluster.

The programme is splitted into two subprogrammes:

1. map-reduce-povray: contains the the implementation of the Mapper and the Reducer
1. map-reduce-povray-ui: contains a gui implementation to start a MapReduce job in an AWS Elastic MapReduce (EMR) cluster.

How to run the subprogrammes
============================

First of all build all projects:

	$ mvn clean install

The map-reduce-povray programme can be executed on a local hadoop cluster via:

	$ $HADOOP_HOME/bin/hadoop jar map-reduce-povray/target/map-reduce-povray-1.0.jar mapReducePovray.Povray <input-dir> <output-dir> <uri-of-pov-file>

The programme can also be executed on Amazon EMR. A small user interface for using it via EMR is available in the project _map-reduce-povray-ui_.
Required preparations before running the user interface (applies to both the command line and GUI version):
* Create an EMR cluster, note the cluster ID.
* Create an S3 bucket and upload the jar-file containing the map-reduce implementation
* Create access credentials which have permissions to create and monitor steps on the cluster and can list and edit files on the S3 bucket. Store them in a properties file called "AwsCredentials.properties" as entries named _accessKey_ and _secretKey_.

The map-reduce-povray-ui gui programme can be executed via:

	$ cd map-reduce-povray-ui
	$ mvn exec:java
