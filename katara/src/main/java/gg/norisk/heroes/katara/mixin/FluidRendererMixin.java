package gg.norisk.heroes.katara.mixin;

import gg.norisk.heroes.katara.client.render.IFluidRendererExt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin implements IFluidRendererExt {
    @Shadow
    @Final
    private Sprite[] lavaSprites;

    @Shadow
    @Final
    private Sprite[] waterSprites;

    @Shadow
    protected static boolean isSameFluid(FluidState fluidState, FluidState fluidState2) {
        return false;
    }

    @Shadow
    public static boolean shouldRenderSide(BlockRenderView blockRenderView, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        return false;
    }

    @Shadow
    protected static boolean isSideCovered(BlockView blockView, BlockPos blockPos, Direction direction, float f, BlockState blockState) {
        return false;
    }

    @Shadow
    protected abstract float getFluidHeight(BlockRenderView blockRenderView, Fluid fluid, BlockPos blockPos);

    @Shadow
    protected abstract float calculateFluidHeight(BlockRenderView blockRenderView, Fluid fluid, float f, float g, float h, BlockPos blockPos);

    @Shadow
    protected abstract float getFluidHeight(BlockRenderView blockRenderView, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState);

    @Shadow
    protected abstract int getLight(BlockRenderView blockRenderView, BlockPos blockPos);

    @Shadow
    protected abstract void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n);

    private void vertex2(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
        vertexConsumer.vertex(matrix4f, f, g, h).color(i, j, k, 1.0F).texture(l, m).light(n).normal(0.0F, 1.0F, 0.0F);
    }

    @Shadow
    private Sprite waterOverlaySprite;

    public void katara_renderFluid(MatrixStack matrixStack, BlockRenderView blockRenderView, Vec3d pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Color waterColor) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        // Beispiel für Vertex-Anwendung:
        // float x = (float) pos.x - 0.5f;
        // float y = (float) pos.y - 0.5f;
        // float z = (float) pos.z - 0.5f;

        boolean bl = fluidState.isIn(FluidTags.LAVA);
        Sprite[] sprites = bl ? this.lavaSprites : this.waterSprites;
        int i;
        if (waterColor != null) {
            i = waterColor.getRGB();
        } else {
            i = bl ? 16777215 : BiomeColors.getWaterColor(blockRenderView, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
        }
        float f = (float) (i >> 16 & 0xFF) / 255.0F;
        float g = (float) (i >> 8 & 0xFF) / 255.0F;
        float h = (float) (i & 0xFF) / 255.0F;
        var blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        /*BlockState blockState2 = blockRenderView.getBlockState(blockPos.offset(Direction.DOWN));
        FluidState fluidState2 = blockState2.getFluidState();
        BlockState blockState3 = blockRenderView.getBlockState(blockPos.offset(Direction.UP));
        FluidState fluidState3 = blockState3.getFluidState();
        BlockState blockState4 = blockRenderView.getBlockState(blockPos.offset(Direction.NORTH));
        FluidState fluidState4 = blockState4.getFluidState();
        BlockState blockState5 = blockRenderView.getBlockState(blockPos.offset(Direction.SOUTH));
        FluidState fluidState5 = blockState5.getFluidState();
        BlockState blockState6 = blockRenderView.getBlockState(blockPos.offset(Direction.WEST));
        FluidState fluidState6 = blockState6.getFluidState();
        BlockState blockState7 = blockRenderView.getBlockState(blockPos.offset(Direction.EAST));
        FluidState fluidState7 = blockState7.getFluidState();*/
        boolean bl2 = true; //!isSameFluid(fluidState, fluidState3);
        boolean bl3 = true;//shouldRenderSide(blockRenderView, blockPos, fluidState, blockState, Direction.DOWN, fluidState2) && !isSideCovered(blockRenderView, blockPos, Direction.DOWN, 0.8888889F, blockState2);
        boolean bl4 = true;//shouldRenderSide(blockRenderView, blockPos, fluidState, blockState, Direction.NORTH, fluidState4);
        boolean bl5 = true;//shouldRenderSide(blockRenderView, blockPos, fluidState, blockState, Direction.SOUTH, fluidState5);
        boolean bl6 = true;//shouldRenderSide(blockRenderView, blockPos, fluidState, blockState, Direction.WEST, fluidState6);
        boolean bl7 = true;//shouldRenderSide(blockRenderView, blockPos, fluidState, blockState, Direction.EAST, fluidState7);
        if (bl2 || bl3 || bl7 || bl6 || bl4 || bl5) {
            float j = blockRenderView.getBrightness(Direction.DOWN, true);
            float k = blockRenderView.getBrightness(Direction.UP, true);
            float l = blockRenderView.getBrightness(Direction.NORTH, true);
            float m = blockRenderView.getBrightness(Direction.WEST, true);
            Fluid fluid = fluidState.getFluid();
            float n = 1.0f;//this.getFluidHeight(blockRenderView, fluid, blockPos, blockState, fluidState);
            float o = 1.0F;
            float p = 1.0F;
            float q = 1.0F;
            float r = 1.0F;
            if (n >= 1.0F) {
                o = 1.0F;
                p = 1.0F;
                q = 1.0F;
                r = 1.0F;
            } else {
                /*float s = this.getFluidHeight(blockRenderView, fluid, blockPos.north(), blockState4, fluidState4);
                float t = this.getFluidHeight(blockRenderView, fluid, blockPos.south(), blockState5, fluidState5);
                float u = this.getFluidHeight(blockRenderView, fluid, blockPos.east(), blockState7, fluidState7);
                float v = this.getFluidHeight(blockRenderView, fluid, blockPos.west(), blockState6, fluidState6);
                o = this.calculateFluidHeight(blockRenderView, fluid, n, s, u, blockPos.offset(Direction.NORTH).offset(Direction.EAST));
                p = this.calculateFluidHeight(blockRenderView, fluid, n, s, v, blockPos.offset(Direction.NORTH).offset(Direction.WEST));
                q = this.calculateFluidHeight(blockRenderView, fluid, n, t, u, blockPos.offset(Direction.SOUTH).offset(Direction.EAST));
                r = this.calculateFluidHeight(blockRenderView, fluid, n, t, v, blockPos.offset(Direction.SOUTH).offset(Direction.WEST));*/
            }


            // float s = (float) ((float) pos.getX() - camera.getPos().getX()) - 0.5f;//(float) (blockPos.getX() & 15);
            // float t = (float) ((float) pos.getY() - camera.getPos().getY()) - 0.5f;//(float) (blockPos.getY() & 15);
            // float u = (float) ((float) pos.getZ() - camera.getPos().getZ()) - 0.5f;//(float) (blockPos.getZ() & 15);
            float s = 0f;//(float) (blockPos.getX() & 15);
            float t = 0f;//(float) (blockPos.getY() & 15);
            float u = 0f;//(float) (blockPos.getZ() & 15);
            float v = 0.001F;
            float w = bl3 ? 0.001F : 0.0F;
            if (bl2 /*&& !isSideCovered(blockRenderView, blockPos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), blockState3)*/) {
                p -= 0.001F;
                r -= 0.001F;
                q -= 0.001F;
                o -= 0.001F;
                Vec3d vec3d = new Vec3d(1f, 1f, 1f); //fluidState.getVelocity(blockRenderView, blockPos);
                float x;
                float z;
                float ab;
                float ad;
                float y;
                float aa;
                float ac;
                float ae;
                if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                    Sprite sprite = sprites[0];
                    x = sprite.getFrameU(0.0F);
                    y = sprite.getFrameV(0.0F);
                    z = x;
                    aa = sprite.getFrameV(1.0F);
                    ab = sprite.getFrameU(1.0F);
                    ac = aa;
                    ad = ab;
                    ae = y;
                } else {
                    Sprite sprite = sprites[1];
                    float af = (float) MathHelper.atan2(vec3d.z, vec3d.x) - (float) (Math.PI / 2);
                    float ag = MathHelper.sin(af) * 0.25F;
                    float ah = MathHelper.cos(af) * 0.25F;
                    float ai = 0.5F;
                    x = sprite.getFrameU(0.5F + (-ah - ag));
                    y = sprite.getFrameV(0.5F + -ah + ag);
                    z = sprite.getFrameU(0.5F + -ah + ag);
                    aa = sprite.getFrameV(0.5F + ah + ag);
                    ab = sprite.getFrameU(0.5F + ah + ag);
                    ac = sprite.getFrameV(0.5F + (ah - ag));
                    ad = sprite.getFrameU(0.5F + (ah - ag));
                    ae = sprite.getFrameV(0.5F + (-ah - ag));
                }

                float aj = (x + z + ab + ad) / 4.0F;
                float af = (y + aa + ac + ae) / 4.0F;
                float ag = sprites[0].getAnimationFrameDelta();
                x = MathHelper.lerp(ag, x, aj);
                z = MathHelper.lerp(ag, z, aj);
                ab = MathHelper.lerp(ag, ab, aj);
                ad = MathHelper.lerp(ag, ad, aj);
                y = MathHelper.lerp(ag, y, af);
                aa = MathHelper.lerp(ag, aa, af);
                ac = MathHelper.lerp(ag, ac, af);
                ae = MathHelper.lerp(ag, ae, af);
                int ak = this.getLight(blockRenderView, blockPos);
                float ai = k * f;
                float al = k * g;
                float am = k * h;
                this.vertex2(matrix, vertexConsumer, s + 0.0F, t + p, u + 0.0F, ai, al, am, x, y, ak);
                this.vertex2(matrix, vertexConsumer, s + 0.0F, t + r, u + 1.0F, ai, al, am, z, aa, ak);
                this.vertex2(matrix, vertexConsumer, s + 1.0F, t + q, u + 1.0F, ai, al, am, ab, ac, ak);
                this.vertex2(matrix, vertexConsumer, s + 1.0F, t + o, u + 0.0F, ai, al, am, ad, ae, ak);
                if (fluidState.canFlowTo(blockRenderView, blockPos.up())) {
                    this.vertex2(matrix, vertexConsumer, s + 0.0F, t + p, u + 0.0F, ai, al, am, x, y, ak);
                    this.vertex2(matrix, vertexConsumer, s + 1.0F, t + o, u + 0.0F, ai, al, am, ad, ae, ak);
                    this.vertex2(matrix, vertexConsumer, s + 1.0F, t + q, u + 1.0F, ai, al, am, ab, ac, ak);
                    this.vertex2(matrix, vertexConsumer, s + 0.0F, t + r, u + 1.0F, ai, al, am, z, aa, ak);
                }
            }

            if (bl3) {
                float xx = sprites[0].getMinU();
                float zx = sprites[0].getMaxU();
                float abx = sprites[0].getMinV();
                float adx = sprites[0].getMaxV();
                int an = this.getLight(blockRenderView, blockPos.down());
                float aax = j * f;
                float acx = j * g;
                float aex = j * h;
                this.vertex2(matrix, vertexConsumer, s, t + w, u + 1.0F, aax, acx, aex, xx, adx, an);
                this.vertex2(matrix, vertexConsumer, s, t + w, u, aax, acx, aex, xx, abx, an);
                this.vertex2(matrix, vertexConsumer, s + 1.0F, t + w, u, aax, acx, aex, zx, abx, an);
                this.vertex2(matrix, vertexConsumer, s + 1.0F, t + w, u + 1.0F, aax, acx, aex, zx, adx, an);
            }

            int ao = this.getLight(blockRenderView, blockPos);

            for (Direction direction : Direction.Type.HORIZONTAL) {
                float adx;
                float yx;
                float aax;
                float acx;
                float aex;
                float ap;
                boolean bl8;
                switch (direction) {
                    case NORTH:
                        adx = p;
                        yx = o;
                        aax = s;
                        aex = s + 1.0F;
                        acx = u + 0.001F;
                        ap = u + 0.001F;
                        bl8 = bl4;
                        break;
                    case SOUTH:
                        adx = q;
                        yx = r;
                        aax = s + 1.0F;
                        aex = s;
                        acx = u + 1.0F - 0.001F;
                        ap = u + 1.0F - 0.001F;
                        bl8 = bl5;
                        break;
                    case WEST:
                        adx = r;
                        yx = p;
                        aax = s + 0.001F;
                        aex = s + 0.001F;
                        acx = u + 1.0F;
                        ap = u;
                        bl8 = bl6;
                        break;
                    default:
                        adx = o;
                        yx = q;
                        aax = s + 1.0F - 0.001F;
                        aex = s + 1.0F - 0.001F;
                        acx = u;
                        ap = u + 1.0F;
                        bl8 = bl7;
                }

                if (bl8 && !isSideCovered(blockRenderView, blockPos, direction, Math.max(adx, yx), blockRenderView.getBlockState(blockPos.offset(direction)))) {
                    BlockPos blockPos2 = blockPos.offset(direction);
                    Sprite sprite2 = sprites[1];
                    if (!bl) {
                        Block block = blockRenderView.getBlockState(blockPos2).getBlock();
                        if (block instanceof TranslucentBlock || block instanceof LeavesBlock) {
                            sprite2 = this.waterOverlaySprite;
                        }
                    }

                    float ah = sprite2.getFrameU(0.0F);
                    float ai = sprite2.getFrameU(0.5F);
                    float al = sprite2.getFrameV((1.0F - adx) * 0.5F);
                    float am = sprite2.getFrameV((1.0F - yx) * 0.5F);
                    float aq = sprite2.getFrameV(0.5F);
                    float ar = direction.getAxis() == Direction.Axis.Z ? l : m;
                    float as = k * ar * f;
                    float at = k * ar * g;
                    float au = k * ar * h;
                    this.vertex2(matrix, vertexConsumer, aax, t + adx, acx, as, at, au, ah, al, ao);
                    this.vertex2(matrix, vertexConsumer, aex, t + yx, ap, as, at, au, ai, am, ao);
                    this.vertex2(matrix, vertexConsumer, aex, t + w, ap, as, at, au, ai, aq, ao);
                    this.vertex2(matrix, vertexConsumer, aax, t + w, acx, as, at, au, ah, aq, ao);
                    if (sprite2 != this.waterOverlaySprite) {
                        this.vertex2(matrix, vertexConsumer, aax, t + w, acx, as, at, au, ah, aq, ao);
                        this.vertex2(matrix, vertexConsumer, aex, t + w, ap, as, at, au, ai, aq, ao);
                        this.vertex2(matrix, vertexConsumer, aex, t + yx, ap, as, at, au, ai, am, ao);
                        this.vertex2(matrix, vertexConsumer, aax, t + adx, acx, as, at, au, ah, al, ao);
                    }
                }
            }
        }
    }
}
