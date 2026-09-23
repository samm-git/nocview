package dev.sammgit.nocview.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sammgit.nocview.data.repo.StatusRepository
import dev.sammgit.nocview.data.settings.SettingsStore

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun statusRepository(): StatusRepository
    fun settingsStore(): SettingsStore
}
