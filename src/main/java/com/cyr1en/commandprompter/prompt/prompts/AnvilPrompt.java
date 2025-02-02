/*
 * MIT License
 *
 * Copyright (c) 2020 Ethan Bacurio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cyr1en.commandprompter.prompt.prompts;

import com.cyr1en.commandprompter.CommandPrompter;
import com.cyr1en.commandprompter.prompt.PromptContext;
import com.cyr1en.commandprompter.util.Util;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AnvilPrompt extends AbstractPrompt {

    public AnvilPrompt(CommandPrompter plugin, PromptContext context, String prompt) {
        super(plugin, context, prompt);
    }

    @Override
    public void sendPrompt() {
        List<String> parts = Arrays.asList(getPrompt().split("\\{br}"));
        ItemStack item = makeItem(parts);
        makeAnvil(parts, item).open((Player) getContext().getSender());
    }

    private AnvilGUI.Builder makeAnvil(List<String> parts, ItemStack item) {
        AtomicBoolean isComplete = new AtomicBoolean(false);
        AnvilGUI.Builder builder = new AnvilGUI.Builder();
        builder.onComplete((p, text) -> {
            String message = ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&', text));
            String cancelKeyword = getPlugin().getConfiguration().cancelKeyword;
            if (cancelKeyword.equalsIgnoreCase(message)) {
                getPromptManager().cancel(p);
                return AnvilGUI.Response.close();
            }
            isComplete.getAndSet(true);
            PromptContext ctx = new PromptContext(null, p, message);
            getPromptManager().processPrompt(ctx);
            return AnvilGUI.Response.close();
        });
        builder.onClose(p -> {
            if (isComplete.get())
                return;
            getPromptManager().cancel(p);
        });
        builder.text(color(parts.get(0)));
        if (getPlugin().getPromptConfig().enableTitle) {
            String title = getPlugin().getPromptConfig().customTitle;
            title = title.isEmpty()?color(parts.get(0)) : color(title);
            builder.title(title);
        }
        builder.itemLeft(item);
        builder.plugin(getPlugin());
        return builder;
    }

    private ItemStack makeItem(List<String> parts) {
        ItemStack item = new ItemStack(Util.getCheckedMaterial(getPlugin().getPromptConfig().anvilItem, Material.PAPER));
        ItemMeta meta = item.getItemMeta();
        getPlugin().getPluginLogger().debug("ItemMeta: " + meta);
        if (getPlugin().getPromptConfig().anvilEnchanted) {
            Objects.requireNonNull(meta).addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        Objects.requireNonNull(meta).addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(parts.get(0));

        if (parts.size() > 1)
            meta.setLore(parts.subList(1, parts.size()).stream().map(this::color).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }
}
