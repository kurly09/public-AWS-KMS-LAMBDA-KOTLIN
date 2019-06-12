import com.sun.org.glassfish.gmbal.Description
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertNotEquals

class EnvelopeEncryptionServiceTest {

    @Test
    @Description("AWS KMS를 이용한 envelope encryption 암복호화 테스트")
    fun encryptDecryptTest() {
        val testMsg = "안녕하세요"
        val envelopeEncryptionService = EnvelopeEncryptionService()

        val now = LocalDateTime.now()

        for(x in 0..1000){
            val encryptedMessage: EnvelopeEncryptedMessage = envelopeEncryptionService.encrypt(testMsg)
        }

        println("start: ${now} // end: ${LocalDateTime.now()}")

    }

    @Test
    @Description("AWS KMS에서 각기 다른 CMK를 사용해 암호화 하고 자동으로 복호화 가능한지 테스트")
    fun cmkChangeTest() {
        val testMsg = "안녕하세요"
        val envelopeEncryptionService1 = EnvelopeEncryptionService("USE_YOURS")
        val envelopeEncryptionService2 = EnvelopeEncryptionService("USE_YOURS")

        val encryptedMessage: EnvelopeEncryptedMessage = envelopeEncryptionService1.encrypt(testMsg)
        val encryptedMessage2: EnvelopeEncryptedMessage = envelopeEncryptionService2.encrypt(testMsg)

        val actual1: String = envelopeEncryptionService1.decrypt(encryptedMessage)
        val actual2: String = envelopeEncryptionService2.decrypt(encryptedMessage)

        assertNotEquals(encryptedMessage, encryptedMessage2)
        assertEquals(actual1, actual2)
    }

}