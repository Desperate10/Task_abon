package ua.POE.Task_abon.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object UniversalInsetsHelper {

    /**
     * Применяет корректные отступы под статус-бар (top) и клавиатуру (bottom)
     * Работает на Android 12–15.
     *
     * Подходит для AppBarLayout, Toolbar, ScrollView, корневых layout фрагментов.
     */
    fun applyEdgeToEdgeInsets(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->

            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val top = statusBars.top
            val bottom = maxOf(ime.bottom, navBars.bottom)

            view.setPadding(
                view.paddingLeft,
                top,
                view.paddingRight,
                bottom
            )

            insets
        }
    }

    /**
     * Этот вариант используется для AppBarLayout,
     * где нужен только верхний inset (status bar).
     */
    fun applyTopInsets(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->

            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.setPadding(
                view.paddingLeft,
                statusBars.top,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }
    }
}
