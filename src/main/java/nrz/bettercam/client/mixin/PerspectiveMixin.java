package nrz.bettercam.client.mixin;

import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class PerspectiveMixin {
    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    private void onCycle(CallbackInfoReturnable<CameraType> cir) {
        CameraType current = (CameraType) (Object) this;
        if (current == CameraType.FIRST_PERSON) {
            cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
        } else {
            cir.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}
