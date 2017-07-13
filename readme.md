# wpiutil

wpiutil is a utility library designed for making C++ interfacing easier. 

## Build Requirements
To build wpiutil, a few requirements must be met:

- Platform Native Toolchain - You must have a toolchain for your native platform installed if you wish to build ntcore for your machine. On Windows, this is Visual Studio. On Mac, this is Clang, and on Linux, this is GCC. Your toolchain must support the `-std=c++11` language flag.
- Platform Native JDK - In order to compile ntcore your native platform, you must have the JDK for your platform installed, so that the correct JNI headers can be included.
- ARM Toolchain - To crosscompile ntcore for the roboRIO, you must have the FRC ARM toolchain installed, which can be found [here](http://first.wpi.edu/FRC/roborio/toolchains/).
- Cross Toolchains (coming soon)

## Building
Gradle is the main build system used by ntcore. All tasks are run with the `gradlew` wrapper, which is included in the root of the repository. All targets that can be accomplished by Gradle are referred to as tasks. The main task available is `build`. To run Gradle, cd into the build directory and run:

```bash
./gradlew build
```

This will build the roboRIO wpiutil library, in addition to the library for your native platform. Note if the roboRIO compiler cannot be found, the build will skip the roboRIO build. To build for either only the roboRIO, or every platform except the roboRIO, use the following flags:

```bash
-PskipAthena
-PonlyAthena
```

Note if you choose the `onlyAthena` flag, tests will not be ran, as they depend on the current platform being built.

In addition, more platforms can be built. For instance, with additional cross compilers more Arm binaries can be built. In addition, the second bitness for your current platform can be built with an additional flag. To enable every possible platform, use the following flag.

```bash
-PbuildAll
```

If you are building the native version on a 64 bit Linux computer, use a GCC installation which has multilib support enabled (it can compile both 32 and 64 bit programs). The package providing that support on most Linux distributions is called `gcc-multilib`.