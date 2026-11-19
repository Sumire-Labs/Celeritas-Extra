/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.hud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sumirelabs.celeritasextra.client.ServerTickRateEstimator;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketTimeUpdate;

import io.netty.channel.ChannelHandlerContext;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    // Netty overrides are not obfuscated. Select Packet explicitly to exclude the bridge method.
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), remap = false)
    private void celeritasExtra$sampleTime(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (!((NetworkManager) (Object) this).isChannelOpen())
            return;
        if (packet instanceof SPacketTimeUpdate time) {
            ServerTickRateEstimator.INSTANCE.record(this, time.getTotalWorldTime(), System.nanoTime());
        } else if (packet instanceof SPacketJoinGame || packet instanceof SPacketRespawn) {
            ServerTickRateEstimator.INSTANCE.reset(this);
        }
    }

    @Inject(method = "channelInactive", at = @At("HEAD"), remap = false)
    private void celeritasExtra$disconnect(ChannelHandlerContext context, CallbackInfo ci) {
        ServerTickRateEstimator.INSTANCE.reset(this);
    }

}
