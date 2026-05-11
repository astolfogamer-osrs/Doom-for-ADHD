package net.runelite.client.plugins.doomhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("doomhelper")
public interface DoomHelperConfig extends Config
{
    @ConfigItem(
            keyName = "volume",
            name = "Volume",
            description = "Volume do som MAGE / RANGED",
            position = 0
    )
    default int volume()
    {
        return 80; // %
    }
}
