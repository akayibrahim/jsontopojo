# JSON TO POJO Maven Plugin

#### This Maven Plugin is for creating POJO's automatically from json.

##### Follow steps:

1. Add below build tag to pom.xml of your project

```xml
<build>
    <plugins>
      <plugin>
        <groupId>io.github.akayibrahim</groupId>
        <artifactId>jsontopojo</artifactId>
        <version>1.5</version>
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
              <path>io.githup.akayibrahim.testing</path> <!-- mandatory -->
              <className>EmployeeTesting</className> <!-- mandatory -->
              <lombok>true</lombok> <!-- optional -->
              <prefix>Pre</prefix> <!-- optional -->
              <postfix>Post</postfix> <!-- optional -->
              <throwForDuplicateObjectName>false</throwForDuplicateObjectName> <!-- optional - default: false
                If it is set true, the plugin will throw error for duplicate object name. You have to fix this names at json and regenerate. 
                If it is set false, the plugin will add "_$" postfix for duplicate object name and add TODO to this class for change information. -->
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

Maven Repo Link => https://repo.maven.apache.org/maven2/io/github/akayibrahim/

NOTE : All data in json should not be null or empty. Tool generate TODO comment for null fields, so you can fix it. Please search TODO comment after generate POJO's.

NOTE : For using lombok, you have to add lombok dependency to your pom.xml.
```xml    
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.10</version>
    </dependency>
```
NOTE 2 : For using jackson, you have to add jackson dependency to your pom.xml. it is necessary for "throwForDuplicateObjectName" key.
```xml    
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-annotations</artifactId>
      <version>2.10.1</version>
     </dependency>
```
Thanks to https://github.com/giordamauro/json-to-pojo/



