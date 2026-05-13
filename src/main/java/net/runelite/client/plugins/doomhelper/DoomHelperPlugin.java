package net.runelite.client.plugins.doomhelper;

import com.google.inject.Provides;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Doom Helper Audio",
        description = "Alertas sonoros para projéteis de Mage e Range",
        tags = {"doom", "pvm", "audio", "helper"}
)
public class DoomHelperPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private DoomHelperConfig config;

    @Inject
    private ScheduledExecutorService executor;

    private static final int MAGE_PROJECTILE_ID   = 3385;
    private static final int RANGED_PROJECTILE_ID = 3384;

    private int lastProjectileTick = -1;

    @Override
    protected void startUp() {

    }

    @Override
    protected void shutDown() {

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

    private void playSound(String fileName) {
        executor.submit(() -> {
            try {
                String path = "/net/runelite/client/plugins/doomhelper/" + fileName;
                InputStream is = getClass().getResourceAsStream(path);

                if (is == null) return;

                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);

                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        gain.setValue(volumeToDb(config.volume()));
                    }

                    clip.start();
                    clip.addLineListener(e -> {
                        if (e.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Erro ao tocar som: " + fileName);
            }
        });
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