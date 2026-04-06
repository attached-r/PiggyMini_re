import common.util.BCryptUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BCryptUtilTest {

    @Test
    public void testEncodeWithDefaultStrength() {
        String password = "testPassword123";
        String encoded = BCryptUtil.encode(password);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$"));
        System.out.println("默认强度加密结果: " + encoded);
    }

    @Test
    public void testEncodeWithCustomStrength() {
        String password = "testPassword123";
        String encoded = BCryptUtil.encode(password, 12);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$12$"));
        System.out.println("自定义强度(12)加密结果: " + encoded);
    }

    @Test
    public void testMatchesWithCorrectPassword() {
        String password = "testPassword123";
        String encoded = BCryptUtil.encode(password);

        assertTrue(BCryptUtil.matches(password, encoded));
    }

    @Test
    public void testMatchesWithWrongPassword() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encoded = BCryptUtil.encode(password);

        assertFalse(BCryptUtil.matches(wrongPassword, encoded));
    }

    @Test
    public void testMatchesWithNullPassword() {
        String password = "testPassword123";
        String encoded = BCryptUtil.encode(password);

        assertFalse(BCryptUtil.matches(null, encoded));
        assertFalse(BCryptUtil.matches("", encoded));
    }

    @Test
    public void testMatchesWithNullEncodedPassword() {
        String password = "testPassword123";

        assertFalse(BCryptUtil.matches(password, null));
        assertFalse(BCryptUtil.matches(password, ""));
    }

    @Test
    public void testEncodeWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            BCryptUtil.encode(null);
        });
    }

    @Test
    public void testEncodeWithEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            BCryptUtil.encode("");
        });
    }

    @Test
    public void testEncodeWithInvalidStrength() {
        String password = "testPassword123";

        assertThrows(IllegalArgumentException.class, () -> {
            BCryptUtil.encode(password, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            BCryptUtil.encode(password, 32);
        });
    }

    @Test
    public void testNeedsUpgrade() {
        String password = "testPassword123";

        String lowStrengthEncoded = BCryptUtil.encode(password, 5);
        assertTrue(BCryptUtil.needsUpgrade(lowStrengthEncoded, 10));

        String defaultStrengthEncoded = BCryptUtil.encode(password, 10);
        assertFalse(BCryptUtil.needsUpgrade(defaultStrengthEncoded, 10));

        String highStrengthEncoded = BCryptUtil.encode(password, 12);
        assertFalse(BCryptUtil.needsUpgrade(highStrengthEncoded, 10));
    }

    @Test
    public void testNeedsUpgradeWithInvalidPassword() {
        assertTrue(BCryptUtil.needsUpgrade(null, 10));
        assertTrue(BCryptUtil.needsUpgrade("", 10));
    }

    @Test
    public void testIsDefaultStrength() {
        String password = "testPassword123";

        String defaultStrengthEncoded = BCryptUtil.encode(password, 10);
        assertTrue(BCryptUtil.isDefaultStrength(defaultStrengthEncoded));

        String lowStrengthEncoded = BCryptUtil.encode(password, 5);
        assertFalse(BCryptUtil.isDefaultStrength(lowStrengthEncoded));

        String highStrengthEncoded = BCryptUtil.encode(password, 15);
        assertFalse(BCryptUtil.isDefaultStrength(highStrengthEncoded));
    }

    @Test
    public void testDifferentPasswordsProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";

        String encoded1 = BCryptUtil.encode(password1);
        String encoded2 = BCryptUtil.encode(password2);

        assertNotEquals(encoded1, encoded2);
    }

    @Test
    public void testSamePasswordProducesDifferentHashes() {
        String password = "testPassword123";

        String encoded1 = BCryptUtil.encode(password);
        String encoded2 = BCryptUtil.encode(password);

        assertNotEquals(encoded1, encoded2);

        assertTrue(BCryptUtil.matches(password, encoded1));
        assertTrue(BCryptUtil.matches(password, encoded2));
    }
}
