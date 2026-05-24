package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.impl.combat.killaura.attack.AttackPerpetrator;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RaytracingUtil;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TriggerBot extends Module {
    public static TriggerBot getInstance() {
        return Instance.get(TriggerBot.class);
    }

    private LivingEntity target = null;

    private final MultiSelectSetting attackSetting = new MultiSelectSetting("Attack setting", "Allows you to customize the attack")
            .value("Only Critical", "Dynamic Cooldown", "Break Shield", "UnPress Shield", "No Attack When Eat");

    public TriggerBot() {
        super("TriggerBot", "Trigger Bot", ModuleCategory.COMBAT);
        setup(attackSetting);
    }

    
    @Override
    public void deactivate() {
        target = null;
        super.deactivate();
    }

    
    @EventHandler
    public void onTick(TickEvent e) {
        EntityHitResult result = RaytracingUtil.raytraceEntity(3, AngleUtil.cameraAngle(), s -> !FriendUtils.isFriend(s.getName().getString()));
        if (result instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity entity) {
            AttackPerpetrator.AttackPerpetratorConfigurable config = new AttackPerpetrator.AttackPerpetratorConfigurable(target = entity, AngleUtil.cameraAngle(),
                    3.3F, attackSetting.getSelected(), new SelectSetting("Mode", "").value("Default"), target.getBoundingBox());
            Main.getInstance().getAttackPerpetrator().performAttack(config);
        } else target = null;
    }
}
