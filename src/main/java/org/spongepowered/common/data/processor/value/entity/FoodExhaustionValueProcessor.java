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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class FoodExhaustionValueProcessor extends AbstractSpongeValueProcessor<EntityPlayer, Double> {

    public FoodExhaustionValueProcessor() {
        super(EntityPlayer.class, Keys.EXHAUSTION);
    }

    @Override
    public BoundedValue.Mutable<Double> constructMutableValue(Double defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.EXHAUSTION)
            .minimum(0D)
            .maximum(4.0)
            .value(defaultValue)
            .build();
    }

    @Override
    protected boolean set(EntityPlayer container, Double value) {
        container.getFoodStats().foodExhaustionLevel = value.floatValue();
        return true;
    }

    @Override
    protected Optional<Double> getVal(EntityPlayer container) {
        return Optional.of((double) container.getFoodStats().foodExhaustionLevel);
    }

    @Override
    protected Value.Immutable<Double> constructImmutableValue(Double value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
