package org.powerbot.bot.rs3.event.debug;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.powerbot.script.PaintListener;
import org.powerbot.script.rs3.ClientContext;
import org.powerbot.script.rs3.Npc;

public class DrawMobs implements PaintListener {
	private final ClientContext ctx;

	public DrawMobs(final ClientContext ctx) {
		this.ctx = ctx;
	}

	public void repaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final FontMetrics metrics = render.getFontMetrics();
		for (final Npc npc : ctx.npcs.select()) {
			final Point location = npc.getCenterPoint();
			if (location.x == -1 || location.y == -1) {
				continue;
			}
			render.setColor(Color.red);
			render.fillRect((int) location.getX() - 1, (int) location.getY() - 1, 2, 2);
			String s = npc.name() + " (" + npc.getLevel() + ") - " + npc.id();
			render.setColor(npc.isInCombat() ? Color.RED : npc.isInMotion() ? Color.GREEN : Color.WHITE);
			render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() / 2);
			final String msg = npc.getMessage();
			boolean raised = false;
			if (npc.getAnimation() != -1 || npc.getStance() != -1) {
				s = "";
				s += "(";
				if (npc.getPrayerIcon() != -1) {
					s += "P: " + npc.getPrayerIcon() + " | ";
				}
				if (npc.getAnimation() != -1 || npc.getStance() > 0) {
					s += "A: " + npc.getAnimation() + " | ST: " + npc.getStance() + " | ";
				}
				s = s.substring(0, s.lastIndexOf(" | "));
				s += ")";

				render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() * 3 / 2);
				raised = true;
			}
			if (msg != null) {
				render.setColor(Color.ORANGE);
				render.drawString(msg, location.x - metrics.stringWidth(msg) / 2, location.y - metrics.getHeight() * (raised ? 5 : 3) / 2);
			}
		}
	}
}