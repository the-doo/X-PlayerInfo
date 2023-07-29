package com.doo.playerinfo.consts;

import net.minecraft.resources.ResourceLocation;

public final class Const {
    public static final String MINECRAFT_NAME = "Minecraft";
    public static final String ID = "player_info";
    public static final ResourceLocation CHANNEL = new ResourceLocation(ID + ":info_packet");
    public static final ResourceLocation CONFIG_CHANNEL = new ResourceLocation(ID + ":config");

    public static final String HEALTH = "attribute.extend.health";
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


    private Const() {
    }

}
