killall java
nohup java -Dlog4j.configurationFile=/tmp/log4j2-server.xml -cp "*" com.chat.server.Main -io nonblocking -userrepository database -sqluser admin -sqlpassword admin123 -sqlurl jdbc:mysql://userdb01.cehtpxvzecp2.us-west-2.rds.amazonaws.com:3306/userdb -sqldriver com.mysql.jdbc.Driver &
