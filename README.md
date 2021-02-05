# Foreward

This section is the only piece of text in this document written by myself; everything else was produced by the Battlecode staff.

See https://battlecode.org/ for game details.

All code outside of the `src/` and `test/` directories was produced by the Battlecode staff.

The Battlecode backend interacts directly with `src/player/RobotPlayer.java`; the backend passes a `RobotController` to `RobotPlayer::run`, where handling of the `RobotController` is then delegated to an instance of `IRobotHandler`. Each concrete `IRobotHandler` contains the state/methods necessary for one of the four robot types.

Enlightenment Centers are the "brain" units; they build all others and assign them various tasks (patrol, attack, etc.). The other units carry out these assignments and report "battlefield intel" to any listening units.

Communication is governed by "flags"-- positive integers of <= 24 bits. See `src/player/util/battlecode/flag/Flag.java` for details.

Many thanks to the Battlecode 2021 staff for a solid IAP course!

# Battlecode 2021 Scaffold

This is the Battlecode 2021 scaffold, containing an `examplefuncsplayer`. Read https://2021.battlecode.org/getting-started!

### Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.


### Useful Commands

- `./gradlew run`
    Runs a game with the settings in gradle.properties
- `./gradlew update`
    Update to the newest version! Run every so often

