package com.f1rq.lifemap.ui.viewmodel

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
    val successMessage: String? = null
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
        operationState.copy(
            events = events,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventUiState(isLoading = true)
    )

    fun addEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _operationState.value = _operationState.value.copy(isAddingEvent = true)
            val id = eventRepository.insertEvent(event)
            _operationState.value = _operationState.value.copy(
                isAddingEvent = false,
                addEventSuccess = id > 0,
                successMessage = if (id > 0) "Event '${event.name}' added successfully" else null,
                error = if (id <= 0) "Failed to add event" else null
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                isAddingEvent = false,
                error = "Error adding event: ${e.message}"
            )
        }
    }

    fun deleteEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEvent(event)
            _operationState.value = _operationState.value.copy(
                successMessage = "Event '${event.name}' deleted successfully"
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}"
            )
        }
    }

    fun deleteEventById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEventById(id)
            _operationState.value = _operationState.value.copy(
                successMessage = "Event deleted successfully!"
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}"
            )
        }
    }

    fun updateEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.updateEvent(event)
            _operationState.value = _operationState.value.copy(
                successMessage = "Event '${event.name}' updated successfully!"
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to update event: ${e.message}"
            )
        }
    }

    suspend fun getEventById(id: Long): Event? {
        return try {
            eventRepository.getEventById(id)
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to get event: ${e.message}"
            )
            null
        }
    }

    fun getEventsByDate(date: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            // This would need additional state management for filtered events
            // For now, just use the main events flow
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to load events for date: ${e.message}"
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