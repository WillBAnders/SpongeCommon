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
package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.Optional;

public class TileEntityCommandValueProcessor extends AbstractSpongeValueProcessor<TileEntityCommandBlock, String> {

    public TileEntityCommandValueProcessor() {
        super(TileEntityCommandBlock.class, Keys.COMMAND);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value.Mutable<String> constructMutableValue(String actualValue) {
        return new SpongeMutableValue<>(Keys.COMMAND, actualValue);
    }

    @Override
    protected boolean set(TileEntityCommandBlock container, String value) {
        container.getCommandBlockLogic().commandStored = value;
        return true;
    }

    @Override
    protected Optional<String> getVal(TileEntityCommandBlock container) {
        return Optional.ofNullable(container.getCommandBlockLogic().commandStored);
    }

    @Override
    protected Value.Immutable<String> constructImmutableValue(String value) {
        return constructMutableValue(value).asImmutable();
    }

}
