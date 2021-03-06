# Preferences 2 Go

Changes an existing application from using standard Java Preferences to using an
implementation of Java Preferences that lives in memory only and loads the 
preference values from a file on startup.

This is specifically useful if you do not have access to the source code of the
original application and if you are seeing too many problems with using a Preferences
implementation that uses Windows Registry or hidden files on Linux/Solaris. For example
you may be encountering `BackingStoreException`(s) and you really just want the
configuration values for your application to reside within a configuration file next
to your application without involving things like Windows Registry or obscure
hidden files. This is what Preferences2Go gives you..... all without changing
the underlying application.



### Configuration


Download the `pref2go.jar` from the [releases](https://github.com/Addicticks/Preferences2Go/releases) section of GitHub.

Now make sure that `pref2go.jar` is on your classpath.

On startup of your application you'll then need to set one or more of the system properties below:


| System Property | Description 
| --- | --- 
| java.util.prefs.PreferencesFactory | Mandatory.  Must be set to `com.addicticks.preferences2go.TemporaryPreferencesFactory`
| pref2go.xmlFile | Optional. If set it's assumed to be the name of a Java Preferences XML file conforming to the DTD as explained in the Javadoc for [Preferences](http://docs.oracle.com/javase/8/docs/api/java/util/prefs/Preferences.html). The content of this XML file will be loaded on startup. The file will only ever be read, never written to.
| pref2go.printPref | Optional. If set to "true" the contents of the loaded preferences are pretty printed and logged to standard logger (level INFO) on startup. This only has effect if property `pref2go.xmlFile` is also set.



That's all. Your application now no longer attempts to use a Preferences implementation
that uses Windows Registry. _The change is completely transparent to your existing application._

### Example

Let's assume **myapp** is started as follows:

    #!/bin/bash
    CP="myapp.jar"
    SYSPROP=""

    java -classpath $CP $SYSPROP myapp.StartUp

then simply change like this:

    #!/bin/bash
    CP="myapp.jar:pref2go.jar"
    SYSPROP="-Djava.util.prefs.PreferencesFactory=com.addicticks.preferences2go.TemporaryPreferencesFactory -Dpref2go.xmlFile=../config/myConfig.xml -Dpref2go.printPref=true"

    java -classpath $CP $SYSPROP myapp.StartUp


### Thomson Reuters RFA Java users

For users of the Thomson Reuters RFA Java API there's a simple way to
generate the config file if you already have your preference values
stored in the classic Preferences backing store.

From the SDK's `Tools` folder execute either `config_editor.bat` (if
on Windows) or `config_editor.ksh` (if on Linux/Solaris).

From within the Configuration Editor export the values as follows:

![RFA Configuration Editor](https://cloud.githubusercontent.com/assets/15076120/16362918/174f4cdc-3bbb-11e6-808e-2783459ea7df.png)

The file that was created as a result of this can then be used
as `pref2go.xmlFile`. 


### Restrictions

Unlike standard Preferences any changes made to the preferences
by the application will not be persisted. In other words: there's no attempt to
to write to file pointed to by `pref2go.xmlFile` or anywhere else.

