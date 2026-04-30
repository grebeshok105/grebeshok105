package com.example.superheroes.hero;

public record HeroTheme(
		int panelTop,
		int panelBottom,
		int panelBorder,
		int panelHighlight,
		int heroNameColor,
		int energyDark,
		int energyBright,
		int energyGlow,
		int energyIcon,
		int manaDark,
		int manaBright,
		int manaGlow,
		int manaIcon,
		int radialBorderIdle,
		int radialBorderActive,
		int radialKeyActive,
		int radialTextActive,
		int radialGlow
) {
	public static final HeroTheme HOMELANDER = new HeroTheme(
			0xE0181C2A,
			0xD0080A14,
			0x88FFD27A,
			0x33FFFFFF,
			0xFFFFE07A,
			0xFFB35900,
			0xFFFFD060,
			0x55FFE08A,
			0xFFFFC538,
			0xFF3B1F8A,
			0xFFB58CFF,
			0x55C7A8FF,
			0xFFB58CFF,
			0x55FFD27A,
			0xFFFFC538,
			0xFFFFC538,
			0xFFFFF1B0,
			0x55FFD27A
	);

	public static final HeroTheme IRON_MAN = new HeroTheme(
			0xE03A0608,
			0xD01A0204,
			0x99FFD24A,
			0x44FFEEAA,
			0xFFFFE060,
			0xFF7A0000,
			0xFFFF3030,
			0x66FF6060,
			0xFFFF3838,
			0xFF8A4A00,
			0xFFFFC85A,
			0x66FFE090,
			0xFFFFC85A,
			0x66FF8A38,
			0xFFFFD24A,
			0xFFFFD24A,
			0xFFFFEFB0,
			0x66FF8A38
	);

	public static final HeroTheme GOKU = new HeroTheme(
			0xE0331504,
			0xD0140602,
			0x88E85D04,
			0x33FFCC88,
			0xFFFF9A4A,
			0xFF7A2900,
			0xFFE85D04,
			0x55FFAA66,
			0xFFFFAA44,
			0xFF1A0A00,
			0xFFFF7A28,
			0x55FF8844,
			0xFFFF7A28,
			0x55E85D04,
			0xFFFFAA44,
			0xFFFFAA44,
			0xFFFFE0B0,
			0x55FFAA66
	);

	public static final HeroTheme NARUTO = new HeroTheme(
			0xE0332B00,
			0xD0141000,
			0x88FFD60A,
			0x33FFE99A,
			0xFFFFE85A,
			0xFF7A6700,
			0xFFFFD60A,
			0x55FFEC8A,
			0xFFFFE060,
			0xFF1A1500,
			0xFFFFCC1A,
			0x55FFE070,
			0xFFFFCC1A,
			0x55FFD60A,
			0xFFFFE060,
			0xFFFFE060,
			0xFFFFF7C0,
			0x55FFE070
	);

	public static final HeroTheme CAPTAIN_AMERICA = new HeroTheme(
			0xE00A1E40,
			0xD0040A1A,
			0x881E40AF,
			0x33EF4444,
			0xFF60A0FF,
			0xFF0E2860,
			0xFF1E40AF,
			0x556090FF,
			0xFF60A0FF,
			0xFF1A0606,
			0xFFEF4444,
			0x55FF7878,
			0xFFEF4444,
			0x551E40AF,
			0xFFEF4444,
			0xFFEF4444,
			0xFFFFFFFF,
			0x556090FF
	);

	public static final HeroTheme SLENDERMAN = new HeroTheme(
			0xE00A0A0F,
			0xD0020203,
			0x88B0B0C0,
			0x33D8D8E8,
			0xFFE6E6E6,
			0xFF1A1A22,
			0xFF707080,
			0x559A9AB0,
			0xFF9A9AB0,
			0xFF120A1A,
			0xFFC4B0FF,
			0x55E0CCFF,
			0xFFC4B0FF,
			0x55B0B0C0,
			0xFFE6E6E6,
			0xFFE6E6E6,
			0xFFFFFFFF,
			0x55B0B0C0
	);

	public static final HeroTheme DEFAULT = HOMELANDER;
}
