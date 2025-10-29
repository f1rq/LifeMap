package com.f1rq.lifemap.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.f1rq.lifemap.data.MapTheme
import com.f1rq.lifemap.data.MapThemeStore
import com.f1rq.lifemap.data.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.time.LocalDate
import java.time.ZoneId

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingEvent: Boolean = false,
    val addEventSuccess: Boolean = false,
    val successMessage: AnnotatedString? = null
)

class EventViewModel(
    application: Application,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val _mapTheme = MutableStateFlow(MapTheme.POSITRON)
    val mapTheme: StateFlow<MapTheme> = _mapTheme.asStateFlow()

    private val _filterText = MutableStateFlow("")
    val filterText: StateFlow<String> = _filterText.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    fun setFilterText(text: String) {
        _filterText.value = text
    }

    fun toggleCategory(category: String) {
        _selectedCategories.update { current ->
            if (current.contains(category)) current - category else current + category
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    init {
        viewModelScope.launch {
            MapThemeStore.mapThemeFlow(getApplication()).collect { theme ->
                _mapTheme.value = theme
            }
        }
    }

    fun setMapTheme(theme: MapTheme) {
        viewModelScope.launch {
            MapThemeStore.setMapTheme(getApplication(), theme)
            _mapTheme.value = theme
        }
    }
    private val _operationState = MutableStateFlow(
        EventUiState(isLoading = true)
    )

    private val _selectedLocation = MutableStateFlow<GeoPoint?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    fun updateUiState(isAddingEvent: Boolean) {
        _operationState.value = _operationState.value.copy(isAddingEvent = isAddingEvent)
    }

    fun setSelectedLocation(location: GeoPoint?) {
        _selectedLocation.value = location
    }

    fun getCurrentLocation(): GeoPoint? {
        return _selectedLocation.value
    }

    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()

    val eventCategories = listOf(
        "Work",
        "Personal",
        "School",
        "Travel",
        "Health",
        "Family",
        "Other"
    )

    val categoryColors = mapOf(
        "Work" to Color(0xFF66B3FC),       // Blue
        "Personal" to Color(0xFF5FD964),   // Green
        "School" to Color(0xFFF8BA63),     // Orange
        "Travel" to Color(0xFFE65DFF),     // Purple
        "Health" to Color(0xFFF85346),     // Red
        "Family" to Color(0xFFF63A7A),     // Pink
        "Other" to Color(0xFF8EB5C9)       // Blue Grey
    )

    data class FormState(
        val eventName: String = "",
        val eventDate: String? = "",
        val eventDesc: String? = "",
        val locationName: String? = null,
        val eventCategory: String? = null
    )

    fun getCategoryColor(category: String?): Color {
        return categoryColors[category] ?: Color(0xFF8EB5C9) // Default to Blue Grey
    }

    fun updateFormState(name: String, date: String?, desc: String?, locationName: String? = null, category: String? = null) {
        _formState.value = FormState(name, date, desc, locationName, category)
    }

    fun updateLocationName(locationName: String?) {
        _formState.value = _formState.value.copy(locationName = locationName)
    }

    fun clearFormState() {
        _formState.value = FormState()
    }

    private fun parseDateToEpochMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val parts = dateStr.trim().split('/')
            if (parts.size != 3) return null
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            val localDate = LocalDate.of(year, month, day)
            localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val uiState: StateFlow<EventUiState> = combine(
        eventRepository.getAllEvents(),
        _operationState,
        _filterText,
        _selectedCategories,
        _sortOption
    ) { events, operationState, filter, categories, sort ->
        var list = events

        if (filter.isNotBlank()) {
            val q = filter.trim().lowercase()
            list = list.filter { ev ->
                ev.name.lowercase().contains(q)
                        || (ev.locationName?.lowercase()?.contains(q) ?: false)
                        || (ev.category?.lowercase()?.contains(q) ?: false)
            }
        }

        if (categories.isNotEmpty()) {
            list = list.filter {
                ev -> ev.category != null && categories.contains(ev.category)
            }
        }

        list = run {
            val mapped = list.map { ev -> ev to (parseDateToEpochMillis(ev.date)) }

            when (sort) {
                SortOption.NEWEST -> mapped
                    .sortedByDescending { it.second ?: Long.MIN_VALUE }
                    .map { it.first }
                SortOption.OLDEST -> mapped
                    .sortedBy { it.second ?: Long.MAX_VALUE }
                    .map { it.first }
                SortOption.TITLE_AZ -> list.sortedBy { it.name.lowercase() }
                SortOption.TITLE_ZA -> list.sortedByDescending { it.name.lowercase() }
            }
        }

        operationState.copy(
            events = list,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = EventUiState(isLoading = true)
    )

    fun addEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {

            _operationState.value = _operationState.value.copy(
                isAddingEvent = true,
                successMessage = null,
                error = null,
                addEventSuccess = false
            )

            val id = eventRepository.insertEvent(event)

            kotlinx.coroutines.delay(200)

            val successMsg = if (id > 0) {
                buildAnnotatedString {
                    append("Event ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(event.name)
                    }
                    append(" added successfully!")
                }
            } else {
                null
            }

            _operationState.value = _operationState.value.copy(
                isAddingEvent = false,
                addEventSuccess = id > 0,
                successMessage = successMsg,
                error = if (id <= 0) "Failed to add event" else null
            )

        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                isAddingEvent = false,
                addEventSuccess = false,
                successMessage = null,
                error = "Error adding event: ${e.message}"
            )
        }
    }

    fun deleteEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEvent(event)
            val successMessage = buildAnnotatedString {
                append("Event ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(event.name)
                }
                append(" deleted successfully")
            }
            _operationState.value = _operationState.value.copy(
                successMessage = successMessage,
                addEventSuccess = false,
                error = null
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}",
                successMessage = null,
                addEventSuccess = false
            )
        }
    }

    fun deleteEventById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEventById(id)
            val successMessage = buildAnnotatedString {
                append("Event deleted successfully!")
            }
            _operationState.value = _operationState.value.copy(
                successMessage = successMessage,
                addEventSuccess = false,
                error = null
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}",
                successMessage = null,
                addEventSuccess = false
            )
        }
    }

    fun updateEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.updateEvent(event)
            val successMessage = buildAnnotatedString {
                append("Event ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(event.name)
                }
                append(" updated successfully!")
            }
            _operationState.value = _operationState.value.copy(
                successMessage = successMessage,
                addEventSuccess = false,
                error = null
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to update event: ${e.message}",
                successMessage = null,
                addEventSuccess = false
            )
        }
    }

    suspend fun getEventById(id: Long): Event? {
        return try {
            eventRepository.getEventById(id)
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to get event: ${e.message}",
                successMessage = null,
                addEventSuccess = false
            )
            null
        }
    }

    fun getEventsByDate(date: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            // This would need additional state management for filtered events
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to load events for date: ${e.message}",
                successMessage = null,
                addEventSuccess = false
            )
        }
    }

    fun clearError() {
        _operationState.value = _operationState.value.copy(error = null)
    }

    fun clearAddEventSuccess() {
        _operationState.value = _operationState.value.copy(addEventSuccess = false)
    }

    fun clearSuccessMessage() {
        _operationState.value = _operationState.value.copy(successMessage = null)
    }
}