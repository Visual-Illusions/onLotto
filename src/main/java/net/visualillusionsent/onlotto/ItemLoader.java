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
import net.canarymod.api.inventory.ItemType;
import net.visualillusionsent.utils.FileUtils;
import org.jdom2.DataConversionException;
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
                if (item.getAttribute("enabled").getBooleanValue()) {
                    int id = item.getAttribute("id").getIntValue();
                    int damage = item.getAttribute("damage").getIntValue();
                    int amount = item.getAttribute("amount").getIntValue();
                    double weight = item.getAttribute("weight").getDoubleValue();
                    if (ItemType.fromId(id) != null) {
                        item_list.add(new WeightedItem(Canary.factory().getItemFactory().newItem(id, damage, amount), weight));
                    }
                    else {
                        onlotto.getLogman().warn("Tried to load a non-existent item: ID=" + id);
                    }
                }
            }
            catch (DataConversionException dcex) {
                onlotto.getLogman().error("Failed to load an Item...", dcex);
                continue;
            }
        }
        return item_list.toArray(new WeightedItem[item_list.size()]);
    }
}
