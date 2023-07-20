package com.doo.playerinfo.forge;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.ClientSideHandler;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.core.InfoUpdatePacket;
import com.doo.playerinfo.gui.InfoScreen;
import com.doo.playerinfo.utils.InfoRegisters;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(XPlayerInfo.MOD_ID)
public class XPlayerInfoForge {

    // Attribute Register
    public static final DeferredRegister<Attribute> ATTRIBUTE_REGISTRY =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, XPlayerInfo.MOD_ID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(XPlayerInfo.MOD_ID, "info_pack_sender"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public XPlayerInfoForge() {
        XPlayerInfo.init(0);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ExtractAttributes.register(attr -> ATTRIBUTE_REGISTRY.register(attr.getDescriptionId(), () -> attr));
        ATTRIBUTE_REGISTRY.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        INSTANCE.registerMessage(0, InfoUpdatePacket.class, InfoUpdatePacket::write, InfoUpdatePacket::new, ((packet, contextSupplier) -> {
            contextSupplier.get().enqueueWork(() -> ClientSideHandler.handle(packet));
            contextSupplier.get().setPacketHandled(true);
        }));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        InfoRegisters.initMinecraft();

        InfoItemCollector.start(event.getServer().getPlayerList().getPlayers(),
                (player, packet) -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet));
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = XPlayerInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InfoGroupItems.addClientSideGetter(Const.PICK_RANGE, minecraft -> minecraft.player == null ? 0 : minecraft.player.getBlockReach());
            InfoGroupItems.addClientSideGetter(Const.ATTACK_RANGE, minecraft ->
                    minecraft.player == null ? 0 : Math.max(minecraft.player.getEntityReach(), minecraft.player.getBlockReach()));
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