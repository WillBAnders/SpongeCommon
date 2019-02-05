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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableGrowthData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeGrowthData;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;

import java.util.Optional;

@Mixin(BlockCactus.class)
public abstract class MixinBlockCactus extends MixinBlock {

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean onSpongeCactusDamage(Entity entity, DamageSource source, float damage, net.minecraft.world.World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (world.isRemote) {
            return entity.attackEntityFrom(source, damage);
        }
        try {
            Location location = new Location((World) world, pos.getX(), pos.getY(), pos.getZ());
            DamageSource.CACTUS = new MinecraftBlockDamageSource(CatalogKey.minecraft("cactus"), location);
            return entity.attackEntityFrom(DamageSource.CACTUS, damage);
        } finally {
            DamageSource.CACTUS = source;
        }
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getGrowthData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableGrowthData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableGrowthData) {
            int growth = ((ImmutableGrowthData) manipulator).growthStage().get();
            return Optional.of((BlockState) blockState.with(BlockCactus.AGE, growth));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends Value<E>> key, E value) {
        if (key.equals(Keys.GROWTH_STAGE)) {
            int growth = (Integer) value;
            return Optional.of((BlockState) blockState.with(BlockCactus.AGE, growth));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutableGrowthData getGrowthData(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeGrowthData.class, blockState.get(BlockCactus.AGE), 0, 15);
    }

}
