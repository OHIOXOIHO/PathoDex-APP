package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Disease
import com.example.data.model.TaxonomyRefs
import com.example.data.model.User
import com.example.data.repository.PathodexRepository
import com.example.util.DeepLinks
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class NavTab {
    EXPLORER,
    FLASHCARDS,
    QUIZ,
    COMPARE,
    SETTINGS
}

enum class SortOrder {
    DATE_DESC,
    NAME_ASC,
    AGENT_ASC
}

enum class InteractionFilter {
    ALL,
    FAVORITES,
    BOOKMARKS,
    LEARNED
}

data class QuizQuestion(
    val title: String,
    val badge: String,
    val clueImage: String? = null,
    val clueExcerpt: String? = null,
    val options: List<String>,
    val correctAnswer: String,
    val targetDisease: Disease
)

class PathodexViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PathodexRepository(application)
    val tts = TextToSpeechHelper(application)

    // Navigation & View State
    private val _currentTab = MutableStateFlow(NavTab.EXPLORER)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _isCompactListMode = MutableStateFlow(repository.prefs.isCompactListMode)
    val isCompactListMode: StateFlow<Boolean> = _isCompactListMode.asStateFlow()

    private val _themeMode = MutableStateFlow(repository.prefs.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _accentId = MutableStateFlow(repository.prefs.accentId)
    val accentId: StateFlow<String> = _accentId.asStateFlow()

    private val _deepLinksEnabled = MutableStateFlow(repository.prefs.deepLinksEnabled)
    val deepLinksEnabled: StateFlow<Boolean> = _deepLinksEnabled.asStateFlow()

    private val _monographFontSize = MutableStateFlow(repository.prefs.monographFontSize)
    val monographFontSize: StateFlow<Float> = _monographFontSize.asStateFlow()

    private val _filterSheetVisible = MutableStateFlow(false)
    val filterSheetVisible: StateFlow<Boolean> = _filterSheetVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOrder.DATE_DESC)
    val selectedSort: StateFlow<SortOrder> = _selectedSort.asStateFlow()

    private val _interactionFilter = MutableStateFlow(InteractionFilter.ALL)
    val interactionFilter: StateFlow<InteractionFilter> = _interactionFilter.asStateFlow()

    private val _hideLearned = MutableStateFlow(false)
    val hideLearned: StateFlow<Boolean> = _hideLearned.asStateFlow()

    // Taxonomy filters (mirrors the website's transmission/host/scope chips)
    private val _txFilter = MutableStateFlow<String?>(null)
    val txFilter: StateFlow<String?> = _txFilter.asStateFlow()

    private val _hostFilter = MutableStateFlow<String?>(null)
    val hostFilter: StateFlow<String?> = _hostFilter.asStateFlow()

    private val _scopeFilter = MutableStateFlow<String?>(null)
    val scopeFilter: StateFlow<String?> = _scopeFilter.asStateFlow()

    private val _blurImages = MutableStateFlow(repository.prefs.blurImages)
    val blurImages: StateFlow<Boolean> = _blurImages.asStateFlow()

    private val _showNsfwDialog = MutableStateFlow(!repository.prefs.nsfwAccepted)
    val showNsfwDialog: StateFlow<Boolean> = _showNsfwDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isServerConnected = MutableStateFlow<Boolean?>(null)
    val isServerConnected: StateFlow<Boolean?> = _isServerConnected.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Modals & Sheets
    private val _selectedDossier = MutableStateFlow<Disease?>(null)
    val selectedDossier: StateFlow<Disease?> = _selectedDossier.asStateFlow()

    private val _lightboxUrl = MutableStateFlow<String?>(null)
    val lightboxUrl: StateFlow<String?> = _lightboxUrl.asStateFlow()

    private val _authDialogVisible = MutableStateFlow(false)
    val authDialogVisible: StateFlow<Boolean> = _authDialogVisible.asStateFlow()

    private val _profileDialogVisible = MutableStateFlow(false)
    val profileDialogVisible: StateFlow<Boolean> = _profileDialogVisible.asStateFlow()

    // User State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Taxonomy reference data (transmission routes / host groups)
    private val _taxonomyRefs = MutableStateFlow<TaxonomyRefs?>(null)
    val taxonomyRefs: StateFlow<TaxonomyRefs?> = _taxonomyRefs.asStateFlow()

    private var taxonomyRefsJob: Job? = null

    private val _taxonomyScreenVisible = MutableStateFlow(false)
    val taxonomyScreenVisible: StateFlow<Boolean> = _taxonomyScreenVisible.asStateFlow()

    // Flashcards State
    private val _flashcardIndex = MutableStateFlow(0)
    val flashcardIndex: StateFlow<Int> = _flashcardIndex.asStateFlow()

    private val _flashcardFlipped = MutableStateFlow(false)
    val flashcardFlipped: StateFlow<Boolean> = _flashcardFlipped.asStateFlow()

    private val _studyCategory = MutableStateFlow("all")
    val studyCategory: StateFlow<String> = _studyCategory.asStateFlow()

    // Compare State
    private val _compareIds = MutableStateFlow<List<Int>>(emptyList())
    val compareIds: StateFlow<List<Int>> = _compareIds.asStateFlow()

    // Quiz State
    private val _currentQuizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val currentQuizQuestion: StateFlow<QuizQuestion?> = _currentQuizQuestion.asStateFlow()

    private val _quizSelectedAnswer = MutableStateFlow<String?>(null)
    val quizSelectedAnswer: StateFlow<String?> = _quizSelectedAnswer.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizTotal = MutableStateFlow(0)
    val quizTotal: StateFlow<Int> = _quizTotal.asStateFlow()

    private val _quizStreak = MutableStateFlow(0)
    val quizStreak: StateFlow<Int> = _quizStreak.asStateFlow()

    // All Diseases from DB
    val allDiseases: StateFlow<List<Disease>> = repository.diseasesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    data class FilterState(
        val query: String,
        val category: String,
        val sort: SortOrder,
        val interaction: InteractionFilter,
        val hideLearned: Boolean
    )

    data class TaxFilterState(val tx: String?, val host: String?, val scope: String?)

    private val filterStateFlow: kotlinx.coroutines.flow.Flow<FilterState> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedSort,
        _interactionFilter,
        _hideLearned
    ) { query, category, sort, interaction, hideL ->
        FilterState(query, category, sort, interaction, hideL)
    }

    private val taxFilterFlow: kotlinx.coroutines.flow.Flow<TaxFilterState> = combine(
        _txFilter,
        _hostFilter,
        _scopeFilter
    ) { tx, host, scope ->
        TaxFilterState(tx, host, scope)
    }

    /**
     * میان‌بَر جستجوی هر پاتوژن.
     *
     * ساختن این رشته شامل چند هزار کاراکتر متن بالینی است؛ اگر در هر
     * فشردن کلید دوباره ساخته شود، فهرست هنگ می‌کند. بنابراین فقط یک بار
     * برای هر فهرستِ تازه از سرور ساخته می‌شود و در حافظه نگه داشته می‌شود.
     */
    private fun searchPoolFor(d: Disease): String {
        val sectionText = d.sections.joinToString(" ") { "${it.name_fa.orEmpty()} ${it.body.orEmpty()}" }
        val hostText = d.hosts.joinToString(" ") { "${it.name_fa.orEmpty()} ${it.name_en.orEmpty()}" }
        val transmissionText = d.transmission.joinToString(" ") { "${it.name_fa.orEmpty()} ${it.name_en.orEmpty()}" }
        return listOfNotNull(
            d.name_fa, d.name_en, d.agent_fa, d.agent_en,
            d.family_fa, d.family_en, d.class_fa, d.class_en,
            d.genus_fa, d.genus_en, d.host_scope, d.description_fa,
            sectionText, hostText, transmissionText
        ).joinToString(" ").lowercase()
    }

    private val searchIndex: StateFlow<Map<Int, String>> = allDiseases
        .map { list -> list.associate { it.id to searchPoolFor(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Filtered Diseases
    val filteredDiseases: StateFlow<List<Disease>> = combine(
        allDiseases,
        searchIndex,
        filterStateFlow,
        taxFilterFlow
    ) { diseases, index, filter, tax ->
        var list = diseases

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { d -> index[d.id]?.contains(q) == true }
        }

        if (filter.category != "all") {
            list = list.filter { it.category == filter.category }
        }

        // فیلترهای تاکسونومی (راه انتقال، میزبان، محدوده میزبان) — مثل سامانه‌ی وب
        if (tax.tx != null) {
            list = list.filter { d -> d.transmission.any { it.slug == tax.tx } }
        }
        if (tax.host != null) {
            list = list.filter { d -> d.hosts.any { it.slug == tax.host } }
        }
        if (tax.scope != null) {
            list = list.filter { it.host_scope == tax.scope }
        }

        when (filter.interaction) {
            InteractionFilter.FAVORITES -> list = list.filter { it.is_favorite == true }
            InteractionFilter.BOOKMARKS -> list = list.filter { it.is_bookmarked == true }
            InteractionFilter.LEARNED -> list = list.filter { it.is_learned == true }
            InteractionFilter.ALL -> {
                if (filter.hideLearned) {
                    list = list.filter { it.is_learned != true }
                }
            }
        }

        list = when (filter.sort) {
            SortOrder.NAME_ASC -> list.sortedBy { it.name_fa }
            SortOrder.AGENT_ASC -> list.sortedBy { it.agent_fa }
            SortOrder.DATE_DESC -> list.sortedByDescending { it.id }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flashcard deck
    val flashcardDeck: StateFlow<List<Disease>> = combine(allDiseases, _studyCategory) { diseases, cat ->
        if (cat == "all") diseases else diseases.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var noteDebounceJob: Job? = null

    init {
        refreshDiseases()
        checkUserSession()
        // اعمال انتخاب کاربر روی پیوندهای عمیق (activity-alias)
        DeepLinks.apply(getApplication(), repository.prefs.deepLinksEnabled)
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
        if (tab == NavTab.QUIZ && _currentQuizQuestion.value == null) {
            generateNextQuizQuestion()
        }
        if (tab == NavTab.FLASHCARDS) {
            _flashcardFlipped.value = false
        }
    }

    /** بارگذاری فهرست مرجع انتقال/میزبان؛ فقط یک بار و هنگام نیاز. */
    fun loadTaxonomyRefs(force: Boolean = false) {
        if (!force && _taxonomyRefs.value != null) return
        if (taxonomyRefsJob?.isActive == true) return
        taxonomyRefsJob = viewModelScope.launch {
            repository.loadTaxonomyRefs().getOrNull()?.let { _taxonomyRefs.value = it }
        }
    }

    /** مرحله ۲ بازیابی رمز بدون ورود (مسیر عمومی). */
    fun resetPasswordPublic(email: String, code: String, newPass: String, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
        if (newPass.length < 6) {
            onError("رمز عبور جدید باید حداقل ۶ کاراکتر باشد")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.resetPasswordPublic(email, code, newPass)
            _isLoading.value = false
            if (res.isSuccess) {
                onSuccess(res.getOrNull()?.message)
            } else {
                onError(res.exceptionOrNull()?.message ?: "کد بازیابی نامعتبر است")
            }
        }
    }

    fun setStatusMessage(msg: String?) {
        _statusMessage.value = msg
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setCategory(c: String) {
        _selectedCategory.value = c
    }

    fun setSortOrder(sort: SortOrder) {
        _selectedSort.value = sort
    }

    fun setInteractionFilter(filter: InteractionFilter) {
        _interactionFilter.value = filter
    }

    fun toggleHideLearned() {
        _hideLearned.value = !_hideLearned.value
    }

    fun setTxFilter(slug: String?) {
        _txFilter.value = if (_txFilter.value == slug) null else slug
    }

    fun setHostFilter(slug: String?) {
        _hostFilter.value = if (_hostFilter.value == slug) null else slug
    }

    fun setScopeFilter(slug: String?) {
        _scopeFilter.value = if (_scopeFilter.value == slug) null else slug
    }

    fun clearTaxonomyFilters() {
        _txFilter.value = null
        _hostFilter.value = null
        _scopeFilter.value = null
    }

    fun setCompactListMode(compact: Boolean) {
        _isCompactListMode.value = compact
        repository.prefs.isCompactListMode = compact
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        repository.prefs.themeMode = mode
    }

    /** تغییر رنگ accent سامانه؛ بلافاصله در سراسر برنامه اعمال می‌شود. */
    fun setAccent(id: String) {
        _accentId.value = id
        repository.prefs.accentId = id
    }

    /**
     * انتخاب کاربر: پیوندهای pathodex.ir در اپلیکیشن باز شوند یا در مرورگر وب.
     *
     * مقدار روی activity-alias پیوند عمیق اعمال می‌شود؛ وقتی خاموش باشد، همان
     * پیوندها در وب باز می‌شوند و اپلیکیشن دست‌نخورده می‌ماند.
     */
    fun setDeepLinksEnabled(enabled: Boolean) {
        _deepLinksEnabled.value = enabled
        repository.prefs.deepLinksEnabled = enabled
        DeepLinks.apply(getApplication(), enabled)
    }

    fun setMonographFontSize(size: Float) {
        _monographFontSize.value = size
        repository.prefs.monographFontSize = size
    }

    fun setFilterSheetVisible(visible: Boolean) {
        _filterSheetVisible.value = visible
        if (visible) loadTaxonomyRefs()
    }

    fun setTaxonomyScreenVisible(visible: Boolean) {
        _taxonomyScreenVisible.value = visible
        if (visible) loadTaxonomyRefs()
    }

    fun toggleBlur() {
        val next = !_blurImages.value
        _blurImages.value = next
        repository.prefs.blurImages = next
    }

    fun acceptNsfw(enableBlur: Boolean) {
        repository.prefs.nsfwAccepted = true
        repository.prefs.blurImages = enableBlur
        _blurImages.value = enableBlur
        _showNsfwDialog.value = false
    }

    /**
     * بارگذاری فهرست پاتوژن‌ها.
     *
     * تا وقتی حافظه‌ی محلی تازه است (پیش‌فرض ۱۲ ساعت)، هیچ داده‌ای از سرور
     * دانلود نمی‌شود و فقط دیتابیس محلی خوانده می‌شود — برنامه فوری باز می‌شود
     * و کاملاً آفلاین کار می‌کند. با «همگام‌سازی دستی» همیشه دانلود انجام می‌شود.
     */
    fun refreshDiseases(isManual: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loadDiseases(forceRefresh = isManual)
            val online = repository.isNetworkAvailable()
            _isLoading.value = false
            _isServerConnected.value = if (result.isSuccess) online else false
            if (result.isSuccess) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                _lastSyncTime.value = sdf.format(Date())
                if (isManual) {
                    _statusMessage.value = if (online) {
                        "همگام‌سازی با موفقیت انجام شد و اطلاعات پاتوژن‌ها بروزرسانی شدند."
                    } else {
                        "آفلاین هستید؛ داده‌ها از حافظه‌ی محلی دستگاه خوانده شدند."
                    }
                }
            } else {
                if (isManual) {
                    _statusMessage.value = "عدم امکان برقراری ارتباط با سرور. از اطلاعات ذخیره‌شده آفلاین استفاده می‌شود."
                }
            }
            if (_currentQuizQuestion.value == null && allDiseases.value.isNotEmpty()) {
                generateNextQuizQuestion()
            }
        }
    }

    fun manualSync() {
        refreshDiseases(isManual = true)
    }

    fun checkUserSession() {
        viewModelScope.launch {
            val res = repository.fetchCurrentUser()
            if (res.isSuccess) {
                _currentUser.value = res.getOrNull()
            }
        }
    }

    fun openDossier(disease: Disease) {
        _selectedDossier.value = disease
        // پرونده‌ی کامل (با بخش‌های بالینی) در پس‌زمینه دریافت و جایگزین می‌شود
        viewModelScope.launch {
            repository.loadDossier(disease.id).getOrNull()?.let { full ->
                if (_selectedDossier.value?.id == full.id) _selectedDossier.value = full
            }
        }
    }

    fun openDossierById(id: Int) {
        val found = allDiseases.value.find { it.id == id }
        if (found != null) {
            openDossier(found)
        } else {
            viewModelScope.launch {
                repository.loadDossier(id).getOrNull()?.let { _selectedDossier.value = it }
            }
        }
    }

    fun closeDossier() {
        _selectedDossier.value = null
    }

    fun openLightbox(url: String) {
        _lightboxUrl.value = url
    }

    fun closeLightbox() {
        _lightboxUrl.value = null
    }

    fun showAuthDialog(show: Boolean) {
        _authDialogVisible.value = show
    }

    fun showProfileDialog(show: Boolean) {
        _profileDialogVisible.value = show
    }

    fun toggleFavorite(disease: Disease) {
        val next = !(disease.is_favorite ?: false)
        viewModelScope.launch {
            repository.interact(disease.id, "favorite", next)
            _selectedDossier.value = _selectedDossier.value?.let {
                if (it.id == disease.id) it.copy(is_favorite = next) else it
            }
        }
    }

    fun toggleBookmark(disease: Disease) {
        val next = !(disease.is_bookmarked ?: false)
        viewModelScope.launch {
            repository.interact(disease.id, "bookmark", next)
            _selectedDossier.value = _selectedDossier.value?.let {
                if (it.id == disease.id) it.copy(is_bookmarked = next) else it
            }
        }
    }

    fun toggleLearned(disease: Disease) {
        val next = !(disease.is_learned ?: false)
        viewModelScope.launch {
            repository.interact(disease.id, "learned", next)
            _selectedDossier.value = _selectedDossier.value?.let {
                if (it.id == disease.id) it.copy(is_learned = next) else it
            }
        }
    }

    fun updatePersonalNote(diseaseId: Int, note: String) {
        _selectedDossier.value = _selectedDossier.value?.let {
            if (it.id == diseaseId) it.copy(personal_note = note) else it
        }
        noteDebounceJob?.cancel()
        noteDebounceJob = viewModelScope.launch {
            delay(500)
            repository.interact(diseaseId, "note", note)
        }
    }

    fun toggleCompare(diseaseId: Int) {
        val current = _compareIds.value.toMutableList()
        if (current.contains(diseaseId)) {
            current.remove(diseaseId)
            _statusMessage.value = "از مقایسه حذف شد"
        } else {
            if (current.size >= 3) {
                current.removeAt(0)
            }
            current.add(diseaseId)
            _statusMessage.value = "به مقایسه اضافه شد"
        }
        _compareIds.value = current
    }

    fun clearCompare() {
        _compareIds.value = emptyList()
    }

    // Flashcards actions
    fun setStudyCategory(c: String) {
        _studyCategory.value = c
        _flashcardIndex.value = 0
        _flashcardFlipped.value = false
    }

    fun flipFlashcard() {
        _flashcardFlipped.value = !_flashcardFlipped.value
    }

    fun nextFlashcard() {
        val deck = flashcardDeck.value
        if (deck.isNotEmpty()) {
            _flashcardFlipped.value = false
            _flashcardIndex.value = (_flashcardIndex.value + 1) % deck.size
        }
    }

    fun prevFlashcard() {
        val deck = flashcardDeck.value
        if (deck.isNotEmpty()) {
            _flashcardFlipped.value = false
            _flashcardIndex.value = (_flashcardIndex.value - 1 + deck.size) % deck.size
        }
    }

    fun shuffleFlashcards() {
        _flashcardIndex.value = 0
        _flashcardFlipped.value = false
        _statusMessage.value = "کارت‌ها بُر زده شدند"
    }

    // Quiz actions
    fun generateNextQuizQuestion() {
        val pool = allDiseases.value
        if (pool.size < 2) return

        val target = pool.random()
        val availableModes = mutableListOf("agent", "clinical")
        if (!target.family_fa.isNullOrBlank() || !target.family_en.isNullOrBlank()) {
            availableModes.add("family")
        }
        if (!target.name_en.isNullOrBlank()) {
            availableModes.add("english")
        }

        val mode = availableModes.random()
        var questionTitle = ""
        var badge = ""
        var correctAnswer = ""
        var clueImg: String? = null
        var clueExcerpt: String? = null

        when (mode) {
            "agent" -> {
                badge = "تشخیص عامل بیماری‌زا"
                questionTitle = "عامل ایجادکننده بیماری «${target.name_fa}» کدام است؟"
                correctAnswer = "${target.agent_fa} ${if (!target.agent_en.isNullOrBlank()) "(${target.agent_en})" else ""}".trim()
            }
            "family" -> {
                badge = "رده‌بندی و خانواده"
                questionTitle = "عامل «${target.agent_fa}» به کدام خانواده تعلق دارد؟"
                correctAnswer = target.family_fa ?: target.family_en ?: "باسیلاسه"
            }
            "english" -> {
                badge = "ترمینولوژی پزشکی"
                questionTitle = "نام علمی / انگلیسی بیماری «${target.name_fa}» کدام است؟"
                correctAnswer = target.name_en ?: "Anthrax"
            }
            else -> { // clinical case
                badge = "کیس بالینی"
                questionTitle = "با توجه به تظاهرات بالینی و شواهد زیر، محتمل‌ترین تشخیص کدام است؟"
                correctAnswer = target.name_fa
                clueImg = target.images?.firstOrNull()?.resolvedUrl
                clueExcerpt = target.description_fa.replace(target.name_fa, "[...]")
            }
        }

        val optionsSet = mutableSetOf(correctAnswer)
        val shuffledPool = pool.shuffled()
        for (item in shuffledPool) {
            if (optionsSet.size >= 4) break
            val distractor = when (mode) {
                "agent" -> "${item.agent_fa} ${if (!item.agent_en.isNullOrBlank()) "(${item.agent_en})" else ""}".trim()
                "family" -> item.family_fa ?: item.family_en ?: "ویبریوناسه"
                "english" -> item.name_en ?: "Infectious Disease"
                else -> item.name_fa
            }
            if (distractor.isNotBlank()) optionsSet.add(distractor)
        }

        // Fillers if needed
        val backupOptions = listOf("استافیلوکوکوس اورئوس", "مایکوباکتریوم توبرکلوزیس", "ویروس اپشتین-بار", "کاندیدا آلبیکنس")
        for (b in backupOptions) {
            if (optionsSet.size >= 4) break
            optionsSet.add(b)
        }

        _currentQuizQuestion.value = QuizQuestion(
            title = questionTitle,
            badge = badge,
            clueImage = clueImg,
            clueExcerpt = clueExcerpt,
            options = optionsSet.shuffled(),
            correctAnswer = correctAnswer,
            targetDisease = target
        )
        _quizSelectedAnswer.value = null
    }

    fun submitQuizAnswer(selectedOption: String) {
        val q = _currentQuizQuestion.value ?: return
        if (_quizSelectedAnswer.value != null) return // already answered

        _quizSelectedAnswer.value = selectedOption
        _quizTotal.value += 1
        if (selectedOption == q.correctAnswer) {
            _quizScore.value += 1
            _quizStreak.value += 1
            _statusMessage.value = "پاسخ صحیح است ✓"
        } else {
            _quizStreak.value = 0
            _statusMessage.value = "پاسخ نادرست! پاسخ صحیح: ${q.correctAnswer}"
        }
    }

    fun speak(text: String?) {
        tts.speak(text)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun login(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.login(username, pass)
            _isLoading.value = false
            if (res.isSuccess) {
                checkUserSession()
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "خطا در ورود")
            }
        }
    }

    fun register(username: String, pass: String, email: String? = null, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.register(username, pass, email)
            _isLoading.value = false
            if (res.isSuccess) {
                checkUserSession()
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "خطا در ثبت‌نام")
            }
        }
    }

    /**
     * ورود با گوگل.
     *
     * انتخاب حساب در UI انجام می‌شود و فقط id_token به اینجا می‌رسد؛ اعتبارسنجی
     * توکن و ساخت نشست سمت سرور است.
     */
    fun googleLogin(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (idToken.isBlank()) {
            onError("ورود با گوگل ناتمام ماند")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.googleLogin(idToken)
            _isLoading.value = false
            if (res.isSuccess) {
                checkUserSession()
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "ورود با گوگل ناموفق بود")
            }
        }
    }

    /** تغییر نام‌کاربری؛ توکن تازه دریافت می‌شود و نشست حفظ می‌ماند. */
    fun changeUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (newUsername.trim().length < 3) {
            onError("نام کاربری باید حداقل ۳ کاراکتر باشد")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.changeUsername(newUsername)
            _isLoading.value = false
            if (res.isSuccess) {
                checkUserSession()
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "خطا در تغییر نام کاربری")
            }
        }
    }

    /** ارسال کد تأیید به ایمیل حساب. */
    fun sendEmailCode(email: String, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.sendEmailCode(email)
            _isLoading.value = false
            if (res.isSuccess) {
                onSuccess(res.getOrNull()?.message)
            } else {
                onError(res.exceptionOrNull()?.message ?: "خطا در ارسال کد")
            }
        }
    }

    /** تأیید ایمیل با کد یک‌بارمصرف. */
    fun verifyEmail(email: String, code: String, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.verifyEmail(email, code)
            _isLoading.value = false
            if (res.isSuccess) {
                checkUserSession()
                onSuccess(res.getOrNull()?.message)
            } else {
                onError(res.exceptionOrNull()?.message ?: "کد تأیید نامعتبر است")
            }
        }
    }

    /** بازنشاندن رمز با کد بازیابی، بدون رمز فعلی. */
    fun resetPassword(email: String, code: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (newPass.length < 6) {
            onError("رمز عبور جدید باید حداقل ۶ کاراکتر باشد")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.resetPassword(email, code, newPass)
            _isLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "کد بازیابی نامعتبر است")
            }
        }
    }

    /** درخواست کد بازیابی رمز برای حساب بدون ورود. */
    fun forgotPassword(email: String, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.forgotPassword(email)
            _isLoading.value = false
            if (res.isSuccess) {
                onSuccess(res.getOrNull()?.message)
            } else {
                onError(res.exceptionOrNull()?.message ?: "خطا در ارسال کد بازیابی")
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.updatePassword(oldPass, newPass)
            _isLoading.value = false
            if (res.isSuccess) {
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "رمز عبور فعلی اشتباه است")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _statusMessage.value = "از حساب کاربری خارج شدید"
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}
