/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.pintonium;

import java.util.ArrayList;
import java.util.List;

import org.taumc.celeritas.api.OptionGUIConstructionEvent;
import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.structure.OptionGroup;
import org.taumc.celeritas.api.options.structure.OptionPage;
import org.taumc.celeritas.api.options.structure.OptionStorage;

import com.google.common.collect.ImmutableList;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.compat.nothirium.ForgeExtraConfigElements;
import com.sumirelabs.celeritasextra.compat.nothirium.VanillaExtraOptionsScreen;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compiled against Pintonium's GUI API, and loaded only for its independent screen. */
public final class PintoniumGuiIntegration {

    public static void register() throws ReflectiveOperationException {
        // Reject partially migrated forks before registering a handler against their incompatible API.
        if (org.taumc.celeritas.api.options.structure.Option.class.getMethod("getName")
            .getReturnType() != net.minecraft.util.text.ITextComponent.class)
            throw new NoSuchMethodException("Pintonium Option.getName must return ITextComponent");
        org.taumc.celeritas.api.options.control.Control.class.getMethod("createElement",
            org.taumc.celeritas.impl.util.Dim2i.class);
        MinecraftForge.EVENT_BUS.register(new PintoniumGuiIntegration());
    }

    @SubscribeEvent
    public void construct(OptionGUIConstructionEvent event) {
        var config = new ForgeExtraConfigElements(CeleritasExtraClientMod.options());
        var storage = new OptionStorage<ForgeExtraConfigElements>() {

            @Override
            public ForgeExtraConfigElements getData() {
                return config;
            }
            @Override
            public void save() {
                config.apply();
            }

        };
        for (var category : config.categories())
            event.addPage(page(category, storage));
    }

    static OptionPage page(IConfigElement category, OptionStorage<?> storage) {
        var groups = new ArrayList<OptionGroup>();
        collect(category.getChildElements(), "", category.getName(), storage, groups);
        return new OptionPage(OptionIdentifier.create("celeritasextra", category.getName()),
            new TextComponentString(VanillaExtraOptionsScreen.label(category)), ImmutableList.copyOf(groups));
    }

    private static void collect(List<IConfigElement> entries, String context, String path, OptionStorage<?> storage,
        List<OptionGroup> groups) {
        var direct = entries.stream().filter(IConfigElement::isProperty).toList();
        if (!direct.isEmpty()) {
            var group = OptionGroup.createBuilder().setId(OptionIdentifier.create("celeritasextra", path));
            for (int i = 0; i < direct.size(); i++)
                group.add(new PintoniumOption(direct.get(i), context, path + "/" + i, storage));
            groups.add(group.build());
        }
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            if (!entry.isProperty()) {
                String label = VanillaExtraOptionsScreen.label(entry);
                collect(entry.getChildElements(), context.isEmpty() ? label : context + " · " + label,
                    path + "/group_" + i, storage, groups);
            }
        }
    }

}
