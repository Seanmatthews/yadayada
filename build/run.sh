killall java
nohup java -Dlog4j.configurationFile=/tmp/log4j2.xml -Djavax.net.ssl.keyStoreType="pkcs12" -Djavax.net.ssl.keyStore=/tmp/yadayada-cert.p12 -Djavax.net.ssl.keyStorePassword="x" -cp "*" com.chat.server.Main -userrepository memory &
#nohup java -Dlog4j.configurationFile=/tmp/log4j2.xml -cp "*" com.chat.image.S3ImageUploader 5001 yadayadaicons AKIAJUTW5SWKURDPMM3Q jS+vITJ7CzMMY/2Al1U78BfPywYqdOTQCLbcwJ0k &
