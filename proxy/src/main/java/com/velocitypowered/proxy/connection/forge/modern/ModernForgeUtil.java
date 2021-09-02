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

package com.velocitypowered.proxy.connection.forge.modern;

import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.LoginPluginMessage;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ModernForgeUtil {

  private ModernForgeUtil() {
    throw new AssertionError();
  }

  static void resetConnectionPhase(ConnectedPlayer player) {
    ByteBuf byteBuf = Unpooled.buffer();
    // Target Network Receiver
    ProtocolUtils.writeString(byteBuf, ModernForgeConstants.HANDSHAKE_CHANNEL);
    // Payload Length
    ProtocolUtils.writeVarInt(byteBuf, 1);
    // Discriminator
    byteBuf.writeByte(ModernForgeConstants.RESET_DISCRIMINATOR);

    if (player.getConnection().getState() == StateRegistry.LOGIN) {
      player.getConnection().write(new LoginPluginMessage(
              ModernForgeConstants.RESET_DISCRIMINATOR,
              ModernForgeConstants.LOGIN_WRAPPER_CHANNEL,
              byteBuf));
    } else {
      player.getConnection().write(new PluginMessage(
              ModernForgeConstants.LOGIN_WRAPPER_CHANNEL,
              byteBuf));
      player.getConnection().setState(StateRegistry.LOGIN);
    }

    player.setPhase(ModernForgeHandshakeClientPhase.WAITING_RESET);
  }
}
