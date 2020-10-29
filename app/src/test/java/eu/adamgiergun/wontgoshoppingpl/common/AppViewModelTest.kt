package eu.adamgiergun.wontgoshoppingpl.common

import android.content.pm.PackageManager
import org.hamcrest.Matchers
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
            result = AppViewModel.areAllPermissionsGranted(grantResults)
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
                Assert.assertThat(result, Matchers.`is`(false))
            }
        }
    }

    @Nested
    @DisplayName("Given all grantResults are PERMISSION_GRANTED")
    inner class AllGranted : SetGrantResults(intArrayOf(PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED)) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is true")
            fun test() {
                Assert.assertThat(result, Matchers.`is`(true))
            }
        }
    }

    @Nested
    @DisplayName("Given not all grantResults are PERMISSION_GRANTED")
    inner class NotAllGranted : SetGrantResults(intArrayOf(2, PackageManager.PERMISSION_GRANTED, 1)) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                Assert.assertThat(result, Matchers.`is`(false))
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
                Assert.assertThat(result, Matchers.`is`(false))
            }
        }
    }
}