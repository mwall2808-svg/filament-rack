package com.example.thefilamentrack

import android.app.Application
import android.nfc.Tag
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thefilamentrack.data.SpoolEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as FilamentRackApplication).repository

    val allSpools: StateFlow<List<SpoolEntity>> = repo.allSpools.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _nfcTag = MutableSharedFlow<Tag>(extraBufferCapacity = 1)
    val nfcTag: SharedFlow<Tag> = _nfcTag.asSharedFlow()

    fun dispatchNfcTag(tag: Tag) {
        _nfcTag.tryEmit(tag)
    }

    fun addSpool(spool: SpoolEntity) = viewModelScope.launch { repo.insert(spool) }

    fun updateSpool(spool: SpoolEntity) = viewModelScope.launch { repo.update(spool) }

    fun deleteSpool(spool: SpoolEntity) = viewModelScope.launch { repo.delete(spool) }

    fun linkNfcTag(spoolId: Int, tagId: String?) =
        viewModelScope.launch { repo.updateNfcTag(spoolId, tagId) }

    suspend fun getSpoolByTag(tagId: String): SpoolEntity? = repo.getSpoolByTag(tagId)

    suspend fun getSpoolById(id: Int): SpoolEntity? = repo.getSpoolById(id)
}
