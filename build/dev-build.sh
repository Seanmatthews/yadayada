server=ec2-user@ec2-54-198-228-23.compute-1.amazonaws.com:/tmp


../apis/ant/apache-ant-1.9.2/bin/ant -f server.xml

if [ "$1" = "q" ]
then
   scp /tmp/chatter.jar $server
else
   scp /tmp/chatter.tar $server
fi
