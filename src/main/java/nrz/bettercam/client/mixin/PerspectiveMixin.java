package nrz.bettercam.client.mixin;

import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public class PerspectiveMixin {
    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void onNext(CallbackInfoReturnable<Perspective> cir) {
        Perspective current = (Perspective) (Object) this;
        if (current == Perspective.FIRST_PERSON) {
            cir.setReturnValue(Perspective.THIRD_PERSON_BACK);
        } else {
            cir.setReturnValue(Perspective.FIRST_PERSON);
        }
    }
}
