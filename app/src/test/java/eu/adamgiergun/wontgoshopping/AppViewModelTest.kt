package eu.adamgiergun.wontgoshopping

import android.content.pm.PackageManager.PERMISSION_GRANTED
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppViewModelTest {

    private lateinit var grantResults: IntArray
    private var result = false

    abstract inner class SetGrantResults(private val testedGrantResults: IntArray) {
        @BeforeEach
        fun setup() {
            grantResults = testedGrantResults
        }
    }

    abstract inner class AreAllPermissionsGranted {
        @BeforeEach
        fun getResult() {
            result = areAllPermissionsGranted(grantResults)
        }
    }

    @Nested
    @DisplayName("Given grantResults array is empty")
    inner class EmptyArray : SetGrantResults(intArrayOf()) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                Assert.assertThat(result, `is`(false))
            }
        }
    }

    @Nested
    @DisplayName("Given all grantResults are PERMISSION_GRANTED")
    inner class AllGranted : SetGrantResults(intArrayOf(PERMISSION_GRANTED, PERMISSION_GRANTED)) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is true")
            fun test() {
                Assert.assertThat(result, `is`(true))
            }
        }
    }

    @Nested
    @DisplayName("Given not all grantResults are PERMISSION_GRANTED")
    inner class NotAllGranted : SetGrantResults(intArrayOf(2, PERMISSION_GRANTED, 1)) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                Assert.assertThat(result, `is`(false))
            }
        }
    }

    @Nested
    @DisplayName("Given all grantResults are not PERMISSION_GRANTED")
    inner class AllNotGranted : SetGrantResults(intArrayOf(2, 1, -3, 7)) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                Assert.assertThat(result, `is`(false))
            }
        }
    }
}