package dev.asamorukov.nocview.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.asamorukov.nocview.data.repo.StatusRepository
import dev.asamorukov.nocview.data.settings.SettingsStore

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun statusRepository(): StatusRepository
    fun settingsStore(): SettingsStore
}
