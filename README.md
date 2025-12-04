[![Java CI with Maven](https://github.com/42BV/database-truncator/actions/workflows/maven.yml/badge.svg)](https://github.com/42BV/database-truncator/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e1e75377df7e465593f545911b8ccbbc)](https://app.codacy.com/gh/42BV/database-truncator/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![codecov](https://codecov.io/gh/42BV/database-truncator/graph/badge.svg?token=tAJHnd8opC)](https://codecov.io/gh/42BV/database-truncator)
[![Maven Central](https://img.shields.io/maven-central/v/nl.42/database-truncator.svg?color=green)](https://central.sonatype.com/artifact/nl.42/database-truncator)
[![Javadocs](https://www.javadoc.io/badge2/nl.42/database-truncator/javadoc.svg)](https://www.javadoc.io/doc/nl.42/database-truncator)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Database Truncator

If you have chosen (as we did) not to rely on the rollback of @Transactional to run your unit tests, you will find that your database needs to be cleaned after every individual unit test. A convoluted way to do this is to empty the tables one-by-one, in the right order. As anyone who has been down this road can tell you, this way madness lies. Much better is to make use of the truncation functionality a database has to offer. Truncation removes the data, but leaves the structure in place.
 
This library helps you to clean up your test data after every individual test, so that the next test can rely on a clean start.

Usage is simple. Just import the dependency and make sure your top-level abstract Spring Context test class calls the truncator in a @Before or @After call.

## Maven Dependency

```xml
<dependency>
    <groupId>nl.42</groupId>
    <artifactId>database-truncator</artifactId>
    <version>4.0.0</version>
</dependency>
```

## How it works

Database Truncator follows the lead of the platform, as registered in ```spring.datasource.platform```. It will create the right ```DatabaseTruncator``` class belonging to the platform. If you have already created your own truncator, it will not interfere and give you right of way.

```yaml
spring:
  datasource:
    platform: postgresql
```

You can exclude tables from truncation by adding the following:

```yaml
database-truncator:
  exclude:
    - some_table
    - "*view"
```

The logic uses Spring's AntPathMatcher to determine matches. Matches are removed from the table truncation list.

## Code sample

The top-level abstract class which ties your tests to your Spring context can be fitted to make use of the truncator. You can inject the truncator and then use an ```@After``` method to make sure it is called after every unit test. Example usage:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @Inject
    private DatabaseTruncator truncator;

    @After
    public void clearAll() throws Exception {
        truncator.truncate();
    }

}
```

## Supported databases
The number of supported database is currently limited to:
* hsqldb
* h2
* postgresql
