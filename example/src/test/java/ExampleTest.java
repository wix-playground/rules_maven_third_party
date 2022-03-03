import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExampleTest {
    @Test
    public void one_plus_two_is_three() {
        int sum = new Example().sum(1, 2);

        assertEquals(3, sum);
    }
}