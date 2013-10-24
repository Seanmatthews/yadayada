ant -f build-server.xml
scp ../apis/mysql/mysql-connector-java-5.1.26/mysql-connector-java-5.1.26-bin.jar ec2-user@ec2-54-200-207-138.us-west-2.compute.amazonaws.com:/tmp
scp out/chatter.jar ec2-user@ec2-54-200-207-138.us-west-2.compute.amazonaws.com:/tmp
