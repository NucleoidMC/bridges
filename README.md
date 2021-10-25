# Bridges

A fast-paced minigame designed for duels on a 1 block wide bridge, with scoring locations in the base of the opposition.

## Contributing

### Maps

More information about the tools used here can be found on the [Nucleoid map-making page](https://docs.nucleoid.xyz/plasmid/maps/)

- Run a server with [Plasmid](https://github.com/nucleoidmc/plasmid) installed
- Create a new map workspace and set the bounds
- Build your map. Make sure to include a team base with end portals as goals
- Add regions to your map as shown below
- Add your map NBT by exporting and putting it in /data/bridges/map_templates
- Add a JSON file in /data/bridges/games, by copying one of the existing maps. Set the `type` property in your game JSON to the name of your map, excluding the file extension
- Open a PR, preferably with screenshots

### Game JSON format

- `type`: Identifier. For Bridges this value is always `bridges:bridges`
- `map`: Identifier. Bridges maps are `bridges:<map_name>
- `players`: Object. Has `min` and `max` properties. Can optionally have `threshold` property
- `time_limit_secs`: Number of seconds the game should last before the game ends, if no winner is found before
- `teams`: Teams Array. Example team entry would be `{"key": "blue", "name": "Blue", "color": "blue"}` 
- `point_win_threshold`: The number of goals at which a team will win the game
- `items`: ItemStack Array. Example entry would be ` { "id": "minecraft:iron_sword", "Count": 1 }`

### Map Regions

- `center`: The most central position on a map

#### Team Regions

Replace `<team>` with the `key` value of the team entry in your map JSON.

- `<team>_base`: The bounds of the team's base. Building is prevented in this earlier
- `<team>_spawn`: The area in which a team's players spawn
- `<team>_goal`: An area composed of end portal blocks which grants another team a point if they enter