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
    val addEventSuccess: Boolean = false
)

class EventViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    // ✅ Create MutableStateFlow for operations state
    private val _operationState = MutableStateFlow(
        EventUiState(isLoading = true)
    )

    // ✅ Combine repository data with operation state
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
                error = if (id <= 0) "Failed to save event" else null
            )
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                isAddingEvent = false,
                error = "Error saving event: ${e.message}"
            )
        }
    }

    fun deleteEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEvent(event)
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}"
            )
        }
    }

    fun deleteEventById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.deleteEventById(id)
        } catch (e: Exception) {
            _operationState.value = _operationState.value.copy(
                error = "Failed to delete event: ${e.message}"
            )
        }
    }

    fun updateEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        try {
            eventRepository.updateEvent(event)
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
}