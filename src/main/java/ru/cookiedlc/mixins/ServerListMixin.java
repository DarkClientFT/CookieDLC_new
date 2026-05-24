package ru.cookiedlc.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerList.class)
public class ServerListMixin {
    @Unique private final List<ServerInfo> sponsorServers = List.of(
            new ServerInfo("Лучшие дуэли", "cracked.applepvp.club", ServerInfo.ServerType.REALM),
            new ServerInfo("ФанТайм", "funtime.su", ServerInfo.ServerType.REALM),
            new ServerInfo("ХолиВорлд", "shield.holyworld.me", ServerInfo.ServerType.REALM),
            new ServerInfo("РилиВорлд", "mc.reallyworld.ru", ServerInfo.ServerType.REALM),
            new ServerInfo("Тестинг АЧ", "pvp.thunderhack.pw", ServerInfo.ServerType.REALM)
    );

    @Shadow @Final private List<ServerInfo> servers;

    @Inject(method = "loadFile", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/ServerList;hiddenServers:Ljava/util/List;", ordinal = 0))
    private void loadFileHook(CallbackInfo ci) {
        servers.addAll(sponsorServers);
    }

    @Redirect(method = "saveFile", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;add(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean saveFileHook(NbtList instance, Object o, @Local(ordinal = 0) ServerInfo info) {
        if (sponsorServers.contains(info)) return true;
        return instance.add((NbtElement) o);
    }
}
