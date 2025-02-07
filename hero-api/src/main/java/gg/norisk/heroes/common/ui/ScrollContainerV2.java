//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gg.norisk.heroes.common.ui;

import gg.norisk.heroes.client.ui.skilltree.AbilitySkillTreeComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Delta;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ScrollContainerV2<C extends Component> extends WrappingParentComponent<C> {
    public static final Identifier VERTICAL_VANILLA_SCROLLBAR_TEXTURE = Identifier.of("owo", "scrollbar/vanilla_vertical");
    public static final Identifier DISABLED_VERTICAL_VANILLA_SCROLLBAR_TEXTURE = Identifier.of("owo", "scrollbar/vanilla_vertical_disabled");
    public static final Identifier HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE = Identifier.of("owo", "scrollbar/vanilla_horizontal_disabled");
    public static final Identifier DISABLED_HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE = Identifier.of("owo", "scrollbar/vanilla_horizontal_disabled");
    public static final Identifier VANILLA_SCROLLBAR_TRACK_TEXTURE = Identifier.of("owo", "scrollbar/track");
    public static final Identifier FLAT_VANILLA_SCROLLBAR_TEXTURE = Identifier.of("owo", "scrollbar/vanilla_flat");
    protected double scrollOffsetVertical = (double) 0.0F;
    protected double scrollOffsetHorizontal = (double) 0.0F;
    protected double currentScrollPositionVertical = (double) 0.0F;
    protected double currentScrollPositionHorizontal = (double) 0.0F;
    protected int lastScrollPositionVertical = -1;
    protected int lastScrollPositionHorizontal = -1;
    protected int scrollStep = 0;
    protected int fixedScrollbarLength = 0;
    protected double lastScrollbarLengthVertical = (double) 0.0F;
    protected double lastScrollbarLengthHorizontal = (double) 0.0F;
    protected boolean scrollbaring = false;
    protected int maxScrollVertical = 0;
    protected int maxScrollHorziontal = 0;
    protected int childSize = 0;
    protected final ScrollDirection verticalDirection = ScrollDirection.VERTICAL;
    protected final ScrollDirection horizontalDirection = ScrollDirection.HORIZONTAL;
    private boolean init = true;

    public ScrollContainerV2(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing, child);
        this.scrollOffsetHorizontal = maxScrollHorziontal - maxScrollHorziontal / 2.0;
        this.currentScrollPositionHorizontal = scrollOffsetHorizontal;
    }

    protected int determineHorizontalContentSize(Sizing sizing) {
        if (this.verticalDirection == ScrollDirection.VERTICAL) {
            return super.determineHorizontalContentSize(sizing);
        } else {
            throw new UnsupportedOperationException("Horizontal ScrollContainer cannot be horizontally content-sized");
        }
    }

    protected int determineVerticalContentSize(Sizing sizing) {
        if (this.horizontalDirection == ScrollDirection.HORIZONTAL) {
            return super.determineVerticalContentSize(sizing);
        } else {
            throw new UnsupportedOperationException("Vertical ScrollContainer cannot be vertically content-sized");
        }
    }

    public void layout(Size space) {
        super.layout(space);
        this.maxScrollVertical = Math.max(0, (Integer) this.verticalDirection.sizeGetter.apply(this.child) - ((Integer) this.verticalDirection.sizeGetter.apply(this) - (Integer) this.verticalDirection.insetGetter.apply((Insets) this.padding.get())));
        this.maxScrollHorziontal = Math.max(0, (Integer) this.horizontalDirection.sizeGetter.apply(this.child) - ((Integer) this.horizontalDirection.sizeGetter.apply(this) - (Integer) this.horizontalDirection.insetGetter.apply((Insets) this.padding.get())));
        this.scrollOffsetVertical = MathHelper.clamp(this.scrollOffsetVertical, (double) 0.0F, (double) this.maxScrollVertical + (double) 0.5F);
        this.scrollOffsetHorizontal = MathHelper.clamp(this.scrollOffsetHorizontal, (double) 0.0F, (double) this.maxScrollHorziontal + (double) 0.5F);
        this.childSize = (Integer) this.verticalDirection.sizeGetter.apply(this.child);
        this.lastScrollPositionVertical = -1;
        this.lastScrollPositionHorizontal = -1;
    }

    protected int childMountX() {
        return (int) ((double) super.childMountX() - this.verticalDirection.choose(this.currentScrollPositionVertical, (double) 0.0F));
    }

    protected int childMountY() {
        return (int) ((double) super.childMountY() - this.verticalDirection.choose((double) 0.0F, this.currentScrollPositionVertical));
    }

    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        super.parentUpdate(delta, mouseX, mouseY);
        this.currentScrollPositionVertical += Delta.compute(this.currentScrollPositionVertical, this.scrollOffsetVertical, (double) delta * (double) 0.5F);
        this.currentScrollPositionHorizontal += Delta.compute(this.currentScrollPositionHorizontal, this.scrollOffsetHorizontal, (double) delta * (double) 0.5F);
    }

    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (init) {
            //for centering
            init = false;
            this.scrollOffsetHorizontal = maxScrollHorziontal - maxScrollHorziontal / 2.0;
            this.currentScrollPositionHorizontal = scrollOffsetHorizontal;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        int effectiveScrollOffsetVertical = this.scrollStep > 0 ? (int) this.scrollOffsetVertical / this.scrollStep * this.scrollStep : (int) this.currentScrollPositionVertical;
        if (this.scrollStep > 0 && (double) this.maxScrollVertical - this.scrollOffsetVertical == (double) -1.0F) {
            effectiveScrollOffsetVertical = (int) ((double) effectiveScrollOffsetVertical + this.scrollOffsetVertical % (double) this.scrollStep);
        }

        int newScrollPositionVertical = this.verticalDirection.coordinateGetter.apply(this) - effectiveScrollOffsetVertical;
        if (newScrollPositionVertical != this.lastScrollPositionVertical) {
            this.verticalDirection.coordinateSetter.accept(this.child, newScrollPositionVertical + (this.padding.get().top() + this.child.margins().get().top()));
            this.lastScrollPositionVertical = newScrollPositionVertical;
        }

        //HORIZONTAL

        int effectiveScrollOffsetHorizontal = this.scrollStep > 0 ? (int) this.scrollOffsetHorizontal / this.scrollStep * this.scrollStep : (int) this.currentScrollPositionHorizontal;
        if (this.scrollStep > 0 && (double) this.maxScrollHorziontal - this.scrollOffsetHorizontal == (double) -1.0F) {
            effectiveScrollOffsetHorizontal = (int) ((double) effectiveScrollOffsetHorizontal + this.scrollOffsetHorizontal % (double) this.scrollStep);
        }

        int newScrollPositionHorizontal = this.horizontalDirection.coordinateGetter.apply(this) - effectiveScrollOffsetHorizontal;
        if (newScrollPositionHorizontal != this.lastScrollPositionHorizontal) {
            this.horizontalDirection.coordinateSetter.accept(this.child, newScrollPositionHorizontal + (this.padding.get().left() + this.child.margins().get().left()));
            this.lastScrollPositionHorizontal = newScrollPositionHorizontal;
        }


        context.getMatrices().push();
        double visualOffsetVertical = -(this.currentScrollPositionVertical % (double) 1.0F);
        if (visualOffsetVertical > 0.9999999 || visualOffsetVertical < 1.0E-7) {
            visualOffsetVertical = (double) 0.0F;
        }
        double visualOffsetHorizontal = -(this.currentScrollPositionHorizontal % (double) 1.0F);
        if (visualOffsetHorizontal > 0.9999999 || visualOffsetHorizontal < 1.0E-7) {
            visualOffsetHorizontal = (double) 0.0F;
        }

        context.getMatrices().translate(this.horizontalDirection.choose(visualOffsetHorizontal, 0.0F), this.verticalDirection.choose(0.0F, visualOffsetVertical), 0.0F);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);
        context.getMatrices().pop();

        Insets padding = this.padding.get();
        int selfSizeVertical = this.verticalDirection.sizeGetter.apply(this);
        int contentSizeVertical = this.verticalDirection.sizeGetter.apply(this) - this.verticalDirection.insetGetter.apply(padding);
        this.lastScrollbarLengthVertical = this.fixedScrollbarLength == 0 ? Math.min(Math.floor((float) selfSizeVertical / (float) this.childSize * (float) contentSizeVertical), contentSizeVertical) : (double) this.fixedScrollbarLength;

        int selfSizeHorizontal = this.horizontalDirection.sizeGetter.apply(this);
        int contentSizeHorizontal = this.horizontalDirection.sizeGetter.apply(this) - this.horizontalDirection.insetGetter.apply(padding);
        this.lastScrollbarLengthHorizontal = this.fixedScrollbarLength == 0 ? Math.min(Math.floor((float) selfSizeHorizontal / (float) this.childSize * (float) contentSizeHorizontal), contentSizeHorizontal) : (double) this.fixedScrollbarLength;
    }

    public boolean canFocus(FocusSource source) {
        return true;
    }

    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (this.child.onMouseScroll((double) this.x + mouseX - (double) this.child.x(), (double) this.y + mouseY - (double) this.child.y(), amount)) {
            return true;
        } else {
            if (this.scrollStep < 1) {
                this.scrollByVertical(-amount * (double) 15.0F, false, true);
            } else {
                this.scrollByVertical(-amount * (double) this.scrollStep, true, true);
            }

            return true;
        }
    }

    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.isInScrollbar((double) this.x + mouseX, (double) this.y + mouseY)) {
            super.onMouseDown(mouseX, mouseY, button);
            return true;
        } else {
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (!this.scrollbaring && !this.isInScrollbar((double) this.x + mouseX, (double) this.y + mouseY)) {
            return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
        } else {
            double deltaVertical = this.verticalDirection.choose(deltaX, deltaY) * -1;
            double selfSizeVertical = this.verticalDirection.sizeGetter.apply(this) - this.verticalDirection.insetGetter.apply(this.padding.get());
            double scalarVertical = (double) this.maxScrollVertical / (selfSizeVertical - this.lastScrollbarLengthVertical);
            if (!Double.isFinite(scalarVertical)) {
                scalarVertical = 0.0F;
            }

            this.scrollByVertical(deltaVertical * scalarVertical, true, false);

            double deltaHorizontal = this.horizontalDirection.choose(deltaX, deltaY) * -1;
            double selfSizeHorizontal = this.horizontalDirection.sizeGetter.apply(this) - this.horizontalDirection.insetGetter.apply(this.padding.get());
            double scalarHorizontal = (double) this.maxScrollHorziontal / (selfSizeHorizontal - this.lastScrollbarLengthVertical);
            if (!Double.isFinite(scalarHorizontal)) {
                scalarHorizontal = 0.0F;
            }

            this.scrollByHorizontal(deltaHorizontal * scalarHorizontal, true, false);
            this.scrollbaring = true;
            return true;
        }
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == this.verticalDirection.lessKeycode) {
            this.scrollByVertical((double) -10.0F, false, true);
        } else if (keyCode == this.verticalDirection.moreKeycode) {
            this.scrollByVertical((double) 10.0F, false, true);
        } else if (keyCode == 267) {
            this.scrollByVertical(this.verticalDirection.choose((double) this.width, (double) this.height) * 0.8, false, true);
        } else if (keyCode == 266) {
            this.scrollByVertical(this.verticalDirection.choose((double) this.width, (double) this.height) * -0.8, false, true);
        }

        return false;
    }

    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        this.scrollbaring = false;
        return true;
    }

    public @Nullable Component childAt(int x, int y) {
        return this.isInScrollbar((double) x, (double) y) ? this : super.childAt(x, y);
    }

    protected void scrollByVertical(double offset, boolean instant, boolean showScrollbar) {
        this.scrollOffsetVertical = MathHelper.clamp(this.scrollOffsetVertical + offset, (double) 0.0F, (double) this.maxScrollVertical + (double) 0.5F);
        if (instant) {
            this.currentScrollPositionVertical = this.scrollOffsetVertical;
        }
    }

    protected void scrollByHorizontal(double offset, boolean instant, boolean showScrollbar) {
        this.scrollOffsetHorizontal = MathHelper.clamp(this.scrollOffsetHorizontal + offset, (double) 0.0F, (double) this.maxScrollHorziontal + (double) 0.5F);
        if (instant) {
            this.currentScrollPositionHorizontal = this.scrollOffsetHorizontal;
        }
    }

    protected boolean isInScrollbar(double mouseX, double mouseY) {
        return true;
    }

    public ScrollContainerV2<C> scrollTo(Component component) {
        this.scrollOffsetVertical = MathHelper.clamp(this.scrollOffsetVertical - (double) (this.y - component.y() + ((Insets) component.margins().get()).top()), (double) 0.0F, (double) this.maxScrollVertical);
        this.scrollOffsetHorizontal = MathHelper.clamp(this.scrollOffsetHorizontal - (double) (this.x - component.x() + ((Insets) component.margins().get()).right()) - component.width() * 4 - 28, (double) 0.0F, (double) this.maxScrollHorziontal);

        return this;
    }

    public ScrollContainerV2<C> scrollTo(@Range(
            from = 0L,
            to = 1L
    ) double horizontal, double vertical) {
        this.scrollOffsetVertical = (double) this.maxScrollVertical * vertical;
        this.scrollOffsetHorizontal = (double) this.maxScrollHorziontal * horizontal;
        return this;
    }

    public ScrollContainerV2<C> scrollStep(int scrollStep) {
        this.scrollStep = scrollStep;
        return this;
    }

    public int scrollStep() {
        return this.scrollStep;
    }

    public ScrollContainerV2<C> fixedScrollbarLength(int fixedScrollbarLength) {
        this.fixedScrollbarLength = fixedScrollbarLength;
        return this;
    }

    public int fixedScrollbarLength() {
        return this.fixedScrollbarLength;
    }

    public static enum ScrollDirection {
        VERTICAL(Component::height, Component::updateY, Component::y, Insets::vertical, 265, 264),
        HORIZONTAL(Component::width, Component::updateX, Component::x, Insets::horizontal, 263, 262);

        public final Function<Component, Integer> sizeGetter;
        public final BiConsumer<Component, Integer> coordinateSetter;
        public final Function<ScrollContainerV2<?>, Integer> coordinateGetter;
        public final Function<Insets, Integer> insetGetter;
        public final int lessKeycode;
        public final int moreKeycode;

        private ScrollDirection(Function<Component, Integer> sizeGetter, BiConsumer<Component, Integer> coordinateSetter, Function<ScrollContainerV2<?>, Integer> coordinateGetter, Function<Insets, Integer> insetGetter, int lessKeycode, int moreKeycode) {
            this.sizeGetter = sizeGetter;
            this.coordinateSetter = coordinateSetter;
            this.coordinateGetter = coordinateGetter;
            this.insetGetter = insetGetter;
            this.lessKeycode = lessKeycode;
            this.moreKeycode = moreKeycode;
        }

        public double choose(double horizontal, double vertical) {
            double var10000;
            switch (this.ordinal()) {
                case 0 -> var10000 = vertical;
                case 1 -> var10000 = horizontal;
                default -> throw new MatchException((String) null, (Throwable) null);
            }

            return var10000;
        }
    }
}
