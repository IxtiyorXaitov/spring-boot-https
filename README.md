# 1a. Generate a self-signed SSL certificate

First of all, we need to generate a pair of cryptographic keys, use them to produce an SSL certificate and store it in a
keystore. The keytool documentation defines a keystore as a database of "cryptographic keys, X.509 certificate chains,
and trusted certificates". To enable HTTPS, we'll provide a Spring Boot application with this keystore containing the
SSL certificate.

The two most common formats used for keystores are JKS, a proprietary format specific for Java, and PKCS12, an
industry-standard format. JKS used to be the default choice, but since Java 9 it's PKCS12 the recommended format. We're
going to see how to use both.

### Generate an SSL certificate in a keystore

Let's open our Terminal prompt and write the following command to create a JKS keystore:

```
keytool -genkeypair -alias springboot -keyalg RSA -keysize 4096 -storetype JKS -keystore springboot.jks -validity 3650 -storepass password
```

To create a PKCS12 keystore, and we should, the command is the following:

```
keytool -genkeypair -alias springboot -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore springboot.p12 -validity 3650 -storepass password
```

Let's have a closer look at the command we just run:

* ``` genkeypair: ``` generates a key pair;
* ``` alias: ``` the alias name for the item we are generating;
* ``` keyalg: ``` the cryptographic algorithm to generate the key pair;
* ``` keysize: ``` the size of the key;
* ``` storetype: ``` the type of keystore;
* ``` keystore: ``` the name of the keystore;
* ``` validity: ``` validity number of days;
* ``` storepass: ``` a password for the keystore.

When running the previous command, we will be asked to input some information, but we are free to skip all of it (just press Return to skip an option). When asked if the information is correct, we should type yes. Finally, we hit return to use the keystore password as key password as well.


```` 
What is your first and last name? 
    [Unknown]: 
What is the name of your organizational unit? 
    [Unknown]: 
What is the name of your organization? 
    [Unknown]: 
What is the name of your City or Locality? 
    [Unknown]: 
What is the name of your State or Province? 
    [Unknown]: 
What is the two-letter country code for this unit? 
    [Unknown]: 
Is CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct? 
    [no]: yes 

Enter key password for <springboot> 
    (RETURN if same as keystore password):
````

At the end of this operation, we'll get a keystore containing a brand new SSL certificate.


###Verify the keystore content
To check the content of the keystore following the JKS format, we can use keytool again:

```
keytool -list -v -keystore springboot.jks
```

To test the content of a keystore following the PKCS12 format:

```
keytool -list -v -keystore springboot.p12
```

###Convert a JKS keystore into PKCS12

Should we have already a JKS keystore, we have the option to migrate it to PKCS12; keytool has a convenient command for that:

```
keytool -importkeystore -srckeystore springboot.jks -destkeystore springboot.p12 -deststoretype pkcs12
```

# 1b. Use an existing SSL certificate

In case we have already got an SSL certificate, for example, one issued by Let's Encrypt, we can import it into a keystore and use it to enable HTTPS in a Spring Boot application.

We can use keytool to import our certificate in a new keystore.

```
keytool -import -alias springboot -file myCertificate.crt -keystore springboot.p12 -storepass password
```

To get more information about the keystore and its format, please refer to the previous section.


#2. Enable HTTPS in Spring Boot

Whether our keystore contains a self-signed certificate or one issued by a trusted Certificate Authority, we can now set up Spring Boot to accept requests over HTTPS instead of HTTP by using that certificate.

The first thing to do is placing the keystore file inside the Spring Boot project. For testing purposes, we want to put it in the resources folder or the root folder. In production, you probably want to use a secret management solution to handle the keystore.

Then, we configure the server to use our brand new keystore and enable HTTPS.

###Enable HTTPS in Spring Boot

To enable HTTPS for our Spring Boot application, let's open our application.yml file (or application.properties) and define the following properties:

```
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: pkcs12
    key-alias: springboot
    key-password: password
  port: 8443
```

###Configuring SSL in Spring Boot

Let's have a closer look at the SSL configuration we have just defined in our Spring Boot application properties.

* ```server.port :``` the port on which the server is listening. We have used ```8443``` rather than the default ```8080``` port.
* ```server.ssl.key-store:``` the path to the key store that contains the SSL certificate. In our example, we want Spring Boot to look for it in the classpath.
* ```server.ssl.key-store-password:``` the password used to access the key store.
* ```server.ssl.key-store-type:``` the type of the key store (JKS or PKCS12).
* ```server.ssl.key-alias:``` the alias that identifies the key in the key store.
* ```server.ssl.key-password:``` the password used to access the key in the key store.

###Redirect to HTTPS with Spring Security

When using Spring Security, we can configure it to automatically block any request coming from a non-secure HTTP channel and redirect them to HTTPS.

Let's create a SecurityConfig class to gather the security policies and configure the application to require a secure channel for all requests. We can also temporarily authorize all requests so we can focus on testing the HTTPS behaviour rather than the user authentication strategy.


```
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .requiresChannel(channel -> 
          channel.anyRequest().requiresSecure())
      .authorizeRequests(authorize ->
          authorize.anyRequest().permitAll())
      .build();
    }

}
```

Congratulations! You have successfully enabled HTTPS in your Spring Boot application! Give it a try: run the application, open your browser, visit https://localhost:8443, and check if everything works as it should.

If you're using a self-signed certificate, you will probably get a security warning from the browser and need to authorize it to open the web page anyway.




[Tutorial owner](https://www.thomasvitale.com/https-spring-boot-ssl-certificate/)