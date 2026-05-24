package ru.cookiedlc.module.impl.misc.autobuy.holy.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;

import java.util.ArrayList;
import java.util.List;

public class ItemDetector {
    
    public static List<String> getTags(ItemStack item) {
        List<String> tags = new ArrayList<>();
        tags.add(item.getName().getString());
        tags.add(item.getItem().toString());
        
        NbtComponent nbt = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt != null) {
            tags.add(nbt.copyNbt().toString());
        }
        
        LoreComponent lore = item.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                tags.add(line.getString());
            }
        }
        
        return tags;
    }
    
    public static String toTagString(List<String> tags) {
        return String.join(" ", tags);
    }
    
    public static EnumItemType detectItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        for (EnumItemType enumItemType : EnumItemType.values()) {
            if (stack.getItem() == enumItemType.getItemType()) {
                String raw = toTagString(getTags(stack));
                
                if (enumItemType.getTags().isEmpty()) {
                    return enumItemType;
                }
                
                int left = enumItemType.getTags().size();
                for (String tag : enumItemType.getTags()) {
                    if (raw.contains(tag)) {
                        left--;
                    }
                }
                if (left <= 0) {
                    return enumItemType;
                }
            }
        }
        return null;
    }
}
