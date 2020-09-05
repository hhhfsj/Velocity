package com.velocitypowered.proxy.connection.forge.modern;

public class ModernForgeConstants {

  /**
   * Clients attempting to connect to 1.13+ Forge servers will have
   * this token appended to the hostname in the initial handshake
   * packet.
   */
  public static final String HANDSHAKE_HOSTNAME_TOKEN = "\0FML2\0";

  /**
   * The channel for forge handshakes.
   */
  public static final String HANDSHAKE_CHANNEL = "fml:handshake";
  
  /**
   * The channel for forge login wrapper.
   */
  public static final String LOGIN_WRAPPER_CHANNEL = "fml:loginwrapper";
  
  /**
   * The reset packet discriminator, Nice!
   */
  static final int RESET_DISCRIMINATOR = 69;
  
  private ModernForgeConstants() {
    throw new AssertionError();
  }

}
