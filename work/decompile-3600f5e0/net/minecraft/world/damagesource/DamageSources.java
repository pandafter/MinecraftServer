package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityFireballFireball;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.entity.projectile.EntityWitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3D;

public class DamageSources {

    private final IRegistry<DamageType> damageTypes;
    private final DamageSource inFire;
    private final DamageSource lightningBolt;
    private final DamageSource onFire;
    private final DamageSource lava;
    private final DamageSource hotFloor;
    private final DamageSource inWall;
    private final DamageSource cramming;
    private final DamageSource drown;
    private final DamageSource starve;
    private final DamageSource cactus;
    private final DamageSource fall;
    private final DamageSource flyIntoWall;
    private final DamageSource fellOutOfWorld;
    private final DamageSource generic;
    private final DamageSource magic;
    private final DamageSource wither;
    private final DamageSource dragonBreath;
    private final DamageSource dryOut;
    private final DamageSource sweetBerryBush;
    private final DamageSource freeze;
    private final DamageSource stalagmite;
    private final DamageSource outsideBorder;
    private final DamageSource genericKill;

    public DamageSources(IRegistryCustom iregistrycustom) {
        this.damageTypes = iregistrycustom.registryOrThrow(Registries.DAMAGE_TYPE);
        this.inFire = this.source(DamageTypes.IN_FIRE);
        this.lightningBolt = this.source(DamageTypes.LIGHTNING_BOLT);
        this.onFire = this.source(DamageTypes.ON_FIRE);
        this.lava = this.source(DamageTypes.LAVA);
        this.hotFloor = this.source(DamageTypes.HOT_FLOOR);
        this.inWall = this.source(DamageTypes.IN_WALL);
        this.cramming = this.source(DamageTypes.CRAMMING);
        this.drown = this.source(DamageTypes.DROWN);
        this.starve = this.source(DamageTypes.STARVE);
        this.cactus = this.source(DamageTypes.CACTUS);
        this.fall = this.source(DamageTypes.FALL);
        this.flyIntoWall = this.source(DamageTypes.FLY_INTO_WALL);
        this.fellOutOfWorld = this.source(DamageTypes.FELL_OUT_OF_WORLD);
        this.generic = this.source(DamageTypes.GENERIC);
        this.magic = this.source(DamageTypes.MAGIC);
        this.wither = this.source(DamageTypes.WITHER);
        this.dragonBreath = this.source(DamageTypes.DRAGON_BREATH);
        this.dryOut = this.source(DamageTypes.DRY_OUT);
        this.sweetBerryBush = this.source(DamageTypes.SWEET_BERRY_BUSH);
        this.freeze = this.source(DamageTypes.FREEZE);
        this.stalagmite = this.source(DamageTypes.STALAGMITE);
        this.outsideBorder = this.source(DamageTypes.OUTSIDE_BORDER);
        this.genericKill = this.source(DamageTypes.GENERIC_KILL);
    }

