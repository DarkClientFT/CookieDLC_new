package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.common.util.entity.SimulatedPlayer;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.task.TaskPriority;
import ru.cookiedlc.common.util.task.scripts.Script;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.event.events.player.PostTickEvent;
import ru.cookiedlc.event.events.player.RotationUpdateEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationConfig;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;
import ru.cookiedlc.module.impl.combat.killaura.rotation.modes.snap.WatchAIMode;

import java.util.Random;
import java.util.stream.Stream;

@Setter
@Getter
@SuppressWarnings("unused")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Spider extends Module {
    Script script = new Script();
    StopWatch stopWatch = new StopWatch();

    SelectSetting mode = new SelectSetting("Режим", "Выбирает режим")
            .value("Блоки", "СпукиТайм", "ФанТайм").selected("Блоки");

    ValueSetting delay = new ValueSetting("Задержка", "Задержка использования ведра")
            .range(0.1f, 1.0f).setValue(0.4f)
            .visible(() -> mode.isSelected("WaterBucket"));

    BooleanSetting holdShift = new BooleanSetting("Зажимать шифт", "Зажимает шифт при лазании")
            .setValue(true)
            .visible(() -> mode.isSelected("WaterBucket"));

    BooleanSetting silentUse = new BooleanSetting("Брать ведро невидимо", "Меняет слот невидимо")
            .setValue(true)
            .visible(() -> mode.isSelected("WaterBucket"));

    BooleanSetting holdSpace = new BooleanSetting("Прыгать в начале", "Прыгает в начале лазания")
            .setValue(false)
            .visible(() -> mode.isSelected("WaterBucket"));

    @NonFinal
    java.util.Timer spookyTimer = new java.util.Timer();
    @NonFinal
    boolean canUseSpooky = true;
    @NonFinal
    long lastWallJumpMs = 0L;
    static final long WALL_JUMP_COOLDOWN_MS = 250L;
    final Random random = new Random();

    @NonFinal
    boolean canUseWaterBucket = true;
    @NonFinal
    StopWatch useCooldownWaterBucket = new StopWatch();

    @NonFinal
    boolean canUseWaterBucket2 = true;
    @NonFinal
    StopWatch useCooldownWaterBucket2 = new StopWatch();


    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT);
        setup(mode, delay, holdShift, silentUse, holdSpace);
    }

    @Override
    public void deactivate() {
        if (mode.isSelected("Ведро лавы")) {
            spookyTimer.cancel();
            spookyTimer = new java.util.Timer();
            canUseSpooky = true;

            if (mc.options != null) {
                mc.options.sneakKey.setPressed(false);
                mc.options.jumpKey.setPressed(false);
            }
        }

        if (mode.isSelected("WaterBucket")) {
            canUseWaterBucket = true;
            useCooldownWaterBucket.reset();

            if (mc.options != null) {
                mc.options.sneakKey.setPressed(false);
                mc.options.jumpKey.setPressed(false);
            }
        }

        if (mode.isSelected("СпукиТайм")) {
            canUseWaterBucket2 = true;
            useCooldownWaterBucket2.reset();

            if (mc.options != null) {
                mc.options.sneakKey.setPressed(false);
                mc.options.jumpKey.setPressed(false);
            }
        }
    }

    private int useItemSequence = 0;
    private double lastWaterY = 0;
    private long lastWaterPlaceTime = 0;


    @EventHandler
    public void onPostTick(PostTickEvent e) {

        if (mode.isSelected("ФанТайм")) {
            if (mc.options.jumpKey.isPressed()) return;
            Box playerBox = mc.player.getBoundingBox().expand(-1e-3);
            Box box = new Box(playerBox.minX, playerBox.minY, playerBox.minZ, playerBox.maxX, playerBox.minY + 0.5, playerBox.maxZ);
            if (stopWatch.finished(400) && PlayerIntersectionUtil.isBox(box, this::hasCollision)) {
                box = new Box(playerBox.minX - 0.3, playerBox.minY + 1, playerBox.minZ - 0.3, playerBox.maxX, playerBox.maxY, playerBox.maxZ);
                if (PlayerIntersectionUtil.isBox(box, this::hasCollision)) {
                    mc.player.setOnGround(true);
                    mc.player.velocity.y = 0.6;
                } else {
                    mc.player.setOnGround(true);
                    mc.player.jump();
                }
            }
        }

        if ( mode.isSelected("FixedPitch")) {

            if (mc.player.horizontalCollision) {
                mc.options.forwardKey.setPressed(false);
            }

            int waterBucketSlot = PlayerInventoryUtil.getHotbarSlotId(i -> mc.player.getInventory().getStack(i).getItem() == Items.WATER_BUCKET);
            boolean hasWaterBucketInMain = mc.player.getMainHandStack().getItem() == Items.WATER_BUCKET;

            if (waterBucketSlot >= 0 && waterBucketSlot <= 8) {
                mc.options.jumpKey.setPressed(true);
                mc.options.sneakKey.setPressed(true);
                mc.player.getInventory().setSelectedSlot(waterBucketSlot);
            } else {
                logDirect("Нужно ведро воды в хотбаре");
            }

            if (hasWaterBucketInMain) {
                BlockPos placeablePos = getPlaceableWaterBlock();
                if (placeablePos != null && stopWatch.finished(340)) {
                    mc.player.setPitch(75);
                    Vec3d vec = placeablePos.toCenterPos();
                    Direction direction = Direction.getFacing(vec.x - mc.player.getX(), vec.y - mc.player.getY(), vec.z - mc.player.getZ());

                    PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND);
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    stopWatch.reset();
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.35, mc.player.getVelocity().z);
                }
            }
        }

        if (mode.isSelected("Ведро лавы")) {
            handleLavaBucketMode();
        }

        if (mode.isSelected("WaterBucket")) {
            handleWaterBucketMode();
        }

        if (mode.isSelected("СпукиТайм")) {
            handleWaterBucketMode2();
        }
    }

    private void handleLavaBucketMode() {
        if (mc.player == null || mc.player.isTouchingWater())
            return;

        mc.player.setPitch(68.0F);

        if (!mc.player.horizontalCollision) {
            mc.options.sneakKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
            return;
        }

        int lavaSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.LAVA_BUCKET) {
                lavaSlot = i;
                break;
            }
        }

        if (lavaSlot == -1)
            return;

        if (mc.player.isOnGround()) {
            long now = System.currentTimeMillis();
            if (now - lastWallJumpMs > WALL_JUMP_COOLDOWN_MS) {
                mc.player.jump();
                lastWallJumpMs = now;
            }
        }
        mc.options.jumpKey.setPressed(true);

        if (canUseSpooky) {
            int currentSlot = mc.player.getInventory().selectedSlot;

            if (lavaSlot != currentSlot) {
                mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(lavaSlot));
            }

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

            if (lavaSlot != currentSlot) {
                mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(currentSlot));
            }

            double yBoost = 0.4D + (random.nextDouble() * 0.03D);
            mc.player.setVelocity(mc.player.getVelocity().x, yBoost, mc.player.getVelocity().z);

            canUseSpooky = false;
            int delayMs = 450 + random.nextInt(60) - 30;
            spookyTimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    canUseSpooky = true;
                }
            }, Math.max(150, delayMs));
        }

        mc.options.sneakKey.setPressed(true);
    }

    @EventHandler

    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() == EventType.PRE) {
            if (mode.isSelected("Блоки")) {
                boolean offHand = mc.player.getOffHandStack().getItem() instanceof BlockItem;
                int slotId = PlayerInventoryUtil.getHotbarSlotId(i -> mc.player.getInventory().getStack(i).getItem() instanceof BlockItem);
                BlockPos blockPos = findPos();
                if (script.isFinished() && (offHand || slotId != -1) && !blockPos.equals(BlockPos.ORIGIN)) {
                    ItemStack stack = offHand ? mc.player.getOffHandStack() : mc.player.getInventory().getStack(slotId);
                    Hand hand = offHand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    Vec3d vec = blockPos.toCenterPos();
                    Direction direction = Direction.getFacing(vec.x - mc.player.getX(), vec.y - mc.player.getY(), vec.z - mc.player.getZ());
                    Angle angle = AngleUtil.calculateAngle(vec.subtract(new Vec3d(direction.getVector()).multiply(0.1F)));
                    Angle.VecRotation vecRotation = new Angle.VecRotation(angle, angle.toVector());
                    RotationController.INSTANCE.rotateTo(vecRotation, mc.player, 1, new RotationConfig(new WatchAIMode(), true, true), TaskPriority.HIGH_IMPORTANCE_1, this);
                    if (canPlace(stack)) {
                        int prev = mc.player.inventory.selectedSlot;
                        if (!offHand) mc.player.inventory.selectedSlot = slotId;
                        mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(vec, direction.getOpposite(), blockPos, false));
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
                        if (!offHand) mc.player.inventory.selectedSlot = prev;
                    }
                }
            }
        }
    }


    private boolean canPlace(ItemStack stack) {
        BlockPos blockPos = getBlockPos();
        if (blockPos.getY() >= mc.player.getBlockY()) return false;
        BlockItem blockItem = (BlockItem) stack.getItem();
        VoxelShape shape = blockItem.getBlock().getDefaultState().getCollisionShape(mc.world, blockPos);
        if (shape.isEmpty()) return false;
        Box box = shape.getBoundingBox().offset(blockPos);
        return !box.intersects(mc.player.getBoundingBox()) && box.intersects(SimulatedPlayer.simulateLocalPlayer(4).boundingBox);
    }


    private BlockPos findPos() {
        BlockPos blockPos = getBlockPos();
        if (mc.world.getBlockState(blockPos).isSolid()) return BlockPos.ORIGIN;
        return Stream.of(blockPos.west(), blockPos.east(), blockPos.south(), blockPos.north()).filter(pos -> mc.world.getBlockState(pos).isSolid()).findFirst().orElse(BlockPos.ORIGIN);
    }

    private BlockPos getPlaceableWaterBlock() {
        BlockPos below = BlockPos.ofFloored(mc.player.getPos().add(0, -1.3, 0));
        if (mc.world.getBlockState(below).isSolidBlock(mc.world, below)) {
            return below;
        }

        for (Direction dir : Direction.values()) {
            BlockPos side = below.offset(dir);
            if (mc.world.getBlockState(side).isSolidBlock(mc.world, side)) {
                return side;
            }
        }

        return null;
    }


    private BlockPos getBlockPos() {
        return BlockPos.ofFloored(SimulatedPlayer.simulateLocalPlayer(1).pos.add(0, -1e-3, 0));
    }
    private boolean hasCollision(BlockPos blockPos) {
        return !mc.world.getBlockState(blockPos).getCollisionShape(mc.world, blockPos).isEmpty();
    }

    private void handleWaterBucketMode() {
        if (mc.player == null) return;

        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.45, mc.player.getVelocity().z);
            return;
        }

        if (!canUseWaterBucket && useCooldownWaterBucket.finished(getAppliedDelayMs())) {
            canUseWaterBucket = true;
        }

        if (silentUse.isValue()) {
            handleSilentWaterBucket();
        } else {
            handleVisibleWaterBucket();
        }

        if (!mc.player.horizontalCollision) {
            if (mc.options.sneakKey.isPressed() && holdShift.isValue()) {
                mc.options.sneakKey.setPressed(false);
            }

            if (mc.options.jumpKey.isPressed() && holdSpace.isValue()) {
                mc.options.jumpKey.setPressed(false);
            }
        }
    }

    private void handleSilentWaterBucket() {
        int waterSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.WATER_BUCKET) {
                waterSlot = i;
                break;
            }
        }

        if (waterSlot != -1 && mc.player.horizontalCollision) {
            handleWallClimb(waterSlot);
        }
    }

    private void handleVisibleWaterBucket() {
        if (mc.player.getMainHandStack().getItem() == Items.WATER_BUCKET && mc.player.horizontalCollision) {
            handleWallClimb(mc.player.getInventory().selectedSlot);
        }
    }

    private void handleWallClimb(int waterSlot) {
        if (holdSpace.isValue()) {
            if (mc.player.isOnGround()) {
                long now = System.currentTimeMillis();
                if (now - lastWallJumpMs > WALL_JUMP_COOLDOWN_MS) {
                    mc.player.jump();
                    lastWallJumpMs = now;
                }
            }
            mc.options.jumpKey.setPressed(true);
        }

        if (canUseWaterBucket) {
            int clientSlot = mc.player.getInventory().selectedSlot;
            mc.player.setPitch(75.0f);

            if (silentUse.isValue() && waterSlot != clientSlot) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(waterSlot));

                mc.player.getInventory().selectedSlot = waterSlot;
            }


            PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND);
            mc.player.setVelocity(mc.player.getVelocity().x, 0.43, mc.player.getVelocity().z);

            if (silentUse.isValue() && waterSlot != clientSlot) {

                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(clientSlot));
                mc.player.getInventory().selectedSlot = clientSlot;
            }

            canUseWaterBucket = false;
            useCooldownWaterBucket.reset();
        }

        if (holdShift.isValue()) {
            mc.options.sneakKey.setPressed(true);
        }
    }

    private int getAppliedDelayMs() {
        float base = delay.getValue();
        return (int)(base * 1000.0f);
    }

    private void handleWaterBucketMode2() {
        if (mc.player == null) return;

        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) {
            mc.player.velocity.y = 0.46;
            return;
        }

        if (!canUseWaterBucket2 && useCooldownWaterBucket2.finished(getAppliedDelayMs2())) {
            canUseWaterBucket2 = true;
        }

        handleSilentWaterBucket2();

        if (!mc.player.horizontalCollision) {
            if (mc.options.sneakKey.isPressed()) {
                mc.options.sneakKey.setPressed(false);
            }
            if (mc.options.jumpKey.isPressed()) {
                mc.options.jumpKey.setPressed(false);
            }
        }
    }

    private void handleSilentWaterBucket2() {
        int waterSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.WATER_BUCKET) {
                waterSlot = i;
                break;
            }
        }

        if (waterSlot != -1 && mc.player.horizontalCollision) {
            handleWallClimb2(waterSlot);
        }
    }

    private void handleWallClimb2(int waterSlot) {
        if (mc.player.isOnGround()) {
            long now = System.currentTimeMillis();
            if (now - lastWallJumpMs > WALL_JUMP_COOLDOWN_MS) {
                mc.player.jump();
                lastWallJumpMs = now;
            }
        }
        mc.options.jumpKey.setPressed(true);

        if (canUseWaterBucket2) {
            int clientSlot = mc.player.getInventory().selectedSlot;
            mc.player.setPitch(77.0f);

            if (waterSlot != clientSlot) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(waterSlot));
            }


            PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND);

            mc.player.velocity.y = 0.46;

            if (waterSlot != clientSlot) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(clientSlot));
            }

            canUseWaterBucket2 = false;
            useCooldownWaterBucket2.reset();
        }

        mc.options.sneakKey.setPressed(true);
    }

    private int getAppliedDelayMs2() {
        float base = 0.405f;
        return (int)(base * 1000.0f);
    }

}
