package ru.cookiedlc.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.cookiedlc.module.impl.misc.autobuy.holy.util.ItemDetector;

@Mixin(Screen.class)
public abstract class NbtCopyMixin {
    @Unique
    private boolean wasPressed = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isPressed = InputUtil.isKeyPressed(
                client.getWindow().getHandle(),
                InputUtil.GLFW_KEY_RIGHT_ALT
        );

        if (isPressed && !wasPressed) {
            handleKeyPress(client);
        }
        wasPressed = isPressed;
    }

    @Unique
    private void handleKeyPress(MinecraftClient client) {
        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen) || client.player == null) {
            return;
        }

        HandledScreenAccessor screenAccessor = (HandledScreenAccessor) handledScreen;
        int screenX = screenAccessor.getX();
        int screenY = screenAccessor.getY();

        double mouseX = client.mouse.getX() * (double)handledScreen.width / (double)client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * (double)handledScreen.height / (double)client.getWindow().getHeight();

        mouseX -= screenX;
        mouseY -= screenY;

        Slot slot = getHoveredSlot(handledScreen, mouseX, mouseY);
        if (slot == null) return;

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        showNbtInChat(client, ItemDetector.toTagString(ItemDetector.getTags(stack)));
    }

    @Unique
    private Slot getHoveredSlot(HandledScreen<?> screen, double mouseX, double mouseY) {
        for (Slot slot : screen.getScreenHandler().slots) {
            if (isPointInSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Unique
    private boolean isPointInSlot(Slot slot, double pointX, double pointY) {
        return pointX >= slot.x && pointX <= slot.x + 16 &&
                pointY >= slot.y && pointY <= slot.y + 16;
    }

    @Unique
    private void showNbtInChat(MinecraftClient client, String nbt) {
        MutableText copyButton = Text.literal("[Копировать]");
        copyButton.setStyle(copyButton.getStyle()
                .withColor(Formatting.GREEN)
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.literal("Нажмите чтобы скопировать NBT")
                ))
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        nbt
                ))
        );

        MutableText message = Text.literal("§a[NBT] §f");
        message.append(copyButton);
        message.append(" §7| §f" + (nbt.length() > 50 ? nbt.substring(0, 50) + "..." : nbt));

        client.player.sendMessage(message, false);
    }
}
