package de.tobi1craft.repairswapper;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.ArrayList;
import java.util.List;

public class RepairSwapperConfig extends MidnightConfig {

    public enum MainHandRequirement {
        SWORDS, MELEE_WEAPONS, ANY
    }

    public enum Priority {
        LOWEST_DURABILITY, LOWEST_PERCENTAGE
    }

    @Entry()
    public static boolean auto = true;

    @Entry(min = 0, max = 8, isSlider = true)
    public static int orbDetectionRange = 4; // in blocks, 0 to only react to picked up orbs

    @Entry(min = 0, max = 60)
    public static double resetDelaySeconds = 3.0;

    @Entry()
    public static MainHandRequirement mainHandRequirement = MainHandRequirement.SWORDS;

    @Entry()
    public static Priority priority = Priority.LOWEST_DURABILITY;

    @Entry()
    public static boolean protectTotem = true;

    @Entry()
    public static boolean disableWhenRepaired = true;

    @Entry()
    public static boolean showHud = true;

    @Entry()
    public static boolean playSounds = true;

    @Entry(idMode = 0)
    public static List<String> excludedItems = new ArrayList<>();
}
