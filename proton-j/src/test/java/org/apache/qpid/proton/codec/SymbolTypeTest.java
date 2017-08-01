package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.apache.qpid.proton.amqp.Symbol;
import org.junit.Test;

public class SymbolTypeTest {

    @Test
    public void testEmptySymbolArray() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.clear();
        final Symbol[] inputValue = new Symbol[0];
        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final Symbol[] outputValue = (Symbol[]) decoder.readObject();

        assertEquals("Failed to round trip Symbol array correctly: ", 0, outputValue.length);
    }
}
