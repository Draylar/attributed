package draylar.attributed.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public interface CriticalHitEvents {

    Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class,
            listeners -> (player, target, stack, chance) -> {
                TypedActionResult<Double> result = TypedActionResult.pass(chance);

                for(Before event : listeners) {
                    result = event.beforeCriticalHit(player, target, stack, result.getValue());

                    if (result.getResult() != ActionResult.PASS) {
                        return result;
                    }
                }

                return result;
            });

    Event<After> AFTER = EventFactory.createArrayBacked(After.class,
            listeners -> (player, target, stack) -> {
                for(After event : listeners) {
                    event.afterCriticalHit(player, target, stack);
                }
            });

    Event<Calculator> CALCULATE_MODIFIER = EventFactory.createArrayBacked(Calculator.class,
            listeners -> (player, target, stack, modifier) -> {
                for(Calculator event : listeners) {
                    modifier = event.calculateCriticalModifier(player, target, stack, modifier);
                }

                return modifier;
            });

    @FunctionalInterface
    interface Before {
        /**
         * Called before the random chance for a critical hit is tested. A critical hit applies a 50% damage bonus to an attack.
         *
         * <p>
         * {@code chance} represents the random chance the critical hit will succeed, with 1 being 100% (always crit), and 0 being 0% (no crit).
         * Listeners can adjust the returned double to modify the chance a critical hit lands.
         * This event is called after Attribute Modifiers are applied,
         *      so {@code chance} starts as the calculated value of the {@link draylar.attributed.CustomEntityAttributes#CRIT_CHANCE} attribute on the player.
         *
         * <p>
         * To account for overfill mechanics and tools that want to ensure a critical hit occurs (or does not occur),
         *   {@code chance} is not capped between 0 and 1 between listener calls. Values <0 or >1 may be present.
         *
         * <p>
         * As with all events that use {@link TypedActionResult} or {@link ActionResult}, the result returned impacts future listener calls.
         * <ul>
         *     <li>{@link ActionResult#SUCCESS} will cancel all future listeners and cause the critical hit to land.</li>
         *     <li>{@link ActionResult#FAIL} will cancel all future listeners and deny a critical hit from occurring.</li>
         *     <li>{@link ActionResult#PASS} will send the specified chance to the next listener. If all listeners return {@link ActionResult#PASS}, {@link ActionResult#SUCCESS} is assumed.</li>
         * </ul>
         *
         * @param player player that is attempting to land a critical hit
         * @param target the entity being attacked by the player
         * @param stack the stack currently held in the player's main hand
         * @param chance the current chance of the critical hit succeeding
         * @return a {@link TypedActionResult} that describes whether the critical hit can continue,
         *      with the attached double describing the chance of the critical hit if the returned {@link ActionResult} is not {@link ActionResult#FAIL}
         */
        TypedActionResult<Double> beforeCriticalHit(PlayerEntity player, Entity target, ItemStack stack, double chance);
    }

    @FunctionalInterface
    interface After {
        /**
         * Called after a player lands a critical hit.
         *
         * @param player player that is attempting to land a critical hit
         * @param target the entity being attacked by the player
         * @param stack the stack in the player's main hand, which was used to land the critical hit
         */
        void afterCriticalHit(PlayerEntity player, Entity target, ItemStack stack);
    }

    @FunctionalInterface
    interface Calculator {
        /**
         * Called when the multiplier/modifier of a critical hit is calculated.
         *
         * <p>
         * By default, the modifier of a critical hit is {@code 1.5D}.
         * For an attack that deals 10 damage, this represents a boost of 5, for a final value of 15 damage.
         *
         * @param player player dealing the critical hit
         * @param target target being attacked
         * @param stack stack being used to land the critical hit (player's main-hand stack)
         * @param modifier the current damage modifier of the critical hit
         * @return an adjusted damage modifier for the critical hit
         */
        double calculateCriticalModifier(PlayerEntity player, Entity target, ItemStack stack, double modifier);
    }
}