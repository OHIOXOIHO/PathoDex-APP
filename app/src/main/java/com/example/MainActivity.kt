package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.GoogleAuthHelper
import com.example.ui.components.GoogleLoginRequester
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PdSpacing
import com.example.ui.components.AuthDialog
import com.example.ui.components.DossierSheet
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.LightboxDialog
import com.example.ui.components.NsfwFirstLaunchDialog
import com.example.ui.components.PdCard
import com.example.ui.components.PdIconAction
import com.example.ui.components.PdPrimaryButton
import com.example.ui.components.PdSecondaryButton
import com.example.ui.components.PathodexBottomBar
import com.example.ui.components.PathodexTopBar
import com.example.ui.components.ProfileDialog
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.FlashcardScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TaxonomyScreen
import com.example.ui.theme.PathodexTheme
import com.example.ui.viewmodel.InteractionFilter
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.PathodexViewModel
import com.example.ui.viewmodel.SortOrder
import com.example.util.DeepLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: PathodexViewModel by viewModels()

    /**
     * نشانی پیوند عمیق ورودی.
     *
     * با `launchMode=singleTask`، اپلیکیشن در حال اجرا باشد پیوند جدید از
     * [onNewIntent] می‌آید؛ وگرنه از intent اولیه‌ی onCreate. هر دو اینجا جمع
     * می‌شوند تا مسیریابی یک‌جا انجام شود.
     */
    private val deepLink = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLink.value = intent?.data
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentId by viewModel.accentId.collectAsState()
            val deepLinkUri by deepLink.collectAsState()
            PathodexTheme(themeMode = themeMode, accentId = accentId) {
                ProvideAppDirection {
                    DeepLinkRouter(
                        uri = deepLinkUri,
                        viewModel = viewModel,
                        onConsumed = { deepLink.value = null }
                    )
                    PathodexApp(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink.value = intent.data
    }
}

/** مسیریابی پیوند عمیق به همان محتوای اپلیکیشن. */
@Composable
private fun DeepLinkRouter(
    uri: Uri?,
    viewModel: PathodexViewModel,
    onConsumed: () -> Unit
) {
    LaunchedEffect(uri) {
        val link = DeepLinks.parse(uri) ?: return@LaunchedEffect
        when (link.route) {
            "disease" -> link.id?.let { viewModel.openDossierById(it) }
            "category" -> {
                val slug = link.slug
                val match = viewModel.allDiseases.value.firstOrNull {
                    it.category_slug == slug || it.category == slug
                }
                viewModel.setCategory(match?.category ?: "all")
                viewModel.setTab(NavTab.EXPLORER)
            }
            "taxonomy" -> viewModel.setTaxonomyScreenVisible(true)
            "study" -> viewModel.setTab(NavTab.FLASHCARDS)
            "quiz" -> viewModel.setTab(NavTab.QUIZ)
            "compare" -> viewModel.setTab(NavTab.COMPARE)
            "about" -> viewModel.setTab(NavTab.SETTINGS)
        }
        onConsumed()
    }
}

/**
 * چیدمان همیشه راست‌به‌چپ است.
 *
 * محتوای برنامه کاملاً فارسی است و زبان پیش‌فرض هم فارسی، پس چیدمان نباید به
 * زبان دستگاه کاربر وابسته باشد. اگر به زبان سیستم گره بخورد، روی دستگاهی با
 * زبان انگلیسی ترتیب دکمه‌ها برعکس، آیکون‌های جهت‌دار غلط و فاصله‌گذاری‌های
 * start/end در جهت نادرست نمایش داده می‌شوند.
 *
 * متن‌های لاتین (نام‌های علمی، سرده‌ها، نشانی‌ها) جداگانه با
 * [com.example.ui.components.PdLtrText] در جهت خودشان قرار می‌گیرند.
 */
@Composable
private fun ProvideAppDirection(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        content = content
    )
}

