/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class UpdateTags implements MinecraftPacket {

  private final Map<String, Map<String, int[]>> tags = new HashMap<>();

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_17) >= 0) {
      int length = ProtocolUtils.readVarInt(buf);
      for (int i = 0; i < length; i++) {
        String key = ProtocolUtils.readString(buf);
        Map<String, int[]> value = deserializeTagEntries(buf);
        this.tags.put(key, value);
      }
    } else {
      tags.put("minecraft:block", deserializeTagEntries(buf));
      tags.put("minecraft:item", deserializeTagEntries(buf));
      tags.put("minecraft:fluid", deserializeTagEntries(buf));
      if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) >= 0) {
        tags.put("minecraft:entity_type", deserializeTagEntries(buf));
      }
    }
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_17) >= 0) {
      ProtocolUtils.writeVarInt(buf, this.tags.size());
      for (Map.Entry<String, Map<String, int[]>> entry : this.tags.entrySet()) {
        ProtocolUtils.writeString(buf, entry.getKey());
        serializeTagEntries(buf, entry.getValue());
      }
    } else {
      serializeTagEntries(buf, tags.get("minecraft:block"));
      serializeTagEntries(buf, tags.get("minecraft:item"));
      serializeTagEntries(buf, tags.get("minecraft:fluid"));
      if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) >= 0) {
        serializeTagEntries(buf, tags.get("minecraft:entity_type"));
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }

  private Map<String, int[]> deserializeTagEntries(ByteBuf buf) {
    Map<String, int[]> tags = new HashMap<>();
    int length = ProtocolUtils.readVarInt(buf);
    for (int i = 0; i < length; i++) {
      String key = ProtocolUtils.readString(buf);
      int[] value = ProtocolUtils.readIntegerArray(buf);
      tags.put(key, value);
    }

    return tags;
  }

  private void serializeTagEntries(ByteBuf buf, Map<String, int[]> tags) {
    ProtocolUtils.writeVarInt(buf, tags.size());
    for (Map.Entry<String, int[]> entry : tags.entrySet()) {
      ProtocolUtils.writeString(buf, entry.getKey());
      ProtocolUtils.writeIntegerArray(buf, entry.getValue());
    }
  }

  public Map<String, Map<String, int[]>> getTags() {
    return tags;
  }
}