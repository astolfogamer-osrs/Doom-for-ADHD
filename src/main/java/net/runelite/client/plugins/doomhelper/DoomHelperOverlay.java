package net.runelite.client.plugins.doomhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Font;
import javax.inject.Inject;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class DoomHelperOverlay extends Overlay {
    private final DoomHelperPlugin plugin;

    @Inject
    private DoomHelperOverlay(DoomHelperPlugin plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        for (VolatileEarth earth : plugin.getActiveEarths()) {
            int ticks = earth.getTicksLeft();
            String text = String.format("%.1fs", ticks * 0.6);

            Point canvasPoint = earth.getNpc().getCanvasTextLocation(graphics, text, 60);

            if (canvasPoint != null) {
                Color color = (ticks <= 6) ? Color.RED : Color.WHITE;
                graphics.setFont(new Font("Arial", Font.BOLD, 14));
                OverlayUtil.renderTextLocation(graphics, canvasPoint, text, color);
            }
        }
        return null;
    }
}