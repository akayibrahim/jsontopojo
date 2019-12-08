# JSON TO POJO Maven Plugin

#### This Maven Plugin is for creating POJO's automatically from json.

##### Follow steps:

1. Add below build tag to pom.xml of your project

```xml
<build>
    <plugins>
      <plugin>
        <groupId>io.githup.akayibrahim</groupId>
        <artifactId>jsontopojo</artifactId>
        <version>1.0-SNAPSHOT</version>
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
              <path>io.githup.akayibrahim.testing</path>
              <className>EmployeeTesting</className>
              <!-- optional <lombok>true</lombok> -->
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

NOTE : if project can not download from maven repository, you can download jar from this [link](https://github.com/akayibrahim/jsontopojo/raw/master/lib/jsontopojo-1.0-SNAPSHOT.jar "link").

NOTE : For using lombok, you have to add lombok dependency to your pom.xml.
```xml    
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.10</version>
    </dependency>
```
Thanks to https://github.com/giordamauro/json-to-pojo/



