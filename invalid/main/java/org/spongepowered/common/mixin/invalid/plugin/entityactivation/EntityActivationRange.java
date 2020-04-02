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
package org.spongepowered.common.mixin.invalid.plugin.entityactivation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.fused.FusedExplosive;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.activation.ActivationCapabilityBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.AbstractChunkProviderBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.EntityActivationModCategory;
import org.spongepowered.common.config.category.EntityActivationRangeCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.util.math.AxisAlignedBBAccessor;

import java.util.Map;

public class EntityActivationRange {

    private static final ImmutableMap<Byte, String> activationTypeMappings = new ImmutableMap.Builder<Byte, String>()
            .put((byte) 1, "monster")
            .put((byte) 2, "creature")
            .put((byte) 3, "aquatic")
            .put((byte) 4, "ambient")
            .put((byte) 5, "misc")
            .build();

    static AxisAlignedBB maxBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB miscBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB creatureBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB monsterBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB aquaticBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB ambientBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB tileEntityBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    static Map<Byte, Integer> maxActivationRanges = Maps.newHashMap();

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity Entity to get type for
     * @return group id
     */
    public static byte initializeEntityActivationType(final Entity entity) {

        // account for entities that dont extend EntityMob, EntityAmbientCreature, EntityCreature
        if (((IMob.class.isAssignableFrom(entity.getClass())
                || IRangedAttackMob.class.isAssignableFrom(entity.getClass())) && (entity.getClass() != MonsterEntity.class))
                || SpongeImplHooks.isCreatureOfType(entity, EntityClassification.MONSTER)) {
            return 1; // Monster
        } else if (SpongeImplHooks.isCreatureOfType(entity, EntityClassification.CREATURE)) {
            return 2; // Creature
        } else if (SpongeImplHooks.isCreatureOfType(entity, EntityClassification.WATER_CREATURE)) {
            return 3; // Aquatic
        } else if (SpongeImplHooks.isCreatureOfType(entity, EntityClassification.AMBIENT)) {
            return 4; // Ambient
        } else {
            return 5; // Misc
        }
    }

