# Composed
A simple library mod for minecraft 1.21.11+

# Adding to your project
First add this to your `build.gradle`
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
Then add this line to your `dependancies`, replacing `VERSION` with the most recent version
```groovy
modImplementation "com.github.z3roco01:composed:VERSION"
```

# Using
Once you have the library added to your project you can begin to use it.<br>
If you want to mark a field as being apart of the config, you must add the `@ConfigProperty` annotation to it. This signifies it will be stored/loaded anytime this class is used as a config file.<br>
You can also add comments before a property by using the `@Comment(comment = "commnet text")` annotation before the variable definition.

## Storing/loading
To store a config class you must call the `ConfigFile.store(path, object)` method with the path ( relative to the `.minecraft` directory ) and an instance of the object which has the properties.
Loading is just as simple, you call `ConfigFile.load(path, object)` then it'll be loaded just like that.<br>
Every property that is apart of the config must have a default value, and `ArrayList`s must have at least one element.<br>

## Examples
A simple config that will store one int with a comment above it
```java
public class ExampleConfig {
    @Comment(comment = "this is a test int")
    @ConfigProperty
    public int test = 10;
}
```
To then load and save the config:
```java
public class ModMain implements ModInitializer {
    public static final ExampleConfig config = new ExampleConfig();
    
    @Override
    public void onInitialize() {
        // need to catch exceptions that load and store can produce
        try {
            // load in the values
            ConfigFile.load("./config/example.conf", config);
            
            // do stuff to them
            config.test += 54;
        
            // store them again
            ConfigFile.store("./config/example.conf", config);
        }catch(IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
```
The config file `.minecraft/config/example.conf` will look like:
```
# this is a test int
test=64
```