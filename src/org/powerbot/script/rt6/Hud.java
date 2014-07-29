package org.powerbot.script.rt6;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.powerbot.script.Condition;

/**
 * Utilities for manipulating the hud.
 */
public class Hud extends ClientAccessor {
	public static final int WIDGET_HUD = 1477;
	public static final int WIDGET_MENU = 1431;
	public static final int WIDGET_MENU_BOUNDS = 28;
	public static final int WIDGET_MENU_WINDOWS = 1432;
	public static final int COMPONENT_MENU_WINDOWS_LIST = 4;
	private Rectangle[] boundsCache;
	private long cachedTime;

	public Hud(final ClientContext factory) {
		super(factory);
	}

	/**
	 * An enumeration of menu options.
	 */
	public enum Menu {//TODO: menu opened & close helper
		NONE(null, -1),
		OTHER(null, -1),
		HERO(null, 18829, Window.SKILLS, Window.ACTIVE_TASK),
		GEAR(null, 18830, Window.BACKPACK, Window.WORN_EQUIPMENT),
		ADVENTURES(new LegacyTab(1819, "Adventures"), 18831, Window.ACTIVE_TASK),
		POWERS(null, 18832, Window.PRAYER_ABILITIES, Window.MAGIC_ABILITIES, Window.MELEE_ABILITIES, Window.RANGED_ABILITIES, Window.DEFENCE_ABILITIES),
		SOCIAL(null, 18833, Window.FRIENDS, Window.FRIENDS_CHAT_INFO, Window.CLAN),
		EXTRAS(new LegacyTab(23663, "Extras"), 18836),
		HELP(null, 18838),
		OPTIONS(new LegacyTab(1829, "Settings"), 18835, Window.NOTES, Window.MUSIC_PLAYER);
		private final LegacyTab tab;
		private final int texture;
		private final Window[] windows;

		Menu(final LegacyTab tab, final int texture, final Window... windows) {
			this.tab = tab;
			this.texture = texture;
			this.windows = windows;
		}

		public int texture() {
			return texture;
		}

		public Window[] windows() {
			return windows;
		}
	}

	/**
	 * An enumeration of known possible windows.
	 */
	public enum Window {
		ALL_CHAT(Menu.NONE, 18726, 18754, 137, 82, null),
		PRIVATE_CHAT(Menu.NONE, 18727, 18755, 1467, 55, null),
		FRIENDS_CHAT(Menu.NONE, 18728, 18756, 1472, 55, null),
		CLAN_CHAT(Menu.NONE, 18729, 18757, 1471, 55, null),
		GUEST_CLAN_CHAT(Menu.NONE, 18731, 18790, 1470, 55, null),
		EMOTES(Menu.NONE, 18741, 18776, 590, 14, new LegacyTab(1830, "Emotes")),
		MINIMAP(Menu.NONE, 18742, 0, 1465, 12, null),

		SKILLS(Menu.HERO, 18738, 18775, 1466, 0, new LegacyTab(1818, "Skills")),
		ACTIVE_TASK(Menu.HERO, 18735, 18789, 1220, 0, new LegacyTab(1820, "Active Task")),
		BACKPACK(Menu.GEAR, 18732, 18772, Backpack.WIDGET, Backpack.COMPONENT_CONTAINER, new LegacyTab(1821, "Backpack")),
		WORN_EQUIPMENT(Menu.GEAR, 18733, 18773, Equipment.WIDGET, 0, new LegacyTab(1822, "Worn Equipment")),
		PRAYER_ABILITIES(Menu.POWERS, 18734, 18774, Powers.WIDGET_PRAYER, Powers.COMPONENT_PRAYER_CONTAINER, new LegacyTab(1823, "Prayer Abilities")),
		MAGIC_ABILITIES(Menu.POWERS, 18724, 18752, 1461, 0, new LegacyTab(1824, "Magic Abilities")),
		MELEE_ABILITIES(Menu.POWERS, 18722, 18750, 1460, 0, new LegacyTab(1817, "Melee Abilities")),
		RANGED_ABILITIES(Menu.POWERS, 18723, 18751, 1452, 0, null),
		DEFENCE_ABILITIES(Menu.POWERS, 18725, 18753, 1449, 0, null),
		FRIENDS(Menu.SOCIAL, 18737, 18759, 550, 33, new LegacyTab(6238, "Friends")),
		FRIENDS_CHAT_INFO(Menu.SOCIAL, 18739, 18761, 1427, 0, new LegacyTab(6237, "Friends Chat Info")),
		CLAN(Menu.SOCIAL, 18740, 18762, 1110, 2, new LegacyTab(1828, "Clan")),
		NOTES(Menu.OPTIONS, 18744, 18779, 1417, 0, new LegacyTab(1832, "Notes")),
		MUSIC_PLAYER(Menu.OPTIONS, 18745, 18780, 1416, 0, new LegacyTab(1831, "Music Player")),

