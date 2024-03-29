# JavaCard Connect 

[![Build Status](https://github.com/nightcode/jcconnect/actions/workflows/maven.yml/badge.svg)](https://github.com/nightcode/jcconnect/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.nightcode/jc-connect.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.nightcode%20AND%20a%3Ajc-connect)

JavaCard Connect is a framework which provides an implementation of a secure communication protocol in conformance with the Global Platform Card Specification.
It allows to establish a secure channel between an off-card entity and a card.

## How to use

#### code

```java
  TerminalFactory tf = TerminalFactory.getInstance("PC/SC", null);
  CardTerminals terminals = tf.terminals();
  CardTerminal terminal = terminals.list(CardTerminals.State.CARD_PRESENT).get(0);
  Card card = terminal.connect("*");

  CardChannelService service = new CardChannelServiceImpl(KeyProvider.DEFAULT); 


  CardProperties cardProperties = CardProperties.builder()
      .aid(Aid.parse("A000000151000000"))
      .keyVersionNumber((byte) 0xFF)
      .scpVersion(ScpVersion.SCP_02)
      .build();

  CardChannelContext context = service.createCardChannelContext(cardProperties, new PlainApduChannel(card.getBasicChannel()));
  SecureChannelSession channelSession = service.createSecureChannelSession(context);

  channelSession.openSecureChannel(EnumSet.of(SecurityLevel.C_MAC));

  // GET STATUS command
  CommandAPDU command = new CommandAPDU(0x80, 0xF2, 0x40, 0x00, new byte[] {0x4F, 0x00});
  ResponseAPDU response = channelSession.transmit(command);

```

## Download

Download [the latest jar][1] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>jc-connect</artifactId>
  <version>0.1.10</version>
</dependency>
```

## License

 * [Apache License 2.0](https://github.com/nightcode/jcconnect/blob/master/LICENSE)

----
Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/jcconnect/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: http://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.nightcode&a=jc-connect&v=LATEST
