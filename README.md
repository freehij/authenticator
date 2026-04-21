# Authenticator
An authentication mod for my javaagent-modloader that adds basic /login and /register commands.  
See [how to use](https://github.com/freehij/authenticator#how-to-use) section.

# Downloads
Download the latest release [here](https://github.com/freehij/authenticator/releases/latest).

# Building
1. Clone the repository with `git clone https://github.com/freehij/authenticator.git` or download it from browser and unpack the zip.
2. Open the folder with mod sources you just downloaded in your terminal and use this command: `gradlew build`.
3. The build will appear in `/build/libs/`.

# How to use
1. Download and install [javaagent-modloader](https://github.com/freehij/javaagent-modloader) (see the provided instructions on how to install it).
2. [Download](https://github.com/freehij/authenticator#downloads) or [build](https://github.com/freehij/authenticator#building) the mod and put it in your servers mod folder.
3. Run the server and ensure that you see the `[Authenticator] Initializing Authenticator.` message in console.

# Configuring
Configuration is located in `/config/authenticator.json`.  
Settings should be pretty straight forward.  
Note that `login-timeout` and `save-interval` should be specified in ticks while `session_time` in seconds.

# TODO
- Config library instead of a custom solution.
- Integrated server support.
- Erase session param in admin commands.

# Known issues
These will most likely be fixed really soon so don't report them.
- Might be incompatible with some plugins/mods (mostly anticheats).
- Mild performance issues with big player count.

Other than that feel free to open an [issue](https://github.com/freehij/authenticator/issues/new) if you notice one!