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
modApi ("com.github.z3roco01:composed:VERSION")
```
The most recent version being `1.4.0`, if you want the bleeding edge, use `master-SNAPSHOT` for the version be wary of bugs though<br>
The player will need to install Composed separately as well, if you do not wish for this you can use:
```groovy
include modApi("com.github.z3roco01:composed:VERSION")
```

# Using
Once you have the library added to your project you can begin to use it.<br>
If you want to mark a field as being a part of the config, you must add the `@ConfigProperty` annotation to it. This signifies it will be stored/loaded anytime this class is used as a config file.<br>
You can also add comments before a property by using the `@Comment(comment = "commnet text")` annotation before the variable definition.

## Storing/loading
To store a config class you must call the `ConfigFile.store(path, object)` method with the path ( relative to the `.minecraft` directory ) and an instance of the object which has the properties.
Loading is just as simple, you call `ConfigFile.load(path, object)` then it'll be loaded just like that.<br>
Every property that is a part of the config must have a default value, and `ArrayList`s must have at least one element.<br>

### Supported datatypes
The currently supported data types are: ( any fields of other types will be ignored )
- `byte`/`Byte`
- `short`/`Short`
- `int`/`Integer`
- `long`/`Long`
- `float`/`Float`
- `double`/`Double`
- `boolean`/`Boolean`
- `String`
- `Item`
- `Block`
- `StatusEffect`

## Processing data
If you need to process data immediately after its loaded ( ex: turing a list of item ids into a list of items ) you can do that by implementing the `ProcessedConfig` interface in your config class.

## Examples
A simple config that will store one int with a comment above it
```java
public class ExampleConfig {
    @Comment(comment = "should we kill them all")
    @ConfigProperty
    public int killEveryone = true;
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
            // load in the values or store defaults
            ConfigFile.load("./config/example.conf", config);
        }catch(IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        // do stuff with the config values
        if(config.killPlayers) {
            killEveryone();
        }
    }
}
```
The config file `.minecraft/config/example.conf` will look like:
```
# should we kill them all
killEveryon=true
```