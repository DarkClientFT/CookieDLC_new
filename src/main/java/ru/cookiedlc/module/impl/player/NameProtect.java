package ru.cookiedlc.module.impl.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.TextSetting;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.event.events.render.TextFactoryEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NameProtect extends Module {
    TextSetting nameSetting = new TextSetting("Name", "Nickname that will be replaced with yours").setText("Protect").setMax(16);
    BooleanSetting friendsSetting = new BooleanSetting("Friends","Hides friends' nicknames").setValue(true);

    public NameProtect() {
        super("NameProtect","Name Protect", ModuleCategory.PLAYER);
        setup(nameSetting, friendsSetting);
    }

    @EventHandler
    public void onTextFactory(TextFactoryEvent e) {
        e.replaceText(mc.getSession().getUsername(), nameSetting.getText());
        if (friendsSetting.isValue()) FriendUtils.getFriends().forEach(friend -> e.replaceText(friend.getName(), nameSetting.getText()));
    }
}
