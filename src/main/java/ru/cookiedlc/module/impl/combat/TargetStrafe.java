package ru.cookiedlc.module.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.Setting;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.player.PlayerVelocityStrafeEvent;
import ru.cookiedlc.event.events.player.TickEvent;

public class TargetStrafe extends Module {
    public final BooleanSetting pauseInLiquids = new BooleanSetting("PauseInLiquids", "Pause strafe when in liquids").setValue(false);
    public final BooleanSetting pauseWhileSneaking = new BooleanSetting("PauseWhileSneaking", "Pause strafe when sneaking").setValue(false);
    public final BooleanSetting onlyPlayers = new BooleanSetting("OnlyPlayers", "Target only players").setValue(false);
    public final ValueSetting speedFactor = new ValueSetting("Speed", "Speed multiplier").setValue(8.0F).range(1.0F, 15.0F);
    public final ValueSetting distance = new ValueSetting("Distance", "Activation distance").setValue(3.0F).range(0.5F, 15.0F);
    public final BooleanSetting predictMovement = new BooleanSetting("PredictMovement", "Predict target movement").setValue(true);
    public final ValueSetting predictionFactor = new ValueSetting("PredictionFactor", "Movement prediction multiplier").setValue(2.0F).range(1.0F, 5.0F);
    public final BooleanSetting smoothMovement = new BooleanSetting("SmoothMovement", "Smooth acceleration").setValue(true);

    private Entity targetEntity = null;
    private Vec3d lastTargetPos = null;
    private Vec3d predictedPos = null;
    private double[] lastMotion = new double[]{0.0D, 0.0D};

    public static TargetStrafe getInstance() {
        return (TargetStrafe) Instance.get(TargetStrafe.class);
    }

    public TargetStrafe() {
        super("TargetStrafe", "TargetStrafe", ModuleCategory.COMBAT);
        this.setup(new Setting[]{
                this.pauseInLiquids,
                this.pauseWhileSneaking,
                this.onlyPlayers,
                this.speedFactor,
                this.distance,
                this.predictMovement,
                this.predictionFactor,
                this.smoothMovement
        });
    }

    @Override
    public void deactivate() {
        this.targetEntity = null;
        this.lastTargetPos = null;
        this.predictedPos = null;
        this.lastMotion = new double[]{0.0D, 0.0D};
    }

    @Override
    public void activate() {
        this.targetEntity = null;
        this.lastTargetPos = null;
        this.predictedPos = null;
        this.lastMotion = new double[]{0.0D, 0.0D};
    }

    private void updateTarget() {
        KillAura killAura = KillAura.getInstance();
        LivingEntity auraTarget = null;
        if (killAura != null && killAura.isState()) {
            auraTarget = killAura.getTarget();
        }

        if (auraTarget == null) {
            this.targetEntity = null;
            this.lastTargetPos = null;
            this.predictedPos = null;
        } else {
            this.targetEntity = auraTarget;
            if (this.targetEntity != null) {
                Vec3d currentPos = this.targetEntity.getPos();

                if (this.lastTargetPos == null) {
                    this.lastTargetPos = currentPos;
                    this.predictedPos = currentPos;
                } else {
                    Vec3d velocity = new Vec3d(
                            currentPos.x - this.lastTargetPos.x,
                            currentPos.y - this.lastTargetPos.y,
                            currentPos.z - this.lastTargetPos.z
                    );

                    if (this.predictMovement.isValue()) {
                        this.predictedPos = currentPos.add(
                                velocity.x * this.predictionFactor.getValue(),
                                velocity.y * this.predictionFactor.getValue(),
                                velocity.z * this.predictionFactor.getValue()
                        );

                        int chunkX = (int) this.predictedPos.x >> 4;
                        int chunkZ = (int) this.predictedPos.z >> 4;
                        if (!mc.world.isChunkLoaded(chunkX, chunkZ)) {
                            this.predictedPos = currentPos;
                        }
                    } else {
                        this.predictedPos = currentPos;
                    }

                    this.lastTargetPos = currentPos;
                }
            }
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player) return false;
        if (entity instanceof PlayerEntity && FriendUtils.isFriend((PlayerEntity) entity)) return false;
        if (this.onlyPlayers.isValue() && !(entity instanceof PlayerEntity)) return false;
        return entity instanceof LivingEntity || entity instanceof EnderDragonEntity;
    }

    private void handleDistanceMode() {
        if (this.targetEntity != null && mc.player.hurtTime <= 0) {
            if (this.isMoving()) {
                Vec3d targetPos = (this.predictMovement.isValue() && this.predictedPos != null) ? this.predictedPos : this.targetEntity.getPos();
                double dist = mc.player.getPos().distanceTo(targetPos);
                float distValue = this.distance.getValue();

                if (dist <= distValue) {
                    BlockPos blockPos = mc.player.getBlockPos();
                    BlockState blockState = mc.world.getBlockState(blockPos);
                    float slipperiness = blockState.getBlock().getSlipperiness();
                    float horizontalFriction = mc.player.isOnGround() ? slipperiness * 0.91F : 0.91F;
                    float verticalFriction = mc.player.isOnGround() ? slipperiness : 0.99F;

                    double actualSpeed = this.speedFactor.getValue() * 0.01D * horizontalFriction * verticalFriction;
                    double[] directionMotion = this.getDirectionToPoint(mc.player.getPos(), targetPos, actualSpeed);

                    if (this.smoothMovement.isValue()) {
                        double accelFactor = 0.6D;
                        directionMotion[0] = this.lastMotion[0] + (directionMotion[0] - this.lastMotion[0]) * accelFactor;
                        directionMotion[1] = this.lastMotion[1] + (directionMotion[1] - this.lastMotion[1]) * accelFactor;
                    }

                    this.lastMotion[0] = directionMotion[0];
                    this.lastMotion[1] = directionMotion[1];

                    mc.player.addVelocity(directionMotion[0], 0.0D, directionMotion[1]);
                }
            }
        }
    }

    private double[] getDirectionToPoint(Vec3d from, Vec3d to, double speed) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        return len == 0.0D ? new double[]{0.0D, 0.0D} : new double[]{dx / len * speed, dz / len * speed};
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (this.isState() && mc.player != null && mc.world != null) {
            boolean inLiquid = mc.player.isTouchingWater();
            boolean isSneaking = mc.player.isSneaking();

            if ((!inLiquid || !this.pauseInLiquids.isValue()) &&
                    (!isSneaking || !this.pauseWhileSneaking.isValue())) {
            } else {
                this.targetEntity = null;
            }
        } else {
            this.targetEntity = null;
        }
    }

    @EventHandler
    public void onVelocity(PlayerVelocityStrafeEvent e) {
        if (this.isState() && mc.player != null) {
            boolean inLiquid = mc.player.isTouchingWater();
            boolean isSneaking = mc.player.isSneaking();

            if ((!inLiquid || !this.pauseInLiquids.isValue()) &&
                    (!isSneaking || !this.pauseWhileSneaking.isValue())) {
                this.updateTarget();
                this.handleDistanceMode();
            }
        }
    }

    public void triggerAttackBoost() {
    }
}