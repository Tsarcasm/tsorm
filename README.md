# Tsorm
A bare-metal Java ORM designed to remove tedious boilerplate SQL while giving the 
programmer a high degree of control.

### Installation
#### Jitpack 
 - Gradle:
 Add `compile 'com.github.Tsarcasm:tsorm:v1.1'` to your dependencies
    
    
 - Maven: Add to your dependencies:
    ```xml
    <dependency>
        <groupId>com.github.Tsarcasm</groupId>
        <artifactId>tsorm</artifactId>  
        <version>v1.1</version>    
    </dependency>
    ```

 * * * 

### Usage

In Tsorm, every object we want to store in a database is an immutable **Entity**. 
To use entities, make every object you want to store subclass `Entity`.

There are several rules for entity subclasses: 
* Every entity has a primary key (or **pk**). This is provided in the `Entity` parent class.
* All fields to be stored in the database must be marked final. This ensures immutability
* Any time one of these fields is to be changed, a new instance should be created.
* Fields to store must be a primitive, a String or a wrapper around a single primitive or String (e.g. Timestamp, UUID)


A collection of Entities is called a Store. A store is equivalent to a database table.