    private DamageSource source(ResourceKey<DamageType> resourcekey) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourcekey));
    }

    private DamageSource source(ResourceKey<DamageType> resourcekey, @Nullable Entity entity) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourcekey), entity);
    }

    private DamageSource source(ResourceKey<DamageType> resourcekey, @Nullable Entity entity, @Nullable Entity entity1) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(resourcekey), entity, entity1);
    }

    public DamageSource inFire() {
        return this.inFire;
    }

    public DamageSource lightningBolt() {
        return this.lightningBolt;
    }

    public DamageSource onFire() {
        return this.onFire;
    }

    public DamageSource lava() {
        return this.lava;
    }

    public DamageSource hotFloor() {
        return this.hotFloor;
    }

    public DamageSource inWall() {
        return this.inWall;
    }

    public DamageSource cramming() {
        return this.cramming;
    }

    public DamageSource drown() {
        return this.drown;
    }

    public DamageSource starve() {
        return this.starve;
    }

    public DamageSource cactus() {
        return this.cactus;
    }

    public DamageSource fall() {
        return this.fall;
    }

    public DamageSource flyIntoWall() {
        return this.flyIntoWall;
    }

    public DamageSource fellOutOfWorld() {
        return this.fellOutOfWorld;
    }

    public DamageSource generic() {
        return this.generic;
    }

    public DamageSource magic() {
        return this.magic;
    }

    public DamageSource wither() {
        return this.wither;
    }

    public DamageSource dragonBreath() {
        return this.dragonBreath;
    }

    public DamageSource dryOut() {
        return this.dryOut;
    }

    public DamageSource sweetBerryBush() {
        return this.sweetBerryBush;
    }

    public DamageSource freeze() {
        return this.freeze;
    }

    public DamageSource stalagmite() {
        return this.stalagmite;
    }

    public DamageSource fallingBlock(Entity entity) {
        return this.source(DamageTypes.FALLING_BLOCK, entity);
    }

    public DamageSource anvil(Entity entity) {
        return this.source(DamageTypes.FALLING_ANVIL, entity);
    }

    public DamageSource fallingStalactite(Entity entity) {
        return this.source(DamageTypes.FALLING_STALACTITE, entity);
    }

    public DamageSource sting(EntityLiving entityliving) {
        return this.source(DamageTypes.STING, entityliving);
    }

    public DamageSource mobAttack(EntityLiving entityliving) {
        return this.source(DamageTypes.MOB_ATTACK, entityliving);
    }

    public DamageSource noAggroMobAttack(EntityLiving entityliving) {
        return this.source(DamageTypes.MOB_ATTACK_NO_AGGRO, entityliving);
    }

    public DamageSource playerAttack(EntityHuman entityhuman) {
        return this.source(DamageTypes.PLAYER_ATTACK, entityhuman);
    }

    public DamageSource arrow(EntityArrow entityarrow, @Nullable Entity entity) {
        return this.source(DamageTypes.ARROW, entityarrow, entity);
    }

    public DamageSource trident(Entity entity, @Nullable Entity entity1) {
        return this.source(DamageTypes.TRIDENT, entity, entity1);
    }

    public DamageSource mobProjectile(Entity entity, @Nullable EntityLiving entityliving) {
        return this.source(DamageTypes.MOB_PROJECTILE, entity, entityliving);
    }

    public DamageSource spit(Entity entity, @Nullable EntityLiving entityliving) {
        return this.source(DamageTypes.SPIT, entity, entityliving);
    }

    public DamageSource windCharge(Entity entity, @Nullable EntityLiving entityliving) {
        return this.source(DamageTypes.WIND_CHARGE, entity, entityliving);
    }

    public DamageSource fireworks(EntityFireworks entityfireworks, @Nullable Entity entity) {
        return this.source(DamageTypes.FIREWORKS, entityfireworks, entity);
    }

    public DamageSource fireball(EntityFireballFireball entityfireballfireball, @Nullable Entity entity) {
        return entity == null ? this.source(DamageTypes.UNATTRIBUTED_FIREBALL, entityfireballfireball) : this.source(DamageTypes.FIREBALL, entityfireballfireball, entity);
    }

    public DamageSource witherSkull(EntityWitherSkull entitywitherskull, Entity entity) {
        return this.source(DamageTypes.WITHER_SKULL, entitywitherskull, entity);
    }

    public DamageSource thrown(Entity entity, @Nullable Entity entity1) {
        return this.source(DamageTypes.THROWN, entity, entity1);
    }

    public DamageSource indirectMagic(Entity entity, @Nullable Entity entity1) {
        return this.source(DamageTypes.INDIRECT_MAGIC, entity, entity1);
    }

    public DamageSource thorns(Entity entity) {
        return this.source(DamageTypes.THORNS, entity);
    }

    public DamageSource explosion(@Nullable Explosion explosion) {
        return explosion != null ? this.explosion(explosion.getDirectSourceEntity(), explosion.getIndirectSourceEntity()) : this.explosion((Entity) null, (Entity) null);
    }

    public DamageSource explosion(@Nullable Entity entity, @Nullable Entity entity1) {
        return this.source(entity1 != null && entity != null ? DamageTypes.PLAYER_EXPLOSION : DamageTypes.EXPLOSION, entity, entity1);
    }

    public DamageSource sonicBoom(Entity entity) {
        return this.source(DamageTypes.SONIC_BOOM, entity);
    }

    public DamageSource badRespawnPointExplosion(Vec3D vec3d) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(DamageTypes.BAD_RESPAWN_POINT), vec3d);
    }

    public DamageSource outOfBorder() {
        return this.outsideBorder;
    }

    public DamageSource genericKill() {
        return this.genericKill;
    }
}
