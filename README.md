# Minecraft_Websocket_Integration
 Allows websockets to run commands on your server

### Building
    $ mvn
The output plugin will be in target/

# [Complete Getting Started](https://github.com/KK964/Minecraft_Websocket_Intergration/wiki/GettingStarted)

## Simple getting started
### Connecting to the websocket
- Connect as if it were a normal websocket server
- Authentication
  - If authentication is enabled in the config.yml an Authentication Bearer Token will need to be supplied
  - Through the connected client, send `Bearer <Token>`, and you will be authenticated. Ex: `Bearer abcdefg123`

### Running Commands
- Once connected to the socket, running commands is as simple as sending `Command <command>`
- Multiple commands can be added at once by putting it on new lines
- ```
  Command say hi
  Command /tellraw @a {"text":"Hi! (but in red)","color":"red"}