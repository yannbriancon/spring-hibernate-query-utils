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

  <h3 align="center">Spring Hibernate Query Count</h3>

  <p align="center">
    <b>No more N+1 queries in your Spring applications</b>
    <br />
    Spring Hibernate Query Count: an easy way of counting queries in a Spring/Hibernate application 
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
* [License](#license)
* [Contact](#contact)



<!-- ABOUT THE PROJECT -->
## About The Project

While investigation the performance problem in my SpringBoot application, I discovered the infamous N+1 queries problem (more details on this problem [here](https://medium.com/@mansoor_ali/hibernate-n-1-queries-problem-8a926b69f618)) that was killing the performance of my services.

After managing to fix this problem, I had to find a way to detect it and raise the alarm to anoid any new service with this problem.

That is why I created Spring Hibernate Query Count to provide an easy way of counting the queries generated in a Spring application using Hibernate.

If you develop Spring applications using Hibernate, you have probably also encountered performance issues caused by n+1 queries.

This library provides several benefits:

* Counting the exact number of queries generated for each service or resource
* Detecting the n+1 queries problem in your Spring tests and raise the alarm before having performance issues in production
* Avoiding performance regressions by adding an assert on the query count in your integration tests
* Improving the onboarding of new developers by showing them the impact of the n+1 queries problem
* Improve the debugging by seeing which query is executed and when



<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

* JDK 8 or more.  

### Installation
##### Maven

Add dependency to your project inside your `pom.xml` file
```xml
<dependency>
    <groupId>com.yannbriancon</groupId>
    <artifactId>spring-hibernate-query-count</artifactId>
    <version>1.0.0</version>
</dependency>
```


<!-- USAGE -->
## Usage

Now that you added the dependency, you can instantiate a HibernateQueryCountInterceptor object and start to use it.

Three methods are available:
* startCounter: Initializes the query count to 0 and allows the queries to increment the count.
* getQueryCount: Returns the current query count for the Thread concerned as a Long.
* removeCounter: Removes the instance of the queryCount that is a ThreadLocal<Long> and stops the counting.

The count is local to a Thread. This choice was made to have a consistent count for a running application and avoid other threads to alter the count.

Example in a test:

```java
...
import com.yannbriancon.interceptor.HibernateQueryCountInterceptor;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class NotificationResourceIntTest {
    @Autowired
    private HibernateQueryCountInterceptor hibernateQueryCountInterceptor;

    @Test
    public void saveFile_isOk() throws Exception {
        // Initialize the query to 0 and allow the counting
        hibernateQueryCountInterceptor.startCounter();

        // Call the resource that we want to test
        MvcResult result = mvc.perform(get("/rest/notifications"))
                .andExpect(status().isOk())
                .andReturn();

        // Get the query count for this thread and check that it is equal to the number of query you expect, let's say 4.
        // The count is checked and we detect potential n+1 queries.
        Assertions.assertThat(hibernateQueryCountInterceptor.getQueryCount()).isEqualTo(4);
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
