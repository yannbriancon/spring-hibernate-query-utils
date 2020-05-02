<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]


<!-- PROJECT LOGO -->
<br />
<p align="center">

  <h3 align="center">Spring Hibernate Query Utils</h3>

  <p align="center">
    <b>No more N+1 queries in your Spring applications</b>
    <br />
    Spring Hibernate Query Utils: an easy way of detecting N+1 queries and counting queries in a Spring/Hibernate application 
    <br />
    <br />
    <a href="https://github.com/yannbriancon/spring-hibernate-query-count/issues">Report Bug</a>
    ·
    <a href="https://github.com/yannbriancon/spring-hibernate-query-count/issues">Request Feature</a>
  </p>
</p>



<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)
  * [N+1 Query Detection](#n1-query-detection)
    * [Detection](#detection)
    * [Configuration](#configuration)
  * [Query Count](#query-count)
* [License](#license)
* [Contact](#contact)



<!-- ABOUT THE PROJECT -->
## About The Project

While investigating the performance problems in my SpringBoot application, I discovered the infamous N+1 queries problem (more details on this problem [here](https://medium.com/@mansoor_ali/hibernate-n-1-queries-problem-8a926b69f618)) that was killing the performance of my services.

After managing to fix this problem, I had to find a way to detect it and raise the alarm to avoid any developer to introduce new ones.

That is why I created Spring Hibernate Query Utils to provide an easy way of detecting N+1 queries and counting the queries generated in a Spring application using Hibernate.

If you develop Spring applications using Hibernate, you have probably also encountered performance issues caused by N+1 queries.

This library provides several benefits:

* Kill the N+1 queries problem by throwing an exception when detecting it in your tests
* Count the exact number of queries generated for each service or resource
* Improve the onboarding of new developers by making them understand the impact of the N+1 queries problem
* Improve the debugging by seeing which query is executed and when


<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

* JDK 8 or more.  

### Installation
##### Maven

Add the dependency to your project inside your `pom.xml` file
```xml
<dependency>
    <groupId>com.yannbriancon</groupId>
    <artifactId>spring-hibernate-query-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```


<!-- USAGE -->
## Usage

### N+1 Query Detection

#### Detection

The N+1 query detection is set up by default.

Each time a N+1 query is detected in a transaction, a log of level error will be sent.

Here is an example:


#### Configuration

By default the detection of a N+1 query logs an error to avoid breaking your code. 

However, my advise is to override the default error level to throw exceptions for your test profile. Then you 
will easily detect which tests are failing and be able to flag them and set the error level to error logs only on 
those tests.

To do this, you can configure the error level when a N+1 query is detected using the property `hibernate.query.interceptor.error-level`. 

4 levels are available to handle the detection of N+1 queries:

* **INFO**: Log a message of level info
* **WARN**: Log a message of level warn
* **ERROR** (default): Log a message of level error
* **EXCEPTION**: Throw a NPlusOneQueryException

Here are two examples on how to use it globally or for a specific test:

* In application.properties:
```yaml
hibernate.query.interceptor.error-level=INFO
```

* In tests:
```java
@SpringBootTest("hibernate.query.interceptor.error-level=INFO")
@Transactional
class NPlusOneQueryLoggingTest {
    ...
}
```

### Query Count

To start counting the generated queries, you need to instantiate a **HibernateQueryInterceptor**.

Three methods are available:
* startQueryCount: Initializes the query count to 0 and allows the queries to increment the count.
* getQueryCount: Returns the current query count for the Thread concerned as a Long.

The count is local to a Thread. This choice was made to have a consistent count for a running application and avoid other threads to alter the count.

Example in a test:

```java
...
import com.yannbriancon.interceptor.HibernateQueryInterceptor;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class NotificationResourceIntTest {
    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    public void saveFile_isOk() throws Exception {
        // Initialize the query to 0 and allow the counting
        hibernateQueryInterceptor.startQueryCount();

        // Call the resource that we want to test
        MvcResult result = mvc.perform(get("/rest/notifications"))
                .andExpect(status().isOk())
                .andReturn();

        // Get the query count for this thread and check that it is equal to the number of query you expect, let's say 4.
        // The count is checked and we detect potential n+1 queries.
        Assertions.assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(4);
    }
}
```



<!-- LICENSE -->
## License

Distributed under the MIT License. See [`LICENSE`][license-url] for more information.



<!-- CONTACT -->
## Contact

[@YBriancon](https://twitter.com/YBriancon) - yann.briancon.73@gmail.com

Project Link: [https://github.com/yannbriancon/spring-hibernate-query-count](https://github.com/yannbriancon/spring-hibernate-query-count)


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/yannbriancon/spring-hibernate-query-count.svg?style=flat-square
[contributors-url]: https://github.com/yannbriancon/spring-hibernate-query-count/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/yannbriancon/spring-hibernate-query-count.svg?style=flat-square
[forks-url]: https://github.com/yannbriancon/spring-hibernate-query-count/network/members
[stars-shield]: https://img.shields.io/github/stars/yannbriancon/spring-hibernate-query-count.svg?style=flat-square
[stars-url]: https://github.com/yannbriancon/spring-hibernate-query-count/stargazers
[issues-shield]: https://img.shields.io/github/issues/yannbriancon/spring-hibernate-query-count.svg?style=flat-square
[issues-url]: https://github.com/yannbriancon/spring-hibernate-query-count/issues
[license-shield]: https://img.shields.io/github/license/yannbriancon/spring-hibernate-query-count.svg?style=flat-square
[license-url]: https://github.com/yannbriancon/spring-hibernate-query-count/blob/master/LICENSE
