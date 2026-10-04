package io.github.lucf15.blurkeyboard.ime

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

internal class ImeViewOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val registry = LifecycleRegistry(this)
    private val controller = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry get() = controller.savedStateRegistry
    init {
        controller.performAttach()
        controller.performRestore(null)
        registry.currentState = Lifecycle.State.CREATED
    }
    fun visible(show: Boolean) { registry.currentState = if (show) Lifecycle.State.RESUMED else Lifecycle.State.CREATED }
    fun destroy() { registry.currentState = Lifecycle.State.DESTROYED }
}
