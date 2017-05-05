/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Ironclad Gameplay Tweaks) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.ironclad.EventHandlers;

import mods.ironclad.config.IroncladConfig;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by CovertJaguar on 4/30/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class HorseSpeedEventHandler implements IIroncladEventHandler {
    private static UUID HORSE_UUID = UUID.fromString("13822227-f299-4b3b-a24f-186884d4e705");
    private static UUID UNDEAD_UUID = UUID.fromString("4d8b275b-8047-4911-9932-0c4c676d4c2e");
    private static UUID MULE_UUID = UUID.fromString("b9107d0a-5e38-4323-beb4-9f2e1629c035");
    public static HorseSpeedEventHandler HORSE = new HorseSpeedEventHandler(HORSE_UUID, () -> IroncladConfig.horseSpeedModifier, HorseType.HORSE);
    public static HorseSpeedEventHandler UNDEAD = new HorseSpeedEventHandler(UNDEAD_UUID, () -> IroncladConfig.undeadHorseSpeedModifier, HorseType.ZOMBIE, HorseType.SKELETON);
    public static HorseSpeedEventHandler MULE = new HorseSpeedEventHandler(MULE_UUID, () -> IroncladConfig.muleSpeedModifier, HorseType.MULE, HorseType.DONKEY);
    private AttributeModifier speedModifier;
    private final UUID id;
    private final Supplier<Float> settingSupplier;
    private final HorseType[] horseTypes;

    private HorseSpeedEventHandler(UUID id, Supplier<Float> settingSupplier, HorseType... horseTypes) {
        this.id = id;
        this.settingSupplier = settingSupplier;
        this.horseTypes = horseTypes;
        speedModifier = getNewModifier();
    }

    @Override
    public boolean isEnabled() {
        return settingSupplier.get() != 0F;
    }

    public void reset() {
        speedModifier = getNewModifier();
    }

    private AttributeModifier getNewModifier() {
        return new AttributeModifier(id, "Speed Modifier", settingSupplier.get(), 1).setSaved(false);
    }

    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityHorse) {
            EntityHorse horse = (EntityHorse) event.getEntity();
            if (ArrayUtils.contains(horseTypes, horse.getType()) && !horse.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(speedModifier)) {
                horse.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(speedModifier);
            }
        }
    }
}
