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

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger("offlinenametagiconMod");
    private static final Identifier BADGE_TEXTURE = new Identifier(offlinenametagiconMod.MOD_ID, "textures/badge.png");
    private boolean isCurrentPlayer(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.getUuid().equals(player.getUuid());
    }

    @Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
    private void renderBadge(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        LOGGER.info("Attempting to render badge for entity: {}", entity);
        if (entity instanceof PlayerEntity && isCurrentPlayer((PlayerEntity) entity)) {
            LOGGER.info("Entity is the current player");

            MinecraftClient client = MinecraftClient.getInstance();

            float nametagHeight = 10.0F; // Minecraft's default font height

            float nametagWidth = client.textRenderer.getWidth(text);
            float yOffset = entity.getHeight() + 0.5F;

            matrices.push();
            matrices.translate(0.0, yOffset, 0.0);

            // Check if we're rendering in the inventory
            boolean isInventory = client.currentScreen != null;

            if (isInventory) {
                // Inventory rendering adjustments (needs to fix)
                matrices.multiply(client.gameRenderer.getCamera().getRotation().conjugate());
                matrices.scale(0.025F, -0.025F, 0.025F);
            } else {
                // In-game rendering
                matrices.multiply(client.gameRenderer.getCamera().getRotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);
            }

            float badgeSize = nametagHeight;
            float badgeX = isInventory ? nametagWidth / 2 + 1 : -nametagWidth / 2 - badgeSize - 1; 
            float badgeY = -nametagHeight / 10; 

            renderBadgeIcon(matrices, vertexConsumers, light, badgeX, badgeY, badgeSize, isInventory);

            matrices.pop();
        } else {
            LOGGER.info("Entity is not the current player");
        }
    }

    private void renderBadgeIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float x, float y, float size, boolean isInventory) {
        LOGGER.info("Attempting to render badge icon");
        matrices.push();
        matrices.translate(x, y, 0);

        if (isInventory) {
            matrices.scale(-1, 1, 1); // (needs to fix)
        }

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BADGE_TEXTURE));

        MinecraftClient.getInstance().getTextureManager().bindTexture(BADGE_TEXTURE);
        LOGGER.info("Texture bound: {}", BADGE_TEXTURE);

        float u0 = 0;
        float u1 = 1;
        float v0 = 0;
        float v1 = 1;
        int color = 0xFFFFFFFF;  // White color

        buffer.vertex(matrices.peek().getPositionMatrix(), 0, size, 0)
            .color(color)
            .texture(u0, v1)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1)
            .next();
        buffer.vertex(matrices.peek().getPositionMatrix(), size, size, 0)
            .color(color)
            .texture(u1, v1)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1)
            .next();
        buffer.vertex(matrices.peek().getPositionMatrix(), size, 0, 0)
            .color(color)
            .texture(u1, v0)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1)
            .next();
        buffer.vertex(matrices.peek().getPositionMatrix(), 0, 0, 0)
            .color(color)
            .texture(u0, v0)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(0, 0, 1)
            .next();

        LOGGER.info("Badge icon drawn");
        matrices.pop();
    }
}