@Composable
fun PathodexApp(viewModel: PathodexViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allDiseases by viewModel.allDiseases.collectAsState()
    val filteredDiseases by viewModel.filteredDiseases.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val interactionFilter by viewModel.interactionFilter.collectAsState()
    val hideLearned by viewModel.hideLearned.collectAsState()
    val taxonomyRefs by viewModel.taxonomyRefs.collectAsState()
    val txFilter by viewModel.txFilter.collectAsState()
    val hostFilter by viewModel.hostFilter.collectAsState()
    val scopeFilter by viewModel.scopeFilter.collectAsState()
    val taxonomyScreenVisible by viewModel.taxonomyScreenVisible.collectAsState()
    val blurImages by viewModel.blurImages.collectAsState()
    val showNsfwDialog by viewModel.showNsfwDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val isCompactListMode by viewModel.isCompactListMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentId by viewModel.accentId.collectAsState()
    val filterSheetVisible by viewModel.filterSheetVisible.collectAsState()
    val deepLinksEnabled by viewModel.deepLinksEnabled.collectAsState()

    val selectedDossier by viewModel.selectedDossier.collectAsState()
    val lightboxUrl by viewModel.lightboxUrl.collectAsState()
    val authDialogVisible by viewModel.authDialogVisible.collectAsState()
    val profileDialogVisible by viewModel.profileDialogVisible.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val flashcardDeck by viewModel.flashcardDeck.collectAsState()
    val flashcardIndex by viewModel.flashcardIndex.collectAsState()
    val flashcardFlipped by viewModel.flashcardFlipped.collectAsState()
    val studyCategory by viewModel.studyCategory.collectAsState()

    val compareIds by viewModel.compareIds.collectAsState()

    val currentQuizQuestion by viewModel.currentQuizQuestion.collectAsState()
    val quizSelectedAnswer by viewModel.quizSelectedAnswer.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val quizTotal by viewModel.quizTotal.collectAsState()
    val quizStreak by viewModel.quizStreak.collectAsState()

    val isServerConnected by viewModel.isServerConnected.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Status message notifications
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Back handling: pop overlays first, return to Explorer tab if on other tabs, and prompt exit confirmation if already on Explorer
    BackHandler(enabled = true) {
        when {
            showExitDialog -> showExitDialog = false
            lightboxUrl != null -> viewModel.closeLightbox()
            selectedDossier != null -> viewModel.closeDossier()
            filterSheetVisible -> viewModel.setFilterSheetVisible(false)
            // درخت تاکسونومی یک overlay تمام‌صفحه است؛ بازگشت باید آن را ببندد،
            // وگرنه کاربر روی تب تنظیمات گیر می‌کند و نمی‌تواند به فهرست برگردد.
            taxonomyScreenVisible -> viewModel.setTaxonomyScreenVisible(false)
            currentTab != NavTab.EXPLORER -> viewModel.setTab(NavTab.EXPLORER)
            else -> showExitDialog = true
        }
    }

    Scaffold(
        bottomBar = {
            PathodexBottomBar(
                currentTab = currentTab,
                compareCount = compareIds.size,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.EXPLORER -> {
                    val hasActiveFilters = selectedSort != SortOrder.DATE_DESC ||
                            interactionFilter != InteractionFilter.ALL ||
                            hideLearned ||
                            txFilter != null || hostFilter != null || scopeFilter != null

                    ExplorerScreen(
                        diseases = filteredDiseases,
                        allDiseases = allDiseases,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        isCompactListMode = isCompactListMode,
                        globalBlur = blurImages,
                        compareIds = compareIds,
                        hasActiveFilters = hasActiveFilters,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryChange = { viewModel.setCategory(it) },
                        onToggleDensityMode = { viewModel.setCompactListMode(!isCompactListMode) },
                        onOpenFilterSheet = { viewModel.setFilterSheetVisible(true) },
                        onCardClick = { viewModel.openDossier(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                        onToggleLearned = { viewModel.toggleLearned(it) },
                        onToggleCompare = { viewModel.toggleCompare(it.id) },
                        onSpeak = { viewModel.speak(it) },
                        onRandomDossier = {
                            if (allDiseases.isNotEmpty()) {
                                viewModel.openDossier(allDiseases.random())
                            }
                        },
                        isLoading = isLoading,
                        onRefreshData = { viewModel.manualSync() }
                    )
                }

                NavTab.FLASHCARDS -> {
                    FlashcardScreen(
                        deck = flashcardDeck,
                        currentIndex = flashcardIndex,
                        isFlipped = flashcardFlipped,
                        selectedCategory = studyCategory,
                        allDiseases = allDiseases,
                        onCategoryChange = { viewModel.setStudyCategory(it) },
                        onFlip = { viewModel.flipFlashcard() },
                        onNext = { viewModel.nextFlashcard() },
                        onPrev = { viewModel.prevFlashcard() },
                        onShuffle = { viewModel.shuffleFlashcards() },
                        onToggleLearned = { viewModel.toggleLearned(it) },
                        onOpenDossier = { viewModel.openDossier(it) },
                        onSpeak = { viewModel.speak(it) }
                    )
                }

                NavTab.QUIZ -> {
                    QuizScreen(
                        question = currentQuizQuestion,
                        selectedAnswer = quizSelectedAnswer,
                        score = quizScore,
                        total = quizTotal,
                        streak = quizStreak,
                        onSelectOption = { viewModel.submitQuizAnswer(it) },
                        onNextQuestion = { viewModel.generateNextQuizQuestion() },
                        onOpenDossier = { viewModel.openDossier(it) }
                    )
                }

                NavTab.COMPARE -> {
                    CompareScreen(
                        compareIds = compareIds,
                        allDiseases = allDiseases,
                        onToggleCompare = { viewModel.toggleCompare(it) },
                        onClearCompare = { viewModel.clearCompare() },
                        onOpenDossier = { viewModel.openDossier(it) }
                    )
                }

                NavTab.SETTINGS -> {
                    SettingsScreen(
                        currentUser = currentUser,
                        themeMode = themeMode,
                        accentId = accentId,
                        isCompactListMode = isCompactListMode,
                        blurImages = blurImages,
                        deepLinksEnabled = deepLinksEnabled,
                        allDiseases = allDiseases,
                        isLoading = isLoading,
                        isServerConnected = isServerConnected,
                        lastSyncTime = lastSyncTime,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetAccent = { viewModel.setAccent(it) },
                        onSetCompactMode = { viewModel.setCompactListMode(it) },
                        onToggleBlur = { viewModel.toggleBlur() },
                        onSetDeepLinksEnabled = { viewModel.setDeepLinksEnabled(it) },
                        onRefreshData = { viewModel.manualSync() },
                        onOpenAuth = { viewModel.showAuthDialog(true) },
                        onOpenProfile = { viewModel.showProfileDialog(true) },
                        onLogout = { viewModel.logout() },
                        onOpenDossier = { viewModel.openDossier(it) },
                        onOpenTaxonomy = { viewModel.setTaxonomyScreenVisible(true) }
                    )
                }
            }

            // Filter BottomSheet for main explorer
            if (filterSheetVisible) {
                FilterBottomSheet(
                    selectedSort = selectedSort,
                    interactionFilter = interactionFilter,
                    hideLearned = hideLearned,
                    onSortChange = { viewModel.setSortOrder(it) },
                    onInteractionChange = { viewModel.setInteractionFilter(it) },
                    onToggleHideLearned = { viewModel.toggleHideLearned() },
                    onResetFilters = {
                        viewModel.setSortOrder(SortOrder.DATE_DESC)
                        viewModel.setInteractionFilter(InteractionFilter.ALL)
                        if (hideLearned) viewModel.toggleHideLearned()
                        viewModel.clearTaxonomyFilters()
                    },
                    onDismiss = { viewModel.setFilterSheetVisible(false) },
                    taxonomyRefs = taxonomyRefs,
                    txFilter = txFilter,
                    hostFilter = hostFilter,
                    scopeFilter = scopeFilter,
                    onTxFilterChange = { viewModel.setTxFilter(it) },
                    onHostFilterChange = { viewModel.setHostFilter(it) },
                    onScopeFilterChange = { viewModel.setScopeFilter(it) }
                )
            }

            // NSFW First Launch Disclaimer
            if (showNsfwDialog) {
                NsfwFirstLaunchDialog(
                    onAccept = { enableBlur ->
                        viewModel.acceptNsfw(enableBlur)
                    }
                )
            }

            // Clinical Dossier Sheet
            selectedDossier?.let { disease ->
                DossierSheet(
                    disease = disease,
                    allDiseases = allDiseases,
                    isCompared = compareIds.contains(disease.id),
                    onDismiss = { viewModel.closeDossier() },
                    onToggleFavorite = { viewModel.toggleFavorite(disease) },
                    onToggleBookmark = { viewModel.toggleBookmark(disease) },
                    onToggleLearned = { viewModel.toggleLearned(disease) },
                    onToggleCompare = { viewModel.toggleCompare(disease.id) },
                    onNoteChange = { note -> viewModel.updatePersonalNote(disease.id, note) },
                    onOpenLightbox = { url -> viewModel.openLightbox(url) },
                    onOpenRelated = { related -> viewModel.openDossier(related) },
                    onSpeak = { text -> viewModel.speak(text) },
                    // چیپ‌های انتقال/میزبان: فیلتر همان مقدار و بازگشت به فهرست
                    onChipClick = { kind, slug ->
                        if (kind == "tx") viewModel.setTxFilter(slug) else viewModel.setHostFilter(slug)
                        viewModel.closeDossier()
                        viewModel.setTab(NavTab.EXPLORER)
                    }
                )
            }

            // Lightbox Fullscreen Pinch-to-zoom
            lightboxUrl?.let { url ->
                LightboxDialog(
                    imageUrl = url,
                    onDismiss = { viewModel.closeLightbox() }
                )
            }

            // Auth Dialog (Login & Register)
            if (authDialogVisible) {
                AuthDialog(
                    isLoading = isLoading,
                    onDismiss = { viewModel.showAuthDialog(false) },
                    onLogin = { u, p, onSuccess, onError ->
                        viewModel.login(u, p, onSuccess, onError)
                    },
                    onRegister = { u, p, email, onSuccess, onError ->
                        viewModel.register(u, p, email, onSuccess, onError)
                    },
                    onForgotPassword = { email, onSuccess, onError ->
                        viewModel.forgotPassword(email, onSuccess, onError)
                    },
                    onResetPassword = { email, code, newPass, onSuccess, onError ->
                        viewModel.resetPasswordPublic(email, code, newPass, onSuccess, onError)
                    },
                    onGoogleLogin = GoogleLoginRequester { onSuccess, onError ->
                        val activity = context as? Activity
                        if (activity == null) {
                            onError("برای ورود با گوگل به پنجره‌ی برنامه نیاز است")
                        } else {
                            coroutineScope.launch {
                                GoogleAuthHelper.signIn(activity).fold(
                                    onSuccess = { idToken ->
                                        viewModel.googleLogin(idToken, onSuccess, onError)
                                    },
                                    onFailure = { e ->
                                        val googleError = e as? GoogleAuthHelper.GoogleSignInException
                                        onError(
                                            buildString {
                                                append(
                                                    googleError?.message
                                                        ?: e.message
                                                        ?: "ورود با گوگل ناموفق بود"
                                                )
                                                if (googleError != null) {
                                                    append("\n\n")
                                                    append(googleError.diagnostics)
                                                    append("\n\nبا نام کاربری و رمز عبور هم می‌توانید وارد شوید.")
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }

            // User Profile Dialog
            if (profileDialogVisible && currentUser != null) {
                ProfileDialog(
                    user = currentUser!!,
                    learnedCount = allDiseases.count { it.is_learned == true },
                    bookmarkCount = allDiseases.count { it.is_bookmarked == true },
                    favoriteCount = allDiseases.count { it.is_favorite == true },
                    onDismiss = { viewModel.showProfileDialog(false) },
                    onLogout = { viewModel.logout() },
                    onUpdatePassword = { oldPass, newPass, onSuccess, onError ->
                        viewModel.updatePassword(oldPass, newPass, onSuccess, onError)
                    },
                    onChangeUsername = { newUsername, onSuccess, onError ->
                        viewModel.changeUsername(newUsername, onSuccess, onError)
                    },
                    onSendEmailCode = { email, onSuccess, onError ->
                        viewModel.sendEmailCode(email, onSuccess, onError)
                    },
                    onVerifyEmail = { email, code, onSuccess, onError ->
                        viewModel.verifyEmail(email, code, onSuccess, onError)
                    },
                    onResetPassword = { email, code, newPass, onSuccess, onError ->
                        viewModel.resetPassword(email, code, newPass, onSuccess, onError)
                    }
                )
            }

            // Exit Confirmation Dialog
            if (showExitDialog) {
                Dialog(onDismissRequest = { showExitDialog = false }) {
                    PdCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PdSpacing.xl)
                            .testTag("exit_confirmation_dialog")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PdSpacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Badge Icon
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(PdSpacing.md))

                            Text(
                                text = "خروج از سامانه PathoDex",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(PdSpacing.xs))

                            Text(
                                text = "آیا از بستن و خروج از برنامه اطمینان دارید؟",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(PdSpacing.xxl))

                            // Action Buttons Row (Equal, Spacious, Modern)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PdSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PdSecondaryButton(
                                    text = "انصراف",
                                    onClick = { showExitDialog = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dismiss_exit_button")
                                )

                                PdPrimaryButton(
                                    text = "خروج",
                                    onClick = {
                                        showExitDialog = false
                                        (context as? Activity)?.finish()
                                    },
                                    leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_exit_button")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Taxonomy Tree overlay (mirrors the website's taxonomy page)
    if (taxonomyScreenVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                // لبه‌به‌لبه: نوار وضعیت و نوار سیستم پایین روی محتوا قرار نگیرند
                // تا دکمه‌ی بازگشت و عنوان درخت خارج از صفحه‌ی قابل‌مشاهده نمانند.
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("taxonomy_overlay")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PdSpacing.sm, vertical = PdSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    PdIconAction(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        onClick = { viewModel.setTaxonomyScreenVisible(false) },
                        tint = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "درخت تاکسونومی",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TaxonomyScreen(
                    allDiseases = allDiseases,
                    onOpenDossier = { d ->
                        viewModel.setTaxonomyScreenVisible(false)
                        viewModel.openDossier(d)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
