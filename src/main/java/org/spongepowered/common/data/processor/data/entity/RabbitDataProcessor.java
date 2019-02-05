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

import net.minecraft.entity.passive.EntityRabbit;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRabbitData;
import org.spongepowered.api.data.manipulator.mutable.entity.RabbitData;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRabbitData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.entity.SpongeRabbitType;
import org.spongepowered.common.registry.type.entity.RabbitTypeRegistryModule;

import java.util.Optional;

public class RabbitDataProcessor
        extends AbstractEntitySingleDataProcessor<EntityRabbit, RabbitType, RabbitData, ImmutableRabbitData> {

    public RabbitDataProcessor() {
        super(EntityRabbit.class, Keys.RABBIT_TYPE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(EntityRabbit entity, RabbitType value) {
        if (value instanceof SpongeRabbitType) {
            entity.setRabbitType(((SpongeRabbitType) value).type);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<RabbitType> getVal(EntityRabbit entity) {
        return Optional.ofNullable(RabbitTypeRegistryModule.RABBIT_IDMAP.get(entity.getRabbitType()));
    }

    @Override
    protected Value.Mutable<RabbitType> constructMutableValue(RabbitType actualValue) {
        return new SpongeMutableValue<>(this.key, actualValue);
    }

    @Override
    protected Value.Immutable<RabbitType> constructImmutableValue(RabbitType value) {
        return SpongeImmutableValue.cachedOf(this.key, value);
    }

    @Override
    protected RabbitData createManipulator() {
        return new SpongeRabbitData();
    }

}
