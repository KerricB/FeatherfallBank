package io.owlcraft.featherfallbank.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private final ItemStack stack;

    public ItemBuilder(Material mat) {
        this.stack = new ItemStack(mat);
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> colored = new ArrayList<>();
            for (String s : lore) colored.add(Text.color(s));
            meta.setLore(colored);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() { return stack; }
}
