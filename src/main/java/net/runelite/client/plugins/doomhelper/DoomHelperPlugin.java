package net.runelite.client.plugins.doomhelper;

import com.google.inject.Provides;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sound.sampled.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "[AG] Doom Helper",
        description = "Sons para Projéteis e Timer para Volatile Earth",
        tags = {"doom", "pvm", "helper"}
)
public class DoomHelperPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private DoomHelperConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DoomHelperOverlay overlay;

    @Getter
    private final List<VolatileEarth> activeEarths = new ArrayList<>();

    private static final int MAGE_PROJECTILE_ID   = 3385;
    private static final int RANGED_PROJECTILE_ID = 3384;
    private static final int VOLATILE_EARTH_ID    = 14714; // Verifique este ID no jogo

    private int lastProjectileTick = -1;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        activeEarths.clear();
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();
        if (projectile.getStartCycle() != client.getGameCycle()) return;

        int currentTick = client.getTickCount();
        if (currentTick == lastProjectileTick) return;

        int id = projectile.getId();
        if (id == MAGE_PROJECTILE_ID) {
            lastProjectileTick = currentTick;
            playSound("mage.wav");
        } else if (id == RANGED_PROJECTILE_ID) {
            lastProjectileTick = currentTick;
            playSound("ranged.wav");
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        // Detecta pelo ID ou pelo Nome se o ID mudar
        if (npc.getId() == VOLATILE_EARTH_ID || (npc.getName() != null && npc.getName().contains("Volatile Earth"))) {
            activeEarths.add(new VolatileEarth(npc, 20));
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        activeEarths.removeIf(earth -> {
            earth.setTicksLeft(earth.getTicksLeft() - 1);
            return earth.getTicksLeft() <= 0 || earth.getNpc().isDead();
        });
    }

    private void playSound(String fileName) {
        new Thread(() -> {
            try {
                // CAMINHO RESTAURADO PARA O SEU PACKAGE
                String path = "/net/runelite/client/plugins/doomhelper/" + fileName;
                InputStream is = getClass().getResourceAsStream(path);

                if (is == null) {
                    log.error("Arquivo de som nao encontrado: " + path);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);

                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(volumeToDb(config.volume()));
                }

                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception e) {
                log.warn("Erro ao tocar som: " + fileName, e);
            }
        }).start();
    }

    private float volumeToDb(int volumePercent) {
        int vol = Math.max(0, Math.min(100, volumePercent));
        if (vol == 0) return -80.0f;
        return (float) (20.0 * Math.log10(vol / 100.0));
    }

    @Provides
    DoomHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DoomHelperConfig.class);
    }
}