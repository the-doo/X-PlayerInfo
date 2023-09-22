package com.doo.playerinfo.forge;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.core.InfoUpdatePacket;
import com.doo.playerinfo.gui.InfoScreen;
import com.doo.playerinfo.utils.ClientSideHandler;
import com.doo.playerinfo.utils.ExtractAttributes;
import com.doo.playerinfo.utils.InfoRegisters;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(XPlayerInfo.MOD_ID)
public class XPlayerInfoForge {

    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(new ResourceLocation(XPlayerInfo.MOD_ID, "info_pack_sender"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .acceptedVersions((status, i) -> i == PROTOCOL_VERSION)
            .simpleChannel()
            .messageBuilder(InfoUpdatePacket.class, PROTOCOL_VERSION, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(InfoUpdatePacket::new)
            .encoder(InfoUpdatePacket::write)
            .consumerNetworkThread((packet, context) -> {
                context.enqueueWork(() -> ClientSideHandler.handle(packet));
            })
            .add();

    public XPlayerInfoForge() {
        XPlayerInfo.init(0,
                id -> ModList.get().getModContainerById(id)
                        .map(mod -> mod.getModInfo().getDisplayName()).orElse(id));
        attachInfo();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::register);

        MinecraftForge.EVENT_BUS.register(this);

//        INSTANCE.messageBuilder(InfoUpdatePacket.class, InfoUpdatePacket::write, InfoUpdatePacket::new, ((packet, contextSupplier) -> {
//            contextSupplier.get().enqueueWork(() -> ClientSideHandler.handle(packet));
//            contextSupplier.get().setPacketHandled(true);
//        }));
    }

    private static void attachInfo() {
        InfoRegisters.infoForgeAttach("damage", Const.CRITICAL_HITS, p -> {
            CriticalHitEvent hit = ForgeHooks.getCriticalHit(p, p, true, 1.5F);
            if (hit != null) {
                return hit.getDamageModifier() - hit.getOldDamageModifier();
            }
            return 0;
        });
        InfoRegisters.infoForgeAttach("xp", ExtractAttributes.XP_BONUS.getDescriptionId(), p -> {
            int drop = ForgeEventFactory.getExperienceDrop(null, p, 100);
            return drop <= 100 ? 0 : (drop - 100D) / 100;
        });
    }

    @SubscribeEvent
    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ATTRIBUTES, helper -> ExtractAttributes.forgeRegister(a -> helper.register(a.getDescriptionId(), a)));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        InfoItemCollector.start(event.getServer().getPlayerList().getPlayers(),
                (player, packet) -> INSTANCE.send(packet, player.connection.getConnection()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStop(ServerStoppedEvent event) {
        InfoItemCollector.clean();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = XPlayerInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InfoGroupItems.addClientSideGetter(Const.PICK_RANGE, minecraft -> minecraft.player.getBlockReach());
            InfoGroupItems.addClientSideGetter(Const.ATTACK_RANGE, minecraft -> minecraft.player.getEntityReach());
            InfoGroupItems.addClientSideGetter(Const.ATTACK_SWEEP_RANGE, minecraft -> minecraft.player.getEntityReach());

            InfoGroupItems.addClientSideGetter(Const.DIGGER_LEVEL, minecraft ->
                    minecraft.player.getMainHandItem().getItem() instanceof TieredItem ti ? ti.getTier().getLevel() : 0);
            InfoGroupItems.addClientSideGetter(Const.DIGGER_SPEED, minecraft ->
                    minecraft.player.getMainHandItem().getItem() instanceof TieredItem ti ? ti.getTier().getSpeed() : 0);
        }

        // Key mapping is lazily initialized so it doesn't exist until it is registered
        public static final Lazy<KeyMapping> EXAMPLE_MAPPING = Lazy.of(() -> InfoScreen.KEY_MAPPING);

        // Event is on the mod event bus only on the physical client
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(EXAMPLE_MAPPING.get());
        }
    }

    // Event is on the Forge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Only call code once as the tick event is called twice every tick
            while (ClientModEvents.EXAMPLE_MAPPING.get().consumeClick()) {
                // Execute logic to perform on click here
                InfoScreen.open(Minecraft.getInstance());
            }
        }
    }
}