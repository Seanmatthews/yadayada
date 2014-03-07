echo "Do you really want to replace the production server?"
echo "(If not, use 'dev-build.sh' instead.)"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) break;;
        No ) exit;;
    esac
done


server=ec2-user@ec2-184-73-141-125.compute-1.amazonaws.com:/tmp


../apis/ant/apache-ant-1.9.2/bin/ant -f server.xml

if [ "$1" = "q" ]
then
   scp /tmp/chatter.jar $server
else
   scp /tmp/chatter.tar $server
fi
