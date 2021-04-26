package draylar.attributed.mixin;

import draylar.attributed.CustomEntityAttributes;
import draylar.attributed.event.CriticalHitEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Applies the Dig Speed attribute ({@link CustomEntityAttributes#DIG_SPEED} to players.
     *
     * @param f  original dig speed of player
     * @return   modified dig speed
     */
    @ModifyVariable(
            method = "getBlockBreakingSpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"
            ),
            index = 2
    )
    private float getBlockBreakingSpeed(float f) {
        EntityAttributeInstance instance = this.getAttributeInstance(CustomEntityAttributes.DIG_SPEED);

        for (EntityAttributeModifier modifier : instance.getModifiers()) {
            float amount = (float) modifier.getValue();

            if (modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                f += amount;
            } else {
                f *= (amount + 1);
            }
        }

        return f;
    }

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "JUMP",
                    ordinal = 2
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z",
                            ordinal = 1
                    )
            ),
            index = 8
    )
    private boolean attack(boolean critical) {
        double customChance = 0;
        EntityAttributeInstance instance = this.getAttributeInstance(CustomEntityAttributes.CRIT_CHANCE);

        // Add each value from the attribute to the chance value, which should (roughly) range in the scale of [0, 1].
        if(instance != null) {
            customChance = instance.computeValue();
        }

        // Call event listeners to adjust the critical hit ratio/value.
        TypedActionResult<Double> result = CriticalHitEvents.BEFORE.invoker().beforeCriticalHit((PlayerEntity) (Object) this, customChance);

        // If the result failed, the chance is now 0.
        if(result.getResult().equals(ActionResult.FAIL)) {
            customChance = 0;
        } else {
            customChance = result.getValue();
        }

        return critical || world.random.nextDouble() < customChance;
    }
}
