package com.velocitypowered.proxy.connection.forge.modern;

import com.velocitypowered.proxy.connection.ConnectionTypes;
import com.velocitypowered.proxy.connection.util.ConnectionTypeImpl;

/**
 * Contains extra logic for {@link ConnectionTypes#MODERN_FORGE}.
 */
public class ModernForgeConnectionType extends ConnectionTypeImpl {

  public ModernForgeConnectionType() {
    super(ModernForgeHandshakeClientPhase.NOT_STARTED,
            ModernForgeHandshakeBackendPhase.NOT_STARTED);
  }
}
