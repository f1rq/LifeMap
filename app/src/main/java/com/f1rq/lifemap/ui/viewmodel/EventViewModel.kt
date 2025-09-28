package com.f1rq.lifemap.ui.viewmodel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingEvent: Boolean = false,
    val addEventSuccess: Boolean = false,
    val successMessage: AnnotatedString? = null
)

class EventViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

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

    data class FormState(
        val eventName: String = "",
        val eventDate: String? = "",
        val eventDesc: String? = "",
        val locationName: String? = null,
        val eventCategory: String? = null
    )

    fun updateFormState(name: String, date: String?, desc: String?, locationName: String? = null, category: String? = null) {
        _formState.value = FormState(name, date, desc, locationName, category)
    }

    fun updateLocationName(locationName: String?) {
        _formState.value = _formState.value.copy(locationName = locationName)
    }

    fun clearFormState() {
        _formState.value = FormState()
    }

    val uiState: StateFlow<EventUiState> = combine(
        eventRepository.getAllEvents(),
        _operationState
    ) { events, operationState ->
        val result = operationState.copy(
            events = events,
            isLoading = false
        )
        result
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

    fun testSuccessMessage() {
        val testMessage = buildAnnotatedString {
            append("TEST MESSAGE - Event ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("'TestEvent'")
            }
            append(" added successfully!")
        }
        _operationState.value = _operationState.value.copy(
            successMessage = testMessage
        )
    }
}