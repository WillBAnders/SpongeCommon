package org.spongepowered.common.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.adventure.ComponentBridge;
import org.spongepowered.common.bridge.util.text.TextComponentBridge;
import org.spongepowered.common.util.LocaleCache;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AdventureTextComponent implements ITextComponent, TextComponentBridge {
    private @MonotonicNonNull ITextComponent converted;
    private @Nullable Locale deepConvertedLocalized;
    private final net.kyori.adventure.text.Component wrapped;
    private final @Nullable ComponentRenderer<Locale> renderer;
    @OnlyIn(Dist.CLIENT)
    private @Nullable Locale lastLocale;
    @OnlyIn(Dist.CLIENT)
    private @Nullable AdventureTextComponent lastRendered;

    public AdventureTextComponent(final net.kyori.adventure.text.Component wrapped, final @Nullable ComponentRenderer<Locale> renderer) {
        this.wrapped = wrapped;
        this.renderer = renderer;
    }

    public @Nullable ComponentRenderer<Locale> renderer() {
        return this.renderer;
    }

    public net.kyori.adventure.text.Component wrapped() {
        return this.wrapped;
    }

    public synchronized AdventureTextComponent rendered(final Locale locale) {
        if (Objects.equals(locale, this.lastLocale)) {
            return this.lastRendered;
        }
        this.lastLocale = locale;
        return this.lastRendered = this.renderer == null ? this : new AdventureTextComponent(this.renderer.render(this.wrapped, locale), null);
    }

    ITextComponent deepConverted() {
        ITextComponent converted = this.converted;
        if (converted == null || this.deepConvertedLocalized != null) {
            converted = this.converted = ((ComponentBridge) this.wrapped).bridge$asVanillaComponent();
            this.deepConvertedLocalized = null;
        }
        return converted;
    }

    @OnlyIn(Dist.CLIENT)
    ITextComponent deepConvertedLocalized() {
        ITextComponent converted = this.converted;
        final Locale target = LocaleCache.getLocale(Minecraft.getInstance().options.languageCode);
        if (converted == null || this.deepConvertedLocalized != target) {
            converted = this.converted = this.rendered(target).deepConverted();
            this.deepConvertedLocalized = target;
        }
        return converted;
    }

    public @Nullable ITextComponent deepConvertedIfPresent() {
        return this.converted;
    }

    @Override
    public Style getStyle() {
        return this.deepConverted().getStyle();
    }

    @Override
    public String getString() {
        return this.rendered(Locale.getDefault()).deepConverted().getString();
    }

    @Override
    public String getString(final int length) {
        return this.deepConverted().getString(length);
    }

    @Override
    public String getContents() {
        if (this.wrapped instanceof TextComponent) {
            return ((TextComponent) this.wrapped).content();
        } else {
            return this.deepConverted().getContents();
        }
    }

    @Override
    public List<ITextComponent> getSiblings() {
        return this.deepConverted().getSiblings();
    }

    @Override
    public IFormattableTextComponent plainCopy() {
        return this.deepConverted().plainCopy();
    }

    @Override
    public IFormattableTextComponent copy() {
        return this.deepConverted().copy();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IReorderingProcessor getVisualOrderText() {
        return this.deepConvertedLocalized().getVisualOrderText();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> Optional<T> visit(final IStyledTextAcceptor<T> visitor, final Style style) {
        return this.deepConvertedLocalized().visit(visitor, style);
    }

    @Override
    public <T> Optional<T> visit(final ITextAcceptor<T> visitor) {
        return this.deepConverted().visit(visitor);
    }

    @Override
    public Component bridge$asAdventureComponent() {
        return this.wrapped;
    }

    @Override
    public @Nullable Component bridge$adventureComponentIfPresent() {
        return this.bridge$asAdventureComponent();
    }
}
