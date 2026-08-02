package eu.adamgiergun.wontgoshoppingpl.common

import android.Manifest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppViewModelTest {
    abstract inner class SetGrantResults(private val grantResults: Map<String, Boolean>) {
        abstract inner class AreAllPermissionsGranted {
            val result = AppViewModel.areAllPermissionsGranted(grantResults)
        }
    }

    @Nested
    @DisplayName("Given grantResults map is empty")
    inner class EmptyMap : SetGrantResults(emptyMap()) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                assertThat(result, `is`(false))
            }
        }
    }

    @Nested
    @DisplayName("Given all permissions are granted")
    inner class AllGranted : SetGrantResults(
        mapOf(Manifest.permission.READ_CALENDAR to true, Manifest.permission.WRITE_CALENDAR to true)
    ) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is true")
            fun test() {
                assertThat(result, `is`(true))
            }
        }
    }

    @Nested
    @DisplayName("Given only some permissions are granted")
    inner class NotAllGranted : SetGrantResults(
        mapOf(Manifest.permission.READ_CALENDAR to true, Manifest.permission.WRITE_CALENDAR to false)
    ) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                assertThat(result, `is`(false))
            }
        }
    }

    @Nested
    @DisplayName("Given no permission is granted")
    inner class AllNotGranted : SetGrantResults(
        mapOf(Manifest.permission.READ_CALENDAR to false, Manifest.permission.WRITE_CALENDAR to false)
    ) {
        @Nested
        @DisplayName("When areAllPermissionsGranted(grantResults)")
        inner class TestResultOf: AreAllPermissionsGranted() {
            @Test
            @DisplayName("Then result is false")
            fun test() {
                assertThat(result, `is`(false))
            }
        }
    }
}
