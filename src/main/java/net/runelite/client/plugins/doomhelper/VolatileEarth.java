package net.runelite.client.plugins.doomhelper;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.NPC;

@Data
@AllArgsConstructor
public class VolatileEarth {
    private final NPC npc;
    private int ticksLeft;
}