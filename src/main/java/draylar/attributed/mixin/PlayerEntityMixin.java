package draylar.attributed.mixin;

import draylar.attributed.CustomEntityAttributes;
import draylar.attributed.event.CriticalHitEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

    @Unique
    private Entity attributed_cachedTarget;

    @Inject(
            method = "attack",
            at = @At("HEAD"))
    private void storeContext(Entity target, CallbackInfo ci) {
        attributed_cachedTarget = target;
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
        TypedActionResult<Double> result = CriticalHitEvents.BEFORE.invoker().beforeCriticalHit((PlayerEntity) (Object) this, attributed_cachedTarget, getMainHandStack(), customChance);

        // If the result failed, the chance is now 0.
        if(result.getResult().equals(ActionResult.FAIL)) {
            customChance = 0;
        } else if(result.getResult().equals(ActionResult.SUCCESS)) {
            customChance = 1;
        } else {
            customChance = result.getValue();
        }

        return critical || world.random.nextDouble() < customChance;
    }

    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getMovementSpeed()F"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void postCriticalHit(Entity target, CallbackInfo ci, float f, float h, boolean bl, boolean bl2, int j, boolean wasCritical, boolean bl4, double d) {
        if(wasCritical) {
            CriticalHitEvents.AFTER.invoker().afterCriticalHit((PlayerEntity) (Object) this, attributed_cachedTarget, getMainHandStack());
        }
    }

    @ModifyConstant(
            method = "attack",
            constant = @Constant(floatValue = 1.5f))
    private float adjustCriticalHitModifier(float original) {
        EntityAttributeInstance instance = this.getAttributeInstance(CustomEntityAttributes.CRIT_DAMAGE);

        // Add each value from the attribute to the chance value, which should (roughly) range in the scale of [0, 1].
        if(instance != null) {
            original = (float) instance.computeValue();
        }

        return (float) CriticalHitEvents.CALCULATE_MODIFIER.invoker().calculateCriticalModifier((PlayerEntity) (Object) this, attributed_cachedTarget, getMainHandStack(), original);
    }
}
