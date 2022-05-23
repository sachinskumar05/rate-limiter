# Getting Started

##Requirements

1) Requests submitted when a client is not connected will be rejected with a Status of ERROR

2) Requests can be rejected with a status of THROTTLED if they are submitted at higher than a configurable frequency 
<br>Implement the validation step that tests if a request was submitted inside the allowed frequency.
<br>The allowed frequency is defined by the number of requests, N, submitted inside the time interval of S seconds.

3) Once a client has connected, there is an expectation that they will submit at least one request every M seconds
   <br>If a connected client does not submit a request within the M second interval, that should be logged as an error.

4) Any client request received by the gateway should be passed to an implementation of the RequestProcessor. It is sufficient to provide an implementation of the RequestProcessor that logs the arguments passed.

5) Your implementation should provide the variables, N, S and M as constructor arguments (you can call the arguments whatever you deem sensible)

6) Expected frequencies for client requests are on the order of 1/sec, and a client can be considered timed out if no message is received for ~3 secs.

7) Aside from logging and unit testing, it should be possible to implement the above using the core java libraries.

---------------------------------------------

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.0/maven-plugin/reference/html/#build-image)

