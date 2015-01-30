<div align="center">
<img src="http://i1279.photobucket.com/albums/y523/textcraft/Jan%202015%20-%204/efbeb7161a01272d77b928c11aaa7c8dafc43cd8da39a3ee5e6b4b0d3255bfef95601890afd80709da39a3ee5e6b4b0d3255bfef95601890afd80709e0eb7f459c1e8c372a01_zps83f4e729.png"></img>
<br>
<img src="http://www.yogaflight.com/images/hor_rule.jpg"></img>
<br>
Unofficial fork inititiated to give a new face to the awesome Bukkit plugin, <a href="https://github.com/Wormhole-X-Treme/Wormhole-X-Treme">Wormholes-X-Treme</a>
<br>
</div>
<br>
Installation:

  Drag and drop the StarGates.jar into your plugins directory. Guaranteed to load on the latest Spigot/Bukkit builds.
  Edit the config.yml to reflect your desired database settings. MySQL and SQLite are supported. While your server is   down, edit the settings.txt to your taste. All options are decribed in detail below.

Usage:
TODO (make a video)

  Making a gate:
    Using the defaults, players must build a portal frame out of obsidian in the following valid shapes:
    TODO (make some images)
    Then, after placing and using a DHD lever (shown above), players can use /sgcomplete to finish the Stargate!
    
  Using a gate:
    Flipping the DHD lever for a gate will activate it and "light" it up. Once activated a player may /dial to another     stargate or use a dial sign. The two gates will connect and temporarily allow anyone to enter. Travel is one-way,     perfect for PvP!
    
  IDCs:
    Stargates can be "locked" via the use of the StarGates iris. If someone dials a gate with an active iris, they        will be denied entry. The iris can be toggled as shown below:
    TODO (add some images)
    Using /sgidc or supplying an idc with /sgcomplete will add an optional password to the gate, allowing the iris to     be disabled for players who dial the gate with the correct IDC.
    NOTE: when using /sgcomplete the "idc=" part of the command is required as a prefix to your IDC.
  
  Networks:
    Networks are used to restrict gate travel. Only gates on the same network can dial each other. This even applies      to the default "PUBLIC" gates.
    NOTE: By default, if a network is not provided with /sgcomplete, the gate will be part of the PUBLIC network.
    NOTE: when using /sgcomplete the "net=" part of the command is required as a prefix to your Network.

Settings.txt:

  TODO (list all options and usage)

Permissions:
  wormhole.use.sign:
    description: Player is able to use the dialer sign
    default: true
  wormhole.use.dialer:
    description: Player is able to use the dialer
    default: true
  wormhole.use.compass:
    description: Can use the wormhole compass
    default: true
  wormhole.use.*:
    description: Can use all wormhole use nodes
    children:
      wormhole.use.sign: true
      wormhole.use.dialer: true
      wormhole.use.compass: true
  wormhole.cooldown.groupone:
    description: Use cooldown group one
    default: false
  wormhole.cooldown.grouptwo:
    description: Use cooldown group two
    default: false
  wormhole.cooldown.groupthree:
    description: Use cooldown group three
    default: false
  wormhole.cooldown.*:
    description: Can use all cooldown nodes
    children:
      wormhole.cooldown.groupone: true
      wormhole.cooldown.grouptwo: true
      wormhole.cooldown.groupthree: true
  wormhole.remove.own:
    description: Can remove own gates
    default: op
  wormhole.remove.all:
    description: Can remove a gate using -all command
    default: op
  wormhole.remove.*:
    description: Can use all remove permissions
    children:
      wormhole.remove.own: true
      wormhole.remove.all: true
  wormhole.build:
    description: Can build Wormhole
    default: op
  wormhole.build.groupone:
    description: Build restriction for groupone
    default: false
  wormhole.build.grouptwo:
    description: Build restriction for grouptwo
    default: false
  wormhole.build.groupthree:
    description: Build restriction for groupthree
    default: false
  wormhole.build.*:
    description: Can use all build permissions
    children:
      wormhole.build: true
      wormhole.build.groupone: true
      wormhole.build.grouptwo: true
      wormhole.build.groupthree: true
  wormhole.config:
    description: Can modify Wormhole config
    default: op
  wormhole.list:
    description: Can list wormhole gates
    default: true
  wormhole.network.use:
    description: Can use gate network
    default: true
  wormhole.network.build:
    description: Can build networks
    default: op
  wormhole.network.*:
    description: Can use all network permissions
    children:
      wormhole.network.use: true
      wormhole.network.build: true
  wormhole.go:
    description: Can teleport to a wormhole location
    default: op
  wormhole.simple.use:
    description: Can use wormhole
    default: true
  wormhole.simple.config:
    description: Can modify configuration
    default: op
  wormhole.simple.build:
    description: Can build a wormhole
    default: op
  wormhole.simple.remove:
    description: Can remove a wormhole
    default: op
  wormhole.simple.*:
    description: Can use all simple wormhole nodes
    children:
      wormhole.simple.config: true
      wormhole.simple.build: true
      wormhole.simple.remove: true
      wormhole.simple.use: true