		MINIGAMES(Menu.OTHER, 18749, 18788, 939, 0, null),
		FAMILIAR(Menu.OTHER, 18748, 18787, Summoning.WIDGET, 0, null);
		private final Menu menu;
		private final int miniTexture;
		private final int texture;
		private final int widget;
		private final int component;
		private final LegacyTab tab;

		Window(final Menu menu, final int texture, final int miniTexture, final int widget, final int component, final LegacyTab tab) {
			this.menu = menu;
			this.texture = texture;
			this.miniTexture = miniTexture;
			this.widget = widget;
			this.component = component;
			this.tab = tab;
		}

		public Menu menu() {
			return menu;
		}

		public int texture() {
			return texture;
		}

		public int miniTexture() {
			return miniTexture;
		}

		public int widget() {
			return widget;
		}

		private int component() {
			return component;
		}
	}

	private static class LegacyTab {
		public final int texture;
		public final String hint;

		public LegacyTab(final int texture, final String hint) {
			this.texture = texture;
			this.hint = hint;
		}
	}

	/**
	 * Returns an array of all the HUD boundaries blocking game interaction.
	 *
	 * @return an array of HUD bounds
	 */
	public Rectangle[] bounds() {
		if (TimeUnit.MILLISECONDS.convert(System.nanoTime() - cachedTime, TimeUnit.NANOSECONDS) < 1000) {
			if (boundsCache != null) {
				return boundsCache;
			}
		}

		final int[][] indexArr = {{1484, 1}, {1189, 6}, {1184, 1}, {1490, 10}};
		final Rectangle[] arr = new Rectangle[Window.values().length + 2 + indexArr.length];
		int index = 0;
		arr[index++] = ctx.widgets.component(WIDGET_MENU, WIDGET_MENU_BOUNDS).viewportRect();//TODO: auto detect
		arr[index++] = ctx.widgets.component(CombatBar.WIDGET, CombatBar.COMPONENT_BOUNDS).viewportRect();
		//subscribe, chat, chat
		for (final int[] pair : indexArr) {
			final Component c = ctx.widgets.component(pair[0], pair[1]);
			if (!c.visible()) {
				continue;
			}
			arr[index++] = c.viewportRect();
		}
		for (final Window window : Window.values()) {
			final Component sprite = getSprite(window);
			if (sprite == null) {
				continue;
			}
			arr[index++] = sprite.parent().viewportRect();
		}
		cachedTime = System.nanoTime();
		for (final Rectangle r : arr) {
			if (r == null) {
				break;
			}

			r.grow(5, 5);
		}
		return boundsCache = Arrays.copyOf(arr, index);
	}

	public boolean legacy() {
		return ctx.varpbits.varpbit(4332) == 0x80000000;
	}

	public boolean fixed() {
		if (!legacy()) {
			return false;
		}
		final Component c1 = getLegacyTab(Window.MELEE_ABILITIES.tab), c2 = getLegacyTab(Window.NOTES.tab);
		return c1 != null && c2 != null && c1.screenPoint().y != c2.screenPoint().y;
	}

	public boolean floating(final Window window) {
		return getSprite(window) != null || getTab(window) != null;
	}

	/**
	 * Returns if a {@link Window} is open or not.
	 *
	 * @param window the {@link Window} to check if open
	 * @return <tt>true</tt> if the window is open; otherwise <tt>false</tt>
	 */
	public boolean opened(final Window window) {
		return legacy() && openTab(window.tab) || ctx.widgets.component(window.widget(), window.component()).visible();
	}

	private boolean opened(final LegacyTab tab) {
		final Component c = getLegacyTab(tab);
		return c != null && c.component(0).textureId() == 23346;
	}

	/**
	 * Opens a menu (even if it's already open).
	 *
	 * @param menu the menu to open.
	 * @return <tt>true</tt> if the menu was opened; otherwise <tt>false</tt>
	 */
	public boolean open(final Menu menu) {
		if (legacy()) {
			return openTab(menu.tab);
		}
		final Component m = getMenu(menu);
		return m != null && m.click();
	}

