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

    val uiState: StateFlow<EventUiState> = combine(
        eventRepository.getAllEvents(),
        _operationState
    ) { events, operationState ->
        println("DEBUG: Flow combine - events count: ${events.size}")
        println("DEBUG: Flow combine - operationState.successMessage: '${operationState.successMessage}'")
        println("DEBUG: Flow combine - operationState.addEventSuccess: ${operationState.addEventSuccess}")
        println("DEBUG: Flow combine - operationState.isAddingEvent: ${operationState.isAddingEvent}")

        val result = operationState.copy(
            events = events,
            isLoading = false
        )

        println("DEBUG: Flow combine - result.successMessage: '${result.successMessage}'")
        println("DEBUG: Flow combine - result.addEventSuccess: ${result.addEventSuccess}")

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = EventUiState(isLoading = true)
    )

    fun addEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            println("DEBUG: addEvent - Starting to add event: ${event.name}")

            _operationState.value = _operationState.value.copy(
                isAddingEvent = true,
                successMessage = null,
                error = null,
                addEventSuccess = false
            )

            val id = eventRepository.insertEvent(event)
            println("DEBUG: addEvent - Repository returned ID: $id")

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

            println("DEBUG: addEvent - State updated with successMessage: '${_operationState.value.successMessage}'")

        } catch (e: Exception) {
            println("DEBUG: addEvent - Exception: ${e.message}")
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
        println("DEBUG: clearError called")
        _operationState.value = _operationState.value.copy(error = null)
    }

    fun clearAddEventSuccess() {
        println("DEBUG: clearAddEventSuccess called")
        _operationState.value = _operationState.value.copy(addEventSuccess = false)
    }

    fun clearSuccessMessage() {
        println("DEBUG: clearSuccessMessage called")
        _operationState.value = _operationState.value.copy(successMessage = null)
    }

    fun testSuccessMessage() {
        println("DEBUG: testSuccessMessage called - setting test message")
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
        println("DEBUG: testSuccessMessage - state updated, successMessage='${_operationState.value.successMessage}'")
    }
}