package draylar.attributed.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

// NYI
public interface CriticalHitEvents {

    Event<PreCriticalHit> PRE = EventFactory.createArrayBacked(PreCriticalHit.class,
            listeners -> player -> {
                for(PreCriticalHit event : listeners) {
                    ActionResult result = event.preCriticalHit(player);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    @FunctionalInterface
    interface PreCriticalHit {
        ActionResult preCriticalHit(PlayerEntity player);
    }
}