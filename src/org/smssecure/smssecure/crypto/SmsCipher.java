package org.smssecure.smssecure.crypto;

import android.content.Context;

import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.RecipientFormattingException;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.sms.IncomingEncryptedMessage;
import org.smssecure.smssecure.sms.IncomingKeyExchangeMessage;
import org.smssecure.smssecure.sms.IncomingPreKeyBundleMessage;
import org.smssecure.smssecure.sms.IncomingTextMessage;
import org.smssecure.smssecure.sms.OutgoingKeyExchangeMessage;
import org.smssecure.smssecure.sms.OutgoingPrekeyBundleMessage;
import org.smssecure.smssecure.sms.OutgoingTextMessage;
import org.smssecure.smssecure.sms.SmsTransportDetails;
import org.signal.libsignal_client.SignalProtocolAddress;
import org.signal.libsignal_client.DuplicateMessageException;
import org.signal.libsignal_client.InvalidKeyException;
import org.signal.libsignal_client.InvalidKeyIdException;
import org.signal.libsignal_client.InvalidMessageException;
import org.signal.libsignal_client.InvalidVersionException;
import org.signal.libsignal_client.LegacyMessageException;
import org.signal.libsignal_client.NoSessionException;
import org.smssecure.smssecure.crypto.SessionBuilder;
import org.signal.libsignal_client.SessionCipher;
import org.signal.libsignal_client.StaleKeyExchangeException;
import org.signal.libsignal_client.UntrustedIdentityException;
import org.signal.libsignal_client.protocol.CiphertextMessage;
import org.smssecure.smssecure.protocol.KeyExchangeMessage;
import org.signal.libsignal_client.protocol.PreKeySignalMessage;
import org.signal.libsignal_client.protocol.SignalMessage;
import org.signal.libsignal_client.state.SignalProtocolStore;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

public class SmsCipher {

  private final SmsTransportDetails transportDetails = new SmsTransportDetails();

  private final SignalProtocolStore signalProtocolStore;

  public SmsCipher(SignalProtocolStore signalProtocolStore) {
    this.signalProtocolStore = signalProtocolStore;
  }

  public IncomingTextMessage decrypt(Context context, IncomingTextMessage message)
      throws LegacyMessageException, InvalidMessageException, DuplicateMessageException,
             NoSessionException, UntrustedIdentityException
  {
    try {
      byte[]        decoded       = transportDetails.getDecodedMessage(message.getMessageBody().getBytes());
      SignalMessage signalMessage = new SignalMessage(decoded);
      SessionCipher sessionCipher = new SessionCipher(signalProtocolStore, new SignalProtocolAddress(message.getSender(), 1));
      byte[]        padded        = sessionCipher.decrypt(signalMessage);
      byte[]        plaintext     = transportDetails.getStrippedPaddingMessageBody(padded);

      if (message.isEndSession() && "TERMINATE".equals(new String(plaintext))) {
        signalProtocolStore.deleteSession(new SignalProtocolAddress(message.getSender(), 1));
      }

      return message.withMessageBody(new String(plaintext));
    } catch (IOException | IllegalArgumentException | NullPointerException e) {
      throw new InvalidMessageException(e);
    }
  }

  public IncomingEncryptedMessage decrypt(Context context, IncomingPreKeyBundleMessage message)
      throws InvalidVersionException, InvalidMessageException, DuplicateMessageException,
             UntrustedIdentityException, LegacyMessageException
  {
    try {
      byte[]              decoded       = transportDetails.getDecodedMessage(message.getMessageBody().getBytes());
      PreKeySignalMessage preKeyMessage = new PreKeySignalMessage(decoded);
      SessionCipher       sessionCipher = new SessionCipher(signalProtocolStore, new SignalProtocolAddress(message.getSender(), 1));
      byte[]              padded        = sessionCipher.decrypt(preKeyMessage);
      byte[]              plaintext     = transportDetails.getStrippedPaddingMessageBody(padded);

      return new IncomingEncryptedMessage(message, new String(plaintext));
    } catch (IOException | InvalidKeyException | InvalidKeyIdException e) {
      throw new InvalidMessageException(e);
    }
  }

  public OutgoingTextMessage encrypt(OutgoingTextMessage message)
    throws NoSessionException, UntrustedIdentityException
  {
    byte[] paddedBody      = transportDetails.getPaddedMessageBody(message.getMessageBody().getBytes());
    String recipientNumber = message.getRecipients().getPrimaryRecipient().getNumber();

    if (!signalProtocolStore.containsSession(new SignalProtocolAddress(recipientNumber, 1))) {
      throw new NoSessionException("No session for: " + recipientNumber);
    }

    SessionCipher     cipher            = new SessionCipher(signalProtocolStore, new SignalProtocolAddress(recipientNumber, 1));
    CiphertextMessage ciphertextMessage = cipher.encrypt(paddedBody);
    String            encodedCiphertext = new String(transportDetails.getEncodedMessage(ciphertextMessage.serialize()));

    if (ciphertextMessage.getType() == CiphertextMessage.PREKEY_TYPE) {
      return new OutgoingPrekeyBundleMessage(message, encodedCiphertext);
    } else {
      return message.withBody(encodedCiphertext);
    }
  }

  public OutgoingKeyExchangeMessage process(Context context, IncomingKeyExchangeMessage message)
      throws UntrustedIdentityException, StaleKeyExchangeException,
             InvalidVersionException, LegacyMessageException, InvalidMessageException
  {
    try {
      Recipients            recipients            = RecipientFactory.getRecipientsFromString(context, message.getSender(), false);
      SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(message.getSender(), 1);
      KeyExchangeMessage    exchangeMessage       = new KeyExchangeMessage(transportDetails.getDecodedMessage(message.getMessageBody().getBytes()));
      SessionBuilder        sessionBuilder        = new SessionBuilder(signalProtocolStore, signalProtocolAddress);

      KeyExchangeMessage response        = sessionBuilder.process(exchangeMessage);

      if (response != null) {
        byte[] serializedResponse = transportDetails.getEncodedMessage(response.serialize());
        return new OutgoingKeyExchangeMessage(recipients, new String(serializedResponse), message.getSubscriptionId());
      } else {
        return null;
      }
    } catch (IOException | InvalidKeyException e) {
      throw new InvalidMessageException(e);
    }
  }

}
