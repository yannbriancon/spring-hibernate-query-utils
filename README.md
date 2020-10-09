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
    <a href="https://github.com/yannbriancon/spring-hibernate-query-utils/issues">Report Bug</a>
    ·
    <a href="https://github.com/yannbriancon/spring-hibernate-query-utils/issues">Request Feature</a>
  </p>
</p>



<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)
  * [N+1 Queries Detection](#n1-queries-detection)
    * [Detection](#detection)
      * [Detection in test with fixtures](#detection-in-test-with-fixtures)
    * [Configuration](#configuration)
      * [Enable](#enable)
      * [Error level](#error-level)
  * [Query Count](#query-count)
* [Changelog](#changelog)
* [License](#license)
* [Contact](#contact)



<!-- ABOUT THE PROJECT -->
## About The Project

While investigating the performance problems in my SpringBoot application, I discovered the infamous N+1 queries 
problem that was killing the performance of my services. 
Check the article [Eliminate Spring Hibernate N+1 Queries](https://medium.com/sipios/eliminate-spring-hibernate-n-plus-1-queries-f0bcf6a83de2?source=friends_link&sk=5ba0f2493af1d8496a46d5f116effa96) for more details.

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

Add the dependency to your project inside your `pom.xml` file with the right version
```xml
<dependency>
    <groupId>com.yannbriancon</groupId>
    <artifactId>spring-hibernate-query-utils</artifactId>
    <version>X.X.X</version>
</dependency>
```


<!-- USAGE -->
## Usage

### N+1 Queries Detection

#### Detection

The N+1 queries detection is enabled by default so no configuration is needed.

Each time N+1 queries are detected in a transaction, a log of level error will be sent.

Two types of N+1 queries are detected:

- N+1 queries triggered on a getter caused by a field needed but not eager fetched on a specific query

- N+1 queries triggered on a query caused by an entity field not configured to be fetched lazily

Here is an example catching the error log for the first type of N+1 queries:

```java
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@Transactional
class NPlusOneQueriesLoggingTest {

    @Autowired
    private MessageRepository messageRepository;

    @Mock
    private Appender mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @BeforeEach
    public void setup() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
    }

    @Test
    void hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingEagerFetchingOnQuery() {
        // Fetch the 2 messages without the authors
        List<Message> messages = messageRepository.findAll();

        // The getters trigger N+1 queries
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());

        verify(mockedAppender, times(2)).doAppend(loggingEventCaptor.capture());

        LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
        assertThat(loggingEvent.getMessage())
                .contains("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                        "    at com.yannbriancon.interceptor.NPlusOneQueriesLoggingTest." +
                        "lambda$hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingEagerFetchingOnQuery$0");
        assertThat(Level.ERROR).isEqualTo(loggingEvent.getLevel());
    }
}
```

##### Detection in test with fixtures

If a setup is present in your test to add the data necessary for testing, Hibernate will load all the data in its 
state. This will hide potential N+1 queries in the method you test.

To avoid this, a method is available to clear the Hibernate state and the N+1 queries detection state.

Here is an example:

```java
@Test
void nPlusOneQueriesDetection_throwsExceptionWhenSessionIsCleared() {
    User author = new User("author");
    userRepository.saveAndFlush(author);
    Message newMessage = new Message("text", author);
    messageRepository.saveAndFlush(newMessage);

    // Test a method that should return a N+1 query
    // The method does not return an exception because we just created the message so it is loaded in the Session
    getMessageAuthorNameWithNPlusOneQuery(newMessage.getId());

    // Clear the session to be able to correctly detect the N+1 queries in the tests
    hibernateQueryInterceptor.clearNPlusOneQuerySession(entityManager);

    try {
        // Test a method that should return a N+1 query
        // This time the Session is empty and the N+1 query is detected
        getMessageAuthorNameWithNPlusOneQuery(newMessage.getId());
        assert false;
    } catch (NPlusOneQueriesException exception) {
        assertThat(exception.getMessage())
                .contains("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                        "    at com.yannbriancon.interceptor.NPlusOneQueriesExceptionTest" +
                        ".getMessageAuthorNameWithNPlusOneQuery");
    }
}
```

#### Configuration

##### Enable

By default the detection of N+1 queries is enabled for every profile.

To disable it, you can set the property `spring-hibernate-query-utils.n-plus-one-queries-detection.error-level` to false.


##### Error level

By default the detection of N+1 queries logs an error to avoid breaking your code. 

However, my advice is to override the default error level to throw exceptions for your test profile. 

Now you will easily detect which tests are failing and be able to flag them and set the error level to error logs only on 
those tests while you are fixing them.

To do this, you can configure the error level when N+1 queries is detected using the property `spring-hibernate-query-utils.n-plus-one-queries-detection.error-level`. 

4 levels are available to handle the detection of N+1 queries:

* **INFO**: Log a message of level info
* **WARN**: Log a message of level warn
* **ERROR** (default): Log a message of level error
* **EXCEPTION**: Throw a NPlusOneQueriesException

Here are two examples on how to use it globally or for a specific test:

* In application.properties:
```yaml
hibernate.query.interceptor.error-level=INFO
```

* In tests:
```java
@SpringBootTest("hibernate.query.interceptor.error-level=INFO")
@Transactional
class NPlusOneQueriesLoggingTest {
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
    public void getNotification_isOk() throws Exception {
        // Initialize the query to 0 and allow the counting
        hibernateQueryInterceptor.startQueryCount();

        // Call the resource that we want to test
        MvcResult result = mvc.perform(get("/rest/notifications"))
                .andExpect(status().isOk())
                .andReturn();

        // Get the query count for this thread and check that it is equal to the number of query you expect,
        // Let's say 4 for the example.
        Assertions.assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(4);
    }
}
```

<!-- CHANGELOG -->
## Changelog

See [`CHANGELOG`][changelog-url] for more information.

<!-- CONTRIBUTING -->
## Contributing

See [`CONTRIBUTING`][contributing] for more information.


<!-- LICENSE -->
## License

Distributed under the MIT License. See [`LICENSE`][license-url] for more information.



<!-- CONTACT -->
## Contact

[@YBriancon](https://twitter.com/YBriancon) - yann.briancon.73@gmail.com

Project Link: [https://github.com/yannbriancon/spring-hibernate-query-utils](https://github.com/yannbriancon/spring-hibernate-query-utils)


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/yannbriancon/spring-hibernate-query-utils.svg?style=flat-square
[contributors-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/yannbriancon/spring-hibernate-query-utils.svg?style=flat-square
[forks-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/network/members
[stars-shield]: https://img.shields.io/github/stars/yannbriancon/spring-hibernate-query-utils.svg?style=flat-square
[stars-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/stargazers
[issues-shield]: https://img.shields.io/github/issues/yannbriancon/spring-hibernate-query-utils.svg?style=flat-square
[issues-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/issues
[license-shield]: https://img.shields.io/github/license/yannbriancon/spring-hibernate-query-utils.svg?style=flat-square
[license-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/blob/master/LICENSE
[changelog-url]: https://github.com/yannbriancon/spring-hibernate-query-utils/blob/master/CHANGELOG
[contributing]: https://github.com/yannbriancon/spring-hibernate-query-utils/blob/master/CONTRIBUTING
