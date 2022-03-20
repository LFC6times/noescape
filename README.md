# NoEscape
A Fabric mod to add rectangular world borders to Minecraft.

### Config file:
Found in noescape.properties

Options:

`x1`: First x coordinate of border

`z1`: First z coordinate of border

`x2`: Second x coordinate of border

`z2`: Second z coordinate of border

The coordinates are integer values.

####Settings:

`justKill`: True/False (default false), whether to "just kill" any world border escapers

`vanillaWorldBorder`: True/False (default true), whether to act like vanilla MC's world border (dmgs a certain amt per block per second). Requires `justKill` to be false

`allowCreative`: True/False (default true), whether to kill or not kill creative mode players (true will not kill, false kills)

`worldBorderDmg`: Number (decimal OK, default 0.2 as in vanilla), of dmg dealt to players each second they are outside of the world border. Requires `vanillaWorldBorder` to be true

### In-game Commands:
Run in-game, requires OP level 4

`/noescape` or `/ne`: The base command

Options:

`create`: Create a border with corners (x1, z1), (x2, z2). Example: `/ne create -100 -100 100 100` creates a world border with corners at -100, -100 and 100, 100
`remove`: Removes the world border (and config file)
`reload`: Reloads the config if you changed it in the file
`setting`: Changes a setting, options listed above. Example: `/ne justKill` toggles `justKill`. For `worldBorderDmg`, add the new damage value after.