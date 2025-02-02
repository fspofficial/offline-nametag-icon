package com.syanicxd.offlinenametagicon.mixin;

import net.minecraft.client.render.OverlayTexture;
import com.syanicxd.offlinenametagicon.offlinenametagiconMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger("offlinenametagiconMod");
    private static final Identifier BADGE_TEXTURE = Identifier.of(offlinenametagiconMod.MOD_ID, "textures/badge.png");

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void renderBadge(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && isCurrentPlayer((PlayerEntity) entity)) {
            MinecraftClient client = MinecraftClient.getInstance();
            Entity cameraEntity = client.getCameraEntity();
            if (cameraEntity == null) return;

            int textWidth = client.textRenderer.getWidth(text);
            float badgeSize = 10.0F;
            float yOffset = entity.getHeight() + 0.5F;

            matrices.push();
            matrices.translate(0.0, yOffset, 0.0);

            // Simplified rotation/scaling
            matrices.multiply(client.gameRenderer.getCamera().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            float badgeX = textWidth / 2 + 1; 
            float badgeY = -badgeSize / 10;

            renderBadgeIcon(matrices, vertexConsumers, light, badgeX, badgeY, badgeSize);

            matrices.pop();
        }
    }

    private void renderBadgeIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float x, float y, float size) {
        matrices.push();
        matrices.translate(x, y, 0);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(BADGE_TEXTURE));
        Matrix4f matrix = matrices.peek().getPositionMatrix(); // <-- Now uses JOML's Matrix4f

        // Define vertices for the badge
        buffer.vertex(matrix, 0, size, 0)
            .color(0xFFFFFFFF)
            .texture(0, 1)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1);
        buffer.vertex(matrix, size, size, 0)
            .color(0xFFFFFFFF)
            .texture(1, 1)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1);
        buffer.vertex(matrix, size, 0, 0)
            .color(0xFFFFFFFF)
            .texture(1, 0)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1);
        buffer.vertex(matrix, 0, 0, 0)
            .color(0xFFFFFFFF)
            .texture(0, 0)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1);

        matrices.pop();
    }

    private boolean isCurrentPlayer(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.getUuid().equals(player.getUuid());
    }
}