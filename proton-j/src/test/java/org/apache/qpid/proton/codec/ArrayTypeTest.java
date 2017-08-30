package org.apache.qpid.proton.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.junit.Test;

public class ArrayTypeTest {

    @Test
    public void testAllFalseArray() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.clear();
        final boolean[] inputValue = new boolean[10];
        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final boolean[] outputValue = (boolean[]) decoder.readObject();

        assertEquals("Failed to round trip boolean array correctly: ", inputValue.length, outputValue.length);
    }

    @Test
    public void testAllFalseArrayofArrays() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(64);

        buffer.clear();
        final boolean[][] inputValue = new boolean[2][10];

        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final Object[] outputValue = decoder.readArray();

        assertEquals(2, outputValue.length);

        assertTrue(outputValue[0].getClass().isArray());
        assertTrue(outputValue[1].getClass().isArray());

        assertTrue(outputValue[0] instanceof boolean[]);
        assertTrue(outputValue[1] instanceof boolean[]);

        assertEquals("Failed to round trip boolean array correctly: ", inputValue.length, outputValue.length);
    }

    @Test
    public void testAllMixedBooleanArray() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.clear();
        final boolean[] inputValue = new boolean[10];
        inputValue[0] = true;
        inputValue[4] = true;
        inputValue[9] = true;

        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final boolean[] outputValue = (boolean[]) decoder.readObject();

        assertEquals("Failed to round trip boolean array correctly: ", inputValue.length, outputValue.length);
    }

    @Test
    public void testArrayOfOneSymbol() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.clear();
        Symbol[] inputValue = new Symbol[1];
        inputValue[0] = Symbol.valueOf("test");

        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final Symbol[] outputValue = (Symbol[]) decoder.readObject();

        assertEquals("Failed to round trip Symbol array correctly: ", inputValue.length, outputValue.length);
    }

    @Test
    public void testArrayOfHeaders() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(32);

        buffer.clear();
        final Header[] inputValue = new Header[10];
        for (int i = 0; i < inputValue.length; ++i) {
            inputValue[i]= new Header();
        }
        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final Header[] outputValue = (Header[]) decoder.readObject();

        assertEquals("Failed to round trip Header array correctly: ", inputValue.length, outputValue.length);
    }

    @Test
    public void testArrayOfArrayOfDisparateTypes() {
        final DecoderImpl decoder = new DecoderImpl();
        final EncoderImpl encoder = new EncoderImpl(decoder);
        AMQPDefinedTypes.registerAllTypes(decoder, encoder);
        final ByteBuffer buffer = ByteBuffer.allocate(64);

        buffer.clear();
        final Object[][] inputValue = new Object[2][10];

        // First type
        for (int i = 0; i < 10; ++i) {
            inputValue[0][i] = new Header();
        }
        // Second type
        for (int i = 0; i < 10; ++i) {
            inputValue[1][i] = new Properties();
        }

        encoder.setByteBuffer(buffer);
        encoder.writeObject(inputValue);

        buffer.clear();
        decoder.setByteBuffer(buffer);
        final Object[] outputValue = (Object[]) decoder.readObject();

        assertEquals("Failed to round trip Header array correctly: ", inputValue.length, outputValue.length);
    }
}
