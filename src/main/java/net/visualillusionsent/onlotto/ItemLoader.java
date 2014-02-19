/*
 * This file is part of onLotto.
 *
 * Copyright © 2011-2014 Visual Illusions Entertainment
 *
 * onLotto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.onlotto;

import net.canarymod.Canary;
import net.canarymod.api.factory.NBTFactory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.nbt.BaseTag;
import net.canarymod.api.nbt.CompoundTag;
import net.canarymod.api.nbt.ListTag;
import net.canarymod.api.nbt.NBTTagType;
import net.visualillusionsent.utils.BooleanUtils;
import net.visualillusionsent.utils.FileUtils;
import net.visualillusionsent.utils.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class ItemLoader {

    private final SAXBuilder builder = new SAXBuilder();
    private final String item_path = "config/onLotto/lotto_items.xml";

    final WeightedItem[] load(OnLotto onlotto) throws Exception {
        File itemFile = new File(item_path);
        if (!itemFile.exists()) {
            FileUtils.cloneFileFromJar(onlotto.getJarPath(), "default_items.xml", "config/onLotto/lotto_items.xml");
        }
        List<WeightedItem> item_list = new ArrayList<WeightedItem>();
        Document doc = builder.build(itemFile);
        Element root = doc.getRootElement();
        List<Element> items = root.getChildren();
        for (Element item : items) {
            try {
                if (BooleanUtils.parseBoolean(item.getChild("enabled").getValue())) {
                    String machineName = item.getChildText("name");
                    int data = Integer.valueOf(item.getChildText("data").trim());
                    int amount = Integer.valueOf(item.getChildText("amount").trim());
                    double weight = Double.valueOf(item.getChildText("weight").trim());
                    String displayName = testAndParseDisp(item.getChildText("displayName"));
                    String[] lore = testAndParseLore(item.getChildText("lore"));
                    ItemType type = ItemType.fromStringAndData(machineName, data);
                    if (type != null) {
                        Item temp = Canary.factory().getItemFactory().newItem(type, data, amount);
                        if (displayName != null) {
                            temp.setDisplayName(displayName);
                        }
                        if (lore != null) {
                            temp.setLore(StringUtils.trimElements(lore));
                        }
                        if (item.getChild("nbt") != null) {
                            testparseapplyNBT(item.getChild("nbt"), temp);
                        }

                        item_list.add(new WeightedItem(temp, weight));
                    }
                    else {
                        onlotto.getLogman().warn("Tried to load a non-existent item: Name=" + machineName);
                    }
                }
            }
            catch (Exception ex) {
                onlotto.getLogman().error("Failed to load an item #" + items.indexOf(item), ex);
            }
        }
        return item_list.toArray(new WeightedItem[item_list.size()]);
    }

    private String[] testAndParseLore(String element) {
        if (element == null) {
            return null;
        }
        element = element.trim();
        return element.trim().isEmpty() ? null : element.split("[\r|\n|\r\n]");
    }

    private String testAndParseDisp(String element) {
        if (element == null) {
            return null;
        }
        element = element.trim();
        return element.isEmpty() ? null : element;
    }

    private void testparseapplyNBT(Element main, Item temp) {
        NBTFactory factory = Canary.factory().getNBTFactory();
        if (!temp.hasDataTag()) {
            temp.setDataTag(factory.newCompoundTag("tag")); // Need a datatag
        }
        for (Element toSet : main.getChildren()) {
            // Let it throw the error and stop the loading of the item
            NBTTagType setType = NBTTagType.valueOf(toSet.getAttributeValue("type").toUpperCase());
            if (setType == NBTTagType.LIST) {
                ListTag<BaseTag> working = factory.newListTag();
                temp.getDataTag().put(toSet.getName(), working);
                recursiveList(toSet, working);
            }
            else if (setType == NBTTagType.COMPOUND) {
                CompoundTag workingTag = temp.getDataTag().containsKey(toSet.getName()) ? temp.getDataTag().getCompoundTag(toSet.getName()) : factory.newCompoundTag(toSet.getName());
                temp.getDataTag().put(toSet.getName(), workingTag);
                recursiveCompund(workingTag, toSet);
            }
            else {
                temp.getDataTag().put(toSet.getName(), getForType(setType, toSet.getValue()));
            }
        }
    }

    private void recursiveCompund(CompoundTag workTag, Element workElement) {
        NBTFactory factory = Canary.factory().getNBTFactory();
        for (Element subElement : workElement.getChildren()) {
            NBTTagType workingType = NBTTagType.valueOf(subElement.getAttributeValue("type").toUpperCase());
            if (workingType == NBTTagType.COMPOUND) {
                CompoundTag workingTag = workTag.containsKey(subElement.getName()) ? workTag.getCompoundTag(subElement.getName()) : factory.newCompoundTag(subElement.getName());
                workTag.put(subElement.getName(), workingTag);
                recursiveCompund(workingTag, subElement);
            }
            else if (workingType == NBTTagType.LIST) {
                ListTag<BaseTag> working = factory.newListTag();
                workTag.put(subElement.getName(), working);
                recursiveList(subElement, working);
            }
            else {
                workTag.put(subElement.getName(), getForType(workingType, subElement.getValue()));
            }
        }
    }

    private void recursiveList(Element working, ListTag listTag) {
        NBTFactory factory = Canary.factory().getNBTFactory();
        for (Element sub : working.getChildren()) {
            NBTTagType subType = NBTTagType.valueOf(sub.getAttributeValue("type").toUpperCase());
            if (subType == NBTTagType.COMPOUND) {
                CompoundTag workingTag = factory.newCompoundTag(sub.getName());
                listTag.add(workingTag);
                recursiveCompund(workingTag, sub);
            }
            else if (subType == NBTTagType.LIST) {
                ListTag<BaseTag> workingTag = factory.newListTag();
                listTag.add(workingTag);
                recursiveList(sub, workingTag);
            }
            else {
                listTag.add(getForType(subType, sub.getValue()));
            }
        }
    }

    private BaseTag getForType(NBTTagType type, String value) {
        NBTFactory factory = Canary.factory().getNBTFactory();
        value = value.trim();
        switch (type) {
            case BYTE:
                return factory.newByteTag(Byte.valueOf(value));
            case BYTE_ARRAY:
                return factory.newByteArrayTag(StringUtils.stringToByteArray(value));
            case DOUBLE:
                return factory.newDoubleTag(Double.valueOf(value));
            case FLOAT:
                return factory.newFloatTag(Float.valueOf(value));
            case INT:
                return factory.newIntTag(Integer.valueOf(value));
            case INT_ARRAY:
                return factory.newIntArrayTag(StringUtils.stringToIntArray(value));
            case LONG:
                return factory.newLongTag(Long.valueOf(value));
            case SHORT:
                return factory.newShortTag(Short.valueOf(value));
            case STRING:
                return factory.newStringTag(value);
            default:
                return null;
        }
    }
}
