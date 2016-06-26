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


Download the `pref2go.jar` from the [releases](releases) section of GitHub.

Now make sure that `pref2go.jar` is on your classpath.

On startup of your application you'll then need to set one or more of the system properties below:


| System Property | Description 
| --- | --- 
| java.util.prefs.PreferencesFactory | Mandatory.  Must be set to `com.addicticks.preferences2go.TemporaryPreferencesFactory`
| pref2go.xmlFile | Optional. If set it's assumed to be the name of a Java Preferences XML file conforming to the DTD as explained in the Javadoc for [Preferences](http://docs.oracle.com/javase/8/docs/api/java/util/prefs/Preferences.html). The content of this XML file will be loaded on startup.
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

