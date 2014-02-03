killall java
#nohup java -Dlog4j.configurationFile=/tmp/log4j2-server.xml -cp "*" com.chat.server.Main -io nonblocking -userrepository database -sqluser admin -sqlpassword admin123 -sqlurl jdbc:mysql://userdb01.cehtpxvzecp2.us-west-2.rds.amazonaws.com:3306/userdb -sqldriver com.mysql.jdbc.Driver &

nohup java -Dlog4j.configurationFile=/tmp/log4j2.xml -cp "*" com.chat.server.Main -userrepository memory &
nohup java -Dlog4j.configurationFile=/tmp/log4j2.xml -cp "*" com.chat.image.S3ImageUploader 5001 yadayadaicons AKIAJUTW5SWKURDPMM3Q jS+vITJ7CzMMY/2Al1U78BfPywYqdOTQCLbcwJ0k &
