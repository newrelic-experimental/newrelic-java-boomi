<a href="https://opensource.newrelic.com/oss-category/#new-relic-experimental"><picture><source media="(prefers-color-scheme: dark)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/dark/Experimental.png"><source media="(prefers-color-scheme: light)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"><img alt="New Relic Open Source experimental project banner." src="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"></picture></a>


![GitHub forks](https://img.shields.io/github/forks/newrelic-experimental/newrelic-java-boomi?style=social)
![GitHub stars](https://img.shields.io/github/stars/newrelic-experimental/newrelic-java-boomi?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/newrelic-experimental/newrelic-java-boomi?style=social)

![GitHub all releases](https://img.shields.io/github/downloads/newrelic-experimental/newrelic-java-boomi/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/newrelic-experimental/newrelic-java-boomi)
![GitHub last commit](https://img.shields.io/github/last-commit/newrelic-experimental/newrelic-java-boomi)
![GitHub Release Date](https://img.shields.io/github/release-date/newrelic-experimental/newrelic-java-boomi)


![GitHub issues](https://img.shields.io/github/issues/newrelic-experimental/newrelic-java-boomi)
![GitHub issues closed](https://img.shields.io/github/issues-closed/newrelic-experimental/newrelic-java-boomi)
![GitHub pull requests](https://img.shields.io/github/issues-pr/newrelic-experimental/newrelic-java-boomi)
![GitHub pull requests closed](https://img.shields.io/github/issues-pr-closed/newrelic-experimental/newrelic-java-boomi)


# New Relic Java Instrumentation for Boomi

Provides visibility into the Boomi Application   

  
## Installation

See Release Notes Below

## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

>We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.

## Contributing

We encourage your contributions to improve [Project Name]! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

[Project Name] is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

>[If applicable: [Project Name] also uses source code from third-party libraries. You can find full details on which libraries are used and the terms under which they are licensed in the third-party notices document.]

## Release Notes :
  
### r1.0 : 04 APR 2023

This release carrying the instrumentation binaries for Boomi runtime 

### What are the componets where new relic agent is required to be deployed

The '**Atom Node**' JVM which handles the general management and housekeeping for the node.  Not responsible for any actual data integration processing.  This JVM does currently have a New Relic Agent associated with it.
Currently the Atom Cloud telemetry data that is provided to the New Relic platform comes from the 'Atom Node' JVM.  This is limited to higher level information about the Atom Cloud (e.g., number of processes running, number of attached sub-accounts, average execution time, queued executions, cluster issues).
This is essentially the monitoring that our own Boomi operations team relies on with respect to the Boomi Public and Private Managed Atom Clouds.  We monitor the cloud as a whole, versus drill down too far into individual processes and process connections.


The '**Atom Worker**' JVMs which handle real-time, web service integration processes.  These are spun up by the procworker.sh shell script and run for 24 hours.  And then continually renewed for another 24 hours. Each Atom Worker JVM sits there and services multiple requests simultaneously (configurable).
This is what SMEs are focused on.  It looks very promising in terms of exposing telemetry data related to real-time processing.  Including, potentially, the ability to 'see inside' the process.  We have verified that Customers uses Atom Workers extensively.  We counted as many as 26 Atom Workers attached to one of customer's production clouds.  That cloud has 10 nodes.  So it would be a matter of attaching a New Relic Agent to each Atom Worker instance.

So, '**Atom Node**' and '**Atom Worker**' where New Relic Agent needs to be deployed.


### STEP1 : Deployment at '**Atom Node**'

#### Apply Java agent and give App Name

Look for atom.vmoptions in your atom node installation and update javaagent [ _The absolute path must point to your newrelic.jar within the agent installation location_ ] and app name with sudo privileges 
eg. /opt/Boomi_AtomSphere/Cloud/Cloud_atom_cloud_01/bin/atom.vmoptions and add the following options with pre-existing parameters

-Dnewrelic.config.app_name=BOOMI_EU_PROD_ATOM
-javaagent:/home/ec2-user/nr_agent/newrelic/newrelic.jar

### STEP 2: Deployment at '**Atom Worker**'

#### Apply Java agent and give App Name

Look for procworker.sh in your atom node installation and carefully update javaagent and app name with sudo privileges 
eg. /opt/Boomi_AtomSphere/Cloud/Cloud_atom_cloud_01/bin/procworker.sh and add the following options with pre-existing parameters


-Dnewrelic.config.app_name=BOOMI_EU_PROD__WORKER
-javaagent:/home/ec2-user/nr_agent/newrelic/newrelic.jar

### STEP 3: Update the existing newrelic agent installation with following instrumented jars under extensions folder

Add the following jar files from [Release bundle](https://github.com/newrelic-experimental/newrelic-java-boomi/releases/download/r1.0/newrelic-java-boomi-r1.0.zip) 

#### Create extensions folder [ if the folder doesn't exist] 
     E.g. /home/ec2-user/nr_agent/newrelic/extensions

#### boomi-container-execution.jar	
#### kafka-clients.jar
#### executors-10.jar
#### executors-17.jar	
#### rmi-stubs.jar
#### rmi.jar
#### executors-8.jar			
#### smallrye-mutiny.jar
#### executors-9.jar

### STEP 4: Restart all the nodes

### STEP 5: Verify the New Relic Platform and check for both ATOM and Worker applications
