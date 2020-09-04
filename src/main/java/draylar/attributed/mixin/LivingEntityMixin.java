package draylar.attributed.mixin;

import draylar.attributed.CustomEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /**
     * Registers all custom attributes from {@link CustomEntityAttributes} to this entity.
     *
     * @param cir  mixin callback info
     */
    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void initAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(CustomEntityAttributes.DIG_SPEED);
        cir.getReturnValue().add(CustomEntityAttributes.CRIT_CHANCE);
    }
}
