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
package org.spongepowered.common.data.datasync.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.accessor.entity.EntityAccessor;

import java.util.List;
import java.util.Optional;

public class EntityCustomNameVisibleConverter extends DataParameterConverter<Boolean> {

    public EntityCustomNameVisibleConverter() {
        super(EntityAccessor.accessor$getCustomNameVisible());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Boolean currentValue, final Boolean value) {
        return Optional.of(DataTransactionResult.builder()
                .replace(Value.immutableOf(Keys.IS_CUSTOM_NAME_VISIBLE, currentValue))
                .success(Value.immutableOf(Keys.IS_CUSTOM_NAME_VISIBLE, value))
                .result(DataTransactionResult.Type.SUCCESS)
                .build());
    }

    @Override
    public Boolean getValueFromEvent(final Boolean originalValue, final List<Immutable<?>> immutableValues) {
        for (final Immutable<?> immutableValue : immutableValues) {
            if (immutableValue.getKey() == Keys.IS_CUSTOM_NAME_VISIBLE.get()) {
                return (Boolean) immutableValue.get();
            }
        }
        return originalValue;
    }
}
