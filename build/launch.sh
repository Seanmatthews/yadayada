../apis/ant/apache-ant-1.9.2/bin/ant -f build-server.xml

if [ "$1" = "q" ]
then
   scp /tmp/chatter.jar ec2-user@ec2-54-200-202-37.us-west-2.compute.amazonaws.com:/tmp
else
   scp /tmp/chatter.tar ec2-user@ec2-54-200-202-37.us-west-2.compute.amazonaws.com:/tmp
fi
