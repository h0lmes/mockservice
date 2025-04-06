package com.mockachu.components;

import com.mockachu.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class ValueProducerImplTest {

    @Mock
    private RandomUtils randomUtils;

    private ValueProducer producer() {
        return new ValueProducerImpl(randomUtils);
    }

    @Test
    public void randomString() {
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(0);

        String str = producer().randomString();

        assertFalse(str.isEmpty());
    }

    @Test
    public void randomString2() {
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(true);

        String str = producer().randomString();

        assertFalse(str.isEmpty());
    }

    @Test
    public void randomNumberString_Integer() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(5);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);

        String str = producer().randomNumberString();

        assertFalse(str.isEmpty());
        assertFalse(str.contains("."));
    }

    @Test
    public void randomNumberString_Float() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(5);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(true);

        String str = producer().randomNumberString();

        assertFalse(str.isEmpty());
        assertTrue(str.contains("."));
    }

    @Test
    public void randomNumberString_Float1Digit() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(0);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(true);

        String str = producer().randomNumberString();

        assertFalse(str.isEmpty());
        assertTrue(str.contains("."));
    }
}
