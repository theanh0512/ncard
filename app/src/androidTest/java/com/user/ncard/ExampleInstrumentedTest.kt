package com.user.ncard

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        shortestPalindrome("abab")
    }

    fun shortestPalindrome(s: String): Int {
        //String reversed = new StringBuilder(s).reverse().toString();
        val reversed = "baba"
        val d = Array(s.length + 1) { IntArray(s.length + 1) }
        for (j in 1..s.length) {
            d[0][j] = j
        }
        for (i in 1..s.length) {
            for (j in 1..reversed.length) {
                if (s[i - 1] == reversed[j - 1])
                    d[i][j] = d[i - 1][j - 1]
                else d[i][j] = Math.min(d[i][j-1] + 1,d[i-1][j]+1)
            }
        }
        return d[s.length][s.length] / 2
    }
}
