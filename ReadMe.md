# Java Busted [![Build Status](https://travis-ci.org/SquidDev/Java-Busted.svg?branch=master)](https://travis-ci.org/SquidDev/Java-Busted)

The [Busted](https://github.com/Olivine-Labs/busted) testing framework for LuaJ and JUnit 4 

This isn't meant to be a pure line for line rewrite as much of the code is already implemented in the main JUnit 4 code.
Mediator, custom output handlers and language support are not supported.
 
## Usage
```java
@RunWith(BustedRunner.class)
public class Test {
	@BustedRunner.Sources(root = "/squiddev/busted/")
	public static String[] sources() {
		return new String[]{
			"BustedTest.lua"
		};
	}
}
```

You can also specify a custom runner function and assign custom variables in the busted environment

```java
@BustedRunner.RunFile
public static void run(String path, LuaFile.Globals globals) {
	LuaValue globals = JsePlatform.debugGlobals();
	globals.set("myAPI", new CustomAPI());
	context.setEnv(globals);
	LoadState.load(BustedRunner.class.getResourceAsStream(file), file, globals).invoke();
}

@BustedRunner.SetupBusted
public static void setupBusted(Busted busted) {
	// Alias ignore with pending
	busted.register("ignore", "pending");
}
```