    /**
     * Initialize entity activation state.
     *
     * @param entity Entity to check
     */
    public static void initializeEntityActivationState(final Entity entity) {
        final ActivationCapabilityBridge spongeEntity = (ActivationCapabilityBridge) entity;
        if (((WorldBridge) entity.world).bridge$isFake()) {
            return;
        }

        // types that should always be active
        if (entity instanceof PlayerEntity && !SpongeImplHooks.isFakePlayer(entity)
            || entity instanceof ThrowableEntity
            || entity instanceof EnderDragonEntity
            || entity instanceof MultiPartEntityPart
            || entity instanceof WitherEntity
            || entity instanceof DamagingProjectileEntity
            || entity instanceof EntityWeatherEffect
            || entity instanceof TNTEntity
            || entity instanceof EnderCrystalEntity
            || entity instanceof FireworkRocketEntity
            || entity instanceof FallingBlockEntity) // Always tick falling blocks
        {
            return;
        }

        final EntityActivationRangeCategory config =
            ((WorldInfoBridge) entity.world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getEntityActivationRange();
        final EntityType type = ((org.spongepowered.api.entity.Entity) entity).getType();
        if (type == EntityTypes.UNKNOWN || !(type instanceof SpongeEntityType)) {
            spongeEntity.activation$setDefaultActivationState(true);
            return;
        }
        final SpongeEntityType spongeType = (SpongeEntityType) type;
        final byte activationType = spongeEntity.activation$getActivationType();
        if (!spongeType.isActivationRangeInitialized()) {
            addEntityToConfig(entity.world, spongeType, activationType);
            spongeType.setActivationRangeInitialized(true);
        }

        final EntityActivationModCategory entityMod = config.getModList().get(spongeType.getModId().toLowerCase());
        final int defaultActivationRange = config.getDefaultRanges().get(activationTypeMappings.get(activationType));
        if (entityMod == null) {
            // use default activation range
            spongeEntity.activation$setActivationRange(defaultActivationRange);
            if (defaultActivationRange > 0) {
                spongeEntity.activation$setDefaultActivationState(false);
            }
        } else {
            if (!entityMod.isEnabled()) {
                spongeEntity.activation$setDefaultActivationState(true);
                return;
            }

            final Integer defaultModActivationRange = entityMod.getDefaultRanges().get(activationTypeMappings.get(activationType));
            final Integer entityActivationRange = entityMod.getEntityList().get(type.getName().toLowerCase());
            if (defaultModActivationRange != null && entityActivationRange == null) {
                spongeEntity.activation$setActivationRange(defaultModActivationRange);
                if (defaultModActivationRange > 0) {
                    spongeEntity.activation$setDefaultActivationState(false);
                }
            } else if (entityActivationRange != null) {
                spongeEntity.activation$setActivationRange(entityActivationRange);
                if (entityActivationRange > 0) {
                    spongeEntity.activation$setDefaultActivationState(false);
                }
            }
        }
    }

    /**
     * Utility method to grow an AABB without creating a new AABB or touching
     * the pool, so we can re-use ones we have.
     *
     * @param target The AABB to modify
     * @param source The AABB to get initial coordinates from
     * @param x The x value to expand by
     * @param y The y value to expand by
     * @param z The z value to expand by
     */
    public static void growBb(final AxisAlignedBB target, final AxisAlignedBB source, final int x, final int y, final int z) {
        ((AxisAlignedBBAccessor) target).accessor$setMinX(source.minX - x);
        ((AxisAlignedBBAccessor) target).accessor$setMinY(source.minY - y);
        ((AxisAlignedBBAccessor) target).accessor$setMinZ(source.minZ - z);
        ((AxisAlignedBBAccessor) target).accessor$setMaxX(source.maxX + x);
        ((AxisAlignedBBAccessor) target).accessor$setMaxY(source.maxY + y);
        ((AxisAlignedBBAccessor) target).accessor$setMaxZ(source.maxZ + z);
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world The world to perform activation checks in
     */
    public static void activateEntities(final World world) {
        if (((WorldBridge) world).bridge$isFake()) {
            return;
        }

        for (final PlayerEntity player : world.playerEntities) {

            int maxRange = 0;
            for (final Integer range : maxActivationRanges.values()) {
                if (range > maxRange) {
                    maxRange = range;
                }
            }

            maxRange = Math.min((((org.spongepowered.api.world.World) world).getViewDistance() << 4) - 8, maxRange);
            ((ActivationCapabilityBridge) player).activation$setActivatedTick(SpongeImpl.getServer().getTickCounter());
            growBb(maxBB, player.getBoundingBox(), maxRange, 256, maxRange);

            final int i = MathHelper.floor(maxBB.minX / 16.0D);
            final int j = MathHelper.floor(maxBB.maxX / 16.0D);
            final int k = MathHelper.floor(maxBB.minZ / 16.0D);
            final int l = MathHelper.floor(maxBB.maxZ / 16.0D);

            for (int i1 = i; i1 <= j; ++i1) {
                for (int j1 = k; j1 <= l; ++j1) {
                    final ServerWorld worldserver = (ServerWorld) world;
                    final Chunk chunk = ((AbstractChunkProviderBridge) worldserver.getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(i1, j1);
                    if (chunk != null) {
                        activateChunkEntities(player, chunk);
                    }
                }
            }
        }
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param chunk Chunk to check for activation
     */
    private static void activateChunkEntities(final PlayerEntity player, final Chunk chunk) {
        for (int i = 0; i < chunk.getEntityLists().length; ++i) {

            for (final Object o : chunk.getEntityLists()[i]) {
                final Entity entity = (Entity) o;
                final EntityType type = ((org.spongepowered.api.entity.Entity) entity).getType();
                final ActivationCapabilityBridge spongeEntity = (ActivationCapabilityBridge) entity;
                final long currentTick = SpongeImpl.getServer().getTickCounter();
                if (!((EntityBridge) entity).bridge$shouldTick()) {
                    continue;
                }
                if (type == EntityTypes.UNKNOWN) {
                    spongeEntity.activation$setActivatedTick(currentTick);
                    continue;
                }

                if (currentTick > spongeEntity.activation$getActivatedTick()) {
                    if (spongeEntity.activation$getDefaultActivationState()) {
                        spongeEntity.activation$setActivatedTick(currentTick);
                        continue;
                    }

                    // check if activation cache needs to be updated
                    if (spongeEntity.activation$requiresActivationCacheRefresh()) {
                        EntityActivationRange.initializeEntityActivationState(entity);
                        spongeEntity.activation$requiresActivationCacheRefresh(false);
                    }
                    // check for entity type overrides
                    final byte activationType = spongeEntity.activation$getActivationType();
                    final int bbActivationRange = spongeEntity.activation$getActivationRange();

                    if (activationType == 5) {
                        growBb(miscBB, player.getBoundingBox(), bbActivationRange, 256, bbActivationRange);
                    } else if (activationType == 4) {
                        growBb(ambientBB, player.getBoundingBox(), bbActivationRange, 256, bbActivationRange);
                    } else if (activationType == 3) {
                        growBb(aquaticBB, player.getBoundingBox(), bbActivationRange, 256, bbActivationRange);
                    } else if (activationType == 2) {
                        growBb(creatureBB, player.getBoundingBox(), bbActivationRange, 256, bbActivationRange);
                    } else {
                        growBb(monsterBB, player.getBoundingBox(), bbActivationRange, 256, bbActivationRange);
                    }

                    switch (spongeEntity.activation$getActivationType()) {
                        case 1:
                            if (monsterBB.intersects(entity.getBoundingBox())) {
                                spongeEntity.activation$setActivatedTick(currentTick);
                            }
                            break;
                        case 2:
                            if (creatureBB.intersects(entity.getBoundingBox())) {
                                spongeEntity.activation$setActivatedTick(currentTick);
                            }
                            break;
                        case 3:
                            if (aquaticBB.intersects(entity.getBoundingBox())) {
                                spongeEntity.activation$setActivatedTick(currentTick);
                            }
                            break;
                        case 4:
                            if (ambientBB.intersects(entity.getBoundingBox())) {
                                spongeEntity.activation$setActivatedTick(currentTick);
                            }
                            break;
                        case 5:
                        default:
                            if (miscBB.intersects(entity.getBoundingBox())) {
                                spongeEntity.activation$setActivatedTick(currentTick);
                            }
                    }
                }
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity Entity to check
     * @return Whether entity should still be maintained active
     */
    public static boolean checkEntityImmunities(final Entity entity) {
        // quick checks.
        if (((EntityAccessor) entity).accessor$getFire() > 0) {
            return true;
        }
        if (!(entity instanceof Projectile)) {
            if (!entity.getPassengers().isEmpty() || entity.getRidingEntity() != null) {
                return true;
            }
        } else if (!((Projectile) entity).isOnGround()) {
            return true;
        }
        // special cases.
        if (entity instanceof LivingEntity) {
            final LivingEntity living = (LivingEntity) entity;
            if (living.hurtTime > 0 || living.getActivePotionEffects().size() > 0) {
                return true;
            }
            if (entity instanceof MobEntity && (((LivingEntityAccessor) entity).accessor$getRevengeTarget() != null || ((MobEntity) entity).getAttackTarget() != null)) {
                return true;
            }
            if (entity instanceof VillagerEntity && ((VillagerEntity) entity).isMating()) {
                return true;
            }
            if (entity instanceof AnimalEntity) {
                final AnimalEntity animal = (AnimalEntity) entity;
                if (animal.isChild() || animal.isInLove()) {
                    return true;
                }
                if (entity instanceof SheepEntity && ((SheepEntity) entity).getSheared()) {
                    return true;
                }
            }
            if (entity instanceof FusedExplosive && ((FusedExplosive) entity).isPrimed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity The entity to check for activity
     * @return Whether the given entity should be active
     */
    public static boolean checkIfActive(final Entity entity) {
        // Never safe to skip fireworks or entities not yet added to chunk
        if (entity instanceof PlayerEntity || entity.world.isRemote || !entity.addedToChunk || entity instanceof FireworkRocketEntity) {
            return true;
        }
        final ChunkBridge activeChunk = ((ActiveChunkReferantBridge) entity).bridge$getActiveChunk();
        if (activeChunk == null) {
            // Should never happen but just in case for mods, always tick
            return true;
        }

        if (!activeChunk.bridge$isActive()) {
            return false;
        }

        // If in forced chunk or is player
        if (activeChunk.bridge$isPersistedChunk() || (!SpongeImplHooks.isFakePlayer(entity) && entity instanceof ServerPlayerEntity)) {
            return true;
        }

        final long currentTick = SpongeImpl.getServer().getTickCounter();
        final ActivationCapabilityBridge spongeEntity = (ActivationCapabilityBridge) entity;
        boolean isActive = spongeEntity.activation$getActivatedTick() >= currentTick || spongeEntity.activation$getDefaultActivationState();

        // Should this entity tick?
        if (!isActive) {
            if ((currentTick - spongeEntity.activation$getActivatedTick() - 1) % 20 == 0) {
                // Check immunities every 20 ticks.
                if (checkEntityImmunities(entity)) {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    spongeEntity.activation$setActivatedTick(currentTick + 20);
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (!spongeEntity.activation$getDefaultActivationState() && entity.ticksExisted % 4 == 0 && !checkEntityImmunities(entity)) {
            isActive = false;
        }

        if (isActive && !activeChunk.bridge$areNeighborsLoaded()) {
            isActive = false;
        }

        return isActive;
    }

    public static void addEntityToConfig(final World world, final SpongeEntityType type, final byte activationType) {
        checkNotNull(world, "world");
        checkNotNull(type, "type");

        final SpongeConfig<WorldConfig> worldConfigAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();

        final boolean autoPopulate = worldConfigAdapter.getConfig().getEntityActivationRange().autoPopulateData();
        boolean requiresSave = false;
        String entityType = "misc";
        entityType = EntityActivationRange.activationTypeMappings.get(activationType);
        final String entityModId = type.getModId().toLowerCase();
        final String entityId = type.getName().toLowerCase();
        final EntityActivationRangeCategory activationCategory = globalConfigAdapter.getConfig().getEntityActivationRange();
        EntityActivationModCategory entityMod = activationCategory.getModList().get(entityModId);
        Integer defaultActivationRange = activationCategory.getDefaultRanges().get(entityType);
        if (defaultActivationRange == null) {
            defaultActivationRange = 32;
        }
        Integer activationRange = activationCategory.getDefaultRanges().get(entityType);

        if (autoPopulate && entityMod == null) {
            entityMod = new EntityActivationModCategory();
            activationCategory.getModList().put(entityModId, entityMod);
            requiresSave = true;
        }
        if (entityMod != null) {
            final Integer modActivationRange = entityMod.getDefaultRanges().get(entityType);
            if (autoPopulate && modActivationRange == null) {
                entityMod.getDefaultRanges().put(entityType, defaultActivationRange);
                requiresSave = true;
            } else if (modActivationRange != null && modActivationRange > activationRange) {
                activationRange = modActivationRange;
            }

            final Integer entityActivationRange = entityMod.getEntityList().get(entityId);
            if (autoPopulate && entityActivationRange == null) {
                entityMod.getEntityList().put(entityId, entityMod.getDefaultRanges().get(entityType));
                requiresSave = true;
            }
            if (entityActivationRange != null && entityActivationRange > activationRange) {
                activationRange = entityActivationRange;
            }
        }

        // check max ranges
        final Integer maxRange = maxActivationRanges.get(activationType);
        if (maxRange == null) {
            maxActivationRanges.put(activationType, activationRange);
        } else if (activationRange > maxRange) {
            maxActivationRanges.put(activationType, activationRange);
        }

        if (autoPopulate && requiresSave) {
            globalConfigAdapter.save();
        }
    }
}
