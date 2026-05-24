package ru.cookiedlc.module.api;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.module.impl.combat.*;
import ru.cookiedlc.module.impl.misc.*;
import ru.cookiedlc.module.impl.movement.*;
import ru.cookiedlc.module.impl.player.*;
import ru.cookiedlc.module.impl.render.*;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleRepository {
    List<Module> modules = new ArrayList<>();

    public void setup() {
        register(
                new ServerHelper(),
                new WaterSpeed(),
                new ClickAction(),
                new ItemTweaks(),
                new Hud(),
                new AuctionHelper(),
                new ProjectilePrediction(),
                new XRay(),
                new KillAura(),
                new BlockESP(),
                new AutoSwap(),
                new NoFriendDamage(),
                new HitBoxModule(),
                new AntiBot(),
                new AutoSprint(),
                new NoPush(),
                new ElytraHelper(),
                new ClanUpgrade(),
                new NoDelay(),
                new AutoRespawn(),
                new NoSlow(),
                new GuiMove(),
                new ElytraFly(),
                new Blink(),
                new ElytraRecast(),
                new AutoTool(),
                new CameraTweaks(),
                new HandTweaks(),
                new BlockHighLight(),
                new EntityESP(),
                new AutoTotem(),
                new DebugCamera(),
                new TriggerBot(),
                new ContainerStealer(),
                new AutoTpAccept(),
                new Arrows(),
                new AutoLeave(),
                new WorldTweaks(),
                new NoRender(),
                new Criticals(),
                new TargetPearl(),
                new NameProtect(),
                new SeeInvisible(),
                new AutoUse(),
                new NoInteract(),
                new CrossHair(),
                new FireWorkBooster(),
                new Spider(),
                new ServerRPSpoofer(),
                new Fly(),
                new NoEffects(),
                new Strafe(),
                new TargetStrafe(),
                new SPDuelsJoiner(),
                new ElytraMotion(),
                new Plugins(),
                new AirStuck(),
                new AutoPotion(),
                new AutoBuy(),
                new FakePlayer(),
                new Jesus(),
                new NoWeb(),
                new AutoDuel(),
                new BowSpammer(),
                new ShiftTap(),
                new Velocity(),
                new SkeletESP(),
                new JumpCircle(),
                new Optimizer(),
                new MobsLol(),
                new PigESP()
        );
    }

    public void register(Module... module) {
        modules.addAll(List.of(module));
    }


    public List<Module> modules() {
        return modules;
    }
}
