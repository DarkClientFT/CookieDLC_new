package ru.cookiedlc.common.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.common.util.task.TaskPriority;
import ru.cookiedlc.common.util.task.scripts.Script;
import ru.cookiedlc.event.events.player.InputEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationConfig;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;
import ru.cookiedlc.ui.clickgui.MenuScreen;

import java.util.List;

@UtilityClass
public class PlayerInventoryComponent implements QuickImports {
    public final List<KeyBinding> moveKeys = List.of(mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey);
    public static final Script script = new Script(), postScript = new Script();
    public boolean canMove = true;

    public void tick() {
        script.update();
    }

    public void postMotion() {
        postScript.update();
    }

    public void input(InputEvent e) {
        if (!canMove) e.inputNone();
    }
    public void addTask(Runnable task) {
        if (script.isFinished() && MovingUtil.hasPlayerMovement()) {
            script.cleanup()
                    .addTickStep(0, PlayerInventoryComponent::disableMoveKeys)
                    .addTickStep(3, task::run)
                    .addTickStep(5, PlayerInventoryComponent::enableMoveKeys);
            return;
        }
        postScript.cleanup()
                .addTickStep(5, () -> {
                    task.run();
                    PlayerInventoryUtil.closeScreen(true);
                });
    }
    public void sendPacketWithOutEvent(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().send(packet, null);
    }

    private void rotateToCamera() {
        Module module = new Module("InventoryComponent","Inventory Component", ModuleCategory.PLAYER);
        module.state = true;
        RotationController.INSTANCE.rotateTo(AngleUtil.cameraAngle(), RotationConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, module);
    }

    public void disableMoveKeys() {
        canMove = false;
        unPressMoveKeys();
    }

    public void enableMoveKeys() {
        PlayerInventoryUtil.closeScreen(true);
        canMove = true;
        updateMoveKeys();
    }

    public void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode())));
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen != null && !PlayerIntersectionUtil.isChat(mc.currentScreen) && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen) && !(mc.currentScreen instanceof MenuScreen);
    }
}