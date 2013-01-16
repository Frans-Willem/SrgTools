srgtool 2.0

A utility for manipulating and applying srg mappings. Based on SrgTools by [Frans-Willem](https://github.com/Frans-Willem/SrgTools).

Downloads: https://bitbucket.org/agaricusb/srgtools/downloads

Useful with mappings from https://github.com/agaricusb/MinecraftRemapping

For usage, run with no arguments.

To build from source, run: mvn package

## Examples
Porting an NMS plugin to MCPC+:

    java -jar srgtool.jar apply --srg vcb2obf.srg --in plugin.jar --inheritance plugin.jar --out plugin2.jar

Porting a mod to MCPC:

    java -jar srgtool.jar apply --srg obf2cb.srg --in mod.jar --inheritance mod.jar --out portedmod.jar

Reobfuscating a mod compiled with MCP (alternative to RetroGuard/reobfuscate.sh):

    java -jar srgtool.jar apply --srg mcp2obf.srg --in compiledmod.jar --inheritance compiledmod.jar --out obfuscatedmod.jar

Deobfuscating to MCP:

    java -jar srgtool.jar apply --srg obf2mcp.srg --in obf.jar --inheritance obf.jar --out deobf.jar

Deobfuscating CB to MCP:

    java -jar srgtool.jar apply --srg cb2mcp.srg --in plugin.jar --inheritance plugin.jar --out plugin2.jar

