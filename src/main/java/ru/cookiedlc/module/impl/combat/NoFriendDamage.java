package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.player.AttackEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoFriendDamage extends Module {
    public NoFriendDamage() {
        super("NoFriendDamage", "No Friend Damage", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        e.setCancelled(FriendUtils.isFriend(e.getEntity()));
    }
}