	/**
	 * Opens a {@link Window}.
	 *
	 * @param window the {@link Window} desired to be opened
	 * @return <tt>true</tt> if the window was opened or is already open; otherwise <tt>false</tt>
	 */
	public boolean open(final Window window) {
		if (legacy()) {
			return openTab(window.tab);
		}
		if (window == null || window.menu() == Menu.OTHER) {
			return false;
		}
		if (opened(window) || window.menu() == Menu.NONE) {
			return true;
		}

		if (isTabInBar(window)) {
			final Component tab = getTab(window);
			return tab != null && tab.click() && Condition.wait(new Condition.Check() {
				@Override
				public boolean poll() {
					return opened(window);
				}
			}, 100, 20);
		}

		final Component menu = getMenu(window.menu());
		if (menu != null && (getToggle(window) != null || menu.hover())) {
			final Component list = ctx.widgets.component(WIDGET_MENU_WINDOWS, COMPONENT_MENU_WINDOWS_LIST);
			if (list == null) {
				return false;
			}
			Condition.wait(new Condition.Check() {
				@Override
				public boolean poll() {
					return list.visible();
				}
			}, 100, 20);
			Condition.sleep();
			final Component toggle = getToggle(window);
			if (toggle != null && toggle.hover()) {
				if (toggle.visible() && ctx.input.click(true)) {
					return Condition.wait(new Condition.Check() {
						@Override
						public boolean poll() {
							return opened(window);
						}
					}, 100, 20);
				}
			}
		}
		return false;
	}

	private boolean openTab(final LegacyTab tab) {
		if (tab == null) {
			return false;
		}
		if (opened(tab)) {
			return true;
		}
		final Component c = getLegacyTab(tab);
		return c != null && c.click() && Condition.wait(new Condition.Check() {
			@Override
			public boolean poll() {
				return opened(tab);
			}
		}, 100, 5);
	}

	/**
	 * Closes a {@link Window}.
	 *
	 * @param window the {@link Window} to be closed
	 * @return <tt>true</tt> if the {@link Window} was closed; otherwise <tt>false</tt>
	 */
	public boolean close(final Window window) {
		if (legacy()) {
			return closeTab(window.tab);
		}
		if (window.menu() == Menu.NONE) {
			return false;
		}
		if (!floating(window)) {
			return true;
		}
		if (open(window)) {
			final Component sprite = getSprite(window);
			if (sprite != null && sprite.widget().component(sprite.parent().index() + 1).component(1).interact("Close")) {
				return Condition.wait(new Condition.Check() {
					@Override
					public boolean poll() {
						return !opened(window);
					}
				}, 125, 20);
			}
		}
		return !opened(window);
	}

	private boolean closeTab(final LegacyTab tab) {
		if (tab == null || fixed()) {
			return false;
		}
		final Component c = getLegacyTab(tab);
		return !opened(tab) || c != null && c.click() && Condition.wait(new Condition.Check() {
			@Override
			public boolean poll() {
				return !opened(tab);
			}
		}, 100, 5);
	}

	private Component getLegacyTab(final LegacyTab tab) {
		for (final Component c : ctx.widgets.widget(1431)) {
			if (c.component(1).textureId() == tab.texture) {
				return c;
			}
		}
		return null;
	}

	public FloatingMessage floatingMessage() {
		final Component c = ctx.widgets.component(1177, 0);
		final Component type = c.component(0), text = c.component(9);
		return new FloatingMessage(text.text(), type.textureId());
	}

	private boolean isTabInBar(final Window window) {
		final Component tab = getTab(window);
		return tab != null && tab.parent().viewportRect().contains(tab.viewportRect());
	}

	private Component getToggle(final Window window) {
		if (window == null) {
			return null;
		}
		final int texture = window.miniTexture();
		for (final Component sub : ctx.widgets.component(WIDGET_MENU_WINDOWS, COMPONENT_MENU_WINDOWS_LIST).components()) {
			if (sub.textureId() == texture && sub.visible()) {
				return sub;
			}
		}
		return null;
	}

	Component getMenu(final Menu menu) {
		if (menu == null) {
			return null;
		}
		final int texture = menu.texture();
		for (final Component c : ctx.widgets.widget(WIDGET_MENU)) {
			for (final Component child : c.components()) {
				if (child.textureId() == texture && child.valid()) {
					return child;
				}
			}
		}
		return null;
	}

	Component getTab(final Window window) {
		if (window == null) {
			return null;
		}
		final int texture = window.miniTexture();
		for (final Component child : ctx.widgets.widget(WIDGET_HUD)) {
			for (final Component sub : child.components()) {
				if (sub.textureId() == texture && sub.valid()) {
					return sub;
				}
			}
		}
		return null;
	}

	Component getSprite(final Window window) {
		if (window == null) {
			return null;
		}
		final int texture = window.texture();
		for (final Component child : ctx.widgets.widget(WIDGET_HUD)) {
			for (final Component sub : child.components()) {
				if (sub.textureId() == texture && sub.visible()) {
					return sub;
				}
			}
		}
		return null;
	}
}
