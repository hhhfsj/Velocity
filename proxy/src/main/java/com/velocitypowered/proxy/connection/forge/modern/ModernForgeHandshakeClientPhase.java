package com.velocitypowered.proxy.connection.forge.modern;

import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ClientConnectionPhase;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.LoginPluginMessage;
import com.velocitypowered.proxy.protocol.packet.LoginPluginResponse;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Allows for simple tracking of the phase that the Legacy Forge handshake is in.
 */
public enum ModernForgeHandshakeClientPhase implements ClientConnectionPhase {

  /**
   * No handshake packets have yet been sent.
   */
  NOT_STARTED {
    @Override
    ModernForgeHandshakeClientPhase nextPhase() {
      return IN_PROGRESS;
    }

    @Override
    public void onFirstJoin(ConnectedPlayer player) {
      // We have something special to do for legacy Forge servers - during first connection the FML
      // handshake will getNewPhase to complete regardless. Thus, we need to ensure that a reset
      // packet is ALWAYS sent on first switch.
      //
      // As we know that calling this branch only happens on first join, we set that if we are a
      // Forge client that we must reset on the next switch.
      player.setPhase(ModernForgeHandshakeClientPhase.COMPLETE);
    }

    @Override
    boolean onHandle(ConnectedPlayer player,
                     LoginPluginResponse message,
                     MinecraftConnection backendConn) {
      // If we stay in this phase, we do nothing because it means the packet wasn't handled.
      // Returning false indicates this
      return false;
    }
  },

  /**
   * Waiting for the client to acknowledge the reset before starting handshake.
   */
  WAITING_RESET {
    @Override
    boolean onHandle(ConnectedPlayer player, LoginPluginResponse message, MinecraftConnection backendConn) {
      // Donut forward!
      if (message.getId() == ModernForgeConstants.RESET_DISCRIMINATOR) {
        player.setPhase(ModernForgeHandshakeClientPhase.IN_PROGRESS);
        return true;
      }

      // Disconnect?
      // We didn't recieve the reset?
      return false;
    }
  },

  /**
   * Client and Server exchange pleasantries.
   */
  IN_PROGRESS {
  },

  /**
   * The handshake is complete. The handshake can be reset.
   *
   * <p>Note that a successful connection to a server does not mean that
   * we will be in this state. After a handshake reset, if the next server
   * is vanilla we will still be in the {@link #NOT_STARTED} phase,
   * which means we must NOT send a reset packet. This is handled by
   * overriding the {@link #resetConnectionPhase(ConnectedPlayer)} in this
   * element (it is usually a no-op).</p>
   */
  COMPLETE {
    @Override
    public void resetConnectionPhase(ConnectedPlayer player) {
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

    @Override
    public boolean consideredComplete() {
      return true;
    }
  };

  /**
   * Creates an instance of the {@link ModernForgeHandshakeClientPhase}.
   */
  ModernForgeHandshakeClientPhase() {
  }

  @Override
  public final boolean handle(ConnectedPlayer player,
                              LoginPluginResponse message,
                              VelocityServerConnection server) {
    if (server != null) {
      MinecraftConnection backendConn = server.getConnection();
      if (backendConn != null) {
        // Get the phase and check if we need to start the next phase.
        ModernForgeHandshakeClientPhase newPhase = getNewPhase(message);

        // Update phase on player
        player.setPhase(newPhase);

        // Perform phase handling
        return newPhase.onHandle(player, message, backendConn);
      }
    }

    // Not handled, fallback
    return false;
  }

  /**
   * Handles the phase tasks.
   *
   * @param player The player
   * @param message The message to handle
   * @param backendConn The backend connection to write to, if required.
   *
   * @return true if handled, false otherwise.
   */
  boolean onHandle(ConnectedPlayer player,
                   LoginPluginResponse message,
                   MinecraftConnection backendConn) {
    // Send the packet on to the server.
    backendConn.write(message.retain());

    // We handled the packet. No need to continue processing.
    return true;
  }

  @Override
  public boolean consideredComplete() {
    return false;
  }

  /**
   * Gets the next phase, if any (will return self if we are at the end
   * of the handshake).
   *
   * @return The next phase
   */
  ModernForgeHandshakeClientPhase nextPhase() {
    return this;
  }

  /**
   * Get the phase to act on, depending on the packet that has been sent.
   *
   * @param packet The packet
   * @return The phase to transition to, which may be the same as before.
   */
  private ModernForgeHandshakeClientPhase getNewPhase(LoginPluginResponse packet) {
    return nextPhase();
  }

}
