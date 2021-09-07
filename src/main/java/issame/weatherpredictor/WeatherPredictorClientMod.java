package issame.weatherpredictor;

import static issame.weatherpredictor.WeatherPredictorMod.WEATH_PRED_ITEM;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

@Environment(EnvType.CLIENT)
public class WeatherPredictorClientMod implements ClientModInitializer {
    private int rainTime = 0;
    private int rainTimePrev = 0;
    private int maxRainTime = 0;
    private boolean isRainingPrev = false;
    private boolean isRaining = false;

    @Override
    public void onInitializeClient() {

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            ServerWorldProperties properties = server.getSaveProperties().getMainWorldProperties();
            rainTimePrev = rainTime;
            rainTime = properties.getClearWeatherTime() > 0 ? properties.getClearWeatherTime() : properties.getRainTime();
            if (rainTime > 1 && (rainTime > maxRainTime || rainTime > rainTimePrev || rainTimePrev - rainTime > 1)) {
                maxRainTime = rainTime;
            }
            isRainingPrev = isRaining;
            isRaining = properties.isRaining();
        });

        FabricModelPredicateProviderRegistry.register(WEATH_PRED_ITEM, new Identifier("rain"), new UnclampedModelPredicateProvider() {
            private double rain;
            private double step;
            private long lastTick;

            public float unclampedCall(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
                Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
                if (entity == null) {
                    return 0.0F;
                } else {
                    if (clientWorld == null && entity.world instanceof ClientWorld) {
                        clientWorld = (ClientWorld) entity.world;
                    }

                    if (clientWorld == null) {
                        return 0.0F;
                    } else {
                        double e;
                        if (clientWorld.getDimension().isNatural()) {
                            e = 0.5F * rainTime / maxRainTime;
                            if (isRaining) e += 0.5F;
                            if (isRaining != isRainingPrev) e += 0.5F;
                            if (clientWorld.isThundering()) e += 0.5F * (Math.random() - 0.5F);
                        } else {
                            e = Math.random();
                        }
                        e = this.getRain(clientWorld, e);
                        return (float) e;
                    }
                }
            }

            private double getRain(World world, double e) {
                if (world.getTime() != this.lastTick) {
                    this.lastTick = world.getTime();
                    double d = e - this.rain;
                    d = MathHelper.floorMod(d + 0.5D, 1.0D) - 0.5D;
                    this.step += d * 0.1D;
                    this.step *= 0.9D;
                    this.rain = MathHelper.floorMod(this.rain + this.step, 1.0D);
                }
                return this.rain;
            }
        });
    }
}
