package com.velocitypowered.proxy.connection.forge.modern;

import com.velocitypowered.proxy.connection.util.ConnectionTypeImpl;

public class ModernForgeConnectionType extends ConnectionTypeImpl {

  public ModernForgeConnectionType() {
    super(ModernForgeHandshakeClientPhase.NOT_STARTED,
          ModernForgeHandshakeBackendPhase.NOT_STARTED);
  }
}