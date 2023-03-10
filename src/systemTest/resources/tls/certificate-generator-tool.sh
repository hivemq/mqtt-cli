#script generates server- and client- certificates (Java11)
hostname="localhost"
clientName="client"

#default file names and passwords
caFolder="certificateAuthority"
caCertName="ca"
caKeyPass="caKeyPassword"
caKeystoreName="ca-keystore"
caKeystorePass="caKeystorePassword"

brokerFolder="server"
brokerCertName="server"
brokerKeyPass="serverKeyPassword"
brokerKeystoreName="server-keystore"
brokerKeystorePass="serverKeystorePassword"
brokerTruststoreName="server-truststore"
brokerTruststorePass="serverTruststorePassword"

clientFolder="client"
clientCertName="client-cert"
clientKeyName="client-key"
clientKeyPass="clientKeyPassword"
clientKeystoreName="client-keystore"
clientKeystorePass="clientKeystorePassword"
clientTruststoreName="client-truststore"
clientTruststorePass="clientTruststorePassword"

#check the time
time1=$(date '+%H_%M_%S')
#crate output directory
outDirName="_certs_$time1"
mkdir "$outDirName"
#goto output directory
pushd "$outDirName" || exit

#*****************************************#
#          CERTIFICATE AUTHORITY          #
#*****************************************#
mkdir "$caFolder"
##PKCS#12
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -validity 365000 -keystore $caFolder/$caKeystoreName.p12 -storetype pkcs12 -storepass $caKeystorePass -keypass $caKeyPass -dname "CN=Root-CA" -ext KeyUsage=digitalSignature,keyCertSign -ext BasicConstraints=ca:true,PathLen:3
keytool -exportcert -alias ca -file $caFolder/$caCertName.cer -keystore $caFolder/$caKeystoreName.p12 -storepass $caKeystorePass
keytool -exportcert -alias ca -file $caFolder/$caCertName.pem -keystore $caFolder/$caKeystoreName.p12 -storepass $caKeystorePass -rfc
##JKS
keytool -importkeystore -srckeystore $caFolder/$caKeystoreName.p12 -destkeystore $caFolder/$caKeystoreName.jks -srcstoretype PKCS12 -deststoretype JKS -srcstorepass $caKeystorePass -deststorepass $caKeystorePass -srcalias ca -destalias ca -srckeypass $caKeyPass -destkeypass $caKeyPass -noprompt



#*****************************************#
#                 BROKER                  #
#*****************************************#
mkdir "$brokerFolder"
##PKCS#12
#truststore
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $brokerFolder/$brokerTruststoreName.p12 -storetype pkcs12 -storepass $brokerTruststorePass -noprompt
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $brokerFolder/$brokerTruststoreName.jks -storetype jks -storepass $brokerTruststorePass -noprompt
#keystore
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 365000 -keystore $brokerFolder/$brokerKeystoreName.p12 -storetype pkcs12 -storepass $brokerKeystorePass -keypass $brokerKeyPass -dname "CN=$hostname" -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1
keytool -certreq -alias server -keyalg RSA -file $brokerFolder/$brokerCertName.csr -keystore $brokerFolder/$brokerKeystoreName.p12 -storepass $brokerKeystorePass -dname "CN=$hostname"
keytool -gencert -alias ca -validity 365000 -keystore $caFolder/$caKeystoreName.p12 -storepass $caKeystorePass -infile $brokerFolder/$brokerCertName.csr -outfile $brokerFolder/$brokerCertName.cer -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $brokerFolder/$brokerKeystoreName.p12 -storetype pkcs12 -storepass $brokerKeystorePass -noprompt
keytool -importcert -alias server -file $brokerFolder/$brokerCertName.cer -keystore $brokerFolder/$brokerKeystoreName.p12 -storepass $brokerKeystorePass -noprompt
keytool -delete -alias ca -keystore $brokerFolder/$brokerKeystoreName.p12 -storepass $brokerKeystorePass
keytool -exportcert -alias server -file $brokerFolder/$brokerCertName.pem -keystore $brokerFolder/$brokerKeystoreName.p12 -storepass $brokerKeystorePass -rfc
##JKS
keytool -importkeystore -srckeystore $brokerFolder/$brokerKeystoreName.p12 -destkeystore $brokerFolder/$brokerKeystoreName.jks -srcstoretype PKCS12 -deststoretype JKS -srcstorepass $brokerKeystorePass -deststorepass $brokerKeystorePass -srcalias server -destalias server -srckeypass $brokerKeyPass -destkeypass $brokerKeyPass -noprompt


