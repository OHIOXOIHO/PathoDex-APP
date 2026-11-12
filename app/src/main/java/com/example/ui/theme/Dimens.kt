package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * مقیاس فاصله‌گذاری سامانه‌ی طراحی.
 *
 * همه‌ی فاصله‌ها از این مقیاس ② توانی (xxs=2 تا huge=48) انتخاب می‌شوند تا
 * ریتم عمودی و افقی صفحه‌ها در کل برنامه یکدست بماند. مقدار دلخواه فقط وقتی
 * مجاز است که در این شمار نباشد و دلیل محلی داشته باشد.
 */
object PdSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp

    /**
     * پله‌های ۲dp مکمل برای چیدمان‌های فشرده‌ی داخل کارت‌ها و فهرست‌ها.
     *
     * مقادیر ۶/۱۰/۱۴/۱۸ در نیم‌پله‌های بالا جا نمی‌شدند و قبلاً به‌صورت رقم
     * ثابت پخش شده بودند؛ حالا به همان مقیاس تعلق دارند تا ریتم عمودی یکدست بماند.
     */
    val tight = 6.dp
    val compact = 10.dp
    val cozy = 14.dp
    val roomy = 18.dp

    /**
     * تورفتگی هر سطح درخت تاکسونومی.
     *
     * کوتاه‌ترین مقدار قابل‌قبول تا با سه سطح تودرتو (دسته › رده › خانواده ›
     * پاتوژن) عمق کافی داشته باشیم بدون اینکه عنوان‌ها بی‌جهت کوتاه شوند.
     */
    val treeIndent = 14.dp

    /**
     * فاصله‌ی پایانی محتوای پیمایش‌پذیر تا نوار پیمایش پایین و نوار سیستم
     * روی محتوا قرار نگیرند.
     */
    val scrollBottomInset = 85.dp
    val sheetBottomInset = 40.dp
}

/** گوشه‌های گرد سامانه. هر کامپوننت فقط یکی از این مقادیر را برمی‌گزیند. */
object PdRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 28.dp
    val pill = 999.dp
}

object PdShapes {
    val xs = RoundedCornerShape(PdRadius.xs)
    val sm = RoundedCornerShape(PdRadius.sm)
    val md = RoundedCornerShape(PdRadius.md)
    val lg = RoundedCornerShape(PdRadius.lg)
    val xl = RoundedCornerShape(PdRadius.xl)
    val xxl = RoundedCornerShape(PdRadius.xxl)
    val pill = RoundedCornerShape(PdRadius.pill)
}

/**
 * اندازه‌های استاندارد اجزا.
 *
 * [touchTarget] حداقل ٤٨dp است طبق دستور العمل دسترس‌پذیری اندروید؛ اجزای
 * لمسی نباید کوچک‌تر از این باشند حتی اگر ظاهری کوچک‌تر داشته باشند.
 */
object PdSize {
    val touchTarget = 48.dp
    val iconXs = 14.dp
    val iconSm = 16.dp
    val iconMd = 20.dp
    val iconLg = 24.dp
    val buttonHeight = 48.dp
    val buttonHeightSm = 40.dp
    val chipHeight = 32.dp
    val listThumb = 50.dp
    val avatar = 40.dp

    /** کاشی گرد پشت آیکون در سربرگ صفحه‌ها. */
    val iconTile = 36.dp

    /** قطر نشانگر برگ در درخت تاکسونومی. */
    val treeDot = 6.dp
    val topBarHeight = 64.dp
    val bottomBarHeight = 64.dp

    /** ارتفاع ثابت فیلد جستجو — با متن بلند بزرگ نمی‌شود. */
    val searchFieldHeight = 56.dp

    // کارت شبکه‌ای
    val gridCardHeight = 236.dp
    val gridMediaHeight = 112.dp
    val gridGradientHeight = 30.dp

    // فلش‌کارت و محتوای بالینی
    val flashMediaHeight = 260.dp
    val contentCardMaxHeight = 520.dp
}

/** مدت‌های انیمیشن؛ برای سرعت حساس شده و از انیمیشن‌های سنگین اجتناب می‌کنیم. */
object PdMotion {
    const val fast = 150
    const val normal = 250
    const val slow = 400
}

/**
 * مقیاس اندازه‌ی قلم برنامه.
 *
 * نام هر عضو همان اندازه‌ی خودش است (`sp11_5` یعنی ۱۱.۵sp) تا تبدیل رقم
 * پراکنده در صفحه‌ها به قرارداد یک‌باره و بدون هیچ تغییری در ظاهر انجام شود.
 * عنوان‌ها و بدنه‌ی اصلی از [com.example.ui.theme.Typography] می‌آیند؛ این جدول
 * برای اندازه‌های ظریف‌تر داخل کارت‌ها و فهرست‌هاست.
 */
object PdType {
    val sp9_5 = 9.5.sp
    val sp10 = 10.sp
    val sp10_5 = 10.5.sp
    val sp11 = 11.sp
    val sp11_5 = 11.5.sp
    val sp12 = 12.sp
    val sp12_5 = 12.5.sp
    val sp13 = 13.sp
    val sp13_5 = 13.5.sp
    val sp14 = 14.sp
    val sp14_5 = 14.5.sp
    val sp15 = 15.sp
    val sp16 = 16.sp
    val sp16_5 = 16.5.sp
    val sp17 = 17.sp
    val sp18 = 18.sp
    val sp20 = 20.sp
    val sp22 = 22.sp
    val sp23 = 23.sp
    val sp24 = 24.sp
    val sp28 = 28.sp
    val sp32 = 32.sp
}
