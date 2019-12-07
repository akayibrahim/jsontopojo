# JSON TO POJO

#### This Maven Plugin is for creating POJO's automatically from json.

##### Follow steps:

1. Add below build tag to pom.xml of project

```xml
<build>
    <plugins>
      <plugin>
        <groupId>com.iakay.jsontopojo</groupId>
        <artifactId>jsontopojo</artifactId>
        <version>1.4-SNAPSHOT</version>
        <executions>
          <execution>
            <configuration>
              <json>
                {
                  "employee": {
                  "name":       "sonoo",
                  "salary":      56000,
                  "married":    true
                  }
                }
              </json>
              <path>com.iakay.jsontopojotest.testing</path>
              <className>EmployeeTesting</className>
            </configuration>
            <goals>
              <goal>create</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```
2. Run mvn clean install. Once the build is complete, navigate to the project root folder and you should see POJO's file.


Thanks to https://github.com/giordamauro/json-to-pojo/blob/master/json-to-pojo/src/main/java/com/mgiorda/JsonToPojo.java