#*****************************************#
#                 CLIENT                  #
#*****************************************#
mkdir "$clientFolder"
###truststore
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $clientFolder/$clientTruststoreName.p12 -storetype pkcs12 -storepass $clientTruststorePass -noprompt
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $clientFolder/$clientTruststoreName.jks -storetype jks -storepass $clientTruststorePass -noprompt
###keystore
##PKCS#12
keytool -genkeypair -alias $clientName -validity 365000 -keyalg RSA -keysize 2048 -keystore $clientFolder/$clientKeystoreName.p12 -storetype pkcs12 -storepass $clientKeystorePass -keypass $clientKeyPass -dname "CN=$clientName"
keytool -certreq -alias $clientName -keyalg RSA -file $clientFolder/$clientCertName.csr -keystore $clientFolder/$clientKeystoreName.p12 -storepass $clientKeystorePass -dname "CN=$hostname"
keytool -gencert -alias ca -validity 365000 -keystore $caFolder/$caKeystoreName.p12 -storepass $caKeystorePass -infile $clientFolder/$clientCertName.csr -outfile $clientFolder/$clientCertName.cer
keytool -importcert -alias ca -file $caFolder/$caCertName.cer -keystore $clientFolder/$clientKeystoreName.p12 -storetype pkcs12 -storepass $clientKeystorePass -noprompt
keytool -importcert -alias $clientName -file $clientFolder/$clientCertName.cer -keystore $clientFolder/$clientKeystoreName.p12 -storepass $clientKeystorePass -noprompt
keytool -delete -alias ca -keystore $clientFolder/$clientKeystoreName.p12 -storepass $clientKeystorePass
keytool -importkeystore -srckeystore $clientFolder/$clientKeystoreName.p12 -destkeystore $clientFolder/$clientKeystoreName.similar_private_key_password.p12 -srcstoretype PKCS12 -deststoretype PKCS12 -srcstorepass $clientKeystorePass -deststorepass $clientKeystorePass -srcalias $clientName -destalias $clientName -srckeypass $clientKeyPass -destkeypass $clientKeystorePass -noprompt
##JKS
keytool -importkeystore -srckeystore $clientFolder/$clientKeystoreName.p12 -destkeystore $clientFolder/$clientKeystoreName.jks -srcstoretype PKCS12 -deststoretype JKS -srcstorepass $clientKeystorePass -deststorepass $clientKeystorePass -srcalias $clientName -destalias $clientName -srckeypass $clientKeyPass -destkeypass $clientKeyPass -noprompt
keytool -importkeystore -srckeystore $clientFolder/$clientKeystoreName.p12 -destkeystore $clientFolder/$clientKeystoreName.similar_private_key_password.jks -srcstoretype PKCS12 -deststoretype JKS -srcstorepass $clientKeystorePass -deststorepass $clientKeystorePass -srcalias $clientName -destalias $clientName -srckeypass $clientKeyPass -destkeypass $clientKeystorePass -noprompt

###standalone
##CERTIFICATE
openssl pkcs12 -nokeys -clcerts -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientCertName.pem -passin pass:$clientKeystorePass

##PRIVATE KEY
#PKCS#8 - PEM
openssl pkcs12 -nocerts -des3 -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientKeyName.pkcs8.des3.pem -passin pass:$clientKeystorePass -passout pass:$clientKeyPass
openssl pkcs12 -nocerts -des -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientKeyName.pkcs8.des.pem -passin pass:$clientKeystorePass -passout pass:$clientKeyPass
openssl pkcs12 -nocerts -aes256 -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientKeyName.pkcs8.aes256.pem -passin pass:$clientKeystorePass -passout pass:$clientKeyPass
openssl pkcs12 -nocerts -camellia256 -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientKeyName.pkcs8.camellia256.pem -passin pass:$clientKeystorePass -passout pass:$clientKeyPass
openssl pkcs12 -nocerts -nodes -in $clientFolder/$clientKeystoreName.p12 -out $clientFolder/$clientKeyName.pkcs8.unencrypted.pem -passin pass:$clientKeystorePass -passout pass:$clientKeyPass
#PKCS#8 - DER
openssl pkcs8 -topk8 -inform pem -outform der -v2 des3 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs8.des3.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl pkcs8 -topk8 -inform pem -outform der -v2 des -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs8.des.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl pkcs8 -topk8 -inform pem -outform der -v2 aes256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs8.aes256.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl pkcs8 -topk8 -inform pem -outform der -v2 camellia256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs8.camellia256.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl pkcs8 -topk8 -inform pem -outform der -nocrypt -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs8.unencrypted.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
#PKCS#1/RSA - PEM
openssl rsa -inform pem -outform pem -des3 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.des3.pem -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform pem -des -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.des.pem -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform pem -aes256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.aes256.pem -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform pem -camellia256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.camellia256.pem -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform pem -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.unencrypted.pem -passin pass:$clientKeyPass -passout pass:$clientKeyPass
#PKCS#1/RSA - DER
openssl rsa -inform pem -outform der -des3 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.des3.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform der -des -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.des.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform der -aes256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.aes256.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform der -camellia256 -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.camellia256.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass
openssl rsa -inform pem -outform der -in $clientFolder/$clientKeyName.pkcs8.des3.pem -out $clientFolder/$clientKeyName.pkcs1.unencrypted.der -passin pass:$clientKeyPass -passout pass:$clientKeyPass

#restore original directory
popd >/dev/null || exit
#where am i?
echo "Certificates saved in the output directory: $(pwd)/$outDirName"
