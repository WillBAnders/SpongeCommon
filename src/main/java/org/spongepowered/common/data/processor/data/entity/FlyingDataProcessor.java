/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.Optional;

public class FlyingDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Boolean, FlyingData, ImmutableFlyingData> {

    public FlyingDataProcessor() {
        super(Entity.class, Keys.IS_FLYING);
    }

    @Override
    protected Value.Mutable<Boolean> constructMutableValue(Boolean actualValue) {
        return new SpongeMutableValue<>(Keys.IS_FLYING, actualValue);
    }

    @Override
    protected FlyingData createManipulator() {
        return new SpongeFlyingData();
    }

    @Override
    protected boolean set(Entity entity, Boolean value) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;
            entityPlayer.capabilities.isFlying = value;
            entityPlayer.sendPlayerAbilities();
        } else {
            entity.isAirBorne = value;
        }
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return Optional.of(((EntityPlayer) entity).capabilities.isFlying);
        }
        return Optional.of(entity.isAirBorne);
    }

    @Override
    protected Value.Immutable<Boolean> constructImmutableValue(Boolean value) {
        return SpongeImmutableValue.cachedOf(Keys.IS_FLYING, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
