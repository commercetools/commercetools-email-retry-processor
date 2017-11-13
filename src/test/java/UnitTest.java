import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitTest {

    @Before
    public void setup() {
    }


    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void methodName_WithSpecificState_ShouldBeTrue() {
        assertThat(true).isTrue();
    }
}
