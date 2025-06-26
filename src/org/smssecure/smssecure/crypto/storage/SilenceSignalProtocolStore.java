package org.smssecure.smssecure.crypto.storage;

import android.content.Context;

import org.smssecure.smssecure.crypto.MasterSecret;
import org.signal.libsignal_client.SignalProtocolAddress;
import org.signal.libsignal_client.IdentityKey;
import org.signal.libsignal_client.IdentityKeyPair;
import org.signal.libsignal_client.InvalidKeyIdException;
import org.signal.libsignal_client.state.SignalProtocolStore;
import org.signal.libsignal_client.state.IdentityKeyStore;
import org.signal.libsignal_client.state.PreKeyRecord;
import org.signal.libsignal_client.state.PreKeyStore;
import org.signal.libsignal_client.state.SessionRecord;
import org.signal.libsignal_client.state.SessionStore;
import org.signal.libsignal_client.state.SignedPreKeyRecord;
import org.signal.libsignal_client.state.SignedPreKeyStore;

import java.util.List;

public class SilenceSignalProtocolStore implements SignalProtocolStore {

  private final PreKeyStore       preKeyStore;
  private final SignedPreKeyStore signedPreKeyStore;
  private final IdentityKeyStore  identityKeyStore;
  private final SessionStore      sessionStore;
  private final int               subscriptionId;

  public SilenceSignalProtocolStore(Context context, MasterSecret masterSecret, int subscriptionId) {
    this.preKeyStore       = new SilencePreKeyStore(context, masterSecret, subscriptionId);
    this.signedPreKeyStore = new SilencePreKeyStore(context, masterSecret, subscriptionId);
    this.identityKeyStore  = new SilenceIdentityKeyStore(context, masterSecret, subscriptionId);
    this.sessionStore      = new SilenceSessionStore(context, masterSecret, subscriptionId);
    this.subscriptionId    = subscriptionId;
  }

  @Override
  public IdentityKeyPair getIdentityKeyPair() {
    return identityKeyStore.getIdentityKeyPair();
  }

  @Override
  public int getLocalRegistrationId() {
    return identityKeyStore.getLocalRegistrationId();
  }

  @Override
  public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
    return preKeyStore.loadPreKey(preKeyId);
  }

  @Override
  public void storePreKey(int preKeyId, PreKeyRecord record) {
    preKeyStore.storePreKey(preKeyId, record);
  }

  @Override
  public boolean containsPreKey(int preKeyId) {
    return preKeyStore.containsPreKey(preKeyId);
  }

  @Override
  public void removePreKey(int preKeyId) {
    preKeyStore.removePreKey(preKeyId);
  }

  @Override
  public SessionRecord loadSession(SignalProtocolAddress axolotlAddress) {
    return sessionStore.loadSession(axolotlAddress);
  }

  @Override
  public List<Integer> getSubDeviceSessions(String number) {
    return sessionStore.getSubDeviceSessions(number);
  }

  @Override
  public void storeSession(SignalProtocolAddress axolotlAddress, SessionRecord record) {
    sessionStore.storeSession(axolotlAddress, record);
  }

  @Override
  public boolean containsSession(SignalProtocolAddress axolotlAddress) {
    return sessionStore.containsSession(axolotlAddress);
  }

  @Override
  public void deleteSession(SignalProtocolAddress axolotlAddress) {
    sessionStore.deleteSession(axolotlAddress);
  }

  @Override
  public void deleteAllSessions(String number) {
    sessionStore.deleteAllSessions(number);
  }

  @Override
  public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
    return signedPreKeyStore.loadSignedPreKey(signedPreKeyId);
  }

  @Override
  public List<SignedPreKeyRecord> loadSignedPreKeys() {
    return signedPreKeyStore.loadSignedPreKeys();
  }

  @Override
  public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
    signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record);
  }

  @Override
  public boolean containsSignedPreKey(int signedPreKeyId) {
    return signedPreKeyStore.containsSignedPreKey(signedPreKeyId);
  }

  @Override
  public void removeSignedPreKey(int signedPreKeyId) {
    signedPreKeyStore.removeSignedPreKey(signedPreKeyId);
  }

  @Override
  public IdentityKey getIdentity(SignalProtocolAddress address) {
    return null;
  }

  @Override
  public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
    return identityKeyStore.isTrustedIdentity(address, identityKey, direction);
  }

  @Override
  public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    return identityKeyStore.saveIdentity(address, identityKey);
  }
}
