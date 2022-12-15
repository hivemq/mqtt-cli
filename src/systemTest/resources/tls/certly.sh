#script generates server- and client- certificates

defaultPass="changeme"
hostname="localhost"
clientName="client"

#hostname input (server CN)
read -p "please input the hostname of the broker:  (default [$hostname]): " hostnameInput
    hostname=${hostnameInput:-$hostname}   # set the default Password (if user skips this entry)
    echo "the hostname is:  $hostname"

#password input
read -p "please input the password you wish to use:  (default [$defaultPass]): " passInput
    defaultPass=${passInput:-$defaultPass}   # set the default Password (if user skips this entry)
    echo "the password is:  $defaultPass"

#client input (client CN)
read -p "please input the CLIENT CN you wish to use:  (default [$clientName]): " cnInput
    clientName=${cnInput:-$clientName}   # set the default client CN (if user skips this entry)
    echo "the password is:  $clientName"

#default file names and passwords
brokerCertName="server"
brokerKeystoreName="broker-keystore"
brokerKeystorePass=$defaultPass
brokerTruststoreName="broker-truststore"
brokerTruststorePass=$defaultPass
clientCertName="client-cert"
clientKeyName="client-key"
clientKeyPass=$defaultPass
clientKeystoreName="client-keystore"
clientKeystorePass=$defaultPass
clientTruststoreName="client-truststore"
clientTruststorePass=$defaultPass

#check the time
time1=$(date '+%H_%M_%S')
#crate output directory
outDirName="certs_$time1"
mkdir $outDirName
#goto output directory
pushd $outDirName

#create new broker .jks keystore
keytool -genkey -keyalg RSA -alias "hivemq" -keystore $brokerKeystoreName.jks -storepass $brokerKeystorePass -validity 36000 -keysize 2048 -dname "CN=$hostname"

#export broker's cert .pem from the keystore
keytool -exportcert -alias "hivemq" -keystore $brokerKeystoreName.jks -rfc -file $brokerCertName.pem -storepass $brokerKeystorePass

#convert broker .pem certificate to .crt
openssl x509 -outform der -in $brokerCertName.pem -out $brokerCertName.crt

#import broker cert. into new client truststore
printf "yes\n" |keytool -import -file $brokerCertName.crt -alias "hivemq" -keystore $clientTruststoreName.jks -storepass $clientTruststorePass

#generate .pem based client certificate and convert to .crt
openssl req -x509 -newkey rsa:2048 -keyout $clientKeyName.pem -out $clientCertName.pem -days 360 -passout pass:$clientKeyPass -subj "/CN=$clientName"
openssl x509 -outform der -in $clientCertName.pem -out $clientCertName.crt

#import client-cert into the broker's truststore
printf "yes\n" |keytool -import -file $clientCertName.crt -alias "client" -keystore $brokerTruststoreName.jks -storepass $brokerTruststorePass

#create client P12 keystore
openssl pkcs12 -export -in $clientCertName.pem -inkey $clientKeyName.pem -certfile $clientCertName.pem -out $clientKeystoreName.p12 -passin pass:$clientKeyPass -passout pass:$clientKeystorePass;

#convert client P12 keystore to JKS keystore
keytool -importkeystore -srckeystore $clientKeystoreName.p12 -srcstoretype pkcs12 -destkeystore $clientKeystoreName.jks  -deststoretype JKS -storepass $clientKeystorePass -srcstorepass $clientKeystorePass 2>/dev/null;

#restore original directory
popd > /dev/null
#where am i?
echo "Certificates saved in the output directory:  $(pwd)/$outDirName"
