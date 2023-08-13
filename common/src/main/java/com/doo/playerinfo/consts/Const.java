package com.doo.playerinfo.consts;

import net.minecraft.resources.ResourceLocation;

public final class Const {
    public static final String MINECRAFT_NAME = "Minecraft";
    public static final String ID = "player_info";
    public static final String DAY = "info.world.day";
    public static final String NOON = "info.world.noon";
    public static final String NIGHT = "info.world.night";
    public static final String MIDNIGHT = "info.world.midnight";
    public static final ResourceLocation CHANNEL = new ResourceLocation(ID + ":info_packet");
    public static final String HEALTH = "attribute.extend.health";
    public static final String HEALTH_PER_SECOND = "attribute.extend.health_per_second";

    public static final String ABSORPTION_AMOUNT = "attribute.extend.absorption_amount";
    public static final String EXPERIENCE_LEVEL = "attribute.extend.experience_level";
    public static final String TOTAL_EXPERIENCE = "attribute.extend.total_experience";
    public static final String EXPERIENCE_PROGRESS = "attribute.extend.experience_progress";
    public static final String PICK_RANGE = "attribute.extend.pick_range";
    public static final String DIGGER_LEVEL = "attribute.extend.digger_level";
    public static final String DIGGER_SPEED = "attribute.extend.digger_speed";
    public static final String DIGGER_EFFICIENCY = "attribute.extend.digger_effect";
    public static final String ATTACK_RANGE = "attribute.extend.attack_range";
    public static final String ATTACK_SWEEP_RANGE = "attribute.extend.attack_sweep_range";
    public static final String CRITICAL_HITS = "attribute.extend.critical_hits";
    public static final String FOOD_LEVEL = "attribute.extend.food_level";
    public static final String EXHAUSTION_LEVEL = "attribute.extend.exhaustion_level";
    public static final String SATURATION_LEVEL = "attribute.extend.saturation_level";
    public static final String DAMAGE_REDUCTION_BY_ARMOR = "attribute.extend.damage_reduction_by_armor";
    public static final String JUMP_POWER = "attribute.extend.jump_power";
    public static final String JUMP_COUNT = "attribute.extend.jump_count";
    public static final String COLLECT_TIME = "info.collect.time";
    public static final String SCORE = "info.stat.score";
    public static final String DEATH_COUNT = "info.stat.death_count";
    public static final String PLAYER_KILL_COUNT = "info.stat.player_killed";
    public static final String MOB_KILL_COUNT = "info.stat.mob_killed";


    private Const() {
    }

}
