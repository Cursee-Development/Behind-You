package com.cursee.behind_you.mixin;

import com.cursee.behind_you.util.TextManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/// Must be in "mixins"/common section of config, not "server"

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Unique
    private boolean behind_you$sent = false;

    @Unique
    private int behind_you$secondsSinceLastMessage = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void behind_you$tick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = player.serverLevel();

        if (level.getGameTime() % 2 != 0) return; // only operate every-other tick

        Vec3 playerCenter = player.position().add(0, player.getBbHeight() / 2.0, 0);

        Vec3 lookDirection = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize(); // ignore pitch (Y rot)

        Vec3 behindPos = playerCenter.subtract(lookDirection.scale(3.0));

        double halfWidth = 1.5;
        double height = 2.0;
        AABB boundingBox = new AABB(
                behindPos.x - halfWidth, behindPos.y - height / 2.0, behindPos.z - halfWidth,
                behindPos.x + halfWidth, behindPos.y + height / 2.0, behindPos.z + halfWidth
        );

        // Render bounding box vertices
        // for (Vec3 pos : behind_you$getVertices(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)) {
        //     level.sendParticles(ParticleTypes.SMALL_FLAME, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.01);
        // }

        List<Monster> monstersBehind = level.getEntitiesOfClass(Monster.class, boundingBox);
        if (!monstersBehind.isEmpty() && !behind_you$sent && behind_you$secondsSinceLastMessage <= 15) {
            String message = TextManager.instance.getText();
            if (message != null && !message.isBlank()) {
                player.sendSystemMessage(Component.literal(message));
                behind_you$sent = true;
            }
        }
        else if (behind_you$sent && behind_you$secondsSinceLastMessage > 15) {
            behind_you$sent = false;
            behind_you$secondsSinceLastMessage = 0;
        }

        if (level.getGameTime() % 20 == 0) {
            behind_you$secondsSinceLastMessage++;
        }
    }

    @Unique @SuppressWarnings("unused")
    private static Vec3[] behind_you$getVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Vec3[] {
                new Vec3(minX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ)
        };
    }
}
